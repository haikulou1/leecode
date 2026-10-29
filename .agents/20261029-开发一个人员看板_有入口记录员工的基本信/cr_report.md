# Code Review Report

> **Change** 「人员看板（员工基本信息 · 增删改查 · 白名单 · 批量导入 · 成本预算）」 · **分支/Commit** `leecode-master` / `task/DEV-807ac241-7ff9-11f1-a7aa-992f2a416e13-5556341c-ecac-4004-81b4-b6b46f8272ae`（base `master`） · **日期** `2026-10-29` · **审查者** AI
>
> **AI**：等级 P0/P1/P2；已先运行 `references/script/scan-all-rules.sh` 并将要点并入 §5。数字编号（P0-1 / P1-1 …）为本次报告的统一问题序号，供 §8 修复任务逐一对应。

---

## 0. 知识库检索留痕（硬性约束）

- 目标库：`mind_线下25`（id `kMXfjjlHQxf6cJAfHpPsI`）
- 已分别执行 `mind content-search -b kMXfjjlHQxf6cJAfHpPsI --query "<需求关键词>" --json --limit 5`（两组关键词）。
- 结果：**已检索、未命中**——`mind` CLI 登录失效（`AUTH_ERROR`，`brd_` token 过期），`content-search`/`list-minds`/`whoami` 均返回认证错误或 500；`mind login` 需浏览器交互，本环境无法完成。已按 JSONL 追加回写 `kb_retrieval.jsonl`（note=「已检索、未命中」）。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 67（主代码 61 + 测试 6） |
| 变更行数 | `+5567 / -0`（`master...HEAD`，全新新增） |
| 跨仓 | `InternetArchitect` 工作区 `git status` 干净，未参与变更，无跨库接口影响 |
| 审查方式 | 静态审查（环境无 `java`/`mvn`，按防超时协议不执行动态构建；已运行 skill 脚本预扫） |

**执行队列（Step 1，逐文件展开，路径省略 `staff-board/src/` 前缀）：**

```
main/java/com/antdigital/staff/StaffBoardApplication.java
main/java/com/antdigital/staff/common/config/{MybatisPlusConfig,TenantContextFilter,AsyncConfig}.java
main/java/com/antdigital/staff/common/constant/StaffConstants.java
main/java/com/antdigital/staff/common/context/StaffContextHolder.java
main/java/com/antdigital/staff/common/enums/{CostType,EmployeeSource,EmployeeStatus,ImportDetailStatus,ImportTaskStatus,StaffErrorCode,WhitelistStatus}Enum.java
main/java/com/antdigital/staff/common/exception/{BusinessException,GlobalExceptionHandler}.java
main/java/com/antdigital/staff/common/model/{ApiResponse,PageQuery,PageResult}.java
main/java/com/antdigital/staff/common/util/{AesCryptUtil,MaskUtil}.java
main/java/com/antdigital/staff/cost/**（controller/dto/entity/mapper/service/impl/vo）
main/java/com/antdigital/staff/employee/**（controller/dto/entity/mapper/service/impl/vo）
main/java/com/antdigital/staff/importbiz/**（config/controller/entity/excel/mapper/processor/service/impl/storage/vo）
main/java/com/antdigital/staff/openapi/controller/StaffOpenApiController.java
main/java/com/antdigital/staff/whitelist/**（controller/dto/entity/mapper/service/impl/vo）
test/java/com/antdigital/staff/**（6 个测试类）
```

- 非 Java 文件：`pom.xml`、`application.yml`、`db/schema.sql`、`docs/**` —— 记 `跳过`（已抽查 schema/application.yml 核对数据与配置契约）。

**上一轮修复闭环确认**：上一轮典型问题已落地——`db/schema.sql:77` 已将其改为 `UNIQUE KEY uk_tenant_file_md5`；`StaffCostServiceImpl.recordBudget:82-98` 已捕获 `DuplicateKeyException` 并重查转 update 覆盖；`AsyncConfig.java` 新增有界 `ThreadPoolTaskExecutor`（`@Async("staffImportExecutor")`）；`StaffImportAsyncWorker.readRows` 已改流式 + 超限即断；`StaffEmployeeServiceImpl.create:60` 已响应 `whitelistEnforced` 开关；`AesCryptUtil` 已切换 AES-GCM + 密钥 fail-fast。本轮复审针对上述修复后的现状，发现 2 处仍存在的 P0 阻塞项与 6 处 P1、6 处 P2。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 2 | 6 | 6 |

---

## 3. Step 2 — 功能（REQ）

> 功能点来源 `[leecode] .agents/20261029-…/design.md` §1 F01–F09 与 §4 接口 W01–W13/O01–O02；REQ 不发明 change 之外功能点。

| REQ | Scenario | 结果 | Spec 证据 | 代码证据（`[leecode] staff-board/`） |
|-----|----------|------|-----------|-----------------------------------|
| F01 录入员工 | 白名单准入 + 工号唯一 + 身份证加密 | ✅ | design §5.1.2 W01 / §5.1.3.1 | `StaffEmployeeServiceImpl.java:58-82`（白名单 60、唯一 63、加密 73） |
| F02 员工查询 | 分页列表 + 详情（脱敏） | ✅ | design §5.1.2 W04/W05 | `StaffEmployeeServiceImpl.java:118-144`（脱敏 221-222） |
| F03 员工修改 | 修改基本信息，empNo 不可变 | ⚠️ | design §5.1.2 W02（入参不含 empNo） | `StaffEmployeeServiceImpl.java:86-98`；`copyToEntity` 未 set empNo，但 `validateRequired` 仍要求 empNo 非空 → 省略 empNo 会误报 `STAFF_EMP_001`（P2-3 关联） |
| F04 员工删除 | 逻辑删除 + 幂等 | ✅ | design §5.1.2 W03 | `StaffEmployeeServiceImpl.java:102-115` |
| F05 导入 | 模板 + 上传 + 结果 | ⚠️ | design §5.3.2 W06/W07/W08 | 模板 `ImportTemplateService.java`、上传 `StaffImportServiceImpl.java:47-92`、结果 `:95-115`；错误报告无下载端点（P1-1） |
| F06 批量导入 | 异步逐行 + 错误报告 + 可重投 | ❌ | design §5.3.3.1（异常场景「可重投」+ 状态机） | `StaffImportAsyncWorker.java:60-128`；FAILED/卡死任务无巡检重投，且 MD5 幂等阻断重试（P0-2） |
| F07 白名单控制 | 维护 + 准入校验 | ✅ | design §5.2.2 / §5.2.3.1 | `StaffWhitelistServiceImpl.java:44-135` |
| F08 成本预算记录 | 记录 + 幂等 upsert | ✅ | design §5.4.2 W09 | `StaffCostServiceImpl.java:50-100`（含 82-98 并发兜底） |
| F09 成本预算汇总 | 列表 + SQL SUM | ✅ | design §5.4.2 W10 | `StaffCostServiceImpl.java:103-139` + `StaffCostBudgetMapper.java:19-27` |
| W01–W13 / O01–O02 | 路径/method 对齐 | ✅ | design §4.1 / §4.2 | 5 个 Controller 逐一核对，无漂移 |

---

## 4. Step 3 — 可读性检查

> 对照 `readability-checklist.md` A1–A7。

| ID | 结果 | 说明 |
|----|------|------|
| A1 源文件格式 | ✅ | 文件名=顶层类名、UTF-8、无 Tab |
| A2 源文件结构 | ✅ | import 分静态/非静态、无 `import *` |
| A3 代码样式 | ✅ | K&R、4 空格缩进、行宽合规 |
| A4 命名规范 | ✅ | DO/DTO/VO/Enum 后缀、常量 UPPER_SNAKE |
| A5 编码实践 | ⚠️ | `StaffImportAsyncWorker.java:147` `doAfterAllAnalysed` 覆写缺 `@Override`（M020，P2） |
| A6 特定元素样式 | ✅ | 无 fall-through/修饰符乱序 |
| A7 Javadoc | ⚠️ | 多数 public 成员有 Javadoc；lombok `@Data` DTO 字段无注释（P2，见 §8） |

---

## 5. Step 4 — 可靠性检查

### 5.1 脚本预扫（`scan-all-rules.sh staff-board/src`，52/222 条）

```
[P0] G16.2 CatchWithoutLogging ×17
[P1] S9.3 WeakCryptoAlgorithm ×1  （StaffImportServiceImpl:133 MD5）
```

**LLM 复核**：17 处 `G16.2` 中，`AesCryptUtil:37/61/82`、`LocalStorageClient:31/42/62` 为「捕获后包装重抛」惯用法（可接受）；`StaffCostServiceImpl:84`、`StaffEmployeeServiceImpl:76`（下一行 78 已 `LOG.warn`）、`StaffImportProcessor:88`（下行 90 已 `LOG.error`）、`StaffImportAsyncWorker:83/121/153`（下行已 `LOG`）为「catch 内后续行已记录」误报。**真实保留**：`StaffEmployeeServiceImpl:181-191`、`StaffImportServiceImpl:81-87`、`:135-137` 冲突/异常分支缺日志 → 计入 P1-5。`S9.3` 的 MD5 仅作 (tenant_id, file_md5) 幂等摘要、非密码学签名，**降级 P2**（建议换 SHA-256）。

### 5.2 可靠性（G）

| 域 | 结果 | 等级 | 说明 |
|----|------|------|------|
| G1 并发控制 | ✅ | — | 员工/白名单/预算唯一键 + `DuplicateKeyException` 兜底，后写覆盖语义明确 |
| G2 幂等拦截 | ❌ | **P0** | 任务级幂等键已唯一，但「FAILED/卡死任务重试入口被幂等吞掉」见 P0-2 |
| G3 事务控制 | ✅ | — | `submitImport` 事务外先落文件后插任务；`@Transactional` 无外部 I/O |
| G4 SQL 与索引 | ✅ | — | 全 `#{}`/LambdaQueryWrapper 参数绑定 + SQL SUM 聚合 |
| G5 消息 / G6 缓存 / G7 调度 | N/A | — | 无 MQ/Redis/定时任务代码（异步用应用内线程池，卡死巡检缺失见 P0-2） |
| G8 防御编程 | ✅→⚠️ | P1 | 有界线程池已配（G8.6 闭环）；文件整块 `getBytes()` 读入内存见 P1-4；ThreadLocal `finally clear()` 已闭环 |
| G9 网络调用 | N/A | — | 无出站 RPC/HTTP |
| G10 接口契约 | ⚠️ | P1 | `fileUrl/errorFileUrl` 为服务器本地绝对路径，前端不可达（P1-1） |
| G11 开发自测 | ⚠️ | P2 | 6 测试类覆盖核心规则，但异步 worker 状态机、并发幂等、文件超限无测试（G11.2） |
| G12 资损 | N/A | — | 预算仅记录、无资金流 |
| G13 监控 | ✅ | — | 业务异常 info、系统异常 error |
| G14 多租户/时区 | ❌ | **P0** | 金额 BigDecimal/十进制合规；但租户 ID 裸信任请求头（P0-1） |
| G15 可灰度 | ✅ | — | `whitelistEnforced`/`enableImport` 开关已生效于录入与导入 |
| G16 可监控 | ⚠️ | P1 | 冲突/异常分支日志缺失（P1-5） |
| G17 可应急 | ⚠️ | P1 | 导入开关可应急；但 FAILED 任务无法重试（同 P0-2） |

### 5.3 安全（S）

| 域 | 结果 | 等级 | 说明 |
|----|------|------|------|
| S1 SQL 注入 | ✅ | — | `#{}`/LambdaQueryWrapper 参数化，无 `${}`（仅 `StaffCostBudgetMapper` 动态 `<if>` 条件，非注入点） |
| S2 XSS / S3 SSRF / S5 XXE | N/A | — | 纯后端 JSON REST |
| S4 命令执行 | ✅ | — | 无 `Runtime.exec`/`ProcessBuilder` |
| S6 反序列化 | ✅ | — | 无 `ObjectInputStream`；EasyExcel 解析受版本依赖治理 |
| S7 文件上传/下载 | ⚠️ | P1 | 10MB multipart 上限生效、`safeName`+UUID 防穿越；但 URL 暴露本地路径（P1-1） |
| S8 访问控制 | ❌ | **P0/P1** | 租户源裸信任（P0-1）；OpenAPI 无签名验签（P1-3） |
| S9 数据安全 | ✅→P2 | — | AES-GCM + fail-fast、日志不落明文、secret 环境变量注入；MD5 幂等摘要降级 P2 |
| S10 CSRF/CORS/跳转 | N/A | — | 无 CORS/跳转 |

### 5.4 Bug 模式（B/M/I）摘要

- 脚本 52 条：除 `G16.2`/`S9.3` 复核结论外无真实命中。
- LLM 补扫：无 `Executors.*`（B008，已用 ThreadPoolTaskExecutor）、无空 catch（M007）、无 `@Transactional` 非 public（B076）、无金额 double 字面量（B010）；`M020` 命中 1 处（P2）；`M016` `parseHireDate` 未显式时区（日期直存本地，P2 并入 §8）；其余 B/M/I 与变更无关，标 N/A。

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | 未启用自定义规则。备注（非正式规则）：Controller 已 `@Valid @RequestBody`，但 DTO 无 JSR-303 注解，`@Valid` 空转（P2，见 §8） |

---

## 7. 结论

- **合并建议**：**修复后合并**（2 个 P0 为安全/可靠性阻塞项，须在上线前闭环）。
- **P0**：
  1. **P0-1** 租户隔离可被伪造（横向越权）——`TenantContextFilter` 仅做格式校验，`X-Tenant-Id` 直接注入上下文参与全部租户查询，无登录态/鉴权承接。
  2. **P0-2** 导入任务状态编排缺陷——`PROCESSING` 卡死无超时巡检/重投，且 FAILED 任务被 (tenant_id, file_md5) 唯一幂等「吞掉」，同文件无法重试，违背 design §5.3.3.1/§6.3 可重试承诺。
- **P1/P2**：6 项 P1、6 项 P2（详见 §7.1 与 §8）。
- **一句话**：上一轮 P0（租户越权、异步租户丢失、AES 弱密钥、并发幂等兜底、无界线程池）已大部修复，功能契约对齐度高、加密与 SQL 参数化到位；剩余 2 个 P0 为「租户源无鉴权」与「导入任务重试闭环缺失」，属合并前必改项。

---

## 7.1 问题片段（必填）

### P0-1 — S8.1 / G14.2 · 租户隔离可伪造（横向越权）
`[leecode] staff-board/src/main/java/com/antdigital/staff/common/config/TenantContextFilter.java:34-45`

```java
L34|        String tenantId = request.getHeader(TENANT_HEADER);
L35|        if (tenantId != null && !TENANT_PATTERN.matcher(tenantId).matches()) {
L36|            LOG.warn("非法租户ID请求被拒绝 tenantId:{}", truncate(tenantId));
L37|            writeReject(response);
L38|            return;
L39|        }
L40|        StaffContextHolder.setTenantId(tenantId);  // 仅格式校验，无鉴权/验签
L41|        try {
L42|            filterChain.doFilter(request, response);
L43|        } finally {
L44|            StaffContextHolder.clear();
L45|        }
```

说明：过滤器只做正则格式校验，未校验调用方身份；`X-Tenant-Id` 客户端任意可控，经 `StaffContextHolder.getTenantId()` 参与所有查询租户条件，构成跨租户越权。注释声称「鉴权由统一网关/登录态拦截器完成」，但本次交付不含该拦截器，`/api/**` 与 `/openapi/**` 均裸暴露。

### P0-2 — G2.1 / G7.2 · 失败/卡死任务不可重试、无巡检重投
`[leecode] staff-board/src/main/java/com/antdigital/staff/importbiz/service/impl/StaffImportServiceImpl.java:60-90`

```java
L60|        String fileMd5 = md5(fileBytes);
L61|        Long existingTaskId = findTaskIdByMd5(tenantId, fileMd5);
L62|        if (existingTaskId != null) {
L63|            return existingTaskId;      // 不区分 SUCCESS/FAILED/PROCESSING，一律返回旧任务
L64|        }
...
L88|        Long taskId = task.getId();
L89|        asyncWorker.processAsync(taskId, tenantId, fileBytes);  // 宕机则任务卡在 PROCESSING
L90|        return taskId;
```

说明：`uk_tenant_file_md5` 唯一键 + `findTaskIdByMd5` 命中即返回，导致「同一文件一旦 FAILED/卡死，重传恒返回旧失败任务 ID」，重试入口被幂等键永久封死；同时异步线程 `markStatus(PROCESSING)` 之后、`updateProgress` 之前宕机时任务永久停留在 `PROCESSING`，无超时巡检/重投兜底。

### P1-1 — S7.3 / G10 · 文件/错误报告 URL 为本地绝对路径且无下载端点
`[leecode] staff-board/src/main/java/com/antdigital/staff/importbiz/storage/LocalStorageClient.java:36-48`

```java
L36|    @Override
L37|    public String upload(InputStream inputStream, String fileName) {
L38|        String uniqueName = UUID.randomUUID() + "_" + safeName(fileName);
L39|        Path target = rootDir.resolve(uniqueName);
L40|        try {
L41|            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
L42|        } catch (IOException ex) {
L43|            throw new UncheckedIOException("存储文件失败", ex);
L44|        }
...
L48|        return target.toString();  // 返回服务器文件系统绝对路径
L49|    }
```

说明：`fileUrl/errorFileUrl` 落库为本地绝对路径，前端既无法下载又泄露服务器目录结构；`StorageClient.download` 无任何 Controller 引用，design W08「可下载错误报告」未闭环。

### P1-2 — 功能 · 导入将已逻辑删除员工静默复活
`[leecode] staff-board/src/main/java/com/antdigital/staff/importbiz/processor/StaffImportProcessor.java:68-71`

```java
L68|        employee.setIdCardNo(null);
L69|        employee.setStatus(1);   // 恒置在职，未校验原删除态
L70|        employee.setSource(StaffConstants.SOURCE_IMPORT);
L71|        employeeService.saveOrUpdateEntity(employee);
```

说明：`saveOrUpdateEntity` 命中已存在员工走 `updateById` 分支，将逻辑删除（status=0）员工静默复活为在职并覆盖来源，与 design F04「逻辑删除保留历史」语义冲突。

---

## 8. 修复任务列表

### P0

- [ ] **P0** `[leecode] staff-board/src/main/java/com/antdigital/staff/common/config/TenantContextFilter.java:34-45` — 接入统一登录态/网关鉴权与租户校验，`X-Tenant-Id` 不得直接信任；在 `/api/**`、`/openapi/**` 前置鉴权拦截器；未接入前视作发布阻断。
- [ ] **P0** `[leecode] staff-board/src/main/java/com/antdigital/staff/importbiz/service/impl/StaffImportServiceImpl.java:60-90` — 幂等命中按任务状态分流（SUCCESS/PARTIAL 返旧任务；FAILED/PROCESSING 卡死允许重投或重建任务）；补充 `PROCESSING` 超时巡检/重投，形成完整状态机闭环。

### P1

- [ ] **P1** `[leecode] staff-board/src/main/java/com/antdigital/staff/importbiz/storage/LocalStorageClient.java:48` — `errorFileUrl/fileUrl` 改为 Web 可访问下载地址，并新增错误报告下载端点（复用 `StorageClient.download`）。
- [ ] **P1** `[leecode] staff-board/src/main/java/com/antdigital/staff/importbiz/processor/StaffImportProcessor.java:69` — 导入前校验员工是否逻辑删除，删除态明文跳过并记失败行，避免静默复活。
- [ ] **P1** S8.1 `[leecode] staff-board/src/main/java/com/antdigital/staff/openapi/controller/StaffOpenApiController.java:25` — 补齐 OpenAPI 签名验签/租户识别（或明确宣告由网关强制实施并在部署文档固化）。
- [ ] **P1** G8 `[leecode] staff-board/src/main/java/com/antdigital/staff/importbiz/controller/StaffImportController.java:43` — 优化 `file.getBytes()` 整文件入内存 + 异步双份驻留（流式/引用传递或临时落盘后仅传路径）。
- [ ] **P1** G16.2 `[leecode] staff-board/.../employee/service/impl/StaffEmployeeServiceImpl.java:181`、`cost/service/impl/StaffCostServiceImpl.java:84`、`importbiz/service/impl/StaffImportServiceImpl.java:81,135` — 唯一键冲突/异常分支补日志，提升排障可观测性。
- [ ] **P1** G11.3 `[leecode] staff-board/src/main/java/com/antdigital/staff/common/exception/GlobalExceptionHandler.java` — 处理 `MaxUploadSizeExceededException` 映射 `STAFF_IMP_002`，使文件超限返回业务错误码而非通用 500。

### P2

- [ ] **P2** M020 `[leecode] staff-board/src/main/java/com/antdigital/staff/importbiz/service/StaffImportAsyncWorker.java:147` — `doAfterAllAnalysed` 补 `@Override`；评估 `RowLimitExceededException` 被 EasyExcel 包装后 `catch` 可能不命中（外层兜底仍安全）。`
- [ ] **P2** S1.3 `[leecode] staff-board/.../employee/service/impl/StaffEmployeeServiceImpl.java:131-132`、`whitelist/service/impl/StaffWhitelistServiceImpl.java:102-103` — `like` 查询前转义 `%`/`_`，避免模糊匹配语义注入。
- [ ] **P2** U1.1 `[leecode] staff-board/.../employee/dto/StaffEmployeeDTO.java`、`whitelist/dto/WhitelistDTO.java`、`cost/dto/CostBudgetDTO.java` — 补 `@NotBlank/@NotNull/@Digits` 等校验注解，或移除形同虚设的 `@Valid`；同时消除 F03 省略 empNo 误报 `STAFF_EMP_001` 的契约矛盾。
- [ ] **P2** G14.3 `[leecode] staff-board/src/main/resources/application.yml:9` — 统一时间存储口径，避免 `serverTimezone=Asia/Shanghai` 与 `LocalDate/LocalDateTime` 混用跨区歧义。
- [ ] **P2** S8.1 `[leecode] staff-board/src/main/java/com/antdigital/staff/common/config/TenantContextFilter.java:51` — `writeReject` 出参结构对齐 `ApiResponse`（`code` 为 Integer），避免错误码通道不一致。
- [ ] **P2** S9.3 `[leecode] staff-board/src/main/java/com/antdigital/staff/importbiz/service/impl/StaffImportServiceImpl.java:133` — 幂等摘要 MD5 建议换 SHA-256 并补充用途注释（非密码学签名场景，仅规避审计误报）。
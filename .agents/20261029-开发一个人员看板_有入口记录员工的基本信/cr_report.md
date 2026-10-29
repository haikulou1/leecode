# Code Review Report

> **Change** 人员看板（staff-board 模块）· **分支/Commit** `task/DEV-807ac241…` / `feef308`（base `c92a4c4`）· **日期** 2026-10-28 · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。已先运行 `scan-all-rules.sh`（52/222 条，输出 13 命中）并入 §5。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| 变更文件总数 | 76（`.java` 66 个：src/main 60 + src/test 6；非 Java 10 个） |
| 变更行数 | `+3857 / -0`（全为新增） |

| 类/接口（代表性） | 路径 | 角色 |
|---------|------|------|
| StaffBoardApplication | staff-board/src/main/java/com/antdigital/staff/StaffBoardApplication.java | 应用入口（@EnableAsync） |
| ApiResponse / PageQuery / PageResult | common/model | 通用出参 / 分页 |
| TenantContextFilter / StaffContextHolder | common/config , common/context | 租户上下文注入与清理 |
| AesCryptUtil / MaskUtil | common/util | 身份证加密 / 手机、身份证脱敏 |
| StaffEmployeeServiceImpl | employee/service/impl | 员工 CRUD（W01–W05） |
| StaffWhitelistServiceImpl | whitelist/service/impl | 白名单维护 + 准入校验（W11–W13） |
| StaffCostServiceImpl | cost/service/impl | 成本预算记录/查询（W09–W10） |
| StaffImportServiceImpl / StaffImportAsyncWorker / StaffImportProcessor | importbiz | 批量导入（W06–W08 / O01） |
| StaffOpenApiController | openapi/controller | 对外 O01/O02 |
| StaffImportController / StaffEmployeeController / StaffCostController / StaffWhitelistController | 各 controller | oneapi 入口 |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 5 | 9 | 6 |

---

## 3. Step 2 — 功能（REQ）

> REQ 来源：`[leecode] .agents/20261029-开发一个人员看板_有入口记录员工的基本信/design.md`（§1 需求功能清单 F01–F09、§4 接口清单、§5.3 导入业务规则）。

### REQ-1: 员工基本信息录用与增删改查（F01–F04 / W01–W05）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 录入员工，白名单准入，工号唯一，身份证加密 | ✅ | design 5.1.2 W01 业务规则 R01–R05 | StaffEmployeeServiceImpl.create:49-66 | 白名单、唯一、加密均已实现 |
| 修改员工（工号不可变） | ⚠️ | W02 入参同 W01，R04 工号不可修改 | StaffEmployeeServiceImpl.update:71-81 | 未校验 name/empNo 非空，`copyToEntity` 会对 dto 空字段错误覆盖（见 P1-3） |
| 逻辑删除（幂等） | ✅ | W03 业务规则 R06–R07 | StaffEmployeeServiceImpl.delete:83-98 | 已删除幂等返回 |
| 详情/列表脱敏 | ✅ | W04/W05 | StaffEmployeeServiceImpl.toVO:182-199 | 手机/身份证脱敏 |

### REQ-2: 白名单控制（F07 / W11–W13）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 新增/复启用/重复拒绝 | ✅ | design 5.2.2 W11 R01–R02 | StaffWhitelistServiceImpl.add:39-64 | 已实现 |
| 准入校验（仅启用态） | ✅ | 5.2.3.1 R01–R02 | StaffWhitelistServiceImpl.checkInWhitelist:106-117 | 已实现 |
| 移除白名单（逻辑停用） | ⚠️ | W13；水平权限 tenant 双重校验 | StaffWhitelistServiceImpl.remove:68-77 | `selectById` 未加租户条件（见 P0-1） |

### REQ-3: 批量导入 + 错误报告（F05/F06 / W06–W08 / O01）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 上传 xlsx、限大小/限行、异步处理 | ⚠️ | W06 R01 文件 10MB/行数 5000 | StaffImportServiceImpl.submitImport:44-69、AsyncWorker:62-65 | 大小依赖 multipart 配置，行数在异步内校验；但提交前无行数上限兜底 |
| 逐行白名单/唯一校验、落明细 | ✅ | 5.3.3.1 R02–R04 | StaffImportProcessor.processRow:47-85 | 已实现 |
| **生成错误报告并回传 errorFileUrl** | ❌ | 5.3.3.1 时序「生成错误报告 → upload → update error_file_url」；W08 出参 errorFileUrl | StaffImportAsyncWorker（通篇）、ImportTaskVO.errorFileUrl | **未实现**：无错误报告生成/上传，error_file_url 恒为 null（P0-3） |
| 重复提交幂等（文件 MD5 + 租户） | ❌ | 5.3.3.1 并发控制「任务级幂等键（文件 MD5 + 租户）」 | StaffImportServiceImpl.submitImport | 未实现 MD5 幂等（未见去重表/字段），同文件重复提交会重复入库（P1-8） |
| 异步任务重试/超时巡检重投 | ❌ | 5.3.3.1 异常场景「消费者宕机超时巡检重试」 | Importbiz 无巡检/重投逻辑 | 未实现（P1-9） |

### REQ-4: 成本预算记录与汇总（F08/F09 / W09–W10）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 员工存在性、金额正数两位小数、幂等 upsert | ✅ | 5.4.2 W09 R01–R03 | StaffCostServiceImpl.recordBudget:47-83 + validateAmount:124-131 | 已实现 |
| 汇总查询（sumAmount） | ⚠️ | W10 aggregate=true 返回汇总 | StaffCostServiceImpl.queryBudget:97-101、sumAmount:143-152 | 应用层拉全量 `selectList` 后再累加，未用 SQL SUM 聚合（P1-6，非数据错误但违背设计「SQL SUM」） |

---

## 4. Step 3 — 可读性检查

对照 `readability-checklist.md` A1–A7：

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ✅ A1 | 文件名/UTF-8/无 Tab（脚本 A1.3 无命中） |
| ⚠️ A2 | `TenantContextFilter.java` 与 `StaffOpenApiController.java` 等 import 未按字典序/未分组（A2.3/A2.4，P2） |
| ✅ A3 | 大括号/缩进/空格基本合规；仅 `StaffCostServiceImpl.java:133` 行宽 >120（脚本 A3.4，P2） |
| ✅ A4 | 命名规范（DO/DTO/VO/Enum 后缀、常量大写）合规 |
| ✅ A5 | @Override 齐全、catch 无非空、静态方法类名调用 |
| ✅ A6 | 无数组反写/无 fall-through/修饰符顺序正确/long 大写 |
| ⚠️ A7 | DO 实体字段、部分 public 方法缺少 Javadoc（A7.1，P2；getter 可豁免 A7.3） |

---

## 5. Step 4 — 可靠性检查

> 预扫 `scan-all-rules.sh staff-board`：13 命中（P0=12、P1=0、P2=1）。其中：`G16.2` 命中 `AesCryptUtil.java:38/53`、`LocalStorageClient.java:31/42/62` 为**脚本误报**（catch 内均 `throw`，异常未吞）；`S1.1` 命中 `pom.xml:39/44/50` 为 `${mybatis-plus.version}` 等 Maven 占位符，**均属于 `#{}`/依赖占位符、非 SQL 拼接**，人工复核为误报。真实命中见下表。

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|-------------------------------------|
| 可靠性 | G1–G17 | ❌ | P0–P2 | G14.2 租户越权/串号（P0），G3.2 事务含 I/O（P1），G1 先读后写无二次校验（P1），G4.3 sumAmount 全量拉取（P1），G7/G17 无巡检/开关未全覆盖（P1），G11 无单测环境验证（P2） |
| 安全 | S1–S10 | ❌ | P0–P1 | S9.1 默认 AES 密钥硬编码（P0），S9.3 ECB 无 IV（P1），S8.1 OpenAPI/鉴权依赖外部网关（P1） |
| Bug 模式 | B/M/I（120） | ❌ | P0–P2 | 预扫命中经复核：真实有效 `G16.4` 空 catch 吞异常（同步）；B-class 其余脚本扫描项**无命中**；LLM 补扫无 B/M/I 结构化缺陷 |

### 5.1 关键问题明细（P0/P1）

- **P0-1｜G14.2/S8.1 租户越权（水平越权）**
  `StaffImportServiceImpl.getTaskResult:72-73` 直接 `taskMapper.selectById(taskId)`；`StaffWhitelistServiceImpl.remove:69` 直接 `whitelistMapper.selectById(whitelistId)`。均未按 `tenant_id` 过滤，跨租户可凭 ID 读取任务结果/删除他人白名单，违反 design §6.4.2「资源操作按 `tenant_id + 资源 ID` 双重校验」。

- **P0-2｜G14.2/G3 租户上下文丢失（异步事务边界）**
  `submitImport` 为 `@Transactional`，先 insert 任务（未提交）再调 `@Async processAsync`；异步线程 `selectById(taskId)` 大概率读到未提交数据返回 null → `StaffContextHolder.setTenantId(null)` → `getTenantId()` 回退为 `"default"`，导致本批员工/预算全部以 `default` 租户落库，真实租户数据丢失。`processAsync` 未把 tenantId 作为参数显式传入。

- **P0-3｜功能缺失 错误报告未实现（REQ F05）**
  design §5.3.3.1 与 W08 明确「生成错误报告并写入 error_file_url」，代码全链路无错误报告生成/上传，`ImportTaskVO.errorFileUrl` 恒为 null。

- **P0-4｜S9.1 默认 AES 密钥硬编码**
  `application.yml:28` 默认 `aes-key: ${STAFF_SECURITY_AES_KEY:c3RhZmYtYm9hcmQta2V5MQ==}`（Base64 解出公开默认值 `staff-board-key1`），`AesCryptUtil:23-24` 未校验密钥长度/未失效启动告警，弱密钥/误用环境可被直接解密身份证号。

- **P0-5｜G16.4 空 catch 吞异常（关键路径无日志）**
  `StaffImportProcessor.processRow:79-81`、`parseHireDate:94-95`、`StaffImportAsyncWorker.processAsync:76-78` 捕获异常后仅拼入返回串/返回 null，无日志无告警，线上排障不可观测。

- **P1-1｜S9.3 加密算法 ECB 无 IV**
  `AesCryptUtil:19` 使用 `AES/ECB/PKCS5Padding`，同明文同密文，身份证号等结构化敏感数据存在模式泄露，建议改 `AES/GCM` 或 `CBC`+随机 IV。

- **P1-2｜G3.2 事务内含外部 I/O**
  `StaffImportServiceImpl.submitImport:54` 在 `@Transactional` 内调用 `storageClient.upload`（文件 I/O），长事务/回滚不回滚文件，建议改为「先落文件 → 再事务写任务」。

- **P1-3｜G11.3 入参校验不完整**
  `StaffEmployeeServiceImpl.update:71-81` 未复用 `validateRequired`，dto 中 name 为空时会以 null 覆盖库内姓名；`StaffEmployeeDTO` 字段缺少 JSR-303 约束注解（仅控制器 `@Valid` 无实质约束）。

- **P1-4｜G1.1 先读后写无二次校验、未转业务错误码**
  `create`/`add`/`saveOrUpdateEntity` 均为「先查后插」，依赖唯一索引兜底但未捕获 `DuplicateKeyException` 转 `STAFF_EMP_002/WL_EMP_EXISTS`，并发场景返回 `STAFF_SYS_001`，违反 design「捕获唯一键冲突幂等返回」。

- **P1-5｜S8.1 OpenAPI 鉴权/操作人缺失**
  `StaffOpenApiController.externalImport:42`、`getByEmpNo:51` 本层无鉴权、无租户、operator 传 null，注释依赖外部网关但代码内无签名验签/租户识别承接（design §6.4.4 要求）。

- **P1-6｜G4.3 汇总全量拉取**
  `StaffCostServiceImpl.sumAmount:143-152` 用 `selectList` 全量 + 应用层累加，违背 design「SQL SUM 聚合」，数据量大时内存/耗时风险。

- **P1-7｜G10.1 null 语义**
  `StaffOpenApiController.getByEmpNo:52-53` 员工不存在返回 `code=0,data=null`，`null` 兼表「无数据」与调用侧歧义。

- **P1-8｜G2.1 幂等缺失（文件级）**
  未实现 design 约定的「文件 MD5 + 租户」任务级幂等，同文件重复提交重复入库。

- **P1-9｜G7/G17 异步可恢复缺失**
  无超时巡检/重投/一键停任务（除 `enable-import` 总开关外），消费者中断后任务滞留 PROCESSING 无自愈。

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | customized-checklist.md U* | N/A | — | 未启用自定义规则（U2 业务红线为空，U1 仅为示例项），整节 `N/A(未启用自定义规则)` |

---

## 7. 结论

- **合并建议**：修复后合并（5 个 P0 阻塞）
- **P0**：
  1. 租户水平越权（getTaskResult / whitelist remove 无 tenant 过滤）
  2. 异步导入租户上下文丢失（提交前读任务 → default 租户串号）
  3. 错误报告未实现（error_file_url 恒 null）
  4. 默认 AES 密钥硬编码（S9.1）
  5. 关键路径空 catch 吞异常无日志（G16.4）
- **P1/P2**：见 §5.1 P1-1～P1-9 及 §4 A2/A3/A7（P2）。
- **一句话**：功能主体（员工 CRUD、白名单、成本预算、异步导入）骨架齐全，但存在**租户隔离与越权**类 P0 隐患，且**错误报告**这一明确交付物缺失，需修复后再合并。

---

## 7.1 问题片段（必填）

- **P0-1** `G14.2` `staff-board/src/main/java/com/antdigital/staff/importbiz/service/impl/StaffImportServiceImpl.java:72` — 查询任务结果未按 tenant 过滤，跨租户越权。
  片段范围：`StaffImportServiceImpl.java:71-76`

```java
L71|    public ImportTaskVO getTaskResult(Long taskId) {
L72|        StaffImportTaskDO task = taskMapper.selectById(taskId);
L73|        if (task == null) {
L74|            throw new BusinessException(StaffErrorCode.IMP_TASK_NOT_FOUND);
L75|        }
L76|        ...
```

```java
L68|    public void remove(Long whitelistId) {
L69|        StaffWhitelistDO existing = whitelistMapper.selectById(whitelistId);
L70|        if (existing == null) {
L71|            return;
L72|        }
```
（`StaffWhitelistServiceImpl.java:68-72`，同属 P0-1）

- **P0-2** `G14.2` `staff-board/src/main/java/com/antdigital/staff/importbiz/service/StaffImportAsyncWorker.java:52-54` — 异步线程依赖未提交的事务读 task，tenantId 回退 default。
  片段范围：`StaffImportAsyncWorker.java:50-55`

```java
L50|    @Async
L51|    public void processAsync(Long taskId, byte[] fileBytes) {
L52|        StaffImportTaskDO task = taskMapper.selectById(taskId);
L53|        String tenantId = task != null ? task.getTenantId() : null;
L54|        StaffContextHolder.setTenantId(tenantId);
L55|        try {
```

- **P0-3** `StaffImportAsyncWorker.java` — 错误报告生成/上传缺失。
  片段范围：`StaffImportAsyncWorker.java:96-110`（处理完成后仅 update 状态，无 error_file 上传）

```java
L96|                int status = ImportTaskStatusEnum.SUCCESS.getCode();
L97|                if (fail > 0 && success > 0) {
...
L103|                updateProgress(taskId, rows.size(), success, fail, status);
L104|            } catch (Exception ex) {
L105|                LOG.error("导入任务处理失败 taskId:{}", taskId, ex);
L106|                markStatus(taskId, ImportTaskStatusEnum.FAILED.getCode());
L107|            }
```
（无 error_file_url 写入）

- **P0-4** `S9.1` `staff-board/src/main/resources/application.yml:28` — 默认 AES 密钥硬编码。
  片段范围：`application.yml:25-28`

```yaml
L25|staff:
L26|  security:
L27|    # 开发默认 AES-128 密钥（Base64）；生产环境必须注入 STAFF_SECURITY_AES_KEY
L28|    aes-key: ${STAFF_SECURITY_AES_KEY:c3RhZmYtYm9hcmQta2V5MQ==}
```

- **P0-5** `G16.4` `staff-board/src/main/java/com/antdigital/staff/importbiz/processor/StaffImportProcessor.java:79` — 空 catch 吞异常无日志。
  片段范围：`StaffImportProcessor.java:69-83`

```java
L69|        if (StringUtils.hasText(row.getCostType()) && row.getCostAmount() != null) {
L70|            StaffEmployeeDO saved = employeeService.getEntityByEmpNo(row.getEmpNo());
L71|            try {
...
L78|                costService.recordBudget(budgetDto);
L79|            } catch (Exception ex) {
L80|                // 预算行失败仅影响本行预算，不影响员工信息落库（遵循设计 R05）
L81|                return "成本预算写入失败: " + ex.getMessage();
L82|            }
L83|        }
```

```java
L92|        try {
L93|            return LocalDate.parse(hireDate, DATE_FORMATTER);
L94|        } catch (Exception ex) {
L95|            return null;
L96|        }
```
（`StaffImportProcessor.java:92-96`）

---

## 8. 修复任务列表

### P0

- [ ] **P0** `staff-board/src/main/java/com/antdigital/staff/importbiz/service/impl/StaffImportServiceImpl.java:72` — `getTaskResult` 增加 `tenant_id` 过滤（用 `LambdaQueryWrapper` 双条件）。
- [ ] **P0** `staff-board/src/main/java/com/antdigital/staff/whitelist/service/impl/StaffWhitelistServiceImpl.java:69` — `remove` 增加 `tenant_id` 过滤。
- [ ] **P0** `staff-board/src/main/java/com/antdigital/staff/importbiz/service/StaffImportAsyncWorker.java:52` — `processAsync` 增加显式 `tenantId` 入参并在任务落库前由 `submitImport` 传入，避免依赖未提交事务读任务。
- [ ] **P0** `staff-board/src/main/java/com/antdigital/staff/importbiz/service/StaffImportAsyncWorker.java` — 补齐错误报告生成（汇总失败行）与上传，回写 `error_file_url`。
- [ ] **P0** `staff-board/src/main/resources/application.yml:28` 与 `AesCryptUtil.java:23` — 移除默认密钥、强制注入并校验密钥长度（16/24/32 字节），缺失时启动即失败并告警。
- [ ] **P0** `staff-board/src/main/java/com/antdigital/staff/importbiz/processor/StaffImportProcessor.java:79,94` 与 `StaffImportAsyncWorker.java:76` — 捕获异常补充 `LOG.error`（含 taskId/empNo/bizId 上下文）或抛出，禁止静默吞异常。

### P1

- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/common/util/AesCryptUtil.java:19` — 改 `AES/GCM/NoPadding`（或 CBC+随机 IV），替换 ECB。
- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/importbiz/service/impl/StaffImportServiceImpl.java:54` — 将 `storageClient.upload` 移出事务，先落文件再事务写入任务。
- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/employee/service/impl/StaffEmployeeServiceImpl.java:71` — `update` 复用必填校验，避免空字段覆盖既有姓名。
- [ ] **P1** `StaffEmployeeServiceImpl.create / StaffWhitelistServiceImpl.add / saveOrUpdateEntity` — 捕获唯一键冲突并转 `STAFF_EMP_002`/`STAFF_WL_EMP_002`。
- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/openapi/controller/StaffOpenApiController.java:41,49` — 补齐/承接签名验签、租户识别与 operator 注入。
- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/cost/service/impl/StaffCostServiceImpl.java:143` — `sumAmount` 改 SQL `SELECT COALESCE(SUM(amount),0)` 聚合。
- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/openapi/controller/StaffOpenApiController.java:52` — 员工不存在返回明确错误码，避免 `data=null` 语义歧义。
- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/importbiz/service/impl/StaffImportServiceImpl.java:44` — 实现「文件 MD5 + tenant」任务级幂等，防重复提交。
- [ ] **P1** `staff-board/importbiz` — 增加 PROCESSING 超时巡检/重投或一键停任务能力。

### P2（可选）

- [ ] **P2** `staff-board/src/main/java/com/antdigital/staff/common/config/TenantContextFilter.java`、`openapi/controller/StaffOpenApiController.java` — 规范 import 顺序（A2.3/A2.4）。
- [ ] **P2** `staff-board/src/main/java/com/antdigital/staff/cost/service/impl/StaffCostServiceImpl.java:133` — 控制行宽 ≤120（A3.4）。
- [ ] **P2** 补齐 DO 实体字段与 public 方法 Javadoc（A7.1）。
# Code Review Report

> **Change** 人员看板（员工基本信息 + 增删改查 + 白名单 + 批量导入 + 成本预算） · **分支/Commit** `task/DEV-807ac241-...`（base `master`） · **日期** 2026-10-29 · **审查者** AI
>
> **AI**：等级 P0 / P1 / P2；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。已先运行 `scan-all-rules.sh` 并将要点并入 §5。本轮为「问题修复」后的复审。

---

## 0. 知识库检索留痕（硬性约束）

- 目标库：`mind_线下25`（id `kMXfjjlHQxf6cJAfHpPsI`）
- 命令：`mind content-search -b kMXfjjlHQxf6cJAfHpPsI --query "人员看板 员工基本信息 增删改查 导入 成本预算 白名单" --json --limit 5`
- 结果：**已检索、未命中**——`mind` CLI 登录失效（`AUTH_ERROR`，`brd_` token 过期），`content-search`/`list-minds`/`whoami` 均返回认证错误或 500；`mind login` 需浏览器交互，本环境无法完成。
- 已按 JSONL 追加回写 `kb_retrieval.jsonl`（note=「已检索、未命中」）。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 68（含 6 个测试类） |
| 变更行数 | `+5481 / -0`（79 个文件，全新新增） |
| 审查方式 | 静态审查（环境无 `java`/`mvn`，按防超时协议跳过动态构建） |

**执行队列（Step 1，逐文件展开，路径省略 `staff-board/src/` 前缀）：**

```
main/java/com/antdigital/staff/StaffBoardApplication.java
main/java/com/antdigital/staff/common/config/MybatisPlusConfig.java
main/java/com/antdigital/staff/common/config/TenantContextFilter.java
main/java/com/antdigital/staff/common/constant/StaffConstants.java
main/java/com/antdigital/staff/common/context/StaffContextHolder.java
main/java/com/antdigital/staff/common/enums/{CostType,EmployeeSource,EmployeeStatus,ImportDetailStatus,ImportTaskStatus,StaffErrorCode,WhitelistStatus}Enum.java
main/java/com/antdigital/staff/common/exception/{BusinessException,GlobalExceptionHandler}.java
main/java/com/antdigital/staff/common/model/{ApiResponse,PageQuery,PageResult}.java
main/java/com/antdigital/staff/common/util/{AesCryptUtil,MaskUtil}.java
main/java/com/antdigital/staff/cost/** (controller/dto/entity/mapper/service/impl/vo) 共 8 文件
main/java/com/antdigital/staff/employee/** (controller/dto/entity/mapper/service/impl/vo) 共 7 文件
main/java/com/antdigital/staff/importbiz/** (config/controller/entity/excel/mapper/processor/service/impl/storage/vo) 共 16 文件
main/java/com/antdigital/staff/openapi/controller/StaffOpenApiController.java
main/java/com/antdigital/staff/whitelist/** (controller/dto/entity/mapper/service/impl/vo) 共 7 文件
test/java/com/antdigital/staff/** 共 6 测试类
```

- 非 Java 文件：`pom.xml`、`application.yml`、`db/schema.sql`、`docs/**` —— 记 `跳过`（抽查 schema/application.yml 核对数据与配置契约）。

**上一轮（问题修复阶段）修复闭环确认：上一轮 P0-1~P0-5、P1-2/P1-4/P1-5 已修复**（tenant 过滤、async tenantId 入参、错误报告生成、AES-GCM、事务外文件 I/O、唯一键冲突转业务码、SQL SUM 聚合、O02 明确错误码、文件 MD5 幂等字段）。本轮复审发现 2 处未真正落地的幂等硬约束与 4 处 P1。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 2 | 4 | 6 |

---

## 3. Step 2 — 功能（REQ）

> 功能点来源于 `design.md` §1 F01–F09。契约核对：13 oneapi + 2 openapi 路径/method 一致；5 表 + 唯一索引对齐；金额 decimal(14,2)；`InternetArchitect` 无改动。

| REQ | Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|----------|------|----------|----------|------|
| REQ-F01/F02 | 录入员工（白名单+工号唯一+身份证加密） | ✅ | design§5.1.2 W01 | `StaffEmployeeServiceImpl.java:50-72` | 校验/白名单/唯一键兜底/加密齐全 |
| REQ-F03 | 修改员工（工号不可变） | ✅ | design§5.1.2 W02 | `StaffEmployeeServiceImpl.java:76-89` | `copyToEntity` 不含 empNo |
| REQ-F04 | 逻辑删除（幂等） | ✅ | design§5.1.2 W03 | `StaffEmployeeServiceImpl.java:92-106` | 状态置 LEAVE，幂等 |
| REQ-F02b | 详情/列表脱敏 | ✅ | design§5.1.2 W04/W05 | `StaffEmployeeServiceImpl.java:202-219` | AES 解密后脱敏 |
| REQ-F05/F06 | 批量导入 + 错误报告 | ✅ | design§5.3.3.1 | `StaffImportServiceImpl.java:47-92`、`StaffImportAsyncWorker.java` | 上传→异步逐行→明细→错误报告已实现 |
| REQ-F07 | 白名单准入 | ✅ | design§5.2.3.1 | `StaffWhitelistServiceImpl.java:120-130` | 录入/导入均调用 |
| REQ-F08/F09 | 成本预算记录 + SQL 汇总 | ✅ | design§5.4.2 W09/W10 | `StaffCostServiceImpl.java:49-122`、`StaffCostBudgetMapper.java:19-27` | SQL SUM 聚合已实现 |
| REQ-幂等 | 同维度预算并发「后写覆盖」 | ❌ | design§5.4.3.1「唯一键 + upsert 原子写」 | `StaffCostServiceImpl.java:60-81` | 先查后插，insert 无唯一键冲突捕获，并发冲突转 500 |
| REQ-幂等 | 导入任务防重复提交 | ❌ | design§5.3.3.1「文件 MD5 + 租户防重复」 | `StaffImportServiceImpl.java:60-87` + `schema.sql:77` | 幂等键仅普通索引非 UNIQUE，并发同文件双任务 |

---

## 4. Step 3 — 可读性检查

| 结果 | 说明 |
|------|--------------------------------|
| ✅ | A1 源文件格式 / A2 结构 / A4 命名 / A5 编码实践 / A6 特定元素样式 通过；脚本 A1.3/A2.2/A3.4/A3.7/A4.1/A5.4/A6.3/A6.5 均无命中 |
| ⚠️ | A7.1：DTO/VO 的 lombok `@Data` public 字段无字段注释（`StaffEmployeeDTO.java:17-34` 等，P2） |
| ⚠️ | `PageQuery.offset()`（`PageQuery.java:44-46`）int 溢出隐患且为死代码（分页走 MyBatis-Plus `Page`）（P2） |

---

## 5. Step 4 — 可靠性检查

### 5.1 脚本预扫（`scan-all-rules.sh`，52/222）

```
[P0] G16.2 CatchWithoutLogging ×15 —— 人工复核：全部为误报（catch 内均为 throw 包装重抛或下方紧跟 LOG.error/warn）
[P1] S9.3 WeakCryptoAlgorithm ×1（StaffImportServiceImpl:132 MD5）—— 用作文件幂等摘要，非密码学场景，降级 P2
```

### 5.2 可靠性（G）

| 域 | 结果 | 等级 | 说明 |
|----|------|------|------|
| G1 并发控制 | ❌ | P0 | `recordBudget` 先查后插无 `FOR UPDATE`/唯一键冲突兜底（G1.1） |
| G2 幂等拦截 | ❌ | P0 | 导入任务幂等依赖普通索引非唯一键（G2.1） |
| G3 事务控制 | ✅ | — | `submitImport` 无 `@Transactional`，文件 I/O 与任务插入分离 |
| G4 SQL与索引 | ✅ | — | 全 `#{}`/LambdaQueryWrapper 参数绑定 + SQL SUM |
| G5 MQ / G6 缓存 / G7 调度 | N/A | — | 无 MQ/Redis/定时任务代码 |
| G8 防御编程 | ❌ | P1 | `@Async` 无自定义 `TaskExecutor`，默认无界 SimpleAsyncTaskExecutor（G8.6） |
| G9 网络调用 | N/A | — | 无出站 RPC/HTTP |
| G10 接口契约 | ✅ | — | 统一 `{code,msg,errorCode,userTip,data}`，null 语义已用错误码区分 |
| G11 开发自测 | ⚠️ | P2 | 6 测试类覆盖核心规则，但并发幂等/异步 worker/校验边界无测试（G11.2） |
| G12 资损 | N/A | — | 预算仅记录不付款 |
| G13 监控 | ✅ | — | 业务异常 info、系统异常 error，级别正确 |
| G14 多租户/时区 | ⚠️ | P1 | 查询均注入 tenant_id + 金额 BigDecimal；但租户 ID 信任请求头（见 S8） |
| G15 可灰度 | ⚠️ | P1 | `whitelistEnforced`/`enableImport` 开关已配，但 `create` 不响应 whitelistEnforced |
| G16 可监控 | ✅ | — | 核心路径日志齐全 |
| G17 可应急 | ⚠️ | P1 | 开关对录入路径失效（同 G15） |

### 5.3 安全（S）

| 域 | 结果 | 等级 | 说明 |
|----|------|------|------|
| S1 SQL 注入 | ✅ | — | 参数化 + `#{}`，脚本 S1.1 无 `${}` 命中 |
| S2 XSS/S3 SSRF/S5 XXE | N/A | — | 纯后端 JSON REST |
| S4 命令执行 | ✅ | — | 无 `Runtime.exec`/`ProcessBuilder` |
| S6 反序列化 | ✅ | — | 无 `ObjectInputStream` |
| S7 文件上传/下载 | ⚠️ | P1 | 10MB multipart 上限生效；safeName+UUID 防穿越；`download` 按 fileUrl 直接 `Paths.get`（当前仅内部生成，风险低） |
| S8 访问控制 | ❌ | P1 | 鉴权仅注释声明，本仓无实现；`X-Tenant-Id` 被无条件信任（S8.1） |
| S9 数据安全 | ⚠️ | P1/P2 | AES-GCM + fail-fast（通过）；日志不落明文（通过）；MD5 仅幂等摘要（P2）；secret 环境变量注入（通过） |
| S10 CSRF/CORS | N/A | — | 无 CORS/跳转 |

### 5.4 Bug 模式（B/M/I）摘要

- 脚本 52 条：除 §5.1 MD5 与「包装重抛误报」外无真实命中。
- LLM 补扫：无 `Executors.*`（B008 间接相关并入 G8.6）；`M016` `parseHireDate` 用 ISO_LOCAL_DATE 未显式时区（日期直存，风险低，P2）；无空 catch（M007）；金额无 double 字面量构造（B010）；其余 B/M/I 与变更无关，标 N/A（无该场景）。

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | 未启用自定义规则。备注（非正式规则）：Controller 已用 `@Valid @RequestBody`，但 DTO 无 JSR-303 注解，`@Valid` 空转（P2，见 §7.1） |

---

## 7. 结论

- **合并建议**：修复后合并（2 个 P0 为幂等/并发正确性缺陷，须先修）
- **P0**：
  1. `StaffCostServiceImpl.recordBudget` 并发幂等兜底缺失（G1.1）
  2. 导入任务幂等键未落唯一约束（G2.1）
- **P1/P2**：`@Async` 无界线程池、鉴权/租户信任请求头、`create` 不响应白名单开关、导入全量驻留内存后校验、DTO 校验注解、死代码等。
- **一句话**：上一轮 P0 已大部修复（租户越权/异步租户丢失/错误报告/AES 弱密钥等），功能契约对齐度高、加密与 SQL 参数化到位；剩余 2 个 P0 均为「幂等硬约束未落地」，属合并前必改项。

---

## 7.1 问题片段（必填）

- **P0** `G1.1` `staff-board/src/main/java/com/antdigital/staff/cost/service/impl/StaffCostServiceImpl.java:60-82` — 先查后插无唯一键冲突兜底，并发同维度写抛 DuplicateKeyException → 500。
  片段范围：`StaffCostServiceImpl.java:47-83`

```java
L47|    @Override
L48|    @Transactional(rollbackFor = Exception.class)
L49|    public Long recordBudget(CostBudgetDTO dto) {
...
L60|        StaffCostBudgetDO existing = findByDimension(tenantId, dto.getEmployeeId(), dto.getYear(),
L61|                dto.getMonth(), dto.getCostType());
L62|        if (existing != null) {
...
L71|        StaffCostBudgetDO entity = new StaffCostBudgetDO();
...
L80|        entity.setRemark(dto.getRemark());
L81|        costBudgetMapper.insert(entity);   // ← 问题：并发同维度冲突未捕获
L82|        return entity.getId();
L83|    }
```

- **P0** `G2.1` `staff-board/src/main/java/com/antdigital/staff/importbiz/service/impl/StaffImportServiceImpl.java:60-87` + `db/schema.sql:77` — 幂等键 `(tenant_id, file_md5)` 仅普通索引 `KEY idx_tenant_file_md5`，无 `UNIQUE`，并发同文件双任务且 `DuplicateKeyException` 兜底不可达。
  片段范围：`StaffImportServiceImpl.java:60-87`

```java
L60|        String fileMd5 = md5(fileBytes);
L61|        Long existingTaskId = findTaskIdByMd5(tenantId, fileMd5);
L62|        if (existingTaskId != null) {
L63|            return existingTaskId;
L64|        }
L67|        String fileUrl = storageClient.upload(new ByteArrayInputStream(fileBytes), fileName);
L69|        StaffImportTaskDO task = new StaffImportTaskDO();
...
L79|        try {
L80|            taskMapper.insert(task);
L81|        } catch (DuplicateKeyException ex) {   // ← 普通索引下不可达
L82|            Long raceTaskId = findTaskIdByMd5(tenantId, fileMd5);
L83|            if (raceTaskId != null) {
L84|                return raceTaskId;
L85|            }
L86|            throw new BusinessException(StaffErrorCode.SYSTEM_ERROR, "重复导入任务冲突");
L87|        }
```

- **P1** `G8.6` `StaffImportAsyncWorker.java:58-59` — `@Async` 无自定义 `TaskExecutor`，默认无界线程池。
- **P1** `S8.1` `TenantContextFilter.java:31-32` — 裸信任 `X-Tenant-Id`，无鉴权承接。
- **P1** `G15.3` `StaffEmployeeServiceImpl.java:52` — `create` 不响应 `whitelistEnforced` 开关。
- **P1** `G8` `StaffImportAsyncWorker.java:64-71` — 全量 `rows::addAll` 读入内存后才校验 maxRows，超大文件 OOM 风险。
- **P2** `StaffEmployeeServiceImpl.java:68`、`StaffWhitelistServiceImpl.java:65` — catch 转化业务无日志且部分丢弃 root cause。
- **P2** `StaffImportServiceImpl.java:132` — MD5 用途未注释，或改用 SHA-256。
- **P2** `StaffEmployeeDTO.java` / `WhitelistDTO.java` / `CostBudgetDTO.java` — DTO 无 JSR-303 注解，`@Valid` 空转。
- **P2** `PageQuery.java:44-46` — `offset()` 溢出隐患 + 死代码。
- **P2** `StaffImportProcessor.java:73` / `StaffImportAsyncWorker.java:94` — 部分预算字段无提示；`rawData` 恒 null。

---

## 8. 修复任务列表

### P0

- [ ] **P0** `staff-board/src/main/java/com/antdigital/staff/cost/service/impl/StaffCostServiceImpl.java:49-83` — `recordBudget` 增加同维度唯一键冲突兜底（`ON DUPLICATE KEY UPDATE` 或捕获 `DuplicateKeyException` 转 update 覆盖），使并发写「后写覆盖」而非 500。
- [ ] **P0** `staff-board/src/main/resources/db/schema.sql:77` — 将 `KEY idx_tenant_file_md5` 改为 `UNIQUE KEY uk_tenant_file_md5 (tenant_id, file_md5)`，落实任务级幂等。

### P1

- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/importbiz/service/StaffImportAsyncWorker.java:58` — 配置有界 `ThreadPoolTaskExecutor`（核心/最大线程 + 有界队列 + 拒绝策略）。
- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/common/config/TenantContextFilter.java:31` — 增加登录态/鉴权拦截，禁止裸信任 `X-Tenant-Id`。
- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/employee/service/impl/StaffEmployeeServiceImpl.java:52` — 让 `create` 响应 `whitelistEnforced` 开关。
- [ ] **P1** `staff-board/src/main/java/com/antdigital/staff/importbiz/service/StaffImportAsyncWorker.java:64-71` — 逐行流式解析并超限即断，避免全量驻留内存。

### P2（可选）

- [ ] **P2** `StaffEmployeeServiceImpl.java:68` / `StaffWhitelistServiceImpl.java:65` — catch 分支补日志并保留 root cause。
- [ ] **P2** `StaffImportServiceImpl.java:132` — 注释明确 MD5 仅作幂等摘要，或改用 SHA-256。
- [ ] **P2** `StaffEmployeeDTO.java` 等 DTO — 补 `@NotBlank/@NotNull/@Digits`，让 `@Valid` 生效。
- [ ] **P2** `PageQuery.java:44-46` — 删除或修正 `offset()` 溢出隐患。
- [ ] **P2** `StaffImportProcessor.java:73` — 部分预算字段缺失时返回行级失败提示。
- [ ] **P2** `StaffImportAsyncWorker.java:94` — `raw_data` 落原始行 JSON 或删除该列。
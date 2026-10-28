# staff-board 模块架构（ARCHITECTURE.md）

## 模块边界与依赖方向

```
common <- employee <- (whitelist, cost)
common <- importbiz（依赖 employee / whitelist / cost）
openapi -> employee / importbiz
```

| 模块 | 职责 | 不得包含 |
|------|------|----------|
| common | 统一响应、分页、错误码、枚举、异常、脱敏/加解密、上下文、MyBatis 配置 | 具体业务表读写 |
| employee | 员工基本信息录入/查询/修改/逻辑删除 | 导入解析、预算聚合 |
| whitelist | 白名单维护与准入校验 | 员工明细业务 |
| cost | 成本预算记录与汇总 | 员工基础信息维护 |
| importbiz | 模板下载、文件解析、异步批量导入、任务明细 | 绕过白名单/员工服务直接写库 |
| openapi | 对外 REST 契约（导入、按工号查询） | DB 访问、业务计算 |

## 配置分层

- `application.yml` 提供缺省值；生产敏感配置（数据库口令、AES 密钥）通过环境变量注入，禁止写入配置文件。
- 关键环境变量：`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`STAFF_SECURITY_AES_KEY`、`STAFF_IMPORT_MAX_ROWS`、`STAFF_WHITELIST_ENFORCED`、`STAFF_IMPORT_ENABLED`。

## 错误处理

- 统一出参 `{ code, msg, errorCode, userTip, data }`，code=0 成功。
- 业务异常统一 `BusinessException`（携带 STAFF_xxx 错误码），`GlobalExceptionHandler` 兜底转换为 HTTP 响应。
- 错误码前缀：`STAFF_EMP_`（员工）、`STAFF_IMP_`（导入）、`STAFF_COST_`（成本）、`STAFF_WL_`（白名单）、`STAFF_` 通用。

## 安全约束

- 身份证号 AES 加密存储、展示脱敏；手机号脱敏展示。
- 日志使用 SLF4J 占位符，禁止输出 PII 明文。
- SQL 一律走 MyBatis-Plus 参数化（LambdaQueryWrapper），禁止字符串拼接。
- 租户 ID 从请求头 `X-Tenant-Id` 注入线程上下文（生产由统一登录态拦截器覆盖），跨租户越权由 `tenant_id` 过滤兜底。

## 跨库接口契约（向后兼容）

- 全部为新增接口/表，不修改既有契约。
- 成本类型采用字符串枚举，新增类型无需改表结构。
- 表结构遵循「新增列不删除、不缩窄」，保证新旧版本兼容。

## 测试策略

- Service / Processor / 工具类：单元测试 + Mockito（`@ExtendWith(MockitoExtension.class)`）。
- 环境无 JDK/Maven：编译/单测执行降级为静态审查（L1），待人工环境执行 `mvn compile` / `mvn test`。
# openapi 模块（对外接口）

## 职责

对外可信系统的批量导入与按工号查询契约（O01-O02）。签名验签/租户识别由统一网关完成。

## 关键类

| 类 | 职责 |
|----|------|
| StaffOpenApiController | O01-O02 openapi 接口 |

## API 接口

- POST /openapi/staff/employee/import（外部批量导入）
- GET /openapi/staff/employee/{empNo}（按工号查询，脱敏）
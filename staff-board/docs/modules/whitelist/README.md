# whitelist 模块（白名单准入）

## 职责

员工准入白名单的维护（新增/停用/分页）与录入/导入时的准入校验。

## 关键类

| 类 | 职责 |
|----|------|
| StaffWhitelistController | W11-W13 oneapi 接口 |
| StaffWhitelistService / Impl | 白名单增删查 + checkInWhitelist 准入校验 |
| StaffWhitelistMapper | 数据访问 |

## 依赖

- 向下依赖：common
- 不得依赖：employee / importbiz / cost

## API 接口

- POST /api/staff/whitelist（新增）
- POST /api/staff/whitelist/list（分页）
- DELETE /api/staff/whitelist/{whitelistId}（停用）

## 表

`staff_whitelist`（唯一键 uk_tenant_emp_no）。
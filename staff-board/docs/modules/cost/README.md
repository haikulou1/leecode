# cost 模块（成本预算）

## 职责

按「员工 × 年度 × 月份 × 成本类型」记录成本预算（幂等覆盖），支持分页与金额汇总。

## 关键类

| 类 | 职责 |
|----|------|
| StaffCostController | W09-W10 oneapi 接口 |
| StaffCostService / Impl | 预算记录（幂等 upsert）+ 查询汇总 |
| StaffCostBudgetMapper | 数据访问 |

## 依赖

- 向下依赖：common、employee（员工存在性校验）
- 不得依赖：importbiz、whitelist

## API 接口

- POST /api/staff/cost/budget（记录）
- POST /api/staff/cost/budget/list（分页/汇总）

## 表

`staff_cost_budget`（唯一键 uk_emp_year_month_type）。
# employee 模块（员工信息）

## 职责

员工基本信息的录入（含白名单准入）、查询列表/详情、修改、逻辑删除；敏感字段加密存储与脱敏展示。

## 关键类

| 类 | 职责 |
|----|------|
| StaffEmployeeController | W01-W05 oneapi 接口 |
| StaffEmployeeService / Impl | 员工 CRUD 业务编排、白名单准入、加密与脱敏 |
| StaffEmployeeMapper | MyBatis-Plus 数据访问 |
| StaffEmployeeDO / DTO / VO | 数据对象 / 请求 / 脱敏展示对象 |

## 依赖

- 向下依赖：common（错误码/枚举/加解密/脱敏/上下文）、whitelist（准入校验）
- 不得依赖：importbiz、cost

## API 接口

- POST /api/staff/employee（录入）
- PUT /api/staff/employee/{employeeId}（修改）
- DELETE /api/staff/employee/{employeeId}（逻辑删除）
- GET /api/staff/employee/{employeeId}（详情）
- POST /api/staff/employee/list（分页）

## 表

`staff_employee`（唯一键 uk_tenant_emp_no）。
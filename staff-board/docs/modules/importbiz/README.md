# importbiz 模块（批量导入）

## 职责

员工信息 Excel 模板下载、文件上传入库、异步解析与逐行导入（白名单准入 + 员工 upsert + 成本预算记录）、任务结果查询。

## 关键类

| 类 | 职责 |
|----|------|
| StaffImportController | W06-W08 oneapi 接口 |
| StaffImportService / Impl | 提交任务、结果查询 |
| StaffImportAsyncWorker | 异步解析与逐行处理（避免 self-invocation） |
| StaffImportProcessor | 单行业务规则（白名单/员工/预算） |
| ImportTemplateService | 生成 xlsx 模板 |
| StorageClient / LocalStorageClient | 文件存储抽象（可切换 OSS） |
| ImportProperties | 导入开关/行数上限/白名单强校验配置 |

## 依赖

- 向下依赖：common、employee、whitelist、cost
- 不得依赖：openapi

## API 接口

- POST /api/staff/employee/import（批量导入）
- GET /api/staff/employee/import/template（模板下载）
- GET /api/staff/employee/import/task/{taskId}（任务结果）

## 表

`staff_import_task`、`staff_import_detail`。
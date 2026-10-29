# PRD：图书管理系统 v1.0

> **文档状态**：已发布  
> **创建日期**：2026-10-28  
> **版本**：1.0.0  
> **适用范围**：小型图书馆、企业图书室、学校班级图书角

---

## 1. 产品概述

### 1.1 产品定位

构建一个基于 Web 的简易图书管理系统，支持图书的增删改查、借阅归还以及基础的读者管理。面向单馆小型场景（千级读者、万级藏书），以操作简洁和数据一致性为核心目标。

### 1.2 用户角色

| 角色 | 权限描述 | 产生方式 |
|------|----------|----------|
| **管理员** | 图书录入/下架、读者管理、借阅记录查看、逾期管理 | 系统预置种子账号（首次登录强制修改密码） |
| **普通读者** | 图书检索、借阅/归还、查看个人借阅历史 | 通过注册页面自助注册 |

### 1.3 技术方案

| 维度 | 选型 |
|------|------|
| 后端 | Java 17 + Spring Boot 3.x |
| 前端 | 现代前端框架（Vue 3 / React 18），响应式适配 |
| 数据库 | SQLite（单文件部署，零运维） |
| 密码存储 | bcrypt 加密 |
| 部署形态 | 单机 Web 服务，浏览器访问 |

---

## 2. 领域模型

### 2.1 实体关系图

```
┌──────────────────┐       ┌──────────────────────┐       ┌──────────────────┐
│      Book        │       │    BorrowRecord       │       │     Reader       │
├──────────────────┤       ├──────────────────────┤       ├──────────────────┤
│ id (PK, AUTO)    │──┐    │ id (PK, AUTO)        │    ┌──│ id (PK, AUTO)    │
│ title            │  │    │ book_id (FK→Book)    │◄───┘  │ name             │
│ author           │  └───►│ reader_id (FK→Reader)│◄──────│ phone (UNIQUE)   │
│ isbn (UNIQUE)    │       │ borrow_date          │       │ email (UNIQUE)   │
│ category         │       │ due_date             │       │ password (bcrypt)│
│ publisher        │       │ return_date          │       │ role             │
│ year             │       │ status               │       │ status           │
│ total_stock      │       │ renewed              │       │ created_at       │
│ location         │       │ penalty_amount       │       │ updated_at       │
│ status           │       │ created_at           │       └──────────────────┘
│ created_at       │       └──────────────────────┘
│ updated_at       │
└──────────────────┘
```

### 2.2 实体定义

#### Book（图书）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 主键 |
| `title` | VARCHAR(256) | NOT NULL | 书名 |
| `author` | VARCHAR(128) | NOT NULL | 作者 |
| `isbn` | VARCHAR(20) | UNIQUE, NOT NULL | ISBN-10 或 ISBN-13 |
| `category` | VARCHAR(32) | NOT NULL | 分类（预定义枚举） |
| `publisher` | VARCHAR(128) | | 出版社 |
| `year` | INTEGER | | 出版年份 |
| `total_stock` | INTEGER | NOT NULL, DEFAULT 1, ≥1 | 总册数（入库数量） |
| `location` | VARCHAR(64) | | 书架位置 |
| `status` | VARCHAR(16) | NOT NULL, DEFAULT 'active' | 状态：active / delisted |
| `created_at` | DATETIME | NOT NULL | 入库时间 |
| `updated_at` | DATETIME | NOT NULL | 最后更新时间 |

**可用册数** = `total_stock - COUNT(BorrowRecord WHERE status IN ('borrowing', 'overdue') AND book_id = id)`

**分类枚举**：`文学` / `科技` / `社会科学` / `历史` / `哲学` / `艺术` / `少儿` / `教育` / `经济` / `其他`

**约束**：
- 同一 ISBN 不可重复录入；多副本通过增加 `total_stock` 管理
- `total_stock ≥ 1`，库存不可为 0 或负数
- 删除图书前须校验：不存在 status 为 `borrowing` 或 `overdue` 的借阅记录
- 删除为软删除（`status=delisted`），保留历史借阅记录可查

#### Reader（读者）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 主键 |
| `name` | VARCHAR(64) | NOT NULL | 姓名 |
| `phone` | VARCHAR(20) | UNIQUE, NOT NULL | 手机号 |
| `email` | VARCHAR(128) | UNIQUE | 邮箱（可选） |
| `password` | VARCHAR(256) | NOT NULL | bcrypt 加密密码 |
| `role` | VARCHAR(16) | NOT NULL, DEFAULT 'reader' | 角色：admin / reader |
| `status` | VARCHAR(16) | NOT NULL, DEFAULT 'active' | 状态：active / inactive |
| `created_at` | DATETIME | NOT NULL | 注册时间 |
| `updated_at` | DATETIME | NOT NULL | 最后更新时间 |

**约束**：
- 手机号全局唯一
- 邮箱全局唯一（可为空）
- 注销（`status=inactive`）时须校验：不存在 status 为 `borrowing` 或 `overdue` 的借阅记录，否则拒绝操作
- 注销后不可登录、不可借书，历史记录保留

#### BorrowRecord（借阅记录）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 主键 |
| `book_id` | BIGINT | FK→Book.id, NOT NULL | 图书 ID |
| `reader_id` | BIGINT | FK→Reader.id, NOT NULL | 读者 ID |
| `borrow_date` | DATE | NOT NULL | 借书日期 |
| `due_date` | DATE | NOT NULL | 应还日期（borrow_date + 30 自然日） |
| `return_date` | DATE | | 实际归还日期 |
| `status` | VARCHAR(16) | NOT NULL, DEFAULT 'borrowing' | borrowing / renewed / returned / overdue |
| `renewed` | BOOLEAN | NOT NULL, DEFAULT FALSE | 是否已续借 |
| `penalty_amount` | DECIMAL(10,2) | DEFAULT 0.00 | 逾期罚金（元） |
| `created_at` | DATETIME | NOT NULL | 记录创建时间 |

**状态转换规则**：

```
borrowing ──(到期未还)──► overdue
borrowing ──(续借)──────► renewed
renewed   ──(到期未还)──► overdue
borrowing ──(归还)──────► returned
renewed   ──(归还)──────► returned
overdue   ──(归还)──────► returned  (同时计算 penalty_amount)
```

---

## 3. 功能需求

### 3.1 认证与授权

| ID | 功能 | 优先级 | 说明 |
|----|------|--------|------|
| F-AUTH-01 | 读者注册 | P0 | 自助填写姓名、手机号、邮箱、密码；手机号唯一校验；密码 bcrypt 加密存储 |
| F-AUTH-02 | 登录 | P0 | 手机号+密码登录；返回角色（admin/reader），前端按角色展示不同功能 |
| F-AUTH-03 | 管理员种子账号 | P0 | 系统预置 admin / admin123，首次登录强制跳转修改密码页面 |
| F-AUTH-04 | 修改密码 | P1 | 登录后修改密码，需验证旧密码 |

### 3.2 图书管理（管理员）

| ID | 功能 | 优先级 | 说明 |
|----|------|--------|------|
| F-BOOK-01 | 图书录入 | P0 | 录入学段：书名*(必填)、作者*(必填)、ISBN*(必填唯一)、分类*(必填下拉)、出版社、出版年份、总册数*(≥1)、书架位置 |
| F-BOOK-02 | 图书编辑 | P0 | 修改已有图书的全部字段；ISBN 不可修改（或修改后校验唯一）；总册数不可低于当前在借数量 |
| F-BOOK-03 | 图书删除 | P0 | 校验无在借记录后，软删除（status→delisted）；有在借记录时返回明确错误提示 |
| F-BOOK-04 | 图书查询 | P0 | 按书名/作者/ISBN/分类模糊搜索，支持组合条件；搜索结果分页展示（默认每页 20 条） |
| F-BOOK-05 | 图书列表 | P0 | 分页展示所有 active 图书；支持按分类筛选、按入库时间排序（升/降序）；每行展示可用册数 |
| F-BOOK-06 | 图书下架 | P1 | 将图书 status 置为 delisted（不可借阅），不影响已有借阅记录 |

### 3.3 借阅管理（读者+管理员）

| ID | 功能 | 优先级 | 说明 |
|----|------|--------|------|
| F-BORROW-01 | 借书 | P0 | 读者选择可用册数>0的图书，生成借阅记录：`borrow_date=TODAY, due_date=TODAY+30天, status=borrowing`；系统自动扣减可用册数（动态计算） |
| F-BORROW-02 | 借阅限制 | P0 | 每位读者同时最多借阅 5 本（status IN ('borrowing','renewed','overdue')）；有逾期未还图书的读者**不可**借书 |
| F-BORROW-03 | 还书 | P0 | 标记归还：`return_date=TODAY, status=returned`；若 `return_date > due_date`，自动计算罚金：`penalty_amount = (return_date - due_date) × 1.00` 元/天 |
| F-BORROW-04 | 续借 | P0 | 在 due_date 之前且 `renewed=false` 时，可续借一次：`due_date = due_date + 15天, renewed=true, status=renewed`；已逾期不可续借 |
| F-BORROW-05 | 借阅历史 | P1 | 按图书/读者查看借阅流转记录，含借书日期、应还日期、实际归还日期、罚金、续借标记 |
| F-BORROW-06 | 个人借阅 | P0 | 读者查看自己的当前在借图书和历史记录 |

### 3.4 读者管理（管理员）

| ID | 功能 | 优先级 | 说明 |
|----|------|--------|------|
| F-READER-01 | 读者列表 | P0 | 分页展示所有读者，支持按姓名/手机号搜索 |
| F-READER-02 | 编辑读者 | P1 | 修改读者信息（姓名、手机号、邮箱）；手机号唯一校验 |
| F-READER-03 | 注销读者 | P0 | 校验无在借记录后，status→inactive；有在借记录时拒绝并提示 |
| F-READER-04 | 查看读者借阅记录 | P1 | 管理员查看指定读者的完整借阅历史 |

### 3.5 数据统计（管理员）

| ID | 功能 | 优先级 | 说明 |
|----|------|--------|------|
| F-STAT-01 | 热门图书排行 | P1 | 按借阅次数降序，Top N（默认 20） |
| F-STAT-02 | 月度借阅量统计 | P1 | 按月份汇总借阅笔数 |
| F-STAT-03 | 逾期未还清单 | P1 | 当前所有 overdue 状态的借阅记录，含读者、图书、超期天数 |

---

## 4. 交互与流程

### 4.1 核心操作步骤上限

所有核心操作不超过 **3 步**完成：

| 操作 | 步骤 |
|------|------|
| 借书 | ① 搜索/浏览图书 → ② 点击借阅 → ③ 确认 |
| 还书 | ① 进入个人借阅列表 → ② 点击归还 → ③ 确认 |
| 图书录入 | ① 点击新增 → ② 填写表单 → ③ 提交 |
| 图书搜索 | ① 输入关键词 → ② 查看结果 |

### 4.2 页面导航

```
登录页
├── 管理员首页
│   ├── 图书管理（列表/新增/编辑/删除）
│   ├── 读者管理（列表/编辑/注销）
│   ├── 借阅记录（全部借阅历史）
│   ├── 逾期管理（逾期清单）
│   └── 数据统计
└── 读者首页
    ├── 图书检索（搜索/列表 → 借阅）
    ├── 我的借阅（当前在借 → 归还/续借）
    └── 借阅历史
```

---

## 5. API 设计概要

### 5.1 RESTful 端点

```
# 认证
POST   /api/auth/register          # 读者注册
POST   /api/auth/login             # 登录
PUT    /api/auth/password          # 修改密码

# 图书
GET    /api/books                  # 图书列表（分页/筛选/排序）
GET    /api/books/search           # 图书搜索（模糊）
GET    /api/books/{id}             # 图书详情
POST   /api/books                  # 录入图书 [ADMIN]
PUT    /api/books/{id}             # 编辑图书 [ADMIN]
DELETE /api/books/{id}             # 删除图书 [ADMIN]
PUT    /api/books/{id}/delist      # 下架图书 [ADMIN]

# 借阅
POST   /api/borrows                # 借书
POST   /api/borrows/{id}/return    # 还书
POST   /api/borrows/{id}/renew     # 续借
GET    /api/borrows/my             # 个人借阅记录
GET    /api/borrows/book/{bookId}  # 图书借阅历史 [ADMIN]
GET    /api/borrows/reader/{readerId} # 读者借阅历史 [ADMIN]
GET    /api/borrows/overdue        # 逾期清单 [ADMIN]

# 读者
GET    /api/readers                # 读者列表 [ADMIN]
GET    /api/readers/{id}           # 读者详情 [ADMIN]
PUT    /api/readers/{id}           # 编辑读者 [ADMIN]
PUT    /api/readers/{id}/deactivate # 注销读者 [ADMIN]

# 统计
GET    /api/stats/popular-books    # 热门图书 [ADMIN]
GET    /api/stats/monthly          # 月度借阅量 [ADMIN]
```

### 5.2 标准响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

错误码：
- `0`：成功
- `400`：参数校验失败
- `401`：未登录
- `403`：权限不足
- `404`：资源不存在
- `409`：业务冲突（如超过借阅上限、有未还书不可删除等）
- `500`：服务器错误

---

## 6. 非功能需求

### 6.1 性能

- 单机部署，支持 50 并发读者
- 图书搜索 < 500ms（万级数据量）
- 列表分页默认 20 条/页

### 6.2 安全

- 密码 bcrypt 加密存储（cost ≥ 10）
- 基于 Session/Token 的认证，登录态过期时间 24h
- SQL 参数化查询防注入
- 管理员接口权限校验

### 6.3 可用性

- 响应式布局，适配桌面和移动端浏览器
- 表单校验：前端即时校验 + 后端二次校验
- 操作结果有明确成功/失败提示

### 6.4 数据

- SQLite 单文件存储，自动初始化表结构
- 软删除策略：Book 和 Reader 删除仅标记状态，BorrowRecord 物理保留

---

## 7. 验收标准

| # | 验收项 | 测试方法 |
|---|--------|----------|
| AC1 | 管理员可完整操作图书增删改查全流程 | 端到端测试：新增→编辑→查询→删除/下架 |
| AC2 | 读者可检索图书、借阅、归还 | 端到端测试：注册→登录→搜索→借书→还书 |
| AC3 | 超期归还自动计算罚金（1元/天） | 单元测试：构造 overdue→return 场景，断言 penalty_amount |
| AC4 | 同一位读者最多借阅 5 本的限制生效 | 单元测试：第 6 次借阅返回 409 |
| AC5 | 有未归还记录的图书无法被删除 | 单元测试：有 borrowing 状态的图书删除返回 409 |
| AC6 | 有逾期未还的读者无法借书 | 单元测试：有 overdue 记录的读者借书返回 409 |
| AC7 | 续借限制：未到期可续一次（+15天），已逾期不可续 | 单元测试：覆盖续借成功和拒绝场景 |
| AC8 | 注销读者时存在未还书则拒绝 | 单元测试：有 borrowing 记录的读者注销返回 409 |

---

## 8. 测试设计（Seam）

### 8.1 测试策略

| 层级 | 范围 | 工具 |
|------|------|------|
| 单元测试 | Service 层业务逻辑（借阅规则、罚金计算、库存校验） | JUnit 5 + Mockito |
| 集成测试 | Repository 层 + 数据库交互 | @DataJpaTest / @SpringBootTest + SQLite |
| API 测试 | Controller 层端到端 | MockMvc / TestRestTemplate |
| 前端测试 | 组件渲染与交互 | Vitest / Jest + Testing Library |

### 8.2 关键测试用例

```
✅ BookServiceTest
   - shouldCreateBookWithUniqueIsbn
   - shouldRejectDuplicateIsbn
   - shouldNotDeleteBookWithActiveBorrows
   - shouldSoftDeleteBook (status=delisted)
   - shouldCalculateAvailableStock (total - active borrows)

✅ BorrowServiceTest
   - shouldCreateBorrowRecordWithDueDate30DaysLater
   - shouldRejectBorrowWhenNoAvailableStock
   - shouldRejectBorrowWhenReaderHasOverdueBooks
   - shouldRejectBorrowWhenReaderReachedMaxLimit (5)
   - shouldReturnBookAndCalculatePenalty (overdue)
   - shouldReturnBookWithoutPenalty (on time)
   - shouldRenewBeforeDueDate (+15 days)
   - shouldRejectRenewWhenOverdue
   - shouldRejectSecondRenew (already renewed)
   - shouldRejectRenewAfterDueDate

✅ ReaderServiceTest
   - shouldRegisterReader (bcrypt password)
   - shouldRejectDuplicatePhone
   - shouldNotDeactivateReaderWithActiveBorrows
   - shouldDeactivateReader (status=inactive, history preserved)
```

### 8.3 测试数据 Fixture

```java
// 预置测试数据
Book bookInStock = Book.builder().title("测试图书").isbn("978-7-111-00001").totalStock(3).status("active").build();
Book bookWithBorrow = Book.builder()...status("active").build();
Reader admin = Reader.builder().name("管理员").phone("13800000001").role("admin").build();
Reader reader = Reader.builder().name("读者张三").phone("13800000002").role("reader").build();
BorrowRecord active = BorrowRecord.builder().book(bookWithBorrow).reader(reader).status("borrowing").borrowDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(30)).build();
```

---

## 9. 排期建议

| 迭代 | 范围 | 预计工作量 |
|------|------|-----------|
| Sprint 1 | 项目脚手架 + 数据模型 + 认证（注册/登录/种子管理员） | 2-3 天 |
| Sprint 2 | 图书管理 CRUD + 搜索/列表分页 | 2-3 天 |
| Sprint 3 | 借阅/归还/续借核心流程 + 业务规则校验 | 3-4 天 |
| Sprint 4 | 读者管理 + 数据统计 + 前端整合 | 2-3 天 |
| Sprint 5 | 测试完善 + 验收 + 文档 | 1-2 天 |

---

## 附录 A：需求拷问决议

以下问题经合理推断取默认值，如有偏差请在评审中指正：

| # | 问题 | 决议 |
|---|------|------|
| A1 | 管理员产生方式 | 预置种子账号 admin/admin123，首次登录强制改密 |
| A2 | 读者注册 | 读者自助注册 |
| A3 | 管理员表设计 | Reader 表加 role 字段，共用 |
| A4 | 注销语义 | 软删除（inactive），有未还书拒绝注销 |
| B1 | stock 含义 | total_stock=总册数，可用数动态计算 |
| B2 | ISBN 唯一性 | 唯一，多副本合并一条记录 |
| B3 | 下架/删除 | 区分：下架=delisted、删除=软删除 |
| B4 | 分类方式 | 预定义枚举（10 类） |
| B5 | 出版年份 | 整数年份 |
| C1 | 借还书操作者 | 读者自助 |
| C2 | 续借到期计算 | 原到期日+15天 |
| C3 | 逾期是否禁借 | 是，有逾期不可借书 |
| C4 | 罚金追踪 | 仅展示金额，不追踪缴纳 |
| C5 | 30天类型 | 自然日 |
| D1 | 统计纳入 MVP | 是 |
| E1 | 技术栈 | Java 17 + Spring Boot 3 + 现代前端 |
| E2 | 数据库 | SQLite |
| E3 | 并发规模 | 50 并发，万级藏书 |
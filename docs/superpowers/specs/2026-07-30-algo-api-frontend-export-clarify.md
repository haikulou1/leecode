# 算法接口 + 前端展示 + 导出 — 澄清设计文档（clarify 阶段）

> 阶段：clarify（需求澄清） · 技能：brainstorming · 日期：2026-07-30
> 本文档为澄清产物，不含代码变更。Git 只读，未提交。

---

## 0. 需求原文

- 用 Java 分别写三个接口：helloworld、哈希算法、冒泡排序。
- 前端新增一个页面，有三个 tab 分别展示不同执行结果。
- 新增导出按钮，后台提供导出接口，支持导出各个页面的展示结果。

---

## 1. 通览（两库现状证据）

| 仓库 | worktree_path | 性质 | 关键证据 | 与需求差距 |
|------|---------------|------|---------|-----------|
| leecode | `…/worktree/leecode-master` | Java 刷题仓 | `leecode/src/l0x_*/Solution.java`（按 leecode 题号包名）；**无 `pom.xml`/`build.gradle`**；无 Spring/Servlet 等 Web 框架；无 `.agents/` | 缺整套后端 Web 工程骨架（构建+运行+HTTP 暴露） |
| haikulou1.github.io | `…/worktree/haikulou1.github.io-master` | Hexo 静态站（NexT 主题） | `index.html` + `js/next-boot.js` + `lib/velocity` + `css/`；纯静态产物；**无 `package.json`**；无前端构建工程；无后端 API | 缺前端工程化；静态站无法直接托管动态 API |

---

## 2. 澄清问题清单 + 推荐默认假设

> brainstorming 原则"一次一问"，但本任务采用全流水线非阻塞模式 → 将所有澄清点与推荐假设一次性列出，供审查阶段集中确认。审查者仅需对"推荐假设"逐条 ✅/✗ 即可推进。

### Q1 后端 Java Web 技术栈选型
leecode 仓当前**无任何 Web 框架与构建工具**。三个"接口"需要 HTTP 暴露。

- 选项 A（推荐）：**Spring Boot**（Maven，`spring-boot-starter-web`）。生态成熟、REST 友好、导出（CSV/JSON）易实现。
- 选项 B：**Servlet + 内嵌 Tomcat / Jetty**（Maven `war`）。更轻量但样板代码多。
- 选项 C：**Java 原生 `com.sun.net.httpserver.HttpServer`**（JDK 自带，零依赖）。最轻，但无依赖注入/序列化便利。

**推荐假设**：采用 A（Spring Boot），在 leecode 仓新增独立 Maven 模块 `algo-api/`，不污染现有 `leecode/src` 刷题目录。

### Q2 "哈希算法"接口的语义
"哈希算法"歧义较大，需澄清指代：
- 选项 A（推荐）：**字符串哈希演示**——入参一段文本 + 算法名（如 MD5/SHA-256/SHA-512），返回各算法哈希摘要 hex 串。
- 选项 B：**哈希表数据结构演示**（如手写 HashMap/哈希冲突解决）。
- 选项 C：**一致性哈希**等分布式场景。

**推荐假设**：A——对入参文本用 `MessageDigest` 计算 MD5/SHA-256/SHA-512 并返回摘要集合，可演示且接口语义清晰。

### Q3 "冒泡排序"接口的入参/出参
- 选项 A（推荐）：**入参整型数组（JSON `int[]`）→ 返回排序后数组 + 每步交换轨迹**（便于前端"展示执行结果"）。
- 选项 B：仅返回排序后数组。
- 选项 C：后端内置固定数组，无入参。

**推荐假设**：A——支持入参数组，返回 `{sorted: int[], steps: [[...], ...]}`，既展示结果又可演示过程。

### Q4 "helloworld"接口语义
- 选项 A（推荐）：GET `/api/hello` → 返回 `{message: "Hello, World!"}`（可含时间戳）。
- 选项 B：返回纯文本 `Hello, World!`。

**推荐假设**：A（JSON 结构，与其他接口统一）。

### Q5 前端页面承载方式
haikulou1.github.io 是 Hexo **静态站**，无前端工程化。

- 选项 A（推荐）：**新增静态页面 `algo-demo/index.html`**（独立目录，原生 HTML+JS+CSS，fetch 调用后端 API）。最小侵入，契合静态站现状。
- 选项 B：引入轻量前端框架（Vue/React CDN 版）。
- 选项 C：新建独立前端工程（Vite 等）。

**推荐假设**：A——原生三 Tab 页面，契合 Hexo 静态站，无需构建。

### Q6 前后端联调的跨域与部署
静态站（GitHub Pages 域）调用本地/远端 Java API 存在跨域。

- 选项 A（推荐）：**后端开启 CORS**（`Access-Control-Allow-Origin`），前端直接 fetch。
- 选项 B：前端通过 Nginx 反代。
- 选项 C：同源部署（后端同时托管前端静态页）。

**推荐假设**：A——后端 CORS 放开，开发期最简。

### Q7 导出接口的格式与范围
"支持导出各个页面的展示结果"：
- 选项 A（推荐）：**按 tab 类型导出**，`GET /api/export?type={hello|hash|bubble}` → 返回对应 tab 结果文件。
- 选项 B：一次性导出全部 tab 为一个文件。
- 选项 C：前端纯前端导出（Blob），不经后端。

**推荐假设**：A——后端按 `type` 参数分别导出。格式默认 **CSV**（结构化、通用）；helloworld 导出单列，hash 导出算法名+摘要，bubble 导出步骤矩阵。

### Q8 导出文件下载方式
- 选项 A（推荐）：后端设置 `Content-Disposition: attachment; filename=...`，浏览器直接下载。
- 选项 B：返回 Base64，前端解码下载。

**推荐假设**：A。

---

## 3. 候选方案（2-3，含权衡与推荐）

### 方案 ①（推荐）：Spring Boot 后端 + Hexo 静态页前端 + CORS 跨域
- **后端**：leecode 仓新增 `algo-api/` Maven 模块，Spring Boot 暴露 4 接口。
- **前端**：haikulou1.github.io 仓新增 `algo-demo/index.html`（原生三 Tab + 导出按钮）。
- **跨库契约**：REST JSON，统一响应体 `{code, message, data}`。
- 优点：契合两库现状（leecode 可承载 Maven 工程；静态站加页即可）；改动隔离，不碰刷题代码与主题。
- 缺点：跨域需 CORS 配置；静态站无构建期类型检查。

### 方案 ②：单一 Java 服务全栈（后端同时托管前端静态页）
- 后端 Spring Boot 静态资源目录放前端 HTML，同源无跨域。
- 缺点：前端页落到 leecode 仓，与"前端仓=haikulou1.github.io"的职责划分冲突；GitHub Pages 无法直接服务。**不推荐**。

### 方案 ③：Java 原生 HttpServer + 前端纯前端导出
- 最轻依赖，但导出不经后端（与"后台提供导出接口"需求相悖）。**不推荐**。

**推荐**：方案 ①。

---

## 4. 推荐设计（分节，待审查确认）

### 4.1 架构
```
[浏览器] --fetch--> [haikulou1.github.io/algo-demo/index.html (静态)]
                         |
                         | CORS
                         v
               [leecode/algo-api (Spring Boot :8080)]
                    ├── GET /api/hello
                    ├── POST /api/hash
                    ├── POST /api/bubble
                    └── GET /api/export?type=...
```

### 4.2 组件与职责（单元化）
| 单元 | 仓库 | 职责 | 依赖 |
|------|------|------|------|
| `HelloController` | leecode | 返回 hello JSON | 无 |
| `HashController` | leecode | 文本→多算法摘要 | `MessageDigest` |
| `BubbleController` | leecode | 数组排序+步骤轨迹 | 无 |
| `ExportController` | leecode | 按 type 导出 CSV | 上述三 service |
| `algo-demo/index.html` | haikulou1.github.io | 三 Tab + 导出按钮 UI | fetch API |

### 4.3 跨库接口契约（REST，统一响应体）
```json
{ "code": 0, "message": "ok", "data": { ... } }
```
- `GET /api/hello` → `data: { message: "Hello, World!", timestamp: "ISO-8601" }`
- `POST /api/hash` body `{ "text": "abc", "algos": ["MD5","SHA-256","SHA-512"] }` → `data: { MD5: "...", "SHA-256": "...", "SHA-512": "..." }`
- `POST /api/bubble` body `{ "array": [3,1,2] }` → `data: { sorted: [1,2,3], steps: [[3,1,2],[1,3,2],[1,2,3]] }`
- `GET /api/export?type=hello|hash|bubble` → `Content-Type: text/csv; Content-Disposition: attachment`

### 4.4 数据流
前端 Tab 切换 → fetch 对应接口 → 渲染结果；导出按钮 → `window.location` 或隐藏 a 标签请求 `/api/export?type=<当前tab>` 触发下载。

### 4.5 错误处理
- 统一 `code`：0 成功，非 0 失败并带 `message`。
- 入参校验失败 → 400 + 错误描述。
- 异常 → `@RestControllerAdvice` 兜底 500。

### 4.6 测试
- 后端：Spring Boot Test 对 4 接口单元+切片测试（MockMvc）。
- 前端：静态页人工联调（无构建期测试框架）。

---

## 5. 待确认事项汇总（审查清单）

| # | 澄清点 | 推荐假设 | 审查 |
|---|--------|---------|------|
| Q1 | 后端栈 | Spring Boot + Maven（leecode 新增 `algo-api/` 模块） | ☐ |
| Q2 | 哈希接口 | 文本→MD5/SHA-256/SHA-512 摘要集合 | ☐ |
| Q3 | 冒泡接口 | 入参数组→排序后数组+步骤轨迹 | ☐ |
| Q4 | helloworld | JSON `{message, timestamp}` | ☐ |
| Q5 | 前端承载 | haikulou1.github.io 新增 `algo-demo/index.html` 静态页 | ☐ |
| Q6 | 跨域 | 后端 CORS 放开 | ☐ |
| Q7 | 导出 | 按 `type` 导出 CSV | ☐ |
| Q8 | 下载 | Content-Disposition 附件下载 | ☐ |

---

## 6. 跨库对齐点（契约兼容性）
- 响应体结构前后端统一为 `{code,message,data}`，新增字段不破坏旧前端。
- `type` 枚举值 `hello|hash|bubble` 前后端必须一致。
- 导出格式 CSV 列定义需前后端约定一致（前端不解析 CSV，仅下载，故兼容性风险低）。

## 7. 风险
- leecode 仓首次引入 Maven/Spring Boot 构建链，需确认 CI/本地具备 JDK8+ 与 Maven。
- 静态站调远端 API 的网络可达性与 CORS 安全策略（生产期应限制 Origin）。

# 代码评审报告：Java 三接口及前端多 Tab 导出页面

| 项目 | 内容 |
|------|------|
| 评审日期 | 2026-07-31 |
| 评审阶段 | review（代码评审，只读）— 第二轮（CR 修复后复审） |
| 评审技能 | code-review-skill |
| 评审范围 | leecode（后端 Java Spring Boot）+ haikulou1.github.io（前端 JS/HTML） |
| 需求摘要 | 用 java 分别写三个接口 helloworld、哈希算法、冒泡排序；前端新增一个页面，有三个 tab 分别展示不同的执行结果；新增导出按钮，后台提供导出接口，支持导出各个页面的展示结果 |
| Blocker 数 | 0 |
| Major 数 | 0 |
| Minor 数 | 5 |
| 评审结论 | **通过** — 第一轮 4 个 Major 全部修复且跨仓契约向后兼容；无阻塞性问题；残留项均为非阻塞 Minor，演示场景可合并，生产化前可选处理 |

---

## 一、通览

本次评审为**第二轮复审**，基于第一轮 CR 报告提出的问题经「问题修复」阶段处理后的最新代码。

- **后端（leecode / algo-demo）**：Spring Boot 2.7.18 + Java 8，提供 4 个 REST 接口（`/api/helloworld`、`/api/hash`、`/api/bubble-sort`、`GET/POST /api/export`）。修复阶段新增 `GlobalExceptionHandler`（统一异常处理）、`ExportRequest`（POST 导出请求体），并收紧 `CorsConfig`、补齐 `HashService` 白名单、`ExportService` 时区改 UTC、Controller 流包裹 try-with-resources。
- **前端（haikulou1.github.io）**：原生 JS（IIFE + strict）+ 单页 HTML，三 Tab 切换。修复阶段将 `API_BASE` 改为可配置（`window.ALGO_API_BASE || 'http://localhost:8080'`），导出改走 POST 路径并携带各 Tab 实时结果，`parseFileName` 增强 RFC 5987 兼容与异常兜底。

设计文档（`java-frontend-demo-design.md`）与实施计划（`java-frontend-demo-implementation-plan.md`）完备。评审聚焦：第一轮 CR 项修复核查、跨仓接口契约对齐（含新增 POST 导出契约）、安全性、健壮性、可维护性。

---

## 二、第一轮 CR 项修复核查

| # | 第一轮问题 | 严重度 | 修复后现状 | 核查结论 |
|---|-----------|--------|-----------|---------|
| 1 | CORS `allowedOriginPatterns("*")` + `allowCredentials(true)` 全开放带凭证 | Major | `CorsConfig` 改为显式白名单：`https://haikulou1.github.io`、`http://localhost:*`、`http://127.0.0.1:*`；移除 `allowCredentials`；方法限定 `GET/POST/OPTIONS`；预检 3600s | ✅ 已修复 |
| 2 | 导出用固定示例数据，不反映用户当前页面结果 | Major | 新增 `POST /api/export` + `ExportRequest{tab,hello,hash,bubble}`，`ExportService.buildContent(ExportRequest)` 优先采用前端实时结果，缺省字段用内置示例兜底；GET 路径保留为向后兼容示例导出 | ✅ 已修复（契约向后兼容） |
| 3 | 前端 `API_BASE` 硬编码 localhost，部署阻断 | Major | `js/algo-demo.js` 改为 `(typeof window !== 'undefined' && window.ALGO_API_BASE) || 'http://localhost:8080'`，支持页面注入全局配置 | ✅ 已修复 |
| 4 | 导出/算法异常冒泡 500，无统一错误体 | Major | 新增 `GlobalExceptionHandler`（`@RestControllerAdvice`）：`IllegalArgumentException`→400，`Exception`→500，返回结构化 JSON `{error,status,message,timestamp}` | ✅ 已修复 |
| 5 | HashService 算法名无白名单，非法值抛 500 | Minor | `HashService` 新增 `ALLOWED = {MD5,SHA-256,SHA-512}` HashSet，大小写不敏感匹配，非法值抛 `IllegalArgumentException`（经全局处理→400） | ✅ 已修复 |
| 8 | ExportService `LocalDateTime.now()` 默认时区 | Minor | `generateFileName()` 改为 `ZonedDateTime.now(ZoneOffset.UTC).format(FILE_TS)`，文件名时间戳固定 UTC | ✅ 已修复 |
| 9 | Controller OutputStream 未 try-with-resources | Minor | `export()` 与 `exportPost()` 均改为 `try (ServletOutputStream out = response.getOutputStream()) { ... }` | ✅ 已修复 |
| 10 | filename 正则不兼容 RFC 5987 | Minor | `parseFileName` 优先匹配 `filename*=UTF-8''...` 分支，回退传统 `filename="..."` | ✅ 已修复 |
| 11 | `decodeURIComponent` 对非编码含 `%` 串可能抛错 | Minor | 各 decode 分支包裹 `try/catch`，失败回退 `fallback` 或原文 | ✅ 已修复 |

> 第一轮 12 项中 9 项已修复；剩余 #6（MD5 选项）、#7（BubbleSortService 冗余复制）、#12（大整数精度）为演示场景可接受项，详见第三节。

---

## 三、跨仓接口契约对齐检查

### 3.1 路由对齐

| 前端调用 | 后端映射 | 状态 |
|----------|----------|------|
| `GET API_BASE + /api/helloworld` | `@GetMapping("/helloworld")` | ✅ 对齐 |
| `POST API_BASE + /api/hash` | `@PostMapping("/hash")` | ✅ 对齐 |
| `POST API_BASE + /api/bubble-sort` | `@PostMapping("/bubble-sort")` | ✅ 对齐 |
| `POST API_BASE + /api/export`（携带实时结果） | `@PostMapping("/export")` + `@RequestBody ExportRequest` | ✅ 对齐（新增契约） |
| `GET API_BASE + /api/export?tab=all`（向后兼容） | `@GetMapping("/export")` + `@RequestParam tab` | ✅ 对齐 |

### 3.2 请求/响应字段对齐

| 接口 | 前端发送 | 后端接收 | 前端读取响应字段 | 后端响应字段 | 状态 |
|------|----------|----------|------------------|--------------|------|
| hash | `{input, algorithm}` | `HashRequest{input, algorithm}` | `input/algorithm/hash/length` | `HashResponse{input, algorithm, hash, length}` | ✅ |
| bubble-sort | `{input:[...]}` | `BubbleSortRequest{input:List<Integer>}` | `input/sorted/steps/round/swaps/array/swapCount` | `BubbleSortResponse{input, sorted, steps, swapCount}` + `Step{round, swaps, array}` | ✅ |
| helloworld | — | — | `result/timestamp` | `Map{result, timestamp}` | ✅ |
| export(POST) | `{tab, hello, hash, bubble}` | `ExportRequest{tab, hello, hash, bubble}` | Content-Disposition `filename` | `setHeader Content-Disposition` | ✅ |
| export(GET) | query `tab` | `@RequestParam tab` | Content-Disposition `filename` | `setHeader Content-Disposition` | ✅ |

### 3.3 导出契约（新增 POST）对齐详查

- **前端 `exportAll()`**：构造 `body = {tab:'all', hello: lastResults.hello||null, hash: lastResults.hash||null, bubble: lastResults.bubble||null}`，POST 至 `/api/export`，解析 `Content-Disposition` 触发 `a[download]` 下载。
- **后端 `exportPost()`**：`@RequestBody ExportRequest` → `exportService.buildContent(request)`，优先采用 `req.getHash()/getBubble()/getHello()`，缺省用 `hashDemo()/bubbleDemo()/helloWorldService.hello()` 兜底。
- **字段映射**：`ExportRequest.HelloResult{result,timestamp}` 对应前端 `lastResults.hello{result,timestamp}`；`hash` 字段直接复用 `HashResponse`，`bubble` 复用 `BubbleSortResponse`，Jackson 反序列化无歧义。
- **结论**：新增 POST 导出契约完整对齐，满足需求「支持导出各个页面的展示结果」（导出用户当前所见结果）。

### 3.4 CORS 对齐

- 后端 `CorsConfig` 白名单含 `https://haikulou1.github.io`（生产站点）与 `http://localhost:*`/`http://127.0.0.1:*`（本地开发），已关闭 `allowCredentials`，方法限定 `GET/POST/OPTIONS`。
- 前端 `fetch` 未显式 `credentials: 'include'`（导出/查询均无需凭证），CORS 实际可通。
- 结论：**跨仓契约完整对齐，无断裂点。**

---

## 四、逐文件审查发现（修复后）

### 后端 leecode / algo-demo

#### 4.1 `pom.xml` — ✅ 无问题
- Spring Boot 2.7.18（Java 8），仅 `spring-boot-starter-web`，依赖最小化。
- 含 `spring-boot-maven-plugin`，可打包可执行 jar。

#### 4.2 `AlgoDemoApplication.java` — ✅ 无问题
- 标准 `@SpringBootApplication` 启动类。

#### 4.3 `config/CorsConfig.java` — ✅ 已修复
- 显式白名单 + 关闭凭证 + 方法限定，符合安全最小化原则。

#### 4.4 `controller/AlgoController.java` — ✅ 良好
- 显式构造器注入四个 Service；GET/POST 双导出入口，try-with-resources 包裹流；Javadoc 完整。

#### 4.5 `controller/GlobalExceptionHandler.java` — ⚠️ Minor（信息泄露）
- `handleGeneral(Exception e)` 直接返回 `e.getMessage()`，可能将内部异常堆栈/类名等信息透传给客户端。演示场景可接受；生产化建议对 `Exception` 兜底返回通用消息（如 `"internal error"`），仅在日志记录原始异常。
- **严重度**：Minor（信息泄露，非阻塞）。

#### 4.6 `service/HashService.java` — ✅ 已修复 + ⚠️ Minor
- 白名单校验已补齐，非法算法名经全局处理返回 400。
- `toHex()` 手写实现正确（`v<0x10` 补零），仅风格可选，可用 `String.format("%02x", b)` 简化。非问题。
- MD5 选项保留（演示哈希可接受，UI 已限定选项，不用于安全场景）。

#### 4.7 `service/BubbleSortService.java` — ⚠️ Minor（冗余复制）
- 算法正确，不可变性良好（复制入参、每轮快照）。
- 冗余复制：`working = new ArrayList<>(input||empty)`、`input = new ArrayList<>(working)`、`response.setInput(new ArrayList<>(input))` 三次复制。无害，仅微冗余，可合并为单次复制。
- **严重度**：Minor（性能微损耗，demo 可接受）。

#### 4.8 `service/ExportService.java` — ✅ 已修复
- 双入口（GET 示例 / POST 实时），优先前端结果缺省兜底；`generateFileName` UTC 时区；`normalizeTab` 健壮；分隔符拼接清晰。

#### 4.9 `service/HelloWorldService.java` — ✅ 无问题
- 返回 `{result:"Hello, World!", timestamp: Instant.now().toString()}`，ISO-8601 UTC。

#### 4.10 Model 类 — ✅ 无问题
- `ExportRequest` 新增 `HelloResult` 内部静态类，结构清晰；`HashResponse`/`BubbleSortResponse` 复用为导出片段，Jackson 映射正确。

#### 4.11 `application.yml` — ✅ 无问题
- `server.port: 8080`，与前端 `API_BASE` 一致。

### 前端 haikulou1.github.io

#### 4.12 `js/algo-demo.js` — ✅ 已修复 + ⚠️ Minor
- `API_BASE` 可配置化；导出改走 POST 携带 `lastResults`；`parseFileName` RFC 5987 兼容 + try/catch 兜底；XSS 防护（全程 `textContent`/`createElement`）；`fetchJson` 校验 `res.ok` 与 `content-type`；`parseBubbleInput` 双校验；Tab 键盘可达性。
- 大整数精度（Minor）：`Number(parts[i])` 超过 `Number.MAX_SAFE_INTEGER` 精度丢失但 `isInteger` 仍 true；demo 小数组可接受。

#### 4.13 `page/algo-demo/index.html` — ✅ 无问题
- 语义化 ARIA，三 Tab + 双导出按钮，深色主题 CSS 内联。

---

## 五、问题汇总表（修复后残留）

| # | 严重度 | 仓库 | 文件 | 问题 | 建议 |
|---|--------|------|------|------|------|
| 1 | Minor | leecode | controller/GlobalExceptionHandler.java | `handleGeneral(Exception)` 直接返回 `e.getMessage()`，可能泄露内部异常信息 | 生产化对 `Exception` 兜底返回通用消息，日志记录原始异常 |
| 2 | Minor | leecode | service/BubbleSortService.java | 三次 `ArrayList` 复制冗余 | 合并为单次复制 |
| 3 | Minor | leecode | service/HashService.java | `toHex()` 手写实现 | 可选 `String.format("%02x", b)` 简化，非问题 |
| 4 | Minor | leecode | service/HashService.java | MD5 选项（已不抗碰撞） | 演示可保留，UI 标注非安全用途 |
| 5 | Minor | haikulou1.github.io | js/algo-demo.js | 大整数精度丢失 | demo 可接受 |

---

## 六、跨仓对齐点检查结论

| 对齐点 | 结论 |
|--------|------|
| 路由路径 | ✅ 前端 `/api/*` 与后端 `@RequestMapping("/api")` 完全匹配；新增 POST `/api/export` 双端对齐 |
| 请求字段 | ✅ hash `{input,algorithm}`、bubble `{input:[...]}`、export(POST) `{tab,hello,hash,bubble}` 与后端 DTO 一致 |
| 响应字段 | ✅ 前端读取的所有字段后端均提供 |
| 导出契约 | ✅ POST 实时结果导出 + GET 向后兼容示例导出，双端一致 |
| CORS | ✅ 后端白名单放开生产站点与本地源，前端跨域 fetch 可通 |
| 端口 | ✅ 前端 `API_BASE:8080` 与后端 `server.port:8080` 一致 |
| 异常处理 | ✅ `GlobalExceptionHandler` 统一结构化错误体，前端 `fetchJson`/`exportAll` 校验 `res.ok` 并展示错误 |

**结论**：跨仓接口契约完整对齐，无断裂；第一轮全部 Major 已修复且向后兼容（新增 POST 导出未破坏 GET 路径）；残留项均为非阻塞 Minor。

---

## 七、评审结论

- **Blocker：0**（无安全漏洞导致 RCE/数据泄露、无运行时崩溃、无契约断裂）
- **Major：0**（第一轮 4 个 Major 全部修复：CORS 收紧白名单、导出新增 POST 实时契约、前端 API_BASE 配置化、异常统一处理）
- **Minor：5**（异常信息泄露、冗余复制、toHex 风格、MD5 选项、大整数精度；均为演示可接受项）
- **建议**：演示场景可直接合并；生产化前可选处理 #1（异常信息脱敏）与 #2（冗余复制），并将 MD5 选项在 UI 标注非安全用途。

评审通过。

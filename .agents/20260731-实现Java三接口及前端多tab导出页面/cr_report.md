# 代码评审报告：Java 三接口及前端多 Tab 导出页面

| 项目 | 内容 |
|------|------|
| 评审日期 | 2026-07-31 |
| 评审阶段 | review（代码评审，只读） |
| 评审技能 | code-review-skill |
| 评审范围 | leecode（后端 Java Spring Boot）+ haikulou1.github.io（前端 JS/HTML） |
| 需求摘要 | 用 java 分别写三个接口 helloworld、哈希算法、冒泡排序；前端新增一个页面，有三个 tab 分别展示不同的执行结果；新增导出按钮，后台提供导出接口，支持导出各个页面的展示结果 |
| Blocker 数 | 0 |
| Major 数 | 4 |
| Minor 数 | 8 |
| 评审结论 | **通过（有条件）** — 无阻塞性问题，契约对齐完整，功能可用；建议在合并前处理导出语义与 CORS/硬编码等 Major 项，或在演示场景下显式接受并登记为已知项 |

---

## 一、通览

本次交付为跨仓双端实现：

- **后端（leecode / algo-demo）**：Spring Boot 2.7.18 + Java 8，单一 `spring-boot-starter-web` 依赖，提供 4 个 REST 接口（`/api/helloworld`、`/api/hash`、`/api/bubble-sort`、`/api/export`），分层清晰（Controller / Service / Model / Config），构造器注入，无 Lombok，POJO 手写 getter/setter。
- **前端（haikulou1.github.io）**：原生 JS（IIFE + strict）+ 单页 HTML，三 Tab 切换，DOM 操作防 XSS，`fetch` 调用后端，双导出按钮（页头 + 页脚）。

设计文档（`java-frontend-demo-design.md`）与实施计划（`java-frontend-demo-implementation-plan.md`）完备，代码与计划基本一致。评审聚焦：需求符合性、跨仓接口契约对齐、安全性、健壮性、可维护性。

---

## 二、跨仓接口契约对齐检查

### 2.1 路由对齐

| 前端调用 | 后端映射 | 状态 |
|----------|----------|------|
| `GET API_BASE + /api/helloworld` | `@RestController @RequestMapping("/api")` + `@GetMapping("/helloworld")` | ✅ 对齐 |
| `POST API_BASE + /api/hash` | `@PostMapping("/hash")` | ✅ 对齐 |
| `POST API_BASE + /api/bubble-sort` | `@PostMapping("/bubble-sort")` | ✅ 对齐 |
| `GET API_BASE + /api/export?tab=all` | `@GetMapping("/export")` + `@RequestParam("tab", defaultValue="all")` | ✅ 对齐 |

### 2.2 请求/响应字段对齐

| 接口 | 前端发送 | 后端接收 | 前端读取响应字段 | 后端响应字段 | 状态 |
|------|----------|----------|------------------|--------------|------|
| hash | `{input, algorithm}` | `HashRequest{input, algorithm}` | `input/algorithm/hash/length` | `HashResponse{input, algorithm, hash, length}` | ✅ |
| bubble-sort | `{input:[...]}` | `BubbleSortRequest{input:List<Integer>}` | `input/sorted/steps/round/swaps/array/swapCount` | `BubbleSortResponse{input, sorted, steps, swapCount}` + `Step{round, swaps, array}` | ✅ |
| helloworld | — | — | `result/timestamp` | `Map{result, timestamp}` | ✅ |
| export | query `tab` | `@RequestParam tab` | Content-Disposition `filename` | `setHeader Content-Disposition` | ✅ |

### 2.3 CORS 对齐

- 后端 `CorsConfig` 对 `/api/**` 放开 `allowedOriginPatterns("*")` + `allowCredentials(true)`，预检 3600s。
- 前端 `fetch` 未显式 `credentials: 'include'`（导出/查询均无需凭证），CORS 实际可通。
- 结论：**跨仓契约完整对齐，无断裂点。**

---

## 三、逐文件审查发现

### 后端 leecode / algo-demo

#### 3.1 `pom.xml` — ✅ 无问题
- Spring Boot 2.7.18（Java 8），仅 `spring-boot-starter-web`，依赖最小化，符合 demo 定位。
- 含 `spring-boot-maven-plugin`，可打包可执行 jar。

#### 3.2 `AlgoDemoApplication.java` — ✅ 无问题
- 标准 `@SpringBootApplication` 启动类，无冗余注解。

#### 3.3 `config/CorsConfig.java` — ⚠️ Major（安全）

```java
registry.addMapping("/api/**")
        .allowedOriginPatterns("*")
        .allowedMethods("*")
        .allowedHeaders("*")
        .allowCredentials(true)   // ← 与通配源组合，等于全开放+凭证
        .maxAge(3600);
```

- **问题**：`allowedOriginPatterns("*")` 配合 `allowCredentials(true)` 在 Spring 中不抛异常（与 `allowedOrigins("*")` 不同），但语义上等于"任意源可携带凭证跨域访问全部 `/api/**`"。对于纯演示可接受，但若后端未来承载敏感操作则为反射型跨域风险。
- **建议**：演示阶段保留但加注释强调；生产化时改为显式白名单（如 GitHub Pages 源 `https://haikulou1.github.io`），并视需关闭 `allowCredentials`。
- **严重度**：Major（安全配置，演示可豁免，须登记为已知项）。

#### 3.4 `controller/AlgoController.java` — ⚠️ Major（导出语义）+ Minor

- **导出语义偏差（Major）**：`export()` 调用 `exportService.buildContent(tab)`，而 `ExportService` 内部用**固定示例数据**（hash 用 `"abc"/SHA-256`，bubble 用 `[5,3,8,1,9,2]`）重新计算，**不反映前端用户当前 Tab 的实际输入与结果**。需求"支持导出各个页面的展示结果"更贴近"导出用户当前所见结果"。当前实现导出的是后端重新计算的固定样例。
  - **建议**：若坚持无状态导出，应在文档/UI 明示"导出为内置示例结果"；若要导出用户实时结果，可由前端将当前结果作为请求体 POST 给导出接口，或后端支持接收参数。
  - **严重度**：Major（需求符合性，非崩溃）。
- **异常未友好化（Minor）**：`exportService.buildContent(tab)` 内部若 `HashService` 抛 `IllegalArgumentException`（非法算法名），将冒泡为 HTTP 500，无统一错误体。演示可接受。
- **流未显式关闭（Minor）**：`response.getOutputStream()` 未 try-with-resources，但由 Servlet 容器管理，无泄漏。
- **构造器注入（✅ 良好）**：显式构造器注入四个 Service，可测试性好，避免字段注入。

#### 3.5 `service/HashService.java` — ⚠️ Minor（输入校验）+ ✅ 实现正确

- **算法白名单缺失（Minor）**：`algorithm` 直接透传 `MessageDigest.getInstance(algorithm)`，前端 select 限定了 MD5/SHA-256/SHA-512，但 API 可被直接调用传任意值（如 `"FOO"`）→ `NoSuchAlgorithmException` → `IllegalArgumentException` → 500。虽有异常兜底，但建议加白名单校验返回明确业务错误。
- **null 处理（✅ 良好）**：`input` null → `""`；`algorithm` null/empty → `SHA-256`。
- **`toHex()` 手写（✅ 正确）**：`v < 0x10` 补零逻辑正确，等价于 `String.format("%02x", b)`，无 bug，仅风格可选。
- **MD5 可用性提示（Minor）**：MD5 已不具抗碰撞性，演示哈希可接受，但不应用于安全场景；UI 已提供选项，可接受。

#### 3.6 `service/BubbleSortService.java` — ✅ 无问题

- 算法正确：标准升序冒泡，外层 `round 1..n-1`，内层 `j 0..n-round-1`，比较 `j` 与 `j+1` 交换。
- **不可变性（✅ 良好）**：复制入参 `new ArrayList<>(request.getInput())` 后排序，不修改原集合；`Step.array` 每轮快照 `new ArrayList<>(working)`。
- **null 处理（✅）**：input null → `emptyList()`，返回空结果而非 NPE。
- **冗余复制（Minor）**：`input` 已复制一次，`working = new ArrayList<>(input)` 再复制一次。无害，仅微冗余。

#### 3.7 `service/ExportService.java` — ⚠️ Major（见 3.4）+ Minor

- **固定示例（Major）**：见 3.4，导出不反映用户实时结果。
- **`generateFileName()` 时区（Minor）**：`LocalDateTime.now()` 用系统默认时区，文件名时间戳可能跨部署环境不一致；演示可接受。
- **tab 白名单（✅ 良好）**：未知/空 tab 回退 `all`，健壮。
- **分隔符拼接（✅）**：`appendSeparatorIfNeeded` 在已存在内容前加空行，格式清晰。

#### 3.8 `service/HelloWorldService.java` — ✅ 无问题
- 返回 `{result:"Hello, World!", timestamp: Instant.now().toString()}`，ISO-8601 UTC，契约匹配前端。

#### 3.9 Model 类（HashRequest/HashResponse/BubbleSortRequest/BubbleSortResponse）— ✅ 无问题
- POJO + getter/setter，Jackson 默认映射正确。
- `BubbleSortResponse.Step` 内部静态类，结构清晰。
- `BubbleSortRequest.input` 为 `List<Integer>`，前端传 `{"input":[5,3,8]}` 正确反序列化。

#### 3.10 `application.yml` — ✅ 无问题
- `server.port: 8080`，与前端 `API_BASE` 一致。

---

### 前端 haikulou1.github.io

#### 3.11 `js/algo-demo.js` — ⚠️ Major（硬编码）+ Minor + ✅ 良好实践

- **`API_BASE` 硬编码 `http://localhost:8080`（Major）**：部署到 GitHub Pages 后将无法连通后端。代码注释已写"部署时改为实际后端域名"，但属必须处理的部署阻断项。
  - **建议**：改为相对/配置化（如 `window.ALGO_API_BASE || 'http://localhost:8080'`），或构建期注入。
- **XSS 防护（✅ 良好）**：全程 `textContent` / `createElement`，不拼 `innerHTML`，渲染用户输入与后端返回均安全。
- **错误处理（✅ 良好）**：`fetchJson` 校验 `res.ok` 与 `content-type`，各 `call*` 有 `.catch` + `setError`，导出失败回退到可见面板展示。
- **`parseBubbleInput` 校验（✅ 良好）**：逗号/中文逗号/空白分隔，`Number.isFinite` + `Number.isInteger` 双校验，拒绝非整数。
- **`exportAll` filename 解析（Minor）**：正则 `filename="?([^"]+)"?` 只匹配 `filename="..."` 形态；后端当前返回纯 ASCII filename，可正常工作。若后端将来用 RFC 5987 `filename*=UTF-8''...`，正则不匹配（不影响当前）。
- **`decodeURIComponent`（Minor）**：后端 filename 纯 ASCII，`decodeURIComponent` 无影响；若后端返回含 `%` 的非编码串会抛 `URIError`（当前不触发）。
- **大整数精度（Minor）**：`Number(parts[i])` 超过 `Number.MAX_SAFE_INTEGER` 精度丢失但 `isInteger` 仍 true；demo 小数组可接受。
- **Tab 键盘可达性（✅ 良好）**：`tabindex="0"` + Enter/Space 激活，符合 ARIA tab 模式。

#### 3.12 `page/algo-demo/index.html` — ✅ 无问题
- 语义化 ARIA（`role=tablist/tab/tabpanel`），三 Tab + 双导出按钮，深色主题 CSS 内联，`<script>` 引用 `/js/algo-demo.js`。
- 输入控件 `autocomplete="off"` 合理。

---

## 四、问题汇总表

| # | 严重度 | 仓库 | 文件 | 问题 | 建议 |
|---|--------|------|------|------|------|
| 1 | Major | leecode | config/CorsConfig.java | `allowedOriginPatterns("*")` + `allowCredentials(true)` 全开放带凭证 | 演示豁免；生产改白名单，登记已知项 |
| 2 | Major | leecode | controller/AlgoController.java + service/ExportService.java | 导出用固定示例数据，不反映用户当前页面结果 | 明示为示例导出，或改为接收前端实时结果 |
| 3 | Major | haikulou1.github.io | js/algo-demo.js | `API_BASE` 硬编码 localhost，部署阻断 | 配置化或构建期注入 |
| 4 | Major | leecode | controller/AlgoController.java | 导出/算法异常冒泡 500，无统一错误体 | 加 `@ExceptionHandler` 或白名单前置校验 |
| 5 | Minor | leecode | service/HashService.java | 算法名无白名单，非法值抛 500 | 加 `Set<String>` 白名单校验 |
| 6 | Minor | leecode | service/HashService.java | MD5 选项（已不抗碰撞） | 演示可保留，UI 标注非安全用途 |
| 7 | Minor | leecode | service/BubbleSortService.java | 双重 `ArrayList` 复制冗余 | 合并为单次复制 |
| 8 | Minor | leecode | service/ExportService.java | `LocalDateTime.now()` 默认时区 | 可用 `ZonedDateTime` 显式时区 |
| 9 | Minor | leecode | controller/AlgoController.java | OutputStream 未 try-with-resources | 容器管理，可选包裹 |
| 10 | Minor | haikulou1.github.io | js/algo-demo.js | filename 正则不兼容 RFC 5987 | 当前可用，未来扩展时增强 |
| 11 | Minor | haikulou1.github.io | js/algo-demo.js | `decodeURIComponent` 对非编码含 `%` 串可能抛错 | 当前不触发，可加 try-catch |
| 12 | Minor | haikulou1.github.io | js/algo-demo.js | 大整数精度丢失 | demo 可接受 |

---

## 五、跨仓对齐点检查结论

| 对齐点 | 结论 |
|--------|------|
| 路由路径 | ✅ 前端 `/api/*` 与后端 `@RequestMapping("/api")` 完全匹配 |
| 请求字段 | ✅ hash `{input,algorithm}`、bubble `{input:[...]}` 与后端 DTO 一致 |
| 响应字段 | ✅ 前端读取的所有字段（result/timestamp/input/algorithm/hash/length/sorted/swapCount/steps/round/swaps/array）后端均提供 |
| 导出契约 | ✅ 前端 GET `?tab=all` + 解析 Content-Disposition，后端 setHeader 一致 |
| CORS | ✅ 后端放开 `/api/**`，前端跨域 fetch 可通（无需凭证） |
| 端口 | ✅ 前端 `API_BASE:8080` 与后端 `server.port:8080` 一致 |

**结论**：跨仓接口契约完整对齐，无断裂；主要改进项集中在导出语义（#2）与部署硬编码（#3），均为非阻塞 Major，演示场景可通过登记已知项推进，建议生产化前处理。

---

## 六、评审结论

- **Blocker：0**（无安全漏洞导致 RCE/数据泄露、无运行时崩溃、无契约断裂）
- **Major：4**（CORS 全开放、导出示例化、前端硬编码、异常未统一处理）
- **Minor：8**（算法白名单、MD5、冗余复制、时区、流关闭、filename 正则、decodeURI、大整数）
- **建议**：演示场景可合并；生产化前优先处理 #2（导出语义）与 #3（API_BASE 硬编码），并将 #1（CORS）收紧为白名单。

评审通过（有条件）。

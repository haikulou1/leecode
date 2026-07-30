# 代码评审报告（复审 R3）：新增 Java 接口与前端展示导出页面

> **评审日期**：2026-07-30  
> **评审阶段**：review（代码评审，只读 · 复审第 3 轮）  
> **评审技能**：code-review-skill  
> **涉及仓库**：leecode（后端 Spring Boot）/ haikulou1.github.io（前端静态页）  
> **契约基线**：`docs/design/2026-07-30-helloworld-hash-bubble-design.md` + `docs/superpowers/specs/2026-07-30-algo-api-frontend-export-clarify.md` + `.agents/plans/algo-api-frontend-plan.md`  
> **前次报告（R2）**：blocker_count=1（B2 前端未解包 data 层），verdict=request_changes

---

## 0. 复审摘要

| 严重度 | 数量 | 说明 |
|--------|------|------|
| **blocking** | 0 | R2 的 B2（callApi 未解包 data 层）已彻底修复，三 Tab 全链路恢复 |
| **important** | 0 | R1/R2 的 I1-I6 全部已修复且无回归 |
| **nit / suggestion** | 3 | 契约未同步 GET + 测试冗余 import + BASE_URL 生产占位符 |
| **praise** | 6 | R2 的 5 项保留 + 新增 1 项 B2 回归修复质量肯定 |

**blocker_count = 0**

**评审结论**：✅ **Approve（可合并）**。R2 唯一 blocking（B2：前端 `callApi` `return body` 未解包 `ApiResult.data` 层，致三 Tab 全失效、bubble Tab `TypeError` 崩溃）已精准修复：`callApi` 现改为 `return body.data !== undefined ? body.data : body`（L72），正确解包 `ApiResult{code,message,data}` 的 `data` 层。前后端响应结构契约现已对齐，各 `call*` 函数无需改动即正确读取业务字段。无新发现 blocking/important。剩余 nit/suggestion 均不阻断合并，可择机处理。

---

## 1. 评审范围

### 后端（leecode / algo-api 模块）
| 文件 | 行数 | 职责 |
|------|------|------|
| `pom.xml` | 44 | Maven 构建配置（Spring Boot 2.7.18 / Java 8） |
| `AlgoApiApplication.java` | 15 | Spring Boot 启动类 |
| `config/CorsConfig.java` | 21 | 全局 CORS 配置 |
| `controller/AlgoController.java` | 110 | 四接口控制器（hello/hash/bubble/export） |
| `exception/GlobalExceptionHandler.java` | 53 | 全局异常兜底 |
| `model/ApiResult.java` | 44 | 统一响应体模型 |
| `service/HashService.java` | 50 | SHA-256 哈希服务 |
| `service/BubbleSortService.java` | 32 | 冒泡排序服务 |
| `service/ExportService.java` | 98 | CSV 导出服务 |
| `application.yml` | 6 | 服务端口 + 应用名配置 |
| `test/.../AlgoControllerTest.java` | 120 | MockMvc 切片测试（8 测试） |
| `test/.../HashServiceTest.java` | 46 | 哈希服务单测 |
| `test/.../BubbleSortServiceTest.java` | 51 | 冒泡排序单测 |
| `test/.../ExportServiceTest.java` | 57 | 导出服务单测（含 CSV 防注入测试） |

### 前端（haikulou1.github.io）
| 文件 | 行数 | 职责 |
|------|------|------|
| `algo-demo/index.html` | 77 | 三 Tab 页面 + 导出按钮 UI |
| `js/algo-demo.js` | 241 | fetch 调用 + Tab 切换 + 导出 + 错误兜底 |

---

## 2. 前次 CR 问题修复核对（R2 → R3）

| 编号 | 前次严重度 | 问题 | 修复状态 | 证据 |
|------|-----------|------|----------|------|
| B1 | blocking | XSS：hash input 原样回显 + 前端 innerHTML | ✅ 已修复（保持） | 前端全改 `textContent`/DOM API：`createResultRow` L83-94（`labelSpan.textContent`/`valueSpan.textContent`）、`renderError` L100-109（`div.textContent`）；后端 `data.input` 回显原文（H1 有意偏离，XSS 由渲染层兜底）；测试 `hash_withXssInput_inputIsRawEchoed` L59-65 |
| **B2** | **blocking** | **前端 callApi 未解包 data 层，三 Tab 全失效** | ✅ **已修复** | **`callApi` L72：`return body.data !== undefined ? body.data : body;`——检测到 `ApiResult` 统一体（`body.code` 为 number 且 `!==0` 已在 L67-69 拦截业务错误）后解包 `data` 层返回，各 `call*` 函数读取 `data.result`/`data.input`/`data.algorithm`/`data.hash`/`data.sorted`/`data.warning` 正确指向业务字段** |
| I1 | important | 响应体偏离统一契约 | ✅ 已修复（保持） | 后端 `ApiResult` L7-44（`final` 字段+私有构造+静态工厂 `ok`/`fail`）；四接口返回 `ApiResult.ok(data)` L47/L61/L89；异常处理器返回 `ApiResult.fail` L31/L40/L51 |
| I2 | important | 无单元测试 | ✅ 已修复（保持） | 4 个测试类：AlgoControllerTest(8)+HashServiceTest(4)+BubbleSortServiceTest(4)+ExportServiceTest(5，含 CSV 防注入) |
| I3 | important | BASE_URL 硬编码 localhost | ✅ 已修复（保持） | L18-21 环境自适应 + `window.ALGO_API_BASE_URL` 运行时注入（M2） |
| I4 | important | 导出 iframe.onerror 不可靠 | ✅ 已修复（保持） | L201-238 `fetch`+`Blob`+`Content-Disposition` 解析+`URL.createObjectURL`+30s 超时+`alert` 错误反馈 |
| I5 | important | CORS allowCredentials(true) | ✅ 已修复（保持） | CorsConfig L15-19：`allowedOrigins("*")`+`allowedMethods("GET","OPTIONS")`+`allowCredentials(false)` |
| I6 | important | HashService 伪哈希 | ✅ 已修复（保持） | HashService L33-36：极端兜底改为 `throw new IllegalStateException` |
| N2 | suggestion | 异常处理器不记日志 | ✅ 已修复（保持） | GlobalExceptionHandler L23/L49：`slf4j Logger`+`log.error("未捕获异常", e)` |
| N3 | suggestion | CSV 无防注入 | ✅ 已修复（保持） | ExportService L88-97 `sanitize()`；ExportServiceTest L51-56 防注入测试 |
| N4 | nit | application.yml 简陋 | ✅ 已修复（保持） | application.yml L4-6 `spring.application.name: algo-api` |
| N5 | nit | AlgoControllerTest 冗余 @Autowired 注入 | ✅ 已修复 | R2 报告的 3 个未使用 `@Autowired` Service 字段已移除；当前 AlgoControllerTest L24-25 仅 `@Autowired MockMvc mockMvc`（其余 import 仍见 N6） |
| M2 | nit | BASE_URL 硬编码占位符 | ✅ 已修复 | L18-21 支持 `window.ALGO_API_BASE_URL` 运行时注入，部署时可覆盖占位符 |

---

## 3. B2 修复验证（本轮重点）

### 现象与修复对比

R2 报告 B2：前端 `callApi`（R2 时 L57-63）获取响应后直接 `return body`（返回整个统一体对象），导致各 `call*` 函数读 `data.*` 实际指向 `body.code/message/data` 整体的字段而非 `body.data.*` 业务字段：

| 函数 | 期望读取 | R2 实际值 | R2 后果 |
|------|----------|-----------|---------|
| `callHello` | `body.data.result`=`"HelloWorld"` | `undefined` | 显示"结果：undefined" |
| `callHash` | `body.data.input`/`algorithm`/`hash` | `undefined` | 全显 undefined |
| `callBubble` | `body.data.input.join()` | `undefined` | **`TypeError` 崩溃** |

### R3 修复证据

`js/algo-demo.js` L51-78 `callApi`（当前版本）：

```javascript
async function callApi(url) {
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), 5000);
    try {
        const res = await fetch(url, { signal: ctrl.signal });
        if (!res.ok) {
            // HTTP 错误：解析统一错误体 {code,message,data}
            ...
            return { _error: '...' };
        }
        const body = await res.json();
        // 统一响应体兜底：code 非 0 视为业务错误
        if (body && typeof body.code === 'number' && body.code !== 0) {
            return { _error: body.message || ('错误码 ' + body.code) };
        }
        // B1: 统一响应体——解包 data 层，使调用方直接拿到业务数据
        // 后端 AlgoController 所有接口均通过 ApiResult.ok(data) 包裹，data 字段必存在
        return body.data !== undefined ? body.data : body;  // ← R2 的 return body 已修正
    } catch (e) { ... }
    finally { clearTimeout(timer); }
}
```

### 端到端数据流验证

后端 `AlgoController.hello()`（L43-48）返回 `ApiResult.ok(data)`，其中 `data={"result":"HelloWorld"}`，序列化为：
```json
{ "code": 0, "message": "ok", "data": { "result": "HelloWorld" } }
```

前端 `callApi`：`body.code` 为 number 0，`!==0` 为 false，跳过业务错误分支；`body.data !== undefined` 为 true → 返回 `body.data`=`{"result":"HelloWorld"}`。

| 函数 | 读取 | 解包后值 | 结论 |
|------|------|----------|------|
| `callHello` L132 | `data.result` | `"HelloWorld"` | ✅ |
| `callHash` L149-151 | `data.input`/`algorithm`/`hash` | `"hello"`/`"SHA-256"`/hex | ✅ |
| `callBubble` L168 `data.warning` | `undefined`（合法输入） | 不显示（非致命） | ✅ |
| `callBubble` L174 `data.input.join(', ')` | `[5,3,8,1,9,2]` | `"5, 3, 8, 1, 9, 2"` | ✅ 不再 TypeError |
| `callBubble` L175 `data.sorted.join(', ')` | `[1,2,3,5,8,9]` | `"1, 2, 3, 5, 8, 9"` | ✅ |

**B2 回归已彻底消除**：bubble Tab 不再抛 `TypeError: Cannot read properties of undefined (reading 'join')`，三 Tab 展示全链路恢复，导出功能（不依赖 `callApi` 返回值，按 `currentTab` 拼 URL）本就未受影响，现前端预览→导出体验链路闭合。

### 后端契约一致性佐证

`ApiResult`（L7-44）使用 `final` 字段 + 私有构造 + 静态工厂：
- `ok(data)` → `new ApiResult(0, "ok", data)`，`data` 必为传入值（非 null，因各接口均传入非 null Map）。
- `fail(code,message)` → `data=null`，但失败路径前端已由 `body.code !== 0` 在 L67-69 拦截返回 `_error`，不会走到 L72。
- 因此 L72 `body.data !== undefined` 的 `undefined` 兜底分支在成功路径永不触发（`data` 恒存在），属防御性冗余但无害——保留作为裸 Map 向后兼容兜底可接受。

> **降级说明**：本机环境无 `java`/`mvn`（`which` 与常见 JDK 路径扫描均未命中），无法执行 `mvn test` 实跑验证。改为静态审查：逐行核对 `callApi` 解包逻辑与各 `call*` 字段读取路径，并对照 `ApiResult.ok/fail` 契约与后端 `AlgoController` 四接口返回值，确认数据流闭合无断裂。降级不影响 B2 修复结论——证据为源码逻辑级可证。

---

## 4. 新发现 Blocking / Important

**无**。本轮在 R2 基础上重点复核 B2 修复点与全量代码，未发现新的 blocking 或 important 问题。

复核覆盖点（均通过）：
- `HashService.toHex()`（L43-48）`String.format("%02x", b)` 字节转 hex：`%x` 转换对 `Byte` 参数按无符号掩码处理，负字节（≥0x80，如 SHA-256("hello") 中大量字节）输出 2 字符 hex，`HashServiceTest` L22 `assertEquals(64, result[1].length())` 与 `hash_normalString_returnsSha256` 一致。✅
- `ExportService.sanitize()`（L88-97）CSV 防注入：`= + - @` 首字符加 `'` 前缀，`ExportServiceTest` L51-56 `=cmd|'/c calc'!A1` → `'=cmd` 验证。✅
- `GlobalExceptionHandler`（L28-52）三种异常处理器覆盖 `IllegalArgumentException`→400 / `MissingServletRequestParameterException`→400 / `Exception`→500+日志，错误体统一 `ApiResult.fail`，不暴露堆栈。✅
- `CorsConfig`（L14-19）`allowedOrigins("*")`+`allowCredentials(false)`：GET-only 无凭据的公开演示 API，`*`+`credentials=false` 安全可接受。✅
- 前端 XSS：`createResultRow`/`renderError` 全 `textContent`/DOM API，无 `innerHTML`。✅
- 导出 `Content-Disposition` 解析（L219）`/filename="?([^"]+)"?/` 匹配后端 `attachment; filename=<type>.csv`。✅

---

## 5. 遗留 Nit / Suggestion（3 项，均不阻断合并）

### N1 — hash/bubble 接口用 GET+query 而非设计的 POST+body（前次遗留）

- **严重度**：`suggestion`
- **状态**：⏳ 未处理（实现合理，低优先级）
- **说明**：前次报告已评估 GET+query 比 POST+body 更符合 RESTful 语义。契约文档 `clarify.md` 仍写 POST，代码注释未标注此为有意偏离。建议同步文档或标注有意偏离。

### N6 — AlgoControllerTest 冗余 import（新增）

- **严重度**：`nit`
- **位置**：`AlgoControllerTest.java` L3 `import algoapi.model.ApiResult;`
- **现象**：`ApiResult` 在测试方法中未被直接引用（测试通过 `jsonPath` 断言响应体，不操作 `ApiResult` 对象），属 R2 N5 移除 `@Autowired` Service 字段时遗留的未清理 import。
- **影响**：无功能影响，纯整洁度。
- **建议**：删除 L3 未使用 import。

### N7 — BASE_URL 生产域名占位符（前次 I3/M2 衍生）

- **严重度**：`nit`
- **位置**：`js/algo-demo.js` L18-21
- **现象**：生产域名 `https://algo-api.example.com/api` 为占位符，部署前需通过 `window.ALGO_API_BASE_URL` 注入实际域名或在 `index.html` 中设置。
- **影响**：I3/M2 已提供运行时注入机制，占位符本身不阻断功能，但部署清单应明确此项。
- **建议**：在部署文档或 `index.html` 注释中标注"部署前必须设置 `window.ALGO_API_BASE_URL`"。

---

## 6. Praise（肯定项，6 项）

### P1 — BubbleSortService 使用 Arrays.copyOf 不修改原数组（前次保留）
- **位置**：`BubbleSortService.java` L19
- **评价**：`int[] a = Arrays.copyOf(arr, arr.length)` 确保传入原始数组不被修改，符合防御性编程原则。

### P2 — bubble 接口入参非法时优雅兜底（前次保留）
- **位置**：`AlgoController.java` L72-86
- **评价**：输入解析失败时回退默认数组并标记 `warning` 字段，容错策略优雅透明。

### P3 — 前端 AbortController 5s 超时 + 错误兜底（前次保留）
- **位置**：`algo-demo.js` L51-78
- **评价**：`callApi` 封装 5 秒超时和统一 `_error` 兜底，网络失败时显示红色提示。

### P4 — 后端统一响应体 + 全局异常处理器设计完整（前次保留）
- **位置**：`ApiResult.java` L7-44 / `GlobalExceptionHandler.java` L20-52
- **评价**：`ApiResult` 不可变对象（`final` 字段+私有构造+静态工厂），三种异常处理器覆盖完整，错误体统一包裹 `ApiResult.fail`，不暴露堆栈。

### P5 — CSV 防注入 + 测试覆盖（前次保留）
- **位置**：`ExportService.java` L88-97 / `ExportServiceTest.java` L51-56
- **评价**：`sanitize()` 防注入 + 对应测试护栏。

### P6 — B2 回归修复精准、修复面最小（新增）
- **位置**：`js/algo-demo.js` L72（`callApi` 返回值）
- **评价**：R2 的 B2 是 CR 修复 I1（统一响应体）时前后端不同步引入的 blocking 回归。本轮修复未采用"各 `call*` 函数改读 `body.data.*`"的宽面改法，而是回归 `callApi` 单点 `return body.data !== undefined ? body.data : body`，解包一次、调用方零改动——修复面最小、风险最低、与各 `call*` 的字段读取完全对齐。注释亦同步说明"后端所有接口均通过 `ApiResult.ok(data)` 包裹，data 字段必存在"，契约意图清晰。回归被精准收敛。

---

## 7. 跨仓对齐点检查

| 对齐点 | 后端（leecode） | 前端（haikulou1.github.io） | 一致性结论 |
|--------|----------------|---------------------------|-----------|
| 接口路径 | `/api/hello` `/api/hash` `/api/bubble` `/api/export` | `BASE_URL + '/hello'` `'/hash?input='` `'/bubble?nums='` `'/export?type='` | ✅ 一致 |
| HTTP 方法 | 全部 GET | 全部 fetch GET | ✅ 一致 |
| **响应结构** | `ApiResult{code,message,data}` 统一体 | `callApi` 解包 `body.data`，各 call 读 `data.*` | ✅ **一致（B2 已修复）** |
| type 枚举 | `hello`/`hash`/`bubble` | `currentTab` = `hello`/`hash`/`bubble` | ✅ 一致 |
| 导出文件名 | `attachment; filename=<type>.csv` | `Content-Disposition` 正则解析 + `a.download` | ✅ 一致 |
| 导出参数透传 | `export(type, input, nums)` | `exportResult` 按 currentTab 拼 `input`/`nums` | ✅ 一致 |
| 端口 | `application.yml: server.port: 8080` | `BASE_URL` 开发期 `http://localhost:8080/api` | ✅ 一致（开发期） |
| CORS | `allowedOrigins("*")` + `allowCredentials(false)` | 前端无特殊处理 | ✅ 一致且安全 |
| 出参字段 | hello→`{result}`; hash→`{input,algorithm,hash}`; bubble→`{input,sorted,warning?}` | 读 `data.result`/`data.input`/`data.algorithm`/`data.hash`/`data.sorted`/`data.warning` | ✅ 字段名 + 嵌套层级均匹配 |
| 算法输入参数名 | hash: `input`; bubble: `nums` | hash: `?input=`; bubble: `?nums=` | ✅ 一致 |
| BASE_URL 生产域名 | 后端部署域名待定 | `https://algo-api.example.com/api`（占位符，可 `window.ALGO_API_BASE_URL` 覆盖） | ⚠️ 占位符（N7，部署前处理） |

---

## 8. 修复优先级建议

| 优先级 | 编号 | 问题 | 修复成本 |
|--------|------|------|----------|
| — | — | 无 blocking/important | — |
| P3 | N1 | 契约文档同步 GET 或标注有意偏离 | 低 |
| P3 | N6 | 删除 AlgoControllerTest 未使用 import `ApiResult` | 低（删 1 行） |
| P3 | N7 | 部署文档标注 `window.ALGO_API_BASE_URL` 必填 | 低 |

---

## 9. 评审结论

本次复审（R3）确认 R2 唯一 blocking（B2：前端 `callApi` 未解包 `ApiResult.data` 层，致三 Tab 全失效、bubble Tab `TypeError` 崩溃）已精准修复：`callApi` 改为 `return body.data !== undefined ? body.data : body`（L72），正确解包统一响应体的 `data` 层。逐行核对端到端数据流后，各 `call*` 函数的 `data.result`/`data.input`/`data.algorithm`/`data.hash`/`data.sorted`/`data.warning` 现正确指向业务字段，bubble Tab 不再抛 `TypeError`，三 Tab 展示全链路恢复，前端预览→导出体验链路闭合。

R1 的 B1（XSS blocking）及 I1-I6（important）、N2-N4（nit）在 R2 已修复且本轮复核保持有效，无回归。M2（BASE_URL 运行时注入）已落地。N5（冗余 @Autowired）已清理。

本轮无新发现 blocking/important。剩余 nit/suggestion（N1 契约同步、N6 冗余 import、N7 生产域名占位符）均不阻断合并，可择机处理。

**综合判定**：✅ **Approve（可合并）**

blocker_count = 0

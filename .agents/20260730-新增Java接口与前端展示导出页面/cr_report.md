# 代码评审报告（复审 R2）：新增 Java 接口与前端展示导出页面

> **评审日期**：2026-07-30  
> **评审阶段**：review（代码评审，只读 · 复审第 2 轮）  
> **评审技能**：code-review-skill  
> **涉及仓库**：leecode（后端 Spring Boot）/ haikulou1.github.io（前端静态页）  
> **契约基线**：`docs/design/2026-07-30-helloworld-hash-bubble-design.md` + `docs/superpowers/specs/2026-07-30-algo-api-frontend-export-clarify.md` + `.agents/plans/algo-api-frontend-plan.md`  
> **前次报告**：blocker_count=1（B1 XSS），verdict=request_changes

---

## 0. 复审摘要

| 严重度 | 数量 | 说明 |
|--------|------|------|
| **blocking** | 1 | 前端未解包统一响应体 data 层，三 Tab 展示全部失效（回归） |
| **important** | 0 | 前次 6 项 important（I1-I6）全部已修复 |
| **nit / suggestion** | 2 | 契约未同步 + 测试冗余注入 |
| **praise** | 5 | 前次 3 项保留 + 新增 2 项修复质量肯定 |

**blocker_count = 1**

**评审结论**：⚠️ **不可直接合并**。前次 blocking B1（XSS）已修复，但前次 important I1（统一响应体）修复不完整——后端正确包裹 `ApiResult{code,message,data}`，前端 `callApi` 未解包 `data` 层，导致三 Tab 展示全部失效（bubble Tab 运行时 `TypeError` 崩溃）。此为 CR 修复阶段引入的回归，需修复后重新评审。

---

## 1. 评审范围

### 后端（leecode / algo-api 模块）
| 文件 | 行数 | 职责 |
|------|------|------|
| `pom.xml` | 44 | Maven 构建配置（Spring Boot 2.7.18 / Java 8） |
| `AlgoApiApplication.java` | 15 | Spring Boot 启动类 |
| `config/CorsConfig.java` | 21 | 全局 CORS 配置 |
| `controller/AlgoController.java` | 143 | 四接口控制器（hello/hash/bubble/export） |
| `exception/GlobalExceptionHandler.java` | 53 | 全局异常兜底 |
| `model/ApiResult.java` | 44 | 统一响应体模型（**新增**） |
| `service/HashService.java` | 50 | SHA-256 哈希服务 |
| `service/BubbleSortService.java` | 32 | 冒泡排序服务 |
| `service/ExportService.java` | 98 | CSV 导出服务 |
| `application.yml` | 6 | 服务端口 + 应用名配置 |
| `test/.../AlgoControllerTest.java` | 130 | MockMvc 切片测试（**新增**） |
| `test/.../HashServiceTest.java` | 46 | 哈希服务单测（**新增**） |
| `test/.../BubbleSortServiceTest.java` | 51 | 冒泡排序单测（**新增**） |
| `test/.../ExportServiceTest.java` | 57 | 导出服务单测（**新增**） |

### 前端（haikulou1.github.io）
| 文件 | 行数 | 职责 |
|------|------|------|
| `algo-demo/index.html` | 77 | 三 Tab 页面 + 导出按钮 UI |
| `js/algo-demo.js` | 232 | fetch 调用 + Tab 切换 + 导出 + 错误兜底 |

---

## 2. 前次 CR 问题修复核对

| 编号 | 前次严重度 | 问题 | 修复状态 | 证据 |
|------|-----------|------|----------|------|
| B1 | blocking | XSS：hash input 原样回显 + 前端 innerHTML | ✅ 已修复 | 后端 `escapeHtml(input)` L58/L114-142；前端全改 `textContent`/DOM API（`createResultRow` L74-85，`renderError` L91-100）；测试 `hash_withXssInput_inputIsEscaped` L70-75 |
| I1 | important | 响应体偏离统一契约 | ⚠️ **部分修复（引入回归）** | 后端 `ApiResult` 已实现 L22-31，四接口返回 `ApiResult.ok(data)` L47/L61/L89；异常处理器返回 `ApiResult.fail` L31/L40/L51。**但前端 `callApi` 未解包 data 层** → 见 B2 |
| I2 | important | 无单元测试 | ✅ 已修复 | 4 个测试类共 ~284 行：AlgoControllerTest(8 测试) + HashServiceTest + BubbleSortServiceTest + ExportServiceTest(含 CSV 防注入测试) |
| I3 | important | BASE_URL 硬编码 localhost | ✅ 已修复 | L13-15 环境自适应：`location.hostname === 'localhost' \|\| '127.0.0.1' ? dev : prod` |
| I4 | important | 导出 iframe.onerror 不可靠 | ✅ 已修复 | L174-229 改用 `fetch` + `Blob` + `Content-Disposition` 解析 + `URL.createObjectURL` + 30s 超时 + `alert` 错误反馈 |
| I5 | important | CORS allowCredentials(true) | ✅ 已修复 | CorsConfig L15-19：`allowedOrigins("*")` + `allowedMethods("GET","OPTIONS")` + `allowedHeaders("*")` + `allowCredentials(false)` |
| I6 | important | HashService 伪哈希 | ✅ 已修复 | HashService L33-36：极端兜底改为 `throw new IllegalStateException` |
| N1 | suggestion | GET vs POST 契约偏离 | ⏳ 未处理 | 仍为 GET+query，契约文档未同步（实现合理，低优先级） |
| N2 | suggestion | 异常处理器不记日志 | ✅ 已修复 | GlobalExceptionHandler L23/L49：注入 `slf4j Logger` + `log.error("未捕获异常", e)` |
| N3 | suggestion | CSV 无防注入 | ✅ 已修复 | ExportService L88-97 `sanitize()` 方法 + ExportServiceTest L52-56 防注入测试 |
| N4 | nit | application.yml 简陋 | ✅ 已修复 | application.yml L4-6 补充 `spring.application.name: algo-api` |

---

## 3. 新发现 Blocking（1 项）

### B2 — 前端未解包统一响应体 data 层，三 Tab 展示全部失效

- **严重度**：`blocking`
- **类型**：CR 修复回归（I1 统一响应体修复前后端不同步）
- **后端位置**：`AlgoController.java` L44-48 / L56-61 / L86-89
- **前端位置**：`js/algo-demo.js` L57-63（`callApi` 返回值）/ L123（`callHello`）/ L140-142（`callHash`）/ L165-166（`callBubble`）

#### 现象

后端 I1 修复后，所有正常接口返回统一响应体 `ApiResult`：
```json
// /api/hello 实际返回
{ "code": 0, "message": "ok", "data": { "result": "HelloWorld" } }
// /api/hash 实际返回
{ "code": 0, "message": "ok", "data": { "input": "hello", "algorithm": "SHA-256", "hash": "2cf2..." } }
// /api/bubble 实际返回
{ "code": 0, "message": "ok", "data": { "input": [5,3,8,1,9,2], "sorted": [1,2,3,5,8,9] } }
```

前端 `callApi`（L57-63）获取响应后直接 `return body`（返回整个统一体对象）：
```javascript
const body = await res.json();
if (body && typeof body.code === 'number' && body.code !== 0) {
    return { _error: body.message || ('错误码 ' + body.code) };
}
// 正常业务 JSON（裸 Map 或 code=0 的统一体），原样返回
return body;  // ← 返回 {code,message,data} 整体，未解包 data 层
```

各 `call*` 函数直接从返回值读取业务字段，但业务字段实际嵌套在 `body.data` 内：

| 函数 | 代码 | 期望读取 | 实际值 | 后果 |
|------|------|----------|--------|------|
| `callHello` L123 | `data.result` | `body.data.result` = `"HelloWorld"` | `undefined` | 显示 "结果：undefined" |
| `callHash` L140 | `data.input` | `body.data.input` = `"hello"` | `undefined` | 显示 "输入：undefined" |
| `callHash` L141 | `data.algorithm` | `body.data.algorithm` = `"SHA-256"` | `undefined` | 显示 "算法：undefined" |
| `callHash` L142 | `data.hash` | `body.data.hash` = `"2cf2..."` | `undefined` | 显示 "哈希：undefined" |
| `callBubble` L165 | `data.input.join(', ')` | `body.data.input` = `[5,3,...]` | `undefined` | **`TypeError: Cannot read properties of undefined (reading 'join')`** → 运行时崩溃 |
| `callBubble` L166 | `data.sorted.join(', ')` | `body.data.sorted` = `[1,2,...]` | `undefined` | 同上 |
| `callBubble` L159 | `data.warning` | `body.data.warning` | `undefined` | warning 不显示（非致命） |

#### 影响

- **hello Tab**：展示 "结果：undefined"——功能失效。
- **hash Tab**：展示 "输入：undefined / 算法：undefined / 哈希：undefined"——功能失效。
- **bubble Tab**：`data.input.join()` 抛 `TypeError`，JavaScript 运行时崩溃，**页面无任何展示**，控制台报错——功能完全不可用。
- **导出功能**：`exportResult`（L176）使用 `currentTab` 拼 URL，不依赖 `callApi` 返回值，**导出本身不受影响**——但用户无法先查看结果再导出，体验链路断裂。

#### 根因

前端 `callApi` 注释（L40-42）写道：
> 兼容两种响应：后端正常接口直接返回业务 JSON（如 `{"result":"HelloWorld"}`），异常时由 GlobalExceptionHandler 返回统一响应体 `{code,message,data}`。

这说明前端开发者**误以为**正常接口仍返回裸 Map（前次 CR 报告 I1 描述的状态），只有异常路径才返回统一体。但后端 I1 修复后，**所有接口均返回 `ApiResult` 统一体**，前端假设已过时，未同步适配。前后端修复不同步导致回归。

#### 修复建议

`callApi` 在检测到 `code === 0` 的统一响应体时，应返回 `body.data` 而非 `body`：

```javascript
const body = await res.json();
if (body && typeof body.code === 'number') {
    if (body.code !== 0) {
        return { _error: body.message || ('错误码 ' + body.code) };
    }
    // 统一响应体 code=0：返回 data 层（业务字段在此层内）
    return body.data;
}
// 兜底：裸 Map 兼容（防御性保留，理论不再出现）
return body;
```

修复后各 `call*` 函数的 `data.result` / `data.input` / `data.algorithm` / `data.hash` / `data.sorted` / `data.warning` 即可正确读取业务字段，无需改动调用方。

> **验证方法**：修复后 `callBubble` 的 `data.input.join(', ')` 应输出 `[5, 3, 8, 1, 9, 2]`，不再抛 `TypeError`。建议补充前端集成测试或手动验证三 Tab 展示 + 导出全链路。

---

## 4. 遗留 Nit / Suggestion（2 项）

### N1 — hash/bubble 接口用 GET+query 而非设计的 POST+body（前次遗留）

- **严重度**：`suggestion`（未升级，未降级）
- **状态**：⏳ 未处理（实现合理，低优先级）
- **说明**：前次报告已评估 GET+query 比 POST+body 更符合 RESTful 语义。代码注释未标注此为有意偏离，契约文档 clarify.md 仍写 POST。建议同步文档或标注有意偏离。

### N5 — AlgoControllerTest 冗余 @Autowired 注入（新增）

- **严重度**：`nit`
- **位置**：`AlgoControllerTest.java` L31-37
- **现象**：`hashService` / `bubbleSortService` / `exportService` 三个 `@Autowired` 字段被注入但从未在测试方法中使用（测试通过 `MockMvc` 调用接口，不直接调用 Service）。
- **影响**：无功能影响，但增加测试上下文加载开销，且暗示测试可能曾计划直接调用 Service 后改为 MockMvc 但未清理。
- **建议**：移除三个未使用的 `@Autowired` 字段。

---

## 5. Praise（肯定项，5 项）

### P1 — BubbleSortService 使用 Arrays.copyOf 不修改原数组（前次保留）

- **位置**：`BubbleSortService.java` L19
- **评价**：`int[] a = Arrays.copyOf(arr, arr.length)` 确保传入的原始数组不被修改，符合防御性编程原则。

### P2 — bubble 接口入参非法时优雅兜底（前次保留）

- **位置**：`AlgoController.java` L72-85
- **评价**：输入解析失败时回退默认数组并标记 `warning` 字段，容错策略优雅透明。

### P3 — 前端 AbortController 5s 超时 + 错误兜底（前次保留）

- **位置**：`algo-demo.js` L43-69
- **评价**：`callApi` 封装 5 秒超时和统一 `_error` 兜底，网络失败时显示红色提示。

### P4 — 后端统一响应体 + 全局异常处理器设计完整（新增）

- **位置**：`ApiResult.java` L7-44 / `GlobalExceptionHandler.java` L20-52
- **评价**：I1/I6/N2 修复质量高——`ApiResult` 不可变对象（`final` 字段 + 私有构造 + 静态工厂），三种异常处理器（`IllegalArgumentException`→400 / `MissingServletRequestParameterException`→400 / `Exception`→500+日志）覆盖完整，错误体统一包裹 `ApiResult.fail`，不暴露堆栈。设计规范。

### P5 — CSV 防注入 + 测试覆盖（新增）

- **位置**：`ExportService.java` L88-97 / `ExportServiceTest.java` L52-56
- **评价**：N3 修复不仅加了 `sanitize()` 防注入，还补了对应测试（`=cmd|'/c calc'!A1` → `'=cmd` 前缀验证），防御性编程有测试护栏。

---

## 6. 跨仓对齐点检查

| 对齐点 | 后端（leecode） | 前端（haikulou1.github.io） | 一致性结论 |
|--------|----------------|---------------------------|-----------|
| 接口路径 | `/api/hello` `/api/hash` `/api/bubble` `/api/export` | `BASE_URL + '/hello'` `'/hash?input='` `'/bubble?nums='` `'/export?type='` | ✅ 一致 |
| HTTP 方法 | 全部 GET | 全部 fetch GET | ✅ 一致 |
| **响应结构** | **`ApiResult{code,message,data}` 统一体** | **`callApi` 返回 `body` 整体，各 call 读 `data.*` 而非 `body.data.*`** | ❌ **不一致（B2 回归）** |
| type 枚举 | `hello`/`hash`/`bubble` | `currentTab` = `hello`/`hash`/`bubble` | ✅ 一致 |
| 导出文件名 | `attachment; filename=<type>.csv` | `Content-Disposition` 正则解析 + `a.download` | ✅ 一致 |
| 导出参数透传 | `export(type, input, nums)` | `exportResult` 按 currentTab 拼 `input`/`nums` | ✅ 一致 |
| 端口 | `application.yml: server.port: 8080` | `BASE_URL` 开发期 `http://localhost:8080/api` | ✅ 一致（开发期） |
| CORS | `allowedOrigins("*")` + `allowCredentials(false)` | 前端无特殊处理 | ✅ 一致且安全 |
| 出参字段 | hello→`{result}`; hash→`{input,algorithm,hash}`; bubble→`{input,sorted,warning?}` | 读 `data.result`/`data.input`/`data.algorithm`/`data.hash`/`data.sorted`/`data.warning` | ⚠️ 字段名匹配但**嵌套层级错位**（见 B2） |
| 算法输入参数名 | hash: `input`; bubble: `nums` | hash: `?input=`; bubble: `?nums=` | ✅ 一致 |
| BASE_URL 生产域名 | 后端部署域名待定 | `https://algo-api.example.com/api`（占位符） | ⚠️ 占位符，部署前需替换（I3 修复可接受） |

---

## 7. 修复优先级建议

| 优先级 | 编号 | 问题 | 修复成本 |
|--------|------|------|----------|
| **P0（必须）** | B2 | 前端 callApi 解包 data 层：`return body.data` | 低（改 1 处，~3 行） |
| P3 | N1 | 契约文档同步 GET 或标注有意偏离 | 低 |
| P3 | N5 | 移除 AlgoControllerTest 冗余 @Autowired | 低（删 3 行） |

---

## 8. 评审结论

本次复审确认前次 CR 报告的 B1（XSS blocking）已彻底修复，I1-I6（important）与 N2-N4（nit）均已修复落地，修复质量整体良好——后端统一响应体、全局异常处理器、CORS 安全收敛、CSV 防注入、测试覆盖等均有实质改进。

但前次 I1（统一响应体）的修复存在**前后端不同步回归**：后端正确包裹 `ApiResult{code,message,data}`，前端 `callApi` 却 `return body` 未解包 `data` 层，导致三 Tab 展示全部失效——hello/hash Tab 显示 "undefined"，bubble Tab 因 `data.input.join()` 抛 `TypeError` 直接崩溃。此为 CR 修复阶段引入的 **blocking 回归**，必须修复后方可合并。

修复成本极低（`callApi` 改 1 处 `return body.data`），修复后无需改动各 `call*` 函数。

**综合判定**：⚠️ **Request Changes（需修改后重新评审）**

blocker_count = 1

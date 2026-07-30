# 代码评审报告：新增 Java 接口与前端展示导出页面（CR 修复复审）

> **评审日期**：2026-07-30
> **评审阶段**：review（代码评审，只读）
> **评审技能**：code-review-skill
> **涉及仓库**：leecode（后端 Spring Boot）/ haikulou1.github.io（前端静态页）
> **契约基线**：`docs/design/2026-07-30-helloworld-hash-bubble-design.md` + `.agents/plans/algo-api-frontend-plan.md`
> **复审范围**：首轮 CR 报告（B1/I1-I6/N1-N4）的修复验证 + 新增回归发现

---

## 0. 评审摘要

### 首轮修复验证结论

| 编号 | 首轮严重度 | 修复状态 | 验证证据 |
|------|-----------|---------|---------|
| B1 XSS | blocking | ✅ 已修复 | 后端 `escapeHtml()` L114-142 + 前端 `textContent`/DOM API；`AlgoControllerTest.hash_withXssInput_inputIsEscaped` 验证转义 |
| I1 统一响应体 | important | ✅ 后端已修复 / ⚠️ 引入新阻断（见 B2） | `ApiResult.java` + 4 接口返回 `ApiResult.ok(data)`；但前端 `callApi` 未解包 `body.data` |
| I2 无测试 | important | ✅ 已修复 | 4 测试文件：`AlgoControllerTest`(9 测试) + `HashServiceTest` + `BubbleSortServiceTest` + `ExportServiceTest`(含 CSV 注入验证) |
| I3 BASE_URL 硬编码 | important | ✅ 已修复 | `algo-demo.js` L13-15 环境自适应 `location.hostname` 判断 |
| I4 导出 iframe | important | ✅ 已修复 | `exportResult()` 改为 fetch+Blob L174-229，含错误状态码捕获 + 按钮禁用态 |
| I5 CORS allowCredentials | important | ✅ 已修复 | `CorsConfig` 改为 `allowedOrigins("*")` + `allowCredentials(false)` + `allowedMethods("GET","OPTIONS")` |
| I6 HashService 伪哈希 | important | ✅ 已修复 | 极端兜底改为 `throw new IllegalStateException` L35 |
| N1 GET vs POST | suggestion | ✅ 有意偏离，文档已标注 | 实现优于设计，不影响功能 |
| N2 无日志 | suggestion | ✅ 已修复 | `GlobalExceptionHandler` L49 `log.error("未捕获异常", e)` |
| N3 CSV 防注入 | suggestion | ✅ 已修复 | `ExportService.sanitize()` L88-97 + `ExportServiceTest` 验证 `'=cmd` 前缀 |
| N4 yml 简陋 | nit | ✅ 已修复 | `application.yml` 补充 `spring.application.name: algo-api` |

### 本轮新增发现

| 严重度 | 数量 | 说明 |
|--------|------|------|
| **blocking** | 1 | I1 修复引入的前后端数据层错位回归 |
| **nit / suggestion** | 1 | 导出接口 export 的 `type` 参数无白名单校验 |
| **praise** | 1 | 测试覆盖质量高，含 XSS/CSV 注入/兜底边界 |

**blocker_count = 1**

**评审结论**：⚠️ **不可直接合并**（I1 后端统一响应体修复后，前端 `callApi` 未同步解包 `body.data`，导致三个 Tab 页面全部显示 `undefined`），需修复后重新评审。

---

## 1. 评审范围

### 后端（leecode / algo-api 模块）
| 文件 | 行数 | 职责 |
|------|------|------|
| `pom.xml` | 44 | Maven 构建配置（Spring Boot 2.7.18 / Java 8） |
| `AlgoApiApplication.java` | 15 | Spring Boot 启动类 |
| `model/ApiResult.java` | 44 | ★ 新增统一响应体 {code,message,data} |
| `config/CorsConfig.java` | 21 | 全局 CORS 配置（已关闭 credentials） |
| `controller/AlgoController.java` | 143 | 四接口控制器（hello/hash/bubble/export）+ escapeHtml |
| `exception/GlobalExceptionHandler.java` | 53 | 全局异常兜底（含 slf4j 日志） |
| `service/HashService.java` | 50 | SHA-256 哈希服务（极端兜底抛异常） |
| `service/BubbleSortService.java` | 32 | 冒泡排序服务 |
| `service/ExportService.java` | 98 | CSV 导出服务（含 CSV 防注入） |
| `application.yml` | 6 | 服务端口 + application.name |
| `src/test/.../AlgoControllerTest.java` | 130 | ★ 新增 MockMvc 切片测试（9 用例） |
| `src/test/.../HashServiceTest.java` | 46 | ★ 新增哈希服务单测 |
| `src/test/.../BubbleSortServiceTest.java` | 51 | ★ 新增冒泡排序单测 |
| `src/test/.../ExportServiceTest.java` | 57 | ★ 新增导出服务单测（含 CSV 注入） |

### 前端（haikulou1.github.io）
| 文件 | 行数 | 职责 |
|------|------|------|
| `algo-demo/index.html` | 77 | 三 Tab 页面 + 导出按钮 UI |
| `js/algo-demo.js` | 232 | fetch 调用 + Tab 切换 + 导出 + 错误兜底 |

---

## 2. Blocking 级发现（1 项）

### B2 — 前端 `callApi` 未解包 `body.data`，导致三 Tab 全部显示 `undefined`（I1 修复回归）

- **严重度**：`blocking`
- **位置**：`js/algo-demo.js` L57-63（`callApi` 函数）+ L123/140-142/165-166（各 call 函数消费处）
- **现象**：

  I1 修复后，后端四个接口全部返回 `ApiResult` 统一响应体：
  ```json
  {"code":0,"message":"ok","data":{"result":"HelloWorld"}}
  ```

  但前端 `callApi()` 在 `code === 0` 时**原样返回整个 `body`**，未提取 `body.data`：
  ```javascript
  // algo-demo.js L57-63
  const body = await res.json();
  if (body && typeof body.code === 'number' && body.code !== 0) {
      return { _error: body.message || ('错误码 ' + body.code) };
  }
  // 正常业务 JSON（裸 Map 或 code=0 的统一体），原样返回
  return body;  // ← 返回 {code:0, message:"ok", data:{...}}，未解包
  ```

  随后 `callHello()` 直接读 `data.result`：
  ```javascript
  // algo-demo.js L123
  container.appendChild(createResultRow('结果：', data.result));
  // data = {code:0, message:"ok", data:{result:"HelloWorld"}}
  // data.result = undefined（实际值在 data.data.result）
  ```

  同理 `callHash()` 的 `data.input`/`data.algorithm`/`data.hash`（L140-142）、`callBubble()` 的 `data.input`/`data.sorted`（L165-166）均为 `undefined`。

- **影响**：**三个 Tab 页面全部功能失效**——HelloWorld 显示"结果：undefined"，哈希算法三个字段均显示 undefined，冒泡排序 `data.input.join()` 直接抛 `TypeError: Cannot read properties of undefined (reading 'join')`。
- **根因**：I1 后端统一响应体修复与前端解包逻辑不同步。文件头部注释声称"callApi 解包 {code,message,data} 的 data 层"（L8），但实际代码 `return body` 未做解包——**注释与实现不一致**。
- **证据**：
  - `AlgoControllerTest` L48 断言 `$.data.result`（JSON 路径含 `.data` 层），证明后端响应确实嵌套在 `data` 下。
  - `algo-demo.js` L63 `return body` 返回完整包装体。
  - `callHello` L123 读 `data.result`（非 `data.data.result`），取值为 `undefined`。
- **修复建议**：在 `callApi` 的 `code === 0` 成功路径中，返回 `body.data` 而非 `body`：
  ```javascript
  // 成功：解包 data 层
  if (body && typeof body.code === 'number') {
      return body.code === 0 ? body.data : { _error: body.message || ('错误码 ' + body.code) };
  }
  // 兼容裸 Map（无 code 字段的遗留响应）
  return body;
  ```

---

## 3. Nit / Suggestion 级发现（1 项）

### N5 — 导出接口 `type` 参数无白名单校验，依赖运行时异常兜底

- **严重度**：`suggestion`
- **位置**：`AlgoController.java` L100 `@RequestParam String type`
- **现象**：`export` 接口的 `type` 参数为必填 String，但无 `@Pattern` 或枚举校验。非法 `type` 依赖 `ExportService.exportCsv` 的 `default` 分支抛 `IllegalArgumentException`，再由全局异常处理器兜底 400。
- **当前影响**：功能正确（`ExportServiceTest` 验证了非法 type 返回 400），但错误消息 `"不支持的导出类型: xxx，可选: hello, hash, bubble"` 在异常路径中构造，不如参数级校验清晰。
- **修复建议**：可引入 `@Pattern(regexp = "hello|hash|bubble")` 注解，或在 Controller 层先校验。非阻断，防御性改进。

---

## 4. Praise（肯定项，1 项）

### P4 — 测试覆盖质量高，含安全与边界场景

- **位置**：`src/test/` 全部 4 个测试类
- **评价**：
  - `AlgoControllerTest`：9 个 MockMvc 用例，覆盖正常路径 + XSS 转义验证（`hash_withXssInput_inputIsEscaped`）+ 非法入参兜底（`bubble_withInvalidNums_fallbackAndWarning`）+ 非法 type 返回 400。
  - `ExportServiceTest`：验证 3 种 type 的 CSV 输出 + **CSV 公式注入防御测试**（`=cmd|'/c calc'!A1` 输入被加 `'` 前缀）。
  - `HashServiceTest`：验证固定值哈希确定性 + 不同输入产生不同哈希。
  - `BubbleSortServiceTest`：验证乱序/已序/单元素/逆序场景。
  - 测试设计体现了"安全用例 + 边界用例 + 正常用例"三层覆盖意识，质量值得肯定。

---

## 5. 跨仓对齐点检查

| 对齐点 | 后端（leecode） | 前端（haikulou1.github.io） | 一致性结论 |
|--------|----------------|---------------------------|-----------|
| 接口路径 | `/api/hello` `/api/hash` `/api/bubble` `/api/export` | `BASE_URL + '/hello'` `'/hash?input='` `'/bubble?nums='` `'/export?type='` | ✅ 一致 |
| HTTP 方法 | 全部 GET | 全部 fetch GET | ✅ 一致 |
| 响应结构 | `{code:0, message:"ok", data:{...}}` 统一体 | `callApi` 返回完整 body，**未解包 data 层** | ❌ **不一致（B2 回归）** |
| type 枚举 | `hello` / `hash` / `bubble` | `currentTab` 取值一致 + 导出传参一致 | ✅ 一致 |
| 导出参数透传 | `export` 接受可选 `input`/`nums` | `exportResult` 拼接 `&input=`/`&nums=` | ✅ 一致 |
| 导出文件名 | `attachment; filename=<type>.csv` | 从 `Content-Disposition` 解析 filename | ✅ 一致 |
| 端口 | `application.yml: server.port: 8080` | `BASE_URL` localhost→8080 / 生产→域名 | ✅ 一致 |
| CORS | `allowedOrigins("*")` + `allowCredentials(false)` + GET,OPTIONS | 前端无特殊处理 | ✅ 一致（安全改进已落地） |
| 出参字段 | hello→data:{result}; hash→data:{input,algorithm,hash}; bubble→data:{input,sorted,warning?} | 读 `data.result`/`data.input`/... （未加 `.data` 前缀） | ❌ **字段路径错位（B2）** |
| 输入参数名 | hash: `input`; bubble: `nums` | hash: `?input=`; bubble: `?nums=` | ✅ 一致 |

---

## 6. 修复优先级建议

| 优先级 | 编号 | 问题 | 修复成本 |
|--------|------|------|----------|
| **P0（必须）** | B2 | `callApi` 成功路径返回 `body.data` 而非 `body` | 极低（改 1 行 return） |
| **P3** | N5 | export type 参数加 `@Pattern` 白名单 | 低 |

---

## 7. 评审结论

首轮 CR 的 10 项发现（1 blocking + 6 important + 3 nit/suggestion）中，**9 项已正确修复**（B1 XSS、I2 测试、I3 BASE_URL、I4 导出、I5 CORS、I6 HashService、N2 日志、N3 CSV 防注入、N4 yml），修复质量整体良好——尤其测试覆盖含安全与边界场景值得肯定。

但 **I1（统一响应体）的修复在后端落地后，前端 `callApi` 未同步解包 `body.data` 层**，导致三个 Tab 页面全部功能失效（显示 `undefined` 或抛 `TypeError`）。这是一个由修复引入的阻断级回归（B2），必须在合并前修复——改动量极小（`callApi` 成功路径 `return body.data`），但影响面是全前端功能。

**综合判定**：⚠️ **Request Changes（需修改后重新评审）**

> 修复 B2 后预期可通过：仅需将 `algo-demo.js` `callApi` 函数 L63 处 `return body` 改为在 `code === 0` 时 `return body.data`，其余代码无需改动。

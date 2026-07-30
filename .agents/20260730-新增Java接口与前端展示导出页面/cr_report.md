# 代码评审报告：新增 Java 接口与前端展示导出页面

> **评审日期**：2026-07-30  
> **评审阶段**：review（代码评审，只读）  
> **评审技能**：code-review-skill  
> **涉及仓库**：leecode（后端 Spring Boot）/ haikulou1.github.io（前端静态页）  
> **契约基线**：`docs/design/2026-07-30-helloworld-hash-bubble-design.md` + `docs/superpowers/specs/2026-07-30-algo-api-frontend-export-clarify.md` + `.agents/plans/algo-api-frontend-plan.md`

---

## 0. 评审摘要

| 严重度 | 数量 | 说明 |
|--------|------|------|
| **blocking** | 1 | 安全漏洞，必须修复才能合并 |
| **important** | 6 | 契约偏离 / 可靠性 / 可测试性 / 可部署性问题 |
| **nit / suggestion** | 4 | 防御性编程与规范性建议 |
| **praise** | 3 | 值得肯定的设计决策 |

**blocker_count = 1**

**评审结论**：⚠️ **不可直接合并**（存在 1 个 blocking 级 XSS 安全漏洞），需修复后重新评审。

---

## 1. 评审范围

### 后端（leecode / algo-api 模块）
| 文件 | 行数 | 职责 |
|------|------|------|
| `pom.xml` | 39 | Maven 构建配置（Spring Boot 2.7.18 / Java 8） |
| `AlgoApiApplication.java` | 15 | Spring Boot 启动类 |
| `config/CorsConfig.java` | 21 | 全局 CORS 配置 |
| `controller/AlgoController.java` | 103 | 四接口控制器（hello/hash/bubble/export） |
| `exception/GlobalExceptionHandler.java` | 44 | 全局异常兜底 |
| `service/HashService.java` | 47 | SHA-256 哈希服务 |
| `service/BubbleSortService.java` | 32 | 冒泡排序服务 |
| `service/ExportService.java` | 56 | CSV 导出服务 |
| `application.yml` | 2 | 服务端口配置 |

### 前端（haikulou1.github.io）
| 文件 | 行数 | 职责 |
|------|------|------|
| `algo-demo/index.html` | 77 | 三 Tab 页面 + 导出按钮 UI |
| `js/algo-demo.js` | 114 | fetch 调用 + Tab 切换 + 导出 + 错误兜底 |

---

## 2. Blocking 级发现（1 项）

### B1 — XSS 漏洞：hash 接口 input 原样回显 + 前端 innerHTML 未转义

- **严重度**：`blocking`
- **后端位置**：`AlgoController.java` L53-61
- **前端位置**：`js/algo-demo.js` L69-72
- **现象**：
  - hash 接口将用户传入的 `input` 参数**原样**放入响应 JSON（`map.put("input", input)`）。
  - 前端 `callHash()` 直接将 `data.input` 拼入 `innerHTML`：
    ```javascript
    '<span class="value">' + data.input + '</span>'
    ```
  - 用户在哈希输入框输入 `<img src=x onerror=alert(document.cookie)>`，后端原样返回，前端 `innerHTML` 渲染后触发脚本执行。
- **影响**：反射型 XSS。攻击者可构造恶意链接诱导用户点击，在用户浏览器上下文执行任意脚本。
- **根因**：后端未对用户输入做输出编码；前端用 `innerHTML` 而非 `textContent` 渲染不可信数据。
- **修复建议**：
  1. **前端优先**：将所有 `innerHTML` 拼接改为 `textContent` 或 `createTextNode`，杜绝 HTML 注入面。
  2. **后端纵深防御**：对 `input` 做基本 HTML 字符转义（`<` → `&lt;` 等）后再回显。
  3. 同理检查 `callHello`（L56 `data.result`）与 `callBubble`（L89-90 `data.input.join`）的渲染路径——hello 返回固定串安全；bubble 经 `Integer::parseInt` 过滤为数字安全；**hash 的 input 回显是唯一 XSS 入口**。

---

## 3. Important 级发现（6 项）

### I1 — 响应体偏离统一契约 `{code, message, data}`

- **严重度**：`important`
- **位置**：`AlgoController.java` L43-47 / L54-61 / L68-89
- **契约要求**：clarify.md §4.3 明确规定统一响应体：
  ```json
  { "code": 0, "message": "ok", "data": { ... } }
  ```
  design.md §4.2 亦规定 `{ "code": 0, "msg": "ok", "data": <object> }`。
- **实际实现**：四个接口直接返回裸 `Map<String, Object>`，**未包裹** `code/message/data` 结构。例如 hello 接口返回 `{"result":"HelloWorld"}` 而非 `{"code":0,"message":"ok","data":{"result":"HelloWorld"}}`。
- **当前影响**：前端 `algo-demo.js` 直接读 `data.result` / `data.input` / `data.hash`，与后端裸 Map 自洽，**功能可用**。
- **潜在风险**：
  - 契约文档与实现不一致，后续消费者（移动端/第三方）按文档调用会失败。
  - 无法区分业务错误码与成功响应（所有接口 HTTP 200 + 无 code 字段）。
- **修复建议**：引入 `ApiResult<T>` 统一响应模型（design.md §4.1 已规划 `model/ApiResult.java` 但未实现），或至少在控制器层包裹。同步更新前端解包逻辑。

### I2 — 无单元测试，不满足契约测试要求

- **严重度**：`important`
- **位置**：整个 `algo-api` 模块无 `src/test/` 目录
- **契约要求**：clarify.md §4.6 明确要求"Spring Boot Test 对 4 接口单元+切片测试（MockMvc）"；plan 验收清单含"后端四接口均返回正确 JSON / CSV"。
- **实际**：零测试覆盖。
- **修复建议**：至少补充：
  - `AlgoControllerTest`（MockMvc 切片测试，验证 4 接口路径/入参/出参/HTTP 状态码）
  - `HashServiceTest` / `BubbleSortServiceTest`（纯单元测试，验证算法正确性 + 边界：空串/null/单元素数组）
  - `ExportServiceTest`（验证 3 种 type 的 CSV 输出 + 非法 type 抛 IllegalArgumentException）

### I3 — 前端 BASE_URL 硬编码 localhost，部署到 GitHub Pages 后不可达

- **严重度**：`important`
- **位置**：`js/algo-demo.js` L5
  ```javascript
  const BASE_URL = 'http://localhost:8080/api';
  ```
- **影响**：页面部署到 GitHub Pages（`haikulou1.github.io`）后，浏览器从远程加载页面，`fetch('http://localhost:8080/...')` 指向访问者本地——**必然失败**。
- **契约要求**：design.md §5.2 与 clarify 均提到"部署时改"，但未提供配置化机制。
- **修复建议**：
  - 开发期用 `localhost`，生产期改为实际后端域名。
  - 可通过 `const API_BASE = location.hostname === 'localhost' ? 'http://localhost:8080/api' : 'https://<prod-domain>/api'` 自动切换，或从页面 `data-*` 属性注入。

### I4 — 导出 iframe.onerror 跨域不可靠，导出失败用户无反馈

- **严重度**：`important`
- **位置**：`js/algo-demo.js` L97-111
- **现象**：导出用隐藏 `iframe.src = url` 触发下载，并用 `iframe.onerror` 兜底提示。但跨域 iframe 受同源策略限制，**onerror 几乎不会触发**——即使后端返回 400（type 非法），iframe 仍会加载错误响应体，浏览器不触发 onerror。
- **影响**：当 `type` 非法或后端宕机时，用户点击导出按钮**无任何反馈**，体验差且难以排查。
- **修复建议**：改用 `fetch` + `Blob` 方式导出，可捕获 HTTP 错误状态码并提示用户：
  ```javascript
  async function exportResult() {
      try {
          const res = await fetch(BASE_URL + '/export?type=' + currentTab);
          if (!res.ok) throw new Error('HTTP ' + res.status);
          const blob = await res.blob();
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url; a.download = currentTab + '.csv';
          a.click(); URL.revokeObjectURL(url);
      } catch (e) { alert('导出失败：' + e.message); }
  }
  ```

### I5 — CORS allowCredentials(true) 与无认证场景矛盾，增加 CSRF 攻击面

- **严重度**：`important`
- **位置**：`config/CorsConfig.java` L16-19
  ```java
  registry.addMapping("/api/**")
          .allowedOriginPatterns("*")
          .allowedMethods("GET")
          .allowCredentials(true);
  ```
- **问题分析**：
  1. `allowCredentials(true)` 在此 demo **无 cookie/session/认证**的场景下毫无意义，反而要求浏览器在跨域请求中携带凭证，扩大 CSRF 攻击面。
  2. Spring 5.3+ 的 `allowedOriginPatterns("*")` + `allowCredentials(true)` 技术上合法（Spring 回显 Origin 而非发 `*`），功能不会报错，但从安全最佳实践角度不妥。
  3. `allowedMethods("GET")` 未显式包含 `OPTIONS`——虽然 Spring MVC 的 CORS 处理器会自动响应预检请求，但显式声明更清晰。
- **修复建议**：无认证场景应简化为：
  ```java
  registry.addMapping("/api/**")
          .allowedOrigins("*")        // 或生产环境指定具体 Origin
          .allowedMethods("GET", "OPTIONS")
          .allowedHeaders("*")
          .allowCredentials(false);   // 无认证场景关闭
  ```

### I6 — HashService 极端兜底返回 input.length() 作"伪哈希"，语义错误

- **严重度**：`important`
- **位置**：`service/HashService.java` L30-33
  ```java
  } catch (NoSuchAlgorithmException ex) {
      // 极端兜底：返回原始字符串长度作为伪哈希
      return new String[]{"fallback", String.valueOf(input.length())};
  }
  ```
- **问题**：SHA-256 和 MD5 均为 JDK 内置算法，`NoSuchAlgorithmException` 理论不可达。但极端兜底路径返回 `input.length()` 作为"哈希值"——这**不是哈希**，而是长度数字，会误导调用方以为得到了哈希摘要。
- **影响**：若该路径被触发（理论不可达但防御性编程应正确），调用方无法区分真哈希与伪值，可能导致数据完整性校验失效。
- **修复建议**：极端兜底应 `throw new IllegalStateException("哈希算法不可用", ex)` 而非返回伪值——让异常向上传播由全局处理器兜底 500，比返回错误数据更安全。

---

## 4. Nit / Suggestion 级发现（4 项）

### N1 — hash/bubble 接口用 GET+query 而非设计的 POST+body

- **严重度**：`suggestion`
- **位置**：`AlgoController.java` L53 `@GetMapping("/hash")` / L67 `@GetMapping("/bubble")`
- **契约**：clarify.md §4.1/§4.3 设计为 `POST /api/hash`（body `{"text":"abc"}`）与 `POST /api/bubble`（body `{"array":[3,1,2]}`）。
- **实际**：实现为 GET + `@RequestParam`（query string `?input=...` / `?nums=...`）。
- **评价**：从 RESTful 角度，只读幂等的查询用 GET + query **比 POST body 更合理**（可缓存、可书签、语义正确）。实现优于设计，但**偏离了文档契约**。
- **建议**：更新 clarify.md 契约以匹配实现（将 POST 改为 GET），或在评审记录中标注此为有意偏离。保持文档与代码一致。

### N2 — GlobalExceptionHandler 兜底 Exception 不记日志，排查困难

- **严重度**：`suggestion`
- **位置**：`exception/GlobalExceptionHandler.java` L39-43
- **现象**：`@ExceptionHandler(Exception.class)` 返回固定文案"服务异常，请稍后重试"，**未记录任何日志**（无 `log.error`）。
- **影响**：生产环境出现未知异常时，后端无任何痕迹，排查困难。
- **修复建议**：注入 `slf4j Logger`，在兜底前 `log.error("未捕获异常", e)` 记录完整堆栈。

### N3 — CSV 导出无防注入处理

- **严重度**：`suggestion`
- **位置**：`service/ExportService.java` L31-54
- **现象**：CSV 字段直接拼接，未对以 `=`、`+`、`-`、`@` 开头的字段值做前缀转义（CSV injection / formula injection）。
- **当前风险**：低——所有字段当前为硬编码值（`hello` / `HelloWorld` / 数字），无注入面。
- **未来风险**：若 hash 的 `input` 改为来自用户输入并导出，攻击者输入 `=cmd|'/c calc'!A1` 可在 Excel 中执行公式。
- **修复建议**：防御性处理——字段值以 `=,+,-,@` 开头时在前面加 `'` 前缀或用双引号包裹并转义内部双引号。

### N4 — application.yml 过于简陋

- **严重度**：`nit`
- **位置**：`application.yml` L1-2（仅 `server.port: 8080`）
- **建议**：补充 `spring.application.name: algo-api`（便于日志/链路追踪识别）、超时配置等基础项。非阻断，规范性改进。

---

## 5. Praise（肯定项，3 项）

### P1 — BubbleSortService 使用 Arrays.copyOf 不修改原数组

- **位置**：`service/BubbleSortService.java` L19
- **评价**：`int[] a = Arrays.copyOf(arr, arr.length)` 确保传入的原始数组不被修改，符合**防御性编程**原则。Controller 层 `map.put("input", input)` 放的是原数组、`map.put("sorted", sorted)` 放的是排序副本，两者独立，结果正确。

### P2 — bubble 接口入参非法时优雅兜底

- **位置**：`AlgoController.java` L71-84
- **评价**：输入解析失败时回退默认数组 `[5,3,8,1,9,2]` 并在响应中标记 `warning` 字段提示用户"输入非法，已使用默认数组"。容错策略优雅，既不阻断服务又透明告知用户，前端 L86-88 也正确渲染了 warning。

### P3 — 前端 AbortController 5s 超时 + 错误兜底

- **位置**：`js/algo-demo.js` L24-36
- **评价**：`callApi` 封装了 5 秒超时（`AbortController` + `setTimeout`）和统一的 `_error` 兜底，网络失败时显示红色提示而非静默失败。用户体验良好，是静态页前端调后端的合理实践。

---

## 6. 跨仓对齐点检查

| 对齐点 | 后端（leecode） | 前端（haikulou1.github.io） | 一致性结论 |
|--------|----------------|---------------------------|-----------|
| 接口路径 | `/api/hello` `/api/hash` `/api/bubble` `/api/export` | `BASE_URL + '/hello'` `'/hash?input='` `'/bubble?nums='` `'/export?type='` | ✅ 一致（路径与 query 参数名匹配） |
| HTTP 方法 | 全部 GET | 全部 fetch GET | ✅ 一致（但偏离 clarify.md POST 设计，见 N1） |
| 响应结构 | 裸 Map，无 `{code,message,data}` 包裹 | 直接读 `data.result` 等，未解包 data 层 | ⚠️ 前后端自洽但偏离契约，见 I1 |
| type 枚举 | `hello` / `hash` / `bubble`（ExportService switch） | `currentTab` 取值 `hello` / `hash` / `bubble`（switchTab） | ✅ 一致 |
| 导出文件名 | `attachment; filename=<type>.csv` | iframe 自动下载 | ✅ 一致 |
| 端口 | `application.yml: server.port: 8080` | `BASE_URL = 'http://localhost:8080/api'` | ✅ 一致（开发期） |
| CORS | `allowedOriginPatterns("*")` + `allowCredentials(true)` | 前端无特殊处理 | ⚠️ 配置可工作但安全不当，见 I5 |
| 出参字段 | hello→`{result}`; hash→`{input,algorithm,hash}`; bubble→`{input,sorted,warning?}` | 读 `data.result` / `data.input` / `data.algorithm` / `data.hash` / `data.sorted` / `data.warning` | ✅ 字段名一致 |
| 算法输入参数名 | hash: `input`; bubble: `nums` | hash: `?input=`; bubble: `?nums=` | ✅ 一致 |

---

## 7. 修复优先级建议

| 优先级 | 编号 | 问题 | 修复成本 |
|--------|------|------|----------|
| **P0（必须）** | B1 | XSS 漏洞：前端 innerHTML → textContent + 后端输入转义 | 低（前端改 3 处渲染） |
| **P1（强烈建议）** | I1 | 统一响应体 ApiResult 包裹 | 中（新增模型 + 改控制器 + 改前端解包） |
| **P1** | I2 | 补充单元测试 | 中（4 接口 MockMvc + 3 Service 单测） |
| **P1** | I3 | BASE_URL 环境自适应 | 低（1 行配置） |
| **P2** | I4 | 导出改 fetch+Blob | 低（重写 exportResult 函数） |
| **P2** | I5 | CORS 关闭 allowCredentials | 低（改 1 行） |
| **P2** | I6 | HashService 兜底改抛异常 | 低（改 1 行） |
| **P3** | N1-N4 | 契约同步 / 日志 / CSV 防注入 / yml 补充 | 低 |

---

## 8. 评审结论

本次交付**功能基本完整**——三接口 + 三 Tab + 导出按钮均已实现，前后端字段对齐，开发期可联调通过。但存在 **1 个 blocking 级 XSS 安全漏洞**（B1）必须修复后方可合并。另有 6 个 important 级问题涉及契约偏离、测试缺失、部署阻塞，建议在合并前或紧随合并后修复。

**综合判定**：⚠️ **Request Changes（需修改后重新评审）**

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
| **blocking** | 1 | 后端删除 HTML 转义（H1 改原样回显）但测试仍断言转义结果 → 测试必然失败，前后端 XSS 防御契约断裂 |
| **important** | 0 | R2 的 B2（前端未解包 data 层）已修复 |
| **nit / suggestion** | 2 | 契约文档未同步 GET + 后端无空 input 校验（依赖 defaultValue 兜底） |
| **praise** | 5 | B2 修复正确 + XSS 前端 textContent 全覆盖 + 统一响应体设计完整 |

**blocker_count = 1**

**评审结论**：⚠️ **不可直接合并**。R2 的 B2（前端 callApi 未解包 data 层）已被正确修复——`callApi` L72 现为 `return body.data !== undefined ? body.data : body`，三 Tab 可正常读取业务字段。但本次发现新的阻断问题 B3：后端在 H1 修复中删除了 `escapeHtml`，`AlgoController.hash` L59 改为原样回显 input（注释"H1: 回显原始 input，不做 HTML 转义"），而 `AlgoControllerTest.hash_withXssInput_inputIsEscaped` L57-63 仍断言 `$.data.input` 等于 `"&lt;img src=x onerror=alert(1)&gt;"`。**该测试用例必然失败**（实际返回原始 `<img...>`，断言期望转义后的 `&lt;img...&gt;`），导致 `mvn test` 红灯，CI 阻断合并。

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
| `test/.../AlgoControllerTest.java` | 118 | MockMvc 切片测试 |
| `test/.../HashServiceTest.java` | 46 | 哈希服务单测 |
| `test/.../BubbleSortServiceTest.java` | 51 | 冒泡排序单测 |
| `test/.../ExportServiceTest.java` | 57 | 导出服务单测 |

### 前端（haikulou1.github.io）
| 文件 | 行数 | 职责 |
|------|------|------|
| `algo-demo/index.html` | 77 | 三 Tab 页面 + 导出按钮 UI |
| `js/algo-demo.js` | 241 | fetch 调用 + Tab 切换 + 导出 + 错误兜底 |

---

## 2. 前次 CR（R2）问题修复核对

| 编号 | R2 严重度 | 问题 | 修复状态 | 证据 |
|------|-----------|------|----------|------|
| B2 | blocking | 前端 callApi 未解包统一响应体 data 层 | ✅ 已修复 | `algo-demo.js` L72 `return body.data !== undefined ? body.data : body`；注释 L11-13 标注 "CR round2 修复：B1 Blocker: callApi 正确解包 ApiResult.data 层"。各 call* 函数读 `data.result`/`data.input`/`data.sorted` 现可正确命中 |
| N1 | suggestion | GET vs POST 契约偏离 | ⏳ 未处理 | 仍为 GET+query，契约文档未同步（实现合理，低优先级） |
| N5 | nit | AlgoControllerTest 冗余 @Autowired | ✅ 已修复 | AlgoControllerTest L22-25 仅保留 `@Autowired MockMvc mockMvc`，已移除 hashService/bubbleSortService/exportService 三个未使用字段 |

---

## 3. 新发现 Blocking（1 项）

### B3 — 后端删除 HTML 转义但测试仍断言转义结果，测试必然失败 + XSS 防御契约断裂

- **严重度**：`blocking`
- **类型**：CR 修复回归（H1 XSS 防御策略变更未同步测试 + 契约不一致）
- **后端位置**：`AlgoController.java` L52-62（`hash` 方法，L59 `data.put("input", input)`）
- **测试位置**：`AlgoControllerTest.java` L54-63（`hash_withXssInput_inputIsEscaped`）

#### 现象

后端 `AlgoController.hash`（L52-62）当前实现：

```java
/**
 * 哈希算法接口（SHA-256）
 * input 为空时回退默认值 "hello"
 * H1: 回显原始 input（不做 HTML 转义），与导出接口的 input 处理保持一致
 */
@GetMapping("/hash")
public ApiResult hash(@RequestParam(defaultValue = "hello") String input) {
    String[] result = hashService.hash(input);
    Map<String, Object> data = new HashMap<>();
    data.put("input", input);   // ← L59: 原样回显，未做任何转义
    data.put("algorithm", result[0]);
    data.put("hash", result[1]);
    return ApiResult.ok(data);
}
```

测试 `AlgoControllerTest.hash_withXssInput_inputIsEscaped`（L54-63）断言：

```java
/**
 * B1 验证：XSS 输入被 HTML 转义
 */
@Test
void hash_withXssInput_inputIsEscaped() throws Exception {
    String xssPayload = "<img src=x onerror=alert(1)>";
    mockMvc.perform(get("/api/hash").param("input", xssPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.input").value("&lt;img src=x onerror=alert(1)&gt;"));  // ← L62: 断言已转义
}
```

#### 矛盾矩阵

| 维度 | 测试断言（L62） | 后端实际（L59） | 一致性 |
|------|------|------|------|
| XSS input 回显值 | `$.data.input` = `"&lt;img src=x onerror=alert(1)&gt;"`（HTML 转义后） | `data.put("input", input)` 原样回显 = `"<img src=x onerror=alert(1)>"` | ❌ **必然失败** |

MockMvc 断言 `jsonPath("$.data.input").value("&lt;img...&gt;")` 期望转义后的字符串，但后端返回的是原始 payload `<img...>`。**该测试用例运行必失败**，`mvn test` 红灯。

#### 根因分析

R1 报告 B1（XSS）的原修复方案是"后端 escapeHtml + 前端 textContent"双重防护。R2 报告核查时确认后端有 `escapeHtml(input)`（R2 L59 证据："后端 `escapeHtml(input)` L58/L114-142"）。

本次复审发现，后端在 R2 之后的修复（标注为 "H1"）改变了策略：**删除了后端 escapeHtml，改为原样回显 input，XSS 防御完全依赖前端 textContent**。这一策略变更本身是合理的（前端 textContent 已彻底消除 innerHTML 注入面，后端无需重复转义；且原样回显对 API 消费方更友好），但**未同步更新测试断言**——`hash_withXssInput_inputIsEscaped` 仍断言后端返回转义结果。

这是典型的"防御策略迁移未同步测试"回归。策略从"后端转义"迁移到"前端转义"，但测试还停留在旧策略的预期上。

#### 影响

1. **测试红灯**：`hash_withXssInput_inputIsEscaped` 必然失败，`mvn test` 返回非零退出码，CI 阻断合并。
2. **XSS 防御契约断裂**：R1 B1 的修复契约是"后端 escapeHtml 兜底 + 前端 textContent"双重防护。H1 删除后端转义后，XSS 防御完全依赖前端单点。当前前端 `callHash`（L149）和 `createResultRow`（L90）确实用 `textContent` 渲染 `data.input`，**前端防护有效**。但防御从双层降为单层，且：
   - 若未来有人改前端用 innerHTML，XSS 立即复活，无后端兜底。
   - `ExportService.exportCsv`（L46）的 `sanitize(actualInput)` 仅防 CSV 公式注入（`= + - @` 前缀），不防 HTML 注入；导出的 CSV 用 Excel 打开无 XSS 风险，但若 CSV 内容被回显到 Web 页面则有风险（当前无此路径，低风险）。
3. **测试与实现矛盾**：测试名 `hash_withXssInput_inputIsEscaped` 暗示"后端转义"，与实现注释"H1: 不做 HTML 转义"直接冲突，后续维护者会困惑。

#### 修复建议

二选一，推荐方案 A（成本最低，符合 H1 策略）：

**方案 A（推荐）：更新测试断言，对齐 H1"原样回显"策略**

将 `hash_withXssInput_inputIsEscaped` 改为验证原样回显，并补充前端 textContent 防护的说明（前端无单测，此处后端测试仅验证回显契约）：

```java
/**
 * H1 验证：XSS 输入原样回显（后端不转义，前端 textContent 负责渲染安全）
 */
@Test
void hash_withXssInput_inputIsRawEchoed() throws Exception {
    String xssPayload = "<img src=x onerror=alert(1)>";
    mockMvc.perform(get("/api/hash").param("input", xssPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.input").value(xssPayload));  // 原样回显
}
```

**方案 B：恢复后端 escapeHtml，与测试断言一致**

在 `AlgoController.hash` 中恢复转义（但需同步修改 ExportService 保持一致，且与 H1 注释矛盾，不推荐）。

> **验证方法**：执行 `mvn test -pl algo-api`，确认 `hash_withXssInput_*` 用例通过。由于本次为静态审查（未触发编译/测试），建议修复后本地跑一次 `mvn test` 确认全绿。

---

## 4. 遗留 Nit / Suggestion（2 项）

### N1 — hash/bubble 接口用 GET+query 而非设计的 POST+body（R2 遗留）

- **严重度**：`suggestion`（未升级，未降级）
- **状态**：⏳ 未处理（实现合理，低优先级）
- **说明**：GET+query 比 POST+body 更符合 RESTful 语义。契约文档 clarify.md 仍写 POST，建议同步文档或标注有意偏离。

### N6 — 后端 hash 接口无显式空 input 校验，依赖 defaultValue 兜底（新增）

- **严重度**：`nit`
- **位置**：`AlgoController.java` L56
- **现象**：`@RequestParam(defaultValue = "hello") String input` 依赖 Spring 的 defaultValue 机制处理空参。若调用方传 `?input=`（显式空串），defaultValue 不触发，input="" 会进入 HashService 计算空串哈希。
- **影响**：无安全风险（空串哈希合法），但与"input 为空时回退默认值"的注释承诺不完全一致。ExportService L44 有显式 `(input == null || input.isEmpty()) ? "hello" : input` 兜底，Controller 层缺同等校验。
- **建议**：低优先级，可在 Controller 补 `if (input == null || input.isEmpty()) input = "hello";` 与 ExportService 对齐。

---

## 5. Praise（肯定项，5 项）

### P1 — B2 修复正确，前端 callApi 正确解包 data 层（新增）

- **位置**：`algo-demo.js` L65-72
- **评价**：R2 的 B2（`return body` 未解包）已修复为 `return body.data !== undefined ? body.data : body`。逻辑严谨：先判断 `body.code` 非 0 走错误路径，再判断 `body.data` 存在则解包，否则兜底返回 body。修复后各 call* 函数无需改动即可正确读取 `data.result`/`data.input`/`data.sorted`。

### P2 — XSS 前端 textContent 全覆盖（保留）

- **位置**：`algo-demo.js` L83-94（`createResultRow`）/ L100-109（`renderError`）/ L168-172（warning）
- **评价**：所有用户可见数据渲染均用 `textContent`/DOM API，无任何 `innerHTML` 调用。`createResultRow` 创建 labelSpan + valueSpan 双 span 结构，`valueSpan.textContent = value`，彻底消除 HTML 注入面。这是 H1 策略能安全删除后端转义的前提。

### P3 — 后端统一响应体 + 全局异常处理器设计完整（保留）

- **位置**：`ApiResult.java` L7-44 / `GlobalExceptionHandler.java` L20-52
- **评价**：`ApiResult` 不可变对象（`final` 字段 + 私有构造 + 静态工厂 `ok`/`fail`），三种异常处理器（`IllegalArgumentException`→400 / `MissingServletRequestParameterException`→400 / `Exception`→500+slf4j 日志）覆盖完整，错误体统一包裹 `ApiResult.fail`，不暴露堆栈。设计规范。

### P4 — BubbleSortService 使用 Arrays.copyOf 不修改原数组（保留）

- **位置**：`BubbleSortService.java` L19
- **评价**：`int[] a = Arrays.copyOf(arr, arr.length)` 确保传入的原始数组不被修改，符合防御性编程原则。

### P5 — CSV 防注入 + 测试覆盖（保留）

- **位置**：`ExportService.java` L88-97 `sanitize()` / `ExportServiceTest.java` L52-56
- **评价**：`sanitize()` 对 `= + - @` 开头字段加单引号前缀防 Excel 公式注入，并有对应测试验证。导出 content-type 设为 `text/csv` + `attachment; filename=` 下载头，规范。

---

## 6. 跨仓对齐点检查

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
| 出参字段 | hello→`{result}`; hash→`{input,algorithm,hash}`; bubble→`{input,sorted,warning?}` | 读 `data.result`/`data.input`/`data.algorithm`/`data.hash`/`data.sorted`/`data.warning` | ✅ 字段名与层级均一致 |
| **XSS input 处理** | **L59 原样回显（H1: 不转义）** | **L149 `createResultRow` 用 `textContent` 渲染** | ✅ 前端防护有效，但**后端测试 L62 断言转义结果与实现矛盾**（见 B3） |
| 算法输入参数名 | hash: `input`; bubble: `nums` | hash: `?input=`; bubble: `?nums=` | ✅ 一致 |
| BASE_URL 生产域名 | 后端部署域名待定 | `https://algo-api.example.com/api`（占位符，支持 `window.ALGO_API_BASE_URL` 覆盖） | ✅ 可接受（部署时注入） |

---

## 7. 修复优先级建议

| 优先级 | 编号 | 问题 | 修复成本 |
|--------|------|------|----------|
| **P0（必须）** | B3 | 更新 `hash_withXssInput_inputIsEscaped` 测试断言对齐 H1 原样回显策略 | 低（改 1 处断言，~3 行） |
| P3 | N1 | 契约文档同步 GET 或标注有意偏离 | 低 |
| P3 | N6 | Controller 补空 input 校验与 ExportService 对齐 | 低 |

---

## 8. 评审结论

本次复审确认 R2 的 B2（前端 callApi 未解包 data 层）已被正确修复——`callApi` L72 现解包 `body.data`，三 Tab 可正常展示业务数据，回归已消除。N5（冗余 @Autowired）也已修复，AlgoControllerTest 仅保留必要的 MockMvc 注入。

但本次发现新的阻断问题 B3：后端在 H1 修复中删除了 `escapeHtml`，`hash` 接口改为原样回显 input（L59），而测试 `hash_withXssInput_inputIsEscaped`（L57-63）仍断言 `$.data.input` 返回 HTML 转义后的 `&lt;img...&gt;`。**测试断言与实现直接矛盾，该用例必然失败**，`mvn test` 红灯，CI 阻断合并。

根因是 XSS 防御策略从"后端转义 + 前端 textContent"双层迁移为"前端 textContent 单层"时，未同步更新测试断言。策略迁移本身合理（前端已全覆盖 textContent，无 innerHTML 调用，防护有效），但测试遗留导致 CI 红灯。

修复成本极低（方案 A：将测试断言从 `&lt;img...&gt;` 改为原样 `<img...>`，~3 行），修复后无需改动生产代码。

**综合判定**：⚠️ **Request Changes（需修改后重新评审）**

blocker_count = 1

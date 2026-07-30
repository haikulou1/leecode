# 代码评审报告 (CR Report)

| 项目 | 内容 |
|------|------|
| 任务 | 新增 Java 接口（HelloWorld/哈希/冒泡排序）+ 前端三 Tab 展示 + 导出功能 |
| 评审日期 | 2026-07-30 |
| 评审阶段 | review (round 2) |
| 涉及仓库 | leecode (后端 Java)、haikulou1.github.io (前端静态站) |
| 使用技能 | /code-review-skill |
| Blocker 数 | **1** |
| High 数 | 1 |
| Medium 数 | 5 |
| Low 数 | 4 |

---

## 一、审查范围

### leecode 仓库（后端 Java）
| # | 文件 | 行数 |
|---|------|------|
| 1 | `algo-api/pom.xml` | 44 |
| 2 | `algo-api/src/main/java/algoapi/AlgoApiApplication.java` | 15 |
| 3 | `algo-api/src/main/java/algoapi/model/ApiResult.java` | 46 |
| 4 | `algo-api/src/main/java/algoapi/config/CorsConfig.java` | 21 |
| 5 | `algo-api/src/main/java/algoapi/controller/AlgoController.java` | 143 |
| 6 | `algo-api/src/main/java/algoapi/service/HashService.java` | 50 |
| 7 | `algo-api/src/main/java/algoapi/service/BubbleSortService.java` | 32 |
| 8 | `algo-api/src/main/java/algoapi/service/ExportService.java` | 98 |
| 9 | `algo-api/src/main/java/algoapi/exception/GlobalExceptionHandler.java` | 53 |
| 10 | `algo-api/src/main/resources/application.yml` | 6 |

### haikulou1.github.io 仓库（前端）
| # | 文件 | 行数 |
|---|------|------|
| 11 | `algo-demo/index.html` | 77 |
| 12 | `js/algo-demo.js` | 232 |

### 设计文档
- `docs/design/2026-07-30-helloworld-hash-bubble-design.md`
- `docs/superpowers/specs/2026-07-30-algo-api-frontend-export-clarify.md`
- `.agents/plans/algo-api-frontend-plan.md`

---

## 二、问题清单

### 🔴 B1 [Blocker] 前端 callApi 未解包 ApiResult.data 层，三 Tab 展示功能全部失效

**仓库**: haikulou1.github.io
**文件**: `js/algo-demo.js`
**位置**: `callApi()` 函数返回值 + `callHello()/callHash()/callBubble()` 取值逻辑

**问题描述**:

后端所有接口（`/api/hello`、`/api/hash`、`/api/bubble`）均返回统一响应体 `ApiResult`：

```json
{
  "code": 0,
  "message": "ok",
  "data": { "result": "HelloWorld" }
}
```

`ApiResult.java`（第 40-43 行）：
```java
public static ApiResult ok(Object data) {
    return new ApiResult(0, "ok", data);
}
```

前端 `callApi()` 的实现：

```javascript
const body = await res.json();
if (body && typeof body.code === 'number' && body.code !== 0) {
    return { _error: body.message };
}
// 正常业务 JSON（裸 Map 或 code=0 的统一体），原样返回
return body;
```

`callApi` 返回的是整个 `body` 对象（`{code, message, data}`），而非 `body.data`。

随后 `callHello()` 使用 `data.result`：
```javascript
const data = await callApi(BASE_URL + '/hello');
container.appendChild(createResultRow('结果：', data.result));
```

`data` 是 `{code:0, message:"ok", data:{result:"HelloWorld"}}`，`data.result` 为 `undefined`。

同理：
- `callHash()` 中 `data.input`、`data.algorithm`、`data.hash` 均为 `undefined`（实际数据在 `data.data.input` 等处）
- `callBubble()` 中 `data.input`、`data.sorted`、`data.warning` 均为 `undefined`，且 `data.input.join(', ')` 会因 `undefined.join` 抛 TypeError

**根因**:

`callApi` 的注释声称"I1 统一响应体: callApi 解包 {code,message,data} 的 data 层"，但代码实际只返回了整个 `body`，未做 `return body.data` 解包。开发者误以为后端可能返回"裸 Map"（如 `{"result":"HelloWorld"}`），但后端 `AlgoController` 所有接口均通过 `ApiResult.ok(data)` 包裹返回，不存在裸 Map 情况。

**修复建议**:

在 `callApi()` 中，当 `body.code === 0` 时返回 `body.data`：

```javascript
if (body && typeof body.code === 'number' && body.code !== 0) {
    return { _error: body.message || ('错误码 ' + body.code) };
}
// 统一响应体：解包 data 层
return body.data !== undefined ? body.data : body;
```

**影响**: 核心功能完全不可用，三 Tab 展示均无法显示后端返回数据。

---

### 🟠 H1 [High] hash 接口与导出接口对 input 处理不一致，展示与导出内容不匹配

**仓库**: leecode
**文件**: `AlgoController.java` (第 60 行) vs `ExportService.java` (第 52 行)

**问题描述**:

- `/api/hash` 接口对回显的 `input` 做了 HTML 转义：`data.put("input", escapeHtml(input))`
- `/api/export?type=hash` 对 `input` 只做了 CSV 防注入（sanitize），未做 HTML 转义：`sanitize(actualInput)`

用户在前端输入 `<script>alert(1)</script>`：
- 页面展示的 input 值为 `&lt;script&gt;alert(1)&lt;/script&gt;`（HTML 转义后）
- 导出 CSV 中 input 值为 `<script>alert(1)</script>`（原始值）

两者内容不一致，违反"导出内容与页面展示一致"的需求。

**修复建议**:

统一策略。由于后端返回 JSON（非 HTML 上下文），且前端已用 `textContent` 渲染（B1 修复已杜绝 XSS），后端 `escapeHtml` 实际是多余的。建议：
- 移除 `AlgoController.hash()` 中的 `escapeHtml(input)`，直接 `data.put("input", input)`
- 或在 `ExportService` 中同步做 `escapeHtml`（不推荐，CSV 场景不需要 HTML 转义）

---

### 🟡 M1 [Medium] CORS 配置 allowedOrigins("*") 生产环境安全风险

**仓库**: leecode
**文件**: `CorsConfig.java` (第 16 行)

```java
.allowedOrigins("*")
```

生产环境允许任意源跨域调用，虽然 `allowCredentials(false)` 不发送凭证，但仍存在被恶意站点利用的风险（如 CSRF 类攻击面、接口滥用）。

**修复建议**: 生产环境配置具体白名单 Origin：
```java
.allowedOrigins("https://haikulou1.github.io", "http://localhost:xxxx")
```

---

### 🟡 M2 [Medium] 前端 BASE_URL 生产域名是占位符，部署时未替换将导致全站不可用

**仓库**: haikulou1.github.io
**文件**: `js/algo-demo.js` (第 16 行)

```javascript
: 'https://algo-api.example.com/api'; // 生产环境部署时替换为实际域名
```

`algo-api.example.com` 是占位符。若未替换直接部署，所有非 localhost 访问的 API 请求将指向不存在域名，前端功能完全不可用。

**修复建议**: 提供环境变量或构建时注入机制，避免硬编码占位符。

---

### 🟡 M3 [Medium] ExportService 无数组长度上限，恶意超长输入可引发性能问题

**仓库**: leecode
**文件**: `ExportService.java` `parseBubbleNums()` (第 62-73 行) 及 `AlgoController.bubble()` (第 72 行)

用户传入超长数字序列字符串（如百万级逗号分隔数字），`Arrays.stream(...).mapToInt(...).toArray()` 会构建超大数组，可能引发 OOM 或高延迟。

**修复建议**: 增加解析后数组长度上限校验（如 1000）：
```java
if (arr.length > 1000) {
    throw new IllegalArgumentException("数组长度超过上限 1000");
}
```

---

### 🟡 M4 [Medium] 无单元测试覆盖

**仓库**: leecode
**文件**: `algo-api/pom.xml` 引入了 `spring-boot-starter-test`（scope=test），但 `src/test/` 下无任何测试类。

关键逻辑（冒泡排序正确性、哈希算法结果、导出 CSV 格式、参数兜底逻辑）缺少自动化测试验证。

**修复建议**: 至少补充以下测试：
- `BubbleSortServiceTest`：空数组、单元素、已排序、逆序、重复元素
- `HashServiceTest`：已知输入的 SHA-256 结果比对
- `ExportServiceTest`：三类型导出 CSV 格式校验、非法 type 异常
- `AlgoControllerTest`（MockMvc）：参数缺失兜底、非法输入回退

---

### 🟡 M5 [Medium] Spring Boot 2.7.18 OSS 支持已结束

**仓库**: leecode
**文件**: `algo-api/pom.xml` (第 11 行)

Spring Boot 2.7.x 的开源社区支持已于 2023 年 11 月结束。对于新项目，建议使用 Spring Boot 3.x（需 Java 17+）。当前 Java 8 配置不兼容 Spring Boot 3.x。

**修复建议**: 若项目需长期维护，评估升级到 Spring Boot 3.2+ / Java 17。若仅为演示，可接受现状但应记录技术债。

---

### 🔵 L1 [Low] Content-Disposition filename 未加引号包裹

**仓库**: leecode
**文件**: `AlgoController.java` (第 106 行)

```java
.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".csv")
```

标准做法应加引号：`filename="hello.csv"`。当前 type 仅允许 `hello/hash/bubble`（纯字母），不含空格，实际无影响，但不规范。

---

### 🔵 L2 [Low] HashService.toHex 使用 String.format 性能低

**仓库**: leecode
**文件**: `HashService.java` (第 47 行)

```java
sb.append(String.format("%02x", b));
```

逐字节调用 `String.format` 存在格式化开销。哈希结果长度固定（32-64 字节），影响可忽略，但可用查表法优化：

```java
private static final char[] HEX = "0123456789abcdef".toCharArray();
sb.append(HEX[(b >> 4) & 0xF]).append(HEX[b & 0xF]);
```

---

### 🔵 L3 [Low] application.yml 缺少日志级别等生产配置

**仓库**: leecode
**文件**: `application.yml`

仅配置 port 和 application name。缺少日志级别控制、`server.error.include-stacktrace=never`（防止堆栈泄露）等生产安全配置。

---

### 🔵 L4 [Low] Controller 中 escapeHtml 在 JSON 上下文做 HTML 转义

**仓库**: leecode
**文件**: `AlgoController.java` (第 60 行, 第 112-135 行)

`AlgoController` 返回 `ApiResult`（JSON 响应），非 HTML 上下文。在 JSON 中做 HTML 转义是多余的纵深防御，且导致数据失真（用户看到的 input 值带 `&lt;` 等转义字符）。前端已用 `textContent` 渲染，XSS 防护已足够。

---

## 三、跨库接口契约对齐检查

| 检查点 | 结论 |
|--------|------|
| 后端 `/api/hello` 响应结构 vs 前端取值 | ❌ 前端未解包 `data` 层，`data.result` 取值为 undefined（B1） |
| 后端 `/api/hash` 响应结构 vs 前端取值 | ❌ 同上，`data.input/algorithm/hash` 均为 undefined（B1） |
| 后端 `/api/bubble` 响应结构 vs 前端取值 | ❌ 同上，且 `undefined.join()` 会抛 TypeError（B1） |
| 后端 `/api/export` Content-Disposition vs 前端文件名解析 | ✅ 前端正则可正确解析无引号 filename |
| 后端错误响应 `{code,message}` vs 前端错误解析 | ✅ 前端 `errBody.message` 取值正确 |
| hash 接口 input 处理 vs 导出 input 处理 | ❌ escapeHtml vs sanitize，内容不一致（H1） |
| bubble 接口兜底逻辑 vs 导出兜底逻辑 | ✅ 两者 `parseBubbleNums` 逻辑一致 |
| CORS 允许方法 vs 前端请求方法 | ✅ 前端仅用 GET，CORS 允许 GET/OPTIONS |
| 前端导出参数透传 vs 后端导出参数接收 | ✅ input/nums 参数名一致，透传正确 |

---

## 四、已修复确认（上一轮 CR 修复点验证）

| 修复点 | 描述 | 验证结论 |
|--------|------|----------|
| B1 (XSS) | 前端 innerHTML 改为 textContent/DOM API | ✅ 已修复，全部用 textContent |
| I1 (统一响应体) | 后端返回包裹 ApiResult | ✅ 后端已包裹；❌ 前端未解包（B1） |
| I3 (BASE_URL) | 环境自适应 localhost/生产 | ✅ 已实现（生产域名需替换，M2） |
| I4 (导出改 fetch) | 可捕获 HTTP 错误状态码 | ✅ 已实现 |
| I6 (哈希兜底) | 不返回伪哈希，抛异常 | ✅ 已修复，抛 IllegalStateException |
| N2 (兜底日志) | Exception 加 slf4j 日志 | ✅ 已实现 |
| N3 (CSV 防注入) | sanitize 加单引号前缀 | ✅ 已实现 |

---

## 五、审查结论

| 指标 | 数值 |
|------|------|
| Blocker | 1 |
| High | 1 |
| Medium | 5 |
| Low | 4 |
| **blocker_count** | **1** |

**审查结论**: ❌ **不通过**

存在 1 个 Blocker（B1：前端 callApi 未解包 ApiResult.data 层），导致三 Tab 展示功能完全失效。必须修复后方可通过。

**必须修复项（阻塞合入）**:
1. **B1**: `callApi()` 返回 `body.data` 而非 `body`，使前端能正确获取后端数据

**建议修复项（不阻塞但推荐本轮一并处理）**:
2. **H1**: 统一 hash 接口与导出接口对 input 的处理策略
3. **M2**: 替换前端生产环境占位域名或提供注入机制

其余 Medium/Low 问题可记入技术债后续迭代处理。

---

## 六、审查涉及文件清单

### leecode（后端）
- `[leecode] algo-api/pom.xml`
- `[leecode] algo-api/src/main/java/algoapi/AlgoApiApplication.java`
- `[leecode] algo-api/src/main/java/algoapi/model/ApiResult.java`（审查补充发现，不在输入清单）
- `[leecode] algo-api/src/main/java/algoapi/config/CorsConfig.java`
- `[leecode] algo-api/src/main/java/algoapi/controller/AlgoController.java`
- `[leecode] algo-api/src/main/java/algoapi/service/HashService.java`
- `[leecode] algo-api/src/main/java/algoapi/service/BubbleSortService.java`
- `[leecode] algo-api/src/main/java/algoapi/service/ExportService.java`
- `[leecode] algo-api/src/main/java/algoapi/exception/GlobalExceptionHandler.java`
- `[leecode] algo-api/src/main/resources/application.yml`

### haikulou1.github.io（前端）
- `[haikulou1.github.io] algo-demo/index.html`
- `[haikulou1.github.io] js/algo-demo.js`

### 设计文档（审查参考）
- `[leecode] docs/design/2026-07-30-helloworld-hash-bubble-design.md`
- `[leecode] docs/superpowers/specs/2026-07-30-algo-api-frontend-export-clarify.md`
- `[leecode] .agents/plans/algo-api-frontend-plan.md`

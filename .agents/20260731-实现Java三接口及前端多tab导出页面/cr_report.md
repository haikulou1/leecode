# 代码评审报告：Java 三接口及前端多 Tab 导出页面

| 项目 | 内容 |
|------|------|
| 评审日期 | 2026-07-31 |
| 评审阶段 | review（代码评审，只读）— 第三轮（问题修复后终审） |
| 评审技能 | code-review-skill |
| 评审范围 | leecode（后端 Java Spring Boot）+ haikulou1.github.io（前端 JS/HTML） |
| 需求摘要 | 用 java 分别写三个接口 helloworld、哈希算法、冒泡排序；前端新增一个页面，有三个 tab 分别展示不同的执行结果；新增导出按钮，后台提供导出接口，支持导出各个页面的展示结果 |
| Blocker 数 | 0 |
| Major 数 | 0 |
| Minor 数 | 1 |
| 评审结论 | **通过** — 第二轮 5 个 Minor 中 4 个已修复（异常脱敏、冗余复制合并、toHex 简化、MD5 UI 标注），跨仓契约保持完整对齐；残留 1 个 Minor（大整数精度）为演示场景可接受项，不阻塞合并 |

---

## 一、通览

本次评审为**第三轮终审**，基于第二轮报告提出的 5 个 Minor 经「问题修复」阶段处理后的最新代码。

- **后端（leecode / algo-demo）**：Spring Boot 2.7.18 + Java 8，4 个 REST 接口（`/api/helloworld`、`/api/hash`、`/api/bubble-sort`、`GET/POST /api/export`）。修复阶段处理了 3 个文件：`GlobalExceptionHandler`（异常脱敏）、`BubbleSortService`（冗余复制合并）、`HashService`（toHex 简化）。
- **前端（haikulou1.github.io）**：原生 JS（IIFE + strict）+ 单页 HTML，三 Tab 切换。修复阶段处理了 `page/algo-demo/index.html`：MD5 选项标注「非安全用途」并新增安全提示 hint。

评审聚焦：第二轮 5 个 Minor 的修复核查、修复未引入新缺陷回归、跨仓契约保持完整对齐。

---

## 二、第二轮 Minor 修复核查

| # | 第二轮问题 | 严重度 | 修复后现状 | 核查结论 |
|---|-----------|--------|-----------|---------|
| 1 | `GlobalExceptionHandler.handleGeneral(Exception)` 直接返回 `e.getMessage()`，可能泄露内部异常信息 | Minor | `handleGeneral` 改为 `log.error("Unhandled exception in algo-demo", e)` 记录原始异常到服务端日志，对客户端返回通用消息 `"internal error"`（`buildError(HttpStatus.INTERNAL_SERVER_ERROR, "internal error")`） | ✅ 已修复 |
| 2 | `BubbleSortService` 三次 `ArrayList` 复制冗余 | Minor | `response.setInput(input)` 直接引用已构造的独立快照 `input`（`List<Integer> input = new ArrayList<>(working)`），不再 `new ArrayList<>(input)` 第三次复制；注释更新为「input 已是入参的独立快照（排序前），sorted 为排序后 working，直接引用即可」 | ✅ 已修复（三次降为两次） |
| 3 | `HashService.toHex()` 手写实现（`v<0x10` 补零） | Minor | `toHex` 改为 `sb.append(String.format("%02x", b & 0xFF))`，简洁且正确 | ✅ 已修复 |
| 4 | `HashService` MD5 选项（已不抗碰撞） | Minor | `index.html` 第182行 `<option value="MD5">MD5（非安全用途）</option>` 标注非安全用途；第187行新增 `<div class="hint">POST /api/hash — MD5 仅供演示，不可用于安全场景</div>` 提示 | ✅ 已按建议处理（保留选项 + UI 标注） |
| 5 | `js/algo-demo.js` 大整数精度丢失 | Minor | `parseBubbleInput` 仍用 `Number(parts[i])`，超过 `Number.MAX_SAFE_INTEGER` 精度丢失但 `isInteger` 仍 true；问题修复阶段未涉及 js 文件，且第二轮已判定 demo 可接受 | ⚠️ 残留（可接受） |

> 第二轮 5 个 Minor 中 4 个已修复/按建议处理；剩余 #5（大整数精度）为演示场景可接受项，详见第四节。

---

## 三、修复回归检查（未引入新缺陷）

对修复涉及的 4 个文件做回归核查，确认修复未破坏既有功能与契约：

### 3.1 `GlobalExceptionHandler.java`（43 行）

- `handleIllegalArgument` 仍返回 `e.getMessage()`（业务参数错误信息，可透传给客户端用于参数修正提示），映射 400 ✅
- `handleGeneral` 改为返回 `"internal error"`，原始异常经 `log.error(..., e)` 落服务端日志 ✅
- `buildError` 结构不变：`{error, status, message, timestamp}`，前端 `fetchJson`/`exportAll` 的 `res.ok` 校验与错误展示链路不受影响 ✅
- **回归结论**：无新缺陷，异常处理契约对前端透明。

### 3.2 `BubbleSortService.java`（63 行）

- `working = new ArrayList<>(request.getInput() == null ? emptyList() : request.getInput())`：复制入参，隔离排序 ✅
- `input = new ArrayList<>(working)`：排序前快照，作为响应 `input` 字段 ✅
- 排序循环 `for (round = 1; round < n; round++)`，每轮 `step.setArray(new ArrayList<>(working))` 快照 ✅
- `response.setInput(input)`（直接引用快照）、`response.setSorted(working)`（排序后 working）、`response.setSteps(steps)`、`response.setSwapCount(totalSwaps)` ✅
- 算法正确性：升序冒泡，`working.get(j) > working.get(j+1)` 交换，`totalSwaps` 累加，`steps` 每轮记录 ✅
- **回归结论**：从三次复制降为两次，`input` 与 `sorted` 仍为独立对象（不可变性保持），无逻辑回归。

### 3.3 `HashService.java`（83 行）

- 白名单 `ALLOWED = {MD5, SHA-256, SHA-512}` 不变，`algorithm.toUpperCase()` 大小写不敏感匹配 ✅
- `digest` 使用 `MessageDigest.getInstance(algorithm)`（白名单归一化后的值），`NoSuchAlgorithmException` 兜底抛 `IllegalArgumentException` ✅
- `toHex` 改为 `String.format("%02x", b & 0xFF)`：`%02x` 自动补零，`b & 0xFF` 处理符号位，输出十六进制小写 ✅
- 响应字段 `input/algorithm/hash/length` 不变 ✅
- **回归结论**：toHex 简化后输出与原实现等价（均为两位十六进制小写），无功能回归。

### 3.4 `page/algo-demo/index.html`（209 行）

- MD5 `<option>` 文案改为 `MD5（非安全用途）`，新增 hash tab hint 提示 ✅
- 其余结构（三 Tab、双导出按钮 `btn-export`/`btn-export-2`、深色主题、ARIA 语义）不变 ✅
- 脚本引用 `/js/algo-demo.js` 不变 ✅
- **回归结论**：仅文案与提示新增，无 DOM 结构/ID 变更，前端 JS 绑定不受影响。

---

## 四、残留项详查

### 4.1 大整数精度（Minor #5）— `js/algo-demo.js` `parseBubbleInput`

**现状**（第148-161行）：
```javascript
function parseBubbleInput(raw) {
  if (!raw) throw new Error('请输入逗号分隔的整数，如 5,3,8,1,9,2');
  var parts = raw.split(/[,，\s]+/).filter(function (s) { return s.length > 0; });
  if (parts.length === 0) throw new Error('未解析到任何数字');
  var arr = [];
  for (var i = 0; i < parts.length; i++) {
    var n = Number(parts[i]);
    if (!Number.isFinite(n) || !Number.isInteger(n)) {
      throw new Error('存在非整数值：' + parts[i]);
    }
    arr.push(n);
  }
  return arr;
}
```

**问题**：`Number(parts[i])` 对超过 `Number.MAX_SAFE_INTEGER`（2^53-1）的整数值会发生精度丢失，但 `Number.isInteger` 仍返回 `true`，导致超大整数被静默截断后传入后端。

**影响评估**：
- 后端 `BubbleSortRequest.input` 为 `List<Integer>`，Jackson 反序列化超大数值时会抛 `JsonMappingException`（溢出 Integer 范围）或截断，经 `GlobalExceptionHandler` 兜底返回 500 `"internal error"`。
- 演示场景输入为小数组（如 `5,3,8,1,9,2`），不触发此路径。
- 第二轮已判定「demo 可接受」，问题修复阶段未涉及 js 文件（修复范围限 index.html + 3 个 Java 文件）。

**严重度**：Minor（演示场景不触发，生产化前可选加固）。

**建议**（生产化前可选）：在 `parseBubbleInput` 增加范围校验，如 `if (n > Number.MAX_SAFE_INTEGER || n < Number.MIN_SAFE_INTEGER) throw new Error('数值超出安全整数范围：' + parts[i]);`，或将 `<input>` 改为 `type="number"` 限制。

---

## 五、跨仓接口契约对齐复查（修复后保持）

| 对齐点 | 结论 |
|--------|------|
| 路由路径 | ✅ 前端 `/api/*` 与后端 `@RequestMapping("/api")` 完全匹配；POST `/api/export` 双端对齐 |
| 请求字段 | ✅ hash `{input,algorithm}`、bubble `{input:[...]}`、export(POST) `{tab,hello,hash,bubble}` 与后端 DTO 一致 |
| 响应字段 | ✅ 前端读取的所有字段后端均提供（helloworld `result/timestamp`、hash `input/algorithm/hash/length`、bubble `input/sorted/steps/round/swaps/array/swapCount`） |
| 导出契约 | ✅ POST 实时结果导出 + GET 向后兼容示例导出，双端一致；`ExportRequest.HelloResult{result,timestamp}` 与前端 `lastResults.hello` 对齐 |
| CORS | ✅ 后端白名单 `https://haikulou1.github.io` + `http://localhost:*`/`http://127.0.0.1:*`，前端 fetch 无 credentials，跨域可通 |
| 端口 | ✅ 前端 `API_BASE:8080` 与后端 `server.port:8080` 一致 |
| 异常处理 | ✅ `GlobalExceptionHandler` 统一结构化错误体（`handleGeneral` 脱敏后仍返回 `{error,status,message:"internal error",timestamp}`），前端 `fetchJson`/`exportAll` 校验 `res.ok` 并展示错误 |

**结论**：修复未破坏任何跨仓契约，接口对齐完整，无断裂点。

---

## 六、逐文件审查结论（修复后）

### 后端 leecode / algo-demo

#### 6.1 `pom.xml` — ✅ 无问题
- Spring Boot 2.7.18（Java 8），仅 `spring-boot-starter-web`，依赖最小化。

#### 6.2 `AlgoDemoApplication.java` — ✅ 无问题
- 标准 `@SpringBootApplication` 启动类。

#### 6.3 `config/CorsConfig.java` — ✅ 无问题
- 显式白名单 + 关闭凭证 + 方法限定 `GET/POST/OPTIONS`，安全最小化。

#### 6.4 `controller/AlgoController.java` — ✅ 良好
- 显式构造器注入四个 Service；GET/POST 双导出入口，try-with-resources 包裹流；Javadoc 完整。

#### 6.5 `controller/GlobalExceptionHandler.java` — ✅ 已修复（第二轮 #1）
- `handleGeneral` 异常脱敏：服务端 `log.error` 记录原始异常，客户端返回 `"internal error"`。

#### 6.6 `service/HashService.java` — ✅ 已修复（第二轮 #3、#4）
- `toHex` 改用 `String.format("%02x", b & 0xFF)`，简洁正确；MD5 选项保留（演示可接受，UI 已标注非安全用途）。

#### 6.7 `service/BubbleSortService.java` — ✅ 已修复（第二轮 #2）
- 冗余复制合并：从三次降为两次，`input` 快照直接引用，`sorted` 引用排序后 `working`，不可变性保持。

#### 6.8 `service/ExportService.java` — ✅ 无问题
- 双入口（GET 示例 / POST 实时），优先前端结果缺省兜底；`generateFileName` UTC 时区；`normalizeTab` 健壮。

#### 6.9 `service/HelloWorldService.java` — ✅ 无问题
- 返回 `{result:"Hello, World!", timestamp: Instant.now().toString()}`，ISO-8601 UTC。

#### 6.10 Model 类 — ✅ 无问题
- `ExportRequest`（含 `HelloResult` 内部静态类）、`HashRequest/Response`、`BubbleSortRequest/Response`（含 `Step`）结构清晰，Jackson 映射正确。

#### 6.11 `application.yml` — ✅ 无问题
- `server.port: 8080`。

### 前端 haikulou1.github.io

#### 6.12 `js/algo-demo.js` — ✅ 良好 + ⚠️ Minor（残留 #5）
- `API_BASE` 可配置化；导出 POST 携带 `lastResults`；`parseFileName` RFC 5987 兼容 + try/catch 兜底；XSS 防护（全程 `textContent`/`createElement`）；`fetchJson` 校验 `res.ok` 与 `content-type`；Tab 键盘可达性。
- 大整数精度（Minor #5）：`Number(parts[i])` 超过 `MAX_SAFE_INTEGER` 精度丢失但 `isInteger` 仍 true；demo 小数组可接受。

#### 6.13 `page/algo-demo/index.html` — ✅ 已修复（第二轮 #4）
- MD5 选项标注「非安全用途」+ 新增 hash tab 安全提示 hint；其余结构不变。

---

## 七、问题汇总表（修复后残留）

| # | 严重度 | 仓库 | 文件 | 问题 | 建议 |
|---|--------|------|------|------|------|
| 1 | Minor | haikulou1.github.io | js/algo-demo.js | `parseBubbleInput` 用 `Number()` 解析，超大整数精度丢失但 `isInteger` 仍 true | 生产化前可选：增加 `MAX_SAFE_INTEGER` 范围校验，或将 input 改为 `type="number"` |

---

## 八、评审结论

- **Blocker：0**（无安全漏洞导致 RCE/数据泄露、无运行时崩溃、无契约断裂）
- **Major：0**（第二轮 4 个 Major 在前一轮已全部修复；本轮无新增 Major）
- **Minor：1**（大整数精度丢失；演示场景不触发，生产化前可选加固）
- **修复核查**：第二轮 5 个 Minor 中 4 个已修复/按建议处理（#1 异常脱敏、#2 冗余复制合并、#3 toHex 简化、#4 MD5 UI 标注），残留 #5 为可接受项。
- **回归检查**：修复未引入新缺陷，4 个修改文件逻辑与契约保持不变。
- **跨仓契约**：修复未破坏任何接口对齐，路由/字段/导出/CORS/端口/异常处理全部完整对齐。
- **建议**：演示场景可直接合并；生产化前可选处理残留 #5（大整数精度校验）。

评审通过。

# Code Review Report

> **Change** `算法演示与导出平台` · **分支/Commit** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-419f6010-3917-49c9-` / `-` · **日期** `2026-07-30` · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。
>
> **扫描说明**：本次为 review 阶段静态审查（LLM 逐文件审查），未运行 `scan-all-rules.sh`（review 阶段 Git 只读约束 + 自动化脚本需 Git diff 上下文）。以下结论基于全量源码人工审查 + PRD/计划契约核对。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | `7` |
| 前端文件数 | `3`（html/js/css） |
| 变更行数 | `+620 / -0`（新增文件） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `Main` | `[leecode] datastruct/src/server/Main.java` | HTTP 服务入口 |
| `RouterHandler` | `[leecode] datastruct/src/server/RouterHandler.java` | 路由分发 + CORS + 错误处理 |
| `HelloWorldService` | `[leecode] datastruct/src/service/HelloWorldService.java` | F1.1 HelloWorld 接口 |
| `HashService` | `[leecode] datastruct/src/service/HashService.java` | F1.2 SHA-256 哈希接口 |
| `BubbleSortService` | `[leecode] datastruct/src/service/BubbleSortService.java` | F1.3 冒泡排序接口 |
| `ExportService` | `[leecode] datastruct/src/service/ExportService.java` | F2 CSV 导出接口 |
| `JsonUtil` | `[leecode] datastruct/src/util/JsonUtil.java` | 零依赖 JSON 工具 |
| `demo.html` | `[haikulou1] demo.html` | 前端三 Tab 页面 |
| `demo.js` | `[haikulou1] js/demo.js` | 前端交互逻辑 |
| `demo.css` | `[haikulou1] css/demo.css` | 前端样式 |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 1 | 2 | 4 |

---

## 3. Step 2 — 功能（REQ）

### REQ-1: HelloWorld 接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /api/helloworld 返回 {result,timestamp} | ✅ | PRD F1.1 | `HelloWorldService.java:14-23` | 返回固定 "Hello World" + 毫秒时间戳，契约匹配 |
| 前端 Tab1 自动加载展示结果 | ✅ | PRD F1.1 | `demo.js:107-129` | 进入 HelloWorld tab 自动 fetch，渲染结果+时间戳 |

### REQ-2: 哈希算法接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /api/hash?input=xxx 返回 SHA-256 | ✅ | PRD F1.2 | `HashService.java:19-32` | 返回 input/algorithm/hash/length 四字段 |
| input 为空返回 400 | ✅ | PRD F1.2 | `RouterHandler.java:66-68` | 空值校验，返回 400 |
| input 超 1024 返回 400 | ⚠️ | PRD F1.2 | `RouterHandler.java:70-73` | 校验逻辑正确，但错误消息 "input is required" 与场景不符，见 P1-1 |
| input 含非 ASCII 字符 | ❌ | PRD F1.2 | `RouterHandler.java:148-174` | 自定义 urlDecode 不支持多字节 UTF-8，见 P1-2 |
| 前端 Tab2 输入+执行展示哈希 | ✅ | PRD F1.2 | `demo.js:133-163` | 输入框+执行按钮，encodeURIComponent 编码，结果渲染四字段 |

### REQ-3: 冒泡排序接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /api/bubble?data=5,3,8,1 返回排序结果 | ✅ | PRD F1.3 | `BubbleSortService.java:15-30` | 返回 input/sorted/steps/asc 四字段 |
| data 非法返回 400 | ✅ | PRD F1.3 | `BubbleSortService.java:37-63` + `RouterHandler.java:84-88` | 1~50 整数，[-1000,1000]，非法抛 IAE 转 400 |
| 纯函数不修改原数组 | ✅ | 计划 Task4 | `BubbleSortService.java:17` | `values.clone()` 后排序 |
| 前端 Tab3 输入+执行展示排序 | ✅ | PRD F1.3 | `demo.js:167-197` | 输入框+执行按钮，结果渲染原数组/排序后/交换次数/方向 |

### REQ-4: CSV 导出接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /api/export?tab=helloworld 返回 CSV | ✅ | PRD F2 | `ExportService.java:38-45` | field,value 两列，含 result+timestamp |
| GET /api/export?tab=hash 返回 CSV | ✅ | PRD F2 | `ExportService.java:55-66` | field,value 两列，含 input/algorithm/hash/length |
| GET /api/export?tab=bubble 返回 CSV | ❌ | PRD F2 | `ExportService.java:76-90` | extractField 无法解析含逗号的 JSON 数组值，bubble.csv 数据截断，见 P0-1 |
| 非法 tab 返回 400 | ✅ | PRD F2 | `ExportService.java:26-28` + `RouterHandler.java:108-110` | 抛 IAE 转 400 |
| 前端导出按钮触发下载 | ✅ | PRD F2 | `demo.js:201-214` | 隐藏 `<a download>` 触发浏览器下载，Content-Disposition 由后端设置 |

---

## 4. Step 3 — 可读性检查

| 结果 | 说明 |
|------|------|
| ✅ | 整体可读性良好，符合阿里 Java 编码风格 |

**亮点**：
- 包结构清晰：`server/`（入口+路由）、`service/`（业务）、`util/`（工具），职责单一
- 每个类/方法均有 Javadoc 注释，说明功能与契约
- 常量提取为 `static final`（`CORS_ORIGIN`、`JSON_CONTENT_TYPE`、`ALGORITHM`、`CRLF` 等）
- 前端 IIFE 封装，无全局污染；函数命名语义清晰（`loadHelloWorld`、`execHash`、`exportCurrent`）

**P2-1 (Info)**: `JsonUtil` API 与计划偏差 — 计划定义 `toJson(LinkedHashMap<String,Object>)`，实际实现为 `escape()` / `quoteField()` / `rawField()` 三个独立方法。功能等价但 API 形态不同，不影响正确性。
- `path`: `[leecode] datastruct/src/util/JsonUtil.java:59-68`

---

## 5. Step 4 — 可靠性检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 可靠性 | G1 并发 | ✅ | — | HttpServer 默认执行器（`setExecutor(null)`），单线程串行处理，无并发问题 |
| 可靠性 | G6 异常处理 | ⚠️ | P1 | RouterHandler catch-all 500 处理在响应已部分发送后可能二次抛异常 |
| 可靠性 | G8 资源管理 | ✅ | — | OutputStream 均在写入后 close() |
| 安全 | S1 注入 | ✅ | — | 无 SQL，JSON 序列化经 escape() 转义 |
| 安全 | S10 CORS | ⚠️ | P2 | CORS `*` + GET/OPTIONS，缺少 `Access-Control-Allow-Headers` |
| Bug 模式 | B012 类 | ✅ | — | 已扫无命中 |

### 5.1 Bug 模式核对

| ID | 规则名 | 结果 | 说明 |
|----|--------|------|------|
| B002 | ArrayEquals | ✅ 已扫无命中 | 无数组 equals 比较 |
| B004 | ArrayToString | ✅ 已扫无命中 | 无数组 toString 直接输出 |
| B005 | ArraysAsListPrimitiveArray | ✅ 已扫无命中 | 无 Arrays.asList 调用 |

---

## 6. Step 5 — 自定义扩展检查

| 项 | 结果 | 说明 |
|----|------|------|
| 跨库契约对齐 | ✅ | 4 个 HTTP 端点路径/参数/响应格式前后端一致：`/api/helloworld`、`/api/hash?input=`、`/api/bubble?data=`、`/api/export?tab=` |
| 向后兼容 | ✅ | 全部为新增文件，未修改两仓库任何现有文件 |
| 编译产物入库 | ⚠️ P2 | `.class` 文件列入变更清单，编译产物不应纳入版本控制 |

---

## 7. 问题清单（含代码片段）

### P0-1: ExportService.extractField 无法解析 JSON 数组值 — bubble.csv 导出数据截断

**等级**: P0 (Blocker)  
**文件**: `[leecode] datastruct/src/service/ExportService.java:95-107`  
**Bug 模式**: 功能缺陷 — extractField 在遇到值内逗号时提前截断

**问题**：`extractField` 通过扫描 `','` 和 `'}'` 作为值结束符，但 JSON 数组值 `[5,3,8,1]` 本身含逗号，导致值在第一个逗号处被截断。

**影响**：`/api/export?tab=bubble` 返回的 CSV 中 `input` 字段值为 `[5` 而非 `[5,3,8,1]`，`sorted` 字段值为 `[1` 而非 `[1,3,5,8]`。导出结果与页面展示不一致，违反需求"支持导出各个页面的展示结果"。

**问题片段** (`ExportService.java:95-107`):
```java
private static String extractField(String json, String field) {
    String key = "\"" + field + "\":";
    int idx = json.indexOf(key);
    if (idx < 0) {
        return "";
    }
    int start = idx + key.length();
    int end = start;
    while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
        end++;  // ← 遇到 [5,3,8,1] 中的第一个逗号即停止，返回 "[5"
    }
    return json.substring(start, end);
}
```

**修复建议**：`buildBubbleCsv()` 不应通过字符串解析 JSON 提取字段。应直接复用 `BubbleSortService` 的内部方法或重构为返回结构化数据（如 `int[]`），由 ExportService 直接格式化为 CSV。或者改用括号配对扫描替代简单逗号分割。

---

### P1-1: RouterHandler input 长度超限错误消息错误

**等级**: P1 (Major)  
**文件**: `[leecode] datastruct/src/server/RouterHandler.java:70-73`

**问题**：当 `input.length() > 1024` 时，错误消息为 `"input is required"`，与"输入过长"语义不符，误导前端用户。

**问题片段** (`RouterHandler.java:70-73`):
```java
if (input.length() > 1024) {
    sendError(exchange, 400, "input is required");  // ← 应为 "input too long"
    return;
}
```

**修复建议**：改为 `sendError(exchange, 400, "input too long (max 1024)")`。

---

### P1-2: RouterHandler 自定义 urlDecode 不支持多字节 UTF-8

**等级**: P1 (Major)  
**文件**: `[leecode] datastruct/src/server/RouterHandler.java:148-174`

**问题**：自定义 `urlDecode` 将每个 `%xx` 直接转为 `char`，无法处理多字节 UTF-8 序列（如中文 `%E4%B8%AD` 应解码为 `中`，但实际产出三个独立 char）。前端使用 `encodeURIComponent()` 正确编码 UTF-8，但后端解码不匹配，导致非 ASCII 输入（如中文）被错误解码，哈希结果不正确。

**问题片段** (`RouterHandler.java:159-163`):
```java
} else if (c == '%' && i + 2 < s.length()) {
    try {
        int hex = Integer.parseInt(s.substring(i + 1, i + 3), 16);
        sb.append((char) hex);  // ← 单字节转 char，多字节 UTF-8 被拆散
        i += 3;
    } catch (NumberFormatException e) {
```

**修复建议**：改用 `java.net.URLDecoder.decode(s, "UTF-8")`，计划 Tech Stack 已列出此依赖。

---

### P2-1: JsonUtil API 与实施计划偏差

**等级**: P2 (Info)  
**文件**: `[leecode] datastruct/src/util/JsonUtil.java`

计划定义 `toJson(LinkedHashMap<String,Object>)`，实际实现为 `escape()` / `quoteField()` / `rawField()`。功能等价，不影响正确性，但 API 契约不一致。

---

### P2-2: CORS 缺少 Access-Control-Allow-Headers

**等级**: P2 (Info)  
**文件**: `[leecode] datastruct/src/server/RouterHandler.java:39-40`

当前只设置 `Allow-Origin` 和 `Allow-Methods`，未设置 `Allow-Headers`。对于当前无自定义请求头的简单 GET 请求不影响功能，但预检请求若含自定义头将失败。

---

### P2-3: ExportService 每次导出创建新 Service 实例

**等级**: P2 (Info)  
**文件**: `[leecode] datastruct/src/service/ExportService.java:56,77`

`buildHashCsv()` 和 `buildBubbleCsv()` 每次调用都 `new HashService()` / `new BubbleSortService()`。这些服务无状态，可复用单例或通过构造注入。

---

### P2-4: 编译产物 .class 文件列入变更清单

**等级**: P2 (Info)  
**文件**: `[leecode] datastruct/out/production/acm/**/*.class`

大量 `.class` 编译产物出现在变更清单中。编译产物不应纳入版本控制，建议添加 `.gitignore` 排除 `out/` 目录。

---

## 8. 评审结论

| 维度 | 结论 |
|------|------|
| 功能完整性 | ⚠️ 3/4 接口功能正常，bubble CSV 导出存在 P0 截断缺陷 |
| 跨库契约对齐 | ✅ 4 个 HTTP 端点前后端路径/参数/响应格式一致 |
| 向后兼容 | ✅ 全部新增文件，未修改现有代码 |
| 可读性 | ✅ 包结构清晰，注释完整，命名规范 |
| 可靠性 | ⚠️ 存在 1 个 P0 + 2 个 P1 需修复 |
| 安全性 | ✅ JSON 转义完整，XSS 防护到位（前端 escapeHtml） |

**总评**: **不通过（Conditional Fail）** — 存在 1 个 P0 Blocker 必须修复后方可合入。P1 问题建议同批修复。

**blocker_count = 1**

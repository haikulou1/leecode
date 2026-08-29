# 设计文档：三接口 + 前端 Tab 展示 + 导出能力

- 日期：2026-07-30
- 阶段：需求澄清（系分设计）
- 涉及仓库：[leecode]（后端 Java）、[haikulou1.github.io]（前端静态站点）
- 技能：brainstorming

---

## 1. 需求拆解

原始需求：
> 用 java 分别写三个接口 helloworld、哈希算法以及冒泡排序；前端新增一个页面，有三个 tab 分别展示不同的执行结果；新增导出按钮，后台提供导出接口，支持导出各个页面的展示结果。

拆解为三部分：

| 编号 | 部分 | 内容 |
|------|------|------|
| B1 | 后端接口 | Java 实现 3 个 HTTP 接口：helloworld、哈希算法、冒泡排序 |
| F1 | 前端页面 | 新增页面，3 个 tab 分别展示 3 个接口的执行结果 |
| B2+F2 | 导出能力 | 后端导出接口 + 前端导出按钮，支持导出各 tab 展示结果 |

---

## 2. 仓库现状（已验证事实）

### [leecode] 后端
- 纯 IntelliJ IDEA 算法刷题项目，模块文件 `leecode/leecode.iml`。
- **无 Maven/Gradle 构建系统**（无 `pom.xml` / `build.gradle`）。
- `leecode/src/` 下为 `l01_two_sum` ~ `l10_regular_expression_matching` 独立题目目录 + `test.java`。
- `java资料/` 含 `spring/`、`mybatis/`、`dubbo/` 等学习资料（非可运行工程）。
- 现状：无 Web 框架、无 HTTP 接口能力、无构建产物机制。

### [haikulou1.github.io] 前端
- 静态博客站点（GitHub Pages），基于 NexT 主题。
- `index.html`（2086 行）+ `js/`（`motion.js`/`utils.js`/`local-search.js` 等纯静态脚本）+ `css/`。
- **无前端框架（React/Vue）、无构建工具（webpack/vite）、无 npm 依赖**。
- 部署形态：纯静态托管，无法运行 Java 后端。

---

## 3. 技术选型决策（安全兜底：改动最小、风险最低）

| 决策点 | 选定方案 | 理由 |
|--------|---------|------|
| 后端框架 | JDK 内置 `com.sun.net.httpserver.HttpServer` | leecode 无构建系统；零外部依赖，直接复用现有 IDEA 工程，无需引入 Maven，改动最小、风险最低 |
| 前端选型 | 纯 HTML + 原生 JS + CSS | 纯静态博客无框架/构建工具；零依赖，与现有站点风格一致，改动最小 |
| 导出格式 | CSV（通用、Excel 可打开） | 最通用、风险最低；后端原生 `text/csv` 字节流即可，无需第三方库 |
| 哈希算法 | SHA-256（默认）+ MD5 | JDK 内置 `MessageDigest`，零依赖，覆盖主流场景 |
| 冒泡排序输入 | 后端生成随机整数数组 | 自包含、可复现对比，无需前端传参，降低接口复杂度 |

---

## 4. 后端设计 [leecode]

### 4.1 新增文件结构

```
leecode/
  src/
    server/
      ApiServer.java                  # HttpServer 启动入口 + 路由注册 + CORS
      handlers/
        HelloWorldHandler.java        # /api/helloworld
        HashHandler.java              # /api/hash
        BubbleSortHandler.java        # /api/bubble-sort
        ExportHandler.java           # /api/export
      model/
        ApiResponse.java              # 统一响应体 {code,msg,data}
```

### 4.2 接口契约（向后兼容，仅新增）

#### 4.2.1 GET /api/helloworld
- 入参：无
- 出参：
```json
{
  "code": 200,
  "msg": "success",
  "data": { "message": "Hello, World!" }
}
```

#### 4.2.2 GET /api/hash
- 入参（Query）：
  - `input`：待哈希字符串（必填）
  - `algo`：算法，`sha256` | `md5`，默认 `sha256`
- 出参：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "input": "abc",
    "algo": "sha256",
    "hash": "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
  }
```
- 错误：`input` 为空返回 `code=400, msg="input required"`

#### 4.2.3 GET /api/bubble-sort
- 入参：无（后端生成 8 个随机整数，范围 [0,100]）
- 出参：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "original": [5, 3, 8, 1, 9, 2, 7, 4],
    "sorted": [1, 3, 4, 5, 7, 8, 9],
    "count": 8
  }
}
```
- 算法：标准冒泡排序（升序），返回排序前后对比

#### 4.2.4 GET /api/export
- 入参（Query）：
  - `type`：`helloworld` | `hash` | `bubble-sort`（必填，指定导出哪个结果）
  - `format`：`csv`（默认 csv，当前仅支持 csv，预留扩展）
- 出参：二进制 CSV 流
  - 响应头：
    - `Content-Type: text/csv; charset=UTF-8`
    - `Content-Disposition: attachment; filename=<type>-result.csv`
  - CSV 内容按 type 不同：
    - `helloworld`：单行 `field,value` + `message,Hello, World!`
    - `hash`：先调 `/api/hash`（默认 input=空串+sha256，或导出最近示例），输出 `input,algo,hash` 行
    - `bubble-sort`：输出 `index,original,sorted`，按数组下标对齐
- 实现要点：`ExportHandler` 内部复用三个 handler 的业务逻辑生成数据，再转 CSV 字节流。

### 4.3 CORS 与统一处理
- 所有 handler 设置响应头：`Access-Control-Allow-Origin: *`、`Access-Control-Allow-Methods: GET, OPTIONS`、`Access-Control-Allow-Headers: Content-Type`。
- `OPTIONS` 预检请求直接返回 204。
- 统一响应体 `ApiResponse`，JSON 序列化使用手写拼接（零依赖，不引入 Jackson）。

### 4.4 启动方式
- `ApiServer.main()` 启动 HttpServer，监听 `0.0.0.0:8080`。
- 本地运行：`javac` 编译 + `java com.server.ApiServer`（或在 IDEA 中直接运行）。
- 端口可通过 `System.getProperty("port")` 覆盖，默认 8080。

---

## 5. 前端设计 [haikulou1.github.io]

### 5.1 新增文件结构
```
haikulou1.github.io/
  algorithm-demo.html        # 新增页面
  js/
    algorithm-demo.js        # tab 切换 + 接口调用 + 导出下载
  css/
    algorithm-demo.css       # 页面样式
```

### 5.2 页面结构（algorithm-demo.html）
- 顶部标题区：标题 + 全局「导出」按钮（导出当前激活 tab 的结果）。
- Tab 导航：3 个 tab —— `HelloWorld`、`哈希算法`、`冒泡排序`。
- Tab 内容区：每个 tab 一块 `<div>`，切换显示。
  - HelloWorld tab：调用 `/api/helloworld`，展示 `data.message`。
  - 哈希算法 tab：输入框 `input` + 算法下拉 `algo` + 「计算」按钮 → 调 `/api/hash`，展示 `data.hash`。
  - 冒泡排序 tab：「生成」按钮 → 调 `/api/bubble-sort`，展示 `original`/`sorted` 两列对比。
- 每个 tab 内可单独导出（按钮），顶部按钮导出当前 tab。

### 5.3 JS 逻辑（algorithm-demo.js）
- 常量 `API_BASE_URL`（默认 `http://localhost:8080`，可配置）。
- `switchTab(tabId)`：切换显示，记录当前激活 tab。
- `fetchHelloWorld()` / `fetchHash()` / `fetchBubbleSort()`：`fetch` 调对应接口，渲染结果。
- `exportCurrent()`：根据当前 tab 构造 `type`，`window.open(API_BASE_URL + '/api/export?type=' + type + '&format=csv')` 触发下载（或动态创建 `<a download>`）。
- 错误处理：接口失败时在内容区显示错误信息。

### 5.4 样式（algorithm-demo.css）
- 与现有静态站点风格一致（简洁卡片、与 NexT 主题配色协调）。
- Tab 激活态高亮，内容区留白。

---

## 6. 跨库接口契约（对齐点）

| 对齐点 | 约定 |
|--------|------|
| 统一响应体 | `{code:number, msg:string, data:object}`，前端从 `data` 取业务字段 |
| CORS | 后端 `Access-Control-Allow-Origin: *`，前端静态托管可跨域调用 |
| 导出 | 后端 `text/csv` + `Content-Disposition: attachment`；前端 `<a download>` / `window.open` 触发下载 |
| 后端地址 | 前端 `API_BASE_URL` 可配置，默认 `http://localhost:8080` |
| 接口路径 | 固定前缀 `/api/`，路径全小写连字符 |
| 算法参数 | `algo` 取值 `sha256` / `md5`，默认 `sha256` |

---

## 7. 部署与联调说明（非本次实现范围，文档备查）

- 后端：本地 `java` 运行 `ApiServer`，监听 8080。
- 前端：静态托管（GitHub Pages 或本地静态服务器），通过 `API_BASE_URL` 指向本地后端。
- 跨域：开发期依赖后端 CORS；生产期若需同源，需反向代理（超出本次范围）。

---

## 8. 验收标准

1. [leecode] 启动 `ApiServer` 后，`curl http://localhost:8080/api/helloworld` 返回 JSON，含 `data.message`。
2. [leecode] `curl 'http://localhost:8080/api/hash?input=abc&algo=sha256'` 返回正确 SHA-256 哈希值。
3. [leecode] `curl http://localhost:8080/api/bubble-sort` 返回 `original` 与 `sorted` 数组，`sorted` 为升序。
4. [leecode] `curl 'http://localhost:8080/api/export?type=bubble-sort&format=csv'` 返回 CSV 文件流，`Content-Disposition` 含 attachment。
5. [haikulou1.github.io] 浏览器打开 `algorithm-demo.html`，三个 tab 可切换并正确展示对应接口结果。
6. [haikulou1.github.io] 点击导出按钮，浏览器下载对应 CSV 文件。
7. 跨域：前端页面对后端接口的请求不被 CORS 阻断。

---

## 9. 风险与降级

| 风险 | 缓解 |
|------|------|
| JDK HttpServer 为 `com.sun` 内部 API，非正式规范 | 功能稳定且长期可用；设计文档已记录，后续可平滑迁移至 Spring Boot |
| 静态站点生产环境跨域 | 开发期 CORS 通配；生产期如需同源需反向代理（文档备查，非本次范围） |
| 哈希 input 含特殊字符 | URL 编码处理；后端 `URLDecoder.decode` |
| 导出大数据量 | 当前为示例数据，量小；如需扩展可改流式写入 |

---

## 10. 范围声明

本阶段为「需求澄清（系分设计）」，仅产出本设计文档，**不修改任何代码文件**。
后续「编码实现」阶段将按本设计执行：后端新增 Java 文件、前端新增 HTML/JS/CSS 文件，并按验收标准验证。

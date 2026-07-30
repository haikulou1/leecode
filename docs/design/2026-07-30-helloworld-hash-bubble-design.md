# 设计文档：HelloWorld / 哈希算法 / 冒泡排序 接口 + 前端三 Tab 展示 + 导出

- 日期：2026-07-30
- 阶段：需求澄清 / 系分设计
- 涉及仓库：leecode（后端，Java） / haikulou1.github.io（前端，静态站）

---

## 1. 需求理解

1. **后端（leecode，Java）**：提供三个"接口"——HelloWorld、哈希算法、冒泡排序。
2. **前端（haikulou1.github.io）**：新增一个页面，含三个 tab，分别展示三种执行结果。
3. **导出能力**：前端新增导出按钮；后端提供导出接口，支持导出"各个页面的展示结果"（即按当前 tab 导出对应结果）。

## 2. 跨仓现状（已验证事实）

| 仓库 | 性质 | 关键事实 |
|---|---|---|
| leecode | Java 刷题练习仓（IntelliJ IDEA 工程） | `leecode/leecode.iml` + `leecode/src/l01_two_sum/...`；`datastruct/`(acm.iml)、`designmodel/`、`rpc/`。**无 pom.xml/build.gradle、无 Spring Boot、无任何 Web 框架、无 HTTP 服务能力**。 |
| haikulou1.github.io | Hexo 3.9.0 静态博客（NexT Gemini 主题） | `index.html` 为静态生成产物，`js/`、`css/`、`page/` 为静态资源。**纯静态站点，无后端运行时**。 |

**架构错位**：需求隐含"前端调用后台接口"，但现状是 leecode 非 Web、github.io 无后端。两者当前都不具备 HTTP 提供或调用能力。

## 3. 方案决策

### 3.1 候选方案对比

| 方案 | 后端形态 | 前端如何拿到结果 | 导出由谁做 | 侵入/风险 |
|---|---|---|---|---|
| A. 引入轻量 HTTP 服务 | JDK 内置 `HttpServer` 暴露 3 算法 + 导出接口 | AJAX 调后端接口 | 后端导出接口 | 低，零外部依赖 |
| B. 纯 Java 方法 + 前端静态展示 | 纯 Java 类/方法，main 入口产出结果 | 结果写死前端 | 前端 JS | 中，违反"后台提供导出接口" |
| C. HTTP 服务 + 前端 JS 自行实现算法 | HTTP 暴露 3 算法 | AJAX 调接口 | 前端 JS | 中，与"后台提供导出接口"冲突 |
| D. Spring Boot HTTP 服务 | Spring Boot REST | AJAX 调接口 | 后端导出接口 | 高，需改造构建链 |

### 3.2 选定方案：A

裁定依据（自主决策优先级）：
1. **上下文优先**：需求明确"后台提供导出接口" → 必须有后端 HTTP 能力，排除 B、C。
2. **契约优先**：三个算法接口 + 导出接口均为新增，向后兼容，不破坏现有刷题目录。
3. **安全兜底**：leecode 当前无构建体系（无 pom.xml/build.gradle）；引入 Spring Boot 侵入大、需改造构建链；改用 **JDK 内置 `com.sun.net.httpserver.HttpServer`（零外部依赖、纯标准库）** 风险最低、改动最小，且满足"用 Java 写接口"。

### 3.3 跨仓调用拓扑

```
[haikulou1.github.io 静态页面]
   ├── tab1 HelloWorld   ──GET /api/helloworld────┐
   ├── tab2 哈希算法     ──GET /api/hash──────────┤── (CORS 允许) ──┐
   ├── tab3 冒泡排序     ──GET /api/bubblesort────┤               │
   └── 导出按钮(按当前tab)──GET /api/export?type=─┘               ▼
                                                          [leecode Java HTTP 服务]
                                                            com.sun.net.httpserver.HttpServer
                                                            端口 8999 (可配)
                                                            纯标准库，零依赖
```

## 4. 后端设计（leecode）

### 4.1 目录结构（新增，不改刷题目录）

```
leecode/src/main/java/io/leecode/demo/
├── server/
│   ├── DemoServer.java          # main 入口，启动 HttpServer，注册路由
│   └── CorsFilter.java          # CORS 处理（前端跨域）
├── handler/
│   ├── HelloWorldHandler.java   # GET /api/helloworld
│   ├── HashHandler.java         # GET /api/hash?input=...
│   ├── BubbleSortHandler.java   # GET /api/bubblesort?input=5,3,8,1
│   └── ExportHandler.java       # GET /api/export?type=helloworld|hash|bubblesort
├── algo/
│   ├── HashAlgo.java            # 哈希算法实现（SHA-256）
│   └── BubbleSort.java          # 冒泡排序实现
└── model/
    └── ApiResult.java           # 统一响应结构 {code,msg,data}
```

> 放在 `leecode/src/main/java/...`，与现有 `leecode/src/l01_two_sum/...` 刷题目录并列，不修改刷题代码。

### 4.2 接口契约

统一响应：`{ "code": 0, "msg": "ok", "data": <object> }`

| 接口 | Method | Path | 入参 | 出参 data |
|---|---|---|---|---|
| HelloWorld | GET | `/api/helloworld` | 无 | `{ "message": "HelloWorld" }` |
| 哈希算法 | GET | `/api/hash` | `input`(string) | `{ "input": "...", "algorithm": "SHA-256", "hash": "<hex>" }` |
| 冒泡排序 | GET | `/api/bubblesort` | `input`(逗号分隔整数) | `{ "input": [5,3,8,1], "output": [1,3,5,8], "steps": <步骤数> }` |
| 导出 | GET | `/api/export` | `type`(helloworld/hash/bubblesort) | 文件下载：`Content-Type: text/csv`，`Content-Disposition: attachment; filename=<type>-result.csv` |

- 哈希算法选 **SHA-256**（`java.security.MessageDigest`，标准库）。
- 冒泡排序：经典升序实现，返回排序前后 + 比较步数。
- 导出：后端按 type 复算结果，输出 CSV（列：序号, 字段, 值），通过 `Content-Disposition` 触发浏览器下载。
- CORS：后端对所有 `/api/*` 响应 `Access-Control-Allow-Origin: *`，支持 OPTIONS 预检。

### 4.3 算法实现要点（伪契约）

- `HashAlgo.sha256(String input): String` → 返回小写十六进制。
- `BubbleSort.sort(int[] arr): int[]` → 原地升序，返回结果；`BubbleSort.sortWithSteps(int[] arr): SortResult{output, steps}`。
- HelloWorld 无算法，直接返回常量串。

## 5. 前端设计（haikulou1.github.io）

### 5.1 新增页面

- 路径：`page/demo/index.html`（与现有 `page/2/index.html` 同级，新增分页目录）。
- 三 tab：HelloWorld / 哈希算法 / 冒泡排序。
- 每个 tab：输入区（哈希/冒泡需要输入框）+ "执行"按钮 + 结果展示区。
- 导出按钮：位于页面顶部工具栏，导出**当前激活 tab** 的结果（调用 `/api/export?type=<当前tab>`）。

### 5.2 调用方式

- 纯静态 HTML + 内联 JS（`fetch`），不引入框架，与博客静态站一致。
- 后端地址通过常量 `const API_BASE = 'http://localhost:8999'` 配置（部署时改）。
- 跨域由后端 CORS 解决。

### 5.3 导出按钮行为

- 点击导出 → `window.location = API_BASE + '/api/export?type=' + currentTab`（浏览器触发文件下载）。
- 若当前 tab 未执行，导出空结果或提示先执行（设计中默认导出后端复算结果，不依赖前端缓存，保证导出数据与服务端一致）。

## 6. 跨仓对齐点

| 对齐点 | leecode 侧 | github.io 侧 | 一致性保证 |
|---|---|---|---|
| 接口路径 | `/api/helloworld` `/api/hash` `/api/bubblesort` `/api/export` | fetch 调用同路径 | 文档契约固化 |
| 入参格式 | hash: `input` 串；bubblesort: `input` 逗号串；export: `type` 枚举 | 前端按契约拼接 query string | 契约表 |
| 出参结构 | 统一 `{code,msg,data}` | 前端解析 `data` 字段 | ApiResult 模型 |
| 导出文件名 | `Content-Disposition: filename=<type>-result.csv` | 浏览器自动下载 | HTTP 头 |
| CORS | 响应 ACAO:* + OPTIONS 预检 | 前端无需特殊处理 | CorsFilter |
| 端口/部署 | 8999（可配） | `API_BASE` 常量 | 配置约定 |

## 7. 验证方式（实现阶段用）

- 后端：`javac` 编译 + `java io.leecode.demo.server.DemoServer` 启动，`curl` 各接口。
- 前端：静态页用浏览器打开，切 tab、执行、导出。
- 降级：若环境无 JDK 或编译失败 ≥2 次，转静态契约审查（入参/出参类型匹配）。

## 8. 未决/待实现阶段处理

- 导出格式当前定 CSV（通用、零依赖）。若需 Excel，后续可加 Apache POI（引入依赖，阶段升级）。
- 前端是否纳入博客导航栏：待实现阶段与用户确认，默认仅新增 `page/demo/index.html` 不改导航。
- 本文档为澄清/系分阶段产物，不含代码实现；代码变更留待"编码实现"阶段。

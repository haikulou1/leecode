# 算法演示全栈功能 设计文档

- 日期：2026-07-30
- 阶段：需求澄清 / brainstorming（本阶段不修改任何代码文件）
- 涉及仓库：[leecode]（Java 后端）、[haikulou1.github.io]（前端静态页）
- 技能：/brainstorming

## 1. 需求理解

1. 后端：用 Java 写三个「接口」——helloworld、哈希算法、冒泡排序。
2. 前端：新增一个页面，含三个 tab，分别展示三个后端执行结果。
3. 导出：前端新增导出按钮；后台提供导出接口，支持导出各页面（各 tab）的展示结果。

## 2. 现状勘探（已核实）

### [leecode] 仓库
- 定位：算法 / 数据结构练习仓库。
- `leecode/src/` 下按题目分目录（如 `l01_two_sum/Solution.java`），为独立 Java 源文件。
- `leecode.iml` 为 IntelliJ IDE 模块文件；**无 pom.xml / 无 build.gradle / 无任何构建系统**，也无 Spring/Web 依赖。
- 结论：当前不具备 Web 后端能力，「接口」需从零搭建。

### [haikulou1.github.io] 仓库
- 定位：Hexo 3.9.0 + NexT 7.4.0 主题生成的**纯静态博客**。
- 根目录为已构建静态产物（`index.html`、`css/`、`js/`、`page/`、`lib/`）；无 `_config.yml` 源、无 `package.json`、无前端构建工具链。
- `js/` 下为 NexT 主题脚本（`local-search.js`、`motion.js` 等）。
- 结论：前端本质是静态站点，「tab 展示执行结果」= 新增一个静态 HTML 页面 + 原生 JS。

## 3. 关键决策（自主采纳，安全兜底 + 契约优先）

| # | 决策 | 选定 | 理由 |
|---|------|------|------|
| 1 | Java「接口」形态 / 前后端架构 | 轻量 HTTP 服务（JDK 内置 `com.sun.net.httpserver.HttpServer`） | 零外部依赖、无需构建系统，契合 leecode 现状；前端可 fetch 联动；可做文件下载导出 |
| 2 | 「哈希算法」含义 | 哈希函数（MD5 + SHA-256 摘要） | 最常见展示语义；输入文本输出定长摘要 |
| 3 | 导出格式与范围 | 三个 tab 结果聚合成单个 txt 文件下载 | 字面匹配「各页面展示结果」，实用且实现简单 |
| 4 | 冒泡排序输入 | 固定示例数组 + 随机生成数组各展示一次 | 便于演示排序过程与步数 |
| 5 | 设计文档落盘仓库 | [leecode] | 后端接口为核心业务承载，Java 实现需可被 leecode 模块追踪 |

> 说明：需求出现「后台导出接口」「前端 tab 展示执行结果」，隐含前后端分离 + 运行中的后端服务。采用轻量 HTTP 服务可在不引入构建系统的前提下满足该语义，风险最低、改动最小。

## 4. 技术方案

### 4.1 后端 [leecode]

- 技术栈：Java（JDK 内置 `com.sun.net.httpserver.HttpServer`），**零外部依赖**。
- 编译/运行：`javac` 直接编译，`java` 启动（契合 leecode 无构建系统现状）。
- 新增目录：`leecode/src/algorithm_demo/`（跟随 leecode 按目录组织源码惯例）。

#### 文件清单（编码阶段产出，本阶段仅设计）
- `AlgorithmDemoServer.java` — main 入口，启动 HttpServer（端口 8080），注册路由，统一处理 CORS。
- `HelloWorldHandler.java` — `/api/helloworld`，返回 `{"result":"Hello, World!"}`。
- `HashHandler.java` — `/api/hash?text=xxx`（可选，默认示例），输出 `{"input","md5","sha256"}`。
- `BubbleSortHandler.java` — `/api/bubble`，对示例数组 + 随机数组执行冒泡排序，返回 `{"original","sorted","steps"}`。
- `ExportHandler.java` — `/api/export`，聚合三个结果，`Content-Disposition: attachment; filename=...`，返回 text/plain 文件流下载。

#### 接口契约（REST，均为 GET，便于前端直接访问与浏览器触发下载）

| 路径 | 方法 | 入参 | 出参 JSON |
|------|------|------|-----------|
| `/api/helloworld` | GET | 无 | `{"result":"Hello, World!"}` |
| `/api/hash` | GET | `?text=xxx`（可选） | `{"input":"...","md5":"...","sha256":"..."}` |
| `/api/bubble` | GET | 无 | `{"original":[...],"sorted":[...],"steps":N}` |
| `/api/export` | GET | 无 | `text/plain` 文件下载（三段聚合，分段标注） |

### 4.2 前端 [haikulou1.github.io]

- 技术栈：原生 HTML + 内联 CSS + 原生 JS（fetch）。无构建工具。
- 新增页面：`algorithm-demo/index.html`（根目录新建子目录，作为静态页面可直接访问）。

#### 页面结构
- 三个 tab：HelloWorld / 哈希算法 / 冒泡排序。
- 每个 tab：一个「执行」按钮 + 结果展示区（点击后 fetch 对应接口并渲染）。
- 全局：一个「导出」按钮 → 访问 `/api/export` 触发浏览器文件下载。
- 配置：JS 顶部声明 `const API_BASE = 'http://localhost:8080'`，便于切换环境。

## 5. 跨仓对齐点（契约兼容性）

1. **接口契约一致性**：前端 fetch 路径（`/api/helloworld` 等）与响应字段名必须与后端 Handler 输出完全一致。
2. **CORS**：后端统一在响应头加 `Access-Control-Allow-Origin: *`（GET 简单请求，无需预检）。
3. **导出链路**：前端导出按钮 = 浏览器直接访问 `/api/export`（或 fetch 后转 blob 下载）；后端 `ExportHandler` 内部调用三个业务逻辑方法聚合结果，保证导出内容与页面展示一致（同一套底层逻辑）。
4. **契约优先**：所有接口为新增，向后兼容（不改动任何既有代码/既有接口）。

## 6. 风险与降级

- leecode 无构建系统 → 用 `javac`/`java` 直接编译运行；若环境无 JDK 则无法运行验证，降级为静态审查（对照接口契约入参/出参类型匹配）。
- 前端静态站与后端跨域 → 已由 CORS 覆盖。
- `com.sun.net.httpserver` 为 JDK 内置公开 API，稳定可用。
- 验证总耗时上限 5 分钟；同模块编译/测试 ≥2 次仍失败即降级静态审查。

## 7. 验收标准

- 后端四个接口可独立访问并返回符合契约的 JSON / 文件流。
- 前端页面三个 tab 可分别触发并展示对应结果。
- 导出按钮可下载含三个 tab 结果的 txt 文件。
- 不改动任何既有文件，仅新增文件。

## 8. 后续阶段

- 编码实现阶段：按本设计在 [leecode] 新增 `algorithm_demo/` 后端、在 [haikulou1.github.io] 新增 `algorithm-demo/` 前端页面，并按接口契约联调。

## 9. Spec 自审（brainstorming 自审步骤）

- 契约一致性：四接口路径、方法、入参、出参字段已明确且前后端对齐。✅
- YAGNI：未引入 Spring Boot / 构建系统 / 前端框架，仅满足需求最小集。✅
- 风险兜底：零依赖 HTTP 服务 + CORS，降级路径明确。✅
- 落盘合规：设计文档写入 [leecode] worktree_path 下，未落到 worktree 父目录。✅

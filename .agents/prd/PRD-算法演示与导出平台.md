# PRD — 算法演示与导出平台

| 项 | 值 |
|---|---|
| 文档版本 | v1.0 |
| 创建日期 | 2026-07-30 |
| 当前阶段 | clarify（需求澄清完成，待进入编码实现） |
| 涉及仓库 | [leecode]（后端 Java）、[haikulou1.github.io]（前端静态页） |
| 决策来源 | brainstorming 技能探索 + 自主裁定（用户拒绝交互提问后按安全兜底接管） |

---

## 1. 背景与目标

### 1.1 背景
现有两个独立仓库：
- **[leecode]**：纯 IntelliJ IDEA 刷题项目，含 `datastruct`（数据结构算法）与 `leecode`（力扣题解）两个手工模块，无 Web 框架、无构建工具（pom/gradle）、无 HTTP 服务入口。已存在可复用算法：`datastruct/src/排序/冒泡排序.java`、`datastruct/src/Hashtab/HashTabDemo.java`（哈希表 demo，非密码哈希）。
- **[haikulou1.github.io]**：Hexo 3.9 + NexT 主题生成的纯静态 GitHub Pages 博客，`index.html` 为构建产物（2086 行），`js/` 全为主题脚本，无后端服务能力。

### 1.2 目标
构建一个前后端联动的「算法演示与导出平台」：
1. 后端（Java）提供三个算法接口：HelloWorld、哈希算法（SHA-256）、冒泡排序。
2. 后端提供统一导出接口，支持导出三个 tab 各自的展示结果（CSV 格式）。
3. 前端新增一个独立演示页面，含三个 tab 分别展示三种算法执行结果，并提供导出按钮触发后端导出下载。

### 1.3 非目标（本期不做）
- 不改造 Hexo 构建链路、不修改主题源码。
- 不引入 Spring Boot 或 maven/gradle 构建工具。
- 不做用户认证、权限、持久化存储。
- 不做生产级部署运维（Nginx 反代、域名、HTTPS）。

---

## 2. 自主决策记录（澄清结论）

> 用户在 clarify 阶段拒绝了交互式澄清提问，按硬性约束「全流水线模式 — 静默接管所有决策权」与自主决策优先级（安全兜底：风险最低、改动最小）裁定如下。

| 决策项 | 裁定结果 | 裁定依据 |
|---|---|---|
| 后端形态 | 轻量 `com.sun.net.httpserver.HttpServer`（JDK 自带，零依赖） | leecode 无构建工具，安全兜底、改动最小；`javac` 直编直跑 |
| 前后端联通 | 前端 fetch 调后端 HTTP API + 后端 CORS 开放 | 最贴合「后台提供导出接口」语义 |
| 导出格式 | CSV（纯文本流式写出） | 零依赖、兼容性最好、风险最低 |
| 哈希算法选型 | SHA-256 | 避免弱哈希 MD5，安全兜底 |
| 冒泡排序语义 | 入参整数数组，返回排序结果 + 交换次数 | 丰富 tab 展示信息 |
| 前端页面落点 | [haikulou1.github.io] 根目录独立 `demo.html` + `js/demo.js` | 不污染 Hexo 主题构建链 |
| 后端包落点 | [leecode] `datastruct/src/server/` | 与现有 `排序/`、`Hashtab/` 同模块，就近放置 |

### 2.1 候选方案对比（brainstorming 探索产出）

| 维度 | 方案A：轻量 HttpServer ✅推荐 | 方案B：Spring Boot | 方案C：纯类无HTTP |
|---|---|---|---|
| 后端依赖 | 零（JDK 自带） | 需引入 Spring + pom | 无 |
| 构建 | `javac` 直编 | 需 maven | `javac` |
| 改动面 | 小 | 大（新增构建工具链） | 最小但前端无法联通 |
| 前端可联通 | ✅ | ✅ | ❌ |
| 风险 | 最低 | 中 | 高（需求不完整） |

---

## 3. 用户角色与使用场景

| 角色 | 场景 |
|---|---|
| 访客（匿名） | 打开前端 demo 页面，切换三个 tab 查看算法执行结果 |
| 访客（匿名） | 点击导出按钮，下载当前 tab 对应结果的 CSV 文件 |

无登录、无权限区分。

---

## 4. 功能需求

### F1 后端算法接口

#### F1.1 HelloWorld 接口
- **路径**：`GET /api/helloworld`
- **入参**：无
- **出参**：
  ```json
  {
    "result": "Hello World",
    "timestamp": 1753872000000
  }
  ```
- **逻辑**：固定返回字符串 "Hello World"，附带当前时间戳。

#### F1.2 哈希算法接口
- **路径**：`GET /api/hash`
- **入参**：
  | 参数 | 类型 | 必填 | 约束 | 说明 |
  |---|---|---|---|---|
  | input | string | 是 | 非空，长度 ≤ 1024 | 待哈希的原始字符串 |
- **出参**：
  ```json
  {
    "input": "hello",
    "algorithm": "SHA-256",
    "hash": "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
    "length": 64
  }
  ```
- **逻辑**：使用 `java.security.MessageDigest.getInstance("SHA-256")`，对入参 UTF-8 字节哈希，输出十六进制小写字符串。
- **异常**：input 缺失或空 → HTTP 400，`{"error":"input is required"}`。

#### F1.3 冒泡排序接口
- **路径**：`GET /api/bubble`
- **入参**：
  | 参数 | 类型 | 必填 | 约束 | 说明 |
  |---|---|---|---|---|
  | data | string | 是 | 逗号分隔整数，1~50 个，每个 [-1000,1000] | 待排序数组 |
- **出参**：
  ```json
  {
    "input": [5, 3, 8, 1],
    "sorted": [1, 3, 5, 8],
    "steps": 4,
    "asc": true
  }
  ```
- **逻辑**：经典冒泡排序（升序），统计交换次数 `steps`，返回原数组、排序结果、交换次数、排序方向。
- **复用**：参考现有 `datastruct/src/排序/冒泡排序.java` 排序逻辑。
- **异常**：data 缺失/格式非法 → HTTP 400，`{"error":"data format invalid"}`。

### F2 后端导出接口

#### F2.1 导出接口
- **路径**：`GET /api/export`
- **入参**：
  | 参数 | 类型 | 必填 | 可选值 | 说明 |
  |---|---|---|---|---|
  | tab | string | 是 | `helloworld` \| `hash` \| `bubble` | 指定导出哪个 tab 的结果 |
- **出参**：CSV 文件流
  - Content-Type: `text/csv; charset=utf-8`
  - Content-Disposition: `attachment; filename="helloworld.csv"`
- **CSV 结构（按 tab）**：
  - helloworld.csv：
    ```csv
    field,value
    result,Hello World
    timestamp,1753872000000
    ```
  - hash.csv（导出时后端用固定示例输入 `hello`，避免无参）：
    ```csv
    field,value
    input,hello
    algorithm,SHA-256
    hash,2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
    length,64
    ```
  - bubble.csv（导出时后端用固定示例 `5,3,8,1`）：
    ```csv
    field,value
    input,"[5,3,8,1]"
    sorted,"[1,3,5,8]"
    steps,4
    asc,true
    ```
- **逻辑**：后端内部调用对应算法逻辑生成结果，再序列化为 CSV 流式返回。
- **异常**：tab 非法 → HTTP 400，`{"error":"invalid tab"}`。

### F3 前端演示页面

#### F3.1 页面结构
- **文件**：[haikulou1.github.io] 根目录 `demo.html` + `js/demo.js` + `css/demo.css`
- **入口**：独立静态页，不纳入 Hexo 构建，直接通过 `https://<域名>/demo.html` 访问。
- **布局**：
  - 顶部标题「算法演示与导出平台」
  - Tab 栏：三个 tab（HelloWorld / 哈希算法 / 冒泡排序）
  - 内容区：根据当前 tab 展示对应执行结果
  - 右上角「导出当前结果」按钮

#### F3.2 各 Tab 交互

**Tab1 HelloWorld**
- 自动调用 `GET /api/helloworld`
- 展示返回的 result 字符串与时间戳

**Tab2 哈希算法**
- 输入框：待哈希字符串（默认值 `hello`）
- 「执行」按钮 → 调用 `GET /api/hash?input=xxx`
- 展示：input、algorithm、hash（等宽字体）、length

**Tab3 冒泡排序**
- 输入框：逗号分隔整数（默认值 `5,3,8,1`）
- 「执行」按钮 → 调用 `GET /api/bubble?data=5,3,8,1`
- 展示：原数组、排序后数组、交换次数、排序方向

#### F3.3 导出按钮
- 点击 → 调用 `GET /api/export?tab=<当前tab>`，触发浏览器下载对应 CSV。
- 使用 `window.location` 或隐藏 `<a download>` 跳转方式触发下载。

---

## 5. 非功能需求

| 项 | 要求 |
|---|---|
| 后端启动 | 单命令启动：`javac` 编译 + `java` 运行，监听 `127.0.0.1:8080` |
| 后端依赖 | 零外部依赖，仅 JDK 8+ 标准库 |
| 跨域 | 后端所有响应统一加 `Access-Control-Allow-Origin: *`、`Access-Control-Allow-Methods: GET, OPTIONS` |
| 前端兼容 | 现代浏览器（Chrome/Edge/Firefox 近 2 年版本），支持 fetch |
| 性能 | 单机本地，无并发要求；单请求响应 < 200ms |
| 安全 | input 长度 ≤ 1024 防滥用；data 元素数 ≤ 50 防资源耗尽；无 SQL/无文件系统写入 |
| 可维护性 | 后端按职责分包：server/service/handler；前端 JS 与 HTML/CSS 分离 |

---

## 6. 接口契约（跨库对齐点）

### 6.1 统一响应约定
- 所有算法接口返回 JSON，Content-Type: `application/json; charset=utf-8`
- 统一 CORS 头
- 错误响应：HTTP 4xx，body `{"error":"<message>"}`

### 6.2 契约清单

| 端点 | 方法 | 用途 | 调用方 |
|---|---|---|---|
| `/api/helloworld` | GET | 返回 helloworld 结果 | 前端 Tab1 |
| `/api/hash` | GET | 返回 SHA-256 哈希结果 | 前端 Tab2 |
| `/api/bubble` | GET | 返回冒泡排序结果 | 前端 Tab3 |
| `/api/export` | GET | 导出指定 tab 结果为 CSV | 前端导出按钮 |

### 6.3 跨库兼容性原则
- 契约仅「新增」，不修改两仓库任何现有文件（向后兼容）。
- 前端 baseURL（`http://127.0.0.1:8080`）集中在 `demo.js` 顶部常量，便于环境切换。

---

## 7. 文件落点规划（编码阶段执行清单）

### 7.1 后端 [leecode]
新增文件（均在 `datastruct/src/server/` 包下，不改动现有文件）：

| 文件 | 职责 |
|---|---|
| `server/Main.java` | 程序入口，启动 HttpServer，注册路由 |
| `server/RouterHandler.java` | 路由分发，统一加 CORS、错误处理 |
| `service/HelloWorldService.java` | HelloWorld 逻辑 |
| `service/HashService.java` | SHA-256 哈希逻辑 |
| `service/BubbleSortService.java` | 冒泡排序逻辑（参考现有 `排序/冒泡排序.java`） |
| `service/ExportService.java` | 导出 CSV 逻辑，内部调用上述 service |
| `util/JsonUtil.java` | 简易 JSON 序列化（零依赖，手写拼接） |

### 7.2 前端 [haikulou1.github.io]
新增文件（根目录，不纳入 Hexo 构建）：

| 文件 | 职责 |
|---|---|
| `demo.html` | 演示页骨架 |
| `js/demo.js` | tab 切换、接口调用、导出触发逻辑 |
| `css/demo.css` | 演示页样式（link 复用现有 `css/main.css` 基础样式） |

---

## 8. 依赖关系与复用

### 8.1 后端复用
- `BubbleSortService` 参考现有 `datastruct/src/排序/冒泡排序.java` 的冒泡交换逻辑，但封装为纯函数返回结果数组与交换次数（原文件为 `main` 内联演示，不直接 import，避免中文包名路径问题）。
- `HashService` 与现有 `Hashtab/HashTabDemo.java`（哈希表数据结构 demo）无关系，是独立的密码哈希实现。

### 8.2 前端复用
- `demo.html` link 现有 `css/main.css` 获取基础 normalize 样式。
- 不复用 `js/` 下任何主题脚本。

---

## 9. 验收标准

### 9.1 后端验收
- [ ] `javac` 编译全部新增 Java 文件无错误
- [ ] `java server.Main` 启动后监听 8080 端口
- [ ] `curl http://127.0.0.1:8080/api/helloworld` 返回 `{"result":"Hello World",...}`
- [ ] `curl 'http://127.0.0.1:8080/api/hash?input=hello'` 返回正确 SHA-256 哈希
- [ ] `curl 'http://127.0.0.1:8080/api/bubble?data=5,3,8,1'` 返回排序结果 `[1,3,5,8]` 与 steps
- [ ] `curl 'http://127.0.0.1:8080/api/export?tab=bubble'` 返回 CSV 文件流
- [ ] 所有响应含 CORS 头
- [ ] 非法入参返回 HTTP 400

### 9.2 前端验收
- [ ] `demo.html` 可独立打开，三个 tab 可切换
- [ ] Tab1 自动加载 helloworld 结果
- [ ] Tab2 输入字符串执行哈希展示结果
- [ ] Tab3 输入数组执行排序展示结果
- [ ] 导出按钮可下载对应 CSV 文件
- [ ] 跨域请求成功（后端 CORS 生效）

---

## 10. 风险与降级

| 风险 | 影响 | 缓解/降级 |
|---|---|---|
| 后端 HttpServer 不支持高并发 | 本地演示无影响 | 非目标，不做并发优化 |
| 前端 GitHub Pages 调本地 127.0.0.1 失败 | 线上页面无法调通后端 | PRD 明确后端地址为本地；线上仅展示静态结构，需用户本地起后端 |
| 中文包名 `排序` 路径编译问题 | 编译失败 | 新增 `server` 包用纯 ASCII 路径，不依赖中文目录 |
| 防超时降级 | 编译/测试失败 | 同模块编译 ≥2 次失败 → 静态审查跨仓对齐点 |

---

## 11. 待办与后续阶段

- 当前阶段：clarify 完成，PRD 产出。
- 下一阶段：进入「编码实现」阶段，按第 7 节文件清单落地代码。
- 后端端口约定：`8080`（可配置）。
- 编译运行命令（编码阶段验证）：
  ```bash
  cd <leecode worktree>/datastruct
  javac -encoding UTF-8 -d out src/server/*.java src/service/*.java src/util/*.java
  java -cp out server.Main
  ```

---

## 附录 A：产物落盘决策

```text
🎯 产物落盘决策（本 PRD 文档）：
- 选定仓库：[leecode]（核心业务库：后端 Java 算法与 HTTP 服务均在此承载）
- worktree_path：/root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-419f6010-3917-49c9-b052-75f7c1bbf34f/worktree/leecode-master
- 产物相对路径：.agents/prd/PRD-算法演示与导出平台.md
- 最终物理路径：/root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-419f6010-3917-49c9-b052-75f7c1bbf34f/worktree/leecode-master/.agents/prd/PRD-算法演示与导出平台.md
- 决策依据：核心业务库优先；PRD 随核心后端库归档
```

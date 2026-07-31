# Java 接口 Demo 设计文档（需求澄清产物）

> 阶段：需求澄清（设计/分析阶段）
> 技能：brainstorming
> 产物性质：本文件为需求澄清与设计产物，**不包含任何代码实现**。编码阶段将依据本文档实施。

---

## 1. 通览（仓库现状理解）

| 模块 | 构建方式 | 说明 |
|------|----------|------|
| `leecode/` | IntelliJ IDEA 原生项目（`leecode.iml`，无 Maven） | LeetCode 刷题，每题 `lXX_xxx/` 目录含 `Solution.java` + `README.md` |
| `datastruct/` | 原生项目（`acm.iml`） | 数据结构练习（Hashtab/kmp/排序/树/链表 等） |
| `designmodel/` | Maven 项目（`pom.xml`） | 设计模式 |
| 根目录 | 无统一构建 | 学习型仓库，**无 Web/Spring 框架依赖** |

**关键事实**：整个仓库为学习/刷题性质，所有模块均为纯 Java，无任何 HTTP/Web 框架。这直接影响「接口」的技术选型（见 §3）。

---

## 2. 需求原文与歧义识别

需求原文：**「用 java 分别写三个接口 helloworld、哈希算法」**

识别出 4 处歧义：

| # | 歧义点 | 说明 |
|---|--------|------|
| D1 | **「接口」语义** | Java 中「接口」可指 (a) `interface` 关键字定义的抽象类型；(b) HTTP API 端点。「helloworld 接口」在 (a) 语义下不自然，在 (b) 语义下是经典入门示例。 |
| D2 | **数量不一致** | 声称「三个接口」，但仅列举 2 项（helloworld、哈希算法）。**第三个接口缺失**。 |
| D3 | **哈希算法范围** | 哈希算法有多种（MD5/SHA-1/SHA-256/SHA-512/MurmurHash 等）。需求未指定具体算法。 |
| D4 | **技术栈/框架** | 若为 HTTP 接口，当前仓库无 Web 框架，需决定是否引入 Spring Boot 或用 JDK 原生方案。 |

---

## 3. 关键决策（按自主决策优先级裁定）

依据优先级：① 上下文已验证事实 > ② 风险最低/改动最小/符合现有架构惯例 > ③ 行业最佳实践。

### 决策 D1：「接口」= HTTP API 端点
- **依据①**：「helloworld 接口」是 HTTP 入门端点的经典语义；「分别写三个接口」的动词搭配在中文开发语境多指 HTTP API。
- **依据②**：若理解为 `interface` 抽象类型，「helloworld interface」语义不自然，故排除。
- **裁定**：按 HTTP API 端点设计。

### 决策 D4：技术栈采用 JDK 原生 `com.sun.net.httpserver.HttpServer`，不引入 Spring Boot
- **依据②（风险最低+符合现状）**：仓库为学习型纯 Java 项目，无 Maven 统一构建（仅 designmodel 有 pom）。引入 Spring Boot 需新增 Maven/Gradle 工程、大量依赖，改动大、与刷题仓库调性不符。
- **依据②（改动最小）**：`com.sun.net.httpserver.HttpServer` 为 JDK 自带（`rt.jar`），**零外部依赖**，单文件可运行，符合现有「原生 Java 项目」惯例。
- **依据③**：学习/Demo 场景下，原生 HttpServer 足以演示接口概念，避免框架学习成本掩盖主题。
- **裁定**：新建独立原生 Java 模块 `api/`，使用 JDK HttpServer，`main` 方法启动。

### 决策 D3：哈希算法接口提供多算法支持
- **依据③（最佳实践）**：哈希工具应支持常用算法，而非硬编码单一算法。
- **裁定**：接口接收 `algorithm` 参数（md5/sha1/sha256/sha512）+ `input` 原文，返回十六进制摘要。使用 JDK 自带 `java.security.MessageDigest`（零依赖）。

### 决策 D2：第三个接口 —— 如实标注缺失，给出候选，不编造
- **裁定**：第三个接口**需求缺失**，本阶段不编造为既定事实。提供合理候选供后续确认：
  - 候选 A：**排序算法接口**（与「哈希算法」对称，同为算法类，接收数组+算法名返回排序结果）
  - 候选 B：**字符串反转接口**（常见 Demo 接口）
  - 候选 C：**时间/健康检查接口**（HTTP 服务惯例，`/health` 返回服务状态）
- **默认推进假设**：若编码阶段仍未澄清，按**候选 A 排序算法接口**实施（理由：与「哈希算法」对称，同为算法演示，主题一致，风险低）。该假设在文档中显式标注，可随时推翻。

---

## 4. 候选方案对比（brainstorming 探索）

针对技术栈选型，列出 3 种方案对比：

| 方案 | 实现 | 依赖 | 改动量 | 与仓库调性匹配 | 风险 |
|------|------|------|--------|----------------|------|
| **方案1：JDK 原生 HttpServer** | `com.sun.net.httpserver.HttpServer` | 无（JDK 自带） | 小（单模块+几个 .java） | 高（原生 Java 惯例） | 低 |
| 方案2：Spring Boot | `@RestController` + `@GetMapping` | 需引入 Spring Boot + Maven/Gradle | 大（新构建系统+大量依赖） | 低（刷题仓库无此调性） | 中（依赖管理复杂） |
| 方案3：Java interface 抽象类型 | `interface Helloable { ... }` + 实现类 | 无 | 小 | 中（但与「helloworld 接口」语义不符） | 低但偏离需求本意 |

**推荐方案1**：风险最低、改动最小、符合现有原生 Java 架构惯例。

---

## 5. 推荐方案：接口设计草案

### 5.1 模块结构
```
api/                          # 新建原生 Java 模块
├── DESIGN.md                 # 本设计文档
├── src/
│   ├── Main.java             # 启动 HttpServer，注册路由，main 方法
│   ├── handler/
│   │   ├── HelloWorldHandler.java    # GET /helloworld
│   │   ├── HashHandler.java          # GET /hash?algorithm=sha256&input=xxx
│   │   └── SortHandler.java         # GET /sort?algorithm=quicksort&input=3,1,2  (默认候选A)
│   └── util/
│       ├── HashUtil.java             # MessageDigest 封装
│       └── SortUtil.java             # 排序算法封装
└── README.md                 # 接口说明（中文，沿用仓库惯例）
```

### 5.2 接口定义

| # | 路径 | 方法 | 入参 | 出参 | 说明 |
|---|------|------|------|------|------|
| 1 | `/helloworld` | GET | 无 | `text/plain` → `Hello, World!` | 经典入门接口 |
| 2 | `/hash` | GET | `algorithm`(md5/sha1/sha256/sha512), `input`(原文) | `text/plain` → 十六进制摘要 | 哈希算法接口 |
| 3 | `/sort` | GET | `algorithm`(冒泡/快排等), `input`(逗号分隔整数) | `text/plain` → 排序后数组 | **默认候选A，待确认** |

### 5.3 错误处理
- 未知算法：HTTP 400，返回 `Unsupported algorithm: xxx`
- 参数缺失：HTTP 400，返回 `Missing required parameter: xxx`
- 未知路径：HTTP 404

### 5.4 技术要点
- `MessageDigest.getInstance(algorithm)` 实现哈希（JDK 自带，支持 MD5/SHA-1/SHA-256/SHA-512）
- `HttpServer.create(new InetSocketAddress(8080), 0)` 启动，`setExecutor(null)` 默认线程池
- 端口默认 8080，可通过 `args[0]` 覆盖

---

## 6. 文件规划（编码阶段将创建，本阶段不写代码）

> 以下为编码阶段变更清单预览，**本澄清阶段不创建任何 .java 文件**。

| 文件 | 操作 | 说明 |
|------|------|------|
| `api/DESIGN.md` | 新建 | 本文档（本次产出） |
| `api/src/Main.java` | 待编码阶段创建 | HttpServer 启动入口 |
| `api/src/handler/HelloWorldHandler.java` | 待编码阶段创建 | 实现 `HttpHandler` |
| `api/src/handler/HashHandler.java` | 待编码阶段创建 | 实现 `HttpHandler` |
| `api/src/handler/SortHandler.java` | 待编码阶段创建 | 默认候选A，待确认 |
| `api/src/util/HashUtil.java` | 待编码阶段创建 | 哈希工具 |
| `api/src/util/SortUtil.java` | 待编码阶段创建 | 排序工具 |
| `api/README.md` | 待编码阶段创建 | 接口说明 |

---

## 7. 风险与缺失项

| 类型 | 项 | 处理 |
|------|----|------|
| **缺失** | 第三个接口未指定 | 列候选 A/B/C，默认按 A 推进，显式标注可推翻 |
| 假设 | 「接口」=HTTP API | 已按证据裁定为 HTTP API，文档记录依据 |
| 假设 | 不引入 Spring Boot | 按最小改动+符合现状裁定 |
| 假设 | 哈希接口支持多算法 | 按最佳实践裁定 |
| 风险 | `com.sun.net.httpserver` 为 JDK 内部 API，部分精简 JDK 可能缺失 | 标准 Oracle JDK / 主流 OpenJDK 均含；学习场景可接受 |
| 风险 | 无统一 Maven 构建，各模块独立 | 新建 `api/` 为独立原生项目，与 `leecode/` 惯例一致 |

---

## 8. 验证策略（编码阶段）

1. `javac` 编译 `api/src/**/*.java`
2. `java Main` 启动服务
3. `curl http://localhost:8080/helloworld` → 期望 `Hello, World!`
4. `curl "http://localhost:8080/hash?algorithm=sha256&input=abc"` → 期望 `ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad`
5. `curl "http://localhost:8080/sort?algorithm=quicksort&input=3,1,2"` → 期望 `1,2,3`（候选A）

---

## 设计产物清点

| 产物 | 状态 | 路径 |
|------|------|------|
| 需求澄清设计文档 | ✅ 已产出 | `api/DESIGN.md` |
| 代码文件 | ⏸ 不在本阶段产出（阶段门控禁止） | — |

**澄清结论**：需求存在「接口语义、数量、哈希范围、技术栈」4 处歧义，已按自主决策优先级对 D1/D3/D4 裁定（HTTP API + JDK 原生 HttpServer + 多算法哈希），对 D2（第三个接口缺失）如实标注并给默认候选 A 待确认。进入编码阶段后，依据本设计文档实施。

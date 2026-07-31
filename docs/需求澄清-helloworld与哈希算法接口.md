# 需求澄清设计文档：helloworld 与 哈希算法 接口

> 阶段：需求澄清（分析/设计前置阶段）
> 技能：/brainstorming（交互决策点已由全自动流水线静默接管）
> 日期：2026-07-31
> 状态：澄清完成，待进入编码实现阶段

---

## 1. 通览 (Overview)

### 1.1 原始需求
> 用 java 分别写 2 个接口：helloworld、哈希算法

### 1.2 仓库现状
- 类型：多模块 IntelliJ IDEA + Maven Java 学习/示例仓库
- 既有模块：`datastruct`、`designmodel`、`leecode`、`rpc/{dubbo,nio,zookeeper,http和https,...}`、`java资料/{mybatis,...}`、`分布式`
- 构建惯例（基于 `rpc/dubbo/pom.xml` 证据）：
  - `groupId = cn.wy`
  - 源码目录：`src/main/java/cn/wy/`
  - 编译版本：`maven.compiler.source/target = 1.7`
  - 测试：JUnit 4.11（test scope）
- 无统一根 `pom.xml`（各模块独立 `pom.xml`）
- 仓库无任何 Spring/Spring Boot 依赖痕迹，既有 Java 文件均为原生 main/示例风格（如 `BootStrapp.java`、`App.java`）

### 1.3 关键依赖关系
- "接口"一词在本仓库语境下存在二义性：Java `interface`（抽象契约） vs HTTP REST 接口。
- 仓库为示例/学习型，无 Web 框架基础；引入 Spring Boot 与现有"原生示例"风格不符，但能最直接满足"接口"的 REST 语义。

---

## 2. 规划 (Planning) — 澄清决策

> 自动决策优先级：上下文已验证事实 > 风险最低/改动最小/符合现有架构惯例 > 行业最佳实践

### 2.1 澄清项与自动决策

| # | 澄清项 | 备选 | 决策 | 依据 |
|---|--------|------|------|------|
| D1 | "接口"语义 | A.Java interface / B.REST HTTP 接口 / C.main 方法演示 | **B. REST HTTP 接口**（Spring Boot `@RestController`） | "接口"在现代 Java 工程语境默认指对外暴露的 REST API；helloworld 作为最简 GET 端点是其典型形态 |
| D2 | 模块归属 | A.复用 `rpc/http和https` / B.新建独立模块 `helloworld-api` | **B. 新建独立模块 `helloworld-api`** | 现有模块均为特定主题示例，强塞会破坏主题一致性；独立模块改动隔离、风险最低、便于后续扩展 |
| D3 | 哈希算法具体算法 | A.MD5 / B.SHA-256 / C.SHA-512 / D.可配置多算法 | **D. 支持多算法（MD5/SHA-1/SHA-256/SHA-512），默认 SHA-256** | "哈希算法"未指定具体算法，多算法支持覆盖面最广且符合最佳实践；默认 SHA-256 为行业推荐安全哈希 |
| D4 | 哈希接口入参形态 | A.明文 query 参数 / B.JSON body | **A. 明文 query 参数 `input`（+可选 `algo`）** | helloworld 级别的演示接口，query 参数最简、可 curl 直接验证 |
| D5 | 统一返回结构 | A.裸字符串 / B.统一 JSON 包装 | **B. 统一 JSON 包装 `{code,message,data}`** | 统一响应体是 Java Web 工程惯例，便于后续接口扩展与前端对接 |
| D6 | Java/编译版本 | A.沿用 1.7 / B.升级 | **A. 沿用 1.7**（与 `rpc/dubbo` 既有惯例一致） | 符合现有架构惯例；避免因版本不一致引发跨模块问题。注：Spring Boot 需选兼容 JDK1.7 的版本（见 D7） |
| D7 | Spring Boot 版本 | — | **Spring Boot 1.5.22.RELEASE** | 该系列最后兼容 JDK 1.7 的版本；与 D6 的 1.7 约束一致 |
| D8 | 包路径 | — | `cn.wy.helloworld` | 沿用 `cn.wy` 根包惯例 |

### 2.2 接口契约（设计产物）

#### 接口一：helloworld
- 路径：`GET /api/helloworld`
- 入参：无
- 出参：`{ "code": 200, "message": "success", "data": "Hello, World!" }`
- 语义：最简健康检查/演示端点

#### 接口二：哈希算法
- 路径：`POST /api/hash`（GET 也可，但 POST 更适合携带待哈希载荷；本设计采用 **GET + query** 以符合 D4 最简原则，最终定为 `GET /api/hash`）
- 入参：
  - `input`（必填，String）：待计算哈希的明文
  - `algo`（可选，String，默认 `SHA-256`）：算法名，取值 `MD5` / `SHA-1` / `SHA-256` / `SHA-512`
- 出参：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "algorithm": "SHA-256",
      "input": "abc",
      "hex": "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    }
  }
  ```
- 异常：`algo` 非法 → `code=400, message="unsupported algorithm: xxx"`；`input` 为空 → `code=400, message="input is required"`

### 2.3 后续编码实施清单（供编码阶段使用，本阶段不执行）
1. 新建模块 `helloworld-api/`，含 `pom.xml`（`groupId=cn.wy`，`artifactId=helloworld-api`，继承/引入 Spring Boot 1.5.22，compiler 1.7）
2. `src/main/java/cn/wy/helloworld/HelloWorldApplication.java` — Spring Boot 启动类
3. `src/main/java/cn/wy/helloworld/controller/HelloWorldController.java` — `@RestController`，实现 `GET /api/helloworld` 与 `GET /api/hash`
4. `src/main/java/cn/wy/helloworld/common/Result.java` — 统一响应体（泛型 `data`）
5. `src/main/java/cn/wy/helloworld/util/HashUtil.java` — 哈希工具类（MessageDigest，支持 MD5/SHA-1/SHA-256/SHA-512，输出小写 hex）
6. `src/main/resources/application.yml`（或 `.properties`）— 端口如 `server.port=8080`

---

## 3. 执行 (Execution)

> 阶段判定：当前为**需求澄清**阶段，等价 `需求分析` 路径 → 仅执行「通览」+「规划」，**禁止修改任何代码文件**。
> 本阶段产物为本设计文档本身，未对 `.java/.xml/.yaml` 等源码与配置做任何变更。

无代码变更执行。

---

## 4. 汇总 (Summary)

### 4.1 设计产物清点
| 产物 | 路径 | 说明 |
|------|------|------|
| 需求澄清设计文档 | `docs/需求澄清-helloworld与哈希算法接口.md` | 本文件；含澄清决策表、接口契约、后续实施清单 |

### 4.2 代码变更清单
- 无（本阶段为需求澄清，未触碰任何代码/配置文件）。

### 4.3 待澄清/风险提示
- R1：若实际意图是"Java `interface` 抽象类"而非 REST 接口，需在编码阶段前纠正 D1 决策（当前按 REST 决策，因其在现代 Java 工程为默认语义）。
- R2：JDK 1.7 + Spring Boot 1.5.22 为较旧组合；若环境实际为 JDK 8+，可在编码阶段升级为 Spring Boot 2.x/3.x（需重新评估 D6/D7）。
- R3：哈希接口用 GET 传明文，长文本受 URL 长度限制；若需大文本哈希应改为 POST body。当前按"最简演示"决策保留 GET。

### 4.4 建议下一步
进入 **编码实现** 阶段，依据 §2.3 实施清单创建 `helloworld-api` 模块并实现两个接口。

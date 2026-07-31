# Java 算法演示 + 前端三 Tab 展示 + 导出 设计文档

> 阶段：需求澄清 / 系分设计（clarify）
> 技能：brainstorming
> 日期：2026-07-31
> 状态：设计待评审（用户已拒绝交互澄清，按自主决策优先级裁定）

---

## 1. 通览 (Overview)

### 1.1 需求拆解

| # | 需求 | 归属仓库 | 性质 |
|---|------|---------|------|
| R1 | Java 写 helloworld / 哈希算法 / 冒泡排序 三个接口 | leecode | 后端 HTTP REST 接口 |
| R2 | 前端新增页面，3 个 tab 分别展示三种执行结果 | haikulou1.github.io | 静态页 + AJAX |
| R3 | 导出按钮 + 后台导出接口，支持导出各页面展示结果 | 两库协同 | 跨库契约 |

### 1.2 现状证据（已探查）

**[leecode] 仓库**（`/root/.agentix/.../worktree/leecode-master`）：
- Java 学习仓库，README 仅为 `# leecode刷题`
- **多个独立子模块**，各自带 `pom.xml`，**无统一根 pom**：
  - `designmodel/`（pom: groupId=`cn.wy`, JDK1.7, 含 servlet-api 2.5 + junit + testng）
  - `rpc/dubbo/`、`rpc/zookeeper/`、`rpc/nio/`、`rpc/MysqlDriver/`、`rpc/DataBaseDriver/`
  - `java资料/mybatis/`（pom）
  - `datastruct/`、`leecode/`（仅 .iml，无 pom）
- `java资料/spring/` 为 **Spring 源码注释版（gradle 工程，无 pom.xml）**，不可复用为 Web 服务模块
- 结论：**无现成可对外提供 HTTP 服务的模块**，需新建独立 Web 模块

**[haikulou1.github.io] 仓库**（`/root/.agentix/.../worktree/haikulou1.github.io-master`）：
- Hexo 3.9.0 + NexT 7.4.0（Gemini 主题）**静态博客产物仓库**
- 根目录为已构建产物：`index.html`、`css/`、`js/`、`lib/`、`page/`（仅含子目录 `2`）
- 无 `package.json`、无 `_config.yml` 在根（源码仓库结构，当前 worktree 为构建产物）
- 结论：前端"新增页面" = 新增一个可独立访问的静态 HTML 页面 + 伴随 JS

### 1.3 跨库协作拓扑

```
[haikulou1.github.io 静态页]  ──HTTP/AJAX──>  [leecode Spring Boot 服务]
  page/algo-demo/index.html                     algo-demo 模块 (:8080)
  3 tabs + 导出按钮                              /api/helloworld
  (CORS 跨域)                                   /api/hash
                                                /api/bubble-sort
                                                /api/export
```

静态博客（HTTPS/自定义域名）与 Java 后端（localhost:8080 或独立域名）分属不同源，**必然涉及 CORS**。

---

## 2. 规划 (Planning)

### 2.1 产物落盘决策

```text
🎯 产物落盘决策：
- 后端 Java 模块 → 仓库 [leecode]（核心业务库，承载接口与导出契约）
  · worktree_path: /root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-37c7a163-84c2-4d5b-ad22-5394182a870e/worktree/leecode-master
  · 新建模块相对路径: algo-demo/  （pom.xml + src/main/java/...）
- 前端页面 → 仓库 [haikulou1.github.io]（静态站点产物库）
  · worktree_path: /root/.agentix/agentic-dev/runs/DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-37c7a163-84c2-4d5b-ad22-5394182a870e/worktree/haikulou1.github.io-master
  · 新增页面相对路径: page/algo-demo/index.html + js/algo-demo.js
- 设计文档（本文件）→ 仓库 [leecode]（核心业务库，承载跨库契约定义）
  · 最终物理路径: /root/.agentix/.../worktree/leecode-master/.agents/design/java-frontend-demo-design.md
- 决策依据：leecode 为核心业务库（承载接口契约与导出逻辑）；前端为静态展示层，依赖后端契约。
```

### 2.2 自主裁定记录（用户拒绝交互澄清后的决策）

按自主决策优先级「上下文优先 → 契约优先 → 安全兜底」裁定三个关键架构点：

| 决策点 | 选项 | 裁定 | 依据 |
|--------|------|------|------|
| **D1 后端技术形态** | Spring Boot REST / 纯控制台 / 复用 spring 模块 | **Spring Boot REST API** | ①需求明确"接口"+"导出接口"语义=HTTP 服务；②spring 模块为源码注释版不可复用（已验证无 pom）；③契约优先：前端需 AJAX 调用，REST 最自然；④安全兜底：Spring Boot 内嵌 Tomcat，零外部容器依赖，启动即用 |
| **D2 哈希算法语义** | 哈希函数演示 / 哈希表数据结构 | **哈希函数演示（MD5/SHA-256）** | ①与 helloworld、冒泡排序并列为"算法演示"最自然；②"哈希算法"字面更倾向函数而非数据结构；③YAGNI：数据结构实现超出展示需求 |
| **D3 导出格式与粒度** | 单文件含三 tab / 每 tab 独立 / JSON 结构化 | **单文件含三 tab 结果（txt）** | ①需求原文"导出各个页面的展示结果"=一次性导出全部；②安全兜底：后端生成文件流，前端统一下载，逻辑最简；③契约向后兼容：可后续扩展按 tab 导出而不破坏现有 |

### 2.3 后端模块设计（[leecode] algo-demo）

**模块位置**：`leecode/algo-demo/`（独立 Maven 模块，不纳入无根 pom 的聚合，独立可构建）

**技术栈**：
- Spring Boot 2.7.18（Starter Web，内嵌 Tomcat，JDK8+ 兼容）
- groupId 沿用现有模块约定 `cn.wy`，artifactId=`algo-demo`
- 注：现有 designmodel 用 JDK1.7，但 Spring Boot 2.7 需 JDK8+；新模块独立声明 `maven.compiler.source/target=8`，不强制对齐旧模块

**目录结构**：
```
algo-demo/
├── pom.xml
└── src/main/
    ├── java/cn/wy/algodemo/
    │   ├── AlgoDemoApplication.java        # Spring Boot 启动类
    │   ├── config/
    │   │   └── CorsConfig.java             # 全局 CORS 配置
    │   ├── controller/
    │   │   └── AlgoController.java          # 四个接口入口
    │   ├── service/
    │   │   ├── HelloWorldService.java
    │   │   ├── HashService.java
    │   │   ├── BubbleSortService.java
    │   │   └── ExportService.java
    │   └── model/
    │       ├── HashRequest.java
    │       ├── HashResponse.java
    │       ├── BubbleSortRequest.java
    │       └── BubbleSortResponse.java
    └── resources/
        └── application.yml                # server.port=8080
```

### 2.4 跨库接口契约（前后端对齐）

> 所有接口前缀 `/api`，JSON 通信，CORS 允许前端源。

#### 接口 1：helloworld
```
GET /api/helloworld
Response 200:
{
  "result": "Hello, World!",
  "timestamp": "2026-07-31T12:00:00Z"
}
```

#### 接口 2：哈希算法
```
POST /api/hash
Request:
{
  "input": "abc",
  "algorithm": "SHA-256"      // 可选: MD5 / SHA-256 / SHA-512，默认 SHA-256
}
Response 200:
{
  "input": "abc",
  "algorithm": "SHA-256",
  "hash": "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
  "length": 64
}
```

#### 接口 3：冒泡排序
```
POST /api/bubble-sort
Request:
{
  "input": [5, 3, 8, 1, 9, 2]
}
Response 200:
{
  "input": [5, 3, 8, 1, 9, 2],
  "sorted": [1, 2, 3, 5, 8, 9],
  "steps": [                  // 每轮交换轨迹（展示用）
    {"round": 1, "swaps": 4, "array": [3, 5, 1, 8, 2, 9]},
    ...
  ],
  "swapCount": 8
}
```

#### 接口 4：导出
```
GET /api/export?tab=all      // tab: helloworld | hash | bubble-sort | all（默认 all）
Response 200:
  Content-Type: text/plain; charset=UTF-8
  Content-Disposition: attachment; filename="algo-export-<timestamp>.txt"
  Body: 三段结果文本（===== HelloWorld ===== / ===== Hash ===== / ===== BubbleSort =====）
```

### 2.5 前端页面设计（[haikulou1.github.io]）

**页面位置**：`page/algo-demo/index.html`（与现有 `page/2` 同级，独立可访问 URL：`/page/algo-demo/`）

**页面结构**：
- 三个 Tab：`HelloWorld` / `哈希算法` / `冒泡排序`
- 每个 Tab 内：输入区（hash/bubble-sort 有输入框，helloworld 无）+ 执行按钮 + 结果展示区
- 页面顶部/底部：`导出全部结果` 按钮
- 伴随 JS：`js/algo-demo.js`（原生 fetch，无框架依赖，契合静态站轻量风格）

**后端地址配置**：JS 顶部常量 `const API_BASE = 'http://localhost:8080'`（开发期），部署时改为实际后端域名。

### 2.6 跨库对齐点（契约清单）

| 对齐项 | 后端 [leecode] | 前端 [haikulou1.github.io] | 状态 |
|--------|---------------|---------------------------|------|
| 接口路径 | `/api/helloworld` `/api/hash` `/api/bubble-sort` `/api/export` | fetch 同路径 | ✅ 契约一致 |
| 请求/响应字段 | 见 2.4 | JS 按字段名读取 | ✅ 字段对齐 |
| CORS | 后端 CorsConfig 允许前端源 | 前端无需特殊处理 | ✅ 后端单向配置 |
| 导出文件名 | `algo-export-<ts>.txt` | 前端用 a[download] 触发 | ✅ |
| 端口/域名 | 8080（可配置） | API_BASE 常量 | ⚠️ 部署时需同步 |

---

## 3. 风险与回滚

| 风险 | 影响 | 缓解 |
|------|------|------|
| JDK 版本不一致（旧模块 1.7 vs 新模块 8） | 新模块独立声明 compiler.source=8，不影响旧模块 | 模块级隔离 |
| Maven 依赖下载失败（无网络/私服） | 构建阻塞 | 属跨库环境问题，触发降级协议转静态审查 |
| 静态站 HTTPS 调用 HTTP 后端（混合内容） | 浏览器拦截 | 后端生产部署需 HTTPS，或前端同源代理；设计阶段标注，部署时解决 |
| 导出大数组文件过大 | 内存/传输压力 | YAGNI：演示数据量小，不预做流式 |

**回滚**：各仓库改动均为新增文件（后端新模块 + 前端新页面），不修改既有文件，回滚即删除新增内容。

---

## 4. 设计产物清点（clarify 阶段输出）

| 产物 | 仓库 | 物理路径 | 状态 |
|------|------|---------|------|
| 本设计文档 | leecode | `.agents/design/java-frontend-demo-design.md` | ✅ 本次产出 |

> clarify 阶段不产出代码变更。后端 `algo-demo/` 模块与前端 `page/algo-demo/` 页面属后续「编码实现」阶段产物，本阶段仅定义契约与结构。

## 5. spec self-review

- ✅ 三接口契约字段完整、前后端对齐
- ✅ 导出契约覆盖"各个页面展示结果"（tab=all 默认）
- ✅ 跨库 CORS 约定明确
- ✅ 产物落盘路径均在选定 worktree 下，无父目录泄漏
- ✅ 无代码文件被修改（clarify 阶段约束遵守）
- ⚠️ 待确认项：后端部署域名/HTTPS（标注为部署期解决，不阻塞设计）

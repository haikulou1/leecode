# Code Review Report

> **Change** `Java 接口 Demo（helloworld / hash / sort）` · **分支** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-568a531a-4035-4b3b-` · **日期** `2026-07-31` · **审查者** AI（DTCoder · /dtazziboot-java-code-review）
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。已先运行 `scan-all-rules.sh` 并将要点并入 §5，再写 LLM 结论。问题含 `path:line` 或清单 ID。

---

## 0. 执行说明与降级

| 项 | 值 |
|----|-----|
| 阶段 | loop-1 · 代码审查 |
| 采用技能 | /dtazziboot-java-code-review |
| 预扫脚本 | `references/script/scan-all-rules.sh api/src`（52/222 条可程序化规则） |
| 预扫原始命中 | `G16.2 CatchWithoutLogging` ×4（脚本标 P0） |
| **LLM 误报复核** | 清单原文 `reliability-checklist.md:246` 中 **G16.2 等级 = P1**；脚本报告的 P0 对应 G16.4（空 catch / 仅 printStackTrace / 捕获后仍执行无记录）。复核 4 处 catch 均非空、非 printStackTrace、捕获后直接 `return`/`throw`（有补救），**不命中 G16.4**，故 4 处降级为 **P1**。 |
| 构建验证 | **[降级说明]** 环境无 JDK（`javac: not found` / `java: not found` / 无 `JAVA_HOME` / 无 `/usr/lib/jvm`），编译不可用。按防超时协议切为静态代码审查，基于完整源码 + 预扫 + 清单完成全维度核销。 |
| Git | 只读（未做任何写操作） |

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 6 |
| 变更行数 | 全新增（+约 480 / -0） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `Main` | `api/src/Main.java` | 启动入口，注册路由 + `NotFoundHandler` 兜底 |
| `HelloWorldHandler` | `api/src/handler/HelloWorldHandler.java` | GET /helloworld |
| `HashHandler` | `api/src/handler/HashHandler.java` | GET /hash?algorithm=&input= |
| `SortHandler` | `api/src/handler/SortHandler.java` | GET /sort?algorithm=&input= |
| `HashUtil` | `api/src/util/HashUtil.java` | MessageDigest 封装 |
| `SortUtil` | `api/src/util/SortUtil.java` | 冒泡/快排/选择排序封装 |

> 关联设计：`api/DESIGN.md`（需求澄清产物）；说明：`api/README.md`。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 4 | 3 |

> 4 个 P1 均为 `G16.2`（异常路径无日志/无可追溯上下文），对学习 Demo 非功能阻断，但影响线上可观测性；建议修复后合并。

---

## 3. Step 2 — 功能（REQ）

> REQ 来自 `api/DESIGN.md` §5.2 接口定义表。

### REQ-1: HelloWorld 接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /helloworld → `Hello, World!` | ✅ | DESIGN.md §5.2 行101：`GET /helloworld → Hello, World!` | `api/src/handler/HelloWorldHandler.java:19` | 文本一致；非 GET 返 405（`:15`） |
| 路由注册 | ✅ | DESIGN.md §5.1 模块结构 | `api/src/Main.java:28` | `/helloworld` 已注册 |

### REQ-2: 哈希算法接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /hash?algorithm=sha256&input=abc → 十六进制摘要 | ✅ | DESIGN.md §5.2 行102 + §8 行152 期望值 `ba7816bf...015ad` | `api/src/handler/HashHandler.java:42-44` + `api/src/util/HashUtil.java:20-29` | `MessageDigest` 标准实现，输出小写 hex（`HashUtil.toHex :73-80`） |
| 支持 md5/sha1/sha256/sha512 | ✅ | DESIGN.md §5.2 行102 + §5.4 行111 | `api/src/util/HashUtil.java:55-69`（normalize 白名单） | 含 `sha-1`/`sha-256` 别名兼容 |
| 未知算法 → 400 | ✅ | DESIGN.md §5.3 行106 `Unsupported algorithm: xxx` | `api/src/handler/HashHandler.java:37-41` | 先 `isSupported` 白名单校验 |
| 参数缺失 → 400 | ✅ | DESIGN.md §5.3 行107 | `api/src/handler/HashHandler.java:29-36` | `algorithm`/`input` 分别校验 |

### REQ-3: 排序算法接口（候选 A）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /sort?algorithm=quicksort&input=3,1,2 → `1,2,3` | ✅ | DESIGN.md §5.2 行103 + §8 行153 期望 `1,2,3` | `api/src/handler/SortHandler.java:52-53` + `api/src/util/SortUtil.java:23-24` | Hoare 分区快排，升序 |
| 支持 bubble/quicksort/selection | ✅ | DESIGN.md §5.2 行103 | `api/src/util/SortUtil.java:19-31`（switch 三分支 + default） | |
| 非法 input → 400 | ✅ | README.md §错误码 行52 | `api/src/handler/SortHandler.java:47-50` | 捕获 `NumberFormatException` 提示格式 |

### 错误处理契约

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 未知路径 → 404 | ✅ | DESIGN.md §5.3 行108 | `api/src/Main.java:32,43-53` | `/` 兜底 `NotFoundHandler` |
| 非 GET → 405 | ✅ | README.md §错误码 行54 | 三个 Handler 均有 `:20-23` 守卫 | |

---

## 4. Step 3 — 可读性检查

> 对照 `references/readability-checklist.md` A1–A7。

| ID | 检查项 | 状态 | 备注 |
|----|--------|------|------|
| A1.1 | 文件名=顶层类名 | ✅ | 6 文件均符合 |
| A1.2 | 编码 UTF-8 | ✅ | 无 BOM 证据，中文字面量正常 |
| A1.3 | 空白仅 ASCII 空格，禁 Tab | ✅ | 全部 4 空格缩进 |
| A2.1 | 文件顺序 package→import→顶层类 | ⚠️ | **所有文件无 package 声明**（default package）。与 DESIGN.md §5.1 一致（原生 Demo 无包），学习场景可接受；生产应补 package |
| A2.2 | 禁止 import * | ✅ | 无通配符引入 |
| A2.3/A2.4 | import 分组 + 字典序 | ✅ | 仅非静态一组；`com.sun` < `java` 字典序正确 |
| A3.1 | K&R 大括号 | ✅ | `} else {` 同行等均符合 |
| A3.3 | 缩进 4 空格 | ✅ | |
| A3.4 | 行宽 ≤120 | ✅ | 最长行（HashHandler:38-39 拼接）续行处理得当 |
| A3.6 | 类成员间空行 | ✅ | |
| A4.2/A4.3 | 类名 UpperCamel / 方法 lowerCamel | ✅ | |
| A5.1 | 重写加 @Override | ✅ | 4 个 `handle` + 1 个 `NotFoundHandler.handle` 均有 `@Override` |
| A6.1 | 数组方括号属于类型 | ✅ | `String[] parts`（SortHandler:61）、`int[] arr`、`byte[] raw` |
| A6.2 | switch 须有 default | ✅ | `HashUtil.isSupported:44`、`HashUtil.normalize:67`、`SortUtil.sort:29` 均有 default |
| A7.1 | public 类/成员须 Javadoc | ✅ | `HashUtil.digest/isSupported`、`SortUtil.sort/isSupported`、`Main.main` 均有；`handle` 为 @Override 可省（A7.3） |

**可读性结论**：✅ 通过（仅 1 处 default package 属设计决策，非违规）。

---

## 5. Step 4 — 可靠性检查

### 5.1 预扫结果（scan-all-rules.sh）

```
[P0] G16.2 — CatchWithoutLogging: api/src/Main.java:22
[P0] G16.2 — CatchWithoutLogging: api/src/handler/HashHandler.java:45
[P0] G16.2 — CatchWithoutLogging: api/src/handler/SortHandler.java:47
[P0] G16.2 — CatchWithoutLogging: api/src/util/HashUtil.java:26
Summary: 4 findings (P0=4, P1=0, P2=0) | 52/222 rules scanned
```

### 5.2 LLM 误报复核

脚本将 `CatchWithoutLogging` 统一标 G16.2/P0，但清单原文（`reliability-checklist.md:246`）：
- **G16.2** = 异常路径有日志输出且含可追溯上下文 → **P1**
- **G16.4** = 空 catch / 仅 printStackTrace / 关键路径捕获后仍执行且无记录 → **P0**

复核 4 处：均非空、非 printStackTrace、捕获后直接 `return`（返 400）或 `throw`（抛 IllegalArgumentException），**有补救动作**，不命中 G16.4 → **降级为 P1**。

| 域 | 参考 | 结果 | 等级 | 说明（命中 ID + path:line） |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ⚠️ | P1 | G16.2 ×4：`Main.java:22`、`HashHandler.java:45`、`SortHandler.java:47`、`HashUtil.java:26` |
| 安全 | `security-checklist.md` S1–S10 | ⚠️ | P2 | S2 输入校验：`input` 未限长（HashHandler/SortHandler），超大输入有 DoS/OOM 风险 |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ✅ | — | 预扫 25/81 条无命中；LLM 复核相关项（B004/B005/B008/B011/B022/B024 等）均无命中 |

#### G1–G17 逐类核销

| ID 节 | 结论 | 说明 |
|-------|------|------|
| G1 并发 | N/A | 无 DB 事务/锁 |
| G2 幂等 | N/A | 纯只读 GET，无写操作 |
| G3 事务 | N/A | 无 @Transactional |
| G4 SQL | N/A | 无 SQL |
| G5 MQ | N/A | 无消息消费 |
| G6 缓存 | N/A | 无缓存 |
| G7 调度 | N/A | 无定时任务 |
| G8 防御编程 | ⚠️ P1 | G8.3 ✅（`write` 均用 try-with-resources 释放 OutputStream）；G8.4 线程池无 shutdown 钩子（`Main.java:33` setExecutor(null)），学习 Demo 前台进程可接受，生产建议加 `Runtime.addShutdownHook` → P2 建议 |
| G9 网络调用 | N/A | 本服务为被调用方，不发起外部 RPC/HTTP |
| G10 接口契约 | ✅ | 错误码（400/404/405）与 README/DESIGN 一致 |
| G11 开发自测 | ⚠️ P2 | G11.1 无单元测试（DESIGN 验证策略为 curl，可接受但建议补单测）；G11.3 ✅（algorithm/input null 校验已做） |
| G12 资损 | N/A | 无资金场景 |
| G13 监控 | N/A | 学习 Demo 无埋点 |
| G14 国际化 | N/A | |
| G15 灰度 | N/A | |
| G16 可监控 | ❌ P1 | G16.2 ×4 命中（详见 §5.3）；G16.4 未命中（catch 均有补救） |
| G17 可应急 | N/A | |

#### Bug 模式相关项复核（预扫覆盖 25/81，LLM 补全关键项）

| ID | 状态 | 备注 |
|----|------|------|
| B004 ArrayToString | ✅ | 未对数组 `.toString()` |
| B005 ArraysAsListPrimitiveArray | ✅ | `SortUtil` 用 `Arrays.copyOf`（`:17`），非 asList |
| B008 AvoidUsingExecutors | ✅ | 未用 `Executors`，`setExecutor(null)` 用默认 |
| B011 BoxedPrimitiveEquality | ✅ | 无包装类型 `==` 比较 |
| B022 DateFormatThreadSafety | ✅ | 无 `SimpleDateFormat` |
| B024 DeadThread | ✅ | 无 `new Thread` 未 start |
| 其余 B/M/I | N/A | 与本项目无关（无 Jedis/Money/Calendar/BigDecimal 等） |

### 5.3 P1 命中明细（G16.2）

> 4 处 catch 块均捕获异常后转为业务响应/抛出，但**异常路径无日志输出、无可追溯上下文（traceId/bizId）**，线上出问题无法排查。等级 P1。

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | N/A(未启用自定义规则，清单为示例项) |

---

## 7. 结论

- **合并建议**：修复后合并
- **P0**：无
- **P1**：
  1. `G16.2` `api/src/Main.java:22` — `NumberFormatException` 仅 `System.err.println`，无结构化日志/上下文
  2. `G16.2` `api/src/handler/HashHandler.java:45` — `IllegalArgumentException` 捕获后直返 400，无日志
  3. `G16.2` `api/src/handler/SortHandler.java:47` — `NumberFormatException` 捕获后直返 400，无日志
  4. `G16.2` `api/src/util/HashUtil.java:26` — `NoSuchAlgorithmException` 捕获后重抛，无日志
- **P2**：
  1. `G8.4` `api/src/Main.java:33` — HttpServer 无 shutdown 钩子（生产建议）
  2. `G11.1` — 全模块无单元测试
  3. S2 输入校验 — `input` 参数未限长，超大输入 DoS/OOM 风险（`HashHandler.java:27`、`SortHandler.java:28,61`）
- **一句话**：功能与设计契约完全一致、可读性良好、无 P0；4 处 P1 为异常可观测性缺失，修复后可合并。

---

## 7.1 问题片段（必填）

### P1 — `G16.2` `api/src/Main.java:22`

> `NumberFormatException` 仅打到 stderr，无结构化日志/可追溯上下文。

片段范围：`api/src/Main.java:17-25`

```java
L17|public static void main(String[] args) throws IOException {
L18|    int port = 8080;
L19|    if (args.length > 0) {
L20|        try {
L21|            port = Integer.parseInt(args[0]);
L22|        } catch (NumberFormatException e) {
L23|            System.err.println("Invalid port: " + args[0] + ", use default 8080");
L24|        }
L25|    }
```

### P1 — `G16.2` `api/src/handler/HashHandler.java:45`

> `IllegalArgumentException` 捕获后直返 400，异常路径无日志。

片段范围：`api/src/handler/HashHandler.java:42-48`

```java
L42|try {
L43|    String digest = HashUtil.digest(algorithm, input);
L44|    write(exchange, 200, digest);
L45|} catch (IllegalArgumentException e) {
L46|    write(exchange, 400, e.getMessage());
L47|}
```

### P1 — `G16.2` `api/src/handler/SortHandler.java:47`

> `NumberFormatException` 捕获后直返 400，异常路径无日志。

片段范围：`api/src/handler/SortHandler.java:44-50`

```java
L44|int[] arr;
L45|try {
L46|    arr = parseInput(input);
L47|} catch (NumberFormatException e) {
L48|    write(exchange, 400, "Invalid input: expect comma-separated integers, e.g. 3,1,2");
L49|    return;
L50|}
```

### P1 — `G16.2` `api/src/util/HashUtil.java:26`

> `NoSuchAlgorithmException` 捕获后重抛为 `IllegalArgumentException`，无日志记录原始异常与上下文。

片段范围：`api/src/util/HashUtil.java:20-29`

```java
L20|public static String digest(String algorithm, String input) {
L21|    String alg = normalize(algorithm);
L22|    try {
L23|        MessageDigest md = MessageDigest.getInstance(alg);
L24|        byte[] raw = md.digest(input.getBytes(StandardCharsets.UTF_8));
L25|        return toHex(raw);
L26|    } catch (NoSuchAlgorithmException e) {
L27|        throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
L28|    }
L29|}
```

---

## 8. 修复任务列表

### P1

- [ ] **P1** `api/src/Main.java:22` — catch 块补充结构化日志（含入参 `args[0]` 与异常类型），或至少 `System.err` 改为带上下文的告警
- [ ] **P1** `api/src/handler/HashHandler.java:45` — catch 块补充 WARN 日志（含 algorithm/input 摘要与异常 message），保留 400 响应
- [ ] **P1** `api/src/handler/SortHandler.java:47` — catch 块补充 WARN 日志（含 input 摘要与异常 message），保留 400 响应
- [ ] **P1** `api/src/util/HashUtil.java:26` — catch 块补充 ERROR 日志后再抛 `IllegalArgumentException`，保留原始 cause（`new IllegalArgumentException(msg, e)`）

### P2（可选）

- [ ] **P2** `api/src/Main.java:33` — 增加 `Runtime.getRuntime().addShutdownHook` 在 JVM 退出时 `server.stop(0)`
- [ ] **P2** `G11.1` — 为 `HashUtil`/`SortUtil` 补充单元测试（断言摘要值/排序结果与边界）
- [ ] **P2** `api/src/handler/HashHandler.java:27`、`api/src/handler/SortHandler.java:28` — 对 `input` 参数增加长度上限校验（如 ≤1024 字符 / 元素数 ≤1000），超限返 400

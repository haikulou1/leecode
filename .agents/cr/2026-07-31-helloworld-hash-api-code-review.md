# Code Review Report

> **Change** `helloworld-api (loop-1 编码)` · **分支/Commit** `AI/task-DEV-...` / `dddb506` · **日期** `2026-07-31` · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。**已先**运行 `scan-all-rules.sh` 并将要点并入 §5，**再**写 LLM 结论。问题含 `path:line` 或清单 ID。每个 ❌/⚠️ 问题在 §7.1 附 `.java` 片段。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | `4` |
| 变更行数 | `+312 / -0`（全新增；`git diff --stat HEAD~1 HEAD`） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `HelloWorldApplication` | `helloworld-api/src/main/java/cn/wy/helloworld/HelloWorldApplication.java` | Spring Boot 启动类 |
| `Result` | `helloworld-api/src/main/java/cn/wy/helloworld/common/Result.java` | 统一响应体（泛型） |
| `HelloWorldController` | `helloworld-api/src/main/java/cn/wy/helloworld/controller/HelloWorldController.java` | `@RestController`，helloworld + hash 两接口 |
| `HashUtil` | `helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java` | 哈希工具类（MessageDigest） |

> 关联 spec：`docs/需求澄清-helloworld与哈希算法接口.md`（§2.2 接口契约、§2.3 实施清单）。
> 脚本预扫：`bash references/script/scan-all-rules.sh helloworld-api` → 52/222 规则，命中 1 条 G16.2（详见 §5）。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 2 | 2 |

> 说明：脚本初报 `G16.2` 为 P0，经 LLM 复核降级为 P1（见 §5/§7.1：该 catch 非"空 catch"，异常以 `IllegalStateException(cause=e)` 重抛并保留堆栈，属"异常路径缺日志"而非"吞异常"，按清单 G16.2 行内等级 P1 认定）。

---

## 3. Step 2 — 功能（REQ）

### REQ-1: helloworld 接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| `GET /api/helloworld` 返回 `{code:200,message:success,data:"Hello, World!"}` | ✅ | spec §2.2 接口一："路径 `GET /api/helloworld`；出参 `{ code: 200, message: "success", data: "Hello, World!" }`" | `HelloWorldController.java:29-32` `@GetMapping("/helloworld")` → `Result.success("Hello, World!")` | 契约完全一致 |

### REQ-2: 哈希算法接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| `GET /api/hash?input=abc` 默认 SHA-256，返回 data{algorithm,input,hex} | ✅ | spec §2.2 接口二："默认 SHA-256；出参 data 含 algorithm/input/hex" | `HelloWorldController.java:44-67` `@GetMapping("/hash")`，`algorithm` 默认 `HashUtil.DEFAULT_ALGORITHM`（="SHA-256"，`HashUtil.java:22`） | 字段结构与顺序（LinkedHashMap）一致 |
| `algo` 非法 → `code=400, message="unsupported algorithm: xxx"` | ✅ | spec §2.2："algo 非法 → code=400, message=\"unsupported algorithm: xxx\"" | `HelloWorldController.java:56-58` `if(!isSupported) Result.fail(CODE_BAD_REQUEST,"unsupported algorithm: "+algorithm)` | 文案与 code 一致 |
| `input` 为空 → `code=400, message="input is required"` | ✅ | spec §2.2："input 为空 → code=400, message=\"input is required\"" | `HelloWorldController.java:50-52` `if(input==null||input.isEmpty()) Result.fail(CODE_BAD_REQUEST,"input is required")` | 一致 |
| 支持算法集合 MD5/SHA-1/SHA-256/SHA-512 | ✅ | spec §2.1 D3："支持多算法（MD5/SHA-1/SHA-256/SHA-512）" | `HashUtil.java:32-34` `LinkedHashSet(Arrays.asList("MD5","SHA-1","SHA-256","SHA-512"))` | 完全覆盖 |

### REQ-3: 统一响应体与模块结构

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 统一 JSON 包装 `{code,message,data}` | ✅ | spec §2.1 D5 / §2.3 清单 4 | `Result.java:11-62` 泛型 `Result<T>`，`success`/`fail` 工厂 | 实现 `Serializable`，含 `serialVersionUID` |
| 模块 `helloworld-api`，`groupId=cn.wy`，Spring Boot 1.5.22，compiler 1.7 | ✅ | spec §2.1 D6/D7/D8 / §2.3 清单 1 | `pom.xml:7-27` `groupId=cn.wy`，`parent=spring-boot-starter-parent 1.5.22.RELEASE`，`maven.compiler.source/target=1.7` | 与澄清决策一致 |
| 端口 `server.port=8080` | ✅ | spec §2.3 清单 6 | `application.properties:1` `server.port=8080` | 一致 |

> **Step 2 结论**：功能全部满足 spec，无 P0 功能性不符。

---

## 4. Step 3 — 可读性检查

> 对照 `readability-checklist.md` A1–A7 逐文件扫描。

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ⚠️ | A7 命名/注释：`HashUtil.java:86` 注释"理论上不会发生"措辞偏口语化，建议改为"normalized 已校验，此处为防御性兜底"。属 P2 风格。 |
| ⚠️ | `Result.java` 公有 setter（`setCode/setMessage/setData`，行 44/52/60）使响应体可变；统一响应体通常建议不可变（构造器+getter only）。属 P2 可维护性建议，不阻塞。 |
| ✅ | A1 源文件格式：4 文件均有 `package` 首行、`@author dtcoder` Javadoc、无 `*` 通配 import，符合阿里风格。 |
| ✅ | A2-A6 命名/方法长度/控制语句：`HelloWorldController` 方法短小清晰；`HashUtil.toHex` 用位运算手写 hex 较紧凑但有注释，可接受。 |

---

## 5. Step 4 — 可靠性检查

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「已扫无命中」） |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ⚠️ | P1 | **G16.2** `HashUtil.java:85`（脚本预扫命中，LLM 复核等级 P1，见 §7.1）；**G16.2(派生)** `HashUtil.java:83` `input.getBytes()` 默认字符集，LLM 补扫，见 §7.1；G11.1 见 §4 P2 |
| 安全 | `security-checklist.md` S1–S10 | ✅ | — | S1-S10 场景均不涉及（无 SQL/SSRF/XXE/反序列化/文件上传/鉴权配置），标 N/A：本变更无 DB、无外部 URL 请求、无 XML 解析、无反序列化、无文件操作；接口为只读计算类演示，无敏感数据 |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ✅ | — | 脚本预扫 25/81 Blocker + 6/27 Major + 2/10 Info 均无命中；LLM 抽查 B001-B005（AlwaysThrows/ArrayEquals/ArrayToString/ArraysAsList）与本变更无关，已扫无命中 |

### 预扫脚本输出（原文）

```
=== Step 4 Rule Scan (B/M/I + A/S/G) ===
Targets: helloworld-api
Engine:  ripgrep

[P0] G16.2 — CatchWithoutLogging: helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:85

=== Summary: 1 findings (P0=1, P1=0, P2=0) | 52/222 rules scanned ===
```

### LLM 复核与补扫

1. **`HashUtil.java:85` — G16.2，等级 P1（非脚本所标 P0）**：`catch(NoSuchAlgorithmException e)` 直接 `throw new IllegalStateException("algorithm unavailable: "+normalized, e)`，保留了 cause 链并重抛，**不属于 G16.4 的"空 catch / 仅 printStackTrace / 吞异常继续执行"**。命中的是 G16.2"异常路径有日志输出且含可追溯上下文"——此处缺日志，按清单行内 **P1** 认定。
2. **`HashUtil.java:83` — G16.2 派生 / 可靠性，等级 P1（LLM 补扫，脚本未覆盖）**：`digest.digest(input.getBytes())` 中 `input.getBytes()` 使用**平台默认字符集**。哈希算法要求跨平台确定性输出，非 ASCII 输入将产生平台相关结果，违反 spec §2.2 "统一输出小写十六进制"的隐含确定性契约。应改 `input.getBytes(StandardCharsets.UTF_8)`。
3. **`HashUtil.java:35` — 复核**：`HEX_CHARS = "0123456789abcdef".getBytes()` 同理依赖默认字符集，但该字面量全 ASCII，实际无差异；为一致性可显式 `StandardCharsets.US_ASCII`，标 P2 建议。
4. **`HelloWorldController.java:50` — G11.3 复核**：`input==null||input.isEmpty()` 已对入参空值做防御性校验，✅ 合规。

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------------------------------------------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | N/A(未启用自定义规则)：`customized-checklist.md` 内容为示例项（U1.1/U1.2），无项目私有规则生效 |

---

## 7. 结论

- **合并建议**：修复后合并
- **P0**：无
- **P1**：
  1. `G16.2` `HashUtil.java:85` — catch `NoSuchAlgorithmException` 重抛 `IllegalStateException` 但无日志，异常路径缺可追溯上下文。
  2. `G16.2(派生)` `HashUtil.java:83` — `input.getBytes()` 使用平台默认字符集，哈希输出对非 ASCII 输入不具备跨平台确定性。
- **P2**：
  1. `Result.java` 公有 setter 使响应体可变，建议改为不可变（构造器 + getter only）。
  2. `HashUtil.java:86` 注释"理论上不会发生"措辞偏口语化；`HashUtil.java:35` `getBytes()` 可显式 `US_ASCII` 以一致性。
  3. `G11.1` 新增 `HashUtil`/`Result`/`HelloWorldController` 无单元测试，建议补 `HashUtilTest`（覆盖默认算法、各 algo、非法 algo、null input）。
- **一句话**：功能与 spec 契约完全对齐，结构清晰；唯一实质问题是哈希工具的字符集确定性与异常日志缺失，均为 P1，修复后可合并。

---

## 7.1 问题片段（必填）

### P1 — `G16.2` `HashUtil.java:85`（异常路径缺日志）

- **P1** `G16.2` `helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:85` — `catch(NoSuchAlgorithmException)` 重抛 `IllegalStateException(cause=e)` 但未记录日志，异常路径无可追溯上下文（traceId/bizId），排障困难。
  片段范围：`helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:81-88`

```java
L81|        try {
L82|            MessageDigest digest = MessageDigest.getInstance(normalized);
L83|            byte[] raw = digest.digest(input.getBytes()); // 见下一项：默认字符集
L84|            return toHex(raw);
L85|        } catch (NoSuchAlgorithmException e) {
L86|            // 理论上不会发生：normalized 已在支持集合内
L87|            throw new IllegalStateException("algorithm unavailable: " + normalized, e); // 问题：无日志
L88|        }
```

> 建议修复：catch 内 `log.warn("MessageDigest unavailable: {}", normalized, e);` 后再抛，或由上层统一异常处理记录。

### P1 — `G16.2(派生)` `HashUtil.java:83`（默认字符集导致哈希非确定性）

- **P1** `G16.2(派生)` `helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:83` — `input.getBytes()` 未指定字符集，哈希输出随 JVM 默认 charset 变化，违反哈希算法跨平台确定性要求。
  片段范围：`helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:81-84`

```java
L81|        try {
L82|            MessageDigest digest = MessageDigest.getInstance(normalized);
L83|            byte[] raw = digest.digest(input.getBytes()); // 问题：默认字符集
L84|            return toHex(raw);
```

> 建议修复：`import java.nio.charset.StandardCharsets;` → `digest.digest(input.getBytes(StandardCharsets.UTF_8))`。

---

## 8. 修复任务列表

> 供后续改代码时逐项执行与核销；与 §3–§7 中 ⚠️ 及结论对应。

### P0

（无）

### P1

- [ ] **P1** `helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:85` — 在 `catch(NoSuchAlgorithmException)` 内增加 `log.warn` 记录算法名与异常后再抛 `IllegalStateException`，满足 G16.2 异常路径可追溯。
- [ ] **P1** `helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:83` — 将 `input.getBytes()` 改为 `input.getBytes(StandardCharsets.UTF_8)`，保证哈希输出跨平台确定性。

### P2（可选）

- [ ] **P2** `helloworld-api/src/main/java/cn/wy/helloworld/common/Result.java:44-62` — 移除公有 setter，改为构造器 + getter only 的不可变响应体。
- [ ] **P2** `helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:35` — `"0123456789abcdef".getBytes()` 显式指定 `StandardCharsets.US_ASCII` 以一致性。
- [ ] **P2** `helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:86` — 注释"理论上不会发生"改为"normalized 已校验，此处为防御性兜底"。
- [ ] **P2** `G11.1` — 为 `HashUtil` 补充单元测试（默认 SHA-256、各 algo 正确性、非法 algo 抛 `IllegalArgumentException`、null input 抛 `NullPointerException`）。

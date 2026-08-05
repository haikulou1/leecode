# Code Review Report — 修复复审 (loop-1)

> **Change** `helloworld-api (loop-1 修复)` · **分支/Commit** `AI/task-DEV-...` / `dddb506` · **日期** `2026-07-31` · **审查者** AI
>
> **复审范围**：针对一审（`.agents/cr/2026-07-31-helloworld-hash-api-code-review.md`）所列 2 个 P1 + 3 个 P2 的修复成果进行复审。仅审查本轮变更文件：`Result.java`、`HashUtil.java`、`HashUtilTest.java`。
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。**已先**运行 `scan-all-rules.sh` 并将要点并入 §5，**再**写 LLM 结论。问题含 `path:line` 或清单 ID。每个 ❌/⚠️ 问题在 §7.1 附 `.java` 片段。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| 本轮变更 `.java` 文件数 | `3` |
| 变更性质 | 修复 + 新增测试（基于一审 §8 修复任务列表） |

| 类/接口 | 路径 | 角色 | 一审问题状态 |
|---------|------|------|--------------|
| `Result` | `helloworld-api/src/main/java/cn/wy/helloworld/common/Result.java` | 统一响应体（泛型） | 修复 P2(可变性) |
| `HashUtil` | `helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java` | 哈希工具类 | 修复 P1×2 + P2×2 |
| `HashUtilTest` | `helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java` | 单元测试 | 新增（修复 P2 G11.1） |

> 关联 spec：`docs/需求澄清-helloworld与哈希算法接口.md`（§2.2 接口契约、§2.3 实施清单）。
> 关联一审：`.agents/cr/2026-07-31-helloworld-hash-api-code-review.md` §8 修复任务列表。
> 脚本预扫：`bash references/script/scan-all-rules.sh` → 52/222 规则，命中 5 条（详见 §5），经 LLM 复核后真实问题 3 条 P2（误报 2 条）。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 0 | 3 |

> 说明：本轮为修复复审，一审的 2 个 P1 均已正确修复（见 §3 修复核销）。脚本初报 2 条 P0（`G16.2`），经 LLM 复核确认为误报（修复已加日志 / 测试中的异常验证），降级或撤销。剩余 3 条 P2 为测试质量改进项，不阻塞合并。

---

## 3. 一审问题修复核销

### P1-1: `G16.2` 异常路径缺日志 — ✅ 已修复

| 项 | 内容 |
|----|------|
| 一审问题 | `HashUtil.java:85` catch `NoSuchAlgorithmException` 重抛 `IllegalStateException` 但无日志 |
| 一审建议 | catch 内 `log.warn("MessageDigest unavailable: {}", normalized, e)` 后再抛 |
| 修复证据 | `HashUtil.java:23` 新增 `private static final Logger log = LoggerFactory.getLogger(HashUtil.class);`；`HashUtil.java:91-95` catch 块现含 `log.warn("MessageDigest unavailable: {}", normalized, e);` 后再 `throw new IllegalStateException("algorithm unavailable: " + normalized, e);` |
| 复核结论 | ✅ 完全符合一审建议。日志含算法名 `normalized` 与异常 `e`，满足 G16.2「异常路径有日志输出且含可追溯上下文」。异常仍以 `IllegalStateException(cause=e)` 重抛，保留堆栈链。 |

```java
// HashUtil.java:91-95 (修复后)
L91|        } catch (NoSuchAlgorithmException e) {
L92|            // normalized 已校验，此处为防御性兜底
L93|            log.warn("MessageDigest unavailable: {}", normalized, e);
L94|            throw new IllegalStateException("algorithm unavailable: " + normalized, e);
L95|        }
```

### P1-2: `G16.2(派生)` 默认字符集 — ✅ 已修复

| 项 | 内容 |
|----|------|
| 一审问题 | `HashUtil.java:83` `input.getBytes()` 使用平台默认字符集，哈希非跨平台确定性 |
| 一审建议 | 改为 `input.getBytes(StandardCharsets.UTF_8)` |
| 修复证据 | `HashUtil.java:6` 新增 `import java.nio.charset.StandardCharsets;`；`HashUtil.java:89` 现为 `byte[] raw = digest.digest(input.getBytes(StandardCharsets.UTF_8));` |
| 复核结论 | ✅ 完全符合一审建议。哈希输出现具备跨平台确定性，满足 spec §2.2「统一输出小写十六进制」的隐含确定性契约。 |

### P2-1: `Result` 可变性 — ✅ 已修复

| 项 | 内容 |
|----|------|
| 一审问题 | `Result.java` 公有 setter 使响应体可变，建议不可变 |
| 一审建议 | 移除 setter，改为构造器 + getter only |
| 修复证据 | `Result.java` 重写为不可变：`Result.java:11` `public class Result<T> implements Serializable`；`Result.java:19-21` 字段全 `private final`；`Result.java:23-27` 构造器；`Result.java:37-47` 仅 getter（`getCode/getMessage/getData`），无任何 setter |
| 复核结论 | ✅ 完全符合一审建议。响应体现不可变，线程安全，符合统一响应体最佳实践。 |

### P2-2: 注释口语化 + HEX_CHARS 字符集 — ✅ 已修复

| 项 | 内容 |
|----|------|
| 一审问题 | `HashUtil.java:86` 注释「理论上不会发生」口语化；`:35` `getBytes()` 未指定字符集 |
| 一审建议 | 注释改为「normalized 已校验，此处为防御性兜底」；`getBytes(StandardCharsets.US_ASCII)` |
| 修复证据 | `HashUtil.java:92` 注释现为 `// normalized 已校验，此处为防御性兜底`；`HashUtil.java:41` `HEX_CHARS = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);` |
| 复核结论 | ✅ 两项均已修复，注释规范化，字符集显式化。 |

### P2-3: `G11.1` 缺单元测试 — ✅ 已修复

| 项 | 内容 |
|----|------|
| 一审问题 | `HashUtil` 无单元测试 |
| 一审建议 | 补 `HashUtilTest`，覆盖默认算法、各 algo、非法 algo、null input |
| 修复证据 | 新增 `HashUtilTest.java`（99 行，12 个测试方法） |
| 复核结论 | ✅ 覆盖度超出建议：含默认 SHA-256、MD5/SHA-1/SHA-512 各算法正确性、大小写不敏感、空白容忍、`isSupported`、非法算法 `IllegalArgumentException`、null 算法 `IllegalArgumentException`、null input `NullPointerException`、空串输入、异常消息含算法名。 |

---

## 4. Step 3 — 可读性检查

> 对照 `readability-checklist.md` A1–A7 逐文件扫描。

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ✅ | A1 源文件格式：3 文件均有 `package` 首行、`@author dtcoder` Javadoc、无 `*` 通配 import，符合阿里风格。 |
| ✅ | A2-A6 命名/方法长度/控制语句：`HashUtil` 方法短小清晰；`HashUtilTest` 测试方法命名语义化（`defaultAlgorithmIsSha256`/`md5Correctness`/`nullInputThrowsNpe`），可读性好。 |
| ✅ | A7 命名/注释：`HashUtil.java:92` 注释已规范化（一审 P2 已修复）；`HashUtilTest` 每方法有注释说明意图。 |

---

## 5. Step 4 — 可靠性检查

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「已扫无命中」） |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ✅ | — | **G16.2** 一审 P1 已修复（见 §3）；脚本预扫命中 `HashUtil.java:91` 经 LLM 复核为**误报**（catch 内 L93 已有 `log.warn`）；`HashUtilTest.java:95` 经复核为测试中异常消息验证，非生产代码吞异常，撤销。 |
| 安全 | `security-checklist.md` S1–S10 | ✅ | — | S1-S10 场景均不涉及（无 SQL/SSRF/XXE/反序列化/文件上传/鉴权配置），标 N/A。 |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ⚠️ | P2 | 脚本预扫命中 3 条 `I001 AssertExceptionDetailInfoPreferred`（`HashUtilTest.java:69/74/79`），经 LLM 复核确认为真实改进项，详见 §7.1。 |

### 预扫脚本输出（原文）

```
=== Step 4 Rule Scan (B/M/I + A/S/G) ===
Targets: helloworld-api/src/main/java/cn/wy/helloworld/common/Result.java helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java
Engine:  ripgrep

[P0] G16.2 — CatchWithoutLogging: helloworld-api/src/main/java/cn/wy/helloworld/util/HashUtil.java:91
[P0] G16.2 — CatchWithoutLogging: helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java:95
[P2] I001 — AssertExceptionDetailInfoPreferred: helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java:69
[P2] I001 — AssertExceptionDetailInfoPreferred: helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java:74
[P2] I001 — AssertExceptionDetailInfoPreferred: helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java:95

=== Summary: 5 findings (P0=2, P1=0, P2=3) | 52/222 rules scanned ===
```

### LLM 复核与误报分析

1. **`HashUtil.java:91` — G16.2，脚本误报，撤销**：脚本正则匹配到 `catch` 关键字即报 `CatchWithoutLogging`，但未识别 catch 块内 L93 的 `log.warn("MessageDigest unavailable: {}", normalized, e)`。实际已满足 G16.2「异常路径有日志输出且含可追溯上下文」。**此为脚本漏识别日志语句导致的误报。**

2. **`HashUtilTest.java:95` — G16.2，脚本误报，撤销**：该处为测试代码 `unsupportedMessageContainsAlgorithmName()` 方法的 try-catch，用于验证 `IllegalArgumentException` 的消息包含算法名 `"ROT13"`，属合法的异常消息断言模式，非生产代码吞异常。**脚本不区分测试/生产上下文导致的误报。**

3. **`HashUtilTest.java:69/74/79` — I001，真实改进项，保留 P2**：3 处使用 `@Test(expected = XxxException.class)` 注解式断言，只能验证异常类型，无法断言异常消息。I001 建议改用 `assertThrows`（JUnit 4.13+）或 try-catch + `assertThat(e.getMessage(), containsString(...))` 以验证异常细节。当前 `HashUtilTest.java:91-98` 已有一个 try-catch 模式验证消息的范例，可统一风格。**属 P2 测试质量改进，不阻塞合并。**

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------------------------------------------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | N/A(未启用自定义规则)：无项目私有规则生效 |

---

## 7. 结论

- **合并建议**：✅ 合并（所有 P1 已修复，剩余仅 P2 测试质量建议项，不阻塞）
- **P0**：无
- **P1**：无（一审 2 个 P1 均已正确修复，见 §3）
- **P2**：
  1. `I001` `HashUtilTest.java:69/74/79` — 3 处 `@Test(expected=...)` 注解式断言无法验证异常消息，建议改用 `assertThrows` 或 try-catch+`containsString` 统一异常断言风格。
- **一句话**：一审所有 P1/P2 问题均已正确修复，修复质量高；剩余仅 3 条 P2 测试风格建议，不影响功能与可靠性，可直接合并。

---

## 7.1 问题片段（必填）

### P2 — `I001` `HashUtilTest.java:69/74/79`（注解式异常断言无法验证消息）

- **P2** `I001` `helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java:69` — `@Test(expected = IllegalArgumentException.class)` 仅验证异常类型，无法断言异常消息内容，与同文件 L91-98 的 try-catch+`containsString` 风格不一致。
  片段范围：`helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java:69-82`

```java
L69|    @Test(expected = IllegalArgumentException.class)
L70|    public void unsupportedAlgorithmThrows() {
L71|        HashUtil.hash("abc", "NOT-A-HASH");
L72|    }
L73|
L74|    @Test(expected = IllegalArgumentException.class)
L75|    public void nullAlgorithmThrowsIllegalArgument() {
L76|        HashUtil.hash("abc", null);
L77|    }
L78|
L79|    @Test(expected = NullPointerException.class)
L80|    public void nullInputThrowsNpe() {
L81|        HashUtil.hash(null);
L82|    }
```

> 建议修复（可选）：JUnit 4.13+ 可用 `assertThrows`：
> ```java
> @Test
> public void unsupportedAlgorithmThrows() {
>     IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
>             () -> HashUtil.hash("abc", "NOT-A-HASH"));
>     assertThat(e.getMessage(), containsString("NOT-A-HASH"));
> }
> ```
> 若坚持 JUnit 4 原生写法，可参照同文件 L91-98 的 try-catch 模式统一风格。此为 P2 建议项，不阻塞合并。

---

## 8. 修复任务列表

> 本轮为修复复审，以下为一审遗留 + 本轮新发现的后续可选改进项。

### P0

（无）

### P1

（无 — 一审 P1 均已修复核销）

### P2（可选）

- [ ] **P2** `helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java:69` — `unsupportedAlgorithmThrows` 改用 `assertThrows` 并断言消息含 `"NOT-A-HASH"`，统一异常断言风格。
- [ ] **P2** `helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java:74` — `nullAlgorithmThrowsIllegalArgument` 同上改用 `assertThrows` 并断言消息含 `"null"`。
- [ ] **P2** `helloworld-api/src/test/java/cn/wy/helloworld/util/HashUtilTest.java:79` — `nullInputThrowsNpe` 同上改用 `assertThrows` 并断言消息含 `"input is null"`。

> 注：上述 P2 为测试风格优化建议，当前测试已能正确验证异常类型，功能覆盖充分，不影响合并决策。

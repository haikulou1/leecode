# Code Review Report

> **Change** `helloworld-hash-api` · **分支/Commit** `AI/task-DEV-...` / `96db8fe`（复审 Round 2，原 `4e1983b`） · **日期** `2026-07-31` · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。✅ 已运行 `scan-all-rules.sh`（Round 2），要点已并入 §5。
>
> **复审结论**：Round-1 的 4 项问题（G16.2 / S9.3 / A5.1 / I001）**全部已修复**；Round-2 scan 报告的 4 处 G16.2 经 LLM 复核均为**误报**。当前 P0=0 / P1=0 / P2=0，建议合并。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 4（复审范围：Round-2 修改文件） |
| 变更行数 | `+29 / -4`（commit `96db8fe`） |

| 类/接口 | 路径 | 角色（可选） |
|---------|------|--------------|
| `HashAlgorithm` | `designmodel/src/main/java/cn/wy/hash/HashAlgorithm.java` | 接口（REQ-2，Javadoc 补充「非安全用途」） |
| `HashAlgorithmImpl` | `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java` | 实现（REQ-2，补 @Override + LOGGER.log） |
| `HelloWorldImpl` | `designmodel/src/main/java/cn/wy/helloworld/HelloWorldImpl.java` | 实现（REQ-1，补 @Override） |
| `HashAlgorithmTest` | `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java` | 测试（REQ-2，异常断言改 try/catch） |

> Round-1 全量 6 文件中，`HelloWorld.java` 与 `HelloWorldTest.java` 未在 Round-2 修改，复审沿用 Round-1 结论（✅）。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 0 | 0 |

> Round-1 为 `P0=0 / P1=2 / P2=3`；Round-2 已全部修复，当前清零。

---

## 3. Step 2 — 功能（REQ）

### REQ-1: HelloWorld 接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| `sayHello()` 返回 "Hello, World!" | ✅ | `§4.1 接口一：HelloWorld` | `HelloWorldImpl.java:12-13` | 固定串一致（行号因 +@Override 后移） |
| `sayHello("Java")` 返回 "Hello, Java!" | ✅ | `§4.1` + `§3 Q2` | `HelloWorldImpl.java:18-22` | 拼接正确 |
| null/空串退化为 "World" | ✅ | `§3 Q2 决策` | `HelloWorldTest.java` 全覆盖 | 未改 |

### REQ-2: HashAlgorithm 接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 三方法签名 `hash/md5/sha256` | ✅ | `§4.2 接口二` | `HashAlgorithm.java:20/30/38` | 一致（行号因 Javadoc 后移） |
| 基于 `MessageDigest` 输出小写 hex | ✅ | `§4.2 决策 B` | `HashAlgorithmImpl.java:28-30,59-69` | UTF-8+小写，核心逻辑未改 |
| 标准向量校验 MD5/SHA-256/SHA-1 | ✅ | `§4.3 验证策略` | `HashAlgorithmTest.java:16-33` | 向量正确，未改 |

---

## 4. Step 3 — 可读性检查

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ✅ | Round-1 `A5.1`（缺 @Override）**已修复**：`HashAlgorithmImpl.java:19,42,48`、`HelloWorldImpl.java:11,17` 均已补加 `@Override` |

---

## 5. Step 4 — 可靠性检查

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「已扫无命中」） |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ✅ | — | Round-1 `G16.2`（catch 无日志）**已修复**：`HashAlgorithmImpl.java:32,36` 新增 `LOGGER.log(Level.WARNING/SEVERE, ...)`；Round-2 scan 仍报 G16.2×2 为**误报**（脚本不识别 JUL `LOGGER.log` 形式） |
| 安全 | `security-checklist.md` S1–S10 | ✅ | — | Round-1 `S9.3`（暴露 MD5/SHA-1）**已缓解**：`HashAlgorithm.java:13-14,24-25` Javadoc 标注「非安全用途」并建议优先 `sha256`；MD5 入口系 spec 显式要求，记录为已知隐患 |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ✅ | — | Round-1 `I001`（异常断言无详情）**已修复**：`HashAlgorithmTest.java:35-43,45-53` 改为 try/catch + `Assert.fail` + `assertEquals` 校验消息；Round-2 scan 不再命中 I001 |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | N/A(未启用自定义规则) |

---

## 7. 结论

- **合并建议**：✅ **复审通过，建议合并**（P0=0 / P1=0 / P2=0）
- **Round-1 问题修复核验**：
  1. ✅ `G16.2` `HashAlgorithmImpl.java:31,34`（原 :26,28）— catch 块已新增 `LOGGER.log`（JUL），WARNING/SEVERE 级别 + 参数化 `{0}` + cause
  2. ✅ `S9.3` `HashAlgorithm.java:13-14,24-25` — Javadoc 标注「非安全用途」+ 建议优先 `sha256`
  3. ✅ `A5.1` `HashAlgorithmImpl.java:19,42,48` + `HelloWorldImpl.java:11,17` — 全部补加 `@Override`
  4. ✅ `I001` `HashAlgorithmTest.java:35-43,45-53` — 改为 try/catch + `Assert.fail` + `assertEquals` 校验异常消息
- **Round-2 scan 误报复核**：`scan-all-rules.sh` 报告 4 处 `G16.2`（Impl:31,34 + Test:40,50），经 LLM 逐处复核均为**误报**：
  - Impl 两处：已用 `java.util.logging.Logger` 记录日志，脚本 CatchWithoutLogging 规则不识别 JUL `LOGGER.log(Level.X, ...)` 形式（疑似仅匹配 SLF4J `log.warn/error`）
  - Test 两处：测试中 catch 异常并 `assertEquals` 校验消息，属标准断言模式，无需日志
- **回归风险**：低。修改均为增量（@Override / LOGGER.log / Javadoc 段落 / try-catch 断言），核心哈希逻辑、方法签名、标准测试向量均未改动
- **一句话**：Round-1 全部问题已修复且无回归，scan 误报已 LLM 复核排除，实现满足 spec，建议合并。

---

## 7.1 问题片段（Round-2 scan 误报复核）

### 误报 — `G16.2` `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:31`

- **scan 报告**：CatchWithoutLogging
- **LLM 复核**：catch 块 L32 已有 `LOGGER.log(Level.WARNING, "unsupported algorithm: {0}", algorithm);`，使用 JUL 参数化日志，非吞异常。脚本不识别 JUL 形式，**误报**。
- 片段范围：`designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:31-33`

```java
L31|    } catch (NoSuchAlgorithmException e) {
L32|        LOGGER.log(Level.WARNING, "unsupported algorithm: {0}", algorithm);
L33|        throw new IllegalArgumentException("unsupported algorithm: " + algorithm, e);
```

### 误报 — `G16.2` `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:34`

- **scan 报告**：CatchWithoutLogging
- **LLM 复核**：catch 块 L36 已有 `LOGGER.log(Level.SEVERE, "UTF-8 not supported, environment is broken", e);`，**误报**。
- 片段范围：`designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:34-38`

```java
L34|    } catch (java.io.UnsupportedEncodingException e) {
L35|        // UTF-8 为 JDK 必备编码，理论上不会到达
L36|        LOGGER.log(Level.SEVERE, "UTF-8 not supported, environment is broken", e);
L37|        throw new IllegalStateException("UTF-8 not supported", e);
```

### 误报 — `G16.2` `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:40`

- **scan 报告**：CatchWithoutLogging
- **LLM 复核**：测试方法中 catch 异常并 `assertEquals` 校验消息，属 JUnit 断言模式，**误报**。
- 片段范围：`designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:40-42`

```java
L40|    } catch (IllegalArgumentException e) {
L41|        Assert.assertEquals("unsupported algorithm: NOT-A-REAL-ALGO", e.getMessage());
L42|    }
```

### 误报 — `G16.2` `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:50`

- **scan 报告**：CatchWithoutLogging
- **LLM 复核**：测试断言模式，**误报**。
- 片段范围：`designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:50-52`

```java
L50|    } catch (NullPointerException e) {
L51|        Assert.assertEquals("input must not be null", e.getMessage());
L52|    }
```

---

## 8. 修复任务列表（Round-1 → Round-2 核销）

### P1

- [x] **P1** `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:26,28` — 引入日志框架后在 catch 块记录 warn/error 日志（含 algorithm 名与 cause） ✅ 已用 JUL `LOGGER.log` 修复（L32,36）
- [x] **P1** `S9.3` `designmodel/src/main/java/cn/wy/hash/HashAlgorithm.java` — 在 `md5`/`hash` Javadoc 标注「非安全用途」或建议优先使用 `sha256` ✅ 已修复（L13-14,24-25）

### P2（可选）

- [x] **P2** `A5.1` `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:15,35,40` + `HelloWorldImpl.java:11,16` — 为所有重写方法补加 `@Override` ✅ 已修复（Impl:19,42,48 + HelloWorldImpl:11,17）
- [x] **P2** `I001` `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:35,40` — 改用 try/catch + `assert` 校验异常消息 ✅ 已修复（L35-43,45-53）

---

## 9. 复审记录（Round 2）

- **复审时间**：2026-07-31
- **复审范围**：commit `96db8fe` 修改的 4 个文件（`HashAlgorithm.java` / `HashAlgorithmImpl.java` / `HelloWorldImpl.java` / `HashAlgorithmTest.java`）
- **scan-all-rules.sh Round-2 输出**：4 findings (P0=4) — 全部 G16.2，经 LLM 复核均为误报（详见 §7.1）
- **Round-1 问题核销**：4/4 已修复（G16.2 / S9.3 / A5.1 / I001）
- **新引入问题**：无
- **回归风险**：低（增量修改，核心逻辑/签名/向量未动）
- **最终结论**：✅ 复审通过，建议合并

# Code Review Report

> **Change** `helloworld-hash-api` · **分支/Commit** `AI/task-DEV-...` / `4e1983b` · **日期** `2026-07-31` · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。✅ 已运行 `scan-all-rules.sh`，要点已并入 §5。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 6 |
| 变更行数 | `+217 / -0` |

| 类/接口 | 路径 | 角色（可选） |
|---------|------|--------------|
| `HashAlgorithm` | `designmodel/src/main/java/cn/wy/hash/HashAlgorithm.java` | 接口（REQ-2） |
| `HashAlgorithmImpl` | `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java` | 实现（REQ-2） |
| `HelloWorld` | `designmodel/src/main/java/cn/wy/helloworld/HelloWorld.java` | 接口（REQ-1） |
| `HelloWorldImpl` | `designmodel/src/main/java/cn/wy/helloworld/HelloWorldImpl.java` | 实现（REQ-1） |
| `HashAlgorithmTest` | `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java` | 测试（REQ-2） |
| `HelloWorldTest` | `designmodel/src/test/java/cn/wy/helloworld/HelloWorldTest.java` | 测试（REQ-1） |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 2 | 3 |

---

## 3. Step 2 — 功能（REQ）

### REQ-1: HelloWorld 接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| `sayHello()` 返回 "Hello, World!" | ✅ | `§4.1 接口一：HelloWorld` | `HelloWorldImpl.java:11-13` | 固定串一致 |
| `sayHello("Java")` 返回 "Hello, Java!" | ✅ | `§4.1` + `§3 Q2` | `HelloWorldImpl.java:16-21` | 拼接正确 |
| null/空串退化为 "World" | ✅ | `§3 Q2 决策` | `HelloWorldTest.java:26-29` | 全覆盖 |

### REQ-2: HashAlgorithm 接口

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 三方法签名 `hash/md5/sha256` | ✅ | `§4.2 接口二` | `HashAlgorithm.java:18/26/34` | 一致 |
| 基于 `MessageDigest` 输出小写 hex | ✅ | `§4.2 决策 B` | `HashAlgorithmImpl.java:23-25,50-60` | UTF-8+小写 |
| 标准向量校验 MD5/SHA-256/SHA-1 | ✅ | `§4.3 验证策略` | `HashAlgorithmTest.java:16-33` | 向量正确 |

---

## 4. Step 3 — 可读性检查

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ⚠️ | **P2 A5.1**：重写接口方法未加 `@Override` — `HashAlgorithmImpl.java:15,35,40`、`HelloWorldImpl.java:11,16` |

---

## 5. Step 4 — 可靠性检查

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「已扫无命中」） |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ⚠️ | P1 | **G16.2** — `HashAlgorithmImpl.java:26,28`；已扫无命中：G8/G11/G16.4；其余 N/A |
| 安全 | `security-checklist.md` S1–S10 | ⚠️ | P1 | **S9.3** — 暴露 MD5/SHA-1 算法入口；其余 N/A |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ⚠️ | P2 | 预扫：`scan-all-rules.sh`；命中 **I001** `HashAlgorithmTest.java:35,40`；其余 N/A |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「未启用自定义规则」） |
|----|------|------|------|------------------------------------------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | N/A(未启用自定义规则) |

---

## 7. 结论

- **合并建议**：修复后合并（P0=0，仅 2 条 P1 + 3 条 P2，均可后续迭代修复）
- **P1**：
  1. `G16.2` `HashAlgorithmImpl.java:26,28` — catch 未输出日志（已重抛非吞异常，风险可控，引入日志框架后补充）
  2. `S9.3` `HashAlgorithm.java:5` / `HashAlgorithmImpl.java:36,41` — 暴露 MD5/SHA-1；若用于安全场景不安全，建议优先 SHA-256（spec 已显式纳入 MD5，记录为隐患）
- **P2**：
  1. `A5.1` `HashAlgorithmImpl.java:15,35,40` + `HelloWorldImpl.java:11,16` — 重写未加 `@Override`
  2. `I001` `HashAlgorithmTest.java:35,40` — `@Test(expected=...)` 仅校验异常类型未断言详情
- **一句话**：实现满足 spec，TDD 向量正确，无阻塞性问题；P1 可观测性与弱算法隐患建议在引入日志框架/安全场景前补齐。

---

## 7.1 问题片段（必填）

### P1 — `G16.2` `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:26,28`

- **问题说明**：catch 块未记录日志，异常路径无可追溯上下文（algorithm 名/cause）。复核：异常已重抛并携带 cause，非吞异常，残留风险低；按清单等级 P1。
- 片段范围：`designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:22-31`

```java
L22|    try {
L23|        MessageDigest digest = MessageDigest.getInstance(algorithm);
L24|        byte[] bytes = digest.digest(input.getBytes("UTF-8"));
L25|        return toHexString(bytes);
L26|    } catch (NoSuchAlgorithmException e) {
L27|        throw new IllegalArgumentException("unsupported algorithm: " + algorithm, e);
L28|    } catch (java.io.UnsupportedEncodingException e) {
L29|        // UTF-8 为 JDK 必备编码，理论上不会到达
L30|        throw new IllegalStateException("UTF-8 not supported", e);
L31|    }
```

### P1 — `S9.3` `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:35-41`

- **问题说明**：便捷方法暴露 `MD5` 入口；MD5/SHA-1 已不再适合安全场景（口令/完整性）。spec 已显式纳入 MD5，记录为隐患，建议文档标注「非安全用途」。
- 片段范围：`designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:34-42`

```java
L34|    /** {@inheritDoc} */
L35|    public String md5(String input) {
L36|        return hash(input, "MD5");
L37|    }
L38|
L39|    /** {@inheritDoc} */
L40|    public String sha256(String input) {
L41|        return hash(input, "SHA-256");
L42|    }
```

### P2 — `A5.1` `designmodel/src/main/java/cn/wy/helloworld/HelloWorldImpl.java:10-21`

- **问题说明**：重写接口方法未加 `@Override`，违反可读性 A5.1（同问题见 `HashAlgorithmImpl.java:15,35,40`）。
- 片段范围：`designmodel/src/main/java/cn/wy/helloworld/HelloWorldImpl.java:10-22`

```java
L10|    /** {@inheritDoc} */
L11|    public String sayHello() {
L12|        return "Hello, World!";
L13|    }
L14|
L15|    /** {@inheritDoc} */
L16|    public String sayHello(String name) {
L17|        if (name == null || name.length() == 0) {
L18|            return sayHello();
L19|        }
L20|        return "Hello, " + name + "!";
L21|    }
```

### P2 — `I001` `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:35-43`

- **问题说明**：`@Test(expected=...)` 仅校验异常类型，未断言异常详情/消息，排障信息不足（Info）。
- 片段范围：`designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:35-43`

```java
L35|    @Test(expected = IllegalArgumentException.class)
L36|    public void hash_unknownAlgorithm_throws() {
L37|        hash.hash("abc", "NOT-A-REAL-ALGO");
L38|    }
L39|
L40|    @Test(expected = NullPointerException.class)
L41|    public void md5_nullInput_throws() {
L42|        hash.md5(null);
L43|    }
```

---

## 8. 修复任务列表

### P1

- [ ] **P1** `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:26,28` — 引入日志框架后在 catch 块记录 warn/error 日志（含 algorithm 名与 cause）
- [ ] **P1** `S9.3` `designmodel/src/main/java/cn/wy/hash/HashAlgorithm.java` — 在 `md5`/`hash` Javadoc 标注「非安全用途」或建议优先使用 `sha256`

### P2（可选）

- [ ] **P2** `A5.1` `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:15,35,40` + `HelloWorldImpl.java:11,16` — 为所有重写方法补加 `@Override`
- [ ] **P2** `I001` `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:35,40` — 改用 try/catch + `assert` 校验异常消息

# Code Review Checklist

> **Change** `helloworld-hash-api` · **分支/Commit** `AI/task-DEV-...` / `4e1983b` · **日期** `2026-07-31`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：✅ 已在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，输出贴入 Step 3/Step 4 备注；再用 LLM 完成 Step 2–5 脚本未覆盖项及复核。

### scan-all-rules.sh 预扫输出（摘录）

```text
[P0] G16.2 — CatchWithoutLogging: designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:26
[P0] G16.2 — CatchWithoutLogging: designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:28
[P2] I001 — AssertExceptionDetailInfoPreferred: designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:35
[P2] I001 — AssertExceptionDetailInfoPreferred: designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:40
=== Summary: 4 findings (P0=2, P1=0, P2=2) | 52/222 rules scanned ===
```

> **LLM 复核**：脚本将 G16.2 标 P0，但 `reliability-checklist.md` 行内 G16.2 等级为 **P1**（最终以清单行内为准）。且两处 catch 均为「重抛 + cause」非空 catch，未吞异常，残留风险低；按 P1 记录并附复核说明。

---

## Step 1 — 执行队列（产物 A）

> 由 `git show --name-only 4e1983b` 展开；均为 `.java`，无跳过。守卫：含 `.java`，继续。

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|-----|--------|
| 1 | `designmodel/src/main/java/cn/wy/hash/HashAlgorithm.java` | REQ-1/REQ-2 接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | ✅ 已审 |
| 2 | `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java` | REQ-1/REQ-2 实现 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | ⚠️ 已审有问题 |
| 3 | `designmodel/src/main/java/cn/wy/helloworld/HelloWorld.java` | REQ-1 接口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | `designmodel/src/main/java/cn/wy/helloworld/HelloWorldImpl.java` | REQ-1 实现 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| 5 | `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java` | REQ-2 验证 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题(I001) |
| 6 | `designmodel/src/test/java/cn/wy/helloworld/HelloWorldTest.java` | REQ-1 验证 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |

**N/A 原因汇总**：本变更为纯 JDK 工具类（哈希/问候），无并发/事务/SQL/MQ/缓存/调度/网络调用/资金/多租户/灰度场景，故 G1–G7、G9–G10、G12–G15、G17 及 S1–S8、S10 与本变更无关。

---

## Step 2 — 功能（产物 B）

> 仅从 spec 提 REQ，勿臆造。spec：`docs/superpowers/specs/2026-07-31-helloworld-hash-api-design.md`。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | HelloWorld 接口：`sayHello()` 返回 "Hello, World!"；`sayHello(String name)` 返回 "Hello, <name>!"，null/空退化为 "World" | `§4.1 接口一：HelloWorld` + `§3 Q2 决策` | `HelloWorld.java`、`HelloWorldImpl.java`、`HelloWorldTest.java` | ✅ | `HelloWorldImpl.java:11-13`（固定串）、`:16-21`（带名/空值退化）；`HelloWorldTest.java:16-29` 全覆盖 |
| REQ-2 | HashAlgorithm 接口：`hash(input, algorithm)` + `md5(input)` + `sha256(input)`；实现基于 `MessageDigest`，输出小写十六进制 | `§4.2 接口二：HashAlgorithm` + `§3 Q3 决策 B` | `HashAlgorithm.java`、`HashAlgorithmImpl.java`、`HashAlgorithmTest.java` | ✅ | `HashAlgorithm.java:18/26/34` 三方法签名一致；`HashAlgorithmImpl.java:23-25` 用 `MessageDigest`+`getBytes("UTF-8")`；`toHexString:50-60` 产出小写 hex；`HashAlgorithmTest.java:16-33` 用标准向量校验 MD5/SHA-256/SHA-1 |

---

## Step 3 — 可读性检查（产物 C）

对照 `references/readability-checklist.md` A1–A7 逐节核销（跨 6 文件合并）：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 文件名=类名；UTF-8；仅 ASCII 空格，无 Tab |
| A2 | 源文件结构/import 顺序 | ✅ | package→import→class；无 `import *`；`HashAlgorithmImpl.java:3-4` import 字典序正确 |
| A3 | 代码样式 | ✅ | K&R 大括号；4 空格缩进；行宽均 ≤120 |
| A4 | 命名规范 | ✅ | 包名全小写；类 UpperCamelCase；方法 lowerCamelCase；测试类=被测类+Test |
| A5 | 编码实践 | ⚠️ | **A5.1**：重写接口方法未加 `@Override` — `HashAlgorithmImpl.java:15,35,40`、`HelloWorldImpl.java:11,16`（P2，风格） |
| A6 | 特定元素样式 | ✅ | `byte[]` 类型形式正确；无 switch/long 字面量问题 |
| A7 | Javadoc 规范 | ✅ | public 类/成员均有 Javadoc；块标记 `@param`→`@return` 顺序正确 |

---

## Step 4 — 可靠性检查（产物 D）

> 逐条核销：G/S 每个 ID 独占一行；Bug 模式 B/M/I 独占一行。无关变更标 `N/A` 并写原因。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫：脚本命中 `I001`（2 处）；其余 B/M 与本变更无关。

| ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
|----|------|--------------------------------------------------|
| B001–B081 | N/A | 本变更无 LocalDateTime.parse/UUID.fromString/数组 equals/Arrays.asList/toString 等命中；均为 N/A(与本变更无关：纯 JDK 哈希/字符串拼接) |
| M001–M027 | N/A | 本变更无资源未关闭/日志门面误用/空指针解引用等命中；均为 N/A(与本变更无关) |
| I001 | ⚠️ | `HashAlgorithmTest.java:35,40` — `@Test(expected=...)` 仅校验异常类型未断言详情（P2，Info） |
| I002–I010 | N/A | 与本变更无关 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 | N/A | 无并发/锁 |
| G2.1–G2.3 | N/A | 无幂等写 |
| G3.1–G3.2 | N/A | 无事务 |
| G4.1–G4.4 | N/A | 无 SQL |
| G5.1 | N/A | 无 MQ |
| G6.1–G6.2 | N/A | 无缓存 |
| G7.1–G7.2 | N/A | 无调度 |
| G8.1 | ✅ | catch 重抛非吞异常（`HashAlgorithmImpl.java:26,28`） |
| G8.2 | N/A | 无核心链路强依赖 |
| G8.3 | ✅ | MessageDigest 无需释放的 I/O 资源 |
| G8.4–G8.7 | N/A | 无线程池/ThreadLocal |
| G9.1–G9.3 | N/A | 无网络/RPC 调用 |
| G10.1–G10.3 | N/A | 无接口契约版本变更 |
| G11.1 | ✅ | 新逻辑均有单测且带断言 |
| G11.2 | ✅ | 覆盖空值/空串/未知算法边界 |
| G11.3 | ✅ | `HashAlgorithmImpl.java:16-21` input/algorithm 空值校验 |
| G11.4 | N/A | 无数值运算/金额 |
| G12.1–G12.2 | N/A | 无资金场景 |
| G13.1 | N/A | 无日志级别误用 |
| G14.1–G14.4 | N/A | 无金额/多租户/时区 |
| G15.1–G15.3 | N/A | 无 DB 灰度 |
| G16.1 | N/A | 非核心链路埋点场景 |
| G16.2 | ⚠️ | `HashAlgorithmImpl.java:26,28` — catch 未输出日志；**复核**：异常已重抛并携带 algorithm 名 + cause，非吞异常，残留风险低；按清单等级 **P1**；建议引入日志框架后补充 |
| G16.3 | N/A | 无日志级别判定 |
| G16.4 | ✅ | 非空 catch，无 printStackTrace，未静默继续 |
| G17.1–G17.3 | N/A | 无功能开关/降级预案场景 |
| G18.1–G18.3 | N/A | 安全补强见 §4.3 S9 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S1.3 | N/A | 无 SQL |
| S2.1–S2.3 | N/A | 无 HTML/模板输出 |
| S3.1–S3.3 | N/A | 无外部 URL 请求 |
| S4.1–S4.2 | N/A | 无命令执行 |
| S5.1–S5.2 | N/A | 无 XML 解析 |
| S6.1–S6.3 | N/A | 无反序列化 |
| S7.1–S7.3 | N/A | 无文件上传/下载 |
| S8.1–S8.4 | N/A | 无鉴权/访问控制场景（纯工具类） |
| S9.1 | ✅ | 无密钥硬编码 |
| S9.2 | ✅ | 无敏感信息日志 |
| S9.3 | ⚠️ | `HashAlgorithm.java:5`/`HashAlgorithmImpl.java:36` — 暴露 MD5/SHA-1；若用于安全场景（口令/完整性）不安全，建议优先 SHA-256（P1，spec 已显式纳入 MD5，记录为隐患） |
| S9.4 | N/A | 无随机数 |
| S10.1–S10.3 | N/A | 无 CSRF/CORS/跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

> `customized-checklist.md` 仅含示例项，未启用项目私有规则。

| ID | 状态 | 备注 |
|----|------|------|
| U1.1–U2.3 | N/A | N/A(未启用自定义规则) |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（`N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`

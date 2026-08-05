# Code Review Checklist

> **Change** `helloworld-hash-api` · **分支/Commit** `AI/task-DEV-...` / `96db8fe`（复审 Round 2，原 `4e1983b`） · **日期** `2026-07-31`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。完成标准：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：✅ 已在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`（Round 2），输出贴入 Step 3/Step 4 备注；再用 LLM 完成 Step 2–5 脚本未覆盖项及复核。

### scan-all-rules.sh 预扫输出（Round 2 摘录）

```text
[P0] G16.2 — CatchWithoutLogging: designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:31
[P0] G16.2 — CatchWithoutLogging: designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java:34
[P0] G16.2 — CatchWithoutLogging: designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:40
[P0] G16.2 — CatchWithoutLogging: designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java:50
=== Summary: 4 findings (P0=4, P1=0, P2=0) | 52/222 rules scanned ===
```

> **LLM 复核**：Round-2 scan 报告的 4 处 G16.2 **全部为误报**。
> - Impl 两处（:31,34）：catch 块已用 `java.util.logging.Logger` 记录日志（`LOGGER.log(Level.WARNING/SEVERE, ...)`），脚本 CatchWithoutLogging 规则不识别 JUL 形式（疑似仅匹配 SLF4J `log.warn/error`），误报。
> - Test 两处（:40,50）：测试方法中 catch 异常并 `assertEquals` 校验消息，属 JUnit 断言模式，无需日志，误报。
> 详见 report §7.1。

---

## Step 1 — 执行队列（产物 A）

> 由 `git show --name-only 96db8fe` 展开；均为 `.java`，无跳过。守卫：含 `.java`，继续。复审仅覆盖 Round-2 修改的 4 个文件，未修改的 `HelloWorld.java`/`HelloWorldTest.java` 沿用 Round-1 结论（✅）。

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|----|-----|--------|
| 1 | `designmodel/src/main/java/cn/wy/hash/HashAlgorithm.java` | REQ-2 接口（Javadoc 补充） | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | ✅ 已审 |
| 2 | `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java` | REQ-2 实现（补 @Override + LOGGER） | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | ✅ 已审 |
| 3 | `designmodel/src/main/java/cn/wy/helloworld/HelloWorldImpl.java` | REQ-1 实现（补 @Override） | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java` | REQ-2 验证（异常断言改造） | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |

**N/A 原因汇总**：本变更为纯 JDK 工具类（哈希/问候），无并发/事务/SQL/MQ/缓存/调度/网络调用/资金/多租户/灰度场景，故 G1–G7、G9–G10、G12–G15、G17 及 S1–S8、S10 与本变更无关。

---

## Step 2 — 功能（产物 B）

> 仅从 spec 提 REQ，勿臆造。spec：`docs/superpowers/specs/2026-07-31-helloworld-hash-api-design.md`。行号按 Round-2 修改后版本。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | HelloWorld 接口：`sayHello()` 返回 "Hello, World!"；`sayHello(String name)` 返回 "Hello, <name>!"，null/空退化为 "World" | `§4.1 接口一：HelloWorld` + `§3 Q2 决策` | `HelloWorldImpl.java` | ✅ | `HelloWorldImpl.java:12-13`（固定串）、`:18-22`（带名/空值退化）；`HelloWorldTest.java` 全覆盖（未改） |
| REQ-2 | HashAlgorithm 接口：`hash(input, algorithm)` + `md5(input)` + `sha256(input)`；实现基于 `MessageDigest`，输出小写十六进制 | `§4.2 接口二：HashAlgorithm` + `§3 Q3 决策 B` | `HashAlgorithm.java`、`HashAlgorithmImpl.java`、`HashAlgorithmTest.java` | ✅ | `HashAlgorithm.java:20/30/38` 三方法签名一致；`HashAlgorithmImpl.java:28-30` 用 `MessageDigest`+`getBytes("UTF-8")`；`toHexString:59-69` 产出小写 hex；`HashAlgorithmTest.java:16-33` 用标准向量校验 MD5/SHA-256/SHA-1 |
 
 ---
 
 ## Step 3 — 可读性检查（产物 C）
 
 对照 `references/readability-checklist.md` A1–A7 逐节核销（复审 4 文件合并）：
 
 | ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
 |----|--------|------|----------------------------|
 | A1 | 源文件格式 | ✅ | 文件名=类名；UTF-8；仅 ASCII 空格，无 Tab |
 | A2 | 源文件结构/import 顺序 | ✅ | package→import→class；`HashAlgorithmImpl.java:3-6` import 字典序正确（新增 `java.util.logging.Level`/`Logger` 位于 `java.security.*` 后，符合字典序） |
 | A3 | 代码样式 | ✅ | K&R 大括号；4 空格缩进；行宽均 ≤120 |
 | A4 | 命名规范 | ✅ | 包名全小写；类 UpperCamelCase；方法 lowerCamelCase；测试类=被测类+Test |
 | A5 | 编码实践 | ✅ | **Round-1 A5.1 已修复**：所有重写方法均补加 `@Override` — `HashAlgorithmImpl.java:19,42,48`、`HelloWorldImpl.java:11,17` |
 | A6 | 特定元素样式 | ✅ | `byte[]` 类型形式正确；无 switch/long 字面量问题 |
 | A7 | Javadoc 规范 | ✅ | public 类/成员均有 Javadoc；`HashAlgorithm.java:13-14,24-25` 新增「非安全用途」标注；块标记 `@param`→`@return` 顺序正确 |
 
 ---
 
 ## Step 4 — 可靠性检查（产物 D）
 
 > 逐条核销：G/S 每个 ID 独占一行；Bug 模式 B/M/I 独占一行。无关变更标 `N/A` 并写原因。
 
 ### 4.1 Bug 模式（`bug-pattern-checklist.md`）
 
 > 预扫（Round 2）：脚本无 B/M/I 命中；I001 已修复不再命中。
 
 | ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
 |----|------|--------------------------------------------------|
 | B001–B081 | N/A | 本变更无 LocalDateTime.parse/UUID.fromString/数组 equals/Arrays.asList/toString 等命中；均为 N/A(与本变更无关：纯 JDK 哈希/字符串拼接) |
 | M001–M027 | N/A | 本变更无资源未关闭/日志门面误用/空指针解引用等命中；均为 N/A(与本变更无关) |
 | I001 | ✅ | **Round-1 已修复**：`HashAlgorithmTest.java:35-43,45-53` 改为 try/catch + `Assert.fail` + `assertEquals` 校验异常消息，不再用 `@Test(expected=...)` |
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
 | G8.1 | ✅ | catch 重抛非吞异常（`HashAlgorithmImpl.java:33,37`） |
 | G8.2 | N/A | 无核心链路强依赖 |
 | G8.3 | ✅ | MessageDigest 无需释放的 I/O 资源 |
 | G8.4–G8.7 | N/A | 无线程池/ThreadLocal |
 | G9.1–G9.3 | N/A | 无网络/RPC 调用 |
 | G10.1–G10.3 | N/A | 无接口契约版本变更 |
 | G11.1 | ✅ | 新逻辑均有单测且带断言 |
 | G11.2 | ✅ | 覆盖空值/空串/未知算法边界 |
 | G11.3 | ✅ | `HashAlgorithmImpl.java:21-26` input/algorithm 空值校验 |
 | G11.4 | N/A | 无数值运算/金额 |
 | G12.1–G12.2 | N/A | 无资金场景 |
 | G13.1 | ✅ | 日志级别正确：WARNING（不支持算法）/SEVERE（UTF-8 异常） |
 | G14.1–G14.4 | N/A | 无金额/多租户/时区 |
 | G15.1–G15.3 | N/A | 无 DB 灰度 |
 | G16.1 | N/A | 非核心链路埋点场景 |
 | G16.2 | ✅ | **Round-1 已修复**：`HashAlgorithmImpl.java:32,36` 新增 `LOGGER.log(Level.WARNING/SEVERE, ...)`；Round-2 scan 仍报为**误报**（脚本不识别 JUL `LOGGER.log` 形式，详见顶部 LLM 复核） |
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
 | S9.3 | ✅ | **Round-1 已缓解**：`HashAlgorithm.java:13-14,24-25` Javadoc 标注「非安全用途」并建议优先 `sha256`；MD5 入口系 spec 显式要求，记录为已知隐患 |
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
 
 ---
 
 ## 复审记录（Round 2）
 
 - **复审时间**：2026-07-31
 - **复审范围**：commit `96db8fe` 修改的 4 个文件
 - **scan-all-rules.sh Round-2 输出**：4 findings (P0=4) — 全部 G16.2，经 LLM 复核均为误报（脚本不识别 JUL `LOGGER.log` + 不区分测试断言性 catch）
 - **Round-1 问题核销**：4/4 已修复（G16.2 ✅ / S9.3 ✅ / A5.1 ✅ / I001 ✅）
 - **新引入问题**：无
 - **回归风险**：低（增量修改：@Override / LOGGER.log / Javadoc 段落 / try-catch 断言；核心哈希逻辑、方法签名、标准测试向量均未改动）
 - **最终结论**：✅ 复审通过，建议合并

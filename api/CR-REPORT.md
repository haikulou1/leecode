# Code Review Report

> **Change** `Java接口Demo loop-1 round2` · **分支/Commit** `AI/task-DEV-ddccb2af` / `fe98acf` · **日期** `2026-07-31` · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。**已**运行 `scan-all-rules.sh` 并将要点并入 §4，**再**写 LLM 结论。问题含 `path:line` 或清单 ID。本报告 §7.1 附 `.java` 问题片段。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | `8` |
| 变更行数 | `+382 / -3`（7 文件，含 1 个 .md） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `Main` | `api/src/Main.java` | HttpServer 启动入口，注册路由，关闭钩子 |
| `HelloWorldHandler` | `api/src/handler/HelloWorldHandler.java` | GET /helloworld 处理器 |
| `HashHandler` | `api/src/handler/HashHandler.java` | GET /hash 处理器 |
| `SortHandler` | `api/src/handler/SortHandler.java` | GET /sort 处理器 |
| `HashUtil` | `api/src/util/HashUtil.java` | 哈希算法工具（MessageDigest 封装） |
| `SortUtil` | `api/src/util/SortUtil.java` | 排序算法工具（冒泡/快排/选择） |
| `HashUtilTest` | `api/test/HashUtilTest.java` | HashUtil 单元测试 |
| `SortUtilTest` | `api/test/SortUtilTest.java` | SortUtil 单元测试 |

> 设计文档：`api/DESIGN.md`；接口说明：`api/README.md`。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 0 | 3 |

> `scan-all-rules.sh` 预扫报 26 条 G16.2（CatchWithoutLogging），经 LLM 逐文件复核均为**误报**（详见 §4.1 与 §7.1）。

---

## 3. Step 2 — 功能（REQ）

> 需求来源：`api/DESIGN.md` §5.2 接口定义表 + `requirement_section`。

### REQ-1: `HelloWorld 接口`

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /helloworld → "Hello, World!" | ✅ | DESIGN.md §5.2 行101 | `HelloWorldHandler.java:19` | 返回 `Hello, World!`，非 GET 返回 405 |
| 非 GET 请求 → 405 | ✅ | DESIGN.md §5.3 行108 | `HelloWorldHandler.java:15-17` | Method Not Allowed |

### REQ-2: `哈希算法接口`

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /hash?algorithm=sha256&input=abc → 十六进制摘要 | ✅ | DESIGN.md §5.2 行102 | `HashHandler.java:49-51` + `HashUtil.java:20-31` | 摘要值与已知值一致（测试验证） |
| 支持 md5/sha1/sha256/sha512 | ✅ | DESIGN.md §5.4 行111 | `HashUtil.java:58-69` normalize() | 算法名归一化+白名单校验 |
| 算法不支持 → 400 | ✅ | DESIGN.md §5.3 行106 | `HashHandler.java:44-47` | isSupported 预校验 |
| 参数缺失 → 400 | ✅ | DESIGN.md §5.3 行107 | `HashHandler.java:32-38` | algorithm/input 空值校验 |
| input 长度上限防 DoS | ✅ | 本轮新增（G11.3 防御） | `HashHandler.java:40-42` | MAX_INPUT_LENGTH=1024 |

### REQ-3: `排序算法接口（候选A）`

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| GET /sort?algorithm=quicksort&input=3,1,2 → 1,2,3 | ✅ | DESIGN.md §5.2 行103 | `SortHandler.java:69-70` + `SortUtil.java:16-33` | 三种算法结果一致（测试验证） |
| 支持 bubble/quicksort/selection | ✅ | DESIGN.md §5.4 | `SortUtil.java:19-31` | switch 分发 |
| 元素数量上限防 OOM | ✅ | 本轮新增（G11.3 防御） | `SortHandler.java:79-82` | MAX_ELEMENT_COUNT=1000 |
| 输入格式错误 → 400 | ✅ | DESIGN.md §5.3 | `SortHandler.java:57-61` | NumberFormatException 捕获 |

---

## 4. Step 3 — 可读性检查

> 对照 `references/readability-checklist.md` A1–A7 逐节核销。

| ID | 检查项 | 结果 | 备注 |
|----|--------|------|------|
| A1 | 源文件格式 | ✅ | 文件名=类名.java，UTF-8，4 空格缩进无 Tab |
| A2 | 源文件结构/import 顺序 | ⚠️ | 无 `package` 声明（default package）；import 无通配符，组内字典序 ✅。无 package 系设计决策（DESIGN.md §5.1 原生 Java 模块），Demo 可接受，生产建议补 |
| A3 | 代码样式 | ✅ | K&R 大括号，行宽 ≤120，类成员间空行 |
| A4 | 命名规范 | ✅ | 类 UpperCamelCase，方法 lowerCamelCase，常量 UPPER_SNAKE_CASE（MAX_INPUT_LENGTH 等） |
| A5 | 编码实践 | ✅ | `@Override` 齐全，catch 非空，静态方法类名调用 |
| A6 | 特定元素样式 | ✅ | `String[] args` 类型式方括号；switch 有 default；long 无小写 l |
| A7 | Javadoc 规范 | ✅ | public 类/方法均有 Javadoc；`@Override` 方法省略合规；private 方法按需补充 |

---

## 5. Step 4 — 可靠性检查

### 5.1 预扫结果（`scan-all-rules.sh`）

```
=== Step 4 Rule Scan (B/M/I + A/S/G) ===
Targets: src/ test/
Engine:  ripgrep

[P0] G16.2 — CatchWithoutLogging: src/Main.java:22
[P0] G16.2 — CatchWithoutLogging: src/handler/HashHandler.java:52
[P0] G16.2 — CatchWithoutLogging: src/handler/SortHandler.java:57
[P0] G16.2 — CatchWithoutLogging: src/handler/SortHandler.java:62
[P0] G16.2 — CatchWithoutLogging: src/util/HashUtil.java:26
[P0] G16.2 — CatchWithoutLogging: test/HashUtilTest.java:33,45,56,66,80,90,92,102
[P0] G16.2 — CatchWithoutLogging: test/SortUtilTest.java:36,45,54,67,76,85,94,103,112,121,134,143,145

=== Summary: 26 findings (P0=26, P1=0, P2=0) | 52/222 rules scanned ===
```

### 5.2 LLM 复核：G16.2 误报分析

| 命中位置 | 复核结论 | 理由 |
|----------|----------|------|
| `src/Main.java:22` | **误报** | catch 块含 `System.err.println("[WARN] [Main] ...")`，脚本行级扫描未跨行识别 println |
| `src/handler/HashHandler.java:52` | **误报** | catch 块含 `System.err.println("[WARN] [HashHandler] ...")` |
| `src/handler/SortHandler.java:57` | **误报** | catch 块含 `System.err.println("[WARN] [SortHandler] ...")` |
| `src/handler/SortHandler.java:62` | **误报** | catch 块含 `System.err.println("[WARN] [SortHandler] ...")` |
| `src/util/HashUtil.java:26` | **误报** | catch 块含 `System.err.println("[ERROR] [HashUtil] ...")` |
| `test/*.java`（21 处） | **误报** | catch(AssertionError e) 块调用 `fail(label, e)`，该方法递增 failed 计数器并输出 `[FAIL] label: msg` 到 System.err，系标准测试断言失败记录模式 |

> **结论**：26 条 G16.2 全部为脚本误报（行级 ripgrep 无法识别 catch 下一行的 println 调用）。源码 catch 块均已补充日志（本轮 diff 正是 G16.2 修复），测试 catch 块使用 fail() 记录失败。**实际 P0 = 0**。

### 5.3 可靠性 G 清单逐条核销

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| G1 并发控制 | G1.1–G1.4 | N/A | — | 无 DB 读写、无并发共享状态 |
| G2 幂等拦截 | G2.1–G2.3 | N/A | — | 只读 GET 接口，无写操作/MQ |
| G3 事务控制 | G3.1–G3.2 | N/A | — | 无事务 |
| G4 SQL与索引 | G4.1–G4.3 | N/A | — | 无 SQL |
| G5 消息MQ | G5.1 | N/A | — | 无 MQ |
| G6 缓存 | G6.1–G6.2 | N/A | — | 无缓存 |
| G7 调度任务 | G7.1–G7.2 | N/A | — | 无调度 |
| G8 防御编程 | G8.1–G8.6 | ✅ | — | G8.1 catch 全部记录日志✅；G8.3 OutputStream try-with-resources✅；G8.4 shutdown hook✅；G8.5 无 ThreadLocal N/A；G8.6 `setExecutor(null)` 用 HttpServer 默认执行器（非 Executors 无界队列）✅ |
| G9 网络调用 | G9.1–G9.3 | N/A | — | HttpServer 为入站服务端，无出站调用 |
| G10 接口契约 | G10.1–G10.2 | ✅ | — | 错误响应格式一致，null 仅表示无数据 |
| G11 开发自测 | G11.1–G11.4 | ⚠️ | P2 | G11.1 有单测✅；G11.2 边界覆盖（空/单元素/null/逆序/重复）✅；G11.3 `HashUtil.digest()` 未 null-check `input` 参数（调用方 HashHandler 已校验，风险已缓解，见 §7.1）；G11.4 无金额运算 N/A |
| G12 资损防控 | G12.1–G12.2 | N/A | — | 无资金场景 |
| G13 监控核对 | G13.1 | ✅ | — | 错误打 warn/error，成功不打 error |
| G14–G17 | — | N/A | — | 无灰度/配置/限流/应急场景（Demo 项目） |

### 5.4 安全 S 清单逐条核销

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| S1 SQL注入 | S1.1 | N/A | — | 无 SQL |
| S2 XSS | S2.1 | ✅ | — | Content-Type: text/plain，无 HTML 渲染 |
| S3 认证授权 | S3.1–S3.2 | N/A | — | Demo API，无认证（设计决策） |
| S4 输入校验 | S4.1–S4.2 | ✅ | — | 算法白名单(isSupported)✅；input 长度限制✅；元素数量限制✅；Integer.parseInt 异常捕获✅ |
| S5 密钥泄露 | S5.1 | N/A | — | 无密钥 |
| S6–S10 | — | N/A | — | 无文件上传/反序列化/SSRF/CSRF/CORS（纯 GET Demo） |

### 5.5 Bug 模式 B/M/I 核销（节选相关项）

> 共 120 条（B81+M27+I10）。以下列出与本项目可能相关的条目，其余标 N/A。

| ID | 规则名 | 状态 | 备注 |
|----|--------|------|------|
| B002 | ArrayEquals | ✅ | 测试用自定义 `assertArrayEquals` 逐元素比较，未用 `array.equals()` |
| B004 | ArrayToString | ✅ | 无 `array.toString()` 调用 |
| B005 | ArraysAsListPrimitiveArray | ✅ | 无 `Arrays.asList(int[])` |
| B006 | AssertEqualsArgumentOrder | ✅ | 自定义 `assertEquals(expected, actual, label)` 参数顺序正确 |
| B007 | AssertionFailureIgnored | ✅ | 测试 catch(AssertionError) 后调用 fail()，未吞断言 |
| B008 | AvoidUsingExecutors | ✅ | 无 `Executors.newXxxThreadPool` |
| B011 | BoxedPrimitiveEquality | ✅ | 无包装类型 `==` 比较 |
| B024 | DeadThread | ✅ | `new Thread(…)` 传入 `addShutdownHook()`，由 JVM 启动，非遗漏 `start()` |
| B001–B120 其余 | — | N/A | 无 BigDecimal/Calendar/Date格式化/JDBC/Spring 等场景 |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | 未启用自定义规则 |

---

## 7. 结论

- **合并建议**：**通过**
- **P0**：无（`scan-all-rules.sh` 报 26 条 G16.2 均为误报，经 LLM 逐文件复核确认）
- **P1/P2**：
  1. **P2** `A2.1` — 无 `package` 声明（default package），Demo 可接受，生产建议补包名
  2. **P2** `G11.3` `api/src/util/HashUtil.java:24` — `digest()` 未对 `input` 做 null 防御校验（调用方已校验，风险缓解）
  3. **P2** DRY — `parseQuery()` / `write()` 在 HashHandler、SortHandler、HelloWorldHandler 间重复，建议提取公共基类或工具方法
- **一句话**：本轮变更质量良好——补充了 DoS 防御（长度/数量限制）、异常日志与 cause 链、shutdown hook 及完整单元测试；预扫 26 条 G16.2 全系误报，无 P0/P1 阻塞项，3 条 P2 改进建议不阻塞合并。

---

## 7.1 问题片段（必填）

> 对 §3–§7 中每个 `⚠️` 问题提供对应 `.java` 代码片段。

### P2 — `G16.2 误报` 说明（`src/Main.java:22`）

- **P2** `G16.2(误报)` `api/src/Main.java:22` — catch 块**已含** `System.err.println` 日志，脚本行级扫描未跨行识别，判定为误报。
  片段范围：`api/src/Main.java:20-26`

```java
L20|        try {
L21|            port = Integer.parseInt(args[0]);
L22|        } catch (NumberFormatException e) {
L23|            System.err.println("[WARN] [Main] Invalid port arg: args[0]=" + args[0]
L24|                    + ", exception=" + e.getClass().getSimpleName()
L25|                    + ", fallback=8080");
L26|        }
```

### P2 — `G16.2 误报` 说明（`src/util/HashUtil.java:26`）

- **P2** `G16.2(误报)` `api/src/util/HashUtil.java:26` — catch 块**已含** `System.err.println` 日志并保留 cause，系本轮 G16.2 修复，误报。
  片段范围：`api/src/util/HashUtil.java:22-31`

```java
L22|        try {
L23|            MessageDigest md = MessageDigest.getInstance(alg);
L24|            byte[] raw = md.digest(input.getBytes(StandardCharsets.UTF_8));
L25|            return toHex(raw);
L26|        } catch (NoSuchAlgorithmException e) {
L27|            System.err.println("[ERROR] [HashUtil] digest failed: algorithm=" + algorithm
L28|                    + ", inputLen=" + input.length()
L29|                    + ", error=" + e.getMessage());
L30|            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm, e);
L31|        }
```

### P2 — `G16.2 误报` 说明（`test/HashUtilTest.java:33`，代表全部测试命中）

- **P2** `G16.2(误报)` `api/test/HashUtilTest.java:33` — catch(AssertionError) 调用 `fail()` 记录失败，系标准测试模式，误报。同模式命中见 SortUtilTest 全部 15 处。
  片段范围：`api/test/HashUtilTest.java:29-35`

```java
L29|    static void testDigestMd5() {
L30|        try {
L31|            String result = HashUtil.digest("md5", "abc");
L32|            assertEquals("900150983cd24fb0d6963f7d28e17f72", result, "MD5(abc)");
L33|        } catch (AssertionError e) {
L34|            fail("testDigestMd5", e);
L35|        }
L36|    }
```

### P2 — `G11.3` `HashUtil.digest` 未 null-check input

- **P2** `G11.3` `api/src/util/HashUtil.java:24` — `input` 参数未做 null 防御校验，直接调用 `input.getBytes()`。调用方 `HashHandler.java:36` 已校验 `input == null`，风险已缓解。
  片段范围：`api/src/util/HashUtil.java:20-25`

```java
L20|    public static String digest(String algorithm, String input) {
L21|        String alg = normalize(algorithm);
L22|        try {
L23|            MessageDigest md = MessageDigest.getInstance(alg);
L24|            byte[] raw = md.digest(input.getBytes(StandardCharsets.UTF_8));
L25|            return toHex(raw);
```

### P2 — DRY 重复代码（`parseQuery` / `write`）

- **P2** DRY `api/src/handler/HashHandler.java:60-89` — `parseQuery()` 与 `write()` 在 `HashHandler`、`SortHandler`、`HelloWorldHandler` 三处重复。建议提取公共基类 `BaseHandler` 或 `HttpUtil` 工具类。
  片段范围：`api/src/handler/HashHandler.java:82-89`（write 方法，三处相同）

```java
L82|    private static void write(HttpExchange exchange, int status, String body) throws IOException {
L83|        byte[] data = body.getBytes(StandardCharsets.UTF_8);
L84|        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
L85|        exchange.sendResponseHeaders(status, data.length);
L86|        try (OutputStream os = exchange.getResponseBody()) {
L87|            os.write(data);
L88|        }
L89|    }
```

---

## 8. 修复任务列表

### P0

- 无待修复项。

### P1

- 无待修复项。

### P2（可选）

- [ ] **P2** `api/src/util/HashUtil.java:24` — 在 `digest()` 入口对 `input` 加 null 校验（如 `Objects.requireNonNull(input, "input")`），提供防御性编程纵深
- [ ] **P2** `A2.1` — 为 `api/` 模块补充 `package` 声明（如 `package demo.api;`），提升生产可维护性
- [ ] **P2** `api/src/handler/` — 提取 `parseQuery()` / `write()` 到公共基类 `BaseHandler` 或 `HttpUtil`，消除三处重复

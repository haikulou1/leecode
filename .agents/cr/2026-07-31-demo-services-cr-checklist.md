# Code Review Checklist

> **Change** `demo-services (HelloWorld/Hash/Sort/Export)` · **分支/Commit** `AI/task-DEV-ddccb2af-...` / `013d4b2` · **日期** `2026-07-31`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。**完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：已先在 leecode 仓库根目录对 9 个变更 Java 路径运行 `scan-all-rules.sh`，输出贴入 Step 3/Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及误报复核。

**scan-all-rules.sh 预扫输出（摘）**：
```
=== Step 4 Rule Scan (B/M/I + A/S/G) ===
Targets: leecode/src/demo/DemoMain.java ... SortAlgorithmServiceImpl.java
Engine:  ripgrep

[P0] G16.2 — CatchWithoutLogging: leecode/src/demo/ExportServiceImpl.java:30
[P0] G16.2 — CatchWithoutLogging: leecode/src/demo/HashAlgorithmServiceImpl.java:26
=== Summary: 2 findings (P0=2, P1=0, P2=0) | 52/222 rules scanned ===
```
**误报复核**：上述 2 条 G16.2 均为**误报**——两个 catch 块内均含 `throw new ...(...)` 重新抛出（ExportServiceImpl.java:31、HashAlgorithmServiceImpl.java:27），满足"捕获后向上抛出"，非吞异常；脚本因正则只扫 catch 行本身而未识别下一行的 throw。结论降级为合规，但建议补日志（见 report P2）。

---

## Step 1 — 执行队列（产物 A）

> 9 个 `.java` 文件（Java 守卫通过）。haikulou1.github.io 的 `algorithm-demo.html` 为非 Java，按技能规则跳过（不纳入本表）。

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | `leecode/src/demo/HelloWorldService.java` | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | `leecode/src/demo/HelloWorldServiceImpl.java` | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 3 | `leecode/src/demo/HashAlgorithmService.java` | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | `leecode/src/demo/HashAlgorithmServiceImpl.java` | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | ⚠️ 已审有问题 |
| 5 | `leecode/src/demo/SortAlgorithmService.java` | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 6 | `leecode/src/demo/SortAlgorithmServiceImpl.java` | REQ-1 | ✅ | ✅ | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 7 | `leecode/src/demo/ExportService.java` | REQ-3 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 8 | `leecode/src/demo/ExportServiceImpl.java` | REQ-3 | ✅ | ✅ | N/A | N/A | ✅ | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |
| 9 | `leecode/src/demo/DemoMain.java` | REQ-1/3 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |

**列说明**：G1 并发 … G17 可应急；S1 SQL 注入 … S10 CSRF/CORS/跳转。接口文件（#1/#3/#5/#7）为纯声明，G/S 全 N/A(接口声明无实现逻辑)。

---

## Step 2 — 功能（产物 B）

> REQ 来源：`requirement_section` 原文。功能不符标 **P0**。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | Given 需要 Java 接口；When 编写 HelloWorld/哈希算法（及第三个排序）接口；Then 提供可调用服务 | `用java分别写三个接口helloworld、哈希算法`（需求原文，未显式列出第三个，代码以排序算法补足三 tab 之第三项） | `leecode/src/demo/HelloWorldService.java:7` `leecode/src/demo/HelloWorldServiceImpl.java:10` `leecode/src/demo/HashAlgorithmService.java:7` `leecode/src/demo/HashAlgorithmServiceImpl.java:10` `leecode/src/demo/SortAlgorithmService.java:7` `leecode/src/demo/SortAlgorithmServiceImpl.java:8` | ✅ | 接口+实现齐全；`hello()` 返回问候串；`hash(input,algorithm)` 支持 MD5/SHA-256；`sort(arr)` 快速排序升序 |
| REQ-2 | Given 前端三 tab 展示不同执行结果；When 后台三接口被调用；Then 各 tab 得到对应结果 | `前端新增一个页面，有三个tab分别展示不同的执行结果`（需求原文） | （非 Java，haikulou1.github.io/algorithm-demo.html，本技能跳过） | N/A(非 Java) | 跨仓契约：三服务结果格式须与前端三 tab 对齐（见 report 跨仓对齐点） |
| REQ-3 | Given 新增导出按钮；When 后台提供导出接口；Then 支持导出各个页面展示结果 | `新增导出按钮，后台提供导出接口，支持导出各个页面的展示结果`（需求原文） | `leecode/src/demo/ExportService.java:8` `leecode/src/demo/ExportServiceImpl.java:15` `leecode/src/demo/DemoMain.java:41-43` | ✅ | `export(name,result,timestamp)` 与重载 `export(name,result)`；DemoMain 对三页面结果均调用 export 落盘 JSON |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7 逐节核销（9 文件汇总）：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 文件名=类名；UTF-8；scan 未检出 Tab 字符 |
| A2 | 源文件结构/import 顺序 | ✅ | 无 `import *`；import 按 ASCII 字典序；重载方法连续（ExportService.export 两重载相邻） |
| A3 | 代码样式 | ✅ | K&R 大括号；4 空格缩进；行宽 ≤120（scan 未超限）；关键字空格规范 |
| A4 | 命名规范 | ✅ | 包名 `demo` 全小写；类名 UpperCamel；方法 lowerCamel；常量 `HEX`/`DEFAULT_DIR` UPPER_SNAKE |
| A5 | 编码实践 | ✅ | 所有重写方法均加 `@Override`；catch 非空（均含 throw）；无 finalize 重写 |
| A6 | 特定元素样式 | ✅ | 数组方括号属于类型 `int[] arr`；修饰符顺序 `private static final` 正确；无 long 字面量 N/A |
| A7 | Javadoc 规范 | ⚠️ | public 成员多数有 Javadoc；但 `DemoMain.java:17` 的 `public static void main(String[])` 缺 Javadoc（P2，main 为约定入口可酌情） |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫 `scan-all-rules.sh` 对 33 条 B/M/I 规则无命中。以下逐条核销，无关项标 N/A 并写原因。

| ID | 状态 | 备注 |
|----|------|------|
| B001 | N/A | 无线程相关代码 |
| B002 | N/A | 无锁使用 |
| B003 | N/A | 无集合判等 |
| B004 | N/A | 无 `==` 比较包装类型 |
| B005 | N/A | 无 `Arrays.asList(new int[])` 原始数组 |
| B006 | N/A | 无 equals/hashCode 不一致 |
| B007 | N/A | 无多线程 |
| B008 | N/A | 无 `Executors` 线程池 |
| B009 | N/A | 无 `ArrayList.subList` |
| B010 | N/A | 无 `new BigDecimal(double)` |
| B011 | N/A | 无并发集合 |
| B012 | N/A | 无 `Calendar` |
| B013 | N/A | 无 `Calendar.HOUR` |
| B014 | N/A | 无并发问题 |
| B015 | N/A | 无集合操作 |
| B016 | N/A | 无日期 |
| B017 | N/A | 无 `this == null` |
| B018 | N/A | 无比较器 |
| B019 | N/A | 无泛型 |
| B020 | N/A | 无 instanceof |
| B021 | N/A | 无位运算误用 |
| B022 | N/A | 无 `SimpleDateFormat` |
| B023 | N/A | 无无用异常对象（scan 排除 throw/return/= 后无命中） |
| B024 | N/A | 无 finally |
| B025 | N/A | 无 IO 流 |
| B026 | N/A | 无 `.equals(null)` |
| B027 | N/A | 无异常处理问题 |
| B028 | N/A | 无 `DateUtil.formatDate` |
| B029 | N/A | 无循环 |
| B030 | N/A | 无泛型边界 |
| B031 | N/A | 无反射 |
| B032 | N/A | 无数值转换 |
| B033 | N/A | 无字符串拼接 |
| B034 | N/A | 无 `Integer.parseInt` |
| B035 | N/A | 无浮点比较 |
| B036 | N/A | 无 `IdentityHashMap` |
| B037 | N/A | 无集合 |
| B038 | N/A | 无数组拷贝问题 |
| B039 | N/A | 无 `String.format` |
| B040 | N/A | 无时间单位 |
| B041 | N/A | 无 switch |
| B042 | N/A | 无可变参数 |
| B043 | N/A | 无 `StringBuilder` 并发 |
| B044 | N/A | 无 Optional |
| B045 | N/A | 无集合遍历修改 |
| B046 | N/A | 无 `Date` |
| B047 | N/A | 无日历 |
| B048 | N/A | 无随机数 |
| B049 | N/A | 无 `MMDD` 误用 |
| B050 | N/A | 无日期格式 |
| B051 | N/A | 无 `Boolean.getBoolean` |
| B052 | N/A | 无 `YYYY` 误用 |
| B053 | N/A | 无反射 |
| B054 | N/A | 无 `List` |
| B055 | N/A | 无 `Map` |
| B056 | N/A | 无 `Arrays.asList().add/remove` |
| B057 | N/A | 无 `Collections` |
| B058 | N/A | 无 `toArray` |
| B059 | N/A | 无 `Collections.nCopies` |
| B060 | N/A | 无 `Random` |
| B061 | N/A | 无 `sun.misc` |
| B062 | N/A | 无 `URLClassLoader` |
| B063 | N/A | 无 `javax.xml` |
| B064 | N/A | 无过时 API |
| B065 | N/A | 无 `String` 构造 |
| B066 | N/A | 无 `(int)Math.random()` |
| B067 | N/A | 无 `nextInt()%` |
| B068 | N/A | 无 `String.indexOf` |
| B069 | N/A | 无 `String.substring` |
| B070 | N/A | 无集合 |
| B071 | N/A | 无 `.size()>=0` |
| B072 | N/A | 无 `List.contains` |
| B073 | N/A | 无 `new StringBuilder('c')` |
| B074 | N/A | 无 `.substring(0)` |
| B075 | N/A | 无线程 |
| B076 | N/A | 无 `@Transactional` |
| B077 | N/A | 无 `List` |
| B078 | N/A | 无 `Map` |
| B079 | N/A | 无集合 |
| B080 | N/A | 无集合 |
| B081 | N/A | 无集合 |
| M001 | N/A | 无包装类型比较 |
| M002 | N/A | 无 `StringBuilder` |
| M003 | N/A | 无 `new Integer()` 等包装构造 |
| M004 | N/A | 无 `printStackTrace()` |
| M005 | N/A | 无 `BigDecimal` |
| M006 | N/A | 无 `Thread` |
| M007 | N/A | 无空 catch（两 catch 块均含 throw） |
| M008 | N/A | 无 `Integer.parseInt` |
| M009 | N/A | 无 `Random` |
| M010 | N/A | 无 switch |
| M011 | N/A | 无 `String.format` |
| M012 | N/A | 无并发 |
| M013 | N/A | 无可变对象 |
| M014 | N/A | 无泛型 |
| M015 | N/A | 无 `Math.random` |
| M016 | N/A | 无 `LocalDateTime.now()` 默认时区（使用 `Instant`+显式 `ZoneId`，合规） |
| M017 | N/A | 无 `ThreadLocal` |
| M018 | N/A | 无 `InetAddress` |
| M019 | N/A | 无 `URL` |
| M020 | N/A | 无 `Date` |
| M021 | N/A | 无 `Calendar` |
| M022 | N/A | 无 `Optional.of(null)` |
| M023 | N/A | 无 `String.getBytes` 无字符集（均用 `StandardCharsets.UTF_8`，合规） |
| M024 | N/A | 无 `StringBuilder` |
| M025 | N/A | 无 `List` |
| M026 | N/A | 无 `Map` |
| M027 | N/A | 无 `ThreadLocal` |
| I001 | N/A | 无 `@Test(expected=)` |
| I002 | N/A | 无日志断言 |
| I003 | N/A | 无注释问题 |
| I004 | N/A | 无 `new Date()`（使用 `java.time`，合规） |
| I005 | N/A | 无 `Random` |
| I006 | N/A | 无 `String` |
| I007 | N/A | 无泛型 |
| I008 | N/A | 无 `List` |
| I009 | N/A | 无 `Map` |
| I010 | N/A | 无测试 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无共享可变状态，服务无状态 |
| G1.2 | N/A | 无线程池 |
| G1.3 | N/A | 无 ThreadLocal |
| G1.4 | N/A | 无并发集合 |
| G2.1 | N/A | 无外部调用需超时 |
| G2.2 | N/A | 无重试 |
| G2.3 | N/A | 无限流 |
| G3.1 | N/A | 无需关闭的流（`Files.write` 自动管理） |
| G3.2 | N/A | 无连接池 |
| G3.3 | N/A | 无锁 |
| G4.1 | N/A | 无事务 |
| G4.2 | N/A | 无事务 |
| G4.3 | N/A | 无事务 |
| G4.4 | N/A | 无事务 |
| G5.1 | ✅ | `HashAlgorithmServiceImpl.java:16,19` null input/algorithm 兜底；`SortAlgorithmServiceImpl.java:12` null arr 兜底；`ExportServiceImpl.java:22` null name 兜底 |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存 |
| G7.1 | N/A | 无序列化 |
| G7.2 | N/A | 无序列化 |
| G8.1 | N/A | 无限流 |
| G8.2 | N/A | 无熔断 |
| G8.3 | N/A | 无降级 |
| G8.4 | N/A | 无资源隔离 |
| G8.5 | N/A | 无过载保护 |
| G8.6 | N/A | 无异步 |
| G8.7 | N/A | 无排队 |
| G9.1 | N/A | 无幂等 |
| G9.2 | N/A | 无幂等 |
| G9.3 | N/A | 无幂等 |
| G10.1 | N/A | 无监控 |
| G10.2 | N/A | 无监控 |
| G10.3 | N/A | 无监控 |
| G11.1 | N/A | 无灰度 |
| G11.2 | N/A | 无灰度 |
| G11.3 | N/A | 无灰度 |
| G11.4 | N/A | 无灰度 |
| G12.1 | N/A | 无变更 |
| G12.2 | N/A | 无变更 |
| G13.1 | ✅ | 日志级别无不匹配（scan 未命中）；但 DemoMain 用 `System.out` 而非 logger（P2 建议） |
| G14.1 | N/A | 无金额字段 |
| G14.2 | N/A | 无金额 |
| G14.3 | N/A | 无金额 |
| G14.4 | N/A | 无金额 |
| G15.1 | N/A | 无 DDL |
| G15.2 | N/A | 无 DDL |
| G15.3 | N/A | 无 DDL |
| G16.1 | N/A | 无自定义异常 |
| G16.2 | ⚠️ | scan 命中 2 处（`ExportServiceImpl.java:30`、`HashAlgorithmServiceImpl.java:26`）为**误报**：catch 内均 `throw` 重新抛出；但建议补日志（P2） |
| G16.3 | N/A | 无异常转换问题 |
| G16.4 | N/A | 无异常 |
| G17.1 | N/A | 无应急 |
| G17.2 | N/A | 无应急 |
| G17.3 | N/A | 无应急 |
| G18.1 | N/A | 无安全补强 |
| G18.2 | N/A | 无安全补强 |
| G18.3 | N/A | 无安全补强 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 MyBatis/SQL |
| S1.2 | N/A | 无 SQL |
| S1.3 | N/A | 无 SQL |
| S2.1 | N/A | 无 XSS |
| S2.2 | N/A | 无 XSS |
| S2.3 | N/A | 无 XSS |
| S3.1 | N/A | 无 SSRF |
| S3.2 | N/A | 无 SSRF |
| S3.3 | ⚠️ | `ExportServiceImpl.java:27` `dir.resolve(safeName+".json")` 未净化 name，若 name 含 `../` 可路径逃逸（P1，见 report §7.1） |
| S4.1 | N/A | 无命令执行（scan 未命中） |
| S4.2 | N/A | 无命令执行 |
| S5.1 | N/A | 无文件上传 |
| S5.2 | N/A | 无文件上传 |
| S6.1 | N/A | 无反序列化（scan 未命中） |
| S6.2 | N/A | 无反序列化 |
| S6.3 | N/A | 无反序列化 |
| S7.1 | N/A | 无 XSS |
| S7.2 | N/A | 无 XSS |
| S7.3 | N/A | 无 XSS |
| S8.1 | N/A | 无 CSRF |
| S8.2 | N/A | 无 CSRF |
| S8.3 | N/A | 无 CSRF |
| S8.4 | N/A | 无 CSRF |
| S9.1 | N/A | 无硬编码密钥（scan 未命中） |
| S9.2 | N/A | 无密钥管理 |
| S9.3 | ⚠️ | `HashAlgorithmServiceImpl.java:23` `MessageDigest.getInstance(algorithm)` 支持 MD5（DemoMain:28 调用），MD5 为弱哈希算法（P1，见 report §7.1） |
| S9.4 | N/A | 无不安全随机数（scan 未命中） |
| S10.1 | N/A | 无 CSRF Token |
| S10.2 | N/A | 无 CORS |
| S10.3 | N/A | 无跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

> `customized-checklist.md` 仅含示例项（U1.1 为示例），按技能约定整节标 `N/A(未启用自定义规则)`。

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | N/A(未启用自定义规则) |
| U1.2 | N/A | N/A(未启用自定义规则) |
| U1.3 | N/A | N/A(未启用自定义规则) |
| U2.1 | N/A | N/A(未启用自定义规则) |
| U2.2 | N/A | N/A(未启用自定义规则) |
| U2.3 | N/A | N/A(未启用自定义规则) |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，且有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`

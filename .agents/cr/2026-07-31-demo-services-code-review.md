# Code Review Report

> **Change** `demo-services (HelloWorld/Hash/Sort/Export)` · **分支/Commit** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-ea881811-3e0b-4ae0-` / `013d4b2` · **日期** `2026-07-31` · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。已先运行 `scan-all-rules.sh` 并将要点并入 §5，再写 LLM 结论。问题均含 `path:line` 或清单 ID。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 9 |
| 变更行数 | `+337 / -0` |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| `HelloWorldService` | `[leecode] leecode/src/demo/HelloWorldService.java` | HelloWorld 接口 |
| `HelloWorldServiceImpl` | `[leecode] leecode/src/demo/HelloWorldServiceImpl.java` | HelloWorld 实现 |
| `HashAlgorithmService` | `[leecode] leecode/src/demo/HashAlgorithmService.java` | 哈希算法接口 |
| `HashAlgorithmServiceImpl` | `[leecode] leecode/src/demo/HashAlgorithmServiceImpl.java` | 哈希算法实现 |
| `SortAlgorithmService` | `[leecode] leecode/src/demo/SortAlgorithmService.java` | 排序算法接口 |
| `SortAlgorithmServiceImpl` | `[leecode] leecode/src/demo/SortAlgorithmServiceImpl.java` | 排序算法实现 |
| `ExportService` | `[leecode] leecode/src/demo/ExportService.java` | 导出接口 |
| `ExportServiceImpl` | `[leecode] leecode/src/demo/ExportServiceImpl.java` | 导出实现 |
| `DemoMain` | `[leecode] leecode/src/demo/DemoMain.java` | 演示入口 |

> 非 Java：`[haikulou1.github.io] algorithm-demo.html` 按技能 Java 守卫跳过，未纳入本次 CR 逐文件清单；跨仓对齐点见 §7。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 2 | 2 |

---

## 3. Step 2 — 功能（REQ）

### REQ-1: Java 三个服务接口（HelloWorld / 哈希 / 排序）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| Given 需要 Java 接口；When 编写 HelloWorld/哈希算法（及第三个排序）接口；Then 提供可调用服务 | ✅ | `用java分别写三个接口helloworld、哈希算法`（需求原文） | `HelloWorldService.java:7` `HashAlgorithmService.java:7` `SortAlgorithmService.java:7` + 各 `*Impl` 实现 | 接口+实现齐全；需求仅显式列两项，第三项以排序算法补足三 tab 之第三项，合理 |

### REQ-2: 前端三 tab 展示不同执行结果

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| Given 前端三 tab；When 后台三接口被调用；Then 各 tab 得到对应结果 | N/A(非 Java) | `前端新增一个页面，有三个tab分别展示不同的执行结果`（需求原文） | `[haikulou1.github.io] algorithm-demo.html`（本技能跳过） | 跨仓契约：三服务返回格式须与前端三 tab 对齐（见 §7 跨仓对齐点） |

### REQ-3: 后台导出接口支持导出各页面结果

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| Given 新增导出按钮；When 后台提供导出接口；Then 支持导出各个页面展示结果 | ✅ | `新增导出按钮，后台提供导出接口，支持导出各个页面的展示结果`（需求原文） | `ExportService.java:8` `ExportServiceImpl.java:15` `DemoMain.java:41-43` | `export(name,result,timestamp)` 及重载；DemoMain 对三页面结果均调用 export 落盘 JSON |

---

## 4. Step 3 — 可读性检查

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ⚠️ | A7.1 `DemoMain.java:17` — `public static void main(String[])` 缺 Javadoc（P2，main 为约定入口可酌情）；其余 A1–A6 均 ✅（无 Tab、无 `import *`、4 空格缩进、K&R 大括号、命名规范、`@Override` 齐全） |

---

## 5. Step 4 — 可靠性检查

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「已扫无命中」） |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ⚠️ | P2 | G16.2 scan 命中 2 处为**误报**（catch 内均 throw 重新抛出，非吞异常）；G5 边界条件处理良好（null 兜底 ✅）；建议补日志（P2） |
| 安全 | `security-checklist.md` S1–S10 | ❌ | P1 | S9.3 命中 `HashAlgorithmServiceImpl.java:23` 支持 MD5（弱哈希）；S3.3 命中 `ExportServiceImpl.java:27` 文件名未净化路径逃逸 |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ✅ | — | 预扫 `scan-all-rules.sh` 对 33 条 B/M/I 无命中；120 条逐条核销完成，无命中 |

**scan-all-rules.sh 预扫摘要**：
```
=== Summary: 2 findings (P0=2, P1=0, P2=0) | 52/222 rules scanned ===
[P0] G16.2 — CatchWithoutLogging: leecode/src/demo/ExportServiceImpl.java:30
[P0] G16.2 — CatchWithoutLogging: leecode/src/demo/HashAlgorithmServiceImpl.java:26
```
**误报复核**：两处 G16.2 均为误报——catch 块内均含 `throw new ...(...)` 重新抛出（ExportServiceImpl.java:31、HashAlgorithmServiceImpl.java:27），满足"捕获后向上抛出"，非吞异常。脚本因正则只扫 catch 行本身未识别下一行 throw。结论降级为合规。

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | N/A(未启用自定义规则) |

---

## 7. 结论

- **合并建议**：修复后合并
- **P0**：无
- **P1**：
  1. `S3.3` `ExportServiceImpl.java:27` — 导出文件名未净化，`name` 含 `../` 可路径逃逸，需对 `safeName` 做文件名安全净化（去除路径分隔符）。
  2. `S9.3` `HashAlgorithmServiceImpl.java:23` — 支持调用方传入 `MD5`，MD5 为弱哈希算法；若仅用于演示无安全场景可保留，否则应限制算法白名单为 `SHA-256`。
- **P2**：
  1. `G16.2` `ExportServiceImpl.java:30` / `HashAlgorithmServiceImpl.java:26` — catch 重新抛出虽非吞异常，但未记录日志，排障可观测性不足，建议在 throw 前补 `e` 日志或使用 logger。
  2. `A7.1` `DemoMain.java:17` — `public static void main` 缺 Javadoc；且 `DemoMain` 全程用 `System.out.println` 而非 logger（`G13.1` 建议），演示代码可酌情。
- **一句话**：功能完整、结构清晰、边界处理良好；2 个 P1 安全隐患（路径逃逸、弱哈希）应修复后合并，2 个 P2 为可观测性/风格建议。

### 跨仓对齐点

- **三服务返回格式 ↔ 前端三 tab**：`HelloWorld` 返回问候串（HelloWorldServiceImpl.java:14）、`Hash` 返回 JSON `{input,MD5,SHA-256}`（DemoMain.java:30）、`Sort` 返回 JSON `{origin,sorted}`（DemoMain.java:36）。前端 `[haikulou1.github.io] algorithm-demo.html` 的三个 tab 需与这三个格式一致——本次未审该 HTML（技能限 Java），建议前端侧自检字段名/结构匹配。
- **导出格式 ↔ 前端导出按钮**：`ExportServiceImpl.buildJson` 产出 `{name,result,timestamp}` JSON（ExportServiceImpl.java:43-48），前端导出按钮调用的返回须与此对齐；`result` 字段对 hash/sort 嵌入内层 JSON（toJsonValue 判断 `{`/`[` 前缀直接嵌入）。

---

## 7.1 问题片段（必填）

### P1-1: 文件名未净化导致路径逃逸

- **P1** `S3.3` `leecode/src/demo/ExportServiceImpl.java:27` — `dir.resolve(safeName+".json")` 中 `safeName` 仅做空判兜底为 `"result"`，未净化路径分隔符；若调用方传入 `"../etc/passwd"`，可逃逸到默认目录之外。
  片段范围：`leecode/src/demo/ExportServiceImpl.java:20-29`

```java
L20|    public String export(String name, String result, String timestamp) {
L21|        String safeName = name == null || name.isEmpty() ? "result" : name;
L22|        String json = buildJson(safeName, result, timestamp);
L23|        Path dir = Paths.get(DEFAULT_DIR);
L24|        try {
L25|            Files.createDirectories(dir);
L26|            Path file = dir.resolve(safeName + ".json");
L27|            // 问题：safeName 未净化，含 ../ 可路径逃逸
L28|            Files.write(file, json.getBytes(StandardCharsets.UTF_8));
L29|            return file.toAbsolutePath().toString();
```

### P1-2: 弱哈希算法 MD5

- **P1** `S9.3` `leecode/src/demo/HashAlgorithmServiceImpl.java:23` — `MessageDigest.getInstance(algorithm)` 接受调用方传入 `MD5`；`DemoMain.java:28` 即以 `"MD5"` 调用。MD5 已被证明不抗碰撞，属弱哈希。
  片段范围：`leecode/src/demo/HashAlgorithmServiceImpl.java:14-29`

```java
L14|    public String hash(String input, String algorithm) {
L15|        if (input == null) {
L16|            input = "";
L17|        }
L18|        if (algorithm == null || algorithm.isEmpty()) {
L19|            algorithm = "SHA-256";
L20|        }
L21|        try {
L22|            MessageDigest digest = MessageDigest.getInstance(algorithm);
L23|            // 问题：algorithm 可为 MD5，弱哈希
L24|            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
L25|            return toHex(bytes);
L26|        } catch (NoSuchAlgorithmException e) {
L27|            throw new IllegalArgumentException("不支持的哈希算法: " + algorithm, e);
```

### P2-1: catch 重新抛出但未记录日志

- **P2** `G16.2` `leecode/src/demo/ExportServiceImpl.java:30` / `leecode/src/demo/HashAlgorithmServiceImpl.java:26` — 两处 catch 块均 `throw new ...(...)` 重新抛出（非吞异常，scan 误报），但 throw 前未记录原始异常日志，排障可观测性不足。
  片段范围：`leecode/src/demo/HashAlgorithmServiceImpl.java:22-28`

```java
L22|        try {
L23|            MessageDigest digest = MessageDigest.getInstance(algorithm);
L24|            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
L25|            return toHex(bytes);
L26|        } catch (NoSuchAlgorithmException e) {
L27|            // 问题：重新抛出但未记录 e 日志
L28|            throw new IllegalArgumentException("不支持的哈希算法: " + algorithm, e);
```

### P2-2: main 方法缺 Javadoc 且全程 System.out

- **P2** `A7.1` `leecode/src/demo/DemoMain.java:17` — `public static void main(String[])` 无 Javadoc；且全类用 `System.out.println`（G13.1 建议），演示代码可酌情。
  片段范围：`leecode/src/demo/DemoMain.java:17-25`

```java
L17|    public static void main(String[] args) {
L18|        HelloWorldService helloService = new HelloWorldServiceImpl();
L19|        HashAlgorithmService hashService = new HashAlgorithmServiceImpl();
L20|        SortAlgorithmService sortService = new SortAlgorithmServiceImpl();
L21|        ExportService exportService = new ExportServiceImpl();
L22|
L23|        // 问题：无 Javadoc；后续用 System.out 而非 logger
L24|        String hello = helloService.hello();
L25|        System.out.println("[HelloWorld] result = " + hello);
```

---

## 8. 修复任务列表

### P0

- 无 P0 待修复项。

### P1

- [ ] **P1** `leecode/src/demo/ExportServiceImpl.java:27` — 净化导出文件名 `safeName`，剥离 `/`、`\`、`..` 等路径分隔符或做白名单（仅允许字母数字下划线短横），防止路径逃逸
- [ ] **P1** `leecode/src/demo/HashAlgorithmServiceImpl.java:23` — 限制哈希算法白名单为 `SHA-256`（必要时保留 `SHA-512`），拒绝 `MD5`/`SHA-1`；或在文档明确仅用于非安全演示场景

### P2（可选）

- [ ] **P2** `leecode/src/demo/ExportServiceImpl.java:30` — catch 重新抛出前补充日志记录原始异常（logger.warn/error）
- [ ] **P2** `leecode/src/demo/HashAlgorithmServiceImpl.java:26` — catch 重新抛出前补充日志记录原始异常
- [ ] **P2** `leecode/src/demo/DemoMain.java:17` — 为 `public static void main` 补 Javadoc；考虑将 `System.out.println` 替换为 logger

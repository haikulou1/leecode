# Code Review Checklist

> **Change** `hello-world` · **分支/Commit** `AI/task-DEV-ddccb2af` / `master` · **日期** `2026-07-29`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：预扫已完成（见下方预扫摘要），再由 LLM 完成 Step 2–5 中脚本未覆盖项及复核。

---

### 预扫摘要（scan-all-rules.sh）

```
=== Step 4 Rule Scan (B/M/I + A/S/G) ===
Targets: leecode/src/l_hello_world/HelloWorld.java
Engine:  ripgrep

[P1] S10.2 — CorsWildcard: 69:            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

=== Summary: 1 findings (P0=0, P1=1, P2=0) | 52/222 rules scanned ===
```

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|----|----|----|----|----|----|----|----|----|----|--------|
| 1 | `leecode/src/l_hello_world/HelloWorld.java` | REQ-1/2/3 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ | ⚠️ 已审有问题 |
| 2 | `leecode/src/l_hello_world/README.md` | 文档 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过(非 Java) |
| 3 | `haikulou1.github.io/hello.html` | 跨仓契约对齐 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过(非 Java) |

> 注：`hello.html` 非 Java 文件，本技能 Step3/4 不审；但其作为前端调用方纳入 Step 2 跨仓契约对齐核销。`README.md` 为文档，跳过。

---

## Step 2 — 功能（产物 B）

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 后端提供 HTTP 服务供前端调用 | `后端是leetcode` | `HelloWorld.java` | ✅ | `HelloWorld.java:41-50` `main()` 启动 HttpServer(8080) 并注册 `/hello` |
| REQ-2 | 前后端接口契约一致（路径/端口/响应字段） | `helloworld 前端是haikulou1.github.io 后端是leetcode` | `HelloWorld.java` `hello.html` | ✅ | 后端 `:8080/hello` 返回 `{"message":"Hello, World!","source":"leecode"}`(`:27,:32-33`)；前端 `fetch("http://localhost:8080/hello")` 解析 `data.message`/`data.source`(`hello.html:49,:62-64`) |
| REQ-3 | 跨域支持（GitHub Pages → 本地后端） | 跨仓前后端架构 | `HelloWorld.java` `hello.html` | ⚠️ | 功能达成：`Access-Control-Allow-Origin: *`(`:69`)；但使用通配符见 S10.2 |

---

## Step 3 — 可读性检查（产物 C）

对照 `references/readability-checklist.md` A1–A7 逐节核销（仅对 `HelloWorld.java`）：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 标准 import + 类声明，UTF-8，4 空格缩进 |
| A2 | 源文件结构/import 顺序 | ✅ | `com.sun.net.httpserver.*` 先于 `java.*`，组内字母序 |
| A3 | 代码样式 | ✅ | K&R 大括号，缩进一致 |
| A4 | 命名规范 | ✅ | 常量 `SERVER_PORT/HELLO_PATH/HELLO_RESPONSE_BODY` 全大写下划线；类名 `HelloHandler` 驼峰 |
| A5 | 编码实践 | ✅ | JSON 为静态常量字面量，已正确转义 |
| A6 | 特定元素样式 | ✅ | — |
| A7 | Javadoc 规范 | ✅ | 类/方法/常量均有 Javadoc，含 `@param/@throws/@author/@date` |

> 预扫 A 类规则无命中，LLM 复核一致。

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫覆盖 B(25/81)+M(6/27)+I(2/10)=33 条，**0 命中**。本文件无集合/数组/日期/线程/BigDecimal/异常捕获/移位/常量溢出等场景，其余 ID 与变更无关。

| ID 区间 | 状态 | 备注 |
|----------|------|------|
| B001–B081 | N/A | 预扫 25 条无命中；其余 N/A(无 parse/数组/集合/日期/线程/BigDecimal/异常捕获/移位/常量溢出/双括号初始化等场景) |
| M001–M027 | N/A | 预扫 6 条无命中；其余 N/A(无集合/资源/异常/线程相关 Major 模式) |
| I001–I010 | N/A | 预扫 2 条无命中；其余 N/A(无 Info 级命中场景) |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务/并发锁场景 |
| G1.2 | N/A | 同上 |
| G1.3 | N/A | 同上 |
| G1.4 | N/A | 无多锁 |
| G2.1 | N/A | 只读 GET 固定响应，无写操作 |
| G2.2 | N/A | 同上 |
| G2.3 | N/A | 同上 |
| G3.1 | N/A | 无分布式事务 |
| G3.2 | N/A | 无 @Transactional |
| G4.1 | N/A | 无 SQL |
| G4.2 | N/A | 无 SQL |
| G4.3 | N/A | 无 SQL |
| G5.1 | N/A | 无 MQ |
| G6.1 | N/A | 无缓存 |
| G6.2 | N/A | 无缓存 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | ✅ | 无 catch 吞异常；异常均向上抛出 |
| G8.2 | N/A | 无核心链路强依赖 |
| G8.3 | ✅ | `HelloWorld.java:75-77` try-with-resources 释放 OutputStream |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无 Executors/无界队列线程池 |
| G9.1 | N/A | 本服务为服务端，无外部调用 |
| G9.2 | N/A | 同上 |
| G9.3 | N/A | 同上 |
| G10.1 | N/A | 固定 JSON 响应无 null 字段 |
| G10.2 | N/A | 新增接口，无契约变更 |
| G11.1 | ⚠️ | `HelloWorld.java` 无单测；P2（demo 可选） |
| G11.2 | N/A | 固定响应无边界 |
| G11.3 | N/A | 无入参 |
| G11.4 | N/A | 无数值运算 |
| G12.1 | N/A | 无资金场景 |
| G12.2 | N/A | 无止血需求 |
| G13.1 | N/A | 启动日志 System.out，demo 可接受 |
| G14.1 | N/A | 无金额 |
| G14.2 | N/A | 无多租户 |
| G14.3 | N/A | 无时区 |
| G14.4 | N/A | 无日期格式化 |
| G15.1 | N/A | 无表结构变更 |
| G15.2 | N/A | 新接口 |
| G15.3 | N/A | 无开关需求 |
| G16.1 | N/A | demo 无埋点需求 |
| G16.2 | ⚠️ | `HelloWorld.java:67` `handle()` 写响应失败仅抛 IOException，无日志/traceId；P1 |
| G16.3 | N/A | 无业务/系统异常分级日志 |
| G16.4 | ✅ | 无空 catch / 无 printStackTrace |
| G17.1 | N/A | demo |
| G17.2 | N/A | demo |
| G17.3 | N/A | 无数据变更 |
| G18.1 | N/A | 无安全补强场景 |
| G18.2 | N/A | 同上 |
| G18.3 | N/A | 同上 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL |
| S1.2 | N/A | 无 SQL |
| S1.3 | N/A | 无 SQL |
| S2.1 | ✅ | 固定 JSON 响应，Content-Type application/json |
| S2.2 | ✅ | 无服务端 HTML 渲染 |
| S2.3 | ✅ | 无反射用户输入 |
| S3.1 | N/A | 无命令执行 |
| S3.2 | N/A | 无命令执行 |
| S3.3 | N/A | 无命令执行 |
| S4.1 | N/A | 无反序列化 |
| S4.2 | N/A | 无反序列化 |
| S5.1 | N/A | 无文件上传 |
| S5.2 | N/A | 无路径操作 |
| S6.1 | N/A | 无 SSRF（本服务为服务端） |
| S7.1 | N/A | 无敏感信息 |
| S7.2 | N/A | 无敏感信息 |
| S7.3 | N/A | 无敏感信息 |
| S8.1 | N/A | 公开 hello world，无认证需求 |
| S8.2 | N/A | 同上 |
| S8.3 | N/A | 同上 |
| S9.1 | N/A | 无密码/加密 |
| S9.2 | N/A | 无弱加密 |
| S9.3 | N/A | 无随机数 |
| S10.1 | N/A | 无 CSRF 风险（GET 只读） |
| S10.2 | ⚠️ | **预扫命中** `HelloWorld.java:69` CORS 通配符 `*`；P1 |
| S10.3 | N/A | 无跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | N/A(未启用自定义规则) |

---

## 收口核销

- 执行队列 `⬜ 待审`：0（跳过项除外）✅
- Step2 章节级勾选与逐文件结论一致 ✅
- Step3/4/5 跨文件条目已合并勾选 ✅
- report 审查范围文件数=1（`HelloWorld.java`），与已审队列一致 ✅

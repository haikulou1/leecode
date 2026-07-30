# Java 算法接口 + 前端展示页 实施计划

> **Skill:** /writing-plans | **阶段:** plan | **仓库:** leecode（后端）、haikulou1.github.io（前端）

## Goal

Java 提供 HelloWorld、哈希算法、冒泡排序三个 HTTP 接口 + 导出接口；前端静态站新增三 Tab 展示页与导出按钮。

## Architecture

后端在 leecode 新建 Spring Boot Maven 子项目 `algo-api`，四个 REST 接口；冒泡排序复用现有 `datastruct/src/排序/冒泡排序.java` 的 `sort()` 逻辑。前端新增 `algo-demo/index.html` + `js/algo-demo.js`，原生 `fetch` 调用后端。后端启用 CORS 支持静态站跨域。

## Tech Stack

- 后端: Java 8+ / Spring Boot 2.7.18 / Maven / JDK `MessageDigest`（SHA-256）
- 前端: 原生 HTML5 + CSS3 + JS（fetch），无构建工具
- 导出: CSV 文件流

## File Structure

**后端 — leecode 仓库**
```
algo-api/
├── pom.xml
├── src/main/java/algoapi/
│   ├── AlgoApiApplication.java          # 启动类
│   ├── controller/AlgoController.java   # hello / hash / bubble / export
│   ├── service/{HashService,BubbleSortService,ExportService}.java
│   ├── config/CorsConfig.java           # 全局 CORS
│   └── exception/GlobalExceptionHandler.java  # ★ 全局异常兜底
└── src/main/resources/application.yml    # port=8080
```

**前端 — haikulou1.github.io 仓库**
```
algo-demo/index.html    # 三 Tab + 导出按钮
js/algo-demo.js         # fetch 调用 + 错误兜底展示
```

## 跨库接口契约

| 接口 | 方法 | 路径 | 入参 | 出参 | 导出 type |
|------|------|------|------|------|-----------|
| HelloWorld | GET | `/api/hello` | 无 | `{"result":"HelloWorld"}` | `hello` |
| 哈希算法 | GET | `/api/hash` | `?input=字符串` | `{"input","algorithm":"SHA-256","hash"}` | `hash` |
| 冒泡排序 | GET | `/api/bubble` | `?nums=3,1,2` | `{"input":[...],"sorted":[...]}` | `bubble` |
| 导出 | GET | `/api/export` | `?type=hello\|hash\|bubble` | CSV 文件流 | — |

**对齐点:** Tab 顺序 = `type` 枚举顺序 `hello|hash|bubble`；CORS 允许 `*` + GET；新增字段向后兼容；不修改 `冒泡排序.java` 公开签名。

## 复用现有实现

`datastruct/src/排序/冒泡排序.java`：`package 排序;` → `public static void sort(int a[])`。因中文包名跨模块不便 import，`BubbleSortService` 内重写相同算法逻辑（一致性），不改原文件。

---

## 异常兜底方案

### 后端 — 统一异常处理

**新增 `exception/GlobalExceptionHandler.java`（`@ControllerAdvice`）**：捕获所有异常，返回统一错误结构：
```java
@ResponseBody @ExceptionHandler(Exception.class)
public Map<String,Object> handle(Exception e) {
    return Map.of("code", 500, "message", "服务异常，请稍后重试", "data", "");
}
```

| 场景 | 兜底策略 | HTTP |
|------|----------|------|
| `hash` 的 `input` 为空/null | `@RequestParam(defaultValue="hello")` 回退默认值 | 200 |
| `bubble` 的 `nums` 非数字/格式错 | Service 内 `try-catch NumberFormatException` → 回退默认数组 `[5,3,8,1,9,2]`，响应加 `"warning":"输入非法，已使用默认数组"` | 200 |
| `export` 的 `type` 非法 | 抛 `IllegalArgumentException` → 全局处理器捕获 → 返回 `{"code":400,"message":"不支持的导出类型"}` | 400 |
| `HashService` SHA-256 计算异常（`NoSuchAlgorithmException`） | 该算法为 JDK 内置，理论不可达；仍 try-catch 回退 MD5 并标记 `"algorithm":"MD5(fallback)"` | 200 |
| 服务未启动 / 端口占用 | 启动失败日志，前端侧兜底（见下） | — |

**兜底原则:** 服务层不抛异常到用户面，参数非法一律回退默认值 + warning 标记，保证接口始终 200 返回有效结构；仅 `export` 非法 `type` 返回 400（避免下载空文件）。

### 前端 — fetch 错误兜底

`js/algo-demo.js` 每个 `fetch` 包裹 `try-catch` + 超时控制：
```js
async function callApi(url) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 5000); // 5s 超时
  try {
    const res = await fetch(url, { signal: ctrl.signal });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    return await res.json();
  } catch (e) {
    return { _error: '接口请求失败：' + e.message + '（后端可能未启动）' };
  } finally { clearTimeout(timer); }
}
```
- 渲染时检测 `_error`：展示红色提示文案 + 保留上次成功结果（不覆盖）。
- 导出按钮：`window.open` 失败时 `alert('导出失败，请检查后端服务')`。

---

## Task 1: 后端项目骨架

**Files:** `algo-api/pom.xml`、`AlgoApiApplication.java`、`application.yml`、`config/CorsConfig.java`、`exception/GlobalExceptionHandler.java`

- [ ] `pom.xml`：`groupId=com.leecode`，`artifactId=algo-api`，`spring-boot-starter-web` 2.7.18，`java.version=8`
- [ ] 启动类 `@SpringBootApplication` + `main`
- [ ] `application.yml`：`server.port: 8080`
- [ ] `CorsConfig`：`WebMvcConfigurer#addCorsMappings`，允许 `/api/**`
- [ ] `GlobalExceptionHandler`：`@ControllerAdvice` + `@ExceptionHandler(Exception.class)` 统一兜底
- [ ] 验证：`mvn spring-boot:run` 启动成功

## Task 2: HelloWorld 接口

**Files:** `controller/AlgoController.java`

- [ ] `GET /api/hello` → `Collections.singletonMap("result","HelloWorld")`
- [ ] 验证：`curl http://localhost:8080/api/hello`

## Task 3: 哈希算法接口

**Files:** `service/HashService.java`、`AlgoController.java`（+端点）

- [ ] `HashService.sha256(String)`：`MessageDigest("SHA-256")` → hex；try-catch 回退 MD5
- [ ] `GET /api/hash?input=`（default "hello"）→ `Map.of("input","algorithm","hash")`
- [ ] 验证：`curl "http://localhost:8080/api/hash?input=abc"`

## Task 4: 冒泡排序接口

**Files:** `service/BubbleSortService.java`、`AlgoController.java`（+端点）

- [ ] `BubbleSortService.sort(int[])`：`Arrays.copyOf` 不改原数组 + 双重循环（同 `冒泡排序.java` 逻辑）
- [ ] `GET /api/bubble?nums=`（default `5,3,8,1,9,2`）：解析失败 try-catch 回退默认 + warning
- [ ] 验证：`curl "http://localhost:8080/api/bubble?nums=3,1,2"` → `{"input":[3,1,2],"sorted":[1,2,3]}`

## Task 5: 导出接口

**Files:** `service/ExportService.java`、`AlgoController.java`（+端点）

- [ ] `ExportService.exportCsv(type)`：注入 Hash/BubbleSort Service，按 type 路由生成 CSV `byte[]`
  - `hello` → `result\nHelloWorld`；`hash` → `input,algorithm,hash\n...`；`bubble` → `index,input,sorted\n...`
  - 非法 type → `IllegalArgumentException`
- [ ] `GET /api/export?type=`：`ResponseEntity<byte[]>` + `Content-Disposition: attachment; filename=<type>.csv`
- [ ] 验证：`curl -o hello.csv "http://localhost:8080/api/export?type=hello"`

## Task 6: 前端展示页

**Files:** `algo-demo/index.html`、`js/algo-demo.js`

- [ ] `index.html`：标题 + 3 Tab 导航 + 内容区 + 导出按钮，引用 `/js/algo-demo.js`
- [ ] `algo-demo.js`：`BASE_URL='http://localhost:8080/api'`；`switchTab(type)` fetch 填充；`exportResult()` `window.open` 下载；全部 fetch 走 `callApi` 兜底封装
- [ ] 验证：浏览器打开页面，三 Tab 切换 + 导出下载

## Task 7: 跨库联调

- [ ] `mvn spring-boot:run` 启动后端
- [ ] 前端切 Tab 验证无 CORS 报错
- [ ] 各 Tab 导出验证 CSV 内容

---

## Execution Handoff

按 Task 1→7 执行。后端 Task 1-5（leecode），前端 Task 6（haikulou1.github.io），Task 7 联调。每个 Task 验证通过后进入下一个。

## 验证清单

- [ ] `GET /api/hello` → `{"result":"HelloWorld"}`
- [ ] `GET /api/hash?input=abc` 返回 64 位 SHA-256
- [ ] `GET /api/bubble?nums=3,1,2` → `{"input":[3,1,2],"sorted":[1,2,3]}`
- [ ] `GET /api/bubble?nums=abc` → 200 + warning 回退默认数组（兜底验证）
- [ ] `GET /api/export?type=xxx` → 400 错误结构（兜底验证）
- [ ] `GET /api/export?type=hello|hash|bubble` → CSV 文件
- [ ] 前端三 Tab 展示正确，网络失败时显示红色兜底提示
- [ ] 导出按钮可下载 CSV
- [ ] CORS 无报错

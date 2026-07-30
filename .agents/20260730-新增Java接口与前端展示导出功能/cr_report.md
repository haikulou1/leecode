# Code Review Report

> **Change** `新增Java接口与前端展示导出功能` · **分支** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-45c01fae-850d-44fb-`
> **评审日期** 2026-07-30 · **评审人** DTCoder · **技能** dtazziboot-java-code-review

---

## 0. 评审概览

| 维度 | 结论 |
|------|------|
| 功能完整性 | ✅ F01-F05 全部实现，与系分接口契约对齐 |
| 编译可行性 | ✅ 依赖完整，import 正确，无编译错误 |
| 可读性 | ✅ 分层清晰，注释充分，命名规范 |
| 可靠性 | ⚠️ 兜底逻辑覆盖完整，但存在手搓 JSON/CSV 转义缺陷 |
| 安全性 | ⚠️ 前端 innerHTML 存在 XSS 风险，CORS 配置过宽 |
| 测试覆盖 | ❌ 无单元测试 |
| **Blocker 数** | **0** |

### 评审范围

| 仓库 | 文件 | 行数 |
|------|------|------|
| [leecode] | web-demo/pom.xml | 69 |
| [leecode] | web-demo/src/main/java/.../WebDemoApplication.java | 17 |
| [leecode] | web-demo/src/main/java/.../common/AlgorithmConstants.java | 42 |
| [leecode] | web-demo/src/main/java/.../common/ApiResponse.java | 84 |
| [leecode] | web-demo/src/main/java/.../common/BizException.java | 28 |
| [leecode] | web-demo/src/main/java/.../common/GlobalExceptionHandler.java | 61 |
| [leecode] | web-demo/src/main/java/.../common/ResultCode.java | 15 |
| [leecode] | web-demo/src/main/java/.../config/CorsConfig.java | 24 |
| [leecode] | web-demo/src/main/java/.../controller/AlgorithmController.java | 113 |
| [leecode] | web-demo/src/main/java/.../dto/BubbleSortRequest.java | 21 |
| [leecode] | web-demo/src/main/java/.../dto/BubbleSortResult.java | 26 |
| [leecode] | web-demo/src/main/java/.../dto/ExportRequest.java | 32 |
| [leecode] | web-demo/src/main/java/.../dto/HashRequest.java | 18 |
| [leecode] | web-demo/src/main/java/.../dto/HashResult.java | 21 |
| [leecode] | web-demo/src/main/java/.../enums/ExportFormatEnum.java | 32 |
| [leecode] | web-demo/src/main/java/.../enums/ExportTypeEnum.java | 34 |
| [leecode] | web-demo/src/main/java/.../enums/HashAlgorithmEnum.java | 48 |
| [leecode] | web-demo/src/main/java/.../enums/SortOrderEnum.java | 32 |
| [leecode] | web-demo/src/main/java/.../service/AlgorithmService.java | 39 |
| [leecode] | web-demo/src/main/java/.../service/ExportService.java | 27 |
| [leecode] | web-demo/src/main/java/.../service/impl/AlgorithmServiceImpl.java | 179 |
| [leecode] | web-demo/src/main/java/.../service/impl/ExportServiceImpl.java | 223 |
| [leecode] | web-demo/src/main/resources/application.yml | 21 |
| [haikulou1.github.io] | algorithm-demo/index.html | 370 |

### 预扫说明

`scan-all-rules.sh` 依赖 `grep`/`rg`，受任务硬约束禁止使用，降级为纯 LLM 逐文件审查（功能核对 → 可读性检查 → 可靠性检查 → 自定义扩展检查）。

---

## 1. 功能核对（Step 2）

### 1.1 接口契约对齐

| 编号 | 设计定义 | 实现位置 | 对齐状态 |
|------|----------|----------|----------|
| W01 | GET /api/helloworld → {message:"Hello World"} | AlgorithmController:50-62 | ✅ |
| W02 | POST /api/hash → {input, algorithm, hashValue} | AlgorithmController:70-76 | ✅ |
| W03 | POST /api/bubbleSort → {input, sorted, order, truncated} | AlgorithmController:84-90 | ✅ |
| W04 | POST /api/export → 文件流(CSV/JSON) | AlgorithmController:98-112 | ✅ |

### 1.2 业务规则对齐

| 规则 | 设计要求 | 实现状态 |
|------|----------|----------|
| Hash R01 | input 空白 → ALGORITHM_002 | ✅ AlgorithmServiceImpl:43-45 |
| Hash R02 | algorithm 空 → 默认 SHA_256 | ✅ AlgorithmServiceImpl:48-55 |
| Hash R03 | algorithm 非法 → ALGORITHM_001 | ✅ AlgorithmServiceImpl:49-52 |
| Hash 兜底 | NoSuchAlgorithmException → SHA-256 重试 | ✅ AlgorithmServiceImpl:64-76 |
| Sort R01 | numbers null/空 → SORT_001 | ✅ AlgorithmServiceImpl:84-86 |
| Sort R02 | order 空 → 默认 ASC | ✅ AlgorithmServiceImpl:90-100 |
| Sort R03 | order 非法 → 见下方设计矛盾 | ⚠️ 降级为 ASC（遵循设计6.1，与5.1 R03 矛盾） |
| Sort R04 | 先 clone 再排序 | ✅ AlgorithmServiceImpl:116 |
| Sort 兜底 | >1000 截断 + truncated 标记 | ✅ AlgorithmServiceImpl:107-113 |
| Export R01 | type 非法 → EXPORT_001 | ✅ ExportServiceImpl:36-39 |
| Export R02 | format 非法 → 降级 CSV | ✅ ExportServiceImpl:42-49 |
| Export R03 | HASH 缺 hashInput → EXPORT_003 | ✅ ExportServiceImpl:102-105 |
| Export R04 | BUBBLE_SORT 缺 sortNumbers → EXPORT_004 | ✅ ExportServiceImpl:122-125 |
| Export R05 | 复用算法服务 | ✅ ExportServiceImpl:51-66 |

### 1.3 前端功能对齐

| 功能 | 设计要求 | 实现状态 |
|------|----------|----------|
| 三 Tab 展示 | HelloWorld / 哈希 / 冒泡排序 | ✅ index.html:43-47 |
| Tab 切换调接口 | HelloWorld 自动调用 | ✅ index.html:137-141 |
| 哈希 Tab | 输入框 + 算法下拉 + 计算按钮 | ✅ index.html:60-82 |
| 排序 Tab | 数组输入 + 方向选择 + 排序按钮 | ✅ index.html:84-107 |
| 导出按钮 | Blob + a 标签下载 | ✅ index.html:109-111, 320-331 |
| 超时兜底 | >10s 超时提示 | ✅ index.html:144-152 |
| 网络错误兜底 | 无法连接提示 | ✅ index.html:159-160 |
| code!=OK 兜底 | 展示 msg 错误 | ✅ index.html:176-179, 206-208 |
| hashValue 空兜底 | 展示兜底提示 | ✅ index.html:216-217 |
| truncated 兜底 | 展示截断提示 | ✅ index.html:265-266 |
| 导出非文件流兜底 | 解析 JSON 展示错误 | ✅ index.html:311-317 |

---

## 2. 可读性检查（Step 3）

### 2.1 优点

- **分层清晰**：Controller → Service → Impl 三层结构规范，职责单一。
- **命名规范**：类名、方法名、变量名符合 Java 命名规范，语义自解释。
- **注释充分**：每个类/方法均有 Javadoc，含 `@author DTCoder`、`@param`、`@return`。
- **常量集中管理**：错误码与业务常量统一收纳于 `AlgorithmConstants`。
- **枚举设计**：四个枚举均提供 `fromName()` 容错解析方法，大小写不敏感。
- **构造器注入**：Controller 和 ExportServiceImpl 均使用构造器注入，无字段注入。

### 2.2 问题清单

| ID | 等级 | 文件 | 行号 | 问题 | 建议 |
|----|------|------|------|------|------|
| R-001 | P2 | [leecode] dto/BubbleSortRequest.java | 8-9 | Javadoc 描述行重复："冒泡排序请求。" 出现两次 | 删除重复行 |
| R-002 | P2 | [leecode] common/AlgorithmConstants.java | 41 | `FALLBACK_EXPORT_MSG = "兜底导出"` 定义后从未引用 | 删除或在使用处引用 |
| R-003 | P2 | [haikulou1.github.io] index.html | 172 | `var btn = null;` 声明后从未使用（callHelloWorld 函数内） | 删除死代码 |

---

## 3. 可靠性检查（Step 4）

### 3.1 P0 (Blocker) — 无

经逐文件审查，未发现阻断性缺陷：所有接口可正常编译运行，核心算法逻辑正确，兜底降级链路完整。

### 3.2 P1 (Major)

| ID | 文件 | 行号 | 问题 | 影响 | 建议 |
|----|------|------|------|------|------|
| M-001 | [haikulou1.github.io] index.html | 178,207,256,315 | **XSS 风险**：`alertEl.innerHTML = '...' + (data.msg \|\| '请求失败')` 将服务端错误消息直接拼入 innerHTML。后端 BizException 消息含用户输入（如 `"不支持的哈希算法: " + algorithm`），攻击者可构造 `algorithm=<img src=x onerror=alert(1)>` 注入恶意脚本 | 安全：演示场景下影响有限，但违反设计7.4"防止恶意输入"要求 | 前端改用 `textContent` 或对 msg 做 HTML 转义后再赋值 |
| M-002 | [leecode] service/impl/ExportServiceImpl.java | 213-221 | **JSON 转义不完整**：`escapeJson()` 仅处理 `\ " \n \r \t`，未处理 U+0000-U001F 其他控制字符（如 `\b` `\f`），输入含此类字符时导出 JSON 格式损坏 | 可靠性：异常输入导致导出文件格式错误 | 补全控制字符转义，或改用 Jackson ObjectMapper 序列化 |
| M-003 | [leecode] common/AlgorithmConstants.java | 23,29 | **死常量**：`SORT_002` 和 `EXPORT_002` 定义后从未抛出。设计5.1 R03 要求 `order 非法 → SORT_002`，但实现遵循设计6.1 降级为 ASC；`EXPORT_002` 同理降级为 CSV | 可维护性：死常量误导维护者，设计自身存在矛盾 | 统一设计口径：若选择降级策略则删除死常量并更新设计文档；若选择抛错则修改实现 |
| M-004 | [leecode] service/impl/ExportServiceImpl.java | 34-49,78-85 | **格式解析逻辑重复**：`export()` 和 `getFileExtension()` 各自独立解析 format 并做降级，逻辑虽当前一致但维护时易出现不一致 | 可维护性：两处逻辑不同步时文件扩展名与内容格式不匹配 | 提取为公共方法 `resolveFormat(request)` 统一调用 |
| M-005 | [leecode] config/CorsConfig.java | 17-22 | **CORS 配置过宽**：`allowedOriginPatterns("*")` + `allowCredentials(true)`，允许任意源携带凭证访问 | 安全：演示场景无 Cookie/Token 影响有限，生产环境存在 CSRF 风险 | 生产环境收紧为具体前端域名，演示场景可去掉 `allowCredentials(true)` |
| M-006 | — | — | **缺失单元测试**：无任何 `*Test.java` 测试文件，哈希计算、冒泡排序、导出格式化等核心逻辑无测试覆盖 | 质量：核心算法逻辑变更无回归保障 | 补充 AlgorithmServiceImpl 和 ExportServiceImpl 的单元测试 |
| M-007 | [leecode] design.md | 5.1 R03 vs 6.1 | **设计自身矛盾**：5.1 W03 R03 要求 `order 非法 → SORT_002`（抛错），6.1 要求 `排序方向非法 → 降级为 ASC`。实现选择降级 | 设计缺陷：同一行为有两种互斥定义 | 评审设计文档，统一为单一策略 |

### 3.3 P2 (Info)

| ID | 文件 | 行号 | 问题 | 建议 |
|----|------|------|------|------|
| I-001 | [leecode] service/impl/AlgorithmServiceImpl.java | 30-38 | `helloWorld()` 的 try-catch 包裹常量访问 `AlgorithmConstants.HELLO_WORLD_MESSAGE`，该语句不可能抛异常，catch 分支为死代码 | 简化为直接 return |
| I-002 | [leecode] service/impl/AlgorithmServiceImpl.java | 71-75 | SHA-256 的 `NoSuchAlgorithmException` fallback 为死代码——SHA-256 是 JDK 内置保证可用的算法 | 可保留作为防御性编程，但应注释说明 |
| I-003 | [leecode] service/impl/AlgorithmServiceImpl.java | 72-74 | SHA-256 降级仍失败时，`result.algorithm` 仍为原始请求算法（如 MD5），但 hashValue 为空，语义矛盾 | 设置 `result.setAlgorithm("")` 或标注降级 |
| I-004 | [leecode] dto/HashRequest.java, dto/BubbleSortRequest.java | — | DTO 必填字段无 `@NotBlank` / `@NotEmpty` 注解，Controller 的 `@Valid` 无法触发校验，实际靠 Service 层手动校验 | 添加 JSR-303 注解，实现 Controller 层校验前置 |
| I-005 | [leecode] service/impl/ExportServiceImpl.java | 200-207 | CSV 转义 `escapeCsv()` 未检查 `\r`（回车符），RFC 4180 规定含 CR 的字段应加引号 | 补充 `\r` 检测条件 |
| I-006 | [leecode] controller/AlgorithmController.java | 99 | `export()` 方法未加 `@Valid`，与 hash/bubbleSort 不一致（虽 ExportRequest 无注解时 @Valid 无效，但风格不统一） | 统一标注或统一不标注 |

---

## 4. 自定义扩展检查（Step 5）

### 4.1 跨仓对齐检查

| 检查项 | 结论 |
|--------|------|
| 前端 API_BASE 与后端端口 | ✅ 前端 `http://localhost:8080` ↔ 后端 `server.port: 8080` |
| 前端请求路径与后端 @RequestMapping | ✅ `/api/helloworld`、`/api/hash`、`/api/bubbleSort`、`/api/export` 完全匹配 |
| 前端请求体字段与后端 DTO | ✅ `{input, algorithm}` ↔ HashRequest；`{numbers, order}` ↔ BubbleSortRequest；`{type, format, hashInput, hashAlgorithm, sortNumbers, sortOrder}` ↔ ExportRequest |
| 前端响应字段消费与后端 ApiResponse | ✅ `data.code`、`data.msg`、`data.data.{message/input/algorithm/hashValue/sorted/order/truncated}` 全部对齐 |
| 前端导出文件名与后端 Content-Disposition | ✅ 前端 `type.toLowerCase() + '_result.' + format` ↔ 后端 `type.toLowerCase() + "_result." + extension` |
| 前端导出 Content-Type 检测 | ✅ 前端检测 `octet-stream` / `text/plain`，后端设 `APPLICATION_OCTET_STREAM` |
| CORS 跨域 | ✅ 后端 CorsConfig 允许 `/api/**` 跨域，前端为静态页面 fetch |

### 4.2 兜底逻辑完整性

| 场景 | 设计要求 | 后端实现 | 前端实现 |
|------|----------|----------|----------|
| HelloWorld 异常 | 返回固定值 | ✅ Controller+Service 双层 try-catch | ✅ 错误展示 |
| 哈希算法异常 | SHA-256 重试 | ✅ catch NoSuchAlgorithmException | ✅ hashValue 空时提示 |
| 哈希输入空 | ALGORITHM_002 | ✅ BizException | ✅ code!=OK 展示 msg |
| 排序数组过大 | 截断+truncated | ✅ >1000 截取 subList | ✅ truncated===true 提示 |
| 排序方向非法 | 降级 ASC | ✅ orderEnum 降级 | ✅ data.order 展示实际值 |
| 导出服务异常 | 空CSV/JSON | ✅ fallbackContent() | ✅ catch 提示重试 |
| 导出格式非法 | 降级 CSV | ✅ formatEnum 降级 | — |
| 框架未知异常 | ERROR+200 | ✅ GlobalExceptionHandler | ✅ code!=OK 展示 |
| 请求超时 | 超时提示 | — | ✅ fetchWithTimeout 10s |
| 服务未启动 | 连接失败提示 | — | ✅ TypeError 检测 |

### 4.3 编码规范检查（数科 Java 编码规范）

| 检查项 | 结论 |
|--------|------|
| 包结构规范 | ✅ `com.leecode.webdemo.{common,config,controller,dto,enums,service,service.impl}` |
| 类名 UpperCamelCase | ✅ |
| 方法名 lowerCamelCase | ✅ |
| 常量 UPPER_SNAKE_CASE | ✅ AlgorithmConstants |
| Javadoc 完整性 | ✅ 除 DTO getter/setter（Lombok 生成）外均有注释 |
| @author 标注 | ✅ 全部为 DTCoder |
| 构造器注入 | ✅ 无 @Autowired 字段注入 |
| 异常分层 | ✅ BizException(业务) → GlobalExceptionHandler(框架) |
| 日志规范 | ✅ @Slf4j，log.info/warn/error 分级使用 |

---

## 5. 评审结论

### 5.1 总体评价

本次代码变更**整体质量良好**，功能完整覆盖设计 F01-F05 全部需求点，跨仓前后端接口契约完全对齐，兜底降级逻辑覆盖设计 6.1/6.2 全部场景。代码分层规范，注释充分，命名自解释。

### 5.2 Blocker 统计

| 等级 | 数量 | 明细 |
|------|------|------|
| **P0 (Blocker)** | **0** | 无阻断性缺陷 |
| P1 (Major) | 7 | M-001~M-007 |
| P2 (Info) | 6 | I-001~I-006 |

**blocker_count = 0**

### 5.3 修复优先级建议

1. **高优**（安全）：M-001 前端 XSS 修复（textContent 替换 innerHTML）
2. **高优**（可靠性）：M-002 JSON 转义补全或改用 ObjectMapper
3. **中优**（一致性）：M-003 死常量清理 + M-007 设计矛盾统一
4. **中优**（可维护性）：M-004 格式解析逻辑提取公共方法
5. **低优**（工程规范）：M-005 CORS 收紧、M-006 补充单元测试
6. **低优**（代码整洁）：I-001~I-006 清理死代码、补全注解

### 5.4 评审状态

**通过（有条件）**——无 Blocker，核心功能可用。建议优先修复 M-001（XSS）和 M-002（JSON 转义）后合入。

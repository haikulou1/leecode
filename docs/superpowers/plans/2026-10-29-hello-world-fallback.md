# Hello World 异常兜底方案

> 关联计划：`docs/superpowers/plans/2026-10-29-hello-world.md`
> 需求：写个 hello world
> 落地位置：独立文档（本文件）、源码 `HelloWorld.java`、计划文档（补充章节）

## 1. 目标与原则

在不改变正常路径输出（`Hello, World!`，正文 13 字节，`println` 追加换行）的前提下，对三类异常提供确定性兜底，使失败可观测、可追因、可恢复。

原则：

- 正常路径零影响：所有兜底不改变成功时的标准输出。
- 失败必显式退出：异常路径返回非零退出码，便于 CI/调用方识别。
- 降级优先于崩溃：标准输出不可用时降级到标准错误并保留错误摘要。

## 2. 兜底矩阵

| 类别 | 异常场景 | 症状 | 影响 | 兜底方案 | 验证口径 |
|------|---------|------|------|---------|---------|
| 构建/运行环境 | JDK 缺失 | `javac: command not found` | 无法编译/运行 | 免安装解包 JDK：下载 `openjdk-21-*-headless` 包并 `dpkg -x` 到 `/tmp`，设置 `JAVA_HOME`/`PATH` | `javac -version` 返回 21.x |
| 构建/运行环境 | 系统包管理器沙箱受限 | apt `setgroups/seteuid 失败`、`/var/cache/apt Permission denied` | apt 安装失败 | 不依赖 apt，改走免安装解包 JDK | `dpkg -x` 成功、javac 可用 |
| 构建/运行环境 | 下载源不可达 | curl 超时、4xx/5xx | 无法获取 JDK | 按 aliyun 镜像 → Adoptium API → Ubuntu 官方池顺序切换 | curl 200/206 且产物可解包 |
| 构建/运行环境 | 路径含特殊字符 | `InvalidPathException: Malformed input or unmappable characters` | javac 扫描目录失败 | 显式 `-encoding UTF-8 -J-Dfile.encoding=UTF-8`，设 `LANG/LC_ALL=C.UTF-8` | 编译退出码 0 |
| 构建/运行环境 | java.security 配置缺失（解包 JDK） | `InternalError: Error loading java.security file` | JVM 无法启动 | 从 dist 附带的 `etc` 目录复制补齐 `$JAVA_HOME/conf/security/java.security` | `java -version` 正常 |
| 程序运行时 | 标准输出流不可用/写入失败 | 打印时抛 `RuntimeException` | 程序崩溃无提示 | `main` 内 try-catch 包裹输出，降级 `System.err` 并 `System.exit(1)` | 异常路径返回非零、stderr 有摘要 |
| 程序运行时 | 输出未落盘（缓冲） | 异常退出导致缓冲丢失 | 输出缺失 | `println` 后 `System.out.flush()` | 输出完整落盘 |
| 验证/CI | 编译失败 | javac 非 0、有错误输出 | 构建红灯 | 回看错误列表，做最小修复后重跑，不改弱检查 | javac 退出 0 |
| 验证/CI | 运行退出码非 0 | 主类无法加载或运行异常 | 无法验收 | 核对主类名 `HelloWorld` 与文件名一致，确认 classpath | java 退出 0 |
| 验证/CI | 输出不符 | 多余空格/换行或缺失标点 | 需求不满足 | `od -c` 字节级比对目标 13 字节 | `Hello, World!` 精确匹配 |

## 3. 源码内置兜底（HelloWorld.java）

- try-catch 包裹问候输出；正常路径仍为 `System.out.println("Hello, World!")`。
- 捕获 `RuntimeException`，降级 `System.err` 输出摘要并 `System.exit(1)`。
- `System.out.flush()` 确保缓冲落盘。

## 4. 兜底触发后的恢复步骤

1. 记录异常类别与原始 stderr。
2. 按第 2 节矩阵定位对应行段的兜底动作。
3. 执行恢复命令，核对「验证口径」列。
4. 重跑验收：`javac -encoding UTF-8 HelloWorld.java && java HelloWorld`，`od -c` 校验输出为 `Hello, World!`。
5. 清理编译产物（`HelloWorld.class`、`javac.*.args`），仓库只保留源码。

## 5. 未覆盖/剩余风险

- 极端环境（无网络、无 curl、无 dpkg）下 JDK 无法获取，需人工预置 JDK。
- 仅供「hello world」单文件程序的兜底，不覆盖多模块构建系统（Maven/Gradle）。
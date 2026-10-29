# Hello World 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在仓库根目录提供一个可编译、可运行、输出 `Hello, World!` 的 Java 程序。

**Architecture:** 单个无依赖的 Java 源文件 `HelloWorld.java`，入口方法 `main` 直接向标准输出打印问候语。

**Tech Stack:** Java SE（JDK 8 及以上，无第三方依赖、无构建工具）

## Global Constraints

- 输出文本必须精确等于 `Hello, World!`（含逗号与感叹号，无多余空格）。
- 源文件名为 `HelloWorld.java`，公共类名必须为 `HelloWorld`（与文件名一致）。
- 仅使用标准 JDK，不引入 Maven/Gradle/JUnit 等依赖。
- 当前主机未安装 JDK（`javac`、`java` 均不存在）：执行编译/运行步骤前必须先安装 JDK 8+，否则无法验证。
- 本轮工作流禁用 Git 写操作，不执行 `git commit`/`git push`；变更由协作流程跟踪。

---

### Task 1: HelloWorld 程序

**Files:**
- Create: `HelloWorld.java`（仓库根目录；当前已存在且内容正确，若无则创建）

**Interfaces:**
- Consumes: 无（不依赖其他任务）
- Produces: `HelloWorld.main(String[])` — 无返回值；运行 `java HelloWorld` 后标准输出为 `Hello, World!`

- [ ] **Step 1: 确认/写源码**

（若文件不存在则创建 `HelloWorld.java`，内容如下：）

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

- [ ] **Step 2: 编译验证语法**

Run: `javac HelloWorld.java`
Expected: 命令退出码 0，在同目录生成 `HelloWorld.class`，无任何错误输出。

- [ ] **Step 3: 运行并校验输出**

Run: `java HelloWorld`
Expected: 标准输出精确为 `Hello, World!`，退出码 0。

- [ ] **Step 4: 清理编译产物（可选）**

Run: `rm -f HelloWorld.class`
Expected: 仅保留源文件 `HelloWorld.java`（编译产物不入库）。

---

## 自检

- 规格覆盖：需求「写个 hello world」由 Task 1 完成（创建/校验源文件 + 编译 + 运行 + 输出校验）。
- 占位符扫描：无 TBD/TODO/待补充。
- 缺失条件：主机未安装 JDK，编译/运行步骤需先安装 JDK 8+；已在上方 Global Constraints 声明。

## 异常兜底方案

> 详细矩阵见独立文档：`docs/superpowers/plans/2026-10-29-hello-world-fallback.md`。

- **构建/运行环境异常**：JDK 缺失时优先使用免安装解包方式（`dpkg -x` 到 `/tmp`，不解包到系统目录），避免依赖受沙箱限制的系统包管理器；下载源不可达时按 aliyun 镜像 → Adoptium API → Ubuntu 官方池顺序切换。
- **程序运行时异常**：`main` 内以 try-catch 包裹输出，正常路径仍精确打印 `Hello, World!`；`System.out` 不可用时降级到 `System.err` 摘要并 `System.exit(1)`，`flush()` 确保缓冲落盘。
- **验证/CI 失败场景**：编译显式 `-encoding UTF-8 -J-Dfile.encoding=UTF-8` 并设 `LANG=C.UTF-8` 规避包含特殊字符路径导致的 `InvalidPathException`；运行退出码非 0 时核对主类名 `HelloWorld` 与文件名一致；输出用 `od -c` 做字节级比对（目标 13 字节）。

## 备注

- 仓库根目录 `HelloWorld.java` 当前已存在且内容与需求一致，Task 1 在已确认现状下无需改动源码，仅需完成编译/运行验证；若后续需从零重建，按 Step 1 创建。
- 上传附件《产品空间迭代测分-测试用例汇总2.md》与「写个 hello world」需求无关，本计划不采纳。
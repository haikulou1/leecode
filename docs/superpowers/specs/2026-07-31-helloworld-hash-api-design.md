# 设计文档：HelloWorld 与 哈希算法 接口

- 日期：2026-07-31
- 阶段：需求澄清 / 系分设计
- 技能：/brainstorming
- 状态：已澄清（自主决策，全流水线模式）

## 1. 需求描述

> 用 java 分别写 2 个接口 helloworld、哈希算法

## 2. 仓库现状（事实依据）

- 多模块 Java 学习/演示仓，Maven 构建，无聚合 POM（各模块独立 `pom.xml`）。
- 包名统一前缀 `cn.wy.*`；Java 编译版本 `1.7`。
- 依赖：junit 4.11 / testng / servlet-api 2.5；无 Spring Boot，无 Web 框架。
- 现有代码均为纯 Java 类（设计模式、NIO、ZK 锁等），无 HTTP 接口先例。
- 目标模块：`designmodel`（已有 `src/main/java/cn/wy/`，包结构成熟）。

## 3. 需求澄清（歧义点与自主决策）

> 全流水线模式：禁止阻塞等待人工，按「上下文事实 > 最小改动/符合架构惯例 > 行业最佳实践」自动决策。

| # | 歧义点 | 备选 | 决策 | 依据 |
|---|--------|------|------|------|
| Q1 | "接口"含义 | A. Java `interface`<br>B. HTTP REST 端点<br>C. 普通 Service 方法 | **A** | 仓库无 Web 框架，引入 Web 栈改动过大且偏离学习仓定位；"分别写 2 个接口"最契合 Java `interface` 语义 |
| Q2 | `helloworld` 形态 | 固定串 / 带参数 | 返回 `"Hello, World!"`，并保留 `sayHello(String name)` 重载 | 体现接口多态，零成本 |
| Q3 | "哈希算法"范围 | A. 手写哈希<br>B. 封装 JDK `MessageDigest` | **B** | 现有惯例零第三方依赖，JDK 自带 MD5/SHA-1/SHA-256 足够 |
| Q4 | 模块归属 | 新建模块 / 放 `designmodel` | 放 `designmodel` | 已有成熟包结构，避免新增聚合 POM 连锁改动 |
| Q5 | Java 版本 | 保持 1.7 / 升级 | 保持 1.7 | 与模块 `maven.compiler.source` 一致 |

## 4. 方案设计

### 4.1 接口一：HelloWorld

```java
package cn.wy.helloworld;

public interface HelloWorld {
    String sayHello();                 // 返回 "Hello, World!"
    String sayHello(String name);      // 返回 "Hello, <name>!"
}
```

实现 `HelloWorldImpl`：实现上述两方法。

### 4.2 接口二：HashAlgorithm

```java
package cn.wy.hash;

public interface HashAlgorithm {
    String hash(String input, String algorithm);   // algorithm: MD5/SHA-1/SHA-256
    String md5(String input);                       // 便捷方法
    String sha256(String input);                    // 便捷方法
}
```

实现 `HashAlgorithmImpl`：基于 `java.security.MessageDigest`，输出小写十六进制串。

### 4.3 验证策略（TDD）

- `HelloWorldTest`：断言 `sayHello()` 返回 `"Hello, World!"`；`sayHello("Java")` 返回 `"Hello, Java!"`。
- `HashAlgorithmTest`：对已知输入断言 MD5/SHA-256 与标准向量一致（如 `"abc"` 的 MD5 = `900150983cd24fb0d6963f7d28e17f72`）。

## 5. 涉及文件清单（实施阶段写入，本阶段不写）

| 文件 | 类型 | 说明 |
|------|------|------|
| `designmodel/src/main/java/cn/wy/helloworld/HelloWorld.java` | 接口 | 新增 |
| `designmodel/src/main/java/cn/wy/helloworld/HelloWorldImpl.java` | 实现 | 新增 |
| `designmodel/src/main/java/cn/wy/hash/HashAlgorithm.java` | 接口 | 新增 |
| `designmodel/src/main/java/cn/wy/hash/HashAlgorithmImpl.java` | 实现 | 新增 |
| `designmodel/src/test/java/cn/wy/helloworld/HelloWorldTest.java` | 测试 | 新增 |
| `designmodel/src/test/java/cn/wy/hash/HashAlgorithmTest.java` | 测试 | 新增 |

不改 `pom.xml`，不改动现有类。

## 6. 影响范围与风险

- 影响面：仅 `designmodel` 模块新增包，无破坏性改动。
- 风险：极低；纯 JDK API，无外部依赖，无并发/IO 副作用。

## 7. 待确认（如后续人工介入）

- 若实际期望为 HTTP REST 接口，则需引入 Spring Boot 并切换为 Controller；本设计按现有架构默认 Java `interface`。

# Java 接口 Demo（原生 HttpServer 实现）

> 基于 JDK 自带 `com.sun.net.httpserver.HttpServer`，零外部依赖。
> 设计文档见 [DESIGN.md](DESIGN.md)。

## 编译与运行

```bash
# 编译（输出到 out/）
javac -d out src/*.java src/handler/*.java src/util/*.java

# 启动服务（默认端口 8080）
java -cp out Main
# 自定义端口
java -cp out Main 9090

# 运行单元测试（需含 test/ 目录一并编译）
javac -d out src/util/*.java test/*.java
java -ea -cp out HashUtilTest
java -ea -cp out SortUtilTest
```

## 接口列表

| # | 路径 | 方法 | 入参 | 出参 | 说明 |
|---|------|------|------|------|------|
| 1 | `/helloworld` | GET | 无 | `Hello, World!` | 经典入门接口 |
| 2 | `/hash` | GET | `algorithm`, `input` | 十六进制摘要 | 哈希算法接口 |
| 3 | `/sort` | GET | `algorithm`, `input` | 排序后数组 | 排序算法接口（候选A） |

### 参数说明

- `/hash`
  - `algorithm`：`md5` / `sha1` / `sha256` / `sha512`
  - `input`：原文
- `/sort`
  - `algorithm`：`bubble` / `quicksort` / `selection`
  - `input`：逗号分隔整数，如 `3,1,2`

## 示例

```bash
curl http://localhost:8080/helloworld
# Hello, World!

curl "http://localhost:8080/hash?algorithm=sha256&input=abc"
# ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad

curl "http://localhost:8080/sort?algorithm=quicksort&input=3,1,2"
# 1,2,3
```

## 错误码

| 状态 | 触发条件 |
|------|----------|
| 400 | 参数缺失 / 算法不支持 / 输入格式错误 |
| 404 | 未知路径 |
| 405 | 非 GET 请求 |

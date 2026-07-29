# HelloWorld 后端服务

> 跨仓 helloworld 示例的后端，与前端 [haikulou1.github.io](https://github.com/haikulou1/haikulou1.github.io) 的 `hello.html` 对接。

## 接口契约

| 项 | 值 |
|:---|:---|
| 方法 | `GET` |
| 路径 | `/hello` |
| 端口 | `8080` |
| 完整 URL | `http://localhost:8080/hello` |
| 响应头 | `Content-Type: application/json; charset=utf-8` |
| 跨域 | `Access-Control-Allow-Origin: *`（兼容 GitHub Pages） |
| 响应体 | `{"message":"Hello, World!","source":"leecode"}` |

## 运行方式

```sh
cd leecode
javac src/l_hello_world/HelloWorld.java -d out/
java -cp out HelloWorld
```

启动后控制台输出：

```
HelloWorld 服务已启动，监听端口：8080
接口地址：http://localhost:8080/hello
```

## 前端对接

前端仓库 `haikulou1.github.io` 根目录 `hello.html` 通过 `fetch` 调用本接口：

- 成功：展示后端返回的 `message`
- 失败（后端未启动）：静态降级展示 `Hello, World! (前端静态兜底)`

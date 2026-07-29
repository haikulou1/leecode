# login-web 登录页

前端登录页，与 `haikulou1.github.io` 仓库的 `login-server` 后端配套。

## 文件说明

| 文件 | 说明 |
|------|------|
| `login.html` | 登录页结构 |
| `login.js` | 登录请求逻辑，调用 `POST /api/login` |
| `style.css` | 样式 |

## 接口契约

- 请求：`POST http://localhost:8080/api/login`
- Content-Type：`application/json`
- 请求体：
  ```json
  { "username": "admin", "password": "123456" }
  ```
- 响应体：
  ```json
  { "code": 200, "message": "success", "data": { "token": "xxx" } }
  ```

## 运行方式

1. 启动后端 `login-server`（见 haikulou1 仓库 `login-server/README.md`）。
2. 浏览器直接打开 `login.html`，或用任意静态服务器托管本目录：
   ```bash
   cd login-web && python3 -m http.server 3000
   ```
3. 演示账号：`admin` / `123456`。

## 配置

如后端部署到其它地址，修改 `login.js` 中的 `API_BASE`。

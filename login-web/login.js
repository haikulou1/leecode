/**
 * 登录页前端逻辑。
 *
 * 调用后端 POST /api/login 接口：
 *   请求体: { "username": string, "password": string }
 *   响应体: { "code": 200, "message": "success", "data": { "token": string } }
 */
(function () {
    'use strict';

    // 后端服务地址；如部署到其它域名请修改此项
    var API_BASE = 'http://localhost:8080';

    var form = document.getElementById('loginForm');
    var usernameInput = document.getElementById('username');
    var passwordInput = document.getElementById('password');
    var loginBtn = document.getElementById('loginBtn');
    var messageEl = document.getElementById('message');

    /**
     * 显示提示信息。
     * @param {string} text 文本
     * @param {string} type success | error
     */
    function showMessage(text, type) {
        messageEl.textContent = text;
        messageEl.className = 'login-message ' + (type || '');
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();

        var username = usernameInput.value.trim();
        var password = passwordInput.value;

        if (!username) {
            showMessage('请输入用户名', 'error');
            return;
        }
        if (!password) {
            showMessage('请输入密码', 'error');
            return;
        }

        loginBtn.disabled = true;
        loginBtn.textContent = '登录中...';
        showMessage('', '');

        fetch(API_BASE + '/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: username, password: password })
        })
            .then(function (response) {
                return response.json().then(function (body) {
                    return { ok: response.ok, body: body };
                });
            })
            .then(function (result) {
                var body = result.body || {};
                if (result.ok && body.code === 200 && body.data && body.data.token) {
                    showMessage('登录成功', 'success');
                    // 实际项目中可跳转：window.location.href = '/home.html';
                    console.log('token:', body.data.token);
                } else {
                    showMessage(body.message || '登录失败', 'error');
                }
            })
            .catch(function (error) {
                console.error('登录请求异常:', error);
                showMessage('网络异常，请稍后重试', 'error');
            })
            .finally(function () {
                loginBtn.disabled = false;
                loginBtn.textContent = '登录';
            });
    });
})();

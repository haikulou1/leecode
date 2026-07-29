/**
 * 登录页面前端逻辑
 *
 * 职责：表单校验、请求发送、Token 存储、页面跳转、登出
 */

// 后端服务地址（开发期），生产环境应通过配置注入
const API_BASE = 'http://localhost:8080';

// 错误码常量，与后端 ApiResponse 保持一致
const CODE_SUCCESS = 0;
const CODE_AUTH_FAILED = 40101;

// Token 在 localStorage 中的存储键
const TOKEN_KEY = 'auth_token';

/**
 * DOM 元素引用
 */
const loginForm = document.getElementById('loginForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const errorMsgDiv = document.getElementById('errorMsg');
const loginBtn = document.getElementById('loginBtn');
const btnText = loginBtn.querySelector('.btn-text');
const btnLoading = loginBtn.querySelector('.btn-loading');

/**
 * 表单提交事件
 */
loginForm.addEventListener('submit', function (event) {
    event.preventDefault();
    handleLogin();
});

/**
 * 处理登录流程：获取输入 → 校验 → 发送请求
 */
function handleLogin() {
    var username = usernameInput.value.trim();
    var password = passwordInput.value;

    var validationError = validateInput(username, password);
    if (validationError) {
        showError(validationError);
        return;
    }

    clearError();
    setLoading(true);
    sendLoginRequest(username, password);
}

/**
 * 前端输入校验
 *
 * @param {string} username 用户名
 * @param {string} password 密码
 * @returns {string|null} 错误描述，null 表示通过
 */
function validateInput(username, password) {
    if (!username || !password) {
        return '用户名和密码不能为空';
    }
    if (username.length < 3 || username.length > 32) {
        return '用户名长度需为3-32位';
    }
    if (password.length < 6 || password.length > 64) {
        return '密码长度需为6-64位';
    }
    return null;
}

/**
 * 发送登录请求到后端
 *
 * @param {string} username 用户名
 * @param {string} password 密码
 */
function sendLoginRequest(username, password) {
    fetch(API_BASE + '/api/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
    .then(function (response) {
        // 无论 HTTP 状态码，统一解析 JSON 响应体
        return response.json().then(function (data) {
            return { status: response.status, body: data };
        });
    })
    .then(function (result) {
        if (result.body.code === CODE_SUCCESS) {
            onLoginSuccess(result.body.data);
        } else {
            onLoginError(result.body.message);
        }
    })
    .catch(function (error) {
        onLoginError('网络异常，请稍后重试');
        console.error('登录请求失败:', error);
    })
    .finally(function () {
        setLoading(false);
    });
}

/**
 * 登录成功回调：存储 Token，提示成功
 *
 * @param {Object} data 响应数据 {token, expiresIn, user}
 */
function onLoginSuccess(data) {
    if (data && data.token) {
        localStorage.setItem(TOKEN_KEY, data.token);
    }
    var user = data && data.user ? data.user : {};
    var nickname = user.nickname || user.username || '用户';
    // 使用 textContent 而非 innerHTML，防止 XSS
    showError('登录成功，欢迎 ' + nickname);
    errorMsgDiv.style.color = '#27ae60';
}

/**
 * 登录失败回调：展示错误信息
 *
 * @param {string} message 错误描述
 */
function onLoginError(message) {
    showError(message || '登录失败');
}

/**
 * 从 localStorage 读取 Token
 *
 * @returns {string|null} Token 字符串
 */
function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

/**
 * 登出：调用后端登出接口，清除本地 Token
 */
function logout() {
    var token = getToken();
    if (!token) {
        return;
    }
    fetch(API_BASE + '/api/logout', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
    .then(function (response) {
        return response.json();
    })
    .then(function (data) {
        if (data.code === CODE_SUCCESS) {
            localStorage.removeItem(TOKEN_KEY);
            showError('已登出');
            errorMsgDiv.style.color = '#555';
        } else {
            showError(data.message || '登出失败');
        }
    })
    .catch(function (error) {
        // 网络异常时仍清除本地 Token（无状态模式下客户端清除即可）
        localStorage.removeItem(TOKEN_KEY);
        console.error('登出请求失败:', error);
    });
}

/**
 * 设置加载状态
 *
 * @param {boolean} loading 是否加载中
 */
function setLoading(loading) {
    loginBtn.disabled = loading;
    if (loading) {
        btnText.setAttribute('hidden', '');
        btnLoading.removeAttribute('hidden');
    } else {
        btnText.removeAttribute('hidden');
        btnLoading.setAttribute('hidden', '');
    }
}

/**
 * 展示错误信息（使用 textContent 防 XSS）
 *
 * @param {string} message 错误描述
 */
function showError(message) {
    errorMsgDiv.textContent = message;
}

/**
 * 清除错误信息
 */
function clearError() {
    errorMsgDiv.textContent = '';
    errorMsgDiv.style.color = '';
}

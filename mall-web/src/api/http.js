import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080/api',
  timeout: 10000
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body.code === 0) return body.data
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(new Error(body.message))
  },
  (err) => {
    ElMessage.error(err.response?.data?.message || '网络错误')
    return Promise.reject(err)
  }
)

export default http

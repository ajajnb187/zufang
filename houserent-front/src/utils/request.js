import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8888',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status } = error.response
      
      // 只处理 401 未授权的情况
      if (status === 401) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_user')
        window.location.href = '/login'
        ElMessage.error('登录已过期，请重新登录')
      }
    }
    
    // 将错误传递给调用方处理
    return Promise.reject(error)
  }
)

export default request

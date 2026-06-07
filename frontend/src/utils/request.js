import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 5000,
  withCredentials: true
})

service.interceptors.request.use(
  config => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        if (user.id) {
          config.headers['X-User-Id'] = user.id
        }
        if (user.username) {
          config.headers['X-Username'] = user.username
        }
        if (user.role) {
          config.headers['X-User-Role'] = user.role
        }
        if (user.className) {
          config.headers['X-User-Class'] = user.className
        }
      } catch (e) {
      }
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res && res.success !== undefined) {
      if (res.success) {
        return res.data
      } else {
        if (!response.config?.skipErrorNotification) {
          ElMessage.error(res.message || '请求失败')
        }
        const err = new Error(res.message || '请求失败')
        err.responseData = res
        return Promise.reject(err)
      }
    }
    return res
  },
  error => {
    let msg = '请求失败，请稍后重试'
    const data = error.response?.data
    if (data && typeof data === 'object') {
      if (data.message) {
        msg = data.message
      } else if (data.error) {
        msg = data.error
      }
    } else if (typeof data === 'string') {
      msg = data
    } else if (error.message && /timeout/i.test(error.message)) {
      msg = '请求超时，请检查网络后重试'
    } else if (error.message && /Network Error/i.test(error.message)) {
      msg = '网络错误，请稍后重试'
    }
    if (msg === 'Invalid credentials') {
      msg = '用户名或密码错误'
    }
    if (!error.config?.skipErrorNotification) {
      ElMessage.error(msg)
    }
    error.userMessage = msg
    return Promise.reject(error)
  }
)

export default service

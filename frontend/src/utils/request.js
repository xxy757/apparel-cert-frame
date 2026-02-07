import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearSession, clearSessionByPath, getActiveUserType, getTokenForPath, getUserTypeByPath, syncSessionForPath } from './auth'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const getCurrentPath = () => {
  if (typeof window !== 'undefined' && window.location) {
    return window.location.pathname || '/'
  }
  return '/'
}

const handleUnauthorized = (requestUrl = '') => {
  const currentPath = getCurrentPath()
  const isLoginRequest = String(requestUrl).includes('/auth/login')
  if (isLoginRequest || currentPath.includes('/login')) {
    return
  }

  const routeType = getUserTypeByPath(currentPath)
  if (routeType) {
    clearSession(routeType)
  } else {
    const activeType = getActiveUserType()
    if (activeType) {
      clearSession(activeType)
    } else {
      clearSessionByPath(currentPath)
    }
  }

  if (!currentPath.includes('/login') && typeof window !== 'undefined') {
    window.location.href = '/login'
  }
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const currentPath = getCurrentPath()
    syncSessionForPath(currentPath)
    const token = getTokenForPath(currentPath)
    if (token) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是 200，说明有错误
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 401: Token 过期或无效
      if (res.code === 401) {
        handleUnauthorized(response.config?.url || '')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理网络错误
    if (error.response) {
      const { status, data } = error.response
      const message = data?.message || '请求失败'
      
      switch (status) {
        case 304:
          // 304 不应该作为错误处理，但如果到这里说明有问题
          console.warn('收到304响应，可能是缓存问题')
          break
        case 400:
          ElMessage.error(message)
          break
        case 401:
          ElMessage.error(message || '认证失败')
          handleUnauthorized(error.config?.url || '')
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(message)
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request

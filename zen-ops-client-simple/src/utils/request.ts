import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { AxiosRequestConfig, AxiosInstance, InternalAxiosRequestConfig } from 'axios'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/platform',
  timeout: 300000 // 5分钟，支持大文件上传
})

// 请求拦截器：注入 token 等
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authentication-Token'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一错误处理
service.interceptors.response.use(
  (response) => {
    // 如果响应头中有新token，更新本地存储
    const newToken = response.headers['token']
    if (newToken) {
      localStorage.setItem('token', newToken)
    }
    const data = response.data
    if (data && data.code !== undefined && data.code !== 0) {
      const msg = data.msg || data.message || '操作失败'
      ElMessage.error(msg)
      if (data.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '/#/login'
      }
      return Promise.reject(new Error(msg))
    }
    return response
  },
  (error) => {
    const msg = error?.response?.data?.msg || error?.response?.data?.message || error?.message || '网络错误'
    ElMessage.error(msg)
    if (error?.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/#/login'
    }
    return Promise.reject(error)
  }
)

// 封装请求方法，自动解包 data
export const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config).then(res => res.data?.data ?? res.data)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config).then(res => res.data?.data ?? res.data)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config).then(res => res.data?.data ?? res.data)
  },
  del<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config).then(res => res.data?.data ?? res.data)
  },
  // 原始 axios 实例，用于上传等特殊场景
  raw: service
}

export default service

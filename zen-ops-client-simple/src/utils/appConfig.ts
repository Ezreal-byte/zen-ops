import { reactive } from 'vue'
import request from './request'

// 应用配置响应式对象
const appConfig = reactive({
  title: 'ZenOps',
  subtitle: '一站式运维平台',
  browserTitle: 'ZenOps一站式运维平台',
  loaded: false
})

// 内存缓存（不使用 localStorage）
let configCache: any = null
let loadingPromise: Promise<void> | null = null

/**
 * 加载应用配置
 * 优先从内存缓存读取，没有则调用接口
 */
export const loadAppConfig = async () => {
  // 如果已经加载过，直接返回
  if (configCache) {
    Object.assign(appConfig, configCache)
    appConfig.loaded = true
    return appConfig
  }

  // 如果正在加载中，等待加载完成
  if (loadingPromise) {
    await loadingPromise
    return appConfig
  }

  // 开始加载
  loadingPromise = (async () => {
    try {
      const res: any = await request.get('/app/config')
      // 处理双层解包：response.data.data
      const data = res.data?.data || res.data || res
      configCache = data
      Object.assign(appConfig, data)
      appConfig.loaded = true

      // 设置浏览器标题
      if (data.browserTitle) {
        document.title = data.browserTitle
      }

      // console.log('应用配置加载成功:', data)
    } catch (error) {
      console.error('加载应用配置失败:', error)
      // 失败时使用默认值
      appConfig.loaded = true
    } finally {
      loadingPromise = null
    }
  })()

  await loadingPromise
  return appConfig
}

/**
 * 获取应用配置
 */
export const getAppConfig = () => {
  return appConfig
}

/**
 * 更新浏览器标题
 */
export const updateBrowserTitle = (title?: string) => {
  if (title) {
    document.title = title
  } else if (appConfig.browserTitle) {
    document.title = appConfig.browserTitle
  }
}

/**
 * 重置配置（清除缓存）
 */
export const resetAppConfig = () => {
  configCache = null
  appConfig.title = 'ZenOps'
  appConfig.subtitle = '一站式运维平台'
  appConfig.browserTitle = 'ZenOps一站式运维平台'
  appConfig.loaded = false
}

export default appConfig

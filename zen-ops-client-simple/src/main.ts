import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import "element-plus/theme-chalk/el-message.css";
import "element-plus/theme-chalk/el-message-box.css";
import "element-plus/theme-chalk/el-loading.css";
import 'splitpanes/dist/splitpanes.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { loadAppConfig } from './utils/appConfig'

const app = createApp(App)

// 全局注册 Element Plus 图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)

// 初始化应用配置（设置浏览器标题）
loadAppConfig()

app.mount('#app')

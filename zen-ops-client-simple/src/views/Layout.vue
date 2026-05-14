<script setup lang="ts">
import {ref, onMounted} from "vue";
import VHeader from './Header.vue'
import router from '../router'
import { request } from '../utils/request'

const menus = ref<any[]>([])
// 需要缓存的组件名列表（注意：keep-alive.include 匹配的是组件名，
// 需要路由 name 与组件 defineOptions({ name }) 保持一致）
const cachedViews = ref<string[]>([])

const registerDynamicRoutes = (menuList: any[]) => {
  menuList.forEach(menu => {
    if (menu.url && menu.nodeType !== '外链' && menu.component) {
      try {
        router.addRoute({
          path: menu.url,
          name: menu.name || menu.url,
          component: () => import(`../${menu.component}.vue`)
        })
      } catch (e) {
        console.warn('路由注册失败', menu.url)
      }
    }
    if (menu.children && menu.children.length) {
      registerDynamicRoutes(menu.children)
    }
  })
}

// 监听路由变化，自动将需要缓存的页面记录下来
// 要求：路由 name 必须与组件名一致才能生效
router.afterEach((to) => {
  if (to.meta && to.meta.keepAlive && to.name) {
    const name = to.name as string
    if (!cachedViews.value.includes(name)) {
      cachedViews.value.push(name)
    }
  }
})

onMounted(async () => {
  try {
    const menuList = await request.get('/auth/menus')
    if (menuList && menuList.length) {
      registerDynamicRoutes(menuList)
      menus.value = menuList
      localStorage.setItem('menus', JSON.stringify(menuList))
    }
  } catch (e) {
    // 使用默认菜单
    const defaultMenus = [
      { pkMenu: 1, name: '首页', url: '/', nodeType: '功能节点', component: 'components/Home.vue' },
      { pkMenu: 2, name: '对象存储', url: '/object-storage', nodeType: '功能节点', component: 'components/fso/index' },
      { pkMenu: 3, name: 'SSH终端', url: '/ssh', nodeType: '功能节点', component: 'components/ssh/index' },
      { pkMenu: 4, name: 'SQL窗口', url: '/sql-window', nodeType: '功能节点', component: 'components/sql-window/index' },
      { pkMenu: 5, name: 'Redis运维', url: '/redis', nodeType: '功能节点', component: 'components/redis/index' },
      { pkMenu: 6, name: '用户管理', url: '/sys-user', nodeType: '功能节点', component: 'views/system/User' },
      { pkMenu: 7, name: '角色管理', url: '/sys-role', nodeType: '功能节点', component: 'views/system/Role' },
      { pkMenu: 8, name: '菜单管理', url: '/sys-menu', nodeType: '功能节点', component: 'views/system/Menu' }
    ]
    menus.value = defaultMenus
    localStorage.setItem('menus', JSON.stringify(defaultMenus))
  }

  // 首次进入Layout时，如果当前路由需要缓存，也加入缓存列表
  const cur = router.currentRoute.value
  if (cur.meta && cur.meta.keepAlive && cur.name) {
    const name = cur.name as string
    if (!cachedViews.value.includes(name)) {
      cachedViews.value.push(name)
    }
  }
})
</script>

<template>
  <div class="layout-wrapper">
    <v-header :menus="menus"/>
    <div class="layout-content">
      <RouterView v-slot="{ Component, route }">
        <keep-alive :include="cachedViews">
          <component :is="Component" />
        </keep-alive>
      </RouterView>
    </div>
  </div>
</template>

<style lang="less" scoped>
  .layout-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;

    .layout-content {
      position: absolute;
      top: 48px;
      left: 0;
      right: 0;
      bottom: 0;
    }
  }
</style>

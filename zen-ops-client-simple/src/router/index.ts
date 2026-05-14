import {createRouter, createWebHashHistory} from 'vue-router'
import Home from '../components/Home.vue'
import ObjectStorage from '../components/fso/index.vue'
import SshTerminal from '../components/ssh/index.vue'
import SqlWindow from '../components/sql-window/index.vue'
import RedisOps from '../components/redis/index.vue'
import Layout from '../views/Layout.vue'
import { request } from '../utils/request'

const router = createRouter({
    history: createWebHashHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: () => import('../views/Login.vue'),
        },
        {
            path: '/',
            component: Layout,
            redirect: '/',
            children: [
                {
                    path: '',
                    name: 'home',
                    component: Home,
                },
                {
                    path: 'object-storage',
                    name: 'ObjectStorage',
                    component: ObjectStorage,
                    meta: { keepAlive: true },
                },
                {
                    path: 'ssh',
                    name: 'SshTerminal',
                    component: SshTerminal,
                    meta: { keepAlive: true },
                },
                {
                    path: 'sql-window',
                    name: 'SqlWindow',
                    component: SqlWindow,
                    meta: { keepAlive: true },
                },
                {
                    path: 'redis',
                    name: 'RedisOps',
                    component: RedisOps,
                    meta: { keepAlive: true },
                },
                {
                    path: 'sys-user',
                    name: 'sysUser',
                    component: () => import('../views/system/User.vue'),
                },
                {
                    path: 'sys-role',
                    name: 'sysRole',
                    component: () => import('../views/system/Role.vue'),
                },
                {
                    path: 'sys-menu',
                    name: 'sysMenu',
                    component: () => import('../views/system/Menu.vue'),
                },
                {
                    path: 'sql-audit-log',
                    name: 'sqlAuditLog',
                    component: () => import('../components/sql-audit-log/index.vue'),
                }
            ]
        }
    ],
})

router.beforeEach(async (to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path === '/login') {
        next()
        return
    }
    if (!token) {
        next('/login')
        return
    }

    // 权限校验：检查目标路由是否在用户菜单权限中
    let menus: any[] = []
    try {
        menus = JSON.parse(localStorage.getItem('menus') || '[]')
    } catch (e) {
        menus = []
    }
    if (!menus.length) {
        try {
            menus = await request.get('/auth/menus')
            localStorage.setItem('menus', JSON.stringify(menus))
        } catch (e) {
            menus = []
        }
    }
    // 权限校验：递归收集所有菜单路径（包括子菜单）
    const collectPaths = (menuList: any[]): string[] => {
        const paths: string[] = []
        menuList.forEach((m: any) => {
            if (m.url || m.path) paths.push(m.url || m.path)
            if (m.children && m.children.length) {
                paths.push(...collectPaths(m.children))
            }
        })
        return paths
    }
    const allowedPaths = new Set(collectPaths(menus))
    allowedPaths.add('/')
    // 允许访问根路径和已授权路径
    if (!allowedPaths.has(to.path)) {
        next('/')
        return
    }
    next()
})

export default router

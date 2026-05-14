<template>
  <header class="app-header">
    <div class="header-left">
      <div class="brand" @click="router.push('/')">
        <div class="brand-logo">
          <img src="/logo.png" :alt="appConfig.title" width="26" height="26" style="border-radius: 4px;" />
        </div>
        <div class="brand-text">
          <span class="brand-name">{{ appConfig.title }}</span>
          <span class="brand-sub">{{ appConfig.subtitle }}</span>
        </div>
      </div>
      <div class="header-divider"></div>
      <!-- 顶栏菜单 -->
      <nav class="header-nav">
        <div
          v-for="item in treeMenus"
          :key="item.pkMenu"
          class="nav-item-wrapper"
          @mouseenter="handleMouseEnter(item.pkMenu)"
          @mouseleave="handleMouseLeave(item.pkMenu)"
        >
          <div
            :class="['nav-item', { active: isActive(item) }]"
            @click="handleMenuClick(item)"
          >
            <span>{{ item.name }}</span>
            <el-icon v-if="hasChildren(item)" class="nav-arrow" :class="{ expanded: expandedKey === item.pkMenu }">
              <ArrowDown />
            </el-icon>
          </div>
          <!-- 子菜单下拉 -->
          <transition name="dropdown">
            <div
              v-if="hasChildren(item) && expandedKey === item.pkMenu"
              class="dropdown-menu"
              @mouseenter="handleMouseEnter(item.pkMenu)"
              @mouseleave="handleMouseLeave(item.pkMenu)"
            >
              <div
                v-for="child in item.children"
                :key="child.pkMenu"
                :class="['dropdown-item', { active: route.path === child.url }]"
                @click="handleMenuClick(child)"
              >
                <span>{{ child.name }}</span>
              </div>
            </div>
          </transition>
        </div>
      </nav>
    </div>
    <div class="header-right">
      <el-popover placement="bottom-end" :width="200" trigger="hover">
        <template #reference>
          <div class="user-avatar">
            <el-avatar v-if="avatarUrl" :size="30" :src="avatarUrl" />
            <el-avatar v-else :size="30" :icon="UserIcon" />
          </div>
        </template>
        <div class="user-popover">
          <div class="user-info">
            <div class="user-name">{{ userInfo.name || userInfo.userName }}</div>
            <div class="user-role">{{ userInfo.userName }}</div>
          </div>
          <div class="user-actions">
            <div class="action-item" @click="openPwdDialog">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </div>
            <div class="action-item" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </div>
          </div>
        </div>
      </el-popover>
    </div>
  </header>

  <el-dialog v-model="pwdDialogVisible" title="修改密码" width="400px" :close-on-click-modal="false">
    <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="90px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleChangePassword">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import router from '@/router'
import { useRoute } from 'vue-router'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'
import { User as UserIcon, Lock, SwitchButton, ArrowDown } from '@element-plus/icons-vue'
import { loadAppConfig, getAppConfig } from '@/utils/appConfig'

interface MenuItem {
  pkMenu: number
  pkParent?: number
  name: string
  icon?: string
  url?: string
  nodeType?: string
  component?: string
  children?: MenuItem[]
}

const props = defineProps<{
  menus: MenuItem[]
}>()

// 应用配置
const appConfig = getAppConfig()

// 组件挂载时加载配置
onMounted(async () => {
  await loadAppConfig()
})

const route = useRoute()
const expandedKey = ref<number | null>(null)
const userInfo = ref<any>({})
const avatarUrl = ref('')

const pwdDialogVisible = ref(false)
const pwdFormRef = ref<any>(null)
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 构建树形菜单（父菜单不需要 component，外链保留）
const treeMenus = computed(() => {
  const buildTree = (menus: MenuItem[]): MenuItem[] => {
    return menus
      .filter(m => m.url || (m.children && m.children.length > 0))
      .map(m => ({
        ...m,
        children: m.children ? buildTree(m.children) : undefined
      }))
  }
  return buildTree(props.menus)
})

const hasChildren = (item: MenuItem) => {
  return item.children && item.children.length > 0
}

const isActive = (item: MenuItem) => {
  if (route.path === item.url) return true
  if (item.children) {
    return item.children.some(child => route.path === child.url)
  }
  return false
}

const handleMouseEnter = (pkMenu: number) => {
  expandedKey.value = pkMenu
}

const handleMouseLeave = (pkMenu: number) => {
  expandedKey.value = null
}

const handleMenuClick = (item: MenuItem) => {
  if (!item.url) return
  if (item.nodeType === 'LINK' || item.nodeType === '外链' || item.url.startsWith('http')) {
    window.open(item.url, '_blank')
    return
  }
  router.push(item.url)
}

const validateConfirmPwd = (rule: any, value: string, callback: any) => {
  if (value !== pwdForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const fetchUserInfo = async () => {
  try {
    const data = await request.get('/auth/getUserInfo')
    userInfo.value = data
  } catch (e) {
    // ignore
  }
}

const fetchAvatar = async () => {
  try {
    const res = await request.raw.get('/auth/header', { responseType: 'blob', showLoading: false })
    if (res.data && res.data.size > 0) {
      avatarUrl.value = URL.createObjectURL(res.data)
    }
  } catch (e) {
    // ignore
  }
}

fetchUserInfo()
fetchAvatar()

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('menus')
  router.push('/login')
}

const openPwdDialog = () => {
  pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  pwdDialogVisible.value = true
}

const handleChangePassword = async () => {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await request.post('/sys/user/change-password', {
    oldPassword: pwdForm.value.oldPassword,
    newPassword: pwdForm.value.newPassword
  })
  ElMessage.success('密码修改成功，请重新登录')
  pwdDialogVisible.value = false
  setTimeout(() => {
    localStorage.removeItem('token')
    router.push('/login')
  }, 1000)
}
</script>

<style lang="less" scoped>
.app-header {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #1e2a3a 0%, #2c3e50 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  height: 100%;
}

.header-right {
  display: flex;
  align-items: center;
  height: 100%;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  transition: background 0.2s;
}

.user-avatar:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-popover {
  .user-info {
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;
    margin-bottom: 8px;

    .user-name {
      font-size: 15px;
      font-weight: 600;
      color: #333;
    }

    .user-role {
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }

  .user-actions {
    .action-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 4px;
      cursor: pointer;
      border-radius: 4px;
      font-size: 13px;
      color: #666;
      transition: all 0.2s;

      &:hover {
        background: #f5f7fa;
        color: #409eff;
      }
    }
  }
}

/* Brand */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 0 4px;
  flex-shrink: 0;
}

.brand-logo {
  display: flex;
  align-items: center;
}

.brand-text {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}

.brand-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 1px;
}

/* Divider */
.header-divider {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.15);
  margin: 0 16px;
  flex-shrink: 0;
}

/* Nav */
.header-nav {
  display: flex;
  align-items: center;
  height: 100%;
}

.nav-item-wrapper {
  position: relative;
  height: 100%;
}

.nav-item {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 4px;
  white-space: nowrap;
  gap: 4px;

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.08);
  }

  &.active {
    color: #fff;
    background: rgba(255, 255, 255, 0.1);

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 20px;
      height: 2px;
      background: #6dd5fa;
      border-radius: 1px;
    }
  }
}

.nav-arrow {
  font-size: 12px;
  transition: transform 0.2s;

  &.expanded {
    transform: rotate(180deg);
  }
}

/* 下拉菜单 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  min-width: 140px;
  background: linear-gradient(135deg, #1e2a3a 0%, #2c3e50 100%);
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  padding: 4px 0;
  z-index: 1001;
  margin-top: 4px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 0 16px;
  height: 36px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    color: #fff;
  }

  &.active {
    color: #6dd5fa;
    background: rgba(109, 213, 250, 0.1);
  }
}

/* 下拉动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-4px);
}

@media (max-width: 768px) {
  .app-header {
    padding: 0 10px;
  }

  .brand-sub {
    display: none;
  }

  .header-divider {
    margin: 0 10px;
  }

  .nav-item {
    padding: 0 10px;
    font-size: 12px;
  }
}
</style>

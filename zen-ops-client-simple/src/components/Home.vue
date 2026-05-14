<template>
  <div class="home-page">
    <!-- 用户欢迎卡片 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <div class="user-info">
          <el-avatar :size="64" :src="avatarUrl">
            {{ avatarText }}
          </el-avatar>
          <div class="user-meta">
            <h2>{{ greeting }}，{{ displayName }}</h2>
            <div class="user-tags">
              <el-tag
                v-for="role in userInfo.roles"
                :key="role"
                size="small"
                effect="plain"
                type="primary"
              >
                {{ role }}
              </el-tag>
              <el-tag v-if="!userInfo.roles?.length" size="small" effect="plain" type="info">
                普通用户
              </el-tag>
            </div>
          </div>
        </div>
        <div class="datetime">
          <div class="time">{{ currentTime }}</div>
          <div class="date">{{ currentDate }}</div>
        </div>
      </div>
    </el-card>

    <!-- 快捷入口 -->
    <template v-if="quickLinks.length">
      <div class="section-title">快捷入口</div>
      <div class="quick-access">
        <el-card
          v-for="item in quickLinks"
          :key="item.path"
          class="access-card"
          shadow="hover"
          @click="goTo(item.path)"
        >
          <div class="access-icon" :style="{ background: item.bgColor }">
            <el-icon :size="28" color="#fff">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="access-title">{{ item.title }}</div>
          <div class="access-desc">{{ item.desc }}</div>
        </el-card>
      </div>
    </template>

    <!-- 底部两栏 -->
    <div class="bottom-section">
      <el-card class="stat-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><TrendCharts /></el-icon>
            <span>资源概览</span>
          </div>
        </template>
        <div class="stat-grid">
          <div class="stat-item" v-for="s in stats" :key="s.label">
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="notice-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Bell /></el-icon>
            <span>系统公告</span>
          </div>
        </template>
        <div class="notice-list">
          <div class="notice-item" v-for="(n, idx) in notices" :key="idx">
            <div class="notice-dot" :class="{ new: idx < 2 }"></div>
            <div class="notice-text">{{ n }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request'
import {
  DataLine,
  Monitor,
  FolderOpened,
  User,
  Setting,
  Collection,
  TrendCharts,
  Bell
} from '@element-plus/icons-vue'

// 用户信息
const userInfo = ref<any>({})
const avatarUrl = ref('')

const loadUserInfo = async () => {
  try {
    const res = await request.get('/auth/getUserInfo')
    if (res) userInfo.value = res
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

const fetchAvatar = async () => {
  try {
    const res = await request.raw.get('/auth/header', { responseType: 'blob', showLoading: false })
    if (res.data && res.data.size > 0) {
      avatarUrl.value = URL.createObjectURL(res.data)
    }
  } catch (e) {
    // 无头像时使用文字头像
  }
}

const displayName = computed(() => {
  return userInfo.value.name || userInfo.value.userName || '用户'
})

const avatarText = computed(() => {
  const name = userInfo.value.name || userInfo.value.userName || 'U'
  return name.charAt(0).toUpperCase()
})

// 时间问候语
const currentTime = ref('')
const currentDate = ref('')
const greeting = ref('')
let timer: number | null = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
  const hour = now.getHours()
  if (hour < 6) greeting.value = '夜深了'
  else if (hour < 9) greeting.value = '早上好'
  else if (hour < 12) greeting.value = '上午好'
  else if (hour < 14) greeting.value = '中午好'
  else if (hour < 18) greeting.value = '下午好'
  else greeting.value = '晚上好'
}

// 菜单权限
const allowedPaths = ref<Set<string>>(new Set())

const loadAllowedPaths = () => {
  try {
    const menus = JSON.parse(localStorage.getItem('menus') || '[]')
    const paths = new Set<string>()
    const collectPaths = (list: any[]) => {
      list.forEach((m: any) => {
        if (m.url) paths.add(m.url)
        if (m.children?.length) collectPaths(m.children)
      })
    }
    collectPaths(menus)
    paths.add('/')
    allowedPaths.value = paths
  } catch (e) {
    allowedPaths.value = new Set()
  }
}

// 快捷入口
const router = useRouter()
const allQuickLinks = [
  {
    title: 'SQL 窗口',
    desc: '在线执行 SQL',
    icon: DataLine,
    path: '/sql-window',
    bgColor: '#409eff'
  },
  {
    title: 'SSH 终端',
    desc: '远程服务器管理',
    icon: Monitor,
    path: '/ssh',
    bgColor: '#67c23a'
  },
  {
    title: '对象存储',
    desc: 'MinIO / OSS 管理',
    icon: FolderOpened,
    path: '/object-storage',
    bgColor: '#e6a23c'
  },
  {
    title: '用户管理',
    desc: '系统用户与权限',
    icon: User,
    path: '/sys-user',
    bgColor: '#f56c6c'
  },
  {
    title: '角色管理',
    desc: '角色权限配置',
    icon: Setting,
    path: '/sys-role',
    bgColor: '#9254de'
  },
  {
    title: '菜单管理',
    desc: '菜单权限配置',
    icon: Collection,
    path: '/sys-menu',
    bgColor: '#909399'
  }
]

const quickLinks = computed(() => {
  return allQuickLinks.filter(item => allowedPaths.value.has(item.path))
})

const goTo = (path: string) => {
  router.push(path)
}

// 统计数据（占位，后续可接真实接口）
const stats = [
  { label: '在线服务器', value: 3 },
  { label: '数据源', value: 2 },
  { label: '系统用户', value: 5 },
  { label: '对象存储', value: 1 }
]

// 公告
const notices = [
  'ZenOps v0.1 版本正式发布，欢迎体验',
  'SSH 终端支持多标签页会话',
  '对象存储模块支持 MinIO 与阿里云 OSS',
  '定时任务调度后端引擎已就绪'
]

onMounted(() => {
  loadUserInfo()
  fetchAvatar()
  loadAllowedPaths()
  updateTime()
  timer = window.setInterval(updateTime, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="less">
.home-page {
  padding: 20px;
  background: #f5f7fa;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;
}

.welcome-card {
  border-radius: 12px;
  margin-bottom: 20px;
  border: none;

  :deep(.el-card__body) {
    padding: 24px;
  }
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-meta {
  h2 {
    margin: 0 0 8px 0;
    font-size: 22px;
    font-weight: 600;
    color: #303133;
  }
}

.user-tags {
  display: flex;
  gap: 8px;
}

.datetime {
  text-align: right;

  .time {
    font-size: 32px;
    font-weight: 700;
    color: #409eff;
    line-height: 1.2;
    font-variant-numeric: tabular-nums;
  }

  .date {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 14px;
  padding-left: 4px;
}

.quick-access {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.access-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;

  :deep(.el-card__body) {
    padding: 20px;
    text-align: center;
  }

  &:hover {
    transform: translateY(-4px);
  }
}

.access-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
}

.access-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.access-desc {
  font-size: 12px;
  color: #909399;
}

.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-card,
.notice-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;

  .el-icon {
    color: #409eff;
  }
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 8px 0;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 10px;

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #409eff;
    margin-bottom: 6px;
  }

  .stat-label {
    font-size: 13px;
    color: #606266;
  }
}

.notice-list {
  padding: 4px 0;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;

  &:last-child {
    border-bottom: none;
  }
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c0c4cc;
  margin-top: 6px;
  flex-shrink: 0;

  &.new {
    background: #f56c6c;
  }
}

.notice-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 1200px) {
  .quick-access {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .datetime {
    text-align: left;
  }

  .quick-access {
    grid-template-columns: repeat(2, 1fr);
  }

  .bottom-section {
    grid-template-columns: 1fr;
  }
}
</style>

<template>
  <div class="login-page">
    <!-- 左侧品牌区域 -->
    <div class="login-left">
      <div class="brand-container">
        <div class="brand-logo">
          <div class="logo-icon">
            <img src="/logo.png" alt="ZenOps" />
          </div>
        </div>
        <h1 class="brand-title">{{ appConfig.title }}</h1>
        <p class="brand-subtitle">{{ appConfig.subtitle }}</p>
        <div class="brand-features">
          <div class="feature-item">
            <img class="feature-icon" :src="sqlIcon" alt="SQL 窗口" />
            <span>SQL 窗口</span>
          </div>
          <div class="feature-item">
            <img class="feature-icon" :src="sshIcon" alt="SSH 终端" />
            <span>SSH 终端</span>
          </div>
          <div class="feature-item">
            <img class="feature-icon" :src="objectStorageIcon" alt="对象存储" />
            <span>对象存储</span>
          </div>
          <div class="feature-item">
            <img class="feature-icon" :src="redisIcon" alt="Redis 控制台" />
            <span>Redis 控制台</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-container">
        <div class="login-header">
          <h2 class="login-title">欢迎登录</h2>
          <p class="login-desc">请输入您的账号信息以继续</p>
        </div>

        <el-form class="login-form" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input
              v-model="form.userName"
              placeholder="用户名 / 邮箱 / 手机"
              size="large"
              :prefix-icon="User"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <p>© 2025 Ezreal-byte. All rights reserved.</p>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { loadAppConfig, getAppConfig } from '@/utils/appConfig'

// 使用 new URL 动态加载图标
const sqlIcon = new URL('/icons/sql.svg', import.meta.url).href
const sshIcon = new URL('/icons/ssh.svg', import.meta.url).href
const objectStorageIcon = new URL('/icons/object-storage.svg', import.meta.url).href
const redisIcon = new URL('/icons/redis2.svg', import.meta.url).href

const router = useRouter()
const loading = ref(false)

const form = reactive({
  userName: '',
  password: ''
})

// 应用配置
const appConfig = getAppConfig()

// 组件挂载时加载配置
onMounted(async () => {
  await loadAppConfig()
})

const handleLogin = async () => {
  if (!form.userName || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res: any = await request.post('/auth/login', {
      userName: form.userName,
      password: form.password
    })
    if (res.token) {
      localStorage.setItem('token', res.token)
      try {
        const menus = await request.get('/auth/menus')
        localStorage.setItem('menus', JSON.stringify(menus))
      } catch (e) {
        // ignore
      }
      ElMessage.success('登录成功')
      router.push('/')
    }
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="less">
.login-page {
  width: 100vw;
  height: 100vh;
  background: #ffffff;
  display: flex;
  position: relative;
  overflow: hidden;
}

// 左侧品牌区域
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
    background-size: 50px 50px;
    animation: moveGrid 20s linear infinite;
  }
}

@keyframes moveGrid {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

.brand-container {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #ffffff;
  max-width: 500px;
}

.brand-logo {
  margin-bottom: 32px;
}

.logo-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  //border: 2px solid rgba(255, 255, 255, 0.3);

  img {
    width: 64px;
    height: 64px;
    border-radius: 8px;
  }
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px 0;
  letter-spacing: 2px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.brand-subtitle {
  font-size: 18px;
  margin: 0 0 48px 0;
  opacity: 0.9;
  font-weight: 300;
}

.brand-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 40px;
}

.feature-item {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: translateY(-4px);
  }

  .feature-icon {
    width: 40px;
    height: 40px;
    object-fit: contain;
  }

  span {
    font-size: 14px;
    font-weight: 500;
    color: #ffffff;
  }
}

// 右侧登录表单
.login-right {
  width: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #ffffff;
  position: relative;
  z-index: 2;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-header {
  margin-bottom: 40px;
}

.login-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  letter-spacing: 1px;
}

.login-desc {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-input__wrapper) {
    padding: 10px 14px;
    border-radius: 8px;
    box-shadow: 0 0 0 1px #d9d9d9 inset;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 0 0 1px #b0b0b0 inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #667eea inset;
    }
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    color: #1a1a1a;

    &::placeholder {
      color: #bfbfbf;
    }
  }

  :deep(.el-input__icon) {
    color: #8c8c8c;
  }
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(102, 126, 234, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.login-footer {
  margin-top: 48px;
  text-align: center;

  p {
    font-size: 12px;
    color: #bfbfbf;
    margin: 0;
  }
}

// 背景装饰
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);

  &.circle-1 {
    width: 400px;
    height: 400px;
    top: -100px;
    right: 200px;
    animation: float1 8s ease-in-out infinite;
  }

  &.circle-2 {
    width: 300px;
    height: 300px;
    bottom: -50px;
    right: 100px;
    animation: float2 10s ease-in-out infinite;
  }

  &.circle-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    right: 50%;
    animation: float3 12s ease-in-out infinite;
  }
}

@keyframes float1 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(-30px, 30px) scale(1.1);
  }
}

@keyframes float2 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(20px, -20px) scale(1.05);
  }
}

@keyframes float3 {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(-20px, -30px) scale(0.95);
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
  }
}
</style>

<template>
  <div class="ssh-container">
    <!-- 顶部工具条 -->
    <div class="ssh-toolbar">
      <div class="toolbar-left">
        <!-- 服务器管理图标 -->
        <div class="manager-icon" @click="managerVisible = true" title="服务器列表">
          <el-icon :size="18"><Monitor /></el-icon>
          <span class="manager-text">服务器</span>
        </div>
        <div class="toolbar-sep"></div>
        <!-- 标签页 -->
        <div class="tab-bar">
          <div
            v-for="tab in tabs"
            :key="tab.id"
            class="tab-item"
            :class="{ active: activeTabId === tab.id }"
            @click="activeTabId = tab.id"
            @contextmenu.prevent="showContextMenu($event, tab)"
          >
            <span class="tab-dot" :class="{ connected: tab.connected }"></span>
            <span class="tab-name">{{ tab.serverName }}</span>
            <el-icon class="tab-close" :size="12" @click.stop="closeTab(tab)"><Close /></el-icon>
          </div>
        </div>
      </div>
      <div class="toolbar-right">
        <!-- 操作按钮已移至终端区域 -->
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="ssh-main" v-if="activeTab">
      <!-- 终端区域顶部：服务器基本信息 -->
      <div class="server-info-bar">
        <div class="info-item info-name">
          <span class="info-value name-text">{{ activeTab.serverName }}</span>
        </div>
        <div class="info-sep"></div>
        <div class="info-item">
          <span class="info-label">IP</span>
          <el-tag size="small" type="info" effect="plain">{{ activeTab.serverData?.ip }}:{{ activeTab.serverData?.portSsh }}</el-tag>
        </div>
        <div class="info-item">
          <span class="info-label">用户</span>
          <span class="info-value">{{ activeTab.serverData?.userName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">登录方式</span>
          <el-tag size="small" :type="activeTab.serverData?.loginTp === '0' ? 'success' : 'warning'" effect="plain">
            {{ activeTab.serverData?.loginTp === '0' ? '密码' : '私钥' }}
          </el-tag>
        </div>
        <div class="info-item" v-if="activeTab.serverData?.initPath">
          <span class="info-label">默认目录</span>
          <span class="info-value mono">{{ activeTab.serverData?.initPath }}</span>

        </div>
        <!-- 标签显示 -->
        <div class="info-item" v-if="activeTab.serverData?.tags">
          <div class="info-tags">
            <el-tag
              v-for="(tag, index) in parseSshTags(activeTab.serverData.tags)"
              :key="index"
              :type="tag.type"
              size="small"
            >
              {{ tag.label }}
            </el-tag>
          </div>
        </div>
        <div class="info-item">
          <div class="icon-btn-group">
            <div class="icon-btn" @click="openSftpDialog(activeTab)" title="文件管理">
              <el-icon><FolderOpened /></el-icon>
            </div>
            <div class="icon-btn" @click="toggleSidebarForTab(activeTab)" :title="activeTab.showSidebar !== false ? '隐藏文件管理' : '显示文件管理'">
              <el-icon><component :is="activeTab.showSidebar !== false ? 'Fold' : 'Expand'" /></el-icon>
            </div>
            <div class="icon-btn" @click="showServerInfo" title="服务器信息">
              <el-icon><InfoFilled /></el-icon>
            </div>
            <div class="icon-btn-sep"></div>
            <div class="icon-btn" @click="decreaseFontSize" :title="'缩小字体 (' + terminalFontSize + ')'">
              <el-icon><ZoomOut /></el-icon>
            </div>
            <div class="icon-btn" @click="increaseFontSize" :title="'放大字体 (' + terminalFontSize + ')'">
              <el-icon><ZoomIn /></el-icon>
            </div>
            <div class="icon-btn-sep"></div>
            <div class="icon-btn" @click="toggleTerminalTheme" :title="terminalLightTheme ? '深色模式' : '浅色模式'">
              <el-icon><component :is="terminalLightTheme ? 'Moon' : 'Sunny'" /></el-icon>
            </div>
          </div>
        </div>
        <div class="info-actions">
          <el-button link type="primary" size="small" @click="activeTab && handleReconnectForTab(activeTab)">
            <el-icon><RefreshRight /></el-icon> 重新连接
          </el-button>
          <el-button link type="danger" size="small" @click="activeTab && handleDisconnectForTab(activeTab)">
            <el-icon><Close /></el-icon> 断开
          </el-button>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="ssh-content">
        <!-- 左侧：SFTP面板（仅当前激活tab，且仅当showSidebar不为false时渲染） -->
        <div class="ssh-sidebar" :style="{ width: sidebarWidth + 'px' }" v-if="activeTab?.showSidebar !== false">
          <!-- SFTP头部 -->
          <div class="sidebar-header">
            <span class="sidebar-title">文件管理</span>
            <div class="sidebar-header-actions">
              <div class="header-icon-btn" @click="reconnectSftp" title="重新连接">
                <el-icon :size="14"><RefreshRight /></el-icon>
              </div>
            </div>
          </div>
          <!-- SFTP内容 -->
          <div class="sftp-panel-wrapper">
            <SftpPanel
              ref="sftpPanelRef"
              :key="activeTab?.id"
              v-if="activeTab"
              :serverId="activeTab.serverId"
              :initPath="activeTab.serverData?.initPath || ''"
              @openDialog="openSftpDialog(activeTab)"
            />
          </div>
          <!-- 拖拽调整宽度 -->
          <div class="sidebar-resizer" @mousedown="startResize"></div>
        </div>

        <!-- 右侧：终端区域（所有tab都渲染，用v-show控制显示，保持连接状态） -->
        <div class="ssh-terminal-area">
        <template v-for="tab in tabs" :key="tab.id">
          <div v-show="activeTabId === tab.id" class="tab-terminal-panel">
            <div v-if="!tab.connected" class="ssh-placeholder">
              <div class="placeholder-title">{{ tab.serverName }}</div>
              <div class="placeholder-icon">
                <svg viewBox="0 0 48 48" width="48" height="48" fill="none">
                  <rect x="4" y="8" width="40" height="32" rx="3" stroke="#555" stroke-width="2"/>
                  <polyline points="12,20 18,26 12,32" stroke="#555" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                  <line x1="22" y1="32" x2="34" y2="32" stroke="#555" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <span class="placeholder-tip">连接已断开</span>
              <el-button size="small" type="primary" @click="handleReconnectForTab(tab)">
                <el-icon><RefreshRight /></el-icon> 重新连接
              </el-button>
            </div>
            <SshTerminal
              v-else
              :ref="(el: any) => { if (el) terminalRefs[tab.id] = el }"
              :serverId="tab.serverId"
              :serverName="tab.serverName"
              :initPath="tab.serverData?.initPath || ''"
              :fontSize="terminalFontSize"
              :lightTheme="terminalLightTheme"
              @close="onTerminalClose(tab)"
              @reconnect="handleReconnectForTab(tab)"
              @disconnect="handleDisconnectForTab(tab)"
            />
          </div>
        </template>
      </div>
      </div>
    </div>

    <!-- 未选择服务器 -->
    <div class="ssh-empty" v-else>
      <div class="empty-icon">
        <svg viewBox="0 0 64 64" width="64" height="64" fill="none">
          <rect x="8" y="12" width="48" height="40" rx="4" stroke="#c0c4cc" stroke-width="2"/>
          <polyline points="20,28 28,36 20,44" stroke="#c0c4cc" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          <line x1="32" y1="44" x2="44" y2="44" stroke="#c0c4cc" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <p>请点击左上角"服务器"选择服务器开始使用</p>
    </div>

    <!-- 服务器列表弹窗 -->
    <ServerManagerDialog v-model="managerVisible" @connect="onServerConnect" />

    <!-- SFTP 大窗口弹窗 -->
    <el-dialog
      v-model="sftpDialogVisible"
      :title="`服务器目录 - ${sftpDialogTab?.serverName || ''}`"
      width="720px"
      top="5vh"
      destroy-on-close
      append-to-body
    >
      <div class="sftp-dialog-body">
        <SftpPanel
          v-if="sftpDialogVisible && sftpDialogTab"
          :serverId="sftpDialogTab.serverId"
          :initPath="sftpDialogTab.serverData?.initPath || ''"
          dialogMode
        />
      </div>
    </el-dialog>

    <!-- 服务器信息弹窗 -->
    <ServerInfoDialog v-model="serverInfoVisible" :serverId="activeTab?.serverId || ''" />

    <!-- Tab 右键菜单 -->
    <div
      v-show="contextMenuVisible"
      class="tab-context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
    >
      <div class="context-menu-item" @click="closeTabsLeft">
        <el-icon><ArrowLeft /></el-icon> 关闭左侧
      </div>
      <div class="context-menu-item" @click="closeTabsRight">
        <el-icon><ArrowRight /></el-icon> 关闭右侧
      </div>
      <div class="context-menu-item" @click="closeTabsOther">
        <el-icon><CircleClose /></el-icon> 关闭其他
      </div>
      <div class="context-menu-divider"></div>
      <div class="context-menu-item" @click="duplicateTab">
        <el-icon><CopyDocument /></el-icon> 复制连接
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onBeforeUnmount, watch, nextTick, onMounted } from 'vue'
import { Monitor, Close, RefreshRight, CopyDocument, ArrowLeft, ArrowRight, CircleClose, FolderOpened, Fold, Expand, InfoFilled, ZoomOut, ZoomIn, Sunny, Moon } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import ServerManagerDialog from './ServerManagerDialog.vue'
import SshTerminal from './SshTerminal.vue'
import SftpPanel from './SftpPanel.vue'
import ServerInfoDialog from './ServerInfoDialog.vue'

// 组件名称，用于keep-alive缓存
defineOptions({ name: 'SshTerminal' })

interface SshTab {
  id: string
  serverId: string
  serverName: string
  serverData: any
  connected: boolean
  showSidebar?: boolean
}

let tabIdCounter = 0
const tabs = ref<SshTab[]>([])
const activeTabId = ref<string>('')
const managerVisible = ref(false)
const sidebarWidth = ref(260)
const terminalRefs = ref<Record<string, any>>({})
const sftpPanelRef = ref<any>(null)

// 终端字体大小控制
const MIN_FONT_SIZE = 10
const MAX_FONT_SIZE = 28
const terminalFontSize = ref(14)

const increaseFontSize = () => {
  if (terminalFontSize.value < MAX_FONT_SIZE) {
    terminalFontSize.value += 1
  }
}

const decreaseFontSize = () => {
  if (terminalFontSize.value > MIN_FONT_SIZE) {
    terminalFontSize.value -= 1
  }
}

// 终端颜色反转
const terminalLightTheme = ref(false)

const toggleTerminalTheme = () => {
  terminalLightTheme.value = !terminalLightTheme.value
}

// 解析SSH服务器标签
const parseSshTags = (tagsStr: string) => {
  if (!tagsStr) return []
  return tagsStr.split(',').filter(t => t.trim()).map(tagStr => {
    const parts = tagStr.split(':')
    return {
      label: parts[0],
      type: parts[1] || ''
    }
  })
}

// 重新连接SFTP
const reconnectSftp = () => {
  sftpPanelRef.value?.reconnect()
}

// 右键菜单状态
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuTab = ref<SshTab | null>(null)

const activeTab = computed(() => {
  return tabs.value.find(t => t.id === activeTabId.value) || null
})

// SFTP 大窗口
const sftpDialogVisible = ref(false)
const sftpDialogTab = ref<SshTab | null>(null)

const openSftpDialog = (tab: SshTab) => {
  sftpDialogTab.value = tab
  sftpDialogVisible.value = true
}

// 服务器信息弹窗
const serverInfoVisible = ref(false)

const showServerInfo = () => {
  serverInfoVisible.value = true
}

const showContextMenu = (e: MouseEvent, tab: SshTab) => {
  contextMenuTab.value = tab
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuVisible.value = true
}

const hideContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuTab.value = null
}

const closeTabsLeft = () => {
  if (!contextMenuTab.value) return
  const idx = tabs.value.findIndex(t => t.id === contextMenuTab.value!.id)
  if (idx > 0) {
    tabs.value.splice(0, idx)
    if (activeTabId.value !== contextMenuTab.value.id) {
      activeTabId.value = contextMenuTab.value.id
    }
  }
  hideContextMenu()
}

const closeTabsRight = () => {
  if (!contextMenuTab.value) return
  const idx = tabs.value.findIndex(t => t.id === contextMenuTab.value!.id)
  if (idx !== -1 && idx < tabs.value.length - 1) {
    tabs.value.splice(idx + 1)
    if (activeTabId.value !== contextMenuTab.value.id) {
      activeTabId.value = contextMenuTab.value.id
    }
  }
  hideContextMenu()
}

const closeTabsOther = () => {
  if (!contextMenuTab.value) return
  tabs.value = [contextMenuTab.value]
  activeTabId.value = contextMenuTab.value.id
  hideContextMenu()
}

const duplicateTab = () => {
  if (!contextMenuTab.value) return
  const src = contextMenuTab.value
  const newTab: SshTab = {
    id: 'tab-' + (++tabIdCounter),
    serverId: src.serverId,
    serverName: getUniqueTabName(src.serverData?.name || src.serverName),
    serverData: src.serverData,
    connected: false
  }
  tabs.value.push(newTab)
  activeTabId.value = newTab.id
  setTimeout(() => {
    const tab = tabs.value.find(t => t.id === newTab.id)
    if (tab) tab.connected = true
  }, 50)
  hideContextMenu()
}

// 点击页面其他地方关闭右键菜单
onMounted(() => {
  document.addEventListener('click', hideContextMenu)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', hideContextMenu)
})

const getUniqueTabName = (baseName: string) => {
  let name = baseName
  let idx = 1
  while (tabs.value.some(t => t.serverName === name)) {
    name = `${baseName} (${idx})`
    idx++
  }
  return name
}

const onServerConnect = (server: any) => {
  const newTab: SshTab = {
    id: 'tab-' + (++tabIdCounter),
    serverId: server.pkServer,
    serverName: getUniqueTabName(server.name),
    serverData: server,
    connected: false,
    showSidebar: true
  }
  tabs.value.push(newTab)
  activeTabId.value = newTab.id
  // 自动连接 - 必须通过 tabs.value 查找才能触发响应式更新
  setTimeout(() => {
    const tab = tabs.value.find(t => t.id === newTab.id)
    if (tab) tab.connected = true
  }, 50)
}

const handleReconnectForTab = (tab: SshTab) => {
  const t = tabs.value.find(t => t.id === tab.id)
  if (!t) return
  if (t.connected) {
    t.connected = false
    setTimeout(() => {
      const target = tabs.value.find(t => t.id === tab.id)
      if (target) target.connected = true
    }, 100)
  } else {
    t.connected = true
  }
}

const handleDisconnectForTab = (tab: SshTab) => {
  const t = tabs.value.find(t => t.id === tab.id)
  if (t) t.connected = false
}

const toggleSidebarForTab = (tab: SshTab) => {
  const t = tabs.value.find(t => t.id === tab.id)
  if (t) {
    t.showSidebar = t.showSidebar === false ? true : false
  }
}

const onTerminalClose = (tab: SshTab) => {
  const t = tabs.value.find(t => t.id === tab.id)
  if (t) t.connected = false
}

const closeTab = (tab: SshTab) => {
  const idx = tabs.value.findIndex(t => t.id === tab.id)
  if (idx === -1) return
  tabs.value.splice(idx, 1)
  // 切换激活标签
  if (activeTabId.value === tab.id) {
    if (tabs.value.length > 0) {
      activeTabId.value = tabs.value[Math.min(idx, tabs.value.length - 1)].id
    } else {
      activeTabId.value = ''
    }
  }
}

const startResize = (e: MouseEvent) => {
  const startX = e.clientX
  const startWidth = sidebarWidth.value
  const onMouseMove = (ev: MouseEvent) => {
    const newWidth = Math.min(Math.max(startWidth + ev.clientX - startX, 180), 420)
    sidebarWidth.value = newWidth
  }
  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 切换标签页时，对当前终端执行 fit
watch(activeTabId, () => {
  nextTick(() => {
    const tab = tabs.value.find(t => t.id === activeTabId.value)
    if (tab && tab.connected) {
      setTimeout(() => {
        const termRef = terminalRefs.value[tab.id]
        if (termRef && termRef.fit) {
          termRef.fit()
        }
      }, 100)
    }
  })
})

onBeforeUnmount(() => {
  document.removeEventListener('click', hideContextMenu)
  tabs.value.forEach(t => { t.connected = false })
})
</script>

<style lang="less" scoped>
.ssh-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  color: #303133;
  font-size: 13px;
}

/* 顶部工具条 */
.ssh-toolbar {
  height: 38px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 0;
  flex: 1;
  overflow: hidden;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

/* 服务器管理图标 */
.manager-icon {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
  height: 32px;
  cursor: pointer;
  color: #606266;
  border-radius: 3px;
  transition: all 0.15s;
  flex-shrink: 0;

  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }

  .manager-text {
    font-size: 12px;
    font-weight: 500;
    white-space: nowrap;
  }
}

.toolbar-sep {
  width: 1px;
  height: 20px;
  background: #dcdfe6;
  margin: 0 6px;
  flex-shrink: 0;
}

/* 标签栏 - xshell风格 */
.tab-bar {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  flex: 1;
  overflow-x: auto;
  padding-bottom: 1px;

  &::-webkit-scrollbar {
    display: none;
  }
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  height: 30px;
  background: #f5f7fa;
  border-radius: 4px 4px 0 0;
  cursor: pointer;
  font-size: 12px;
  color: #606266;
  border: 1px solid transparent;
  border-bottom: none;
  white-space: nowrap;
  transition: all 0.15s;
  user-select: none;

  &:hover {
    background: #e4e7ed;
    color: #303133;

    .tab-close {
      opacity: 1;
    }
  }

  &.active {
    background: #fff;
    color: #409eff;
    border-color: #e4e7ed;
    border-bottom: 1px solid #fff;
    margin-bottom: -1px;
    z-index: 1;

    .tab-close {
      opacity: 1;
    }
  }
}

.tab-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c0c4cc;
  flex-shrink: 0;

  &.connected {
    background: #67c23a;
  }
}

.tab-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tab-close {
  opacity: 0;
  cursor: pointer;
  color: #909399;
  transition: all 0.15s;
  flex-shrink: 0;

  &:hover {
    color: #f56c6c;
  }
}

/* 主体区域 */
.ssh-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 5px;
  gap: 5px;
}

/* 服务器信息顶栏 */
.server-info-bar {
  height: 36px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  padding: 0 12px;
  gap: 16px;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  .info-item {
    display: flex;
    align-items: center;
    gap: 6px;

    .info-label {
      font-size: 12px;
      color: #909399;
      white-space: nowrap;
    }

    .info-value {
      font-size: 12px;
      color: #303133;
    }

    .mono {
      font-family: 'Cascadia Code', 'Fira Code', Menlo, Monaco, Consolas, monospace;
      font-size: 11px;
      background: #f5f7fa;
      padding: 2px 6px;
      border-radius: 3px;
    }

    // 标签容器
    .info-tags {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .info-name {
    .name-text {
      font-size: 13px;
      font-weight: 600;
      color: #303133;
    }
  }

  .info-sep {
    width: 1px;
    height: 16px;
    background: #e4e7ed;
    flex-shrink: 0;
  }

  .icon-btn-group {
    display: flex;
    align-items: center;
    background: #f5f7fa;
    border-radius: 4px;
    padding: 2px;
    gap: 2px;
    font-size: 16px;

    .icon-btn-sep {
      width: 1px;
      height: 14px;
      background: #dcdfe6;
      margin: 0 2px;
    }

    .icon-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 22px;
      height: 22px;
      cursor: pointer;
      color: #606266;
      border-radius: 3px;
      transition: all 0.15s;

      &:hover {
        background: #e4e7ed;
        color: #409eff;
      }
    }
  }

  .info-actions {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

/* 主体内容区 */
.ssh-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  gap: 5px;
}

/* 左侧侧边栏 */
.ssh-sidebar {
  position: relative;
  width: 220px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* SFTP头部 */
.sidebar-header {
  height: 28px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8px;
  flex-shrink: 0;

  .sidebar-title {
    font-size: 12px;
    color: #606266;
    font-weight: 500;
  }

  .sidebar-header-actions {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .header-icon-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    cursor: pointer;
    color: #909399;
    border-radius: 3px;
    transition: all 0.15s;

    &:hover {
      color: #409eff;
      background: #e4e7ed;
    }
  }
}

.sftp-panel-wrapper {
  height: 100%;
  overflow: hidden;
}

/* 拖拽调整宽度 */
.sidebar-resizer {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  cursor: col-resize;

  &:hover {
    background: #409eff;
  }
}

/* 右侧终端区域 */
.ssh-terminal-area {
  flex: 1;
  background: #1e1e1e;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
}

.tab-terminal-panel {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
}

.ssh-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #666;
}

.placeholder-icon {
  opacity: 0.6;
}

.placeholder-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.placeholder-tip {
  font-size: 13px;
  color: #999;
}

/* 空状态 */
.ssh-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  padding: 5px;
  gap: 12px;

  p {
    margin: 0;
    font-size: 14px;
  }
}

.empty-icon {
  opacity: 0.5;
}

/* Tab 右键菜单 */
.tab-context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 4px 0;
  min-width: 140px;
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }

  .el-icon {
    flex-shrink: 0;
  }
}

.context-menu-divider {
  height: 1px;
  background: #e4e7ed;
  margin: 4px 0;
}

/* SFTP 大窗口弹窗 */
.sftp-dialog-body {
  height: 65vh;
  overflow: hidden;
}
</style>

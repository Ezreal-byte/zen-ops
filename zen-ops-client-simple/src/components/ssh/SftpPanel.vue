<template>
  <div class="sftp-panel" @contextmenu.prevent="onContextMenu($event, null)">
    <!-- 面包屑 + 工具栏 -->
    <div class="sftp-toolbar">
      <div class="breadcrumb">
        <div class="toolbar-btn" @click="goHome" title="根目录">
          <el-icon :size="16"><HomeFilled /></el-icon>
        </div>
        <div v-if="currentPath !== '/'" class="toolbar-btn" @click="goBack" title="上级目录">
          <el-icon :size="16"><ArrowLeft /></el-icon>
        </div>
        <el-input
          v-model="currentPath"
          size="small"
          class="path-input"
          @keyup.enter="onPathEnter"
        />
      </div>
      <div class="actions">
        <div class="toolbar-btn" @click="fetchFiles" :title="loading ? '加载中' : '刷新'">
          <el-icon :size="16"><Refresh /></el-icon>
        </div>
        <div v-if="!dialogMode" class="toolbar-btn" @click="emit('openDialog')" title="大窗口打开">
          <el-icon :size="16"><FullScreen /></el-icon>
        </div>
      </div>
    </div>
    <input ref="fileInputRef" type="file" style="display: none" @change="handleFileChange" />

    <!-- 上传进度弹窗 -->
    <el-dialog
      v-model="uploadVisible"
      title="文件上传"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      destroy-on-close
    >
      <div class="upload-dialog-body">
        <div class="upload-file-name" v-if="uploadFileName">
          <el-icon style="margin-right: 4px"><Document /></el-icon>{{ uploadFileName }}
        </div>
        <el-progress
          :percentage="uploadPercent"
          :stroke-width="12"
          :status="uploadStatus"
        />
        <div class="upload-tip">{{ uploadTip }}</div>
      </div>
      <template #footer>
        <el-button size="small" @click="handleUploadCancel" :disabled="uploading">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新建文件夹弹窗 -->
    <el-dialog v-model="mkdirDialogVisible" title="新建文件夹" width="360px" destroy-on-close>
      <el-input v-model="mkdirName" placeholder="请输入文件夹名称" @keyup.enter="onMkdirConfirm" />
      <template #footer>
        <el-button size="small" @click="mkdirDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="onMkdirConfirm" :disabled="!mkdirName.trim()">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重命名弹窗 -->
    <el-dialog v-model="renameDialogVisible" title="重命名" width="360px" destroy-on-close>
      <el-input v-model="renameNewName" placeholder="请输入新名称" @keyup.enter="onRenameConfirm" />
      <template #footer>
        <el-button size="small" @click="renameDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="onRenameConfirm" :disabled="!renameNewName.trim()">确定</el-button>
      </template>
    </el-dialog>

    <!-- 文件列表区域 -->
    <div class="sftp-list" v-loading="loading">
      <!-- 列表视图（默认） -->
      <template v-if="viewMode === 'list'">
        <div
          v-for="item in fileList"
          :key="item.filename"
          class="file-item"
          :class="{ 'is-dir': item.attrs?.dir, active: selectedFile?.filename === item.filename }"
          @click="handleFileClick(item)"
          @dblclick="handleFileDblClick(item)"
          @contextmenu.stop="onContextMenu($event, item)"
        >
          <el-icon class="file-icon" :size="16">
            <FolderOpened v-if="item.attrs?.dir" />
            <Document v-else />
          </el-icon>
          <span class="file-name" :title="item.filename">{{ item.filename }}</span>
          <span v-if="!item.attrs?.dir" class="file-size">{{ formatSize(item.attrs?.size) }}</span>
        </div>
      </template>

      <!-- 中等图标视图 -->
      <template v-if="viewMode === 'icon'">
        <div class="icon-grid">
          <div
            v-for="item in fileList"
            :key="item.filename"
            class="icon-item"
            :class="{ 'is-dir': item.attrs?.dir, active: selectedFile?.filename === item.filename }"
            @click="handleFileClick(item)"
            @dblclick="handleFileDblClick(item)"
            @contextmenu.stop="onContextMenu($event, item)"
          >
            <el-icon class="icon-file-icon" :size="40">
              <FolderOpened v-if="item.attrs?.dir" />
              <Document v-else />
            </el-icon>
            <span class="icon-file-name" :title="item.filename">{{ item.filename }}</span>
          </div>
        </div>
      </template>

      <!-- 详细信息视图 -->
      <template v-if="viewMode === 'detail'">
        <div class="detail-header">
          <span class="dh-name">名称</span>
          <span class="dh-size">大小</span>
          <span class="dh-type">类型</span>
          <span class="dh-perm">权限</span>
          <span class="dh-time">修改时间</span>
        </div>
        <div
          v-for="item in fileList"
          :key="item.filename"
          class="detail-row"
          :class="{ 'is-dir': item.attrs?.dir, active: selectedFile?.filename === item.filename }"
          @click="handleFileClick(item)"
          @dblclick="handleFileDblClick(item)"
          @contextmenu.stop="onContextMenu($event, item)"
        >
          <span class="dr-name">
            <el-icon class="file-icon" :size="15">
              <FolderOpened v-if="item.attrs?.dir" />
              <Document v-else />
            </el-icon>
            {{ item.filename }}
          </span>
          <span class="dr-size">{{ item.attrs?.dir ? '' : formatSize(item.attrs?.size) }}</span>
          <span class="dr-type">{{ item.attrs?.dir ? '文件夹' : getFileType(item.filename) }}</span>
          <span class="dr-perm">{{ formatPermissions(item.attrs) }}</span>
          <span class="dr-time">{{ formatTime(item.attrs?.mtime) }}</span>
        </div>
      </template>

      <el-empty v-if="fileList.length === 0" description="空目录" :image-size="60" />
    </div>

    <!-- 底部状态栏 -->
    <div class="sftp-footer">
      <span class="footer-count">{{ fileList.length }} 项</span>
      <div v-if="dialogMode" class="view-toggle">
        <el-tooltip content="列表" placement="top" :show-after="500">
          <el-icon
            :size="22"
            :class="{ active: viewMode === 'list' }"
            @click="viewMode = 'list'"
          ><List /></el-icon>
        </el-tooltip>
        <el-tooltip content="中等图标" placement="top" :show-after="500">
          <el-icon
            :size="22"
            :class="{ active: viewMode === 'icon' }"
            @click="viewMode = 'icon'"
          ><Grid /></el-icon>
        </el-tooltip>
        <el-tooltip content="详细信息" placement="top" :show-after="500">
          <el-icon
            :size="22"
            :class="{ active: viewMode === 'detail' }"
            @click="viewMode = 'detail'"
          ><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div
      v-show="contextMenuVisible"
      class="sftp-context-menu"
      :style="{ left: contextMenuLeft + 'px', top: contextMenuTop + 'px' }"
    >
      <template v-if="contextMenuItem">
        <div v-if="!contextMenuItem.attrs?.dir" class="ctx-item" @click="onDownloadFile">
          <el-icon><Download /></el-icon> 下载
        </div>
        <div class="ctx-item" @click="onRename">
          <el-icon><EditPen /></el-icon> 重命名
        </div>
        <div class="ctx-item ctx-danger" @click="onDelete">
          <el-icon><Delete /></el-icon> 删除
        </div>
        <div class="ctx-divider"></div>
      </template>
      <div class="ctx-item" @click="onUploadToDir">
        <el-icon><Upload /></el-icon> 上传文件到此目录
      </div>
      <div class="ctx-item" @click="onMkdir">
        <el-icon><FolderAdd /></el-icon> 新建文件夹
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled, ArrowLeft, Upload, Download, Refresh,
  FolderOpened, Document, EditPen, Delete, FolderAdd, FullScreen,
  List, Grid, InfoFilled
} from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import service from '@/utils/request'

const props = defineProps<{
  serverId: string
  initPath: string
  dialogMode?: boolean
}>()

const emit = defineEmits<{
  (e: 'openDialog'): void
}>()

const sftpId = ref('')
const currentPath = ref('')
const fileList = ref<any[]>([])
const loading = ref(false)
const selectedFile = ref<any>(null)
const viewMode = ref<'list' | 'icon' | 'detail'>('list')

const baseUrl = import.meta.env.VITE_PROXY_DOMAIN || '/platform'

// 上传相关
const fileInputRef = ref<HTMLInputElement>()
const uploading = ref(false)
const uploadVisible = ref(false)
const uploadPercent = ref(0)
const uploadFileName = ref('')
const uploadStatus = ref<'' | 'success' | 'exception' | 'warning'>('')
const uploadTip = ref('')
const uploadTargetPath = ref('')

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuLeft = ref(0)
const contextMenuTop = ref(0)
const contextMenuItem = ref<any>(null)

// 新建文件夹
const mkdirDialogVisible = ref(false)
const mkdirName = ref('')

// 重命名
const renameDialogVisible = ref(false)
const renameOldName = ref('')
const renameNewName = ref('')

const initSftp = async () => {
  if (!props.serverId) return
  loading.value = true
  try {
    const body = JSON.stringify({
      type: 'ID',
      channelType: 'sftp',
      id: props.serverId,
      idType: 'SERVER',
      initPath: props.initPath || ''
    })
    const res: any = await request.post('/commons/sftp/init', body, {
      headers: { 'Content-Type': 'application/json' }
    })
    sftpId.value = res.id
    currentPath.value = res.initPath || '/home'
    fileList.value = sortFiles((res.files || []).filter((f: any) => f.filename !== '.'))
  } catch (e: any) {
    ElMessage.error('SFTP 连接失败: ' + (e?.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重新连接SFTP
const reconnect = () => {
  sftpId.value = ''
  initSftp()
}

defineExpose({ reconnect })

const fetchFiles = async (retry = true) => {
  if (!sftpId.value) {
    await initSftp()
    if (!sftpId.value) return
  }
  loading.value = true
  try {
    const res: any = await request.get(`/commons/sftp/ls/${sftpId.value}`, {
      params: { path: currentPath.value, showHiddenFiles: false }
    })
    fileList.value = sortFiles((res || []).filter((f: any) => f.filename !== '.'))
    selectedFile.value = null
  } catch (e: any) {
    const msg = e?.message || ''
    if (retry && isConnectionError(msg)) {
      sftpId.value = ''
      await initSftp()
      if (sftpId.value) {
        await fetchFiles(false)
        return
      }
    }
    ElMessage.error('获取文件列表失败: ' + msg)
  } finally {
    loading.value = false
  }
}

const isConnectionError = (msg: string) => {
  return msg.includes('连接') || msg.includes('断开') || msg.includes('Channel') || msg.includes('Session')
    || msg.includes('channel') || msg.includes('session') || msg.includes('not connected')
    || msg.includes('timeout') || msg.includes('closed')
}

const handleFileClick = (item: any) => {
  selectedFile.value = item
}

const handleFileDblClick = (item: any) => {
  if (item.attrs?.dir) {
    if (item.filename === '..') {
      goBack()
    } else {
      currentPath.value = currentPath.value === '/' ? '/' + item.filename : currentPath.value + '/' + item.filename
      fetchFiles()
    }
  }
}

const goHome = () => {
  currentPath.value = '/'
  fetchFiles()
}

// 输入路径回车进入
const onPathEnter = () => {
  const path = currentPath.value.trim()
  if (!path) {
    currentPath.value = '/'
  }
  fetchFiles()
}

const goBack = () => {
  if (currentPath.value === '/' || currentPath.value === '') return
  // 直接从当前路径计算父路径
  const parts = currentPath.value.replace(/\/+$/, '').split('/')
  parts.pop()
  currentPath.value = parts.length <= 1 ? '/' : parts.join('/')
  fetchFiles()
}

// ===== 右键菜单 =====
const onContextMenu = (e: MouseEvent, item: any) => {
  e.preventDefault()
  contextMenuItem.value = item
  if (item) {
    selectedFile.value = item
  }
  const panel = (e.currentTarget as HTMLElement).closest('.sftp-panel')
  const rect = panel ? panel.getBoundingClientRect() : { left: 0, top: 0 }
  contextMenuLeft.value = e.clientX - rect.left
  contextMenuTop.value = e.clientY - rect.top
  contextMenuVisible.value = true
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
}

// 上传到此目录
const onUploadToDir = () => {
  closeContextMenu()
  uploadTargetPath.value = currentPath.value
  triggerUpload()
}

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleUploadCancel = () => {
  uploadVisible.value = false
}

const doUpload = async (file: File, retry = true) => {
  if (!sftpId.value) {
    await initSftp()
    if (!sftpId.value) {
      ElMessage.warning('SFTP 未连接')
      return
    }
  }

  uploading.value = true
  uploadVisible.value = true
  uploadPercent.value = 0
  uploadFileName.value = file.name
  uploadStatus.value = ''
  uploadTip.value = '正在上传文件...'

  try {
    const targetPath = uploadTargetPath.value || currentPath.value
    const formData = new FormData()
    formData.append('file', file)
    formData.append('path', targetPath)
    const res: any = await service.post(`/commons/sftp/upload/${sftpId.value}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (progressEvent: any) => {
        if (progressEvent.total) {
          uploadPercent.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          if (uploadPercent.value >= 100) {
            uploadTip.value = '文件上传中，请稍候...'
          }
        }
      },
      showLoading: false
    })
    if (res && typeof res === 'object' && res.code !== undefined && res.code !== 0) {
      const errMsg = res.msg || '未知错误'
      if (retry && isConnectionError(errMsg)) {
        sftpId.value = ''
        await initSftp()
        if (sftpId.value) {
          await doUpload(file, false)
          return
        }
      }
      uploadStatus.value = 'exception'
      uploadTip.value = '上传失败: ' + errMsg
      ElMessage.error('上传失败: ' + errMsg)
      return
    }
    uploadPercent.value = 100
    uploadStatus.value = 'success'
    uploadTip.value = '上传成功！'
    ElMessage.success('上传成功')
    fetchFiles()
  } catch (e: any) {
    const msg = e?.message || '请重试'
    if (retry && isConnectionError(msg)) {
      sftpId.value = ''
      await initSftp()
      if (sftpId.value) {
        await doUpload(file, false)
        return
      }
    }
    uploadStatus.value = 'exception'
    uploadTip.value = '上传失败，请重试'
    ElMessage.error('上传失败: ' + msg)
  } finally {
    uploading.value = false
  }
}

const handleFileChange = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (fileInputRef.value) fileInputRef.value.value = ''
  if (!file) return
  await doUpload(file)
}

const handleDownload = async (item?: any, retry = true) => {
  const target = item || selectedFile.value
  if (!target || target.attrs?.dir) return
  if (!sftpId.value) {
    await initSftp()
    if (!sftpId.value) {
      ElMessage.warning('SFTP 未连接')
      return
    }
  }
  try {
    const path = currentPath.value === '/' ? '/' + target.filename : currentPath.value + '/' + target.filename
    const fileId: string = await request.get(`/commons/sftp/download/${sftpId.value}`, {
      params: { path }
    })
    const url = `${baseUrl}/commons/sftp/open_download?id=${fileId}&name=${encodeURIComponent(target.filename)}`
    const a = document.createElement('a')
    a.href = url
    a.download = target.filename
    a.click()
  } catch (e: any) {
    const msg = e?.message || '未知错误'
    if (retry && isConnectionError(msg)) {
      sftpId.value = ''
      await initSftp()
      if (sftpId.value) {
        await handleDownload(item, false)
        return
      }
    }
    ElMessage.error('下载失败: ' + msg)
  }
}

// ===== 下载文件 =====
const onDownloadFile = () => {
  const item = contextMenuItem.value
  closeContextMenu()
  if (!item || item.attrs?.dir) return
  handleDownload(item)
}

// ===== 新建文件夹 =====
const onMkdir = () => {
  closeContextMenu()
  mkdirName.value = ''
  mkdirDialogVisible.value = true
}

const onMkdirConfirm = async () => {
  const name = mkdirName.value.trim()
  if (!name) return
  try {
    const dirPath = currentPath.value === '/' ? '/' + name : currentPath.value + '/' + name
    await request.post(`/commons/sftp/mkdir/${sftpId.value}`, null, { params: { path: dirPath } })
    ElMessage.success('文件夹创建成功')
    mkdirDialogVisible.value = false
    fetchFiles()
  } catch (e: any) {
    ElMessage.error('创建文件夹失败: ' + (e?.message || '未知错误'))
  }
}

// ===== 重命名 =====
const onRename = () => {
  const item = contextMenuItem.value
  closeContextMenu()
  if (!item) return
  renameOldName.value = item.filename
  renameNewName.value = item.filename
  renameDialogVisible.value = true
}

const onRenameConfirm = async () => {
  const newName = renameNewName.value.trim()
  if (!newName || newName === renameOldName.value) {
    renameDialogVisible.value = false
    return
  }
  try {
    const oldPath = currentPath.value === '/' ? '/' + renameOldName.value : currentPath.value + '/' + renameOldName.value
    const newPath = currentPath.value === '/' ? '/' + newName : currentPath.value + '/' + newName
    await request.post(`/commons/sftp/rename/${sftpId.value}`, null, { params: { path: oldPath, pathNew: newPath } })
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
    fetchFiles()
  } catch (e: any) {
    ElMessage.error('重命名失败: ' + (e?.message || '未知错误'))
  }
}

// ===== 删除 =====
const onDelete = () => {
  const item = contextMenuItem.value
  closeContextMenu()
  if (!item) return
  const isDir = !!item.attrs?.dir
  const typeName = isDir ? '文件夹' : '文件'
  ElMessageBox.confirm(
    `确定删除${typeName} "${item.filename}" 吗？${isDir ? '文件夹内所有内容将被删除。' : ''}此操作不可恢复。`,
    '删除确认',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      const path = currentPath.value === '/' ? '/' + item.filename : currentPath.value + '/' + item.filename
      await request.del(`/commons/sftp/rm/${sftpId.value}`, { params: { path, dir: isDir } })
      ElMessage.success('删除成功')
      fetchFiles()
    } catch (e: any) {
      ElMessage.error('删除失败: ' + (e?.message || '未知错误'))
    }
  }).catch(() => {})
}

// ===== 通用方法 =====
const sortFiles = (files: any[]) => {
  return files.sort((a, b) => {
    if (a.filename === '..') return -1
    if (b.filename === '..') return 1
    const aDir = a.attrs?.dir
    const bDir = b.attrs?.dir
    if (aDir && !bDir) return -1
    if (!aDir && bDir) return 1
    return a.filename.localeCompare(b.filename, undefined, { sensitivity: 'base' })
  })
}

const formatSize = (size?: number) => {
  if (size == null) return ''
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / 1024 / 1024).toFixed(1) + ' MB'
  return (size / 1024 / 1024 / 1024).toFixed(1) + ' GB'
}

const formatTime = (mtime?: number) => {
  if (mtime == null || mtime === 0) return ''
  const d = new Date(mtime * 1000)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const getFileType = (filename: string) => {
  if (!filename || !filename.includes('.')) return '文件'
  const ext = filename.split('.').pop()!.toLowerCase()
  const map: Record<string, string> = {
    txt: '文本文件', log: '日志文件', md: 'Markdown',
    json: 'JSON', xml: 'XML', yaml: 'YAML', yml: 'YAML',
    sh: 'Shell脚本', bat: '批处理', cmd: '批处理',
    java: 'Java', py: 'Python', js: 'JavaScript', ts: 'TypeScript',
    html: 'HTML', css: 'CSS', vue: 'Vue',
    sql: 'SQL', conf: '配置文件', cfg: '配置文件', ini: '配置文件',
    zip: 'ZIP压缩', tar: 'TAR归档', gz: 'GZ压缩', rar: 'RAR压缩',
    jpg: '图片', jpeg: '图片', png: '图片', gif: '图片', svg: 'SVG图片',
    pdf: 'PDF文档', doc: 'Word文档', docx: 'Word文档',
    xls: 'Excel', xlsx: 'Excel', csv: 'CSV',
  }
  return map[ext] || ext.toUpperCase() + ' 文件'
}

const formatPermissions = (attrs: any) => {
  if (!attrs) return ''
  const perm = attrs.permissions
  if (perm == null) return ''
  // Unix 权限位转换为 rwxrwxrwx 格式
  const type = attrs.dir ? 'd' : '-'
  const owner = permToString((perm >> 6) & 7)
  const group = permToString((perm >> 3) & 7)
  const other = permToString(perm & 7)
  return type + owner + group + other
}

const permToString = (n: number) => {
  return (n & 4 ? 'r' : '-') + (n & 2 ? 'w' : '-') + (n & 1 ? 'x' : '-')
}

const onDocumentClick = () => {
  if (contextMenuVisible.value) {
    closeContextMenu()
  }
}

watch(() => props.serverId, () => {
  initSftp()
})

onMounted(() => {
  initSftp()
  document.addEventListener('click', onDocumentClick)
})

onBeforeUnmount(() => {
  if (sftpId.value) {
    request.get(`/commons/sftp/disconnect/${sftpId.value}`).catch(() => {})
  }
  document.removeEventListener('click', onDocumentClick)
})
</script>

<style lang="less" scoped>
.sftp-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.sftp-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  gap: 8px;

  .breadcrumb {
    display: flex;
    align-items: center;
    gap: 4px;
    flex: 1;
    overflow: hidden;
  }

  .toolbar-btn {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    cursor: pointer;
    color: #606266;
    flex-shrink: 0;
    transition: all 0.15s;

    &:hover {
      color: #409eff;
      background: #ecf5ff;
    }
  }

  .path-input {
    flex: 1;
    :deep(.el-input__wrapper) {
      border-radius: 4px;
      box-shadow: none;
      background: #f5f7fa;
      padding: 1px 8px;
      font-family: 'Cascadia Code', Menlo, Monaco, Consolas, monospace;
      font-size: 12px;
    }
    :deep(.el-input__wrapper:hover),
    :deep(.el-input__wrapper.is-focus) {
      box-shadow: 0 0 0 1px #409eff inset;
      background: #fff;
    }
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-shrink: 0;
  }
}

.sftp-list {
  flex: 1;
  overflow-y: auto;
  padding: 2px 0;
  min-height: 0;
}

// ===== 列表视图 =====
.file-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.15s;
  user-select: none;
  -webkit-user-select: none;

  &:hover { background: #f5f7fa; }
  &.active { background: #ecf5ff; }

  .file-icon { flex-shrink: 0; color: #409eff; }
  &.is-dir .file-icon { color: #e6a23c; }

  .file-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .file-size {
    font-size: 11px;
    color: #909399;
    flex-shrink: 0;
    margin-right: 4px;
  }
}

// ===== 中等图标视图 =====
.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 4px;
  padding: 8px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 4px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s;
  user-select: none;

  &:hover { background: #f5f7fa; }
  &.active { background: #ecf5ff; }

  .icon-file-icon { color: #409eff; margin-bottom: 4px; }
  &.is-dir .icon-file-icon { color: #e6a23c; }

  .icon-file-name {
    font-size: 11px;
    color: #303133;
    text-align: center;
    word-break: break-all;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.3;
    max-width: 100%;
  }
}

// ===== 详细信息视图 =====
.detail-header {
  display: flex;
  align-items: center;
  padding: 4px 10px;
  font-size: 11px;
  color: #909399;
  font-weight: 500;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
  flex-shrink: 0;

  .dh-name { flex: 1; min-width: 0; }
  .dh-size { width: 80px; text-align: right; flex-shrink: 0; }
  .dh-type { width: 90px; text-align: left; flex-shrink: 0; padding-left: 12px; }
  .dh-perm { width: 100px; text-align: left; flex-shrink: 0; padding-left: 12px; }
  .dh-time { width: 130px; text-align: left; flex-shrink: 0; padding-left: 12px; }
}

.detail-row {
  display: flex;
  align-items: center;
  padding: 3px 10px;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.15s;
  user-select: none;

  &:hover { background: #f5f7fa; }
  &.active { background: #ecf5ff; }

  .dr-name {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    .file-icon { flex-shrink: 0; color: #409eff; }
  }
  &.is-dir .dr-name .file-icon { color: #e6a23c; }

  .dr-size {
    width: 80px;
    text-align: right;
    flex-shrink: 0;
    color: #909399;
    font-size: 11px;
  }
  .dr-type {
    width: 90px;
    text-align: left;
    flex-shrink: 0;
    color: #909399;
    font-size: 11px;
    padding-left: 12px;
  }
  .dr-perm {
    width: 100px;
    text-align: left;
    flex-shrink: 0;
    color: #909399;
    font-size: 11px;
    padding-left: 12px;
    font-family: 'Cascadia Code', Menlo, Monaco, Consolas, monospace;
  }
  .dr-time {
    width: 130px;
    text-align: left;
    flex-shrink: 0;
    color: #909399;
    font-size: 11px;
    padding-left: 12px;
    font-family: 'Cascadia Code', Menlo, Monaco, Consolas, monospace;
  }
}

// ===== 底部视图切换栏 =====
.sftp-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 10px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
  flex-shrink: 0;

  .footer-count {
    font-size: 14px;
    color: #606266;
  }

  .view-toggle {
    display: flex;
    align-items: center;
    gap: 6px;

    .el-icon {
      cursor: pointer;
      color: #c0c4cc;
      padding: 4px;
      border-radius: 4px;
      transition: all 0.15s;

      &:hover { color: #606266; background: #f0f0f0; }
      &.active { color: #409eff; background: #ecf5ff; }
    }
  }
}

.upload-dialog-body {
  padding: 10px 0;
}

.upload-file-name {
  font-size: 13px;
  color: #303133;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
  text-align: center;
}

// 右键菜单
.sftp-context-menu {
  position: absolute;
  z-index: 3000;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  padding: 4px 0;
  min-width: 180px;

  .ctx-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 16px;
    font-size: 13px;
    color: #303133;
    cursor: pointer;
    transition: background 0.15s;

    &:hover { background: #ecf5ff; color: #409eff; }
    .el-icon { font-size: 14px; }
  }

  .ctx-danger {
    color: #f56c6c;
    &:hover { background: #fef0f0; color: #f56c6c; }
  }

  .ctx-divider {
    height: 1px;
    background: #e4e7ed;
    margin: 4px 0;
  }
}
</style>

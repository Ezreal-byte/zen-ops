<template>
  <div class="object-content">
    <div class="content-header">
      <div class="header-left">
        <el-input
          v-model="keyword"
          placeholder="搜索对象..."
          size="small"
          style="width: 220px"
          clearable
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
        />
        <el-button size="small" type="primary" @click="handleSearch">查询</el-button>
      </div>
      <div class="header-right">
        <el-button size="small" type="primary" @click="triggerUpload" :disabled="uploading">
          <el-icon style="margin-right: 4px"><Upload /></el-icon>{{ uploading ? '上传中...' : '上传文件' }}
        </el-button>
        <input ref="fileInputRef" type="file" style="display: none" @change="handleFileChange" />
      </div>
    </div>

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

    <div class="content-table-wrapper">
      <el-table
        :data="pagedData"
        v-loading="loading"
        empty-text="暂无对象"
        size="small"
        height="100%"
        stripe
        border
      >
        <el-table-column prop="key" label="对象名称" min-width="300">
          <template #default="{ row }">
            <el-icon style="vertical-align: middle; margin-right: 4px;"><Document /></el-icon>
            <span>{{ row.key }}</span>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="120">
          <template #default="{ row }">
            {{ formatSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastModified" label="修改日期" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="emit('download', row)">下载</el-button>
            <el-button link type="danger" size="small" @click="emit('deleteObj', row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="content-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[20, 50, 100, 200]"
        :total="list.length"
        layout="total, sizes, prev, pager, next"
        size="small"
        background
      />
    </div>

    <!-- 对象详情弹窗 -->
    <el-dialog v-model="detailVisible" title="对象详情" width="480px" destroy-on-close>
      <el-descriptions :column="1" border size="small" v-loading="detailLoading">
        <el-descriptions-item label="对象Key">{{ detailData.key }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{ formatSize(detailData.size) }}</el-descriptions-item>
        <el-descriptions-item label="最后修改">{{ detailData.lastModified }}</el-descriptions-item>
        <el-descriptions-item label="ETag">{{ detailData.etag || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Content-Type">{{ detailData.contentType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="存储类型">{{ detailData.storageClass || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属桶">{{ detailData.bucketName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="是否目录">{{ detailData.isDir ? '是' : '否' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Document, Search } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import service from '@/utils/request'

const props = defineProps<{
  dsId: string
  bucketName: string
  list: any[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'download', row: any): void
  (e: 'deleteObj', row: any): void
  (e: 'refresh'): void
  (e: 'search', keyword: string): void
}>()

// 搜索 - 传递给父级调用后端
const keyword = ref('')
const handleSearch = () => { emit('search', keyword.value) }

// 分页（前端分页，后端已过滤）
const currentPage = ref(1)
const pageSize = ref(50)
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return props.list.slice(start, start + pageSize.value)
})

// 列表变化或每页数量变化时重置分页
watch(() => props.list, () => { currentPage.value = 1 })
watch(pageSize, () => { currentPage.value = 1 })

// 对象详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<any>({})

const handleDetail = async (row: any) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const data = await request.get('/fso/object/detail', {
      params: { pkFsoDs: props.dsId, bucketName: props.bucketName, objectKey: row.key }
    })
    detailData.value = data || {}
  } catch (e) {
    detailData.value = row
  } finally {
    detailLoading.value = false
  }
}

// 上传
const fileInputRef = ref<HTMLInputElement>()
const uploading = ref(false)
const uploadVisible = ref(false)
const uploadPercent = ref(0)
const uploadFileName = ref('')
const uploadStatus = ref<'' | 'success' | 'exception' | 'warning'>('')
const uploadTip = ref('')

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleUploadCancel = () => {
  uploadVisible.value = false
}

const handleFileChange = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploading.value = true
  uploadVisible.value = true
  uploadPercent.value = 0
  uploadFileName.value = file.name
  uploadStatus.value = ''
  uploadTip.value = '正在上传文件到服务器...'

  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('pkFsoDs', props.dsId)
    formData.append('bucketName', props.bucketName)
    formData.append('objectKey', file.name)
    await service.post('/fso/object/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total) {
          uploadPercent.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          if (uploadPercent.value >= 100) {
            uploadTip.value = '文件已上传至服务器，正在推送到存储服务，请稍候...'
          }
        }
      },
      showLoading: false
    })
    uploadPercent.value = 100
    uploadStatus.value = 'success'
    uploadTip.value = '上传成功！'
    ElMessage.success('上传成功')
    emit('refresh')
  } catch (e) {
    uploadStatus.value = 'exception'
    uploadTip.value = '上传失败，请重试'
  } finally {
    uploading.value = false
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

// 格式化大小
const formatSize = (bytes: number) => {
  if (!bytes && bytes !== 0) return '-'
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + units[i]
}
</script>

<style lang="less" scoped>
.object-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.content-header {
  height: 34px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.content-table-wrapper {
  flex: 1;
  overflow: hidden;
  padding: 5px;
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

.content-pagination {
  height: 36px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

:deep(.el-table) {
  --el-table-border-color: #ebeef5;
  --el-table-header-bg-color: #fafafa;
}

:deep(.el-table th.el-table__cell) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
  font-size: 12px;
}

:deep(.el-table .el-table__cell) {
  padding: 4px 0;
}

:deep(.el-table--small .el-table__cell) {
  padding: 4px 0;
}
</style>

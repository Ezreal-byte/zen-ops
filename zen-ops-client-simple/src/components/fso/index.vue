<template>
  <div class="fso-container">
    <!-- 顶部窄条：数据源管理 -->
    <div class="fso-toolbar">
      <div class="toolbar-left">
        <el-select
          v-model="activeDsId"
          placeholder="选择数据源"
          size="small"
          style="width: 213px"
          @change="handleDsChange"
          clearable
        >
          <el-option
            v-for="ds in dataSourceList"
            :key="ds.pkFsoDs"
            :label="ds.name"
            :value="ds.pkFsoDs"
          >
            <span>
              <el-icon v-if="ds.isDefault === 1 || ds.isDefault === true" color="#E6A23C" style="margin-right: 4px"><StarFilled /></el-icon>
              {{ ds.name }}
            </span>
            <el-tag size="small" :type="ds.type === 'MINIO' ? 'danger' : ds.type === 'ALIYUN_OSS' ? 'warning' : 'info'" style="margin-left: 8px; scale: 0.85">
              {{ ds.type === 'MINIO' ? 'MinIO' : ds.type === 'ALIYUN_OSS' ? 'OSS' : 'RustFs' }}
            </el-tag>
          </el-option>
        </el-select>

        <!-- 设置默认数据源星星 -->
        <el-icon
          v-if="activeDsId"
          class="default-star"
          :class="{ active: currentDsIsDefault }"
          :color="currentDsIsDefault ? '#E6A23C' : '#C0C4CC'"
          :size="20"
          @click="handleSetDefault"
        >
          <StarFilled />
        </el-icon>
        <!-- 当前选中数据源的标签 -->
        <template v-if="activeDsId && currentDsTags.length">
          <el-tag
            v-for="tag in currentDsTags"
            :key="tag.type"
            :type="tag.type"
          >
            {{ tag.label }}
          </el-tag>
        </template>
      </div>
      <div class="toolbar-right">
        <el-button size="small" type="primary" @click="handleAddDs">新建数据源</el-button>
        <el-button size="small" @click="handleEditDs" :disabled="!activeDsId">编辑</el-button>
        <el-button size="small" type="danger" @click="handleDeleteDs" :disabled="!activeDsId">删除</el-button>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="fso-main" v-if="activeDsId">
      <!-- 左侧：桶列表卡片 -->
      <BucketSidebar
        :list="bucketList"
        :active="activeBucket"
        :loading="bucketLoading"
        @select="handleSelectBucket"
        @delete="handleDeleteBucket"
        @create="showCreateBucket = true"
        @detail="handleBucketDetail"
      />

      <!-- 右侧：对象表格卡片 -->
      <ObjectTable
        v-if="activeBucket"
        :dsId="activeDsId"
        :bucketName="activeBucket"
        :list="objectList"
        :loading="objectLoading"
        @download="handleDownloadObject"
        @deleteObj="handleDeleteObject"
        @refresh="fetchObjectList(activeBucket)"
        @search="handleObjectSearch"
      />
      <div v-else class="fso-placeholder">
        <span style="color: #909399">请从左侧选择一个桶查看对象</span>
      </div>
    </div>

    <!-- 未选择数据源 -->
    <div class="fso-empty" v-else>
      <el-icon :size="48" color="#c0c4cc"><Connection /></el-icon>
      <p>请选择或新建一个数据源开始使用</p>
    </div>

    <!-- 新建/编辑数据源弹窗 -->
    <DsDialog
      v-model="dsDialogVisible"
      :isEdit="isEdit"
      :editData="editDsData"
      @success="handleDsSaved"
    />

    <!-- 创建桶弹窗 -->
    <el-dialog v-model="showCreateBucket" title="创建桶" width="380px" destroy-on-close>
      <el-form :model="bucketForm" label-width="60px">
        <el-form-item label="桶名">
          <el-input v-model="bucketForm.name" placeholder="请输入桶名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateBucket = false">取消</el-button>
        <el-button type="primary" @click="handleCreateBucket">确定</el-button>
      </template>
    </el-dialog>

    <!-- 桶详细信息弹窗 -->
    <BucketDetailDialog
      v-model="showBucketDetail"
      :bucket="currentBucket"
      :dsId="activeDsId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Connection, StarFilled } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import DsDialog from './DsDialog.vue'
import BucketSidebar from './BucketSidebar.vue'
import ObjectTable from './ObjectTable.vue'
import BucketDetailDialog from './BucketDetailDialog.vue'

// 组件名称，用于keep-alive缓存
defineOptions({ name: 'ObjectStorage' })

// 数据源
const dataSourceList = ref<any[]>([])
const activeDsId = ref<string>('')

// 桶
const bucketList = ref<any[]>([])
const bucketLoading = ref(false)
const activeBucket = ref('')

// 对象
const objectList = ref<any[]>([])
const objectLoading = ref(false)
const objectKeyword = ref('')

// 侧边栏宽度
const sidebarWidth = ref(200)

// 数据源对话框
const dsDialogVisible = ref(false)
const isEdit = ref(false)
const editDsData = ref<any>(null)

// 创建桶
const showCreateBucket = ref(false)
const bucketForm = reactive({ name: '' })

// 桶详细信息
const showBucketDetail = ref(false)
const currentBucket = ref<any>(null)

// 获取数据源列表
const fetchDataSourceList = async () => {
  try {
    const data = await request.get('/fso/datasource/list')
    dataSourceList.value = data || []
    // 自动选择默认数据源
    if (!activeDsId.value && dataSourceList.value.length > 0) {
      const defaultDs = dataSourceList.value.find((ds: any) => ds.isDefault === 1 || ds.isDefault === true)
      if (defaultDs) {
        activeDsId.value = defaultDs.pkFsoDs
        handleDsChange(defaultDs.pkFsoDs)
      }
    }
  } catch (e) { /* 拦截器处理 */ }
}

// 计算当前数据源是否为默认
const currentDsIsDefault = computed(() => {
  if (!activeDsId.value) return false
  const currentDs = dataSourceList.value.find(ds => ds.pkFsoDs === activeDsId.value)
  return currentDs?.isDefault === 1 || currentDs?.isDefault === true
})

// 当前选中数据源的标签
const currentDsTags = computed(() => {
  if (!activeDsId.value) return []
  const currentDs = dataSourceList.value.find(ds => ds.pkFsoDs === activeDsId.value)
  return currentDs?.tags ? parseTags(currentDs.tags) : []
})

// 解析标签字符串 "生产:primary,测试:warning" -> [{label: '生产', type: 'primary'}, ...]
const parseTags = (tagsStr: string) => {
  if (!tagsStr) return []
  return tagsStr.split(',').map(tag => {
    const [label, type] = tag.split(':')
    return { label: label || '', type: type || 'info' }
  }).filter(t => t.label)
}

// 设置/取消默认数据源
const handleSetDefault = async () => {
  if (!activeDsId.value) return
  try {
    await request.post('/fso/datasource/set-default', null, {
      params: { pkFsoDs: activeDsId.value }
    })
    ElMessage.success(currentDsIsDefault.value ? '已取消默认' : '已设为默认')
    await fetchDataSourceList()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 切换数据源
const handleDsChange = async (val: string) => {
  activeBucket.value = ''
  objectList.value = []
  if (val) {
    await fetchBucketList()
  } else {
    bucketList.value = []
  }
}

// 新建数据源
const handleAddDs = () => {
  isEdit.value = false
  editDsData.value = null
  dsDialogVisible.value = true
}

// 编辑数据源
const handleEditDs = () => {
  const ds = dataSourceList.value.find((d: any) => d.pkFsoDs === activeDsId.value)
  if (!ds) return
  isEdit.value = true
  editDsData.value = ds
  dsDialogVisible.value = true
}

// 数据源保存成功回调
const handleDsSaved = async () => {
  await fetchDataSourceList()
  if (isEdit.value && activeDsId.value === editDsData.value?.pkFsoDs) {
    fetchBucketList()
  }
}

// 删除数据源
const handleDeleteDs = async () => {
  const ds = dataSourceList.value.find((d: any) => d.pkFsoDs === activeDsId.value)
  if (!ds) return
  try {
    await ElMessageBox.confirm('确定删除数据源 "' + ds.name + '" 吗？', '提示', { type: 'warning' })
    await request.get(`/fso/datasource/delete/${ds.pkFsoDs}`)
    ElMessage.success('删除成功')
    activeDsId.value = ''
    bucketList.value = []
    activeBucket.value = ''
    objectList.value = []
    fetchDataSourceList()
  } catch (e) { /* cancel */ }
}

// 获取桶列表
const fetchBucketList = async () => {
  if (!activeDsId.value) return
  try {
    bucketLoading.value = true
    const data = await request.get('/fso/bucket/list', { params: { pkFsoDs: activeDsId.value } })
    bucketList.value = data || []
  } catch (e) {
    // 拦截器已处理，这里额外清空
    bucketList.value = []
  } finally {
    bucketLoading.value = false
  }
}

// 选择桶
const handleSelectBucket = async (bucket: any) => {
  activeBucket.value = bucket.name
  await fetchObjectList(bucket.name)
}

// 创建桶
const handleCreateBucket = async () => {
  if (!bucketForm.name) { ElMessage.warning('请输入桶名'); return }
  try {
    await request.get('/fso/bucket/create', { params: { pkFsoDs: activeDsId.value, bucketName: bucketForm.name } })
    ElMessage.success('创建成功')
    showCreateBucket.value = false
    bucketForm.name = ''
    fetchBucketList()
  } catch (e) { /* 拦截器处理 */ }
}

// 删除桶
const handleDeleteBucket = async (bucket: any) => {
  try {
    await ElMessageBox.confirm('确定删除桶 "' + bucket.name + '" 吗？', '提示', { type: 'warning' })
    await request.get('/fso/bucket/delete', { params: { pkFsoDs: activeDsId.value, bucketName: bucket.name } })
    ElMessage.success('删除成功')
    if (activeBucket.value === bucket.name) {
      activeBucket.value = ''
      objectList.value = []
    }
    fetchBucketList()
  } catch (e) { /* cancel */ }
}

// 查看桶详情
const handleBucketDetail = async (bucket: any) => {
  currentBucket.value = bucket
  showBucketDetail.value = true
}

// 获取对象列表
const fetchObjectList = async (bucketName: string) => {
  if (!activeDsId.value) return
  try {
    objectLoading.value = true
    const data: any = await request.get('/fso/object/list', { params: { pkFsoDs: activeDsId.value, bucketName, keyword: objectKeyword.value || undefined } })
    objectList.value = data?.list || []
  } catch (e) {
    objectList.value = []
  } finally {
    objectLoading.value = false
  }
}

// 对象搜索
const handleObjectSearch = (keyword: string) => {
  objectKeyword.value = keyword
  fetchObjectList(activeBucket.value)
}

// 下载对象
const handleDownloadObject = (row: any) => {
  const url = `/platform/fso/object/download?pkFsoDs=${activeDsId.value}&bucketName=${activeBucket.value}&objectKey=${encodeURIComponent(row.key)}`
  window.open(url, '_blank')
}

// 删除对象
const handleDeleteObject = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除对象 "' + row.key + '" 吗？', '提示', { type: 'warning' })
    await request.get('/fso/object/delete', { params: { pkFsoDs: activeDsId.value, bucketName: activeBucket.value, objectKey: row.key } })
    ElMessage.success('删除成功')
    fetchObjectList(activeBucket.value)
  } catch (e) { /* cancel */ }
}

onMounted(() => {
  fetchDataSourceList()
})
</script>

<style lang="less" scoped>
.default-star {
  cursor: pointer;
  transition: color 0.2s;
  margin-left: 8px;
  &:hover {
    transform: scale(1.2);
  }
}

.fso-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  color: #303133;
  font-size: 13px;
}

/* 顶部工具条 */
.fso-toolbar {
  height: 38px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  flex-shrink: 0;
  border-radius: 4px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 主体区域 */
.fso-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  padding: 5px;
  gap: 5px;
}

/* 右侧占位 */
.fso-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* 空状态 */
.fso-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  padding: 5px;

  p {
    margin-top: 12px;
    font-size: 14px;
  }
}
</style>

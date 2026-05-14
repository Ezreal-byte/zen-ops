<template>
  <div class="redis-container">
    <!-- 顶部工具条：数据源管理 -->
    <div class="redis-toolbar">
      <div class="toolbar-left">
        <el-select
          v-model="activeDsId"
          placeholder="选择Redis数据源"
          size="small"
          style="width: 200px"
          @change="handleDsChange"
          clearable
        >
          <el-option
            v-for="ds in dataSourceList"
            :key="ds.pkRedisDs"
            :label="ds.name"
            :value="ds.pkRedisDs"
          >
            <span>
              <el-icon v-if="ds.isDefault === 1 || ds.isDefault === true" color="#E6A23C" style="margin-right: 4px"><StarFilled /></el-icon>
              {{ ds.name }}
            </span>
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

        <div v-if="activeDs" class="ds-info">
          <el-tag type="info" effect="plain">
            {{ activeDs.host }}:{{ activeDs.port }}
          </el-tag>
        </div>
      </div>
      <div class="toolbar-right">
        <el-button size="small" type="primary" @click="handleAddDs">新建数据源</el-button>
        <el-button size="small" @click="handleEditDs" :disabled="!activeDsId">编辑</el-button>
        <el-button size="small" type="danger" @click="handleDeleteDs" :disabled="!activeDsId">删除</el-button>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="redis-main" v-if="activeDsId">
      <!-- 左侧：数据库列表 -->
      <div class="db-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">Databases</span>
          <el-icon class="refresh-icon" @click="fetchDbList" :class="{ 'rotating': dbLoading }"><Refresh /></el-icon>
        </div>
        <div class="db-list" v-loading="dbLoading">
          <div
            v-for="db in dbList"
            :key="db.index"
            class="db-item"
            :class="{ active: activeDb === db.index }"
            @click="handleSelectDb(db.index)"
          >
            <img :src="redisIcon" class="db-icon" alt="redis" />
            <span class="db-name">{{ db.name }}</span>
            <span class="db-count" v-if="db.size !== undefined">{{ db.size }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧：缓存Keys表格 -->
      <div class="key-content" v-if="activeDb !== null">
        <div class="content-header">
          <div class="header-left">
            <el-input
              v-model="keyword"
              placeholder="搜索Key..."
              size="small"
              style="width: 220px"
              clearable
              :prefix-icon="Search"
              @keyup.enter="handleSearch"
            />
            <el-select v-model="filterType" placeholder="类型筛选" size="small" style="width: 120px" clearable @change="handleSearch">
              <el-option label="String" value="string" />
              <el-option label="Hash" value="hash" />
              <el-option label="List" value="list" />
              <el-option label="Set" value="set" />
              <el-option label="ZSet" value="zset" />
            </el-select>
            <el-button size="small" type="primary" @click="handleSearch">查询</el-button>
          </div>
          <div class="header-right">
            <span class="refresh-time" v-if="refreshTime">上次刷新：{{ refreshTime }}</span>
            <div
              class="view-toggle-wrapper"
              @click="toggleFlat"
              :title="isFlat ? '切换为层级显示' : '切换为平铺显示'"
            >
              <el-icon class="view-toggle-icon" :class="{ active: isFlat }">
                <component :is="isFlat ? 'SetUp' : 'Menu'" />
              </el-icon>
            </div>
            <el-button size="small" type="primary" @click="handleAddKey">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
          </div>
        </div>

        <div class="content-table-wrapper">
          <el-table
            ref="tableRef"
            :data="tableData"
            v-loading="keyLoading"
            empty-text="暂无数据"
            size="small"
            style="flex: 1; min-height: 0;"
            stripe
            border
            row-key="key"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          >
            <el-table-column label="Key" min-width="300">
              <template #default="{ row }">
                <!-- 层级模式的父级节点 -->
                <span v-if="row.isParent" class="parent-key-text" @click="handleToggleRow(row)">
                  {{ row.parent }}
                  <el-tag size="small" type="info" style="margin-left: 6px">{{ row.childCount }} 个</el-tag>
                </span>
                <!-- 层级模式的子节点或平铺模式的完整key -->
                <span v-else>{{ isFlat ? row.key : (row.hasParent ? row.child : row.key) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <span v-if="!row.isParent">
                  <el-tag :type="getTypeColor(row.type)" size="small">{{ row.type }}</el-tag>
                </span>
              </template>
            </el-table-column>
            <el-table-column label="值预览" min-width="200">
              <template #default="{ row }">
                <span class="preview-text">{{ row.isParent ? '' : (row.preview || '-') }}</span>
              </template>
            </el-table-column>
            <el-table-column label="大小" width="100">
              <template #default="{ row }">
                <span class="size-text">{{ row.sizeText || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="TTL" width="120">
              <template #default="{ row }">
                <span v-if="!row.isParent">
                  {{ row.ttlFormat || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <template v-if="!row.isParent">
                  <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
                  <el-button v-if="row.type === 'string'" link type="success" size="small" @click="handleEdit(row)">编辑</el-button>
                  <el-button link type="warning" size="small" @click="handleSetExpiry(row)">过期</el-button>
                  <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页组件 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[50, 100, 200, 500]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              background
              small
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
      <div v-else class="redis-placeholder">
        <span style="color: #909399">请从左侧选择一个数据库</span>
      </div>
    </div>

    <!-- 未选择数据源 -->
    <div class="redis-empty" v-else>
      <el-icon :size="48" color="#c0c4cc"><Connection /></el-icon>
      <p>请选择或新建一个Redis数据源开始使用</p>
    </div>

    <!-- 新建/编辑数据源弹窗 -->
    <el-dialog v-model="dsDialogVisible" :title="isEdit ? '编辑数据源' : '新建数据源'" width="500px" destroy-on-close>
      <el-form :model="dsForm" label-width="100px">
        <el-form-item label="数据源名称" required>
          <el-input v-model="dsForm.name" placeholder="请输入数据源名称"/>
        </el-form-item>
        <el-form-item label="主机地址" required>
          <el-input v-model="dsForm.host" placeholder="例如: 127.0.0.1"/>
        </el-form-item>
        <el-form-item label="端口号" required>
          <el-input v-model="dsForm.port" placeholder="例如: 6379"/>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="dsForm.password" placeholder="请输入密码" type="password" show-password/>
        </el-form-item>
        <el-form-item label="超时时间(ms)">
          <el-input-number v-model="dsForm.timeout" :min="1000" :max="10000" :step="1000"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dsForm.des" placeholder="请输入描述" type="textarea" :rows="3"/>
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="selectedTags"
            multiple
            filterable
            placeholder="选择标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in predefinedTags"
              :key="tag.type"
              :label="tag.label"
              :value="`${tag.label}:${tag.type}`"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dsDialogVisible = false">取消</el-button>
        <el-button @click="handleTestConnectionInDialog" :loading="testingConnection">
          {{ testingConnection ? '测试中...' : '测试连接' }}
        </el-button>
        <el-button type="primary" @click="handleSaveDs">保存</el-button>
      </template>
    </el-dialog>

    <!-- Key详情弹窗 -->
    <el-dialog v-model="detailVisible" title="Key详情" width="600px" destroy-on-close>
      <el-descriptions :column="1" border size="small" v-loading="detailLoading">
        <el-descriptions-item label="Key">{{ detailData.key }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailData.type }}</el-descriptions-item>
        <el-descriptions-item label="TTL">{{ detailData.ttl > 0 ? detailData.ttl + 's' : '永久' }}</el-descriptions-item>
        <el-descriptions-item label="值">
          <div v-if="detailData.tooLarge" class="large-value-notice">
            <el-icon :size="20" color="#e6a23c"><WarningFilled /></el-icon>
            <div class="notice-text">
              <div class="notice-title">该值过大，无法直接显示</div>
              <div class="notice-size">大小：{{ detailData.sizeText }}</div>
              <el-button size="small" type="primary" @click="handleDownloadValue" style="margin-top: 10px">
                <el-icon><Download /></el-icon>
                下载查看
              </el-button>
            </div>
          </div>
          <pre v-else class="value-pre">{{ formatValue(detailData.value) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 设置过期时间弹窗 -->
    <el-dialog v-model="expiryVisible" title="设置过期时间" width="500px" destroy-on-close>
      <el-form :model="expiryForm" label-width="100px" size="small">
        <el-form-item label="Key">
          <el-input v-model="expiryForm.key" disabled size="small" />
        </el-form-item>
        <el-form-item label="过期时间" required>
          <div style="display: flex; align-items: center; gap: 8px; width: 100%">
            <el-select v-model="expiryForm.ttlType" placeholder="过期类型" size="small" style="width: 150px" @change="handleExpiryTtlTypeChange">
              <el-option label="无过期时间" value="none" />
              <el-option label="过期时间（秒）" value="seconds" />
              <el-option label="过期时间（本地时间）" value="datetime" />
            </el-select>
            <el-input-number
              v-if="expiryForm.ttlType === 'seconds'"
              v-model="expiryForm.seconds"
              :min="1"
              :max="86400"
              :value="3600"
              size="small"
              controls-position="right"
              style="width: 150px"
              placeholder="秒数"
            />
            <el-date-picker
              v-if="expiryForm.ttlType === 'datetime'"
              v-model="expiryForm.expiryDatetime"
              type="datetime"
              placeholder="选择日期时间"
              size="small"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 200px"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="expiryVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="handleSaveExpiry">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑String值弹窗 -->
    <el-dialog v-model="editVisible" title="编辑Key值" width="600px" destroy-on-close>
      <el-form :model="editForm" label-width="80px" size="small">
        <el-form-item label="Key">
          <el-input v-model="editForm.key" disabled size="small" />
        </el-form-item>
        <el-form-item label="值" required>
          <el-input
            v-model="editForm.value"
            type="textarea"
            :rows="10"
            placeholder="请输入值"
            size="small"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px">字符数：{{ editForm.value?.length || 0 }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="editVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增Key弹窗 -->
    <el-dialog v-model="addKeyVisible" title="新增Key" width="600px" destroy-on-close>
      <el-form :model="addKeyForm" label-width="100px" size="small">
        <el-form-item label="Key" required>
          <el-input v-model="addKeyForm.key" placeholder="例如: user:1001:name" size="small" />
        </el-form-item>
        <el-form-item label="数据类型" required>
          <el-select v-model="addKeyForm.type" placeholder="选择数据类型" size="small" style="width: 100%" disabled>
            <el-option label="String" value="string" />
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 4px">目前仅支持String类型</div>
        </el-form-item>
        <el-form-item label="值" required>
          <el-input
            v-model="addKeyForm.value"
            type="textarea"
            :rows="8"
            placeholder="请输入值"
            size="small"
          />
        </el-form-item>
        <el-form-item label="过期时间">
          <div style="display: flex; align-items: center; gap: 8px; width: 100%">
            <el-select v-model="addKeyForm.ttlType" placeholder="过期类型" size="small" style="width: 150px" @change="handleTtlTypeChange">
              <el-option label="无过期时间" value="none" />
              <el-option label="过期时间（秒）" value="seconds" />
              <el-option label="过期时间（本地时间）" value="datetime" />
            </el-select>
            <el-input-number
              v-if="addKeyForm.ttlType === 'seconds'"
              v-model="addKeyForm.ttl"
              :min="1"
              :max="86400"
              :value="3600"
              size="small"
              controls-position="right"
              style="width: 150px"
              placeholder="秒数"
            />
            <el-date-picker
              v-if="addKeyForm.ttlType === 'datetime'"
              v-model="addKeyForm.ttlDatetime"
              type="datetime"
              placeholder="选择日期时间"
              size="small"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 200px"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="addKeyVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="handleSaveAddKey">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Connection, Search, WarningFilled, Download, SetUp, Menu, Refresh, Plus, StarFilled } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

// 使用 new URL 动态加载图标，Vite 打包后路径正确
const redisIcon = new URL('/icons/redis.svg', import.meta.url).href

// 组件名称，用于keep-alive缓存
defineOptions({ name: 'RedisOps' })

// 数据源
const dataSourceList = ref<any[]>([])
const activeDsId = ref<string>('')

// 当前选中的数据源
const activeDs = computed(() => {
  return dataSourceList.value.find((d: any) => d.pkRedisDs === activeDsId.value) || null
})

// 数据库
const dbList = ref<any[]>([])
const dbLoading = ref(false)
const activeDb = ref<number | null>(null)

// Keys
const keyList = ref<any[]>([])
const tableData = ref<any[]>([])
const keyLoading = ref(false)
const keyword = ref('*')
const refreshTime = ref('')
const tableRef = ref()
const filterType = ref('')
const isFlat = ref(true)

// 分页
const currentPage = ref(1)
const pageSize = ref(100)
const total = ref(0)

// 数据源对话框
const dsDialogVisible = ref(false)
const isEdit = ref(false)
const testingConnection = ref(false)
const connectionResult = ref<any>(null)
const dsForm = reactive<any>({
  name: '',
  host: '',
  port: '6379',
  password: '',
  timeout: 3000,
  des: ''
})

// 预设标签
const predefinedTags = ref<any[]>([])
const selectedTags = ref<string[]>([])

const fetchTags = async () => {
  try {
    predefinedTags.value = await request.get('/tags/list')
  } catch (e) { /* ignore */ }
}
fetchTags()

// Key详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<any>({})

// 过期时间
const expiryVisible = ref(false)
const expiryForm = reactive({
  key: '',
  ttlType: 'seconds',
  seconds: 3600,
  expiryDatetime: ''
})

// 编辑String值
const editVisible = ref(false)
const editForm = reactive({
  key: '',
  value: ''
})

// 新增Key
const addKeyVisible = ref(false)
const addKeyForm = reactive({
  key: '',
  type: 'string',
  value: '',
  ttlType: 'none',
  ttl: 0,
  ttlDatetime: ''
})

// 获取数据源列表
const fetchDataSourceList = async () => {
  try {
    const data = await request.get('/redis/datasource/list')
    dataSourceList.value = data || []
    // 自动选择默认数据源
    if (!activeDsId.value && dataSourceList.value.length > 0) {
      const defaultDs = dataSourceList.value.find((ds: any) => ds.isDefault === 1 || ds.isDefault === true)
      if (defaultDs) {
        activeDsId.value = defaultDs.pkRedisDs
        handleDsChange(defaultDs.pkRedisDs)
      }
    }
  } catch (e) { /* 拦截器处理 */ }
}

// 计算当前数据源是否为默认
const currentDsIsDefault = computed(() => {
  if (!activeDsId.value) return false
  const currentDs = dataSourceList.value.find(ds => ds.pkRedisDs === activeDsId.value)
  return currentDs?.isDefault === 1 || currentDs?.isDefault === true
})

// 解析标签字符串
const parseTags = (tagsStr: string) => {
  if (!tagsStr) return []
  return tagsStr.split(',').map(tag => {
    const [label, type] = tag.split(':')
    return { label: label || '', type: type || 'info' }
  }).filter(t => t.label)
}

// 当前选中数据源的标签
const currentDsTags = computed(() => {
  if (!activeDsId.value) return []
  const currentDs = dataSourceList.value.find(ds => ds.pkRedisDs === activeDsId.value)
  return currentDs?.tags ? parseTags(currentDs.tags) : []
})

// 设置/取消默认数据源
const handleSetDefault = async () => {
  if (!activeDsId.value) return
  try {
    await request.post('/redis/datasource/set-default', null, {
      params: { pkRedisDs: activeDsId.value }
    })
    ElMessage.success(currentDsIsDefault.value ? '已取消默认' : '已设为默认')
    await fetchDataSourceList()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 切换数据源
const handleDsChange = async (val: string) => {
  activeDb.value = null
  keyList.value = []
  currentPage.value = 1 // 重置分页
  total.value = 0
  if (val) {
    await fetchDbList()
  } else {
    dbList.value = []
  }
}

// 获取数据库列表
const fetchDbList = async () => {
  if (!activeDsId.value) return
  try {
    dbLoading.value = true
    const data = await request.get('/redis/db/list', { params: { pkRedisDs: activeDsId.value } })
    dbList.value = data || []
  } catch (e) {
    dbList.value = []
  } finally {
    dbLoading.value = false
  }
}

// 选择数据库
const handleSelectDb = async (dbIndex: number) => {
  activeDb.value = dbIndex
  keyword.value = '*'
  await fetchKeyList()
}

// 获取Keys列表
const fetchKeyList = async () => {
  if (!activeDsId.value || activeDb.value === null) return
  try {
    keyLoading.value = true
    const data: any = await request.get('/redis/keys/list', {
      params: {
        pkRedisDs: activeDsId.value,
        dbIndex: activeDb.value,
        pattern: keyword.value || '*',
        type: filterType.value || undefined,
        flat: isFlat.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    tableData.value = data?.list || []
    total.value = data?.total || 0
    refreshTime.value = data?.refreshTime || ''
  } catch (e) {
    tableData.value = []
    total.value = 0
  } finally {
    keyLoading.value = false
  }
}

// 分页 - 页码变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchKeyList()
}

// 分页 - 每页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
  fetchKeyList()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1 // 重置到第一页
  fetchKeyList()
}

// 切换平铺/层级
const toggleFlat = () => {
  isFlat.value = !isFlat.value
  currentPage.value = 1 // 重置到第一页
  fetchKeyList()
}

// 切换行展开/收起
const handleToggleRow = (row: any) => {
  if (tableRef.value) {
    tableRef.value.toggleRowExpansion(row)
  }
}

// 编辑String值
const handleEdit = (row: any) => {
  editForm.key = row.key
  editForm.value = ''
  editVisible.value = true

  // 获取当前值
  request.get('/redis/key/detail', {
    params: {
      pkRedisDs: activeDsId.value,
      dbIndex: activeDb.value,
      key: row.key
    }
  }).then((data: any) => {
    editForm.value = data.value || ''
  }).catch(() => {
    ElMessage.error('获取值失败')
  })
}

// 保存编辑
const handleSaveEdit = async () => {
  if (!editForm.value) {
    ElMessage.warning('请输入值')
    return
  }
  try {
    await request.post('/redis/key/edit', null, {
      params: {
        pkRedisDs: activeDsId.value,
        dbIndex: activeDb.value,
        key: editForm.key,
        value: editForm.value
      }
    })
    ElMessage.success('更新成功')
    editVisible.value = false
    fetchKeyList()
  } catch (e) {
    // 错误由拦截器处理
  }
}

// 新增Key
const handleAddKey = () => {
  if (!activeDsId.value || activeDb.value === null) {
    ElMessage.warning('请先选择数据源和数据库')
    return
  }
  addKeyForm.key = ''
  addKeyForm.type = 'string'
  addKeyForm.value = ''
  addKeyForm.ttlType = 'none'
  addKeyForm.ttl = 0
  addKeyForm.ttlDatetime = ''
  addKeyVisible.value = true
}

// 过期类型切换
const handleTtlTypeChange = () => {
  // 切换时清空
  addKeyForm.ttlDatetime = ''
  // 秒模式默认3600
  if (addKeyForm.ttlType === 'seconds') {
    addKeyForm.ttl = 3600
  }
}

// 保存新增Key
const handleSaveAddKey = async () => {
  if (!addKeyForm.key || !addKeyForm.value) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    let ttlParam = 0
    let expiryTime = ''

    // 根据过期类型设置参数
    if (addKeyForm.ttlType === 'seconds') {
      if (!addKeyForm.ttl || addKeyForm.ttl <= 0) {
        ElMessage.warning('请输入有效的秒数')
        return
      }
      ttlParam = addKeyForm.ttl
    } else if (addKeyForm.ttlType === 'datetime') {
      if (!addKeyForm.ttlDatetime) {
        ElMessage.warning('请选择日期时间')
        return
      }
      expiryTime = addKeyForm.ttlDatetime
    }

    await request.post('/redis/key/add', null, {
      params: {
        pkRedisDs: activeDsId.value,
        dbIndex: activeDb.value,
        key: addKeyForm.key,
        value: addKeyForm.value,
        ttl: ttlParam,
        expiryTime: expiryTime
      }
    })
    ElMessage.success('新增成功')
    addKeyVisible.value = false
    fetchKeyList()
  } catch (e) {
    // 错误由拦截器处理
  }
}

// 新建数据源
const handleAddDs = () => {
  isEdit.value = false
  connectionResult.value = null
  Object.assign(dsForm, {
    name: '',
    host: '',
    port: '6379',
    password: '',
    timeout: 3000,
    des: ''
  })
  selectedTags.value = []
  dsDialogVisible.value = true
}

// 编辑数据源
const handleEditDs = () => {
  const ds = dataSourceList.value.find((d: any) => d.pkRedisDs === activeDsId.value)
  if (!ds) return
  isEdit.value = true
  connectionResult.value = null
  Object.assign(dsForm, ds)
  selectedTags.value = ds.tags ? ds.tags.split(',').filter((t: string) => t.trim()) : []
  dsDialogVisible.value = true
}

// 保存数据源
const handleSaveDs = async () => {
  if (!dsForm.name || !dsForm.host || !dsForm.port) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    const payload = { ...dsForm, tags: selectedTags.value.join(',') }
    if (isEdit.value) {
      await request.post('/redis/datasource/update', payload)
      ElMessage.success('更新成功')
    } else {
      await request.post('/redis/datasource/add', payload)
      ElMessage.success('创建成功')
    }
    dsDialogVisible.value = false
    fetchDataSourceList()
  } catch (e) { /* 拦截器处理 */ }
}

// 在弹窗中测试连接
const handleTestConnectionInDialog = async () => {
  if (!dsForm.host || !dsForm.port) {
    ElMessage.warning('请先填写主机地址和端口号')
    return
  }
  try {
    testingConnection.value = true
    connectionResult.value = null
    // 使用表单中的临时数据测试连接
    const data: any = await request.post('/redis/datasource/test', {
      host: dsForm.host,
      port: dsForm.port,
      password: dsForm.password,
      timeout: dsForm.timeout
    })
    connectionResult.value = data
    if (data.success) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.error('连接失败: ' + data.message)
    }
  } catch (e) {
    connectionResult.value = { success: false, message: '连接失败' }
  } finally {
    testingConnection.value = false
  }
}

// 删除数据源
const handleDeleteDs = async () => {
  const ds = dataSourceList.value.find((d: any) => d.pkRedisDs === activeDsId.value)
  if (!ds) return
  try {
    await ElMessageBox.confirm('确定删除数据源 "' + ds.name + '" 吗？', '提示', { type: 'warning' })
    await request.get(`/redis/datasource/delete/${ds.pkRedisDs}`)
    ElMessage.success('删除成功')
    activeDsId.value = ''
    dbList.value = []
    activeDb.value = null
    keyList.value = []
    fetchDataSourceList()
  } catch (e) { /* cancel */ }
}

// 测试连接
const handleTestConnection = async () => {
  if (!activeDsId.value) return
  try {
    const data: any = await request.get(`/redis/datasource/test/${activeDsId.value}`)
    if (data && data.success) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.error('连接失败: ' + (data?.message || '未知错误'))
    }
  } catch (e) { /* 拦截器处理 */ }
}

// Key详情
const handleDetail = async (row: any) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const data = await request.get('/redis/key/detail', {
      params: {
        pkRedisDs: activeDsId.value,
        dbIndex: activeDb.value,
        key: row.key
      }
    })
    detailData.value = data || {}
  } catch (e) {
    detailData.value = row
  } finally {
    detailLoading.value = false
  }
}

// 下载大值
const handleDownloadValue = () => {
  const url = `/platform/redis/key/download?pkRedisDs=${activeDsId.value}&dbIndex=${activeDb.value}&key=${encodeURIComponent(detailData.value.key)}`
  window.open(url, '_blank')
}

// 设置过期时间
const handleSetExpiry = (row: any) => {
  expiryForm.key = row.key
  expiryForm.ttlType = 'seconds'
  expiryForm.seconds = row.ttl > 0 ? row.ttl : 3600
  expiryForm.expiryDatetime = ''
  expiryVisible.value = true
}

// 过期类型切换
const handleExpiryTtlTypeChange = () => {
  // 切换时清空
  expiryForm.expiryDatetime = ''
  // 秒模式默认3600
  if (expiryForm.ttlType === 'seconds') {
    expiryForm.seconds = 3600
  }
}

const handleSaveExpiry = async () => {
  try {
    let ttlParam = 0
    let expiryTime = ''

    // 根据过期类型设置参数
    if (expiryForm.ttlType === 'seconds') {
      if (!expiryForm.seconds || expiryForm.seconds <= 0) {
        ElMessage.warning('请输入有效的秒数')
        return
      }
      ttlParam = expiryForm.seconds
    } else if (expiryForm.ttlType === 'datetime') {
      if (!expiryForm.expiryDatetime) {
        ElMessage.warning('请选择日期时间')
        return
      }
      expiryTime = expiryForm.expiryDatetime
    } else if (expiryForm.ttlType === 'none') {
      // 无过期时间，传递-1表示移除过期时间
      ttlParam = -1
    }

    await request.get('/redis/key/expiry', {
      params: {
        pkRedisDs: activeDsId.value,
        dbIndex: activeDb.value,
        key: expiryForm.key,
        ttl: ttlParam,
        expiryTime: expiryTime
      }
    })
    ElMessage.success('设置成功')
    expiryVisible.value = false
    fetchKeyList()
  } catch (e) { /* 拦截器处理 */ }
}

// 删除Key
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除 Key "' + row.key + '" 吗？', '提示', { type: 'warning' })
    await request.get('/redis/key/delete', {
      params: {
        pkRedisDs: activeDsId.value,
        dbIndex: activeDb.value,
        key: row.key
      }
    })
    ElMessage.success('删除成功')
    fetchKeyList()
  } catch (e) { /* cancel */ }
}

// 格式化值显示
const formatValue = (value: any) => {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2)
  }
  return String(value)
}

// 获取类型颜色
const getTypeColor = (type: string) => {
  const colorMap: any = {
    string: 'primary',
    hash: 'success',
    list: 'warning',
    set: 'danger',
    zset: 'info'
  }
  return colorMap[type] || ''
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

.redis-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  color: #303133;
  font-size: 13px;
}

/* 顶部工具条 */
.redis-toolbar {
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

.ds-info {
  // el-tag自带样式，无需额外设置
}

/* 主体区域 */
.redis-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  padding: 5px;
  gap: 5px;
}

/* 左侧数据库列表 */
.db-sidebar {
  width: 200px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.sidebar-header {
  height: 34px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
  background: #fff;

  .refresh-icon {
    cursor: pointer;
    color: #909399;
    font-size: 14px;
    transition: all 0.3s;

    &:hover {
      color: #409eff;
      transform: rotate(180deg);
    }

    &.rotating {
      animation: rotate 1s linear infinite;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.sidebar-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.db-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.db-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 4px;
  background: #f8f9fa;
  border: 1px solid transparent;

  &:hover {
    background: #f0f2f5;
    border-color: #dcdfe6;
  }

  &.active {
    background: #ecf5ff;
    border-color: #409eff;
  }
}

.db-icon {
  flex-shrink: 0;
  width: 14px;
  height: 14px;
  margin-right: 6px;
}

.db-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  font-weight: 500;
}

.db-count {
  font-size: 11px;
  color: #909399;
  padding: 2px 6px;
  background: #f0f2f5;
  border-radius: 3px;
  margin-left: 6px;
}

/* 右侧内容区 */
.key-content {
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

.header-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;

  .refresh-time {
    font-size: 11px;
    color: #909399;
  }

  .view-toggle-wrapper {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.3s;

    &:hover {
      background: #f0f2f5;
    }

    .view-toggle-icon {
      font-size: 16px;
      color: #909399;
      transition: all 0.3s;

      &.active {
        color: #409eff;
      }
    }

    &:hover .view-toggle-icon {
      color: #409eff;
    }
  }
}

.content-table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 5px;
}

.preview-text {
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.size-text {
  color: #909399;
  font-size: 11px;
}

.parent-key-text {
  color: #409eff;
  cursor: pointer;
  font-weight: 500;

  &:hover {
    color: #66b1ff;
    text-decoration: underline;
  }
}

/* 占位符 */
.redis-placeholder {
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
.redis-empty {
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

.value-pre {
  margin: 0;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  max-height: 300px;
  overflow: auto;
  word-break: break-all;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.large-value-notice {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fdf6ec;
  border: 1px solid #e6a23c;
  border-radius: 4px;

  .notice-text {
    flex: 1;

    .notice-title {
      font-size: 14px;
      font-weight: 500;
      color: #e6a23c;
      margin-bottom: 6px;
    }

    .notice-size {
      font-size: 12px;
      color: #909399;
    }
  }
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

/* 分页组件样式 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 10px 5px 5px 5px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0; /* 防止被压缩 */
}
</style>

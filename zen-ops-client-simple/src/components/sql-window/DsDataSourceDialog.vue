<script setup lang="ts">
import {ref, reactive, watch, nextTick, computed, onMounted} from 'vue'
import {request} from '@/utils/request'
import {ElMessage, ElMessageBox} from 'element-plus'
import {ArrowLeft, Plus, Star, StarFilled} from '@element-plus/icons-vue'

// 使用 new URL 动态加载图标，Vite 打包后路径正确
const mysqlIcon = new URL('/icons/Mysql.svg', import.meta.url).href
const oracleIcon = new URL('/icons/Oracle.svg', import.meta.url).href
const postgresqlIcon = new URL('/icons/PostgreSQL.svg', import.meta.url).href
const clickhouseIcon = new URL('/icons/ClickHouse.svg', import.meta.url).href
const databaseIcon = new URL('/icons/database.svg', import.meta.url).href

// 计算数据库类型图标路径
const getDbTypeIcon = (dbType: string) => {
  const iconMap: Record<string, string> = {
    'MYSQL': mysqlIcon,
    'ORACLE': oracleIcon,
    'POSTGRE_SQL': postgresqlIcon,
    'CLICK_HOUSE': clickhouseIcon
  }
  return iconMap[dbType] || databaseIcon
}

const props = defineProps<{ modelValue: boolean; selectMode?: boolean }>()
const emit = defineEmits(['update:modelValue', 'refresh', 'select'])

const visible = ref(props.modelValue)
watch(() => props.modelValue, val => { visible.value = val })
watch(visible, val => { emit('update:modelValue', val) })

const list = ref<any[]>([])
const loading = ref(false)
const dialogEdit = ref(false)
const isEdit = ref(false)
const editPk = ref<string | null>(null)
const step = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref()
const predefinedTags = ref<any[]>([])
const selectedTags = ref<string[]>([])
const form = reactive({
  dbType: 'MYSQL',
  connType: 'HOST',
  name: '',
  host: '',
  port: '',
  dbSchema: '',
  url: '',
  userName: '',
  userPwd: '',
  driver: '',
  connMax: 10,
  connMin: 1,
  des: '',
  tags: ''
})

// 获取标签列表
const fetchTags = async () => {
  try {
    predefinedTags.value = await request.get('/tags/list')
  } catch (e) { /* ignore */ }
}

// 解析标签
const parseTags = (tagsStr: string) => {
  if (!tagsStr) return []
  return tagsStr.split(',').filter(t => t.trim()).map(tagStr => {
    const parts = tagStr.split(':')
    return {
      label: parts[0],
      type: parts[1] || ''
    }
  })
}

// 切换收藏状态
const toggleFavorite = async (row: any) => {
  try {
    await request.post(`/ds/datasource/favorite/${row.pkDs}`)
    ElMessage.success(row.dtFavorite ? '已取消收藏' : '已收藏')
    loadList()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const dbTypes = [
  { label: 'MySQL', value: 'MYSQL', icon: mysqlIcon, desc: '最流行的开源关系型数据库', defaultPort: '3306' },
  { label: 'Oracle', value: 'ORACLE', icon: oracleIcon, desc: '企业级商业数据库', defaultPort: '1521' },
  { label: 'PostgreSQL', value: 'POSTGRE_SQL', icon: postgresqlIcon, desc: '功能强大的开源对象关系数据库', defaultPort: '5432' },
  { label: 'ClickHouse', value: 'CLICK_HOUSE', icon: clickhouseIcon, desc: '高性能列式分析型数据库', defaultPort: '8123' }
]

const currentTypeLabel = computed(() => {
  const t = dbTypes.find((t: any) => t.value === form.dbType)
  return t?.label || form.dbType
})

const connTypes = [
  { label: '主机', value: 'HOST' },
  { label: 'URL', value: 'URL' }
]

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  dbType: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
  connType: [{ required: true, message: '请选择连接方式', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口号', trigger: 'blur' }],
  dbSchema: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userPwd: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try {
    const data: any = await request.get('/ds/datasource/queryByPage', { params: { pageNum: pageNum.value, pageSize: pageSize.value } })
    list.value = data?.list || []
    total.value = data?.totalCount || 0
  } finally {
    loading.value = false
  }
}

const onPageChange = (val: number) => {
  pageNum.value = val
  loadList()
}

const onSizeChange = (val: number) => {
  pageSize.value = val
  pageNum.value = 1
  loadList()
}

const openAdd = () => {
  isEdit.value = false
  editPk.value = null
  step.value = 0
  resetForm()
  dialogEdit.value = true
}

const selectType = (type: string) => {
  form.dbType = type
  const t = dbTypes.find((item: any) => item.value === type)
  if (t?.defaultPort) {
    form.port = t.defaultPort
  }
  step.value = 1
}

const openEdit = (row: any) => {
  isEdit.value = true
  editPk.value = row.pkDs
  Object.assign(form, {
    dbType: row.dbType || 'MYSQL',
    connType: row.connType || 'HOST',
    name: row.name || '',
    host: row.host || '',
    port: row.port || '',
    dbSchema: row.dbSchema || '',
    url: row.url || '',
    userName: row.userName || '',
    userPwd: row.userPwd || '',
    driver: row.driver || '',
    connMax: row.connMax || 10,
    connMin: row.connMin || 1,
    des: row.des || '',
    tags: row.tags || ''
  })
  // 处理标签
  selectedTags.value = row.tags ? row.tags.split(',').filter((t: string) => t.trim()) : []
  dialogEdit.value = true
}

const openClone = (row: any) => {
  isEdit.value = false
  editPk.value = null
  step.value = 1 // 克隆时直接跳到第二步（填写信息）
  // 克隆数据，名称后面追加 _copy
  Object.assign(form, {
    dbType: row.dbType || 'MYSQL',
    connType: row.connType || 'HOST',
    name: (row.name || '') + '_copy',
    host: row.host || '',
    port: row.port || '',
    dbSchema: row.dbSchema || '',
    url: row.url || '',
    userName: row.userName || '',
    userPwd: row.userPwd || '',
    driver: row.driver || '',
    connMax: row.connMax || 10,
    connMin: row.connMin || 1,
    des: row.des || '',
    tags: row.tags || ''
  })
  // 处理标签
  selectedTags.value = row.tags ? row.tags.split(',').filter((t: string) => t.trim()) : []
  dialogEdit.value = true
}

const onDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该数据源?', '提示', { type: 'warning' })
    await request.get(`/ds/datasource/delete/${row.pkDs}`)
    ElMessage.success('删除成功')
    loadList()
    emit('refresh')
  } catch (e) { /* cancel */ }
}

const onTest = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      const payload = buildPayload()
      const data: any = await request.post('/ds/datasource/test/connection', payload)
      if (data?.success) {
        ElMessage.success('连接成功')
      } else {
        ElMessage.error(data?.msg || '连接失败')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '连接失败')
    }
  })
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      const payload = buildPayload()
      if (isEdit.value && editPk.value) {
        await request.post('/ds/datasource/update', payload)
        ElMessage.success('更新成功')
      } else {
        await request.post('/ds/datasource/add', payload)
        ElMessage.success('创建成功')
      }
      dialogEdit.value = false
      step.value = 0
      loadList()
      emit('refresh')
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    }
  })
}

const buildPayload = () => {
  const payload: any = {
    dbType: form.dbType,
    connType: form.connType,
    name: form.name,
    host: form.host,
    port: form.port,
    dbSchema: form.dbSchema,
    url: form.url,
    userName: form.userName,
    userPwd: form.userPwd,
    driver: form.driver,
    connMax: form.connMax,
    connMin: form.connMin,
    des: form.des,
    tags: selectedTags.value.join(',')
  }
  if (isEdit.value && editPk.value) {
    payload.pkDs = editPk.value
  }
  return payload
}

const resetForm = () => {
  Object.assign(form, {
    dbType: 'MYSQL',
    connType: 'HOST',
    name: '',
    host: '',
    port: '',
    dbSchema: '',
    url: '',
    userName: '',
    userPwd: '',
    driver: '',
    connMax: 10,
    connMin: 1,
    des: '',
    tags: ''
  })
  selectedTags.value = []
  nextTick(() => formRef.value?.clearValidate())
}

watch(visible, (val) => {
  if (val) {
    loadList()
    fetchTags() // 打开弹窗时加载标签
  }
})
</script>

<template>
  <el-dialog v-model="visible" title="数据源管理" width="90vw" @close="dialogEdit = false" class="ds-dialog">
    <div class="ds-dialog-body">
      <div class="toolbar-wrap">
        <div></div>
        <el-button size="small" type="primary" :icon="Plus" @click="openAdd">新增数据源</el-button>
      </div>
      <el-table :data="list" v-loading="loading" size="small" stripe height="calc(75vh - 220px)">
        <el-table-column prop="name" label="名称" min-width="120"/>
        <el-table-column label="类型" width="60">
          <template #default="{ row }">
            <el-tooltip :content="row.dbType === 'MYSQL' ? 'MySQL' : row.dbType === 'ORACLE' ? 'Oracle' : row.dbType === 'POSTGRE_SQL' ? 'PostgreSQL' : row.dbType === 'CLICK_HOUSE' ? 'ClickHouse' : row.dbType" placement="top">
              <img class="db-type-icon" :src="getDbTypeIcon(row.dbType)" />
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="连接方式" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.connType === 'URL' || row.connType === '2' ? 'warning' : 'primary'" effect="plain">
              {{ row.connType === 'URL' || row.connType === '2' ? 'URL' : '主机' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="连接信息" min-width="200">
          <template #default="{ row }">
            <span v-if="row.connType === 'URL' || row.connType === '2'" class="conn-url" :title="row.url">{{ row.url || '-' }}</span>
            <span v-else>{{ row.host || '-' }}:{{ row.port || '-' }} / {{ row.dbSchema || '-' }}</span>
          </template>
        </el-table-column>
        <!-- 标签列 -->
        <el-table-column label="标签" min-width="150">
          <template #default="{ row }">
            <div class="tags-container">
              <el-tag
                v-for="(tag, index) in parseTags(row.tags)"
                :key="index"
                :type="tag.type"
                size="small"
                class="tag-item"
              >
                {{ tag.label }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <!-- 收藏列 -->
        <el-table-column label="收藏" width="60" align="center">
          <template #default="{ row }">
            <el-icon
              class="favorite-icon"
              :class="{ 'is-favorite': row.dtFavorite }"
              @click.stop="toggleFavorite(row)"
              :size="18"
            >
              <StarFilled v-if="row.dtFavorite" />
              <Star v-else />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-button v-if="selectMode" link type="primary" size="small" @click="emit('select', row)">连接</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="openClone(row)">克隆</el-button>
            <el-button link type="danger" size="small" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          size="small"
          background
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogEdit" :title="`${isEdit ? '编辑数据源' : '新增数据源'}` + `${step !== 0 ? ' | ' + currentTypeLabel : ''}`" width="860px" append-to-body destroy-on-close>
      <!-- 步骤一：选择类型（仅新建时显示） -->
      <div v-if="!isEdit && step === 0" class="type-select">
        <div
          v-for="t in dbTypes"
          :key="t.value"
          class="type-card"
          :class="{ active: form.dbType === t.value }"
          @click="selectType(t.value)"
        >
          <div class="type-icon">
            <img :src="t.icon" width="48" height="48" />
          </div>
          <div class="type-label">{{ t.label }}</div>
          <div class="type-desc">{{ t.desc }}</div>
        </div>
      </div>

      <!-- 步骤二：填写信息 -->
      <div v-else>
        <div v-if="!isEdit" class="step-back">
          <el-button link type="primary" @click="step = 0"><el-icon><ArrowLeft /></el-icon> 返回选择类型</el-button>
<!--          <el-tag size="small">{{ currentTypeLabel }}</el-tag>-->
        </div>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="margin-top: 8px">
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入数据源名称"/>
          </el-form-item>
          <el-form-item label="连接方式" prop="connType">
            <el-radio-group v-model="form.connType">
              <el-radio v-for="t in connTypes" :key="t.value" :label="t.value">{{ t.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <template v-if="form.connType === 'HOST'">
            <el-form-item label="主机地址" prop="host">
              <el-input v-model="form.host" placeholder="请输入IP或域名"/>
            </el-form-item>
            <el-form-item label="端口号" prop="port">
              <el-input v-model="form.port" placeholder="请输入端口号"/>
            </el-form-item>
            <el-form-item label="数据库" prop="dbSchema">
              <el-input v-model="form.dbSchema" placeholder="请输入数据库名/实例名"/>
            </el-form-item>
          </template>
          <template v-else>
            <el-form-item label="JDBC URL" prop="url">
              <el-input v-model="form.url" placeholder="请输入完整JDBC URL" type="textarea" :rows="2"/>
            </el-form-item>
          </template>
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="form.userName" placeholder="请输入用户名"/>
          </el-form-item>
          <el-form-item label="密码" prop="userPwd">
            <el-input v-model="form.userPwd" placeholder="请输入密码" type="password" show-password/>
          </el-form-item>
          <el-form-item label="驱动类">
            <el-input v-model="form.driver" placeholder="可选，留空将自动匹配"/>
          </el-form-item>
          <el-form-item label="连接池">
            <el-col :span="11">
              <el-input-number v-model="form.connMin" :min="1" :max="100" style="width: 100%"/>
            </el-col>
            <el-col :span="2" style="text-align: center">-</el-col>
            <el-col :span="11">
              <el-input-number v-model="form.connMax" :min="1" :max="500" style="width: 100%"/>
            </el-col>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.des" placeholder="可选"/>
          </el-form-item>
          <!-- 标签选择 -->
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
              >
                <el-tag size="small" :type="tag.type">{{ tag.label }}</el-tag>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="dialogEdit = false">取消</el-button>
        <el-button v-if="step === 0 && !isEdit" type="primary" :disabled="!form.dbType" @click="step = 1">下一步</el-button>
        <template v-else>
          <el-button @click="onTest">测试连接</el-button>
          <el-button type="primary" @click="onSubmit">确定</el-button>
        </template>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<style scoped lang="less">
.type-select {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding: 20px 0;
}

.type-card {
  flex: 1;
  max-width: 190px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  }

  &.active {
    border-color: #409eff;
    background: #ecf5ff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
  }
}

.type-icon {
  margin: 0 auto 10px;

  img {
    display: block;
    margin: 0 auto;
  }
}

.db-type-icon {
  width: 22px;
  height: 22px;
  display: block;
  margin: 0 auto;
}

.type-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.type-desc {
  font-size: 12px;
  color: #909399;
}

.step-back {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ds-dialog-body {
  display: flex;
  flex-direction: column;
  max-height: 75vh;
}

.toolbar-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

:deep(.ds-dialog .el-dialog__body) {
  padding-top: 8px;
}

// 收藏图标
.favorite-icon {
  cursor: pointer;
  color: #dcdfe6;
  transition: all 0.3s ease;

  &:hover {
    transform: scale(1.2);
  }

  &.is-favorite {
    color: #e6a23c;
  }
}

// 标签容器
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.tag-item {
  margin: 0;
}
</style>

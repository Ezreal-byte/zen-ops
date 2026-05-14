<template>
  <el-dialog
    v-model="visible"
    width="820px"
    destroy-on-close
    @close="handleClose"
  >
    <template #header>
      <span class="el-dialog__title">
        <span>{{ isEdit ? '编辑数据源' : '新建数据源' }}</span>
        <span v-if="step !== 0">
          <span> | {{ currentTypeLabel }}</span>
        </span>
      </span>
    </template>
    <!-- 步骤一：选择类型（仅新建时显示） -->
    <div v-if="!isEdit && step === 0" class="type-select">
      <div
        v-for="t in dsTypes"
        :key="t.code"
        class="type-card"
        :class="{ active: dsForm.type === t.code }"
        @click="selectType(t.code)"
      >
        <div class="type-icon">
          <img v-if="t.icon" :src="t.icon" width="48" height="48" class="type-icon-img" />
          <svg v-else viewBox="0 0 64 64" width="48" height="48">
            <rect x="8" y="8" width="48" height="48" rx="8" :fill="t.color || '#409eff'"/>
            <text x="32" y="40" text-anchor="middle" fill="#fff" font-size="14" font-weight="bold">{{ t.label && t.label.length <= 4 ? t.label : t.code.substring(0, 3) }}</text>
          </svg>
        </div>
        <div class="type-label">{{ t.label }}</div>
        <div class="type-desc">{{ t.desc }}</div>
      </div>
    </div>

    <!-- 步骤二：填写信息 -->
    <div v-else>
      <div v-if="!isEdit" class="step-back">
        <el-button link type="primary" @click="step = 0"><el-icon><ArrowLeft /></el-icon> 返回选择类型</el-button>
      </div>
      <el-form :model="dsForm" label-width="120px" :rules="dsRules" ref="formRef" style="margin-top: 8px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="dsForm.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <template v-for="field in currentFields" :key="field.key">
          <el-form-item :label="field.label" :prop="field.key">
            <el-input
              v-model="dsForm[field.key]"
              :placeholder="field.placeholder || ''"
              :type="field.inputType === 'password' ? 'password' : 'text'"
              :show-password="field.inputType === 'password'"
            />
          </el-form-item>
        </template>

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
      <el-button @click="handleClose" size="small">取消</el-button>
      <el-button v-if="step === 0 && !isEdit" type="primary" :disabled="!dsForm.type" @click="step = 1" size="small">下一步</el-button>

      <template v-else>
        <el-button @click="handleTestInDialog" :loading="testingConnection" size="small">
          {{ testingConnection ? '测试中...' : '测试连接' }}
        </el-button>
        <el-button type="primary" @click="handleSubmit" size="small">保存</el-button>
      </template>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

// 使用 new URL 预加载图标，Vite 打包后路径正确
const minioIcon = new URL('/icons/minio.svg', import.meta.url).href
const aliyunOssIcon = new URL('/icons/aliyun-oss.svg', import.meta.url).href
const rustFsIcon = new URL('/icons/rust-fs.svg', import.meta.url).href

// 图标路径映射（支持多种路径格式）
const iconPathMap: Record<string, string> = {
  'minio.svg': minioIcon,
  'aliyun-oss.svg': aliyunOssIcon,
  'rust-fs.svg': rustFsIcon,
  '/icons/minio.svg': minioIcon,
  '/icons/aliyun-oss.svg': aliyunOssIcon,
  '/icons/rust-fs.svg': rustFsIcon,
  'Minio.svg': minioIcon,
  'Aliyun-Oss.svg': aliyunOssIcon,
  'Rust-Fs.svg': rustFsIcon,
  '/icons/Minio.svg': minioIcon,
  '/icons/Aliyun-Oss.svg': aliyunOssIcon,
  '/icons/Rust-Fs.svg': rustFsIcon
}

const props = defineProps<{
  modelValue: boolean
  isEdit: boolean
  editData?: any
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'success'): void
}>()

// 转换图标路径
const convertIconPath = (iconPath: string) => {
  if (!iconPath) return ''
  // 如果已经是以 http 开头的完整 URL，直接返回
  if (iconPath.startsWith('http')) return iconPath
  // 尝试从映射表中查找
  const fileName = iconPath.split('/').pop() || iconPath
  return iconPathMap[iconPath] || iconPathMap[fileName] || iconPath
}

const visible = ref(props.modelValue)
watch(() => props.modelValue, val => { visible.value = val })
watch(visible, val => { emit('update:modelValue', val) })

const step = ref(0)
const formRef = ref<FormInstance>()
const testingConnection = ref(false)
const predefinedTags = ref<any[]>([])
const selectedTags = ref<string[]>([])

// 数据源类型模型（从后端获取）
const dsTypes = ref<any[]>([])

const fetchTypes = async () => {
  try {
    const data = await request.get('/fso/datasource/types')
    // 转换图标路径为 Vite 打包后的正确路径
    dsTypes.value = (data || []).map((type: any) => ({
      ...type,
      icon: convertIconPath(type.icon)
    }))
  } catch (e) { /* ignore */ }
}

onMounted(() => { fetchTypes(); fetchTags(); })

const fetchTags = async () => {
  try {
    predefinedTags.value = await request.get('/tags/list')
  } catch (e) { /* ignore */ }
}

// 当前选中类型的字段
const currentFields = computed(() => {
  const t = dsTypes.value.find((t: any) => t.code === dsForm.type)
  return t?.fields || []
})

const currentTypeLabel = computed(() => {
  const t = dsTypes.value.find((t: any) => t.code === dsForm.type)
  return t?.label || dsForm.type
})

// 当前类型的标签颜色类型
const currentTypeTagType = computed(() => {
  const typeMap: Record<string, any> = {
    'MINIO': 'danger',
    'ALIYUN_OSS': 'warning',
    'RUST_FS': 'info'
  }
  return typeMap[dsForm.type] || 'info'
})

// 动态表单数据
const dsForm = reactive<Record<string, string>>({
  pkFsoDs: '',
  name: '',
  type: 'MINIO'
})

// 动态校验规则
const dsRules = computed<FormRules>(() => {
  const rules: FormRules = {
    name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
  }
  for (const field of currentFields.value) {
    if (field.required) {
      rules[field.key] = [{ required: true, message: `请输入${field.label}`, trigger: 'blur' }]
    }
  }
  return rules
})

// 监听编辑数据
watch(() => props.editData, (ds) => {
  if (ds) {
    // 先清空动态字段
    for (const key of Object.keys(dsForm)) {
      if (key !== 'pkFsoDs' && key !== 'name' && key !== 'type') {
        dsForm[key] = ''
      }
    }
    dsForm.pkFsoDs = ds.pkFsoDs || ''
    dsForm.name = ds.name || ''
    dsForm.type = ds.type || 'MINIO'
    if (ds.clobConfig) {
      try {
        const config = JSON.parse(ds.clobConfig)
        for (const [k, v] of Object.entries(config)) {
          dsForm[k] = v as string || ''
        }
      } catch (e) { /* ignore */ }
    }
    // 处理标签 "生产:primary,测试:warning" -> ["生产:primary", "测试:warning"]
    selectedTags.value = ds.tags ? ds.tags.split(',').filter((t: string) => t.trim()) : []
  }
}, { immediate: true })

const selectType = (type: string) => {
  // 清空旧类型的字段
  for (const key of Object.keys(dsForm)) {
    if (key !== 'pkFsoDs' && key !== 'name' && key !== 'type') {
      dsForm[key] = ''
    }
  }
  dsForm.type = type
  step.value = 1
}

const buildConfigJson = () => {
  const config: Record<string, string> = {}
  for (const field of currentFields.value) {
    config[field.key] = dsForm[field.key] || ''
  }
  return JSON.stringify(config)
}

const handleTestInDialog = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      testingConnection.value = true
      const payload: any = {
        name: dsForm.name,
        type: dsForm.type,
        clobConfig: buildConfigJson()
      }
      const data: any = await request.post('/fso/datasource/test-temp', payload)
      if (data?.success) {
        ElMessage.success('连接成功')
      } else {
        ElMessage.error(data?.message || '连接失败')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '连接失败')
    } finally {
      testingConnection.value = false
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const payload: any = {
        name: dsForm.name,
        type: dsForm.type,
        clobConfig: buildConfigJson(),
        tags: selectedTags.value.join(',')
      }
      if (props.isEdit) {
        payload.pkFsoDs = dsForm.pkFsoDs
        await request.post('/fso/datasource/update', payload)
        ElMessage.success('更新成功')
      } else {
        await request.post('/fso/datasource/add', payload)
        ElMessage.success('创建成功')
      }
      handleClose()
      emit('success')
    } catch (e) { /* 已由拦截器处理 */ }
  })
}

const handleClose = () => {
  visible.value = false
  step.value = 0
  testingConnection.value = false
  // 清空所有字段
  for (const key of Object.keys(dsForm)) {
    dsForm[key] = ''
  }
  dsForm.type = 'MINIO'
  selectedTags.value = []
}
</script>

<style lang="less" scoped>
.type-select {
  display: flex;
  gap: 20px;
  justify-content: center;
  padding: 20px 0;
  flex-wrap: wrap;
}

.type-card {
  width: 180px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 24px 16px;
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
  margin: 0 auto 12px;
}

.type-icon-img {
  object-fit: contain;
  border-radius: 8px;
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
  margin-bottom: 8px;
}
</style>

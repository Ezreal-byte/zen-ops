<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑服务器' : '新建服务器'"
    width="520px"
    destroy-on-close
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入服务器名称" />
      </el-form-item>
      <el-form-item label="描述" prop="des">
        <el-input v-model="form.des" placeholder="服务器描述（可选）" />
      </el-form-item>
      <el-form-item label="IP地址" prop="ip">
        <el-input v-model="form.ip" placeholder="如 192.168.1.100" />
      </el-form-item>
      <el-form-item label="SSH端口" prop="portSsh">
        <el-input v-model="form.portSsh" placeholder="默认22" />
      </el-form-item>
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="form.userName" placeholder="SSH登录用户名" />
      </el-form-item>
      <el-form-item label="登录方式" prop="loginTp">
        <el-radio-group v-model="form.loginTp">
          <el-radio label="0">密码登录</el-radio>
          <el-radio label="1">私钥登录</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.loginTp === '0'" label="密码" prop="userPwd">
        <el-input v-model="form.userPwd" type="password" show-password placeholder="SSH登录密码" />
      </el-form-item>
      <template v-if="form.loginTp === '1'">
        <el-form-item label="私钥" prop="prvKey">
          <el-input v-model="form.prvKey" type="textarea" :rows="4" placeholder="粘贴私钥内容" />
        </el-form-item>
        <el-form-item label="私钥密码">
          <el-input v-model="form.prvKeyPasswd" type="password" show-password placeholder="私钥密码（可选）" />
        </el-form-item>
      </template>
      <el-form-item label="默认目录">
        <el-input v-model="form.initPath" placeholder="连接后默认进入的目录（可选）" />
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
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { request } from '@/utils/request'

const props = defineProps<{
  modelValue: boolean
  isEdit: boolean
  editData?: any
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'success'): void
}>()

const visible = ref(props.modelValue)
watch(() => props.modelValue, val => { visible.value = val })
watch(visible, val => { emit('update:modelValue', val) })

const formRef = ref<FormInstance>()
const predefinedTags = ref<any[]>([])
const selectedTags = ref<string[]>([])

// 获取标签列表
const fetchTags = async () => {
  try {
    predefinedTags.value = await request.get('/tags/list')
  } catch (e) { /* ignore */ }
}

onMounted(() => {
  fetchTags()
})

const form = reactive({
  pkServer: '',
  name: '',
  des: '',
  ip: '',
  portSsh: '22',
  userName: '',
  userPwd: '',
  loginTp: '0',
  prvKey: '',
  prvKeyPasswd: '',
  initPath: '',
  tags: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  ip: [{ required: true, message: '请输入IP地址', trigger: 'blur' }],
  portSsh: [{ required: true, message: '请输入SSH端口', trigger: 'blur' }],
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userPwd: [{
    validator: (_rule: any, value: any, callback: any) => {
      if (form.loginTp === '0' && !value) {
        callback(new Error('请输入密码'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }],
  prvKey: [{
    validator: (_rule: any, value: any, callback: any) => {
      if (form.loginTp === '1' && !value) {
        callback(new Error('请输入私钥'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }]
})

// 监听编辑数据（监听弹窗打开时加载数据）
watch(() => props.modelValue, (visible) => {
  if (visible && props.editData) {
    // 无论是编辑还是克隆，只要有 editData 就加载
    form.pkServer = props.editData.pkServer || ''
    form.name = props.editData.name || ''
    form.des = props.editData.des || ''
    form.ip = props.editData.ip || ''
    form.portSsh = props.editData.portSsh || '22'
    form.userName = props.editData.userName || ''
    form.userPwd = props.editData.userPwd || ''
    form.loginTp = props.editData.loginTp || '0'
    form.prvKey = props.editData.prvKey || ''
    form.prvKeyPasswd = props.editData.prvKeyPasswd || ''
    form.initPath = props.editData.initPath || ''
    form.tags = props.editData.tags || ''
    // 处理标签 "生产:primary,测试:warning" -> ["生产:primary", "测试:warning"]
    selectedTags.value = props.editData.tags ? props.editData.tags.split(',').filter((t: string) => t.trim()) : []
  } else if (!visible && !props.isEdit) {
    // 新建模式打开时也清空
    if (!props.editData) {
      form.pkServer = ''
      form.name = ''
      form.des = ''
      form.ip = ''
      form.portSsh = '22'
      form.userName = ''
      form.userPwd = ''
      form.loginTp = '0'
      form.prvKey = ''
      form.prvKeyPasswd = ''
      form.initPath = ''
      form.tags = ''
      selectedTags.value = []
    }
  }
})

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const payload: any = {
        name: form.name,
        des: form.des,
        ip: form.ip,
        portSsh: form.portSsh,
        userName: form.userName,
        loginTp: form.loginTp,
        initPath: form.initPath,
        tags: selectedTags.value.join(',')
      }
      if (form.loginTp === '0') {
        payload.userPwd = form.userPwd
      } else {
        payload.prvKey = form.prvKey
        payload.prvKeyPasswd = form.prvKeyPasswd
      }
      if (props.isEdit) {
        payload.pkServer = form.pkServer
        await request.post('/ssh/ds/update', payload)
        ElMessage.success('更新成功')
      } else {
        await request.post('/ssh/ds/add', payload)
        ElMessage.success('创建成功')
      }
      handleClose()
      emit('success')
    } catch (e) { /* 已由拦截器处理 */ }
  })
}

const handleClose = () => {
  visible.value = false
  form.pkServer = ''
  form.name = ''
  form.des = ''
  form.ip = ''
  form.portSsh = '22'
  form.userName = ''
  form.userPwd = ''
  form.loginTp = '0'
  form.prvKey = ''
  form.prvKeyPasswd = ''
  form.initPath = ''
  form.tags = ''
  selectedTags.value = []
}
</script>

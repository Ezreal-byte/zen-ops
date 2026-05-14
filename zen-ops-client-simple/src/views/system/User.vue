<template>
  <div class="user-page">
    <div class="user-card">
      <!-- 查询 + 操作区 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            placeholder="用户名/姓名/电话/邮箱"
            clearable
            size="small"
            style="width: 220px"
            @keyup.enter="onSearch"
            @clear="onSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="small" @click="onSearch">查询</el-button>
        </div>
        <el-button type="primary" size="small" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增
        </el-button>
      </div>

      <!-- 表格区 -->
      <div class="table-wrap">
        <el-table
          :data="tableData"
          size="small"
          stripe
          border
          style="width: 100%"
          v-loading="loading"
          header-cell-class-name="dark-header"
        >
          <el-table-column prop="userName" label="用户名" min-width="100" />
          <el-table-column prop="name" label="姓名" min-width="80" />
          <el-table-column prop="phone" label="电话" min-width="110" />
          <el-table-column prop="email" label="邮箱" min-width="150" />
          <el-table-column prop="sex" label="性别" width="60" align="center" />
          <el-table-column label="角色" min-width="120">
            <template #default="{ row }">
              <el-tag v-for="name in row.roleNames" :key="name" size="small" style="margin: 1px 2px">{{ name }}</el-tag>
              <span v-if="!row.roleNames?.length" style="color: #c0c4cc; font-size: 11px">未分配</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="warning" size="small" @click="handleResetPassword(row)"><el-icon><RefreshRight /></el-icon> 重置密码</el-button>
              <el-button link type="primary" size="small" @click="openDialog(row)"><el-icon><EditPen /></el-icon> 编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)"><el-icon><Delete /></el-icon> 删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页区 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          size="small"
          background
          @current-change="fetchData"
          @size-change="onSizeChange"
        />
      </div>
    </div>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.pkUser ? '编辑用户' : '新增用户'" width="460px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="60px" size="small">
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload-area" @click="triggerAvatarUpload">
            <el-avatar :size="48" :src="avatarUrl" class="avatar-clickable" :style="!avatarUrl ? { backgroundColor: avatarBgColor } : {}">
              <span v-if="!avatarUrl && nameAvatar" class="name-avatar">{{ nameAvatar }}</span>
              <el-icon v-else-if="!avatarUrl && !nameAvatar" :size="20"><UserIcon /></el-icon>
            </el-avatar>
            <input
              ref="avatarInputRef"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleAvatarChange"
            />
          </div>
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="form.sex" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleList" :key="role.pkRole" :label="role.name" :value="role.pkRole" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" size="small" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>


  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User as UserIcon, Search, Plus, EditPen, Delete, RefreshRight } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { request } from '@/utils/request'
import service from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const roleList = ref<any[]>([])

// 查询与分页
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref<FormInstance>()
const form = ref<any>({})
const avatarFile = ref<File | null>(null)
const avatarInputRef = ref<HTMLInputElement>()

// 表单校验规则
const formRules: FormRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

// 姓名头像：取姓名最后两个字，钉钉风格
const nameAvatar = computed(() => {
  const name = form.value.name || ''
  if (!name) return ''
  return name.length <= 2 ? name : name.slice(-2)
})

// 根据姓名生成固定背景色
const AVATAR_COLORS = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9c27b0', '#00bcd4', '#ff5722']
const avatarBgColor = computed(() => {
  const name = form.value.name || form.value.userName || ''
  if (!name) return AVATAR_COLORS[0]
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length]
})

// 头像预览地址
const avatarUrl = computed(() => {
  if (avatarFile.value) {
    return URL.createObjectURL(avatarFile.value)
  }
  if (form.value.pkUser) {
    return `/platform/sys/user/header/${form.value.pkUser}?t=${Date.now()}`
  }
  return ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/sys/user/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: keyword.value || undefined
      }
    })
    tableData.value = res.list || []
    total.value = res.totalCount || 0
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  roleList.value = await request.get('/sys/role/list')
}

const onSearch = () => {
  pageNum.value = 1
  fetchData()
}

const onSizeChange = () => {
  pageNum.value = 1
  fetchData()
}

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

const handleAvatarChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files[0]) {
    avatarFile.value = input.files[0]
  }
}

const openDialog = (row?: any) => {
  if (row) {
    form.value = { ...row, roleIds: row.roleIds || [] }
  } else {
    form.value = { roleIds: [] }
  }
  avatarFile.value = null
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  const isEdit = !!form.value.pkUser
  const url = isEdit ? '/sys/user/update' : '/sys/user/save'

  // 使用 FormData 将表单和头像一起提交
  const formData = new FormData()
  formData.append('form', new Blob([JSON.stringify(form.value)], { type: 'application/json' }))
  if (avatarFile.value) {
    formData.append('file', avatarFile.value)
  }
  // 角色列表作为逗号分隔的字符串，与用户信息一起提交保证事务一致性
  if (form.value.roleIds && form.value.roleIds.length > 0) {
    formData.append('roleIds', form.value.roleIds.join(','))
  }

  await request.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
  await request.get(`/sys/user/delete/${row.pkUser}`)
  ElMessage.success('删除成功')
  fetchData()
}

const handleResetPassword = async (row: any) => {
  await ElMessageBox.confirm(`确定将用户「${row.name || row.userName}」的密码重置为默认密码吗？`, '重置密码', { type: 'warning' })
  await request.post(`/sys/user/reset-password/${row.pkUser}`, {})
  ElMessage.success('密码已重置为默认密码')
}

onMounted(() => {
  fetchData()
  fetchRoles()
})
</script>

<style scoped lang="less">
.user-page {
  height: 100%;
  padding: 5px;
  box-sizing: border-box;
}

.user-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 5px;
  flex-shrink: 0;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.table-wrap {
  flex: 1;
  overflow: hidden;
  padding: 0 5px;

  :deep(.el-table) {
    height: 100% !important;
  }

  :deep(.el-table__inner-wrapper) {
    height: 100% !important;
  }

  :deep(.el-table__body-wrapper) {
    flex: 1;
    overflow-y: auto;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 5px 5px;
  flex-shrink: 0;
}

.avatar-upload-area {
  cursor: pointer;
  display: inline-block;

  .avatar-clickable {
    transition: box-shadow 0.2s;
    &:hover {
      box-shadow: 0 0 0 2px #409eff;
    }
  }

  .name-avatar {
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    user-select: none;
  }
}
</style>

<style lang="less">
/* 深色表头（需要全局样式穿透 scoped） */
.dark-header {
  background-color: #f5f7fa !important;
  color: #303133 !important;
  font-weight: 600 !important;
}
</style>

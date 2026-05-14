<template>
  <el-dialog
    v-model="visible"
    title="服务器列表"
    width="75%"
    destroy-on-close
    @open="fetchList"
  >
    <div class="manager-toolbar">
      <el-input v-model="keyword" placeholder="搜索名称/IP" size="small" style="width: 200px" clearable @keyup.enter="fetchList">
        <template #suffix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button size="small" type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增服务器
      </el-button>
    </div>

    <el-table :data="tableData" size="small" highlight-current-row @row-click="handleRowClick" style="margin-top: 10px" height="320">
      <el-table-column prop="name" label="名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="portSsh" label="端口" width="60" />
      <el-table-column prop="userName" label="用户" width="90" />
      <el-table-column label="登录方式" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.loginTp === '0' ? '' : 'success'" effect="plain">
            {{ row.loginTp === '0' ? '密码' : '私钥' }}
          </el-tag>
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
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="success" size="small" @click.stop="handleConnect(row)">连接</el-button>
          <el-button link type="primary" size="small" @click.stop="handleEdit(row)">编辑</el-button>
          <el-button link type="warning" size="small" @click.stop="handleClone(row)">克隆</el-button>
          <el-button link type="danger" size="small" @click.stop="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="manager-footer">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        background
        :total="total"
        size="small"
        @change="fetchList"
      />
    </div>
    <ServerDialog v-model="editVisible" :isEdit="isEdit" :editData="editData" @success="onEditSuccess" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Star, StarFilled } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import ServerDialog from './ServerDialog.vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'connect', server: any): void
}>()

const visible = ref(props.modelValue)
watch(() => props.modelValue, val => { visible.value = val })
watch(visible, val => { emit('update:modelValue', val) })

const keyword = ref('')
const tableData = ref<any[]>([])
const pageNum = ref(1)
const pageSize = ref(50)
const total = ref(0)
const editVisible = ref(false)
const isEdit = ref(false)
const editData = ref<any>(null)

const fetchList = async () => {
  try {
    const payload: any = {}
    if (keyword.value) {
      payload.keyword = keyword.value
    }
    const res: any = await request.post('/ssh/ds/list', payload, {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    tableData.value = res?.list || []
    total.value = res?.totalCount || 0
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
    await request.post(`/ssh/ds/favorite/${row.pkServer}`)
    ElMessage.success(row.dtFavorite ? '已取消收藏' : '已收藏')
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleConnect = (row: any) => {
  emit('connect', row)
  visible.value = false
}

const handleAdd = () => {
  isEdit.value = false
  editData.value = null
  editVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  editData.value = row
  editVisible.value = true
}

const handleClone = (row: any) => {
  isEdit.value = false
  // 克隆数据，名称后面追加 _copy
  const clonedData = {
    ...row,
    pkServer: undefined, // 清空主键
    name: row.name + '_copy',
    dtFavorite: undefined, // 清空收藏时间
    dtCreated: undefined,
    dtModified: undefined,
    pkCreatedby: undefined,
    pkModifiedby: undefined
  }
  editData.value = clonedData
  editVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定删除服务器 "${row.name}" 吗？`, '提示', { type: 'warning' })
    await request.get(`/ssh/ds/delete/${row.pkServer}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) { /* cancel */ }
}

const onEditSuccess = () => {
  fetchList()
}


</script>

<style lang="less" scoped>
.manager-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.manager-footer {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  padding: 8px 12px;
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

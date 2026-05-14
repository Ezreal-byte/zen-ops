<template>
  <div class="role-page">
    <div class="role-card">
      <!-- 查询 + 操作区 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            placeholder="角色名称"
            clearable
            size="small"
            style="width: 180px"
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
          <el-table-column prop="name" label="角色名称" min-width="160" />
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openMenuDialog(row)"><el-icon><Menu /></el-icon> 配置菜单</el-button>
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

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.pkRole ? '编辑角色' : '新增角色'" width="400px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px" size="small">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" size="small" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 配置菜单弹窗 -->
    <el-dialog v-model="menuDialogVisible" title="配置菜单" width="800px" top="5vh">
      <el-table
        ref="menuTableRef"
        :data="menuFlatList"
        size="small"
        stripe
        border
        style="width: 100%"
        row-key="pkMenu"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        max-height="500"
        header-cell-class-name="dark-header"
        @select="handleMenuSelect"
        @select-all="handleMenuSelectAll"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column label="菜单名称" min-width="180">
          <template #default="{ row }">
            <el-icon v-if="row.icon" style="vertical-align: middle; margin-right: 4px"><component :is="row.icon" /></el-icon>
            <span>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="路由/外链地址" min-width="160" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <template v-if="!row.children?.length">
              <el-tag v-if="row.nodeType === 'LINK'" size="small" type="warning">外链</el-tag>
              <el-tag v-else size="small">功能节点</el-tag>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button size="small" @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" size="small" @click="handleAssignMenus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, EditPen, Delete, Menu } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { request } from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const menuFlatList = ref<any[]>([])
const menuTableRef = ref<any>(null)
const currentRole = ref<any>(null)
const selectedMenuIds = ref<string[]>([])

const formRef = ref<FormInstance>()
const form = ref<any>({})

// 查询与分页
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
// 全量列表（角色量不大，前端过滤+分页）
let allList: any[] = []

const formRules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    allList = await request.get('/sys/role/list')
    total.value = allList.length
    filterData()
  } finally {
    loading.value = false
  }
}

const filterData = () => {
  let filtered = allList
  const kw = keyword.value.trim()
  if (kw) {
    filtered = allList.filter(item => item.name?.includes(kw))
  }
  total.value = filtered.length
  const start = (pageNum.value - 1) * pageSize.value
  tableData.value = filtered.slice(start, start + pageSize.value)
}

const fetchMenus = async () => {
  const list = await request.get('/sys/menu/list')
  menuFlatList.value = buildTree(list)
}

const buildTree = (list: any[]) => {
  const map: Record<string, any> = {}
  list.forEach(item => {
    map[item.pkMenu] = { ...item, children: [] }
  })
  const tree: any[] = []
  list.forEach(item => {
    const pkParent = item.pkParent ?? '0'
    if (pkParent === '0' || pkParent === 0 || !map[pkParent]) {
      tree.push(map[item.pkMenu])
    } else {
      map[pkParent].children.push(map[item.pkMenu])
    }
  })
  return tree
}

const onSearch = () => {
  pageNum.value = 1
  filterData()
}

const onSizeChange = () => {
  pageNum.value = 1
  filterData()
}

const openDialog = (row?: any) => {
  form.value = row ? { ...row } : {}
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  const url = form.value.pkRole ? '/sys/role/update' : '/sys/role/save'
  await request.post(url, form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
  await request.get(`/sys/role/delete/${row.pkRole}`)
  ElMessage.success('删除成功')
  fetchData()
}

const openMenuDialog = async (row: any) => {
  currentRole.value = row
  const [menuList, menuIds] = await Promise.all([
    fetchMenus(),
    request.get(`/sys/role/menu-ids/${row.pkRole}`)
  ])
  selectedMenuIds.value = menuIds ? menuIds.map(String) : []
  menuDialogVisible.value = true
  // dialog 打开后延迟勾选，确保表格完全渲染
  setTimeout(() => {
    toggleMenuSelection()
  }, 200)
}

const toggleMenuSelection = () => {
  const table = menuTableRef.value
  if (!table) return
  // 先清空所有选中
  table.clearSelection()
  // 统一将 selectedMenuIds 转为字符串集合
  const idSet = new Set(selectedMenuIds.value.map(String))
  // 遍历树形数据递归勾选，只勾选精确匹配的ID
  const toggle = (list: any[]) => {
    for (const row of list) {
      const rowId = String(row.pkMenu)
      if (idSet.has(rowId)) {
        table.toggleRowSelection(row, true)
      } else {
        // 明确取消未匹配的行的选中状态
        table.toggleRowSelection(row, false)
      }
      // 递归子节点
      if (row.children?.length) {
        toggle(row.children)
      }
    }
  }
  toggle(menuFlatList.value)
}

const handleMenuSelect = (selection: any[], row: any) => {
  // 更新 selectedMenuIds
  const isSelected = selection.some((s: any) => s.pkMenu === row.pkMenu)
  if (isSelected) {
    if (!selectedMenuIds.value.includes(row.pkMenu)) {
      selectedMenuIds.value.push(row.pkMenu)
    }
  } else {
    selectedMenuIds.value = selectedMenuIds.value.filter(id => id !== row.pkMenu)
  }
}

const handleMenuSelectAll = (selection: any[]) => {
  // 全选/取消全选时收集所有 pkMenu
  const collectIds = (list: any[]): string[] => {
    const ids: string[] = []
    for (const row of list) {
      ids.push(row.pkMenu)
      if (row.children?.length) ids.push(...collectIds(row.children))
    }
    return ids
  }
  const allIds = collectIds(menuFlatList.value)
  if (selection.length > 0) {
    // 全选
    selectedMenuIds.value = [...new Set([...selectedMenuIds.value, ...allIds])]
  } else {
    // 取消全选
    selectedMenuIds.value = []
  }
}

const handleAssignMenus = async () => {
  await request.post('/sys/role/assign-menus', {
    pkRole: currentRole.value.pkRole,
    menuIds: selectedMenuIds.value
  })
  ElMessage.success('配置成功')
  menuDialogVisible.value = false
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="less">
.role-page {
  height: 100%;
  padding: 5px;
  box-sizing: border-box;
}

.role-card {
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
</style>

<style lang="less">
.dark-header {
  background-color: #f5f7fa !important;
  color: #303133 !important;
  font-weight: 600 !important;
}
</style>

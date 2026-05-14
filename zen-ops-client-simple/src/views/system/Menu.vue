<template>
  <div class="menu-page">
    <div class="menu-card">
      <!-- 查询 + 操作区 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            placeholder="菜单名称/路由"
            clearable
            size="small"
            style="width: 200px"
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
          row-key="pkMenu"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          default-expand-all
          highlight-current-row
          header-cell-class-name="dark-header"
        >
          <el-table-column label="菜单名称" min-width="180">
            <template #default="{ row }">
              <el-icon v-if="row.icon" style="vertical-align: middle; margin-right: 4px"><component :is="row.icon" /></el-icon>
              <span>{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="url" label="路由/外链地址" min-width="160" />
          <el-table-column prop="component" label="组件路径" min-width="160" />
          <el-table-column label="排序" width="90" align="center">
            <template #default="{ row }">
              <div class="sort-cell">
                <span class="sort-num">{{ row.sortOrder }}</span>
                <span class="sort-btns">
                  <el-icon class="sort-btn" :class="{ disabled: !canMoveUp(row) }" @click="canMoveUp(row) && handleMoveUp(row)"><Top /></el-icon>
                  <el-icon class="sort-btn" :class="{ disabled: !canMoveDown(row) }" @click="canMoveDown(row) && handleMoveDown(row)"><Bottom /></el-icon>
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="90" align="center">
            <template #default="{ row }">
              <template v-if="!row.children?.length">
                <el-tag v-if="row.nodeType === 'LINK'" size="small" type="warning">外链</el-tag>
                <el-tag v-else size="small">功能节点</el-tag>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openDialog(row)"><el-icon><EditPen /></el-icon> 编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)"><el-icon><Delete /></el-icon> 删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新增/编辑菜单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.pkMenu ? '编辑菜单' : '新增菜单'" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" size="small">
        <el-form-item label="上级菜单" prop="pkParent">
          <el-tree-select
            v-model="form.pkParent"
            :data="menuTreeData"
            :props="{ label: 'name', value: 'pkMenu', children: 'children' }"
            check-strictly
            clearable
            default-expand-all
            placeholder="请选择上级菜单"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="路由/外链地址" prop="url">
          <el-input v-model="form.url" placeholder="如：/sys-user 或 https://xxx.com" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="如：system/User.vue，外链留空" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <icon-picker v-model="form.icon" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="nodeType">
          <el-radio-group v-model="form.nodeType">
            <el-radio label="FUNC">功能节点</el-radio>
            <el-radio label="LINK">外链</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否隐藏" prop="isHidden">
          <el-switch v-model="form.isHidden" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, EditPen, Delete, Top, Bottom } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { request } from '@/utils/request'
import IconPicker from '@/components/IconPicker.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const menuTreeData = ref<any[]>([])
const dialogVisible = ref(false)

const formRef = ref<FormInstance>()
const form = ref<any>({
  pkParent: '0',
  nodeType: 'FUNC',
  isHidden: false
})

// 查询
const keyword = ref('')
let allTreeData: any[] = []

const formRules: FormRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const list = await request.get('/sys/menu/list')
    allTreeData = buildTree(list)
    menuTreeData.value = [{ name: '顶级菜单', pkMenu: '0', children: allTreeData }]
    filterData()
  } finally {
    loading.value = false
  }
}

const filterData = () => {
  const kw = keyword.value.trim()
  if (!kw) {
    tableData.value = allTreeData
    return
  }
  // 递归过滤树
  tableData.value = filterTree(allTreeData, kw)
}

const filterTree = (nodes: any[], kw: string): any[] => {
  const result: any[] = []
  for (const node of nodes) {
    const children = node.children?.length ? filterTree(node.children, kw) : []
    const match = node.name?.includes(kw) || node.url?.includes(kw)
    if (match || children.length > 0) {
      result.push({ ...node, children })
    }
  }
  return result
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
  filterData()
}

const openDialog = (row?: any) => {
  form.value = row
    ? { ...row }
    : { pkParent: '0', nodeType: 'FUNC', isHidden: false }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  const url = form.value.pkMenu ? '/sys/menu/update' : '/sys/menu/save'
  await request.post(url, form.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该菜单吗？', '提示', { type: 'warning' })
  await request.get(`/sys/menu/delete/${row.pkMenu}`)
  ElMessage.success('删除成功')
  fetchData()
}

// 排序：获取同级兄弟列表
const getSiblings = (nodes: any[], pkParent: string): any[] => {
  for (const node of nodes) {
    if (node.pkMenu === pkParent) return node.children || []
    if (node.children?.length) {
      const found = getSiblings(node.children, pkParent)
      if (found.length) return found
    }
  }
  return pkParent === '0' || pkParent === 0 ? nodes : []
}

const canMoveUp = (row: any) => {
  const pkParent = row.pkParent ?? '0'
  const siblings = pkParent === '0' || pkParent === 0 ? allTreeData : getSiblings(allTreeData, pkParent)
  const idx = siblings.findIndex((s: any) => s.pkMenu === row.pkMenu)
  return idx > 0
}

const canMoveDown = (row: any) => {
  const pkParent = row.pkParent ?? '0'
  const siblings = pkParent === '0' || pkParent === 0 ? allTreeData : getSiblings(allTreeData, pkParent)
  const idx = siblings.findIndex((s: any) => s.pkMenu === row.pkMenu)
  return idx >= 0 && idx < siblings.length - 1
}

const handleMoveUp = async (row: any) => {
  await request.post('/sys/menu/move', { pkMenu: row.pkMenu, direction: 'up' })
  fetchData()
}

const handleMoveDown = async (row: any) => {
  await request.post('/sys/menu/move', { pkMenu: row.pkMenu, direction: 'down' })
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="less">
.menu-page {
  height: 100%;
  padding: 5px;
  box-sizing: border-box;
}

.menu-card {
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

.sort-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;

  .sort-num {
    font-size: 12px;
    color: #606266;
    min-width: 16px;
    text-align: center;
  }

  .sort-btns {
    display: inline-flex;
    flex-direction: column;
    gap: 0;
    line-height: 1;
  }

  .sort-btn {
    font-size: 11px;
    cursor: pointer;
    color: #409eff;
    transition: color 0.15s;

    &:hover {
      color: #66b1ff;
    }

    &.disabled {
      color: #c0c4cc;
      cursor: not-allowed;
    }
  }
}
</style>

<style lang="less">
.dark-header {
  background-color: #f5f7fa !important;
  color: #303133 !important;
  font-weight: 600 !important;
}

.el-table__body tr.current-row > td.el-table__cell {
  background-color: #ecf5ff !important;
}
</style>

<script setup lang="ts">
import {ref, watch, nextTick, computed} from 'vue'
import {request} from '@/utils/request'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, View, RefreshRight, Download, CopyDocument, Delete} from '@element-plus/icons-vue'

// 使用 new URL 动态加载图标，Vite 打包后路径正确
const navigationIcon = new URL('/icons/daohang.svg', import.meta.url).href
const databaseIcon = new URL('/icons/database.svg', import.meta.url).href
const tableIcon = new URL('/icons/table.svg', import.meta.url).href

const props = defineProps<{ pkDs?: string | number }>()
const emit = defineEmits(['nodeSelect', 'executeSql'])

const treeRef = ref<any>(null)
const treeData = ref<any[]>([])
const treeLoading = ref(false)
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextNode = ref<any>(null)

const structDialogVisible = ref(false)
const structLoading = ref(false)
const structColumns = ref<any[]>([])
const structTableName = ref('')

const copyDialogVisible = ref(false)
const copyLoading = ref(false)
const copyOldTableName = ref('')
const copyNewTableName = ref('')
const copyDbName = ref('')

const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)
const deleteTableName = ref('')
const deleteDbName = ref('')
const deleteConfirmText = ref('')

const defaultProps = {
  children: 'children',
  label: 'name',
  isLeaf: 'isLeaf'
}

const loadDatabases = async (pkDs?: string | number) => {
  if (!pkDs) {
    treeData.value = []
    return
  }
  treeLoading.value = true
  try {
    const data = await request.get(`/sql-window/databases/${pkDs}`)
    treeData.value = (data || []).map((db: any) => ({
      name: db.databaseName || db.database_name || db.DATABASE_NAME,
      type: 'database',
      isLeaf: false,
      children: []
    }))
  } catch (e: any) {
    ElMessage.error(e.message || '加载数据库失败')
  } finally {
    treeLoading.value = false
  }
}

const loadNode = async (node: any, resolve: any) => {
  if (node.level === 0) {
    resolve(treeData.value)
    return
  }
  if (node.data.type === 'database') {
    try {
      const data = await request.get(`/sql-window/tables/${props.pkDs}`, {
        params: { database: node.data.name }
      })
      const tables = (data || []).map((t: any) => ({
        name: t.tableName || t.table_name || t.TABLE_NAME,
        comment: t.comments || t.COMMENTS || '',
        type: 'table',
        dbName: node.data.name,
        isLeaf: true
      }))
      resolve(tables)
    } catch (e) {
      resolve([])
    }
  } else {
    resolve([])
  }
}

const onNodeClick = (data: any) => {
  emit('nodeSelect', data)
}

const onNodeContextMenu = (event: MouseEvent, data: any) => {
  event.preventDefault()
  contextNode.value = data
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuVisible.value = true
  nextTick(() => {
    setTimeout(() => {
      document.addEventListener('click', closeContextMenu, {once: true})
    }, 0)
  })
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
}

const onCreateDatabase = async () => {
  closeContextMenu()
  try {
    const { value } = await ElMessageBox.prompt('请输入数据库名称', '新建数据库', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^\S+$/,
      inputErrorMessage: '数据库名称不能为空'
    })
    const sql = `CREATE DATABASE \`${value}\``
    emit('executeSql', sql)
  } catch (e) { /* cancel */ }
}

const onViewStructure = async () => {
  closeContextMenu()
  const node = contextNode.value
  if (!node || node.type !== 'table') return
  structTableName.value = node.name
  structDialogVisible.value = true
  structLoading.value = true
  try {
    const data = await request.get(`/sql-window/columns/${props.pkDs}`, {
      params: { database: node.dbName, table: node.name }
    })
    structColumns.value = data || []
  } catch (e: any) {
    ElMessage.error(e.message || '获取表结构失败')
    structColumns.value = []
  } finally {
    structLoading.value = false
  }
}

const onRefreshTables = () => {
  closeContextMenu()
  const node = contextNode.value
  if (!node || node.type !== 'database') return
  const treeNode = treeRef.value?.getNode(node)
  if (treeNode) {
    treeNode.loaded = false
    treeNode.expand()
  }
}

const onExportData = async () => {
  closeContextMenu()
  const node = contextNode.value
  if (!node || node.type !== 'table' || !props.pkDs) return
  try {
    const data: any = await request.get(`/sql-window/table/count`, {
      params: { pkDs: props.pkDs, database: node.dbName, table: node.name }
    })
    const count = data?.count ?? 0
    await ElMessageBox.confirm(
      `表 ${node.name} 共有 ${count} 条数据，确定导出？`,
      '导出数据',
      { confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info' }
    )
    // 触发下载（需要带token，因为window.open不走axios拦截器）
    const token = localStorage.getItem('token') || ''
    const url = `/platform/sql-window/table/export?pkDs=${props.pkDs}&database=${encodeURIComponent(node.dbName)}&table=${encodeURIComponent(node.name)}&Authentication-Token=${encodeURIComponent(token)}`
    window.open(url, '_blank')
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('导出失败: ' + (e.message || '未知错误'))
    }
  }
}

const onCopyTable = async () => {
  closeContextMenu()
  const node = contextNode.value
  if (!node || node.type !== 'table' || !props.pkDs) return
  copyOldTableName.value = node.name
  copyDbName.value = node.dbName
  copyNewTableName.value = `${node.name}_copy`
  copyDialogVisible.value = true
}

const handleCopyTable = async () => {
  if (!copyNewTableName.value || !copyNewTableName.value.trim()) {
    ElMessage.warning('请输入新表名')
    return
  }
  copyLoading.value = true
  try {
    await request.post(`/sql-window/table/copy/${props.pkDs}`, null, {
      params: {
        database: copyDbName.value,
        oldTable: copyOldTableName.value,
        newTable: copyNewTableName.value.trim()
      }
    })
    ElMessage.success('复制表成功')
    copyDialogVisible.value = false
    // 刷新表列表
    refreshDatabaseTables(copyDbName.value)
  } catch (e: any) {
    ElMessage.error(e.message || '复制表失败')
  } finally {
    copyLoading.value = false
  }
}

// 刷新指定数据库的表列表
const refreshDatabaseTables = (dbName: string) => {
  // 遍历树节点找到对应的 database 节点
  const findAndRefresh = (nodes: any[]): boolean => {
    for (const node of nodes) {
      if (node.name === dbName && node.type === 'database') {
        const treeNode = treeRef.value?.getNode(node)
        if (treeNode) {
          treeNode.loaded = false
          treeNode.expand()
        }
        return true
      }
      if (node.children && findAndRefresh(node.children)) {
        return true
      }
    }
    return false
  }
  findAndRefresh(treeData.value)
}

const onDeleteTable = async () => {
  closeContextMenu()
  const node = contextNode.value
  if (!node || node.type !== 'table' || !props.pkDs) return
  deleteTableName.value = node.name
  deleteDbName.value = node.dbName
  deleteConfirmText.value = ''
  deleteDialogVisible.value = true
}

const handleDeleteTable = async () => {
  if (deleteConfirmText.value !== '确认删除') {
    ElMessage.warning('请输入“确认删除”')
    return
  }
  deleteLoading.value = true
  try {
    await request.post(`/sql-window/table/delete/${props.pkDs}`, null, {
      params: {
        database: deleteDbName.value,
        table: deleteTableName.value
      }
    })
    ElMessage.success('删除表成功')
    deleteDialogVisible.value = false
    // 刷新表列表
    refreshDatabaseTables(deleteDbName.value)
  } catch (e: any) {
    ElMessage.error(e.message || '删除表失败')
  } finally {
    deleteLoading.value = false
  }
}

watch(() => props.pkDs, (val) => {
  loadDatabases(val)
}, { immediate: true })

defineExpose({ loadDatabases })
</script>

<template>
  <el-card shadow="never" class="db-tree-card">
    <div class="tree-header">
      <div class="header-left">
        <img class="header-icon" :src="navigationIcon" />
        <span class="header-title">数据库导航</span>
      </div>
      <el-button link size="small" :icon="RefreshRight" @click="loadDatabases(props.pkDs)">刷新</el-button>
    </div>
    <div class="tree-wrap" v-loading="treeLoading">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="defaultProps"
        :load="loadNode"
        lazy
        highlight-current
        @node-click="onNodeClick"
      >
        <template #default="{ node, data }">
          <span class="tree-node" @contextmenu.stop="onNodeContextMenu($event, data)">
            <img v-if="data.type === 'database'" class="node-icon" :src="databaseIcon" />
            <img v-else class="node-icon" :src="tableIcon" />
            <span class="node-label">{{ node.label }}</span>
            <span v-if="data.comment" class="node-comment" :title="data.comment">{{ data.comment }}</span>
          </span>
        </template>
      </el-tree>
    </div>

    <!-- 右键菜单 -->
    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
      @click.stop
    >
      <div v-if="contextNode?.type === 'database'" class="menu-item" @click="onCreateDatabase">
        <el-icon><Plus /></el-icon>
        <span>新建数据库</span>
      </div>
      <div v-if="contextNode?.type === 'database'" class="menu-item" @click="onRefreshTables">
        <el-icon><RefreshRight /></el-icon>
        <span>刷新表</span>
      </div>
      <div v-if="contextNode?.type === 'table'" class="menu-item" @click="onViewStructure">
        <el-icon><View /></el-icon>
        <span>查看表结构</span>
      </div>
      <div v-if="contextNode?.type === 'table'" class="menu-item" @click="onExportData">
        <el-icon><Download /></el-icon>
        <span>导出数据</span>
      </div>
      <div v-if="contextNode?.type === 'table'" class="menu-item" @click="onCopyTable">
        <el-icon><CopyDocument /></el-icon>
        <span>复制表</span>
      </div>
      <div v-if="contextNode?.type === 'table'" class="menu-item menu-item-danger" @click="onDeleteTable">
        <el-icon><Delete /></el-icon>
        <span>删除表</span>
      </div>
    </div>

    <!-- 表结构弹窗 -->
    <el-dialog v-model="structDialogVisible" :title="`表结构 - ${structTableName}`" width="700px" append-to-body destroy-on-close>
      <el-table :data="structColumns" v-loading="structLoading" size="small" stripe border>
        <el-table-column prop="columnName" label="字段名" min-width="120"/>
        <el-table-column prop="dataType" label="类型" width="120"/>
        <el-table-column prop="comments" label="注释" min-width="150"/>
        <el-table-column prop="isNullable" label="可空" width="80"/>
        <el-table-column prop="columnDefault" label="默认值" min-width="100"/>
      </el-table>
    </el-dialog>

    <!-- 复制表弹窗 -->
    <el-dialog v-model="copyDialogVisible" title="复制表" width="500px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="原表名">
          <el-input :value="copyOldTableName" disabled />
        </el-form-item>
        <el-form-item label="新表名">
          <el-input v-model="copyNewTableName" placeholder="请输入新表名" @keyup.enter="handleCopyTable" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="copyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="copyLoading" @click="handleCopyTable">确定</el-button>
      </template>
    </el-dialog>

    <!-- 删除表弹窗 -->
    <el-dialog v-model="deleteDialogVisible" title="删除表" width="500px" append-to-body>
      <div class="delete-warning">
        <el-icon class="warning-icon"><Delete /></el-icon>
        <div class="warning-text">
          确定要删除表 <span class="table-name">{{ deleteDbName }}.{{ deleteTableName }}</span> 吗？<span class="warning-highlight">此操作不可恢复！</span>
        </div>
      </div>
      <el-form class="delete-form">
        <el-input 
          v-model="deleteConfirmText" 
          placeholder="请输入“确认删除”" 
          @keyup.enter="handleDeleteTable"
          size="large"
        />
      </el-form>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button 
          type="danger" 
          :loading="deleteLoading"
          :disabled="deleteConfirmText !== '确认删除'"
          @click="handleDeleteTable"
        >
          确认删除
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped lang="less">
.db-tree-card {
  height: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 4px;

  :deep(.el-card__body) {
    padding: 0;
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .tree-header {
    padding: 6px 12px;
    font-weight: 600;
    font-size: 13px;
    border-bottom: 1px solid #ebeef5;
    background: #fafafa;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .header-left {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .header-icon {
      width: 16px;
      height: 16px;
    }

    .header-title {
      color: #303133;
    }
  }

  .tree-wrap {
    flex: 1;
    overflow: auto;
    padding: 2px 0;

    :deep(.el-tree-node__content) {
      height: 26px;
      padding: 0 4px;

      &:hover {
        background-color: #f0f7ff;
      }
    }

    :deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
      background-color: #e6f2ff;
    }

    :deep(.el-tree-node__expand-icon) {
      padding: 4px;
      font-size: 12px;
    }
  }

  .tree-node {
    display: flex;
    align-items: center;
    gap: 4px;
    width: 100%;
    overflow: hidden;
    font-size: 12px;

    .node-icon {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
    }

    .node-label {
      font-size: 12px;
      color: #303133;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .node-comment {
      font-size: 10px;
      color: #909399;
      margin-left: 4px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      flex-shrink: 1;
    }
  }

  .context-menu {
    position: fixed;
    z-index: 3000;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    padding: 2px 0;
    min-width: 120px;

    .menu-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      font-size: 12px;
      color: #303133;
      cursor: pointer;
      transition: background 0.2s;

      &:hover {
        background: #f0f7ff;
        color: #409eff;
      }

      .el-icon {
        font-size: 13px;
      }

      &.menu-item-danger {
        color: #f56c6c;

        &:hover {
          background: #fef0f0;
          color: #f56c6c;
        }
      }
    }
  }

  .delete-warning {
    display: flex !important;
    align-items: center !important;
    gap: 16px !important;
    padding: 20px !important;
    background: #fef0f0 !important;
    border: 1px solid #fbc4c4 !important;
    border-radius: 4px !important;
    margin-bottom: 20px !important;

    .warning-icon {
      font-size: 32px !important;
      color: #f56c6c !important;
      flex-shrink: 0 !important;
    }

    .warning-text {
      flex: 1 !important;
      font-size: 16px !important;
      color: #606266 !important;
      line-height: 1.6 !important;
      display: inline !important;

      .table-name {
        font-weight: 600 !important;
        color: #409eff !important;
        font-family: 'Courier New', monospace !important;
      }

      .warning-highlight {
        font-weight: 600 !important;
        color: #f56c6c !important;
      }
    }
  }

  .delete-form {
    margin-top: 16px;
  }
}
</style>

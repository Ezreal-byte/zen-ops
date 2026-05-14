<script setup lang="ts">
import {ref, computed, h, defineComponent} from 'vue'
import {ElMessage, ElButton, ElMessageBox} from 'element-plus'
import {Close, Document, CircleCloseFilled, CircleCheckFilled, Timer, List, EditPen, Grid, ArrowDown, Download, View, Check, Plus} from '@element-plus/icons-vue'
import {StkTable} from 'stk-table-vue'
import 'stk-table-vue/lib/style.css'
import {request} from '@/utils/request'

// 定义 emit
const emit = defineEmits<{
  'loadMore': [payload: { sql: string; pageNum: number; pageSize: number; tabId: string }]
}>()

interface ColumnMeta {
  name: string
  type: string
  colType: string
  sqlType: number
  precision: number
  scale: number
}

interface ResultTab {
  id: string
  sql: string
  sqlType: string
  dmlType?: string
  success: boolean
  execTimeMs: number
  message: string
  affectedRows: number | null
  columns: string[]
  columnsMeta?: ColumnMeta[]
  rows: any[]
  newRows?: any[]  // 待插入的新行
  total: number
  pageNum: number
  pageSize: number
  singleTableQuery?: boolean
  queryTable?: string
  querySchema?: string
  pkColumn?: string
  pkDs?: number
  comments?: string[]
}

const tabs = ref<ResultTab[]>([])
const activeTabId = ref('')
let tabIdCounter = 0

const activeTab = computed(() => tabs.value.find(t => t.id === activeTabId.value))
const hasTabs = computed(() => tabs.value.length > 0)

const selectedRow = ref<any>(null)
const viewMode = ref<'table' | 'row'>('table')
const loadMoreLoading = ref(false)
const editingCell = ref<string | null>(null)
const editedCells = ref<Map<string, string>>(new Map())
const submitLoading = ref(false)

// 是否可编辑：DML + SELECT + 单表 + 有主键
const canEdit = computed(() => {
  const tab = activeTab.value
  return tab && tab.dmlType === 'SELECT' && tab.singleTableQuery && !!tab.pkColumn
})

// 是否有未提交的编辑
const hasEdits = computed(() => editedCells.value.size > 0)

// 是否有待插入的新行
const hasNewRows = computed(() => {
  const tab = activeTab.value
  return tab && tab.newRows && tab.newRows.length > 0
})

const getEditKey = (row: any, col: string) => `${row.__stkRowKey}__${col}`

let lastCellClickInfo = { time: 0, rowKey: '', colName: '' }

const onCellDblClick = (ev: MouseEvent, row: any, col: any) => {
  // 有待插入新行时禁止编辑现有行
  if (!canEdit.value || hasNewRows.value) return
  const colName = col?.dataIndex
  if (!colName) return
  const tab = activeTab.value
  if (!tab) return
  const meta = tab.columnsMeta?.find(m => m.name === colName)
  const isPk = colName === tab.pkColumn
  const isBlob = meta?.type === 'BLOB'
  const isClob = meta?.type === 'CLOB'
  if (isPk || isBlob || isClob) return
  const editKey = getEditKey(row, colName)
  editingCell.value = editKey
}

const onCellClick = (ev: MouseEvent, row: any, col: any) => {
  const colName = col?.dataIndex
  if (!colName) return
  const rowKey = row?.__stkRowKey ?? ''
  const now = Date.now()

  // 双击检测：同单元格、300ms 内第二次点击视为双击
  if (lastCellClickInfo.rowKey === String(rowKey) &&
      lastCellClickInfo.colName === colName &&
      now - lastCellClickInfo.time < 300) {
    lastCellClickInfo = { time: 0, rowKey: '', colName: '' }
    onCellDblClick(ev, row, col)
    return
  }

  lastCellClickInfo = { time: now, rowKey: String(rowKey), colName }

  // 点击其他单元格时退出当前编辑
  if (editingCell.value) {
    const editKey = getEditKey(row, colName)
    if (editingCell.value !== editKey) {
      editingCell.value = null
    }
  }
}

const hasMore = computed(() => {
  const tab = activeTab.value
  if (!tab || tab.dmlType !== 'SELECT') return false
  return tab.pageNum * tab.pageSize < tab.total
})

const rowDetailList = computed(() => {
  if (!selectedRow.value) return []
  return Object.entries(selectedRow.value)
    .filter(([key]) => key !== '__stkRowKey')
    .map(([key, value]) => ({
      key,
      value: value === null || value === undefined ? '' : String(value)
    }))
})

let rowKeyCounter = 0

const setResults = (data: any[], pkDs?: number, fallbackDbSchema?: string) => {
  if (!data || data.length === 0) return
  const newTabs: ResultTab[] = data.map((res: any) => ({
    id: `tab_${++tabIdCounter}`,
    sql: res.sql || '',
    sqlType: res.sqlType || 'UPDATE',
    dmlType: res.dmlType,
    success: res.success !== false,
    execTimeMs: res.execTimeMs || 0,
    message: res.message || '',
    affectedRows: res.affectedRows,
    columns: res.columns || [],
    columnsMeta: res.columnsMeta || [],
    rows: (res.rows || []).map((r: any) => ({ ...r, __stkRowKey: ++rowKeyCounter })),
    total: res.total || (res.rows || []).length,
    pageNum: res.pageNum || 1,
    pageSize: res.pageSize || (res.rows || []).length,
    singleTableQuery: res.singleTableQuery,
    queryTable: res.queryTable,
    querySchema: res.querySchema || fallbackDbSchema || '',
    pkColumn: res.pkColumn,
    pkDs: pkDs,
    comments: res.comments || []
  }))
  tabs.value.push(...newTabs)
  activeTabId.value = newTabs[newTabs.length - 1].id
}

const closeTab = (id: string) => {
  const idx = tabs.value.findIndex(t => t.id === id)
  if (idx === -1) return
  tabs.value.splice(idx, 1)
  if (activeTabId.value === id) {
    activeTabId.value = tabs.value.length > 0 ? tabs.value[Math.min(idx, tabs.value.length - 1)].id : ''
  }
}

const closeAllTabs = () => {
  tabs.value = []
  activeTabId.value = ''
}

const clearTabs = () => {
  tabs.value = []
  activeTabId.value = ''
}

const onStkRowClick = (ev: MouseEvent, row: any) => {
  selectedRow.value = row
}

// 为每个 tab 生成 stk-table 列配置
const getStkColumns = (tab: ResultTab) => {
  return tab.columns.map(col => {
    const meta = tab.columnsMeta?.find(m => m.name === col)
    const isPk = col === tab.pkColumn
    const isBlob = meta?.type === 'BLOB'
    const isClob = meta?.type === 'CLOB'
    const canEditCell = canEdit.value && !isBlob && !isClob

    return {
      title: col,
      dataIndex: col,
      width: 160,
      customCell: isBlob
        ? defineComponent({
            props: ['row', 'col', 'cellValue', 'rowIndex', 'colIndex'],
            setup(props: any) {
              return () => h(ElButton, {
                size: 'small', text: true, type: 'primary', icon: Download,
                onClick: (e: Event) => { e.stopPropagation(); downloadBlob(props.row, col) }
              }, () => '下载')
            }
          })
        : isClob
        ? defineComponent({
            props: ['row', 'col', 'cellValue', 'rowIndex', 'colIndex'],
            setup(props: any) {
              return () => {
                const isNewRow = props.row.__isNew === true
                const rowKey = String(props.row.__stkRowKey ?? '')
                const hasRowEdit = isNewRow || Array.from(editedCells.value.keys()).some(k => k.startsWith(rowKey + '__'))
                const btnText = hasRowEdit ? '编辑' : '查看'
                const btnIcon = hasRowEdit ? EditPen : View
                return h(ElButton, {
                  size: 'small', text: true, type: 'primary', icon: btnIcon,
                  onClick: (e: Event) => { e.stopPropagation(); showClobDetail(props.row, col, !hasRowEdit) }
                }, () => btnText)
              }
            }
          })
        : canEditCell
        ? defineComponent({
            props: ['row', 'col', 'cellValue', 'rowIndex', 'colIndex'],
            setup(props: any) {
              return () => {
                const editKey = getEditKey(props.row, col)
                const isNewRow = props.row.__isNew === true
                const isEditingNow = editingCell.value === editKey || isNewRow
                if (isEditingNow) {
                  const editedVal = editedCells.value.get(editKey)
                  const displayVal = isNewRow
                    ? (props.row[col] ?? '')
                    : (editedVal !== undefined ? editedVal : formatCellValue(props.cellValue))
                  return h('input', {
                    value: displayVal,
                    class: 'cell-edit-input',
                    onClick: (e: Event) => e.stopPropagation(),
                    onInput: (e: Event) => {
                      const val = (e.target as HTMLInputElement).value
                      if (isNewRow) {
                        props.row[col] = val
                      } else {
                        editedCells.value = new Map(editedCells.value.set(editKey, val))
                      }
                    }
                  })
                }
                return h('span', {
                  style: 'overflow:hidden;text-overflow:ellipsis;white-space:nowrap;display:block'
                }, editedCells.value.get(editKey) !== undefined ? editedCells.value.get(editKey) : formatCellValue(props.cellValue))
              }
            }
          })
        : undefined
    }
  })
}

// 单行视图列配置
const rowDetailColumns = [
  { title: '字段名', dataIndex: 'key', width: 200 },
  { title: '值', dataIndex: 'value' }
]

const onToggleView = () => {
  if (viewMode.value === 'table') {
    if (!selectedRow.value) {
      ElMessage.warning('请先点击表格选中一行数据')
      return
    }
    viewMode.value = 'row'
  } else {
    viewMode.value = 'table'
  }
}

const onLoadMore = () => {
  const tab = activeTab.value
  if (!tab) return
  loadMoreLoading.value = true
  emit('loadMore', {
    sql: tab.sql,
    pageNum: tab.pageNum + 1,
    pageSize: tab.pageSize,
    tabId: tab.id
  })
}

const getColumnMeta = (field: string): ColumnMeta | undefined => {
  return activeTab.value?.columnsMeta?.find(m => m.name === field)
}

const isBlobType = (field: string): boolean => {
  return getColumnMeta(field)?.type === 'BLOB'
}

const isClobType = (field: string): boolean => {
  return getColumnMeta(field)?.type === 'CLOB'
}

const isDateType = (field: string): boolean => {
  return getColumnMeta(field)?.type === 'DATETIME'
}

const formatCellValue = (value: any): string => {
  if (value === null || value === undefined) return ''
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

const detectBlobExtAndMime = (base64: string): { ext: string; mime: string } => {
  const s = base64.substring(0, 20)
  if (s.startsWith('iVBORw0KGgo')) return { ext: 'png', mime: 'image/png' }
  if (s.startsWith('/9j/')) return { ext: 'jpg', mime: 'image/jpeg' }
  if (s.startsWith('R0lG')) return { ext: 'gif', mime: 'image/gif' }
  if (s.startsWith('Qk1')) return { ext: 'bmp', mime: 'image/bmp' }
  if (s.startsWith('JVBERi')) return { ext: 'pdf', mime: 'application/pdf' }
  if (s.startsWith('UEsDB') || s.startsWith('UEsFBg')) return { ext: 'zip', mime: 'application/zip' }
  return { ext: 'txt', mime: 'text/plain' }
}

const downloadBlob = (row: any, field: string) => {
  const base64 = row[field]
  if (!base64) {
    ElMessage.warning('无数据可下载')
    return
  }
  try {
    const { ext, mime } = detectBlobExtAndMime(base64)
    const byteCharacters = atob(base64)
    const byteNumbers = new Array(byteCharacters.length)
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i)
    }
    const byteArray = new Uint8Array(byteNumbers)
    const blob = new Blob([byteArray], { type: mime })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${field}.${ext}`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (e) {
    ElMessage.error('下载失败: ' + (e as Error).message)
  }
}

const clobDialogVisible = ref(false)
const clobDialogField = ref('')
const clobDialogValue = ref('')
const clobDialogReadonly = ref(true)
const clobDialogRow = ref<any>(null)

const showClobDetail = (row: any, field: string, readonly: boolean = true) => {
  const value = row[field]
  clobDialogRow.value = row
  clobDialogField.value = field
  clobDialogValue.value = value === null || value === undefined ? '' : String(value)
  clobDialogReadonly.value = readonly
  clobDialogVisible.value = true
}

const onClobDialogConfirm = () => {
  const row = clobDialogRow.value
  const field = clobDialogField.value
  if (row && field) {
    row[field] = clobDialogValue.value
    // 同时写入 editedCells，确保提交时能收集到变更
    const editKey = getEditKey(row, field)
    editedCells.value = new Map(editedCells.value.set(editKey, clobDialogValue.value))
  }
  clobDialogVisible.value = false
}

const downloadClobAsTxt = () => {
  const blob = new Blob([clobDialogValue.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${clobDialogField.value}.txt`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('下载成功')
}

const appendRows = (tabId: string, newRows: any[]) => {
  loadMoreLoading.value = false
  const tab = tabs.value.find(t => t.id === tabId)
  if (tab && newRows.length > 0) {
    tab.rows = [...tab.rows, ...newRows.map((r: any) => ({ ...r, __stkRowKey: ++rowKeyCounter }))]
    tab.pageNum += 1
  }
}

const getRowValueIgnoreCase = (row: any, key: string): any => {
  if (row[key] !== undefined) return row[key]
  const lowerKey = key.toLowerCase()
  for (const k of Object.keys(row)) {
    if (k.toLowerCase() === lowerKey) return row[k]
  }
  return undefined
}

const onSubmitEdits = async () => {
  const tab = activeTab.value
  if (!tab || !hasEdits.value) return

  const pkCol = tab.pkColumn!
  const editedRowKeys = new Set<string>()
  for (const key of editedCells.value.keys()) {
    const rowKeyStr = key.split('__')[0]
    editedRowKeys.add(rowKeyStr)
  }

  // 按行分组编辑数据
  const changesPayload: { pkValue: any; changes: Record<string, string> }[] = []
  for (const rowKeyStr of editedRowKeys) {
    const row = tab.rows.find((r: any) => String(r.__stkRowKey) === rowKeyStr)
    if (!row) continue
    const pkValue = getRowValueIgnoreCase(row, pkCol)
    if (pkValue === undefined) continue
    const rowChanges: Record<string, string> = {}
    for (const [key, value] of editedCells.value.entries()) {
      if (key.startsWith(rowKeyStr + '__')) {
        const colName = key.substring(rowKeyStr.length + 2)
        rowChanges[colName] = value
      }
    }
    if (Object.keys(rowChanges).length > 0) {
      changesPayload.push({ pkValue, changes: rowChanges })
    }
  }

  submitLoading.value = true
  try {
    const data = await request.post('/sql-window/update-rows', {
      pkDs: tab.pkDs,
      dbSchema: tab.querySchema,
      tableName: tab.queryTable,
      pkColumn: tab.pkColumn,
      changes: changesPayload,
      columnsMeta: tab.columnsMeta
    })
    ElMessage.success(`更新成功，影响 ${data || 0} 行`)
    // 更新本地数据
    for (const change of changesPayload) {
      const row = tab.rows.find((r: any) => getRowValueIgnoreCase(r, pkCol) === change.pkValue)
      if (row) {
        for (const [col, val] of Object.entries(change.changes)) {
          row[col] = val
        }
      }
    }
    editedCells.value.clear()
    editingCell.value = null
  } catch (e: any) {
    ElMessage.error(e.message || '更新失败')
  } finally {
    submitLoading.value = false
  }
}

const onAddNewRow = () => {
  const tab = activeTab.value
  if (!tab || !canEdit.value) return
  if (hasEdits.value) {
    ElMessage.warning('请先提交或取消当前修改')
    return
  }
  if (!tab.newRows) tab.newRows = []
  const newRow: any = { __stkRowKey: ++rowKeyCounter, __isNew: true }
  // 所有非BLOB/CLOB列初始为空字符串（包括主键，插入时需要输入）
  for (const col of tab.columns) {
    const meta = tab.columnsMeta?.find(m => m.name === col)
    const isBlob = meta?.type === 'BLOB'
    const isClob = meta?.type === 'CLOB'
    if (!isBlob && !isClob) {
      newRow[col] = ''
    }
  }
  tab.newRows.push(newRow)
}

const onSubmitInserts = async () => {
  const tab = activeTab.value
  if (!tab || !tab.newRows || tab.newRows.length === 0) return

  // 收集新行数据（去掉内部字段）
  const rowsPayload: Record<string, string>[] = []
  for (const row of tab.newRows) {
    const rowData: Record<string, string> = {}
    for (const [key, val] of Object.entries(row)) {
      if (key !== '__stkRowKey' && key !== '__isNew') {
        rowData[key] = String(val ?? '')
      }
    }
    if (Object.keys(rowData).length > 0) {
      rowsPayload.push(rowData)
    }
  }

  if (rowsPayload.length === 0) return

  submitLoading.value = true
  try {
    const data = await request.post('/sql-window/insert-rows', {
      pkDs: tab.pkDs,
      dbSchema: tab.querySchema,
      tableName: tab.queryTable,
      rows: rowsPayload,
      columnsMeta: tab.columnsMeta
    })
    ElMessage.success(`插入成功，影响 ${data || 0} 行`)
    // 将新行合并到正式数据中
    for (const row of tab.newRows!) {
      delete row.__isNew
      tab.rows.push(row)
    }
    tab.newRows = []
  } catch (e: any) {
    ElMessage.error(e.message || '插入失败')
  } finally {
    submitLoading.value = false
  }
}

const onCancelInserts = () => {
  const tab = activeTab.value
  if (!tab) return
  tab.newRows = []
}

const onExportQuery = async () => {
  const tab = activeTab.value
  if (!tab || !tab.pkDs || !tab.sql) return
  try {
    await ElMessageBox.confirm(
      `确定导出当前查询的全部结果（共 ${tab.total} 条）？`,
      '导出为表格',
      {confirmButtonText: '确定导出', cancelButtonText: '取消', type: 'info'}
    )
    const token = localStorage.getItem('token') || ''
    const url = `/platform/sql-window/query/export?pkDs=${tab.pkDs}&database=${encodeURIComponent(tab.querySchema || '')}&sql=${encodeURIComponent(tab.sql)}&Authentication-Token=${encodeURIComponent(token)}`
    window.open(url, '_blank')
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('导出失败: ' + (e.message || '未知错误'))
    }
  }
}

defineExpose({ setResults, clearTabs, appendRows })
</script>

<template>
  <div class="result-panel">
    <!-- Tab 页签栏 -->
    <div v-if="hasTabs" class="tab-bar">
      <div class="tab-list">
        <div
          v-for="tab in tabs"
          :key="tab.id"
          class="tab-item"
          :class="{ active: activeTabId === tab.id, fail: !tab.success }"
          @click="activeTabId = tab.id"
        >
          <span class="tab-dot" :class="{ 'dot-fail': !tab.success }"></span>
          <el-tooltip :content="tab.sql" placement="top" :show-after="500">
            <span class="tab-title">{{ tab.sql }}</span>
          </el-tooltip>
          <el-icon class="tab-close" @click.stop="closeTab(tab.id)"><Close /></el-icon>
        </div>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="tab-content-area">
      <div v-if="!hasTabs" class="empty-state">
        <el-icon :size="40" color="#c0c4cc"><Document /></el-icon>
        <p>执行SQL后在此查看结果</p>
      </div>

      <template v-else-if="activeTab">
        <!-- 错误结果 -->
        <div v-if="!activeTab.success" class="error-box">
          <el-icon color="#f56c6c" :size="16"><CircleCloseFilled /></el-icon>
          <span>{{ activeTab.message }}</span>
        </div>

        <!-- DDL/DML 结果 -->
        <div v-else-if="activeTab.dmlType !== 'SELECT'" class="affected-box">
          <el-icon color="#67c23a" :size="16"><CircleCheckFilled /></el-icon>
          <span>{{ activeTab.affectedRows !== null && activeTab.affectedRows !== undefined ? `影响行数: ${activeTab.affectedRows}` : activeTab.message || '执行成功' }}</span>
        </div>

        <!-- 查询结果 -->
        <div v-else class="query-area">
          <!-- 表格视图 -->
          <div v-show="viewMode === 'table'" class="table-area main-table">
            <StkTable
              v-for="tab in tabs"
              v-show="activeTabId === tab.id"
              :key="tab.id"
              :columns="getStkColumns(tab)"
              :data-source="[...tab.rows, ...(tab.newRows || [])]"
              row-key="__stkRowKey"
              stripe
              bordered
              virtual
              virtual-x
              :row-height="28"
              :header-row-height="28"
              show-overflow
              show-header-overflow
              row-active
              auto-resize
              class="result-stk-table"
              @row-click="onStkRowClick"
              @cell-dblclick="onCellDblClick"
              @cell-click="onCellClick"
            />
          </div>
          <!-- 单行视图 -->
          <div v-show="viewMode === 'row'" class="row-view-area main-table">
            <StkTable
              :columns="rowDetailColumns"
              :data-source="rowDetailList"
              row-key="key"
              stripe
              bordered
              :row-height="28"
              :header-row-height="28"
              show-overflow
              show-header-overflow
              auto-resize
              class="result-stk-table"
            />
          </div>
          <!-- 底部信息栏 -->
          <div class="result-footer">
            <div class="footer-left">
              <el-button size="small" text :icon="Grid" @click="onToggleView">
                {{ viewMode === 'table' ? '单行显示' : '返回表格' }}
              </el-button>
              <el-button
                size="small"
                text
                :icon="Download"
                @click="onExportQuery"
              >
                导出
              </el-button>
              <span v-if="canEdit && viewMode === 'table' && !hasNewRows" class="edit-hint">
                <el-icon><EditPen /></el-icon> 双击单元格可编辑
              </span>
              <el-button
                v-if="hasEdits"
                size="small"
                text
                type="success"
                :icon="Check"
                :loading="submitLoading"
                @click="onSubmitEdits"
              >
                提交修改
              </el-button>
              <el-button
                v-if="canEdit && viewMode === 'table' && !hasEdits && !hasNewRows"
                size="small"
                text
                type="primary"
                :icon="Plus"
                @click="onAddNewRow"
              >
                新增行
              </el-button>
              <el-button
                v-if="hasNewRows"
                size="small"
                text
                type="success"
                :icon="Check"
                :loading="submitLoading"
                @click="onSubmitInserts"
              >
                提交插入
              </el-button>
              <el-button
                v-if="hasNewRows"
                size="small"
                text
                type="danger"
                :icon="Close"
                @click="onCancelInserts"
              >
                取消插入
              </el-button>
              <el-button
                v-if="hasMore && viewMode === 'table'"
                size="small"
                text
                :icon="ArrowDown"
                :loading="loadMoreLoading"
                @click="onLoadMore"
              >
                加载下一页
              </el-button>
            </div>
            <div class="footer-right">
              <span class="footer-item footer-count">
                <el-icon><List /></el-icon>
                已加载 {{ activeTab.rows.length }} 条 / 共 {{ activeTab.total }} 条
              </span>
              <span class="footer-item footer-time">
                <el-icon><Timer /></el-icon>
                耗时: {{ activeTab.execTimeMs }}ms
              </span>
              <span v-if="activeTab.affectedRows !== null" class="footer-item">
                <el-icon><EditPen /></el-icon>
                影响: {{ activeTab.affectedRows }} 行
              </span>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- CLOB 详情弹窗 -->
    <el-dialog
      v-model="clobDialogVisible"
      :title="`字段: ${clobDialogField}`"
      width="720px"
      top="6vh"
      destroy-on-close
    >
      <el-input
        v-model="clobDialogValue"
        type="textarea"
        :rows="20"
        :readonly="clobDialogReadonly"
        resize="none"
      />
      <template #footer>
        <el-button v-if="!clobDialogReadonly" type="primary" @click="onClobDialogConfirm">确定</el-button>
        <el-button @click="downloadClobAsTxt" :icon="Download">下载为TXT</el-button>
        <el-button @click="clobDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.result-panel {
  height: 100%;
  display: flex;
  flex-direction: column;

  .tab-bar {
    display: flex;
    align-items: flex-end;
    border-bottom: 1px solid #e4e7ed;
    background: #fff;
    padding: 0 8px;
    gap: 8px;

    .tab-list {
      display: flex;
      flex: 1;
      overflow-x: auto;
      gap: 4px;

      &::-webkit-scrollbar {
        height: 2px;
      }
    }

    .tab-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 0 10px;
      height: 28px;
      background: #f5f7fa;
      border-radius: 4px 4px 0 0;
      cursor: pointer;
      font-size: 12px;
      color: #606266;
      border: 1px solid transparent;
      border-bottom: none;
      white-space: nowrap;
      transition: all 0.15s;
      user-select: none;
      flex-shrink: 0;
      max-width: 220px;

      &:hover {
        background: #e4e7ed;
        color: #303133;

        .tab-close {
          opacity: 1;
        }
      }

      &.active {
        background: #fff;
        color: #303133;
        border-color: #e4e7ed;
        border-bottom: 1px solid #fff;
        margin-bottom: -1px;
      }

      &.fail {
        .tab-title {
          color: #f56c6c;
        }
      }

      .tab-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #67c23a;
        flex-shrink: 0;

        &.dot-fail {
          background: #f56c6c;
        }
      }

      .tab-title {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .tab-close {
        font-size: 12px;
        color: #c0c4cc;
        opacity: 0;
        transition: opacity 0.15s;
        flex-shrink: 0;

        &:hover {
          color: #f56c6c;
        }
      }
    }
  }

  .tab-content-area {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .empty-state {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #909399;
      font-size: 13px;
    }

    .error-box {
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #f56c6c;
      background: #fff1f0;
      font-size: 13px;
    }

    .affected-box {
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #303133;
      font-size: 13px;
    }

    .query-area {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;

      .main-table {
        flex: 1;
        overflow: hidden;
        min-height: 0;
      }

      .table-area {
        flex: 1;
        overflow: hidden;
        min-height: 0;

        .result-stk-table {
          height: 100% !important;
          font-size: 12px;

          :deep(.stk-table) {
            height: 100%;
          }

          :deep(.stk-table-main) {
            height: 100%;
          }

          :deep(th.stk-th) {
            background: #f2f6fc;
            font-weight: 500;
            color: #606266;
          }

          :deep(tr.stk-row--active) {
            background: #ecf5ff !important;
          }

          :deep(.cell-edit-input) {
            border: none;
            width: 100%;
            height: 100%;
            padding: 0 4px;
            font-size: 12px;
            outline: none;
            background: transparent;
            box-sizing: border-box;
          }
        }
      }

      .result-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 4px 12px;
        border-top: 1px solid #e4e7ed;
        background: #fafafa;
        font-size: 12px;
        flex-shrink: 0;

        .footer-left {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .edit-hint {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #909399;
          font-size: 12px;
        }

        .footer-right {
          display: flex;
          align-items: center;
          gap: 16px;
        }

        .footer-item {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #303133;
          font-weight: 500;

          &.footer-time {
            color: #e6a23c;
          }

          &.footer-count {
            color: #409eff;
          }
        }
      }

      .row-view-area {
        flex: 1;
        overflow: hidden;
        min-height: 0;

        .result-stk-table {
          height: 100% !important;

          :deep(.stk-table) {
            height: 100%;
          }

          :deep(.stk-table-main) {
            height: 100%;
          }

          :deep(th.stk-th) {
            background: #f2f6fc;
            font-weight: 500;
            color: #606266;
          }
        }
      }
    }
  }
}
</style>

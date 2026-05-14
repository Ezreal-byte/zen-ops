<script setup lang="ts">
import {ref, onMounted, onBeforeUnmount, nextTick, watch, computed} from 'vue'
import {Splitpanes, Pane} from 'splitpanes'
import {request} from '@/utils/request'
import {ElMessage} from 'element-plus'
import {Link, VideoPlay, CaretRight, Brush, User, InfoFilled} from '@element-plus/icons-vue'
import {format} from 'sql-formatter'
import DatabaseTree from './DatabaseTree.vue'
import SqlEditor from './SqlEditor.vue'
import ResultPanel from './ResultPanel.vue'

const props = defineProps<{ ds: any; active?: boolean; modelDbSchema?: string; modelSqlText?: string }>()
const emit = defineEmits(['update:modelDbSchema', 'update:modelSqlText'])

// 使用 new URL 预加载图标，Vite 打包后路径正确
const mysqlIcon = new URL('/icons/Mysql.svg', import.meta.url).href
const oracleIcon = new URL('/icons/Oracle.svg', import.meta.url).href
const postgresqlIcon = new URL('/icons/PostgreSQL.svg', import.meta.url).href
const clickhouseIcon = new URL('/icons/ClickHouse.svg', import.meta.url).href
const databaseIcon = new URL('/icons/database.svg', import.meta.url).href

// 计算数据库类型图标
const dbTypeIcon = computed(() => {
  if (!props.ds?.dbType) return databaseIcon

  const iconMap: Record<string, string> = {
    'MYSQL': mysqlIcon,
    'ORACLE': oracleIcon,
    'POSTGRE_SQL': postgresqlIcon,
    'CLICK_HOUSE': clickhouseIcon
  }
  return iconMap[props.ds.dbType] || databaseIcon
})

const dbSchema = ref(props.modelDbSchema || props.ds?.dbSchema || '')
const sqlText = ref(props.modelSqlText || '')
const treeRef = ref()
const resultRef = ref()
const editorRef = ref()
const loading = ref(false)
const dbInfoDialogVisible = ref(false)
const dbInfo = ref<Record<string, string>>({})
const dbInfoLoading = ref(false)

// 计算显示的连接信息
const connectionDisplay = computed(() => {
  if (!props.ds) return ''
  return props.ds.connType === 'URL' || props.ds.connType === '2' ? props.ds.url : (props.ds.host + ':' + props.ds.port + ' / ' + (dbSchema.value || props.ds.dbSchema || ''))
})

// URL最大显示长度
const MAX_DISPLAY_LENGTH = 50

// 截断后的显示文本
const truncatedDisplay = computed(() => {
  const text = connectionDisplay.value
  if (!text) return ''
  return text.length > MAX_DISPLAY_LENGTH ? text.substring(0, MAX_DISPLAY_LENGTH) + '...' : text
})

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

// 数据源标签
const dsTags = computed(() => {
  return parseTags(props.ds?.tags || '')
})

watch(dbSchema, (val) => emit('update:modelDbSchema', val))
watch(sqlText, (val) => emit('update:modelSqlText', val))

// 全局快捷键处理
const handleKeydown = (e: KeyboardEvent) => {
  // 非活跃窗口不响应
  if (!props.active) return
  // Ctrl+R: 执行全部
  if (e.ctrlKey && !e.shiftKey && e.key === 'r') {
    e.preventDefault()
    onExecute('all')
    return
  }
  // Ctrl+Shift+R: 执行选中
  if (e.ctrlKey && e.shiftKey && e.key === 'R') {
    e.preventDefault()
    onExecute('selected')
    return
  }
}

onMounted(() => {
  if (!dbSchema.value && props.ds?.dbSchema) {
    dbSchema.value = props.ds.dbSchema
  }
  nextTick(() => {
    treeRef.value?.loadDatabases(props.ds?.pkDs)
  })
  // 注册全局快捷键
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

const onTestConnection = async () => {
  try {
    const data: any = await request.post(`/sql-window/test-connection/${props.ds.pkDs}`)
    if (data?.connected) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.error(data?.msg || '连接失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '连接失败')
  }
}

const onExecute = async (mode: 'all' | 'selected') => {
  let sql = sqlText.value
  if (mode === 'selected') {
    const selected = editorRef.value?.getSelectedSql()
    if (!selected || !selected.trim()) {
      ElMessage.warning('请先选中要执行的SQL')
      return
    }
    sql = selected
  }
  if (!sql.trim()) {
    ElMessage.warning('请输入SQL')
    return
  }
  loading.value = true
  // 执行前清空结果页签
  resultRef.value?.clearTabs()
  try {
    const data = await request.post('/sql-window/execute', {
      pkDs: props.ds.pkDs,
      sqlText: sql,
      pageNum: 1,
      pageSize: 1000
    })
    resultRef.value?.setResults(data || [], props.ds.pkDs, dbSchema.value)
  } catch (e: any) {
    ElMessage.error(e.message || '执行失败')
  } finally {
    loading.value = false
  }
}

const onLoadMore = async (payload: any) => {
  try {
    const data = await request.post('/sql-window/execute', {
      pkDs: props.ds.pkDs,
      sqlText: payload.sql,
      pageNum: payload.pageNum,
      pageSize: payload.pageSize
    })

    // 处理返回数据
    if (data && Array.isArray(data) && data.length > 0) {
      const newRows = data[0].rows || []
      resultRef.value?.appendRows(payload.tabId, newRows)
    } else {
      ElMessage.warning('没有更多数据')
      // 即使没有数据也要关闭 loading
      resultRef.value?.appendRows(payload.tabId, [])
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
    // 错误时也要关闭 loading
    resultRef.value?.appendRows(payload.tabId, [])
  }
}

const formatLanguageMap: Record<string, string> = {
  'MYSQL': 'mysql',
  'ORACLE': 'plsql',
  'POSTGRE_SQL': 'postgresql',
  'CLICK_HOUSE': 'sql'
}

const onFormatSql = () => {
  const language = formatLanguageMap[props.ds?.dbType] || 'sql'
  const formatted = format(sqlText.value, {
    language: language as any,
    keywordCase: 'upper',
    linesBetweenQueries: 2
  })
  sqlText.value = formatted
}

const onNodeSelect = (node: any) => {
  if (node.type === 'table') {
    const sql = `SELECT * FROM ${node.dbName}.${node.name};`
    const current = sqlText.value.trim()
    if (current) {
      sqlText.value = current + '\n' + sql
    } else {
      sqlText.value = sql
    }
  }
}

const onTreeExecuteSql = (sql: string) => {
  const current = sqlText.value.trim()
  if (current) {
    sqlText.value = current + '\n' + sql
  } else {
    sqlText.value = sql
  }
}

// 复制连接信息到剪贴板
const copyConnectionInfo = async () => {
  try {
    await navigator.clipboard.writeText(connectionDisplay.value)
    ElMessage.success('已复制到剪贴板')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

// 显示数据库信息
const showDbInfo = async () => {
  dbInfoDialogVisible.value = true
  dbInfoLoading.value = true
  try {
    dbInfo.value = await request.get(`/sql-window/db-info/${props.ds.pkDs}`)
  } catch (e: any) {
    ElMessage.error(e.message || '获取数据库信息失败')
    dbInfo.value = {}
  } finally {
    dbInfoLoading.value = false
  }
}
</script>

<template>
  <div class="sql-tab">
    <div class="tab-toolbar">
      <div class="toolbar-left">
        <img class="ds-type-icon" :src="dbTypeIcon" />
        <span class="ds-name">{{ ds.name }}</span>
        <span class="ds-info" @click="copyConnectionInfo" :title="connectionDisplay">
          {{ truncatedDisplay }}
        </span>
        <span class="ds-user">
          <el-icon><User /></el-icon>
          {{ ds.userName || '-' }}
        </span>
        <!-- 标签显示 -->
        <div class="ds-tags" v-if="dsTags.length > 0">
          <el-tag
            v-for="(tag, index) in dsTags"
            :key="index"
            :type="tag.type"
            size="small"
          >
            {{ tag.label }}
          </el-tag>
        </div>

      </div>
      <div class="toolbar-right">
        <div class="icon-btn-group">
          <div class="icon-btn" @click="showDbInfo" title="数据库信息">
            <el-icon><InfoFilled /></el-icon>
          </div>
        </div>
        <el-button size="small" text :icon="Link" @click="onTestConnection">测试连接</el-button>
        <el-tooltip content="Ctrl+R" placement="bottom" :show-after="500"><el-button size="small" text type="primary" :icon="VideoPlay" :loading="loading" @click="onExecute('all')">执行全部</el-button></el-tooltip>
        <el-tooltip content="Ctrl+Shift+R" placement="bottom" :show-after="500"><el-button size="small" text type="success" :icon="CaretRight" :loading="loading" @click="onExecute('selected')">执行选中</el-button></el-tooltip>
        <el-button size="small" text :icon="Brush" @click="onFormatSql">SQL美化</el-button>
<!--        <span class="warning-text">数据无价，谨慎操作</span>-->
      </div>
    </div>
    <div class="tab-main">
      <splitpanes class="default-theme">
        <pane size="20" min-size="15">
          <div class="panel-card tree-panel">
            <DatabaseTree ref="treeRef" :pk-ds="ds?.pkDs" @node-select="onNodeSelect" @execute-sql="onTreeExecuteSql"/>
          </div>
        </pane>
        <pane>
          <splitpanes horizontal>
            <pane size="50" min-size="20">
              <div class="panel-card editor-panel">
                <SqlEditor ref="editorRef" v-model="sqlText"/>
              </div>
            </pane>
            <pane size="50" min-size="20">
              <div class="panel-card result-panel">
                <ResultPanel ref="resultRef" @load-more="onLoadMore"/>
              </div>
            </pane>
          </splitpanes>
        </pane>
      </splitpanes>
    </div>

    <!-- 数据库信息弹窗 -->
    <el-dialog v-model="dbInfoDialogVisible" title="数据库信息" width="500px" top="15vh">
      <div v-loading="dbInfoLoading" class="db-info-content">
        <div v-if="Object.keys(dbInfo).length > 0" class="info-list">
          <div v-for="(value, key) in dbInfo" :key="key" class="info-item">
            <span class="info-label">{{ key }}</span>
            <span class="info-value">{{ value }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无信息" :image-size="80" />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.sql-tab {
  height: 100%;
  display: flex;
  flex-direction: column;

  .tab-toolbar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    border-bottom: 1px solid #e4e7ed;
    background: #fff;
    border-radius: 4px;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .ds-name {
        font-size: 13px;
        font-weight: 600;
        color: #303133;
      }

      .ds-info {
        font-size: 12px;
        color: #909399;
        max-width: 400px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        cursor: pointer;
        transition: color 0.2s;

        &:hover {
          color: #409eff;
        }
      }

      .ds-type-icon {
        width: 18px;
        height: 18px;
        flex-shrink: 0;
      }

      .ds-user {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #606266;
      }

      .ds-tags {
        display: flex;
        align-items: center;
        gap: 4px;
      }

    }

    .toolbar-right {
      display: flex;
      align-items: center;
      margin-left: auto;

      .warning-text {
        font-size: 12px;
        color: #f56c6c;
        font-weight: 500;
        white-space: nowrap;
        margin-left: 8px;
      }

      .icon-btn-group {
        display: flex;
        align-items: center;
        background: #f5f7fa;
        border-radius: 4px;
        //padding: 2px;
        gap: 2px;
        font-size: 16px;

        .icon-btn {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 24px;
          height: 24px;
          border-radius: 4px;
          cursor: pointer;
          color: #606266;
          transition: all 0.2s;

          &:hover {
            background: #e4e7ed;
            color: #409eff;
          }
        }
      }
    }
  }

  .tab-main {
    flex: 1;
    overflow: hidden;
    padding: 5px 0;
    gap: 5px;

    .panel-card {
      height: 100%;
      border-radius: 4px;
      border: 1px solid #e4e7ed;
      background: #fff;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
      overflow: hidden;

      :deep(.el-card__body) {
        padding: 0;
        height: 100%;
      }
    }

    .tree-panel {
      :deep(.db-tree-card) {
        border: none;
        box-shadow: none;
      }

      :deep(.tree-header) {
        padding: 4px 10px;
      }
    }
  }
}

:deep(.splitpanes__splitter) {
  background-color: #e4e7ed;
  position: relative;

  &:hover {
    background-color: #409eff;
  }

  &::before {
    content: '';
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 2px;
    height: 24px;
    background-color: #c0c4cc;
    border-radius: 1px;
  }
}

:deep(.splitpanes--horizontal > .splitpanes__splitter) {
  &::before {
    width: 24px;
    height: 2px;
  }
}

.db-info-content {
  min-height: 100px;

  .info-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .info-item {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      background: #f5f7fa;
      border-radius: 4px;

      .info-label {
        font-size: 13px;
        color: #606266;
        font-weight: 500;
        min-width: 100px;
        flex-shrink: 0;
      }

      .info-value {
        font-size: 13px;
        color: #303133;
        font-family: 'Courier New', monospace;
      }
    }
  }
}
</style>

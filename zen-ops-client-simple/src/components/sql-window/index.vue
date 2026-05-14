<script setup lang="ts">
import {ref, watch, onMounted} from 'vue'
import {ElMessage} from 'element-plus'
import {DataLine, Close} from '@element-plus/icons-vue'
import {request} from '@/utils/request'
import SqlWindowTab from './SqlWindowTab.vue'
import DsDataSourceDialog from './DsDataSourceDialog.vue'

// 组件名称，用于keep-alive缓存
defineOptions({ name: 'SqlWindow' })

const HISTORY_KEY = 'sql-window-history'

interface TabItem {
  id: string
  ds: any
  dbSchema: string
  sqlText: string
}

const tabs = ref<TabItem[]>([])
const activeTabId = ref('')
const dialogVisible = ref(false)
let tabCounter = 0

const activeTab = ref<TabItem | null>(null)

const addTab = (ds: any, initialDbSchema?: string, initialSqlText?: string) => {
  const id = `sql_tab_${++tabCounter}`
  const tab = { id, ds, dbSchema: initialDbSchema || ds?.dbSchema || '', sqlText: initialSqlText || '' }
  tabs.value.push(tab)
  activeTabId.value = id
  activeTab.value = tab
}

const closeTab = (id: string) => {
  const idx = tabs.value.findIndex(t => t.id === id)
  if (idx === -1) return
  tabs.value.splice(idx, 1)
  if (activeTabId.value === id) {
    const next = tabs.value[Math.min(idx, tabs.value.length - 1)]
    activeTabId.value = next?.id || ''
    activeTab.value = next || null
  }
}

const onSelectDs = (ds: any) => {
  addTab(ds)
  dialogVisible.value = false
}

// 保存历史到 localStorage
const saveHistory = () => {
  const history = {
    counter: tabCounter,
    tabs: tabs.value.map(t => ({
      pkDs: t.ds?.pkDs,
      dbSchema: t.dbSchema,
      sqlText: t.sqlText
    }))
  }
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history))
}

watch(tabs, saveHistory, { deep: true })

// 恢复历史
const restoreHistory = async () => {
  const raw = localStorage.getItem(HISTORY_KEY)
  if (!raw) return
  try {
    const history = JSON.parse(raw)
    if (!history.tabs?.length) return
    tabCounter = history.counter || 0
    const dsList: any[] = await request.get('/sql-window/datasource/list')
    for (const item of history.tabs) {
      const ds = dsList.find((d: any) => String(d.pkDs) === String(item.pkDs))
      if (ds) {
        addTab(ds, item.dbSchema, item.sqlText)
      }
    }
  } catch (e) {
    console.error('恢复SQL窗口历史失败', e)
  }
}

onMounted(() => {
  restoreHistory()
})
</script>

<template>
  <div class="sql-window">
    <!-- Tab 栏 -->
    <div class="sql-toolbar">
      <div class="toolbar-left">
        <div class="manager-icon" @click="dialogVisible = true" title="管理数据源">
          <el-icon :size="18"><DataLine /></el-icon>
          <span class="manager-text">数据源</span>
        </div>
        <div class="toolbar-sep"></div>
        <div class="tab-bar">
          <div
            v-for="tab in tabs"
            :key="tab.id"
            class="tab-item"
            :class="{ active: activeTabId === tab.id }"
            @click="activeTabId = tab.id; activeTab = tab"
          >
            <span class="tab-dot" :class="tab.ds.dbType"></span>
            <span class="tab-name">{{ tab.ds.name }}</span>
            <el-icon class="tab-close" :size="12" @click.stop="closeTab(tab.id)"><Close /></el-icon>
          </div>
        </div>
      </div>
      <div class="toolbar-right"></div>
    </div>

    <!-- 内容区 -->
    <div class="tab-content">
      <div v-if="tabs.length === 0" class="empty-state">
        <el-icon :size="48" color="#c0c4cc"><DataLine /></el-icon>
        <p>请点击左上角"数据源"选择一个数据源开始</p>
      </div>
      <template v-else>
        <SqlWindowTab
          v-for="tab in tabs"
          :key="tab.id"
          v-show="activeTabId === tab.id"
          :ds="tab.ds"
          :active="activeTabId === tab.id"
          v-model:model-db-schema="tab.dbSchema"
          v-model:model-sql-text="tab.sqlText"
        />
      </template>
    </div>

    <!-- 数据源选择弹窗 -->
    <DsDataSourceDialog
      v-model="dialogVisible"
      select-mode
      @select="onSelectDs"
    />
  </div>
</template>

<style scoped lang="less">
.sql-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;

  .sql-toolbar {
    height: 38px;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 12px;
    flex-shrink: 0;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 0;
      flex: 1;
      overflow: hidden;
    }

    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-shrink: 0;
    }

    .manager-icon {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 0 8px;
      height: 32px;
      cursor: pointer;
      color: #606266;
      border-radius: 3px;
      transition: all 0.15s;
      flex-shrink: 0;

      &:hover {
        background: #f5f7fa;
        color: #409eff;
      }

      .manager-text {
        font-size: 12px;
        font-weight: 500;
        white-space: nowrap;
      }
    }

    .toolbar-sep {
      width: 1px;
      height: 20px;
      background: #dcdfe6;
      margin: 0 6px;
      flex-shrink: 0;
    }

    .tab-bar {
      display: flex;
      align-items: flex-end;
      gap: 2px;
      flex: 1;
      overflow-x: auto;
      padding-bottom: 1px;

      &::-webkit-scrollbar {
        display: none;
      }
    }

    .tab-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 0 12px;
      height: 30px;
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

      &:hover {
        background: #e4e7ed;
        color: #303133;

        .tab-close {
          opacity: 1;
        }
      }

      &.active {
        background: #fff;
        color: #409eff;
        border-color: #e4e7ed;
        border-bottom: 1px solid #fff;
        margin-bottom: -1px;
        z-index: 1;

        .tab-close {
          opacity: 1;
        }
      }

      .tab-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #c0c4cc;
        flex-shrink: 0;

        &.MYSQL { background: #67c23a; }
        &.ORACLE { background: #d81e06; }
        &.POSTGRE_SQL { background: #306092; }
        &.CLICK_HOUSE { background: #ffcb14; }
      }

      .tab-name {
        max-width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
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

  .tab-content {
    flex: 1;
    overflow: hidden;
    padding: 5px;
    gap: 5px;

    .empty-state {
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
      color: #909399;
      font-size: 14px;
      background: #fff;
      border-radius: 4px;
      border: 1px solid #e4e7ed;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
    }
  }
}
</style>

<template>
  <div class="sql-audit-page">
    <div class="sql-audit-card">
      <!-- 查询条件区 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select
            v-model="searchForm.pkCreatedby"
            placeholder="操作人"
            clearable
            size="small"
            style="width: 130px"
            @change="onSearch"
          >
            <el-option
              v-for="user in userList"
              :key="user.pkUser"
              :label="user.name || user.userName"
              :value="user.pkUser"
            />
          </el-select>
          <el-select
            v-model="searchForm.dbSchema"
            placeholder="操作数据库"
            clearable
            size="small"
            style="width: 140px"
            @change="onSearch"
          >
            <el-option v-for="schema in schemaList" :key="schema" :label="schema" :value="schema" />
          </el-select>
          <el-select
            v-model="searchForm.sqlType"
            placeholder="操作类型"
            clearable
            size="small"
            style="width: 110px"
            @change="onSearch"
          >
            <el-option label="DDL" value="DDL" />
            <el-option label="DML" value="DML" />
          </el-select>
          <el-select
            v-model="searchForm.execStatus"
            placeholder="执行状态"
            clearable
            size="small"
            style="width: 100px"
            @change="onSearch"
          >
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
          </el-select>
          <el-input
            v-model="searchForm.keyword"
            placeholder="SQL关键字"
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
          <el-button size="small" @click="onReset">重置</el-button>
        </div>
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
          <el-table-column label="执行时间" width="170" align="center">
            <template #default="{ row }">{{ row.dtCreated }}</template>
          </el-table-column>
          <el-table-column label="操作人" width="100" align="center">
            <template #default="{ row }">{{ row.userName || row.userLoginName || '-' }}</template>
          </el-table-column>
          <el-table-column label="数据库" width="120" align="center">
            <template #default="{ row }">{{ row.dbSchema || '-'  }}</template>
          </el-table-column>
          <el-table-column label="类型" width="70" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.sqlType === 'DDL' ? 'warning' : 'primary'">{{ row.sqlType || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="DML子类型" width="90" align="center" v-if="hasDmlType">
            <template #default="{ row }">
              <el-tag v-if="row.dmlType" size="small" type="success">{{ row.dmlType }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="查询表" width="120" align="center" v-if="hasQueryTable">
            <template #default="{ row }">{{ row.queryTable || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="70" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.execStatus === 'SUCCESS' ? 'success' : 'danger'">
                {{ row.execStatus === 'SUCCESS' ? '成功' : row.execStatus === 'FAIL' ? '失败' : '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="耗时" width="80" align="center">
            <template #default="{ row }">{{ row.execTimeMs ? row.execTimeMs + 'ms' : '-' }}</template>
          </el-table-column>
          <el-table-column label="影响行数" width="90" align="center">
            <template #default="{ row }">
              <span :title="row.sqlType === 'QUERY' ? '查询结果总数' : '影响行数'">
                {{ row.affectedRows ?? '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="SQL内容" min-width="300">
            <template #default="{ row }">
              <div class="sql-cell" @click="showSqlDialog(row.sqlText)" :title="row.sqlText">
                {{ row.sqlText || '-' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="错误信息" min-width="200">
            <template #default="{ row }">
              <div 
                class="error-cell" 
                :class="{ 'success-text': row.errorMsg === '查询成功' }"
                @click="showErrorDialog(row.errorMsg)"
                :title="row.errorMsg"
              >
                {{ row.errorMsg || '-' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="showDetailDialog(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页区 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[50, 100, 200]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          size="small"
          background
          @current-change="fetchData"
          @size-change="onSizeChange"
        />
      </div>
    </div>

    <!-- SQL内容弹窗 -->
    <el-dialog v-model="sqlDialogVisible" title="SQL内容" width="800px" top="10vh">
      <div class="sql-dialog-content">{{ currentSql }}</div>
    </el-dialog>

    <!-- 错误信息弹窗 -->
    <el-dialog v-model="errorDialogVisible" :title="errorDialogTitle" width="800px" top="10vh">
      <div class="error-dialog-content" :class="{ 'success-text': currentError === '查询成功' }">
        {{ currentError }}
      </div>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="SQL执行详情" width="900px" top="5vh">
      <div class="detail-container" v-if="currentDetail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="执行时间">{{ currentDetail.dtCreated }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentDetail.userName || currentDetail.userLoginName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="数据源ID">{{ currentDetail.pkDs || '-' }}</el-descriptions-item>
          <el-descriptions-item label="数据库">{{ currentDetail.dbSchema || '-' }}</el-descriptions-item>
          <el-descriptions-item label="SQL类型">
            <el-tag size="small" :type="currentDetail.sqlType === 'DDL' ? 'warning' : 'primary'">{{ currentDetail.sqlType || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="DML子类型">
            <el-tag v-if="currentDetail.dmlType" size="small" type="success">{{ currentDetail.dmlType }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="执行状态">
            <el-tag size="small" :type="currentDetail.execStatus === 'SUCCESS' ? 'success' : 'danger'">
              {{ currentDetail.execStatus === 'SUCCESS' ? '成功' : currentDetail.execStatus === 'FAIL' ? '失败' : '-' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行耗时">{{ currentDetail.execTimeMs ? currentDetail.execTimeMs + 'ms' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="影响行数/查询总数">
            <span :title="currentDetail.sqlType === 'QUERY' ? '查询结果总数' : '影响行数'">
              {{ currentDetail.affectedRows ?? '-' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="单表查询">
            <el-tag v-if="currentDetail.singleTableQuery !== null" size="small" :type="currentDetail.singleTableQuery ? 'success' : 'info'">
              {{ currentDetail.singleTableQuery ? '是' : '否' }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="查询表名">{{ currentDetail.queryTable || '-' }}</el-descriptions-item>
          <el-descriptions-item label="主键列名">{{ currentDetail.pkColumn || '-' }}</el-descriptions-item>
          <el-descriptions-item label="查询Schema">{{ currentDetail.querySchema || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <div class="section-title">SQL内容</div>
          <div class="sql-content">{{ currentDetail.sqlText || '-' }}</div>
        </div>

        <div class="detail-section" v-if="currentDetail.errorMsg">
          <div class="section-title" :class="{ 'success-text': currentDetail.errorMsg === '查询成功' }">
            {{ currentDetail.errorMsg === '查询成功' ? '执行结果' : '错误信息' }}
          </div>
          <div class="error-content" :class="{ 'success-text': currentDetail.errorMsg === '查询成功' }">
            {{ currentDetail.errorMsg }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { request } from '@/utils/request'

defineOptions({
  name: 'SqlAuditLog'
})

const loading = ref(false)
const tableData = ref<any[]>([])
const userList = ref<any[]>([])
const schemaList = ref<string[]>([])

// 动态列显示
const hasDmlType = ref(false)
const hasQueryTable = ref(false)

// 查询条件
const searchForm = ref({
  pkCreatedby: undefined as number | undefined,
  dbSchema: '',
  sqlType: '',
  execStatus: '',
  keyword: ''
})

const pageNum = ref(1)
const pageSize = ref(100)
const total = ref(0)

// SQL和错误信息弹窗
const sqlDialogVisible = ref(false)
const currentSql = ref('')
const errorDialogVisible = ref(false)
const currentError = ref('')
const errorDialogTitle = ref('错误信息')

// 详情弹窗
const detailDialogVisible = ref(false)
const currentDetail = ref<any>(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/sql/audit/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        pkCreatedby: searchForm.value.pkCreatedby || undefined,
        dbSchema: searchForm.value.dbSchema || undefined,
        sqlType: searchForm.value.sqlType || undefined,
        execStatus: searchForm.value.execStatus || undefined,
        keyword: searchForm.value.keyword || undefined
      }
    })
    // request 已自动解包，res 就是 data 字段
    tableData.value = res?.list || []
    total.value = res?.totalCount || 0
    
    // 检测是否有DML子类型和查询表数据，动态显示列
    hasDmlType.value = tableData.value.some(row => row.dmlType)
    hasQueryTable.value = tableData.value.some(row => row.queryTable)
  } finally {
    loading.value = false
  }
}

const fetchUsers = async () => {
  const res = await request.get('/sql/audit/users')
  userList.value = res || []
}

const fetchSchemas = async () => {
  const res = await request.get('/sql/audit/schemas')
  schemaList.value = res || []
}

const getUserName = (pkCreatedby: number) => {
  const user = userList.value.find(u => u.pkUser === pkCreatedby)
  return user ? (user.name || user.userName) : '-'
}

const onSearch = () => {
  pageNum.value = 1
  fetchData()
}

const onReset = () => {
  searchForm.value = {
    pkCreatedby: undefined,
    dbSchema: '',
    sqlType: '',
    execStatus: '',
    keyword: ''
  }
  pageNum.value = 1
  fetchData()
}

const onSizeChange = () => {
  pageNum.value = 1
  fetchData()
}

const showSqlDialog = (sqlText: string) => {
  if (!sqlText) return
  currentSql.value = sqlText
  sqlDialogVisible.value = true
}

const showErrorDialog = (errorMsg: string) => {
  if (!errorMsg) return
  currentError.value = errorMsg
  errorDialogTitle.value = errorMsg === '查询成功' ? '执行结果' : '错误信息'
  errorDialogVisible.value = true
}

const showDetailDialog = (row: any) => {
  currentDetail.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchData()
  fetchUsers()
  fetchSchemas()
})
</script>

<style scoped lang="less">
.sql-audit-page {
  height: 100%;
  padding: 5px;
  box-sizing: border-box;
}

.sql-audit-card {
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
    flex-wrap: wrap;
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

.sql-cell {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Cascadia Code', Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  color: #303133;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #409eff;
  }
}

.error-cell {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: #f56c6c;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    opacity: 0.8;
  }
}

.success-text {
  color: #67c23a !important;
}

.sql-dialog-content {
  font-family: 'Cascadia Code', Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 60vh;
  overflow-y: auto;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.error-dialog-content {
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 60vh;
  overflow-y: auto;
  padding: 12px;
  background: #fef0f0;
  border-radius: 4px;
  color: #f56c6c;

  &.success-text {
    background: #f0f9eb;
    color: #67c23a;
  }
}

.detail-container {
  .detail-section {
    margin-top: 20px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
      padding-bottom: 8px;
      border-bottom: 1px solid #ebeef5;

      &.success-text {
        color: #67c23a;
      }
    }

    .sql-content {
      font-family: 'Cascadia Code', Menlo, Monaco, Consolas, monospace;
      font-size: 13px;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-all;
      max-height: 40vh;
      overflow-y: auto;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 4px;
      color: #303133;
    }

    .error-content {
      font-size: 13px;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-all;
      max-height: 30vh;
      overflow-y: auto;
      padding: 12px;
      background: #fef0f0;
      border-radius: 4px;
      color: #f56c6c;

      &.success-text {
        background: #f0f9eb;
        color: #67c23a;
      }
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
</style>

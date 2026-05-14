<template>
  <div class="bucket-sidebar">
    <div class="sidebar-header">
      <span class="sidebar-title">Buckets</span>
      <el-button link type="primary" size="small" @click="emit('create')" title="创建桶">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>
    <div class="sidebar-list" v-loading="loading">
      <div
        v-for="bucket in list"
        :key="bucket.name"
        class="bucket-card"
        :class="{ active: active === bucket.name }"
        @click="emit('select', bucket)"
      >
        <div class="card-header">
          <el-icon class="bucket-icon" :size="16"><FolderOpened /></el-icon>
          <span class="bucket-name" :title="bucket.name">{{ bucket.name }}</span>
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, bucket)" @click.stop>
            <el-icon class="more-icon" :size="14"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="detail">
                  <el-icon><InfoFilled /></el-icon> 查看详情
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <el-icon><Delete /></el-icon> 删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="card-info">
          <span class="creation-time">{{ bucket.creationDate || '未知' }}</span>
        </div>
      </div>
      <div v-if="!loading && list.length === 0" class="sidebar-empty">暂无桶</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, FolderOpened, Delete, MoreFilled, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps<{
  list: any[]
  active: string
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'select', bucket: any): void
  (e: 'delete', bucket: any): void
  (e: 'create'): void
  (e: 'detail', bucket: any): void
}>()

// 处理下拉菜单命令
const handleCommand = (command: string, bucket: any) => {
  if (command === 'detail') {
    emit('detail', bucket)
  } else if (command === 'delete') {
    emit('delete', bucket)
  }
}
</script>

<style lang="less" scoped>
.bucket-sidebar {
  position: relative;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  width: 220px;
}

.sidebar-header {
  height: 34px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
  background: #fff;
}

.sidebar-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bucket-card {
  background: #f8f9fa;
  border-radius: 4px;
  padding: 6px 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;

  &:hover {
    background: #f0f2f5;
    border-color: #dcdfe6;
  }

  &.active {
    background: #ecf5ff;
    border-color: #409eff;
  }
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
  gap: 6px;
}

.bucket-icon {
  flex-shrink: 0;
  color: #e6a23c;
}

.bucket-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.more-icon {
  flex-shrink: 0;
  color: #909399;
  cursor: pointer;
  padding: 2px;
  border-radius: 3px;
  transition: all 0.2s;

  &:hover {
    background: #e4e7ed;
    color: #409eff;
  }
}

.card-info {
  padding-left: 22px;
}

.creation-time {
  font-size: 11px;
  color: #909399;
}

.sidebar-empty {
  padding: 30px 0;
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
}
</style>

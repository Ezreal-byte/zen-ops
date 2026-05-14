<template>
  <el-popover
    placement="bottom-start"
    :width="360"
    trigger="click"
    v-model:visible="popoverVisible"
  >
    <template #reference>
      <div class="icon-picker-trigger">
        <el-input
          :model-value="modelValue"
          placeholder="请选择图标"
          size="small"
          readonly
          style="cursor: pointer"
        >
          <template #prefix>
            <el-icon v-if="modelValue" style="vertical-align: middle">
              <component :is="modelValue" />
            </el-icon>
          </template>
        </el-input>
      </div>
    </template>
    <div class="icon-picker">
      <el-input
        v-model="searchKey"
        placeholder="搜索图标"
        size="small"
        clearable
        style="margin-bottom: 8px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div class="icon-grid">
        <div
          v-for="name in filteredIcons"
          :key="name"
          class="icon-item"
          :class="{ 'is-active': modelValue === name }"
          :title="name"
          @click="handleSelect(name)"
        >
          <el-icon :size="18"><component :is="name" /></el-icon>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'

// 常用 Element Plus 图标列表
const ICON_LIST = [
  'House', 'HomeFilled', 'Setting', 'Tools', 'Grid', 'Menu',
  'Document', 'Folder', 'FolderOpened', 'Files', 'Notebook',
  'User', 'UserFilled', 'Avatar', 'Key', 'Lock', 'Unlock',
  'Search', 'View', 'Hide', 'ZoomIn', 'ZoomOut', 'FullScreen',
  'Plus', 'Minus', 'Check', 'Close', 'CircleCheck', 'CircleClose',
  'InfoFilled', 'WarningFilled', 'QuestionFilled', 'SuccessFilled',
  'Edit', 'EditPen', 'Delete', 'CopyDocument', 'DocumentCopy',
  'Refresh', 'RefreshRight', 'RefreshLeft',
  'Upload', 'Download', 'UploadFilled', 'DownloadFilled',
  'Link', 'Connection', 'Share', 'ChatDotRound', 'Message',
  'Bell', 'Notification', 'Phone', 'PhoneFilled', 'Iphone',
  'Monitor', 'Computer', 'Platform', 'DataBoard', 'DataLine', 'DataAnalysis',
  'Management', 'Operation', 'Opportunity', 'Position', 'Location',
  'Guide', 'Compass', 'MapLocation', 'Place',
  'Calendar', 'Clock', 'Timer', 'AlarmClock',
  'PieChart', 'TrendCharts', 'Histogram', 'Coin', 'Wallet',
  'ShoppingCart', 'ShoppingBag', 'Goods', 'Box', 'Present',
  'Star', 'StarFilled', 'Medal', 'Trophy',
  'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight',
  'DArrowRight', 'SortUp', 'SortDown',
  'CaretRight', 'CaretLeft', 'CaretTop', 'CaretBottom',
  'TopRight', 'TopLeft', 'BottomRight', 'BottomLeft',
  'SetUp', 'SwitchButton', 'TurnOff', 'Open',
  'Filter', 'MagicStick',
  'Ship', 'Sunny', 'Moon', 'Cloudy'
]

defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const popoverVisible = ref(false)
const searchKey = ref('')

const filteredIcons = computed(() => {
  const key = searchKey.value.trim().toLowerCase()
  if (!key) return ICON_LIST
  return ICON_LIST.filter(name => name.toLowerCase().includes(key))
})

const handleSelect = (name: string) => {
  emit('update:modelValue', name)
  popoverVisible.value = false
}
</script>

<style scoped lang="less">
.icon-picker-trigger {
  cursor: pointer;
}

.icon-picker {
  .icon-grid {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 4px;
    max-height: 260px;
    overflow-y: auto;
    overflow-x: hidden;
  }

  .icon-item {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.15s;
    border: 1px solid transparent;

    &:hover {
      background: #ecf5ff;
      border-color: #c6e2ff;
    }

    &.is-active {
      background: #409eff;
      color: #fff;
      border-color: #409eff;
    }
  }
}
</style>

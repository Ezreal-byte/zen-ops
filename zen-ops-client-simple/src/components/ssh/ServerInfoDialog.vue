<template>
  <el-dialog
    v-model="visible"
    title="服务器信息"
    width="1200px"
    destroy-on-close
    append-to-body
    class="server-info-dialog-wrapper"
  >
    <div class="server-info-container" v-loading="loading">
      <div class="layout-wrapper">
        <!-- 左侧：基本信息 -->
        <div class="left-panel">
          <div class="info-cards">
            <div class="info-card">
              <div class="info-card-label">服务器名称</div>
              <div class="info-card-value">{{ info.serverName || '-' }}</div>
            </div>
            <div class="info-card">
              <div class="info-card-label">IP地址</div>
              <div class="info-card-value mono">{{ info.ip }}:{{ info.port }}</div>
            </div>
            <div class="info-card">
              <div class="info-card-label">登录用户</div>
              <div class="info-card-value">{{ info.user || '-' }}</div>
            </div>
            <div class="info-card">
              <div class="info-card-label">主机名</div>
              <div class="info-card-value">{{ info.hostname || '-' }}</div>
            </div>
            <div class="info-card">
              <div class="info-card-label">操作系统</div>
              <div class="info-card-value">{{ info.osVersion || '-' }}</div>
            </div>
            <div class="info-card">
              <div class="info-card-label">内核版本</div>
              <div class="info-card-value mono">{{ info.kernel || '-' }}</div>
            </div>
            <div class="info-card">
              <div class="info-card-label">运行时间</div>
              <div class="info-card-value">{{ info.uptime || '-' }}</div>
            </div>
            <div class="info-card">
              <div class="info-card-label">CPU型号</div>
              <div class="info-card-value">{{ info.cpuModel || '-' }}</div>
            </div>
          </div>
        </div>

        <!-- 右侧：资源监控 -->
        <div class="right-panel">
          <!-- 第一行：内存监控（全宽） -->
          <div class="monitor-card">
            <div class="monitor-header">
              <span class="monitor-title">内存使用率</span>
              <span class="monitor-value" :class="getUsageClass(info.memUsage)">{{ info.memUsage || '-' }}</span>
            </div>
            <div ref="memChartRef" class="chart-container chart-container-large"></div>
            <div class="usage-bar-card">
              <div class="usage-bar-track">
                <div
                  class="usage-bar-fill mem"
                  :style="{ width: parseUsage(info.memUsage) + '%' }"
                ></div>
                <div class="usage-bar-overlay">
                  <div class="usage-bar-left">
                    <span class="usage-bar-icon">MEM</span>
                    <span class="usage-bar-detail">{{ info.memUsed || '-' }} / {{ info.memTotal || '-' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 第二行：CPU和磁盘监控 -->
          <div class="monitor-row">
            <!-- CPU 监控（65%） -->
            <div class="monitor-card monitor-card-cpu">
              <div class="monitor-header">
                <span class="monitor-title">CPU 使用率</span>
                <span class="monitor-value" :class="getUsageClass(info.cpuUsage)">{{ info.cpuUsage || '-' }}</span>
              </div>
              <div ref="cpuChartRef" class="chart-container"></div>
              <div class="usage-bar-card">
                <div class="usage-bar-track">
                  <div
                    class="usage-bar-fill cpu"
                    :style="{ width: parseUsage(info.cpuUsage) + '%' }"
                  ></div>
                  <div class="usage-bar-overlay">
                    <div class="usage-bar-left">
                      <span class="usage-bar-icon">CPU</span>
                      <span class="usage-bar-detail">{{ info.cpuCores || '-' }} 核心</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 磁盘监控（35%） -->
            <div class="monitor-card monitor-card-disk">
              <div class="monitor-header">
                <span class="monitor-title">磁盘使用率</span>
                <span class="monitor-value" :class="getUsageClass(info.diskUsage)">{{ info.diskUsage || '-' }}</span>
              </div>
              <div ref="diskChartRef" class="chart-container"></div>
              <div class="usage-bar-card">
                <div class="usage-bar-track">
                  <div
                    class="usage-bar-fill disk"
                    :style="{ width: parseUsage(info.diskUsage) + '%' }"
                  ></div>
                  <div class="usage-bar-overlay">
                    <div class="usage-bar-left">
                      <span class="usage-bar-icon">DISK</span>
                      <span class="usage-bar-detail">{{ info.diskUsed || '-' }} / {{ info.diskTotal || '-' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '@/utils/request'

const props = defineProps<{
  modelValue: boolean
  serverId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const info = ref<Record<string, any>>({})
let updateTimer: number | null = null

// 图表实例
const cpuChartRef = ref<HTMLDivElement>()
const memChartRef = ref<HTMLDivElement>()
const diskChartRef = ref<HTMLDivElement>()
let cpuChart: echarts.ECharts | null = null
let memChart: echarts.ECharts | null = null
let diskChart: echarts.ECharts | null = null

// 历史数据（最多60个点，5分钟）
const MAX_DATA_POINTS = 60
const cpuHistory = ref<number[]>([])
const memHistory = ref<number[]>([])
const diskHistory = ref<number[]>([])
const timeHistory = ref<string[]>([])

const parseUsage = (usage: string) => {
  if (!usage || usage === '未知') return 0
  const val = parseFloat(usage)
  return isNaN(val) ? 0 : Math.min(100, Math.max(0, val))
}

const getUsageClass = (usage: string) => {
  const val = parseUsage(usage)
  if (val >= 90) return 'danger'
  if (val >= 70) return 'warning'
  return 'success'
}

// 初始化图表
const initCharts = () => {
  if (cpuChartRef.value) {
    cpuChart = echarts.init(cpuChartRef.value)
    cpuChart.setOption(getChartOption('CPU', '#409eff'))
  }
  if (memChartRef.value) {
    memChart = echarts.init(memChartRef.value)
    memChart.setOption(getChartOption('MEM', '#67c23a'))
  }
  if (diskChartRef.value) {
    diskChart = echarts.init(diskChartRef.value)
    diskChart.setOption(getChartOption('DISK', '#e6a23c'))
  }
}

// 获取图表配置
const getChartOption = (name: string, color: string) => {
  return {
    grid: {
      top: 5,
      bottom: 20,
      left: 40,
      right: 10
    },
    xAxis: {
      type: 'category',
      data: [],
      axisLine: {
        show: true,
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisTick: {
        show: true,
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        show: false  // 隐藏X轴标签
      }
    },
    yAxis: {
      type: 'value',
      max: 100,
      min: 0,
      splitNumber: 4,
      axisLine: {
        show: true,
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisTick: {
        show: true,
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        fontSize: 10,
        color: '#909399',
        formatter: '{value}%'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f2f5'
        }
      }
    },
    series: [{
      name: name,
      type: 'line',
      smooth: true,
      symbol: 'none',
      lineStyle: {
        width: 2,
        color: color
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: color + '40' },
          { offset: 1, color: color + '05' }
        ])
      },
      data: []
    }],
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const point = params[0]
        return `${point.axisValue}<br/>${point.seriesName}: ${point.value}%`
      }
    }
  }
}

// 更新图表数据
const updateCharts = () => {
  const now = new Date()
  const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`

  // 添加新数据
  timeHistory.value.push(timeStr)
  cpuHistory.value.push(parseUsage(info.value.cpuUsage))
  memHistory.value.push(parseUsage(info.value.memUsage))
  diskHistory.value.push(parseUsage(info.value.diskUsage))

  // 保持最多60个点
  if (timeHistory.value.length > MAX_DATA_POINTS) {
    timeHistory.value.shift()
    cpuHistory.value.shift()
    memHistory.value.shift()
    diskHistory.value.shift()
  }

  // 更新CPU图表
  if (cpuChart) {
    cpuChart.setOption({
      xAxis: { data: timeHistory.value },
      series: [{ data: cpuHistory.value }]
    })
  }

  // 更新内存图表
  if (memChart) {
    memChart.setOption({
      xAxis: { data: timeHistory.value },
      series: [{ data: memHistory.value }]
    })
  }

  // 更新磁盘图表
  if (diskChart) {
    diskChart.setOption({
      xAxis: { data: timeHistory.value },
      series: [{ data: diskHistory.value }]
    })
  }
}

// 加载完整信息
const loadFullInfo = async () => {
  loading.value = true
  info.value = {}
  // 清空历史数据
  cpuHistory.value = []
  memHistory.value = []
  diskHistory.value = []
  timeHistory.value = []

  try {
    const res = await request.get(`/ssh/ds/info/${props.serverId}`)
    info.value = res?.data?.data || res?.data || res || {}

    // 等待DOM更新后初始化图表
    await nextTick()
    initCharts()

    // 添加初始数据点
    updateCharts()
  } catch (e: any) {
    ElMessage.error('获取服务器信息失败: ' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 更新资源使用情况
const updateUsage = async () => {
  try {
    const res = await request.get(`/ssh/ds/usage/${props.serverId}`)
    const usage = res?.data?.data || res?.data || res || {}
    // 只更新 CPU、内存、磁盘相关信息
    info.value = {
      ...info.value,
      cpuUsage: usage.cpuUsage || info.value.cpuUsage,
      cpuCores: usage.cpuCores || info.value.cpuCores,
      memUsage: usage.memUsage || info.value.memUsage,
      memUsed: usage.memUsed || info.value.memUsed,
      memTotal: usage.memTotal || info.value.memTotal,
      diskUsage: usage.diskUsage || info.value.diskUsage,
      diskUsed: usage.diskUsed || info.value.diskUsed,
      diskTotal: usage.diskTotal || info.value.diskTotal
    }

    // 更新图表
    updateCharts()
  } catch (e) {
    console.error('更新资源使用情况失败', e)
  }
}

// 启动定时更新
const startAutoUpdate = () => {
  stopAutoUpdate()
  updateTimer = window.setInterval(() => {
    updateUsage()
  }, 5000)
}

// 停止定时更新
const stopAutoUpdate = () => {
  if (updateTimer !== null) {
    clearInterval(updateTimer)
    updateTimer = null
  }
}

// 销毁图表
const disposeCharts = () => {
  if (cpuChart) {
    cpuChart.dispose()
    cpuChart = null
  }
  if (memChart) {
    memChart.dispose()
    memChart = null
  }
  if (diskChart) {
    diskChart.dispose()
    diskChart = null
  }
}

// 弹窗打开时自动加载数据
watch(() => props.modelValue, async (val) => {
  if (val && props.serverId) {
    await loadFullInfo()
    startAutoUpdate()
  } else {
    stopAutoUpdate()
    disposeCharts()
  }
})

// 组件销毁时清理
onBeforeUnmount(() => {
  stopAutoUpdate()
  disposeCharts()
})
</script>

<style lang="less" scoped>
.server-info-dialog-wrapper {
  :deep(.el-dialog__body) {
    padding: 16px 20px;
  }
}

.server-info-container {
  padding: 4px 0;
}

.layout-wrapper {
  display: flex;
  gap: 20px;
  max-height: 650px;
}

.left-panel {
  width: 320px;
  flex-shrink: 0;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 第二行布局
.monitor-row {
  display: flex;
  gap: 16px;
}

.monitor-card-cpu {
  flex: 0 0 65%;
}

.monitor-card-disk {
  flex: 1;
}

// 左侧信息卡片
.info-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-card {
  background: #f8f9fb;
  border-radius: 6px;
  padding: 10px 10px;
  transition: background 0.15s;

  &:hover {
    background: #f0f2f5;
  }
}

.info-card-label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
  line-height: 1;
}

.info-card-value {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  word-break: break-all;

  &.mono {
    font-family: 'Cascadia Code', Menlo, Monaco, Consolas, monospace;
  }
}

// 右侧监控卡片
.monitor-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px 12px;
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.monitor-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.monitor-value {
  font-size: 14px;
  font-weight: 700;
  font-family: 'Cascadia Code', Menlo, Monaco, Consolas, monospace;

  &.success { color: #67c23a; }
  &.warning { color: #e6a23c; }
  &.danger { color: #f56c6c; }
}

.chart-container {
  width: 100%;
  height: 148px;
  margin-bottom: 10px;
}

.chart-container-large {
  height: 150px;
}

// 进度条
.usage-bar-card {
  margin-bottom: 0;
}

.usage-bar-track {
  position: relative;
  height: 32px;
  background: #f0f2f5;
  border-radius: 6px;
  overflow: hidden;
}

.usage-bar-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  border-radius: 6px;
  transition: width 0.6s ease;

  &.cpu {
    background: linear-gradient(135deg, #409eff, #79bbff);
  }

  &.mem {
    background: linear-gradient(135deg, #67c23a, #95d475);
  }

  &.disk {
    background: linear-gradient(135deg, #e6a23c, #eebe77);
  }
}

.usage-bar-overlay {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 14px;
}

.usage-bar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.usage-bar-icon {
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: rgba(0, 0, 0, 0.25);
  padding: 2px 8px;
  border-radius: 3px;
  letter-spacing: 0.5px;
}

.usage-bar-detail {
  font-size: 12px;
  color: #303133;
  font-family: 'Cascadia Code', Menlo, Monaco, Consolas, monospace;
}
</style>

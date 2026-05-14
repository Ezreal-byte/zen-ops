<template>
  <el-dialog v-model="visible" title="桶详情" width="480px" destroy-on-close>
    <el-descriptions :column="1" border size="small" v-loading="loading">
      <el-descriptions-item label="桶名称">{{ bucketDetail.name }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ bucketDetail.creationDate || '-' }}</el-descriptions-item>
      <el-descriptions-item label="区域">{{ bucketDetail.region || '-' }}</el-descriptions-item>
      <el-descriptions-item label="对象数量">{{ bucketDetail.objectCount !== undefined ? bucketDetail.objectCount : '-' }}</el-descriptions-item>
      <el-descriptions-item label="总大小">{{ bucketDetail.totalSize !== undefined ? formatSize(bucketDetail.totalSize) : '-' }}</el-descriptions-item>
      <el-descriptions-item label="存储类型">{{ bucketDetail.storageClass || '-' }}</el-descriptions-item>
      <el-descriptions-item label="访问权限">{{ bucketDetail.accessPolicy || '-' }}</el-descriptions-item>
      <el-descriptions-item label="版本控制">{{ bucketDetail.versioningStatus || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { request } from '@/utils/request'

const props = defineProps<{
  modelValue: boolean
  bucket: any
  dsId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const visible = ref(false)
const loading = ref(false)
const bucketDetail = ref<any>({})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.bucket && props.dsId) {
    fetchBucketDetail()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 获取桶详细信息
const fetchBucketDetail = async () => {
  if (!props.bucket || !props.dsId) return
  try {
    loading.value = true
    const data = await request.get('/fso/bucket/detail', {
      params: {
        pkFsoDs: props.dsId,
        bucketName: props.bucket.name
      }
    })
    bucketDetail.value = data || {}
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

// 格式化文件大小
const formatSize = (bytes: number) => {
  if (!bytes && bytes !== 0) return '-'
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + units[i]
}
</script>

<style lang="less" scoped>
</style>

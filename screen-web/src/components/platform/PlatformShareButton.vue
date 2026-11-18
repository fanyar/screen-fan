<template>
  <el-button type="primary" plain :loading="sharing" @click="share">
    <el-icon><Share/></el-icon>
    <span>复制链接</span>
  </el-button>
</template>

<script setup>
import {ref} from 'vue'
import {ElMessage} from 'element-plus'

const props = defineProps({
  title: {
    type: String,
    default: ''
  }
})

const sharing = ref(false)

const share = async () => {
  if (sharing.value) return
  sharing.value = true
  try {
    await navigator.clipboard.writeText(window.location.href)
    ElMessage.success('链接已复制')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制地址栏链接')
  } finally {
    sharing.value = false
  }
}
</script>

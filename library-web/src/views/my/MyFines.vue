<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMyFines } from '@/api/fines'

const loading = ref(false)
const fines = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10
})

const fetchFines = async () => {
  loading.value = true
  try {
    const res = await getMyFines(query)
    fines.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFines()
})
</script>

<template>
  <div class="my-fines">
    <h2>我的罚款</h2>
    <el-table v-loading="loading" :data="fines" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="borrowRecordId" label="借阅记录 ID" />
      <el-table-column prop="amount" label="金额" />
      <el-table-column prop="reason" label="原因" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column prop="paidAt" label="缴费时间" />
    </el-table>
  </div>
</template>

<style scoped>
.my-fines {
  padding: 20px;
}
</style>

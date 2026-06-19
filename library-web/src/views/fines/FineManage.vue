<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getFines, payFine } from '@/api/fines'
import { ElMessage, ElMessageBox } from 'element-plus'

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
    const res = await getFines(query)
    fines.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handlePay = (row: any) => {
  ElMessageBox.prompt('请确认缴费金额', '缴纳罚款', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: row.amount.toString(),
    inputPattern: /^\d+(\.\d{1,2})?$/,
    inputErrorMessage: '金额格式不正确'
  }).then(async ({ value }) => {
    try {
      await payFine(row.id, { amount: parseFloat(value) })
      ElMessage.success('缴费成功')
      fetchFines()
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchFines()
})
</script>

<template>
  <div class="fine-manage">
    <el-table v-loading="loading" :data="fines" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="readerId" label="读者 ID" />
      <el-table-column prop="amount" label="金额" />
      <el-table-column prop="reason" label="原因" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button 
            v-if="row.status === 'UNPAID'" 
            link type="primary" 
            @click="handlePay(row)"
          >缴费</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.fine-manage {
  padding: 20px;
}
</style>

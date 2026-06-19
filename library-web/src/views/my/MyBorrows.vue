<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMyBorrows, renewBook } from '@/api/borrows'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10
})

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getMyBorrows(query)
    records.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleRenew = async (id: number) => {
  try {
    await renewBook(id)
    ElMessage.success('续借成功')
    fetchRecords()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<template>
  <div class="my-borrows">
    <h2>我的借阅</h2>
    <el-table v-loading="loading" :data="records" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="bookCopyId" label="复本 ID" />
      <el-table-column prop="borrowTime" label="借阅时间" />
      <el-table-column prop="dueDate" label="应还日期" />
      <el-table-column prop="renewCount" label="续借次数" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button 
            v-if="row.status === 'BORROWING'" 
            link type="primary" 
            @click="handleRenew(row.id)"
          >续借</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.my-borrows {
  padding: 20px;
}
</style>

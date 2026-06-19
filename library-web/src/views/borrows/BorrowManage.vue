<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { borrowBook, returnBook, getBorrows } from '@/api/borrows'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10
})

const borrowForm = reactive({
  readerNo: '',
  barcode: ''
})

const returnBarcode = ref('')

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getBorrows(query)
    records.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleBorrow = async () => {
  if (!borrowForm.readerNo || !borrowForm.barcode) {
    ElMessage.warning('请输入读者编号和条码')
    return
  }
  try {
    await borrowBook(borrowForm)
    ElMessage.success('借阅成功')
    borrowForm.barcode = ''
    fetchRecords()
  } catch (e) {
    console.error(e)
  }
}

const handleReturn = async () => {
  if (!returnBarcode.value) {
    ElMessage.warning('请输入条码')
    return
  }
  try {
    await returnBook(returnBarcode.value)
    ElMessage.success('归还成功')
    returnBarcode.value = ''
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
  <div class="borrow-manage">
    <el-card class="action-card">
      <template #header>借书 / 还书</template>
      <el-row :gutter="40">
        <el-col :span="12">
          <h3>办理借书</h3>
          <el-form :model="borrowForm" inline>
            <el-form-item label="读者编号">
              <el-input v-model="borrowForm.readerNo" />
            </el-form-item>
            <el-form-item label="条码">
              <el-input v-model="borrowForm.barcode" @keyup.enter="handleBorrow" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleBorrow">借书</el-button>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <h3>办理还书</h3>
          <el-form inline>
            <el-form-item label="条码">
              <el-input v-model="returnBarcode" @keyup.enter="handleReturn" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="handleReturn">还书</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="list-card" style="margin-top: 20px">
      <template #header>最近借阅记录</template>
      <el-table v-loading="loading" :data="records" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="readerId" label="读者 ID" />
        <el-table-column prop="bookCopyId" label="复本 ID" />
        <el-table-column prop="borrowTime" label="借阅时间" />
        <el-table-column prop="dueDate" label="应还日期" />
        <el-table-column prop="status" label="状态" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.borrow-manage {
  padding: 20px;
}
</style>

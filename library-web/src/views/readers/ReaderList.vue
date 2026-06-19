<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getReaders, createReader, updateReader, updateReaderStatus } from '@/api/readers'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const readers = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10
})

const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  userAccountId: undefined,
  readerNo: '',
  name: '',
  phone: '',
  email: '',
  registerDate: ''
})

const rules = {
  userAccountId: [{ required: true, message: '必填', trigger: 'blur' }],
  readerNo: [{ required: true, message: '必填', trigger: 'blur' }],
  name: [{ required: true, message: '必填', trigger: 'blur' }],
  registerDate: [{ required: true, message: '必填', trigger: 'change' }]
}

const editRules = {
  name: [{ required: true, message: '必填', trigger: 'blur' }]
}

const fetchReaders = async () => {
  loading.value = true
  try {
    const res = await getReaders(query)
    readers.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.id = undefined
  form.userAccountId = undefined
  form.readerNo = ''
  form.name = ''
  form.phone = ''
  form.email = ''
  form.registerDate = new Date().toISOString().split('T')[0]
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.readerNo = row.readerNo
  dialogVisible.value = true
}

const handleStatusChange = async (row: any, newStatus: string) => {
  try {
    await updateReaderStatus(row.id, newStatus)
    ElMessage.success('状态已更新')
    fetchReaders()
  } catch (e) {
    console.error(e)
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateReader(form.id!, { name: form.name, phone: form.phone, email: form.email })
          ElMessage.success('更新成功')
        } else {
          await createReader(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchReaders()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

onMounted(() => {
  fetchReaders()
})
</script>

<template>
  <div class="reader-list">
    <div class="toolbar">
      <el-button type="success" @click="handleAdd">新增读者</el-button>
    </div>

    <el-table v-loading="loading" :data="readers" style="width: 100%; margin-top: 16px">
      <el-table-column prop="readerNo" label="读者编号" width="150" />
      <el-table-column prop="name" label="姓名" width="150" />
      <el-table-column prop="phone" label="电话" width="150" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="registerDate" label="注册日期" width="150" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button 
            v-if="row.status === 'ACTIVE'" 
            link type="danger" 
            @click="handleStatusChange(row, 'DISABLED')"
          >禁用</el-button>
          <el-button 
            v-if="row.status === 'DISABLED'" 
            link type="success" 
            @click="handleStatusChange(row, 'ACTIVE')"
          >启用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      style="margin-top: 16px; justify-content: flex-end"
      @current-change="fetchReaders"
    />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑读者' : '新增读者'" width="500px">
      <el-form ref="formRef" :model="form" :rules="isEdit ? editRules : rules" label-width="120px">
        <el-form-item v-if="!isEdit" label="账号 ID" prop="userAccountId">
          <el-input-number v-model="form.userAccountId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="读者编号" prop="readerNo">
          <el-input v-model="form.readerNo" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="注册日期" prop="registerDate">
          <el-date-picker v-model="form.registerDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: flex-end;
}
</style>

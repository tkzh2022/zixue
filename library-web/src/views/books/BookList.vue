<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getBooks, createBook, updateBook, deleteBook } from '@/api/books'
import { ElMessage, ElMessageBox } from 'element-plus'
import CopyManage from './CopyManage.vue'

const loading = ref(false)
const books = ref([])
const total = ref(0)
const query = reactive({
  keyword: '',
  page: 1,
  size: 10
})

const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const form = reactive({
  id: undefined,
  isbn: '',
  title: '',
  publisher: '',
  publishYear: undefined,
  location: '',
  summary: '',
  authorNames: [] as string[],
  categoryCodes: [] as string[]
})

const copyDialogVisible = ref(false)
const currentBookId = ref<number>()

const rules = {
  isbn: [{ required: true, message: '请输入 ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }]
}

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await getBooks(query)
    books.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchBooks()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = undefined
  form.isbn = ''
  form.title = ''
  form.publisher = ''
  form.publishYear = undefined
  form.location = ''
  form.summary = ''
  form.authorNames = []
  form.categoryCodes = []
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleManageCopies = (row: any) => {
  currentBookId.value = row.id
  copyDialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除图书「${row.title}」？如果有未归还的复本将无法删除。`, '删除确认', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBook(row.id)
      ElMessage.success('删除成功')
      fetchBooks()
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateBook(form.id!, form)
          ElMessage.success('更新成功')
        } else {
          await createBook(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchBooks()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

onMounted(() => {
  fetchBooks()
})
</script>

<template>
  <div class="book-list">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索 ISBN 或书名"
        style="width: 300px; margin-right: 16px"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增图书</el-button>
    </div>

    <el-table v-loading="loading" :data="books" style="width: 100%; margin-top: 16px">
      <el-table-column prop="isbn" label="ISBN" width="150" />
      <el-table-column prop="title" label="书名" min-width="200" />
      <el-table-column prop="publisher" label="出版社" width="150" />
      <el-table-column prop="totalCopies" label="总复本数" width="120" />
      <el-table-column prop="availableCopies" label="可借数量" width="120" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleManageCopies(row)">复本管理</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      style="margin-top: 16px; justify-content: flex-end"
      @current-change="fetchBooks"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑图书' : '新增图书'"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" />
        </el-form-item>
        <el-form-item label="出版年份" prop="publishYear">
          <el-input-number v-model="form.publishYear" :min="1000" :max="2100" />
        </el-form-item>
        <el-form-item label="馆藏位置" prop="location">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="简介" prop="summary">
          <el-input v-model="form.summary" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <CopyManage
      v-if="copyDialogVisible"
      v-model:visible="copyDialogVisible"
      :book-id="currentBookId"
      @refresh="fetchBooks"
    />
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}
</style>

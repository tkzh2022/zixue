<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { addCopy, deleteCopy } from '@/api/books'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const props = defineProps<{
  visible: boolean
  bookId?: number
}>()

const emit = defineEmits(['update:visible', 'refresh'])

const dialogVisible = ref(props.visible)
const loading = ref(false)
const copies = ref([])
const newBarcode = ref('')

watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val && props.bookId) {
    fetchCopies()
  }
})

onMounted(() => {
  if (props.visible && props.bookId) {
    fetchCopies()
  }
})

watch(dialogVisible, (val) => {
  emit('update:visible', val)
})

const fetchCopies = async () => {
  if (!props.bookId) return
  loading.value = true
  try {
    const res = await request.get(`/books/${props.bookId}/copies`)
    copies.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAddCopy = async () => {
  if (!newBarcode.value) {
    ElMessage.warning('请输入条码')
    return
  }
  try {
    await addCopy(props.bookId!, { barcode: newBarcode.value })
    ElMessage.success('复本添加成功')
    newBarcode.value = ''
    fetchCopies()
    emit('refresh')
  } catch (e) {
    console.error(e)
  }
}

const handleDeleteCopy = async (copyId: number) => {
  try {
    await deleteCopy(copyId)
    ElMessage.success('复本已删除')
    fetchCopies()
    emit('refresh')
  } catch (e) {
    console.error(e)
  }
}
</script>

<template>
  <el-dialog v-model="dialogVisible" title="复本管理" width="600px">
    <div style="margin-bottom: 16px; display: flex; gap: 10px;">
      <el-input v-model="newBarcode" placeholder="新条码" style="width: 200px" />
      <el-button type="primary" @click="handleAddCopy">添加复本</el-button>
    </div>

    <el-table v-loading="loading" :data="copies" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="barcode" label="条码" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDeleteCopy(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

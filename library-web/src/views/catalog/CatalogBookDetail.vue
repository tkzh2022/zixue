<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCatalogBook } from '@/api/catalog'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const book = ref<any>(null)

const fetchBookDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  book.value = null
  try {
    const res = await getCatalogBook(id)
    book.value = res
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, (newId) => {
  if (newId) fetchBookDetail()
})

onMounted(() => {
  fetchBookDetail()
})
</script>

<template>
  <div class="book-detail" v-loading="loading">
    <div class="header">
      <el-button icon="Back" @click="router.push('/catalog')">返回搜索</el-button>
    </div>

    <div v-if="book" class="content">
      <el-row :gutter="40">
        <el-col :span="8" :xs="24">
          <div class="cover-placeholder">
            <el-icon :size="80" color="#c0c4cc"><Reading /></el-icon>
          </div>
        </el-col>
        <el-col :span="16" :xs="24">
          <h1 class="title">{{ book.title }}</h1>
          
          <el-descriptions :column="1" border class="info-table">
            <el-descriptions-item label="作者">
              {{ book.authorNames?.join(', ') || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
            <el-descriptions-item label="出版社">{{ book.publisher || '-' }}</el-descriptions-item>
            <el-descriptions-item label="出版年份">{{ book.publishYear || '-' }}</el-descriptions-item>
            <el-descriptions-item label="分类">
              <el-tag v-for="cat in book.categoryCodes" :key="cat" size="small" style="margin-right: 5px">
                {{ cat }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="馆藏位置">{{ book.location || '-' }}</el-descriptions-item>
            <el-descriptions-item label="可借状态">
              <el-tag :type="book.availableCopies > 0 ? 'success' : 'info'">
                {{ book.availableCopies }} / {{ book.totalCopies }} 可借
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <div class="summary-section">
            <h3>简介</h3>
            <p class="summary-text">{{ book.summary || '暂无简介' }}</p>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-empty v-if="!loading && !book" description="未找到该图书" />
  </div>
</template>

<style scoped>
.book-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  margin-bottom: 30px;
}

.cover-placeholder {
  width: 100%;
  aspect-ratio: 2/3;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin-bottom: 20px;
}

.title {
  margin-top: 0;
  margin-bottom: 24px;
  font-size: 28px;
  color: #303133;
}

.info-table {
  margin-bottom: 30px;
}

.summary-section h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 12px;
}

.summary-text {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>

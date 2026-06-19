<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { searchCatalog } from '@/api/catalog'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const books = ref([])
const total = ref(0)
const query = reactive({
  keyword: '',
  page: 1,
  size: 12
})

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await searchCatalog(query)
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

const goToDetail = (id: number) => {
  router.push(`/catalog/books/${id}`)
}

onMounted(() => {
  fetchBooks()
})
</script>

<template>
  <div class="catalog-search">
    <div class="search-box">
      <el-input
        v-model="query.keyword"
        placeholder="按书名、作者或 ISBN 搜索..."
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </div>

    <div v-loading="loading" class="results">
      <el-row :gutter="20">
        <el-col v-for="book in books" :key="book.id" :span="6" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="book-card" shadow="hover" @click="goToDetail(book.id)">
            <div class="book-cover">
              <el-icon :size="50" color="#c0c4cc"><Reading /></el-icon>
            </div>
            <div class="book-info">
              <h3 class="title" :title="book.title">{{ book.title }}</h3>
              <p class="author">{{ book.authorNames?.join(', ') || '未知作者' }}</p>
              <div class="status">
                <el-tag :type="book.availableCopies > 0 ? 'success' : 'info'" size="small">
                  {{ book.availableCopies > 0 ? '可借' : '不可借' }}
                </el-tag>
                <span class="copies">{{ book.availableCopies }} / {{ book.totalCopies }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && books.length === 0" description="未找到相关图书" />
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchBooks"
      />
    </div>
  </div>
</template>

<style scoped>
.catalog-search {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-box {
  display: flex;
  justify-content: center;
  margin-bottom: 40px;
}

.search-input {
  width: 100%;
  max-width: 600px;
}

.book-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.book-card:hover {
  transform: translateY(-5px);
}

.book-cover {
  height: 200px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: -20px -20px 15px -20px;
}

.book-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title {
  margin: 0;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.author {
  margin: 0;
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.copies {
  font-size: 12px;
  color: #909399;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>

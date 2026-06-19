<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAdminDashboard, getReaderDashboard } from '@/api/dashboard'

const userStore = useUserStore()
const loading = ref(false)
const stats = ref<any>({})

const fetchStats = async () => {
  loading.value = true
  try {
    if (userStore.role === 'LIBRARIAN') {
      const res = await getAdminDashboard()
      // axios 拦截器已经处理了 res.data 的解包，所以这里直接用 res
      stats.value = res || {}
    } else if (userStore.role === 'READER') {
      const res = await getReaderDashboard()
      stats.value = res || {}
    }
  } catch (e) {
    console.error(e)
    stats.value = {}
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <h2>{{ $t('menu.dashboard') }}</h2>
    
    <!-- Admin Dashboard -->
    <el-row :gutter="20" v-if="userStore.role === 'LIBRARIAN'">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="图书总数" :value="stats?.totalBooks || 0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="读者总数" :value="stats?.totalReaders || 0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="在借数量" :value="stats?.activeBorrows || 0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="未缴罚款" :value="stats?.unpaidFines || 0" :precision="2" prefix="¥" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Reader Dashboard -->
    <el-row :gutter="20" v-if="userStore.role === 'READER'">
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="在借数量" :value="stats?.activeBorrows || 0" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="逾期图书" :value="stats?.overdueBorrows || 0" value-style="color: red" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="未缴罚款" :value="stats?.unpaidFines || 0" :precision="2" prefix="¥" value-style="color: red" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 20px;
}
.el-card {
  margin-bottom: 20px;
}
</style>

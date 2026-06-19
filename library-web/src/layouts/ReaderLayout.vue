<template>
  <el-container class="reader-layout">
    <el-header class="header">
      <div class="logo">图书馆读者中心</div>
      <div class="actions">
        <LangSwitch />
        <el-button link @click="handleLogout">{{ $t('common.logout') }}</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu router :default-active="activeMenu">
          <el-menu-item index="/reader/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>{{ $t('menu.dashboard') }}</span>
          </el-menu-item>
          <el-menu-item index="/catalog">
            <el-icon><Search /></el-icon>
            <span>{{ $t('menu.catalog') }}</span>
          </el-menu-item>
          <el-menu-item index="/reader/borrows">
            <el-icon><Tickets /></el-icon>
            <span>{{ $t('menu.myBorrowings') }}</span>
          </el-menu-item>
          <el-menu-item index="/reader/fines">
            <el-icon><Wallet /></el-icon>
            <span>{{ $t('menu.myFines') }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import LangSwitch from '@/components/global/LangSwitch.vue'

const route = useRoute()
const userStore = useUserStore()
const router = useRouter()

const activeMenu = computed(() => {
  if (route.path.startsWith('/catalog')) return '/catalog'
  if (route.path === '/reader/dashboard') return '/reader/dashboard'
  return route.path
})

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.reader-layout {
  height: 100vh;
}
.header {
  background-color: #67c23a;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.logo {
  font-size: 20px;
  font-weight: bold;
}
.actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>

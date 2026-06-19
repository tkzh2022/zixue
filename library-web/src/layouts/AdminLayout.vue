<template>
  <el-container class="admin-layout">
    <el-header class="header">
      <div class="logo">图书馆管理系统</div>
      <div class="actions">
        <LangSwitch />
        <el-button link @click="handleLogout">{{ $t('common.logout') }}</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>{{ $t('menu.dashboard') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/books">
            <el-icon><Reading /></el-icon>
            <span>{{ $t('menu.books') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/readers">
            <el-icon><User /></el-icon>
            <span>{{ $t('menu.readers') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/borrows">
            <el-icon><Tickets /></el-icon>
            <span>{{ $t('menu.borrowings') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/fines">
            <el-icon><Wallet /></el-icon>
            <span>{{ $t('menu.fines') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/rules">
            <el-icon><Setting /></el-icon>
            <span>{{ $t('menu.rules') }}</span>
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
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import LangSwitch from '@/components/global/LangSwitch.vue'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.header {
  background-color: #409eff;
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

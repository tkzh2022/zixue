<template>
  <el-dropdown @command="handleCommand">
    <span class="el-dropdown-link lang-switch">
      {{ currentLangLabel }}
      <el-icon class="el-icon--right"><arrow-down /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh" :disabled="appStore.language === 'zh'">中文</el-dropdown-item>
        <el-dropdown-item command="en" :disabled="appStore.language === 'en'">English</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const currentLangLabel = computed(() => {
  return appStore.language === 'zh' ? '中文' : 'English'
})

const handleCommand = (command: string) => {
  appStore.setLanguage(command)
}
</script>

<style scoped>
.lang-switch {
  cursor: pointer;
  color: inherit;
  display: flex;
  align-items: center;
}
</style>

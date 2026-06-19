<template>
  <slot v-if="!hasError"></slot>
  <div v-else class="error-boundary">
    <h3>Something went wrong.</h3>
    <p>{{ error?.message }}</p>
    <el-button @click="resetError">Try Again</el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'

const hasError = ref(false)
const error = ref<Error | null>(null)

onErrorCaptured((err: any) => {
  hasError.value = true
  error.value = err
  console.error('ErrorBoundary caught:', err)
  return false // prevent propagation
})

const resetError = () => {
  hasError.value = false
  error.value = null
}
</script>

<style scoped>
.error-boundary {
  padding: 20px;
  border: 1px solid #f56c6c;
  background-color: #fef0f0;
  border-radius: 4px;
  margin: 20px;
}
</style>

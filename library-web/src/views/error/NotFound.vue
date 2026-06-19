<template>
  <div class="error-page">
    <h2>404 页面未找到</h2>
    <p>您访问的页面不存在。</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.error-page {
  text-align: center;
  margin-top: 100px;
}
</style>

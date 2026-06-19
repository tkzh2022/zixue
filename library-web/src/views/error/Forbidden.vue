<template>
  <div class="error-page">
    <h2>403 禁止访问</h2>
    <p>您没有权限访问此页面。</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.error-page {
  text-align: center;
  margin-top: 100px;
}
</style>

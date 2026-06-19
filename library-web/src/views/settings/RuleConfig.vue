<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRules, updateRule } from '@/api/rules'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const rules = ref<any[]>([])

const fetchRules = async () => {
  loading.value = true
  try {
    const res = await getRules()
    rules.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSave = async (rule: any) => {
  try {
    await updateRule(rule.id, rule)
    ElMessage.success('规则更新成功')
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchRules()
})
</script>

<template>
  <div class="rule-config" v-loading="loading">
    <el-card v-for="rule in rules" :key="rule.id" class="rule-card">
      <template #header>
        <div class="card-header">
          <span>读者类型：{{ rule.readerType }}</span>
          <el-button type="primary" @click="handleSave(rule)">保存</el-button>
        </div>
      </template>
      <el-form :model="rule" label-width="150px" inline>
        <el-form-item label="最大借阅天数">
          <el-input-number v-model="rule.maxBorrowDays" :min="1" />
        </el-form-item>
        <el-form-item label="最大借阅数量">
          <el-input-number v-model="rule.maxBorrowCount" :min="1" />
        </el-form-item>
        <el-form-item label="最大续借次数">
          <el-input-number v-model="rule.maxRenewCount" :min="0" />
        </el-form-item>
        <el-form-item label="每日罚款金额">
          <el-input-number v-model="rule.finePerDay" :min="0" :precision="2" :step="0.1" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.rule-config {
  padding: 20px;
}
.rule-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

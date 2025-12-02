<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1>智能体创作平台</h1>
        <p>搭建您的专属AI智能体</p>
      </div>

      <el-card class="login-card">
        <el-form :model="formData" :rules="rules" ref="formRef" @submit.prevent="onSubmit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="formData.username" placeholder="请输入用户名" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              @keyup.enter="onSubmit"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="onSubmit">
              登录
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button size="large" style="width: 100%" @click="goToRegister">注册账号</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'
import { ElMessage, FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      loading.value = true
      const data = await login(formData)

      if (!data || !data.token) {
        ElMessage.error('登录失败：未获取到token')
        return
      }

      userStore.login(data)
      ElMessage.success('登录成功')

      setTimeout(() => {
        const redirect = (route.query.redirect as string) || '/console/agents'
        router.replace(redirect)
      }, 500)
    } catch (error: any) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

function goToRegister() {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-header {
  text-align: center;
  color: white;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 12px;
}

.login-header p {
  font-size: 16px;
  opacity: 0.9;
}

.login-card {
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
</style>




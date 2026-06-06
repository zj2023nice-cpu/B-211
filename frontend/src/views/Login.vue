<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧展示区 -->
      <div class="login-hero">
        <div class="hero-content">
          <div class="logo-area">
            <el-icon :size="48" color="#fff"><School /></el-icon>
            <h1 class="system-title"> 成绩管理系统</h1>
          </div>
          <p class="slogan">专业 · 高效 · 智能的教学辅助平台</p>
          <ul class="feature-list">
            <li><el-icon><DataAnalysis /></el-icon> 多维度成绩数据分析</li>
            <li><el-icon><User /></el-icon> 角色化权限管理体系</li>
            <li><el-icon><Monitor /></el-icon> 响应式交互体验设计</li>
            <li><el-icon><Lock /></el-icon> 安全可靠的数据保障</li>
          </ul>
        </div>
        <div class="hero-footer">
          <p>&copy; 2026 Grade Management System. All Rights Reserved.</p>
        </div>
        <div class="hero-decoration"></div>
      </div>

      <!-- 右侧表单区 -->
      <div class="login-form-section">
        <div class="form-wrapper">
          <div class="form-header">
            <h2>欢迎登录</h2>
            <p>请输入您的账号信息以继续</p>
          </div>
          <el-form :model="form" :rules="rules" ref="formRef" size="large" class="login-form">
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="用户名" 
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="密码" 
                :prefix-icon="Lock" 
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin">
                立即登录
              </el-button>
            </el-form-item>
          
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { User, Lock, School, DataAnalysis, Monitor } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/auth/login', form)
        userStore.setUser(res)
        ElMessage.success({
          message: '登录成功，欢迎回来',
          type: 'success',
          plain: true,
        })
        router.push('/')
      } catch (error) {
        // Error handled in request interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  background-color: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.login-wrapper {
  width: 1000px;
  height: 600px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
  position: relative;
}

/* 左侧展示区 */
.login-hero {
  flex: 1;
  background: var(--gradient-primary);
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center; /* Center content vertically */
  position: relative;
  color: #fff;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center; /* Center text */
  margin-top: -40px; /* Slight adjustment to visual center */
}

.logo-area {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.system-title {
  font-size: 32px;
  font-weight: 700;
  margin-top: 15px;
  letter-spacing: 2px;
  font-family: 'Montserrat', sans-serif;
}

.slogan {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 40px;
  font-weight: 300;
  font-family: 'Roboto', sans-serif;
}

.feature-list {
  list-style: none;
  padding: 0;
  text-align: left;
  display: inline-block; /* Allow centering of the list itself */
  margin: 0 auto;
}

.feature-list li {
  font-size: 16px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  opacity: 0.85;
  transition: all 0.3s;
}

.feature-list li:hover {
  opacity: 1;
  transform: translateX(5px);
}

.feature-list li .el-icon {
  margin-right: 12px;
  font-size: 20px;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 50%;
}

.hero-footer {
  position: absolute;
  bottom: 30px;
  left: 0;
  width: 100%;
  text-align: center;
  opacity: 0.6;
  font-size: 12px;
  z-index: 2;
}

.hero-decoration {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23ffffff' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
  animation: bg-move 60s linear infinite;
}

@keyframes bg-move {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 右侧表单区 */
.login-form-section {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.form-wrapper {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: 40px;
  text-align: left;
}

.form-header h2 {
  font-size: 28px;
  color: var(--text-primary);
  margin-bottom: 10px;
  font-weight: 600;
  font-family: 'Montserrat', sans-serif;
}

.form-header p {
  color: var(--text-secondary);
  font-size: 14px;
  font-family: 'Roboto', sans-serif;
}

.login-form :deep(.el-input__wrapper) {
  padding: 10px 15px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
  transform: scale(1.02);
  z-index: 10;
}

.login-button {
  width: 100%;
  padding: 22px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  margin-top: 20px;
  transition: all 0.3s;
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(79, 70, 229, 0.3);
  background-color: var(--primary-light);
  border-color: var(--primary-light);
}

@media (max-width: 768px) {
  .login-wrapper {
    width: 90%;
    height: auto;
    flex-direction: column;
  }
  
  .login-hero {
    padding: 40px 20px;
    min-height: 200px;
  }
  
  .hero-footer {
    position: relative;
    margin-top: 20px;
    bottom: auto;
  }
  
  .login-form-section {
    padding: 40px 20px;
  }
}
</style>

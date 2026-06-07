<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="box-card user-info-card">
          <template #header>
            <div class="clearfix">
              <span>个人信息</span>
            </div>
          </template>
          <div class="user-profile">
            <div class="avatar-wrapper">
              <el-avatar :size="100" class="user-avatar">{{ userStore.user?.name?.charAt(0) }}</el-avatar>
            </div>
            <div class="user-name">{{ userStore.user?.name }}</div>
            <div class="user-role">{{ roleName }}</div>
          </div>
          <div class="user-details">
            <p><el-icon><User /></el-icon> 用户名: {{ userStore.user?.username }}</p>
            <p><el-icon><Phone /></el-icon> 联系方式: {{ userStore.user?.contact || '未填写' }}</p>
            <p v-if="userStore.user?.className"><el-icon><School /></el-icon> 班级: {{ userStore.user?.className }}</p>
          </div>
          <div class="quick-actions">
            <el-button type="primary" @click="goToMyLogs" :icon="Document">查看我的操作记录</el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">
              <span>资料修改</span>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本资料" name="info">
              <el-form :model="infoForm" :rules="infoRules" ref="infoFormRef" label-width="100px">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="infoForm.name" />
                </el-form-item>
                <el-form-item label="联系方式" prop="contact">
                  <el-input v-model="infoForm.contact" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleInfoSubmit" :loading="loading">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="password">
                  <el-input v-model="pwdForm.password" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handlePwdSubmit" :loading="loading">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { User, Phone, School, Document } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const activeTab = ref('info')
const loading = ref(false)
const infoFormRef = ref(null)
const pwdFormRef = ref(null)

const infoForm = reactive({
  name: '',
  contact: ''
})

const pwdForm = reactive({
  oldPassword: '',
  password: '',
  confirmPassword: ''
})

const infoRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== pwdForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

const roleName = computed(() => {
  const map = {
    'ADMIN': '管理员',
    'TEACHER': '教师',
    'HEAD_TEACHER': '班主任',
    'STUDENT': '学生'
  }
  return map[userStore.role] || userStore.role
})

onMounted(() => {
  infoForm.name = userStore.user?.name || ''
  infoForm.contact = userStore.user?.contact || ''
})

const handleInfoSubmit = async () => {
  if (!infoFormRef.value) return
  await infoFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.put(`/users/${userStore.user.id}`, {
          name: infoForm.name,
          contact: infoForm.contact
        })
        userStore.updateUser({
          name: res.name,
          contact: res.contact
        })
        ElMessage.success('基本资料修改成功')
      } finally {
        loading.value = false
      }
    }
  })
}

const handlePwdSubmit = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await request.put(`/users/${userStore.user.id}/change-password`, {
          oldPassword: pwdForm.oldPassword,
          newPassword: pwdForm.password
        })
        ElMessage.success('密码修改成功')
        pwdForm.oldPassword = ''
        pwdForm.password = ''
        pwdForm.confirmPassword = ''
      } catch (error) {
      } finally {
        loading.value = false
      }
    }
  })
}

const goToMyLogs = () => {
  router.push('/my-operation-logs')
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.user-info-card {
  text-align: center;
}
.user-profile {
  margin-bottom: 20px;
}
.user-avatar {
  background-color: var(--primary-color);
  font-size: 32px;
}
.user-name {
  margin-top: 15px;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  font-family: 'Montserrat', sans-serif;
}
.user-role {
  margin-top: 5px;
  color: var(--text-light);
}
.user-details {
  text-align: left;
  padding: 0 20px;
}
.user-details p {
  margin: 10px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
}

.quick-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
  text-align: center;
}
</style>

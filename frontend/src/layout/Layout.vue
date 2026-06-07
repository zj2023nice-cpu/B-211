<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="app-sidebar">
      <div class="logo" :class="{ 'collapsed': isCollapse }">
        <el-icon class="logo-icon"><School /></el-icon>
        <span class="logo-text" v-show="!isCollapse">成绩管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        class="el-menu-vertical"
        :collapse="isCollapse"
        background-color="#111827"
        text-color="#9CA3AF"
        active-text-color="#818CF8"
        :collapse-transition="false"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        
        <el-sub-menu index="/system" v-if="userStore.role === 'ADMIN'">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/courses">
            <el-icon><Reading /></el-icon>
            <template #title>课程管理</template>
          </el-menu-item>
          <el-menu-item index="/audit-logs">
            <el-icon><Document /></el-icon>
            <template #title>审计日志</template>
          </el-menu-item>
          <el-menu-item index="/announcements">
            <el-icon><Bell /></el-icon>
            <template #title>公告管理</template>
          </el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/grades-manage" v-if="['ADMIN', 'TEACHER', 'HEAD_TEACHER'].includes(userStore.role)">
          <el-icon><Edit /></el-icon>
          <template #title>成绩录入</template>
        </el-menu-item>
        
        <el-menu-item index="/grades-query">
          <el-icon><DataLine /></el-icon>
          <template #title>成绩查询</template>
        </el-menu-item>

        <el-menu-item index="/class-ranking">
          <el-icon><Trophy /></el-icon>
          <template #title>班级排行榜</template>
        </el-menu-item>

        <el-menu-item index="/class-profile" v-if="['ADMIN', 'TEACHER', 'HEAD_TEACHER'].includes(userStore.role)">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>班级画像</template>
        </el-menu-item>

        <el-menu-item index="/grade-warnings" v-if="['ADMIN', 'TEACHER', 'HEAD_TEACHER'].includes(userStore.role)">
          <el-icon><Warning /></el-icon>
          <template #title>成绩预警</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-icon class="fold-btn" @click="toggleCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <el-avatar :size="32" :src="userAvatar" class="header-avatar">{{ userStore.user?.name?.charAt(0) }}</el-avatar>
                <span class="username">{{ userStore.user?.name }}</span>
                <el-tag size="small" effect="plain" class="role-tag">{{ roleName }}</el-tag>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="my-logs">我的操作记录</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Expand, Fold, School, Odometer, Setting, User, Reading, Edit, DataLine, ArrowDown, Document, Warning, Bell, Trophy, DataAnalysis } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// Auto collapse on small screens
const checkScreenSize = () => {
  if (window.innerWidth < 768) {
    isCollapse.value = true
  } else {
    isCollapse.value = false
  }
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

const roleName = computed(() => {
  const map = {
    'ADMIN': '管理员',
    'TEACHER': '教师',
    'HEAD_TEACHER': '班主任',
    'STUDENT': '学生'
  }
  return map[userStore.user?.role] || userStore.user?.role
})

const userAvatar = computed(() => {
  return '' // Return URL if available
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    await userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'my-logs') {
    router.push('/my-operation-logs')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background-color: var(--bg-color);
}

/* Sidebar Styles */
.app-sidebar {
  background-color: #111827;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
  z-index: 10;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 64px;
  line-height: 64px;
  background-color: #111827;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  overflow: hidden;
  transition: all 0.3s;
  border-bottom: 1px solid #1F2937;
}

.logo.collapsed {
  padding: 0;
  justify-content: center;
}

.logo-icon {
  font-size: 24px;
  color: var(--primary-light);
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  font-family: 'Montserrat', sans-serif;
  color: #F9FAFB;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

/* Header Styles */
.app-header {
  background-color: var(--surface-color);
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--shadow-sm);
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.fold-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: color 0.3s;
}

.fold-btn:hover {
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.el-dropdown-link:hover {
  background: rgba(0, 0, 0, 0.025);
}

.header-avatar {
  background: var(--gradient-primary);
  color: white;
  font-weight: 600;
}

.username {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.role-tag {
  margin-left: 0;
}

/* Main Content Styles */
.el-main {
  padding: 24px;
  background-color: var(--bg-color);
  overflow-y: auto;
}

/* Transitions */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s cubic-bezier(0.55, 0, 0.1, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

@media (max-width: 768px) {
  .app-sidebar {
    position: absolute;
    height: 100%;
  }
  .app-sidebar:not([style*="width: 240px"]) {
    width: 0 !important; /* Hide completely when collapsed on mobile if preferred, or keep 64px icon bar */
  }
}
</style>

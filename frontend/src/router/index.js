import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../layout/Layout.vue'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('../views/UserList.vue'),
          meta: { title: '用户管理', roles: ['ADMIN'] }
        },
        {
          path: 'courses',
          name: 'Courses',
          component: () => import('../views/CourseList.vue'),
          meta: { title: '课程管理', roles: ['ADMIN'] }
        },
        {
          path: 'audit-logs',
          name: 'AuditLog',
          component: () => import('../views/AuditLog.vue'),
          meta: { title: '审计日志', roles: ['ADMIN'] }
        },
        {
          path: 'announcements',
          name: 'AnnouncementManage',
          component: () => import('../views/AnnouncementManage.vue'),
          meta: { title: '公告管理', roles: ['ADMIN'] }
        },
        {
          path: 'grades-manage',
          name: 'GradeManage',
          component: () => import('../views/GradeManage.vue'),
          meta: { title: '成绩录入', roles: ['TEACHER', 'ADMIN', 'HEAD_TEACHER'] }
        },
        {
          path: 'grades-query',
          name: 'GradeQuery',
          component: () => import('../views/GradeQuery.vue'),
          meta: { title: '成绩查询' }
        },
        {
          path: 'grade-warnings',
          name: 'GradeWarning',
          component: () => import('../views/GradeWarning.vue'),
          meta: { title: '成绩预警', roles: ['ADMIN', 'TEACHER', 'HEAD_TEACHER'] }
        },
        {
          path: 'class-ranking',
          name: 'ClassRanking',
          component: () => import('../views/ClassRanking.vue'),
          meta: { title: '班级排行榜' }
        },
        {
          path: 'class-profile',
          name: 'ClassProfile',
          component: () => import('../views/ClassProfile.vue'),
          meta: { title: '班级画像', roles: ['ADMIN', 'HEAD_TEACHER', 'TEACHER'] }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/Profile.vue'),
          meta: { title: '个人中心' }
        },
        {
          path: 'my-operation-logs',
          name: 'MyOperationLogs',
          component: () => import('../views/MyOperationLogs.vue'),
          meta: { title: '我的操作记录' }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.name !== 'Login' && !userStore.isLoggedIn) {
    next({ name: 'Login' })
  } else if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router

<template>
  <div class="dashboard-container">
    <el-row :gutter="24">
      <el-col :span="8" :xs="24">
        <el-card class="welcome-card" shadow="hover">
          <div class="welcome-header">
            <el-avatar :size="64" class="welcome-avatar" :src="avatarUrl">{{ userStore.user?.name?.charAt(0) }}</el-avatar>
            <div class="welcome-text">
              <h3>早安，{{ userStore.user?.name }}，祝你今天过得开心！</h3>
              <p>{{ roleName }} | {{ userStore.user?.className || '暂无班级' }}</p>
            </div>
          </div>
          <el-divider />
          <div class="user-stats">
            <div class="stat-item">
              <span class="label">上次登录</span>
              <span class="value">{{ lastLoginTime }}</span>
            </div>
            <div class="stat-item" v-if="userStore.role === 'STUDENT'">
              <span class="label">不及格科目</span>
              <span class="value" :class="{ 'text-red': dashboardStats?.failCourseCount > 0 }">{{ dashboardStats?.failCourseCount || 0 }}</span>
            </div>
            <div class="stat-item" v-if="userStore.role === 'STUDENT'">
              <span class="label">未出成绩</span>
              <span class="value">{{ dashboardStats?.ungradedCount || 0 }}</span>
            </div>
            <div class="stat-item" v-if="userStore.role !== 'STUDENT'">
              <span class="label">待录入成绩</span>
              <span class="value" :class="{ 'text-orange': dashboardStats?.pendingCount > 0 }">{{ dashboardStats?.pendingCount || 0 }}</span>
            </div>
            <div class="stat-item" v-if="userStore.role !== 'STUDENT'">
              <span class="label">不及格人次</span>
              <span class="value" :class="{ 'text-red': dashboardStats?.failCourseCount > 0 }">{{ dashboardStats?.failCourseCount || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16" :xs="24">
        <el-card class="announcement-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Bell /></el-icon> 系统公告</span>
              <el-button 
                v-if="userStore.role === 'ADMIN'" 
                type="primary" 
                plain 
                size="small" 
                @click="goToTeacherOverview"
              >
                <el-icon><Guide /></el-icon>
                教师授课总览
              </el-button>
            </div>
          </template>
          <div class="announcement-list" v-loading="announcementLoading">
            <template v-if="announcements.length > 0">
              <div 
                v-for="item in announcements" 
                :key="item.id" 
                class="announcement-item"
                @click="handleViewAnnouncement(item)"
              >
                <el-tag size="small" :type="getTypeTagType(item.type)" effect="light">
                  {{ getTypeName(item.type) }}
                </el-tag>
                <span class="title">{{ item.title }}</span>
                <span class="date">{{ formatDate(item.createdAt) }}</span>
              </div>
            </template>
            <el-empty v-else description="暂无公告" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="24">
         <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="header-title"><el-icon><TrendCharts /></el-icon> 成绩概览</span>
                <el-radio-group v-model="chartPeriod" size="small">
                  <el-radio-button label="week">本周</el-radio-button>
                  <el-radio-button label="month">本月</el-radio-button>
                  <el-radio-button label="term">本学期</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="chartRef" style="height: 350px;"></div>
         </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="detailVisible" title="公告详情" width="500px">
      <el-descriptions :column="1" border v-if="currentAnnouncement">
        <el-descriptions-item label="标题">{{ currentAnnouncement.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getTypeTagType(currentAnnouncement.type)" size="small">
            {{ getTypeName(currentAnnouncement.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatDateTime(currentAnnouncement.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap; line-height: 1.6; min-height: 60px;">
            {{ currentAnnouncement.content || '暂无内容' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import { Bell, TrendCharts, Guide } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElDialog } from 'element-plus'

const router = useRouter()

const userStore = useUserStore()
const chartRef = ref(null)
const chartPeriod = ref('term')
const avatarUrl = ref('')
const lastLoginTime = ref('暂无记录')
const loading = ref(false)
const dashboardStats = ref(null)
const announcements = ref([])
const announcementLoading = ref(false)
const detailVisible = ref(false)
const currentAnnouncement = ref(null)

const roleName = computed(() => {
  const map = {
    'ADMIN': '管理员',
    'TEACHER': '教师',
    'HEAD_TEACHER': '班主任',
    'STUDENT': '学生'
  }
  return map[userStore.user?.role] || userStore.user?.role
})

const getTypeName = (type) => {
  const map = { 'IMPORTANT': '重要', 'NOTICE': '通知', 'INFO': '消息' }
  return map[type] || type || '-'
}

const getTypeTagType = (type) => {
  const map = { 'IMPORTANT': 'danger', 'NOTICE': 'primary', 'INFO': 'success' }
  return map[type] || 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchAnnouncements = async () => {
  announcementLoading.value = true
  try {
    const res = await request.get('/announcements/active')
    if (res && Array.isArray(res)) {
      announcements.value = res
    }
  } catch (error) {
    console.error('获取公告数据失败:', error)
  } finally {
    announcementLoading.value = false
  }
}

const goToTeacherOverview = () => {
  router.push('/teacher-course-overview')
}

const handleViewAnnouncement = (item) => {
  currentAnnouncement.value = item
  detailVisible.value = true
}

const fetchDashboardData = async () => {
  loading.value = true
  try {
    const params = {}
    if (userStore.user?.className) {
      params.className = userStore.user.className
    }
    params.period = chartPeriod.value

    const res = await request.get('/dashboard/stats', { params }).catch(() => null)

    if (res) {
      dashboardStats.value = res
      if (res.lastLoginTime) {
        lastLoginTime.value = res.lastLoginTime
      }
      updateChart()
    }
  } catch (error) {
    console.error('获取首页统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const updateChart = () => {
  if (!chartRef.value) return

  const myChart = echarts.init(chartRef.value)
  
  const courseStats = dashboardStats.value?.courseStats || []
  
  const xAxisData = courseStats.length > 0 
    ? courseStats.map(s => s.courseName) 
    : ['暂无数据']
  
  const seriesData = courseStats.length > 0 
    ? courseStats.map(s => s.averageScore) 
    : [0]

  let option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const dataIndex = params[0].dataIndex
        const stat = courseStats[dataIndex]
        if (stat) {
          return `<div style="font-weight: bold; margin-bottom: 5px;">${stat.courseName}</div>
            <div>平均分: ${stat.averageScore}分</div>
            <div>最高分: ${stat.maxScore}分</div>
            <div>最低分: ${stat.minScore}分</div>
            <div>学生人数: ${stat.studentCount}人</div>
            <div>及格: ${stat.passCount}人</div>
            <div>不及格: ${stat.failCount}人</div>`
        }
        return params[0].name + ': ' + params[0].value + '分'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLine: { lineStyle: { color: '#909399' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: '#E4E7ED' } }
    },
    series: [
      {
        name: '平均分',
        type: 'bar',
        barWidth: '40%',
        data: seriesData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4F46E5' },
            { offset: 1, color: '#818CF8' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        showBackground: true,
        backgroundStyle: {
          color: 'rgba(180, 180, 180, 0.1)'
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}分'
        }
      }
    ]
  }

  myChart.setOption(option, true)
  window.addEventListener('resize', () => {
    myChart.resize()
  })
}

watch(chartPeriod, () => {
  fetchDashboardData()
})

onMounted(async () => {
  await fetchDashboardData()
  fetchAnnouncements()
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.welcome-card {
  height: 100%;
}

.welcome-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.welcome-avatar {
  background: var(--gradient-primary);
  font-size: 24px;
  color: white;
  font-weight: bold;
}

.welcome-text h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: var(--text-primary);
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.stat-item .label {
  font-size: 12px;
  color: var(--text-light);
}

.stat-item .value {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
  font-family: 'Roboto', sans-serif;
}

.stat-item .value.text-red {
  color: #f56c6c;
}

.stat-item .value.text-orange {
  color: #e6a23c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--bg-color);
  transition: all 0.2s;
}

.announcement-item:hover {
  transform: translateX(5px);
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item .title {
  flex: 1;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
}

.announcement-item .title:hover {
  color: var(--primary-color);
}

.announcement-item .date {
  font-size: 12px;
  color: var(--text-light);
}
</style>

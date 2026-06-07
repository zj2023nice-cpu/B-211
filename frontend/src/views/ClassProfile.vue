<template>
  <div class="class-profile-container">
    <el-card shadow="hover" class="profile-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><DataAnalysis /></el-icon>
            <span class="header-title">班级画像</span>
          </div>
          <div class="profile-controls">
            <el-select v-model="selectedTerm" placeholder="选择学期" clearable style="width: 150px" @change="fetchClassProfile">
              <el-option v-for="term in termOptions" :key="term" :label="term" :value="term" />
            </el-select>
            <el-select 
              v-if="canSelectClass"
              v-model="selectedClass" 
              placeholder="选择班级" 
              style="width: 150px" 
              @change="fetchClassProfile"
            >
              <el-option v-for="cls in classOptions" :key="cls" :label="cls" :value="cls" />
            </el-select>
            <el-tag v-else type="info" size="large">{{ selectedClass }}</el-tag>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6" :xs="12">
            <div class="stat-card stat-students">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ profileData?.studentCount || 0 }}</div>
                <div class="stat-label">班级人数</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6" :xs="12">
            <div class="stat-card stat-courses">
              <div class="stat-icon">
                <el-icon><Reading /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ profileData?.courseCount || 0 }}</div>
                <div class="stat-label">已录入课程数</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6" :xs="12">
            <div class="stat-card stat-average">
              <div class="stat-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ profileData?.averageScore || 0 }}</div>
                <div class="stat-label">平均分</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6" :xs="12">
            <div class="stat-card stat-pass">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ profileData?.passRate || 0 }}%</div>
                <div class="stat-label">及格率</div>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="stats-row" style="margin-top: 20px;">
          <el-col :span="12">
            <div class="stat-card stat-excellent">
              <div class="stat-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ profileData?.excellentRate || 0 }}%</div>
                <div class="stat-label">优秀率 (≥90分)</div>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stat-card stat-class-name">
              <div class="stat-icon">
                <el-icon><School /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ profileData?.className || '-' }}</div>
                <div class="stat-label">当前班级</div>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row style="margin-top: 24px;">
          <el-col :span="24">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="card-header">
                  <span class="header-title"><el-icon><Histogram /></el-icon> 各课程均分</span>
                </div>
              </template>
              <div ref="chartRef" style="height: 380px;"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-row style="margin-top: 24px;">
          <el-col :span="24">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="card-header">
                  <span class="header-title"><el-icon><PieChart /></el-icon> 成绩分布</span>
                </div>
              </template>
              <div ref="pieChartRef" style="height: 350px;"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-row style="margin-top: 24px;">
          <el-col :span="24">
            <el-card shadow="hover" class="detail-card">
              <template #header>
                <div class="card-header">
                  <span class="header-title"><el-icon><List /></el-icon> 课程详情</span>
                </div>
              </template>
              <el-table :data="profileData?.courseAverages || []" stripe border>
                <el-table-column prop="courseName" label="课程名称" align="center" />
                <el-table-column prop="averageScore" label="平均分" align="center" width="120">
                  <template #default="scope">
                    <span :class="getScoreClass(scope.row.averageScore)">{{ scope.row.averageScore }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="maxScore" label="最高分" align="center" width="120" />
                <el-table-column prop="minScore" label="最低分" align="center" width="120" />
                <el-table-column prop="studentCount" label="参考人数" align="center" width="120" />
                <el-table-column prop="passCount" label="及格人数" align="center" width="120" />
                <el-table-column prop="excellentCount" label="优秀人数" align="center" width="120" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import { DataAnalysis, User, Reading, TrendCharts, CircleCheck, Star, School, Histogram, PieChart, List } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const profileData = ref(null)
const termOptions = ref([])
const classOptions = ref([])
const selectedTerm = ref('')
const selectedClass = ref('')
const chartRef = ref(null)
const pieChartRef = ref(null)

const canSelectClass = computed(() => {
  return userStore.role === 'ADMIN'
})

const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

const fetchTerms = async () => {
  try {
    let res = []
    try {
      res = await request.get('/terms/names')
    } catch (e) {
      res = await request.get('/grades/ranking/terms')
    }
    termOptions.value = res
  } catch (e) {
    console.error('获取学期列表失败', e)
  }
}

const fetchClasses = async () => {
  try {
    const res = await request.get('/dashboard/classes')
    classOptions.value = res
  } catch (e) {
    console.error('获取班级列表失败', e)
  }
}

const fetchClassProfile = async () => {
  loading.value = true
  try {
    const params = {}
    if (selectedClass.value) {
      params.className = selectedClass.value
    }
    if (selectedTerm.value) {
      params.term = selectedTerm.value
    }
    const res = await request.get('/dashboard/class-profile', { params })
    profileData.value = res
    await nextTick()
    updateCharts()
  } catch (e) {
    console.error('获取班级画像失败', e)
  } finally {
    loading.value = false
  }
}

const updateCharts = () => {
  updateBarChart()
  updatePieChart()
}

const updateBarChart = () => {
  if (!chartRef.value) return

  const myChart = echarts.init(chartRef.value)
  
  const courseAverages = profileData.value?.courseAverages || []
  
  const xAxisData = courseAverages.length > 0 
    ? courseAverages.map(s => s.courseName) 
    : ['暂无数据']
  
  const seriesData = courseAverages.length > 0 
    ? courseAverages.map(s => s.averageScore) 
    : [0]

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const dataIndex = params[0].dataIndex
        const stat = courseAverages[dataIndex]
        if (stat) {
          return `<div style="font-weight: bold; margin-bottom: 5px;">${stat.courseName}</div>
            <div>平均分: ${stat.averageScore}分</div>
            <div>最高分: ${stat.maxScore}分</div>
            <div>最低分: ${stat.minScore}分</div>
            <div>参考人数: ${stat.studentCount}人</div>
            <div>及格人数: ${stat.passCount}人</div>
            <div>优秀人数: ${stat.excellentCount}人</div>`
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
        barWidth: '45%',
        data: seriesData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#8B5CF6' },
            { offset: 1, color: '#A78BFA' }
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

const updatePieChart = () => {
  if (!pieChartRef.value) return

  const myChart = echarts.init(pieChartRef.value)
  
  const courseAverages = profileData.value?.courseAverages || []
  
  let totalPass = 0
  let totalExcellent = 0
  let totalFail = 0
  let totalMid = 0
  
  courseAverages.forEach(course => {
    totalExcellent += course.excellentCount || 0
    totalPass += course.passCount || 0
    totalFail += (course.studentCount || 0) - (course.passCount || 0)
  })
  
  const totalStudents = courseAverages.reduce((sum, c) => sum + (c.studentCount || 0), 0)
  totalMid = totalPass - totalExcellent
  totalPass = totalPass - totalExcellent

  const pieData = []
  if (totalExcellent > 0) {
    pieData.push({ value: totalExcellent, name: '优秀 (≥90)' })
  }
  if (totalMid > 0) {
    pieData.push({ value: totalMid, name: '良好 (70-89)' })
  }
  if (totalPass > 0) {
    pieData.push({ value: totalPass, name: '及格 (60-69)' })
  }
  if (totalFail > 0) {
    pieData.push({ value: totalFail, name: '不及格 (<60)' })
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center'
    },
    series: [
      {
        name: '成绩分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: pieData.length > 0 ? pieData : [{ value: 1, name: '暂无数据' }],
        color: pieData.length > 0 
          ? ['#10B981', '#3B82F6', '#F59E0B', '#EF4444'] 
          : ['#E5E7EB']
      }
    ]
  }

  myChart.setOption(option, true)
  window.addEventListener('resize', () => {
    myChart.resize()
  })
}

onMounted(async () => {
  await Promise.all([fetchTerms(), fetchClasses()])
  
  if (userStore.role === 'HEAD_TEACHER' && userStore.user?.className) {
    selectedClass.value = userStore.user.className
  } else if (classOptions.value.length > 0) {
    selectedClass.value = classOptions.value[0]
  }
  
  if (termOptions.value.length > 0) {
    selectedTerm.value = termOptions.value[0]
  }
  
  fetchClassProfile()
})
</script>

<style scoped>
.class-profile-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
  color: var(--primary-color);
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  font-family: 'Montserrat', sans-serif;
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-controls {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.stats-row {
  margin-top: 0;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-students .stat-icon {
  background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%);
}

.stat-courses .stat-icon {
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
}

.stat-average .stat-icon {
  background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
}

.stat-pass .stat-icon {
  background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%);
}

.stat-excellent .stat-icon {
  background: linear-gradient(135deg, #EC4899 0%, #F472B6 100%);
}

.stat-class-name .stat-icon {
  background: linear-gradient(135deg, #6366F1 0%, #818CF8 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--text-primary);
  font-family: 'Roboto', sans-serif;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.chart-card {
  border-radius: 8px;
}

.detail-card {
  border-radius: 8px;
}

.score-excellent {
  color: #10B981;
  font-weight: 600;
}

.score-pass {
  color: #3B82F6;
  font-weight: 500;
}

.score-fail {
  color: #EF4444;
  font-weight: 600;
}

@media (max-width: 768px) {
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }
  
  .stat-value {
    font-size: 24px;
  }
}
</style>

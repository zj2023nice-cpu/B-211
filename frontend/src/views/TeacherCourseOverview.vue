<template>
  <div class="teacher-overview-container">
    <el-card shadow="hover" class="overview-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Reading /></el-icon>
            <span class="header-title">教师授课总览</span>
          </div>
          <div class="header-right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索教师姓名"
              clearable
              style="width: 200px"
              :prefix-icon="Search"
            />
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="filteredTeachers.length === 0" description="暂无数据" :image-size="100" />

        <div v-else class="teacher-list">
          <div v-for="teacher in filteredTeachers" :key="teacher.teacherId" class="teacher-card">
            <div class="teacher-header">
              <div class="teacher-info">
                <el-avatar :size="48" class="teacher-avatar">{{ teacher.teacherName?.charAt(0) }}</el-avatar>
                <div class="teacher-meta">
                  <h3 class="teacher-name">{{ teacher.teacherName }}</h3>
                  <p class="teacher-courses">
                    <el-icon><Reading /></el-icon>
                    共 {{ teacher.courseCount }} 门课程
                  </p>
                </div>
              </div>
              <div class="teacher-stats-summary">
                <div class="summary-item">
                  <span class="summary-label">总体平均分</span>
                  <span class="summary-value" :class="getScoreClass(getTeacherAverage(teacher))">
                    {{ getTeacherAverage(teacher) }}
                  </span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">总体不及格</span>
                  <span class="summary-value fail">{{ getTeacherTotalFail(teacher) }}人</span>
                </div>
              </div>
            </div>

            <el-table :data="teacher.courseProgressList" stripe border size="small" style="margin-top: 16px;">
              <el-table-column prop="courseName" label="课程名称" min-width="140" />
              <el-table-column label="成绩录入进度" min-width="200">
                <template #default="scope">
                  <div class="progress-wrapper">
                    <el-progress
                      :percentage="scope.row.progressPercent"
                      :stroke-width="10"
                      :color="getProgressColor(scope.row.progressPercent)"
                    />
                    <span class="progress-text">
                      {{ scope.row.enteredCount }}/{{ scope.row.totalStudents }}人
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="averageScore" label="平均分" width="100" align="center">
                <template #default="scope">
                  <span :class="getScoreClass(scope.row.averageScore)">
                    {{ scope.row.averageScore || '-' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="failCount" label="不及格人数" width="100" align="center">
                <template #default="scope">
                  <span :class="scope.row.failCount > 0 ? 'fail-count' : ''">
                    {{ scope.row.failCount || 0 }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="lastGradeChangeTime" label="最近成绩变更" width="160" align="center">
                <template #default="scope">
                  <el-tooltip :content="scope.row.lastGradeChangeTime" placement="top">
                    <span class="last-change-time">
                      <el-icon><Clock /></el-icon>
                      {{ formatTime(scope.row.lastGradeChangeTime) }}
                    </span>
                  </el-tooltip>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { Search, Reading, Clock } from '@element-plus/icons-vue'

const loading = ref(false)
const teacherList = ref([])
const searchKeyword = ref('')

const filteredTeachers = computed(() => {
  if (!searchKeyword.value) {
    return teacherList.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return teacherList.value.filter(t =>
    t.teacherName?.toLowerCase().includes(keyword)
  )
})

const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

const getProgressColor = (percent) => {
  if (percent >= 80) return '#10B981'
  if (percent >= 50) return '#3B82F6'
  if (percent >= 30) return '#F59E0B'
  return '#EF4444'
}

const getTeacherAverage = (teacher) => {
  const courses = teacher.courseProgressList || []
  if (courses.length === 0) return 0
  const validCourses = courses.filter(c => c.averageScore > 0)
  if (validCourses.length === 0) return 0
  const total = validCourses.reduce((sum, c) => sum + c.averageScore, 0)
  return Math.round((total / validCourses.length) * 100) / 100
}

const getTeacherTotalFail = (teacher) => {
  const courses = teacher.courseProgressList || []
  return courses.reduce((sum, c) => sum + (c.failCount || 0), 0)
}

const formatTime = (timeStr) => {
  if (!timeStr || timeStr === '暂无记录') return timeStr
  return timeStr
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/dashboard/teacher-course-overviews')
    teacherList.value = res || []
  } catch (error) {
    console.error('获取教师授课总览失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.teacher-overview-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-card {
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

.teacher-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.teacher-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.teacher-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.teacher-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.teacher-avatar {
  background: var(--gradient-primary);
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.teacher-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.teacher-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.teacher-courses {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.teacher-stats-summary {
  display: flex;
  gap: 32px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.summary-label {
  font-size: 12px;
  color: var(--text-light);
}

.summary-value {
  font-size: 22px;
  font-weight: bold;
  font-family: 'Roboto', sans-serif;
}

.summary-value.fail {
  color: #EF4444;
}

.progress-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-text {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.last-change-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.fail-count {
  color: #EF4444;
  font-weight: 600;
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
  .teacher-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .teacher-stats-summary {
    width: 100%;
    justify-content: space-around;
  }

  .summary-value {
    font-size: 18px;
  }
}
</style>

<template>
  <div class="grade-warning-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>成绩预警</span>
            <el-tag size="small" type="danger" style="margin-left: 10px">不及格: {{ failCount }}</el-tag>
            <el-tag size="small" type="warning" style="margin-left: 5px">临界: {{ borderlineCount }}</el-tag>
            <el-tag size="small" type="success" style="margin-left: 5px">补考通过: {{ makeupPassCount }}</el-tag>
          </div>
          <div class="header-right">
            <el-select v-model="filterTerm" placeholder="筛选学期" clearable style="width: 140px; margin-right: 10px">
              <el-option v-for="term in termOptions" :key="term" :label="term" :value="term" />
            </el-select>
            <el-select v-model="filterCourse" placeholder="筛选课程" clearable style="width: 140px; margin-right: 10px">
              <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
            </el-select>
            <el-select v-model="filterClass" placeholder="筛选班级" clearable style="width: 140px; margin-right: 10px">
              <el-option v-for="cls in classOptions" :key="cls" :label="cls" :value="cls" />
            </el-select>
            <el-button type="primary" @click="fetchData">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe :row-class-name="getRowClassName">
        <el-table-column prop="studentName" label="学生姓名" align="center" width="120" />
        <el-table-column prop="className" label="班级" align="center" width="140" />
        <el-table-column prop="courseName" label="课程" align="center" />
        <el-table-column prop="term" label="学期" align="center" width="120" />
        <el-table-column prop="score" label="原始成绩" align="center" width="120">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.score)">{{ scope.row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="makeupScore" label="补考成绩" align="center" width="120">
          <template #default="scope">
            <span v-if="scope.row.makeupScore !== null" :class="getScoreClass(scope.row.makeupScore)">{{ scope.row.makeupScore }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningLevel" label="预警等级" align="center" width="120">
          <template #default="scope">
            <el-tag :type="getWarningTagType(scope.row.warningLevel)" size="small">
              {{ getWarningLevelText(scope.row.warningLevel) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPageForDisplay"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const courses = ref([])
const allGrades = ref([])

const filterTerm = ref('')
const filterCourse = ref('')
const filterClass = ref('')

const currentPage = ref(0)
const pageSize = ref(10)
const total = ref(0)

const failCount = ref(0)
const borderlineCount = ref(0)
const makeupPassCount = ref(0)

const currentPageForDisplay = computed({
  get: () => currentPage.value + 1,
  set: (val) => {
    currentPage.value = val - 1
  }
})

const termOptions = computed(() => {
  return [...new Set(allGrades.value.map(item => item.term))].sort()
})

const classOptions = computed(() => {
  const classes = [...new Set(allGrades.value.map(item => {
    const studentName = item.studentId || item.studentName
    return item.className || ''
  }))].filter(c => c)
  return classes.sort()
})

const handlePageChange = (val) => {
  currentPage.value = val - 1
  fetchData()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 0
  fetchData()
}

const getScoreClass = (score) => {
  if (score === null || score === undefined) return ''
  if (score < 60) return 'score-fail'
  if (score >= 60 && score < 70) return 'score-borderline'
  return 'score-pass'
}

const getWarningLevelText = (level) => {
  const map = {
    'FAIL': '不及格',
    'BORDERLINE': '临界',
    'MAKEUP_PASS': '补考通过'
  }
  return map[level] || level
}

const getWarningTagType = (level) => {
  const map = {
    'FAIL': 'danger',
    'BORDERLINE': 'warning',
    'MAKEUP_PASS': 'success'
  }
  return map[level] || 'info'
}

const getRowClassName = ({ row }) => {
  if (row.warningLevel === 'FAIL') return 'row-fail'
  if (row.warningLevel === 'BORDERLINE') return 'row-borderline'
  if (row.warningLevel === 'MAKEUP_PASS') return 'row-makeup-pass'
  return ''
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (filterTerm.value) {
      params.term = filterTerm.value
    }
    if (filterCourse.value) {
      params.courseId = filterCourse.value
    }
    if (filterClass.value) {
      params.className = filterClass.value
    }

    const [coursesRes, gradesRes] = await Promise.all([
      request.get('/courses'),
      request.get('/grade-warnings', { params })
    ])

    courses.value = coursesRes

    if (gradesRes && gradesRes.content !== undefined) {
      tableData.value = gradesRes.content
      total.value = gradesRes.totalElements
    } else {
      tableData.value = gradesRes
      total.value = gradesRes.length
    }

    calculateStats()
  } finally {
    loading.value = false
  }
}

const calculateStats = () => {
  let fail = 0
  let borderline = 0
  let makeupPass = 0

  tableData.value.forEach(item => {
    if (item.warningLevel === 'FAIL') fail++
    if (item.warningLevel === 'BORDERLINE') borderline++
    if (item.warningLevel === 'MAKEUP_PASS') makeupPass++
  })

  failCount.value = fail
  borderlineCount.value = borderline
  makeupPassCount.value = makeupPass
}

const resetFilters = () => {
  filterTerm.value = ''
  filterCourse.value = ''
  filterClass.value = ''
  currentPage.value = 0
  fetchData()
}

const fetchAllGradesForOptions = async () => {
  try {
    const res = await request.get('/grades')
    if (res && res.content !== undefined) {
      allGrades.value = res.content
    } else {
      allGrades.value = res
    }
  } catch (e) {
    allGrades.value = []
  }
}

onMounted(() => {
  fetchAllGradesForOptions()
  fetchData()
})
</script>

<style scoped>
.grade-warning-container {
  min-height: 100%;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left span {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  font-family: 'Montserrat', sans-serif;
}

.header-right {
  display: flex;
  align-items: center;
}

.score-fail {
  color: #EF4444;
  font-weight: 600;
  font-family: 'Roboto', sans-serif;
}

.score-borderline {
  color: #F59E0B;
  font-weight: 600;
  font-family: 'Roboto', sans-serif;
}

.score-pass {
  color: #10B981;
  font-weight: 500;
  font-family: 'Roboto', sans-serif;
}

:deep(.row-fail) {
  background-color: rgba(239, 68, 68, 0.08) !important;
}

:deep(.row-fail:hover > td) {
  background-color: rgba(239, 68, 68, 0.15) !important;
}

:deep(.row-borderline) {
  background-color: rgba(245, 158, 11, 0.08) !important;
}

:deep(.row-borderline:hover > td) {
  background-color: rgba(245, 158, 11, 0.15) !important;
}

:deep(.row-makeup-pass) {
  background-color: rgba(16, 185, 129, 0.06) !important;
}

:deep(.row-makeup-pass:hover > td) {
  background-color: rgba(16, 185, 129, 0.12) !important;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

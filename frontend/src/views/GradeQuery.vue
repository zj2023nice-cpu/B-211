<template>
  <div class="grade-query-container">
    <el-card shadow="hover" class="query-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
             <el-icon class="header-icon"><Search /></el-icon>
             <span class="header-title">成绩查询</span>
          </div>
          <div class="query-controls">
             <el-select v-model="termFilter" placeholder="选择学期" clearable style="width: 150px">
                <el-option v-for="term in termOptions" :key="term" :label="term" :value="term" />
             </el-select>
             <el-select v-model="courseFilter" placeholder="选择课程" clearable style="width: 150px">
                <el-option v-for="course in courseOptions" :key="course.id" :label="course.name" :value="course.id" />
             </el-select>
             
             <template v-if="['TEACHER', 'HEAD_TEACHER', 'ADMIN'].includes(userStore.role)">
                <el-select v-model="classFilter" placeholder="选择班级" clearable style="width: 160px" v-if="!(userStore.role === 'HEAD_TEACHER' && queryMode.value === 'my_class')">
                    <el-option v-for="clazz in classOptions" :key="clazz" :label="clazz" :value="clazz" />
                </el-select>
                <el-radio-group v-if="userStore.role === 'HEAD_TEACHER'" v-model="queryMode" @change="handleQueryModeChange" class="mode-switch">
                    <el-radio-button label="my_class">本班成绩</el-radio-button>
                    <el-radio-button label="teaching">任课成绩</el-radio-button>
                </el-radio-group>
                <div class="search-box">
                    <el-input 
                        v-model="studentFilter" 
                        placeholder="输入学生姓名筛选" 
                        :prefix-icon="Search" 
                        clearable 
                        style="width: 180px"
                    />
                </div>
             </template>
             <el-switch
                v-model="isReportMode"
                active-text="综合报表"
                inactive-text="列表视图"
                style="margin: 0 10px"
             />
             <el-button type="success" :icon="Download" @click="exportData">导出</el-button>
          </div>
        </div>
      </template>
      
      <el-table v-if="!isReportMode" :data="displayData" style="width: 100%" v-loading="loading" stripe border height="400">
        <el-table-column prop="term" label="学期" align="center" width="120" sortable />
        <el-table-column prop="courseId" label="课程" align="center" sortable :sort-method="(a, b) => getCourseName(a.courseId).localeCompare(getCourseName(b.courseId))">
             <template #default="scope">
                <el-tag effect="plain">{{ getCourseName(scope.row.courseId) }}</el-tag>
             </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学生" align="center" sortable :sort-method="(a, b) => getStudentName(a.studentId).localeCompare(getStudentName(b.studentId))">
             <template #default="scope">
                {{ getStudentName(scope.row.studentId) }}
             </template>
        </el-table-column>
        <el-table-column label="班级" align="center" width="120">
             <template #default="scope">
                {{ getStudentClass(scope.row.studentId) }}
             </template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" sortable align="center">
            <template #default="scope">
                <span :class="getScoreClass(scope.row.score)">{{ scope.row.score }}</span>
            </template>
        </el-table-column>
        <el-table-column prop="makeupScore" label="补考成绩" align="center">
            <template #default="scope">
                <span :class="scope.row.makeupScore < 60 ? 'score-fail' : ''" v-if="scope.row.makeupScore !== null">{{ scope.row.makeupScore }}</span>
                <span v-else>-</span>
            </template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="reportData" style="width: 100%" v-loading="loading" stripe border height="400">
        <el-table-column prop="studentId" label="学号" align="center" width="100" sortable fixed />
        <el-table-column prop="name" label="姓名" align="center" width="120" fixed />
        <el-table-column prop="className" label="班级" align="center" width="130" v-if="userStore.role !== 'STUDENT'" />
        <el-table-column v-for="course in reportCourses" :key="course.id" :label="course.name" align="center" width="120">
            <template #default="scope">
                <span :class="getScoreClass(scope.row.scores[course.id] || 0)" v-if="scope.row.scores[course.id] !== undefined">
                    {{ scope.row.scores[course.id] }}
                </span>
                <span v-else>-</span>
            </template>
        </el-table-column>
        <el-table-column prop="total" label="总分" align="center" sortable width="100" fixed="right" />
        <el-table-column prop="avg" label="平均分" align="center" sortable width="100" fixed="right" />
        <el-table-column prop="rank" label="排名" align="center" sortable width="80" fixed="right">
            <template #default="scope">
                <el-tag :type="scope.row.rank <= 3 ? 'danger' : 'info'" effect="dark" v-if="scope.row.rank <= 3">{{ scope.row.rank }}</el-tag>
                <span v-else>{{ scope.row.rank }}</span>
            </template>
        </el-table-column>
      </el-table>
      
      <div v-if="!isReportMode" class="pagination-container">
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
    </el-card>
    
    <el-card shadow="hover" class="chart-card">
        <template #header>
            <div class="card-header">
                <div class="header-left">
                    <el-icon class="header-icon"><DataAnalysis /></el-icon>
                    <span class="header-title">成绩统计分析</span>
                </div>
            </div>
        </template>
        <div class="charts-container">
            <div ref="chartRef" class="chart-item"></div>
            <div v-if="userStore.role === 'STUDENT'" ref="radarChartRef" class="chart-item"></div>
            <div v-else ref="pieChartRef" class="chart-item"></div>
        </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, watch, nextTick, computed } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { Search, Download, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { usePagination } from '@/composables/usePagination'
import { getScoreClass } from '@/utils/format'
import { exportCsv } from '@/utils/csv'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const fullTableData = ref([])
const courses = ref([])
const allCourses = ref([])
const students = ref([])
const queryMode = ref('my_class')
const chartRef = ref(null)
const radarChartRef = ref(null)
const pieChartRef = ref(null)
let myChart = null
let radarChart = null
let pieChart = null

const termFilter = ref('')
const courseFilter = ref('')
const classFilter = ref('')
const studentFilter = ref('')
const isReportMode = ref(false)
const filterTermOptions = ref([])
const filterCourseOptions = ref([])
const filterClassOptions = ref([])

const { currentPage, pageSize, total, currentPageForDisplay, resetPage } = usePagination(10)

const termOptions = computed(() => filterTermOptions.value)
const courseOptions = computed(() => filterCourseOptions.value)
const classOptions = computed(() => filterClassOptions.value)

const displayData = computed(() => tableData.value)
const chartData = computed(() => fullTableData.value)

const reportCourses = computed(() => {
    const courseIds = [...new Set(fullTableData.value.map(item => item.courseId))]
    const courseList = courseIds.map(id => allCourses.value.find(c => c.id === id)).filter(Boolean)
    
    const courseScoresCount = {}
    fullTableData.value.forEach(grade => {
        const finalScore = grade.makeupScore !== null ? grade.makeupScore : grade.score
        if (finalScore !== null && finalScore !== undefined) {
            courseScoresCount[grade.courseId] = (courseScoresCount[grade.courseId] || 0) + 1
        }
    })
    
    return courseList
        .filter(course => courseScoresCount[course.id] > 0)
        .sort((a, b) => {
            if (a.id && b.id) {
                return String(a.id).localeCompare(String(b.id))
            }
            return a.name.localeCompare(b.name)
        })
})

const reportData = computed(() => {
    const studentMap = {}
    const availableCourseIds = reportCourses.value.map(c => c.id)
    
    fullTableData.value.forEach(grade => {
        if (!availableCourseIds.includes(grade.courseId)) return
        
        if (!studentMap[grade.studentId]) {
            const student = students.value.find(s => s.id === grade.studentId)
            studentMap[grade.studentId] = {
                studentId: grade.studentId,
                name: student ? student.name : String(grade.studentId),
                className: student ? (student.className || '未知班级') : '-',
                scores: {},
                total: 0,
                count: 0
            }
        }
        const finalScore = grade.makeupScore !== null ? grade.makeupScore : grade.score
        if (finalScore !== null && finalScore !== undefined) {
            studentMap[grade.studentId].scores[grade.courseId] = finalScore
            studentMap[grade.studentId].total += finalScore
            studentMap[grade.studentId].count += 1
        }
    })
    
    const studentList = Object.values(studentMap).map(student => ({
        ...student,
        avg: student.count > 0 ? parseFloat((student.total / student.count).toFixed(2)) : 0
    }))
    
    studentList.sort((a, b) => {
        if (b.total !== a.total) return b.total - a.total
        if (b.avg !== a.avg) return b.avg - a.avg
        return a.name.localeCompare(b.name)
    })
    
    let currentRank = 1
    let previousTotal = null
    let previousAvg = null
    let sameRankCount = 0
    
    studentList.forEach((student, index) => {
        if (index === 0) {
            student.rank = 1
            previousTotal = student.total
            previousAvg = student.avg
            sameRankCount = 1
        } else {
            if (student.total === previousTotal && student.avg === previousAvg) {
                student.rank = currentRank
                sameRankCount++
            } else {
                currentRank += sameRankCount
                student.rank = currentRank
                previousTotal = student.total
                previousAvg = student.avg
                sameRankCount = 1
            }
        }
    })
    
    return studentList
})

const handlePageChange = (val) => {
    currentPageForDisplay.value = val
    fetchData()
}

const handleSizeChange = (val) => {
    pageSize.value = val
    resetPage()
    fetchData()
}

const handleQueryModeChange = () => {
    classFilter.value = ''
    resetPage()
    fetchData()
}

const handleReportModeChange = () => {
    resetPage()
    fetchData()
}

const getCourseName = (id) => {
    const course = allCourses.value.find(c => c.id === id)
    return course ? course.name : id
}

const getStudentName = (id) => {
    const student = students.value.find(s => s.id === id)
    return student ? student.name : id
}

const getStudentClass = (id) => {
    const student = students.value.find(s => s.id === id)
    return student ? (student.className || '未知班级') : '-'
}

const buildFilterParams = () => {
    const params = {}
    if (termFilter.value) params.term = termFilter.value
    if (courseFilter.value) params.courseId = courseFilter.value
    if (classFilter.value && !(userStore.role === 'HEAD_TEACHER' && queryMode.value === 'my_class')) {
        params.className = classFilter.value
    }
    if (studentFilter.value) params.studentName = studentFilter.value
    return params
}

const getGradesBaseUrl = () => {
    if (userStore.role === 'STUDENT' && userStore.user?.id) {
        return `/grades/student/${userStore.user.id}`
    } else if (userStore.role === 'TEACHER' && userStore.user?.id) {
        return `/grades/teacher/${userStore.user.id}`
    } else if (userStore.role === 'HEAD_TEACHER') {
        if (queryMode.value === 'my_class' && userStore.user?.className) {
            return `/grades/class/${userStore.user.className}`
        } else if (userStore.user?.id) {
            return `/grades/teacher/${userStore.user.id}`
        }
    }
    return '/grades'
}

const getFilterOptionsParams = () => {
    const params = {}
    if (userStore.role === 'STUDENT' && userStore.user?.id) {
        params.studentId = userStore.user.id
    } else if (userStore.role === 'TEACHER' && userStore.user?.id) {
        params.teacherId = userStore.user.id
    } else if (userStore.role === 'HEAD_TEACHER') {
        if (queryMode.value === 'my_class' && userStore.user?.className) {
            params.className = userStore.user.className
        } else if (userStore.user?.id) {
            params.teacherId = userStore.user.id
        }
    }
    return params
}

const fetchData = async () => {
  loading.value = true
  try {
    const gradesUrl = getGradesBaseUrl()
    const filterParams = buildFilterParams()
    const optionsParams = getFilterOptionsParams()
    
    const [coursesRes, usersRes, filterTermsRes, filterCoursesRes, filterClassesRes] = await Promise.all([
        request.get('/courses'),
        request.get('/users'),
        request.get('/grades/filter/terms', { params: optionsParams }).catch(() => []),
        request.get('/grades/filter/courses', { params: { ...optionsParams, term: termFilter.value } }).catch(() => []),
        userStore.role !== 'STUDENT' ? request.get('/grades/filter/classes', { params: { ...optionsParams, term: termFilter.value } }).catch(() => []) : Promise.resolve([])
    ])
    
    filterTermOptions.value = filterTermsRes || []
    filterCourseOptions.value = filterCoursesRes || []
    filterClassOptions.value = filterClassesRes || []
    
    allCourses.value = coursesRes
    courses.value = coursesRes
    students.value = usersRes.filter(u => u.role === 'STUDENT')
    
    const fullGradesRes = await request.get(gradesUrl, { params: filterParams })
    fullTableData.value = fullGradesRes
    
    if (isReportMode.value) {
        tableData.value = fullGradesRes
        total.value = fullGradesRes.length
    } else {
        const gradesRes = await request.get(gradesUrl, { 
            params: { 
                ...filterParams, 
                page: currentPage.value, 
                size: pageSize.value 
            } 
        })
        if (gradesRes && gradesRes.content !== undefined) {
            tableData.value = gradesRes.content
            total.value = gradesRes.totalElements
        } else {
            tableData.value = gradesRes
            total.value = gradesRes.length
        }
    }
    
    updateChart()
  } finally {
    loading.value = false
  }
}



const exportData = () => {
    if (isReportMode.value) {
        const sortedCourses = [...reportCourses.value].sort((a, b) => {
            if (a.id && b.id) {
                return String(a.id).localeCompare(String(b.id))
            }
            return a.name.localeCompare(b.name)
        })
        
        const header = userStore.role !== 'STUDENT' 
            ? ['学号', '姓名', '班级', ...sortedCourses.map(c => c.name), '总分', '平均分', '排名']
            : ['学号', '姓名', ...sortedCourses.map(c => c.name), '总分', '平均分', '排名']
        
        const data = reportData.value.map(row => {
            const baseRow = [row.studentId, row.name]
            if (userStore.role !== 'STUDENT') {
                baseRow.push(row.className)
            }
            baseRow.push(
                ...sortedCourses.map(c => row.scores[c.id] !== undefined ? row.scores[c.id] : '-'),
                row.total,
                row.avg,
                row.rank
            )
            return baseRow
        })
        
        exportCsv(header, data, `综合成绩单_${new Date().toLocaleDateString()}.csv`)
        return
    }

    const header = ['学期', '课程', '学生', '班级', '成绩', '补考成绩']
    const data = displayData.value.map(row => [
        row.term,
        getCourseName(row.courseId),
        getStudentName(row.studentId),
        getStudentClass(row.studentId),
        row.score,
        row.makeupScore || '-'
    ])
    
    exportCsv(header, data, `成绩导出_${new Date().toLocaleDateString()}.csv`)
}

const updateChart = () => {
    nextTick(() => {
        if (!myChart) myChart = echarts.init(chartRef.value)
        
        const dataToAnalyze = chartData.value
        
        // 1. Bar Chart: Course Average
        const courseGroups = {}
        dataToAnalyze.forEach(grade => {
            const courseName = getCourseName(grade.courseId)
            if (!courseGroups[courseName]) {
                courseGroups[courseName] = { total: 0, count: 0 }
            }
            const finalScore = grade.makeupScore || grade.score || 0
            courseGroups[courseName].total += finalScore
            courseGroups[courseName].count += 1
        })
        
        const xAxisData = Object.keys(courseGroups)
        const seriesData = xAxisData.map(name => {
            return (courseGroups[name].total / courseGroups[name].count).toFixed(2)
        })
        
        const barOption = {
            title: { text: '课程平均分统计' },
            tooltip: { trigger: 'axis' },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: { type: 'category', data: xAxisData },
            yAxis: { type: 'value' },
            series: [{
                name: '平均分',
                type: 'bar',
                data: seriesData,
                itemStyle: { 
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#4F46E5' },
                        { offset: 1, color: '#818CF8' }
                    ])
                },
                label: { show: true, position: 'top' }
            }]
        }
        myChart.setOption(barOption)
        
        // 2. Student: Radar Chart
        if (userStore.role === 'STUDENT' && radarChartRef.value) {
            if (!radarChart) radarChart = echarts.init(radarChartRef.value)
            
            const indicator = xAxisData.map(name => ({ name, max: 100 }))
            const scores = xAxisData.map(name => {
                // For student, average is actual score since unique course per term usually
                return (courseGroups[name].total / courseGroups[name].count).toFixed(2)
            })
            
            const radarOption = {
                title: { text: '学科能力雷达图' },
                tooltip: {},
                radar: { indicator },
                series: [{
                    name: '成绩分布',
                    type: 'radar',
                    data: [{ value: scores, name: '我的成绩' }],
                    areaStyle: { opacity: 0.2, color: '#10B981' },
                    itemStyle: { color: '#10B981' }
                }]
            }
            radarChart.setOption(radarOption)
        }
        
        // 3. Teacher/Admin: Pie Chart
        if (userStore.role !== 'STUDENT' && pieChartRef.value) {
            if (!pieChart) pieChart = echarts.init(pieChartRef.value)
            
            let distribution = { fail: 0, pass: 0, good: 0, excellent: 0 }
            dataToAnalyze.forEach(grade => {
                const s = grade.makeupScore || grade.score || 0
                if (s < 60) distribution.fail++
                else if (s < 75) distribution.pass++
                else if (s < 90) distribution.good++
                else distribution.excellent++
            })
            
            const pieOption = {
                title: { text: '成绩区间分布' },
                tooltip: { trigger: 'item' },
                legend: { top: '5%', left: 'center' },
                series: [{
                    name: '分布情况',
                    type: 'pie',
                    radius: ['40%', '65%'],
                    center: ['50%', '60%'],
                    avoidLabelOverlap: false,
                    itemStyle: {
                        borderRadius: 10,
                        borderColor: '#fff',
                        borderWidth: 2
                    },
                    data: [
                        { value: distribution.fail, name: '不及格 (<60)', itemStyle: { color: '#EF4444' } },
                        { value: distribution.pass, name: '及格 (60-74)', itemStyle: { color: '#F59E0B' } },
                        { value: distribution.good, name: '良好 (75-89)', itemStyle: { color: '#4F46E5' } },
                        { value: distribution.excellent, name: '优秀 (≥90)', itemStyle: { color: '#10B981' } }
                    ]
                }]
            }
            pieChart.setOption(pieOption)
        }
    })
}

watch([termFilter, courseFilter, classFilter, studentFilter], () => {
    resetPage()
    fetchData()
})

watch(isReportMode, () => {
    handleReportModeChange()
})

const handleResize = () => {
  myChart?.resize()
  radarChart?.resize()
  pieChart?.resize()
}

const destroyCharts = () => {
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
  if (radarChart) {
    radarChart.dispose()
    radarChart = null
  }
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  destroyCharts()
})
</script>

<style scoped>
.grade-query-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
}
.query-card, .chart-card {
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
}
.query-controls {
    display: flex;
    align-items: center;
    gap: 15px;
    flex-wrap: wrap;
}
.search-box {
    display: flex;
    gap: 10px;
}
.score-fail {
    color: #EF4444;
    font-weight: 600;
    font-family: 'Roboto', sans-serif;
}
.score-excellent {
    color: #10B981;
    font-weight: 600;
    font-family: 'Roboto', sans-serif;
}
.charts-container {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
}
.chart-item {
    flex: 1;
    min-width: 400px;
    height: 400px;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>

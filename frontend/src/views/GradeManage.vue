<template>
  <div class="grade-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>成绩录入管理</span>
          </div>
          <div class="header-right">
             <el-switch
                v-model="isBatchMode"
                active-text="批量录入模式"
                inactive-text="普通模式"
                style="margin-right: 20px"
             />
             <el-button v-if="isBatchMode" type="success" @click="handleBatchSave" :loading="batchSaving">
               保存全部变更
               <el-badge v-if="modifiedRows.size > 0" :value="modifiedRows.size" :max="99" class="save-badge" />
             </el-button>
             <div v-else style="display: inline-block">
                <el-select v-model="filterTerm" placeholder="筛选学期" clearable style="width: 120px; margin-right: 10px">
                    <el-option v-for="term in filterTermOptions" :key="term" :label="term" :value="term" />
                </el-select>
                <el-select v-model="filterCourse" placeholder="筛选课程" clearable style="width: 120px; margin-right: 10px">
                    <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
                </el-select>
                <el-input v-model="filterStudent" placeholder="搜索学生姓名" style="width: 120px; margin-right: 10px" clearable />
                <el-button type="primary" @click="handleAdd">录入成绩</el-button>
                <el-button type="warning" @click="handleImportClick">批量导入</el-button>
                <el-button type="info" @click="downloadTemplate">下载模板</el-button>
             </div>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe :row-class-name="tableRowClassName">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="term" label="学期" align="center" sortable width="120" />
        <el-table-column prop="courseId" label="课程" align="center" sortable>
             <template #default="scope">
                {{ getCourseName(scope.row.courseId) }}
             </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学生" align="center" sortable>
             <template #default="scope">
                {{ getStudentName(scope.row.studentId) }}
             </template>
        </el-table-column>
        <el-table-column label="班级" align="center" width="120">
             <template #default="scope">
                {{ getStudentClass(scope.row.studentId) }}
             </template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" align="center" sortable width="180">
             <template #default="scope">
                <div v-if="isBatchMode">
                    <el-input-number 
                        v-model="scope.row.score" 
                        :min="0" :max="100" 
                        controls-position="right"
                        size="small"
                        :class="{'is-error': !isValidScore(scope.row.score)}"
                        @change="handleScoreChange(scope.row)"
                    />
                    <div v-if="!isValidScore(scope.row.score)" class="error-tip">0-100</div>
                </div>
                <span v-else :class="getScoreClass(scope.row.score)">{{ scope.row.score }}</span>
             </template>
        </el-table-column>
        <el-table-column prop="makeupScore" label="补考成绩" align="center" width="180">
             <template #default="scope">
                <div v-if="isBatchMode">
                     <el-input-number 
                        v-model="scope.row.makeupScore" 
                        :min="0" :max="100" 
                        controls-position="right"
                        size="small"
                        placeholder="-"
                        @change="handleScoreChange(scope.row)"
                    />
                </div>
                <div v-else>
                    <span v-if="scope.row.makeupScore !== null" :class="scope.row.makeupScore < 60 ? 'score-fail' : ''">{{ scope.row.makeupScore }}</span>
                    <span v-else>-</span>
                </div>
             </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">修改</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)" v-if="userStore.role === 'ADMIN'">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="80px">
        <el-form-item label="学期">
          <el-select v-model="form.term" placeholder="请选择学期" style="width: 100%" filterable>
            <el-option
              v-for="option in dialogTermOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="form.courseId" placeholder="请选择课程">
            <el-option
              v-for="item in courses"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学生">
          <el-select v-model="form.studentId" placeholder="请选择学生" filterable>
            <el-option
              v-for="item in students"
              :key="item.id"
              :label="item.name + ' (' + item.username + ')'"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="成绩">
          <el-input-number v-model="form.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="补考成绩">
          <el-input-number v-model="form.makeupScore" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="批量导入成绩" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        accept=".csv"
        drag
        style="text-align: center"
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传CSV格式的文件，且不超过10MB
          </div>
          <div class="el-upload__tip" style="margin-top: 8px; color: #606266;">
            请先下载模板，按照模板格式填写数据后再上传
          </div>
        </template>
      </el-upload>
      
      <div v-if="importResult" class="import-result" style="margin-top: 20px;">
        <el-divider>导入结果</el-divider>
        <div class="result-summary">
          <el-statistic title="总计" :value="importResult.total" style="margin-right: 30px;" />
          <el-statistic title="成功" :value="importResult.successCount" value-style="color: #67C23A;" style="margin-right: 30px;" />
          <el-statistic title="失败" :value="importResult.failCount" value-style="color: #F56C6C;" />
        </div>
        
        <div v-if="importResult.errors && importResult.errors.length > 0" style="margin-top: 20px;">
          <h4 style="margin-bottom: 10px; color: #F56C6C;">错误详情：</h4>
          <el-table :data="importResult.errors" max-height="300" border size="small">
            <el-table-column prop="rowNumber" label="行号" width="80" align="center" />
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="courseName" label="课程" width="100" />
            <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleImport" :loading="importing" :disabled="!selectedFile">
            开始导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch, nextTick } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const courses = ref([])
const allCourses = ref([])
const students = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')

const filterTerm = ref('')
const filterCourse = ref('')
const filterStudent = ref('')

const isBatchMode = ref(false)
const batchSaving = ref(false)
const modifiedRows = ref(new Set())

const currentPage = ref(0)
const pageSize = ref(10)
const total = ref(0)
const allGradesForOptions = ref([])
const termOptionsFromAPI = ref([])
const enabledTermsFromAPI = ref([])

const importDialogVisible = ref(false)
const uploading = ref(false)
const importing = ref(false)
const selectedFile = ref(null)
const importResult = ref(null)
const uploadRef = ref(null)

const currentPageForDisplay = computed({
  get: () => currentPage.value + 1,
  set: (val) => {
    currentPage.value = val - 1
  }
})

const filterTermOptions = computed(() => {
    if (termOptionsFromAPI.value && termOptionsFromAPI.value.length > 0) {
        return termOptionsFromAPI.value
    }
    return [...new Set(allGradesForOptions.value.map(item => item.term))].sort()
})

const editableTermOptions = computed(() => enabledTermsFromAPI.value || [])

const dialogTermOptions = computed(() => {
    const options = editableTermOptions.value.map(term => ({
        value: term,
        label: term
    }))

    if (form.term && !editableTermOptions.value.includes(form.term)) {
        options.push({
            value: form.term,
            label: `${form.term}（历史学期）`
        })
    }

    return options
})

const confirmUnsavedChanges = async () => {
    if (!isBatchMode.value || modifiedRows.value.size === 0) {
        return true
    }
    try {
        await ElMessageBox.confirm(
            `当前有 ${modifiedRows.value.size} 条修改未保存，确定要离开吗？未保存的修改将会丢失。`,
            '未保存变更提醒',
            {
                confirmButtonText: '确定离开',
                cancelButtonText: '取消',
                type: 'warning',
                distinguishCancelAndClose: true
            }
        )
        return true
    } catch {
        return false
    }
}

const handlePageChange = async (val) => {
    const confirmed = await confirmUnsavedChanges()
    if (!confirmed) {
        nextTick(() => {
            currentPageForDisplay.value = currentPage.value + 1
        })
        return
    }
    modifiedRows.value.clear()
    currentPage.value = val - 1
    fetchData()
}

const handleSizeChange = async (val) => {
    const confirmed = await confirmUnsavedChanges()
    if (!confirmed) return
    modifiedRows.value.clear()
    pageSize.value = val
    currentPage.value = 0
    fetchData()
}

const isValidScore = (score) => {
    return score !== null && score >= 0 && score <= 100
}

const handleScoreChange = (row) => {
    modifiedRows.value.add(row.id)
}

const tableRowClassName = ({ row }) => {
    if (isBatchMode.value && modifiedRows.value.has(row.id)) {
        return 'row-modified'
    }
    return ''
}

const handleBatchSave = async () => {
    if (modifiedRows.value.size === 0) {
        ElMessage.info('没有需要保存的更改')
        return
    }
    
    const rowsToSave = tableData.value.filter(row => modifiedRows.value.has(row.id))
    const invalidRows = rowsToSave.filter(row => !isValidScore(row.score) || (row.makeupScore !== null && !isValidScore(row.makeupScore)))
    
    if (invalidRows.length > 0) {
        ElMessage.error(`存在 ${invalidRows.length} 条无效数据，请修正后再保存`)
        return
    }
    
    batchSaving.value = true
    try {
        const promises = rowsToSave.map(row => 
            request.put(`/grades/${row.id}`, row, { skipErrorNotification: true })
                .then(() => ({ success: true, row }))
                .catch(error => ({ success: false, row, error }))
        )
        
        const results = await Promise.all(promises)
        const successCount = results.filter(r => r.success).length
        const failedResults = results.filter(r => !r.success)
        
        if (failedResults.length === 0) {
            ElMessage.success(`成功保存 ${successCount} 条记录`)
            modifiedRows.value.clear()
            isBatchMode.value = false
            fetchData()
        } else {
            const failedIds = failedResults.map(r => r.row.id)
            const errorMessages = [...new Set(failedResults.map(r => r.error?.userMessage || r.error?.message || '保存失败'))]
            
            modifiedRows.value.forEach(id => {
                if (!failedIds.includes(id)) {
                    modifiedRows.value.delete(id)
                }
            })
            
            ElMessage.error({
                message: `保存失败 ${failedResults.length} 条，成功 ${successCount} 条。\n失败原因：${errorMessages.join('；')}`,
                duration: 5000,
                showClose: true
            })
        }
    } catch (error) {
        ElMessage.error('保存失败：' + (error.message || '未知错误'))
    } finally {
        batchSaving.value = false
    }
}

const form = reactive({
  id: null,
  studentId: null,
  courseId: null,
  score: 0,
  makeupScore: null,
  term: ''
})

const getCourseName = (id) => {
    const course = allCourses.value.find(c => c.id == id)
    return course ? course.name : id
}

const getStudentName = (id) => {
    const student = students.value.find(s => s.id == id)
    return student ? student.name : id
}

const getStudentClass = (id) => {
    const student = students.value.find(s => s.id == id)
    return student ? (student.className || '未知班级') : '-'
}

const fetchData = async () => {
  loading.value = true
  try {
    let gradesUrl = '/grades'
    if (userStore.role === 'TEACHER' && userStore.user?.id) {
        gradesUrl = `/grades/teacher/${userStore.user.id}`
    } else if (userStore.role === 'HEAD_TEACHER' && userStore.user?.className) {
        gradesUrl = `/grades/class/${userStore.user.className}`
    }
    
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
    if (filterStudent.value) {
        params.studentName = filterStudent.value
    }
    
    const [coursesRes, usersRes, termsRes, enabledTermsRes] = await Promise.all([
        request.get('/courses'),
        request.get('/users'),
        request.get('/terms/names').catch(() => []),
        request.get('/terms/enabled').catch(() => [])
    ])

    termOptionsFromAPI.value = termsRes || []
    enabledTermsFromAPI.value = (enabledTermsRes || []).map(term => term.name)
    
    allCourses.value = coursesRes
    courses.value = coursesRes
    students.value = usersRes.filter(u => u.role === 'STUDENT')
    
    if (['TEACHER', 'HEAD_TEACHER'].includes(userStore.role)) {
        courses.value = coursesRes.filter(c => c.teacherId === userStore.user.id)
    }
    
    if (userStore.role === 'HEAD_TEACHER' && userStore.user.className) {
        students.value = students.value.filter(s => s.className === userStore.user.className)
    }
    
    if (allGradesForOptions.value.length === 0) {
        const allGradesRes = await request.get(gradesUrl)
        allGradesForOptions.value = allGradesRes
    }
    
    const gradesRes = await request.get(gradesUrl, { params })
    
    if (gradesRes && gradesRes.content !== undefined) {
        tableData.value = gradesRes.content
        total.value = gradesRes.totalElements
    } else {
        tableData.value = gradesRes
        total.value = gradesRes.length
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  if (editableTermOptions.value.length === 0) {
    ElMessage.warning('暂无可用学期，请先由管理员维护并启用学期')
    return
  }

  dialogTitle.value = '录入成绩'
  Object.assign(form, {
    id: null,
    studentId: null,
    courseId: null,
    score: 0,
    makeupScore: null,
    term: editableTermOptions.value[0]
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '修改成绩'
  Object.assign(form, {
    ...row,
    term: row.term || ''
  })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该成绩记录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/grades/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await request.put(`/grades/${form.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/grades', form)
      ElMessage.success('录入成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    // Error handled
  }
}

const getScoreClass = (score) => {
    if (score < 60) return 'score-fail'
    if (score >= 90) return 'score-excellent'
    return ''
}

let filterWatchInitialized = false
watch([filterTerm, filterCourse, filterStudent], async () => {
    if (!filterWatchInitialized) {
        filterWatchInitialized = true
        return
    }
    const confirmed = await confirmUnsavedChanges()
    if (!confirmed) return
    modifiedRows.value.clear()
    currentPage.value = 0
    fetchData()
})

const handleImportClick = async () => {
    const confirmed = await confirmUnsavedChanges()
    if (!confirmed) return
    modifiedRows.value.clear()
    importDialogVisible.value = true
    importResult.value = null
    selectedFile.value = null
    if (uploadRef.value) {
        uploadRef.value.clearFiles()
    }
}

watch(isBatchMode, async (newVal, oldVal) => {
    if (oldVal === true && newVal === false && modifiedRows.value.size > 0) {
        const confirmed = await confirmUnsavedChanges()
        if (!confirmed) {
            nextTick(() => {
                isBatchMode.value = true
            })
            return
        }
        modifiedRows.value.clear()
    }
})

const handleFileChange = (file) => {
    const isCSV = file.raw.type === 'text/csv' || file.raw.name.endsWith('.csv')
    const isLt10M = file.raw.size / 1024 / 1024 < 10

    if (!isCSV) {
        ElMessage.error('只能上传CSV格式的文件！')
        if (uploadRef.value) {
            uploadRef.value.clearFiles()
        }
        selectedFile.value = null
        return
    }
    if (!isLt10M) {
        ElMessage.error('文件大小不能超过10MB！')
        if (uploadRef.value) {
            uploadRef.value.clearFiles()
        }
        selectedFile.value = null
        return
    }
    selectedFile.value = file.raw
    importResult.value = null
}

const handleExceed = () => {
    ElMessage.warning('只能上传一个文件！')
}

const handleImport = async () => {
    if (!selectedFile.value) {
        ElMessage.warning('请选择要上传的文件！')
        return
    }

    importing.value = true
    try {
        const formData = new FormData()
        formData.append('file', selectedFile.value)

        const result = await request.post('/grades/import', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })

        importResult.value = result
        
        if (result.failCount === 0) {
            ElMessage.success(`成功导入 ${result.successCount} 条成绩记录`)
            fetchData()
        } else {
            ElMessage.warning(`导入完成，成功 ${result.successCount} 条，失败 ${result.failCount} 条，请查看错误详情`)
            if (result.successCount > 0) {
                fetchData()
            }
        }
    } catch (error) {
        console.error('导入失败:', error)
    } finally {
        importing.value = false
    }
}

const downloadTemplate = () => {
    const header = ['学期', '课程名称', '学生姓名', '班级', '成绩', '补考成绩']
    const templateTerm = editableTermOptions.value[0] || '请替换为已启用学期'
    const exampleData = [
        [templateTerm, '高等数学', '张三', '计算机一班', '85', '-'],
        [templateTerm, '高等数学', '李四', '计算机一班', '78', '82'],
        [templateTerm, '大学英语', '王五', '计算机一班', '92', '-']
    ]
    
    const csvContent = [
        header.join(','),
        ...exampleData.map(row => row.join(','))
    ].join('\n')
    
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `成绩导入模板.csv`
    link.click()
    
    ElMessage.success('模板下载成功，请按照模板格式填写数据')
}

onBeforeRouteLeave(async (to, from, next) => {
    const confirmed = await confirmUnsavedChanges()
    if (confirmed) {
        modifiedRows.value.clear()
        next()
    } else {
        next(false)
    }
})

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.grade-manage-container {
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
    margin-right: 15px;
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

.score-excellent {
    color: #10B981;
    font-weight: 600;
    font-family: 'Roboto', sans-serif;
}

.error-tip {
    color: #EF4444;
    font-size: 12px;
    line-height: 1;
    margin-top: 4px;
    font-family: 'Roboto', sans-serif;
}

:deep(.is-error .el-input__wrapper) {
    box-shadow: 0 0 0 1px #EF4444 inset;
}

:deep(.el-table .row-modified) {
    background-color: #fef3c7 !important;
}

:deep(.el-table .row-modified:hover > td) {
    background-color: #fde68a !important;
}

:deep(.el-table .row-modified td:first-child .cell) {
    position: relative;
    padding-left: 20px;
}

:deep(.el-table .row-modified td:first-child .cell::before) {
    content: '';
    position: absolute;
    left: 6px;
    top: 50%;
    transform: translateY(-50%);
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: #f59e0b;
    box-shadow: 0 0 4px rgba(245, 158, 11, 0.6);
}

.save-badge {
    margin-left: 6px;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>

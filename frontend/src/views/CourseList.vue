<template>
  <div class="course-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程管理</span>
          <div class="header-actions">
            <el-button type="success" plain @click="goToTeacherOverview">
              <el-icon><Guide /></el-icon>
              教师授课总览
            </el-button>
            <el-button type="primary" @click="handleAdd">新增课程</el-button>
          </div>
        </div>
      </template>
      
      <div class="search-filter-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="课程名称">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入课程名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="任课教师">
            <el-select
              v-model="searchForm.teacherId"
              placeholder="请选择教师"
              clearable
              style="width: 200px"
              @change="handleSearch"
            >
              <el-option
                v-for="item in teachers"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><RefreshRight /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="teacherId" label="任课教师">
             <template #default="scope">
                {{ getTeacherName(scope.row.teacherId) }}
             </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" @close="handleDialogClose">
      <el-form
        ref="courseFormRef"
        :model="form"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="任课教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择教师" style="width: 100%">
            <el-option
              v-for="item in teachers"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Guide, Search, RefreshRight } from '@element-plus/icons-vue'
import { usePagination } from '@/composables/usePagination'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const teachers = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const courseFormRef = ref(null)

const { currentPage, pageSize, total, currentPageForDisplay, resetPage } = usePagination(10)

const searchForm = reactive({
  name: '',
  teacherId: null
})

const form = reactive({
  id: null,
  name: '',
  teacherId: null
})

const validateCourseName = async (rule, value, callback) => {
  if (!value || !value.trim()) {
    callback(new Error('请输入课程名称'))
    return
  }
  callback()
}

const formRules = {
  name: [
    { required: true, validator: validateCourseName, trigger: 'blur' }
  ],
  teacherId: [
    { required: true, message: '请选择任课教师', trigger: 'change' }
  ]
}

const getTeacherName = (id) => {
    const teacher = teachers.value.find(t => t.id === id)
    return teacher ? teacher.name : id
}

const fetchData = async () => {
  loading.value = true
  try {
    const [coursesRes, usersRes] = await Promise.all([
        request.get('/courses', {
          params: {
            page: currentPage.value,
            size: pageSize.value,
            name: searchForm.name || undefined,
            teacherId: searchForm.teacherId || undefined
          }
        }),
        request.get('/users')
    ])
    if (coursesRes && coursesRes.content !== undefined) {
      tableData.value = coursesRes.content
      total.value = coursesRes.totalElements
    } else {
      tableData.value = coursesRes
      total.value = coursesRes.length
    }
    teachers.value = usersRes.filter(u => ['TEACHER', 'HEAD_TEACHER'].includes(u.role))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  resetPage()
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.teacherId = null
  resetPage()
  fetchData()
}

const handlePageChange = (val) => {
  currentPageForDisplay.value = val
  fetchData()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  resetPage()
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增课程'
  Object.assign(form, {
    id: null,
    name: '',
    teacherId: null
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑课程'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDialogClose = () => {
  courseFormRef.value?.resetFields()
}

const handleDelete = async (row) => {
  try {
    const impact = await request.get(`/courses/${row.id}/deletion-impact`)
    
    let confirmMessage = `确认删除课程【${row.name}】吗？`
    
    if (impact.gradeCount > 0) {
      confirmMessage = `
        课程【${row.name}】下存在 ${impact.gradeCount} 条关联的成绩记录。
        为保证数据完整性，该课程暂无法删除。
        请先删除或转移相关成绩数据后再操作。
      `
      ElMessageBox.alert(confirmMessage, '无法删除', {
        confirmButtonText: '我知道了',
        type: 'warning',
        dangerouslyUseHTMLString: true
      })
    } else {
      ElMessageBox.confirm(confirmMessage, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await request.delete(`/courses/${row.id}`)
        ElMessage.success('删除成功')
        fetchData()
      }).catch(() => {})
    }
  } catch (error) {
    // Error handled by interceptor
  }
}

const goToTeacherOverview = () => {
  router.push('/teacher-course-overview')
}

const handleSubmit = async () => {
  if (!courseFormRef.value) return
  
  try {
    await courseFormRef.value.validate()
  } catch (error) {
    return
  }
  
  try {
    if (form.id) {
      await request.put(`/courses/${form.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/courses', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    // Error handled by interceptor
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.course-list-container {
    min-height: 100%;
    padding: 20px;
}
.main-card {
    border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
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

.search-filter-bar {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.search-filter-bar :deep(.el-form-item) {
  margin-bottom: 0;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>

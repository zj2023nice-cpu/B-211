<template>
  <div class="announcement-manage-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>公告管理</span>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增公告</el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="重要" value="IMPORTANT" />
            <el-option label="通知" value="NOTICE" />
            <el-option label="消息" value="INFO" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="上线" :value="true" />
            <el-option label="下线" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe max-height="600">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)" size="small">{{ getTypeName(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status ? 'success' : 'info'" size="small">
              {{ scope.row.status ? '上线' : '下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="createdAt" label="创建时间" width="170" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              :type="scope.row.status ? 'danger' : 'success'" 
              link 
              size="small" 
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status ? '下线' : '上线' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择公告类型" style="width: 100%">
            <el-option label="重要" value="IMPORTANT" />
            <el-option label="通知" value="NOTICE" />
            <el-option label="消息" value="INFO" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="公告详情" width="600px">
      <el-descriptions :column="1" border v-if="currentView">
        <el-descriptions-item label="标题">{{ currentView.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getTypeTagType(currentView.type)" size="small">{{ getTypeName(currentView.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentView.status ? 'success' : 'info'" size="small">
            {{ currentView.status ? '上线' : '下线' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="排序">{{ currentView.sortOrder }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentView.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentView.updatedAt) }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentView.content || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { usePagination } from '@/composables/usePagination'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const currentView = ref(null)

const { currentPage, pageSize, total, currentPageForDisplay, resetPage } = usePagination(10)

const searchForm = reactive({
  title: '',
  type: '',
  status: null,
  dateRange: []
})

const form = reactive({
  id: null,
  title: '',
  type: 'NOTICE',
  content: '',
  sortOrder: 0
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }]
}

const getTypeName = (type) => {
  const map = { 'IMPORTANT': '重要', 'NOTICE': '通知', 'INFO': '消息' }
  return map[type] || type || '-'
}

const getTypeTagType = (type) => {
  const map = { 'IMPORTANT': 'danger', 'NOTICE': 'primary', 'INFO': 'success' }
  return map[type] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (searchForm.title) {
      params.title = searchForm.title
    }
    if (searchForm.type) {
      params.type = searchForm.type
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const res = await request.get('/announcements', { params })
    if (res && res.content !== undefined) {
      tableData.value = res.content
      total.value = res.totalElements
    }
  } finally {
    loading.value = false
  }
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

const handleSearch = () => {
  resetPage()
  fetchData()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.type = ''
  searchForm.status = null
  searchForm.dateRange = []
  resetPage()
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增公告'
  Object.assign(form, {
    id: null,
    title: '',
    type: 'NOTICE',
    content: '',
    sortOrder: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑公告'
  Object.assign(form, {
    id: row.id,
    title: row.title,
    type: row.type,
    content: row.content,
    sortOrder: row.sortOrder || 0
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  currentView.value = row
  viewDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await request.put(`/announcements/${form.id}`, form)
        ElMessage.success('更新成功')
      } else {
        await request.post('/announcements', form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } finally {
      submitting.value = false
    }
  })
}

const handleToggleStatus = (row) => {
  const action = row.status ? '下线' : '上线'
  ElMessageBox.confirm(`确定要${action}该公告吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.patch(`/announcements/${row.id}/status`, null, {
      params: { status: !row.status }
    })
    ElMessage.success(`${action}成功`)
    fetchData()
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/announcements/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.announcement-manage-container {
  padding: 20px;
  min-height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left span {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  font-family: 'Montserrat', sans-serif;
}

.search-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: var(--bg-color);
  border-radius: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

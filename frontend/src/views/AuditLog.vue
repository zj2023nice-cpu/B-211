<template>
  <div class="audit-log-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>审计日志</span>
          </div>
          <div class="header-right">
            <el-button type="success" @click="handleExport" :icon="Download" :loading="exporting">导出</el-button>
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
            <el-button @click="handleReset" :icon="Refresh">重置</el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="searchForm.module" placeholder="全部模块" clearable style="width: 150px">
            <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作">
          <el-select v-model="searchForm.action" placeholder="全部操作" clearable style="width: 150px">
            <el-option v-for="item in actionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="成功" :value="true" />
            <el-option label="失败" :value="false" />
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
      </el-form>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe max-height="600">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="操作用户" width="120" align="center">
          <template #default="scope">
            {{ scope.row.username || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="userRole" label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.userRole)" size="small">{{ getRoleName(scope.row.userRole) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="100" align="center" />
        <el-table-column prop="action" label="操作" width="80" align="center">
          <template #default="scope">
            <el-tag :type="getActionType(scope.row.action)" size="small">{{ scope.row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方法" width="90" align="center">
          <template #default="scope">
            <el-tag :type="getRequestMethodType(scope.row.requestMethod)" size="small">{{ scope.row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestPath" label="请求路径" min-width="150" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" align="center">
          <template #default="scope">
            {{ scope.row.ipAddress || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status ? 'success' : 'danger'" size="small">
              {{ scope.row.status ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="170" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="详情" width="80" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleViewDetail(scope.row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="日志详情" width="60%" v-loading="detailLoading">
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="日志ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ currentDetail.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentDetail.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户角色">
          <el-tag :type="getRoleType(currentDetail.userRole)" size="small">{{ getRoleName(currentDetail.userRole) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentDetail.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentDetail.action || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentDetail.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">
          <el-tag :type="getRequestMethodType(currentDetail.requestMethod)" size="small">{{ currentDetail.requestMethod || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求路径">{{ currentDetail.requestPath || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentDetail.ipAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <el-input
            v-if="currentDetail.requestParams"
            type="textarea"
            :rows="3"
            :model-value="formatJson(currentDetail.requestParams)"
            readonly
          />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作结果">
          <el-tag :type="currentDetail.status ? 'success' : 'danger'" size="small">
            {{ currentDetail.status ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" v-if="currentDetail.responseResult">
          <el-input
            type="textarea"
            :rows="3"
            :model-value="formatJson(currentDetail.responseResult)"
            readonly
          />
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentDetail.errorMessage">
          <el-input
            type="textarea"
            :rows="2"
            :model-value="currentDetail.errorMessage"
            readonly
            style="background-color: #fef0f0"
          />
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatDateTime(currentDetail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="用户代理">{{ currentDetail.userAgent || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import axios from 'axios'

const loading = ref(false)
const exporting = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const currentDetail = ref(null)

const currentPage = ref(0)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  username: '',
  module: '',
  action: '',
  status: null,
  dateRange: []
})

const moduleOptions = [
  { label: '认证', value: '认证' },
  { label: '用户管理', value: '用户管理' },
  { label: '课程管理', value: '课程管理' },
  { label: '成绩管理', value: '成绩管理' }
]

const actionOptions = [
  { label: '登录', value: '登录' },
  { label: '新增', value: '新增' },
  { label: '修改', value: '修改' },
  { label: '删除', value: '删除' },
  { label: '导入', value: '导入' }
]

const currentPageForDisplay = computed({
  get: () => currentPage.value + 1,
  set: (val) => {
    currentPage.value = val - 1
  }
})

const getRoleName = (role) => {
  const map = { 'ADMIN': '管理员', 'TEACHER': '教师', 'HEAD_TEACHER': '班主任', 'STUDENT': '学生' }
  return map[role] || role || '-'
}

const getRoleType = (role) => {
  const map = { 'ADMIN': 'danger', 'TEACHER': 'warning', 'HEAD_TEACHER': 'primary', 'STUDENT': 'success' }
  return map[role] || 'info'
}

const getActionType = (action) => {
  const map = { 
    '登录': 'primary', 
    '新增': 'success', 
    '修改': 'warning', 
    '删除': 'danger',
    '导入': 'info'
  }
  return map[action] || ''
}

const getRequestMethodType = (method) => {
  const map = { 'GET': 'info', 'POST': 'success', 'PUT': 'warning', 'DELETE': 'danger' }
  return map[method] || ''
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
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatJson = (str) => {
  if (!str) return ''
  try {
    const obj = JSON.parse(str)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return str
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (searchForm.username) {
      params.username = searchForm.username
    }
    if (searchForm.module) {
      params.module = searchForm.module
    }
    if (searchForm.action) {
      params.action = searchForm.action
    }
    if (searchForm.status !== null) {
      params.status = searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const res = await request.get('/audit-logs', { params })
    if (res && res.content !== undefined) {
      tableData.value = res.content
      total.value = res.totalElements
    } else {
      tableData.value = res
      total.value = res.length
    }
  } finally {
    loading.value = false
  }
}

const handlePageChange = (val) => {
  currentPage.value = val - 1
  fetchData()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 0
  fetchData()
}

const handleSearch = () => {
  currentPage.value = 0
  fetchData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.module = ''
  searchForm.action = ''
  searchForm.status = null
  searchForm.dateRange = []
  currentPage.value = 0
  fetchData()
}

const handleExport = async () => {
  exporting.value = true
  try {
    const params = {}
    
    if (searchForm.username) {
      params.username = searchForm.username
    }
    if (searchForm.module) {
      params.module = searchForm.module
    }
    if (searchForm.action) {
      params.action = searchForm.action
    }
    if (searchForm.status !== null) {
      params.status = searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const userStr = localStorage.getItem('user')
    const headers = {}
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        if (user.id) headers['X-User-Id'] = user.id
        if (user.username) headers['X-Username'] = user.username
        if (user.role) headers['X-User-Role'] = user.role
      } catch (e) {}
    }

    const response = await axios.get('/api/audit-logs/export', {
      params,
      headers,
      responseType: 'blob'
    })

    const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    const disposition = response.headers['content-disposition']
    let fileName = 'audit_logs.csv'
    if (disposition) {
      const matches = disposition.match(/filename="?([^"]+)"?/)
      if (matches && matches[1]) {
        fileName = matches[1]
      }
    }
    
    link.setAttribute('href', url)
    link.setAttribute('download', fileName)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}

const detailLoading = ref(false)

const handleViewDetail = async (row) => {
  detailLoading.value = true
  try {
    const res = await request.get(`/audit-logs/${row.id}`)
    currentDetail.value = res
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败，请稍后重试')
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.audit-log-container {
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

.header-right {
    display: flex;
    align-items: center;
    gap: 10px;
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

:deep(.el-descriptions__label) {
    width: 120px;
}
</style>

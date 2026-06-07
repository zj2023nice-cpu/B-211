<template>
  <div class="user-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>用户管理</span>
          </div>
          <div class="header-right">
             <el-radio-group v-model="viewMode" size="small" style="margin-right: 15px">
                <el-radio-button label="table"><el-icon><finished /></el-icon> 列表</el-radio-button>
                <el-radio-button label="card"><el-icon><grid /></el-icon> 卡片</el-radio-button>
             </el-radio-group>
             <el-button @click="handleDownloadTemplate" :icon="Download" style="margin-right: 10px">下载模板</el-button>
             <el-button type="success" @click="handleImport" :icon="Upload" style="margin-right: 10px">批量导入</el-button>
             <el-button type="primary" @click="handleAdd" :icon="Plus">新增用户</el-button>
          </div>
        </div>
      </template>
      
      <!-- Table View -->
      <el-table v-if="viewMode === 'table'" :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" align="center" />
        <el-table-column prop="name" label="姓名" align="center" />
        <el-table-column prop="role" label="角色" align="center">
            <template #default="scope">
                <el-tag :type="getRoleType(scope.row.role)" effect="light">{{ getRoleName(scope.row.role) }}</el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" align="center">
            <template #default="scope">
                {{ scope.row.className || '-' }}
            </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系方式" align="center">
             <template #default="scope">
                {{ scope.row.contact || '-' }}
            </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" :icon="Key" @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Card View -->
      <div v-else v-loading="loading" class="card-view-container">
        <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="user in tableData" :key="user.id" style="margin-bottom: 20px">
                <el-card shadow="hover" class="user-card">
                    <div class="user-card-header">
                        <el-avatar :size="50" :style="{ backgroundColor: getRoleColor(user.role) }">
                            {{ user.name ? user.name.charAt(0) : user.username.charAt(0) }}
                        </el-avatar>
                        <div class="user-info-basic">
                            <div class="user-name">{{ user.name || user.username }}</div>
                            <el-tag size="small" :type="getRoleType(user.role)">{{ getRoleName(user.role) }}</el-tag>
                        </div>
                    </div>
                    <div class="user-card-content">
                        <p><el-icon><User /></el-icon> 用户名: {{ user.username }}</p>
                        <p v-if="user.className"><el-icon><School /></el-icon> 班级: {{ user.className }}</p>
                        <p><el-icon><Phone /></el-icon> 联系: {{ user.contact || '未填写' }}</p>
                    </div>
                    <div class="user-card-actions">
                        <el-button type="primary" link :icon="Edit" @click="handleEdit(user)">编辑</el-button>
                        <el-button type="warning" link :icon="Key" @click="handleResetPassword(user)">重置密码</el-button>
                        <el-button type="danger" link :icon="Delete" @click="handleDelete(user)">删除</el-button>
                    </div>
                </el-card>
            </el-col>
        </el-row>
      </div>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px" class="user-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
            <el-input v-model="form.password" placeholder="留空则不修改(新增时默认为123456)" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role" size="default">
            <el-radio-button label="ADMIN">管理员</el-radio-button>
            <el-radio-button label="TEACHER">教师</el-radio-button>
            <el-radio-button label="HEAD_TEACHER">班主任</el-radio-button>
            <el-radio-button label="STUDENT">学生</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="班级" v-if="['STUDENT', 'HEAD_TEACHER'].includes(form.role)">
          <el-input v-model="form.className" placeholder="例如: 计算机2023-1班" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contact" placeholder="手机号或邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="resetPasswordDialogVisible" title="重置用户密码" width="450px">
      <el-alert
        title="不输入新密码则重置为默认密码 123456"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      />
      <el-form :model="resetPasswordForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input :value="resetPasswordTargetUser.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input :value="resetPasswordTargetUser.name" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetPasswordForm.newPassword" placeholder="请输入新密码，留空则使用默认密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleResetPasswordSubmit">确定重置</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="批量导入用户" width="600px">
      <el-alert
        title="请下载模板并按照格式填写后上传，默认密码为 123456"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      />
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".csv"
        :on-exceed="handleExceed"
        :on-change="handleFileChange"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 CSV 格式文件，角色可选值：ADMIN、TEACHER、HEAD_TEACHER、STUDENT
          </div>
        </template>
      </el-upload>

      <div v-if="importResult" class="import-result">
        <el-divider>导入结果</el-divider>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="总数">{{ importResult.total }}</el-descriptions-item>
          <el-descriptions-item label="成功">
            <span style="color: #67c23a">{{ importResult.successCount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败">
            <span style="color: #f56c6c">{{ importResult.failCount }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="importResult.errors && importResult.errors.length > 0" style="margin-top: 20px">
          <el-table :data="importResult.errors" border stripe size="small">
            <el-table-column prop="rowNumber" label="行号" width="80" align="center" />
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="errorMessage" label="错误信息" />
          </el-table>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">关闭</el-button>
          <el-button type="primary" :loading="importing" @click="handleImportSubmit">开始导入</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Plus, Edit, Delete, School, Phone, Finished, Grid, Upload, Download, UploadFilled, Key } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const viewMode = ref('table')

const currentPage = ref(0)
const pageSize = ref(10)
const total = ref(0)

const currentPageForDisplay = computed({
  get: () => currentPage.value + 1,
  set: (val) => {
    currentPage.value = val - 1
  }
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  role: 'STUDENT',
  name: '',
  className: '',
  contact: ''
})

const importDialogVisible = ref(false)
const importing = ref(false)
const uploadRef = ref(null)
const selectedFile = ref(null)
const importResult = ref(null)

const resetPasswordDialogVisible = ref(false)
const resetPasswordTargetUser = reactive({
  id: null,
  username: '',
  name: ''
})
const resetPasswordForm = reactive({
  newPassword: ''
})

const getRoleName = (role) => {
    const map = { 'ADMIN': '管理员', 'TEACHER': '教师', 'HEAD_TEACHER': '班主任', 'STUDENT': '学生' }
    return map[role] || role
}

const getRoleType = (role) => {
    const map = { 'ADMIN': 'danger', 'TEACHER': 'warning', 'HEAD_TEACHER': 'primary', 'STUDENT': 'success' }
    return map[role] || 'info'
}

const getRoleColor = (role) => {
     const map = { 
         'ADMIN': '#EF4444', // Red 500
         'TEACHER': '#F59E0B', // Amber 500
         'HEAD_TEACHER': '#4F46E5', // Indigo 600
         'STUDENT': '#10B981' // Emerald 500
     }
     return map[role] || '#9CA3AF' // Gray 400
}


const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/users', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
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

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    role: 'STUDENT',
    name: '',
    className: '',
    contact: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该用户吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/users/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleResetPassword = (row) => {
  Object.assign(resetPasswordTargetUser, {
    id: row.id,
    username: row.username,
    name: row.name
  })
  resetPasswordForm.newPassword = ''
  resetPasswordDialogVisible.value = true
}

const handleResetPasswordSubmit = async () => {
  try {
    const payload = {}
    if (resetPasswordForm.newPassword && resetPasswordForm.newPassword.trim()) {
      payload.newPassword = resetPasswordForm.newPassword
    }
    await request.put(`/users/${resetPasswordTargetUser.id}/reset-password`, payload)
    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
  } catch (error) {
    // Error handled
  }
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await request.put(`/users/${form.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/users', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    // Error handled
  }
}

const handleDownloadTemplate = () => {
  window.open('/api/users/template', '_blank')
}

const handleImport = () => {
  importDialogVisible.value = true
  selectedFile.value = null
  importResult.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleImportSubmit = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const result = await request.post('/users/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    importResult.value = result
    fetchData()
  } catch (error) {
    if (error.responseData) {
      importResult.value = error.responseData.data
    }
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-list-container {
  padding: 20px;
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
}

.card-view-container {
    padding: 10px 0;
}

.user-card {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: none;
    background: var(--surface-color);
    border-radius: 12px;
    overflow: hidden;
}

.user-card:hover {
    transform: translateY(-5px);
    box-shadow: var(--shadow-lg);
}

.user-card-header {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid var(--bg-color);
}

.user-info-basic {
    margin-left: 15px;
}

.user-name {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 5px;
    font-family: 'Roboto', sans-serif;
}

.user-card-content p {
    font-size: 14px;
    color: var(--text-secondary);
    margin: 8px 0;
    display: flex;
    align-items: center;
}

.user-card-content .el-icon {
    margin-right: 8px;
    color: var(--text-light);
}

.user-card-actions {
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid var(--bg-color);
    display: flex;
    justify-content: flex-end;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

.import-result {
    margin-top: 20px;
}

.upload-demo {
    margin-bottom: 20px;
}
</style>

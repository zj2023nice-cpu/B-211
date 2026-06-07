<template>
  <div class="term-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>学期管理</span>
          </div>
          <div class="header-right">
            <el-select v-model="filterEnabled" placeholder="筛选状态" clearable style="width: 120px; margin-right: 10px">
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
            <el-input v-model="filterName" placeholder="搜索学期名称" style="width: 150px; margin-right: 10px" clearable />
            <el-button type="primary" @click="handleAdd">新增学期</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="学期名称" align="center" sortable width="150" />
        <el-table-column prop="startDate" label="开始日期" align="center" sortable width="120">
          <template #default="scope">
            {{ scope.row.startDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" align="center" sortable width="120">
          <template #default="scope">
            {{ scope.row.endDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序权重" align="center" sortable width="100" />
        <el-table-column prop="enabled" label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" effect="plain">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" align="center" sortable width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="学期名称" prop="name">
          <el-input v-model="form.name" placeholder="例如: 2024-Spring" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序权重" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
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
import { ref, onMounted, reactive, computed, watch } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const filterName = ref('')
const filterEnabled = ref(null)

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
  name: '',
  startDate: null,
  endDate: null,
  enabled: true,
  sortOrder: 0
})

const rules = {
  name: [
    { required: true, message: '请输入学期名称', trigger: 'blur' }
  ]
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (filterName.value) {
      params.name = filterName.value
    }
    if (filterEnabled.value !== null) {
      params.enabled = filterEnabled.value
    }
    
    const res = await request.get('/terms', { params })
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

watch([filterName, filterEnabled], () => {
  currentPage.value = 0
  fetchData()
})

const handleAdd = () => {
  dialogTitle.value = '新增学期'
  Object.assign(form, {
    id: null,
    name: '',
    startDate: null,
    endDate: null,
    enabled: true,
    sortOrder: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑学期'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    startDate: row.startDate,
    endDate: row.endDate,
    enabled: row.enabled,
    sortOrder: row.sortOrder || 0
  })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该学期吗？删除后不会影响已有的成绩数据。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/terms/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/terms/${form.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/terms', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.term-manage-container {
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

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>

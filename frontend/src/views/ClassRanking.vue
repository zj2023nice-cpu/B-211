<template>
  <div class="class-ranking-container">
    <el-card shadow="hover" class="ranking-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Trophy /></el-icon>
            <span class="header-title">班级总分排行榜</span>
          </div>
          <div class="ranking-controls">
            <el-select v-model="selectedTerm" placeholder="选择学期" clearable style="width: 150px" @change="fetchRanking">
              <el-option v-for="term in termOptions" :key="term" :label="term" :value="term" />
            </el-select>
            <el-select v-model="selectedClass" placeholder="选择班级" clearable style="width: 150px" @change="fetchRanking">
              <el-option v-for="cls in classOptions" :key="cls" :label="cls" :value="cls" />
            </el-select>
            <el-button type="success" :icon="Download" @click="exportData">导出排行榜</el-button>
          </div>
        </div>
      </template>

      <el-table :data="rankingData" style="width: 100%" v-loading="loading" stripe border height="500">
        <el-table-column prop="rank" label="排名" align="center" width="100" fixed>
          <template #default="scope">
            <div class="rank-cell">
              <el-tag v-if="scope.row.rank <= 3" :type="getRankTagType(scope.row.rank)" effect="dark" size="large" class="rank-tag">
                {{ scope.row.rank }}
              </el-tag>
              <span v-else class="rank-text">{{ scope.row.rank }}</span>
              <span v-if="scope.row.isTied" class="tied-badge">并列</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生姓名" align="center" width="150" />
        <el-table-column prop="className" label="班级" align="center" width="150">
          <template #default="scope">
            <el-tag effect="plain">{{ scope.row.className }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" align="center" width="120" sortable :sort-method="(a, b) => b.totalScore - a.totalScore">
          <template #default="scope">
            <span class="total-score">{{ scope.row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="averageScore" label="平均分" align="center" width="120" sortable :sort-method="(a, b) => b.averageScore - a.averageScore">
          <template #default="scope">
            <span :class="getAvgScoreClass(scope.row.averageScore)">{{ scope.row.averageScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="courseCount" label="参考科目数" align="center" width="120">
          <template #default="scope">
            <el-tag type="info" effect="plain">{{ scope.row.courseCount }} 门</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="ranking-summary" v-if="rankingData.length > 0">
        <el-statistic title="参与排名人数" :value="rankingData.length" />
        <el-divider direction="vertical" />
        <el-statistic title="最高分" :value="maxScore" :precision="2" />
        <el-divider direction="vertical" />
        <el-statistic title="最低分" :value="minScore" :precision="2" />
        <el-divider direction="vertical" />
        <el-statistic title="班级平均分" :value="classAverage" :precision="2" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { Trophy, Download } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const rankingData = ref([])
const termOptions = ref([])
const classOptions = ref([])
const selectedTerm = ref('')
const selectedClass = ref('')

const maxScore = computed(() => {
  if (rankingData.value.length === 0) return 0
  return Math.max(...rankingData.value.map(r => r.totalScore))
})

const minScore = computed(() => {
  if (rankingData.value.length === 0) return 0
  return Math.min(...rankingData.value.map(r => r.totalScore))
})

const classAverage = computed(() => {
  if (rankingData.value.length === 0) return 0
  const sum = rankingData.value.reduce((acc, r) => acc + r.averageScore, 0)
  return (sum / rankingData.value.length).toFixed(2)
})

const getRankTagType = (rank) => {
  if (rank === 1) return 'danger'
  if (rank === 2) return 'warning'
  if (rank === 3) return 'success'
  return 'info'
}

const getAvgScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score < 60) return 'score-fail'
  return ''
}

const fetchTerms = async () => {
  try {
    const res = await request.get('/grades/ranking/terms')
    termOptions.value = res
    if (res.length > 0) {
      selectedTerm.value = res[0]
    }
  } catch (e) {
    console.error('获取学期列表失败', e)
  }
}

const fetchClasses = async () => {
  try {
    const res = await request.get('/grades/ranking/classes')
    classOptions.value = res
  } catch (e) {
    console.error('获取班级列表失败', e)
  }
}

const fetchRanking = async () => {
  loading.value = true
  try {
    const params = {}
    if (selectedTerm.value) {
      params.term = selectedTerm.value
    }
    if (selectedClass.value) {
      params.className = selectedClass.value
    }
    const res = await request.get('/grades/ranking', { params })
    rankingData.value = res
  } catch (e) {
    console.error('获取排行榜失败', e)
  } finally {
    loading.value = false
  }
}

const exportData = () => {
  const header = ['排名', '学生姓名', '班级', '总分', '平均分', '参考科目数', '是否并列']
  const data = rankingData.value.map(row => [
    row.rank,
    row.studentName,
    row.className,
    row.totalScore,
    row.averageScore,
    row.courseCount,
    row.isTied ? '是' : '否'
  ])

  const csvContent = [
    header.join(','),
    ...data.map(row => row.join(','))
  ].join('\n')

  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  const suffix = selectedClass.value ? `_${selectedClass.value}` : ''
  const termSuffix = selectedTerm.value ? `_${selectedTerm.value}` : ''
  link.download = `班级总分排行榜${termSuffix}${suffix}_${new Date().toLocaleDateString()}.csv`
  link.click()
}

onMounted(async () => {
  await Promise.all([fetchTerms(), fetchClasses()])
  fetchRanking()
})
</script>

<style scoped>
.class-ranking-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ranking-card {
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

.ranking-controls {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.rank-tag {
  font-weight: bold;
  min-width: 36px;
}

.rank-text {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-secondary);
}

.tied-badge {
  font-size: 10px;
  padding: 2px 6px;
  background-color: #FEF3C7;
  color: #D97706;
  border-radius: 4px;
  font-weight: 500;
}

.total-score {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
  font-family: 'Roboto', sans-serif;
}

.score-excellent {
  color: #10B981;
  font-weight: 600;
  font-family: 'Roboto', sans-serif;
}

.score-fail {
  color: #EF4444;
  font-weight: 600;
  font-family: 'Roboto', sans-serif;
}

.ranking-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 20px;
  margin-top: 20px;
  background: linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 100%);
  border-radius: 8px;
}
</style>

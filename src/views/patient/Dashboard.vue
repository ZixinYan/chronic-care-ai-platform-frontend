<template>
  <div class="patient-dashboard">
    <el-row :gutter="20">
      <el-col :span="16">
        <div class="card">
          <div class="card-header">
            <span class="card-title">我的健康概览</span>
            <el-button type="primary" size="small" @click="$router.push('/patient/health-report/upload')">
              上传报告
            </el-button>
          </div>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="health-item">
                <div class="health-label">最新血糖</div>
                <div class="health-value">{{ healthData.bloodSugar || '--' }} <span class="unit">mmol/L</span></div>
                <el-tag :type="healthData.bloodSugarStatus" size="small">{{ healthData.bloodSugarText || '暂无数据' }}</el-tag>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="health-item">
                <div class="health-label">最新血压</div>
                <div class="health-value">{{ healthData.bloodPressure || '--' }} <span class="unit">mmHg</span></div>
                <el-tag :type="healthData.bloodPressureStatus" size="small">{{ healthData.bloodPressureText || '暂无数据' }}</el-tag>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="health-item">
                <div class="health-label">健康评分</div>
                <div class="health-value">{{ healthData.score || '--' }} <span class="unit">分</span></div>
                <el-tag :type="healthData.scoreStatus" size="small">{{ healthData.scoreText || '暂无数据' }}</el-tag>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="card mt-20">
          <div class="card-header">
            <span class="card-title">健康报告列表</span>
            <el-button type="primary" link @click="$router.push('/patient/health-report')">
              查看全部
            </el-button>
          </div>
          <el-table :data="reportList" stripe style="width: 100%">
            <el-table-column prop="title" label="报告标题" min-width="150" />
            <el-table-column prop="reportType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getReportTypeTag(row.reportType)">
                  {{ getReportTypeText(row.reportType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="uploadTime" label="上传时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewReport(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="card">
          <div class="card-header">
            <span class="card-title">待办事项</span>
          </div>
          <div class="todo-list">
            <div v-for="item in todoList" :key="item.id" class="todo-item">
              <el-checkbox v-model="item.completed" @change="handleTodoChange(item)">
                {{ item.title }}
              </el-checkbox>
              <span class="todo-time">{{ item.time }}</span>
            </div>
            <el-empty v-if="todoList.length === 0" description="暂无待办事项" />
          </div>
        </div>

        <div class="card mt-20">
          <div class="card-header">
            <span class="card-title">AI健康建议</span>
          </div>
          <div class="ai-suggestion">
            <el-icon class="ai-icon"><MagicStick /></el-icon>
            <p>{{ aiSuggestion }}</p>
            <el-button type="primary" size="small" @click="getMoreSuggestion">
              获取更多建议
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import healthReportApi from '@/api/healthReport'

const router = useRouter()
const userStore = useUserStore()

const healthData = ref({
  bloodSugar: 5.6,
  bloodSugarStatus: 'success',
  bloodSugarText: '正常',
  bloodPressure: '120/80',
  bloodPressureStatus: 'success',
  bloodPressureText: '正常',
  score: 85,
  scoreStatus: 'success',
  scoreText: '良好'
})

const reportList = ref([])
const todoList = ref([
  { id: 1, title: '测量血糖', time: '08:00', completed: false },
  { id: 2, title: '服用药物', time: '09:00', completed: true },
  { id: 3, title: '记录饮食', time: '12:00', completed: false }
])

const aiSuggestion = ref('根据您最近的健康数据，建议您保持规律作息，适当增加运动量，控制饮食中的糖分摄入。')

const getReportTypeText = (type) => {
  const types = { 1: '图片', 2: '文字', 3: 'PDF' }
  return types[type] || '未知'
}

const getReportTypeTag = (type) => {
  const tags = { 1: 'primary', 2: 'success', 3: 'warning' }
  return tags[type] || 'info'
}

const getStatusText = (status) => {
  const statusTexts = { 0: '待审核', 1: '已审核', 2: '已驳回' }
  return statusTexts[status] || '未知'
}

const getStatusTag = (status) => {
  const tags = { 0: 'warning', 1: 'success', 2: 'danger' }
  return tags[status] || 'info'
}

const viewReport = (row) => {
  router.push(`/patient/health-report/detail/${row.id}`)
}

const handleTodoChange = (item) => {
  console.log('Todo changed:', item)
}

const getMoreSuggestion = () => {
  router.push('/ai/schedule')
}

const fetchReportList = async () => {
  try {
    const res = await healthReportApi.getReportList({ pageNum: 1, pageSize: 5 })
    if (res.code === 0) {
      reportList.value = res.data?.list || []
    }
  } catch (error) {
    console.error('获取报告列表失败:', error)
  }
}

onMounted(() => {
  fetchReportList()
})
</script>

<style lang="scss" scoped>
.patient-dashboard {
  padding: 0;
}

.health-item {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;

  .health-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 10px;
  }

  .health-value {
    font-size: 32px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;

    .unit {
      font-size: 14px;
      font-weight: normal;
      color: #909399;
    }
  }
}

.todo-list {
  .todo-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .todo-time {
      font-size: 12px;
      color: #909399;
    }
  }
}

.ai-suggestion {
  text-align: center;
  padding: 20px;

  .ai-icon {
    font-size: 48px;
    color: #409eff;
    margin-bottom: 16px;
  }

  p {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 16px;
  }
}
</style>

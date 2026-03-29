<template>
  <div class="patient-info">
    <div class="card">
      <div class="card-header">
        <span class="card-title">患者详情</span>
        <el-button @click="$router.back()">返回</el-button>
      </div>

      <el-descriptions title="基本信息" :column="3" border>
        <el-descriptions-item label="姓名">{{ patientInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ patientInfo.gender === 'MALE' ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ patientInfo.age }}岁</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ patientInfo.phone }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ patientInfo.idCard }}</el-descriptions-item>
        <el-descriptions-item label="健康状态">
          <el-tag :type="getHealthStatusTag(patientInfo.healthStatus)">{{ patientInfo.healthStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="诊断" :span="3">{{ patientInfo.diagnosis }}</el-descriptions-item>
        <el-descriptions-item label="过敏史" :span="3">{{ patientInfo.allergyHistory || '无' }}</el-descriptions-item>
        <el-descriptions-item label="家族病史" :span="3">{{ patientInfo.familyHistory || '无' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="card mt-20">
      <div class="card-header">
        <span class="card-title">健康数据</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="health-item">
            <div class="health-label">最新血糖</div>
            <div class="health-value">{{ patientInfo.bloodSugar || '--' }} <span class="unit">mmol/L</span></div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="health-item">
            <div class="health-label">最新血压</div>
            <div class="health-value">{{ patientInfo.bloodPressure || '--' }} <span class="unit">mmHg</span></div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="health-item">
            <div class="health-label">最近就诊</div>
            <div class="health-value">{{ patientInfo.lastVisitTime || '--' }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="card mt-20">
      <div class="card-header">
        <span class="card-title">健康报告</span>
      </div>
      <el-table :data="reportList" stripe style="width: 100%">
        <el-table-column prop="title" label="报告标题" min-width="150" />
        <el-table-column prop="reportType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getReportTypeTag(row.reportType)">{{ getReportTypeText(row.reportType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewReport(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card mt-20">
      <div class="card-header">
        <span class="card-title">就诊记录</span>
      </div>
      <el-timeline>
        <el-timeline-item
          v-for="record in visitRecords"
          :key="record.id"
          :timestamp="record.visitTime"
          placement="top"
        >
          <el-card>
            <h4>{{ record.type }}</h4>
            <p>诊断结果：{{ record.diagnosis }}</p>
            <p>治疗方案：{{ record.treatment }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="visitRecords.length === 0" description="暂无就诊记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import healthReportApi from '@/api/healthReport'
import userApi from '@/api/user'

const route = useRoute()

const patientInfo = ref({
  id: null,
  name: '',
  gender: '',
  age: 0,
  phone: '',
  idCard: '',
  healthStatus: '',
  healthStatusText: '',
  diagnosis: '',
  allergyHistory: '',
  familyHistory: '',
  bloodSugar: '',
  bloodPressure: '',
  lastVisitTime: ''
})

const reportList = ref([])
const visitRecords = ref([])

const getHealthStatusTag = (status) => {
  const tags = { GOOD: 'success', NORMAL: 'warning', POOR: 'danger' }
  return tags[status] || 'info'
}

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
  console.log('查看报告:', row)
}

const fetchPatientInfo = async () => {
  const patientId = route.params.id
  if (!patientId) return

  try {
    const res = await userApi.getPatientInfo(patientId)
    if (res.code === 0) {
      patientInfo.value = res.data || {}
    }
  } catch (error) {
    console.error('获取患者信息失败:', error)
  }
}

const fetchReportList = async () => {
  const patientId = route.params.id
  if (!patientId) return

  try {
    const res = await healthReportApi.getReportList({ patientId, pageNum: 1, pageSize: 10 })
    if (res.code === 0) {
      reportList.value = res.data?.list || []
    }
  } catch (error) {
    console.error('获取报告列表失败:', error)
  }
}

onMounted(() => {
  fetchPatientInfo()
  fetchReportList()
})
</script>

<style lang="scss" scoped>
.patient-info {
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
    font-size: 24px;
    font-weight: 600;
    color: #303133;

    .unit {
      font-size: 14px;
      font-weight: normal;
      color: #909399;
    }
  }
}

.el-timeline {
  padding: 20px 0;

  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 10px;
  }

  p {
    font-size: 14px;
    color: #606266;
    margin-bottom: 5px;
  }
}
</style>

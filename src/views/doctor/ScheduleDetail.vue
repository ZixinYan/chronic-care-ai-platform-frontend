<template>
  <div class="schedule-detail">
    <div class="card">
      <div class="card-header">
        <span class="card-title">日程详情</span>
        <el-button @click="$router.back()">返回</el-button>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="日程日期">{{ scheduleDetail.scheduleDay }}</el-descriptions-item>
        <el-descriptions-item label="日程时间">{{ scheduleDetail.scheduleTime }}</el-descriptions-item>
        <el-descriptions-item label="患者姓名">{{ scheduleDetail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="日程类型">
          <el-tag :type="getScheduleTypeTag(scheduleDetail.type)">{{ scheduleDetail.typeText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(scheduleDetail.status)">{{ getStatusText(scheduleDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ scheduleDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="日程内容" :span="2">{{ scheduleDetail.content }}</el-descriptions-item>
        <el-descriptions-item v-if="scheduleDetail.cancelReason" label="取消原因" :span="2">
          {{ scheduleDetail.cancelReason }}
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="scheduleDetail.status === 'COMPLETED'" class="diagnosis-report mt-20">
        <h4>诊断报告</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="诊断结果">{{ scheduleDetail.diagnosisResult }}</el-descriptions-item>
          <el-descriptions-item label="治疗方案">{{ scheduleDetail.treatmentPlan }}</el-descriptions-item>
          <el-descriptions-item label="医嘱">{{ scheduleDetail.medicalAdvice }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-if="showCompleteForm" class="complete-form mt-20">
        <h4>完成日程 - 上传诊断报告</h4>
        <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="100px">
          <el-form-item label="诊断结果" prop="diagnosisResult">
            <el-input v-model="completeForm.diagnosisResult" type="textarea" :rows="3" placeholder="请输入诊断结果" />
          </el-form-item>
          <el-form-item label="治疗方案" prop="treatmentPlan">
            <el-input v-model="completeForm.treatmentPlan" type="textarea" :rows="3" placeholder="请输入治疗方案" />
          </el-form-item>
          <el-form-item label="医嘱" prop="medicalAdvice">
            <el-input v-model="completeForm.medicalAdvice" type="textarea" :rows="3" placeholder="请输入医嘱" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitLoading" @click="submitComplete">提交</el-button>
            <el-button @click="showCompleteForm = false">取消</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="!showCompleteForm && scheduleDetail.status !== 'COMPLETED' && scheduleDetail.status !== 'CANCELLED'" class="actions mt-20">
        <el-button v-if="scheduleDetail.status === 'PENDING'" type="primary" @click="startSchedule">开始处理</el-button>
        <el-button v-if="scheduleDetail.status === 'IN_PROGRESS'" type="success" @click="showCompleteForm = true">完成日程</el-button>
        <el-button type="danger" @click="cancelSchedule">取消日程</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import doctorApi from '@/api/doctor'

const route = useRoute()
const router = useRouter()
const submitLoading = ref(false)
const showCompleteForm = ref(false)
const completeFormRef = ref(null)

const scheduleDetail = ref({
  id: null,
  scheduleDay: '',
  scheduleTime: '',
  patientName: '',
  patientId: null,
  type: '',
  typeText: '',
  status: '',
  content: '',
  createTime: '',
  cancelReason: '',
  diagnosisResult: '',
  treatmentPlan: '',
  medicalAdvice: ''
})

const completeForm = reactive({
  diagnosisResult: '',
  treatmentPlan: '',
  medicalAdvice: ''
})

const completeRules = {
  diagnosisResult: [{ required: true, message: '请输入诊断结果', trigger: 'blur' }],
  treatmentPlan: [{ required: true, message: '请输入治疗方案', trigger: 'blur' }]
}

const getScheduleTypeTag = (type) => {
  const tags = { FOLLOW_UP: 'primary', CONSULTATION: 'success', EXAMINATION: 'warning', OTHER: 'info' }
  return tags[type] || 'info'
}

const getStatusTag = (status) => {
  const tags = { PENDING: 'warning', IN_PROGRESS: 'primary', COMPLETED: 'success', CANCELLED: 'info' }
  return tags[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { PENDING: '待处理', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }
  return texts[status] || '未知'
}

const fetchScheduleDetail = async () => {
  const scheduleId = route.params.id
  if (!scheduleId) return

  try {
    const res = await doctorApi.getScheduleDetail(scheduleId)
    if (res.code === 0) {
      scheduleDetail.value = res.data || {}
      
      if (route.query.action === 'complete' && scheduleDetail.value.status === 'IN_PROGRESS') {
        showCompleteForm.value = true
      }
    }
  } catch (error) {
    console.error('获取日程详情失败:', error)
  }
}

const startSchedule = async () => {
  try {
    const res = await doctorApi.updateScheduleStatus(scheduleDetail.value.id, 'IN_PROGRESS')
    if (res.code === 0) {
      ElMessage.success('已开始处理')
      scheduleDetail.value.status = 'IN_PROGRESS'
    }
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

const submitComplete = async () => {
  if (!completeFormRef.value) return

  await completeFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const res = await doctorApi.completeSchedule({
        scheduleId: scheduleDetail.value.id,
        ...completeForm
      })
      if (res.code === 0) {
        ElMessage.success('日程已完成')
        fetchScheduleDetail()
        showCompleteForm.value = false
      }
    } catch (error) {
      console.error('完成日程失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

const cancelSchedule = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消日程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入取消原因'
    })
    const res = await doctorApi.cancelSchedule(scheduleDetail.value.id, value)
    if (res.code === 0) {
      ElMessage.success('已取消')
      router.back()
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  fetchScheduleDetail()
})
</script>

<style lang="scss" scoped>
.schedule-detail {
  padding: 0;
}

.diagnosis-report,
.complete-form {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
  }
}

.actions {
  display: flex;
  gap: 12px;
}
</style>

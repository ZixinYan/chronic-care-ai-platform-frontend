<template>
  <div class="report-detail">
    <div class="card">
      <div class="card-header">
        <span class="card-title">报告详情</span>
        <el-button @click="$router.back()">返回</el-button>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="报告标题">{{ reportDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="报告类型">
          <el-tag :type="getReportTypeTag(reportDetail.reportType)">
            {{ getReportTypeText(reportDetail.reportType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报告分类">{{ reportDetail.category }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(reportDetail.status)">
            {{ getStatusText(reportDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ reportDetail.uploadTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ reportDetail.auditTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ reportDetail.remark || '--' }}</el-descriptions-item>
      </el-descriptions>

      <div class="report-content mt-20">
        <h4>报告内容</h4>
        <div v-if="reportDetail.reportType === 1 || reportDetail.reportType === 3" class="file-preview">
          <el-image
            v-if="reportDetail.reportType === 1"
            :src="reportDetail.fileUrl"
            :preview-src-list="[reportDetail.fileUrl]"
            fit="contain"
            class="preview-image"
          />
          <div v-else class="pdf-preview">
            <el-icon class="pdf-icon"><Document /></el-icon>
            <p>{{ reportDetail.fileName }}</p>
            <el-button type="primary" @click="downloadFile">下载查看</el-button>
          </div>
        </div>
        <div v-if="reportDetail.reportType === 2" class="text-content">
          <pre>{{ reportDetail.textContent }}</pre>
        </div>
      </div>

      <div v-if="reportDetail.status === 0" class="doctor-section mt-20">
        <div v-if="reportDetail.doctorName && reportDetail.scheduleId" class="current-doctor">
          <h4>
            <el-icon><User /></el-icon>
            当前审核医生
          </h4>
          <div class="doctor-info-card">
            <el-avatar :size="50">{{ reportDetail.doctorName?.charAt(0) }}</el-avatar>
            <div class="doctor-detail">
              <div class="doctor-name">{{ reportDetail.doctorName }}</div>
              <div class="doctor-status">已接收审核请求，等待审核中...</div>
            </div>
            <el-button type="primary" link @click="showSwitchDoctorDialog">
              切换医生
            </el-button>
          </div>
        </div>

        <div v-else class="recommended-doctors">
          <h4>
            <el-icon><User /></el-icon>
            AI推荐医生
            <el-button 
              type="primary" 
              link 
              :loading="loadingDoctors"
              @click="fetchRecommendedDoctors"
            >
              刷新推荐
            </el-button>
          </h4>
          <el-alert
            v-if="aiRecommendation"
            :title="aiRecommendation"
            type="info"
            :closable="false"
            class="mb-16"
          />
          <div v-if="loadingDoctors" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在获取AI推荐医生...</span>
          </div>
          <div v-else-if="doctors.length > 0" class="doctor-list">
            <div 
              v-for="doctor in doctors" 
              :key="doctor.doctorId" 
              class="doctor-card"
              :class="{ 'selected': selectedDoctorId === doctor.doctorId }"
              @click="selectDoctor(doctor.doctorId)"
            >
              <el-avatar :size="60" :src="doctor.avatar || defaultAvatar">
                {{ doctor.doctorName?.charAt(0) }}
              </el-avatar>
              <div class="doctor-info">
                <div class="doctor-name">{{ doctor.doctorName }}</div>
                <div class="doctor-dept">{{ doctor.department }} · {{ doctor.title }}</div>
                <div class="doctor-recommendation">{{ doctor.recommendation }}</div>
              </div>
              <el-radio 
                v-model="selectedDoctorId" 
                :value="doctor.doctorId"
                @click.stop
              />
            </div>
          </div>
          <el-empty v-else-if="!loadingDoctors" description="暂无推荐医生" />
          
          <div v-if="doctors.length > 0" class="send-action mt-16">
            <el-button 
              type="primary" 
              :disabled="!selectedDoctorId"
              :loading="sending"
              @click="sendReportToDoctor"
            >
              发送报告给选中医生
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="reportDetail.status !== 0 && reportDetail.doctorName" class="assigned-doctor mt-20">
        <h4>审核医生</h4>
        <div class="doctor-info-card">
          <el-avatar :size="50">{{ reportDetail.doctorName?.charAt(0) }}</el-avatar>
          <div class="doctor-detail">
            <div class="doctor-name">{{ reportDetail.doctorName }}</div>
            <div class="doctor-status">{{ getStatusText(reportDetail.status) }}</div>
          </div>
        </div>
      </div>

      <div v-if="reportDetail.auditComment" class="audit-comment mt-20">
        <h4>审核意见</h4>
        <el-alert :title="reportDetail.auditComment" type="info" :closable="false" />
      </div>
    </div>

    <el-dialog
      v-model="switchDoctorDialogVisible"
      title="切换审核医生"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-alert
        type="warning"
        title="切换医生后，原医生的审核日程将被取消"
        :closable="false"
        class="mb-16"
      />
      <div v-if="loadingDoctors" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在获取AI推荐医生...</span>
      </div>
      <div v-else-if="doctors.length > 0" class="doctor-list">
        <div 
          v-for="doctor in doctors" 
          :key="doctor.doctorId" 
          class="doctor-card"
          :class="{ 'selected': selectedDoctorId === doctor.doctorId }"
          @click="selectDoctor(doctor.doctorId)"
        >
          <el-avatar :size="50" :src="doctor.avatar || defaultAvatar">
            {{ doctor.doctorName?.charAt(0) }}
          </el-avatar>
          <div class="doctor-info">
            <div class="doctor-name">{{ doctor.doctorName }}</div>
            <div class="doctor-dept">{{ doctor.department }} · {{ doctor.title }}</div>
          </div>
          <el-radio 
            v-model="selectedDoctorId" 
            :value="doctor.doctorId"
            @click.stop
          />
        </div>
      </div>
      <el-empty v-else description="暂无推荐医生" />
      <template #footer>
        <el-button @click="switchDoctorDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :disabled="!selectedDoctorId"
          :loading="sending"
          @click="switchDoctor"
        >
          确认切换
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, User, Loading } from '@element-plus/icons-vue'
import healthReportApi from '@/api/healthReport'

const route = useRoute()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const reportDetail = ref({
  id: null,
  title: '',
  reportType: 1,
  category: '',
  status: 0,
  uploadTime: '',
  auditTime: '',
  remark: '',
  fileUrl: '',
  fileName: '',
  auditComment: '',
  textContent: '',
  doctorName: '',
  attendingDoctorId: null,
  scheduleId: null
})

const doctors = ref([])
const selectedDoctorId = ref(null)
const loadingDoctors = ref(false)
const sending = ref(false)
const aiRecommendation = ref('')
const switchDoctorDialogVisible = ref(false)

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

const downloadFile = () => {
  if (reportDetail.value.fileUrl) {
    window.open(reportDetail.value.fileUrl, '_blank')
  }
}

const selectDoctor = (doctorId) => {
  selectedDoctorId.value = doctorId
}

const fetchReportDetail = async () => {
  const reportId = route.params.id
  if (!reportId) return

  try {
    const res = await healthReportApi.getReportDetail(reportId)
    if (res.code === 0) {
      const data = res.data?.report || {}
      reportDetail.value = {
        id: data.reportId,
        title: data.title || '',
        reportType: data.reportType || 1,
        category: data.category || '',
        status: data.status ?? 0,
        uploadTime: formatTime(data.createTime),
        auditTime: formatTime(data.updateTime),
        remark: data.description || '',
        fileUrl: data.fileUrl || '',
        fileName: data.title || '',
        auditComment: data.auditRemark || '',
        textContent: data.textContent || '',
        doctorName: data.doctorName || '',
        attendingDoctorId: data.attendingDoctorId || null,
        scheduleId: data.scheduleId || null
      }
      
      if (data.status === 0 && !data.scheduleId) {
        fetchRecommendedDoctors()
      }
    }
  } catch (error) {
    console.error('获取报告详情失败:', error)
  }
}

const fetchRecommendedDoctors = async () => {
  const reportId = route.params.id
  if (!reportId) return

  loadingDoctors.value = true
  doctors.value = []
  selectedDoctorId.value = null
  aiRecommendation.value = ''

  try {
    const res = await healthReportApi.getRecommendedDoctors(reportId)
    if (res.code === 0) {
      doctors.value = res.data?.doctors || []
      aiRecommendation.value = res.data?.aiRecommendation || ''
      if (doctors.value.length > 0) {
        selectedDoctorId.value = doctors.value[0].doctorId
      }
    } else {
      ElMessage.warning(res.message || '获取推荐医生失败')
    }
  } catch (error) {
    console.error('获取推荐医生失败:', error)
    ElMessage.error('获取推荐医生失败')
  } finally {
    loadingDoctors.value = false
  }
}

const showSwitchDoctorDialog = () => {
  switchDoctorDialogVisible.value = true
  fetchRecommendedDoctors()
}

const sendReportToDoctor = async () => {
  if (!selectedDoctorId.value) {
    ElMessage.warning('请先选择一位医生')
    return
  }

  const selectedDoctor = doctors.value.find(d => d.doctorId === selectedDoctorId.value)
  const doctorName = selectedDoctor?.doctorName || '该医生'

  try {
    await ElMessageBox.confirm(
      `确定要将报告发送给 ${doctorName} 医生审核吗？`,
      '确认发送',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    sending.value = true
    const res = await healthReportApi.sendReportToDoctor(reportDetail.value.id, selectedDoctorId.value)
    
    if (res.code === 0) {
      ElMessage.success('报告已成功发送给医生')
      fetchReportDetail()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发送报告失败:', error)
      ElMessage.error('发送报告失败')
    }
  } finally {
    sending.value = false
  }
}

const switchDoctor = async () => {
  if (!selectedDoctorId.value) {
    ElMessage.warning('请先选择一位医生')
    return
  }

  if (selectedDoctorId.value === reportDetail.value.attendingDoctorId) {
    ElMessage.warning('请选择不同的医生')
    return
  }

  const selectedDoctor = doctors.value.find(d => d.doctorId === selectedDoctorId.value)
  const doctorName = selectedDoctor?.doctorName || '该医生'

  try {
    await ElMessageBox.confirm(
      `确定要切换到 ${doctorName} 医生吗？原医生的审核日程将被取消。`,
      '确认切换',
      {
        confirmButtonText: '确定切换',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    sending.value = true
    const res = await healthReportApi.sendReportToDoctor(reportDetail.value.id, selectedDoctorId.value)
    
    if (res.code === 0) {
      ElMessage.success('已成功切换审核医生')
      switchDoctorDialogVisible.value = false
      fetchReportDetail()
    } else {
      ElMessage.error(res.message || '切换失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换医生失败:', error)
      ElMessage.error('切换医生失败')
    }
  } finally {
    sending.value = false
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  fetchReportDetail()
})
</script>

<style lang="scss" scoped>
.report-detail {
  padding: 0;
}

.report-content {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.file-preview {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 500px;
}

.pdf-preview {
  padding: 40px;

  .pdf-icon {
    font-size: 64px;
    color: #f56c6c;
    margin-bottom: 16px;
  }

  p {
    font-size: 14px;
    color: #606266;
    margin-bottom: 16px;
  }
}

.text-content {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  
  pre {
    white-space: pre-wrap;
    word-wrap: break-word;
    margin: 0;
    font-family: inherit;
    font-size: 14px;
    line-height: 1.6;
    color: #303133;
  }
}

.doctor-section {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.current-doctor {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #909399;
  gap: 8px;
}

.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.doctor-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
  }

  &.selected {
    border-color: #409eff;
    background-color: #ecf5ff;
  }
}

.doctor-info {
  flex: 1;
  margin-left: 16px;
}

.doctor-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.doctor-dept {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.doctor-recommendation {
  font-size: 12px;
  color: #67c23a;
}

.send-action {
  display: flex;
  justify-content: flex-end;
}

.assigned-doctor {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
  }
}

.doctor-info-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.doctor-detail {
  flex: 1;
  margin-left: 16px;
  
  .doctor-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
  
  .doctor-status {
    font-size: 13px;
    color: #67c23a;
    margin-top: 4px;
  }
}

.audit-comment {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
  }
}

.mt-16 {
  margin-top: 16px;
}

.mb-16 {
  margin-bottom: 16px;
}
</style>

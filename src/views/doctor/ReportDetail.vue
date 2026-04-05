<template>
  <div class="report-detail">
    <div class="card">
      <div class="card-header">
        <span class="card-title">报告详情</span>
        <el-button @click="$router.back()">返回</el-button>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="患者姓名">{{ reportDetail.patientName }}</el-descriptions-item>
        <el-descriptions-item label="报告类型">
          <el-tag :type="getReportTypeTag(reportDetail.reportType)">
            {{ getReportTypeText(reportDetail.reportType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报告分类">{{ reportDetail.categoryDesc || reportDetail.category }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(reportDetail.status)">
            {{ getStatusText(reportDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ reportDetail.uploadTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ reportDetail.auditTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="报告标题" :span="2">{{ reportDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="报告描述" :span="2">{{ reportDetail.description || '--' }}</el-descriptions-item>
      </el-descriptions>

      <div class="report-content-section mt-20">
        <h4>报告内容</h4>
        <div v-if="reportDetail.reportType === 1 || reportDetail.reportType === 3" class="file-preview">
          <el-image
            v-if="reportDetail.reportType === 1 && reportDetail.fileUrl"
            :src="reportDetail.fileUrl"
            :preview-src-list="[reportDetail.fileUrl]"
            fit="contain"
            class="preview-image"
          />
          <div v-else-if="reportDetail.reportType === 3 && reportDetail.fileUrl" class="pdf-preview">
            <el-icon class="pdf-icon"><Document /></el-icon>
            <p>{{ reportDetail.title }}</p>
            <el-button type="primary" @click="downloadFile(reportDetail.fileUrl)">下载查看</el-button>
          </div>
          <el-empty v-else description="暂无文件" />
        </div>
        <div v-if="reportDetail.reportType === 2" class="text-content">
          <pre>{{ reportDetail.textContent }}</pre>
        </div>
      </div>

      <div v-if="reportDetail.auditRemark" class="audit-remark mt-20">
        <h4>审核意见</h4>
        <el-alert :title="reportDetail.auditRemark" type="info" :closable="false" />
      </div>

      <div v-if="reportDetail.status === 0" class="approval-section mt-20">
        <h4>审批操作</h4>
        <el-form :model="approvalForm" :rules="approvalRules" ref="approvalFormRef" label-width="100px">
          <el-form-item label="审批意见" prop="comment">
            <el-input
              v-model="approvalForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请填写审批意见（必填）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button type="success" :loading="submitting" @click="handleApprove">
              通过
            </el-button>
            <el-button type="danger" :loading="submitting" @click="handleReject">
              拒绝
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import healthReportApi from '@/api/healthReport'

const route = useRoute()
const router = useRouter()

const reportDetail = ref({
  reportId: null,
  patientName: '',
  reportType: 1,
  category: '',
  categoryDesc: '',
  title: '',
  description: '',
  status: 0,
  uploadTime: '',
  auditTime: '',
  fileUrl: '',
  textContent: '',
  auditRemark: ''
})

const approvalForm = reactive({
  comment: ''
})

const approvalRules = {
  comment: [
    { required: true, message: '请填写审批意见', trigger: 'blur' },
    { min: 5, message: '审批意见至少5个字符', trigger: 'blur' }
  ]
}

const approvalFormRef = ref(null)
const submitting = ref(false)

const getReportTypeText = (type) => {
  const types = { 1: '图片报告', 2: '文字报告', 3: 'PDF报告' }
  return types[type] || '未知'
}

const getReportTypeTag = (type) => {
  const tags = { 1: 'primary', 2: 'success', 3: 'warning' }
  return tags[type] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return texts[status] || '未知'
}

const getStatusTag = (status) => {
  const tags = { 0: 'warning', 1: 'success', 2: 'danger' }
  return tags[status] || 'info'
}

const downloadFile = (url) => {
  if (url) {
    window.open(url, '_blank')
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

const fetchReportDetail = async () => {
  const reportId = route.params.id
  if (!reportId) {
    ElMessage.error('报告ID不存在')
    router.back()
    return
  }

  try {
    const res = await healthReportApi.getReportDetail(reportId)
    if (res.code === 0) {
      const data = res.data?.report || {}
      reportDetail.value = {
        reportId: data.reportId,
        patientName: data.patientName || '',
        reportType: data.reportType || 1,
        category: data.category || '',
        categoryDesc: data.categoryDesc || '',
        title: data.title || '',
        description: data.description || '',
        status: data.status ?? 0,
        uploadTime: formatTime(data.createTime),
        auditTime: formatTime(data.updateTime),
        fileUrl: data.fileUrl || '',
        textContent: data.textContent || '',
        auditRemark: data.auditRemark || ''
      }
    }
  } catch (error) {
    console.error('获取报告详情失败:', error)
    ElMessage.error('获取报告详情失败')
  }
}

const handleApprove = async () => {
  if (!approvalFormRef.value) return
  
  try {
    await approvalFormRef.value.validate()
    
    await ElMessageBox.confirm(
      '确定要通过该报告吗？',
      '确认通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    submitting.value = true
    const res = await healthReportApi.processReport({
      reportId: reportDetail.value.reportId,
      result: 1,
      comment: approvalForm.comment
    })
    
    if (res.code === 0) {
      ElMessage.success('审批通过')
      router.back()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== false) {
      console.error('审批失败:', error)
      ElMessage.error('审批失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleReject = async () => {
  if (!approvalFormRef.value) return
  
  try {
    await approvalFormRef.value.validate()
    
    await ElMessageBox.confirm(
      '确定要拒绝该报告吗？',
      '确认拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true
    const res = await healthReportApi.processReport({
      reportId: reportDetail.value.reportId,
      result: 2,
      comment: approvalForm.comment
    })
    
    if (res.code === 0) {
      ElMessage.success('已拒绝')
      router.back()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== false) {
      console.error('审批失败:', error)
      ElMessage.error('审批失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchReportDetail()
})
</script>

<style lang="scss" scoped>
.report-detail {
  padding: 0;
}

.report-content-section {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
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

.audit-remark {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
  }
}

.approval-section {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
  }
}

.mt-20 {
  margin-top: 20px;
}
</style>

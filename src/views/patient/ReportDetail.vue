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
      </div>

      <div v-if="reportDetail.auditComment" class="audit-comment mt-20">
        <h4>审核意见</h4>
        <el-alert :title="reportDetail.auditComment" type="info" :closable="false" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Document } from '@element-plus/icons-vue'
import healthReportApi from '@/api/healthReport'

const route = useRoute()

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
  auditComment: ''
})

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
        status: data.status || 0,
        uploadTime: formatTime(data.createTime),
        auditTime: formatTime(data.updateTime),
        remark: data.description || '',
        fileUrl: data.fileUrl || '',
        fileName: data.title || '',
        auditComment: data.auditRemark || ''
      }
    }
  } catch (error) {
    console.error('获取报告详情失败:', error)
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

.audit-comment {
  h4 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: #303133;
  }
}
</style>

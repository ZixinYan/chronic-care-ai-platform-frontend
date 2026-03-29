<template>
  <div class="report-approval">
    <div class="card">
      <div class="card-header">
        <span class="card-title">报告审批</span>
      </div>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="患者姓名">
          <el-input v-model="queryParams.patientName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="报告类型">
          <el-select v-model="queryParams.reportType" placeholder="请选择报告类型" clearable>
            <el-option label="血糖报告" value="BLOOD_SUGAR" />
            <el-option label="血压报告" value="BLOOD_PRESSURE" />
            <el-option label="心电图报告" value="ECG" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="reportList" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="reportType" label="报告类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getReportTypeTag(row.reportType)">{{ getReportTypeText(row.reportType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="报告标题" min-width="150" />
        <el-table-column prop="uploadTime" label="上传时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewReport(row)">查看</el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="success" 
              link 
              size="small" 
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="danger" 
              link 
              size="small" 
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="fetchReportList"
        @current-change="fetchReportList"
      />
    </div>

    <el-dialog v-model="detailDialogVisible" title="报告详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="患者姓名">{{ currentReport.patientName }}</el-descriptions-item>
        <el-descriptions-item label="报告类型">{{ getReportTypeText(currentReport.reportType) }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ currentReport.uploadTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(currentReport.status) }}</el-descriptions-item>
        <el-descriptions-item label="报告标题" :span="2">{{ currentReport.title }}</el-descriptions-item>
        <el-descriptions-item label="报告内容" :span="2">
          <div class="report-content">{{ currentReport.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <div v-if="currentReport.fileUrl" class="report-file mt-20">
        <el-image 
          v-if="isImageFile(currentReport.fileUrl)"
          :src="currentReport.fileUrl" 
          :preview-src-list="[currentReport.fileUrl]"
          fit="contain"
          style="max-height: 400px;"
        />
        <el-link v-else :href="currentReport.fileUrl" target="_blank" type="primary">
          点击查看文件
        </el-link>
      </div>

      <template v-if="currentReport.status === 'PENDING'" #footer>
        <el-button @click="detailDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject(currentReport)">拒绝</el-button>
        <el-button type="success" @click="handleApprove(currentReport)">通过</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import healthReportApi from '@/api/healthReport'

const loading = ref(false)
const submitLoading = ref(false)
const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const total = ref(0)
const rejectReason = ref('')
const currentReport = ref({})

const queryParams = reactive({
  patientName: '',
  reportType: '',
  status: 'PENDING',
  pageNum: 1,
  pageSize: 10
})

const reportList = ref([])

const getReportTypeText = (type) => {
  const types = { 
    BLOOD_SUGAR: '血糖报告', 
    BLOOD_PRESSURE: '血压报告', 
    ECG: '心电图报告', 
    OTHER: '其他' 
  }
  return types[type] || '未知'
}

const getReportTypeTag = (type) => {
  const tags = { 
    BLOOD_SUGAR: 'primary', 
    BLOOD_PRESSURE: 'success', 
    ECG: 'warning', 
    OTHER: 'info' 
  }
  return tags[type] || 'info'
}

const getStatusText = (status) => {
  const texts = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }
  return texts[status] || '未知'
}

const getStatusTag = (status) => {
  const tags = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return tags[status] || 'info'
}

const isImageFile = (url) => {
  if (!url) return false
  const ext = url.split('.').pop().toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(ext)
}

const fetchReportList = async () => {
  loading.value = true
  try {
    const res = await healthReportApi.getReportList(queryParams)
    if (res.code === 0) {
      reportList.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('获取报告列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchReportList()
}

const handleReset = () => {
  queryParams.patientName = ''
  queryParams.reportType = ''
  queryParams.status = 'PENDING'
  queryParams.pageNum = 1
  fetchReportList()
}

const viewReport = async (row) => {
  try {
    const res = await healthReportApi.getReportDetail(row.id)
    if (res.code === 0) {
      currentReport.value = res.data || row
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取报告详情失败:', error)
  }
}

const handleApprove = async (row) => {
  try {
    const res = await healthReportApi.processReport({
      reportId: row.id,
      status: 'APPROVED'
    })
    if (res.code === 0) {
      ElMessage.success('审批通过')
      detailDialogVisible.value = false
      fetchReportList()
    }
  } catch (error) {
    console.error('审批失败:', error)
  }
}

const handleReject = (row) => {
  currentReport.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  submitLoading.value = true
  try {
    const res = await healthReportApi.processReport({
      reportId: currentReport.value.id,
      status: 'REJECTED',
      rejectReason: rejectReason.value
    })
    if (res.code === 0) {
      ElMessage.success('已拒绝')
      rejectDialogVisible.value = false
      detailDialogVisible.value = false
      fetchReportList()
    }
  } catch (error) {
    console.error('审批失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchReportList()
})
</script>

<style lang="scss" scoped>
.report-approval {
  padding: 0;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.report-content {
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
}

.report-file {
  text-align: center;
}
</style>

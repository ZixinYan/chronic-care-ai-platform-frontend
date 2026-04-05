<template>
  <div class="report-approval">
    <div class="card">
      <div class="card-header">
        <span class="card-title">报告审批</span>
      </div>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="报告类型">
          <el-select v-model="queryParams.reportType" placeholder="请选择报告类型" clearable style="width: 140px">
            <el-option label="图片报告" :value="1" />
            <el-option label="文字报告" :value="2" />
            <el-option label="PDF报告" :value="3" />
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
        <el-table-column label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewReport(row)">查看详情</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import healthReportApi from '@/api/healthReport'

const router = useRouter()
const loading = ref(false)
const total = ref(0)

const queryParams = reactive({
  reportType: null,
  pageNum: 1,
  pageSize: 10
})

const reportList = ref([])

const getReportTypeText = (type) => {
  const types = { 
    1: '图片报告', 
    2: '文字报告', 
    3: 'PDF报告'
  }
  return types[type] || '未知'
}

const getReportTypeTag = (type) => {
  const tags = { 
    1: 'primary', 
    2: 'success', 
    3: 'warning'
  }
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

const fetchReportList = async () => {
  loading.value = true
  try {
    const res = await healthReportApi.getPendingApprovalList(queryParams)
    if (res.code === 0) {
      reportList.value = res.data?.reportList || []
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
  queryParams.reportType = null
  queryParams.pageNum = 1
  fetchReportList()
}

const viewReport = (row) => {
  router.push(`/doctor/report/${row.reportId}`)
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
</style>

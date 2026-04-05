<template>
  <div class="health-report">
    <div class="card">
      <div class="card-header">
        <span class="card-title">健康报告列表</span>
        <el-button type="primary" :icon="Upload" @click="$router.push('/patient/health-report/upload')">
          上传报告
        </el-button>
      </div>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="报告类型">
          <el-select v-model="queryParams.reportType" placeholder="全部" clearable style="width: 140px">
            <el-option label="图片报告" :value="1" />
            <el-option label="文字报告" :value="2" />
            <el-option label="PDF报告" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="已审核" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="reportList" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="报告标题" min-width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="reportType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getReportTypeTag(row.reportType)">
              {{ getReportTypeText(row.reportType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewReport(row)">查看</el-button>
            <el-button type="danger" link @click="deleteReport(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import healthReportApi from '@/api/healthReport'

const router = useRouter()
const loading = ref(false)
const reportList = ref([])
const total = ref(0)

const queryParams = reactive({
  reportType: null,
  status: null,
  pageNum: 1,
  pageSize: 10
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
    const res = await healthReportApi.getReportList(queryParams)
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
  queryParams.status = null
  queryParams.pageNum = 1
  fetchReportList()
}

const handleSizeChange = () => {
  fetchReportList()
}

const handleCurrentChange = () => {
  fetchReportList()
}

const viewReport = (row) => {
  router.push(`/patient/health-report/detail/${row.reportId}`)
}

const deleteReport = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该报告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('删除成功')
    fetchReportList()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  fetchReportList()
})
</script>

<style lang="scss" scoped>
.health-report {
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

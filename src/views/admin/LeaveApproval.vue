<template>
  <div class="leave-approval">
    <div class="card">
      <div class="card-header">
        <span class="card-title">休假审批</span>
      </div>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="queryParams.startDay"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="queryParams.endDay"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="leaveList" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="doctorName" label="医生姓名" width="120" />
        <el-table-column prop="leaveTypeDesc" label="休假类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getLeaveTypeTagType(row.leaveType)">
              {{ row.leaveTypeDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDay" label="开始日期" width="120" />
        <el-table-column prop="endDay" label="结束日期" width="120" />
        <el-table-column prop="reason" label="休假原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link size="small" @click="handleApprove(row, 'APPROVED')">通过</el-button>
            <el-button type="danger" link size="small" @click="handleApprove(row, 'REJECTED')">拒绝</el-button>
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

    <el-dialog v-model="approvalDialogVisible" title="审批意见" width="500px">
      <el-form :model="approvalForm" label-width="80px">
        <el-form-item label="医生">
          <el-input :value="currentLeave?.doctorName" disabled />
        </el-form-item>
        <el-form-item label="休假类型">
          <el-input :value="currentLeave?.leaveTypeDesc" disabled />
        </el-form-item>
        <el-form-item label="休假时间">
          <el-input :value="`${currentLeave?.startDay} 至 ${currentLeave?.endDay}`" disabled />
        </el-form-item>
        <el-form-item label="休假原因">
          <el-input :value="currentLeave?.reason" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input 
            v-model="approvalForm.approvalComment" 
            type="textarea" 
            :rows="3"
            placeholder="请输入审批意见（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button 
          :type="approvalForm.status === 'APPROVED' ? 'success' : 'danger'" 
          @click="handleConfirmApproval"
          :loading="saveLoading"
        >
          {{ approvalForm.status === 'APPROVED' ? '确认通过' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import adminApi from '@/api/admin'

const loading = ref(false)
const saveLoading = ref(false)
const total = ref(0)
const leaveList = ref([])
const approvalDialogVisible = ref(false)
const currentLeave = ref(null)

const queryParams = reactive({
  startDay: null,
  endDay: null,
  pageNum: 1,
  pageSize: 10
})

const approvalForm = reactive({
  leaveId: null,
  status: null,
  approvalComment: ''
})

const getLeaveTypeTagType = (type) => {
  const types = {
    'SICK': 'danger',
    'ANNUAL': 'success',
    'PERSONAL': 'warning',
    'TRAINING': 'primary',
    'OTHER': 'info'
  }
  return types[type] || 'info'
}

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchLeaveList = async () => {
  loading.value = true
  try {
    const res = await adminApi.getPendingLeaves(queryParams)
    if (res.code === 0) {
      leaveList.value = res.data?.leaves?.list || []
      total.value = res.data?.leaves?.total || 0
    }
  } catch (error) {
    console.error('获取待审批休假列表失败:', error)
    ElMessage.error('获取待审批休假列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchLeaveList()
}

const handleReset = () => {
  queryParams.startDay = null
  queryParams.endDay = null
  queryParams.pageNum = 1
  fetchLeaveList()
}

const handleSizeChange = () => {
  fetchLeaveList()
}

const handleCurrentChange = () => {
  fetchLeaveList()
}

const handleApprove = (row, status) => {
  currentLeave.value = row
  approvalForm.leaveId = row.id
  approvalForm.status = status
  approvalForm.approvalComment = ''
  approvalDialogVisible.value = true
}

const handleConfirmApproval = async () => {
  if (!approvalForm.leaveId) return
  
  saveLoading.value = true
  try {
    const res = await adminApi.approveLeave({
      leaveId: approvalForm.leaveId,
      status: approvalForm.status,
      approvalComment: approvalForm.approvalComment
    })
    if (res.code === 0) {
      ElMessage.success(approvalForm.status === 'APPROVED' ? '审批通过成功' : '审批拒绝成功')
      approvalDialogVisible.value = false
      fetchLeaveList()
    }
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error('审批失败')
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  fetchLeaveList()
})
</script>

<style lang="scss" scoped>
.leave-approval {
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

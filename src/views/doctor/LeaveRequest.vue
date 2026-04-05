<template>
  <div class="leave-request">
    <div class="card">
      <div class="card-header">
        <span class="card-title">请假申请</span>
        <el-button type="primary" size="small" @click="handleAddLeave">
          新增请假
        </el-button>
      </div>

      <el-table :data="leaveList" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="leaveType" label="请假类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getLeaveTypeTag(row.leaveType)">{{ getLeaveTypeText(row.leaveType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDay" label="开始日期" width="120" />
        <el-table-column prop="endDay" label="结束日期" width="120" />
        <el-table-column prop="reason" label="请假原因" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="primary" 
              link 
              size="small" 
              @click="handleEditLeave(row)"
            >
              编辑
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="danger" 
              link 
              size="small" 
              @click="handleDeleteLeave(row)"
            >
              撤销
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
        @size-change="fetchLeaveList"
        @current-change="fetchLeaveList"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑请假' : '新增请假'" width="500px">
      <el-form ref="formRef" :model="leaveForm" :rules="leaveRules" label-width="80px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="leaveForm.leaveType" placeholder="请选择请假类型">
            <el-option label="事假" value="PERSONAL" />
            <el-option label="病假" value="SICK" />
            <el-option label="年假" value="ANNUAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startDay">
          <el-date-picker
            v-model="leaveForm.startDay"
            type="date"
            placeholder="选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endDay">
          <el-date-picker
            v-model="leaveForm.endDay"
            type="date"
            placeholder="选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input v-model="leaveForm.reason" type="textarea" :rows="3" placeholder="请输入请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import doctorApi from '@/api/doctor'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const leaveList = ref([])

const leaveForm = reactive({
  leaveId: null,
  leaveType: '',
  startDay: '',
  endDay: '',
  reason: ''
})

const leaveRules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDay: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDay: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

const getLeaveTypeText = (type) => {
  const types = { PERSONAL: '事假', SICK: '病假', ANNUAL: '年假', OTHER: '其他' }
  return types[type] || '未知'
}

const getLeaveTypeTag = (type) => {
  const tags = { PERSONAL: 'primary', SICK: 'danger', ANNUAL: 'success', OTHER: 'info' }
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

const fetchLeaveList = async () => {
  loading.value = true
  try {
    const today = new Date().toISOString().split('T')[0]
    const res = await doctorApi.getLeaveList({
      ...queryParams,
      startDay: today
    })
    if (res.code === 0) {
      const allLeaves = res.data?.leaves?.list || []
      leaveList.value = allLeaves.filter(leave => {
        return leave.endDay >= today
      })
      total.value = leaveList.value.length
    }
  } catch (error) {
    console.error('获取请假列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAddLeave = () => {
  isEdit.value = false
  Object.assign(leaveForm, {
    leaveId: null,
    leaveType: '',
    startDay: '',
    endDay: '',
    reason: ''
  })
  dialogVisible.value = true
}

const handleEditLeave = (row) => {
  isEdit.value = true
  Object.assign(leaveForm, {
    leaveId: row.id,
    leaveType: row.leaveType,
    startDay: row.startDay,
    endDay: row.endDay,
    reason: row.reason
  })
  dialogVisible.value = true
}

const handleDeleteLeave = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤销这条请假申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await doctorApi.deleteLeave(row.id)
    if (res.code === 0) {
      ElMessage.success('撤销成功')
      fetchLeaveList()
    }
  } catch {
    // 用户取消
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const res = isEdit.value 
        ? await doctorApi.updateLeave(leaveForm)
        : await doctorApi.addLeave(leaveForm)
      
      if (res.code === 0) {
        ElMessage.success(isEdit.value ? '修改成功' : '申请成功')
        dialogVisible.value = false
        fetchLeaveList()
      }
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  fetchLeaveList()
})
</script>

<style lang="scss" scoped>
.leave-request {
  padding: 0;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>

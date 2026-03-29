<template>
  <div class="schedule-manage">
    <div class="card">
      <div class="card-header">
        <span class="card-title">日程管理</span>
        <el-button type="primary" :icon="Plus" @click="handleAddSchedule">添加日程</el-button>
      </div>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="日期">
          <el-date-picker
            v-model="queryParams.scheduleDay"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="scheduleList" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="scheduleDay" label="日期" width="120" />
        <el-table-column prop="scheduleTime" label="时间" width="100" />
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getScheduleTypeTag(row.type)">{{ row.typeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewSchedule(row)">详情</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              link
              size="small"
              @click="startSchedule(row)"
            >
              开始
            </el-button>
            <el-button
              v-if="row.status === 'IN_PROGRESS'"
              type="success"
              link
              size="small"
              @click="completeSchedule(row)"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status !== 'CANCELLED' && row.status !== 'COMPLETED'"
              type="danger"
              link
              size="small"
              @click="cancelSchedule(row)"
            >
              取消
            </el-button>
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

    <el-dialog v-model="scheduleDialogVisible" title="添加日程" width="500px">
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="80px">
        <el-form-item label="日期" prop="scheduleDay">
          <el-date-picker
            v-model="scheduleForm.scheduleDay"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="时间" prop="scheduleTime">
          <el-time-picker
            v-model="scheduleForm.scheduleTime"
            placeholder="选择时间"
            format="HH:mm"
            value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="患者" prop="patientId">
          <el-select v-model="scheduleForm.patientId" placeholder="请选择患者" filterable>
            <el-option
              v-for="patient in patientList"
              :key="patient.id"
              :label="patient.name"
              :value="patient.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="scheduleForm.type" placeholder="请选择类型">
            <el-option label="复诊" value="FOLLOW_UP" />
            <el-option label="咨询" value="CONSULTATION" />
            <el-option label="检查" value="EXAMINATION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="scheduleForm.content" type="textarea" :rows="3" placeholder="请输入日程内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitSchedule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import doctorApi from '@/api/doctor'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const scheduleDialogVisible = ref(false)
const scheduleFormRef = ref(null)
const total = ref(0)

const queryParams = reactive({
  scheduleDay: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const scheduleList = ref([])
const patientList = ref([])

const scheduleForm = reactive({
  scheduleDay: '',
  scheduleTime: '',
  patientId: '',
  type: '',
  content: ''
})

const scheduleRules = {
  scheduleDay: [{ required: true, message: '请选择日期', trigger: 'change' }],
  scheduleTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
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

const fetchScheduleList = async () => {
  loading.value = true
  try {
    const res = await doctorApi.getScheduleList(queryParams)
    if (res.code === 0) {
      scheduleList.value = res.data?.schedules?.list || []
      total.value = res.data?.schedules?.totalCount || 0
    }
  } catch (error) {
    console.error('获取日程列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchPatientList = async () => {
  try {
    const res = await doctorApi.getMyPatients({ pageNum: 1, pageSize: 100 })
    if (res.code === 0) {
      patientList.value = res.data?.list || []
    }
  } catch (error) {
    console.error('获取患者列表失败:', error)
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  fetchScheduleList()
}

const handleReset = () => {
  queryParams.scheduleDay = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  fetchScheduleList()
}

const handleSizeChange = () => {
  fetchScheduleList()
}

const handleCurrentChange = () => {
  fetchScheduleList()
}

const handleAddSchedule = () => {
  fetchPatientList()
  scheduleDialogVisible.value = true
}

const submitSchedule = async () => {
  if (!scheduleFormRef.value) return

  await scheduleFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const res = await doctorApi.addSchedule(scheduleForm)
      if (res.code === 0) {
        ElMessage.success('添加成功')
        scheduleDialogVisible.value = false
        fetchScheduleList()
      }
    } catch (error) {
      console.error('添加日程失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

const viewSchedule = (row) => {
  router.push(`/doctor/schedule/detail/${row.id}`)
}

const startSchedule = async (row) => {
  try {
    const res = await doctorApi.updateScheduleStatus(row.id, 'IN_PROGRESS')
    if (res.code === 0) {
      ElMessage.success('已开始处理')
      fetchScheduleList()
    }
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

const completeSchedule = (row) => {
  router.push({ path: `/doctor/schedule/detail/${row.id}`, query: { action: 'complete' } })
}

const cancelSchedule = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消日程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入取消原因'
    })
    const res = await doctorApi.cancelSchedule(row.id, value)
    if (res.code === 0) {
      ElMessage.success('已取消')
      fetchScheduleList()
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  fetchScheduleList()
})
</script>

<style lang="scss" scoped>
.schedule-manage {
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

<template>
  <div class="doctor-workbench">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #409eff;">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.todaySchedules }}</div>
            <div class="stat-label">今日日程</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a;">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.completedSchedules }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c;">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingSchedules }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #f56c6c;">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.patientCount }}</div>
            <div class="stat-label">患者数量</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <div class="card">
          <div class="card-header">
            <span class="card-title">今日日程</span>
            <el-button type="primary" size="small" @click="handleAddSchedule">
              添加日程
            </el-button>
          </div>
          <el-table :data="scheduleList" stripe v-loading="loading" style="width: 100%">
            <el-table-column prop="scheduleTime" label="时间" width="120" />
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
                <el-button type="danger" link size="small" @click="cancelSchedule(row)">取消</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="card">
          <div class="card-header">
            <span class="card-title">快捷操作</span>
          </div>
          <div class="quick-actions">
            <el-button type="primary" :icon="Calendar" @click="$router.push('/doctor/schedule')">
              日程管理
            </el-button>
            <el-button type="success" :icon="User" @click="$router.push('/doctor/patients')">
              患者管理
            </el-button>
            <el-button type="warning" :icon="MagicStick" @click="$router.push('/ai/schedule')">
              AI日程生成
            </el-button>
          </div>
        </div>

        <div class="card mt-20">
          <div class="card-header">
            <span class="card-title">待处理报告</span>
          </div>
          <div class="pending-reports">
            <div v-for="report in pendingReports" :key="report.id" class="report-item">
              <div class="report-info">
                <span class="report-title">{{ report.title }}</span>
                <span class="report-patient">{{ report.patientName }}</span>
              </div>
              <el-button type="primary" link size="small" @click="processReport(report)">
                处理
              </el-button>
            </div>
            <el-empty v-if="pendingReports.length === 0" description="暂无待处理报告" />
          </div>
        </div>
      </el-col>
    </el-row>

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
import { Calendar, Check, Clock, User, MagicStick } from '@element-plus/icons-vue'
import doctorApi from '@/api/doctor'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const scheduleDialogVisible = ref(false)
const scheduleFormRef = ref(null)

const stats = ref({
  todaySchedules: 8,
  completedSchedules: 3,
  pendingSchedules: 5,
  patientCount: 42
})

const scheduleList = ref([])
const pendingReports = ref([
  { id: 1, title: '血糖检测报告', patientName: '张三' },
  { id: 2, title: '心电图报告', patientName: '李四' }
])

const scheduleForm = reactive({
  scheduleDay: '',
  scheduleTime: '',
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
    const res = await doctorApi.getScheduleList({ scheduleDay: new Date().toISOString().split('T')[0] })
    if (res.code === 0) {
      scheduleList.value = res.data?.schedules?.list || []
    }
  } catch (error) {
    console.error('获取日程列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAddSchedule = () => {
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

const processReport = (report) => {
  router.push({ path: '/doctor/patient/' + report.patientId, query: { reportId: report.id } })
}

onMounted(() => {
  fetchScheduleList()
})
</script>

<style lang="scss" scoped>
.doctor-workbench {
  padding: 0;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin-right: 16px;

  .el-icon {
    font-size: 28px;
    color: #fff;
  }
}

.stat-content {
  flex: 1;

  .stat-value {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .el-button {
    width: 100%;
  }
}

.pending-reports {
  .report-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .report-info {
      flex: 1;

      .report-title {
        display: block;
        font-size: 14px;
        color: #303133;
      }

      .report-patient {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>

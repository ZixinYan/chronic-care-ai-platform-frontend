<template>
  <div class="appointment-doctor">
    <el-row :gutter="20">
      <el-col :span="16">
        <div class="card">
          <div class="card-header">
            <span class="card-title">选择医生</span>
          </div>

          <el-form :inline="true" :model="queryParams" class="search-form">
            <el-form-item label="医生姓名">
              <el-input v-model="queryParams.name" placeholder="请输入医生姓名" clearable />
            </el-form-item>
            <el-form-item label="科室">
              <el-select v-model="queryParams.department" placeholder="请选择科室" clearable style="width: 140px">
                <el-option label="内科" value="内科" />
                <el-option label="内分泌科" value="内分泌科" />
                <el-option label="心血管科" value="心血管科" />
                <el-option label="神经内科" value="神经内科" />
                <el-option label="全科" value="全科" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="doctor-list">
            <div 
              v-for="doctor in filteredDoctorList" 
              :key="doctor.id" 
              class="doctor-card"
              :class="{ selected: selectedDoctor?.id === doctor.id }"
              @click="selectDoctor(doctor)"
            >
              <div class="doctor-avatar">
                <el-avatar :size="60" :src="doctor.avatarUrl">
                  {{ doctor.username?.charAt(0) }}
                </el-avatar>
              </div>
              <div class="doctor-info">
                <div class="doctor-name">
                  {{ doctor.username }}
                  <el-tag size="small" type="primary">{{ doctor.title }}</el-tag>
                </div>
                <div class="doctor-department">{{ doctor.department }}</div>
                <div class="doctor-specialty">擅长：{{ doctor.bio || '暂无介绍' }}</div>
              </div>
            </div>
          </div>

          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :total="filteredTotal"
            :page-sizes="[6, 12, 24]"
            layout="total, sizes, prev, pager, next"
            class="pagination"
          />
        </div>
      </el-col>

      <el-col :span="8">
        <div class="card">
          <div class="card-header">
            <span class="card-title">预约信息</span>
          </div>

          <div v-if="!selectedDoctor" class="no-doctor-selected">
            <el-empty description="请选择医生" />
          </div>

          <div v-else>
            <div class="selected-doctor-info">
              <el-avatar :size="50" :src="selectedDoctor.avatarUrl">
                {{ selectedDoctor.username?.charAt(0) }}
              </el-avatar>
              <div class="doctor-detail">
                <div class="name">{{ selectedDoctor.username }}</div>
                <div class="department">{{ selectedDoctor.department }}</div>
              </div>
            </div>

            <el-form 
              ref="appointmentFormRef" 
              :model="appointmentForm" 
              :rules="appointmentRules" 
              label-width="80px"
              class="mt-20"
            >
              <el-form-item label="预约日期" prop="appointmentDate">
                <el-date-picker
                  v-model="appointmentForm.appointmentDate"
                  type="date"
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disabledDate"
                />
              </el-form-item>
              <el-form-item label="预约时段" prop="timeSlot">
                <el-select v-model="appointmentForm.timeSlot" placeholder="选择时段">
                  <el-option 
                    v-for="slot in availableSlots" 
                    :key="slot.value" 
                    :label="slot.label" 
                    :value="slot.value"
                    :disabled="slot.disabled"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="就诊类型" prop="visitType">
                <el-select v-model="appointmentForm.visitType" placeholder="选择类型">
                  <el-option label="初诊" value="FIRST" />
                  <el-option label="复诊" value="FOLLOW_UP" />
                  <el-option label="咨询" value="CONSULTATION" />
                </el-select>
              </el-form-item>
              <el-form-item label="病情描述" prop="description">
                <el-input 
                  v-model="appointmentForm.description" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请简要描述您的病情或咨询需求" 
                />
              </el-form-item>
              <el-form-item>
                <el-button 
                  type="primary" 
                  :loading="submitting" 
                  :disabled="!selectedDoctor"
                  @click="handleSubmitAppointment"
                >
                  提交预约
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <div class="card mt-20">
          <div class="card-header">
            <span class="card-title">我的预约</span>
            <el-button type="primary" link @click="$router.push('/patient/appointments')">
              查看全部
            </el-button>
          </div>
          <div class="my-appointments">
            <div v-for="item in myAppointments" :key="item.id" class="appointment-item">
              <div class="appointment-info">
                <div class="doctor-name">{{ item.doctorName }}</div>
                <div class="appointment-time">{{ item.scheduleDay }}</div>
              </div>
              <el-tag :type="getStatusTag(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
            <el-empty v-if="myAppointments.length === 0" description="暂无预约记录" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import doctorApi from '@/api/doctor'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const selectedDoctor = ref(null)
const appointmentFormRef = ref(null)

const queryParams = reactive({
  name: '',
  department: '',
  pageNum: 1,
  pageSize: 6
})

const doctorList = ref([])

const filteredTotal = computed(() => {
  let result = doctorList.value
  
  if (queryParams.name) {
    result = result.filter(doctor => 
      doctor.username && doctor.username.includes(queryParams.name)
    )
  }
  
  if (queryParams.department) {
    result = result.filter(doctor => 
      doctor.department && doctor.department === queryParams.department
    )
  }
  
  return result.length
})

const filteredDoctorList = computed(() => {
  let result = doctorList.value
  
  if (queryParams.name) {
    result = result.filter(doctor => 
      doctor.username && doctor.username.includes(queryParams.name)
    )
  }
  
  if (queryParams.department) {
    result = result.filter(doctor => 
      doctor.department && doctor.department === queryParams.department
    )
  }
  
  const start = (queryParams.pageNum - 1) * queryParams.pageSize
  const end = start + queryParams.pageSize
  
  return result.slice(start, end)
})

const appointmentForm = reactive({
  appointmentDate: '',
  timeSlot: '',
  visitType: '',
  description: ''
})

const appointmentRules = {
  appointmentDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择预约时段', trigger: 'change' }],
  visitType: [{ required: true, message: '请选择就诊类型', trigger: 'change' }]
}

const availableSlots = ref([
  { label: '上午 08:00-09:00', value: '08:00-09:00', disabled: false },
  { label: '上午 09:00-10:00', value: '09:00-10:00', disabled: false },
  { label: '上午 10:00-11:00', value: '10:00-11:00', disabled: false },
  { label: '上午 11:00-12:00', value: '11:00-12:00', disabled: false },
  { label: '下午 14:00-15:00', value: '14:00-15:00', disabled: false },
  { label: '下午 15:00-16:00', value: '15:00-16:00', disabled: false },
  { label: '下午 16:00-17:00', value: '16:00-17:00', disabled: false }
])

const myAppointments = ref([])

const getStatusText = (status) => {
  const texts = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }
  return texts[status] || '未知'
}

const getStatusTag = (status) => {
  const tags = { PENDING: 'warning', CONFIRMED: 'success', CANCELLED: 'info', COMPLETED: 'primary' }
  return tags[status] || 'info'
}

const disabledDate = (date) => {
  return date.getTime() < Date.now() - 86400000
}

const fetchDoctorList = async () => {
  loading.value = true
  try {
    const res = await doctorApi.getDoctorList()
    if (res.code === 0) {
      doctorList.value = res.data || []
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    ElMessage.error('获取医生列表失败')
  } finally {
    loading.value = false
  }
}

const fetchMyAppointments = async () => {
  try {
    const res = await doctorApi.getPatientSchedules({
      patientId: userStore.userId,
      pageNum: 1,
      pageSize: 5
    })
    if (res.code === 0) {
      myAppointments.value = res.data.schedules?.list || []
    }
  } catch (error) {
    console.error('获取预约记录失败:', error)
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.department = ''
  queryParams.pageNum = 1
}

const selectDoctor = (doctor) => {
  selectedDoctor.value = doctor
}

const handleSubmitAppointment = async () => {
  if (!selectedDoctor.value) {
    ElMessage.warning('请先选择医生')
    return
  }

  if (!appointmentFormRef.value) return

  await appointmentFormRef.value.validate(async (valid) => {
    if (!valid) return

    const [startTimeStr, endTimeStr] = appointmentForm.timeSlot.split('-')

    submitting.value = true
    try {
      const res = await doctorApi.addSchedule({
        doctorId: selectedDoctor.value.userId,
        patientId: userStore.userId,
        scheduleDay: appointmentForm.appointmentDate,
        schedule: appointmentForm.description,
        scheduleCategory: '门诊',
        startTimeStr,
        endTimeStr,
        ext: {
          timeSlot: appointmentForm.timeSlot,
          visitType: appointmentForm.visitType
        }
      })
      if (res.code === 0) {
        ElMessage.success('预约成功')
        Object.assign(appointmentForm, {
          appointmentDate: '',
          timeSlot: '',
          visitType: '',
          description: ''
        })
        fetchMyAppointments()
      }
    } catch (error) {
      console.error('预约失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  fetchDoctorList()
  fetchMyAppointments()
})
</script>

<style lang="scss" scoped>
.doctor-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.doctor-card {
  display: flex;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;

  &:hover {
    background-color: #ecf5ff;
  }

  &.selected {
    border-color: #409eff;
    background-color: #ecf5ff;
  }

  .doctor-avatar {
    margin-right: 16px;
  }

  .doctor-info {
    flex: 1;

    .doctor-name {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 4px;

      .el-tag {
        margin-left: 8px;
      }
    }

    .doctor-department {
      font-size: 14px;
      color: #606266;
      margin-bottom: 4px;
    }

    .doctor-specialty {
      font-size: 12px;
      color: #909399;
    }
  }
}

.selected-doctor-info {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;

  .doctor-detail {
    margin-left: 12px;

    .name {
      font-size: 16px;
      font-weight: 500;
    }

    .department {
      font-size: 14px;
      color: #909399;
    }
  }
}

.my-appointments {
  .appointment-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .appointment-info {
      .doctor-name {
        font-size: 14px;
        color: #303133;
      }

      .appointment-time {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.no-doctor-selected {
  padding: 40px 0;
}

.search-form {
  margin-bottom: 20px;
}
</style>

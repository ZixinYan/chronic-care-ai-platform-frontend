<template>
  <div class="ai-schedule">
    <div class="card">
      <div class="card-header">
        <span class="card-title">AI日程生成</span>
      </div>

      <el-form ref="formRef" :model="generateForm" :rules="generateRules" label-width="100px" class="generate-form">
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="generateForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="生成类型" prop="generateType">
          <el-radio-group v-model="generateForm.generateType">
            <el-radio label="DAILY">日常日程</el-radio>
            <el-radio label="FOLLOW_UP">复诊安排</el-radio>
            <el-radio label="EXAMINATION">检查安排</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-select v-model="generateForm.priority" placeholder="请选择优先级">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>

        <el-form-item label="特殊要求">
          <el-input
            v-model="generateForm.specialRequirements"
            type="textarea"
            :rows="3"
            placeholder="请输入特殊要求（可选）"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" :icon="MagicStick" @click="handleGenerate">
            生成日程
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="generatedSchedules.length > 0" class="card mt-20">
      <div class="card-header">
        <span class="card-title">生成的日程建议</span>
        <el-button type="primary" @click="confirmSchedules">确认添加</el-button>
      </div>

      <el-table :data="generatedSchedules" stripe style="width: 100%">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="scheduleDay" label="日期" width="120" />
        <el-table-column prop="scheduleTime" label="时间" width="100" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getScheduleTypeTag(row.type)">{{ row.typeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityTag(row.priority)" size="small">{{ row.priorityText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="AI建议理由" min-width="200" />
      </el-table>
    </div>

    <div class="card mt-20">
      <div class="card-header">
        <span class="card-title">AI建议说明</span>
      </div>
      <div class="ai-tips">
        <el-alert
          title="AI日程生成功能说明"
          type="info"
          :closable="false"
          show-icon
        >
          <p>1. AI会根据您的工作习惯、患者情况和历史数据智能生成日程安排建议</p>
          <p>2. 生成的日程仅供参考，您可以根据实际情况进行调整</p>
          <p>3. 确认后日程将添加到您的日程表中</p>
        </el-alert>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import aiApi from '@/api/ai'
import doctorApi from '@/api/doctor'

const loading = ref(false)
const formRef = ref(null)
const generatedSchedules = ref([])

const generateForm = reactive({
  dateRange: [],
  generateType: 'DAILY',
  priority: 'MEDIUM',
  specialRequirements: ''
})

const generateRules = {
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  generateType: [{ required: true, message: '请选择生成类型', trigger: 'change' }]
}

const getScheduleTypeTag = (type) => {
  const tags = { FOLLOW_UP: 'primary', CONSULTATION: 'success', EXAMINATION: 'warning', OTHER: 'info' }
  return tags[type] || 'info'
}

const getPriorityTag = (priority) => {
  const tags = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return tags[priority] || 'info'
}

const handleGenerate = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await aiApi.generateSchedule({
        startDate: generateForm.dateRange[0],
        endDate: generateForm.dateRange[1],
        generateType: generateForm.generateType,
        priority: generateForm.priority,
        specialRequirements: generateForm.specialRequirements
      })
      if (res.code === 0) {
        generatedSchedules.value = res.data?.schedules || []
        ElMessage.success('日程生成成功')
      }
    } catch (error) {
      console.error('生成日程失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const confirmSchedules = async () => {
  try {
    const schedulesToAdd = generatedSchedules.value.filter(s => s.selected)
    if (schedulesToAdd.length === 0) {
      ElMessage.warning('请选择要添加的日程')
      return
    }

    for (const schedule of schedulesToAdd) {
      await doctorApi.addSchedule(schedule)
    }

    ElMessage.success('日程添加成功')
    generatedSchedules.value = []
  } catch (error) {
    console.error('添加日程失败:', error)
  }
}
</script>

<style lang="scss" scoped>
.ai-schedule {
  padding: 0;
}

.generate-form {
  max-width: 600px;
}

.ai-tips {
  p {
    margin: 8px 0;
    font-size: 14px;
    color: #606266;
  }
}
</style>

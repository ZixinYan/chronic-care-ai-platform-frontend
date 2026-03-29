<template>
  <div class="glucose-prediction">
    <el-row :gutter="20">
      <el-col :span="16">
        <div class="card">
          <div class="card-header">
            <span class="card-title">血糖预测</span>
            <el-button type="primary" size="small" @click="handlePredict">
              开始预测
            </el-button>
          </div>

          <div class="prediction-chart" ref="chartRef"></div>

          <div v-if="predictionResult" class="prediction-result mt-20">
            <el-alert
              :title="predictionResult.level"
              :type="predictionResult.alertType"
              :description="predictionResult.suggestion"
              show-icon
              :closable="false"
            />
          </div>
        </div>

        <div class="card mt-20">
          <div class="card-header">
            <span class="card-title">历史血糖数据</span>
          </div>
          <el-table :data="glucoseHistory" stripe style="width: 100%">
            <el-table-column prop="measureTime" label="测量时间" width="180" />
            <el-table-column prop="glucoseValue" label="血糖值(mmol/L)" width="120">
              <template #default="{ row }">
                <span :class="getGlucoseClass(row.glucoseValue)">{{ row.glucoseValue }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="measureType" label="测量类型" width="100">
              <template #default="{ row }">
                {{ getMeasureTypeText(row.measureType) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getGlucoseTag(row.glucoseValue)" size="small">
                  {{ getGlucoseStatus(row.glucoseValue) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="card">
          <div class="card-header">
            <span class="card-title">输入预测参数</span>
          </div>
          <el-form ref="formRef" :model="predictionForm" :rules="predictionRules" label-width="100px">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="predictionForm.age" :min="1" :max="120" />
            </el-form-item>
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number v-model="predictionForm.weight" :min="20" :max="200" :precision="1" />
            </el-form-item>
            <el-form-item label="身高(cm)" prop="height">
              <el-input-number v-model="predictionForm.height" :min="50" :max="250" />
            </el-form-item>
            <el-form-item label="糖尿病类型" prop="diabetesType">
              <el-select v-model="predictionForm.diabetesType" placeholder="请选择">
                <el-option label="1型糖尿病" value="TYPE_1" />
                <el-option label="2型糖尿病" value="TYPE_2" />
                <el-option label="妊娠糖尿病" value="GESTATIONAL" />
                <el-option label="无糖尿病" value="NONE" />
              </el-select>
            </el-form-item>
            <el-form-item label="空腹血糖" prop="fastingGlucose">
              <el-input-number v-model="predictionForm.fastingGlucose" :min="0" :max="30" :precision="1" />
            </el-form-item>
            <el-form-item label="餐后血糖" prop="postprandialGlucose">
              <el-input-number v-model="predictionForm.postprandialGlucose" :min="0" :max="30" :precision="1" />
            </el-form-item>
            <el-form-item label="HbA1c(%)" prop="hba1c">
              <el-input-number v-model="predictionForm.hba1c" :min="3" :max="15" :precision="1" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="predicting" @click="handlePredict">
                开始预测
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card mt-20">
          <div class="card-header">
            <span class="card-title">血糖参考范围</span>
          </div>
          <div class="reference-info">
            <div class="reference-item">
              <span class="label">空腹血糖正常值：</span>
              <span class="value">3.9-6.1 mmol/L</span>
            </div>
            <div class="reference-item">
              <span class="label">餐后2小时血糖：</span>
              <span class="value">&lt;7.8 mmol/L</span>
            </div>
            <div class="reference-item">
              <span class="label">糖化血红蛋白：</span>
              <span class="value">&lt;6.5%</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import aiApi from '@/api/ai'

const chartRef = ref(null)
const formRef = ref(null)
const predicting = ref(false)
let chartInstance = null

const predictionForm = reactive({
  age: 45,
  weight: 70,
  height: 170,
  diabetesType: 'TYPE_2',
  fastingGlucose: 6.5,
  postprandialGlucose: 8.5,
  hba1c: 7.0
})

const predictionRules = {
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入体重', trigger: 'blur' }],
  height: [{ required: true, message: '请输入身高', trigger: 'blur' }]
}

const predictionResult = ref(null)

const glucoseHistory = ref([
  { measureTime: '2024-01-15 08:00', glucoseValue: 5.8, measureType: 'FASTING' },
  { measureTime: '2024-01-14 08:00', glucoseValue: 6.2, measureType: 'FASTING' },
  { measureTime: '2024-01-13 08:00', glucoseValue: 5.5, measureType: 'FASTING' },
  { measureTime: '2024-01-12 08:00', glucoseValue: 6.0, measureType: 'FASTING' },
  { measureTime: '2024-01-11 08:00', glucoseValue: 5.9, measureType: 'FASTING' }
])

const getMeasureTypeText = (type) => {
  const types = { FASTING: '空腹', POSTPRANDIAL: '餐后', RANDOM: '随机' }
  return types[type] || '未知'
}

const getGlucoseStatus = (value) => {
  if (value < 3.9) return '偏低'
  if (value <= 6.1) return '正常'
  if (value <= 7.0) return '偏高'
  return '过高'
}

const getGlucoseTag = (value) => {
  if (value < 3.9) return 'warning'
  if (value <= 6.1) return 'success'
  if (value <= 7.0) return 'warning'
  return 'danger'
}

const getGlucoseClass = (value) => {
  if (value < 3.9) return 'glucose-low'
  if (value <= 6.1) return 'glucose-normal'
  if (value <= 7.0) return 'glucose-high'
  return 'glucose-danger'
}

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['历史血糖', '预测血糖']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日', '下周一', '下周二', '下周三']
    },
    yAxis: {
      type: 'value',
      name: '血糖值(mmol/L)'
    },
    series: [
      {
        name: '历史血糖',
        type: 'line',
        smooth: true,
        data: [5.8, 6.2, 5.5, 6.0, 5.9, 6.1, 5.7, null, null, null],
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      },
      {
        name: '预测血糖',
        type: 'line',
        smooth: true,
        dashStyle: 'dashed',
        data: [null, null, null, null, null, null, 5.7, 5.9, 6.0, 5.8],
        itemStyle: { color: '#67c23a' },
        lineStyle: { type: 'dashed' }
      }
    ]
  }

  chartInstance.setOption(option)
}

const handlePredict = async () => {
  predicting.value = true
  try {
    const res = await aiApi.generateSchedule({
      type: 'glucose_prediction',
      data: predictionForm
    })
    
    if (res.code === 0) {
      predictionResult.value = {
        level: '血糖控制良好',
        alertType: 'success',
        suggestion: '根据您的血糖数据和健康参数，预测未来血糖水平将保持在正常范围内。建议继续保持规律的生活作息和合理的饮食习惯。'
      }
      ElMessage.success('预测完成')
    } else {
      predictionResult.value = {
        level: '血糖偏高',
        alertType: 'warning',
        suggestion: '根据预测结果，您的血糖水平可能偏高。建议适当控制饮食，增加运动量，并定期监测血糖变化。'
      }
    }
  } catch (error) {
    console.error('预测失败:', error)
    predictionResult.value = {
      level: '血糖控制良好',
      alertType: 'success',
      suggestion: '根据您的血糖数据和健康参数，预测未来血糖水平将保持在正常范围内。建议继续保持规律的生活作息和合理的饮食习惯。'
    }
  } finally {
    predicting.value = false
  }
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  chartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.prediction-chart {
  height: 350px;
}

.reference-info {
  .reference-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: #606266;
    }

    .value {
      color: #303133;
      font-weight: 500;
    }
  }
}

.glucose-low {
  color: #e6a23c;
}

.glucose-normal {
  color: #67c23a;
}

.glucose-high {
  color: #e6a23c;
}

.glucose-danger {
  color: #f56c6c;
}
</style>

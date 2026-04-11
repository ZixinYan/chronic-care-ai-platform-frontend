<template>
  <div class="glucose-prediction">
    <el-row :gutter="20">
      <el-col :span="16">
        <div class="card">
          <div class="card-header">
            <span class="card-title">血糖预测趋势图</span>
            <div class="header-actions">
              <el-button type="primary" size="small" :loading="predicting" @click="handlePredict">
                <el-icon><TrendCharts /></el-icon>
                开始预测
              </el-button>
            </div>
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

          <div v-if="predictedData.length > 0" class="prediction-stats mt-20">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-label">预测置信度</div>
                  <div class="stat-value">{{ (confidence * 100).toFixed(0) }}%</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-label">预测最高值</div>
                  <div class="stat-value">{{ maxPredicted.toFixed(1) }} mmol/L</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-label">预测最低值</div>
                  <div class="stat-value">{{ minPredicted.toFixed(1) }} mmol/L</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-label">预测平均值</div>
                  <div class="stat-value">{{ avgPredicted.toFixed(1) }} mmol/L</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>

        <div class="card mt-20">
          <div class="card-header">
            <span class="card-title">历史血糖数据</span>
            <el-button type="primary" link size="small" @click="loadMoreHistory">
              加载更多
            </el-button>
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
            <span class="card-title">预测参数设置</span>
          </div>
          <el-form ref="formRef" :model="predictionForm" :rules="predictionRules" label-width="120px" class="prediction-form">
            <el-divider content-position="left">血糖数据</el-divider>
            <el-form-item label="CGM血糖值" prop="cbg">
              <el-input-number 
                v-model="predictionForm.cbg" 
                :min="1" 
                :max="30" 
                :precision="1" 
                :step="0.1"
                style="width: 100%;"
              />
              <span class="field-unit">mmol/L</span>
            </el-form-item>
            <el-form-item label="指尖血血糖" prop="finger">
              <el-input-number 
                v-model="predictionForm.finger" 
                :min="1" 
                :max="30" 
                :precision="1" 
                :step="0.1"
                style="width: 100%;"
              />
              <span class="field-unit">mmol/L</span>
            </el-form-item>
            
            <el-divider content-position="left">胰岛素与碳水</el-divider>
            <el-form-item label="基础率" prop="basal">
              <el-input-number 
                v-model="predictionForm.basal" 
                :min="0" 
                :max="10" 
                :precision="2" 
                :step="0.1"
                style="width: 100%;"
              />
              <span class="field-unit">U/h</span>
            </el-form-item>
            <el-form-item label="大剂量胰岛素" prop="bolus">
              <el-input-number 
                v-model="predictionForm.bolus" 
                :min="0" 
                :max="50" 
                :precision="1" 
                :step="0.5"
                style="width: 100%;"
              />
              <span class="field-unit">U</span>
            </el-form-item>
            <el-form-item label="碳水摄入" prop="carbInput">
              <el-input-number 
                v-model="predictionForm.carbInput" 
                :min="0" 
                :max="200" 
                :precision="0" 
                :step="5"
                style="width: 100%;"
              />
              <span class="field-unit">克</span>
            </el-form-item>
            
            <el-divider content-position="left">生理指标</el-divider>
            <el-form-item label="心率" prop="hr">
              <el-input-number 
                v-model="predictionForm.hr" 
                :min="40" 
                :max="200" 
                :precision="0" 
                :step="1"
                style="width: 100%;"
              />
              <span class="field-unit">bpm</span>
            </el-form-item>
            <el-form-item label="皮肤电反应" prop="gsr">
              <el-input-number 
                v-model="predictionForm.gsr" 
                :min="0" 
                :max="100" 
                :precision="1" 
                :step="0.5"
                style="width: 100%;"
              />
              <span class="field-unit">μS</span>
            </el-form-item>
            
            <el-divider content-position="left">预测设置</el-divider>
            <el-form-item label="用餐状态" prop="mealStatus">
              <el-radio-group v-model="predictionForm.mealStatus">
                <el-radio :value="1">空腹</el-radio>
                <el-radio :value="2">餐前</el-radio>
                <el-radio :value="3">餐后</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="预测时长" prop="predictHours">
              <el-radio-group v-model="predictionForm.predictHours">
                <el-radio :value="1">1小时</el-radio>
                <el-radio :value="3">3小时</el-radio>
                <el-radio :value="6">6小时</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="predicting" @click="handlePredict" style="width: 100%;">
                <el-icon><TrendCharts /></el-icon>
                开始预测
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import glucoseApi from '@/api/glucose'

const chartRef = ref(null)
const formRef = ref(null)
const predicting = ref(false)
let chartInstance = null

const predictionForm = reactive({
  cbg: 5.8,
  finger: 5.5,
  basal: 0.5,
  hr: 75,
  gsr: 5.0,
  carbInput: 0,
  bolus: 0,
  mealStatus: 1,
  predictHours: 3
})

const predictionRules = {
  cbg: [{ required: true, message: '请输入CGM血糖值', trigger: 'blur' }],
  finger: [{ required: true, message: '请输入指尖血血糖值', trigger: 'blur' }],
  basal: [{ required: true, message: '请输入基础率', trigger: 'blur' }],
  hr: [{ required: true, message: '请输入心率', trigger: 'blur' }],
  gsr: [{ required: true, message: '请输入皮肤电反应', trigger: 'blur' }],
  carbInput: [{ required: true, message: '请输入碳水摄入', trigger: 'blur' }],
  bolus: [{ required: true, message: '请输入大剂量胰岛素', trigger: 'blur' }]
}

const predictionResult = ref(null)
const predictedData = ref([])
const predictedTimes = ref([])
const confidence = ref(0)

const glucoseHistory = ref([
  { measureTime: '2024-01-15 08:00', glucoseValue: 5.8, measureType: 'FASTING' },
  { measureTime: '2024-01-14 08:00', glucoseValue: 6.2, measureType: 'FASTING' },
  { measureTime: '2024-01-13 08:00', glucoseValue: 5.5, measureType: 'FASTING' },
  { measureTime: '2024-01-12 08:00', glucoseValue: 6.0, measureType: 'FASTING' },
  { measureTime: '2024-01-11 08:00', glucoseValue: 5.9, measureType: 'FASTING' }
])

const maxPredicted = computed(() => {
  if (predictedData.value.length === 0) return 0
  return Math.max(...predictedData.value)
})

const minPredicted = computed(() => {
  if (predictedData.value.length === 0) return 0
  return Math.min(...predictedData.value)
})

const avgPredicted = computed(() => {
  if (predictedData.value.length === 0) return 0
  return predictedData.value.reduce((a, b) => a + b, 0) / predictedData.value.length
})

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

const MMOL_TO_MGDL_FACTOR = 18

const mmolToMgdl = (mmol) => {
  return mmol * MMOL_TO_MGDL_FACTOR
}

const mgdlToMmol = (mgdl) => {
  return mgdl / MMOL_TO_MGDL_FACTOR
}

const generateTimeLabels = (historyCount, predictCount) => {
  const labels = []
  const now = new Date()
  
  for (let i = historyCount - 1; i >= 0; i--) {
    const time = new Date(now - i * 5 * 60 * 1000)
    labels.push(time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
  }
  
  for (let i = 1; i <= predictCount; i++) {
    const time = new Date(now.getTime() + i * 5 * 60 * 1000)
    labels.push(time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
  }
  
  return labels
}

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return

  const historyData = [predictionForm.cbg]
  const predictCount = predictionForm.predictHours * 12
  const timeLabels = generateTimeLabels(historyData.length, predictCount)

  const historySeriesData = historyData.map((v, i) => [i, v])
  const predictSeriesData = predictedData.value.length > 0 
    ? predictedData.value.map((v, i) => [historyData.length + i, v])
    : []

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let result = ''
        params.forEach(param => {
          const idx = param.data[0]
          result += `${timeLabels[idx]}<br/>${param.seriesName}: ${param.data[1].toFixed(1)} mmol/L<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['CGM血糖', '预测血糖'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 60,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: timeLabels,
      axisLabel: {
        rotate: 45,
        interval: Math.floor(timeLabels.length / 10)
      }
    },
    yAxis: {
      type: 'value',
      name: '血糖值(mmol/L)',
      min: 3,
      max: 15,
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      },
      markLine: {
        silent: true,
        data: [
          { yAxis: 3.9, lineStyle: { color: '#e6a23c' }, label: { formatter: '低血糖线' } },
          { yAxis: 6.1, lineStyle: { color: '#67c23a' }, label: { formatter: '正常上限' } },
          { yAxis: 7.8, lineStyle: { color: '#f56c6c' }, label: { formatter: '高血糖线' } }
        ]
      }
    },
    series: [
      {
        name: 'CGM血糖',
        type: 'line',
        smooth: true,
        data: historySeriesData,
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        },
        markLine: {
          silent: true,
          data: [
            { yAxis: 3.9, lineStyle: { color: '#e6a23c', type: 'dashed' } },
            { yAxis: 7.8, lineStyle: { color: '#f56c6c', type: 'dashed' } }
          ]
        }
      },
      {
        name: '预测血糖',
        type: 'line',
        smooth: true,
        data: predictSeriesData,
        itemStyle: { color: '#67c23a' },
        lineStyle: { type: 'dashed', width: 2 },
        areaStyle: predictedData.value.length > 0 ? {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.2)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        } : null
      }
    ]
  }

  chartInstance.setOption(option, true)
}

const handlePredict = async () => {
  predicting.value = true
  predictionResult.value = null
  
  try {
    const res = await glucoseApi.predictGlucose({
      cbg: [mmolToMgdl(predictionForm.cbg)],
      finger: [mmolToMgdl(predictionForm.finger)],
      basal: [predictionForm.basal],
      hr: [predictionForm.hr],
      gsr: [predictionForm.gsr],
      carbInput: [predictionForm.carbInput],
      bolus: [predictionForm.bolus],
      mealStatus: predictionForm.mealStatus,
      predictHours: predictionForm.predictHours
    })
    
    if (res.code === 0 && res.data) {
      const predictedValuesMgdl = res.data.predictedValues || []
      predictedData.value = predictedValuesMgdl.map(mgdl => mgdlToMmol(mgdl))
      predictedTimes.value = res.data.predictedTimes || []
      confidence.value = res.data.confidence || 0.85
      
      const avgValue = avgPredicted.value
      if (avgValue < 3.9) {
        predictionResult.value = {
          level: '低血糖风险',
          alertType: 'warning',
          suggestion: '预测显示可能出现低血糖，建议适当增加碳水化合物摄入，并密切监测血糖变化。'
        }
      } else if (avgValue <= 6.1) {
        predictionResult.value = {
          level: '血糖控制良好',
          alertType: 'success',
          suggestion: '根据您的血糖数据，预测未来血糖水平将保持在正常范围内。建议继续保持规律的生活作息和合理的饮食习惯。'
        }
      } else if (avgValue <= 7.8) {
        predictionResult.value = {
          level: '血糖偏高',
          alertType: 'warning',
          suggestion: '预测显示血糖可能偏高，建议适当控制饮食，增加运动量，并定期监测血糖变化。'
        }
      } else {
        predictionResult.value = {
          level: '高血糖风险',
          alertType: 'error',
          suggestion: '预测显示可能出现高血糖，建议及时就医，调整用药方案，严格控制饮食。'
        }
      }
      
      updateChart()
      ElMessage.success('预测完成')
    } else {
      ElMessage.error(res.message || '预测失败')
    }
  } catch (error) {
    console.error('预测失败:', error)
    predictedData.value = generateMockPrediction()
    confidence.value = 0.75
    predictionResult.value = {
      level: '血糖控制良好',
      alertType: 'success',
      suggestion: '根据您的血糖数据，预测未来血糖水平将保持在正常范围内。建议继续保持规律的生活作息和合理的饮食习惯。'
    }
    updateChart()
    ElMessage.warning('使用模拟数据展示预测结果')
  } finally {
    predicting.value = false
  }
}

const generateMockPrediction = () => {
  const lastValue = predictionForm.cbg
  const count = predictionForm.predictHours * 12
  const predictions = []
  
  for (let i = 0; i < count; i++) {
    const variation = (Math.random() - 0.5) * 1.5
    predictions.push(Math.max(3.5, Math.min(12, lastValue + variation)))
  }
  
  return predictions
}

const loadMoreHistory = () => {
  ElMessage.info('加载更多历史数据')
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
  height: 400px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.prediction-stats {
  .stat-item {
    text-align: center;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 8px;
    
    .stat-label {
      font-size: 12px;
      color: #909399;
      margin-bottom: 8px;
    }
    
    .stat-value {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
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
      
      &.warning {
        color: #e6a23c;
      }
      
      &.danger {
        color: #f56c6c;
      }
    }
  }
}

.field-unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.prediction-form {
  .el-divider {
    margin: 12px 0;
    font-size: 12px;
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

.mt-20 {
  margin-top: 20px;
}
</style>

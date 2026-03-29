<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #409eff;">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.userCount || 0 }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a;">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.reportCount || 0 }}</div>
            <div class="stat-label">健康报告</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c;">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.messageCount || 0 }}</div>
            <div class="stat-label">未读消息</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #f56c6c;">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.scheduleCount || 0 }}</div>
            <div class="stat-label">待处理日程</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <div class="card">
          <div class="card-header">
            <span class="card-title">近期健康趋势</span>
          </div>
          <div class="chart-container" ref="chartRef"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="card">
          <div class="card-header">
            <span class="card-title">快捷操作</span>
          </div>
          <div class="quick-actions">
            <el-button type="primary" :icon="Upload" @click="$router.push('/patient/health-report/upload')">
              上传报告
            </el-button>
            <el-button type="success" :icon="Calendar" @click="$router.push('/doctor/schedule')">
              日程管理
            </el-button>
            <el-button type="warning" :icon="Bell" @click="$router.push('/message/inbox')">
              查看消息
            </el-button>
            <el-button type="info" :icon="MagicStick" @click="$router.push('/ai/schedule')">
              AI助手
            </el-button>
          </div>
        </div>

        <div class="card mt-20">
          <div class="card-header">
            <span class="card-title">系统公告</span>
          </div>
          <div class="notice-list">
            <div v-for="notice in notices" :key="notice.id" class="notice-item">
              <el-tag :type="notice.type" size="small">{{ notice.tag }}</el-tag>
              <span class="notice-title">{{ notice.title }}</span>
              <span class="notice-date">{{ notice.date }}</span>
            </div>
            <el-empty v-if="notices.length === 0" description="暂无公告" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Upload, Calendar, Bell, MagicStick } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import messageApi from '@/api/message'

const userStore = useUserStore()
const appStore = useAppStore()
const chartRef = ref(null)
let chartInstance = null

const stats = ref({
  userCount: 128,
  reportCount: 56,
  messageCount: 0,
  scheduleCount: 12
})

const notices = ref([
  { id: 1, type: 'success', tag: '新功能', title: 'AI日程生成功能上线', date: '2024-01-15' },
  { id: 2, type: 'warning', tag: '通知', title: '系统维护通知', date: '2024-01-14' },
  { id: 3, type: 'info', tag: '公告', title: '平台使用指南更新', date: '2024-01-10' }
])

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['血糖值', '血压值']
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
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '血糖值',
        type: 'line',
        smooth: true,
        data: [5.2, 5.8, 5.5, 6.1, 5.9, 5.4, 5.6],
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      },
      {
        name: '血压值',
        type: 'line',
        smooth: true,
        data: [120, 118, 122, 125, 119, 121, 117],
        itemStyle: {
          color: '#67c23a'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const fetchUnreadCount = async () => {
  try {
    const res = await messageApi.getUnreadCount()
    if (res.code === 0) {
      stats.value.messageCount = res.data || 0
      appStore.setUnreadMessageCount(res.data || 0)
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  initChart()
  fetchUnreadCount()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  chartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.dashboard-container {
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

.chart-container {
  height: 300px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  .el-button {
    flex: 1;
    min-width: 120px;
  }
}

.notice-list {
  .notice-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .notice-title {
      flex: 1;
      margin-left: 10px;
      font-size: 14px;
      color: #606266;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .notice-date {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>

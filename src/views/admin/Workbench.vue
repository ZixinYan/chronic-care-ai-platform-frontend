<template>
  <div class="admin-workbench">
    <div class="stats-row">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <el-icon :size="40"><User /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ stats.totalUsers || 0 }}</div>
                <div class="stats-label">注册用户总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <div class="stats-icon doctor">
                <el-icon :size="40"><Avatar /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ stats.totalDoctors || 0 }}</div>
                <div class="stats-label">医生数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <div class="stats-icon patient">
                <el-icon :size="40"><UserFilled /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ stats.totalPatients || 0 }}</div>
                <div class="stats-label">患者数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-content">
              <div class="stats-icon today">
                <el-icon :size="40"><TrendCharts /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ stats.todayNewUsers || 0 }}</div>
                <div class="stats-label">今日新增用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/users')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
            <el-button type="success" @click="$router.push('/admin/roles')">
              <el-icon><Setting /></el-icon>
              角色管理
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>角色分布</span>
            </div>
          </template>
          <div class="role-distribution">
            <div class="role-item">
              <div class="role-icon admin">
                <el-icon><Setting /></el-icon>
              </div>
              <span class="role-name">管理员</span>
              <el-progress 
                :percentage="getPercentage(stats.totalAdmins, stats.totalUsers)" 
                :stroke-width="18"
                status="exception"
              />
              <span class="role-count">{{ stats.totalAdmins || 0 }} 人</span>
            </div>
            <div class="role-item">
              <div class="role-icon doctor">
                <el-icon><Avatar /></el-icon>
              </div>
              <span class="role-name">医生</span>
              <el-progress 
                :percentage="getPercentage(stats.totalDoctors, stats.totalUsers)" 
                :stroke-width="18"
                status="success"
              />
              <span class="role-count">{{ stats.totalDoctors || 0 }} 人</span>
            </div>
            <div class="role-item">
              <div class="role-icon patient">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="role-name">患者</span>
              <el-progress 
                :percentage="getPercentage(stats.totalPatients, stats.totalUsers)" 
                :stroke-width="18"
              />
              <span class="role-count">{{ stats.totalPatients || 0 }} 人</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>系统信息</span>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="系统名称">慢病管理AI平台</el-descriptions-item>
        <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="当前时间">{{ currentTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { User, Avatar, UserFilled, TrendCharts, Setting } from '@element-plus/icons-vue'
import adminApi from '@/api/admin'

const stats = ref({
  totalUsers: 0,
  totalDoctors: 0,
  totalPatients: 0,
  totalAdmins: 0,
  todayNewUsers: 0,
  activeUsersToday: 0
})

const currentTime = ref('')
let timer = null

const getPercentage = (value, total) => {
  if (!total || total === 0) return 0
  return Math.round((value / total) * 100)
}

const updateTime = () => {
  currentTime.value = new Date().toLocaleString('zh-CN')
}

const fetchStats = async () => {
  try {
    const res = await adminApi.getSystemStats()
    if (res.code === 0) {
      stats.value = res.data || {}
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.admin-workbench {
  padding: 0;
}

.stats-card {
  .stats-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stats-icon {
    width: 70px;
    height: 70px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;

    &.total {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.doctor {
      background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    }

    &.patient {
      background: linear-gradient(135deg, #2193b0 0%, #6dd5ed 100%);
    }

    &.today {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
  }

  .stats-info {
    flex: 1;
  }

  .stats-value {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
  }

  .stats-label {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.role-distribution {
  .role-item {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .role-icon {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-right: 12px;
      flex-shrink: 0;

      &.admin {
        background: linear-gradient(135deg, #f56c6c 0%, #e6526b 100%);
      }

      &.doctor {
        background: linear-gradient(135deg, #67c23a 0%, #4caf50 100%);
      }

      &.patient {
        background: linear-gradient(135deg, #409eff 0%, #29b6f6 100%);
      }
    }

    .role-name {
      width: 50px;
      font-size: 14px;
      color: #606266;
      flex-shrink: 0;
    }

    .el-progress {
      flex: 1;
      margin: 0 16px;
    }

    .role-count {
      width: 60px;
      text-align: right;
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      flex-shrink: 0;
    }
  }
}
</style>

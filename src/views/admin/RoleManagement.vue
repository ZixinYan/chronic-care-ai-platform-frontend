<template>
  <div class="role-management">
    <div class="card">
      <div class="card-header">
        <span class="card-title">角色权限管理</span>
      </div>

      <el-alert
        title="角色说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <p>系统支持以下角色：</p>
          <ul style="margin: 8px 0; padding-left: 20px;">
            <li><strong>管理员(ADMIN)</strong>：拥有系统全部权限，可管理用户和角色</li>
            <li><strong>医生(DOCTOR)</strong>：可管理患者、查看报告、安排日程</li>
            <li><strong>患者(PATIENT)</strong>：可查看个人健康数据、上传报告、预约医生</li>
          </ul>
        </template>
      </el-alert>

      <el-table :data="roleList" stripe style="width: 100%">
        <el-table-column prop="code" label="角色代码" width="100" />
        <el-table-column prop="name" label="角色名称" width="120" />
        <el-table-column label="权限范围" min-width="300">
          <template #default="{ row }">
            <div class="permission-list">
              <el-tag 
                v-for="permission in getRolePermissions(row.code)" 
                :key="permission"
                type="info"
                style="margin: 2px"
              >
                {{ permission }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户数量" width="100">
          <template #default="{ row }">
            <el-badge :value="getUserCountByRole(row.code)" type="primary" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" style="margin-top: 20px">
      <div class="card-header">
        <span class="card-title">角色分配统计</span>
      </div>

      <el-row :gutter="20">
        <el-col :span="6" v-for="role in roleList" :key="role.code">
          <el-card shadow="hover" class="role-card">
            <div class="role-card-content">
              <div class="role-icon">
                <el-icon :size="40" :class="getRoleIconClass(role.code)">
                  <component :is="getRoleIcon(role.code)" />
                </el-icon>
              </div>
              <div class="role-info">
                <div class="role-name">{{ role.name }}</div>
                <div class="role-count">{{ getUserCountByRole(role.code) }} 人</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { User, Avatar, UserFilled, Setting } from '@element-plus/icons-vue'
import adminApi from '@/api/admin'

const roleList = ref([])
const userStats = ref({})

const rolePermissions = {
  1: ['患者管理', '日程管理', '报告审批', '请假申请', '查看患者信息'],
  2: ['查看健康报告', '上传报告', '血糖预测', '预约医生', '查看个人信息'],
  4: ['用户管理', '角色管理', '系统配置', '全部权限']
}

const getRolePermissions = (code) => {
  return rolePermissions[code] || []
}

const getUserCountByRole = (code) => {
  return userStats.value[code] || 0
}

const getRoleIcon = (code) => {
  const icons = {
    1: Avatar,
    2: User,
    4: Setting
  }
  return icons[code] || User
}

const getRoleIconClass = (code) => {
  const classes = {
    1: 'doctor-icon',
    2: 'patient-icon',
    4: 'admin-icon'
  }
  return classes[code] || ''
}

const fetchRoles = async () => {
  try {
    const res = await adminApi.getAllRoles()
    if (res.code === 0) {
      roleList.value = res.data || []
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const fetchUserStats = async () => {
  try {
    const res = await adminApi.getUsersList({ pageNum: 1, pageSize: 1000 })
    if (res.code === 0) {
      const stats = {}
      res.data?.users?.forEach(user => {
        user.roleCodes?.forEach(code => {
          stats[code] = (stats[code] || 0) + 1
        })
      })
      userStats.value = stats
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

onMounted(() => {
  fetchRoles()
  fetchUserStats()
})
</script>

<style lang="scss" scoped>
.role-management {
  padding: 0;
}

.permission-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.role-card {
  .role-card-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .role-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
  }

  .role-info {
    flex: 1;
  }

  .role-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .role-count {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }
}

.doctor-icon {
  color: #67c23a;
}

.patient-icon {
  color: #409eff;
}

.family-icon {
  color: #e6a23c;
}

.admin-icon {
  color: #f56c6c;
}
</style>

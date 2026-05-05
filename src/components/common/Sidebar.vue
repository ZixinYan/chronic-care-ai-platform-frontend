<template>
  <div class="sidebar-container" :class="{ collapsed: collapsed }">
    <div class="sidebar-logo">
      <el-icon class="logo-icon"><FirstAidKit /></el-icon>
      <span v-show="!collapsed" class="logo-text">慢病管理AI平台</span>
    </div>

    <el-scrollbar class="sidebar-menu">
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        :unique-opened="true"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item v-if="userStore.hasRole('ADMIN')" index="/workbench">
          <el-icon><HomeFilled /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>

        <template v-if="userStore.hasRole('ADMIN')">
          <el-sub-menu index="admin">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>管理功能</span>
            </template>
            <el-menu-item index="/admin/workbench">管理后台</el-menu-item>
            <el-menu-item index="/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/admin/roles">角色管理</el-menu-item>
            <el-menu-item index="/admin/leave-approval">休假审批</el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="userStore.hasRole('PATIENT')">
          <el-sub-menu index="patient">
            <template #title>
              <el-icon><User /></el-icon>
              <span>患者中心</span>
            </template>
            <el-menu-item index="/patient/glucose-prediction">血糖预测</el-menu-item>
            <el-menu-item index="/patient/health-report">报告解析</el-menu-item>
            <el-menu-item index="/patient/appointment">预约医生</el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="userStore.hasRole('DOCTOR')">
          <el-sub-menu index="doctor">
            <template #title>
              <el-icon><Avatar /></el-icon>
              <span>医生工作台</span>
            </template>
            <el-menu-item index="/doctor/schedule">日程管理</el-menu-item>
            <el-menu-item index="/doctor/leave">请假申请</el-menu-item>
            <el-menu-item index="/doctor/report-approval">报告审批</el-menu-item>
          </el-sub-menu>
        </template>

        <el-sub-menu index="message">
          <template #title>
            <el-icon><Message /></el-icon>
            <span>消息中心</span>
          </template>
          <el-menu-item index="/message/inbox">
            收件箱
            <el-badge v-if="appStore.unreadMessageCount > 0" :value="appStore.unreadMessageCount" class="menu-badge" />
          </el-menu-item>
          <el-menu-item index="/message/sent">发件箱</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="user">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>个人中心</span>
          </template>
          <el-menu-item index="/user/profile">个人信息</el-menu-item>
          <el-menu-item index="/user/avatar">修改头像</el-menu-item>
          <el-menu-item index="/user/password">修改密码</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { 
  HomeFilled, 
  User, 
  Avatar, 
  Message, 
  Setting,
  FirstAidKit 
} from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #304156;

  &.collapsed {
    .sidebar-logo {
      padding: 0;
      justify-content: center;

      .logo-text {
        display: none;
      }
    }
  }
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #263445;
  overflow: hidden;

  .logo-icon {
    font-size: 28px;
    color: #409eff;
    flex-shrink: 0;
  }

  .logo-text {
    margin-left: 12px;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
  }
}

.sidebar-menu {
  flex: 1;
  overflow: hidden;

  :deep(.el-menu) {
    border-right: none;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 50px;
    line-height: 50px;

    &:hover {
      background-color: #263445 !important;
    }
  }

  :deep(.el-menu-item.is-active) {
    background-color: #409eff !important;
    color: #fff !important;
  }

  :deep(.el-sub-menu .el-menu-item) {
    min-width: auto;
    padding-left: 50px !important;
  }
}

.menu-badge {
  margin-left: 10px;

  :deep(.el-badge__content) {
    transform: scale(0.8);
  }
}
</style>

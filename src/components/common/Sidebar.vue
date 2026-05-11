<template>
  <div class="sidebar-container" :class="{ collapsed: collapsed }">
    <div class="sidebar-logo">
      <div class="logo-icon-wrapper">
        <PlatformIcon :size="24" />
      </div>
      <transition name="logo-fade">
        <span v-show="!collapsed" class="logo-text">诊疗辅助系统</span>
      </transition>
    </div>

    <el-scrollbar class="sidebar-menu">
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        :unique-opened="true"
        background-color="transparent"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#fff"
        router
      >
        <template v-if="userStore.hasRole('ADMIN')">
          <el-sub-menu index="admin">
            <template #title>
              <div class="menu-icon-bg bg-gradient-orange">
                <el-icon><Setting /></el-icon>
              </div>
              <span class="menu-text">管理功能</span>
            </template>
            <el-menu-item index="/admin/users">
              <span class="sub-menu-dot"></span>
              <span class="menu-text">用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/roles">
              <span class="sub-menu-dot"></span>
              <span class="menu-text">角色管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/leave-approval">
              <span class="sub-menu-dot"></span>
              <span class="menu-text">休假审批</span>
            </el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="userStore.hasRole('PATIENT')">
          <el-sub-menu index="patient">
            <template #title>
              <div class="menu-icon-bg bg-gradient-green">
                <el-icon><User /></el-icon>
              </div>
              <span class="menu-text">患者中心</span>
            </template>
            <el-menu-item index="/patient/glucose-prediction">
              <span class="sub-menu-dot"></span>
              <span class="menu-text">血糖预测</span>
            </el-menu-item>
            <el-menu-item index="/patient/health-report">
              <span class="sub-menu-dot"></span>
              <span class="menu-text">报告解析</span>
            </el-menu-item>
            <el-menu-item index="/patient/appointment">
              <span class="sub-menu-dot"></span>
              <span class="menu-text">预约医生</span>
            </el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="userStore.hasRole('DOCTOR')">
          <el-sub-menu index="doctor">
            <template #title>
              <div class="menu-icon-bg bg-gradient-purple">
                <el-icon><Avatar /></el-icon>
              </div>
              <span class="menu-text">医生工作台</span>
            </template>
            <el-menu-item index="/doctor/schedule">
              <span class="sub-menu-dot"></span>
              <span class="menu-text">日程管理</span>
            </el-menu-item>
            <el-menu-item index="/doctor/leave">
              <span class="sub-menu-dot"></span>
              <span class="menu-text">请假申请</span>
            </el-menu-item>
            <el-menu-item index="/doctor/report-approval">
              <span class="sub-menu-dot"></span>
              <span class="menu-text">报告审批</span>
            </el-menu-item>
          </el-sub-menu>
        </template>

        <el-sub-menu index="message">
          <template #title>
            <div class="menu-icon-bg bg-gradient-cyan">
              <el-icon><Message /></el-icon>
            </div>
            <span class="menu-text">消息中心</span>
          </template>
          <el-menu-item index="/message/inbox">
            <span class="sub-menu-dot"></span>
            <span class="menu-text">收件箱</span>
            <el-badge v-if="appStore.unreadMessageCount > 0" :value="appStore.unreadMessageCount" class="menu-badge" />
          </el-menu-item>
          <el-menu-item index="/message/sent">
            <span class="sub-menu-dot"></span>
            <span class="menu-text">发件箱</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="user">
          <template #title>
            <div class="menu-icon-bg bg-gradient-pink">
              <el-icon><Setting /></el-icon>
            </div>
            <span class="menu-text">个人中心</span>
          </template>
          <el-menu-item index="/user/profile">
            <span class="sub-menu-dot"></span>
            <span class="menu-text">个人信息</span>
          </el-menu-item>
          <el-menu-item index="/user/avatar">
            <span class="sub-menu-dot"></span>
            <span class="menu-text">修改头像</span>
          </el-menu-item>
          <el-menu-item index="/user/password">
            <span class="sub-menu-dot"></span>
            <span class="menu-text">修改密码</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>

    <div class="sidebar-footer">
      <div class="footer-decoration"></div>
    </div>
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
  Setting
} from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import PlatformIcon from '@/components/common/PlatformIcon.vue'

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
  background: linear-gradient(180deg, #1a1f36 0%, #16213e 40%, #0f3460 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(ellipse at 20% 50%, rgba(64, 150, 255, 0.08) 0%, transparent 50%),
      radial-gradient(ellipse at 80% 20%, rgba(120, 80, 255, 0.06) 0%, transparent 50%),
      radial-gradient(ellipse at 50% 80%, rgba(0, 200, 255, 0.05) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
  }

  &.collapsed {
    .sidebar-logo {
      padding: 0;
      justify-content: center;

      .logo-text {
        display: none;
      }

      .logo-icon-wrapper {
        margin: 0;
      }
    }
  }
}

.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  overflow: hidden;
  position: relative;
  z-index: 1;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 16px;
    right: 16px;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(64, 150, 255, 0.3), transparent);
  }

  .logo-icon-wrapper {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #4096ff 0%, #69b1ff 50%, #95c8ff 100%);
    box-shadow: 0 4px 15px rgba(64, 150, 255, 0.4);
    flex-shrink: 0;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      inset: -1px;
      border-radius: 13px;
      background: linear-gradient(135deg, rgba(255,255,255,0.3), transparent);
      z-index: -1;
    }
  }

  .logo-icon {
    font-size: 22px;
    color: #fff;
  }

  .logo-text {
    margin-left: 12px;
    font-size: 16px;
    font-weight: 700;
    color: #fff;
    white-space: nowrap;
    letter-spacing: 1.5px;
    background: linear-gradient(135deg, #fff 0%, #91caff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  }
}

.sidebar-menu {
  flex: 1;
  overflow: hidden;
  position: relative;
  z-index: 1;

  :deep(.el-menu) {
    border-right: none;
    padding: 8px;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 48px;
    line-height: 48px;
    margin: 2px 0;
    border-radius: 10px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    padding-left: 12px !important;

    &:hover {
      background-color: rgba(255, 255, 255, 0.08) !important;
      transform: translateX(2px);
    }
  }

  :deep(.el-menu-item.is-active) {
    background: linear-gradient(135deg, rgba(64, 150, 255, 0.25) 0%, rgba(64, 150, 255, 0.15) 100%) !important;
    color: #fff !important;
    position: relative;
    box-shadow: 0 2px 12px rgba(64, 150, 255, 0.2);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background: linear-gradient(180deg, #4096ff, #69b1ff);
      border-radius: 0 3px 3px 0;
    }

    .menu-text {
      font-weight: 600;
    }
  }

  :deep(.el-sub-menu .el-menu-item) {
    min-width: auto;
    padding-left: 44px !important;
    height: 42px;
    line-height: 42px;
    margin: 1px 0;
  }

  :deep(.el-sub-menu__icon-arrow) {
    color: rgba(255, 255, 255, 0.5);
  }

  :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
    color: #fff !important;

    .menu-icon-bg {
      box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
    }
  }
}

.menu-icon-bg {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  flex-shrink: 0;
  transition: all 0.3s ease;

  .el-icon {
    font-size: 16px;
    color: #fff;
  }

  &.bg-gradient-blue {
    background: linear-gradient(135deg, #4096ff, #1677ff);
  }

  &.bg-gradient-orange {
    background: linear-gradient(135deg, #fa8c16, #ff7a45);
  }

  &.bg-gradient-green {
    background: linear-gradient(135deg, #52c41a, #73d13d);
  }

  &.bg-gradient-purple {
    background: linear-gradient(135deg, #722ed1, #9254de);
  }

  &.bg-gradient-cyan {
    background: linear-gradient(135deg, #13c2c2, #36cfc9);
  }

  &.bg-gradient-pink {
    background: linear-gradient(135deg, #eb2f96, #f759ab);
  }
}

.sub-menu-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: inline-block;
  margin-right: 10px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

:deep(.el-menu-item.is-active) .sub-menu-dot {
  background: #4096ff;
  box-shadow: 0 0 8px rgba(64, 150, 255, 0.6);
}

:deep(.el-menu-item:hover) .sub-menu-dot {
  background: rgba(255, 255, 255, 0.6);
}

.menu-text {
  font-size: 14px;
  letter-spacing: 0.3px;
}

.menu-badge {
  margin-left: 8px;

  :deep(.el-badge__content) {
    transform: scale(0.85);
    border: none;
    box-shadow: 0 2px 6px rgba(245, 108, 108, 0.4);
  }
}

.sidebar-footer {
  position: relative;
  z-index: 1;
  padding: 12px 16px;

  .footer-decoration {
    height: 3px;
    border-radius: 2px;
    background: linear-gradient(90deg, 
      rgba(64, 150, 255, 0.6), 
      rgba(114, 46, 209, 0.6), 
      rgba(19, 194, 194, 0.6), 
      rgba(64, 150, 255, 0.6)
    );
    background-size: 200% 100%;
    animation: gradientShift 4s ease infinite;
  }
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.logo-fade-enter-active {
  transition: opacity 0.3s ease 0.1s;
}

.logo-fade-leave-active {
  transition: opacity 0.1s ease;
}

.logo-fade-enter-from,
.logo-fade-leave-to {
  opacity: 0;
}
</style>

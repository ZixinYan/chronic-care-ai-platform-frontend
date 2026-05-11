<template>
  <div class="header-container">
    <div class="header-left">
      <div class="toggle-btn" @click="$emit('toggle-sidebar')">
        <el-icon :size="20">
          <Fold v-if="!appStore.sidebarCollapsed" />
          <Expand v-else />
        </el-icon>
      </div>
      <div class="header-greeting">
        <span class="greeting-text">{{ greetingText }}</span>
        <span class="greeting-name">{{ userStore.nickname || userStore.username }}</span>
      </div>
    </div>

    <div class="header-right">
      <el-badge :value="appStore.unreadMessageCount" :hidden="appStore.unreadMessageCount === 0" class="message-badge">
        <div class="header-icon-btn" @click="goToMessage">
          <el-icon :size="20"><Bell /></el-icon>
        </div>
      </el-badge>

      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="36" :src="userStore.avatar" class="user-avatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <span class="username">{{ userStore.nickname || userStore.username }}</span>
          <el-icon class="arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人信息
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Fold, 
  Expand, 
  Bell, 
  UserFilled, 
  ArrowDown, 
  User, 
  Lock, 
  SwitchButton 
} from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import messageApi from '@/api/message'

defineEmits(['toggle-sidebar'])

const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const userStore = useUserStore()

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，'
  if (hour < 9) return '早上好，'
  if (hour < 12) return '上午好，'
  if (hour < 14) return '中午好，'
  if (hour < 18) return '下午好，'
  return '晚上好，'
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'password':
      router.push('/user/password')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    authStore.clearAuth()
    userStore.clearUser()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}

const goToMessage = () => {
  router.push('/message/inbox')
}

const fetchUnreadCount = async () => {
  try {
    const res = await messageApi.getUnreadCount()
    if (res.code === 0) {
      appStore.setUnreadMessageCount(res.data || 0)
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

onMounted(() => {
  fetchUnreadCount()
})
</script>

<style lang="scss" scoped>
.header-container {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.toggle-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: #606266;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba(64, 150, 255, 0.1);
    color: #4096ff;
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.95);
  }
}

.header-greeting {
  display: flex;
  align-items: baseline;
  gap: 4px;

  .greeting-text {
    font-size: 14px;
    color: #86909c;
    font-weight: 400;
    letter-spacing: 0.3px;
  }

  .greeting-name {
    font-size: 15px;
    font-weight: 600;
    color: #1d2129;
    letter-spacing: 0.5px;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: #606266;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba(64, 150, 255, 0.1);
    color: #4096ff;
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }
}

.message-badge {
  cursor: pointer;

  :deep(.el-badge__content) {
    border: 2px solid #fff;
    box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba(64, 150, 255, 0.08);
  }

  .user-avatar {
    border: 2px solid rgba(64, 150, 255, 0.2);
    transition: border-color 0.3s;
  }

  &:hover .user-avatar {
    border-color: rgba(64, 150, 255, 0.5);
  }

  .username {
    font-size: 14px;
    font-weight: 500;
    color: #1d2129;
    letter-spacing: 0.3px;
  }

  .arrow {
    font-size: 12px;
    color: #909399;
    transition: transform 0.3s;
  }

  &:hover .arrow {
    transform: rotate(180deg);
  }
}
</style>

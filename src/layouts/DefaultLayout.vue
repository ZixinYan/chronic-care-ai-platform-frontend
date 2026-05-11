<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="layout-aside">
      <Sidebar :collapsed="appStore.sidebarCollapsed" />
    </el-aside>
    <el-container class="layout-main">
      <el-header class="layout-header">
        <Header @toggle-sidebar="toggleSidebar" />
      </el-header>
      <el-main class="layout-content">
        <Breadcrumb />
        <div class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useAuthStore } from '@/stores/auth'
import Header from '@/components/common/Header.vue'
import Sidebar from '@/components/common/Sidebar.vue'
import Breadcrumb from '@/components/common/Breadcrumb.vue'
import authApi from '@/api/auth'

const appStore = useAppStore()
const userStore = useUserStore()
const authStore = useAuthStore()

const sidebarWidth = computed(() => {
  return appStore.sidebarCollapsed ? '64px' : '220px'
})

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const fetchUserInfo = async () => {
  if (!authStore.isAuthenticated) {
    return
  }
  
  if (userStore.userInfo && userStore.roles.length > 0) {
    return
  }

  try {
    const res = await authApi.getCurrentUserInfo()
    if (res.code === 0 && res.data) {
      const userData = res.data
      userStore.setUser({
        id: userData.userId,
        username: userData.username,
        nickname: userData.nickname,
        phone: userData.phone,
        email: userData.email,
        gender: userData.gender,
        avatar: userData.avatarUrl,
        address: userData.address,
        birthday: userData.birthday,
        roles: userData.roles || [],
        permissions: userData.permissions || []
      })
      userStore.setRoles(userData.roles || [])
      userStore.setPermissions(userData.permissions || [])
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

let isFetching = false
onMounted(() => {
  if (!isFetching) {
    isFetching = true
    fetchUserInfo().finally(() => {
      isFetching = false
    })
  }
})
</script>

<style lang="scss" scoped>
.layout-container {
  width: 100%;
  height: 100vh;
}

.layout-aside {
  background: linear-gradient(180deg, #1a1f36 0%, #16213e 40%, #0f3460 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
  z-index: 2;
}

.layout-main {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f0f2f5;
}

.layout-header {
  height: 64px;
  padding: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 1px 8px rgba(0, 21, 41, 0.06);
  z-index: 10;
}

.layout-content {
  flex: 1;
  padding: 0;
  background-color: #f0f2f5;
  overflow: auto;
}

.main-content {
  padding: 20px;
  min-height: calc(100vh - 64px - 40px);
}

.fade-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>

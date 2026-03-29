<template>
  <div class="workbench-container">
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
    <AdminWorkbench v-else-if="isAdmin" />
    <el-tabs v-else-if="visibleTabs.length > 0" v-model="activeTab" type="border-card" @tab-change="handleTabChange">
      <el-tab-pane
        v-for="tab in visibleTabs"
        :key="tab.name"
        :label="tab.label"
        :name="tab.name"
      >
        <component :is="tab.component" />
      </el-tab-pane>
    </el-tabs>
    <div v-else class="no-role-container">
      <el-empty description="未获取到用户角色信息，请重新登录" />
      <el-button type="primary" @click="handleLogout">重新登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, shallowRef, defineAsyncComponent, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAuthStore } from '@/stores/auth'
import { Loading } from '@element-plus/icons-vue'
import AdminWorkbench from '@/views/admin/Workbench.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()

const loading = ref(true)
const activeTab = ref('')

const tabComponents = {
  Schedule: shallowRef(defineAsyncComponent(() => import('@/views/doctor/Schedule.vue'))),
  LeaveRequest: shallowRef(defineAsyncComponent(() => import('@/views/doctor/LeaveRequest.vue'))),
  ReportApproval: shallowRef(defineAsyncComponent(() => import('@/views/doctor/ReportApproval.vue'))),
  GlucosePrediction: shallowRef(defineAsyncComponent(() => import('@/views/patient/GlucosePrediction.vue'))),
  HealthReport: shallowRef(defineAsyncComponent(() => import('@/views/patient/HealthReport.vue'))),
  AppointmentDoctor: shallowRef(defineAsyncComponent(() => import('@/views/patient/AppointmentDoctor.vue'))),
  Patients: shallowRef(defineAsyncComponent(() => import('@/views/doctor/Patients.vue')))
}

const allTabs = {
  DOCTOR: [
    { name: 'schedule', label: '日程管理', component: tabComponents.Schedule },
    { name: 'leave', label: '请假申请', component: tabComponents.LeaveRequest },
    { name: 'report-approval', label: '报告审批', component: tabComponents.ReportApproval }
  ],
  PATIENT: [
    { name: 'glucose-prediction', label: '血糖预测', component: tabComponents.GlucosePrediction },
    { name: 'report-analysis', label: '报告解析', component: tabComponents.HealthReport },
    { name: 'appointment', label: '预约医生', component: tabComponents.AppointmentDoctor }
  ]
}

const roles = computed(() => {
  return userStore.roles || []
})

const isAdmin = computed(() => {
  return roles.value.includes('ADMIN')
})

const currentRole = computed(() => {
  const roleList = roles.value
  
  if (roleList.includes('ADMIN')) {
    return 'ADMIN'
  }
  if (roleList.includes('DOCTOR')) {
    return 'DOCTOR'
  }
  if (roleList.includes('PATIENT')) {
    return 'PATIENT'
  }
  return null
})

const visibleTabs = computed(() => {
  const role = currentRole.value
  return allTabs[role] || []
})

const handleTabChange = (tabName) => {
  const query = { ...route.query, tab: tabName }
  router.replace({ query })
}

const initTabs = () => {
  if (visibleTabs.value.length > 0) {
    const tabFromQuery = route.query.tab
    if (tabFromQuery && visibleTabs.value.some(t => t.name === tabFromQuery)) {
      activeTab.value = tabFromQuery
    } else {
      activeTab.value = visibleTabs.value[0].name
    }
  }
  loading.value = false
}

const handleLogout = () => {
  authStore.clearAuth()
  userStore.clearUser()
  router.push('/login')
}

watch(roles, (newRoles) => {
  if (newRoles && newRoles.length > 0) {
    initTabs()
  }
}, { immediate: true })

onMounted(() => {
  if (roles.value && roles.value.length > 0) {
    initTabs()
  } else {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.workbench-container {
  padding: 0;

  :deep(.el-tabs__content) {
    padding: 20px;
  }

  :deep(.el-tab-pane) {
    min-height: 400px;
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #909399;

  p {
    margin-top: 16px;
  }
}

.no-role-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}
</style>

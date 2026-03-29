import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const roles = ref([])
  const permissions = ref([])

  const isLoggedIn = computed(() => !!userInfo.value)
  const userId = computed(() => userInfo.value?.id || null)
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const phone = computed(() => userInfo.value?.phone || '')
  const email = computed(() => userInfo.value?.email || '')
  const currentRole = computed(() => roles.value[0] || '')

  const setUser = (user) => {
    userInfo.value = user
  }

  const setRoles = (roleList) => {
    roles.value = roleList || []
  }

  const setPermissions = (permissionList) => {
    permissions.value = permissionList || []
  }

  const hasRole = (role) => {
    return roles.value.includes(role)
  }

  const hasAnyRole = (roleList) => {
    return roleList.some(role => roles.value.includes(role))
  }

  const hasPermission = (permission) => {
    return permissions.value.includes(permission)
  }

  const hasAnyPermission = (permissionList) => {
    return permissionList.some(permission => permissions.value.includes(permission))
  }

  const updateUserInfo = (info) => {
    userInfo.value = { ...userInfo.value, ...info }
  }

  const clearUser = () => {
    userInfo.value = null
    roles.value = []
    permissions.value = []
  }

  return {
    userInfo,
    roles,
    permissions,
    isLoggedIn,
    userId,
    username,
    nickname,
    avatar,
    phone,
    email,
    currentRole,
    setUser,
    setRoles,
    setPermissions,
    hasRole,
    hasAnyRole,
    hasPermission,
    hasAnyPermission,
    updateUserInfo,
    clearUser
  }
}, {
  persist: true
})

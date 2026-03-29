import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref('')
  const refreshToken = ref('')
  const tokenType = ref('Bearer')

  const isAuthenticated = computed(() => !!accessToken.value)

  const setTokens = (tokens) => {
    accessToken.value = tokens.accessToken || ''
    refreshToken.value = tokens.refreshToken || ''
    tokenType.value = tokens.tokenType || 'Bearer'
  }

  const setAccessToken = (token) => {
    accessToken.value = token
  }

  const setRefreshToken = (token) => {
    refreshToken.value = token
  }

  const getAccessToken = () => {
    return accessToken.value
  }

  const getRefreshToken = () => {
    return refreshToken.value
  }

  const getAuthorizationHeader = () => {
    if (accessToken.value) {
      return `${tokenType.value} ${accessToken.value}`
    }
    return ''
  }

  const clearAuth = () => {
    accessToken.value = ''
    refreshToken.value = ''
    tokenType.value = 'Bearer'
  }

  return {
    accessToken,
    refreshToken,
    tokenType,
    isAuthenticated,
    setTokens,
    setAccessToken,
    setRefreshToken,
    getAccessToken,
    getRefreshToken,
    getAuthorizationHeader,
    clearAuth
  }
}, {
  persist: true
})

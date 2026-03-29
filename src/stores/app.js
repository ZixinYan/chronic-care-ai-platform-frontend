import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const loading = ref(false)
  const theme = ref('light')
  const language = ref('zh-CN')
  const breadcrumbList = ref([])
  const unreadMessageCount = ref(0)

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setSidebarCollapsed = (collapsed) => {
    sidebarCollapsed.value = collapsed
  }

  const setLoading = (status) => {
    loading.value = status
  }

  const setTheme = (newTheme) => {
    theme.value = newTheme
  }

  const setLanguage = (lang) => {
    language.value = lang
  }

  const setBreadcrumb = (list) => {
    breadcrumbList.value = list
  }

  const setUnreadMessageCount = (count) => {
    unreadMessageCount.value = count
  }

  const incrementUnreadCount = () => {
    unreadMessageCount.value++
  }

  const decrementUnreadCount = () => {
    if (unreadMessageCount.value > 0) {
      unreadMessageCount.value--
    }
  }

  return {
    sidebarCollapsed,
    loading,
    theme,
    language,
    breadcrumbList,
    unreadMessageCount,
    toggleSidebar,
    setSidebarCollapsed,
    setLoading,
    setTheme,
    setLanguage,
    setBreadcrumb,
    setUnreadMessageCount,
    incrementUnreadCount,
    decrementUnreadCount
  }
}, {
  persist: {
    paths: ['sidebarCollapsed', 'theme', 'language']
  }
})

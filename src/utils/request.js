import axios from 'axios'
import JSONBig from 'json-bigint'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'

const JSONBigNative = JSONBig({ 
  useNativeBigInt: false,
  alwaysParseAsBig: false,
  constructorAction: 'preserve'
})

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const instance = axios.create({
  baseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  },
  transformResponse: [
    function (data) {
      if (typeof data === 'string') {
        try {
          return JSONBigNative.parse(data)
        } catch (e) {
          return data
        }
      }
      return data
    }
  ]
})

instance.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    const token = authStore.getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    console.log(`%c[API Request] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`, 'color: #4CAF50; font-weight: bold', {
      params: config.params,
      data: config.data
    })
    return config
  },
  (error) => {
    console.error('%c[API Request Error]', 'color: #f44336; font-weight: bold', error)
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response) => {
    console.log(`%c[API Response] ${response.config.method?.toUpperCase()} ${response.config.baseURL}${response.config.url}`, 'color: #2196F3; font-weight: bold', response.data)
    const { data } = response
    if (data.code === 0) {
      return data
    } else {
      ElMessage.error(data.msg || data.message || '请求失败')
      return Promise.reject(data)
    }
  },
  async (error) => {
    const { response } = error
    console.error(`%c[API Error] ${error.config?.method?.toUpperCase()} ${error.config?.baseURL}${error.config?.url}`, 'color: #f44336; font-weight: bold', {
      status: response?.status,
      message: error.message,
      data: response?.data
    })
    if (response) {
      switch (response.status) {
        case 401: {
          const authStore = useAuthStore()
          const refreshToken = authStore.getRefreshToken()
          if (refreshToken) {
            try {
              const res = await axios.post(`${baseURL}/auth/refresh`, {
                refreshToken
              })
              if (res.data.code === 0) {
                authStore.setTokens(res.data.data)
                return instance(response.config)
              }
            } catch (refreshError) {
              authStore.clearAuth()
              router.push('/login')
              ElMessage.error('登录已过期，请重新登录')
            }
          } else {
            authStore.clearAuth()
            router.push('/login')
            ElMessage.error('请先登录')
          }
          break
        }
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default instance

export const get = (url, params, config = {}) => {
  return instance.get(url, { params, ...config })
}

export const post = (url, data, config = {}) => {
  return instance.post(url, data, config)
}

export const put = (url, data, config = {}) => {
  return instance.put(url, data, config)
}

export const del = (url, config = {}) => {
  return instance.delete(url, config)
}

export const uploadFile = (url, file, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  return instance.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
}

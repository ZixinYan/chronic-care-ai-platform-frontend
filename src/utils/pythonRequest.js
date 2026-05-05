import axios from 'axios'
import { ElMessage } from 'element-plus'

const pythonBaseURL = import.meta.env.VITE_PYTHON_API_BASE_URL || '/python-api'

const pythonInstance = axios.create({
  baseURL: pythonBaseURL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

pythonInstance.interceptors.request.use(
  (config) => {
    console.log(`%c[Python API Request] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`, 'color: #FF9800; font-weight: bold', {
      params: config.params,
      data: config.data
    })
    return config
  },
  (error) => {
    console.error('%c[Python API Request Error]', 'color: #f44336; font-weight: bold', error)
    return Promise.reject(error)
  }
)

pythonInstance.interceptors.response.use(
  (response) => {
    console.log(`%c[Python API Response] ${response.config.method?.toUpperCase()} ${response.config.baseURL}${response.config.url}`, 'color: #4CAF50; font-weight: bold', response.data)
    return response.data
  },
  (error) => {
    const { response } = error
    console.error(`%c[Python API Error] ${error.config?.method?.toUpperCase()} ${error.config?.baseURL}${error.config?.url}`, 'color: #f44336; font-weight: bold', {
      status: response?.status,
      message: error.message,
      data: response?.data
    })
    if (response) {
      const detail = response.data?.detail || response.data?.message || ''
      switch (response.status) {
        case 400:
          ElMessage.error(`请求参数错误: ${detail}`)
          break
        case 503:
          ElMessage.error('预测模型尚未加载，请稍后重试')
          break
        case 500:
          ElMessage.error('预测服务内部错误')
          break
        default:
          ElMessage.error(detail || '预测服务请求失败')
      }
    } else {
      ElMessage.error('无法连接预测服务，请检查服务是否启动')
    }
    return Promise.reject(error)
  }
)

export const pythonPost = (url, data, config = {}) => {
  return pythonInstance.post(url, data, config)
}

export const pythonGet = (url, params, config = {}) => {
  return pythonInstance.get(url, { params, ...config })
}

export default pythonInstance

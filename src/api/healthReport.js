import { get, post, uploadFile } from '@/utils/request'

export const healthReportApi = {
  uploadReport(data, file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    Object.keys(data).forEach(key => {
      formData.append(key, data[key])
    })
    return post('/health/report/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (onProgress) {
          onProgress(Math.round((e.loaded * 100) / e.total))
        }
      }
    })
  },

  getReportList(params) {
    return get('/health/report/list', params)
  },

  getReportDetail(reportId) {
    return get('/health/report/detail', { reportId })
  },

  processReport(data) {
    return post('/health/report/process', data)
  }
}

export default healthReportApi

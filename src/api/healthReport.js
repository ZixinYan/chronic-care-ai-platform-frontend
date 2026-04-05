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

  saveTextReport(data) {
    return post('/health/report/save-text', data)
  },

  getReportList(params) {
    return get('/health/report/list', params)
  },

  getPendingApprovalList(params) {
    return get('/health/report/pending-approval', params)
  },

  getReportDetail(reportId) {
    return get('/health/report/detail', { reportId })
  },

  processReport(data) {
    return post('/doctor/report/process', data)
  },

  getRecommendedDoctors(reportId) {
    return get('/health/report/recommended-doctors', { reportId })
  },

  sendReportToDoctor(reportId, doctorId) {
    return post('/health/report/send-to-doctor', { reportId, doctorId })
  }
}

export default healthReportApi

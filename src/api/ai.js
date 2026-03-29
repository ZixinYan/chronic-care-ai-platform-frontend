import { post } from '@/utils/request'

export const aiApi = {
  generateSchedule(data) {
    return post('/ai/schedule/generate', data)
  },

  generateMedicalRecord(data) {
    return post('/ai/medical-record/generate', data)
  }
}

export default aiApi

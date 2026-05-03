import { post } from '@/utils/request'

export const glucoseApi = {
  predictGlucose(data) {
    return post('/api/v1/glucose/predict', {
      cbg: data.cbg,
      finger: data.finger,
      basal: data.basal,
      hr: data.hr,
      gsr: data.gsr,
      carbInput: data.carbInput,
      bolus: data.bolus,
      mealStatus: data.mealStatus,
      predictHours: data.predictHours
    })
  }
}

export default glucoseApi

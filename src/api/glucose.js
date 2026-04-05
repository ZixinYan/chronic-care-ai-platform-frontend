import { post } from '@/utils/request'

export const glucoseApi = {
  predictGlucose(data) {
    return post('/glucose/predict', {
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
  },

  predictGlucoseSimple(data) {
    return post('/glucose/predict/simple', data.cbg, {
      params: {
        mealStatus: data.mealStatus || 1,
        predictHours: data.predictHours || 3
      }
    })
  }
}

export default glucoseApi

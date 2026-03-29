import { get, post } from '@/utils/request'

export const userApi = {
  getUserInfo(userId) {
    return get(`/user/info/${userId}`)
  },

  updateUserInfo(data) {
    return post('/auth/update-user', { updateData: data })
  },

  updatePassword(data) {
    return post('/account/password/update', data)
  },

  updatePhone(data) {
    return post('/account/phone/update', data)
  },

  updateEmail(data) {
    return post('/account/email/update', data)
  },

  getDoctorInfo(doctorId) {
    return get('/account/doctor/info', { doctorId })
  },

  getPatientInfo(patientId) {
    return get('/account/patient/info', { patientId })
  },

  getMyPatients(doctorId, params) {
    return post('/account/patients', { doctorId, ...params })
  },

  getAllDoctors() {
    return get('/account/doctors/all')
  }
}

export default userApi

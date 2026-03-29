import { get, post } from '@/utils/request'

export const doctorApi = {
  addSchedule(data) {
    return post('/doctor/workbench/schedule/add', { schedule: data })
  },

  getScheduleList(params) {
    return get('/doctor/workbench/schedule/list', params)
  },

  getScheduleDetail(scheduleId) {
    return get('/doctor/workbench/schedule/detail', { scheduleId })
  },

  completeSchedule(data) {
    return post('/doctor/workbench/schedule/complete', data)
  },

  cancelSchedule(scheduleId, reason) {
    return post('/doctor/workbench/schedule/cancel', null, {
      params: { scheduleId, reason }
    })
  },

  updateScheduleStatus(scheduleId, status) {
    return post('/doctor/workbench/schedule/status', null, {
      params: { scheduleId, status }
    })
  },

  getMyPatients(params) {
    return post('/doctor/patient/patients', params)
  },

  addLeave(data) {
    return post('/doctor/leave/add', data)
  },

  getLeaveList(params) {
    return get('/doctor/leave/list', params)
  },

  deleteLeave(leaveId) {
    return post('/doctor/leave/delete', null, {
      params: { leaveId }
    })
  },

  updateLeave(data) {
    return post('/doctor/leave/update', data)
  }
}

export default doctorApi

import { get, post, put, del } from '@/utils/request'

export const adminApi = {
  getUsersList(params) {
    return get('/auth/admin/users', params)
  },

  updateUserRoles(userId, roleCodes) {
    return put(`/auth/admin/users/${userId}/roles`, roleCodes)
  },

  deleteUsers(userIds) {
    return del('/auth/admin/users', { data: userIds })
  },

  getAllRoles() {
    return get('/auth/admin/roles')
  },

  getSystemStats() {
    return get('/auth/admin/stats')
  },

  getPendingLeaves(params) {
    return get('/doctor/leave/admin/pending', params)
  },

  approveLeave(data) {
    return post('/doctor/leave/admin/approve', data)
  }
}

export default adminApi

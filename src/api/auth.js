import { post, get } from '@/utils/request'

export const authApi = {
  login(data) {
    return post('/auth/login', data)
  },

  register(data) {
    return post('/auth/register', data)
  },

  sendSmsCode(phone) {
    return post('/auth/sms/code', { phone })
  },

  refreshToken(refreshToken) {
    return post('/auth/refresh', { refreshToken })
  },

  logout(token) {
    return post('/auth/logout', { token })
  },

  validateToken(token) {
    return post('/auth/validate', { token })
  },

  forgotPassword(data) {
    return post('/auth/forgot-password', data)
  },

  updateUserInfo(data) {
    return post('/auth/update-user', data)
  },

  getCurrentUserInfo() {
    return get('/auth/user-info')
  },

  uploadAvatar(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    return post('/auth/upload-avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (onProgress) {
          onProgress(Math.round((e.loaded * 100) / e.total))
        }
      }
    })
  }
}

export default authApi

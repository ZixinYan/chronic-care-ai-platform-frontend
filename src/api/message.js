import { get, post } from '@/utils/request'

export const messageApi = {
  getInbox(params) {
    return get('/message/inbox', params)
  },

  getSent(params) {
    return get('/message/sent', params)
  },

  getDetail(messageId) {
    return get('/message/detail', { messageId })
  },

  markAsRead(messageId) {
    return post('/message/read', null, {
      params: { messageId }
    })
  },

  batchMarkAsRead(messageIds) {
    return post('/message/batch-read', messageIds)
  },

  deleteMessage(messageId) {
    return post('/message/delete', null, {
      params: { messageId }
    })
  },

  batchDelete(messageIds) {
    return post('/message/batch-delete', messageIds)
  },

  getUnreadCount() {
    return get('/message/unread-count')
  },

  sendEmail(content, receiverId, title) {
    return post('/message/send_email', null, {
      params: { message: content, receiverId, title }
    })
  }
}

export default messageApi

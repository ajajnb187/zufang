import request from '@/utils/request'

// 获取用户通知列表
export const getUserNotifications = (params) => {
  return request.get('/api/notifications/user', { params })
}

// 获取未读通知数量
export const getUnreadNotificationCount = () => {
  return request.get('/api/notifications/unread-count')
}

// 标记通知为已读
export const markNotificationAsRead = (notificationId) => {
  return request.put(`/api/notifications/${notificationId}/read`)
}

// 标记所有通知为已读
export const markAllNotificationsAsRead = () => {
  return request.put('/api/notifications/read-all')
}

// 删除通知
export const deleteNotification = (notificationId) => {
  return request.delete(`/api/notifications/${notificationId}`)
}

// 批量发送通知（管理员）
export const sendBulkNotification = (data) => {
  return request.post('/api/admin/notifications/bulk-send', data)
}

// 发送系统通知
export const sendSystemNotification = (data) => {
  return request.post('/api/notifications/system', data)
}

// 获取通知模板列表
export const getNotificationTemplates = () => {
  return request.get('/api/notifications/templates')
}

// 创建通知模板
export const createNotificationTemplate = (data) => {
  return request.post('/api/notifications/templates', data)
}

// 更新通知模板
export const updateNotificationTemplate = (templateId, data) => {
  return request.put(`/api/notifications/templates/${templateId}`, data)
}

// 删除通知模板
export const deleteNotificationTemplate = (templateId) => {
  return request.delete(`/api/notifications/templates/${templateId}`)
}

// 获取通知统计数据
export const getNotificationStats = (params) => {
  return request.get('/api/notifications/stats', { params })
}

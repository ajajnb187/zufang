import request from '@/utils/request'

// 获取管理员仪表板数据
export const getDashboardData = () => {
  return request.get('/api/admin/dashboard')
}

// 获取待审核房源列表
export const getPendingAuditHouses = (params) => {
  return request.get('/api/admin/houses/pending', { params })
}

// 审核房源
export const auditHouse = (houseId, approved, opinion) => {
  return request.post(`/api/admin/houses/${houseId}/audit`, null, { 
    params: { approved, opinion } 
  })
}

// 获取小区待审核认证申请
export const getCommunityPendingVerifications = (communityId, params) => {
  return request.get(`/api/admin/verifications/community/${communityId}/pending`, { params })
}

// 获取平台待审核认证申请
export const getPlatformPendingVerifications = (params) => {
  return request.get('/api/admin/verifications/platform/pending', { params })
}

// 小区管理员审核认证申请
export const communityAdminAudit = (verificationId, approved, opinion) => {
  return request.post(`/api/admin/verifications/${verificationId}/community-audit`, null, { 
    params: { approved, opinion } 
  })
}

// 平台管理员审核认证申请
export const platformAdminAudit = (verificationId, approved, opinion) => {
  return request.post(`/api/admin/verifications/${verificationId}/platform-audit`, null, { 
    params: { approved, opinion } 
  })
}

// 获取待审核合同列表
export const getPendingContracts = (params) => {
  return request.get('/api/admin/contracts/pending', { params })
}

// 审核合同
export const auditContract = (contractId, approved, opinion) => {
  return request.post(`/api/admin/contracts/${contractId}/audit`, null, { 
    params: { approved, opinion } 
  })
}

// 获取论坛内容管理列表
export const getForumPosts = (params) => {
  return request.get('/api/admin/forum/posts', { params })
}

// 获取论坛回复列表
export const getForumReplies = (postId, params) => {
  return request.get(`/api/admin/forum/posts/${postId}/replies`, { params })
}

// 删除论坛帖子
export const deleteForumPost = (postId, reason) => {
  return request.delete(`/api/admin/forum/posts/${postId}`, { params: { reason } })
}

// 删除论坛回复
export const deleteForumReply = (replyId, reason) => {
  return request.delete(`/api/admin/forum/replies/${replyId}`, { params: { reason } })
}

// 批量发送通知
export const sendBulkNotification = (userIds, notificationType, title, content, relatedType = null, relatedId = null) => {
  return request.post('/api/admin/notifications/batch-send', null, { 
    params: { 
      userIds: userIds.join(','), // 将数组转为逗号分隔字符串
      notificationType, 
      title, 
      content, 
      relatedType, 
      relatedId 
    } 
  })
}

// 获取系统统计数据
export const getSystemStats = (params) => {
  return request.get('/api/admin/stats/system', { params })
}

// 获取平台数据统计
export const getPlatformStats = (params) => {
  return request.get('/api/admin/stats/platform', { params })
}

// 获取用户活跃度统计
export const getUserActivityStats = (params) => {
  return request.get('/api/admin/stats/user-activity', { params })
}

// 获取系统日志查询
export const getSystemLogs = (params) => {
  return request.get('/api/admin/logs', { params })
}

// 导出数据
export const exportData = (type, params) => {
  return request.get(`/api/admin/export/${type}`, { params, responseType: 'blob' })
}

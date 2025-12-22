import request from '@/utils/request'

// 分页查询用户列表
export const getUserList = (params) => {
  return request.get('/api/auth/users', { params })
}

// 获取用户统计信息
export const getUserStats = () => {
  return request.get('/api/admin/stats/user-activity')
}

// 更新用户状态（启用/禁用）
export const updateUserStatus = (userId, status) => {
  return request.put(`/api/auth/users/${userId}/status`, null, {
    params: { status }
  })
}

// 获取用户详细档案
export const getUserProfile = (userId) => {
  return request.get(`/api/auth/users/${userId}`)
}

// 更新用户信用评分
export const updateUserCreditScore = (userId, creditScore) => {
  return request.put(`/api/user/${userId}/credit-score`, null, {
    params: { creditScore }
  })
}

// 获取信用评分排行榜
export const getCreditRanking = (limit = 10) => {
  return request.get('/api/user/credit-ranking', { params: { limit } })
}

// 根据小区查询已认证用户
export const getVerifiedUsersByCommunity = (communityId, params) => {
  return request.get(`/api/user/community/${communityId}/verified`, { params })
}

// 重置用户密码
export const resetUserPassword = (userId, newPassword) => {
  return request.put(`/api/auth/users/${userId}/password`, { newPassword })
}

// 删除用户（软删除）
export const deleteUser = (userId) => {
  return request.delete(`/api/auth/users/${userId}`)
}

// 批量操作用户
export const batchUpdateUserStatus = (userIds, status) => {
  return request.post('/api/user/batch-status', {
    userIds,
    status
  })
}

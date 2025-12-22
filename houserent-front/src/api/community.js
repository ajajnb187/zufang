import request from '@/utils/request'

// 申请小区认证
export const applyVerification = (data) => {
  return request.post('/api/community-verification/apply', data)
}

// 小区管理员审核认证申请
export const communityAdminAudit = (verificationId, data) => {
  return request.post(`/api/admin/verifications/${verificationId}/community-audit`, null, {
    params: {
      approved: data.status === 'approved',
      opinion: data.opinion
    }
  })
}

// 平台管理员审核认证申请
export const platformAdminAudit = (verificationId, data) => {
  return request.post(`/api/admin/verifications/${verificationId}/platform-audit`, null, {
    params: {
      approved: data.status === 'approved',
      opinion: data.opinion
    }
  })
}

// 查询用户在指定小区的认证状态
export const getUserCommunityVerification = (communityId) => {
  return request.get(`/api/community-verification/status/${communityId}`)
}

// 分页查询用户的认证记录
export const getUserVerifications = (params) => {
  return request.get('/api/community-verification/user-records', { params })
}

// 分页查询待审核的认证申请（小区管理员）
export const getCommunityPendingVerifications = (communityId, params) => {
  return request.get(`/api/admin/verifications/community/${communityId}/pending`, { params })
}

// 分页查询待平台审核的认证申请
export const getPlatformPendingVerifications = (params) => {
  return request.get('/api/admin/verifications/platform/pending', { params })
}

// 获取认证详情
export const getVerificationDetail = (verificationId) => {
  return request.get(`/api/community-verification/detail/${verificationId}`)
}

// 查询认证统计信息
export const getVerificationStats = (communityId = null) => {
  const params = communityId ? { communityId } : {}
  return request.get('/api/community-verification/stats', { params })
}

// 查询用户已认证的小区列表
export const getUserApprovedCommunities = () => {
  return request.get('/api/community-verification/approved-communities')
}

// 检查用户是否已在小区认证通过
export const isUserVerifiedInCommunity = (communityId) => {
  return request.get(`/api/community-verification/is-verified/${communityId}`)
}

// 撤回认证申请
export const withdrawVerification = (verificationId) => {
  return request.delete(`/api/community-verification/withdraw/${verificationId}`)
}

// 获取小区列表
export const getCommunityList = (params) => {
  return request.get('/api/admin/communities', { params })
}

// 获取小区详情
export const getCommunityDetail = (communityId) => {
  return request.get(`/api/admin/communities/${communityId}`)
}

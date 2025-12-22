import request from '@/utils/request'

// 获取小区管理员仪表盘数据
export const getCommunityDashboard = (communityId) => {
  return request.get(`/api/admin/dashboard/community/${communityId}`)
}

// 认证审核相关接口
export const getCommunityPendingVerifications = (communityId, params) => {
  return request.get(`/api/admin/verifications/community/${communityId}/pending`, { params })
}

export const communityAdminAudit = (verificationId, approved, opinion) => {
  return request.post(`/api/admin/verifications/${verificationId}/community-audit`, null, { 
    params: { approved, opinion } 
  })
}

// 房源审核相关接口
export const getPendingAuditHouses = (communityId, params) => {
  return request.get('/api/admin/houses/pending', { 
    params: { ...params, communityId } 
  })
}

export const auditHouse = (houseId, approved, opinion) => {
  return request.post(`/api/admin/houses/${houseId}/audit`, null, { 
    params: { approved, opinion } 
  })
}

// 举报处理相关接口
export const getCommunityReports = (communityId, params) => {
  return request.get(`/api/admin/reports/community/${communityId}`, { params })
}

export const handleReport = (reportId, data) => {
  return request.post(`/api/admin/reports/${reportId}/handle`, null, {
    params: { action: data.action, reason: data.reason }
  })
}

// 合同审核相关接口
export const getPendingContracts = (communityId, params) => {
  return request.get('/api/admin/contracts/pending', { 
    params: { ...params, communityId } 
  })
}

// 获取小区合同列表（支持状态筛选）
export const getCommunityContracts = (communityId, params) => {
  return request.get('/api/admin/contracts/community', { 
    params: { ...params, communityId } 
  })
}

export const auditContract = (contractId, approved, opinion) => {
  return request.post(`/api/admin/contracts/${contractId}/audit`, null, { 
    params: { approved, opinion } 
  })
}

export const terminateContract = (contractId, data) => {
  return request.post(`/api/admin/contracts/${contractId}/terminate`, null, {
    params: { reason: data.reason }
  })
}

export const auditTermination = (contractId, approved, opinion) => {
  return request.post(`/api/admin/contracts/${contractId}/audit-termination`, null, {
    params: { approved, opinion }
  })
}

// 配套维护相关接口
export const getCommunityFacilities = (communityId, params) => {
  return request.get(`/api/admin/facilities/community/${communityId}`, { params })
}

export const addFacility = (communityId, data) => {
  return request.post(`/api/admin/facilities/community/${communityId}`, data)
}

export const updateFacility = (facilityId, data) => {
  return request.put(`/api/admin/facilities/${facilityId}`, data)
}

export const deleteFacility = (facilityId) => {
  return request.delete(`/api/admin/facilities/${facilityId}`)
}

// 配套反馈相关接口
export const getFacilityFeedbacks = (communityId, params) => {
  return request.get(`/api/admin/facilities/feedbacks/community/${communityId}`, { params })
}

export const handleFacilityFeedback = (feedbackId, action, reply) => {
  return request.post(`/api/admin/facilities/feedbacks/${feedbackId}/handle`, null, {
    params: { action, reply }
  })
}

// 小区用户管理
export const getCommunityUsers = (communityId, params) => {
  return request.get(`/api/admin/users/community/${communityId}`, { params })
}

// 小区统计数据
export const getCommunityStats = (communityId) => {
  return request.get(`/api/admin/stats/community/${communityId}`)
}

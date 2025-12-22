import request from '@/utils/request'

// 获取平台管理员仪表盘数据
export const getDashboardData = () => {
  return request.get('/api/admin/dashboard')
}

// 用户管理相关接口
export const getUserList = (params) => {
  return request.get('/api/admin/users', { params })
}

export const auditUserAuth = (userId, approved, opinion) => {
  return request.post(`/api/admin/users/${userId}/auth-audit`, null, {
    params: { approved, opinion }
  })
}

export const toggleUserStatus = (userId, banned) => {
  return request.post(`/api/admin/users/${userId}/toggle-status`, null, {
    params: { banned }
  })
}

// 小区管理相关接口
export const getCommunityList = (params) => {
  return request.get('/api/admin/communities', { params })
}

export const addCommunity = (data) => {
  return request.post('/api/admin/communities', data)
}

export const updateCommunity = (communityId, data) => {
  return request.put(`/api/admin/communities/${communityId}`, data)
}

export const deleteCommunity = (communityId) => {
  return request.delete(`/api/admin/communities/${communityId}`)
}

// 小区管理员分配相关接口
export const getUnassignedCommunityAdmins = () => {
  return request.get('/api/admin/admins/unassigned-community')
}

export const assignCommunityToAdmin = (adminId, communityId) => {
  return request.post(`/api/admin/admins/${adminId}/assign-community`, null, {
    params: { communityId }
  })
}

// 数据监控相关接口
export const getSystemStats = (params) => {
  return request.get('/api/admin/stats/system', { params })
}

export const getPlatformStats = (params) => {
  return request.get('/api/admin/stats/platform', { params })
}

export const getUserActivityStats = (params) => {
  return request.get('/api/admin/stats/user-activity', { params })
}

export const getSystemLogs = (params) => {
  return request.get('/api/admin/logs', { params })
}

// 系统设置相关接口
export const getContractTemplates = (params) => {
  return request.get('/api/admin/contract-templates', { params })
}

export const addContractTemplate = (data) => {
  return request.post('/api/admin/contract-templates', data)
}

export const updateContractTemplate = (templateId, data) => {
  return request.put(`/api/admin/contract-templates/${templateId}`, data)
}

export const deleteContractTemplate = (templateId) => {
  return request.delete(`/api/admin/contract-templates/${templateId}`)
}

export const uploadRegulation = (data) => {
  return request.post('/api/admin/regulations/upload', data)
}

export const getRegulationList = (params) => {
  return request.get('/api/admin/regulations', { params })
}

// 导出数据
export const exportData = (type, params) => {
  return request.get(`/api/admin/export/${type}`, { 
    params, 
    responseType: 'blob' 
  })
}

// 平台统计数据接口
export const getPendingAuditStats = () => {
  return request.get('/api/admin/stats/pending-audits')
}

export const getTodayStats = () => {
  return request.get('/api/admin/stats/today')
}

export const getCommunityRanking = () => {
  return request.get('/api/admin/stats/community-ranking')
}

export const getSystemHealth = () => {
  return request.get('/api/admin/system/health')
}

// ==================== 违规处理接口 ====================

// 获取违规列表
export const getViolationList = (params) => {
  return request.get('/api/admin/violations', { params })
}

// 获取违规详情
export const getViolationDetail = (violationId) => {
  return request.get(`/api/admin/violations/${violationId}`)
}

// 获取违规统计
export const getViolationStats = () => {
  return request.get('/api/admin/violations/stats')
}

// 警告用户
export const warnViolation = (violationId, data) => {
  return request.post(`/api/admin/violations/${violationId}/warn`, data)
}

// 禁言用户
export const muteViolation = (violationId, data) => {
  return request.post(`/api/admin/violations/${violationId}/mute`, data)
}

// 封禁用户
export const banViolation = (violationId, data) => {
  return request.post(`/api/admin/violations/${violationId}/ban`, data)
}

// 驳回违规
export const dismissViolation = (violationId, data) => {
  return request.post(`/api/admin/violations/${violationId}/dismiss`, data)
}

// 解封用户
export const unbanViolation = (violationId, data) => {
  return request.post(`/api/admin/violations/${violationId}/unban`, data)
}

// 触发AI扫描
export const triggerAiScan = (data) => {
  return request.post('/api/admin/violations/scan', data)
}

// 兼容旧接口名
export const getViolationReports = getViolationList
export const handleViolationReport = (violationId, data) => {
  return request.post(`/api/admin/violations/${violationId}/handle`, data)
}

// ==================== 条例文档管理接口 ====================

// 上传条例文档
export const uploadRulesDocument = (formData) => {
  return request.post('/api/admin/rules-documents/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取条例列表
export const getRulesDocuments = (params) => {
  return request.get('/api/admin/rules-documents', { params })
}

// 获取条例详情
export const getRulesDocumentDetail = (documentId) => {
  return request.get(`/api/admin/rules-documents/${documentId}`)
}

// 更新条例信息
export const updateRulesDocument = (documentId, data) => {
  return request.put(`/api/admin/rules-documents/${documentId}`, data)
}

// 删除条例
export const deleteRulesDocument = (documentId) => {
  return request.delete(`/api/admin/rules-documents/${documentId}`)
}

// 设置条例生效
export const activateRulesDocument = (documentId) => {
  return request.post(`/api/admin/rules-documents/${documentId}/activate`)
}

// 撤销条例生效
export const deactivateRulesDocument = (documentId) => {
  return request.post(`/api/admin/rules-documents/${documentId}/deactivate`)
}

// 下载条例
export const downloadRulesDocument = (documentId) => {
  return request.get(`/api/admin/rules-documents/${documentId}/download`)
}

// ==================== 权限管控接口 ====================

// 获取权限配置列表
export const getPermissionConfigs = (params) => {
  return request.get('/api/admin/admin-permissions', { params })
}

// 创建权限配置
export const createPermissionConfig = (data) => {
  return request.post('/api/admin/admin-permissions', data)
}

// 更新权限配置
export const updatePermissionConfig = (configId, data) => {
  return request.put(`/api/admin/admin-permissions/${configId}`, data)
}

// 删除权限配置
export const deletePermissionConfig = (configId) => {
  return request.delete(`/api/admin/admin-permissions/${configId}`)
}

// 切换权限状态（通过permissionId）
export const togglePermissionStatus = (configId) => {
  return request.post(`/api/admin/admin-permissions/${configId}/toggle-status`)
}

// 切换管理员状态（通过adminId，用于没有权限记录的管理员）
export const toggleAdminStatus = (adminId) => {
  return request.post(`/api/admin/admin-permissions/admin/${adminId}/toggle-status`)
}

// 创建小区管理员
export const createCommunityAdmin = (data) => {
  return request.post('/api/admin/admins/create', data)
}

// 更新小区管理员信息
export const updateCommunityAdmin = (adminId, data) => {
  return request.put(`/api/admin/admins/${adminId}`, data)
}

// 删除小区管理员
export const deleteCommunityAdmin = (adminId) => {
  return request.delete(`/api/admin/admins/${adminId}`)
}

// 获取小区管理员列表（用于权限配置）
export const getCommunityAdmins = () => {
  return request.get('/api/admin/admin-permissions/admins')
}

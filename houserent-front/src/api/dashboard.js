import request from '@/utils/request'

// 获取平台统计数据
export const getPlatformStats = () => {
  return request.get('/api/admin/stats/platform')
}

// 获取用户统计
export const getUserStats = () => {
  return request.get('/api/admin/stats/user-activity')
}

// 获取房源统计
export const getHouseStats = (communityId = null) => {
  const params = communityId ? { communityId } : {}
  return request.get('/api/houses/stats', { params })
}

// 获取合同统计
export const getContractStats = () => {
  return request.get('/api/contracts/stats')
}

// 获取评价统计
export const getEvaluationStats = () => {
  return request.get('/api/credit-evaluation/platform-stats')
}

// 获取小区认证统计
export const getVerificationStats = (communityId = null) => {
  const params = communityId ? { communityId } : {}
  return request.get('/api/community-verification/stats', { params })
}

// 获取待处理事项
export const getPendingTasks = () => {
  return Promise.all([
    request.get('/api/admin/verifications/platform/pending', { params: { pageNum: 1, pageSize: 1 } }),
    request.get('/api/admin/houses/pending', { params: { pageNum: 1, pageSize: 1 } }),
    request.get('/api/admin/contracts/pending', { params: { pageNum: 1, pageSize: 1 } })
  ])
}

// 获取最新活动日志
export const getRecentActivities = () => {
  return request.get('/api/admin/logs', { params: { pageNum: 1, pageSize: 10 } })
}

// 获取活跃度趋势数据
export const getActivityTrend = (period = 'week') => {
  return request.get('/api/admin/stats/system', { params: { period } })
}

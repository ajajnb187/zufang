import request from '@/utils/request'

// 分页搜索房源
export const searchHouses = (data) => {
  return request.get('/api/houses/search', {
    params: {
      ...data.searchDTO,
      pageNum: data.current,
      pageSize: data.size
    }
  })
}

// 获取房源详情
export const getHouseDetail = (houseId) => {
  return request.get(`/api/houses/${houseId}`)
}

// 审核房源
export const auditHouse = (houseId, data) => {
  return request.post(`/api/admin/houses/${houseId}/audit`, null, {
    params: {
      approved: data.status === 'approved',
      opinion: data.opinion
    }
  })
}

// 更新房源发布状态
export const updateHouseStatus = (houseId, status) => {
  return request.put(`/api/houses/${houseId}/status`, null, {
    params: { status }
  })
}

// 获取房东的房源列表
export const getLandlordHouses = (params) => {
  return request.get('/api/houses/landlord', { params })
}

// 获取待审核房源列表
export const getPendingHouses = (params) => {
  return request.get('/api/admin/houses/pending', { params })
}

// 获取小区房源列表
export const getCommunityHouses = (communityId, params) => {
  return request.get(`/api/houses/community/${communityId}`, { params })
}

// 获取热门房源
export const getPopularHouses = (limit = 10) => {
  return request.get('/api/house/popular', { params: { limit } })
}

// 获取推荐房源
export const getRecommendedHouses = (limit = 10) => {
  return request.get('/api/house/recommended', { params: { limit } })
}

// 获取房源统计信息
export const getHouseStats = (communityId = null) => {
  const params = communityId ? { communityId } : {}
  return request.get('/api/houses/stats', { params })
}

// 发布房源
export const publishHouse = (data) => {
  return request.post('/api/houses/publish', data)
}

// 更新房源信息
export const updateHouse = (houseId, data) => {
  return request.put(`/api/houses/${houseId}`, data)
}

// 删除房源
export const deleteHouse = (houseId) => {
  return request.delete(`/api/houses/${houseId}`)
}

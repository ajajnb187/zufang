import request from '@/utils/request'

// 创建租赁合同
export const createContract = (data) => {
  return request.post('/api/contracts/create', data)
}

// 签署合同
export const signContract = (contractId, data) => {
  return request.post(`/api/contracts/${contractId}/sign`, data)
}

// 生成合同PDF
export const generateContractPDF = (contractId) => {
  return request.post(`/api/contracts/${contractId}/generate-pdf`, null, {
    responseType: 'blob'
  })
}

// 审核合同
export const auditContract = (contractId, data) => {
  return request.post(`/api/admin/contracts/${contractId}/audit`, null, {
    params: {
      approved: data.status === 'approved',
      opinion: data.opinion
    }
  })
}

// 验证合同完整性
export const verifyContract = (contractId) => {
  return request.get(`/api/contracts/${contractId}/verify`)
}

// 获取合同详情
export const getContractDetail = (contractId) => {
  return request.get(`/api/contracts/${contractId}`)
}

// 分页查询用户合同列表
export const getUserContracts = (params) => {
  return request.get('/api/contracts/user', { params })
}

// 分页查询待审核合同列表
export const getPendingContracts = (params) => {
  return request.get('/api/admin/contracts/pending', { params })
}

// 分页查询所有合同列表
export const getAllContracts = (params) => {
  return request.get('/api/contracts', { params })
}

// 获取合同统计信息
export const getContractStats = () => {
  return request.get('/api/contracts/stats')
}

// 终止合同
export const terminateContract = (contractId, reason) => {
  return request.post(`/api/contracts/${contractId}/terminate`, null, {
    params: { reason }
  })
}

// 续签合同
export const renewContract = (contractId, data) => {
  return request.post(`/api/contracts/${contractId}/renew`, data)
}

// 获取合同模板
export const getContractTemplate = () => {
  return request.get('/api/contracts/template')
}

// 更新合同模板
export const updateContractTemplate = (data) => {
  return request.put('/api/contracts/template', data)
}

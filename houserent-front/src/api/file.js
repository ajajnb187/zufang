import request from '@/utils/request'

// 上传文件
export const uploadFile = (file, category, relatedId) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', category)
  if (relatedId) {
    formData.append('relatedId', relatedId)
  }
  
  return request.post('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 批量上传文件
export const uploadMultipleFiles = (files, category, relatedId) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  formData.append('category', category)
  if (relatedId) {
    formData.append('relatedId', relatedId)
  }
  
  return request.post('/api/files/upload-multiple', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文件
export const deleteFile = (fileId) => {
  return request.delete(`/api/files/${fileId}`)
}

// 获取文件URL
export const getFileUrl = (fileId) => {
  return request.get(`/api/files/${fileId}/url`)
}

// 按分类获取文件列表
export const getFilesByCategory = (category, params) => {
  return request.get('/api/files/category', { 
    params: { 
      category,
      ...params 
    }
  })
}

// 获取用户文件列表
export const getUserFiles = (params) => {
  return request.get('/api/files/user', { params })
}

// 下载文件
export const downloadFile = (fileId) => {
  return request.get(`/api/files/${fileId}/download`, {
    responseType: 'blob'
  })
}

// 获取文件统计信息
export const getFileStats = () => {
  return request.get('/api/files/stats')
}

// 批量删除文件
export const batchDeleteFiles = (fileIds) => {
  return request.post('/api/files/batch-delete', { fileIds })
}

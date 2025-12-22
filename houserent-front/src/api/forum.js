import request from '@/utils/request'

// 获取论坛帖子列表（管理员）
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

// 获取论坛帖子详情
export const getForumPostDetail = (postId) => {
  return request.get(`/api/forum/posts/${postId}`)
}

// 创建论坛帖子
export const createForumPost = (data) => {
  return request.post('/api/forum/posts', data)
}

// 回复论坛帖子
export const replyForumPost = (postId, data) => {
  return request.post(`/api/forum/posts/${postId}/replies`, data)
}

// 点赞/取消点赞帖子
export const likeForumPost = (postId) => {
  return request.post(`/api/forum/posts/${postId}/like`)
}

// 获取用户论坛帖子
export const getUserForumPosts = (params) => {
  return request.get('/api/forum/posts/user', { params })
}

// 搜索论坛帖子
export const searchForumPosts = (params) => {
  return request.get('/api/forum/posts/search', { params })
}

// 获取热门帖子
export const getHotForumPosts = (limit = 10) => {
  return request.get('/api/forum/posts/hot', { params: { limit } })
}

// 获取论坛统计数据
export const getForumStats = () => {
  return request.get('/api/forum/stats')
}

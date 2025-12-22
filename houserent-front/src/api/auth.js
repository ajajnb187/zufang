import request from '@/utils/request'

// 管理员登录
export const adminLogin = (data) => {
  return request.post('/api/auth/admin-login', {
    username: data.phone || data.username,
    password: data.password
  })
}

// 管理员注册
export const adminRegister = (data) => {
  return request.post('/api/auth/admin-register', {
    phone: data.phone,
    nickname: data.nickname,
    password: data.password,
    adminType: data.adminType
  })
}

// 租户登录
export const tenantLogin = (data) => {
  return request.post('/api/auth/tenant-login', {
    phone: data.phone,
    password: data.password
  })
}

// 房东登录
export const landlordLogin = (data) => {
  return request.post('/api/auth/landlord-login', {
    phone: data.phone,
    password: data.password
  })
}

// 微信登录
export const wechatLogin = (data) => {
  return request.post('/api/auth/login', {
    code: data.code
  })
}

// 微信注册
export const wechatRegister = (data) => {
  return request.post('/api/auth/register', {
    code: data.code,
    phone: data.phone,
    userType: data.userType
  })
}

// 登出
export const logout = () => {
  return request.post('/api/auth/logout')
}

// 检查token
export const checkToken = () => {
  return request.get('/api/auth/check-token')
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/api/auth/user-info')
}

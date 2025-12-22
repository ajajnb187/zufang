// 用户类型工具类
export const USER_TYPE = {
  TENANT: 3,    // 租客
  LANDLORD: 4   // 房东
}

// 获取用户类型
export function getUserType() {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    console.log('【userType.js】getUserType被调用，userInfo:', JSON.stringify(userInfo, null, 2))
    const userType = userInfo?.userType || null
    console.log('【userType.js】返回userType:', userType, '类型:', typeof userType)
    return userType
  } catch (error) {
    console.error('【userType.js】获取用户类型失败:', error)
    return null
  }
}

// 判断是否为租客
export function isTenant() {
  return getUserType() === USER_TYPE.TENANT
}

// 判断是否为房东
export function isLandlord() {
  return getUserType() === USER_TYPE.LANDLORD
}

// 判断是否登录（与storage.js保持一致，检查'token'键）
export function isLogin() {
  try {
    const token = uni.getStorageSync('token')
    console.log('【userType.js】isLogin检查，token存在:', !!token)
    return !!token
  } catch (error) {
    console.error('【userType.js】isLogin检查失败:', error)
    return false
  }
}

// 获取用户信息
export function getUserInfo() {
  try {
    return uni.getStorageSync('userInfo') || {}
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return {}
  }
}

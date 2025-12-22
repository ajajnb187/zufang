// 本地存储封装
const KEYS = {
	TOKEN: 'token',
	USER_INFO: 'userInfo',
	SEARCH_HISTORY: 'searchHistory'
}

// 存储token
export function setToken(token) {
	console.log('【storage.js】setToken被调用，token长度:', token ? token.length : 0)
	uni.setStorageSync(KEYS.TOKEN, token)
	// 立即验证是否保存成功
	const saved = uni.getStorageSync(KEYS.TOKEN)
	console.log('【storage.js】token保存后立即读取验证:', saved === token ? '成功' : '失败')
	return saved
}

// 获取token
export function getToken() {
	const token = uni.getStorageSync(KEYS.TOKEN)
	console.log('【storage.js】getToken被调用，token存在:', !!token)
	return token
}

// 移除token
export function removeToken() {
	return uni.removeStorageSync(KEYS.TOKEN)
}

// 存储用户信息
export function setUserInfo(userInfo) {
	console.log('【storage.js】setUserInfo被调用，userInfo:', JSON.stringify(userInfo, null, 2))
	const result = uni.setStorageSync(KEYS.USER_INFO, userInfo)
	// 立即验证存储
	const saved = uni.getStorageSync(KEYS.USER_INFO)
	console.log('【storage.js】用户信息存储后验证:', JSON.stringify(saved, null, 2))
	return result
}

// 获取用户信息
export function getUserInfo() {
	return uni.getStorageSync(KEYS.USER_INFO)
}

// 移除用户信息
export function removeUserInfo() {
	return uni.removeStorageSync(KEYS.USER_INFO)
}

// 清空所有本地存储
export function clearStorage() {
	removeToken()
	removeUserInfo()
}

// 获取用户类型
export function getUserType() {
	const userInfo = getUserInfo()
	console.log('【storage.js】getUserType被调用，userInfo:', JSON.stringify(userInfo, null, 2))
	const userType = userInfo ? userInfo.userType : null // 不设默认值，返回null
	console.log('【storage.js】返回userType:', userType)
	return userType
}

// 检查是否登录
export function isLogin() {
	return !!getToken()
}

// 检查是否为房东
export function isLandlord() {
	return getUserType() === 4
}

// 检查是否为租客
export function isTenant() {
	return getUserType() === 3
}

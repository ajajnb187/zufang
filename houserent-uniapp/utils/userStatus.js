/**
 * 用户状态管理工具
 * 处理用户封禁、禁言等状态检查和限制
 */

import api from './api.js'

/**
 * 用户状态枚举
 */
export const UserStatus = {
	ACTIVE: 'active',      // 正常
	MUTED: 'muted',        // 禁言（可浏览，不能聊天）
	BANNED: 'banned'       // 封禁（不能使用任何功能）
}

/**
 * 检查用户状态并执行相应限制
 * @returns {Promise<{status: string, canChat: boolean, canBrowse: boolean, message: string}>}
 */
export async function checkUserStatus() {
	try {
		const userInfo = uni.getStorageSync('userInfo')
		if (!userInfo || !userInfo.userId) {
			return { status: 'unknown', canChat: false, canBrowse: true, message: '未登录' }
		}
		
		// 从服务器获取最新用户状态
		const res = await api.user.getProfile()
		if (res.code === 200 && res.data) {
			const status = res.data.status || 'active'
			const banUntil = res.data.banUntil
			
			// 更新本地存储的用户信息
			const updatedUserInfo = { ...userInfo, status, banUntil }
			uni.setStorageSync('userInfo', updatedUserInfo)
			
			return getStatusInfo(status, banUntil)
		}
		
		// 使用本地缓存的状态
		return getStatusInfo(userInfo.status || 'active', userInfo.banUntil)
		
	} catch (e) {
		console.error('检查用户状态失败:', e)
		// 出错时使用本地缓存
		const userInfo = uni.getStorageSync('userInfo')
		return getStatusInfo(userInfo?.status || 'active', userInfo?.banUntil)
	}
}

/**
 * 根据状态获取详细信息
 */
function getStatusInfo(status, banUntil) {
	switch (status) {
		case UserStatus.BANNED:
			return {
				status: UserStatus.BANNED,
				canChat: false,
				canBrowse: false,
				message: banUntil ? `您的账户已被封禁，解封时间：${banUntil}` : '您的账户已被永久封禁'
			}
		case UserStatus.MUTED:
			return {
				status: UserStatus.MUTED,
				canChat: false,
				canBrowse: true,
				message: banUntil ? `您的账户已被禁言，解禁时间：${banUntil}` : '您的账户已被禁言'
			}
		default:
			return {
				status: UserStatus.ACTIVE,
				canChat: true,
				canBrowse: true,
				message: ''
			}
	}
}

/**
 * 检查是否可以发送聊天消息
 * @returns {Promise<boolean>}
 */
export async function canSendMessage() {
	const statusInfo = await checkUserStatus()
	
	if (!statusInfo.canChat) {
		uni.showModal({
			title: '无法发送消息',
			content: statusInfo.message,
			showCancel: false
		})
		return false
	}
	
	return true
}

/**
 * 检查是否可以使用应用（封禁检查）
 * @returns {Promise<boolean>}
 */
export async function canUseApp() {
	const statusInfo = await checkUserStatus()
	
	if (!statusInfo.canBrowse) {
		// 封禁用户，显示封禁页面
		showBannedPage(statusInfo.message)
		return false
	}
	
	return true
}

/**
 * 显示封禁页面
 */
function showBannedPage(message) {
	uni.showModal({
		title: '账户已封禁',
		content: message + '\n\n如有疑问，请联系客服申诉。',
		showCancel: false,
		confirmText: '我知道了',
		success: () => {
			// 清除登录信息并跳转到登录页
			uni.removeStorageSync('token')
			uni.removeStorageSync('userInfo')
			uni.reLaunch({
				url: '/pages/login/login'
			})
		}
	})
}

/**
 * 处理WebSocket通知消息
 * @param {Object} notification 通知消息
 */
export function handleNotification(notification) {
	if (notification.type !== 'notification') return
	
	const { notificationType, title, content, extra } = notification
	
	// 更新本地用户状态
	if (extra && extra.status) {
		const userInfo = uni.getStorageSync('userInfo')
		if (userInfo) {
			userInfo.status = extra.status
			if (extra.banUntil) {
				userInfo.banUntil = extra.banUntil
			} else {
				delete userInfo.banUntil
			}
			uni.setStorageSync('userInfo', userInfo)
		}
	}
	
	// 显示通知弹窗
	switch (notificationType) {
		case 'warning':
			uni.showModal({
				title: title,
				content: content,
				showCancel: false,
				confirmText: '我知道了'
			})
			break
			
		case 'mute':
			uni.showModal({
				title: title,
				content: content,
				showCancel: false,
				confirmText: '我知道了'
			})
			break
			
		case 'ban':
			uni.showModal({
				title: title,
				content: content,
				showCancel: false,
				confirmText: '我知道了',
				success: () => {
					if (extra && extra.forceLogout) {
						// 强制退出登录
						uni.removeStorageSync('token')
						uni.removeStorageSync('userInfo')
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}
				}
			})
			break
			
		case 'unban':
			uni.showModal({
				title: title,
				content: content,
				showCancel: false,
				confirmText: '太好了'
			})
			break
			
		default:
			// 普通系统通知
			uni.showToast({
				title: title,
				icon: 'none',
				duration: 3000
			})
	}
}

/**
 * 获取本地缓存的用户状态
 */
export function getLocalUserStatus() {
	const userInfo = uni.getStorageSync('userInfo')
	return userInfo?.status || 'active'
}

/**
 * 是否是禁言状态
 */
export function isMuted() {
	return getLocalUserStatus() === UserStatus.MUTED
}

/**
 * 是否是封禁状态
 */
export function isBanned() {
	return getLocalUserStatus() === UserStatus.BANNED
}

export default {
	UserStatus,
	checkUserStatus,
	canSendMessage,
	canUseApp,
	handleNotification,
	getLocalUserStatus,
	isMuted,
	isBanned
}

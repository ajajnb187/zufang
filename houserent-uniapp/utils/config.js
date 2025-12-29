// 全局配置

// 获取当前环境的API地址
const getBaseURL = () => {
	// #ifdef MP-WEIXIN
	// 微信小程序环境
	const systemInfo = uni.getSystemInfoSync()
	const DEV_IP = '192.168.31.106' // 修改为你的电脑IP
	
	// 判断是否在真机上运行（非开发者工具）
	if (systemInfo.platform !== 'devtools') {
		return `http://${DEV_IP}:8888/api`
	}
	
	// 开发者工具使用localhost
	return 'http://localhost:8888/api'
	// #endif
	
	// #ifndef MP-WEIXIN
	// 非微信环境（H5等）
	return 'http://localhost:8888/api'
	// #endif
}

// WebSocket地址
const getWsURL = () => {
	// #ifdef MP-WEIXIN
	const systemInfo = uni.getSystemInfoSync()
	const DEV_IP = '192.168.31.106' // 与API地址保持一致
	
	if (systemInfo.platform !== 'devtools') {
		return `ws://${DEV_IP}:8888`
	}
	
	return 'ws://localhost:8888'
	// #endif
	
	// #ifndef MP-WEIXIN
	return 'ws://localhost:8888'
	// #endif
}

export default {
	// 后端API地址
	baseURL: getBaseURL(),
	
	// WebSocket地址
	wsURL: getWsURL(),
	
	// 超时时间
	timeout: 10000,
	
	// 用户类型
	USER_TYPE: {
		TENANT: 3,    // 租客
		LANDLORD: 4   // 房东
	},
	
	// 房源状态
	HOUSE_STATUS: {
		DRAFT: 'draft',           // 草稿
		PENDING: 'pending',       // 待审核
		APPROVED: 'approved',     // 已通过
		REJECTED: 'rejected',     // 已拒绝
		ONLINE: 'online',         // 在线
		OFFLINE: 'offline',       // 下线
		RENTED: 'rented'          // 已出租
	},
	
	// 合同状态
	CONTRACT_STATUS: {
		DRAFT: 'draft',           // 草稿
		PENDING: 'pending',       // 待签署
		SIGNED: 'signed',         // 已签署
		ACTIVE: 'active',         // 履约中
		EXPIRED: 'expired',       // 已到期
		TERMINATED: 'terminated'  // 已终止
	}
}

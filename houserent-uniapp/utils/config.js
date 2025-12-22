// 全局配置

// 获取当前环境的API地址
// 真机调试时需要使用电脑的实际IP地址，localhost在真机上指向手机本身
const getBaseURL = () => {
	// #ifdef MP-WEIXIN
	// 微信小程序环境
	const systemInfo = uni.getSystemInfoSync()
	// 如果是开发版本且在真机上运行，需要使用电脑IP
	// 请将下面的IP地址改为你电脑的局域网IP
	const DEV_IP = '172.31.14.194' // ⚠️ 改成你电脑的实际IP地址
	
	// 判断是否在真机上运行（非开发者工具）
	if (systemInfo.platform !== 'devtools') {
		return `http://${DEV_IP}:8888/api`
	}
	// #endif
	
	// 开发者工具或其他环境使用localhost
	return 'http://localhost:8888/api'
}

// WebSocket地址
const getWsURL = () => {
	// #ifdef MP-WEIXIN
	const systemInfo = uni.getSystemInfoSync()
	const DEV_IP = '172.31.14.194' // ⚠️ 与baseURL保持一致的IP地址
	
	if (systemInfo.platform !== 'devtools') {
		return `ws://${DEV_IP}:8888`
	}
	// #endif
	
	return 'ws://localhost:8888'
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

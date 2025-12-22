import config from './config.js'
import { getToken, clearStorage } from './storage.js'

// 请求封装
export default function request(options = {}) {
	return new Promise((resolve, reject) => {
		const {
			url,
			method = 'GET',
			data = {},
			header = {}
		} = options
		
		// 完整URL - GET请求时拼接查询参数
		let fullUrl = config.baseURL + url
		let requestData = data
		
		if (method.toUpperCase() === 'GET' && data && Object.keys(data).length > 0) {
			// GET请求：将data转换为URL查询参数
			const params = Object.entries(data)
				.filter(([_, v]) => v !== null && v !== undefined && v !== '')
				.map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
				.join('&')
			if (params) {
				fullUrl += (url.includes('?') ? '&' : '?') + params
			}
			requestData = {} // GET请求不需要body
		}
		
		console.log('【request.js】请求URL:', fullUrl)
		
		// 请求头
		const requestHeader = {
			'Content-Type': 'application/json',
			...header
		}
		
		// 添加token（后端Sa-Token配置：token-name=Authorization, token-prefix=Bearer）
		const token = getToken()
		console.log('【request.js】获取到的token:', token ? token.substring(0, 10) + '...' : 'null')
		if (token) {
			requestHeader['Authorization'] = 'Bearer ' + token
			console.log('【request.js】已添加Authorization请求头: Bearer ' + token.substring(0, 10) + '...')
		} else {
			console.warn('【request.js】警告: token为空，未添加到请求头')
		}
		
		uni.request({
			url: fullUrl,
			method,
			data: requestData,
			header: requestHeader,
			timeout: config.timeout,
			success: (res) => {
				const { statusCode, data } = res
				
				// 请求成功
				if (statusCode === 200) {
					// 业务成功
					if (data.code === 200) {
						resolve(data)
					} 
					// 未登录
					else if (data.code === 401) {
						clearStorage()
						uni.showToast({
							title: '请先登录',
							icon: 'none'
						})
						setTimeout(() => {
							uni.reLaunch({
								url: '/pages/login/login'
							})
						}, 1500)
						reject(data)
					}
					// 业务失败
					else {
						uni.showToast({
							title: data.msg || '请求失败',
							icon: 'none'
						})
						reject(data)
					}
				} 
				// HTTP错误
				else {
					uni.showToast({
						title: `请求错误: ${statusCode}`,
						icon: 'none'
					})
					reject(res)
				}
			},
			fail: (err) => {
				console.error('请求失败:', err)
				uni.showToast({
					title: '网络请求失败',
					icon: 'none'
				})
				reject(err)
			}
		})
	})
}

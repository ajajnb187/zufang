/**
 * WebSocket聊天工具类
 */
import { getToken } from './storage.js'
import config from './config.js'

class WebSocketClient {
	constructor() {
		this.socketTask = null
		this.isConnected = false
		this.reconnectTimer = null
		this.heartbeatTimer = null
		this.callbacks = {
			onMessage: null,
			onOpen: null,
			onClose: null,
			onError: null
		}
		this.messageQueue = []
	}
	
	/**
	 * 连接WebSocket
	 */
	connect() {
		if (this.isConnected || this.socketTask) {
			console.log('WebSocket已连接或正在连接')
			return
		}
		
		const token = getToken()
		if (!token) {
			console.error('WebSocket连接失败：未登录')
			return
		}
		
		// 获取用户ID用于WebSocket连接
		const userInfo = uni.getStorageSync('userInfo')
		const userId = userInfo?.userId || userInfo?.id
		if (!userId) {
			console.error('WebSocket连接失败：无法获取用户ID')
			return
		}
		
		// 使用config中的wsURL，WebSocket路径需要用户ID
		const wsUrl = `${config.wsURL}/websocket/chat/${userId}`
		
		console.log('正在连接WebSocket:', wsUrl)
		
		this.socketTask = uni.connectSocket({
			url: wsUrl,
			success: () => {
				console.log('WebSocket连接请求成功')
			},
			fail: (err) => {
				console.error('WebSocket连接请求失败:', err)
				this.reconnect()
			}
		})
		
		this.socketTask.onOpen(() => {
			console.log('WebSocket连接已打开')
			this.isConnected = true
			this.startHeartbeat()
			
			// 发送队列中的消息
			while (this.messageQueue.length > 0) {
				const msg = this.messageQueue.shift()
				this.send(msg)
			}
			
			if (this.callbacks.onOpen) {
				this.callbacks.onOpen()
			}
		})
		
		this.socketTask.onMessage((res) => {
			console.log('WebSocket收到消息:', res.data)
			try {
				const data = JSON.parse(res.data)
				if (this.callbacks.onMessage) {
					this.callbacks.onMessage(data)
				}
			} catch (e) {
				console.error('解析WebSocket消息失败:', e)
			}
		})
		
		this.socketTask.onClose((res) => {
			console.log('WebSocket连接已关闭:', res)
			this.isConnected = false
			this.socketTask = null
			this.stopHeartbeat()
			
			if (this.callbacks.onClose) {
				this.callbacks.onClose(res)
			}
			
			// 自动重连
			this.reconnect()
		})
		
		this.socketTask.onError((err) => {
			console.error('WebSocket错误:', err)
			this.isConnected = false
			
			if (this.callbacks.onError) {
				this.callbacks.onError(err)
			}
		})
	}
	
	/**
	 * 发送消息
	 */
	send(data) {
		if (!this.isConnected || !this.socketTask) {
			console.log('WebSocket未连接，消息加入队列')
			this.messageQueue.push(data)
			this.connect()
			return false
		}
		
		const message = typeof data === 'string' ? data : JSON.stringify(data)
		
		this.socketTask.send({
			data: message,
			success: () => {
				console.log('消息发送成功')
			},
			fail: (err) => {
				console.error('消息发送失败:', err)
			}
		})
		
		return true
	}
	
	/**
	 * 发送聊天消息
	 */
	sendChatMessage(receiverId, content, messageType = 'TEXT') {
		return this.send({
			type: 'CHAT',
			receiverId: receiverId,
			content: content,
			messageType: messageType
		})
	}
	
	/**
	 * 关闭连接
	 */
	close() {
		this.stopHeartbeat()
		if (this.reconnectTimer) {
			clearTimeout(this.reconnectTimer)
			this.reconnectTimer = null
		}
		
		if (this.socketTask) {
			this.socketTask.close({
				success: () => {
					console.log('WebSocket连接已关闭')
				}
			})
			this.socketTask = null
		}
		this.isConnected = false
	}
	
	/**
	 * 重连
	 */
	reconnect() {
		if (this.reconnectTimer) return
		
		console.log('5秒后尝试重连WebSocket...')
		this.reconnectTimer = setTimeout(() => {
			this.reconnectTimer = null
			this.connect()
		}, 5000)
	}
	
	/**
	 * 开始心跳
	 */
	startHeartbeat() {
		this.stopHeartbeat()
		this.heartbeatTimer = setInterval(() => {
			if (this.isConnected) {
				this.send({ type: 'PING' })
			}
		}, 30000)
	}
	
	/**
	 * 停止心跳
	 */
	stopHeartbeat() {
		if (this.heartbeatTimer) {
			clearInterval(this.heartbeatTimer)
			this.heartbeatTimer = null
		}
	}
	
	/**
	 * 设置消息回调
	 */
	onMessage(callback) {
		this.callbacks.onMessage = callback
	}
	
	/**
	 * 设置连接打开回调
	 */
	onOpen(callback) {
		this.callbacks.onOpen = callback
	}
	
	/**
	 * 设置连接关闭回调
	 */
	onClose(callback) {
		this.callbacks.onClose = callback
	}
	
	/**
	 * 设置错误回调
	 */
	onError(callback) {
		this.callbacks.onError = callback
	}
}

// 导出单例
const wsClient = new WebSocketClient()
export default wsClient

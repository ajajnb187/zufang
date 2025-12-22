<template>
	<view class="chat-detail-page">
		<!-- 聊天消息列表 -->
		<scroll-view class="message-list" scroll-y :scroll-top="scrollTop" scroll-with-animation>
			<view class="message-item" v-for="msg in messageList" :key="msg.messageId" 
				:class="{ 'is-mine': msg.isMine }">
				<image class="avatar" :src="msg.avatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'" mode="aspectFill"></image>
				<view class="message-content">
					<view class="message-info">
						<text class="nickname">{{ msg.nickname }}</text>
						<text class="time">{{ formatTime(msg.createTime) }}</text>
					</view>
					<view class="message-text">{{ msg.content }}</view>
				</view>
			</view>
			<view class="empty" v-if="messageList.length === 0">
				<text class="empty-text">暂无聊天记录</text>
			</view>
		</scroll-view>
		
		<!-- 输入框 -->
		<view class="input-bar" v-if="!userMuted">
			<input class="message-input" v-model="inputMessage" placeholder="请输入消息..." 
				confirm-type="send" @confirm="sendMessage" />
			<button class="send-btn" @click="sendMessage">发送</button>
		</view>
		<!-- 禁言状态提示 -->
		<view class="muted-bar" v-else>
			<text class="muted-text">🔇 您的账户已被禁言，暂时无法发送消息</text>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'
import { canSendMessage, handleNotification, isMuted } from '@/utils/userStatus.js'

export default {
	data() {
		return {
			userId: '',
			userName: '',
			userAvatar: '', // 对方头像
			myAvatar: '', // 我的头像
			messageList: [],
			inputMessage: '',
			scrollTop: 0,
			websocket: null,
			userMuted: false // 用户是否被禁言
		}
	},
	
	onLoad(options) {
		this.userId = options.userId
		// 解码URL编码的昵称
		this.userName = options.name ? decodeURIComponent(options.name) : '房东'
		// 对方头像
		this.userAvatar = options.avatar ? decodeURIComponent(options.avatar) : ''
		// 我的头像
		const userInfo = uni.getStorageSync('userInfo')
		this.myAvatar = userInfo?.avatarUrl || ''
		
		// 检查用户是否被禁言
		this.userMuted = isMuted()
		
		uni.setNavigationBarTitle({ title: this.userName })
		this.loadHistory()
		this.connectWebSocket()
	},
	
	onUnload() {
		this.closeWebSocket()
	},
	
	methods: {
		async loadHistory() {
			try {
				console.log('加载聊天记录, otherUserId:', this.userId)
				const res = await api.chat.getHistory({
					otherUserId: this.userId,
					pageNum: 1,
					pageSize: 50
				})
				console.log('聊天记录响应:', res)
				if (res.code === 200) {
					const records = res.data?.records || res.data || []
					console.log('聊天记录数据:', records)
					
					// 获取当前用户ID
					const userInfo = uni.getStorageSync('userInfo')
					const currentUserId = userInfo?.userId || userInfo?.id
					
					this.messageList = records.map(item => {
						const isMine = item.senderId == currentUserId
						return {
							messageId: item.messageId,
							// 优先使用过滤后的内容（敏感词已被替换为***），如果没有则使用原始内容
							content: item.filteredContent || item.content,
							isMine: isMine,
							// 自己的消息用自己的头像，对方的消息用对方的头像
							avatar: isMine ? this.myAvatar : (item.senderAvatar || this.userAvatar),
							nickname: isMine ? '我' : (item.senderNickname || this.userName),
							createTime: item.createdAt || item.createTime
						}
					})
					this.scrollToBottom()
					if (this.messageList.length > 0) {
						this.markAsRead()
					}
				}
			} catch (e) {
				console.error('加载聊天记录失败:', e)
			}
		},
		
		connectWebSocket() {
			const userInfo = uni.getStorageSync('userInfo')
			const currentUserId = userInfo?.userId || userInfo?.id
			if (!currentUserId) {
				console.error('未获取到当前用户ID，无法连接WebSocket')
				return
			}
			const wsUrl = `${api.wsUrl}/websocket/chat/${currentUserId}`
			
			this.websocket = uni.connectSocket({
				url: wsUrl,
				success: () => {
					console.log('WebSocket连接成功')
				},
				fail: (err) => {
					console.error('WebSocket连接失败:', err)
				}
			})
			
			this.websocket.onOpen(() => {
				console.log('WebSocket已打开')
			})
			
			this.websocket.onMessage((res) => {
				try {
					const msg = JSON.parse(res.data)
					console.log('收到WebSocket消息:', msg)
					
					// 处理系统通知（警告、禁言、封禁等）
					if (msg.type === 'notification') {
						handleNotification(msg)
						return
					}
					
					// 收到对方消息
					if (msg.type === 'message' && msg.senderId == this.userId) {
						this.messageList.push({
							messageId: msg.messageId || Date.now(),
							// WebSocket已经发送过滤后的内容，直接使用
							content: msg.content,
							isMine: false,
							avatar: msg.senderAvatar || this.userAvatar,
							nickname: msg.senderNickname || this.userName,
							createTime: msg.createdAt || msg.createTime || new Date()
						})
						this.scrollToBottom()
						this.markAsRead()
					}
					
					// 自己发送的消息确认（服务器返回的是过滤后的内容）
					if (msg.type === 'sent') {
						console.log('收到发送确认，过滤后内容:', msg.content)
						// 找到待确认的消息并更新为服务器返回的过滤后内容
						const pendingIndex = this.messageList.findIndex(m => m.pending && m.isMine)
						if (pendingIndex !== -1) {
							// 使用Vue响应式方式更新数组元素
							this.$set(this.messageList, pendingIndex, {
								...this.messageList[pendingIndex],
								messageId: msg.messageId,
								content: msg.content, // 使用服务器返回的过滤后内容
								pending: false
							})
							console.log('消息已更新为过滤后内容')
						}
					}
				} catch (e) {
					console.error('解析消息失败:', e)
				}
			})
			
			this.websocket.onError((err) => {
				console.error('WebSocket错误:', err)
			})
			
			this.websocket.onClose(() => {
				console.log('WebSocket已关闭')
			})
		},
		
		closeWebSocket() {
			if (this.websocket) {
				this.websocket.close()
				this.websocket = null
			}
		},
		
		async sendMessage() {
			const content = this.inputMessage.trim()
			if (!content) {
				uni.showToast({ title: '请输入消息内容', icon: 'none' })
				return
			}
			
			// 检查用户是否可以发送消息（禁言/封禁检查）
			const canSend = await canSendMessage()
			if (!canSend) {
				return
			}
			
			const message = {
				type: 'chat',
				receiverId: parseInt(this.userId),
				content: content
			}
			
			if (this.websocket) {
				// 生成临时ID用于后续匹配更新
				const tempId = Date.now()
				this.websocket.send({
					data: JSON.stringify(message),
					success: () => {
						// 先显示"发送中..."的占位消息
						this.messageList.push({
							messageId: tempId,
							content: '发送中...',
							originalContent: content, // 保存原始内容用于发送失败时显示
							isMine: true,
							avatar: this.myAvatar,
							nickname: '我',
							createTime: new Date(),
							pending: true // 标记为待确认状态
						})
						this.inputMessage = ''
						this.scrollToBottom()
					},
					fail: (err) => {
						console.error('发送消息失败:', err)
						uni.showToast({ title: '发送失败', icon: 'none' })
					}
				})
			} else {
				uni.showToast({ title: '连接已断开，请重新进入', icon: 'none' })
			}
		},
		
		async markAsRead() {
			try {
				await api.chat.markRead(this.userId)
			} catch (e) {
				console.error('标记已读失败:', e)
			}
		},
		
		scrollToBottom() {
			this.$nextTick(() => {
				this.scrollTop = 999999
			})
		},
		
		formatTime(time) {
			if (!time) return ''
			const date = new Date(time)
			const now = new Date()
			const diff = now - date
			
			if (diff < 60000) return '刚刚'
			if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
			if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
			
			const month = date.getMonth() + 1
			const day = date.getDate()
			const hour = date.getHours().toString().padStart(2, '0')
			const minute = date.getMinutes().toString().padStart(2, '0')
			return `${month}-${day} ${hour}:${minute}`
		}
	}
}
</script>

<style scoped>
.chat-detail-page {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: linear-gradient(180deg, #F7F9FC 0%, #FFFFFF 100%);
}

.message-list {
	flex: 1;
	padding: 24rpx 30rpx;
}

.message-item {
	display: flex;
	margin-bottom: 32rpx;
	animation: slideIn 0.3s ease;
}

@keyframes slideIn {
	from { opacity: 0; transform: translateY(20rpx); }
	to { opacity: 1; transform: translateY(0); }
}

.message-item.is-mine {
	flex-direction: row-reverse;
}

.avatar {
	width: 88rpx;
	height: 88rpx;
	border-radius: 50%;
	margin: 0 20rpx;
	border: 4rpx solid #FFE5D9;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.message-content {
	max-width: 520rpx;
}

.message-info {
	display: flex;
	align-items: center;
	margin-bottom: 12rpx;
}

.is-mine .message-info {
	flex-direction: row-reverse;
}

.nickname {
	font-size: 24rpx;
	color: #8B95A5;
	margin-right: 12rpx;
	font-weight: 500;
}

.is-mine .nickname {
	margin-right: 0;
	margin-left: 12rpx;
}

.time {
	font-size: 22rpx;
	color: #BDC3C7;
}

.message-text {
	background: #FFFFFF;
	padding: 24rpx 28rpx;
	border-radius: 20rpx;
	font-size: 30rpx;
	line-height: 1.6;
	word-wrap: break-word;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
	position: relative;
}

.message-text::before {
	content: '';
	position: absolute;
	top: 12rpx;
	left: -12rpx;
	width: 0;
	height: 0;
	border-top: 12rpx solid transparent;
	border-bottom: 12rpx solid transparent;
	border-right: 12rpx solid #FFFFFF;
}

.is-mine .message-text {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}

.is-mine .message-text::before {
	left: auto;
	right: -12rpx;
	border-right: none;
	border-left: 12rpx solid #FF8C61;
}

.empty {
	text-align: center;
	padding: 150rpx 40rpx;
	animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
	from { opacity: 0; }
	to { opacity: 1; }
}

.empty-text {
	font-size: 28rpx;
	color: #8B95A5;
	font-weight: 500;
}

.input-bar {
	display: flex;
	align-items: center;
	padding: 24rpx 30rpx;
	background: #FFFFFF;
	border-top: 1rpx solid #F2F6FC;
	padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.message-input {
	flex: 1;
	background: #F7F9FC;
	padding: 24rpx 28rpx;
	border-radius: 48rpx;
	font-size: 30rpx;
	margin-right: 20rpx;
	color: #2C3E50;
	border: 2rpx solid #E4E7ED;
	transition: all 0.3s ease;
}

.message-input:focus {
	background: #FFFFFF;
	border-color: #FF6B35;
	box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
}

.send-btn {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	padding: 24rpx 44rpx;
	border-radius: 48rpx;
	font-size: 28rpx;
	font-weight: 700;
	border: none;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
	transition: all 0.3s ease;
}

.send-btn:active {
	transform: scale(0.95);
	box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.3);
}

.muted-bar {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 32rpx 30rpx;
	background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
	border-top: 2rpx solid #FFB74D;
	padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.muted-text {
	font-size: 28rpx;
	color: #E65100;
	font-weight: 600;
	text-align: center;
	line-height: 1.6;
}
</style>

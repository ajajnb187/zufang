<template>
	<view class="chat-page">
		<!-- 消息列表区域 -->
		<scroll-view 
			class="chat-scroll" 
			scroll-y 
			:scroll-into-view="scrollIntoView" 
			:upper-threshold="50"
			@scrolltoupper="onScrollToTop"
		>
			<view class="scroll-inner">
				<!-- 加载更多 loading -->
				<view class="history-loading" v-if="isLoadingHistory">
					<view class="spinner"></view>
				</view>

				<!-- 消息循环 -->
				<view 
					class="message-row" 
					v-for="(msg, index) in messageList" 
					:key="msg.messageId" 
					:id="'msg-' + msg.messageId"
				>
					<!-- 时间戳 -->
					<view class="timestamp" v-if="shouldShowTime(index)">
						<text>{{ formatTime(msg.createTime) }}</text>
					</view>

					<view class="message-body" :class="{ 'self': msg.isMine }">
						<image class="avatar" :src="msg.avatar || defaultAvatar" mode="aspectFill"></image>
						
						<view class="content-wrapper">
							<text class="nickname" v-if="!msg.isMine">{{ msg.nickname }}</text>
							
							<view class="bubble" :class="{ 'bubble-mine': msg.isMine, 'bubble-other': !msg.isMine }">
								<text class="msg-text" user-select>{{ msg.content }}</text>
								<!-- 发送状态 -->
								<view class="state-icon" v-if="msg.pending">
									<view class="spinner-small"></view>
								</view>
							</view>
						</view>
					</view>
				</view>
				
				<!-- 底部垫高，防止被输入框遮挡 -->
				<view class="bottom-spacer"></view>
				<!-- 底部锚点 -->
				<view id="scroll-bottom-anchor"></view>
			</view>
		</scroll-view>

		<!-- 底部输入框 -->
		<view class="input-area">
			<view class="input-wrapper" v-if="!userMuted">
				<input 
					class="chat-input" 
					v-model="inputMessage" 
					confirm-type="send" 
					:cursor-spacing="20"
					placeholder="" 
					@confirm="sendMessage"
				/>
				<view class="send-btn" :class="{ 'active': inputMessage.trim().length > 0 }" @click="sendMessage">
					发送
				</view>
			</view>
			<view class="muted-bar" v-else>
				<text>全员禁言中</text>
			</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'
import { canSendMessage, handleNotification, isMuted } from '@/utils/userStatus.js'

export default {
	data() {
		return {
			// 用户信息
			userId: '',
			userName: '',
			userAvatar: '',
			myAvatar: '',
			defaultAvatar: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
			
			// 消息数据
			messageList: [],
			inputMessage: '',
			
			// 状态控制
			scrollIntoView: '', // 控制滚动ID
			isLoadingHistory: false, // 是否正在加载历史
			isFirstLoad: true, // 是否是首次进入
			userMuted: false,
			websocket: null,
			
			// 分页参数
			pageNum: 1,
			pageSize: 20,
			hasMore: true
		}
	},
	
	onLoad(options) {
		this.userId = options.userId
		this.userName = options.name ? decodeURIComponent(options.name) : '用户'
		this.userAvatar = options.avatar ? decodeURIComponent(options.avatar) : ''
		
		const userInfo = uni.getStorageSync('userInfo')
		this.myAvatar = userInfo?.avatarUrl || ''
		this.userMuted = isMuted()
		
		uni.setNavigationBarTitle({ title: this.userName })
		
		// 初始化加载
		this.loadHistory()
		this.connectWebSocket()
	},
	
	onUnload() {
		if (this.websocket) {
			this.websocket.close()
		}
	},
	
	methods: {
		// 触顶加载更多
		onScrollToTop() {
			if (this.hasMore && !this.isLoadingHistory) {
				this.pageNum++
				this.loadHistory()
			}
		},

		async loadHistory() {
			if (this.isLoadingHistory) return
			this.isLoadingHistory = true
			
			try {
				const res = await api.chat.getHistory({
					otherUserId: this.userId,
					pageNum: this.pageNum,
					pageSize: this.pageSize
				})
				
				if (res.code === 200) {
					const records = res.data?.records || res.data || []
					const userInfo = uni.getStorageSync('userInfo')
					const currentUserId = userInfo?.userId || userInfo?.id
					
					// 格式化消息
					const formattedRecords = records.map(item => ({
						messageId: item.messageId, 
						content: item.filteredContent || item.content,
						isMine: item.senderId == currentUserId,
						avatar: (item.senderId == currentUserId) ? this.myAvatar : (item.senderAvatar || this.userAvatar),
						nickname: (item.senderId == currentUserId) ? '我' : (item.senderNickname || this.userName),
						createTime: item.createdAt || item.createTime
					})).reverse() // 假设接口返回的是按时间倒序(最新在前)，我们需要正序显示
					
					// 如果没有更多数据了
					if (records.length < this.pageSize) {
						this.hasMore = false
					}

					if (this.isFirstLoad) {
						// 1. 首次加载：直接赋值，并滚动到底部
						this.messageList = formattedRecords
						this.scrollToBottom()
						this.isFirstLoad = false
					} else {
						// 2. 加载历史：拼接到头部，【不】滚动到底部
						// 记录当前最顶部的消息ID，用于维持视图位置（进阶做法，这里先保持不乱跳）
						const firstMsgId = this.messageList[0]?.messageId
						this.messageList = [...formattedRecords, ...this.messageList]
						
						// 这里设置 scrollIntoView 到刚才的第一条消息，可以防止页面跳动太厉害
						if (firstMsgId) {
							this.$nextTick(() => {
								this.scrollIntoView = 'msg-' + firstMsgId
							})
						}
					}
				}
			} catch (e) {
				console.error(e)
			} finally {
				this.isLoadingHistory = false
			}
		},
		
		connectWebSocket() {
			const userInfo = uni.getStorageSync('userInfo')
			if (!userInfo) return
			const id = userInfo.userId || userInfo.id
			
			this.websocket = uni.connectSocket({
				url: `${api.wsUrl}/websocket/chat/${id}`,
				success: () => console.log('WS Connected')
			})
			
			this.websocket.onMessage((res) => {
				const msg = JSON.parse(res.data)
				if (msg.type === 'message' && msg.senderId == this.userId) {
					this.pushMessage({
						messageId: msg.messageId || Date.now(),
						content: msg.content,
						isMine: false,
						avatar: msg.senderAvatar || this.userAvatar,
						createTime: new Date()
					})
				}
			})
		},
		
		// 接收/发送新消息时调用
		pushMessage(msg) {
			this.messageList.push(msg)
			this.scrollToBottom() // 只有新消息才滚到底部
		},

		sendMessage() {
			const text = this.inputMessage.trim()
			if (!text) return
			
			const tempId = Date.now()
			this.pushMessage({
				messageId: tempId,
				content: text,
				isMine: true,
				avatar: this.myAvatar,
				createTime: new Date(),
				pending: true
			})
			this.inputMessage = ''

			if (this.websocket) {
				this.websocket.send({
					data: JSON.stringify({
						type: 'chat',
						receiverId: this.userId,
						content: text
					}),
					success: () => {
						const target = this.messageList.find(m => m.messageId === tempId)
						if (target) target.pending = false
					}
				})
			}
		},
		
		scrollToBottom() {
			this.$nextTick(() => {
				// 先清空，确保下次赋值能触发变更
				this.scrollIntoView = ''
				setTimeout(() => {
					this.scrollIntoView = 'scroll-bottom-anchor'
				}, 100)
			})
		},
		
		shouldShowTime(index) {
			if (index === 0) return true
			const prev = new Date(this.messageList[index - 1].createTime).getTime()
			const curr = new Date(this.messageList[index].createTime).getTime()
			return (curr - prev) > 5 * 60 * 1000
		},

		formatTime(time) {
			if (!time) return ''
			const date = new Date(time)
			const h = date.getHours().toString().padStart(2, '0')
			const m = date.getMinutes().toString().padStart(2, '0')
			return `${h}:${m}`
		}
	}
}
</script>

<style scoped>
/* 页面容器 */
.chat-page {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #EDEDED;
}

/* 消息滚动区 */
.chat-scroll {
	flex: 1;
	height: 0; /* 关键：强制占据剩余空间 */
	width: 100%;
}

.scroll-inner {
	padding: 20rpx 24rpx 0;
}

/* 底部垫高，必须大于输入框高度 */
.bottom-spacer {
	height: 130rpx; 
	width: 100%;
}

/* 加载更多 Loading */
.history-loading {
	display: flex;
	justify-content: center;
	padding: 20rpx 0;
}
.spinner {
	width: 30rpx;
	height: 30rpx;
	border: 4rpx solid #ccc;
	border-top-color: #999;
	border-radius: 50%;
	animation: spin 0.8s linear infinite;
}

/* 消息行 */
.message-row {
	margin-bottom: 30rpx;
	width: 100%;
}

.timestamp {
	display: flex;
	justify-content: center;
	margin-bottom: 20rpx;
}
.timestamp text {
	font-size: 24rpx;
	color: #999;
	background-color: rgba(0,0,0,0.05);
	padding: 4rpx 10rpx;
	border-radius: 6rpx;
}

.message-body {
	display: flex;
	flex-direction: row;
	align-items: flex-start;
}

.message-body.self {
	flex-direction: row-reverse;
}

.avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 10rpx;
	background: #fff;
	flex-shrink: 0;
}

.content-wrapper {
	max-width: 70%;
	margin: 0 20rpx;
	display: flex;
	flex-direction: column;
}

.message-body.self .content-wrapper {
	align-items: flex-end;
}

.nickname {
	font-size: 22rpx;
	color: #999;
	margin-bottom: 6rpx;
}

/* 气泡 - 微信风格 */
.bubble {
	padding: 18rpx 24rpx;
	border-radius: 10rpx;
	font-size: 32rpx;
	line-height: 1.5;
	position: relative;
	word-break: break-all;
	display: flex;
	align-items: center;
}

.bubble-other {
	background-color: #FFFFFF;
	color: #000;
	border: 1rpx solid #E5E5E5;
}
.bubble-other::before {
	content: '';
	position: absolute;
	top: 24rpx;
	left: -11rpx;
	border-top: 10rpx solid transparent;
	border-bottom: 10rpx solid transparent;
	border-right: 12rpx solid #FFFFFF;
}

.bubble-mine {
	background-color: #95EC69;
	color: #000;
}
.bubble-mine::after {
	content: '';
	position: absolute;
	top: 24rpx;
	right: -11rpx;
	border-top: 10rpx solid transparent;
	border-bottom: 10rpx solid transparent;
	border-left: 12rpx solid #95EC69;
}

/* 发送中 Loading */
.state-icon {
	margin-right: 12rpx;
}
.spinner-small {
	width: 24rpx;
	height: 24rpx;
	border: 3rpx solid rgba(0,0,0,0.1);
	border-top-color: #fff;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

@keyframes spin { 100% { transform: rotate(360deg); } }

/* 底部输入区 - 固定定位 */
.input-area {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background-color: #F7F7F7;
	border-top: 1rpx solid #DCDCDC;
	padding: 16rpx 20rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	z-index: 100;
}

.input-wrapper {
	display: flex;
	align-items: center;
	height: 72rpx;
}

.chat-input {
	flex: 1;
	background: #fff;
	height: 72rpx;
	border-radius: 10rpx;
	padding: 0 20rpx;
	font-size: 30rpx;
	margin-right: 20rpx;
}

.send-btn {
	background-color: #EDEDED;
	color: #999; /* 默认灰色 */
	padding: 0 24rpx;
	height: 64rpx;
	line-height: 64rpx;
	border-radius: 8rpx;
	font-size: 28rpx;
	font-weight: 500;
	transition: background-color 0.2s;
}

.send-btn.active {
	background-color: #07C160; /* 微信绿 */
	color: #fff;
}

.muted-bar {
	text-align: center;
	color: #999;
	font-size: 28rpx;
	padding: 20rpx 0;
}
</style>
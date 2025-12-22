<template>
	<view class="chat-page">
		<!-- 系统通知入口 -->
		<view class="notification-entry" @click="goToNotifications">
			<view class="notification-icon">🔔</view>
			<view class="notification-info">
				<text class="notification-title">系统通知</text>
				<text class="notification-desc">{{ latestNotification || '查看举报处理结果等通知' }}</text>
			</view>
			<view class="notification-badge" v-if="unreadNotificationCount > 0">
				{{ unreadNotificationCount > 99 ? '99+' : unreadNotificationCount }}
			</view>
			<text class="notification-arrow">›</text>
		</view>
		
		<!-- 聊天列表 -->
		<scroll-view class="chat-list" scroll-y @scrolltolower="loadMore">
			<view class="chat-item" v-for="item in chatList" :key="item.sessionId" @click="goToChat(item)">
				<view class="avatar-wrap">
					<image class="avatar" :src="item.avatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'" mode="aspectFill"></image>
					<view class="unread-badge" v-if="item.unreadCount > 0">{{ item.unreadCount > 99 ? '99+' : item.unreadCount }}</view>
				</view>
				<view class="chat-info">
					<view class="chat-header">
						<text class="nickname">{{ item.nickname }}</text>
						<text class="time">{{ formatTime(item.lastTime) }}</text>
					</view>
					<view class="last-message">{{ item.lastMessage }}</view>
				</view>
			</view>
			
			<view class="empty" v-if="!loading && chatList.length === 0">
				<text class="empty-icon">💬</text>
				<text class="empty-text">暂无聊天记录</text>
				<text class="empty-hint">和房东聊聊吧</text>
			</view>
			
			<view class="loading" v-if="loading">
				<text>加载中...</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			chatList: [],
			loading: false,
			unreadNotificationCount: 0,
			latestNotification: ''
		}
	},
	
	onShow() {
		this.loadChatList()
		this.loadNotificationCount()
	},
	
	onPullDownRefresh() {
		Promise.all([
			this.loadChatList(),
			this.loadNotificationCount()
		]).finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadNotificationCount() {
			try {
				const res = await api.notification.getUnreadCount()
				if (res.code === 200) {
					this.unreadNotificationCount = res.data || 0
				}
				// 获取最新通知预览
				const listRes = await api.notification.getList({ pageNum: 1, pageSize: 1 })
				if (listRes.code === 200 && listRes.data?.records?.length > 0) {
					this.latestNotification = listRes.data.records[0].title
				}
			} catch (e) {
				console.error('加载通知数量失败:', e)
			}
		},
		
		goToNotifications() {
			uni.navigateTo({
				url: '/pages/notification/notification'
			})
		},
		
		async loadChatList() {
			this.loading = true
			try {
				const res = await api.chat.getRecent()
				if (res.code === 200) {
					this.chatList = (res.data || []).map(item => ({
						sessionId: item.sessionId || item.otherUserId,
						otherUserId: item.otherUserId,
						nickname: item.otherNickname || '用户',
						avatar: item.otherAvatar,
						lastMessage: item.lastMessage || '',
						lastTime: item.lastTime,
						unreadCount: item.unreadCount || 0
					}))
				}
			} catch (e) {
				console.error('加载聊天列表失败:', e)
			} finally {
				this.loading = false
			}
		},
		
		goToChat(item) {
			const avatar = item.avatar ? encodeURIComponent(item.avatar) : ''
			uni.navigateTo({
				url: `/pages/chat/chat-detail/chat-detail?userId=${item.otherUserId}&name=${encodeURIComponent(item.nickname)}&avatar=${avatar}`
			})
		},
		
		loadMore() {
			// 分页加载更多
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
			return `${month}-${day}`
		}
	}
}
</script>

<style scoped>
.chat-page {
	min-height: 100vh;
	background: #F7F9FC;
}

/* 系统通知入口 */
.notification-entry {
	display: flex;
	align-items: center;
	padding: 32rpx;
	background: linear-gradient(135deg, #FFF5F0, #FFFFFF);
	margin: 20rpx 30rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	transition: all 0.3s ease;
}

.notification-entry:active {
	transform: translateY(-2rpx);
	box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.08);
}

.notification-icon {
	font-size: 52rpx;
	margin-right: 24rpx;
	filter: drop-shadow(0 2rpx 4rpx rgba(255, 107, 53, 0.2));
}

.notification-info {
	flex: 1;
	overflow: hidden;
}

.notification-title {
	font-size: 34rpx;
	font-weight: 700;
	color: #2C3E50;
	display: block;
}

.notification-desc {
	font-size: 26rpx;
	color: #8B95A5;
	display: block;
	margin-top: 8rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.notification-badge {
	min-width: 40rpx;
	height: 40rpx;
	line-height: 40rpx;
	text-align: center;
	font-size: 22rpx;
	color: #FFFFFF;
	background: linear-gradient(135deg, #F5222D, #FF4D4F);
	border-radius: 20rpx;
	padding: 0 12rpx;
	margin-right: 16rpx;
	box-shadow: 0 4rpx 12rpx rgba(245, 34, 45, 0.3);
	font-weight: 700;
}

.notification-arrow {
	font-size: 40rpx;
	color: #E4E7ED;
}

.chat-list {
	height: calc(100vh - 160rpx);
}

.chat-item {
	display: flex;
	align-items: center;
	padding: 32rpx;
	background: #FFFFFF;
	margin: 0 30rpx 20rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.chat-item::before {
	content: '';
	position: absolute;
	top: 0;
	left: 0;
	width: 6rpx;
	height: 100%;
	background: linear-gradient(180deg, #FF6B35, #4ECDC4);
	opacity: 0;
	transition: opacity 0.3s ease;
}

.chat-item:active {
	transform: translateY(-4rpx);
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.chat-item:active::before {
	opacity: 1;
}

.avatar-wrap {
	position: relative;
	margin-right: 24rpx;
}

.avatar {
	width: 110rpx;
	height: 110rpx;
	border-radius: 50%;
	border: 4rpx solid #FFE5D9;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.unread-badge {
	position: absolute;
	top: -8rpx;
	right: -8rpx;
	min-width: 40rpx;
	height: 40rpx;
	line-height: 40rpx;
	text-align: center;
	font-size: 22rpx;
	color: #FFFFFF;
	background: linear-gradient(135deg, #F5222D, #FF4D4F);
	border-radius: 20rpx;
	padding: 0 10rpx;
	box-shadow: 0 4rpx 12rpx rgba(245, 34, 45, 0.3);
	font-weight: 700;
}

.chat-info {
	flex: 1;
	overflow: hidden;
}

.chat-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
}

.nickname {
	font-size: 34rpx;
	font-weight: 700;
	color: #2C3E50;
}

.time {
	font-size: 24rpx;
	color: #8B95A5;
	font-weight: 500;
}

.last-message {
	font-size: 28rpx;
	color: #5A6C7D;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 200rpx 40rpx;
	animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
	from { opacity: 0; transform: translateY(20rpx); }
	to { opacity: 1; transform: translateY(0); }
}

.empty-icon {
	font-size: 140rpx;
	margin-bottom: 32rpx;
	opacity: 0.5;
	filter: grayscale(0.3);
}

.empty-text {
	font-size: 32rpx;
	color: #2C3E50;
	font-weight: 600;
}

.empty-hint {
	font-size: 26rpx;
	color: #8B95A5;
	margin-top: 16rpx;
}

.loading {
	text-align: center;
	padding: 40rpx;
	color: #8B95A5;
	font-size: 28rpx;
	font-weight: 500;
	animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
	0%, 100% { opacity: 1; }
	50% { opacity: 0.5; }
}
</style>

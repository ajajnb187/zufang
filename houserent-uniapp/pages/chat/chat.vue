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
	background: #f5f7fa;
}

/* 系统通知入口 */
.notification-entry {
	display: flex;
	align-items: center;
	padding: 30rpx;
	background: #fff;
	margin-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.notification-icon {
	font-size: 48rpx;
	margin-right: 24rpx;
}

.notification-info {
	flex: 1;
	overflow: hidden;
}

.notification-title {
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
	display: block;
}

.notification-desc {
	font-size: 26rpx;
	color: #999;
	display: block;
	margin-top: 8rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.notification-badge {
	min-width: 36rpx;
	height: 36rpx;
	line-height: 36rpx;
	text-align: center;
	font-size: 22rpx;
	color: #fff;
	background: #f56c6c;
	border-radius: 18rpx;
	padding: 0 10rpx;
	margin-right: 16rpx;
}

.notification-arrow {
	font-size: 36rpx;
	color: #ccc;
}

.chat-list {
	height: calc(100vh - 140rpx);
}

.chat-item {
	display: flex;
	align-items: center;
	padding: 30rpx;
	background: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.avatar-wrap {
	position: relative;
	margin-right: 24rpx;
}

.avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
}

.unread-badge {
	position: absolute;
	top: -10rpx;
	right: -10rpx;
	min-width: 36rpx;
	height: 36rpx;
	line-height: 36rpx;
	text-align: center;
	font-size: 22rpx;
	color: #fff;
	background: #f56c6c;
	border-radius: 18rpx;
	padding: 0 8rpx;
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
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
}

.time {
	font-size: 24rpx;
	color: #999;
}

.last-message {
	font-size: 28rpx;
	color: #999;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 200rpx 40rpx;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 32rpx;
	color: #666;
}

.empty-hint {
	font-size: 26rpx;
	color: #999;
	margin-top: 16rpx;
}

.loading {
	text-align: center;
	padding: 30rpx;
	color: #999;
	font-size: 28rpx;
}
</style>

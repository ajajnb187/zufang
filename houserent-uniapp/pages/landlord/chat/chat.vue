<template>
	<view class="chat-page">
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
				<text class="empty-hint">和租客聊聊吧</text>
			</view>
			
			<view class="loading" v-if="loading">
				<text>加载中...</text>
			</view>
		</scroll-view>
		
		<!-- 房东底部导航栏 -->
		<view class="tabbar">
			<view class="tabbar-item" @click="goToHouses">
				<text class="tabbar-icon">🏠</text>
				<text class="tabbar-text">房源</text>
			</view>
			<view class="tabbar-item" @click="goToTenants">
				<text class="tabbar-icon">👥</text>
				<text class="tabbar-text">租客</text>
			</view>
			<view class="tabbar-item active">
				<text class="tabbar-icon">💬</text>
				<text class="tabbar-text">消息</text>
			</view>
			<view class="tabbar-item" @click="goToProfile">
				<text class="tabbar-icon">👤</text>
				<text class="tabbar-text">我的</text>
			</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			chatList: [],
			loading: false
		}
	},
	
	onShow() {
		this.loadChatList()
	},
	
	onPullDownRefresh() {
		this.loadChatList().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadChatList() {
			this.loading = true
			try {
				const res = await api.chat.getRecent()
				if (res.code === 200) {
					this.chatList = (res.data || []).map(item => ({
						sessionId: item.sessionId || item.otherUserId,
						otherUserId: item.otherUserId,
						nickname: item.otherNickname || '租客',
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
		
		loadMore() {},
		
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
		},
		
		// 底部导航
		goToHouses() {
			uni.reLaunch({ url: '/pages/landlord/houses/houses' })
		},
		goToTenants() {
			uni.reLaunch({ url: '/pages/landlord/tenants/tenants' })
		},
		goToProfile() {
			uni.reLaunch({ url: '/pages/landlord/profile/profile' })
		}
	}
}
</script>

<style scoped>
.chat-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 120rpx;
}

.chat-list {
	height: calc(100vh - 120rpx);
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

/* 底部导航栏 */
.tabbar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 100rpx;
	display: flex;
	background: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
	padding-bottom: env(safe-area-inset-bottom);
	z-index: 999;
}

.tabbar-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.tabbar-icon {
	font-size: 40rpx;
	margin-bottom: 4rpx;
}

.tabbar-text {
	font-size: 22rpx;
	color: #999;
}

.tabbar-item.active .tabbar-text {
	color: #409eff;
}
</style>

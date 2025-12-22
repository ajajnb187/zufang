<template>
	<view class="notification-page">
		<scroll-view class="notification-list" scroll-y @scrolltolower="loadMore">
			<view class="notification-item" v-for="item in notificationList" :key="item.notificationId" 
				:class="{ 'unread': !item.isRead }" @click="viewNotification(item)">
				<view class="notification-icon">
					{{ getNotificationIcon(item.notificationType) }}
				</view>
				<view class="notification-content">
					<view class="notification-header">
						<text class="notification-title">{{ item.title }}</text>
						<text class="notification-time">{{ formatTime(item.createdAt) }}</text>
					</view>
					<view class="notification-body">{{ item.content }}</view>
				</view>
				<view class="unread-dot" v-if="!item.isRead"></view>
			</view>
			
			<view class="empty" v-if="!loading && notificationList.length === 0">
				<text class="empty-icon">🔔</text>
				<text class="empty-text">暂无通知</text>
			</view>
			
			<view class="loading" v-if="loading">
				<text>加载中...</text>
			</view>
			
			<view class="no-more" v-if="noMore && notificationList.length > 0">
				<text>没有更多了</text>
			</view>
		</scroll-view>
		
		<!-- 底部操作栏 -->
		<view class="bottom-bar" v-if="notificationList.length > 0">
			<button class="mark-all-btn" @click="markAllAsRead">全部标记已读</button>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			notificationList: [],
			loading: false,
			noMore: false,
			pageNum: 1,
			pageSize: 20
		}
	},
	
	onLoad() {
		this.loadNotifications()
	},
	
	onShow() {
		// 刷新通知列表
		this.pageNum = 1
		this.noMore = false
		this.loadNotifications()
	},
	
	onPullDownRefresh() {
		this.pageNum = 1
		this.noMore = false
		this.loadNotifications().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadNotifications() {
			if (this.loading) return
			
			this.loading = true
			try {
				const res = await api.notification.getList({
					pageNum: this.pageNum,
					pageSize: this.pageSize
				})
				
				if (res.code === 200) {
					const records = res.data?.records || res.data || []
					if (this.pageNum === 1) {
						this.notificationList = records
					} else {
						this.notificationList = [...this.notificationList, ...records]
					}
					this.noMore = records.length < this.pageSize
				}
			} catch (e) {
				console.error('加载通知失败:', e)
			} finally {
				this.loading = false
			}
		},
		
		loadMore() {
			if (this.noMore || this.loading) return
			this.pageNum++
			this.loadNotifications()
		},
		
		async viewNotification(item) {
			// 标记为已读
			if (!item.isRead) {
				try {
					await api.notification.markAsRead(item.notificationId)
					item.isRead = true
				} catch (e) {
					console.error('标记已读失败:', e)
				}
			}
			
			// 根据通知类型跳转到对应页面
			if (item.relatedType === 'report' && item.relatedId) {
				// 举报相关通知，显示详情弹窗
				uni.showModal({
					title: item.title,
					content: item.content,
					showCancel: false,
					confirmText: '知道了'
				})
			} else if (item.relatedType === 'house' && item.relatedId) {
				uni.navigateTo({
					url: `/pages/house/detail/detail?id=${item.relatedId}`
				})
			} else if (item.relatedType === 'contract' && item.relatedId) {
				uni.navigateTo({
					url: `/pages/contract/detail/detail?id=${item.relatedId}`
				})
			} else if (item.relatedType === 'verification' && item.relatedId) {
				uni.navigateTo({
					url: '/pages/community/auth/auth'
				})
			} else {
				// 显示通知详情弹窗
				uni.showModal({
					title: item.title,
					content: item.content,
					showCancel: false,
					confirmText: '知道了'
				})
			}
		},
		
		async markAllAsRead() {
			try {
				uni.showLoading({ title: '处理中...' })
				await api.notification.markAllAsRead()
				// 更新本地状态
				this.notificationList.forEach(item => {
					item.isRead = true
				})
				uni.showToast({ title: '已全部标记已读', icon: 'success' })
			} catch (e) {
				console.error('标记全部已读失败:', e)
				uni.showToast({ title: '操作失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},
		
		getNotificationIcon(type) {
			const iconMap = {
				'report_handled': '✅',
				'report_rejected': '❌',
				'report_received': '📋',
				'house_approved': '🏠',
				'house_rejected': '🚫',
				'contract_created': '📝',
				'contract_signed': '✍️',
				'contract_approved': '✅',
				'contract_rejected': '❌',
				'verification_approved': '✅',
				'verification_rejected': '❌',
				'verification_community_approved': '👍',
				'verification_community_rejected': '👎'
			}
			return iconMap[type] || '🔔'
		},
		
		formatTime(time) {
			if (!time) return ''
			const date = new Date(time)
			const now = new Date()
			const diff = now - date
			
			if (diff < 60000) return '刚刚'
			if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
			if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
			if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
			
			const month = date.getMonth() + 1
			const day = date.getDate()
			return `${month}-${day}`
		}
	}
}
</script>

<style scoped>
.notification-page {
	min-height: 100vh;
	background: #F7F9FC;
	padding-bottom: 140rpx;
}

.notification-list {
	height: calc(100vh - 140rpx);
	padding: 20rpx 0;
}

.notification-item {
	display: flex;
	align-items: flex-start;
	padding: 32rpx;
	background: #FFFFFF;
	margin: 0 30rpx 20rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	position: relative;
	transition: all 0.3s ease;
	overflow: hidden;
}

.notification-item::before {
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

.notification-item:active {
	transform: translateY(-4rpx);
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.notification-item:active::before {
	opacity: 1;
}

.notification-item.unread {
	background: linear-gradient(135deg, #FFF5F0, #FFFFFF);
	border-left: 4rpx solid #FF6B35;
}

.notification-icon {
	font-size: 52rpx;
	margin-right: 24rpx;
	flex-shrink: 0;
	filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}

.notification-content {
	flex: 1;
	overflow: hidden;
}

.notification-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
}

.notification-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #2C3E50;
	flex: 1;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.notification-time {
	font-size: 24rpx;
	color: #8B95A5;
	margin-left: 20rpx;
	flex-shrink: 0;
	font-weight: 500;
}

.notification-body {
	font-size: 28rpx;
	color: #5A6C7D;
	line-height: 1.6;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.unread-dot {
	position: absolute;
	top: 32rpx;
	right: 32rpx;
	width: 20rpx;
	height: 20rpx;
	background: linear-gradient(135deg, #F5222D, #FF4D4F);
	border-radius: 50%;
	box-shadow: 0 4rpx 12rpx rgba(245, 34, 45, 0.4);
	animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
	0%, 100% { transform: scale(1); opacity: 1; }
	50% { transform: scale(1.1); opacity: 0.8; }
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
	color: #8B95A5;
	font-weight: 600;
}

.loading, .no-more {
	text-align: center;
	padding: 40rpx;
	color: #8B95A5;
	font-size: 28rpx;
	font-weight: 500;
}

.loading {
	animation: pulse 1.5s ease-in-out infinite;
}

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	padding: 24rpx 30rpx;
	background: #FFFFFF;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
	padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	backdrop-filter: blur(10rpx);
}

.mark-all-btn {
	width: 100%;
	height: 92rpx;
	line-height: 92rpx;
	background: linear-gradient(135deg, #4ECDC4, #44A3D5);
	color: #FFFFFF;
	font-size: 32rpx;
	font-weight: 700;
	border-radius: 48rpx;
	border: none;
	box-shadow: 0 8rpx 24rpx rgba(78, 205, 196, 0.35);
	transition: all 0.3s ease;
}

.mark-all-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(78, 205, 196, 0.3);
}
</style>

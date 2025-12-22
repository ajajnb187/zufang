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
	background: #f5f7fa;
	padding-bottom: 120rpx;
}

.notification-list {
	height: calc(100vh - 120rpx);
}

.notification-item {
	display: flex;
	align-items: flex-start;
	padding: 30rpx;
	background: #fff;
	border-bottom: 1rpx solid #f0f0f0;
	position: relative;
}

.notification-item.unread {
	background: #f8faff;
}

.notification-icon {
	font-size: 48rpx;
	margin-right: 24rpx;
	flex-shrink: 0;
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
	font-size: 30rpx;
	font-weight: 500;
	color: #333;
	flex: 1;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.notification-time {
	font-size: 24rpx;
	color: #999;
	margin-left: 20rpx;
	flex-shrink: 0;
}

.notification-body {
	font-size: 28rpx;
	color: #666;
	line-height: 1.5;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.unread-dot {
	position: absolute;
	top: 30rpx;
	right: 30rpx;
	width: 16rpx;
	height: 16rpx;
	background: #f56c6c;
	border-radius: 50%;
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
	opacity: 0.5;
}

.empty-text {
	font-size: 32rpx;
	color: #999;
}

.loading, .no-more {
	text-align: center;
	padding: 30rpx;
	color: #999;
	font-size: 28rpx;
}

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	padding: 20rpx 30rpx;
	background: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.mark-all-btn {
	width: 100%;
	height: 88rpx;
	line-height: 88rpx;
	background: #409eff;
	color: #fff;
	font-size: 32rpx;
	border-radius: 44rpx;
	border: none;
}
</style>

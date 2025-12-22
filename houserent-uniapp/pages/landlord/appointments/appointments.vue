<template>
	<view class="appointments-page">
		<!-- 状态筛选 -->
		<scroll-view class="status-tabs" scroll-x>
			<view class="tab-item" :class="{ active: activeStatus === '' }" @click="switchStatus('')">
				全部
			</view>
			<view class="tab-item" :class="{ active: activeStatus === 'pending' }" @click="switchStatus('pending')">
				待确认
			</view>
			<view class="tab-item" :class="{ active: activeStatus === 'confirmed' }" @click="switchStatus('confirmed')">
				已确认
			</view>
			<view class="tab-item" :class="{ active: activeStatus === 'completed' }" @click="switchStatus('completed')">
				已看房
			</view>
		</scroll-view>
		
		<!-- 预约列表 -->
		<scroll-view class="appointment-list" scroll-y>
			<view class="appointment-card" v-for="item in appointmentList" :key="item.appointmentId">
				<view class="card-header">
					<view class="user-info">
						<image class="avatar" :src="item.userAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
						<view class="user-detail">
							<view class="user-name">{{ item.userName }}</view>
							<view class="user-phone">{{ item.userPhone }}</view>
						</view>
					</view>
					<view class="status-badge" :class="item.status">
						{{ getStatusText(item.status) }}
					</view>
				</view>
				
				<view class="card-body">
					<view class="info-row">
						<text class="label">预约房源</text>
						<text class="value">{{ item.houseTitle }}</text>
					</view>
					<view class="info-row">
						<text class="label">预约时间</text>
						<text class="value">{{ item.appointmentTime }}</text>
					</view>
					<view class="info-row" v-if="item.remark">
						<text class="label">备注</text>
						<text class="value">{{ item.remark }}</text>
					</view>
				</view>
				
				<view class="card-actions">
					<!-- 待确认状态 -->
					<template v-if="item.status === 'pending'">
						<view class="action-btn secondary" @click="rejectAppointment(item)">
							拒绝
						</view>
						<view class="action-btn primary" @click="confirmAppointment(item)">
							确认预约
						</view>
					</template>
					
					<!-- 已确认状态 -->
					<template v-if="item.status === 'confirmed'">
						<view class="action-btn secondary" @click="chatWithUser(item)">
							💬 联系
						</view>
						<view class="action-btn primary" @click="markCompleted(item)">
							标记已看房
						</view>
					</template>
					
					<!-- 已看房状态 - 可发起合同 -->
					<template v-if="item.status === 'completed'">
						<view class="action-btn secondary" @click="chatWithUser(item)">
							💬 联系
						</view>
						<view class="action-btn primary" @click="createContract(item)">
							📄 发起合同
						</view>
					</template>
				</view>
			</view>
			
			<view class="empty" v-if="appointmentList.length === 0">
				<text class="empty-icon">📅</text>
				<text class="empty-text">暂无预约记录</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			activeStatus: '',
			appointmentList: []
		}
	},
	
	onShow() {
		this.loadAppointments()
	},
	
	onPullDownRefresh() {
		this.loadAppointments().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadAppointments() {
			try {
				const res = await api.appointment.getLandlordAppointments()
				if (res.code === 200) {
					let list = res.data || []
					if (this.activeStatus) {
						list = list.filter(item => item.status === this.activeStatus)
					}
					this.appointmentList = list
				}
			} catch (e) {
				console.error('加载预约失败:', e)
			}
		},
		
		switchStatus(status) {
			this.activeStatus = status
			this.loadAppointments()
		},
		
		getStatusText(status) {
			const map = {
				'pending': '待确认',
				'confirmed': '已确认',
				'completed': '已看房',
				'cancelled': '已取消',
				'rejected': '已拒绝'
			}
			return map[status] || status
		},
		
		async confirmAppointment(item) {
			try {
				uni.showLoading({ title: '确认中...' })
				const res = await api.appointment.confirm(item.appointmentId)
				uni.hideLoading()
				if (res.code === 200) {
					uni.showToast({ title: '已确认', icon: 'success' })
					this.loadAppointments()
				}
			} catch (e) {
				uni.hideLoading()
				console.error('确认预约失败:', e)
			}
		},
		
		rejectAppointment(item) {
			uni.showModal({
				title: '拒绝预约',
				content: '确定要拒绝此预约吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							const result = await api.appointment.cancel(item.appointmentId)
							if (result.code === 200) {
								uni.showToast({ title: '已拒绝', icon: 'success' })
								this.loadAppointments()
							}
						} catch (e) {
							console.error('拒绝预约失败:', e)
						}
					}
				}
			})
		},
		
		async markCompleted(item) {
			uni.showModal({
				title: '确认看房完成',
				content: '确认租客已完成看房？',
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '处理中...' })
							const result = await api.appointment.complete(item.appointmentId)
							uni.hideLoading()
							if (result.code === 200) {
								uni.showToast({ title: '已标记完成', icon: 'success' })
								this.loadAppointments()
							} else {
								uni.showToast({ title: result.message || '操作失败', icon: 'none' })
							}
						} catch (e) {
							uni.hideLoading()
							console.error('标记完成失败:', e)
							uni.showToast({ title: '操作失败', icon: 'none' })
						}
					}
				}
			})
		},
		
		chatWithUser(item) {
			uni.navigateTo({
				url: `/pages/chat/chat-detail/chat-detail?userId=${item.userId}&houseId=${item.houseId}`
			})
		},
		
		createContract(item) {
			// 跳转到发起合同页面，带上租客和房源信息
			const params = `houseId=${item.houseId}&tenantId=${item.userId}&tenantName=${encodeURIComponent(item.userName)}&tenantAvatar=${encodeURIComponent(item.userAvatar || '')}&source=预约看房`
			uni.navigateTo({
				url: `/pages/landlord/contract-create/contract-create?${params}`
			})
		}
	}
}
</script>

<style scoped>
.appointments-page {
	min-height: 100vh;
	background: #f5f7fa;
}

.status-tabs {
	display: flex;
	background: #fff;
	padding: 20rpx;
	white-space: nowrap;
	position: sticky;
	top: 0;
	z-index: 10;
}

.tab-item {
	display: inline-block;
	padding: 16rpx 32rpx;
	margin-right: 20rpx;
	background: #f5f7fa;
	border-radius: 40rpx;
	font-size: 28rpx;
	color: #666;
}

.tab-item.active {
	background: #409eff;
	color: #fff;
}

.appointment-list {
	height: calc(100vh - 100rpx);
	padding: 20rpx;
}

.appointment-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.user-info {
	display: flex;
	align-items: center;
}

.avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.user-name {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 8rpx;
}

.user-phone {
	font-size: 24rpx;
	color: #999;
}

.status-badge {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.status-badge.pending {
	background: #fff3e0;
	color: #ff9800;
}

.status-badge.confirmed {
	background: #e3f2fd;
	color: #2196f3;
}

.status-badge.completed {
	background: #e8f5e9;
	color: #4caf50;
}

.status-badge.cancelled,
.status-badge.rejected {
	background: #fafafa;
	color: #999;
}

.card-body {
	margin-bottom: 20rpx;
}

.info-row {
	display: flex;
	justify-content: space-between;
	padding: 12rpx 0;
	font-size: 28rpx;
}

.info-row .label {
	color: #999;
}

.info-row .value {
	color: #333;
	flex: 1;
	text-align: right;
}

.card-actions {
	display: flex;
	gap: 20rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid #f0f0f0;
}

.action-btn {
	flex: 1;
	text-align: center;
	padding: 20rpx;
	border-radius: 50rpx;
	font-size: 28rpx;
}

.action-btn.primary {
	background: #409eff;
	color: #fff;
}

.action-btn.secondary {
	background: #f5f7fa;
	color: #666;
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
	color: #999;
}
</style>

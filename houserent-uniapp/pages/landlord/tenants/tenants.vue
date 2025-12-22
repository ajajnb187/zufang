<template>
	<view class="tenants-page">
		<!-- 统计卡片 -->
		<view class="stats-card">
			<view class="stat-item">
				<text class="stat-num">{{ stats.total }}</text>
				<text class="stat-label">租客总数</text>
			</view>
			<view class="stat-item">
				<text class="stat-num">{{ stats.active }}</text>
				<text class="stat-label">在租</text>
			</view>
			<view class="stat-item">
				<text class="stat-num">{{ stats.expiring }}</text>
				<text class="stat-label">即将到期</text>
			</view>
		</view>
		
		<!-- 租客列表 -->
		<scroll-view class="tenant-list" scroll-y>
			<view class="tenant-card" v-for="tenant in tenantList" :key="tenant.contractId">
				<view class="tenant-header">
					<image class="avatar" :src="tenant.avatarUrl"></image>
					<view class="tenant-info">
						<view class="tenant-name">{{ tenant.nickname }}</view>
						<view class="tenant-rating">
							<text class="star">⭐</text>
							<text class="rating-score">{{ tenant.rating || '暂无评分' }}</text>
						</view>
					</view>
					<view class="status-badge" :class="tenant.status">
						{{ getStatusText(tenant.status) }}
					</view>
				</view>
				
				<view class="rental-info">
					<view class="info-row">
						<text class="label">房源</text>
						<text class="value">{{ tenant.houseTitle }}</text>
					</view>
					<view class="info-row">
						<text class="label">租期</text>
						<text class="value">{{ tenant.startDate }} 至 {{ tenant.endDate }}</text>
					</view>
					<view class="info-row">
						<text class="label">月租金</text>
						<text class="value price">¥{{ tenant.monthlyRent }}</text>
					</view>
					<view class="info-row">
						<text class="label">联系方式</text>
						<text class="value phone" @click="callTenant(tenant.phone)">{{ tenant.phone }}</text>
					</view>
				</view>
				
				<view class="tenant-actions">
					<view class="action-btn" @click="chatWithTenant(tenant)">
						<text>💬 联系</text>
					</view>
					<view class="action-btn" @click="viewContract(tenant.contractId)">
						<text>📄 合同</text>
					</view>
					<!-- 确认入住按钮 - 合同生效后显示 -->
					<view class="action-btn primary" v-if="canConfirmCheckin(tenant)" @click="confirmCheckin(tenant)">
						<text>✅ 确认入住</text>
					</view>
					<view class="action-btn disabled" v-else-if="showCheckinStatus(tenant)">
						<text>{{ getCheckinStatusText(tenant) }}</text>
					</view>
					<view class="action-btn" v-if="tenant.canEvaluate" @click="evaluateTenant(tenant)">
						<text>⭐ 评价</text>
					</view>
				</view>
			</view>
			
			<view class="empty" v-if="tenantList.length === 0">
				<text class="empty-icon">👥</text>
				<text class="empty-text">暂无租客</text>
			</view>
		</scroll-view>
		
		<!-- 房东底部导航栏 -->
		<view class="tabbar">
			<view class="tabbar-item" @click="goToHouses">
				<text class="tabbar-icon">🏠</text>
				<text class="tabbar-text">房源</text>
			</view>
			<view class="tabbar-item active">
				<text class="tabbar-icon">👥</text>
				<text class="tabbar-text">租客</text>
			</view>
			<view class="tabbar-item" @click="goToChat">
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
			stats: {
				total: 0,
				active: 0,
				expiring: 0
			},
			tenantList: []
		}
	},
	
	onShow() {
		this.loadStats()
		this.loadTenants()
	},
	
	// 下拉刷新 - 必须放在methods外面作为页面生命周期函数
	onPullDownRefresh() {
		console.log('触发下拉刷新')
		Promise.all([
			this.loadStats(),
			this.loadTenants()
		]).finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadStats() {
			try {
				const res = await api.landlord.getTenantStats()
				if (res.code === 200) {
					// 映射后端字段名
					this.stats = {
						total: res.data.totalTenants || 0,
						active: res.data.activeTenants || 0,
						expiring: res.data.expiringSoon || 0
					}
				}
			} catch (e) {
				console.error('加载统计失败:', e)
			}
		},
		
		async loadTenants() {
			try {
				const res = await api.landlord.getTenants({ pageNum: 1, pageSize: 100 })
				if (res.code === 200) {
					// 格式化数据
					this.tenantList = (res.data.records || []).map(item => ({
						...item,
						userId: item.tenantId,
						nickname: item.tenantName,
						avatarUrl: item.avatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
						monthlyRent: item.rentPrice,
						rating: item.rating || null,
						canEvaluate: item.status === 'active' || item.status === 'expiring'
					}))
				}
			} catch (e) {
				console.error('加载租客列表失败:', e)
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		
		getStatusText(status) {
			const texts = {
				pending_sign: '待签署',
				pending_audit: '审核中',
				active: '租约中',
				expiring: '即将到期',
				expired: '已到期',
				terminated: '已终止',
				rejected: '已拒绝'
			}
			return texts[status] || status
		},
		
		// 是否可以确认入住（合同生效 + 房东未确认）
		canConfirmCheckin(tenant) {
			return tenant.contractStatus === 'effective' && 
				   !tenant.landlordCheckinConfirm && 
				   tenant.transactionId
		},
		
		// 是否显示入住状态
		showCheckinStatus(tenant) {
			return tenant.contractStatus === 'effective' && tenant.transactionId
		},
		
		// 获取入住状态文字
		getCheckinStatusText(tenant) {
			if (tenant.landlordCheckinConfirm && tenant.tenantCheckinConfirm) {
				return '✅ 已入住'
			} else if (tenant.landlordCheckinConfirm) {
				return '等待租客确认'
			} else if (tenant.tenantCheckinConfirm) {
				return '待您确认'
			}
			return '待确认入住'
		},
		
		// 确认入住
		async confirmCheckin(tenant) {
			if (!tenant.transactionId) {
				uni.showToast({ title: '交易记录不存在', icon: 'none' })
				return
			}
			
			uni.showModal({
				title: '确认入住',
				content: `确认租客 ${tenant.nickname} 已入住？`,
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '确认中...' })
							const result = await api.transaction.confirmCheckin(tenant.transactionId, {
								checkinDate: new Date().toISOString().split('T')[0]
							})
							uni.hideLoading()
							if (result.code === 200) {
								uni.showToast({ title: '确认成功', icon: 'success' })
								this.loadTenants()
							} else {
								uni.showToast({ title: result.message || '确认失败', icon: 'none' })
							}
						} catch (e) {
							uni.hideLoading()
							console.error('确认入住失败:', e)
							uni.showToast({ title: '确认失败', icon: 'none' })
						}
					}
				}
			})
		},
		
		callTenant(phone) {
			uni.makePhoneCall({
				phoneNumber: phone
			})
		},
		
		chatWithTenant(tenant) {
			uni.navigateTo({
				url: `/pages/chat/chat-detail/chat-detail?userId=${tenant.userId}`
			})
		},
		
		viewContract(contractId) {
			uni.navigateTo({
				url: `/pages/contract/detail/detail?id=${contractId}`
			})
		},
		
		evaluateTenant(tenant) {
			uni.navigateTo({
				url: `/pages/evaluation/evaluation?targetUserId=${tenant.userId}&contractId=${tenant.contractId}`
			})
		},
		
		// 底部导航
		goToHouses() {
			uni.reLaunch({ url: '/pages/landlord/houses/houses' })
		},
		goToChat() {
			uni.reLaunch({ url: '/pages/landlord/chat/chat' })
		},
		goToProfile() {
			uni.reLaunch({ url: '/pages/landlord/profile/profile' })
		}
	}
}
</script>

<style scoped>
.tenants-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 120rpx;
}

.stats-card {
	display: flex;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 40rpx 20rpx;
	color: #fff;
}

.stat-item {
	flex: 1;
	text-align: center;
}

.stat-num {
	display: block;
	font-size: 48rpx;
	font-weight: bold;
	margin-bottom: 8rpx;
}

.stat-label {
	font-size: 24rpx;
	opacity: 0.9;
}

.tenant-list {
	height: calc(100vh - 250rpx);
	padding: 20rpx;
}

.tenant-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.tenant-header {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.tenant-info {
	flex: 1;
}

.tenant-name {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 12rpx;
}

.tenant-rating {
	display: flex;
	align-items: center;
	gap: 8rpx;
	font-size: 24rpx;
	color: #666;
}

.star {
	font-size: 28rpx;
}

.status-badge {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.status-badge.active {
	background: #e8f5e9;
	color: #4caf50;
}

.status-badge.pending_sign {
	background: #e3f2fd;
	color: #2196f3;
}

.status-badge.pending_audit {
	background: #fff8e1;
	color: #ffa000;
}

.status-badge.expiring {
	background: #fff3e0;
	color: #ff9800;
}

.status-badge.expired,
.status-badge.terminated {
	background: #fafafa;
	color: #999;
}

.status-badge.rejected {
	background: #ffebee;
	color: #f44336;
}

.rental-info {
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
}

.info-row .value.price {
	color: #ff6b6b;
	font-weight: 600;
}

.info-row .value.phone {
	color: #409eff;
}

.tenant-actions {
	display: flex;
	gap: 20rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid #f0f0f0;
}

.action-btn {
	flex: 1;
	text-align: center;
	padding: 20rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
	font-size: 26rpx;
	color: #666;
}

.action-btn.primary {
	background: #409eff;
	color: #fff;
}

.action-btn.disabled {
	background: #f0f0f0;
	color: #999;
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

.landlord-tabbar .tab-item.active .tab-text {
	color: #409eff;
}
</style>

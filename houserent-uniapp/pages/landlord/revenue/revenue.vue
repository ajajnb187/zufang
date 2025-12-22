<template>
	<view class="revenue-page">
		<!-- 总收益卡片 -->
		<view class="total-revenue-card">
			<view class="total-label">累计已确认收益（元）</view>
			<view class="total-amount">{{ formatMoney(stats.totalRevenue) }}</view>
			<view class="revenue-detail">
				<view class="detail-item">
					<text class="item-label">本月预期</text>
					<text class="item-value expected">¥{{ formatMoney(stats.expectedMonthRevenue) }}</text>
				</view>
				<view class="detail-item">
					<text class="item-label">本月已收</text>
					<text class="item-value">¥{{ formatMoney(stats.monthRevenue) }}</text>
				</view>
				<view class="detail-item">
					<text class="item-label">在租房源</text>
					<text class="item-value">{{ stats.livingCount || 0 }}套</text>
				</view>
			</view>
		</view>
		
		<!-- 操作按钮 -->
		<view class="action-bar">
			<view class="action-btn full" @click="goToPaymentList">
				<text class="btn-icon">📋</text>
				<text>查看收款记录</text>
			</view>
		</view>
		
		<!-- 待确认收款 -->
		<view class="pending-section" v-if="pendingRecords.length > 0">
			<view class="section-title">待确认收款</view>
			<view class="pending-card" v-for="record in pendingRecords" :key="record.recordId">
				<view class="pending-info">
					<view class="tenant-name">{{ record.tenantName || '租客' }}</view>
					<view class="payment-detail">
						<text>{{ record.paymentPeriod }} · {{ getPaymentTypeText(record.paymentType) }}</text>
					</view>
				</view>
				<view class="pending-right">
					<view class="pending-amount">¥{{ record.amount }}</view>
					<view class="confirm-btn" @click="confirmPayment(record)" 
						v-if="!record.landlordConfirmed">确认收款</view>
					<view class="confirmed-tag" v-else>已确认</view>
				</view>
			</view>
		</view>
		
		<!-- 月度收益统计 -->
		<view class="monthly-section">
			<view class="section-title">月度收益</view>
			<view class="monthly-list">
				<view class="monthly-item" v-for="item in monthlyStats" :key="item.period">
					<text class="month-label">{{ item.period }}</text>
					<text class="month-amount">¥{{ formatMoney(item.total) }}</text>
				</view>
				<view class="empty-monthly" v-if="monthlyStats.length === 0">
					<text>暂无收益记录</text>
				</view>
			</view>
		</view>
		
		<!-- 说明提示 -->
		<view class="tips-card">
			<text class="tips-title">💡 收益说明</text>
			<text class="tips-content">系统自动管理您的租金收益：</text>
			<text class="tips-item">1. 双方确认入住后，系统自动生成租金账单</text>
			<text class="tips-item">2. 每月1日自动生成在租房源的当月账单</text>
			<text class="tips-item">3. 本月预期 = 所有在租房源的月租总和</text>
			<text class="tips-item">4. 水电费等其他费用可手动记录</text>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			stats: {
				totalRevenue: 0,
				monthRevenue: 0,
				expectedMonthRevenue: 0,
				pendingAmount: 0,
				livingCount: 0
			},
			pendingRecords: [],
			monthlyStats: []
		}
	},
	
	onShow() {
		this.loadData()
	},
	
	onPullDownRefresh() {
		this.loadData().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadData() {
			await Promise.all([
				this.loadRevenueStats(),
				this.loadPendingRecords()
			])
		},
		
		async loadRevenueStats() {
			try {
				const res = await api.payment.getLandlordStats()
				if (res.code === 200) {
					this.stats = {
						totalRevenue: res.data.totalRevenue || 0,
						monthRevenue: res.data.monthRevenue || 0,
						expectedMonthRevenue: res.data.expectedMonthRevenue || 0,
						pendingAmount: res.data.pendingAmount || 0,
						livingCount: res.data.livingCount || 0
					}
					this.monthlyStats = res.data.monthlyStats || []
				}
			} catch (e) {
				console.error('加载收益统计失败:', e)
			}
		},
		
		async loadPendingRecords() {
			try {
				const res = await api.payment.getLandlordRecords()
				if (res.code === 200) {
					// 过滤出待确认的记录
					this.pendingRecords = (res.data || []).filter(r => r.status === 'pending')
				}
			} catch (e) {
				console.error('加载收款记录失败:', e)
			}
		},
		
		formatMoney(value) {
			if (!value) return '0.00'
			return Number(value).toFixed(2)
		},
		
		getPaymentTypeText(type) {
			const map = {
				'rent': '租金',
				'deposit': '押金',
				'utility': '水电费',
				'other': '其他'
			}
			return map[type] || '租金'
		},
		
		async confirmPayment(record) {
			uni.showModal({
				title: '确认收款',
				content: `确认已收到 ¥${record.amount} 的${this.getPaymentTypeText(record.paymentType)}？`,
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '确认中...' })
							const result = await api.payment.confirm(record.recordId)
							uni.hideLoading()
							if (result.code === 200) {
								uni.showToast({ title: '确认成功', icon: 'success' })
								this.loadData()
							} else {
								uni.showToast({ title: result.message || '确认失败', icon: 'none' })
							}
						} catch (e) {
							uni.hideLoading()
							uni.showToast({ title: '操作失败', icon: 'none' })
						}
					}
				}
			})
		},
		
		goToPaymentList() {
			uni.navigateTo({
				url: '/pages/landlord/payment-list/payment-list'
			})
		}
	}
}
</script>

<style scoped>
.revenue-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding: 20rpx;
	padding-bottom: 40rpx;
}

.total-revenue-card {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 20rpx;
	padding: 50rpx 30rpx;
	color: #fff;
	margin-bottom: 20rpx;
}

.total-label {
	font-size: 28rpx;
	opacity: 0.9;
	margin-bottom: 20rpx;
}

.total-amount {
	font-size: 72rpx;
	font-weight: bold;
	margin-bottom: 30rpx;
}

.revenue-detail {
	display: flex;
	justify-content: space-around;
	padding-top: 30rpx;
	border-top: 1rpx solid rgba(255, 255, 255, 0.3);
}

.detail-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.item-label {
	font-size: 24rpx;
	opacity: 0.8;
	margin-bottom: 12rpx;
}

.item-value {
	font-size: 36rpx;
	font-weight: 600;
}

.item-value.pending {
	color: #ffd54f;
}

.item-value.expected {
	color: #81d4fa;
}

/* 操作按钮 */
.action-bar {
	display: flex;
	gap: 20rpx;
	margin-bottom: 20rpx;
}

.action-btn {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	background: #fff;
	padding: 24rpx;
	border-radius: 16rpx;
	font-size: 28rpx;
	color: #333;
}

.action-btn.full {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
}

.btn-icon {
	font-size: 36rpx;
}

/* 待确认收款 */
.pending-section {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 24rpx;
}

.pending-card {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 24rpx;
	background: #fff3e0;
	border-radius: 12rpx;
	margin-bottom: 16rpx;
}

.pending-info {
	flex: 1;
}

.tenant-name {
	font-size: 30rpx;
	font-weight: 600;
	margin-bottom: 8rpx;
}

.payment-detail {
	font-size: 24rpx;
	color: #999;
}

.pending-right {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	gap: 12rpx;
}

.pending-amount {
	font-size: 32rpx;
	font-weight: 600;
	color: #ff9800;
}

.confirm-btn {
	padding: 10rpx 24rpx;
	background: #4caf50;
	color: #fff;
	border-radius: 30rpx;
	font-size: 24rpx;
}

.confirmed-tag {
	padding: 10rpx 24rpx;
	background: #e0e0e0;
	color: #666;
	border-radius: 30rpx;
	font-size: 24rpx;
}

/* 月度收益 */
.monthly-section {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.monthly-list {
	max-height: 400rpx;
}

.monthly-item {
	display: flex;
	justify-content: space-between;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.month-label {
	font-size: 28rpx;
	color: #666;
}

.month-amount {
	font-size: 28rpx;
	font-weight: 600;
	color: #4caf50;
}

.empty-monthly {
	text-align: center;
	padding: 40rpx;
	color: #999;
	font-size: 28rpx;
}

/* 说明提示 */
.tips-card {
	background: #e3f2fd;
	border-radius: 16rpx;
	padding: 30rpx;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.tips-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #1976d2;
}

.tips-content {
	font-size: 26rpx;
	color: #333;
}

.tips-item {
	font-size: 24rpx;
	color: #666;
	padding-left: 20rpx;
}
</style>

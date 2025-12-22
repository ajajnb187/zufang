<template>
	<view class="payment-list-page">
		<!-- 筛选标签 -->
		<view class="filter-tabs">
			<view class="tab-item" :class="{ active: activeStatus === '' }" @click="switchStatus('')">
				全部
			</view>
			<view class="tab-item" :class="{ active: activeStatus === 'confirmed' }" @click="switchStatus('confirmed')">
				已确认
			</view>
			<view class="tab-item" :class="{ active: activeStatus === 'pending' }" @click="switchStatus('pending')">
				待确认
			</view>
		</view>
		
		<!-- 统计概览 -->
		<view class="stats-bar">
			<view class="stat-item">
				<text class="stat-value">¥{{ formatMoney(totalConfirmed) }}</text>
				<text class="stat-label">已确认</text>
			</view>
			<view class="stat-divider"></view>
			<view class="stat-item">
				<text class="stat-value pending-color">{{ confirmedCount }}</text>
				<text class="stat-label">确认笔数</text>
			</view>
			<view class="stat-divider"></view>
			<view class="stat-item">
				<text class="stat-value">{{ pendingCount }}</text>
				<text class="stat-label">待确认</text>
			</view>
		</view>
		
		<!-- 收款记录列表 -->
		<scroll-view class="record-list" scroll-y @scrolltolower="loadMore">
			<view class="record-card" v-for="record in filteredRecords" :key="record.recordId">
				<view class="record-header">
					<view class="tenant-info">
						<text class="tenant-name">{{ record.tenantName || '租客' }}</text>
						<text class="house-title">{{ record.houseTitle || '' }}</text>
					</view>
					<view class="status-badge" :class="record.status">
						{{ getStatusText(record.status) }}
					</view>
				</view>
				
				<view class="record-body">
					<view class="amount-row">
						<text class="amount-value">¥{{ record.amount }}</text>
						<text class="amount-type">{{ getPaymentTypeText(record.paymentType) }}</text>
					</view>
					<view class="detail-grid">
						<view class="detail-item">
							<text class="detail-label">账期</text>
							<text class="detail-value">{{ record.paymentPeriod || '-' }}</text>
						</view>
						<view class="detail-item">
							<text class="detail-label">创建时间</text>
							<text class="detail-value">{{ formatTime(record.createdAt) }}</text>
						</view>
						<view class="detail-item" v-if="record.landlordConfirmTime">
							<text class="detail-label">房东确认</text>
							<text class="detail-value">{{ formatTime(record.landlordConfirmTime) }}</text>
						</view>
						<view class="detail-item" v-if="record.tenantConfirmTime">
							<text class="detail-label">租客确认</text>
							<text class="detail-value">{{ formatTime(record.tenantConfirmTime) }}</text>
						</view>
					</view>
					<view class="remark-row" v-if="record.remark">
						<text class="remark-label">备注：</text>
						<text class="remark-text">{{ record.remark }}</text>
					</view>
				</view>
				
				<view class="record-footer" v-if="record.status === 'pending' && !record.landlordConfirmed">
					<view class="confirm-btn" @click="confirmPayment(record)">确认收款</view>
				</view>
				<view class="record-footer confirmed" v-else-if="record.status === 'confirmed'">
					<text class="confirmed-text">✅ 双方已确认</text>
				</view>
				<view class="record-footer" v-else-if="record.status === 'pending' && record.landlordConfirmed">
					<text class="wait-text">⏳ 等待租客确认</text>
				</view>
			</view>
			
			<view class="empty" v-if="filteredRecords.length === 0">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无收款记录</text>
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
			records: []
		}
	},
	
	computed: {
		filteredRecords() {
			if (!this.activeStatus) return this.records
			return this.records.filter(r => r.status === this.activeStatus)
		},
		totalConfirmed() {
			return this.records
				.filter(r => r.status === 'confirmed')
				.reduce((sum, r) => sum + (Number(r.amount) || 0), 0)
		},
		confirmedCount() {
			return this.records.filter(r => r.status === 'confirmed').length
		},
		pendingCount() {
			return this.records.filter(r => r.status === 'pending').length
		}
	},
	
	onShow() {
		this.loadRecords()
	},
	
	methods: {
		async loadRecords() {
			try {
				const res = await api.payment.getLandlordRecords()
				if (res.code === 200) {
					this.records = res.data || []
				}
			} catch (e) {
				console.error('加载收款记录失败:', e)
			}
		},
		
		switchStatus(status) {
			this.activeStatus = status
		},
		
		loadMore() {
			// 分页加载
		},
		
		getStatusText(status) {
			const map = {
				'pending': '待确认',
				'confirmed': '已确认',
				'disputed': '有争议'
			}
			return map[status] || status
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
		
		formatTime(time) {
			if (!time) return '-'
			return time.replace('T', ' ').substring(0, 16)
		},
		
		formatMoney(value) {
			if (!value) return '0.00'
			return Number(value).toFixed(2)
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
								this.loadRecords()
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
		}
	}
}
</script>

<style scoped>
.payment-list-page {
	min-height: 100vh;
	background: #f5f7fa;
}

.filter-tabs {
	display: flex;
	background: #fff;
	padding: 20rpx;
	position: sticky;
	top: 0;
	z-index: 10;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 16rpx;
	font-size: 28rpx;
	color: #666;
	border-radius: 8rpx;
}

.tab-item.active {
	background: #409eff;
	color: #fff;
}

/* 统计概览 */
.stats-bar {
	display: flex;
	background: #fff;
	padding: 30rpx 20rpx;
	margin: 20rpx;
	border-radius: 16rpx;
}

.stat-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-value {
	font-size: 36rpx;
	font-weight: 600;
	color: #4caf50;
}

.stat-value.pending-color {
	color: #409eff;
}

.stat-label {
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
}

.stat-divider {
	width: 1rpx;
	background: #eee;
	margin: 10rpx 0;
}

.record-list {
	height: calc(100vh - 280rpx);
	padding: 0 20rpx 20rpx;
}

.record-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.record-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.tenant-info {
	display: flex;
	flex-direction: column;
}

.tenant-name {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.house-title {
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
}

.status-badge {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.status-badge.confirmed {
	background: #e8f5e9;
	color: #4caf50;
}

.status-badge.pending {
	background: #fff3e0;
	color: #ff9800;
}

.status-badge.disputed {
	background: #ffebee;
	color: #f44336;
}

.record-body {
	margin-bottom: 20rpx;
}

.amount-row {
	display: flex;
	align-items: baseline;
	gap: 16rpx;
	margin-bottom: 20rpx;
}

.amount-value {
	font-size: 40rpx;
	font-weight: 600;
	color: #f56c6c;
}

.amount-type {
	font-size: 24rpx;
	color: #999;
	background: #f5f7fa;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
}

.detail-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 16rpx;
}

.detail-item {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.detail-label {
	font-size: 24rpx;
	color: #999;
}

.detail-value {
	font-size: 26rpx;
	color: #333;
}

.remark-row {
	margin-top: 16rpx;
	padding-top: 16rpx;
	border-top: 1rpx dashed #eee;
	display: flex;
	font-size: 24rpx;
}

.remark-label {
	color: #999;
	flex-shrink: 0;
}

.remark-text {
	color: #666;
}

.record-footer {
	padding-top: 20rpx;
	border-top: 1rpx solid #f0f0f0;
	text-align: center;
}

.confirm-btn {
	display: inline-block;
	padding: 16rpx 60rpx;
	background: #4caf50;
	color: #fff;
	border-radius: 40rpx;
	font-size: 28rpx;
}

.wait-text {
	color: #ff9800;
	font-size: 26rpx;
}

.record-footer.confirmed {
	background: #e8f5e9;
	margin: 0 -30rpx -30rpx;
	padding: 20rpx;
	border-radius: 0 0 16rpx 16rpx;
	border-top: none;
}

.confirmed-text {
	color: #4caf50;
	font-size: 26rpx;
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
	font-size: 28rpx;
	color: #999;
}
</style>

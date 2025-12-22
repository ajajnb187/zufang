<template>
	<view class="my-rental-page">
		<!-- 状态筛选 -->
		<scroll-view class="status-tabs" scroll-x>
			<view class="tab-item" :class="{ active: activeStatus === '' }" @click="switchStatus('')">
				全部
			</view>
			<view class="tab-item" :class="{ active: activeStatus === item.value }" 
				v-for="item in statusList" :key="item.value" @click="switchStatus(item.value)">
				{{ item.label }}
			</view>
		</scroll-view>
		
		<!-- 交易列表 -->
		<scroll-view class="transaction-list" scroll-y @scrolltolower="loadMore" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
			<view class="transaction-card" v-for="item in transactionList" :key="item.transactionId" @click="viewDetail(item)">
				<view class="card-header">
					<view class="house-title">{{ item.houseTitle || '房源信息' }}</view>
					<view class="status-badge" :class="getStatusClass(item)">
						{{ getStatusText(item.status, item) }}
					</view>
				</view>
				
				<view class="card-body">
					<view class="info-row">
						<text class="label">小区</text>
						<text class="value">{{ item.communityName }}</text>
					</view>
					<view class="info-row">
						<text class="label">{{ isLandlord(item) ? '租客' : '房东' }}</text>
						<text class="value">{{ isLandlord(item) ? item.tenantName : item.landlordName }}</text>
					</view>
					<view class="info-row">
						<text class="label">合同状态</text>
						<text class="value" :class="'contract-' + item.contractStatus">{{ getContractStatusText(item.contractStatus) }}</text>
					</view>
					<view class="info-row" v-if="item.checkinDate">
						<text class="label">入住日期</text>
						<text class="value">{{ item.checkinDate }}</text>
					</view>
					<view class="info-row" v-if="item.rentPrice">
						<text class="label">月租</text>
						<text class="value rent-price">¥{{ item.rentPrice }}/月</text>
					</view>
				</view>
				
				<!-- 操作按钮 -->
				<view class="card-actions" v-if="showActions(item)">
					<!-- 待入住确认 -->
					<template v-if="item.status === 'signed' || item.status === 'pending_checkin'">
						<!-- 合同生效才能确认入住 -->
						<template v-if="canConfirmCheckin(item)">
							<view class="action-btn primary" @click.stop="confirmCheckin(item)" 
								v-if="!hasConfirmedCheckin(item)">
								确认入住
							</view>
							<view class="action-btn disabled" v-else>
								已确认，等待对方
							</view>
						</template>
						<!-- 合同未生效提示 -->
						<view class="action-btn disabled" v-else @click.stop="showContractTip(item)">
							{{ getContractTip(item) }}
						</view>
					</template>
					
					<!-- 在租中 - 可确认完成 -->
					<template v-if="item.status === 'living' || item.status === 'pending_complete'">
						<view class="action-btn secondary" @click.stop="confirmComplete(item)"
							v-if="!hasConfirmedComplete(item)">
							确认完成
						</view>
						<view class="action-btn disabled" v-else>
							已确认，等待对方
						</view>
					</template>
					
					<!-- 已完成 - 可评价 -->
					<template v-if="item.status === 'completed'">
						<view class="action-btn primary" @click.stop="goEvaluate(item)"
							v-if="!hasEvaluated(item)">
							去评价
						</view>
						<view class="action-btn disabled" v-else>
							已评价
						</view>
					</template>
					
					<!-- 查看合同 -->
					<view class="action-btn secondary" @click.stop="viewContract(item)">
						查看合同
					</view>
				</view>
			</view>
			
			<view class="empty" v-if="transactionList.length === 0 && !loading">
				<text class="empty-icon">🏠</text>
				<text class="empty-text">暂无租赁记录</text>
				<text class="empty-tip">完成合同签署后，租赁记录将显示在这里</text>
			</view>
			
			<view class="loading-more" v-if="loading">
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
			activeStatus: '',
			statusList: [
				{ label: '待入住', value: 'signed' },
				{ label: '确认中', value: 'pending_checkin' },
				{ label: '在租中', value: 'living' },
				{ label: '已完成', value: 'completed' },
				{ label: '已取消', value: 'cancelled' }
			],
			transactionList: [],
			loading: false,
			refreshing: false,
			currentUserId: null
		}
	},
	
	onLoad() {
		this.currentUserId = uni.getStorageSync('userId')
		this.loadTransactions()
	},
	
	onShow() {
		// 每次显示页面时刷新数据
		this.loadTransactions()
	},
	
	onPullDownRefresh() {
		this.loadTransactions().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadTransactions() {
			this.loading = true
			try {
				const res = await api.transaction.getMyList({ status: this.activeStatus })
				if (res.code === 200) {
					this.transactionList = res.data || []
				}
			} catch (e) {
				console.error('加载交易列表失败:', e)
			} finally {
				this.loading = false
				this.refreshing = false
			}
		},
		
		onRefresh() {
			this.refreshing = true
			this.loadTransactions()
		},
		
		loadMore() {
			// 分页加载更多
		},
		
		switchStatus(status) {
			this.activeStatus = status
			this.loadTransactions()
		},
		
		getStatusText(status, item) {
			// 如果合同已终止或到期，显示对应状态
			if (item && (item.contractStatus === 'terminated' || item.contractStatus === 'expired')) {
				return item.contractStatus === 'terminated' ? '合同已终止' : '合同已到期'
			}
			const map = {
				'pending_sign': '待签署',
				'signed': '待入住',
				'pending_checkin': '入住确认中',
				'living': '在租中',
				'pending_complete': '完成确认中',
				'completed': '已完成',
				'evaluated': '已评价',
				'cancelled': '已取消'
			}
			return map[status] || status
		},
		
		getContractStatusText(status) {
			const map = {
				'draft': '草稿',
				'signed': '待审核',
				'effective': '生效中',
				'terminated': '已终止',
				'expired': '已到期'
			}
			return map[status] || status || '-'
		},
		
		getStatusClass(item) {
			// 如果合同已终止或到期，使用特殊样式
			if (item.contractStatus === 'terminated') {
				return 'terminated'
			}
			if (item.contractStatus === 'expired') {
				return 'expired'
			}
			return item.status
		},
		
		canConfirmCheckin(item) {
			// 只有合同生效后才能确认入住
			return item.contractStatus === 'effective'
		},
		
		getContractTip(item) {
			const status = item.contractStatus
			if (status === 'draft') return '合同未签署'
			if (status === 'signed') return '等待合同审核'
			if (status === 'terminated') return '合同已终止'
			if (status === 'expired') return '合同已到期'
			return '合同未生效'
		},
		
		showContractTip(item) {
			const status = item.contractStatus
			let message = ''
			if (status === 'draft') {
				message = '合同尚未签署，请先完成合同签署'
			} else if (status === 'signed') {
				message = '合同正在等待管理员审核，审核通过后即可确认入住'
			} else if (status === 'terminated') {
				message = '合同已终止，无法进行入住确认'
			} else if (status === 'expired') {
				message = '合同已到期，无法进行入住确认'
			} else {
				message = '合同尚未生效，请等待合同生效后再确认入住'
			}
			uni.showToast({ title: message, icon: 'none', duration: 2000 })
		},
		
		isLandlord(item) {
			return this.currentUserId == item.landlordId
		},
		
		showActions(item) {
			// 合同已终止或到期时不显示操作按钮
			if (item.contractStatus === 'terminated' || item.contractStatus === 'expired') {
				return false
			}
			return ['signed', 'pending_checkin', 'living', 'pending_complete', 'completed'].includes(item.status)
		},
		
		hasConfirmedCheckin(item) {
			if (this.isLandlord(item)) {
				return item.landlordCheckinConfirm
			}
			return item.tenantCheckinConfirm
		},
		
		hasConfirmedComplete(item) {
			if (this.isLandlord(item)) {
				return item.landlordCompleteConfirm
			}
			return item.tenantCompleteConfirm
		},
		
		hasEvaluated(item) {
			if (this.isLandlord(item)) {
				return item.landlordEvaluated
			}
			return item.tenantEvaluated
		},
		
		viewDetail(item) {
			uni.navigateTo({
				url: `/pages/rental/transaction-detail?id=${item.transactionId}`
			})
		},
		
		viewContract(item) {
			uni.navigateTo({
				url: `/pages/contract/detail/detail?id=${item.contractId}`
			})
		},
		
		confirmCheckin(item) {
			uni.showModal({
				title: '确认入住',
				content: '请确认已完成房屋交接，确认后将进入在租状态',
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '确认中...' })
							const result = await api.transaction.confirmCheckin(item.transactionId, {
								checkinDate: new Date().toISOString().split('T')[0]
							})
							uni.hideLoading()
							if (result.code === 200) {
								uni.showToast({ title: '确认成功', icon: 'success' })
								this.loadTransactions()
							} else {
								uni.showToast({ title: result.message || '确认失败', icon: 'none' })
							}
						} catch (e) {
							uni.hideLoading()
							console.error('确认入住失败:', e)
						}
					}
				}
			})
		},
		
		confirmComplete(item) {
			uni.showModal({
				title: '确认交易完成',
				content: '请确认租赁交易已结束，房屋已完成交接。确认后双方可进行互评。',
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '确认中...' })
							const result = await api.transaction.confirmComplete(item.transactionId, {
								checkoutDate: new Date().toISOString().split('T')[0]
							})
							uni.hideLoading()
							if (result.code === 200) {
								uni.showToast({ title: '确认成功', icon: 'success' })
								this.loadTransactions()
							} else {
								uni.showToast({ title: result.message || '确认失败', icon: 'none' })
							}
						} catch (e) {
							uni.hideLoading()
							console.error('确认完成失败:', e)
						}
					}
				}
			})
		},
		
		goEvaluate(item) {
			const targetUserId = this.isLandlord(item) ? item.tenantId : item.landlordId
			uni.navigateTo({
				url: `/pages/evaluation/evaluation?targetUserId=${targetUserId}&contractId=${item.contractId}`
			})
		}
	}
}
</script>

<style scoped>
.my-rental-page {
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

.transaction-list {
	height: calc(100vh - 100rpx);
	padding: 20rpx;
}

.transaction-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.house-title {
	flex: 1;
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.status-badge {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	margin-left: 20rpx;
}

.status-badge.signed,
.status-badge.pending_checkin {
	background: #fff3e0;
	color: #ff9800;
}

.status-badge.living {
	background: #e8f5e9;
	color: #4caf50;
}

.status-badge.pending_complete {
	background: #e3f2fd;
	color: #2196f3;
}

.status-badge.completed {
	background: #f3e5f5;
	color: #9c27b0;
}

.status-badge.evaluated {
	background: #fafafa;
	color: #999;
}

.status-badge.cancelled {
	background: #ffebee;
	color: #f44336;
}

.status-badge.terminated {
	background: #ffebee;
	color: #f44336;
}

.status-badge.expired {
	background: #fafafa;
	color: #909399;
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
}

.info-row .value.rent-price {
	color: #f56c6c;
	font-weight: 600;
}

.info-row .value.contract-effective {
	color: #67c23a;
}

.info-row .value.contract-signed {
	color: #e6a23c;
}

.info-row .value.contract-draft {
	color: #909399;
}

.info-row .value.contract-terminated {
	color: #f56c6c;
}

.info-row .value.contract-expired {
	color: #909399;
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

.action-btn.disabled {
	background: #f5f7fa;
	color: #ccc;
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
	color: #333;
	margin-bottom: 12rpx;
}

.empty-tip {
	font-size: 26rpx;
	color: #999;
}

.loading-more {
	text-align: center;
	padding: 30rpx;
	color: #999;
	font-size: 26rpx;
}
</style>

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
	background: #F7F9FC;
}

.status-tabs {
	display: flex;
	background: #FFFFFF;
	padding: 24rpx 30rpx;
	white-space: nowrap;
	position: sticky;
	top: 0;
	z-index: 10;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.tab-item {
	display: inline-block;
	padding: 16rpx 36rpx;
	margin-right: 24rpx;
	background: #F7F9FC;
	border-radius: 48rpx;
	font-size: 28rpx;
	color: #5A6C7D;
	font-weight: 500;
	transition: all 0.3s ease;
	border: 2rpx solid transparent;
}

.tab-item.active {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
	transform: translateY(-2rpx);
}

.transaction-list {
	height: calc(100vh - 120rpx);
	padding: 24rpx 30rpx;
}

.transaction-card {
	background: #FFFFFF;
	border-radius: 24rpx;
	padding: 32rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.transaction-card::before {
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

.transaction-card:active {
	transform: translateY(-4rpx);
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.transaction-card:active::before {
	opacity: 1;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
	padding-bottom: 24rpx;
	border-bottom: 1rpx solid #F7F9FC;
}

.house-title {
	flex: 1;
	font-size: 34rpx;
	font-weight: 700;
	color: #2C3E50;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.status-badge {
	padding: 10rpx 24rpx;
	border-radius: 24rpx;
	font-size: 24rpx;
	margin-left: 20rpx;
	font-weight: 600;
}

.status-badge.signed,
.status-badge.pending_checkin {
	background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
	color: #E65100;
}

.status-badge.living {
	background: linear-gradient(135deg, #E8F5E9, #C8E6C9);
	color: #2E7D32;
}

.status-badge.pending_complete {
	background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
	color: #1565C0;
}

.status-badge.completed {
	background: linear-gradient(135deg, #F3E5F5, #E1BEE7);
	color: #6A1B9A;
}

.status-badge.evaluated {
	background: #F7F9FC;
	color: #8B95A5;
}

.status-badge.cancelled {
	background: linear-gradient(135deg, #FFEBEE, #FFCDD2);
	color: #C62828;
}

.status-badge.terminated {
	background: linear-gradient(135deg, #FFEBEE, #FFCDD2);
	color: #C62828;
}

.status-badge.expired {
	background: #F7F9FC;
	color: #8B95A5;
}

.card-body {
	margin-bottom: 20rpx;
}

.info-row {
	display: flex;
	justify-content: space-between;
	padding: 16rpx 0;
	font-size: 28rpx;
}

.info-row .label {
	color: #8B95A5;
	font-weight: 500;
}

.info-row .value {
	color: #2C3E50;
	font-weight: 600;
}

.info-row .value.rent-price {
	color: #FF6B35;
	font-weight: 700;
	font-size: 32rpx;
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
	gap: 24rpx;
	padding-top: 24rpx;
	border-top: 1rpx solid #F7F9FC;
}

.action-btn {
	flex: 1;
	text-align: center;
	padding: 24rpx;
	border-radius: 48rpx;
	font-size: 28rpx;
	font-weight: 600;
	transition: all 0.3s ease;
}

.action-btn:active {
	transform: scale(0.95);
}

.action-btn.primary {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}

.action-btn.secondary {
	background: #F7F9FC;
	color: #5A6C7D;
	border: 2rpx solid #E4E7ED;
}

.action-btn.disabled {
	background: #F7F9FC;
	color: #D5DBDB;
	border: 2rpx solid #EBEEF5;
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
	margin-bottom: 16rpx;
}

.empty-tip {
	font-size: 26rpx;
	color: #8B95A5;
	text-align: center;
	line-height: 1.6;
}

.loading-more {
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

<template>
	<view class="transaction-detail-page">
		<!-- 交易状态卡片 -->
		<view class="status-card" :class="transaction.status">
			<view class="status-icon">{{ getStatusIcon(transaction.status) }}</view>
			<view class="status-text">{{ getStatusText(transaction.status) }}</view>
			<view class="status-desc">{{ getStatusDesc(transaction.status) }}</view>
		</view>
		
		<!-- 合同信息 -->
		<view class="section-card">
			<view class="section-title">合同信息</view>
			<view class="info-row">
				<text class="label">合同编号</text>
				<text class="value">{{ transaction.contractNo || '-' }}</text>
			</view>
			<view class="info-row">
				<text class="label">合同状态</text>
				<text class="value" :class="'contract-' + transaction.contractStatus">
					{{ getContractStatusText(transaction.contractStatus) }}
				</text>
			</view>
			<view class="info-row">
				<text class="label">租赁周期</text>
				<text class="value">{{ transaction.contractStartDate }} 至 {{ transaction.contractEndDate }}</text>
			</view>
			<view class="info-row">
				<text class="label">月租金</text>
				<text class="value rent-price">¥{{ transaction.rentPrice || 0 }}/月</text>
			</view>
		</view>
		
		<!-- 房源信息 -->
		<view class="section-card">
			<view class="section-title">房源信息</view>
			<view class="house-info" @click="viewHouse">
				<view class="house-title">{{ transaction.houseTitle }}</view>
				<view class="house-community">{{ transaction.communityName }}</view>
			</view>
		</view>
		
		<!-- 交易双方 -->
		<view class="section-card">
			<view class="section-title">交易双方</view>
			<view class="parties">
				<view class="party-item">
					<view class="party-label">房东</view>
					<view class="party-info">
						<view class="party-name">{{ transaction.landlordName || '-' }}</view>
						<view class="party-phone" v-if="transaction.landlordPhone" @click.stop="callPhone(transaction.landlordPhone)">
							📞 {{ transaction.landlordPhone }}
						</view>
					</view>
					<text class="arrow" @click="viewUserProfile(transaction.landlordId)">›</text>
				</view>
				<view class="party-item">
					<view class="party-label">租客</view>
					<view class="party-info">
						<view class="party-name">{{ transaction.tenantName || '-' }}</view>
						<view class="party-phone" v-if="transaction.tenantPhone" @click.stop="callPhone(transaction.tenantPhone)">
							📞 {{ transaction.tenantPhone }}
						</view>
					</view>
					<text class="arrow" @click="viewUserProfile(transaction.tenantId)">›</text>
				</view>
			</view>
		</view>
		
		<!-- 入住信息 -->
		<view class="section-card" v-if="transaction.checkinDate">
			<view class="section-title">入住信息</view>
			<view class="info-row">
				<text class="label">入住日期</text>
				<text class="value">{{ transaction.checkinDate }}</text>
			</view>
			<view class="info-row" v-if="transaction.checkinRemark">
				<text class="label">交接备注</text>
				<text class="value">{{ transaction.checkinRemark }}</text>
			</view>
		</view>
		
		<!-- 确认状态 -->
		<view class="section-card" v-if="showConfirmStatus">
			<view class="section-title">确认状态</view>
			<view class="confirm-status">
				<!-- 入住确认 -->
				<template v-if="transaction.status === 'signed' || transaction.status === 'pending_checkin'">
					<view class="confirm-item">
						<text class="confirm-label">房东确认入住</text>
						<text class="confirm-value" :class="{ confirmed: transaction.landlordCheckinConfirm }">
							{{ transaction.landlordCheckinConfirm ? '已确认' : '待确认' }}
						</text>
					</view>
					<view class="confirm-item">
						<text class="confirm-label">租客确认入住</text>
						<text class="confirm-value" :class="{ confirmed: transaction.tenantCheckinConfirm }">
							{{ transaction.tenantCheckinConfirm ? '已确认' : '待确认' }}
						</text>
					</view>
				</template>
				
				<!-- 完成确认 -->
				<template v-if="transaction.status === 'living' || transaction.status === 'pending_complete'">
					<view class="confirm-item">
						<text class="confirm-label">房东确认完成</text>
						<text class="confirm-value" :class="{ confirmed: transaction.landlordCompleteConfirm }">
							{{ transaction.landlordCompleteConfirm ? '已确认' : '待确认' }}
						</text>
					</view>
					<view class="confirm-item">
						<text class="confirm-label">租客确认完成</text>
						<text class="confirm-value" :class="{ confirmed: transaction.tenantCompleteConfirm }">
							{{ transaction.tenantCompleteConfirm ? '已确认' : '待确认' }}
						</text>
					</view>
				</template>
			</view>
		</view>
		
		<!-- 操作按钮 -->
		<view class="action-area" v-if="showActions">
			<!-- 查看合同 -->
			<view class="action-btn secondary" @click="viewContract">查看合同</view>
			
			<!-- 入住确认 - 需要合同生效 -->
			<template v-if="needCheckinConfirm">
				<template v-if="contractIsEffective">
					<view class="action-btn primary" @click="confirmCheckin" v-if="canConfirmCheckin">
						确认入住
					</view>
					<view class="action-btn disabled" v-else>
						已确认，等待对方
					</view>
				</template>
				<view class="action-btn disabled" v-else @click="showContractTip">
					{{ contractTipText }}
				</view>
			</template>
			
			<!-- 完成确认 -->
			<template v-if="canConfirmComplete">
				<view class="action-btn primary" @click="confirmComplete">确认交易完成</view>
			</template>
			
			<!-- 去评价 -->
			<template v-if="canEvaluate">
				<view class="action-btn primary" @click="goEvaluate">去评价</view>
			</template>
			
			<!-- 联系对方 -->
			<view class="action-btn secondary" @click="contactOther">联系{{ isLandlord ? '租客' : '房东' }}</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			transactionId: '',
			transaction: {},
			currentUserId: null
		}
	},
	
	computed: {
		isLandlord() {
			return this.currentUserId == this.transaction.landlordId
		},
		
		showConfirmStatus() {
			return ['signed', 'pending_checkin', 'living', 'pending_complete'].includes(this.transaction.status)
		},
		
		showActions() {
			return !['cancelled', 'evaluated'].includes(this.transaction.status)
		},
		
		// 是否需要入住确认（状态为signed或pending_checkin）
		needCheckinConfirm() {
			return this.transaction.status === 'signed' || this.transaction.status === 'pending_checkin'
		},
		
		// 合同是否生效
		contractIsEffective() {
			return this.transaction.contractStatus === 'effective'
		},
		
		// 合同提示文字
		contractTipText() {
			const status = this.transaction.contractStatus
			if (status === 'draft') return '合同未签署'
			if (status === 'signed') return '等待合同审核'
			if (status === 'terminated') return '合同已终止'
			if (status === 'expired') return '合同已到期'
			return '合同未生效'
		},
		
		canConfirmCheckin() {
			if (!this.needCheckinConfirm || !this.contractIsEffective) {
				return false
			}
			if (this.isLandlord) {
				return !this.transaction.landlordCheckinConfirm
			}
			return !this.transaction.tenantCheckinConfirm
		},
		
		canConfirmComplete() {
			if (this.transaction.status !== 'living' && this.transaction.status !== 'pending_complete') {
				return false
			}
			if (this.isLandlord) {
				return !this.transaction.landlordCompleteConfirm
			}
			return !this.transaction.tenantCompleteConfirm
		},
		
		canEvaluate() {
			if (this.transaction.status !== 'completed') {
				return false
			}
			if (this.isLandlord) {
				return !this.transaction.landlordEvaluated
			}
			return !this.transaction.tenantEvaluated
		}
	},
	
	onLoad(options) {
		this.transactionId = options.id
		this.currentUserId = uni.getStorageSync('userId')
		this.loadDetail()
	},
	
	methods: {
		async loadDetail() {
			try {
				uni.showLoading({ title: '加载中...' })
				const res = await api.transaction.getDetail(this.transactionId)
				uni.hideLoading()
				if (res.code === 200) {
					this.transaction = res.data
				}
			} catch (e) {
				uni.hideLoading()
				console.error('加载交易详情失败:', e)
			}
		},
		
		getStatusIcon(status) {
			const map = {
				'pending_sign': '📝',
				'signed': '✅',
				'pending_checkin': '🏠',
				'living': '🏡',
				'pending_complete': '📋',
				'completed': '🎉',
				'evaluated': '⭐',
				'cancelled': '❌'
			}
			return map[status] || '📄'
		},
		
		getStatusText(status) {
			const map = {
				'pending_sign': '待签署',
				'signed': '已签署，待入住',
				'pending_checkin': '入住确认中',
				'living': '在租中',
				'pending_complete': '完成确认中',
				'completed': '交易已完成',
				'evaluated': '已完成评价',
				'cancelled': '交易已取消'
			}
			return map[status] || status
		},
		
		getStatusDesc(status) {
			const map = {
				'pending_sign': '等待双方签署合同',
				'signed': '合同已签署，请确认入住',
				'pending_checkin': '等待双方确认入住',
				'living': '租赁进行中，请遵守合同约定',
				'pending_complete': '等待双方确认交易完成',
				'completed': '交易已完成，可进行评价',
				'evaluated': '感谢您的评价',
				'cancelled': '交易已取消'
			}
			return map[status] || ''
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
		
		showContractTip() {
			const status = this.transaction.contractStatus
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
		
		callPhone(phone) {
			if (phone) {
				uni.makePhoneCall({ phoneNumber: phone })
			}
		},
		
		viewHouse() {
			uni.navigateTo({
				url: `/pages/house/detail/detail?id=${this.transaction.houseId}`
			})
		},
		
		viewUserProfile(userId) {
			uni.navigateTo({
				url: `/pages/user/user-profile?id=${userId}`
			})
		},
		
		viewContract() {
			uni.navigateTo({
				url: `/pages/contract/detail/detail?id=${this.transaction.contractId}`
			})
		},
		
		confirmCheckin() {
			uni.showModal({
				title: '确认入住',
				content: '请确认已完成房屋交接，确认后将进入在租状态',
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '确认中...' })
							const result = await api.transaction.confirmCheckin(this.transactionId, {
								checkinDate: new Date().toISOString().split('T')[0]
							})
							uni.hideLoading()
							if (result.code === 200) {
								uni.showToast({ title: '确认成功', icon: 'success' })
								this.loadDetail()
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
		
		confirmComplete() {
			uni.showModal({
				title: '确认交易完成',
				content: '请确认租赁交易已结束，房屋已完成交接。确认后双方可进行互评。',
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '确认中...' })
							const result = await api.transaction.confirmComplete(this.transactionId, {
								checkoutDate: new Date().toISOString().split('T')[0]
							})
							uni.hideLoading()
							if (result.code === 200) {
								uni.showToast({ title: '确认成功', icon: 'success' })
								this.loadDetail()
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
		
		goEvaluate() {
			const targetUserId = this.isLandlord ? this.transaction.tenantId : this.transaction.landlordId
			uni.navigateTo({
				url: `/pages/evaluation/evaluation?targetUserId=${targetUserId}&contractId=${this.transaction.contractId}`
			})
		},
		
		contactOther() {
			const targetUserId = this.isLandlord ? this.transaction.tenantId : this.transaction.landlordId
			uni.navigateTo({
				url: `/pages/chat/chat-detail/chat-detail?userId=${targetUserId}&houseId=${this.transaction.houseId}`
			})
		}
	}
}
</script>

<style scoped>
.transaction-detail-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 200rpx;
}

.status-card {
	background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
	padding: 60rpx 40rpx;
	text-align: center;
	color: #fff;
}

.status-card.living {
	background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.status-card.completed,
.status-card.evaluated {
	background: linear-gradient(135deg, #9c27b0 0%, #ba68c8 100%);
}

.status-card.cancelled {
	background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
}

.status-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.status-text {
	font-size: 36rpx;
	font-weight: 600;
	margin-bottom: 12rpx;
}

.status-desc {
	font-size: 26rpx;
	opacity: 0.9;
}

.section-card {
	background: #fff;
	margin: 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.section-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 24rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.house-info {
	padding: 20rpx 0;
}

.house-title {
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
	margin-bottom: 12rpx;
}

.house-community {
	font-size: 26rpx;
	color: #999;
}

.parties {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.party-item {
	display: flex;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}

.party-item:last-child {
	border-bottom: none;
}

.party-label {
	width: 100rpx;
	font-size: 28rpx;
	color: #999;
}

.party-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.party-name {
	font-size: 28rpx;
	color: #333;
	margin-bottom: 8rpx;
}

.party-phone {
	font-size: 24rpx;
	color: #409eff;
}

.arrow {
	color: #ccc;
	font-size: 32rpx;
	padding: 10rpx;
}

.info-row {
	display: flex;
	justify-content: space-between;
	padding: 16rpx 0;
}

.info-row .label {
	font-size: 28rpx;
	color: #999;
}

.info-row .value {
	font-size: 28rpx;
	color: #333;
}

.info-row .value.rent-price {
	color: #f56c6c;
	font-weight: 600;
}

.info-row .value.contract-effective {
	color: #67c23a;
	font-weight: 500;
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

.confirm-status {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.confirm-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16rpx 0;
}

.confirm-label {
	font-size: 28rpx;
	color: #666;
}

.confirm-value {
	font-size: 28rpx;
	color: #ff9800;
}

.confirm-value.confirmed {
	color: #67c23a;
}

.action-area {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	padding: 20rpx 30rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	display: flex;
	gap: 20rpx;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.action-btn {
	flex: 1;
	text-align: center;
	padding: 28rpx;
	border-radius: 50rpx;
	font-size: 28rpx;
	font-weight: 500;
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
	color: #c0c4cc;
}
</style>

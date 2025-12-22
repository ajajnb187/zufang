<template>
	<view class="contract-page">
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
		
		<!-- 合同列表 -->
		<scroll-view class="contract-list" scroll-y>
			<view class="contract-card" v-for="contract in contractList" :key="contract.contractId" 
				@click="viewDetail(contract.contractId)">
				<view class="contract-header">
					<view class="contract-title">{{ contract.houseTitle }}</view>
					<view class="status-badge" :class="contract.status">
						{{ getStatusText(contract.status) }}
					</view>
				</view>
				
				<view class="contract-info">
					<view class="info-item">
						<text class="label">合同编号</text>
						<text class="value">{{ contract.contractNo }}</text>
					</view>
					<view class="info-item">
						<text class="label">租期</text>
						<text class="value">{{ contract.startDate }} 至 {{ contract.endDate }}</text>
					</view>
					<view class="info-item">
						<text class="label">月租金</text>
						<text class="value price">¥{{ contract.monthlyRent }}</text>
					</view>
					<view class="info-item" v-if="contract.otherParty">
						<text class="label">对方</text>
						<text class="value">{{ contract.otherParty }}</text>
					</view>
				</view>
				
				<view class="contract-actions" v-if="contract.needSign || contract.canReject">
					<view class="action-btn secondary" v-if="contract.canReject" @click.stop="rejectContract(contract.contractId)">拒绝</view>
					<view class="action-btn primary" v-if="contract.needSign" @click.stop="signContract(contract.contractId)">去签署</view>
				</view>
			</view>
			
			<view class="empty" v-if="contractList.length === 0">
				<text class="empty-icon">📄</text>
				<text class="empty-text">暂无合同</text>
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
			currentUserId: null,
			userType: null,
			statusList: [
				{ label: '待签署', value: 'draft' },
				{ label: '审核中', value: 'signed' },
				{ label: '生效中', value: 'effective' },
				{ label: '已终止', value: 'terminated' },
				{ label: '已过期', value: 'expired' }
			],
			contractList: []
		}
	},
	
	onLoad() {
		this.getCurrentUser()
	},
	
	onShow() {
		this.loadContracts()
	},
	
	onPullDownRefresh() {
		this.loadContracts().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		getCurrentUser() {
			const userInfo = uni.getStorageSync('userInfo')
			if (userInfo) {
				this.currentUserId = userInfo.userId
				this.userType = userInfo.userType
			}
		},
		
		async loadContracts() {
			try {
				const res = await api.contract.getList({
					status: this.activeStatus,
					pageNum: 1,
					pageSize: 100
				})
				if (res.code === 200) {
					const records = res.data.records || res.data || []
					this.contractList = records.map(item => this.formatContract(item))
				}
			} catch (e) {
				console.error('加载合同列表失败:', e)
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		
		formatContract(item) {
			// 判断当前用户角色
			const isLandlord = this.currentUserId === item.landlordId
			const isTenant = this.currentUserId === item.tenantId
			
			// 计算显示状态
			let displayStatus = item.contractStatus
			let needSign = false
			
			if (item.contractStatus === 'draft') {
				if (isLandlord && !item.landlordSignature) {
					displayStatus = 'pending_my_sign'
					needSign = true
				} else if (isTenant && !item.tenantSignature) {
					displayStatus = 'pending_my_sign'
					needSign = true
				} else if (isLandlord && item.landlordSignature && !item.tenantSignature) {
					displayStatus = 'pending_other_sign'
				} else if (isTenant && item.tenantSignature && !item.landlordSignature) {
					displayStatus = 'pending_other_sign'
				}
			}
			
			// 对方信息
			const otherParty = isLandlord ? item.tenantName : item.landlordName
			
			return {
				contractId: item.contractId,
				contractNo: item.contractNo,
				houseTitle: item.houseTitle || '未知房源',
				startDate: item.startDate,
				endDate: item.endDate,
				monthlyRent: item.rentPrice,
				status: displayStatus,
				otherParty: otherParty,
				needSign: needSign,
				canReject: isTenant && item.contractStatus === 'draft' && !item.tenantSignature
			}
		},
		
		switchStatus(status) {
			this.activeStatus = status
			this.loadContracts()
		},
		
		getStatusText(status) {
			const statusMap = {
				'draft': '待签署',
				'pending_my_sign': '待我签署',
				'pending_other_sign': '等待对方',
				'signed': '审核中',
				'effective': '生效中',
				'terminated': '已终止',
				'expired': '已过期'
			}
			return statusMap[status] || status
		},
		
		viewDetail(contractId) {
			uni.navigateTo({
				url: `/pages/contract/detail/detail?id=${contractId}`
			})
		},
		
		signContract(contractId) {
			// 直接跳转到详情页签署
			uni.navigateTo({
				url: `/pages/contract/detail/detail?id=${contractId}`
			})
		},
		
		rejectContract(contractId) {
			uni.showModal({
				title: '拒绝合同',
				content: '确定要拒绝此合同吗？拒绝后合同将作废。',
				success: async (res) => {
					if (res.confirm) {
						try {
							await api.contract.reject(contractId)
							uni.showToast({ title: '已拒绝', icon: 'success' })
							this.loadContracts()
						} catch (e) {
							console.error('拒绝合同失败:', e)
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
.contract-page {
	min-height: 100vh;
	background: #f5f7fa;
}

.status-tabs {
	display: flex;
	background: #fff;
	padding: 20rpx;
	white-space: nowrap;
}

.tab-item {
	display: inline-block;
	padding: 12rpx 32rpx;
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

.contract-list {
	height: calc(100vh - 120rpx);
	padding: 20rpx;
}

.contract-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.contract-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.contract-title {
	flex: 1;
	font-size: 32rpx;
	font-weight: 600;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.status-badge {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.status-badge.draft,
.status-badge.pending,
.status-badge.pending_my_sign {
	background: #fff3e0;
	color: #ff9800;
}

.status-badge.pending_other_sign {
	background: #e8eaf6;
	color: #5c6bc0;
}

.status-badge.signed {
	background: #e3f2fd;
	color: #2196f3;
}

.status-badge.active,
.status-badge.effective {
	background: #e8f5e9;
	color: #4caf50;
}

.status-badge.expired {
	background: #fafafa;
	color: #999;
}

.status-badge.terminated {
	background: #ffebee;
	color: #f44336;
}

.contract-info {
	border-top: 1rpx solid #f0f0f0;
	padding-top: 20rpx;
}

.info-item {
	display: flex;
	justify-content: space-between;
	padding: 12rpx 0;
	font-size: 28rpx;
}

.info-item .label {
	color: #999;
}

.info-item .value {
	color: #333;
}

.info-item .value.price {
	color: #ff6b6b;
	font-weight: 600;
}

.contract-actions {
	display: flex;
	gap: 20rpx;
	margin-top: 20rpx;
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

.action-btn.secondary {
	background: #f5f7fa;
	color: #666;
}

.action-btn.primary {
	background: #409eff;
	color: #fff;
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

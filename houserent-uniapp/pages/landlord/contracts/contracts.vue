<template>
	<view class="contracts-page">
		<!-- 合同统计 -->
		<view class="stats-header">
			<view class="stat-item">
				<text class="stat-num">{{ stats.total }}</text>
				<text class="stat-label">合同总数</text>
			</view>
			<view class="stat-item">
				<text class="stat-num">{{ stats.draft }}</text>
				<text class="stat-label">待签署</text>
			</view>
			<view class="stat-item">
				<text class="stat-num">{{ stats.active }}</text>
				<text class="stat-label">生效中</text>
			</view>
		</view>
		
		<!-- 筛选标签 -->
		<scroll-view class="filter-tabs" scroll-x enable-flex>
			<view class="tab-item" :class="{ active: activeTab === 0 }" @click="switchTab(0)">
				全部
			</view>
			<view class="tab-item" :class="{ active: activeTab === 1 }" @click="switchTab(1)">
				待签署
			</view>
			<view class="tab-item" :class="{ active: activeTab === 2 }" @click="switchTab(2)">
				审核中
			</view>
			<view class="tab-item" :class="{ active: activeTab === 3 }" @click="switchTab(3)">
				生效中
			</view>
			<view class="tab-item" :class="{ active: activeTab === 4 }" @click="switchTab(4)">
				已终止
			</view>
		</scroll-view>
		
		<!-- 合同列表 -->
		<scroll-view class="contract-list" scroll-y enable-flex @scrolltolower="loadMore">
			<view class="contract-card" v-for="contract in contractList" :key="contract.contractId" @click="viewContract(contract.contractId)">
				<view class="contract-header">
					<view class="contract-info">
						<view class="contract-title">{{ contract.houseTitle }}</view>
						<view class="contract-parties">
							<text class="party">房东：{{ contract.landlordName }}</text>
							<text class="party">租客：{{ contract.tenantName }}</text>
						</view>
					</view>
					<view class="status-badge" :class="contract.status">
						{{ getStatusText(contract.status) }}
					</view>
				</view>
				
				<view class="contract-details">
					<view class="detail-row">
						<text class="label">租期</text>
						<text class="value">{{ contract.startDate }} 至 {{ contract.endDate }}</text>
					</view>
					<view class="detail-row">
						<text class="label">月租金</text>
						<text class="value price">¥{{ contract.monthlyRent }}</text>
					</view>
					<view class="detail-row">
						<text class="label">创建时间</text>
						<text class="value">{{ contract.createTime }}</text>
					</view>
				</view>
				
				<view class="contract-actions">
					<view class="action-btn" @click.stop="viewContract(contract.contractId)">
						<text>📄 查看</text>
					</view>
					<view class="action-btn" v-if="contract.canDownload" @click.stop="downloadContract(contract.contractId)">
						<text>📥 下载</text>
					</view>
					<view class="action-btn" v-if="contract.canTerminate" @click.stop="terminateContract(contract.contractId)">
						<text>❌ 终止</text>
					</view>
				</view>
			</view>
			
			<!-- 加载状态 -->
			<view class="load-more" v-if="loading">
				<text>加载中...</text>
			</view>
			<view class="no-more" v-if="noMore">
				<text>没有更多了</text>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-state" v-if="!loading && contractList.length === 0">
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
			activeTab: 0,
			contractList: [],
			loading: false,
			noMore: false,
			pageNum: 1,
			pageSize: 10,
			stats: {
				total: 0,
				active: 0,
				draft: 0,
				pending: 0
			},
			refreshing: false
		}
	},
	
	onLoad() {
		this.loadStats()
		this.loadContracts()
	},
	
	// 下拉刷新 - 必须放在methods外面作为页面生命周期函数
	onPullDownRefresh() {
		console.log('触发下拉刷新')
		this.pageNum = 1
		this.contractList = []
		Promise.all([
			this.loadStats(),
			this.loadContracts()
		]).finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadStats() {
			try {
				const res = await api.landlord.getContractStats()
				if (res.code === 200) {
					// 映射后端返回的字段
					this.stats = {
						total: res.data.totalContracts || 0,
						active: res.data.activeContracts || 0,
						draft: res.data.draftContracts || 0, // 待签署
						pending: res.data.pendingContracts || 0 // 待审核
					}
				}
			} catch (e) {
				console.error('加载合同统计失败:', e)
				this.stats = { total: 0, active: 0, draft: 0, pending: 0 }
			}
		},
		
		async loadContracts() {
			if (this.loading || this.noMore) return
			
			this.loading = true
			try {
				// 构建请求参数，只有status有值时才传递
				const params = {
					pageNum: this.pageNum,
					pageSize: this.pageSize
				}
				const statusFilter = this.getStatusFilter()
				if (statusFilter) {
					params.status = statusFilter
				}
				console.log('加载合同列表，参数:', params)
				const res = await api.landlord.getContracts(params)
				
				if (res.code === 200) {
					const records = (res.data.records || []).map(item => this.formatContract(item))
					this.contractList = this.pageNum === 1 ? records : [...this.contractList, ...records]
					this.noMore = records.length < this.pageSize
				}
			} catch (e) {
				console.error('加载合同列表失败:', e)
				uni.showToast({ title: '加载失败', icon: 'none' })
			} finally {
				this.loading = false
			}
		},
		
		// 格式化合同数据
		formatContract(item) {
			// 根据合同状态和签名状态计算显示状态
			let status = item.contractStatus
			console.log('合同状态:', item.contractNo, 'contractStatus:', item.contractStatus, 'auditStatus:', item.auditStatus)
			
			if (status === 'draft') {
				// 草稿状态，检查签名情况
				if (!item.landlordSignature && !item.tenantSignature) {
					status = 'pending_sign' // 待签署
				} else if (item.landlordSignature && !item.tenantSignature) {
					status = 'pending_tenant_sign' // 待租客签署
				} else if (!item.landlordSignature && item.tenantSignature) {
					status = 'pending_landlord_sign' // 待房东签署
				}
			} else if (status === 'signed') {
				// 已签署状态，显示为审核中
				status = 'pending_audit'
			} else if (status === 'effective') {
				status = 'active' // 生效中
			}
			
			return {
				contractId: item.contractId,
				contractNo: item.contractNo,
				houseTitle: item.houseTitle || '未知房源',
				houseImage: item.houseImage,
				landlordName: item.landlordName || '房东',
				tenantName: item.tenantName || '租客',
				status: status,
				startDate: item.startDate,
				endDate: item.endDate,
				monthlyRent: item.rentPrice,
				createTime: item.createdAt ? item.createdAt.split('T')[0] : '',
				canDownload: status === 'active' || status === 'terminated',
				canTerminate: status === 'active',
				canSign: status === 'pending_sign' || status === 'pending_landlord_sign'
			}
		},
		
		getStatusFilter() {
			const statusMap = {
				0: null, // 全部
				1: 'draft', // 待签署
				2: 'signed', // 审核中
				3: 'effective', // 生效中
				4: 'terminated' // 已终止
			}
			return statusMap[this.activeTab]
		},
		
		switchTab(index) {
			this.activeTab = index
			this.pageNum = 1
			this.noMore = false
			this.contractList = []
			this.loadContracts()
		},
		
		loadMore() {
			this.pageNum++
			this.loadContracts()
		},
		
		getStatusText(status) {
			const statusMap = {
				'pending_sign': '待签署',
				'pending_tenant_sign': '待租客签署',
				'pending_landlord_sign': '待房东签署',
				'pending_audit': '审核中',
				'active': '生效中',
				'effective': '生效中',
				'terminated': '已终止',
				'expired': '已过期',
				'draft': '草稿'
			}
			return statusMap[status] || status
		},
		
		viewContract(contractId) {
			uni.navigateTo({
				url: `/pages/contract/detail/detail?id=${contractId}`
			})
		},
		
		async downloadContract(contractId) {
			try {
				uni.showLoading({ title: '下载中...' })
				const res = await api.contract.download(contractId)
				if (res.code === 200) {
					// 处理PDF下载
					uni.showToast({ title: '下载成功', icon: 'success' })
				}
			} catch (e) {
				console.error('下载合同失败:', e)
				uni.showToast({ title: '下载失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},
		
		terminateContract(contractId) {
			uni.showModal({
				title: '确认终止',
				content: '确定要终止此合同吗？此操作不可撤销。',
				success: async (res) => {
					if (res.confirm) {
						try {
							const result = await api.contract.terminate(contractId)
							if (result.code === 200) {
								uni.showToast({ title: '终止成功', icon: 'success' })
								this.loadContracts()
							}
						} catch (e) {
							console.error('终止合同失败:', e)
							uni.showToast({ title: '终止失败', icon: 'none' })
						}
					}
				}
			})
		}
	}
}
</script>

<style scoped>
.contracts-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 40rpx;
}

/* 统计头部 */
.stats-header {
	display: flex;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 40rpx 20rpx;
	color: #fff;
}

.stat-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-num {
	font-size: 48rpx;
	font-weight: bold;
	margin-bottom: 8rpx;
}

.stat-label {
	font-size: 24rpx;
	opacity: 0.9;
}

/* 筛选标签 */
.filter-tabs {
	display: flex;
	background: #fff;
	padding: 20rpx;
	margin: 20rpx;
	border-radius: 16rpx;
	white-space: nowrap;
}

.filter-tabs .tab-item {
	display: inline-block;
	padding: 12rpx 32rpx;
	margin-right: 20rpx;
	background: #f5f7fa;
	border-radius: 40rpx;
	font-size: 28rpx;
	color: #333;
}

.filter-tabs .tab-item.active {
	background: #409eff;
	color: #fff;
}

/* 合同列表 */
.contract-list {
	height: calc(100vh - 400rpx);
	padding: 0 20rpx;
}

.contract-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.contract-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 20rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.contract-info {
	flex: 1;
}

.contract-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 12rpx;
}

.contract-parties {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.party {
	font-size: 24rpx;
	color: #666;
}

.status-badge {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	font-weight: 500;
}

.status-badge.pending_sign,
.status-badge.pending_tenant_sign,
.status-badge.pending_landlord_sign,
.status-badge.draft {
	background: #fff3e0;
	color: #ff9800;
}

.status-badge.pending_audit,
.status-badge.signed {
	background: #e3f2fd;
	color: #2196f3;
}

.status-badge.active,
.status-badge.effective {
	background: #e8f5e9;
	color: #4caf50;
}

.status-badge.terminated,
.status-badge.expired {
	background: #fafafa;
	color: #999;
}

.contract-details {
	margin-bottom: 20rpx;
}

.detail-row {
	display: flex;
	justify-content: space-between;
	padding: 12rpx 0;
	font-size: 28rpx;
}

.detail-row .label {
	color: #999;
}

.detail-row .value {
	color: #333;
}

.detail-row .value.price {
	color: #ff6b6b;
	font-weight: 600;
}

.contract-actions {
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

/* 加载状态 */
.load-more, .no-more {
	text-align: center;
	padding: 40rpx;
	color: #999;
	font-size: 28rpx;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 40rpx;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 24rpx;
	opacity: 0.3;
}

.empty-text {
	font-size: 32rpx;
	color: #999;
}

/* 房东底部导航栏 */
.landlord-tabbar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	background: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
	padding-bottom: env(safe-area-inset-bottom);
}

.landlord-tabbar .tab-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 16rpx 0;
}

.tab-icon {
	font-size: 44rpx;
	margin-bottom: 8rpx;
}

.tab-text {
	font-size: 22rpx;
	color: #666;
}

.landlord-tabbar .tab-item.active .tab-text {
	color: #409eff;
}
</style>

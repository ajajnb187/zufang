<template>
	<view class="page">
		<!-- 渐变背景头部 -->
		<view class="header">
			<view class="header-bg"></view>
			<view class="header-content">
					<!-- 统计卡片 -->
				<view class="stats-card">
					<view class="stat-item">
						<text class="stat-num">{{ totalHouses }}</text>
						<text class="stat-label">全部房源</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item">
						<text class="stat-num success">{{ availableHouses }}</text>
						<text class="stat-label">可出租</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item">
						<text class="stat-num primary">{{ rentedHouses }}</text>
						<text class="stat-label">已出租</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 筛选标签 -->
		<view class="filter-section">
			<scroll-view class="filter-tabs" scroll-x enable-flex>
				<view class="tab" :class="{ active: activeTab === 0 }" @click="switchTab(0)">全部</view>
				<view class="tab" :class="{ active: activeTab === 1 }" @click="switchTab(1)">待审核</view>
				<view class="tab" :class="{ active: activeTab === 2 }" @click="switchTab(2)">已上架</view>
				<view class="tab" :class="{ active: activeTab === 3 }" @click="switchTab(3)">已下架</view>
			</scroll-view>
		</view>
		
		<!-- 房源列表 -->
		<scroll-view class="house-list" scroll-y @scrolltolower="loadMore">
			<view class="house-card" v-for="house in houseList" :key="house.houseId" @click="manageHouse(house.houseId)">
				<!-- 图片区域 -->
				<view class="card-image">
					<image :src="house.coverImage || '/static/logo.png'" mode="aspectFill" @error="house.coverImage = '/static/logo.png'"></image>
					<!-- 状态标签 -->
					<view class="status-badge" :class="getStatusClass(house)">
						{{ getStatusText(house) }}
					</view>
					<!-- 价格 -->
					<view class="price-badge">
						<text class="price">{{ house.rentPrice }}</text>
						<text class="unit">元/月</text>
					</view>
				</view>
				<!-- 信息区域 -->
				<view class="card-info">
					<view class="card-title">{{ house.title }}</view>
					<view class="card-meta">
						<text class="meta-tag">{{ house.houseType || house.roomType }}</text>
						<text class="meta-tag">{{ house.area }}㎡</text>
					</view>
					<view class="card-location">
						<text>📍</text>
						<text>{{ house.communityName }}</text>
					</view>
					<!-- 操作按钮 -->
					<view class="card-actions">
						<button class="btn-edit" @click.stop="editHouse(house.houseId)">编辑</button>
						<button v-if="house.auditStatus === 'approved'" 
							class="btn-toggle" 
							:class="house.publishStatus === 'online' ? 'offline' : 'online'"
							@click.stop="toggleStatus(house)">
							{{ house.publishStatus === 'online' ? '下架' : '上架' }}
						</button>
						<button v-else-if="house.auditStatus === 'pending'" class="btn-pending" disabled>审核中</button>
						<button v-else-if="house.auditStatus === 'rejected'" class="btn-resubmit" @click.stop="editHouse(house.houseId)">重新提交</button>
					</view>
				</view>
			</view>
			
			<!-- 加载状态 -->
			<view class="loading" v-if="loading">
				<view class="spinner"></view>
				<text>加载中...</text>
			</view>
			<view class="no-more" v-if="noMore && houseList.length > 0">
				<text>—— 已经到底了 ——</text>
			</view>
			
			<!-- 空状态 -->
			<view class="empty" v-if="!loading && houseList.length === 0">
				<view class="empty-icon">🏠</view>
				<text class="empty-title">暂无房源</text>
				<text class="empty-desc">可前往个人中心发布您的第一套房源</text>
				<button class="empty-btn" @click="goToProfile">去个人中心</button>
			</view>
		</scroll-view>
		
		<!-- 底部导航 -->
		<view class="tabbar">
			<view class="tabbar-item active">
				<text class="tabbar-icon">🏠</text>
				<text class="tabbar-label">房源</text>
			</view>
			<view class="tabbar-item" @click="goToTenants">
				<text class="tabbar-icon">👥</text>
				<text class="tabbar-label">租客</text>
			</view>
			<view class="tabbar-item" @click="goToChat">
				<text class="tabbar-icon">💬</text>
				<text class="tabbar-label">消息</text>
			</view>
			<view class="tabbar-item" @click="goToProfile">
				<text class="tabbar-icon">👤</text>
				<text class="tabbar-label">我的</text>
			</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			activeTab: 0,
			houseList: [],
			loading: false,
			noMore: false,
			pageNum: 1,
			pageSize: 10,
			totalHouses: 0,
			rentedHouses: 0,
			availableHouses: 0
		}
	},
	
	onLoad() {
		console.log('【房东页面】onLoad被调用，页面开始加载');
		console.log('【房东页面】当前页面路径:', getCurrentPages()[getCurrentPages().length - 1]?.route);
		this.loadLandlordHouses()
		this.loadStatistics()
	},
	
	// 页面显示时刷新数据（从编辑页返回时）
	onShow() {
		// 重新加载数据确保编辑后立即显示最新数据
		this.pageNum = 1
		this.noMore = false
		this.houseList = []
		this.loadLandlordHouses()
		this.loadStatistics()
	},
	
	// 下拉刷新 - 必须放在methods外面作为页面生命周期函数
	onPullDownRefresh() {
		console.log('触发下拉刷新')
		this.pageNum = 1
		this.noMore = false
		this.houseList = []
		Promise.all([
			this.loadLandlordHouses(),
			this.loadStatistics()
		]).finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadLandlordHouses() {
			if (this.loading || this.noMore) return
			
			this.loading = true
			try {
				// 获取筛选条件
				const filter = this.getStatusFilter()
				// 调用房东房源接口
				const res = await api.house.getLandlordHouses({
					status: filter.status,
					auditStatus: filter.auditStatus,
					pageNum: this.pageNum,
					pageSize: this.pageSize
				})
				
				if (res.code === 200) {
					const records = res.data.records || []
					// 处理每个房源的图片
					records.forEach(house => {
						let imageList = []
						if (house.images) {
							if (typeof house.images === 'string') {
								try {
									imageList = JSON.parse(house.images)
								} catch (e) {
									if (house.images.startsWith('http')) {
										imageList = [house.images]
									}
								}
							} else if (Array.isArray(house.images)) {
								imageList = house.images
							}
						}
						house.coverImage = imageList.length > 0 ? imageList[0] : '/static/logo.png'
					})
					this.houseList = this.pageNum === 1 ? records : [...this.houseList, ...records]
					this.noMore = records.length < this.pageSize
				}
			} catch (e) {
				console.error('加载房东房源失败:', e)
			} finally {
				this.loading = false
			}
		},
		
		async loadStatistics() {
			try {
				const res = await api.landlord.getHouseStatistics()
				if (res.code === 200) {
					this.totalHouses = res.data.totalHouses || 0
					this.rentedHouses = res.data.rentedHouses || 0
					this.availableHouses = res.data.availableHouses || 0
				}
			} catch (e) {
				console.error('加载统计数据失败:', e)
			}
		},
		
		getStatusFilter() {
			// 0-全部, 1-待审核, 2-已上架, 3-已下架
			const filterMap = {
				0: { status: null, auditStatus: null },
				1: { status: null, auditStatus: 'pending' },
				2: { status: 'online', auditStatus: null },
				3: { status: 'offline', auditStatus: 'approved' } // 已下架且审核通过的
			}
			return filterMap[this.activeTab]
		},
		
		switchTab(index) {
			this.activeTab = index
			this.pageNum = 1
			this.noMore = false
			this.houseList = []
			this.loadLandlordHouses()
		},
		
		loadMore() {
			this.pageNum++
			this.loadLandlordHouses()
		},
		
		viewHouse(house) {
			uni.navigateTo({
				url: `/pages/landlord/house-detail/house-detail?id=${house.houseId}`
			})
		},
		
		manageHouse(houseId) {
			uni.navigateTo({
				url: `/pages/landlord/house-detail/house-detail?id=${houseId}`
			})
		},
		
		editHouse(houseId) {
			uni.navigateTo({
				url: `/pages/landlord/publish/publish?id=${houseId}&mode=edit`
			})
		},
		
		async toggleStatus(house) {
			try {
				uni.showLoading({ title: '处理中...' })
				
				const action = house.publishStatus === 'online' ? 'offline' : 'online'
				const res = await api.house[action](house.houseId)
				
				if (res.code === 200) {
					house.publishStatus = action
					uni.showToast({ 
						title: action === 'online' ? '上架成功' : '下架成功', 
						icon: 'success' 
					})
					this.loadStatistics() // 刷新统计数据
				} else {
					throw new Error(res.message || '操作失败')
				}
			} catch (e) {
				console.error('切换房源状态失败:', e)
				uni.showToast({ 
					title: e.message || '操作失败', 
					icon: 'none' 
				})
			} finally {
				uni.hideLoading()
			}
		},
		
		goPublish() {
			uni.navigateTo({
				url: '/pages/landlord/publish/publish'
			})
		},
		
		goAppointments() {
			uni.navigateTo({
				url: '/pages/landlord/appointments/appointments'
			})
		},
		
		goCreateContract() {
			uni.navigateTo({
				url: '/pages/landlord/contract-create/contract-create'
			})
		},
		
		getStatusText(house) {
			// 优先显示审核状态
			if (house.auditStatus === 'pending') return '待审核'
			if (house.auditStatus === 'rejected') return '审核拒绝'
			// 审核通过后显示发布状态
			const statusMap = {
				'online': '已上架',
				'offline': '已下架',
				'rented': '已出租'
			}
			return statusMap[house.publishStatus] || '未知'
		},
		
		getStatusClass(house) {
			if (house.auditStatus === 'pending') return 'status-pending'
			if (house.auditStatus === 'rejected') return 'status-rejected'
			return `status-${house.publishStatus}`
		},
		
		// 底部导航
		goToTenants() {
			uni.reLaunch({ url: '/pages/landlord/tenants/tenants' })
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

.page {
	height: 100vh;
	background: #F7F9FC;
	padding-bottom: 120rpx;
	display: flex;
	flex-direction: column;
	box-sizing: border-box;
}

.header-bg {
	display: none;
}

.header-content {
	background: #FFFFFF;
	border-radius: 20rpx;
	padding: 28rpx 24rpx;
	box-shadow: 0 6rpx 18rpx rgba(17, 24, 39, 0.06);
}

.stats-card {
	display: flex;
	align-items: center;
}

.stat-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-num {
	font-size: 44rpx;
	font-weight: 800;
	color: #1F2937;
	line-height: 1.2;
}

.stat-num.success {
	color: #16A34A;
}

.stat-num.primary {
	color: #FF6B35;
}

.stat-label {
	font-size: 24rpx;
	color: #8B95A5;
	margin-top: 8rpx;
}

.stat-divider {
	width: 1rpx;
	height: 52rpx;
	background: #EEF2F7;
}

.filter-section {
	padding: 0 24rpx 12rpx;
}

.filter-tabs {
	white-space: nowrap;
}

.tab {
	display: inline-block;
	padding: 14rpx 28rpx;
	margin-right: 16rpx;
	background: #FFFFFF;
	border-radius: 999rpx;
	font-size: 28rpx;
	color: #5A6C7D;
	font-weight: 500;
	border: 1rpx solid #EEF2F7;
}

.tab.active {
	background: #FF6B35;
	color: #FFFFFF;
	border-color: #FF6B35;
}

.house-list {
	height: calc(100vh - 360rpx);
	padding: 12rpx 24rpx 24rpx;
}

.house-card {
	background: #FFFFFF;
	border-radius: 20rpx;
	margin-bottom: 20rpx;
	overflow: hidden;
	border: 1rpx solid #EEF2F7;
	box-shadow: 0 6rpx 18rpx rgba(17, 24, 39, 0.06);
}

.house-card:active {
	transform: scale(0.99);
}

.card-image {
	position: relative;
	height: 300rpx;
}

.card-image image {
	width: 100%;
	height: 100%;
}

.status-badge {
	position: absolute;
	top: 16rpx;
	left: 16rpx;
	padding: 8rpx 18rpx;
	border-radius: 999rpx;
	font-size: 24rpx;
	font-weight: 600;
	color: #FFFFFF;
}

.status-badge.status-online {
	background: rgba(22, 163, 74, 0.92);
}

.status-badge.status-offline {
	background: rgba(250, 140, 22, 0.92);
}

.status-badge.status-pending {
	background: rgba(99, 102, 241, 0.92);
}

.status-badge.status-rejected {
	background: rgba(255, 77, 79, 0.92);
}

.status-badge.status-rented {
	background: rgba(24, 144, 255, 0.92);
}

.price-badge {
	position: absolute;
	bottom: 16rpx;
	right: 16rpx;
	background: rgba(0, 0, 0, 0.55);
	padding: 10rpx 16rpx;
	border-radius: 14rpx;
	display: flex;
	align-items: baseline;
}

.price-badge .price {
	font-size: 34rpx;
	font-weight: 800;
	color: #FFFFFF;
}

.price-badge .unit {
	font-size: 22rpx;
	color: rgba(255, 255, 255, 0.9);
	margin-left: 4rpx;
}

.card-info {
	padding: 22rpx 22rpx 18rpx;
}

.card-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #1F2937;
	line-height: 1.4;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
	margin-bottom: 12rpx;
}

.card-meta {
	display: flex;
	gap: 12rpx;
	margin-bottom: 12rpx;
}

.meta-tag {
	font-size: 24rpx;
	color: #5A6C7D;
	background: #F7F9FC;
	padding: 8rpx 14rpx;
	border-radius: 10rpx;
	border: 1rpx solid #EEF2F7;
}

.card-location {
	display: flex;
	align-items: center;
	gap: 8rpx;
	font-size: 26rpx;
	color: #8B95A5;
	margin-bottom: 18rpx;
}

.card-actions {
	display: flex;
	gap: 14rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #F3F6FB;
}

.card-actions button {
	flex: 1;
	height: 68rpx;
	line-height: 68rpx;
	border-radius: 999rpx;
	font-size: 26rpx;
	font-weight: 600;
	border: none;
	padding: 0;
}

.card-actions button::after {
	border: none;
}

.btn-edit {
	background: #F7F9FC;
	color: #5A6C7D;
}

.btn-toggle.online {
	background: #16A34A;
	color: #FFFFFF;
}

.btn-toggle.offline {
	background: #FA8C16;
	color: #FFFFFF;
}

.btn-pending {
	background: #EEF2F7;
	color: #8B95A5;
}

.btn-resubmit {
	background: #FF4D4F;
	color: #FFFFFF;
}

.loading {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	padding: 40rpx;
	color: #8B95A5;
	font-size: 26rpx;
}

.spinner {
	width: 34rpx;
	height: 34rpx;
	border: 4rpx solid #EEF2F7;
	border-top-color: #FF6B35;
	border-radius: 50%;
	animation: spin 0.8s linear infinite;
}

@keyframes spin {
	to { transform: rotate(360deg); }
}

.no-more {
	text-align: center;
	padding: 40rpx;
	color: #B0B8C1;
	font-size: 26rpx;
}

.empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 100rpx 40rpx;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 24rpx;
	opacity: 0.35;
}

.empty-title {
	font-size: 34rpx;
	font-weight: 700;
	color: #1F2937;
	margin-bottom: 12rpx;
}

.empty-desc {
	font-size: 28rpx;
	color: #8B95A5;
	margin-bottom: 36rpx;
}

.empty-btn {
	background: #FF6B35;
	color: #FFFFFF;
	padding: 20rpx 56rpx;
	border-radius: 999rpx;
	font-size: 30rpx;
	font-weight: 600;
	border: none;
}

.empty-btn::after {
	border: none;
}

.tabbar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 100rpx;
	background: #FFFFFF;
	display: flex;
	border-top: 1rpx solid #EEF2F7;
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
	font-size: 44rpx;
	margin-bottom: 4rpx;
}

.tabbar-label {
	font-size: 22rpx;
	color: #8B95A5;
	font-weight: 500;
}

.tabbar-item.active .tabbar-label {
	color: #FF6B35;
	font-weight: 600;
}
</style>

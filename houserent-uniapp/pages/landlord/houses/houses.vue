<template>
	<view class="landlord-page">
		<!-- 头部统计 -->
		<view class="header-stats">
			<view class="stat-item">
				<text class="stat-number">{{ totalHouses }}</text>
				<text class="stat-label">房源总数</text>
			</view>
			<view class="stat-item">
				<text class="stat-number">{{ rentedHouses }}</text>
				<text class="stat-label">已出租</text>
			</view>
			<view class="stat-item">
				<text class="stat-number">{{ availableHouses }}</text>
				<text class="stat-label">可出租</text>
			</view>
		</view>
		
		<!-- 操作按钮 -->
		<view class="action-buttons">
			<button class="publish-btn" @click="goPublish">
				<text class="btn-icon">➕</text>
				发布房源
			</button>
			<button class="action-btn-small" @click="goAppointments">
				<text class="btn-icon">📅</text>
				预约管理
			</button>
			<button class="action-btn-small" @click="goCreateContract">
				<text class="btn-icon">📄</text>
				发起合同
			</button>
		</view>
		
		<!-- 房源状态筛选 -->
		<scroll-view class="filter-tabs" scroll-x enable-flex>
			<view class="tab-item" :class="{ active: activeTab === 0 }" @click="switchTab(0)">
				全部
			</view>
			<view class="tab-item" :class="{ active: activeTab === 1 }" @click="switchTab(1)">
				待审核
			</view>
			<view class="tab-item" :class="{ active: activeTab === 2 }" @click="switchTab(2)">
				已上架
			</view>
			<view class="tab-item" :class="{ active: activeTab === 3 }" @click="switchTab(3)">
				已下架
			</view>
		</scroll-view>
		
		<!-- 我的房源列表 -->
		<scroll-view class="house-list" scroll-y enable-flex @scrolltolower="loadMore">
			<view class="house-card" v-for="house in houseList" :key="house.houseId" @click="manageHouse(house.houseId)">
				<image class="house-img" :src="house.coverImage || '/static/logo.png'" mode="aspectFill" @error="house.coverImage = '/static/logo.png'"></image>
				<view class="house-info">
					<view class="house-title">{{ house.title }}</view>
					<view class="house-tags">
						<text class="tag">{{ house.area }}㎡</text>
						<text class="tag">{{ house.roomType }}</text>
						<text class="status-tag" :class="getStatusClass(house)">{{ getStatusText(house) }}</text>
					</view>
					<view class="house-location">{{ house.communityName }}</view>
					<view class="house-bottom">
						<view class="house-price">
							<text class="price-num">{{ house.rentPrice }}</text>
							<text class="price-unit">/月</text>
						</view>
						<view class="house-actions">
							<button class="action-btn" @click.stop="editHouse(house.houseId)">编辑</button>
							<!-- 只有审核通过的房源才能上架/下架 -->
							<button v-if="house.auditStatus === 'approved'" 
								class="action-btn" @click.stop="toggleStatus(house)" 
								:class="house.publishStatus === 'online' ? 'offline-btn' : 'online-btn'">
								{{ house.publishStatus === 'online' ? '下架' : '上架' }}
							</button>
							<button v-else-if="house.auditStatus === 'pending'" 
								class="action-btn pending-btn" disabled>
								审核中
							</button>
							<button v-else-if="house.auditStatus === 'rejected'" 
								class="action-btn rejected-btn" @click.stop="editHouse(house.houseId)">
								重新提交
							</button>
						</view>
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
			<view class="empty-state" v-if="!loading && houseList.length === 0">
				<text class="empty-icon">🏠</text>
				<text class="empty-text">暂无房源</text>
				<button class="empty-btn" @click="goPublish">立即发布</button>
			</view>
		</scroll-view>
		
		<!-- 房东底部导航栏 -->
		<view class="tabbar">
			<view class="tabbar-item active">
				<text class="tabbar-icon">🏠</text>
				<text class="tabbar-text">房源</text>
			</view>
			<view class="tabbar-item" @click="goToTenants">
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
.landlord-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 120rpx;
}

/* 头部统计 */
.header-stats {
	display: flex;
	background: #fff;
	padding: 30rpx 20rpx;
	margin: 20rpx;
	border-radius: 16rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.stat-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-number {
	font-size: 48rpx;
	font-weight: bold;
	color: #409eff;
	margin-bottom: 8rpx;
}

.stat-label {
	font-size: 24rpx;
	color: #666;
}

/* 操作按钮 */
.action-buttons {
	padding: 0 20rpx 20rpx;
	display: flex;
	gap: 16rpx;
	flex-wrap: wrap;
}

.publish-btn {
	flex: 1;
	min-width: 200rpx;
	background: #409eff;
	color: white;
	border-radius: 16rpx;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 24rpx rgba(64, 158, 255, 0.3);
}

.action-btn-small {
	flex: 1;
	min-width: 180rpx;
	background: #fff;
	color: #666;
	border-radius: 16rpx;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	border: 1rpx solid #e0e0e0;
}

.btn-icon {
	margin-right: 12rpx;
	font-size: 36rpx;
}

/* 筛选标签 */
.filter-tabs {
	display: flex;
	background: #fff;
	padding: 20rpx;
	margin: 0 20rpx 20rpx;
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

/* 房源列表 */
.house-list {
	height: calc(100vh - 400rpx);
	padding: 0 20rpx;
}

.house-card {
	display: flex;
	background: #fff;
	margin-bottom: 20rpx;
	padding: 24rpx;
	border-radius: 16rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.house-img {
	width: 200rpx;
	height: 150rpx;
	border-radius: 12rpx;
	margin-right: 20rpx;
}

.house-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.house-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.house-tags {
	display: flex;
	gap: 12rpx;
	margin: 8rpx 0;
	flex-wrap: wrap;
}

.tag {
	font-size: 24rpx;
	color: #666;
	background: #f5f7fa;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
}

.status-tag {
	font-size: 22rpx;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
	font-weight: 500;
}

.status-online {
	background: #e8f5e8;
	color: #52c41a;
}

.status-offline {
	background: #fff2e8;
	color: #fa8c16;
}

.status-rented {
	background: #e6f7ff;
	color: #1890ff;
}

.status-pending {
	background: #f6f6f6;
	color: #999;
}

.house-location {
	font-size: 24rpx;
	color: #999;
}

.house-bottom {
	display: flex;
	justify-content: space-between;
	align-items: flex-end;
}

.house-price {
	color: #ff6b6b;
}

.price-num {
	font-size: 36rpx;
	font-weight: bold;
}

.price-unit {
	font-size: 24rpx;
}

.house-actions {
	display: flex;
	gap: 12rpx;
}

.action-btn {
	padding: 8rpx 16rpx;
	border-radius: 8rpx;
	font-size: 24rpx;
	background: #f5f7fa;
	color: #666;
	border: none;
}

.online-btn {
	background: #e8f5e8;
	color: #52c41a;
}

.offline-btn {
	background: #fff2e8;
	color: #fa8c16;
}

.pending-btn {
	background: #f6f6f6;
	color: #999;
	opacity: 0.7;
}

.rejected-btn {
	background: #fff1f0;
	color: #ff4d4f;
}

.status-rejected {
	background: #fff1f0;
	color: #ff4d4f;
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
	margin-bottom: 40rpx;
}

.empty-btn {
	background: #409eff;
	color: white;
	border-radius: 40rpx;
	padding: 16rpx 40rpx;
	font-size: 28rpx;
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
</style>

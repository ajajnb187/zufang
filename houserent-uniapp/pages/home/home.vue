<template>
	<view class="home-page">
		<!-- 顶部定位和搜索栏 -->
		<view class="top-bar">
			<!-- 地区选择 -->
			<view class="location-selector" @click="showDistrictPicker = true">
				<text class="location-icon">📍</text>
				<text class="location-text">{{ selectedDistrictName || '选择地区' }}</text>
				<text class="location-arrow">▼</text>
			</view>
			<!-- 搜索框 -->
			<view class="search-box" @click="goSearch">
				<text class="search-icon">🔍</text>
				<text class="search-placeholder">搜索房源</text>
			</view>
		</view>
		
		<!-- 筛选标签 -->
		<scroll-view class="filter-tabs" scroll-x enable-flex>
			<view class="tab-item" :class="{ active: activeTab === 0 }" @click="switchTab(0)">
				全部
			</view>
			<view class="tab-item" :class="{ active: activeTab === 1 }" @click="switchTab(1)">
				短期
			</view>
			<view class="tab-item" :class="{ active: activeTab === 2 }" @click="switchTab(2)">
				长期
			</view>
		</scroll-view>
		
		<!-- 地区选择弹窗 -->
		<view class="picker-mask" v-if="showDistrictPicker" @click="showDistrictPicker = false"></view>
		<view class="community-picker" :class="{ show: showDistrictPicker }">
			<view class="picker-header">
				<text class="picker-title">选择地区</text>
				<text class="picker-close" @click="showDistrictPicker = false">×</text>
			</view>
			<!-- 三级联动选择 -->
			<view class="region-tabs">
				<view class="region-tab" :class="{ active: regionStep === 1 }" @click="regionStep = 1">
					{{ selectedProvince || '选择省份' }}
				</view>
				<view class="region-tab" :class="{ active: regionStep === 2 }" @click="regionStep = 2" v-if="selectedProvince">
					{{ selectedCity || '选择城市' }}
				</view>
				<view class="region-tab" :class="{ active: regionStep === 3 }" @click="regionStep = 3" v-if="selectedCity">
					{{ selectedDistrict || '选择区县' }}
				</view>
			</view>
			<scroll-view class="picker-list" scroll-y>
				<!-- 省份列表 -->
				<template v-if="regionStep === 1">
					<view class="picker-item" :class="{ active: !selectedProvince }" @click="selectProvince('')">
						<text>全部地区</text>
					</view>
					<view class="picker-item" :class="{ active: selectedProvince === item.name }" 
						v-for="item in regionData" :key="item.name" @click="selectProvince(item.name)">
						<text>{{ item.name }}</text>
					</view>
				</template>
				<!-- 城市列表 -->
				<template v-if="regionStep === 2">
					<view class="picker-item" :class="{ active: !selectedCity }" @click="selectCity('')">
						<text>全部城市</text>
					</view>
					<view class="picker-item" :class="{ active: selectedCity === item.name }" 
						v-for="item in currentCityList" :key="item.name" @click="selectCity(item.name)">
						<text>{{ item.name }}</text>
					</view>
				</template>
				<!-- 区县列表 -->
				<template v-if="regionStep === 3">
					<view class="picker-item" :class="{ active: !selectedDistrict }" @click="selectDistrict('')">
						<text>全部区县</text>
					</view>
					<view class="picker-item" :class="{ active: selectedDistrict === item }" 
						v-for="item in currentDistrictList" :key="item" @click="selectDistrict(item)">
						<text>{{ item }}</text>
					</view>
				</template>
			</scroll-view>
		</view>
		
		<!-- 房源列表 -->
		<scroll-view class="house-list" scroll-y enable-flex @scrolltolower="loadMore">
			<view class="house-grid">
				<view class="house-card" v-for="house in houseList" :key="house.houseId" @click="viewDetail(house.houseId)">
					<!-- 房源图片 -->
					<view class="card-image-wrapper">
						<image class="card-image" :src="house.coverImage || '/static/logo.png'" mode="aspectFill" @error="house.coverImage = '/static/logo.png'"></image>
						<!-- 价格标签 -->
						<view class="price-tag">
							<text class="price-num">{{ house.rentPrice }}</text>
							<text class="price-unit">元/月</text>
						</view>
						<!-- 浏览数 -->
						<view class="view-badge" v-if="house.viewCount">
							<text>👁️ {{ house.viewCount }}</text>
						</view>
					</view>
					<!-- 房源信息 -->
					<view class="card-content">
						<view class="card-title">{{ house.title }}</view>
						<view class="card-tags">
							<text class="card-tag">{{ house.houseType || house.roomType }}</text>
							<text class="card-tag">{{ house.area }}㎡</text>
							<text class="card-tag" v-if="house.orientation">{{ house.orientation }}</text>
						</view>
						<view class="card-location">
							<text class="location-icon">📍</text>
							<text class="location-text">{{ house.communityName }}</text>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 加载状态 -->
			<view class="load-more" v-if="loading">
				<view class="loading-spinner"></view>
				<text>加载中...</text>
			</view>
			<view class="no-more" v-if="noMore">
				<text>—— 已经到底了 ——</text>
			</view>
		</scroll-view>
		

	</view>
</template>

<script>
import api from '@/utils/api.js'
import { regionData } from '@/static/data/region.js'

export default {
	data() {
		return {
			activeTab: 0,
			houseList: [],
			loading: false,
			noMore: false,
			pageNum: 1,
			pageSize: 10,
			// 地区选择相关
			showDistrictPicker: false,
			regionStep: 1,
			selectedProvince: '',
			selectedCity: '',
			selectedDistrict: '',
			selectedDistrictName: '',
			// 中国省市区数据（从外部文件引入）
			regionData
		}
	},
	
	onLoad() {
		console.log('【租客页面】onLoad被调用');
		this.loadHouses()
	},
	
	// 下拉刷新 - 页面生命周期函数
	onPullDownRefresh() {
		console.log('【租客首页】触发下拉刷新')
		this.pageNum = 1
		this.noMore = false
		this.houseList = []
		this.loadHouses().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	computed: {
		// 当前省份的城市列表
		currentCityList() {
			if (!this.selectedProvince) return []
			const province = this.regionData.find(p => p.name === this.selectedProvince)
			return province ? province.cities : []
		},
		// 当前城市的区县列表
		currentDistrictList() {
			if (!this.selectedCity) return []
			const city = this.currentCityList.find(c => c.name === this.selectedCity)
			return city ? city.districts : []
		}
	},
	
	methods: {
		// 选择省份
		selectProvince(name) {
			if (!name) {
				// 选择全部地区
				this.selectedProvince = ''
				this.selectedCity = ''
				this.selectedDistrict = ''
				this.selectedDistrictName = ''
				this.showDistrictPicker = false
				this.refreshHouses()
			} else {
				this.selectedProvince = name
				this.selectedCity = ''
				this.selectedDistrict = ''
				this.regionStep = 2
			}
		},
		
		// 选择城市
		selectCity(name) {
			if (!name) {
				// 选择全部城市，确认省份筛选
				this.selectedCity = ''
				this.selectedDistrict = ''
				this.selectedDistrictName = this.selectedProvince
				this.showDistrictPicker = false
				this.refreshHouses()
			} else {
				this.selectedCity = name
				this.selectedDistrict = ''
				this.regionStep = 3
			}
		},
		
		// 选择区县
		selectDistrict(name) {
			this.selectedDistrict = name || ''
			if (name) {
				this.selectedDistrictName = name
			} else {
				// 选择全部区县
				this.selectedDistrictName = this.selectedCity || this.selectedProvince
			}
			this.showDistrictPicker = false
			this.refreshHouses()
		},
		
		// 刷新房源列表
		refreshHouses() {
			this.pageNum = 1
			this.noMore = false
			this.houseList = []
			this.loadHouses()
		},
		
		async loadHouses() {
			if (this.loading || this.noMore) return Promise.resolve()
			
			this.loading = true
			try {
				// 构建筛选参数
				const params = {
					pageNum: this.pageNum,
					pageSize: this.pageSize
				}
				
				// 地区筛选
				if (this.selectedProvince) {
					params.province = this.selectedProvince
				}
				if (this.selectedCity) {
					params.city = this.selectedCity
				}
				if (this.selectedDistrict) {
					params.district = this.selectedDistrict
				}
				
				// 租赁期限筛选：0-全部 1-短期 2-长期
				if (this.activeTab === 1) {
					params.rentPeriod = '短期'
				} else if (this.activeTab === 2) {
					params.rentPeriod = '长期'
				}
				
				console.log('【首页】搜索参数:', params)
				const res = await api.house.search(params)
				
				if (res.code === 200) {
					const records = res.data.records || []
					console.log('【首页】加载房源数据:', records.length, '条')
					// 处理每个房源的封面图
					records.forEach(house => {
						console.log('【首页】房源图片原始数据:', house.houseId, house.images)
						let imageList = []
						if (house.images) {
							if (typeof house.images === 'string') {
								try {
									imageList = JSON.parse(house.images)
								} catch (e) {
									// 如果不是JSON，直接作为URL使用
									if (house.images.startsWith('http')) {
										imageList = [house.images]
									}
								}
							} else if (Array.isArray(house.images)) {
								imageList = house.images
							}
						}
						house.coverImage = imageList.length > 0 ? imageList[0] : '/static/logo.png'
						console.log('【首页】处理后封面图:', house.houseId, house.coverImage)
					})
					this.houseList = this.pageNum === 1 ? records : [...this.houseList, ...records]
					this.noMore = records.length < this.pageSize
				}
			} catch (e) {
				console.error('加载房源失败:', e)
			} finally {
				this.loading = false
			}
		},
		
		switchTab(index) {
			this.activeTab = index
			this.pageNum = 1
			this.noMore = false
			this.houseList = []
			this.loadHouses()
		},
		
		loadMore() {
			this.pageNum++
			this.loadHouses()
		},
		
		viewDetail(houseId) {
			uni.navigateTo({
				url: `/pages/house/detail/detail?id=${houseId}`
			})
		},
		
		goSearch() {
			uni.navigateTo({
				url: '/pages/house/search/search'
			})
		},
		
		switchToChat() {
			uni.switchTab({
				url: '/pages/chat/chat'
			})
		},
		
		switchToProfile() {
			uni.switchTab({
				url: '/pages/profile/profile'
			})
		}
	}
}
</script>

<style scoped>
.home-page {
	min-height: 100vh;
	background: #F7F9FC;
}

/* 顶部栏 */
.top-bar {
	display: flex;
	align-items: center;
	gap: 20rpx;
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	padding: 24rpx 30rpx;
	position: sticky;
	top: 0;
	z-index: 100;
	box-shadow: 0 4rpx 20rpx rgba(255, 107, 53, 0.2);
}

.location-selector {
	display: flex;
	align-items: center;
	padding: 16rpx 24rpx;
	background: rgba(255, 255, 255, 0.25);
	backdrop-filter: blur(10rpx);
	border-radius: 48rpx;
	min-width: 180rpx;
	border: 1rpx solid rgba(255, 255, 255, 0.3);
}

.location-icon {
	font-size: 28rpx;
	margin-right: 8rpx;
	filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}

.location-text {
	font-size: 26rpx;
	color: #FFFFFF;
	font-weight: 600;
	max-width: 140rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.location-arrow {
	font-size: 20rpx;
	color: rgba(255, 255, 255, 0.9);
	margin-left: 8rpx;
}

.search-box {
	flex: 1;
	display: flex;
	align-items: center;
	background: rgba(255, 255, 255, 0.95);
	padding: 18rpx 28rpx;
	border-radius: 48rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
	transition: all 0.3s ease;
}

.search-box:active {
	background: #FFFFFF;
	transform: scale(0.98);
}

.search-icon {
	margin-right: 12rpx;
	font-size: 28rpx;
	filter: grayscale(0.3);
}

.search-placeholder {
	color: #8B95A5;
	font-size: 28rpx;
}

/* 小区选择器弹窗 */
.picker-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(44, 62, 80, 0.6);
	backdrop-filter: blur(8rpx);
	z-index: 200;
	animation: fadeIn 0.3s ease;
}

.community-picker {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #FFFFFF;
	border-radius: 40rpx 40rpx 0 0;
	z-index: 201;
	transform: translateY(100%);
	transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
	max-height: 70vh;
	display: flex;
	flex-direction: column;
	box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.12);
}

.community-picker.show {
	transform: translateY(0);
}

.picker-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 36rpx 40rpx;
	border-bottom: 1rpx solid #F2F6FC;
	position: relative;
}

.picker-header::before {
	content: '';
	position: absolute;
	top: 16rpx;
	left: 50%;
	transform: translateX(-50%);
	width: 80rpx;
	height: 8rpx;
	background: #E4E7ED;
	border-radius: 4rpx;
}

.picker-title {
	font-size: 36rpx;
	font-weight: 700;
	color: #2C3E50;
}

.picker-close {
	font-size: 52rpx;
	color: #8B95A5;
	line-height: 1;
	transition: color 0.3s ease;
}

.picker-close:active {
	color: #5A6C7D;
}

.picker-search {
	padding: 20rpx 30rpx;
}

.picker-input {
	background: #f5f7fa;
	padding: 20rpx 30rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
}

.picker-list {
	flex: 1;
	max-height: 50vh;
}

.picker-item {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	padding: 28rpx 40rpx;
	border-bottom: 1rpx solid #F7F9FC;
	transition: all 0.3s ease;
}

.picker-item:active {
	background: #F7F9FC;
}

.picker-item.active {
	background: linear-gradient(90deg, #FFF5F0, #FFFFFF);
	border-left: 6rpx solid #FF6B35;
	padding-left: 34rpx;
}

.picker-item text:first-child {
	font-size: 32rpx;
	color: #2C3E50;
	font-weight: 500;
}

.picker-item.active text:first-child {
	color: #FF6B35;
	font-weight: 600;
}

.picker-item-address {
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
}

.picker-item-count {
	font-size: 24rpx;
	color: #409eff;
	margin-left: auto;
}

/* 三级联动选择tabs */
.region-tabs {
	display: flex;
	padding: 24rpx 40rpx;
	border-bottom: 1rpx solid #F2F6FC;
	background: #FAFBFC;
}

.region-tab {
	padding: 16rpx 28rpx;
	margin-right: 24rpx;
	font-size: 30rpx;
	color: #8B95A5;
	border-bottom: 4rpx solid transparent;
	transition: all 0.3s ease;
	font-weight: 500;
}

.region-tab.active {
	color: #FF6B35;
	border-bottom-color: #FF6B35;
	font-weight: 700;
}

.filter-tabs {
	display: flex;
	background: #FFFFFF;
	padding: 24rpx 30rpx;
	white-space: nowrap;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.filter-tabs .tab-item {
	display: inline-block;
	padding: 16rpx 36rpx;
	margin-right: 24rpx;
	background: #F7F9FC;
	border-radius: 48rpx;
	font-size: 30rpx;
	color: #5A6C7D;
	font-weight: 500;
	transition: all 0.3s ease;
	border: 2rpx solid transparent;
}

.filter-tabs .tab-item.active {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
	transform: translateY(-2rpx);
}

.house-list {
	height: calc(100vh - 300rpx);
	padding: 20rpx 24rpx;
	background: #F5F7FA;
}

.house-grid {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
}

.house-card {
	width: calc(50% - 12rpx);
	background: #FFFFFF;
	border-radius: 24rpx;
	margin-bottom: 24rpx;
	overflow: hidden;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.house-card:active {
	transform: scale(0.98);
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.12);
}

/* 图片容器 */
.card-image-wrapper {
	position: relative;
	width: 100%;
	height: 240rpx;
	overflow: hidden;
}

.card-image {
	width: 100%;
	height: 100%;
	transition: transform 0.5s ease;
}

.house-card:active .card-image {
	transform: scale(1.05);
}

/* 价格标签 */
.price-tag {
	position: absolute;
	bottom: 12rpx;
	left: 12rpx;
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	display: flex;
	align-items: baseline;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.4);
}

.price-tag .price-num {
	font-size: 32rpx;
	font-weight: 800;
	color: #FFFFFF;
	letter-spacing: -1rpx;
}

.price-tag .price-unit {
	font-size: 20rpx;
	color: rgba(255, 255, 255, 0.9);
	margin-left: 4rpx;
}

/* 浏览数徽章 */
.view-badge {
	position: absolute;
	top: 12rpx;
	right: 12rpx;
	background: rgba(0, 0, 0, 0.5);
	padding: 6rpx 12rpx;
	border-radius: 16rpx;
	backdrop-filter: blur(4rpx);
}

.view-badge text {
	font-size: 20rpx;
	color: #FFFFFF;
}

/* 卡片内容 */
.card-content {
	padding: 20rpx;
}

.card-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #2C3E50;
	line-height: 1.4;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
	text-overflow: ellipsis;
	min-height: 78rpx;
}

.card-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 8rpx;
	margin: 12rpx 0;
}

.card-tag {
	font-size: 20rpx;
	color: #5A6C7D;
	background: #F0F4F8;
	padding: 6rpx 12rpx;
	border-radius: 6rpx;
	font-weight: 500;
}

.card-location {
	display: flex;
	align-items: center;
	margin-top: 8rpx;
}

.card-location .location-icon {
	font-size: 24rpx;
	margin-right: 6rpx;
}

.card-location .location-text {
	font-size: 22rpx;
	color: #8B95A5;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

/* 加载状态 */
.load-more, .no-more {
	width: 100%;
	text-align: center;
	padding: 40rpx;
	color: #8B95A5;
	font-size: 26rpx;
}

.load-more {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
}

.loading-spinner {
	width: 32rpx;
	height: 32rpx;
	border: 4rpx solid #E4E7ED;
	border-top-color: #FF6B35;
	border-radius: 50%;
	animation: spin 0.8s linear infinite;
}

@keyframes spin {
	to { transform: rotate(360deg); }
}

.tabbar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	background: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
	padding-bottom: env(safe-area-inset-bottom);
}

.tabbar .tab-item {
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

.tabbar .tab-item.active .tab-text {
	color: #409eff;
}
</style>

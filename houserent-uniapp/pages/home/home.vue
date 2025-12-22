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
				整租
			</view>
			<view class="tab-item" :class="{ active: activeTab === 2 }" @click="switchTab(2)">
				合租
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
			<view class="house-card" v-for="house in houseList" :key="house.houseId" @click="viewDetail(house.houseId)">
				<image class="house-img" :src="house.coverImage || '/static/logo.png'" mode="aspectFill" @error="house.coverImage = '/static/logo.png'"></image>
				<view class="house-info">
					<view class="house-title">{{ house.title }}</view>
					<view class="house-tags">
						<text class="tag">{{ house.area }}㎡</text>
						<text class="tag">{{ house.houseType || house.roomType }}</text>
						<text class="tag" v-if="house.floor">{{ house.floor }}层</text>
						<text class="tag" v-if="house.orientation">{{ house.orientation }}</text>
					</view>
					<view class="house-location">📍 {{ house.communityName }}</view>
					<view class="house-bottom">
						<view class="house-price">
							<text class="price-num">{{ house.rentPrice }}</text>
							<text class="price-unit">元/月</text>
						</view>
						<view class="house-stats" v-if="house.viewCount">
							<text class="stats-text">{{ house.viewCount }}次浏览</text>
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
				
				// 租赁类型筛选：0-全部 1-整租 2-合租
				if (this.activeTab === 1) {
					params.rentType = 'whole'
				} else if (this.activeTab === 2) {
					params.rentType = 'shared'
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
	background: #f5f7fa;
}

/* 顶部栏 */
.top-bar {
	display: flex;
	align-items: center;
	gap: 20rpx;
	background: #fff;
	padding: 20rpx;
	position: sticky;
	top: 0;
	z-index: 100;
}

.location-selector {
	display: flex;
	align-items: center;
	padding: 16rpx 20rpx;
	background: #f5f7fa;
	border-radius: 40rpx;
	min-width: 180rpx;
}

.location-icon {
	font-size: 28rpx;
	margin-right: 8rpx;
}

.location-text {
	font-size: 26rpx;
	color: #333;
	max-width: 140rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.location-arrow {
	font-size: 20rpx;
	color: #999;
	margin-left: 8rpx;
}

.search-box {
	flex: 1;
	display: flex;
	align-items: center;
	background: #f5f7fa;
	padding: 16rpx 24rpx;
	border-radius: 40rpx;
}

.search-icon {
	margin-right: 12rpx;
	font-size: 28rpx;
}

.search-placeholder {
	color: #999;
	font-size: 26rpx;
}

/* 小区选择器弹窗 */
.picker-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0,0,0,0.5);
	z-index: 200;
}

.community-picker {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #fff;
	border-radius: 30rpx 30rpx 0 0;
	z-index: 201;
	transform: translateY(100%);
	transition: transform 0.3s ease;
	max-height: 70vh;
	display: flex;
	flex-direction: column;
}

.community-picker.show {
	transform: translateY(0);
}

.picker-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #eee;
}

.picker-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.picker-close {
	font-size: 48rpx;
	color: #999;
	line-height: 1;
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
	padding: 24rpx 30rpx;
	border-bottom: 1rpx solid #f5f5f5;
}

.picker-item.active {
	background: #e6f7ff;
}

.picker-item text:first-child {
	font-size: 30rpx;
	color: #333;
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
	padding: 20rpx 30rpx;
	border-bottom: 1rpx solid #eee;
	background: #fafafa;
}

.region-tab {
	padding: 12rpx 24rpx;
	margin-right: 20rpx;
	font-size: 28rpx;
	color: #666;
	border-bottom: 4rpx solid transparent;
}

.region-tab.active {
	color: #409eff;
	border-bottom-color: #409eff;
}

.filter-tabs {
	display: flex;
	background: #fff;
	padding: 20rpx;
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

.house-list {
	height: calc(100vh - 300rpx);
}

.house-card {
	display: flex;
	background: #fff;
	margin: 20rpx;
	padding: 24rpx;
	border-radius: 16rpx;
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
}

.tag {
	font-size: 24rpx;
	color: #666;
	background: #f5f7fa;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
}

.house-location {
	font-size: 24rpx;
	color: #999;
}

.house-bottom {
	display: flex;
	justify-content: space-between;
	align-items: center;
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

.house-stats {
	display: flex;
	align-items: center;
}

.stats-text {
	font-size: 22rpx;
	color: #999;
}

.load-more, .no-more {
	text-align: center;
	padding: 40rpx;
	color: #999;
	font-size: 28rpx;
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

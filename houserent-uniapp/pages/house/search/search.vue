<template>
	<view class="search-page">
		<!-- 搜索框 -->
		<view class="search-bar">
			<view class="search-input-box">
				<text class="search-icon">🔍</text>
				<input class="search-input" v-model="keyword" placeholder="搜索小区/地铁站/商圈" @confirm="doSearch" />
				<text class="clear-icon" v-if="keyword" @click="clearKeyword">×</text>
			</view>
			<view class="cancel-btn" @click="goBack">取消</view>
		</view>
		
		<!-- 筛选条件 -->
		<view class="filter-section">
			<!-- 小区筛选 -->
			<view class="filter-row">
				<view class="filter-label">小区</view>
				<scroll-view class="filter-options" scroll-x>
					<view class="filter-option" :class="{ active: filters.communityId === '' }" @click="selectCommunity('')">
						不限
					</view>
					<view class="filter-option" :class="{ active: filters.communityId === item.communityId }" 
						v-for="item in communityList" :key="item.communityId" @click="selectCommunity(item.communityId)">
						{{ item.communityName }}
					</view>
				</scroll-view>
			</view>
			
			<view class="filter-row">
				<view class="filter-label">户型</view>
				<scroll-view class="filter-options" scroll-x>
					<view class="filter-option" :class="{ active: filters.roomType === '' }" @click="selectRoomType('')">
						不限
					</view>
					<view class="filter-option" :class="{ active: filters.roomType === item }" 
						v-for="item in roomTypes" :key="item" @click="selectRoomType(item)">
						{{ item }}
					</view>
				</scroll-view>
			</view>
			
			<view class="filter-row">
				<view class="filter-label">面积</view>
				<scroll-view class="filter-options" scroll-x>
					<view class="filter-option" :class="{ active: filters.areaRange === '' }" @click="selectAreaRange('')">
						不限
					</view>
					<view class="filter-option" :class="{ active: filters.areaRange === item.value }" 
						v-for="item in areaRanges" :key="item.value" @click="selectAreaRange(item.value)">
						{{ item.label }}
					</view>
				</scroll-view>
			</view>
			
			<view class="filter-row">
				<view class="filter-label">租金</view>
				<scroll-view class="filter-options" scroll-x>
					<view class="filter-option" :class="{ active: filters.priceRange === '' }" @click="selectPriceRange('')">
						不限
					</view>
					<view class="filter-option" :class="{ active: filters.priceRange === item.value }" 
						v-for="item in priceRanges" :key="item.value" @click="selectPriceRange(item.value)">
						{{ item.label }}
					</view>
				</scroll-view>
			</view>
		</view>
		
		<!-- 搜索结果 -->
		<scroll-view class="result-list" scroll-y>
			<view class="house-card" v-for="house in searchResults" :key="house.houseId" @click="viewDetail(house.houseId)">
				<image class="house-img" :src="house.coverImage" mode="aspectFill"></image>
				<view class="house-info">
					<view class="house-title">{{ house.title }}</view>
					<view class="house-tags">
						<text class="tag">{{ house.area }}㎡</text>
						<text class="tag">{{ house.roomType }}</text>
					</view>
					<view class="house-location">{{ house.communityName }}</view>
					<view class="house-price">
						<text class="price-num">{{ house.rentPrice }}</text>
						<text class="price-unit">/月</text>
					</view>
				</view>
			</view>
			
			<view class="empty" v-if="searched && searchResults.length === 0">
				<text class="empty-icon">🔍</text>
				<text class="empty-text">未找到相关房源</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			keyword: '',
			searched: false,
			filters: {
				roomType: '',
				areaRange: '',
				priceRange: '',
				communityId: ''
			},
			communityList: [],
			roomTypes: ['1室', '2室', '3室', '4室', '5室及以上'],
			areaRanges: [
				{ label: '50㎡以下', value: '0-50' },
				{ label: '50-80㎡', value: '50-80' },
				{ label: '80-100㎡', value: '80-100' },
				{ label: '100-150㎡', value: '100-150' },
				{ label: '150㎡以上', value: '150-999' }
			],
			priceRanges: [
				{ label: '1000以下', value: '0-1000' },
				{ label: '1000-2000', value: '1000-2000' },
				{ label: '2000-3000', value: '2000-3000' },
				{ label: '3000-5000', value: '3000-5000' },
				{ label: '5000以上', value: '5000-99999' }
			],
			searchResults: []
		}
	},
	
	onLoad() {
		this.loadCommunities()
	},
	
	// 下拉刷新 - 必须放在methods外面作为页面生命周期函数
	onPullDownRefresh() {
		console.log('触发下拉刷新')
		this.doSearch().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadCommunities() {
			try {
				const res = await api.community.getList()
				if (res.code === 200) {
					this.communityList = res.data || []
				}
			} catch (e) {
				console.error('加载小区列表失败:', e)
			}
		},
		
		selectCommunity(communityId) {
			this.filters.communityId = communityId
			this.doSearch()
		},
		
		async doSearch() {
			this.searched = true
			try {
				const params = {
					keyword: this.keyword,
					houseType: this.filters.roomType,
					areaRange: this.filters.areaRange,
					priceRange: this.filters.priceRange,
					communityId: this.filters.communityId || null,
					pageNum: 1,
					pageSize: 100
				}
				
				const res = await api.house.search(params)
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
					this.searchResults = records
				}
			} catch (e) {
				console.error('搜索失败:', e)
			}
		},
		
		selectRoomType(type) {
			this.filters.roomType = type
			this.doSearch()
		},
		
		selectAreaRange(range) {
			this.filters.areaRange = range
			this.doSearch()
		},
		
		selectPriceRange(range) {
			this.filters.priceRange = range
			this.doSearch()
		},
		
		clearKeyword() {
			this.keyword = ''
		},
		
		viewDetail(houseId) {
			uni.navigateTo({
				url: `/pages/house/detail/detail?id=${houseId}`
			})
		},
		
		goBack() {
			uni.navigateBack()
		}
	}
}
</script>

<style scoped>
.search-page {
	min-height: 100vh;
	background: #f5f7fa;
}

.search-bar {
	display: flex;
	align-items: center;
	gap: 20rpx;
	background: #fff;
	padding: 20rpx;
}

.search-input-box {
	flex: 1;
	display: flex;
	align-items: center;
	background: #f5f7fa;
	padding: 16rpx 24rpx;
	border-radius: 50rpx;
}

.search-icon {
	font-size: 32rpx;
	margin-right: 16rpx;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
}

.clear-icon {
	font-size: 40rpx;
	color: #999;
	padding: 0 12rpx;
}

.cancel-btn {
	font-size: 28rpx;
	color: #409eff;
}

.filter-section {
	background: #fff;
	padding: 20rpx;
}

.filter-row {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.filter-label {
	width: 100rpx;
	font-size: 28rpx;
	color: #666;
}

.filter-options {
	flex: 1;
	white-space: nowrap;
}

.filter-option {
	display: inline-block;
	padding: 12rpx 24rpx;
	margin-right: 16rpx;
	background: #f5f7fa;
	border-radius: 40rpx;
	font-size: 26rpx;
	color: #333;
}

.filter-option.active {
	background: #409eff;
	color: #fff;
}

.result-list {
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
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.house-tags {
	display: flex;
	gap: 12rpx;
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

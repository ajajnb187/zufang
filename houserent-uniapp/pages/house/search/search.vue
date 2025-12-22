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
	background: #F7F9FC;
}

.search-bar {
	display: flex;
	align-items: center;
	gap: 20rpx;
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	padding: 24rpx 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(255, 107, 53, 0.2);
}

.search-input-box {
	flex: 1;
	display: flex;
	align-items: center;
	background: rgba(255, 255, 255, 0.95);
	padding: 18rpx 28rpx;
	border-radius: 48rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.search-icon {
	font-size: 32rpx;
	margin-right: 16rpx;
	filter: grayscale(0.3);
}

.search-input {
	flex: 1;
	font-size: 30rpx;
	color: #2C3E50;
}

.clear-icon {
	font-size: 44rpx;
	color: #8B95A5;
	padding: 0 12rpx;
	transition: color 0.3s ease;
}

.clear-icon:active {
	color: #5A6C7D;
}

.cancel-btn {
	font-size: 30rpx;
	color: #FFFFFF;
	font-weight: 600;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.filter-section {
	background: #FFFFFF;
	padding: 28rpx 30rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.filter-row {
	display: flex;
	align-items: center;
	margin-bottom: 24rpx;
}

.filter-row:last-child {
	margin-bottom: 0;
}

.filter-label {
	width: 110rpx;
	font-size: 30rpx;
	color: #2C3E50;
	font-weight: 700;
}

.filter-options {
	flex: 1;
	white-space: nowrap;
}

.filter-option {
	display: inline-block;
	padding: 14rpx 28rpx;
	margin-right: 20rpx;
	background: #F7F9FC;
	border-radius: 48rpx;
	font-size: 28rpx;
	color: #5A6C7D;
	font-weight: 500;
	transition: all 0.3s ease;
	border: 2rpx solid transparent;
}

.filter-option.active {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
	transform: translateY(-2rpx);
}

.result-list {
	height: calc(100vh - 300rpx);
	padding: 0 0 20rpx 0;
}

.house-card {
	display: flex;
	background: #FFFFFF;
	margin: 24rpx 30rpx;
	padding: 20rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.house-card::before {
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

.house-card:active {
	transform: translateY(-4rpx);
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.house-card:active::before {
	opacity: 1;
}

.house-img {
	width: 220rpx;
	height: 165rpx;
	border-radius: 16rpx;
	margin-right: 24rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.house-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.house-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #2C3E50;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	margin-bottom: 8rpx;
}

.house-tags {
	display: flex;
	gap: 12rpx;
	margin: 12rpx 0;
}

.tag {
	font-size: 22rpx;
	color: #5A6C7D;
	background: linear-gradient(135deg, #F7F9FC, #EEF2F6);
	padding: 6rpx 16rpx;
	border-radius: 8rpx;
	font-weight: 500;
	border: 1rpx solid #E4E7ED;
}

.house-location {
	font-size: 24rpx;
	color: #8B95A5;
	margin: 8rpx 0;
}

.house-price {
	color: #FF6B35;
}

.price-num {
	font-size: 40rpx;
	font-weight: 800;
	letter-spacing: -1rpx;
}

.price-unit {
	font-size: 24rpx;
	font-weight: 500;
	opacity: 0.9;
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
	filter: grayscale(0.5);
}

.empty-text {
	font-size: 32rpx;
	color: #8B95A5;
	font-weight: 500;
}
</style>

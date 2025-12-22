<template>
	<view class="favorite-page">
		<scroll-view class="house-list" scroll-y>
			<view class="house-card" v-for="house in favoriteList" :key="house.houseId" @click="viewDetail(house.houseId)">
				<image class="house-img" :src="getCoverImage(house)" mode="aspectFill"></image>
				<view class="house-info">
					<view class="house-title">{{ house.title }}</view>
					<view class="house-tags">
						<text class="tag">{{ house.area }}㎡</text>
						<text class="tag">{{ house.roomType }}</text>
					</view>
					<view class="house-bottom">
						<view class="house-price">
							<text class="price-num">{{ house.rentPrice }}</text>
							<text class="price-unit">/月</text>
						</view>
						<view class="remove-btn" @click.stop="removeFavorite(house.houseId)">
							<text>❤️</text>
						</view>
					</view>
				</view>
			</view>
			
			<view class="empty" v-if="favoriteList.length === 0">
				<text class="empty-icon">💔</text>
				<text class="empty-text">还没有收藏房源</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			favoriteList: []
		}
	},
	
	onShow() {
		this.loadFavorites()
	},
	
	// 下拉刷新 - 必须放在methods外面作为页面生命周期函数
	onPullDownRefresh() {
		console.log('触发下拉刷新')
		this.loadFavorites().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadFavorites() {
			try {
				const res = await api.house.getFavorites()
				if (res.code === 200) {
					this.favoriteList = res.data || []
				}
			} catch (e) {
				console.error('加载收藏失败:', e)
			}
		},
		
		async removeFavorite(houseId) {
			try {
				await api.house.unfavorite(houseId)
				uni.showToast({ title: '已取消收藏', icon: 'success' })
				this.loadFavorites()
			} catch (e) {
				console.error('取消收藏失败:', e)
			}
		},
		
		viewDetail(houseId) {
			uni.navigateTo({
				url: `/pages/house/detail/detail?id=${houseId}`
			})
		},
		
		getCoverImage(house) {
			// 优先使用coverImage，否则从 images JSON数组获取第一张
			if (house.coverImage) {
				return house.coverImage
			}
			if (house.images) {
				try {
					const imgs = typeof house.images === 'string' 
						? JSON.parse(house.images) 
						: house.images
					if (Array.isArray(imgs) && imgs.length > 0) {
						return imgs[0]
					}
				} catch (e) {
					console.error('解析图片失败:', e)
				}
			}
			return '/static/images/default-house.png'
		}
	}
}
</script>

<style scoped>
.favorite-page {
	min-height: 100vh;
	background: #F7F9FC;
}

.house-list {
	height: 100vh;
	padding: 20rpx 0;
}

.house-card {
	display: flex;
	background: #FFFFFF;
	margin: 0 30rpx 24rpx;
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
	font-size: 24rpx;
	color: #666;
	background: #f5f7fa;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
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

.remove-btn {
	font-size: 40rpx;
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

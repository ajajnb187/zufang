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
	background: #f5f7fa;
}

.house-list {
	height: 100vh;
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

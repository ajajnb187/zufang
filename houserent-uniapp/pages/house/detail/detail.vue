<template>
	<view class="detail-page">
		<!-- 房源图片轮播 -->
		<swiper class="house-swiper" indicator-dots>
			<swiper-item v-for="(img, index) in houseImages" :key="index">
				<image class="swiper-img" :src="img" mode="aspectFill" @click="previewImage(index)"></image>
			</swiper-item>
		</swiper>
		
		<!-- 房源信息 -->
		<view class="house-info">
			<view class="price-row">
				<view class="price">
					<text class="price-num">{{ houseInfo.rentPrice }}</text>
					<text class="price-unit">元/月</text>
				</view>
				<view class="favorite-btn" @click="toggleFavorite">
					<text class="fav-icon" :class="{ favorited: isFavorited }">{{ isFavorited ? '❤️' : '🤍' }}</text>
					<text class="fav-text">{{ isFavorited ? '已收藏' : '收藏' }}</text>
				</view>
			</view>
			
			<view class="title">{{ houseInfo.title }}</view>
			
			<view class="tags">
				<text class="tag">{{ houseInfo.area }}㎡</text>
				<text class="tag">{{ houseInfo.houseType || houseInfo.roomType }}</text>
				<text class="tag">{{ houseInfo.floor }}/{{ houseInfo.totalFloor }}层</text>
				<text class="tag">{{ houseInfo.orientation }}</text>
			</view>
			
			<view class="address">
				<text class="icon">📍</text>
				<text>{{ houseInfo.communityName }}</text>
			</view>
			
			<!-- 统计信息 -->
			<view class="stats-row">
				<text class="stat-item">👁️ {{ houseInfo.viewCount || 0 }}次浏览</text>
				<text class="stat-item">❤️ {{ houseInfo.favoriteCount || 0 }}人收藏</text>
			</view>
		</view>
		
		<!-- 房东信息 -->
		<view class="landlord-info" v-if="landlordInfo">
			<image class="avatar" :src="landlordInfo.avatarUrl"></image>
			<view class="info">
				<view class="name">{{ landlordInfo.nickname }}</view>
				<view class="desc">房东</view>
			</view>
			<view class="chat-btn" @click="chatWithLandlord">
				<text>💬 咨询</text>
			</view>
		</view>
		
		<!-- 房源基本信息 -->
		<view class="section">
			<view class="section-title">基本信息</view>
			<view class="info-grid">
				<view class="info-item">
					<text class="info-label">户型</text>
					<text class="info-value">{{ houseInfo.houseType || houseInfo.roomType || '-' }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">面积</text>
					<text class="info-value">{{ houseInfo.area }}㎡</text>
				</view>
				<view class="info-item">
					<text class="info-label">楼层</text>
					<text class="info-value">{{ houseInfo.floor }}/{{ houseInfo.totalFloor }}层</text>
				</view>
				<view class="info-item">
					<text class="info-label">朝向</text>
					<text class="info-value">{{ houseInfo.orientation || '-' }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">装修</text>
					<text class="info-value">{{ decorationText }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">付款方式</text>
					<text class="info-value">{{ houseInfo.paymentMethod || '-' }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">租期</text>
					<text class="info-value">{{ houseInfo.rentPeriod || '-' }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">联系电话</text>
					<text class="info-value phone" @click="callPhone">{{ houseInfo.contactPhone || '-' }}</text>
				</view>
			</view>
		</view>
		
		<!-- 配套设施 -->
		<view class="section" v-if="facilitiesList && facilitiesList.length > 0">
			<view class="section-title">配套设施</view>
			<view class="facilities-grid">
				<view class="facility-item" v-for="(item, index) in facilitiesList" :key="index">
					<text class="facility-icon">{{ getFacilityIcon(item) }}</text>
					<text class="facility-name">{{ item }}</text>
				</view>
			</view>
		</view>
		
		<!-- 房源描述 -->
		<view class="section">
			<view class="section-title">房源描述</view>
			<view class="description">{{ houseInfo.description || '暂无描述' }}</view>
		</view>
		
		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<view class="action-icons">
				<button class="icon-btn" open-type="share">
					<text class="icon">📤</text>
				</button>
				<view class="icon-btn" @click="reportHouse">
					<text class="icon">🚨</text>
				</view>
			</view>
			<view class="action-btns">
				<view class="btn btn-secondary" @click="chatWithLandlord">咨询</view>
				<view class="btn btn-primary" @click="applyRent">预约看房</view>
			</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			houseId: '',
			houseInfo: {},
			houseImages: [],
			landlordInfo: null,
			isFavorited: false,
			facilitiesList: []
		}
	},
	
	computed: {
		// 装修程度文本转换
		decorationText() {
			const map = {
				'rough': '毛坯',
				'simple': '简装',
				'fine': '精装',
				'luxury': '豪装'
			}
			return map[this.houseInfo.decoration] || this.houseInfo.decoration || '-'
		}
	},
	
	onLoad(options) {
		this.houseId = options.id
		this.loadHouseDetail()
	},
	
	
	// 分享给好友（微信小程序必需的生命周期函数）
	onShareAppMessage() {
		return {
			title: this.houseInfo.title || '优质房源推荐',
			path: `/pages/house/detail/detail?id=${this.houseId}`,
			imageUrl: this.houseImages[0] || '/static/logo.png'
		}
	},
	
	// 分享到朋友圈（微信小程序）
	onShareTimeline() {
		return {
			title: `${this.houseInfo.rentPrice}元/月 | ${this.houseInfo.title || '优质房源'}`,
			query: `id=${this.houseId}`,
			imageUrl: this.houseImages[0] || '/static/logo.png'
		}
	},
	
	methods: {
		async loadHouseDetail() {
			try {
				const res = await api.house.getDetail(this.houseId)
				if (res.code === 200) {
					this.houseInfo = res.data
					
					// 处理图片数组 - 后端已经返回了所有图片
					if (res.data.images && typeof res.data.images === 'string') {
						try {
							this.houseImages = JSON.parse(res.data.images)
						} catch (e) {
							this.houseImages = [res.data.images]
						}
					} else if (res.data.images && Array.isArray(res.data.images)) {
						this.houseImages = res.data.images
					}
					
					if (!this.houseImages || this.houseImages.length === 0) {
						this.houseImages = ['/static/logo.png']
					}
					
					// 处理配套设施
					if (res.data.facilities) {
						try {
							this.facilitiesList = typeof res.data.facilities === 'string' 
								? JSON.parse(res.data.facilities) 
								: res.data.facilities
						} catch (e) {
							this.facilitiesList = []
						}
					}
					
					// 房东信息（后端已返回完整房东信息）
					this.landlordInfo = res.data.landlord
					
					// 收藏状态（后端已返回）
					this.isFavorited = res.data.isFavorited || false
				}
			} catch (e) {
				console.error('加载房源详情失败:', e)
			}
		},
		
		async toggleFavorite() {
			try {
				if (this.isFavorited) {
					const res = await api.house.unfavorite(this.houseId)
					if (res.code === 200) {
						this.isFavorited = false
						uni.showToast({ title: '已取消收藏', icon: 'success' })
					}
				} else {
					const res = await api.house.favorite(this.houseId)
					if (res.code === 200) {
						this.isFavorited = true
						uni.showToast({ title: '收藏成功', icon: 'success' })
					}
				}
			} catch (e) {
				console.error('收藏操作失败:', e)
				uni.showToast({ title: '请先登录', icon: 'none' })
			}
		},
		
		chatWithLandlord() {
			if (!this.landlordInfo || !this.landlordInfo.userId) {
				uni.showToast({ title: '无法获取房东信息', icon: 'none' })
				return
			}
			uni.navigateTo({
				url: `/pages/chat/chat-detail/chat-detail?userId=${this.landlordInfo.userId}&name=${this.landlordInfo.nickname}`
			})
		},
		
		applyRent() {
			uni.navigateTo({
				url: `/pages/appointment/create/create?houseId=${this.houseId}&houseTitle=${encodeURIComponent(this.houseInfo.title)}`
			})
		},
		
				
		// 图片预览放大查看
		previewImage(index) {
			uni.previewImage({
				urls: this.houseImages,
				current: index,
				longPressActions: {
					itemList: ['保存图片'],
					success: (data) => {
						if (data.tapIndex === 0) {
							uni.saveImageToPhotosAlbum({
								filePath: this.houseImages[data.index],
								success: () => {
									uni.showToast({ title: '保存成功', icon: 'success' })
								},
								fail: () => {
									uni.showToast({ title: '保存失败', icon: 'none' })
								}
							})
						}
					}
				}
			})
		},
		
		// 拨打电话
		callPhone() {
			if (this.houseInfo.contactPhone) {
				uni.makePhoneCall({
					phoneNumber: this.houseInfo.contactPhone,
					fail: () => {
						uni.showToast({ title: '拨打失败', icon: 'none' })
					}
				})
			}
		},
		
		// 获取设施图标
		getFacilityIcon(name) {
			const iconMap = {
				'空调': '❄️', '冰箱': '🧊', '洗衣机': '🧺', '热水器': '🚿',
				'电视': '📺', 'WiFi': '📶', '宽带': '🌐', '床': '🛏️',
				'衣柜': '🚪', '沙发': '🛋️', '餐桌': '🪑', '燃气': '🔥',
				'暖气': '♨️', '阳台': '🏠', '电梯': '🛗', '停车位': '🅿️'
			}
			return iconMap[name] || '✅'
		},
		
		reportHouse() {
			const reasonTypes = ['虚假房源', '重复发布', '已出租', '信息有误', '其他']
			uni.showActionSheet({
				itemList: reasonTypes,
				success: (res) => {
					const selectedReasonType = reasonTypes[res.tapIndex]
					// 使用输入框获取详细描述
					uni.showModal({
						title: '举报详情',
						placeholderText: '请输入详细举报理由（选填）',
						editable: true,
						success: async (modalRes) => {
							if (modalRes.confirm) {
								try {
									// 后端期望的字段：reportType, targetId, reasonType, reasonDetail
									const submitRes = await api.report.submit({
										reportType: 'house',
										targetId: Number(this.houseId),
										reasonType: selectedReasonType,
										reasonDetail: modalRes.content || ''
									})
									if (submitRes.code === 200) {
										uni.showToast({ title: '举报成功', icon: 'success' })
									} else {
										uni.showToast({ title: submitRes.message || '举报失败', icon: 'none' })
									}
								} catch (e) {
									console.error('举报失败:', e)
									uni.showToast({ title: '举报失败，请先登录', icon: 'none' })
								}
							}
						}
					})
				}
			})
		}
	}
}
</script>

<style scoped>
.detail-page {
	min-height: 100vh;
	background: #F7F9FC;
	padding-bottom: 180rpx;
}

.house-swiper {
	width: 100%;
	height: 550rpx;
	position: relative;
}

.swiper-img {
	width: 100%;
	height: 100%;
}

.house-info {
	background: #FFFFFF;
	padding: 36rpx;
	margin: -40rpx 24rpx 24rpx;
	border-radius: 32rpx;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
	position: relative;
	z-index: 10;
}

.price-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
}

.price {
	color: #FF6B35;
}

.price-num {
	font-size: 56rpx;
	font-weight: 800;
	letter-spacing: -1rpx;
}

.price-unit {
	font-size: 28rpx;
	font-weight: 500;
	opacity: 0.9;
}

.favorite-btn {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
	padding: 12rpx 20rpx;
	border-radius: 48rpx;
	background: #FFF5F0;
	transition: all 0.3s ease;
}

.favorite-btn:active {
	transform: scale(0.95);
}

.fav-icon {
	font-size: 48rpx;
	transition: transform 0.3s;
	filter: drop-shadow(0 2rpx 4rpx rgba(255, 107, 53, 0.2));
}

.fav-icon.favorited {
	transform: scale(1.2);
	animation: heartBeat 0.5s ease;
}

@keyframes heartBeat {
	0%, 100% { transform: scale(1.2); }
	50% { transform: scale(1.4); }
}

.fav-text {
	font-size: 22rpx;
	color: #FF6B35;
	font-weight: 600;
}

.title {
	font-size: 40rpx;
	font-weight: 700;
	color: #2C3E50;
	margin-bottom: 24rpx;
	line-height: 1.4;
}

.tags {
	display: flex;
	gap: 16rpx;
	margin-bottom: 24rpx;
	flex-wrap: wrap;
}

.tag {
	font-size: 24rpx;
	color: #5A6C7D;
	background: linear-gradient(135deg, #F7F9FC, #EEF2F6);
	padding: 10rpx 20rpx;
	border-radius: 12rpx;
	font-weight: 500;
	border: 1rpx solid #E4E7ED;
}

.address {
	font-size: 28rpx;
	color: #5A6C7D;
	padding: 16rpx 20rpx;
	background: #F7F9FC;
	border-radius: 12rpx;
	margin-bottom: 16rpx;
}

.stats-row {
	display: flex;
	gap: 40rpx;
	margin-top: 20rpx;
	padding-top: 20rpx;
	border-top: 1rpx solid #F2F6FC;
}

.stat-item {
	font-size: 24rpx;
	color: #8B95A5;
	font-weight: 500;
}

.icon {
	margin-right: 8rpx;
}

/* 基本信息网格 */
.info-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 24rpx;
}

.info-item {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	padding: 20rpx;
	background: #F7F9FC;
	border-radius: 16rpx;
	border: 1rpx solid #E4E7ED;
}

.info-label {
	font-size: 24rpx;
	color: #8B95A5;
	font-weight: 500;
}

.info-value {
	font-size: 30rpx;
	color: #2C3E50;
	font-weight: 600;
}

.info-value.phone {
	color: #FF6B35;
}


/* 配套设施网格 */
.facilities-grid {
	display: flex;
	flex-wrap: wrap;
	gap: 24rpx;
}

.facility-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: calc(25% - 18rpx);
	padding: 24rpx 12rpx;
	background: #F7F9FC;
	border-radius: 16rpx;
	border: 1rpx solid #E4E7ED;
	transition: all 0.3s ease;
}

.facility-item:active {
	background: #EEF2F6;
	transform: scale(0.95);
}

.facility-icon {
	font-size: 44rpx;
	margin-bottom: 12rpx;
	filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}

.facility-name {
	font-size: 24rpx;
	color: #5A6C7D;
	font-weight: 500;
}

.landlord-info {
	display: flex;
	align-items: center;
	background: #FFFFFF;
	padding: 32rpx;
	margin: 0 24rpx 24rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.avatar {
	width: 110rpx;
	height: 110rpx;
	border-radius: 50%;
	margin-right: 24rpx;
	border: 4rpx solid #FFE5D9;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.15);
}

.info {
	flex: 1;
}

.name {
	font-size: 34rpx;
	font-weight: 700;
	color: #2C3E50;
	margin-bottom: 8rpx;
}

.desc {
	font-size: 24rpx;
	color: #8B95A5;
	background: #F7F9FC;
	padding: 6rpx 16rpx;
	border-radius: 8rpx;
	display: inline-block;
}

.chat-btn {
	background: linear-gradient(135deg, #4ECDC4, #44A3D5);
	color: #FFFFFF;
	padding: 20rpx 44rpx;
	border-radius: 48rpx;
	font-size: 28rpx;
	font-weight: 600;
	box-shadow: 0 4rpx 12rpx rgba(78, 205, 196, 0.3);
	transition: all 0.3s ease;
}

.chat-btn:active {
	transform: scale(0.95);
}

.section {
	background: #FFFFFF;
	padding: 36rpx;
	margin: 0 24rpx 24rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.section-title {
	font-size: 36rpx;
	font-weight: 700;
	color: #2C3E50;
	margin-bottom: 28rpx;
	padding-left: 16rpx;
	border-left: 6rpx solid #FF6B35;
}

.description {
	font-size: 30rpx;
	color: #5A6C7D;
	line-height: 2;
	padding: 20rpx;
	background: #F7F9FC;
	border-radius: 16rpx;
	border-left: 4rpx solid #4ECDC4;
}

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	align-items: center;
	gap: 24rpx;
	background: #FFFFFF;
	padding: 24rpx 30rpx;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
	padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	backdrop-filter: blur(10rpx);
}

.action-icons {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.icon-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 72rpx;
	height: 72rpx;
	background: #F7F9FC;
	border: none;
	border-radius: 50%;
	padding: 0;
	margin: 0;
	line-height: 1;
}

.icon-btn::after {
	border: none;
}

.icon-btn .icon {
	font-size: 36rpx;
}

.action-btns {
	flex: 1;
	display: flex;
	gap: 20rpx;
}

.btn {
	flex: 1;
	text-align: center;
	padding: 32rpx;
	border-radius: 48rpx;
	font-size: 32rpx;
	font-weight: 700;
	transition: all 0.3s ease;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.btn-secondary {
	background: linear-gradient(135deg, #F7F9FC, #EEF2F6);
	color: #2C3E50;
	border: 2rpx solid #E4E7ED;
}

.btn-primary {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.35);
}
</style>

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
					<text class="info-value phone" v-if="isVerified" @click="callPhone">{{ houseInfo.contactPhone || '-' }}</text>
					<text class="info-value not-verified" v-else @click="goVerify">认证后查看</text>
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
				<!-- 微信小程序分享按钮，必须使用button的open-type="share" -->
				<button class="share-btn" open-type="share">
					<text class="icon">📤</text>
					<text class="icon-label">分享</text>
				</button>
				<view class="icon-item" @click="reportHouse">
					<text class="icon">🚨</text>
					<text class="icon-label">举报</text>
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
			facilitiesList: [],
			isVerified: false // 是否已通过小区认证
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
	
	onShow() {
		// 重新进入页面时检查认证状态
		if (this.houseInfo.communityId) {
			this.checkVerification(this.houseInfo.communityId)
		}
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
					
					// 检查用户是否已通过该小区认证
					if (res.data.communityId) {
						this.checkVerification(res.data.communityId)
					}
				}
			} catch (e) {
				console.error('加载房源详情失败:', e)
			}
		},
		
		// 检查小区认证状态
		async checkVerification(communityId) {
			try {
				const res = await api.communityVerification.check(communityId)
				if (res.code === 200) {
					this.isVerified = res.data === true
				}
			} catch (e) {
				// 未登录或检查失败，默认未认证
				this.isVerified = false
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
			if (!this.isVerified) {
				this.goVerify()
				return
			}
			if (this.houseInfo.contactPhone) {
				uni.makePhoneCall({
					phoneNumber: this.houseInfo.contactPhone,
					fail: () => {
						uni.showToast({ title: '拨打失败', icon: 'none' })
					}
				})
			}
		},
		
		// 跳转到小区认证页面
		goVerify() {
			uni.showModal({
				title: '需要小区认证',
				content: '认证通过后可查看联系电话、发布房源等功能',
				confirmText: '去认证',
				success: (res) => {
					if (res.confirm) {
						uni.navigateTo({ url: '/pages/community/auth/auth' })
					}
				}
			})
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
	background: #f5f7fa;
	padding-bottom: 160rpx;
}

.house-swiper {
	width: 100%;
	height: 500rpx;
}

.swiper-img {
	width: 100%;
	height: 100%;
}

.house-info {
	background: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.price-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.price {
	color: #ff6b6b;
}

.price-num {
	font-size: 48rpx;
	font-weight: bold;
}

.price-unit {
	font-size: 28rpx;
}

.favorite-btn {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
}

.fav-icon {
	font-size: 48rpx;
	transition: transform 0.3s;
}

.fav-icon.favorited {
	transform: scale(1.2);
}

.fav-text {
	font-size: 22rpx;
	color: #999;
}

.title {
	font-size: 36rpx;
	font-weight: 600;
	margin-bottom: 20rpx;
}

.tags {
	display: flex;
	gap: 16rpx;
	margin-bottom: 20rpx;
}

.tag {
	font-size: 24rpx;
	color: #666;
	background: #f5f7fa;
	padding: 8rpx 16rpx;
	border-radius: 8rpx;
}

.address {
	font-size: 28rpx;
	color: #666;
}

.stats-row {
	display: flex;
	gap: 30rpx;
	margin-top: 16rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #eee;
}

.stat-item {
	font-size: 24rpx;
	color: #999;
}

.icon {
	margin-right: 8rpx;
}

/* 基本信息网格 */
.info-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
}

.info-item {
	display: flex;
	justify-content: space-between;
	padding: 16rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}

.info-label {
	font-size: 28rpx;
	color: #999;
}

.info-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.info-value.phone {
	color: #409eff;
}

.info-value.not-verified {
	color: #f56c6c;
	font-size: 24rpx;
}

/* 配套设施网格 */
.facilities-grid {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.facility-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: calc(25% - 15rpx);
	padding: 16rpx 0;
}

.facility-icon {
	font-size: 40rpx;
	margin-bottom: 8rpx;
}

.facility-name {
	font-size: 24rpx;
	color: #666;
}

.landlord-info {
	display: flex;
	align-items: center;
	background: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.info {
	flex: 1;
}

.name {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 8rpx;
}

.desc {
	font-size: 24rpx;
	color: #999;
}

.chat-btn {
	background: #409eff;
	color: #fff;
	padding: 16rpx 40rpx;
	border-radius: 50rpx;
	font-size: 28rpx;
}

.section {
	background: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 20rpx;
}

.description {
	font-size: 28rpx;
	color: #666;
	line-height: 1.8;
}

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	align-items: center;
	gap: 20rpx;
	background: #fff;
	padding: 20rpx;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.action-icons {
	display: flex;
	gap: 30rpx;
}

.icon-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

/* 分享按钮样式（重置button默认样式） */
.share-btn {
	display: flex;
	flex-direction: column;
	align-items: center;
	background: transparent;
	border: none;
	padding: 0;
	margin: 0;
	line-height: 1;
	font-size: inherit;
}

.share-btn::after {
	border: none;
}

.share-btn .icon,
.icon-item .icon {
	font-size: 40rpx;
	margin-bottom: 4rpx;
}

.share-btn .icon-label,
.icon-item .icon-label {
	font-size: 22rpx;
	color: #666;
}

.action-btns {
	flex: 1;
	display: flex;
	gap: 20rpx;
}

.btn {
	flex: 1;
	text-align: center;
	padding: 28rpx;
	border-radius: 50rpx;
	font-size: 32rpx;
}

.btn-secondary {
	background: #f5f7fa;
	color: #333;
}

.btn-primary {
	background: #409eff;
	color: #fff;
}
</style>

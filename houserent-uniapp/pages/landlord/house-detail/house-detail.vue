<template>
	<view class="landlord-house-detail">
		<!-- 房源图片轮播 -->
		<swiper class="house-images" :indicator-dots="true" :autoplay="false" :circular="true">
			<swiper-item v-for="(image, index) in houseDetail.images" :key="index">
				<image class="house-image" :src="image" mode="aspectFill" @click="previewImage(index)"></image>
			</swiper-item>
		</swiper>
		
		<!-- 房源基本信息 -->
		<view class="house-info">
			<view class="house-header">
				<view class="house-title">{{ houseDetail.title }}</view>
				<view class="house-status" :class="getStatusClass()">
					{{ getStatusText() }}
				</view>
			</view>
			<!-- 审核拒绝原因 -->
			<view class="reject-reason" v-if="houseDetail.auditStatus === 'rejected' && houseDetail.auditOpinion">
				<text class="reject-label">拒绝原因：</text>
				<text class="reject-text">{{ houseDetail.auditOpinion }}</text>
			</view>
			
			<view class="price-section">
				<text class="price">¥{{ houseDetail.rentPrice }}</text>
				<text class="price-unit">/月</text>
			</view>
			
			<view class="house-tags">
				<text class="tag">{{ houseDetail.roomType }}</text>
				<text class="tag">{{ houseDetail.area }}㎡</text>
				<text class="tag">{{ houseDetail.floor }}</text>
			</view>
			
			<view class="house-address">
				<text class="address-icon">📍</text>
				<text class="address-text">{{ houseDetail.communityName }}</text>
			</view>
		</view>
		
		<!-- 房源描述 -->
		<view class="description-section">
			<view class="section-title">房源描述</view>
			<text class="description-text">{{ houseDetail.description || '暂无描述' }}</text>
		</view>
		
		<!-- 租赁统计 -->
		<view class="stats-section">
			<view class="section-title">租赁统计</view>
			<view class="stats-grid">
				<view class="stat-item">
					<text class="stat-number">{{ houseDetail.viewCount || 0 }}</text>
					<text class="stat-label">浏览次数</text>
				</view>
				<view class="stat-item">
					<text class="stat-number">{{ houseDetail.favoriteCount || 0 }}</text>
					<text class="stat-label">收藏次数</text>
				</view>
				<view class="stat-item">
					<text class="stat-number">{{ houseDetail.inquiryCount || 0 }}</text>
					<text class="stat-label">咨询次数</text>
				</view>
			</view>
		</view>
		
		<!-- 当前租客信息 -->
		<view class="tenant-section" v-if="houseDetail.currentTenant">
			<view class="section-title">当前租客</view>
			<view class="tenant-card">
				<image class="tenant-avatar" :src="houseDetail.currentTenant.avatarUrl"></image>
				<view class="tenant-info">
					<view class="tenant-name">{{ houseDetail.currentTenant.nickname }}</view>
					<view class="tenant-phone">{{ houseDetail.currentTenant.phone }}</view>
					<view class="rent-period">租期：{{ houseDetail.currentTenant.startDate }} 至 {{ houseDetail.currentTenant.endDate }}</view>
				</view>
				<view class="tenant-actions">
					<button class="action-btn" @click="contactTenant">联系</button>
				</view>
			</view>
		</view>
		
		<!-- 操作按钮 -->
		<view class="action-buttons">
			<button class="btn btn-secondary" @click="editHouse">编辑房源</button>
			<!-- 审核通过且已下架的可以上架 -->
			<button class="btn btn-primary" 
				v-if="houseDetail.auditStatus === 'approved' && houseDetail.publishStatus === 'offline'" 
				@click="publishHouse">申请上架</button>
			<!-- 审核通过且已上架的可以下架 -->
			<button class="btn btn-warning" 
				v-if="houseDetail.auditStatus === 'approved' && houseDetail.publishStatus === 'online'" 
				@click="unpublishHouse">下架</button>
			<!-- 审核拒绝的可以重新提交 -->
			<button class="btn btn-primary" 
				v-if="houseDetail.auditStatus === 'rejected'" 
				@click="resubmitHouse">重新提交审核</button>
			<button class="btn btn-danger" @click="deleteHouse">删除</button>
		</view>
		
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			houseId: '',
			houseDetail: {
				images: [],
				title: '',
				rentPrice: 0,
				area: 0,
				roomType: '',
				floor: '',
				communityName: '',
				description: '',
				publishStatus: 'offline',
				viewCount: 0,
				favoriteCount: 0,
				inquiryCount: 0,
				currentTenant: null
			}
		}
	},
	
	onLoad(options) {
		console.log('【房东房源详情】onLoad被调用，参数:', options);
		if (options.id) {
			this.houseId = options.id
			this.loadHouseDetail()
		}
	},
	
	methods: {
		// 图片预览放大查看
		previewImage(index) {
			uni.previewImage({
				urls: this.houseDetail.images,
				current: index,
				longPressActions: {
					itemList: ['保存图片'],
					success: (data) => {
						if (data.tapIndex === 0) {
							uni.saveImageToPhotosAlbum({
								filePath: this.houseDetail.images[data.index],
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
		
		async loadHouseDetail() {
			try {
				uni.showLoading({ title: '加载中...' })
				const res = await api.house.getDetail(this.houseId)
				if (res.code === 200) {
					this.houseDetail = res.data
					// 处理图片数组 - 后端已经返回了所有图片
					if (this.houseDetail.images && typeof this.houseDetail.images === 'string') {
						try {
							this.houseDetail.images = JSON.parse(this.houseDetail.images)
						} catch (e) {
							this.houseDetail.images = [this.houseDetail.images]
						}
					}
					if (!this.houseDetail.images || this.houseDetail.images.length === 0) {
						this.houseDetail.images = ['/static/logo.png']
					}
				}
			} catch (e) {
				console.error('加载房源详情失败:', e)
				this.houseDetail = {
					images: ['/static/logo.png'],
					title: '精装两室 · 南北通透',
					rentPrice: 2700,
					area: 85,
					roomType: '2室1厅1卫',
					floor: '10/20层',
					communityName: '阳光小区',
					description: '房源位置优越，交通便利，精装修，拎包入住。',
					publishStatus: 'online',
					viewCount: 128,
					favoriteCount: 15,
					inquiryCount: 8,
					currentTenant: {
						avatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
						nickname: '李小姐',
						phone: '138****5678',
						startDate: '2024-01-01',
						endDate: '2024-12-31'
					}
				}
			} finally {
				uni.hideLoading()
			}
		},
		
		getStatusText() {
			// 优先显示审核状态
			if (this.houseDetail.auditStatus === 'pending') return '待审核'
			if (this.houseDetail.auditStatus === 'rejected') return '审核拒绝'
			// 审核通过后显示发布状态
			const statusMap = {
				'online': '已上架',
				'offline': '已下架',
				'rented': '已出租'
			}
			return statusMap[this.houseDetail.publishStatus] || '未知'
		},
		
		getStatusClass() {
			if (this.houseDetail.auditStatus === 'pending') return 'status-pending'
			if (this.houseDetail.auditStatus === 'rejected') return 'status-rejected'
			return `status-${this.houseDetail.publishStatus}`
		},
		
		editHouse() {
			uni.navigateTo({
				url: `/pages/landlord/publish/publish?id=${this.houseId}&mode=edit`
			})
		},
		
		async publishHouse() {
			try {
				uni.showLoading({ title: '处理中...' })
				const res = await api.house.online(this.houseId)
				if (res.code === 200) {
					this.houseDetail.publishStatus = 'online'
					uni.showToast({ title: '上架成功', icon: 'success' })
				} else {
					throw new Error(res.message || '上架失败')
				}
			} catch (e) {
				console.error('上架失败:', e)
				uni.showToast({ title: e.message || '上架失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},
		
		async unpublishHouse() {
			try {
				uni.showLoading({ title: '处理中...' })
				const res = await api.house.offline(this.houseId)
				if (res.code === 200) {
					this.houseDetail.publishStatus = 'offline'
					uni.showToast({ title: '下架成功', icon: 'success' })
				} else {
					throw new Error(res.message || '下架失败')
				}
			} catch (e) {
				console.error('下架失败:', e)
				uni.showToast({ title: e.message || '下架失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},
		
		async resubmitHouse() {
			// 重新提交审核 - 跳转到编辑页面修改后提交
			uni.showModal({
				title: '重新提交审核',
				content: '请修改房源信息后重新提交审核',
				confirmText: '去修改',
				success: (res) => {
					if (res.confirm) {
						uni.navigateTo({
							url: `/pages/landlord/publish/publish?id=${this.houseId}&mode=edit`
						})
					}
				}
			})
		},
		
		deleteHouse() {
			uni.showModal({
				title: '确认删除',
				content: '确定要删除此房源吗？此操作不可撤销。',
				success: async (res) => {
					if (res.confirm) {
						try {
							const result = await api.house.delete(this.houseId)
							if (result.code === 200) {
								uni.showToast({ title: '删除成功', icon: 'success' })
								setTimeout(() => {
									uni.navigateBack()
								}, 1500)
							}
						} catch (e) {
							console.error('删除失败:', e)
							uni.showToast({ title: '删除失败', icon: 'none' })
						}
					}
				}
			})
		},
		
		contactTenant() {
			if (this.houseDetail.currentTenant) {
				uni.navigateTo({
					url: `/pages/chat/detail/detail?userId=${this.houseDetail.currentTenant.userId}`
				})
			}
		}
	}
}
</script>

<style scoped>
.landlord-house-detail {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 40rpx;
}

/* 房源图片 */
.house-images {
	height: 500rpx;
}

.house-image {
	width: 100%;
	height: 100%;
}

/* 房源信息 */
.house-info {
	background: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.house-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 20rpx;
}

.house-title {
	flex: 1;
	font-size: 36rpx;
	font-weight: 600;
	color: #333;
	margin-right: 20rpx;
}

.house-status {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	font-weight: 500;
}

.status-online {
	background: #e8f5e9;
	color: #4caf50;
}

.status-offline {
	background: #fafafa;
	color: #999;
}

.status-rented {
	background: #e3f2fd;
	color: #2196f3;
}

.status-pending {
	background: #fff3e0;
	color: #ff9800;
}

.status-rejected {
	background: #ffebee;
	color: #f44336;
}

/* 拒绝原因 */
.reject-reason {
	background: #fff3e0;
	padding: 16rpx 20rpx;
	border-radius: 8rpx;
	margin-bottom: 20rpx;
}

.reject-label {
	font-size: 26rpx;
	color: #ff9800;
	font-weight: 500;
}

.reject-text {
	font-size: 26rpx;
	color: #666;
}

.price-section {
	display: flex;
	align-items: baseline;
	margin-bottom: 20rpx;
}

.price {
	font-size: 48rpx;
	font-weight: bold;
	color: #ff6b6b;
}

.price-unit {
	font-size: 28rpx;
	color: #999;
	margin-left: 8rpx;
}

.house-tags {
	display: flex;
	gap: 16rpx;
	margin-bottom: 20rpx;
}

.tag {
	padding: 8rpx 16rpx;
	background: #f0f9ff;
	color: #0ea5e9;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.house-address {
	display: flex;
	align-items: center;
	color: #666;
}

.address-icon {
	margin-right: 8rpx;
	font-size: 28rpx;
}

.address-text {
	font-size: 28rpx;
}

/* 描述区域 */
.description-section {
	background: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 20rpx;
	color: #333;
}

.description-text {
	font-size: 28rpx;
	line-height: 1.6;
	color: #666;
}

/* 统计区域 */
.stats-section {
	background: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.stats-grid {
	display: flex;
	justify-content: space-around;
}

.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-number {
	font-size: 36rpx;
	font-weight: bold;
	color: #409eff;
	margin-bottom: 8rpx;
}

.stat-label {
	font-size: 24rpx;
	color: #999;
}

/* 租客信息 */
.tenant-section {
	background: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.tenant-card {
	display: flex;
	align-items: center;
	padding: 20rpx;
	background: #f8f9fa;
	border-radius: 16rpx;
}

.tenant-avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.tenant-info {
	flex: 1;
}

.tenant-name {
	font-size: 30rpx;
	font-weight: 600;
	margin-bottom: 8rpx;
}

.tenant-phone {
	font-size: 26rpx;
	color: #666;
	margin-bottom: 8rpx;
}

.rent-period {
	font-size: 24rpx;
	color: #999;
}

.tenant-actions {
	margin-left: 20rpx;
}

.action-btn {
	padding: 16rpx 32rpx;
	background: #409eff;
	color: #fff;
	border-radius: 20rpx;
	font-size: 26rpx;
}

/* 操作按钮 */
.action-buttons {
	display: flex;
	gap: 20rpx;
	padding: 20rpx;
	background: #fff;
	margin-bottom: 20rpx;
}

.btn {
	flex: 1;
	text-align: center;
	padding: 28rpx;
	border-radius: 12rpx;
	font-size: 28rpx;
	font-weight: 500;
}

.btn-primary {
	background: #409eff;
	color: #fff;
}

.btn-secondary {
	background: #f5f7fa;
	color: #333;
}

.btn-warning {
	background: #ff9800;
	color: #fff;
}

.btn-danger {
	background: #f56c6c;
	color: #fff;
}

/* 房东底部导航栏 */
.landlord-tabbar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	background: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
	padding-bottom: env(safe-area-inset-bottom);
}

.landlord-tabbar .tab-item {
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
</style>

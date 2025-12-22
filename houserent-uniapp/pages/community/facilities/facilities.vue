<template>
	<view class="facilities-page">
		<!-- 小区选择 -->
		<view class="community-selector">
			<picker mode="selector" :range="communityList" range-key="communityName" @change="onCommunityChange">
				<view class="picker-box">
					<text class="community-name">{{ selectedCommunity.communityName || '请选择小区' }}</text>
					<text class="arrow">▼</text>
				</view>
			</picker>
		</view>
		
		<!-- 配套分类 -->
		<view class="category-tabs">
			<view class="tab-item" :class="{ active: activeTab === 0 }" @click="switchTab(0)">
				小区内配套
			</view>
			<view class="tab-item" :class="{ active: activeTab === 1 }" @click="switchTab(1)">
				周边3公里
			</view>
		</view>
		
		<!-- 配套列表 -->
		<scroll-view class="facilities-list" scroll-y>
			<view class="facility-card" v-for="facility in facilitiesList" :key="facility.facilityId">
				<view class="facility-header">
					<text class="facility-icon">{{ getCategoryIcon(facility.category) }}</text>
					<view class="facility-info">
						<view class="facility-name">{{ facility.name }}</view>
						<view class="facility-category">{{ facility.category }}</view>
					</view>
				</view>
				<view class="facility-desc">{{ facility.description }}</view>
				<view class="facility-contact" v-if="facility.contactInfo">
					<text class="label">联系方式：</text>
					<text class="value">{{ facility.contactInfo }}</text>
				</view>
				<view class="facility-distance" v-if="facility.distance">
					<text class="label">距离：</text>
					<text class="value">{{ facility.distance }}</text>
				</view>
				<view class="facility-images" v-if="facility.images">
					<image class="facility-img" v-for="(img, index) in parseImages(facility.images)" 
						:key="index" :src="img" mode="aspectFill" @click="previewImage(img, facility.images)"></image>
				</view>
				<view class="facility-footer">
					<text class="update-time">更新于 {{ formatTime(facility.updatedAt) }}</text>
					<view class="feedback-btn" @click="feedbackFacility(facility)">
						<text>📝 反馈</text>
					</view>
				</view>
			</view>
			
			<view class="empty" v-if="facilitiesList.length === 0">
				<text class="empty-icon">🏢</text>
				<text class="empty-text">暂无配套信息</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			activeTab: 0,
			communityList: [],
			selectedCommunity: {},
			facilitiesList: []
		}
	},
	
	onLoad(options) {
		if (options.communityId) {
			this.selectedCommunity.communityId = options.communityId
		}
		this.loadCommunityList()
	},
	
	// 下拉刷新
	onPullDownRefresh() {
		this.loadFacilities().finally(() => {
			uni.stopPullDownRefresh()
		})
	},
	
	methods: {
		async loadCommunityList() {
			try {
				const res = await api.community.getList({ pageNum: 1, pageSize: 100 })
				if (res.code === 200) {
					this.communityList = res.data.records || []
					if (this.selectedCommunity.communityId) {
						const community = this.communityList.find(c => c.communityId === this.selectedCommunity.communityId)
						if (community) {
							this.selectedCommunity = community
							this.loadFacilities()
						}
					}
				}
			} catch (e) {
				console.error('加载小区列表失败:', e)
			}
		},
		
		onCommunityChange(e) {
			const index = e.detail.value
			this.selectedCommunity = this.communityList[index]
			this.loadFacilities()
		},
		
		switchTab(index) {
			this.activeTab = index
			this.loadFacilities()
		},
		
		async loadFacilities() {
			if (!this.selectedCommunity.communityId) return Promise.resolve()
			
			try {
				// internal-小区内配套, surrounding-周边3公里配套
				const facilityType = this.activeTab === 0 ? 'internal' : 'surrounding'
				const res = await api.community.getFacilities(
					this.selectedCommunity.communityId,
					facilityType
				)
				if (res.code === 200) {
					this.facilitiesList = res.data || []
				}
			} catch (e) {
				console.error('加载配套信息失败:', e)
			}
		},
		
		getCategoryIcon(category) {
			const icons = {
				'物业': '🏢',
				'快递': '📦',
				'安保': '👮',
				'停车': '🅿️',
				'商超': '🛒',
				'公交': '🚌',
				'地铁': '🚇',
				'学校': '🏫',
				'医院': '🏥',
				'银行': '🏦'
			}
			return icons[category] || '📍'
		},
		
		parseImages(images) {
			try {
				return JSON.parse(images)
			} catch (e) {
				return []
			}
		},
		
		previewImage(current, images) {
			const urls = this.parseImages(images)
			uni.previewImage({
				current: current,
				urls: urls
			})
		},
		
		formatTime(time) {
			if (!time) return ''
			const date = new Date(time)
			return `${date.getMonth() + 1}月${date.getDate()}日`
		},
		
		feedbackFacility(facility) {
			uni.showModal({
				title: '信息反馈',
				content: '请描述需要更新或纠正的内容',
				editable: true,
				placeholderText: '请输入反馈内容',
				success: async (res) => {
					if (res.confirm && res.content) {
						try {
							const result = await api.community.feedbackFacility(
								this.selectedCommunity.communityId,
								{
									facilityId: facility.facilityId,
									content: res.content
								}
							)
							if (result.code === 200) {
								uni.showToast({ title: '反馈提交成功', icon: 'success' })
							} else {
								uni.showToast({ title: result.message || '提交失败', icon: 'none' })
							}
						} catch (e) {
							console.error('提交反馈失败:', e)
							uni.showToast({ title: '提交失败，请先登录', icon: 'none' })
						}
					} else if (res.confirm && !res.content) {
						uni.showToast({ title: '请输入反馈内容', icon: 'none' })
					}
				}
			})
		}
	}
}
</script>

<style scoped>
.facilities-page {
	min-height: 100vh;
	background: #f5f7fa;
}

.community-selector {
	background: #fff;
	padding: 20rpx 30rpx;
}

.picker-box {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 24rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
}

.community-name {
	font-size: 30rpx;
	font-weight: 600;
}

.arrow {
	font-size: 24rpx;
	color: #999;
}

.category-tabs {
	display: flex;
	background: #fff;
	margin-top: 20rpx;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 30rpx 0;
	font-size: 30rpx;
	color: #666;
	border-bottom: 4rpx solid transparent;
}

.tab-item.active {
	color: #409eff;
	border-bottom-color: #409eff;
	font-weight: 600;
}

.facilities-list {
	height: calc(100vh - 260rpx);
	padding: 20rpx;
}

.facility-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.facility-header {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.facility-icon {
	font-size: 48rpx;
	margin-right: 20rpx;
}

.facility-info {
	flex: 1;
}

.facility-name {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 8rpx;
}

.facility-category {
	font-size: 24rpx;
	color: #999;
}

.facility-desc {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
	margin-bottom: 16rpx;
}

.facility-contact {
	font-size: 26rpx;
	color: #666;
	margin-bottom: 16rpx;
}

.facility-contact .label {
	color: #999;
}

.facility-contact .value {
	color: #409eff;
}

.facility-distance {
	font-size: 26rpx;
	color: #666;
	margin-bottom: 16rpx;
}

.facility-distance .label {
	color: #999;
}

.facility-distance .value {
	color: #52c41a;
}

.facility-images {
	display: flex;
	gap: 12rpx;
	margin-bottom: 16rpx;
}

.facility-img {
	width: 200rpx;
	height: 150rpx;
	border-radius: 8rpx;
}

.facility-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 16rpx;
	border-top: 1rpx solid #f0f0f0;
}

.update-time {
	font-size: 24rpx;
	color: #999;
}

.feedback-btn {
	font-size: 26rpx;
	color: #409eff;
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

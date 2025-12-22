<template>
	<view class="evaluation-page">
		<!-- 评价对象信息 -->
		<view class="target-card">
			<image class="avatar" :src="targetInfo.avatarUrl"></image>
			<view class="target-info">
				<view class="nickname">{{ targetInfo.nickname }}</view>
				<view class="role">{{ targetInfo.userType === 3 ? '租客' : '房东' }}</view>
			</view>
		</view>
		
		<!-- 评分 -->
		<view class="rating-section">
			<view class="section-title">综合评分</view>
			<view class="stars">
				<text class="star" :class="{ active: rating >= 1 }" @click="setRating(1)">⭐</text>
				<text class="star" :class="{ active: rating >= 2 }" @click="setRating(2)">⭐</text>
				<text class="star" :class="{ active: rating >= 3 }" @click="setRating(3)">⭐</text>
				<text class="star" :class="{ active: rating >= 4 }" @click="setRating(4)">⭐</text>
				<text class="star" :class="{ active: rating >= 5 }" @click="setRating(5)">⭐</text>
			</view>
			<view class="rating-text">{{ getRatingText(rating) }}</view>
		</view>
		
		<!-- 评价标签 -->
		<view class="tags-section">
			<view class="section-title">评价标签</view>
			<view class="tags-list">
				<view class="tag-item" :class="{ active: selectedTags.includes(tag) }" 
					v-for="tag in availableTags" :key="tag" @click="toggleTag(tag)">
					{{ tag }}
				</view>
			</view>
		</view>
		
		<!-- 文字评价 -->
		<view class="comment-section">
			<view class="section-title">详细评价</view>
			<textarea class="comment-input" v-model="comment" 
				placeholder="请分享您的租房体验..." maxlength="500"></textarea>
			<view class="word-count">{{ comment.length }}/500</view>
		</view>
		
		<!-- 提交按钮 -->
		<view class="submit-btn" @click="submitEvaluation">提交评价</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			targetUserId: '',
			contractId: '',
			targetInfo: {},
			rating: 5,
			selectedTags: [],
			comment: '',
			availableTags: []
		}
	},
	
	onLoad(options) {
		this.targetUserId = options.targetUserId
		this.contractId = options.contractId
		this.loadTargetInfo()
		this.loadAvailableTags()
	},
	
	methods: {
		async loadTargetInfo() {
			try {
				// 从评价检查接口获取目标用户信息
				const checkRes = await api.evaluation.checkCanEvaluate(this.contractId)
				if (checkRes.code === 200 && checkRes.data) {
					if (!checkRes.data.canEvaluate) {
						uni.showModal({
							title: '提示',
							content: checkRes.data.reason || '当前无法评价',
							showCancel: false,
							success: () => {
								uni.navigateBack()
							}
						})
						return
					}
				}
				
				// 获取目标用户评价列表来获取基本信息
				const res = await api.evaluation.getUserEvaluations(this.targetUserId)
				if (res.code === 200) {
					// 设置默认信息
					this.targetInfo = {
						userId: this.targetUserId,
						nickname: '用户',
						avatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
						userType: 3
					}
				}
				this.loadAvailableTags()
			} catch (e) {
				console.error('加载用户信息失败:', e)
				this.loadAvailableTags()
			}
		},
		
		loadAvailableTags() {
			// 根据当前用户角色加载不同的标签
			const currentUserType = uni.getStorageSync('userType')
			if (currentUserType == 4) {
				// 房东评价租客
				this.availableTags = ['按时交租', '爱护房屋', '沟通顺畅', '守信用', '整洁干净', '配合度高']
			} else {
				// 租客评价房东
				this.availableTags = ['房东友善', '及时维修', '房源真实', '沟通顺畅', '守信用', '服务周到']
			}
		},
		
		setRating(score) {
			this.rating = score
		},
		
		getRatingText(score) {
			const texts = ['', '很差', '较差', '一般', '满意', '非常满意']
			return texts[score] || ''
		},
		
		toggleTag(tag) {
			const index = this.selectedTags.indexOf(tag)
			if (index > -1) {
				this.selectedTags.splice(index, 1)
			} else {
				this.selectedTags.push(tag)
			}
		},
		
		async submitEvaluation() {
			if (this.rating === 0) {
				return uni.showToast({ title: '请选择评分', icon: 'none' })
			}
			
			try {
				uni.showLoading({ title: '提交中...' })
				const data = {
					targetUserId: this.targetUserId,
					contractId: this.contractId,
					rating: this.rating,
					tags: this.selectedTags.join(','),
					comment: this.comment
				}
				
				const res = await api.evaluation.submit(data)
				uni.hideLoading()
				
				if (res.code === 200) {
					uni.showToast({ title: '评价成功', icon: 'success' })
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				} else {
					uni.showToast({ title: res.message || '提交失败', icon: 'none' })
				}
			} catch (e) {
				uni.hideLoading()
				console.error('提交评价失败:', e)
				uni.showToast({ title: '提交失败，请重试', icon: 'none' })
			}
		}
	}
}
</script>

<style scoped>
.evaluation-page {
	min-height: 100vh;
	background: #F7F9FC;
	padding: 24rpx 30rpx;
}

.target-card {
	display: flex;
	align-items: center;
	background: linear-gradient(135deg, #FFF5F0, #FFFFFF);
	border-radius: 24rpx;
	padding: 36rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	position: relative;
	overflow: hidden;
}

.target-card::before {
	content: '⭐';
	position: absolute;
	top: 20rpx;
	right: 20rpx;
	font-size: 60rpx;
	opacity: 0.1;
}

.avatar {
	width: 130rpx;
	height: 130rpx;
	border-radius: 50%;
	margin-right: 32rpx;
	border: 6rpx solid #FFE5D9;
	box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.2);
}

.target-info {
	flex: 1;
	position: relative;
	z-index: 1;
}

.nickname {
	font-size: 38rpx;
	font-weight: 700;
	color: #2C3E50;
	margin-bottom: 12rpx;
}

.role {
	font-size: 26rpx;
	color: #8B95A5;
	background: #F7F9FC;
	padding: 6rpx 20rpx;
	border-radius: 20rpx;
	display: inline-block;
	font-weight: 500;
}

.rating-section, .tags-section, .comment-section {
	background: #FFFFFF;
	border-radius: 24rpx;
	padding: 36rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.section-title {
	font-size: 36rpx;
	font-weight: 700;
	color: #2C3E50;
	margin-bottom: 32rpx;
	padding-left: 16rpx;
	border-left: 6rpx solid #FF6B35;
}

.stars {
	display: flex;
	justify-content: center;
	gap: 24rpx;
	margin-bottom: 24rpx;
}

.star {
	font-size: 88rpx;
	opacity: 0.2;
	transition: all 0.3s ease;
	filter: grayscale(1);
}

.star:active {
	transform: scale(1.2);
}

.star.active {
	opacity: 1;
	filter: grayscale(0) drop-shadow(0 4rpx 12rpx rgba(255, 215, 0, 0.4));
	animation: starPop 0.3s ease;
}

@keyframes starPop {
	0% { transform: scale(1); }
	50% { transform: scale(1.3); }
	100% { transform: scale(1); }
}

.rating-text {
	text-align: center;
	font-size: 34rpx;
	color: #FF6B35;
	font-weight: 700;
}

.tags-list {
	display: flex;
	flex-wrap: wrap;
	gap: 24rpx;
}

.tag-item {
	padding: 18rpx 36rpx;
	background: #F7F9FC;
	border-radius: 48rpx;
	font-size: 28rpx;
	color: #5A6C7D;
	font-weight: 500;
	border: 2rpx solid #E4E7ED;
	transition: all 0.3s ease;
}

.tag-item:active {
	transform: scale(0.95);
}

.tag-item.active {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	border-color: transparent;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
	transform: translateY(-2rpx);
}

.comment-input {
	width: 100%;
	min-height: 320rpx;
	padding: 24rpx;
	background: #F7F9FC;
	border-radius: 16rpx;
	font-size: 30rpx;
	line-height: 1.8;
	color: #2C3E50;
	border: 2rpx solid #E4E7ED;
	transition: all 0.3s ease;
}

.comment-input:focus {
	background: #FFFFFF;
	border-color: #FF6B35;
	box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
}

.word-count {
	text-align: right;
	font-size: 24rpx;
	color: #8B95A5;
	margin-top: 16rpx;
	font-weight: 500;
}

.submit-btn {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	text-align: center;
	padding: 36rpx;
	border-radius: 48rpx;
	font-size: 34rpx;
	font-weight: 700;
	box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.35);
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.submit-btn::before {
	content: '';
	position: absolute;
	top: 0;
	left: -100%;
	width: 100%;
	height: 100%;
	background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
	transition: left 0.5s;
}

.submit-btn:active::before {
	left: 100%;
}

.submit-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}
</style>

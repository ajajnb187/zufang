<template>
	<view class="user-profile-page">
		<!-- 用户信息卡片 -->
		<view class="user-card">
			<image class="avatar" :src="userInfo.avatarUrl || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'" mode="aspectFill"></image>
			<view class="user-info">
				<view class="nickname">{{ userInfo.nickname || '用户' }}</view>
				<view class="user-type">{{ userInfo.userType === 4 ? '房东' : '租客' }}</view>
			</view>
			<view class="credit-score">
				<view class="score-value">{{ userInfo.creditScore || '5.0' }}</view>
				<view class="score-label">信用分</view>
			</view>
		</view>
		
		<!-- 评价统计 -->
		<view class="stats-card">
			<view class="stat-item">
				<view class="stat-value">{{ evaluationStats.totalCount || 0 }}</view>
				<view class="stat-label">收到评价</view>
			</view>
			<view class="stat-item">
				<view class="stat-value">{{ evaluationStats.avgRating || '0.0' }}</view>
				<view class="stat-label">平均评分</view>
			</view>
			<view class="stat-item">
				<view class="stat-value">{{ goodRate }}%</view>
				<view class="stat-label">好评率</view>
			</view>
		</view>
		
		<!-- 评价列表 -->
		<view class="evaluations-section">
			<view class="section-header">
				<text class="section-title">信用评价</text>
				<text class="eval-count">共{{ evaluations.length }}条</text>
			</view>
			
			<view class="evaluation-list" v-if="evaluations.length > 0">
				<view class="evaluation-item" v-for="item in evaluations" :key="item.evaluationId">
					<view class="eval-header">
						<image class="evaluator-avatar" :src="item.evaluatorAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
						<view class="evaluator-info">
							<view class="evaluator-name">{{ item.evaluatorName || '匿名用户' }}</view>
							<view class="eval-time">{{ formatTime(item.createdAt) }}</view>
						</view>
						<view class="eval-rating">
							<text class="stars">{{ getStars(item.starRating) }}</text>
							<text class="rating-value">{{ item.starRating }}</text>
						</view>
					</view>
					
					<view class="eval-tags" v-if="item.tags">
						<view class="tag" v-for="tag in item.tags.split(',')" :key="tag">{{ tag }}</view>
					</view>
					
					<view class="eval-content" v-if="item.content">{{ item.content }}</view>
				</view>
			</view>
			
			<view class="empty-evaluations" v-else>
				<text class="empty-icon">⭐</text>
				<text class="empty-text">暂无评价</text>
			</view>
		</view>
		
		<!-- 操作按钮 -->
		<view class="action-bar" v-if="!isSelf">
			<view class="action-btn" @click="startChat">
				<text class="btn-icon">💬</text>
				<text>发消息</text>
			</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			userId: '',
			userInfo: {},
			evaluationStats: {},
			evaluations: [],
			currentUserId: null
		}
	},
	
	computed: {
		isSelf() {
			return this.currentUserId == this.userId
		},
		
		goodRate() {
			if (!this.evaluations.length) return 0
			const goodCount = this.evaluations.filter(e => e.starRating >= 4).length
			return Math.round((goodCount / this.evaluations.length) * 100)
		}
	},
	
	onLoad(options) {
		this.userId = options.id
		this.currentUserId = uni.getStorageSync('userId')
		this.loadUserInfo()
		this.loadEvaluations()
	},
	
	methods: {
		async loadUserInfo() {
			try {
				// 这里需要一个获取用户公开信息的API
				// 暂时使用模拟数据
				this.userInfo = {
					userId: this.userId,
					nickname: '用户' + this.userId,
					avatarUrl: '',
					userType: 3,
					creditScore: 5.0
				}
			} catch (e) {
				console.error('加载用户信息失败:', e)
			}
		},
		
		async loadEvaluations() {
			try {
				const res = await api.evaluation.getUserEvaluations(this.userId)
				if (res.code === 200) {
					this.evaluations = res.data.evaluations || []
					this.evaluationStats = {
						totalCount: res.data.totalCount,
						avgRating: res.data.avgRating
					}
					
					// 更新用户信息中的信用分
					if (res.data.avgRating) {
						this.userInfo.creditScore = res.data.avgRating
					}
				}
			} catch (e) {
				console.error('加载评价失败:', e)
			}
		},
		
		getStars(rating) {
			const fullStars = Math.floor(rating)
			return '⭐'.repeat(fullStars)
		},
		
		formatTime(timeStr) {
			if (!timeStr) return ''
			const date = new Date(timeStr)
			return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
		},
		
		startChat() {
			uni.navigateTo({
				url: `/pages/chat/chat-detail/chat-detail?userId=${this.userId}`
			})
		}
	}
}
</script>

<style scoped>
.user-profile-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 120rpx;
}

.user-card {
	background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
	padding: 60rpx 40rpx;
	display: flex;
	align-items: center;
	color: #fff;
}

.avatar {
	width: 140rpx;
	height: 140rpx;
	border-radius: 50%;
	border: 4rpx solid rgba(255, 255, 255, 0.5);
}

.user-info {
	flex: 1;
	margin-left: 30rpx;
}

.nickname {
	font-size: 40rpx;
	font-weight: 600;
	margin-bottom: 12rpx;
}

.user-type {
	font-size: 26rpx;
	opacity: 0.9;
	background: rgba(255, 255, 255, 0.2);
	padding: 6rpx 20rpx;
	border-radius: 20rpx;
	display: inline-block;
}

.credit-score {
	text-align: center;
}

.score-value {
	font-size: 48rpx;
	font-weight: 600;
}

.score-label {
	font-size: 24rpx;
	opacity: 0.8;
}

.stats-card {
	background: #fff;
	margin: -30rpx 20rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
	display: flex;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.stat-item {
	flex: 1;
	text-align: center;
}

.stat-value {
	font-size: 40rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 8rpx;
}

.stat-label {
	font-size: 24rpx;
	color: #999;
}

.evaluations-section {
	background: #fff;
	margin: 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.eval-count {
	font-size: 26rpx;
	color: #999;
}

.evaluation-list {
	display: flex;
	flex-direction: column;
	gap: 30rpx;
}

.evaluation-item {
	padding-bottom: 30rpx;
	border-bottom: 1rpx solid #f5f5f5;
}

.evaluation-item:last-child {
	border-bottom: none;
	padding-bottom: 0;
}

.eval-header {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.evaluator-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
}

.evaluator-info {
	flex: 1;
	margin-left: 20rpx;
}

.evaluator-name {
	font-size: 28rpx;
	color: #333;
	margin-bottom: 6rpx;
}

.eval-time {
	font-size: 24rpx;
	color: #999;
}

.eval-rating {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.stars {
	font-size: 24rpx;
}

.rating-value {
	font-size: 28rpx;
	font-weight: 600;
	color: #ff9800;
}

.eval-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
	margin-bottom: 16rpx;
}

.tag {
	background: #e8f4ff;
	color: #409eff;
	font-size: 24rpx;
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
}

.eval-content {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
}

.empty-evaluations {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 80rpx 0;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

.action-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	padding: 20rpx 30rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.action-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	background: #409eff;
	color: #fff;
	padding: 28rpx;
	border-radius: 50rpx;
	font-size: 32rpx;
}

.btn-icon {
	font-size: 36rpx;
}
</style>

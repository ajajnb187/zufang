<template>
	<view class="index-page">
		<view class="loading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { isLogin, getUserType } from '@/utils/storage.js'

export default {
	onLoad() {
		// 启动页：根据登录状态和用户类型跳转
		setTimeout(() => {
			if (isLogin()) {
				const userType = getUserType()
				if (userType === 4) {
					// 房东 -> 房源管理
					uni.reLaunch({ url: '/pages/landlord/houses/houses' })
				} else {
					// 租客 -> 找房首页
					uni.reLaunch({ url: '/pages/home/home' })
				}
			} else {
				// 未登录 -> 登录页
				uni.reLaunch({ url: '/pages/login/login' })
			}
		}, 500)
	}
}
</script>

<style scoped>
.index-page {
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 100vh;
	background: linear-gradient(135deg, #FFE5D9 0%, #FFF5F0 50%, #E8F5F3 100%);
	position: relative;
	overflow: hidden;
}

.index-page::before {
	content: '';
	position: absolute;
	top: -200rpx;
	right: -200rpx;
	width: 600rpx;
	height: 600rpx;
	border-radius: 50%;
	background: radial-gradient(circle, rgba(255, 107, 53, 0.1), transparent);
	animation: rotate 20s linear infinite;
}

.index-page::after {
	content: '';
	position: absolute;
	bottom: -150rpx;
	left: -150rpx;
	width: 500rpx;
	height: 500rpx;
	border-radius: 50%;
	background: radial-gradient(circle, rgba(78, 205, 196, 0.08), transparent);
	animation: rotate 15s linear infinite reverse;
}

@keyframes rotate {
	from { transform: rotate(0deg); }
	to { transform: rotate(360deg); }
}

.loading {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 40rpx;
}

.loading::before {
	content: '';
	width: 80rpx;
	height: 80rpx;
	border: 6rpx solid #FFE5D9;
	border-top-color: #FF6B35;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

@keyframes spin {
	from { transform: rotate(0deg); }
	to { transform: rotate(360deg); }
}

.loading-text {
	font-size: 32rpx;
	color: #5A6C7D;
	font-weight: 600;
	animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
	0%, 100% { opacity: 1; }
	50% { opacity: 0.5; }
}
</style>

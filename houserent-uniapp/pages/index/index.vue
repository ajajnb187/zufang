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
	background: #f5f7fa;
}

.loading-text {
	font-size: 32rpx;
	color: #999;
}
</style>

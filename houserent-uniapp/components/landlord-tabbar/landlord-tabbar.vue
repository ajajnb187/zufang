<template>
	<view class="landlord-tabbar">
		<view class="tab-item" :class="{ active: current === 'houses' }" @click="switchTab('houses')">
			<text class="tab-icon">🏠</text>
			<text class="tab-text">房源</text>
		</view>
		<view class="tab-item" :class="{ active: current === 'tenants' }" @click="switchTab('tenants')">
			<text class="tab-icon">👥</text>
			<text class="tab-text">租客</text>
		</view>
		<view class="tab-item" :class="{ active: current === 'chat' }" @click="switchTab('chat')">
			<text class="tab-icon">📨</text>
			<text class="tab-text">消息</text>
		</view>
		<view class="tab-item" :class="{ active: current === 'profile' }" @click="switchTab('profile')">
			<text class="tab-icon">👤</text>
			<text class="tab-text">我的</text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'LandlordTabbar',
	props: {
		current: {
			type: String,
			default: 'houses'
		}
	},
	methods: {
		switchTab(tab) {
			if (tab === this.current) return
			
			const routes = {
				houses: '/pages/landlord/houses/houses',
				tenants: '/pages/landlord/tenants/tenants',
				chat: '/pages/chat/chat',
				profile: '/pages/landlord/profile/profile'
			}
			
			uni.reLaunch({ url: routes[tab] })
		}
	}
}
</script>

<style scoped>
.landlord-tabbar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 100rpx;
	display: flex;
	background: #fff;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
	padding-bottom: env(safe-area-inset-bottom);
	z-index: 999;
}

.tab-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 10rpx 0;
}

.tab-icon {
	font-size: 40rpx;
	margin-bottom: 4rpx;
}

.tab-text {
	font-size: 22rpx;
	color: #999;
}

.tab-item.active .tab-text {
	color: #409eff;
}
</style>

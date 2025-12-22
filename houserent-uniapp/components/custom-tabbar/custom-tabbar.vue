<template>
	<view class="tabbar-container" v-if="showTabBar">
		<view class="tabbar">
			<view 
				v-for="(item, index) in tabBarList" 
				:key="index" 
				class="tabbar-item"
				@click="switchTab(item.pagePath, index)"
			>
				<image 
					class="tabbar-icon" 
					:src="currentIndex === index ? item.selectedIconPath : item.iconPath" 
					mode="aspectFit"
				></image>
				<text 
					class="tabbar-text"
					:style="{ color: currentIndex === index ? selectedColor : color }"
				>
					{{ item.text }}
				</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getUserType, USER_TYPE } from '@/utils/userType.js';

const _sfc_main = {
	data() {
		return {
			currentIndex: 0,
			color: '#999999',
			selectedColor: '#667eea',
			showTabBar: true,
			tabBarList: [],
			// 租户TabBar配置
			tenantTabBar: [
				{
					pagePath: '/pages/home/home',
					text: '找房',
					iconPath: '/static/tabbar/home.png',
					selectedIconPath: '/static/tabbar/home-active.png'
				},
				{
					pagePath: '/pages/favorite/favorite',
					text: '收藏',
					iconPath: '/static/tabbar/chat.png',
					selectedIconPath: '/static/tabbar/chat-active.png'
				},
				{
					pagePath: '/pages/chat/chat',
					text: '消息',
					iconPath: '/static/tabbar/chat.png',
					selectedIconPath: '/static/tabbar/chat-active.png'
				},
				{
					pagePath: '/pages/profile/profile',
					text: '我的',
					iconPath: '/static/tabbar/profile.png',
					selectedIconPath: '/static/tabbar/profile-active.png'
				}
			],
			// 房东TabBar配置
			landlordTabBar: [
				{
					pagePath: '/pages/landlord/houses/houses',
					text: '房源',
					iconPath: '/static/tabbar/home.png',
					selectedIconPath: '/static/tabbar/home-active.png'
				},
				{
					pagePath: '/pages/chat/chat',
					text: '消息',
					iconPath: '/static/tabbar/chat.png',
					selectedIconPath: '/static/tabbar/chat-active.png'
				},
				{
					pagePath: '/pages/profile/profile',
					text: '我的',
					iconPath: '/static/tabbar/profile.png',
					selectedIconPath: '/static/tabbar/profile-active.png'
				}
			]
		};
	},
	
	created() {
		this.initTabBar();
	},
	
	mounted() {
		// mounted时再次初始化，确保能获取到当前页面信息
		this.initTabBar();
	},
	
	methods: {
		// 初始化TabBar
		initTabBar() {
			const userType = getUserType();
			
			// 根据用户类型设置TabBar
			if (userType === USER_TYPE.LANDLORD) {
				// 房东
				this.tabBarList = this.landlordTabBar;
			} else if (userType === USER_TYPE.TENANT) {
				// 租户
				this.tabBarList = this.tenantTabBar;
			} else {
				// 未登录，默认显示租户TabBar
				this.tabBarList = this.tenantTabBar;
			}
			
			// 延迟设置当前选中的tab，确保页面已加载
			this.$nextTick(() => {
				this.setCurrentTab();
			});
		},
		
		// 设置当前选中的tab
		setCurrentTab() {
			const pages = getCurrentPages();
			if (pages.length === 0) return;
			
			const currentPage = pages[pages.length - 1];
			const currentRoute = '/' + currentPage.route;
			
			// 查找当前页面在tabBar中的索引
			const index = this.tabBarList.findIndex(item => item.pagePath === currentRoute);
			if (index !== -1) {
				this.currentIndex = index;
				this.showTabBar = true;
			} else {
				// 不在tabBar页面，隐藏tabBar
				this.showTabBar = false;
			}
		},
		
		// 切换Tab
		switchTab(pagePath, index) {
			if (this.currentIndex === index) return;
			
			// 重新获取用户类型，确保最新状态
			const currentUserType = getUserType();
			
			// 如果是租客，强制跳转到租客首页
			if (currentUserType === USER_TYPE.TENANT && pagePath.includes('landlord')) {
				uni.showToast({
					title: '您没有访问权限',
					icon: 'none',
					duration: 800
				});
				uni.reLaunch({
					url: '/pages/home/home'
				});
				return;
			}
			
			uni.reLaunch({
				url: pagePath,
				success: () => {
					this.currentIndex = index;
				}
			});
		}
	}
};

// 兼容的导出方式
if (typeof module !== 'undefined' && module.exports) {
  module.exports = _sfc_main;
} else {
  export default _sfc_main;
}

<style lang="scss" scoped>
.tabbar-container {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	z-index: 9999;
	background: #ffffff;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
	padding-bottom: constant(safe-area-inset-bottom);
	padding-bottom: env(safe-area-inset-bottom);
}

.tabbar {
	display: flex;
	height: 100rpx;
	background: #ffffff;
}

.tabbar-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 10rpx 0;
}

.tabbar-icon {
	width: 50rpx;
	height: 50rpx;
	margin-bottom: 6rpx;
}

.tabbar-text {
	font-size: 22rpx;
}
</style>

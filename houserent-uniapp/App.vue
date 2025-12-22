<script>
	import initRouteInterceptor from '@/utils/routeInterceptor.js'
	import { canUseApp, checkUserStatus } from '@/utils/userStatus.js'
	
	export default {
		onLaunch: function() {
			console.log('App Launch')
			
			// 注册全局路由拦截器
			initRouteInterceptor()
		},
		onShow: async function() {
			console.log('App Show')
			
			// 检查用户是否已登录
			const token = uni.getStorageSync('token')
			if (token) {
				// 检查用户状态（封禁检查）
				const canUse = await canUseApp()
				if (!canUse) {
					console.log('用户已被封禁，无法使用应用')
					return
				}
				
				// 更新用户状态信息
				checkUserStatus()
			}
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style lang="scss">
	/* Global Styles */
	view, text, image, scroll-view, input, button {
		box-sizing: border-box;
	}

	page {
		background-color: #f5f7fa;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
		color: #333;
		font-size: 28rpx;
		line-height: 1.5;
	}

	/* Utility Classes */
	.page-container {
		min-height: 100vh;
		background-color: #f5f7fa;
	}

	.card {
		background: #ffffff;
		border-radius: 24rpx;
		padding: 24rpx;
		margin: 24rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
	}

	.flex-center {
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.flex-between {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.text-primary {
		color: #409eff;
	}

	.text-success {
		color: #67c23a;
	}

	.text-warning {
		color: #e6a23c;
	}

	.text-danger {
		color: #f56c6c;
	}

	.text-info {
		color: #909399;
	}
	
	/* Button Overrides */
	button::after {
		border: none;
	}
</style>

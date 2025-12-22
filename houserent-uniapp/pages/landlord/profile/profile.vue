<template>
	<view class="landlord-profile-page">
		<view class="user-card" @click="showEditProfile">
			<view class="avatar-wrapper">
				<image class="avatar" :src="getAvatar()" mode="aspectFill"></image>
				<view class="edit-badge">编辑</view>
			</view>
			<view class="user-info">
				<view class="nickname">{{ userInfo.nickname || '未登录' }}</view>
				<view class="user-type">房东</view>
				<view class="edit-tip">点击修改个人信息</view>
			</view>
		</view>
		
		<!-- 快捷操作 -->
		<view class="quick-actions">
			<view class="action-grid">
				<view class="action-card" @click="goPage('/pages/landlord/publish/publish')">
					<view class="action-icon">➕</view>
					<text class="action-title">发布房源</text>
				</view>
				<view class="action-card" @click="goPage('/pages/landlord/appointments/appointments')">
					<view class="action-icon">📅</view>
					<text class="action-title">预约管理</text>
				</view>
				<view class="action-card" @click="goPage('/pages/landlord/contract-create/contract-create')">
					<view class="action-icon">📄</view>
					<text class="action-title">发起合同</text>
				</view>
			</view>
		</view>
		
		<!-- 房东功能菜单 -->
		<view class="menu-section">
			<view class="menu-item" @click="goPage('/pages/landlord/revenue/revenue')">
				<text class="menu-icon">💰</text>
				<text class="menu-label">收益统计</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="goPage('/pages/landlord/contracts/contracts')">
				<text class="menu-icon">📝</text>
				<text class="menu-label">合同管理</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="goPage('/pages/community/auth/auth')">
				<text class="menu-icon">🏠</text>
				<text class="menu-label">小区认证</text>
				<text class="menu-arrow">›</text>
			</view>
		</view>
		
		<!-- 设置 -->
<!-- 		<view class="menu-section">
			<view class="menu-item" @click="switchToTenant">
				<text class="menu-icon">🔄</text>
				<text class="menu-label">切换到租客视角</text>
				<text class="menu-arrow">›</text>
			</view>
		</view> -->
		
		<view class="logout-btn" @click="logout">
			<text>退出登录</text>
		</view>
		
		<!-- 编辑个人信息弹窗 -->
		<view class="edit-modal" v-if="showEditModal" @click="closeEditModal">
			<view class="edit-content" @click.stop>
				<view class="edit-header">
					<text class="edit-title">编辑个人信息</text>
					<text class="close-btn" @click="closeEditModal">×</text>
				</view>
				
				<view class="edit-form">
					<!-- 头像选择 -->
					<view class="form-item avatar-item">
						<text class="form-label">头像</text>
						<image class="avatar-preview" :src="getEditAvatar()" mode="aspectFill"></image>
						<view class="avatar-btns">
							<button class="avatar-btn wx-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">微信头像</button>
							<button class="avatar-btn album-btn" @click="chooseFromAlbum">相册选择</button>
						</view>
					</view>
					
					<!-- 昵称输入 -->
					<view class="form-item">
						<text class="form-label">昵称</text>
						<input class="form-input" type="nickname" v-model="editForm.nickname" 
							placeholder="请输入昵称" @blur="onNicknameBlur" @input="onNicknameInput" />
					</view>
					
					<!-- 手机号输入 -->
					<view class="form-item">
						<text class="form-label">手机号</text>
						<input class="form-input" type="number" v-model="editForm.phone" 
							placeholder="请输入手机号" maxlength="11" />
					</view>
				</view>
				
				<view class="edit-actions">
					<button class="cancel-btn" @click="closeEditModal">取消</button>
					<button class="save-btn" @click="saveProfile" :loading="saving">保存</button>
				</view>
			</view>
		</view>
		
		<!-- 房东底部导航栏 -->
		<view class="tabbar">
			<view class="tabbar-item" @click="goToHouses">
				<text class="tabbar-icon">🏠</text>
				<text class="tabbar-text">房源</text>
			</view>
			<view class="tabbar-item" @click="goToTenants">
				<text class="tabbar-icon">👥</text>
				<text class="tabbar-text">租客</text>
			</view>
			<view class="tabbar-item" @click="goToChat">
				<text class="tabbar-icon">💬</text>
				<text class="tabbar-text">消息</text>
			</view>
			<view class="tabbar-item active">
				<text class="tabbar-icon">👤</text>
				<text class="tabbar-text">我的</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getUserInfo, setUserInfo, clearStorage, getToken } from '@/utils/storage.js'
import api from '@/utils/api.js'

export default {
	data() {
		return {
			userInfo: {},
			showEditModal: false,
			saving: false,
			editForm: {
				nickname: '',
				avatarUrl: '',
				phone: ''
			},
			tempAvatarPath: '' // 临时头像路径
		}
	},
	
	onLoad() {
		console.log('【房东个人中心】onLoad被调用，页面开始加载');
		this.userInfo = getUserInfo() || {}
	},
	
	onShow() {
		this.loadUserInfo()
	},
	
	onPullDownRefresh() {
		this.loadUserInfo()
		setTimeout(() => {
			uni.stopPullDownRefresh()
		}, 500)
	},
	
	methods: {
		loadUserInfo() {
			this.userInfo = getUserInfo() || {}
		},
		
		getAvatar() {
			const defaultAvatar = 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'
			return this.userInfo.avatarUrl || defaultAvatar
		},
		
		// 获取编辑弹窗中的头像
		getEditAvatar() {
			const defaultAvatar = 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'
			// 优先使用临时头像，其次编辑表单中的头像，最后默认头像
			if (this.tempAvatarPath) {
				return this.tempAvatarPath
			}
			if (this.editForm.avatarUrl) {
				return this.editForm.avatarUrl
			}
			return defaultAvatar
		},
		
		// 显示编辑弹窗
		showEditProfile() {
			this.editForm.nickname = this.userInfo.nickname || ''
			this.editForm.avatarUrl = this.userInfo.avatarUrl || ''
			this.editForm.phone = this.userInfo.phone || ''
			this.tempAvatarPath = ''
			this.showEditModal = true
		},
		
		closeEditModal() {
			this.showEditModal = false
		},
		
		// 选择微信头像
		onChooseAvatar(e) {
			const tempPath = e.detail.avatarUrl
			if (tempPath) {
				this.tempAvatarPath = tempPath
				console.log('选择了微信头像:', tempPath)
			}
		},
		
		// 从相册选择头像
		chooseFromAlbum() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album'],
				success: (res) => {
					const tempPath = res.tempFilePaths[0]
					this.tempAvatarPath = tempPath
					console.log('从相册选择了头像:', tempPath)
				},
				fail: (err) => {
					console.error('选择图片失败:', err)
				}
			})
		},
		
		// 昵称输入
		onNicknameInput(e) {
			this.editForm.nickname = e.detail.value
		},
		
		onNicknameBlur(e) {
			if (e.detail.value) {
				this.editForm.nickname = e.detail.value
			}
		},
		
		// 保存个人信息
		async saveProfile() {
			if (!this.editForm.nickname.trim()) {
				return uni.showToast({ title: '请输入昵称', icon: 'none' })
			}
			
			this.saving = true
			try {
				let avatarUrl = this.editForm.avatarUrl
				
				// 如果有新选择的临时头像，先上传
				if (this.tempAvatarPath) {
					console.log('上传头像:', this.tempAvatarPath)
					try {
						avatarUrl = await this.uploadAvatar(this.tempAvatarPath)
						console.log('头像上传成功:', avatarUrl)
					} catch (uploadErr) {
						console.error('头像上传失败:', uploadErr)
						this.saving = false
						return uni.showToast({ title: '头像上传失败', icon: 'none' })
					}
				}
				
				// 验证手机号格式
				if (this.editForm.phone && !/^1[3-9]\d{9}$/.test(this.editForm.phone)) {
					this.saving = false
					return uni.showToast({ title: '手机号格式不正确', icon: 'none' })
				}
				
				// 更新后端
				const res = await api.user.updateProfile({
					nickname: this.editForm.nickname,
					avatarUrl: avatarUrl,
					phone: this.editForm.phone
				})
				
				if (res.code === 200) {
					// 更新本地存储
					const updatedUser = { ...this.userInfo, nickname: this.editForm.nickname, avatarUrl: avatarUrl, phone: this.editForm.phone }
					setUserInfo(updatedUser)
					this.userInfo = updatedUser
					
					uni.showToast({ title: '保存成功', icon: 'success' })
					this.closeEditModal()
				} else {
					uni.showToast({ title: res.message || '保存失败', icon: 'none' })
				}
			} catch (e) {
				console.error('保存个人信息失败:', e)
				uni.showToast({ title: '保存失败', icon: 'none' })
			} finally {
				this.saving = false
			}
		},
		
		// 上传头像
		async uploadAvatar(tempPath) {
			return new Promise((resolve, reject) => {
				const token = getToken()
				const uploadUrl = `${api.baseUrl.replace('/api', '')}/api/upload/avatar`
				console.log('上传地址:', uploadUrl)
				console.log('文件路径:', tempPath)
				
				uni.uploadFile({
					url: uploadUrl,
					filePath: tempPath,
					name: 'file',
					header: {
						'Authorization': `Bearer ${token}`
					},
					success: (uploadRes) => {
						console.log('上传响应:', uploadRes)
						try {
							const data = JSON.parse(uploadRes.data)
							if (data.code === 200) {
								resolve(data.data)
							} else {
								console.error('上传业务错误:', data)
								reject(new Error(data.message || '上传失败'))
							}
						} catch (e) {
							console.error('解析响应失败:', e, uploadRes.data)
							reject(e)
						}
					},
					fail: (err) => {
						console.error('上传请求失败:', err)
						reject(err)
					}
				})
			})
		},
		
		goPage(url) {
			uni.navigateTo({ url })
		},
		
		switchToTenant() {
			uni.showModal({
				title: '切换视角',
				content: '确定要切换到租客视角吗？',
				success: (res) => {
					if (res.confirm) {
						uni.reLaunch({ url: '/pages/home/home' })
					}
				}
			})
		},
		
		logout() {
			uni.showModal({
				title: '退出登录',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						clearStorage()
						uni.reLaunch({ url: '/pages/login/login' })
					}
				}
			})
		},
		
		// 底部导航
		goToHouses() {
			uni.reLaunch({ url: '/pages/landlord/houses/houses' })
		},
		goToTenants() {
			uni.reLaunch({ url: '/pages/landlord/tenants/tenants' })
		},
		goToChat() {
			uni.reLaunch({ url: '/pages/landlord/chat/chat' })
		}
	}
}
</script>

<style scoped>

.landlord-profile-page {
	min-height: 100vh;
	background: #F7F9FC;
	padding-bottom: 120rpx;
}

.user-card {
	margin: 24rpx;
	padding: 28rpx;
	background: #FFFFFF;
	border-radius: 20rpx;
	display: flex;
	align-items: center;
	border: 1rpx solid #EEF2F7;
	box-shadow: 0 6rpx 18rpx rgba(17, 24, 39, 0.06);
}

.avatar-wrapper {
	position: relative;
	margin-right: 22rpx;
}

.avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	background: #F7F9FC;
	border: 4rpx solid #FFF3ED;
}

.edit-badge {
	position: absolute;
	right: -6rpx;
	bottom: -6rpx;
	background: #FF6B35;
	color: #FFFFFF;
	font-size: 20rpx;
	padding: 4rpx 14rpx;
	border-radius: 999rpx;
}

.user-info {
	flex: 1;
}

.nickname {
	font-size: 36rpx;
	font-weight: 700;
	color: #1F2937;
	margin-bottom: 10rpx;
}

.user-type {
	display: inline-block;
	font-size: 24rpx;
	font-weight: 600;
	color: #FF6B35;
	background: #FFF3ED;
	padding: 6rpx 18rpx;
	border-radius: 999rpx;
}

.edit-tip {
	font-size: 24rpx;
	color: #8B95A5;
	margin-top: 10rpx;
}

.quick-actions {
	margin: 0 24rpx 24rpx;
}

.action-grid {
	display: flex;
	gap: 18rpx;
}

.action-card {
	flex: 1;
	height: 180rpx;
	background: #FFFFFF;
	border-radius: 20rpx;
	padding: 20rpx 12rpx;
	border: 1rpx solid #EEF2F7;
	box-shadow: 0 6rpx 18rpx rgba(17, 24, 39, 0.06);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	box-sizing: border-box;
}

.action-card:active {
	transform: scale(0.99);
}

.action-card.primary {
	background: #35ff5aff;
	border-color: #FF6B35;
}

.action-icon {
	font-size: 44rpx;
	margin-bottom: 12rpx;
}

.action-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #1F2937;
}

.action-desc {
	font-size: 24rpx;
	color: #8B95A5;
	margin-top: 6rpx;
}

.action-card.primary .action-icon,
.action-card.primary .action-title,
.action-card.primary .action-desc {
	color: #FFFFFF;
}

.action-row {
	display: flex;
	gap: 18rpx;
	margin-top: 18rpx;
}

.action-card.small {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 28rpx 16rpx;
}

.action-card.small .action-icon {
	margin-bottom: 8rpx;
}

.action-card.small .action-title {
	font-size: 28rpx;
}

.menu-section {
	margin: 0 24rpx 24rpx;
	background: #FFFFFF;
	border-radius: 20rpx;
	border: 1rpx solid #EEF2F7;
	overflow: hidden;
}

.menu-item {
	display: flex;
	align-items: center;
	padding: 28rpx 22rpx;
	border-bottom: 1rpx solid #F3F6FB;
}

.menu-item:active {
	background: #F9FAFB;
}

.menu-item:last-child {
	border-bottom: none;
}

.menu-icon {
	width: 60rpx;
	text-align: center;
	font-size: 42rpx;
	margin-right: 18rpx;
}

.menu-label {
	flex: 1;
	font-size: 30rpx;
	color: #1F2937;
	font-weight: 500;
}

.menu-arrow {
	font-size: 36rpx;
	color: #C0C4CC;
}

.logout-btn {
	margin: 0 24rpx;
	padding: 28rpx;
	background: #FFFFFF;
	border-radius: 20rpx;
	text-align: center;
	font-size: 30rpx;
	color: #FF4D4F;
	font-weight: 600;
	border: 1rpx solid #EEF2F7;
}

.logout-btn:active {
	background: #FFF1F0;
}

.tabbar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 100rpx;
	display: flex;
	background: #FFFFFF;
	border-top: 1rpx solid #EEF2F7;
	padding-bottom: env(safe-area-inset-bottom);
	z-index: 999;
}

.tabbar-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.tabbar-icon {
	font-size: 44rpx;
	margin-bottom: 4rpx;
}

.tabbar-text {
	font-size: 22rpx;
	color: #8B95A5;
	font-weight: 500;
}

.tabbar-item.active .tabbar-text {
	color: #FF6B35;
	font-weight: 600;
}

.edit-modal {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	z-index: 9999;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx;
}

.edit-content {
	width: 100%;
	max-height: 80vh;
	background: #FFFFFF;
	border-radius: 20rpx;
	overflow: hidden;
}

.edit-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 24rpx;
	border-bottom: 1rpx solid #EEF2F7;
}

.edit-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #1F2937;
}

.close-btn {
	font-size: 48rpx;
	color: #C0C4CC;
	line-height: 1;
}

.edit-form {
	padding: 24rpx;
}

.form-item {
	margin-bottom: 24rpx;
}

.form-label {
	display: block;
	font-size: 26rpx;
	color: #5A6C7D;
	margin-bottom: 12rpx;
}

.form-input {
	width: 100%;
	height: 84rpx;
	background: #F7F9FC;
	border-radius: 14rpx;
	padding: 0 22rpx;
	font-size: 28rpx;
	border: 1rpx solid #EEF2F7;
}

.avatar-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.avatar-preview {
	width: 140rpx;
	height: 140rpx;
	border-radius: 50%;
	background: #F7F9FC;
	border: 4rpx solid #FFF3ED;
	margin-bottom: 16rpx;
}

.avatar-btns {
	display: flex;
	gap: 16rpx;
}

.avatar-btn {
	height: 64rpx;
	padding: 0 20rpx;
	border-radius: 999rpx;
	font-size: 24rpx;
	line-height: 64rpx;
}

.avatar-btn::after {
	border: none;
}

.wx-btn {
	background: #07C160;
	color: #FFFFFF;
}

.album-btn {
	background: #FF6B35;
	color: #FFFFFF;
}

.edit-actions {
	display: flex;
	gap: 16rpx;
	padding: 20rpx 24rpx 28rpx;
	border-top: 1rpx solid #EEF2F7;
}

.cancel-btn, .save-btn {
	flex: 1;
	height: 76rpx;
	border-radius: 999rpx;
	font-size: 28rpx;
	font-weight: 600;
	display: flex;
	align-items: center;
	justify-content: center;
}

.cancel-btn {
	background: #F7F9FC;
	color: #5A6C7D;
}

.save-btn {
	background: #FF6B35;
	color: #FFFFFF;
}

.cancel-btn::after, .save-btn::after {
	border: none;
}
</style>

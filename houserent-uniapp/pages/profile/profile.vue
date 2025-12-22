<template>
	<view class="profile-page">
		<view class="user-card" @click="showEditProfile">
			<view class="avatar-wrapper">
				<image class="avatar" :src="getAvatar()" mode="aspectFill"></image>
				<view class="edit-badge">编辑</view>
			</view>
			<view class="user-info">
				<view class="nickname">{{ userInfo.nickname || '未登录' }}</view>
				<view class="user-type">{{ userInfo.userType === 4 ? '房东' : '租客' }}</view>
				<view class="edit-tip">点击修改个人信息</view>
			</view>
		</view>
		
		<view class="menu-section">
			<view class="menu-item" @click="goPage('/pages/favorite/favorite')">
				<text class="menu-icon">❤️</text>
				<text class="menu-label">我的收藏</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="goPage('/pages/contract/list/list')">
				<text class="menu-icon">📄</text>
				<text class="menu-label">我的合同</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="goPage('/pages/rental/my-rental')">
				<text class="menu-icon">🏡</text>
				<text class="menu-label">我的租赁</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="goPage('/pages/community/auth/auth')">
				<text class="menu-icon">🏘</text>
				<text class="menu-label">小区认证</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="goPage('/pages/community/facilities/facilities')">
				<text class="menu-icon">🏪</text>
				<text class="menu-label">配套查询</text>
				<text class="menu-arrow">›</text>
			</view>
						<view class="menu-item" v-if="userInfo.userType === 4" @click="goPage('/pages/landlord/tenants/tenants')">
				<text class="menu-icon">👥</text>
				<text class="menu-label">租客管理</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" v-if="userInfo.userType === 4" @click="goPage('/pages/landlord/revenue/revenue')">
				<text class="menu-icon">💰</text>
				<text class="menu-label">收益统计</text>
				<text class="menu-arrow">›</text>
			</view>
		<!-- 	<view class="menu-item" v-if="userInfo.userType === 4" @click="switchToTenant">
				<text class="menu-icon">🏠</text>
				<text class="menu-label">切换到租客视角</text>
				<text class="menu-arrow">›</text>
			</view> -->
		</view>
		
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
			tempAvatarPath: ''
		}
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
			uni.reLaunch({ url: '/pages/home/home' })
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
		}
	}
}
</script>

<style scoped>
.profile-page {
	min-height: 100vh;
	background: #F7F9FC;
}

.user-card {
	display: flex;
	align-items: center;
	background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 50%, #4ECDC4 100%);
	padding: 80rpx 40rpx 60rpx;
	color: #FFFFFF;
	position: relative;
	overflow: hidden;
}

.user-card::before {
	content: '';
	position: absolute;
	top: -100rpx;
	right: -100rpx;
	width: 300rpx;
	height: 300rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.1);
}

.user-card::after {
	content: '';
	position: absolute;
	bottom: -80rpx;
	left: -80rpx;
	width: 250rpx;
	height: 250rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.08);
}

.avatar {
	width: 140rpx;
	height: 140rpx;
	border-radius: 50%;
	margin-right: 32rpx;
	border: 6rpx solid rgba(255, 255, 255, 0.3);
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.2);
	position: relative;
	z-index: 1;
}

.user-info {
	flex: 1;
	position: relative;
	z-index: 1;
}

.nickname {
	font-size: 40rpx;
	font-weight: 800;
	margin-bottom: 12rpx;
	text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.user-type {
	font-size: 26rpx;
	opacity: 0.95;
	background: rgba(255, 255, 255, 0.25);
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	display: inline-block;
	backdrop-filter: blur(10rpx);
}

.menu-section {
	background: #FFFFFF;
	margin: 24rpx 30rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	overflow: hidden;
}

.menu-item {
	display: flex;
	align-items: center;
	padding: 36rpx 32rpx;
	border-bottom: 1rpx solid #F7F9FC;
	transition: all 0.3s ease;
	position: relative;
}

.menu-item:last-child {
	border-bottom: none;
}

.menu-item:active {
	background: #F7F9FC;
}

.menu-item::after {
	content: '';
	position: absolute;
	left: 0;
	top: 50%;
	transform: translateY(-50%);
	width: 6rpx;
	height: 0;
	background: linear-gradient(180deg, #FF6B35, #4ECDC4);
	transition: height 0.3s ease;
}

.menu-item:active::after {
	height: 60%;
}

.menu-icon {
	font-size: 44rpx;
	margin-right: 24rpx;
	filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}

.menu-label {
	flex: 1;
	font-size: 32rpx;
	color: #2C3E50;
	font-weight: 600;
}

.menu-arrow {
	font-size: 44rpx;
	color: #E4E7ED;
}

.logout-btn {
	background: #FFFFFF;
	margin: 40rpx 30rpx;
	padding: 36rpx;
	border-radius: 48rpx;
	text-align: center;
	font-size: 32rpx;
	color: #F5222D;
	font-weight: 700;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	transition: all 0.3s ease;
}

.logout-btn:active {
	background: #FFF5F5;
	transform: scale(0.98);
}

/* 头像编辑相关 */
.avatar-wrapper {
	position: relative;
	margin-right: 32rpx;
}

.edit-badge {
	position: absolute;
	bottom: 0;
	right: 0;
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	font-size: 20rpx;
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
	font-weight: 600;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.4);
}

.edit-tip {
	font-size: 24rpx;
	opacity: 0.9;
	margin-top: 8rpx;
	text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

/* 编辑弹窗 */
.edit-modal {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(44, 62, 80, 0.7);
	backdrop-filter: blur(10rpx);
	z-index: 9999;
	display: flex;
	align-items: center;
	justify-content: center;
	animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
	from { opacity: 0; }
	to { opacity: 1; }
}

.edit-content {
	width: 85%;
	background: #FFFFFF;
	border-radius: 32rpx;
	overflow: hidden;
	box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.2);
	animation: slideUp 0.4s ease;
}

@keyframes slideUp {
	from { transform: translateY(100rpx); opacity: 0; }
	to { transform: translateY(0); opacity: 1; }
}

.edit-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 36rpx 40rpx;
	border-bottom: 1rpx solid #F2F6FC;
	background: linear-gradient(135deg, #FFF5F0, #FFFFFF);
}

.edit-title {
	font-size: 36rpx;
	font-weight: 700;
	color: #2C3E50;
}

.close-btn {
	font-size: 52rpx;
	color: #8B95A5;
	line-height: 1;
	transition: color 0.3s ease;
}

.close-btn:active {
	color: #5A6C7D;
}

.edit-form {
	padding: 30rpx;
}

.form-item {
	margin-bottom: 30rpx;
}

.form-label {
	display: block;
	font-size: 28rpx;
	color: #666;
	margin-bottom: 16rpx;
}

.form-input {
	width: 100%;
	height: 88rpx;
	background: #F7F9FC;
	border: 2rpx solid #E4E7ED;
	border-radius: 16rpx;
	padding: 0 28rpx;
	font-size: 30rpx;
	color: #2C3E50;
	transition: all 0.3s ease;
}

.form-input:focus {
	background: #FFFFFF;
	border-color: #FF6B35;
	box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
}

.avatar-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.avatar-choose-btn {
	display: flex;
	flex-direction: column;
	align-items: center;
	background: transparent;
	border: none;
	padding: 0;
}

.avatar-choose-btn::after {
	display: none;
}

.avatar-preview {
	width: 140rpx;
	height: 140rpx;
	border-radius: 50%;
	border: 6rpx solid #FFE5D9;
	margin-bottom: 20rpx;
	box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.2);
}

.avatar-btns {
	display: flex;
	gap: 20rpx;
	margin-top: 12rpx;
}

.avatar-btn {
	padding: 12rpx 24rpx;
	font-size: 24rpx;
	border-radius: 30rpx;
	line-height: 1.5;
}

.avatar-btn::after {
	display: none;
}

.wx-btn {
	background: linear-gradient(135deg, #07C160, #06AE56);
	color: #FFFFFF;
	box-shadow: 0 4rpx 12rpx rgba(7, 193, 96, 0.3);
}

.album-btn {
	background: linear-gradient(135deg, #4ECDC4, #44A3D5);
	color: #FFFFFF;
	box-shadow: 0 4rpx 12rpx rgba(78, 205, 196, 0.3);
}

.edit-actions {
	display: flex;
	padding: 30rpx;
	gap: 20rpx;
}

.cancel-btn, .save-btn {
	flex: 1;
	height: 88rpx;
	border-radius: 48rpx;
	font-size: 32rpx;
	font-weight: 700;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s ease;
}

.cancel-btn {
	background: #F7F9FC;
	color: #5A6C7D;
	border: 2rpx solid #E4E7ED;
}

.cancel-btn:active {
	background: #EEF2F6;
}

.save-btn {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.35);
}

.save-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}
</style>

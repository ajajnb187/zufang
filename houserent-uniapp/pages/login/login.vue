<template>
	<view class="login-container">
		<!-- 真机：微注册插件登录弹窗 -->
		<view class="login-modal" v-if="showLoginModal && !isDevTools">
			<login :modal="loginModal" @success="loginSuccess" @fail="loginFail" @cancel="loginCancel"></login>
		</view>
		
		<!-- 模拟器/开发工具：手动选择头像昵称弹窗 -->
		<view class="profile-setup-modal" v-if="showProfileSetup">
			<view class="setup-content">
				<view class="setup-header">
					<text class="setup-title">完善个人信息</text>
					<text class="setup-subtitle">设置头像和昵称，让其他用户更容易识别您</text>
				</view>
				
				<view class="setup-form">
					<!-- 头像选择 -->
					<view class="avatar-section">
						<text class="section-label">头像</text>
						<button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
							<image class="avatar-preview" :src="tempAvatarUrl" mode="aspectFill"></image>
							<view class="avatar-edit-icon">📷</view>
						</button>
					</view>
					
					<!-- 昵称输入 -->
					<view class="nickname-section">
						<text class="section-label">昵称</text>
						<input 
							class="nickname-input" 
							type="nickname" 
							v-model="tempNickname"
							placeholder="点击输入昵称"
							@blur="onNicknameBlur"
						/>
					</view>
				</view>
				
				<view class="setup-actions">
					<button class="confirm-btn" @tap="confirmProfile">完成设置</button>
					<button class="skip-btn" @tap="skipProfile">暂不设置</button>
				</view>
			</view>
		</view>
		
		<!-- 欢迎页面 -->
		<view class="welcome-page" v-if="!showLoginModal && !showProfileSetup">
			<!-- 装饰背景 -->
			<view class="welcome-bg-circle1"></view>
			<view class="welcome-bg-circle2"></view>
			
			<view class="welcome-content">
				<!-- Logo区域 -->
				<view class="logo-section">
					<view class="logo-box">
						<image class="logo-image" src="/static/logo.png" mode="aspectFit"></image>
					</view>
					<text class="app-name">稳住租房</text>
					<text class="app-slogan">理想生活，从这里开始</text>
				</view>
				
				<!-- 按钮区域 -->
				<view class="button-section">
					<button class="primary-btn" @tap="handleWechatLogin">
						<text class="icon">🚀</text>
						微信一键登录
					</button>
					
					<!-- 开发环境测试登录 -->
					<view class="dev-login-section">
						<text class="dev-title">开发测试登录</text>
						<view class="dev-buttons">
							<button class="dev-btn tenant" @tap="devLoginAsTenant">
								🏠 租客登录
							</button>
							<button class="dev-btn landlord" @tap="devLoginAsLandlord">
								🔑 房东登录
							</button>
						</view>
					</view>
				</view>
				
				<view class="footer-agreement">
					<text class="agreement-text">登录即代表同意</text>
					<text class="link" @tap="showAgreement('user')">《用户协议》</text>
					<text class="agreement-text">和</text>
					<text class="link" @tap="showAgreement('privacy')">《隐私政策》</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'
import { getToken, setToken, getUserInfo, setUserInfo } from '@/utils/storage.js'

export default {
	data() {
		return {
			isDevTools: false, // 是否为开发工具/模拟器
			showLoginModal: false,
			showProfileSetup: false,
			tempAvatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
			tempNickname: '',
			pendingUserInfo: null,
			// 微注册插件配置 - 对话框模式
			loginModal: {
				type: 0,
				canIUse: typeof wx !== 'undefined' && wx.canIUse ? wx.canIUse('functional-page-navigator') : false,
				title: '微信授权登录',
				content: '授权登录后，即可获取您的微信头像和昵称',
				confirmText: '立即登录',
				cancelText: '暂不登录',
				confirmStyle: {
					color: '#409eff',
					backgroundColor: '#ffffff'
				},
				cancelStyle: {
					color: '#999999',
					backgroundColor: '#ffffff'
				}
			}
		};
	},
	
	onLoad() {
		this.detectEnvironment();
		this.checkLoginStatus();
	},
	
	methods: {
		// 检测运行环境（真机 vs 模拟器/开发工具）
		detectEnvironment() {
			try {
				const systemInfo = uni.getSystemInfoSync();
				// platform为devtools表示开发工具，Windows/mac表示PC模拟器
				this.isDevTools = systemInfo.platform === 'devtools' || 
					systemInfo.platform === 'windows' || 
					systemInfo.platform === 'mac';
				console.log('【登录页】运行环境:', systemInfo.platform, '是否开发工具:', this.isDevTools);
			} catch (e) {
				this.isDevTools = true; // 出错时默认使用手动选择
			}
		},
		
		checkLoginStatus() {
			const token = getToken();
			const userInfo = getUserInfo();
			
			if (token && userInfo && userInfo.userType) {
				this.redirectByUserType(userInfo.userType);
			}
		},
		
		redirectByUserType(type) {
			console.log('【登录页】根据用户类型跳转，userType:', type, '类型:', typeof type);
			// 确保类型转换，防止字符串和数字比较问题
			const userType = parseInt(type);
			console.log('【登录页】转换后的userType:', userType);
			
			if (userType === 3) {
				console.log('【登录页】租客用户，跳转到首页');
				console.log('【登录页】执行 uni.reLaunch 到 /pages/home/home');
				uni.reLaunch({ 
					url: '/pages/home/home',
					success: () => {
						console.log('【登录页】租客页面跳转成功');
					},
					fail: (err) => {
						console.error('【登录页】租客页面跳转失败:', err);
					}
				});
			} else if (userType === 4) {
				console.log('【登录页】房东用户，跳转到房源管理页');
				console.log('【登录页】执行 uni.reLaunch 到 /pages/landlord/houses/houses');
				uni.reLaunch({ 
					url: '/pages/landlord/houses/houses',
					success: () => {
						console.log('【登录页】房东页面跳转成功');
					},
					fail: (err) => {
						console.error('【登录页】房东页面跳转失败:', err);
					}
				});
			} else {
				console.log('【登录页】未知用户类型:', userType, '默认跳转到首页');
				console.log('【登录页】执行 uni.reLaunch 到 /pages/home/home (默认)');
				uni.reLaunch({ 
					url: '/pages/home/home',
					success: () => {
						console.log('【登录页】默认页面跳转成功');
					},
					fail: (err) => {
						console.error('【登录页】默认页面跳转失败:', err);
					}
				});
			}
		},
		
		// 点击微信登录按钮
		async handleWechatLogin() {
			console.log('【登录页】点击微信登录，isDevTools:', this.isDevTools);
			
			if (this.isDevTools) {
				// 模拟器/开发工具：先登录后端，再判断是否需要设置头像昵称
				await this.doBackendLogin();
			} else {
				// 真机：显示微注册插件弹窗
				this.showLoginModal = true;
			}
		},
		
		// 插件登录成功回调 - 获取到真实微信头像和昵称（真机）
		async loginSuccess(res) {
			console.log('【登录页】插件登录成功:', res);
			this.showLoginModal = false;
			
			const wechatInfo = res.detail?.res || res.target?.res || {};
			const avatarUrl = wechatInfo.avatarUrl;
			const nickName = wechatInfo.nickName;
			
			console.log('【登录页】获取到微信头像:', avatarUrl);
			console.log('【登录页】获取到微信昵称:', nickName);
			
			// 执行后端登录流程
			await this.doBackendLogin(avatarUrl, nickName);
		},
		
		// 插件登录失败回调（真机）
		async loginFail(res) {
			console.log('【登录页】插件登录失败:', res);
			this.showLoginModal = false;
			
			// 某些手机会失败，使用默认值继续登录
			const wechatInfo = res.detail?.res || res.target?.res || {};
			const avatarUrl = wechatInfo.avatarUrl;
			const nickName = wechatInfo.nickName;
			
			await this.doBackendLogin(avatarUrl, nickName);
		},
		
		// 用户取消登录
		loginCancel(res) {
			console.log('【登录页】用户取消登录:', res);
			this.showLoginModal = false;
		},
		
		// 头像选择回调（模拟器手动选择）
		onChooseAvatar(e) {
			const { avatarUrl } = e.detail;
			console.log('【登录页】选择头像:', avatarUrl);
			this.tempAvatarUrl = avatarUrl;
		},
		
		// 昵称blur回调（模拟器手动输入）
		onNicknameBlur(e) {
			if (e.detail.value) {
				this.tempNickname = e.detail.value;
				console.log('【登录页】昵称blur:', this.tempNickname);
			}
		},
		
		// 确认设置头像昵称（模拟器）
		async confirmProfile() {
			if (!this.tempNickname || !this.tempNickname.trim()) {
				uni.showToast({ title: '请输入昵称', icon: 'none' });
				return;
			}
			
			await this.saveProfileAndFinish();
		},
		
		// 跳过设置（模拟器）
		async skipProfile() {
			this.showProfileSetup = false;
			if (this.pendingUserInfo) {
				setUserInfo(this.pendingUserInfo);
				uni.showToast({ title: '登录成功', icon: 'success' });
				setTimeout(() => {
					this.redirectByUserType(this.pendingUserInfo.userType);
				}, 1500);
			}
		},
		
		// 保存头像昵称并完成登录
		async saveProfileAndFinish() {
			try {
				uni.showLoading({ title: '保存中...' });
				
				let finalAvatarUrl = this.tempAvatarUrl;
				
				// 如果是临时头像路径，需要先上传到MinIO
				if (this.tempAvatarUrl.startsWith('http://tmp') || this.tempAvatarUrl.startsWith('wxfile://')) {
					const uploadRes = await this.uploadAvatarToServer(this.tempAvatarUrl);
					if (uploadRes) {
						finalAvatarUrl = uploadRes;
					}
				}
				
				// 调用后端API更新用户信息
				const updateRes = await api.user.updateProfile({
					nickname: this.tempNickname.trim(),
					avatarUrl: finalAvatarUrl
				});
				
				uni.hideLoading();
				
				if (updateRes && updateRes.code === 200) {
					const userInfo = {
						...this.pendingUserInfo,
						nickname: this.tempNickname.trim(),
						avatarUrl: finalAvatarUrl
					};
					setUserInfo(userInfo);
					this.showProfileSetup = false;
					
					uni.showToast({ title: '设置成功', icon: 'success' });
					setTimeout(() => {
						this.redirectByUserType(userInfo.userType);
					}, 1500);
				} else {
					throw new Error(updateRes?.message || '保存失败');
				}
			} catch (error) {
				uni.hideLoading();
				console.error('【登录页】保存个人信息失败:', error);
				uni.showToast({ title: error.message || '保存失败', icon: 'none' });
			}
		},
		
		// 上传头像到服务器（MinIO）
		async uploadAvatarToServer(tempFilePath) {
			return new Promise((resolve) => {
				const token = getToken();
				uni.uploadFile({
					url: `${api.baseUrl}/upload/avatar`,
					filePath: tempFilePath,
					name: 'file',
					header: {
						'Authorization': token ? `Bearer ${token}` : ''
					},
					success: (res) => {
						try {
							const data = JSON.parse(res.data);
							if (data.code === 200 && data.data) {
								console.log('【登录页】头像上传成功:', data.data);
								resolve(data.data);
							} else {
								console.error('【登录页】头像上传失败:', data);
								resolve(null);
							}
						} catch (e) {
							console.error('【登录页】解析上传响应失败:', e);
							resolve(null);
						}
					},
					fail: (err) => {
						console.error('【登录页】头像上传失败:', err);
						resolve(null);
					}
				});
			});
		},
		
		// 执行后端登录流程
		async doBackendLogin(avatarUrl, nickName) {
			try {
				uni.showLoading({ title: '登录中...' });
				
				// 1. 调用uni.login获取code
				const loginRes = await new Promise((resolve, reject) => {
					uni.login({
						provider: 'weixin',
						success: resolve,
						fail: reject
					});
				});
				
				if (!loginRes.code) {
					throw new Error('获取微信授权码失败');
				}
				
				// 2. 调用后端接口登录
				const res = await api.auth.wechatLogin(loginRes.code);
				
				if (res && res.code === 200) {
					const token = res.data;
					console.log('【登录页】后端返回token:', token);
					setToken(token);
					
					await new Promise(resolve => setTimeout(resolve, 300));
					
					// 3. 获取用户信息
					const userRes = await api.user.getProfile();
					
					if (userRes && userRes.code === 200) {
						const userInfo = userRes.data;
						console.log('【登录页】用户信息:', userInfo);
						
						// 4. 判断是否已有头像昵称（已设置过则直接跳转）
						const hasProfile = userInfo.nickname && 
							userInfo.nickname !== '微信用户' && 
							userInfo.nickname !== '' &&
							userInfo.avatarUrl &&
							!userInfo.avatarUrl.includes('icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg');
						
						if (hasProfile) {
							// 已有头像昵称，直接登录成功
							console.log('【登录页】用户已有头像昵称，直接跳转');
							setUserInfo(userInfo);
							uni.hideLoading();
							uni.showToast({ title: '登录成功', icon: 'success' });
							setTimeout(() => {
								this.redirectByUserType(userInfo.userType);
							}, 1500);
						} else if (avatarUrl && nickName && nickName !== '微信用户') {
							// 真机插件获取到了头像昵称，保存到后端
							console.log('【登录页】保存插件获取的头像昵称');
							
							// 如果是临时路径，先上传
							let finalAvatarUrl = avatarUrl;
							if (avatarUrl.startsWith('http://tmp') || avatarUrl.startsWith('wxfile://')) {
								const uploadRes = await this.uploadAvatarToServer(avatarUrl);
								if (uploadRes) {
									finalAvatarUrl = uploadRes;
								}
							}
							
							await api.user.updateProfile({
								nickname: nickName,
								avatarUrl: finalAvatarUrl
							});
							
							userInfo.nickname = nickName;
							userInfo.avatarUrl = finalAvatarUrl;
							setUserInfo(userInfo);
							
							uni.hideLoading();
							uni.showToast({ title: '登录成功', icon: 'success' });
							setTimeout(() => {
								this.redirectByUserType(userInfo.userType);
							}, 1500);
						} else {
							// 需要手动设置头像昵称（模拟器场景）
							console.log('【登录页】需要手动设置头像昵称');
							this.pendingUserInfo = userInfo;
							uni.hideLoading();
							this.showProfileSetup = true;
						}
					} else {
						throw new Error('获取用户信息失败');
					}
				} else {
					throw new Error('登录失败');
				}
			} catch (error) {
				uni.hideLoading();
				console.error('【登录页】登录异常:', error);
				uni.showToast({ title: error.message || '登录失败', icon: 'none' });
			}
		},
		
		showAgreement(type) {
			uni.showToast({ title: '查看协议', icon: 'none' });
		},
		
		// 开发环境 - 租客登录
		async devLoginAsTenant() {
			await this.devLogin(3, '租客')
		},
		
		// 开发环境 - 房东登录
		async devLoginAsLandlord() {
			await this.devLogin(4, '房东')
		},
		
		// 开发环境登录通用方法
		async devLogin(userType, roleName) {
			try {
				uni.showLoading({ title: `${roleName}登录中...` })
				
				// 获取测试账户列表
				const testAccountsRes = await api.auth.getTestAccounts()
				console.log(`【${roleName}登录】测试账户列表:`, testAccountsRes)
				
				if (testAccountsRes && testAccountsRes.code === 200) {
					const accounts = testAccountsRes.data || []
					const account = accounts.find(a => a.userType === userType)
					console.log(`【${roleName}登录】找到账户:`, account)
					
					if (account) {
						const loginRes = await api.auth.devLogin(account.userId)
						console.log(`【${roleName}登录】登录响应:`, loginRes)
						
						if (loginRes && loginRes.code === 200) {
							// 后端返回 { token: "xxx", user: {...} }
							const { token, user } = loginRes.data
							console.log(`【${roleName}登录】token:`, token)
							console.log(`【${roleName}登录】user:`, user)
							
							// 保存token和用户信息
							setToken(token)
							setUserInfo(user)
							
							uni.hideLoading()
							uni.showToast({ title: `${roleName}登录成功`, icon: 'success' })
							
							setTimeout(() => {
								this.redirectByUserType(user.userType)
							}, 1500)
						} else {
							throw new Error(loginRes?.message || '登录失败')
						}
					} else {
						throw new Error(`未找到${roleName}测试账户`)
					}
				} else {
					throw new Error('获取测试账户失败')
				}
			} catch (error) {
				uni.hideLoading()
				console.error(`【${roleName}登录】异常:`, error)
				uni.showToast({ title: error.message || '登录失败', icon: 'none' })
			}
		},
		
		// 房东登录功能
		async handleLandlordLogin() {
			try {
				uni.showLoading({ title: '获取房东账户...' });
				
				// 1. 先获取测试账户列表
				const testAccountsRes = await api.auth.getTestAccounts();
				
				if (testAccountsRes && testAccountsRes.code === 200) {
					const accounts = testAccountsRes.data || [];
					// 找到房东类型的测试账户
					const landlordAccount = accounts.find(account => account.userType === 4);
					
					if (landlordAccount) {
						// 2. 使用房东账户ID进行开发登录
						const loginRes = await api.auth.devLogin(landlordAccount.userId);
						
						if (loginRes && loginRes.code === 200) {
							// 后端返回 { token: "xxx", user: {...} }
							const { token, user } = loginRes.data;
							console.log('【房东登录】后端返回token:', token);
							console.log('【房东登录】后端返回user:', user);
							
							// 3. 保存token和用户信息
							setToken(token);
							setUserInfo(user);
							
							uni.hideLoading();
							uni.showToast({ title: '房东登录成功', icon: 'success' });
							
							setTimeout(() => {
								this.redirectByUserType(user.userType);
							}, 1500);
						} else {
							throw new Error('房东登录失败');
						}
					} else {
						throw new Error('未找到房东测试账户');
					}
				} else {
					throw new Error('获取测试账户失败');
				}
			} catch (error) {
				uni.hideLoading();
				console.error('【房东登录】登录异常:', error);
				uni.showToast({ 
					title: error.message || '房东登录失败，请重试', 
					icon: 'none' 
				});
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.login-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #FFE5D9 0%, #FFF5F0 50%, #E8F5F3 100%);
	position: relative;
	overflow: hidden;
}

/* 微注册插件登录弹窗 */
.login-modal {
	position: fixed;
	top: 0;
	left: 0;
	z-index: 99999999;
	width: 100%;
	height: 100%;
	background-color: rgba(44, 62, 80, 0.7);
	backdrop-filter: blur(10rpx);
	display: flex;
	align-items: center;
	justify-content: center;
}

/* 模拟器手动设置头像昵称弹窗 */
.profile-setup-modal {
	position: fixed;
	top: 0;
	left: 0;
	z-index: 99999;
	width: 100%;
	height: 100%;
	background-color: rgba(44, 62, 80, 0.7);
	backdrop-filter: blur(10rpx);
	display: flex;
	align-items: center;
	justify-content: center;
	animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
	from { opacity: 0; }
	to { opacity: 1; }
}

.setup-content {
	width: 90%;
	max-width: 600rpx;
	background: #ffffff;
	border-radius: 32rpx;
	padding: 48rpx;
	box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
	animation: slideUp 0.4s ease;
}

@keyframes slideUp {
	from { transform: translateY(100rpx); opacity: 0; }
	to { transform: translateY(0); opacity: 1; }
}

.setup-header {
	text-align: center;
	margin-bottom: 40rpx;
}

.setup-title {
	display: block;
	font-size: 40rpx;
	font-weight: 700;
	color: #2C3E50;
	margin-bottom: 16rpx;
}

.setup-subtitle {
	display: block;
	font-size: 26rpx;
	color: #8B95A5;
	line-height: 1.6;
}

.setup-form {
	margin-bottom: 40rpx;
}

.avatar-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 30rpx;
}

.section-label {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
	margin-bottom: 20rpx;
}

.avatar-btn {
	position: relative;
	width: 140rpx;
	height: 140rpx;
	padding: 0;
	background: transparent;
	border: none;
}

.avatar-btn::after {
	display: none;
}

.avatar-preview {
	width: 140rpx;
	height: 140rpx;
	border-radius: 50%;
	border: 6rpx solid #FFE5D9;
	box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.2);
}

.avatar-edit-icon {
	position: absolute;
	right: 0;
	bottom: 0;
	width: 48rpx;
	height: 48rpx;
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}

.nickname-section {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.nickname-input {
	width: 100%;
	height: 88rpx;
	background: #F7F9FC;
	border: 2rpx solid #E4E7ED;
	border-radius: 16rpx;
	padding: 0 28rpx;
	font-size: 30rpx;
	color: #2C3E50;
	text-align: center;
	transition: all 0.3s ease;
}

.nickname-input:focus {
	background: #FFFFFF;
	border-color: #FF6B35;
	box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
}

.setup-actions {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.confirm-btn {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: white;
	border-radius: 48rpx;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
	transition: all 0.3s ease;
}

.confirm-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}

.skip-btn {
	background: transparent;
	color: #8B95A5;
	border: none;
	font-size: 28rpx;
	transition: color 0.3s ease;
}

.skip-btn:active {
	color: #5A6C7D;
}

.skip-btn::after {
	display: none;
}

/* Welcome Page */
.welcome-page {
	position: relative;
	height: 100vh;
	overflow: hidden;
	background: linear-gradient(135deg, #FFE5D9 0%, #FFF5F0 50%, #E8F5F3 100%);
}

.welcome-bg-circle1 {
	position: absolute;
	top: -150rpx;
	right: -150rpx;
	width: 500rpx;
	height: 500rpx;
	border-radius: 50%;
	background: radial-gradient(circle, rgba(255, 107, 53, 0.15), transparent);
	animation: float 6s ease-in-out infinite;
}

@keyframes float {
	0%, 100% { transform: translate(0, 0) scale(1); }
	50% { transform: translate(-20rpx, 20rpx) scale(1.05); }
}

.welcome-bg-circle2 {
	position: absolute;
	bottom: -100rpx;
	left: -100rpx;
	width: 400rpx;
	height: 400rpx;
	border-radius: 50%;
	background: radial-gradient(circle, rgba(78, 205, 196, 0.12), transparent);
	animation: float 8s ease-in-out infinite reverse;
}

.welcome-content {
	position: relative;
	z-index: 1;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 0 60rpx;
}

.logo-section {
	margin-top: 200rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.logo-box {
	width: 200rpx;
	height: 200rpx;
	background: #ffffff;
	border-radius: 50rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 20rpx 60rpx rgba(255, 107, 53, 0.2);
	margin-bottom: 48rpx;
	animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
	0%, 100% { transform: translateY(0); }
	50% { transform: translateY(-10rpx); }
}

.logo-image {
	width: 140rpx;
	height: 140rpx;
}

.app-name {
	font-size: 56rpx;
	font-weight: 800;
	color: #2C3E50;
	margin-bottom: 20rpx;
	letter-spacing: 3rpx;
	background: linear-gradient(135deg, #FF6B35, #4ECDC4);
	-webkit-background-clip: text;
	-webkit-text-fill-color: transparent;
	background-clip: text;
}

.app-slogan {
	font-size: 30rpx;
	color: #5A6C7D;
	letter-spacing: 2rpx;
	font-weight: 500;
}

.button-section {
	width: 100%;
	margin-top: auto;
	margin-bottom: 60rpx;
	padding: 0 60rpx;
}

.primary-btn {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: white;
	border-radius: 60rpx;
	height: 108rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 34rpx;
	font-weight: 700;
	box-shadow: 0 12rpx 40rpx rgba(255, 107, 53, 0.35);
	margin-bottom: 30rpx;
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.primary-btn::before {
	content: '';
	position: absolute;
	top: 0;
	left: -100%;
	width: 100%;
	height: 100%;
	background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
	transition: left 0.5s;
}

.primary-btn:active::before {
	left: 100%;
}

.primary-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
}

.primary-btn .icon {
	margin-right: 16rpx;
	font-size: 40rpx;
}

.secondary-btn {
	background: white;
	color: #409eff;
	border-radius: 60rpx;
	height: 100rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	font-weight: 600;
	border: 2rpx solid #e0f2fe;
}

.footer-agreement {
	margin-bottom: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-wrap: wrap;
}

.agreement-text {
	font-size: 24rpx;
	color: #8B95A5;
}

.link {
	font-size: 24rpx;
	color: #FF6B35;
	font-weight: 500;
}

/* Form Page */
.form-page {
	min-height: 100vh;
	background: #ffffff;
}

.nav-header {
	padding: 88rpx 30rpx 20rpx;
}

.back-icon {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 40rpx;
	color: #333;
}

.form-content {
	padding: 20rpx 60rpx;
}

.form-header {
	margin-bottom: 60rpx;
}

.form-header .title {
	font-size: 48rpx;
	font-weight: bold;
	color: #333;
	display: block;
	margin-bottom: 16rpx;
}

.form-header .subtitle {
	font-size: 28rpx;
	color: #909399;
}

.role-selector {
	display: flex;
	gap: 30rpx;
	margin-bottom: 80rpx;
	padding: 0 60rpx;
}

.role-card {
	flex: 1;
	height: 160rpx;
	background: #f5f7fa;
	border-radius: 24rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	border: 2rpx solid transparent;
	position: relative;
	transition: all 0.3s;
}

.role-card.active {
	background: #ecf5ff;
	border-color: #409eff;
}

.role-icon {
	font-size: 48rpx;
	margin-bottom: 16rpx;
}

.role-name {
	font-size: 28rpx;
	color: #606266;
	font-weight: 500;
}

.role-card.active .role-name {
	color: #409eff;
}

.check-mark {
	position: absolute;
	top: 12rpx;
	right: 12rpx;
	width: 32rpx;
	height: 32rpx;
	background: #409eff;
	border-radius: 50%;
	color: white;
	font-size: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.input-form {
	width: 100%;
}

.input-group {
	margin-bottom: 40rpx;
}

.input-label {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
	margin-bottom: 20rpx;
	display: block;
}

.custom-input {
	height: 100rpx;
	background: #f5f7fa;
	border-radius: 16rpx;
	padding: 0 30rpx;
	font-size: 30rpx;
	color: #333;
}

.password-wrapper {
	position: relative;
}

.eye-icon {
	position: absolute;
	right: 0;
	top: 0;
	height: 100%;
	width: 100rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #909399;
}

.placeholder-style {
	color: #c0c4cc;
}

.form-extras {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 60rpx;
}

.remember-me {
	display: flex;
	align-items: center;
	font-size: 26rpx;
	color: #606266;
}

.checkbox {
	width: 32rpx;
	height: 32rpx;
	border: 2rpx solid #dcdfe6;
	border-radius: 8rpx;
	margin-right: 12rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 20rpx;
	color: white;
}

.checkbox.checked {
	background: #409eff;
	border-color: #409eff;
}

.forgot-pwd {
	font-size: 26rpx;
	color: #909399;
}

.submit-btn {
	background: #409eff;
	color: white;
	border-radius: 50rpx;
	height: 100rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	font-weight: 600;
	box-shadow: 0 10rpx 30rpx rgba(64, 158, 255, 0.3);
	margin-bottom: 40rpx;
}

.register-link {
	text-align: center;
	font-size: 28rpx;
	color: #606266;
}

.register-link .link {
	color: #409eff;
	font-weight: 600;
	margin-left: 10rpx;
}

/* 开发测试登录样式 */
.dev-login-section {
	margin-top: 60rpx;
	padding-top: 40rpx;
	border-top: 1rpx solid #e0e0e0;
}

.dev-title {
	display: block;
	text-align: center;
	font-size: 24rpx;
	color: #999;
	margin-bottom: 30rpx;
}

.dev-buttons {
	display: flex;
	gap: 20rpx;
}

.dev-btn {
	flex: 1;
	height: 88rpx;
	border-radius: 48rpx;
	font-size: 30rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-weight: 600;
	transition: all 0.3s ease;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.dev-btn:active {
	transform: scale(0.98);
}

.dev-btn.tenant {
	background: linear-gradient(135deg, #E8F5E9, #C8E6C9);
	color: #2E7D32;
}

.dev-btn.landlord {
	background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
	color: #E65100;
}
</style>

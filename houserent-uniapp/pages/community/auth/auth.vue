<template>
	<view class="auth-page">
		<!-- 认证状态显示 -->
		<view class="status-card" v-if="authStatus && authStatus.finalStatus">
			<view class="status-header" :class="authStatus.finalStatus">
				<text class="status-icon">{{ getStatusIcon(authStatus.finalStatus) }}</text>
				<text class="status-text">{{ getStatusText(authStatus.finalStatus) }}</text>
			</view>
			<view class="status-community">
				<text>小区：{{ authStatus.communityName || formData.communityName || '-' }}</text>
			</view>
			
			<!-- 审核中状态详情 -->
			<view class="status-detail" v-if="authStatus.finalStatus === 'pending'">
				<view class="detail-item">
					<text class="label">证明类型：</text>
					<text>{{ getProofTypeLabel(authStatus.proofType) }}</text>
				</view>
				<view class="detail-item">
					<text class="label">申请理由：</text>
					<text>{{ authStatus.applyReason }}</text>
				</view>
				<view class="detail-item">
					<text class="label">提交时间：</text>
					<text>{{ formatTime(authStatus.createdAt) }}</text>
				</view>
				<view class="detail-item">
					<text class="label">小区初审：</text>
					<text :class="authStatus.communityAdminStatus">{{ getAuditStatusText(authStatus.communityAdminStatus) }}</text>
				</view>
				<view class="detail-item" v-if="authStatus.communityAdminStatus === 'approved'">
					<text class="label">平台终审：</text>
					<text :class="authStatus.platformAdminStatus">{{ getAuditStatusText(authStatus.platformAdminStatus) }}</text>
				</view>
				<!-- 显示已上传的证明图片 -->
				<view class="proof-preview" v-if="proofImageList.length > 0">
					<text class="label">证明材料：</text>
					<view class="image-list">
						<image v-for="(img, index) in proofImageList" :key="index" :src="img" mode="aspectFill" class="proof-thumb" @click="previewImage(index)"></image>
					</view>
				</view>
			</view>
			
			<!-- 已通过状态 -->
			<view class="status-info success-info" v-if="authStatus.finalStatus === 'approved'">
				<text>🎉 您已通过该小区认证，可以查看联系电话和发布房源</text>
			</view>
			
			<!-- 被拒绝状态 -->
			<view class="status-info reject-info" v-if="authStatus.finalStatus === 'rejected'">
				<text class="reject-reason">驳回原因：{{ authStatus.platformAdminOpinion || authStatus.communityAdminOpinion || '未说明' }}</text>
			</view>
			
			<view class="status-actions">
				<view class="action-btn primary" v-if="authStatus.finalStatus === 'approved'" @click="changeCommunity">认证其他小区</view>
				<view class="action-btn warning" v-if="authStatus.finalStatus === 'pending'" @click="cancelAuth">撤销申请</view>
				<view class="action-btn primary" v-if="authStatus.finalStatus === 'rejected'" @click="resubmit">重新提交</view>
			</view>
		</view>
		
		<!-- 认证表单 - 只有未提交或被拒绝后重新提交时显示 -->
		<view class="form-container" v-if="showForm">
			<view class="form-section">
				<view class="section-title">选择小区 <text class="required">*</text></view>
				<picker mode="selector" :range="communityList" range-key="communityName" @change="onCommunityChange">
					<view class="picker-input">
						<text class="placeholder" v-if="!formData.communityName">请选择小区</text>
						<text v-else>{{ formData.communityName }}</text>
						<text class="arrow">›</text>
					</view>
				</picker>
			</view>
			
			<view class="form-section">
				<view class="section-title">证明类型 <text class="required">*</text></view>
				<picker mode="selector" :range="proofTypeList" range-key="label" @change="onProofTypeChange">
					<view class="picker-input">
						<text class="placeholder" v-if="!formData.proofTypeLabel">请选择证明类型</text>
						<text v-else>{{ formData.proofTypeLabel }}</text>
						<text class="arrow">›</text>
					</view>
				</picker>
			</view>
			
			<view class="form-section">
				<view class="section-title">上传证明材料 <text class="required">*</text></view>
				<view class="upload-tip">请上传{{ formData.proofTypeLabel || '证明材料' }}的照片</view>
				<view class="image-upload">
					<view class="upload-item" v-for="(img, index) in formData.proofImages" :key="index">
						<image class="upload-img" :src="img" mode="aspectFill"></image>
						<view class="delete-btn" @click="deleteImage(index)">×</view>
					</view>
					<view class="upload-btn" @click="chooseImage" v-if="formData.proofImages.length < 3">
						<text class="upload-icon">📷</text>
						<text class="upload-text">上传证明</text>
					</view>
				</view>
			</view>
			
			<view class="form-section">
				<view class="section-title">申请理由 <text class="required">*</text></view>
				<textarea class="textarea" v-model="formData.applyReason" placeholder="请填写申请理由，如：本人是该小区业主/租户..." maxlength="200"></textarea>
			</view>
			
			<view class="submit-btn" @click="submitAuth">提交认证</view>
		</view>
		
		<!-- 认证说明 -->
		<view class="tips-card">
			<view class="tips-title">认证说明</view>
			<view class="tips-item">1. 通过小区认证后，您可以查看和发布该小区的房源信息</view>
			<view class="tips-item">2. 认证需要小区管理员和平台管理员双重审核</view>
			<view class="tips-item">3. 请确保上传的证明材料真实有效</view>
			<view class="tips-item">4. 审核结果将通过消息通知告知</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			authStatus: null,
			showForm: true, // 是否显示表单
			communityList: [],
			proofTypeList: [
				{ value: 'rental_contract', label: '租房合同' },
				{ value: 'property_fee', label: '物业缴费截图' },
				{ value: 'ownership_cert', label: '房产证' },
				{ value: 'other', label: '其他证明' }
			],
			formData: {
				communityId: '',
				communityName: '',
				proofType: '',
				proofTypeLabel: '',
				proofImages: [],
				applyReason: ''
			},
			proofImageList: [] // 已提交的证明图片列表
		}
	},
	
	computed: {
		// 根据认证状态决定是否显示表单
		// showForm() {
		// 	return !this.authStatus || !this.authStatus.finalStatus || this.authStatus.finalStatus === 'rejected'
		// }
	},
	
	onLoad() {
		this.loadCommunityList()
		// 页面加载时检查是否有已提交的认证申请
		this.loadExistingVerification()
	},
	
	onShow() {
		// 每次显示页面时检查认证状态
		this.loadExistingVerification()
	},
	
	methods: {
		// 加载用户已有的认证申请（不限小区）
		async loadExistingVerification() {
			try {
				// 获取用户的认证历史，查找最新的pending或approved的认证
				const res = await api.communityVerification.getHistory({ pageNum: 1, pageSize: 10 })
				if (res.code === 200 && res.data) {
					const records = res.data.records || res.data || []
					// 查找最新的pending或approved的认证
					const activeVerification = records.find(v => 
						v.finalStatus === 'pending' || v.finalStatus === 'approved'
					)
					
					if (activeVerification) {
						// 有进行中或已通过的认证，加载详情
						this.formData.communityId = activeVerification.communityId
						this.formData.communityName = activeVerification.communityName || ''
						await this.loadAuthStatus()
					} else {
						// 没有进行中的认证，显示表单
						this.authStatus = null
						this.showForm = true
					}
				} else {
					this.showForm = true
				}
			} catch (e) {
				console.error('加载认证历史失败:', e)
				this.showForm = true
			}
		},
		
		async loadAuthStatus() {
			// 只有选择了小区才加载该小区的认证状态
			if (!this.formData.communityId) return
			try {
				const res = await api.communityVerification.getStatus(this.formData.communityId)
				if (res.code === 200 && res.data) {
					this.authStatus = res.data
					// 解析证明图片
					if (res.data.proofImages) {
						try {
							this.proofImageList = typeof res.data.proofImages === 'string' 
								? JSON.parse(res.data.proofImages) 
								: res.data.proofImages
						} catch (e) {
							this.proofImageList = []
						}
					}
					// 根据状态决定是否显示表单
					this.showForm = !res.data.finalStatus || res.data.finalStatus === 'rejected'
				} else {
					this.authStatus = null
					this.showForm = true
				}
			} catch (e) {
				console.error('加载认证状态失败:', e)
				this.authStatus = null
				this.showForm = true
			}
		},
		
		async loadCommunityList() {
			try {
				const res = await api.community.getList({ pageNum: 1, pageSize: 100 })
				if (res.code === 200) {
					this.communityList = res.data.records || res.data || []
				}
			} catch (e) {
				console.error('加载小区列表失败:', e)
			}
		},
		
		onCommunityChange(e) {
			const index = e.detail.value
			this.formData.communityId = this.communityList[index].communityId
			this.formData.communityName = this.communityList[index].communityName
			// 选择小区后加载该小区的认证状态
			this.loadAuthStatus()
		},
		
		onProofTypeChange(e) {
			const index = e.detail.value
			this.formData.proofType = this.proofTypeList[index].value
			this.formData.proofTypeLabel = this.proofTypeList[index].label
		},
		
		chooseImage() {
			uni.chooseImage({
				count: 3 - this.formData.proofImages.length,
				success: (res) => {
					this.formData.proofImages = [...this.formData.proofImages, ...res.tempFilePaths]
				}
			})
		},
		
		deleteImage(index) {
			this.formData.proofImages.splice(index, 1)
		},
		
		async submitAuth() {
			if (!this.formData.communityId) {
				return uni.showToast({ title: '请选择小区', icon: 'none' })
			}
			if (!this.formData.proofType) {
				return uni.showToast({ title: '请选择证明类型', icon: 'none' })
			}
			if (this.formData.proofImages.length === 0) {
				return uni.showToast({ title: '请上传证明材料', icon: 'none' })
			}
			if (!this.formData.applyReason || this.formData.applyReason.trim().length < 5) {
				return uni.showToast({ title: '请填写申请理由（至少5个字）', icon: 'none' })
			}
			
			try {
				uni.showLoading({ title: '提交中...' })
				
				// 先上传图片到服务器
				const uploadedImages = []
				for (const img of this.formData.proofImages) {
					if (img.startsWith('http')) {
						uploadedImages.push(img)
					} else {
						// 上传本地图片
						const uploadRes = await this.uploadImage(img)
						if (uploadRes) {
							uploadedImages.push(uploadRes)
						}
					}
				}
				
				const data = {
					communityId: this.formData.communityId,
					proofType: this.formData.proofType,
					proofImages: uploadedImages,
					applyReason: this.formData.applyReason.trim()
				}
				
				const res = await api.communityVerification.submit(data)
				uni.hideLoading()
				
				if (res.code === 200) {
					uni.showToast({ title: '提交成功，等待审核', icon: 'success' })
					// 提交成功后刷新页面显示提交状态
					setTimeout(() => {
						this.loadExistingVerification()
					}, 1000)
				} else {
					uni.showToast({ title: res.message || '提交失败', icon: 'none' })
				}
			} catch (e) {
				uni.hideLoading()
				console.error('提交认证失败:', e)
				uni.showToast({ title: '提交失败，请重试', icon: 'none' })
			}
		},
		
		async uploadImage(filePath) {
			return new Promise((resolve) => {
				uni.uploadFile({
					url: `${api.baseUrl.replace('/api', '')}/api/files/upload`,
					filePath: filePath,
					name: 'file',
					formData: {
						category: 'proof'  // 证明材料类型
					},
					header: {
						'satoken': uni.getStorageSync('token')
					},
					success: (res) => {
						try {
							const data = JSON.parse(res.data)
							if (data.code === 200 && data.data) {
								// 返回文件URL
								resolve(data.data.fileUrl || data.data)
							} else {
								console.error('上传失败:', data.message)
								resolve(null)
							}
						} catch (e) {
							console.error('解析上传响应失败:', e)
							resolve(null)
						}
					},
					fail: (err) => {
						console.error('上传请求失败:', err)
						resolve(null)
					}
				})
			})
		},
		
		getStatusIcon(status) {
			const icons = {
				pending: '⏳',
				approved: '✅',
				rejected: '❌'
			}
			return icons[status] || '📝'
		},
		
		getStatusText(status) {
			const texts = {
				pending: '审核中',
				approved: '已通过认证',
				rejected: '认证未通过'
			}
			return texts[status] || '待提交'
		},
		
		getAuditStatusText(status) {
			const texts = {
				pending: '待审核',
				approved: '已通过',
				rejected: '已驳回'
			}
			return texts[status] || '待审核'
		},
		
		getProofTypeLabel(type) {
			const map = {
				'rental_contract': '租房合同',
				'property_fee': '物业缴费截图',
				'ownership_cert': '房产证',
				'other': '其他证明'
			}
			return map[type] || type || '未知'
		},
		
		formatTime(time) {
			if (!time) return '-'
			return new Date(time).toLocaleString('zh-CN')
		},
		
		previewImage(index) {
			uni.previewImage({
				urls: this.proofImageList,
				current: index
			})
		},
		
		changeCommunity() {
			// 清空当前状态，显示表单选择其他小区
			this.authStatus = null
			this.showForm = true
			this.formData.communityId = ''
			this.formData.communityName = ''
			this.formData.proofType = ''
			this.formData.proofTypeLabel = ''
			this.formData.proofImages = []
			this.formData.applyReason = ''
			this.proofImageList = []
		},
		
		resubmit() {
			// 被拒绝后重新提交，保留小区信息，清空其他表单
			this.showForm = true
			this.formData.proofType = ''
			this.formData.proofTypeLabel = ''
			this.formData.proofImages = []
			this.formData.applyReason = ''
		},
		
		async cancelAuth() {
			if (!this.authStatus || !this.authStatus.verificationId) {
				return uni.showToast({ title: '无法撤销', icon: 'none' })
			}
			
			uni.showModal({
				title: '确认撤销',
				content: '确定要撤销该认证申请吗？撤销后可以重新提交。',
				success: async (res) => {
					if (res.confirm) {
						try {
							const result = await api.communityVerification.cancel(this.authStatus.verificationId)
							if (result.code === 200) {
								uni.showToast({ title: '已撤销申请', icon: 'success' })
								this.authStatus = null
								this.showForm = true
								this.proofImageList = []
							} else {
								uni.showToast({ title: result.message || '撤销失败', icon: 'none' })
							}
						} catch (e) {
							console.error('撤销失败:', e)
							uni.showToast({ title: '撤销失败', icon: 'none' })
						}
					}
				}
			})
		}
	}
}
</script>

<style scoped>
.auth-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding: 20rpx;
}

.status-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.status-header {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 20rpx;
	border-radius: 12rpx;
}

.status-header.pending {
	background: #fff3e0;
}

.status-header.approved {
	background: #e8f5e9;
}

.status-header.rejected {
	background: #ffebee;
}

.status-icon {
	font-size: 48rpx;
}

.status-text {
	font-size: 32rpx;
	font-weight: 600;
}

.status-info {
	margin-top: 20rpx;
	padding: 20rpx;
	background: #f5f7fa;
	border-radius: 8rpx;
}

.reject-reason {
	font-size: 28rpx;
	color: #666;
}

.status-community {
	margin-top: 16rpx;
	font-size: 28rpx;
	color: #666;
}

.status-detail {
	margin-top: 20rpx;
	padding: 20rpx;
	background: #fafafa;
	border-radius: 8rpx;
}

.detail-item {
	display: flex;
	margin-bottom: 16rpx;
	font-size: 26rpx;
	line-height: 1.6;
}

.detail-item:last-child {
	margin-bottom: 0;
}

.detail-item .label {
	color: #999;
	min-width: 140rpx;
	flex-shrink: 0;
}

.detail-item .pending {
	color: #e6a23c;
}

.detail-item .approved {
	color: #67c23a;
}

.detail-item .rejected {
	color: #f56c6c;
}

.proof-preview {
	margin-top: 16rpx;
}

.proof-preview .label {
	display: block;
	margin-bottom: 12rpx;
	color: #999;
	font-size: 26rpx;
}

.image-list {
	display: flex;
	gap: 16rpx;
	flex-wrap: wrap;
}

.proof-thumb {
	width: 150rpx;
	height: 150rpx;
	border-radius: 8rpx;
	object-fit: cover;
}

.success-info {
	background: #f0f9eb;
	color: #67c23a;
}

.reject-info {
	background: #fef0f0;
}

.status-actions {
	margin-top: 24rpx;
	display: flex;
	justify-content: center;
	gap: 20rpx;
}

.action-btn {
	padding: 16rpx 40rpx;
	border-radius: 8rpx;
	font-size: 28rpx;
}

.action-btn.primary {
	background: #409eff;
	color: #fff;
}

.action-btn.warning {
	background: #e6a23c;
	color: #fff;
}

.form-container {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.form-section {
	margin-bottom: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 20rpx;
}

.required {
	color: #f56c6c;
	margin-left: 4rpx;
}

.picker-input {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 24rpx;
	background: #f5f7fa;
	border-radius: 8rpx;
	font-size: 28rpx;
}

.placeholder {
	color: #999;
}

.arrow {
	font-size: 40rpx;
	color: #ccc;
}

.upload-tip {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 20rpx;
}

.image-upload {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.upload-item {
	position: relative;
	width: 200rpx;
	height: 200rpx;
}

.upload-img {
	width: 100%;
	height: 100%;
	border-radius: 12rpx;
}

.delete-btn {
	position: absolute;
	top: -10rpx;
	right: -10rpx;
	width: 48rpx;
	height: 48rpx;
	background: #f56c6c;
	color: #fff;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 36rpx;
}

.upload-btn {
	width: 200rpx;
	height: 200rpx;
	background: #f5f7fa;
	border: 2rpx dashed #ddd;
	border-radius: 12rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.upload-icon {
	font-size: 60rpx;
	margin-bottom: 12rpx;
}

.upload-text {
	font-size: 24rpx;
	color: #999;
}

.textarea {
	width: 100%;
	min-height: 150rpx;
	padding: 20rpx;
	background: #f5f7fa;
	border-radius: 8rpx;
	font-size: 28rpx;
}

.submit-btn {
	background: #409eff;
	color: #fff;
	text-align: center;
	padding: 32rpx;
	border-radius: 50rpx;
	font-size: 32rpx;
	font-weight: 600;
}

.tips-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
}

.tips-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 20rpx;
}

.tips-item {
	font-size: 26rpx;
	color: #666;
	line-height: 1.8;
	margin-bottom: 12rpx;
}
</style>

<template>
	<view class="contract-detail-page">
		<!-- 导航栏 -->
		<view class="nav-bar">
			<view class="nav-left" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<text class="nav-title">合同详情</text>
			<view class="nav-right"></view>
		</view>
		
		<!-- 加载中 -->
		<view class="loading" v-if="loading">
			<text>加载中...</text>
		</view>
		
		<!-- 合同内容 -->
		<view class="content" v-else-if="contract.contractId">
			<!-- 状态卡片 -->
			<view class="status-card" :class="getStatusClass()">
				<view class="status-header">
					<text class="contract-no">合同编号：{{ contract.contractNo }}</text>
					<view class="status-tag">{{ getStatusText() }}</view>
				</view>
				<view class="status-tips" v-if="getStatusTips()">
					<text>{{ getStatusTips() }}</text>
				</view>
			</view>
			
			<!-- 签署进度 -->
			<view class="sign-progress card" v-if="contract.contractStatus === 'draft'">
				<view class="card-title">签署进度</view>
				<view class="progress-list">
					<view class="progress-item" :class="{ done: contract.landlordSignature }">
						<view class="progress-icon">{{ contract.landlordSignature ? '✓' : '1' }}</view>
						<view class="progress-info">
							<text class="progress-label">房东签署</text>
							<text class="progress-status">{{ contract.landlordSignature ? '已签署' : '待签署' }}</text>
						</view>
					</view>
					<view class="progress-line"></view>
					<view class="progress-item" :class="{ done: contract.tenantSignature }">
						<view class="progress-icon">{{ contract.tenantSignature ? '✓' : '2' }}</view>
						<view class="progress-info">
							<text class="progress-label">租客签署</text>
							<text class="progress-status">{{ contract.tenantSignature ? '已签署' : '待签署' }}</text>
						</view>
					</view>
					<view class="progress-line"></view>
					<view class="progress-item" :class="{ done: contract.auditStatus === 'approved' }">
						<view class="progress-icon">{{ contract.auditStatus === 'approved' ? '✓' : '3' }}</view>
						<view class="progress-info">
							<text class="progress-label">管理员审核</text>
							<text class="progress-status">{{ getAuditStatusText() }}</text>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 房源信息 -->
			<view class="card">
				<view class="card-title">房源信息</view>
				<view class="house-info">
					<image :src="contract.houseImage || '/static/default-house.png'" class="house-img" mode="aspectFill"></image>
					<view class="house-detail">
						<text class="house-title">{{ contract.houseTitle || '房源信息' }}</text>
						<text class="house-address">{{ contract.houseAddress }}</text>
						<view class="house-tags">
							<text class="tag">{{ contract.houseType || '整租' }}</text>
							<text class="tag">{{ contract.houseArea || 0 }}㎡</text>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 租赁双方 -->
			<view class="card">
				<view class="card-title">租赁双方</view>
				<view class="party-list">
					<view class="party-item">
						<view class="party-role">房东（出租方）</view>
						<view class="party-info">
							<image :src="contract.landlordAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'" class="party-avatar"></image>
							<view class="party-detail">
								<text class="party-name">{{ contract.landlordName || '房东' }}</text>
								<text class="party-phone">{{ contract.landlordPhone || '未设置' }}</text>
							</view>
							<view class="sign-status" :class="{ signed: contract.landlordSignature }">
								{{ contract.landlordSignature ? '已签署' : '待签署' }}
							</view>
						</view>
					</view>
					<view class="party-item">
						<view class="party-role">租客（承租方）</view>
						<view class="party-info">
							<image :src="contract.tenantAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'" class="party-avatar"></image>
							<view class="party-detail">
								<text class="party-name">{{ contract.tenantName || '租客' }}</text>
								<text class="party-phone">{{ contract.tenantPhone || '未设置' }}</text>
							</view>
							<view class="sign-status" :class="{ signed: contract.tenantSignature }">
								{{ contract.tenantSignature ? '已签署' : '待签署' }}
							</view>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 租赁条款 -->
			<view class="card">
				<view class="card-title">租赁条款</view>
				<view class="term-list">
					<view class="term-item">
						<text class="term-label">租赁期限</text>
						<text class="term-value">{{ contract.startDate }} 至 {{ contract.endDate }}</text>
					</view>
					<view class="term-item">
						<text class="term-label">月租金</text>
						<text class="term-value price">¥{{ contract.rentPrice || 0 }}</text>
					</view>
					<view class="term-item">
						<text class="term-label">押金</text>
						<text class="term-value price">¥{{ contract.deposit || 0 }}</text>
					</view>
					<view class="term-item">
						<text class="term-label">付款方式</text>
						<text class="term-value">{{ contract.paymentMethod || '押一付三' }}</text>
					</view>
				</view>
			</view>
			
			<!-- 补充条款 -->
			<view class="card" v-if="contract.customContent">
				<view class="card-title">补充条款</view>
				<view class="custom-content">
					<text>{{ contract.customContent }}</text>
				</view>
			</view>
			
			<!-- 签名展示 -->
			<view class="card" v-if="contract.landlordSignature || contract.tenantSignature">
				<view class="card-title">电子签名</view>
				<view class="signature-list">
					<view class="signature-item" v-if="contract.landlordSignature">
						<text class="signature-label">房东签名</text>
						<image :src="contract.landlordSignature" class="signature-img" mode="aspectFit"></image>
						<text class="signature-time">{{ formatTime(contract.landlordSignTime) }}</text>
					</view>
					<view class="signature-item" v-if="contract.tenantSignature">
						<text class="signature-label">租客签名</text>
						<image :src="contract.tenantSignature" class="signature-img" mode="aspectFit"></image>
						<text class="signature-time">{{ formatTime(contract.tenantSignTime) }}</text>
					</view>
				</view>
			</view>
			
			<!-- 审核信息 -->
			<view class="card" v-if="contract.auditOpinion">
				<view class="card-title">审核信息</view>
				<view class="audit-info">
					<view class="audit-item">
						<text class="audit-label">审核状态</text>
						<text class="audit-value" :class="contract.auditStatus">{{ getAuditStatusText() }}</text>
					</view>
					<view class="audit-item" v-if="contract.auditOpinion">
						<text class="audit-label">审核意见</text>
						<text class="audit-value">{{ contract.auditOpinion }}</text>
					</view>
					<view class="audit-item" v-if="contract.auditTime">
						<text class="audit-label">审核时间</text>
						<text class="audit-value">{{ formatTime(contract.auditTime) }}</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 底部操作按钮 -->
		<view class="bottom-actions" v-if="contract.contractId && !loading">
			<!-- 待签署状态 - 房东操作 -->
			<template v-if="isLandlord && !contract.landlordSignature && contract.contractStatus === 'draft'">
				<button class="btn btn-primary" @click="goToSign">签署合同</button>
			</template>
			
			<!-- 待签署状态 - 租客操作 -->
			<template v-if="isTenant && !contract.tenantSignature && contract.contractStatus === 'draft'">
				<button class="btn btn-default" @click="rejectContract">拒绝签署</button>
				<button class="btn btn-primary" @click="goToSign">签署合同</button>
			</template>
			
			<!-- 合同生效后操作 -->
			<template v-if="contract.contractStatus === 'effective'">
				<button class="btn btn-default" @click="contactOther">联系对方</button>
				<button class="btn btn-default" @click="downloadPDF">下载合同</button>
				<button class="btn btn-danger" @click="showTerminateModal = true">申请解约</button>
			</template>
			
			<!-- 查看PDF -->
			<template v-if="contract.pdfUrl">
				<button class="btn btn-default" @click="viewPDF">查看PDF</button>
			</template>
		</view>
		
		<!-- 签名弹窗 -->
		<view class="modal" v-if="showSignModal" @click="showSignModal = false">
			<view class="modal-content sign-modal" @click.stop>
				<view class="modal-header">
					<text class="modal-title">电子签名</text>
					<text class="modal-close" @click="showSignModal = false">×</text>
				</view>
				<view class="modal-body">
					<canvas canvas-id="signCanvas" class="sign-canvas" 
						@touchstart="touchStart" 
						@touchmove="touchMove" 
						@touchend="touchEnd">
					</canvas>
					<view class="sign-actions">
						<button class="btn btn-default" @click="clearSign">清除</button>
						<button class="btn btn-primary" @click="confirmSign">确认签署</button>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 解约弹窗 -->
		<view class="modal" v-if="showTerminateModal" @click="showTerminateModal = false">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">申请解约</text>
					<text class="modal-close" @click="showTerminateModal = false">×</text>
				</view>
				<view class="modal-body">
					<textarea class="reason-input" v-model="terminateReason" placeholder="请输入解约原因..."></textarea>
					<view class="modal-actions">
						<button class="btn btn-default" @click="showTerminateModal = false">取消</button>
						<button class="btn btn-danger" @click="submitTerminate">确认解约</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			contractId: null,
			contract: {},
			loading: true,
			currentUserId: null,
			userType: null,
			
			// 弹窗控制
			showSignModal: false,
			showTerminateModal: false,
			terminateReason: '',
			
			// 签名相关
			signCtx: null,
			signPoints: [],
			isDrawing: false,
			lastX: 0,
			lastY: 0
		}
	},
	
	computed: {
		isLandlord() {
			return this.currentUserId && this.contract.landlordId && 
				   this.currentUserId == this.contract.landlordId
		},
		isTenant() {
			return this.currentUserId && this.contract.tenantId && 
				   this.currentUserId == this.contract.tenantId
		}
	},
	
	onLoad(options) {
		if (options.id) {
			this.contractId = options.id
		}
		this.getCurrentUser()
	},
	
	onShow() {
		if (this.contractId) {
			this.loadContract()
		}
	},
	
	methods: {
		goBack() {
			const pages = getCurrentPages()
			if (pages.length > 1) {
				uni.navigateBack()
			} else {
				// 没有上一页时，跳转到合同列表或首页
				uni.reLaunch({ url: '/pages/landlord/contracts/contracts' })
			}
		},
		
		getCurrentUser() {
			const userInfo = uni.getStorageSync('userInfo')
			if (userInfo) {
				this.currentUserId = userInfo.userId
				this.userType = userInfo.userType
			}
		},
		
		async loadContract() {
			this.loading = true
			try {
				const res = await api.contract.getDetail(this.contractId)
				if (res.code === 200) {
					this.contract = res.data
				} else {
					uni.showToast({ title: res.message || '加载失败', icon: 'none' })
				}
			} catch (e) {
				console.error('加载合同详情失败:', e)
				uni.showToast({ title: '加载失败', icon: 'none' })
			} finally {
				this.loading = false
			}
		},
		
		getStatusClass() {
			const status = this.contract.contractStatus
			if (status === 'effective') return 'status-effective'
			if (status === 'terminated' || status === 'expired') return 'status-ended'
			return 'status-draft'
		},
		
		getStatusText() {
			const statusMap = {
				'draft': '草稿',
				'effective': '生效中',
				'terminated': '已解约',
				'expired': '已到期'
			}
			return statusMap[this.contract.contractStatus] || '未知'
		},
		
		getStatusTips() {
			const status = this.contract.contractStatus
			const auditStatus = this.contract.auditStatus
			
			if (status === 'draft') {
				if (!this.contract.landlordSignature) {
					return '等待房东签署合同'
				}
				if (!this.contract.tenantSignature) {
					return '等待租客签署合同'
				}
				if (auditStatus === 'pending') {
					return '等待管理员审核'
				}
				if (auditStatus === 'rejected') {
					return '审核未通过：' + (this.contract.auditOpinion || '')
				}
			}
			if (status === 'effective') {
				return '合同已生效，请按时履约'
			}
			return ''
		},
		
		getAuditStatusText() {
			const statusMap = {
				'draft': '待提交',
				'pending': '审核中',
				'approved': '已通过',
				'rejected': '已拒绝'
			}
			return statusMap[this.contract.auditStatus] || '待审核'
		},
		
		formatTime(time) {
			if (!time) return ''
			return time.replace('T', ' ').substring(0, 16)
		},
		
		goToSign() {
			this.showSignModal = true
			this.$nextTick(() => {
				this.initCanvas()
			})
		},
		
		initCanvas() {
			this.signCtx = uni.createCanvasContext('signCanvas', this)
			// 设置白色背景
			this.signCtx.setFillStyle('#ffffff')
			this.signCtx.fillRect(0, 0, 600, 300)
			this.signCtx.draw()
			// 设置绘制样式
			this.signCtx.setStrokeStyle('#000000')
			this.signCtx.setLineWidth(3)
			this.signCtx.setLineCap('round')
			this.signCtx.setLineJoin('round')
			this.signPoints = []
		},
		
		touchStart(e) {
			this.isDrawing = true
			const touch = e.touches[0]
			this.lastX = touch.x
			this.lastY = touch.y
			this.signPoints.push({ x: touch.x, y: touch.y })
		},
		
		touchMove(e) {
			if (!this.isDrawing) return
			const touch = e.touches[0]
			this.signPoints.push({ x: touch.x, y: touch.y })
			
			// 绘制线条
			this.signCtx.beginPath()
			this.signCtx.moveTo(this.lastX, this.lastY)
			this.signCtx.lineTo(touch.x, touch.y)
			this.signCtx.stroke()
			this.signCtx.draw(true)
			
			this.lastX = touch.x
			this.lastY = touch.y
		},
		
		touchEnd() {
			this.isDrawing = false
		},
		
		clearSign() {
			// 重新初始化画布
			this.signCtx.setFillStyle('#ffffff')
			this.signCtx.fillRect(0, 0, 600, 300)
			this.signCtx.draw()
			this.signPoints = []
		},
		
		async confirmSign() {
			console.log('签名点数:', this.signPoints.length)
			if (this.signPoints.length < 5) {
				uni.showToast({ title: '请先完成签名', icon: 'none' })
				return
			}
			
			// 获取合同ID
			const contractId = this.contractId || this.contract.contractId
			console.log('签署合同ID:', contractId, 'this.contractId:', this.contractId, 'contract.contractId:', this.contract.contractId)
			
			if (!contractId) {
				uni.showToast({ title: '合同ID不存在', icon: 'none' })
				return
			}
			
			uni.showLoading({ title: '提交中...' })
			
			try {
				// 获取签名图片并转换为Base64
				console.log('开始获取canvas图片...')
				const tempFilePath = await new Promise((resolve, reject) => {
					uni.canvasToTempFilePath({
						canvasId: 'signCanvas',
						fileType: 'png',
						quality: 1,
						success: (res) => {
							console.log('canvas转图片成功:', res.tempFilePath)
							resolve(res.tempFilePath)
						},
						fail: (err) => {
							console.error('canvas转图片失败:', err)
							reject(err)
						}
					}, this)
				})
				
				// 将临时文件转换为Base64
				console.log('开始转换Base64...')
				const signatureData = await new Promise((resolve, reject) => {
					// #ifdef MP-WEIXIN
					const fs = uni.getFileSystemManager()
					fs.readFile({
						filePath: tempFilePath,
						encoding: 'base64',
						success: (res) => {
							console.log('Base64转换成功, 长度:', res.data.length)
							resolve('data:image/png;base64,' + res.data)
						},
						fail: (err) => {
							console.error('Base64转换失败:', err)
							reject(err)
						}
					})
					// #endif
					// #ifndef MP-WEIXIN
					resolve(tempFilePath)
					// #endif
				})
				
				console.log('准备调用API, 角色:', this.isLandlord ? '房东' : '租客')
				
				let apiRes
				if (this.isLandlord) {
					apiRes = await api.contract.landlordSign(contractId, { signatureData })
				} else if (this.isTenant) {
					apiRes = await api.contract.tenantSign(contractId, { signatureData })
				}
				
				console.log('API响应:', apiRes)
				
				uni.hideLoading()
				
				if (apiRes && apiRes.code === 200) {
					uni.showToast({ title: '签署成功', icon: 'success' })
					this.showSignModal = false
					this.loadContract()
				} else {
					uni.showToast({ title: apiRes?.message || '签署失败', icon: 'none' })
				}
			} catch (e) {
				uni.hideLoading()
				console.error('签署失败:', e)
				uni.showToast({ title: '签署失败', icon: 'none' })
			}
		},
		
		async rejectContract() {
			uni.showModal({
				title: '确认拒绝',
				content: '确定要拒绝签署此合同吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							const apiRes = await api.contract.reject(this.contractId)
							if (apiRes.code === 200) {
								uni.showToast({ title: '已拒绝', icon: 'success' })
								setTimeout(() => uni.navigateBack(), 1500)
							}
						} catch (e) {
							console.error('拒绝失败:', e)
						}
					}
				}
			})
		},
		
		contactOther() {
			const phone = this.isLandlord ? this.contract.tenantPhone : this.contract.landlordPhone
			if (phone) {
				uni.makePhoneCall({ phoneNumber: phone })
			} else {
				uni.showToast({ title: '暂无联系方式', icon: 'none' })
			}
		},
		
		async downloadPDF() {
			const contractId = this.contractId || this.contract.contractId
			if (!contractId) {
				uni.showToast({ title: '合同ID不存在', icon: 'none' })
				return
			}
			
			uni.showLoading({ title: 'PDF生成中...' })
			try {
				const res = await api.contract.generatePDF(contractId)
				uni.hideLoading()
				if (res.code === 200 && res.data) {
					// 后端返回相对路径，需要拼接完整URL
					const baseUrl = api.baseUrl.replace('/api', '')
					this.contract.pdfUrl = baseUrl + res.data
					console.log('PDF下载地址:', this.contract.pdfUrl)
					uni.showToast({ title: 'PDF已生成', icon: 'success' })
					// 自动打开查看
					setTimeout(() => this.viewPDF(), 500)
				} else {
					uni.showToast({ title: res.message || 'PDF生成失败', icon: 'none' })
				}
			} catch (e) {
				uni.hideLoading()
				console.error('生成PDF失败:', e)
				uni.showToast({ title: 'PDF生成失败', icon: 'none' })
			}
		},
		
		viewPDF() {
			const pdfUrl = this.contract.pdfUrl
			if (!pdfUrl) {
				uni.showToast({ title: '请先生成PDF', icon: 'none' })
				return
			}
			
			console.log('正在下载PDF:', pdfUrl)
			uni.showLoading({ title: '下载中...' })
			uni.downloadFile({
				url: pdfUrl,
				header: {
					'satoken': uni.getStorageSync('token')
				},
				success: (res) => {
					uni.hideLoading()
					console.log('下载结果:', res)
					if (res.statusCode === 200) {
						uni.openDocument({
							filePath: res.tempFilePath,
							fileType: 'pdf',
							showMenu: true,
							success: () => {
								console.log('打开PDF成功')
							},
							fail: (err) => {
								console.error('打开PDF失败:', err)
								uni.showToast({ title: '打开失败', icon: 'none' })
							}
						})
					} else {
						uni.showToast({ title: '下载失败: ' + res.statusCode, icon: 'none' })
					}
				},
				fail: (err) => {
					uni.hideLoading()
					console.error('下载PDF失败:', err)
					uni.showToast({ title: '下载失败', icon: 'none' })
				}
			})
		},
		
		async submitTerminate() {
			if (!this.terminateReason.trim()) {
				uni.showToast({ title: '请输入解约原因', icon: 'none' })
				return
			}
			
			const contractId = this.contractId || this.contract.contractId
			if (!contractId) {
				uni.showToast({ title: '合同ID不存在', icon: 'none' })
				return
			}
			
			uni.showLoading({ title: '提交中...' })
			try {
				const res = await api.contract.terminate(contractId, { 
					terminationReason: this.terminateReason 
				})
				uni.hideLoading()
				
				if (res.code === 200) {
					uni.showToast({ title: '解约申请已提交', icon: 'success' })
					this.showTerminateModal = false
					this.loadContract()
				} else {
					uni.showToast({ title: res.message || '提交失败', icon: 'none' })
				}
			} catch (e) {
				uni.hideLoading()
				console.error('提交解约失败:', e)
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.contract-detail-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 140rpx;
}

/* 导航栏 */
.nav-bar {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	height: 88rpx;
	background: #fff;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 30rpx;
	padding-top: var(--status-bar-height);
	box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
	z-index: 100;
	
	.nav-left, .nav-right {
		width: 80rpx;
	}
	
	.back-icon {
		font-size: 40rpx;
		color: #333;
	}
	
	.nav-title {
		font-size: 34rpx;
		font-weight: 600;
		color: #333;
	}
}

/* 加载中 */
.loading {
	display: flex;
	align-items: center;
	justify-content: center;
	height: 60vh;
	padding-top: calc(88rpx + var(--status-bar-height));
	color: #999;
}

/* 内容区 */
.content {
	padding: 24rpx;
	padding-top: calc(112rpx + var(--status-bar-height));
}

/* 状态卡片 */
.status-card {
	padding: 32rpx;
	border-radius: 16rpx;
	margin-bottom: 24rpx;
	color: #fff;
	
	&.status-draft {
		background: linear-gradient(135deg, #ffa726, #fb8c00);
	}
	
	&.status-effective {
		background: linear-gradient(135deg, #66bb6a, #43a047);
	}
	
	&.status-ended {
		background: linear-gradient(135deg, #78909c, #546e7a);
	}
	
	.status-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		
		.contract-no {
			font-size: 26rpx;
			opacity: 0.9;
		}
		
		.status-tag {
			padding: 8rpx 20rpx;
			background: rgba(255,255,255,0.2);
			border-radius: 20rpx;
			font-size: 24rpx;
		}
	}
	
	.status-tips {
		margin-top: 20rpx;
		font-size: 28rpx;
		opacity: 0.95;
	}
}

/* 签署进度 */
.sign-progress {
	.progress-list {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx 0;
	}
	
	.progress-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		flex: 1;
		
		.progress-icon {
			width: 60rpx;
			height: 60rpx;
			border-radius: 50%;
			background: #e0e0e0;
			color: #999;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 28rpx;
			font-weight: 600;
		}
		
		.progress-info {
			margin-top: 16rpx;
			text-align: center;
			
			.progress-label {
				display: block;
				font-size: 26rpx;
				color: #333;
			}
			
			.progress-status {
				display: block;
				font-size: 22rpx;
				color: #999;
				margin-top: 4rpx;
			}
		}
		
		&.done {
			.progress-icon {
				background: #4caf50;
				color: #fff;
			}
			
			.progress-status {
				color: #4caf50;
			}
		}
	}
	
	.progress-line {
		width: 60rpx;
		height: 4rpx;
		background: #e0e0e0;
		margin: 0 10rpx;
		margin-bottom: 60rpx;
	}
}

/* 通用卡片 */
.card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 24rpx;
	
	.card-title {
		font-size: 30rpx;
		font-weight: 600;
		color: #333;
		margin-bottom: 24rpx;
		padding-bottom: 16rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}
}

/* 房源信息 */
.house-info {
	display: flex;
	gap: 24rpx;
	
	.house-img {
		width: 160rpx;
		height: 120rpx;
		border-radius: 12rpx;
		background: #f5f5f5;
	}
	
	.house-detail {
		flex: 1;
		
		.house-title {
			display: block;
			font-size: 30rpx;
			font-weight: 600;
			color: #333;
			margin-bottom: 8rpx;
		}
		
		.house-address {
			display: block;
			font-size: 24rpx;
			color: #999;
			margin-bottom: 12rpx;
		}
		
		.house-tags {
			display: flex;
			gap: 12rpx;
			
			.tag {
				padding: 6rpx 16rpx;
				background: #f0f0f0;
				border-radius: 6rpx;
				font-size: 22rpx;
				color: #666;
			}
		}
	}
}

/* 租赁双方 */
.party-list {
	.party-item {
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f5f5f5;
		
		&:last-child {
			border-bottom: none;
		}
		
		.party-role {
			font-size: 24rpx;
			color: #999;
			margin-bottom: 16rpx;
		}
		
		.party-info {
			display: flex;
			align-items: center;
			
			.party-avatar {
				width: 80rpx;
				height: 80rpx;
				border-radius: 50%;
				background: #f5f5f5;
			}
			
			.party-detail {
				flex: 1;
				margin-left: 20rpx;
				
				.party-name {
					display: block;
					font-size: 30rpx;
					font-weight: 600;
					color: #333;
				}
				
				.party-phone {
					display: block;
					font-size: 26rpx;
					color: #666;
					margin-top: 6rpx;
				}
			}
			
			.sign-status {
				padding: 8rpx 20rpx;
				border-radius: 20rpx;
				font-size: 24rpx;
				background: #fff3e0;
				color: #ff9800;
				
				&.signed {
					background: #e8f5e9;
					color: #4caf50;
				}
			}
		}
	}
}

/* 租赁条款 */
.term-list {
	.term-item {
		display: flex;
		justify-content: space-between;
		padding: 16rpx 0;
		border-bottom: 1rpx solid #f5f5f5;
		
		&:last-child {
			border-bottom: none;
		}
		
		.term-label {
			font-size: 28rpx;
			color: #666;
		}
		
		.term-value {
			font-size: 28rpx;
			color: #333;
			font-weight: 500;
			
			&.price {
				color: #f44336;
			}
		}
	}
}

/* 补充条款 */
.custom-content {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
}

/* 签名展示 */
.signature-list {
	display: flex;
	gap: 30rpx;
	
	.signature-item {
		flex: 1;
		text-align: center;
		
		.signature-label {
			display: block;
			font-size: 26rpx;
			color: #666;
			margin-bottom: 16rpx;
		}
		
		.signature-img {
			width: 100%;
			height: 120rpx;
			background: #fafafa;
			border: 1rpx dashed #ddd;
			border-radius: 8rpx;
		}
		
		.signature-time {
			display: block;
			font-size: 22rpx;
			color: #999;
			margin-top: 12rpx;
		}
	}
}

/* 审核信息 */
.audit-info {
	.audit-item {
		display: flex;
		justify-content: space-between;
		padding: 12rpx 0;
		
		.audit-label {
			font-size: 28rpx;
			color: #666;
		}
		
		.audit-value {
			font-size: 28rpx;
			color: #333;
			
			&.approved {
				color: #4caf50;
			}
			
			&.rejected {
				color: #f44336;
			}
			
			&.pending {
				color: #ff9800;
			}
		}
	}
}

/* 底部操作 */
.bottom-actions {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	padding: 20rpx 30rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	display: flex;
	gap: 20rpx;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
	
	.btn {
		flex: 1;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-weight: 600;
		text-align: center;
		border: none;
		
		&::after {
			border: none;
		}
		
		&.btn-primary {
			background: linear-gradient(135deg, #667eea, #764ba2);
			color: #fff;
		}
		
		&.btn-default {
			background: #f5f5f5;
			color: #333;
		}
		
		&.btn-danger {
			background: #f44336;
			color: #fff;
		}
	}
}

/* 弹窗 */
.modal {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0,0,0,0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
	
	.modal-content {
		width: 90%;
		max-width: 650rpx;
		background: #fff;
		border-radius: 24rpx;
		overflow: hidden;
		
		&.sign-modal {
			width: 95%;
			max-width: 700rpx;
		}
		
		.modal-header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 30rpx;
			border-bottom: 1rpx solid #f0f0f0;
			
			.modal-title {
				font-size: 32rpx;
				font-weight: 600;
				color: #333;
			}
			
			.modal-close {
				font-size: 44rpx;
				color: #999;
				line-height: 1;
			}
		}
		
		.modal-body {
			padding: 30rpx;
			
			.sign-canvas {
				width: 100%;
				height: 300rpx;
				background: #fafafa;
				border: 1rpx dashed #ddd;
				border-radius: 12rpx;
			}
			
			.sign-actions {
				display: flex;
				gap: 20rpx;
				margin-top: 30rpx;
			}
			
			.reason-input {
				width: 100%;
				height: 200rpx;
				padding: 20rpx;
				border: 1rpx solid #e0e0e0;
				border-radius: 12rpx;
				font-size: 28rpx;
				box-sizing: border-box;
			}
			
			.modal-actions {
				display: flex;
				gap: 20rpx;
				margin-top: 30rpx;
			}
		}
	}
}
</style>

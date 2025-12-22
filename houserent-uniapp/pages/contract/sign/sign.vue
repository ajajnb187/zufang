<template>
	<view class="sign-page">
		<!-- 合同预览 -->
		<scroll-view class="contract-preview" scroll-y>
			<view class="contract-header">
				<text class="contract-title">房屋租赁合同</text>
				<text class="contract-no">合同编号：{{ contractInfo.contractNo }}</text>
			</view>
			
			<view class="contract-content">
				<view class="clause-section" v-for="(section, index) in contractSections" :key="index">
					<view class="section-title">{{ section.title }}</view>
					<view class="section-content">{{ section.content }}</view>
				</view>
			</view>
		</scroll-view>
		
		<!-- 签名区域 -->
		<view class="sign-section">
			<view class="section-header">
				<text class="sign-label">{{ signType === 'landlord' ? '房东签名' : '租客签名' }}</text>
				<text class="clear-btn" @click="clearSignature">重新签名</text>
			</view>
			
			<canvas class="signature-canvas" canvas-id="signatureCanvas" 
				@touchstart="touchStart" @touchmove="touchMove" @touchend="touchEnd"
				@tap="handleCanvasTap"></canvas>
			
			<view class="sign-tips">
				<text>请使用手指在上方白色区域内签名</text>
			</view>
		</view>
		
		<!-- 确认签署按钮 -->
		<view class="submit-btn" @click="submitSignature">确认签署</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			contractId: '',
			contractInfo: {},
			contractSections: [],
			canvasContext: null,
			isDrawing: false,
			lastX: 0,
			lastY: 0,
			signType: 'tenant' // landlord 或 tenant
		}
	},
	
	onLoad(options) {
		this.contractId = options.id
		this.signType = options.type || 'tenant' // 从路由参数获取签名类型
		this.loadContractDetail()
		this.initCanvas()
	},
	
	methods: {
		async loadContractDetail() {
			try {
				const res = await api.contract.getDetail(this.contractId)
				if (res.code === 200) {
					this.contractInfo = res.data
					this.parseContractContent(res.data.content)
				}
			} catch (e) {
				console.error('加载合同详情失败:', e)
			}
		},
		
		parseContractContent(content) {
			// 解析合同内容为章节
			this.contractSections = [
				{ title: '第一条 房屋基本信息', content: content.houseInfo },
				{ title: '第二条 租赁期限', content: content.term },
				{ title: '第三条 租金及支付方式', content: content.rent },
				{ title: '第四条 双方权利义务', content: content.rights },
				{ title: '第五条 违约责任', content: content.liability },
				{ title: '第六条 其他约定', content: content.other }
			]
		},
		
		initCanvas() {
			this.canvasContext = uni.createCanvasContext('signatureCanvas', this)
			this.canvasContext.setStrokeStyle('#000')
			this.canvasContext.setLineWidth(3)
			this.canvasContext.setLineCap('round')
		},
		
		handleCanvasTap() {
			// Canvas点击事件
		},
		
		touchStart(e) {
			this.isDrawing = true
			this.lastX = e.touches[0].x
			this.lastY = e.touches[0].y
		},
		
		touchMove(e) {
			if (!this.isDrawing) return
			
			const x = e.touches[0].x
			const y = e.touches[0].y
			
			this.canvasContext.beginPath()
			this.canvasContext.moveTo(this.lastX, this.lastY)
			this.canvasContext.lineTo(x, y)
			this.canvasContext.stroke()
			this.canvasContext.draw(true)
			
			this.lastX = x
			this.lastY = y
		},
		
		touchEnd() {
			this.isDrawing = false
		},
		
		clearSignature() {
			this.canvasContext.clearRect(0, 0, 750, 300)
			this.canvasContext.draw()
		},
		
		async submitSignature() {
			try {
				uni.showLoading({ title: '签署中...' })
				
				// 导出签名图片
				const signImage = await new Promise((resolve, reject) => {
					uni.canvasToTempFilePath({
						canvasId: 'signatureCanvas',
						success: (res) => resolve(res.tempFilePath),
						fail: reject
					}, this)
				})
				
				// 上传签名图片到服务器
				const uploadRes = await new Promise((resolve, reject) => {
					uni.uploadFile({
						url: `${api.baseUrl.replace('/api', '')}/api/files/upload`,
						filePath: signImage,
						name: 'file',
						header: {
							'Authorization': `Bearer ${uni.getStorageSync('token')}`
						},
						success: (res) => {
							resolve(JSON.parse(res.data))
						},
						fail: reject
					})
				})
				
				if (uploadRes.code !== 200) {
					throw new Error('签名上传失败')
				}
				
				// 提交签名 - 根据签名类型调用不同API
				const signData = { signatureData: uploadRes.data.url }
				if (this.signType === 'landlord') {
					await api.contract.landlordSign(this.contractId, signData)
				} else {
					await api.contract.tenantSign(this.contractId, signData)
				}
				
				uni.hideLoading()
				uni.showToast({ title: '签署成功', icon: 'success' })
				
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			} catch (e) {
				uni.hideLoading()
				console.error('签署失败:', e)
			}
		}
	}
}
</script>

<style scoped>
.sign-page {
	min-height: 100vh;
	background: #f5f7fa;
}

.contract-preview {
	height: calc(100vh - 500rpx);
	background: #fff;
	padding: 30rpx;
}

.contract-header {
	text-align: center;
	margin-bottom: 40rpx;
	padding-bottom: 30rpx;
	border-bottom: 2rpx solid #f0f0f0;
}

.contract-title {
	display: block;
	font-size: 40rpx;
	font-weight: 600;
	margin-bottom: 20rpx;
}

.contract-no {
	display: block;
	font-size: 26rpx;
	color: #999;
}

.clause-section {
	margin-bottom: 40rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 20rpx;
}

.section-content {
	font-size: 28rpx;
	color: #666;
	line-height: 1.8;
	text-indent: 2em;
}

.sign-section {
	background: #fff;
	margin-top: 20rpx;
	padding: 30rpx;
}

.sign-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 20rpx;
	text-align: center;
}

.signature-canvas {
	width: 690rpx;
	height: 300rpx;
	border: 2rpx dashed #ddd;
	border-radius: 12rpx;
	background: #fff;
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.sign-label {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.clear-btn {
	font-size: 28rpx;
	color: #409eff;
}

.sign-tips {
	text-align: center;
	margin-top: 16rpx;
	font-size: 24rpx;
	color: #999;
}

.submit-btn {
	background: #409eff;
	color: #fff;
	text-align: center;
	padding: 32rpx;
	font-size: 32rpx;
	font-weight: 600;
	margin: 30rpx;
	border-radius: 12rpx;
}

.sign-actions {
	display: flex;
	gap: 20rpx;
	margin-top: 30rpx;
}

.action-btn {
	flex: 1;
	text-align: center;
	padding: 28rpx;
	border-radius: 50rpx;
	font-size: 32rpx;
}

.action-btn.secondary {
	background: #f5f7fa;
	color: #666;
}

.action-btn.primary {
	background: #409eff;
	color: #fff;
}
</style>

<template>
	<view class="signature-container">
		<view class="signature-header">
			<text class="title">{{ title || '手写签名' }}</text>
			<text class="tip">请在下方区域签名</text>
		</view>
		
		<view class="canvas-wrapper">
			<canvas 
				canvas-id="signatureCanvas" 
				class="signature-canvas"
				@touchstart="touchStart"
				@touchmove="touchMove"
				@touchend="touchEnd"
				disable-scroll="true"
			></canvas>
			
			<view class="placeholder" v-if="!hasSignature">
				<text>请在此处签名</text>
			</view>
		</view>
		
		<view class="signature-actions">
			<view class="action-btn clear" @click="clearSignature">
				<text class="icon">🗑️</text>
				<text>清除</text>
			</view>
			<view class="action-btn confirm" @click="confirmSignature">
				<text class="icon">✓</text>
				<text>确认签名</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'SignaturePad',
	props: {
		title: {
			type: String,
			default: '手写签名'
		},
		lineColor: {
			type: String,
			default: '#000000'
		},
		lineWidth: {
			type: Number,
			default: 3
		}
	},
	
	data() {
		return {
			ctx: null,
			canvasWidth: 0,
			canvasHeight: 0,
			startX: 0,
			startY: 0,
			hasSignature: false,
			points: [] // 存储绘制点
		}
	},
	
	mounted() {
		this.initCanvas()
	},
	
	methods: {
		initCanvas() {
			// 获取canvas上下文
			this.ctx = uni.createCanvasContext('signatureCanvas', this)
			
			// 获取canvas尺寸
			const query = uni.createSelectorQuery().in(this)
			query.select('.signature-canvas').boundingClientRect(rect => {
				if (rect) {
					this.canvasWidth = rect.width
					this.canvasHeight = rect.height
					
					// 设置画笔样式
					this.ctx.setStrokeStyle(this.lineColor)
					this.ctx.setLineWidth(this.lineWidth)
					this.ctx.setLineCap('round')
					this.ctx.setLineJoin('round')
					
					// 填充白色背景
					this.ctx.setFillStyle('#ffffff')
					this.ctx.fillRect(0, 0, this.canvasWidth, this.canvasHeight)
					this.ctx.draw()
				}
			}).exec()
		},
		
		touchStart(e) {
			if (e.touches.length === 0) return
			
			const touch = e.touches[0]
			this.startX = touch.x
			this.startY = touch.y
			
			this.ctx.beginPath()
			this.ctx.moveTo(this.startX, this.startY)
			
			// 画一个点（处理点击不移动的情况）
			this.ctx.arc(this.startX, this.startY, this.lineWidth / 2, 0, 2 * Math.PI)
			this.ctx.fill()
			this.ctx.draw(true)
			
			this.hasSignature = true
		},
		
		touchMove(e) {
			if (e.touches.length === 0) return
			
			const touch = e.touches[0]
			const moveX = touch.x
			const moveY = touch.y
			
			this.ctx.moveTo(this.startX, this.startY)
			this.ctx.lineTo(moveX, moveY)
			this.ctx.stroke()
			this.ctx.draw(true)
			
			this.startX = moveX
			this.startY = moveY
		},
		
		touchEnd(e) {
			this.ctx.closePath()
		},
		
		clearSignature() {
			// 清空画布
			this.ctx.setFillStyle('#ffffff')
			this.ctx.fillRect(0, 0, this.canvasWidth, this.canvasHeight)
			this.ctx.draw()
			
			// 重新设置画笔样式
			this.ctx.setStrokeStyle(this.lineColor)
			this.ctx.setLineWidth(this.lineWidth)
			this.ctx.setLineCap('round')
			this.ctx.setLineJoin('round')
			
			this.hasSignature = false
			this.points = []
			
			this.$emit('clear')
		},
		
		confirmSignature() {
			if (!this.hasSignature) {
				uni.showToast({
					title: '请先签名',
					icon: 'none'
				})
				return
			}
			
			// 将canvas转为图片
			uni.canvasToTempFilePath({
				canvasId: 'signatureCanvas',
				fileType: 'png',
				quality: 1,
				success: (res) => {
					// 读取图片并转为base64
					// #ifdef MP-WEIXIN
					const fs = uni.getFileSystemManager()
					fs.readFile({
						filePath: res.tempFilePath,
						encoding: 'base64',
						success: (data) => {
							const base64 = 'data:image/png;base64,' + data.data
							this.$emit('confirm', {
								tempFilePath: res.tempFilePath,
								base64: base64
							})
						},
						fail: (err) => {
							console.error('读取签名文件失败:', err)
							// 如果无法读取base64，只返回临时路径
							this.$emit('confirm', {
								tempFilePath: res.tempFilePath,
								base64: ''
							})
						}
					})
					// #endif
					
					// #ifndef MP-WEIXIN
					// H5或其他平台
					this.$emit('confirm', {
						tempFilePath: res.tempFilePath,
						base64: ''
					})
					// #endif
				},
				fail: (err) => {
					console.error('生成签名图片失败:', err)
					uni.showToast({
						title: '签名保存失败',
						icon: 'none'
					})
				}
			}, this)
		},
		
		// 获取签名图片（供外部调用）
		getSignatureImage() {
			return new Promise((resolve, reject) => {
				if (!this.hasSignature) {
					reject(new Error('未签名'))
					return
				}
				
				uni.canvasToTempFilePath({
					canvasId: 'signatureCanvas',
					fileType: 'png',
					quality: 1,
					success: (res) => {
						resolve(res.tempFilePath)
					},
					fail: (err) => {
						reject(err)
					}
				}, this)
			})
		}
	}
}
</script>

<style scoped>
.signature-container {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
}

.signature-header {
	margin-bottom: 20rpx;
}

.signature-header .title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	display: block;
	margin-bottom: 8rpx;
}

.signature-header .tip {
	font-size: 26rpx;
	color: #999;
}

.canvas-wrapper {
	position: relative;
	width: 100%;
	height: 400rpx;
	border: 2rpx dashed #ddd;
	border-radius: 12rpx;
	overflow: hidden;
	background: #fff;
}

.signature-canvas {
	width: 100%;
	height: 100%;
}

.placeholder {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	color: #ccc;
	font-size: 32rpx;
	pointer-events: none;
}

.signature-actions {
	display: flex;
	gap: 30rpx;
	margin-top: 30rpx;
}

.action-btn {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	padding: 24rpx;
	border-radius: 50rpx;
	font-size: 28rpx;
}

.action-btn .icon {
	font-size: 32rpx;
}

.action-btn.clear {
	background: #f5f7fa;
	color: #666;
}

.action-btn.confirm {
	background: #409eff;
	color: #fff;
}
</style>

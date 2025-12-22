<template>
	<view class="signature-container">
		<view class="signature-title">请在下方签名</view>
		
		<canvas 
			canvas-id="signatureCanvas" 
			class="signature-canvas"
			@touchstart="touchStart"
			@touchmove="touchMove"
			@touchend="touchEnd"
			disable-scroll
		></canvas>
		
		<view class="signature-actions">
			<button class="clear-btn" @tap="clearCanvas">清除</button>
			<button class="confirm-btn" @tap="confirmSignature" :disabled="!hasSigned">确认</button>
		</view>
	</view>
</template>

<script>
export default {
	props: {
		width: {
			type: Number,
			default: 600
		},
		height: {
			type: Number,
			default: 400
		}
	},
	
	data() {
		return {
			ctx: null,
			points: [],
			hasSigned: false,
			startX: 0,
			startY: 0
		};
	},
	
	mounted() {
		this.initCanvas();
	},
	
	methods: {
		/**
		 * 初始化画布
		 */
		initCanvas() {
			this.ctx = uni.createCanvasContext('signatureCanvas', this);
			
			// 设置画笔样式
			this.ctx.lineWidth = 4;
			this.ctx.lineCap = 'round';
			this.ctx.lineJoin = 'round';
			this.ctx.strokeStyle = '#000';
			
			// 设置背景色
			this.ctx.fillStyle = '#fff';
			this.ctx.fillRect(0, 0, this.width, this.height);
			this.ctx.draw();
		},
		
		/**
		 * 触摸开始
		 */
		touchStart(e) {
			const touch = e.touches[0];
			this.startX = touch.x;
			this.startY = touch.y;
			
			this.points = [];
			this.points.push({
				x: touch.x,
				y: touch.y
			});
			
			this.ctx.beginPath();
			this.ctx.moveTo(touch.x, touch.y);
		},
		
		/**
		 * 触摸移动
		 */
		touchMove(e) {
			const touch = e.touches[0];
			
			this.points.push({
				x: touch.x,
				y: touch.y
			});
			
			if (this.points.length >= 2) {
				const point1 = this.points[this.points.length - 2];
				const point2 = this.points[this.points.length - 1];
				
				this.ctx.moveTo(point1.x, point1.y);
				this.ctx.lineTo(point2.x, point2.y);
				this.ctx.stroke();
				this.ctx.draw(true);
			}
			
			this.hasSigned = true;
		},
		
		/**
		 * 触摸结束
		 */
		touchEnd() {
			this.points = [];
		},
		
		/**
		 * 清除画布
		 */
		clearCanvas() {
			this.initCanvas();
			this.hasSigned = false;
		},
		
		/**
		 * 确认签名
		 */
		confirmSignature() {
			if (!this.hasSigned) {
				uni.showToast({
					title: '请先签名',
					icon: 'none'
				});
				return;
			}
			
			uni.canvasToTempFilePath({
				canvasId: 'signatureCanvas',
				success: (res) => {
					console.log('签名图片路径:', res.tempFilePath);
					this.$emit('confirm', res.tempFilePath);
				},
				fail: (err) => {
					console.error('生成签名图片失败:', err);
					uni.showToast({
						title: '生成签名失败',
						icon: 'none'
					});
				}
			}, this);
		}
	}
};
</script>

<style lang="scss" scoped>
.signature-container {
	padding: 40rpx;
	background-color: #fff;
}

.signature-title {
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
	margin-bottom: 30rpx;
	text-align: center;
}

.signature-canvas {
	width: 100%;
	height: 400rpx;
	border: 2rpx dashed #ddd;
	border-radius: 10rpx;
	background-color: #fff;
}

.signature-actions {
	display: flex;
	justify-content: space-between;
	margin-top: 40rpx;
	
	button {
		flex: 1;
		height: 80rpx;
		line-height: 80rpx;
		font-size: 30rpx;
		border-radius: 10rpx;
		
		&::after {
			border: none;
		}
	}
	
	.clear-btn {
		margin-right: 20rpx;
		background-color: #f5f5f5;
		color: #666;
	}
	
	.confirm-btn {
		background-color: #07c160;
		color: #fff;
		
		&:disabled {
			background-color: #ccc;
		}
	}
}
</style>

<template>
	<view class="publish-page">
		<scroll-view class="form-container" scroll-y>
			<!-- 房源图片 -->
			<view class="form-section">
				<view class="section-title required">房源图片</view>
				<view class="image-upload">
					<view class="upload-item" v-for="(img, index) in formData.images" :key="index">
						<image class="upload-img" :src="typeof img === 'string' ? img : img.url" mode="aspectFill"></image>
						<view class="delete-btn" @click="deleteImage(index)">×</view>
					</view>
					<view class="upload-btn" @click="chooseImage" v-if="formData.images.length < 9">
						<text class="upload-icon">📷</text>
						<text class="upload-text">上传图片</text>
					</view>
				</view>
			</view>
			
			<!-- 基本信息 -->
			<view class="form-section">
				<view class="section-title required">基本信息</view>
				<view class="form-item">
					<text class="label">标题</text>
					<input class="input" v-model="formData.title" placeholder="请输入房源标题" />
				</view>
				<view class="form-item">
					<text class="label">所属小区</text>
					<picker mode="selector" :value="communityIndex" :range="communityList" range-key="communityName" @change="onCommunityChange">
						<view class="picker">
							{{ formData.communityId ? communityList[communityIndex]?.communityName : '请选择小区' }}
						</view>
					</picker>
				</view>
				<view class="form-item">
					<text class="label">租金</text>
					<input class="input" type="number" v-model="formData.rentPrice" placeholder="请输入月租金" />
					<text class="unit">元/月</text>
				</view>
				<view class="form-item">
					<text class="label">面积</text>
					<input class="input" type="number" v-model="formData.area" placeholder="请输入面积" />
					<text class="unit">㎡</text>
				</view>
				<view class="form-item">
					<text class="label">户型</text>
					<input class="input" v-model="formData.roomType" placeholder="如：2室1厅1卫" />
				</view>
				<view class="form-item">
					<text class="label">楼层</text>
					<input class="input" v-model="formData.floor" placeholder="如：10/20层" />
				</view>
				<view class="form-item">
					<text class="label">支付方式</text>
					<picker mode="selector" :value="paymentIndex" :range="paymentMethods" @change="onPaymentChange">
						<view class="picker">
							{{ formData.paymentMethod || '请选择支付方式' }}
						</view>
					</picker>
				</view>
				<view class="form-item">
					<text class="label">租赁期限</text>
					<picker mode="selector" :value="rentPeriodIndex" :range="rentPeriods" @change="onRentPeriodChange">
						<view class="picker">
							{{ formData.rentPeriod || '请选择租赁期限' }}
						</view>
					</picker>
				</view>
			</view>
			
			<!-- 详细描述 -->
			<view class="form-section">
				<view class="section-title">详细描述</view>
				<textarea class="textarea" v-model="formData.description" placeholder="请详细描述房源情况..." maxlength="500"></textarea>
			</view>
		</scroll-view>
		
		<!-- 底部按钮 -->
		<view class="bottom-bar">
			<view class="btn btn-secondary" @click="saveDraft">保存草稿</view>
			<view class="btn btn-primary" @click="submitHouse">提交审核</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			houseId: '',
			communityIndex: 0,
			paymentIndex: 0,
			rentPeriodIndex: 0,
			communityList: [],
			paymentMethods: ['押一付一', '押一付三', '押二付一', '押三付三', '半年付', '年付'],
			rentPeriods: ['短期', '长期'],
			formData: {
				images: [],
				title: '',
				communityId: '',
				rentPrice: '',
				area: '',
				roomType: '',
				floor: '',
				paymentMethod: '',
				rentPeriod: '长期',
				description: ''
			}
		}
	},
	
	async onLoad(options) {
		// 先加载小区列表，再加载房源详情（编辑模式需要小区列表来设置索引）
		await this.loadCommunityList()
		if (options.id) {
			this.houseId = options.id
			this.loadHouseDetail()
		}
	},
	
	methods: {
		
		chooseImage() {
			const maxCount = 9 - this.formData.images.length
			if (maxCount <= 0) {
				return uni.showToast({ title: '最多只能上传9张图片', icon: 'none' })
			}
			
			uni.chooseImage({
				count: maxCount,
				success: (res) => {
					// 将新选择的图片添加到数组中，标记为新图片
					const newImages = res.tempFilePaths.map(path => ({
						url: path,
						isExisting: false,
						isNew: true
					}))
					this.formData.images = [...this.formData.images, ...newImages]
				}
			})
		},
		
		// 删除图片
		async deleteImage(index) {
			const image = this.formData.images[index]
			
			// 如果是已存在的图片，需要调用API删除
			if (image.isExisting && image.imageId) {
				try {
					await api.house.deleteImage(image.imageId)
					uni.showToast({ title: '图片删除成功', icon: 'success' })
				} catch (e) {
					console.error('删除图片失败:', e)
					return uni.showToast({ title: '删除图片失败', icon: 'none' })
				}
			}
			
			// 从数组中移除
			this.formData.images.splice(index, 1)
		},
		
		async saveDraft() {
			try {
				uni.showLoading({ title: '保存中...' })
				
				// 先保存房源基本信息，不包含图片
				const houseData = {
					title: this.formData.title,
					communityId: this.formData.communityId,
					rentPrice: parseFloat(this.formData.rentPrice),
					area: parseFloat(this.formData.area),
					houseType: this.formData.roomType,  // 后端字段名是houseType
					floor: this.formData.floor,
					paymentMethod: this.formData.paymentMethod,
					rentPeriod: this.formData.rentPeriod,
					description: this.formData.description,
					publishStatus: 'draft'
				}
				
				let houseId = this.houseId
				if (houseId) {
					await api.house.update(houseId, houseData)
				} else {
					const res = await api.house.publish(houseData)
					houseId = res.data.houseId
					this.houseId = houseId
				}
				
				// 上传图片
				if (this.formData.images.length > 0) {
					await this.uploadImages(houseId)
				}
				
				uni.hideLoading()
				uni.showToast({ title: '保存成功', icon: 'success' })
				setTimeout(() => {
					// 检查是否可以返回，否则跳转到房源列表
					const pages = getCurrentPages()
					if (pages.length > 1) {
						uni.navigateBack()
					} else {
						uni.redirectTo({
							url: '/pages/landlord/houses/houses'
						})
					}
				}, 1500)
			} catch (e) {
				uni.hideLoading()
				console.error('保存失败:', e)
				uni.showToast({ title: '保存失败', icon: 'none' })
			}
		},
		
		async submitHouse() {
			// 调试：打印表单数据
			console.log('【发布房源】表单数据:', this.formData)
			console.log('【发布房源】户型值:', this.formData.roomType, '类型:', typeof this.formData.roomType)
			console.log('【发布房源】图片数组:', this.formData.images, '长度:', this.formData.images.length)
			console.log('【发布房源】小区ID:', this.formData.communityId, '类型:', typeof this.formData.communityId)
			
			// 验证
			if (!this.formData.title) {
				return uni.showToast({ title: '请输入标题', icon: 'none' })
			}
			if (!this.formData.communityId) {
				return uni.showToast({ title: '请选择小区', icon: 'none' })
			}
			if (!this.formData.roomType) {
				return uni.showToast({ title: '请输入户型', icon: 'none' })
			}
			if (!this.formData.paymentMethod) {
				return uni.showToast({ title: '请选择支付方式', icon: 'none' })
			}
			if (!this.formData.rentPrice) {
				return uni.showToast({ title: '请输入租金', icon: 'none' })
			}
			// 注释掉图片验证，因为图片会在房源创建后单独上传
			// if (this.formData.images.length === 0) {
			// 	return uni.showToast({ title: '请上传房源图片', icon: 'none' })
			// }
			
			try {
				uni.showLoading({ title: '提交中...' })
				
				// 先保存房源基本信息，不包含图片
				const houseData = {
					title: this.formData.title,
					communityId: this.formData.communityId,
					rentPrice: parseFloat(this.formData.rentPrice),
					area: parseFloat(this.formData.area),
					houseType: this.formData.roomType,  // 后端字段名是houseType
					floor: this.formData.floor,
					paymentMethod: this.formData.paymentMethod,
					rentPeriod: this.formData.rentPeriod,
					description: this.formData.description,
					publishStatus: 'pending'
				}
				
				console.log('【发布房源】准备提交的数据:', houseData)
				
				let houseId = this.houseId
				if (houseId) {
					await api.house.update(houseId, houseData)
				} else {
					const res = await api.house.publish(houseData)
					houseId = res.data.houseId
					this.houseId = houseId
				}
				
				// 上传图片
				if (this.formData.images.length > 0) {
					await this.uploadImages(houseId)
				}
				
				uni.hideLoading()
				uni.showToast({ title: '提交成功', icon: 'success' })
				setTimeout(() => {
					// 检查是否可以返回，否则跳转到房源列表
					const pages = getCurrentPages()
					if (pages.length > 1) {
						uni.navigateBack()
					} else {
						uni.redirectTo({
							url: '/pages/landlord/houses/houses'
						})
					}
				}, 1500)
			} catch (e) {
				uni.hideLoading()
				console.error('提交失败:', e)
				uni.showToast({ title: '提交失败', icon: 'none' })
			}
		},
		
		// 加载房源详情（编辑模式）
		async loadHouseDetail() {
			try {
				uni.showLoading({ title: '加载中...' })
				const res = await api.house.getDetail(this.houseId)
				if (res.code === 200) {
					const house = res.data
					// 填充表单数据
					this.formData = {
						title: house.title || '',
						communityId: house.communityId || '',
						rentPrice: house.rentPrice ? house.rentPrice.toString() : '',
						area: house.area ? house.area.toString() : '',
						roomType: house.houseType || '',
						floor: house.floor || '',
						paymentMethod: house.paymentMethod || '',
						rentPeriod: house.rentPeriod || '长期',
						description: house.description || '',
						images: []
					}
					
					// 设置小区选择器索引
					if (house.communityId && Array.isArray(this.communityList) && this.communityList.length > 0) {
						const communityIndex = this.communityList.findIndex(c => c.communityId === house.communityId)
						if (communityIndex >= 0) {
							this.communityIndex = communityIndex
						}
					}
					
					// 设置支付方式选择器索引
					if (house.paymentMethod) {
						const paymentIndex = this.paymentMethods.findIndex(p => p === house.paymentMethod)
						if (paymentIndex >= 0) {
							this.paymentIndex = paymentIndex
						}
					}
					
					// 设置租赁期限选择器索引
					if (house.rentPeriod) {
						const rentPeriodIndex = this.rentPeriods.findIndex(p => p === house.rentPeriod)
						if (rentPeriodIndex >= 0) {
							this.rentPeriodIndex = rentPeriodIndex
						}
					}
					
					// 加载现有图片
					await this.loadExistingImages()
				}
			} catch (e) {
				console.error('加载房源详情失败:', e)
				uni.showToast({ title: '加载房源详情失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},
		
		// 加载现有图片
		async loadExistingImages() {
			try {
				const res = await api.house.getImages(this.houseId)
				if (res.code === 200 && res.data && res.data.length > 0) {
					// 将现有图片添加到formData.images中，标记为已存在的图片
					this.formData.images = res.data.map(img => ({
						url: img.imageUrl,
						imageId: img.imageId,
						isExisting: true,
						isCover: img.isCover
					}))
				}
			} catch (e) {
				console.error('加载现有图片失败:', e)
			}
		},
		
		// 加载小区列表
		async loadCommunityList() {
			try {
				const res = await api.community.getList()
				if (res.code === 200) {
					this.communityList = res.data || []
				}
			} catch (e) {
				console.error('加载小区列表失败:', e)
			}
		},
		
		// 小区选择
		onCommunityChange(e) {
			this.communityIndex = e.detail.value
			this.formData.communityId = this.communityList[e.detail.value].communityId
		},
		
		// 支付方式选择
		onPaymentChange(e) {
			this.paymentIndex = e.detail.value
			this.formData.paymentMethod = this.paymentMethods[e.detail.value]
		},
		
		// 租赁期限选择
		onRentPeriodChange(e) {
			this.rentPeriodIndex = e.detail.value
			this.formData.rentPeriod = this.rentPeriods[e.detail.value]
		},
		
		// 上传图片到MinIO
		async uploadImages(houseId) {
			const token = uni.getStorageSync('token')
			
			// 只上传新选择的图片
			const newImages = this.formData.images.filter(img => img.isNew && !img.isExisting)
			
			if (newImages.length === 0) {
				console.log('没有新图片需要上传')
				return
			}
			
			for (let i = 0; i < newImages.length; i++) {
				const image = newImages[i]
				
				try {
					await new Promise((resolve, reject) => {
						uni.uploadFile({
							url: `${api.baseUrl.replace('/api', '')}/api/houses/${houseId}/images`,
							filePath: image.url, // 新图片使用url字段存储临时路径
							name: 'files',
							header: {
								'Authorization': `Bearer ${token}`
							},
							success: (uploadRes) => {
								console.log('图片上传成功:', uploadRes)
								resolve(uploadRes)
							},
							fail: (error) => {
								console.error('图片上传失败:', error)
								reject(error)
							}
						})
					})
				} catch (e) {
					console.error(`第${i + 1}张图片上传失败:`, e)
					// 继续上传其他图片
				}
			}
		}
	}
}
</script>

<style scoped>
.publish-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 160rpx;
}

.form-container {
	height: calc(100vh - 160rpx);
}

.form-section {
	background: #fff;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 30rpx;
}

.section-title.required::before {
	content: '*';
	color: #f56c6c;
	margin-right: 8rpx;
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

.form-item {
	display: flex;
	align-items: center;
	padding: 24rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.label {
	width: 140rpx;
	font-size: 28rpx;
	color: #333;
}

.input {
	flex: 1;
	font-size: 28rpx;
}

.picker {
	flex: 1;
	height: 80rpx;
	padding: 0 20rpx;
	background: #f8f9fa;
	border-radius: 8rpx;
	font-size: 28rpx;
	display: flex;
	align-items: center;
	color: #333;
}

.unit {
	font-size: 24rpx;
	color: #999;
	margin-left: 12rpx;
}

.textarea {
	width: 100%;
	min-height: 200rpx;
	font-size: 28rpx;
	line-height: 1.6;
}

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	gap: 20rpx;
	background: #fff;
	padding: 20rpx;
	box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.btn {
	flex: 1;
	text-align: center;
	padding: 28rpx;
	border-radius: 50rpx;
	font-size: 32rpx;
}

.btn-secondary {
	background: #f5f7fa;
	color: #333;
}

.btn-primary {
	background: #409eff;
	color: #fff;
}
</style>

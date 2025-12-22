<template>
	<view class="create-page">
		<view class="house-info">
			<text class="house-title">{{ houseInfo.title }}</text>
			<text class="house-detail">{{ houseInfo.communityName }} · {{ houseInfo.area }}㎡</text>
		</view>
		
		<view class="form-section">
			<view class="section-title">租赁信息</view>
			
			<view class="form-item">
				<text class="label">月租金(元)</text>
				<input class="input" v-model="formData.rentPrice" type="digit" placeholder="请输入月租金" />
			</view>
			
			<view class="form-item">
				<text class="label">支付方式</text>
				<picker mode="selector" :range="paymentMethods" @change="onPaymentChange">
					<view class="picker-value">
						{{ formData.paymentMethod || '请选择支付方式' }}
					</view>
				</picker>
			</view>
			
			<view class="form-item">
				<text class="label">租赁开始日期</text>
				<picker mode="date" :value="formData.startDate" @change="onStartDateChange">
					<view class="picker-value">
						{{ formData.startDate || '请选择开始日期' }}
					</view>
				</picker>
			</view>
			
			<view class="form-item">
				<text class="label">租赁结束日期</text>
				<picker mode="date" :value="formData.endDate" :start="formData.startDate" @change="onEndDateChange">
					<view class="picker-value">
						{{ formData.endDate || '请选择结束日期' }}
					</view>
				</picker>
			</view>
		</view>
		
		<view class="form-section">
			<view class="section-title">标准条款</view>
			<view class="template-content">
				<text class="template-text">{{ templateContent }}</text>
			</view>
		</view>
		
		<view class="form-section">
			<view class="section-title">补充条款（可选）</view>
			<textarea class="textarea" v-model="formData.customContent" 
				placeholder="请输入补充条款内容，如租金支付日期、违约责任等..." 
				maxlength="1000"></textarea>
		</view>
		
		<view class="submit-btn" @click="createContract">生成合同草稿</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			houseId: '',
			houseInfo: {},
			formData: {
				rentPrice: '',
				paymentMethod: '',
				startDate: '',
				endDate: '',
				customContent: ''
			},
			paymentMethods: ['押一付一', '押一付三', '押一付六', '押二付一', '押二付三'],
			templateContent: `根据《中华人民共和国民法典》《住房租赁条例》等相关法律法规，甲乙双方在平等、自愿、协商一致的基础上，就房屋租赁事宜达成如下协议：

第一条 房屋基本情况
甲方将其所有的房屋出租给乙方使用，房屋位置、面积、装修状况等详见附件。

第二条 租赁期限
租赁期限自[起始日期]至[结束日期]止。

第三条 租金及支付方式  
月租金为人民币[租金金额]元，支付方式为[支付方式]。

第四条 甲方权利义务
1. 保证房屋符合居住条件，房屋设施完好
2. 及时维修房屋及设施
3. 不得随意进入租赁房屋

第五条 乙方权利义务
1. 按时支付租金
2. 爱护房屋及设施，合理使用
3. 不得擅自转租、改变房屋用途

第六条 违约责任
任何一方违约，应承担相应的违约责任。

第七条 争议解决
双方如发生争议，应友好协商解决；协商不成的，可向房屋所在地人民法院起诉。

本合同一式三份，甲乙双方各执一份，平台备案一份，自双方签字盖章后生效。`
		}
	},
	
	onLoad(options) {
		this.houseId = options.houseId
		this.loadHouseInfo()
	},
	
	methods: {
		async loadHouseInfo() {
			try {
				const res = await api.house.getDetail(this.houseId)
				if (res.code === 200) {
					this.houseInfo = res.data
					this.formData.rentPrice = res.data.rentPrice || ''
				}
			} catch (e) {
				console.error('加载房源信息失败:', e)
			}
		},
		
		onPaymentChange(e) {
			this.formData.paymentMethod = this.paymentMethods[e.detail.value]
		},
		
		onStartDateChange(e) {
			this.formData.startDate = e.detail.value
		},
		
		onEndDateChange(e) {
			this.formData.endDate = e.detail.value
		},
		
		async createContract() {
			// 表单验证
			if (!this.formData.rentPrice) {
				return uni.showToast({ title: '请输入租金', icon: 'none' })
			}
			if (!this.formData.paymentMethod) {
				return uni.showToast({ title: '请选择支付方式', icon: 'none' })
			}
			if (!this.formData.startDate) {
				return uni.showToast({ title: '请选择开始日期', icon: 'none' })
			}
			if (!this.formData.endDate) {
				return uni.showToast({ title: '请选择结束日期', icon: 'none' })
			}
			
			try {
				uni.showLoading({ title: '创建中...' })
				
				const res = await api.contract.create({
					houseId: this.houseId,
					rentPrice: parseFloat(this.formData.rentPrice),
					paymentMethod: this.formData.paymentMethod,
					startDate: this.formData.startDate,
					endDate: this.formData.endDate,
					customContent: this.formData.customContent
				})
				
				uni.hideLoading()
				
				if (res.code === 200) {
					uni.showToast({
						title: '合同创建成功',
						icon: 'success',
						duration: 2000
					})
					setTimeout(() => {
						uni.navigateTo({
							url: `/pages/contract/detail/detail?id=${res.data.contractId}`
						})
					}, 2000)
				}
			} catch (e) {
				uni.hideLoading()
				console.error('创建合同失败:', e)
				uni.showToast({ title: '创建失败', icon: 'none' })
			}
		}
	}
}
</script>

<style scoped>
.create-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding: 20rpx;
	padding-bottom: 200rpx;
}

.house-info {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.house-title {
	display: block;
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 12rpx;
}

.house-detail {
	font-size: 26rpx;
	color: #999;
}

.form-section {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.form-item {
	margin-bottom: 30rpx;
}

.label {
	display: block;
	font-size: 28rpx;
	color: #666;
	margin-bottom: 16rpx;
}

.input {
	width: 100%;
	height: 80rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
	padding: 0 24rpx;
	font-size: 28rpx;
}

.picker-value {
	width: 100%;
	height: 80rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
	padding: 0 24rpx;
	font-size: 28rpx;
	line-height: 80rpx;
	color: #333;
}

.template-content {
	background: #f9f9f9;
	border-radius: 12rpx;
	padding: 24rpx;
	max-height: 400rpx;
	overflow-y: scroll;
}

.template-text {
	font-size: 24rpx;
	color: #666;
	line-height: 1.8;
	white-space: pre-wrap;
}

.textarea {
	width: 100%;
	min-height: 200rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
	padding: 20rpx;
	font-size: 28rpx;
}

.submit-btn {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #409eff;
	color: #fff;
	text-align: center;
	padding: 32rpx;
	font-size: 32rpx;
	font-weight: 600;
	padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}
</style>

<template>
	<view class="contract-create-page">
		<view class="page-header">
			<text class="title">发起租赁合同</text>
			<text class="subtitle">选择房源和租客，设置租期</text>
		</view>
		
		<!-- 第一步：选择房源 -->
		<view class="section-card">
			<view class="section-title">
				<text class="step-num">1</text>
				<text>选择出租房源</text>
			</view>
			
			<view class="house-selector" @click="selectHouse" v-if="!selectedHouse">
				<text class="selector-icon">🏠</text>
				<text class="selector-text">点击选择房源</text>
				<text class="arrow">›</text>
			</view>
			
			<view class="selected-house" v-else>
				<view class="house-info">
					<view class="house-title">{{ selectedHouse.title }}</view>
					<view class="house-detail">
						<text>{{ selectedHouse.communityName }}</text>
						<text class="divider">|</text>
						<text>{{ selectedHouse.houseType }}</text>
						<text class="divider">|</text>
						<text>{{ selectedHouse.area }}㎡</text>
					</view>
					<view class="house-price">¥{{ selectedHouse.rentPrice }}/月</view>
				</view>
				<text class="change-btn" @click="selectHouse">更换</text>
			</view>
		</view>
		
		<!-- 第二步：选择租客 -->
		<view class="section-card">
			<view class="section-title">
				<text class="step-num">2</text>
				<text>选择租客</text>
			</view>
			
			<view class="tenant-selector" @click="selectTenant" v-if="!selectedTenant">
				<text class="selector-icon">👤</text>
				<text class="selector-text">从预约/聊天中选择租客</text>
				<text class="arrow">›</text>
			</view>
			
			<view class="selected-tenant" v-else>
				<image class="tenant-avatar" :src="selectedTenant.avatarUrl || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
				<view class="tenant-info">
					<view class="tenant-name">{{ selectedTenant.nickname }}</view>
					<view class="tenant-source">来源：{{ selectedTenant.source }}</view>
				</view>
				<text class="change-btn" @click="selectTenant">更换</text>
			</view>
		</view>
		
		<!-- 第三步：设置租期 -->
		<view class="section-card">
			<view class="section-title">
				<text class="step-num">3</text>
				<text>设置租赁条款</text>
			</view>
			
			<view class="form-item">
				<text class="label">开始日期</text>
				<picker mode="date" :value="formData.startDate" @change="onStartDateChange">
					<view class="date-picker">
						<text>{{ formData.startDate || '请选择' }}</text>
						<text class="arrow">›</text>
					</view>
				</picker>
			</view>
			
			<view class="form-item">
				<text class="label">租期（月）</text>
				<picker :range="monthOptions" @change="onMonthChange">
					<view class="date-picker">
						<text>{{ formData.months || '请选择' }}个月</text>
						<text class="arrow">›</text>
					</view>
				</picker>
			</view>
			
			<view class="form-item">
				<text class="label">结束日期</text>
				<view class="readonly-value">{{ endDate || '自动计算' }}</view>
			</view>
			
			<view class="form-item">
				<text class="label">月租金（元）</text>
				<input class="input" type="number" v-model="formData.monthlyRent" placeholder="请输入月租金" />
			</view>
			
			<view class="form-item">
				<text class="label">押金（元）</text>
				<input class="input" type="number" v-model="formData.deposit" placeholder="请输入押金金额" />
			</view>
			
			<view class="form-item">
				<text class="label">付款方式</text>
				<picker :range="paymentMethods" @change="onPaymentChange">
					<view class="date-picker">
						<text>{{ formData.paymentMethod || '请选择' }}</text>
						<text class="arrow">›</text>
					</view>
				</picker>
			</view>
		</view>
		
		<!-- 第四步：补充条款 -->
		<view class="section-card">
			<view class="section-title">
				<text class="step-num">4</text>
				<text>补充条款（选填）</text>
			</view>
			<textarea class="custom-terms" v-model="formData.customTerms" 
				placeholder="可添加其他约定事项，如：宠物、装修、设备清单等" maxlength="500"></textarea>
			<view class="char-count">{{ (formData.customTerms || '').length }}/500</view>
		</view>
		
		<!-- 提交按钮 -->
		<view class="submit-area">
			<view class="tips">
				<text>📌 提交后将发送给租客确认，双方签署后生效</text>
			</view>
			<view class="submit-btn" :class="{ disabled: !canSubmit }" @click="submitContract">
				发起合同
			</view>
		</view>
		
		<!-- 选择房源弹窗 -->
		<view class="popup-mask" v-if="showHousePopup" @click="closeHousePopup">
			<view class="popup-content" @click.stop>
				<view class="popup-header">
					<text class="popup-title">选择房源</text>
					<text class="popup-close" @click="closeHousePopup">✕</text>
				</view>
				<scroll-view class="popup-list" scroll-y>
					<view class="popup-item" v-for="house in myHouses" :key="house.houseId" 
						@click="onHouseSelect(house)">
						<view class="item-title">{{ house.title }}</view>
						<view class="item-desc">{{ house.communityName }} · {{ house.houseType }} · {{ house.area }}㎡</view>
						<view class="item-price">¥{{ house.rentPrice }}/月</view>
					</view>
					<view class="popup-empty" v-if="myHouses.length === 0">
						<text>暂无可用房源</text>
					</view>
				</scroll-view>
			</view>
		</view>
		
		<!-- 选择租客弹窗 -->
		<view class="popup-mask" v-if="showTenantPopup" @click="closeTenantPopup">
			<view class="popup-content" @click.stop>
				<view class="popup-header">
					<text class="popup-title">选择租客</text>
					<text class="popup-close" @click="closeTenantPopup">✕</text>
				</view>
				
				<!-- 租客来源Tab -->
				<view class="tenant-tabs">
					<view class="tab" :class="{ active: tenantTab === 'appointment' }" 
						@click="tenantTab = 'appointment'">预约看房</view>
					<view class="tab" :class="{ active: tenantTab === 'chat' }" 
						@click="tenantTab = 'chat'">聊天记录</view>
				</view>
				
				<scroll-view class="popup-list" scroll-y>
					<!-- 预约列表 -->
					<template v-if="tenantTab === 'appointment'">
						<view class="popup-item" v-for="item in appointmentList" :key="item.appointmentId" 
							@click="onTenantSelect(item, '预约看房')">
							<image class="item-avatar" :src="item.userAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
							<view class="item-info">
								<view class="item-title">{{ item.userName }}</view>
								<view class="item-desc">预约时间：{{ item.appointmentTime }}</view>
								<view class="item-desc">房源：{{ item.houseTitle }}</view>
							</view>
						</view>
					</template>
					
					<!-- 聊天列表 -->
					<template v-if="tenantTab === 'chat'">
						<view class="popup-item" v-for="item in chatList" :key="item.sessionId" 
							@click="onTenantSelect(item, '聊天沟通')">
							<image class="item-avatar" :src="item.avatarUrl || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
							<view class="item-info">
								<view class="item-title">{{ item.nickname }}</view>
								<view class="item-desc">最近聊天：{{ item.lastMessage }}</view>
							</view>
						</view>
					</template>
					
					<view class="popup-empty" v-if="(tenantTab === 'appointment' && appointmentList.length === 0) || (tenantTab === 'chat' && chatList.length === 0)">
						<text>暂无记录</text>
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'

export default {
	data() {
		return {
			// 选中的房源和租客
			selectedHouse: null,
			selectedTenant: null,
			
			// 表单数据
			formData: {
				startDate: '',
				months: 12,
				monthlyRent: '',
				deposit: '',
				paymentMethod: '押一付三',
				customTerms: ''
			},
			
			// 选项
			monthOptions: ['1', '3', '6', '12', '24', '36'],
			paymentMethods: ['押一付一', '押一付三', '押二付一', '半年付', '年付'],
			
			// 弹窗数据
			showHousePopup: false,
			showTenantPopup: false,
			myHouses: [],
			appointmentList: [],
			chatList: [],
			tenantTab: 'appointment',
			
			// 路由参数（预填充）
			preselectedHouseId: null,
			preselectedTenantId: null
		}
	},
	
	computed: {
		endDate() {
			if (!this.formData.startDate || !this.formData.months) return ''
			const start = new Date(this.formData.startDate)
			start.setMonth(start.getMonth() + parseInt(this.formData.months))
			start.setDate(start.getDate() - 1)
			return this.formatDate(start)
		},
		
		canSubmit() {
			return this.selectedHouse && this.selectedTenant && 
				this.formData.startDate && this.formData.months &&
				this.formData.monthlyRent && this.formData.deposit
		}
	},
	
	onLoad(options) {
		// 支持从其他页面带参数跳转
		if (options.houseId) {
			this.preselectedHouseId = options.houseId
		}
		if (options.tenantId) {
			this.preselectedTenantId = options.tenantId
		}
		if (options.tenantName) {
			// 直接预填租客信息
			this.selectedTenant = {
				userId: options.tenantId,
				nickname: decodeURIComponent(options.tenantName),
				avatarUrl: options.tenantAvatar ? decodeURIComponent(options.tenantAvatar) : '',
				source: options.source || '直接选择'
			}
		}
		
		// 设置默认开始日期为今天
		this.formData.startDate = this.formatDate(new Date())
		
		this.loadMyHouses()
		this.loadAppointments()
		this.loadChatList()
	},
	
	methods: {
		formatDate(date) {
			const y = date.getFullYear()
			const m = String(date.getMonth() + 1).padStart(2, '0')
			const d = String(date.getDate()).padStart(2, '0')
			return `${y}-${m}-${d}`
		},
		
		async loadMyHouses() {
			try {
				const res = await api.house.getLandlordHouses({ status: 'online', pageNum: 1, pageSize: 100 })
				if (res.code === 200) {
					this.myHouses = res.data.records || res.data || []
					
					// 如果有预选房源ID，自动选中
					if (this.preselectedHouseId) {
						const house = this.myHouses.find(h => h.houseId == this.preselectedHouseId)
						if (house) {
							this.selectedHouse = house
							this.formData.monthlyRent = house.rentPrice
							this.formData.deposit = house.rentPrice * 2 // 默认押二
						}
					}
				}
			} catch (e) {
				console.error('加载房源失败:', e)
			}
		},
		
		async loadAppointments() {
			try {
				const res = await api.appointment.getLandlordAppointments()
				if (res.code === 200) {
					// 只显示已确认或已完成的预约
					this.appointmentList = (res.data || []).filter(a => 
						a.status === 'confirmed' || a.status === 'completed'
					)
					
					// 如果有预选租客ID，自动选中
					if (this.preselectedTenantId && !this.selectedTenant) {
						const appointment = this.appointmentList.find(a => a.userId == this.preselectedTenantId)
						if (appointment) {
							this.onTenantSelect(appointment, '预约看房')
						}
					}
				}
			} catch (e) {
				console.error('加载预约失败:', e)
			}
		},
		
		async loadChatList() {
			try {
				const res = await api.chat.getRecent()
				if (res.code === 200) {
					this.chatList = res.data || []
					
					// 如果有预选租客ID且尚未选中，从聊天中查找
					if (this.preselectedTenantId && !this.selectedTenant) {
						const chat = this.chatList.find(c => c.userId == this.preselectedTenantId)
						if (chat) {
							this.onTenantSelect(chat, '聊天沟通')
						}
					}
				}
			} catch (e) {
				console.error('加载聊天记录失败:', e)
			}
		},
		
		selectHouse() {
			this.showHousePopup = true
		},
		
		closeHousePopup() {
			this.showHousePopup = false
		},
		
		onHouseSelect(house) {
			this.selectedHouse = house
			this.formData.monthlyRent = house.rentPrice
			this.formData.deposit = house.rentPrice * 2
			this.closeHousePopup()
		},
		
		selectTenant() {
			this.showTenantPopup = true
		},
		
		closeTenantPopup() {
			this.showTenantPopup = false
		},
		
		onTenantSelect(item, source) {
			this.selectedTenant = {
				userId: item.userId || item.senderId,
				nickname: item.userName || item.nickname,
				avatarUrl: item.userAvatar || item.avatarUrl,
				source: source
			}
			this.closeTenantPopup()
		},
		
		onStartDateChange(e) {
			this.formData.startDate = e.detail.value
		},
		
		onMonthChange(e) {
			this.formData.months = parseInt(this.monthOptions[e.detail.value])
		},
		
		onPaymentChange(e) {
			this.formData.paymentMethod = this.paymentMethods[e.detail.value]
		},
		
		async submitContract() {
			if (!this.canSubmit) {
				uni.showToast({ title: '请填写完整信息', icon: 'none' })
				return
			}
			
			uni.showModal({
				title: '确认发起合同',
				content: `确定向 ${this.selectedTenant.nickname} 发起租赁合同？发起后将通知对方确认。`,
				success: async (res) => {
					if (res.confirm) {
						await this.doSubmit()
					}
				}
			})
		},
		
		async doSubmit() {
			try {
				uni.showLoading({ title: '创建合同中...' })
				
				const data = {
					houseId: this.selectedHouse.houseId,
					tenantId: this.selectedTenant.userId,
					startDate: this.formData.startDate,
					endDate: this.endDate,
					months: this.formData.months,
					monthlyRent: this.formData.monthlyRent,
					deposit: this.formData.deposit,
					paymentMethod: this.formData.paymentMethod,
					customContent: this.formData.customTerms
				}
				
				const res = await api.contract.create(data)
				uni.hideLoading()
				
				if (res.code === 200) {
					uni.showToast({ title: '合同创建成功', icon: 'success' })
					
					// 跳转到合同详情或合同列表
					setTimeout(() => {
						uni.redirectTo({
							url: `/pages/contract/detail/detail?id=${res.data.contractId}`
						})
					}, 1500)
				} else {
					uni.showToast({ title: res.message || '创建失败', icon: 'none' })
				}
			} catch (e) {
				uni.hideLoading()
				console.error('创建合同失败:', e)
				uni.showToast({ title: '创建失败，请重试', icon: 'none' })
			}
		}
	}
}
</script>

<style scoped>
.contract-create-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 200rpx;
}

.page-header {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 60rpx 30rpx;
	color: #fff;
}

.page-header .title {
	display: block;
	font-size: 40rpx;
	font-weight: 600;
	margin-bottom: 12rpx;
}

.page-header .subtitle {
	font-size: 28rpx;
	opacity: 0.9;
}

.section-card {
	background: #fff;
	margin: 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.section-title {
	display: flex;
	align-items: center;
	gap: 16rpx;
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 24rpx;
}

.step-num {
	width: 48rpx;
	height: 48rpx;
	background: #409eff;
	color: #fff;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
}

/* 选择器样式 */
.house-selector, .tenant-selector {
	display: flex;
	align-items: center;
	padding: 30rpx;
	background: #f8f9fa;
	border-radius: 12rpx;
	border: 2rpx dashed #ddd;
}

.selector-icon {
	font-size: 48rpx;
	margin-right: 20rpx;
}

.selector-text {
	flex: 1;
	font-size: 28rpx;
	color: #999;
}

.arrow {
	font-size: 32rpx;
	color: #ccc;
}

/* 已选择样式 */
.selected-house, .selected-tenant {
	display: flex;
	align-items: center;
	padding: 24rpx;
	background: #f0f7ff;
	border-radius: 12rpx;
	border: 2rpx solid #409eff;
}

.selected-house .house-info {
	flex: 1;
}

.house-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 8rpx;
}

.house-detail {
	font-size: 24rpx;
	color: #666;
	margin-bottom: 8rpx;
}

.house-detail .divider {
	margin: 0 12rpx;
	color: #ddd;
}

.house-price {
	font-size: 28rpx;
	color: #ff6b6b;
	font-weight: 600;
}

.tenant-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.tenant-info {
	flex: 1;
}

.tenant-name {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 8rpx;
}

.tenant-source {
	font-size: 24rpx;
	color: #666;
}

.change-btn {
	font-size: 26rpx;
	color: #409eff;
	padding: 12rpx 24rpx;
}

/* 表单样式 */
.form-item {
	display: flex;
	align-items: center;
	padding: 24rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.form-item:last-child {
	border-bottom: none;
}

.form-item .label {
	width: 180rpx;
	font-size: 28rpx;
	color: #666;
}

.form-item .input {
	flex: 1;
	font-size: 28rpx;
	text-align: right;
}

.date-picker {
	flex: 1;
	display: flex;
	justify-content: flex-end;
	align-items: center;
	gap: 12rpx;
	font-size: 28rpx;
	color: #333;
}

.readonly-value {
	flex: 1;
	text-align: right;
	font-size: 28rpx;
	color: #999;
}

/* 补充条款 */
.custom-terms {
	width: 100%;
	height: 200rpx;
	padding: 20rpx;
	background: #f8f9fa;
	border-radius: 12rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}

.char-count {
	text-align: right;
	font-size: 24rpx;
	color: #999;
	margin-top: 12rpx;
}

/* 提交区域 */
.submit-area {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	padding: 20rpx 30rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.tips {
	text-align: center;
	font-size: 24rpx;
	color: #999;
	margin-bottom: 16rpx;
}

.submit-btn {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	text-align: center;
	padding: 28rpx;
	border-radius: 50rpx;
	font-size: 32rpx;
	font-weight: 600;
}

.submit-btn.disabled {
	opacity: 0.5;
}

/* 弹窗样式 */
.popup-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	z-index: 999;
	display: flex;
	align-items: flex-end;
}

.popup-content {
	background: #fff;
	border-radius: 24rpx 24rpx 0 0;
	max-height: 70vh;
	width: 100%;
}

.popup-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.popup-title {
	font-size: 32rpx;
	font-weight: 600;
}

.popup-close {
	font-size: 36rpx;
	color: #999;
	padding: 10rpx;
}

.popup-list {
	max-height: 60vh;
	padding: 20rpx;
}

.popup-item {
	display: flex;
	align-items: center;
	padding: 24rpx;
	background: #f8f9fa;
	border-radius: 12rpx;
	margin-bottom: 16rpx;
}

.popup-item .item-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.popup-item .item-info {
	flex: 1;
}

.popup-item .item-title {
	font-size: 30rpx;
	font-weight: 500;
	color: #333;
	margin-bottom: 8rpx;
}

.popup-item .item-desc {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 4rpx;
}

.popup-item .item-price {
	font-size: 28rpx;
	color: #ff6b6b;
	font-weight: 600;
}

.popup-empty {
	text-align: center;
	padding: 80rpx;
	color: #999;
	font-size: 28rpx;
}

/* 租客Tab */
.tenant-tabs {
	display: flex;
	padding: 0 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.tenant-tabs .tab {
	flex: 1;
	text-align: center;
	padding: 24rpx;
	font-size: 28rpx;
	color: #666;
	position: relative;
}

.tenant-tabs .tab.active {
	color: #409eff;
	font-weight: 600;
}

.tenant-tabs .tab.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 4rpx;
	background: #409eff;
	border-radius: 2rpx;
}
</style>

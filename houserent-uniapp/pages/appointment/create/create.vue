<template>
	<view class="appointment-page">
		<view class="house-info">
			<text class="house-title">{{ houseTitle }}</text>
		</view>
		
		<view class="form-section">
			<view class="form-item">
				<text class="label">预约日期</text>
				<picker mode="date" :value="appointmentDate" :start="minDate" @change="onDateChange">
					<view class="picker-value">
						{{ appointmentDate || '请选择日期' }}
					</view>
				</picker>
			</view>
			
			<view class="form-item">
				<text class="label">预约时间</text>
				<picker mode="time" :value="appointmentTime" @change="onTimeChange">
					<view class="picker-value">
						{{ appointmentTime || '请选择时间' }}
					</view>
				</picker>
			</view>
			
			<view class="form-item">
				<text class="label">联系人姓名</text>
				<input class="input" v-model="contactName" placeholder="请输入联系人姓名" />
			</view>
			
			<view class="form-item">
				<text class="label">联系电话</text>
				<input class="input" v-model="contactPhone" type="number" placeholder="请输入联系电话" />
			</view>
			
			<view class="form-item">
				<text class="label">备注说明</text>
				<textarea class="textarea" v-model="remark" placeholder="请输入备注信息（选填）" 
					maxlength="200" />
			</view>
		</view>
		
		<view class="submit-btn" @click="submitAppointment">提交预约</view>
	</view>
</template>

<script>
import api from '@/utils/api.js'
import { getUserInfo } from '@/utils/storage.js'

export default {
	data() {
		return {
			houseId: '',
			houseTitle: '',
			appointmentDate: '',
			appointmentTime: '',
			contactName: '',
			contactPhone: '',
			remark: '',
			minDate: ''
		}
	},
	
	onLoad(options) {
		this.houseId = options.houseId
		this.houseTitle = decodeURIComponent(options.houseTitle || '房源')
		
		const today = new Date()
		this.minDate = this.formatDate(today)
		
		const userInfo = getUserInfo()
		if (userInfo) {
			this.contactName = userInfo.realName || userInfo.nickname || ''
			this.contactPhone = userInfo.phone || ''
		}
	},
	
	methods: {
		onDateChange(e) {
			this.appointmentDate = e.detail.value
		},
		
		onTimeChange(e) {
			this.appointmentTime = e.detail.value
		},
		
		async submitAppointment() {
			if (!this.appointmentDate) {
				uni.showToast({ title: '请选择预约日期', icon: 'none' })
				return
			}
			if (!this.appointmentTime) {
				uni.showToast({ title: '请选择预约时间', icon: 'none' })
				return
			}
			if (!this.contactName.trim()) {
				uni.showToast({ title: '请输入联系人姓名', icon: 'none' })
				return
			}
			if (!this.contactPhone.trim()) {
				uni.showToast({ title: '请输入联系电话', icon: 'none' })
				return
			}
			if (!/^1[3-9]\d{9}$/.test(this.contactPhone)) {
				uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
				return
			}
			
			try {
				const appointmentTime = `${this.appointmentDate} ${this.appointmentTime}:00`
				const res = await api.appointment.create({
					houseId: this.houseId,
					appointmentTime: appointmentTime,
					contactName: this.contactName.trim(),
					contactPhone: this.contactPhone.trim(),
					remark: this.remark.trim()
				})
				
				if (res.code === 200) {
					uni.showToast({ 
						title: '预约成功', 
						icon: 'success',
						duration: 2000
					})
					setTimeout(() => {
						uni.navigateBack()
					}, 2000)
				}
			} catch (e) {
				console.error('预约失败:', e)
				uni.showToast({ title: '预约失败，请重试', icon: 'none' })
			}
		},
		
		formatDate(date) {
			const year = date.getFullYear()
			const month = (date.getMonth() + 1).toString().padStart(2, '0')
			const day = date.getDate().toString().padStart(2, '0')
			return `${year}-${month}-${day}`
		}
	}
}
</script>

<style scoped>
.appointment-page {
	min-height: 100vh;
	background: #F7F9FC;
	padding: 24rpx 30rpx;
}

.house-info {
	background: linear-gradient(135deg, #FFF5F0, #FFFFFF);
	padding: 36rpx;
	border-radius: 24rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	position: relative;
	overflow: hidden;
}

.house-info::before {
	content: '📅';
	position: absolute;
	top: 20rpx;
	right: 20rpx;
	font-size: 60rpx;
	opacity: 0.1;
}

.house-title {
	font-size: 36rpx;
	font-weight: 700;
	color: #2C3E50;
	position: relative;
	z-index: 1;
}

.form-section {
	background: #FFFFFF;
	border-radius: 24rpx;
	padding: 24rpx 36rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.form-item {
	padding: 32rpx 0;
	border-bottom: 1rpx solid #F7F9FC;
}

.form-item:last-child {
	border-bottom: none;
}

.label {
	display: block;
	font-size: 30rpx;
	color: #2C3E50;
	font-weight: 700;
	margin-bottom: 20rpx;
}

.picker-value {
	font-size: 32rpx;
	color: #5A6C7D;
	padding: 16rpx 24rpx;
	background: #F7F9FC;
	border-radius: 12rpx;
	border: 2rpx solid #E4E7ED;
	transition: all 0.3s ease;
}

.picker-value:active {
	background: #FFFFFF;
	border-color: #FF6B35;
}

.input {
	font-size: 32rpx;
	color: #2C3E50;
	width: 100%;
	padding: 16rpx 24rpx;
	background: #F7F9FC;
	border-radius: 12rpx;
	border: 2rpx solid #E4E7ED;
	transition: all 0.3s ease;
}

.input:focus {
	background: #FFFFFF;
	border-color: #FF6B35;
	box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
}

.textarea {
	font-size: 32rpx;
	color: #2C3E50;
	width: 100%;
	min-height: 180rpx;
	padding: 16rpx 24rpx;
	background: #F7F9FC;
	border-radius: 12rpx;
	border: 2rpx solid #E4E7ED;
	line-height: 1.6;
	transition: all 0.3s ease;
}

.textarea:focus {
	background: #FFFFFF;
	border-color: #FF6B35;
	box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
}

.submit-btn {
	background: linear-gradient(135deg, #FF6B35, #FF8C61);
	color: #FFFFFF;
	text-align: center;
	padding: 36rpx;
	border-radius: 48rpx;
	font-size: 34rpx;
	font-weight: 700;
	margin-top: 48rpx;
	box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.35);
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.submit-btn::before {
	content: '';
	position: absolute;
	top: 0;
	left: -100%;
	width: 100%;
	height: 100%;
	background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
	transition: left 0.5s;
}

.submit-btn:active::before {
	left: 100%;
}

.submit-btn:active {
	transform: translateY(2rpx);
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}
</style>

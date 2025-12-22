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
	background: #f5f7fa;
	padding: 20rpx;
}

.house-info {
	background: #fff;
	padding: 30rpx;
	border-radius: 16rpx;
	margin-bottom: 20rpx;
}

.house-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.form-section {
	background: #fff;
	border-radius: 16rpx;
	padding: 20rpx 30rpx;
}

.form-item {
	padding: 30rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.form-item:last-child {
	border-bottom: none;
}

.label {
	display: block;
	font-size: 28rpx;
	color: #666;
	margin-bottom: 20rpx;
}

.picker-value {
	font-size: 30rpx;
	color: #333;
	padding: 10rpx 0;
}

.input {
	font-size: 30rpx;
	color: #333;
	width: 100%;
}

.textarea {
	font-size: 30rpx;
	color: #333;
	width: 100%;
	min-height: 150rpx;
	padding: 10rpx 0;
}

.submit-btn {
	background: #409eff;
	color: #fff;
	text-align: center;
	padding: 28rpx;
	border-radius: 16rpx;
	font-size: 32rpx;
	font-weight: 600;
	margin-top: 40rpx;
}
</style>

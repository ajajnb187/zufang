import request from './request.js'
import config from './config.js'

export default {
	// 导出基础URL供其他文件使用
	baseUrl: config.baseURL,
	wsUrl: config.wsURL,
	
	// ========== 认证 ========== AuthController /api/auth
	auth: {
		wechatLogin: (code) => request({ url: '/auth/wechat-login', method: 'POST', data: { code } }),
		adminLogin: (data) => request({ url: '/auth/admin-login', method: 'POST', data }),
		adminRegister: (data) => request({ url: '/auth/admin-register', method: 'POST', data }),
		refreshToken: () => request({ url: '/auth/refresh-token', method: 'POST' }),
		logout: () => request({ url: '/auth/logout', method: 'POST' }),
		getCurrentUser: () => request({ url: '/auth/current-user' }),
		checkLogin: () => request({ url: '/auth/check-login' }),
		getTokenInfo: () => request({ url: '/auth/token-info' }),
		devLogin: (userId) => request({ url: '/auth/dev-login', method: 'POST', data: { userId } }),
		getTestAccounts: () => request({ url: '/auth/test-accounts' })
	},
	
	// ========== 用户信息 ========== UserController /api/user
	user: {
		getProfile: () => request({ url: '/user/profile' }),
		updateProfile: (data) => request({ url: '/user/profile', method: 'PUT', data }),
		updateAvatar: (avatarUrl) => request({ url: '/user/avatar', method: 'POST', data: { avatarUrl } }),
		getStatistics: () => request({ url: '/user/statistics' }),
		getOrders: (params) => request({ url: '/user/orders', data: params })
	},
	
	// ========== 房源管理 ========== HouseController /api/houses
	house: {
		search: (data) => request({ url: '/houses/search', method: 'POST', data }),
		getDetail: (houseId) => request({ url: `/houses/${houseId}/detail` }),
		publish: (data) => request({ url: '/houses/publish', method: 'POST', data }),
		update: (houseId, data) => request({ url: `/houses/${houseId}`, method: 'PUT', data }),
		delete: (houseId) => request({ url: `/houses/${houseId}`, method: 'DELETE' }),
		online: (houseId) => request({ url: `/houses/${houseId}/online`, method: 'POST' }),
		offline: (houseId) => request({ url: `/houses/${houseId}/offline`, method: 'POST' }),
		favorite: (houseId) => request({ url: `/houses/${houseId}/favorite`, method: 'POST' }),
		unfavorite: (houseId) => request({ url: `/houses/${houseId}/favorite`, method: 'DELETE' }),
		getFavorites: () => request({ url: '/houses/favorites' }),
		getLandlordHouses: (params) => request({ url: '/houses/landlord', data: params }),
		report: (houseId, data) => request({ url: `/houses/${houseId}/report`, method: 'POST', data }),
		audit: (houseId, data) => request({ url: `/houses/${houseId}/audit`, method: 'POST', data }),
		getPendingAudit: (params) => request({ url: '/houses/pending-audit', data: params }),
		// 房源图片管理
		getImages: (houseId) => request({ url: `/houses/${houseId}/images` }),
		deleteImage: (imageId) => request({ url: `/houses/images/${imageId}`, method: 'DELETE' }),
		setCoverImage: (houseId, imageId) => request({ url: `/houses/${houseId}/images/${imageId}/cover`, method: 'POST' }),
		updateImageSort: (imageId, sortOrder) => request({ url: `/houses/images/${imageId}/sort`, method: 'PUT', data: { sortOrder } })
	},
	
	// ========== 预约看房 ========== AppointmentController /api/appointments
	appointment: {
		create: (data) => request({ url: '/appointments/create', method: 'POST', data }),
		getMyAppointments: () => request({ url: '/appointments/my-appointments' }),
		getLandlordAppointments: () => request({ url: '/appointments/landlord-appointments' }),
		confirm: (appointmentId) => request({ url: `/appointments/${appointmentId}/confirm`, method: 'POST' }),
		cancel: (appointmentId) => request({ url: `/appointments/${appointmentId}/cancel`, method: 'POST' }),
		complete: (appointmentId) => request({ url: `/appointments/${appointmentId}/complete`, method: 'POST' }),
		reject: (appointmentId, reason) => request({ url: `/appointments/${appointmentId}/reject`, method: 'POST', data: { reason } })
	},
	
	// ========== 合同管理 ========== ContractController /api/contracts
	contract: {
		create: (data) => request({ url: '/contracts/create', method: 'POST', data }),
		getDetail: (contractId) => request({ url: `/contracts/${contractId}` }),
		getList: (params) => request({ url: '/contracts/list', data: params }),
		generatePDF: (contractId) => request({ url: `/contracts/${contractId}/generate-pdf`, method: 'POST' }),
		landlordSign: (contractId, data) => request({ url: `/contracts/${contractId}/landlord-sign`, method: 'POST', data }),
		tenantSign: (contractId, data) => request({ url: `/contracts/${contractId}/tenant-sign`, method: 'POST', data }),
		audit: (contractId, data) => request({ url: `/contracts/${contractId}/audit`, method: 'POST', data }),
		verify: (contractId) => request({ url: `/contracts/${contractId}/verify` }),
		amend: (contractId, data) => request({ url: `/contracts/${contractId}/amend`, method: 'POST', data }),
		terminate: (contractId, data) => request({ url: `/contracts/${contractId}/terminate`, method: 'POST', data }),
		generateHash: (contractId) => request({ url: `/contracts/${contractId}/generate-hash`, method: 'POST' }),
		reject: (contractId) => request({ url: `/contracts/${contractId}/reject`, method: 'POST' })
	},
	
	// ========== 聊天 ========== ChatController /api/chat
	chat: {
		getHistory: (params) => request({ url: '/chat/history', data: params }),
		getUnreadCount: () => request({ url: '/chat/unread-count' }),
		markRead: (senderId) => request({ url: '/chat/mark-read', method: 'POST', data: { senderId } }),
		getRecent: () => request({ url: '/chat/recent' }),
		deleteSession: (sessionId) => request({ url: `/chat/session/${sessionId}`, method: 'DELETE' }),
		getOnlineCount: () => request({ url: '/chat/online-count' }),
		checkOnlineStatus: (userId) => request({ url: '/chat/online-status', data: { userId } })
	},
	
	// ========== 小区信息 ========== CommunityController /api/communities
	community: {
		getList: (params) => request({ url: '/communities/list', data: params }),
		getDetail: (communityId) => request({ url: `/communities/${communityId}` }),
		search: (keyword) => request({ url: '/communities/search', data: { keyword } }),
		getByCity: (city) => request({ url: `/communities/city/${city}` }),
		getPopular: (limit) => request({ url: '/communities/popular', data: { limit } }),
		getFacilities: (communityId, facilityType) => request({ url: `/communities/${communityId}/facilities`, data: { facilityType } }),
		addFacility: (communityId, data) => request({ url: `/communities/${communityId}/facilities`, method: 'POST', data }),
		updateFacility: (facilityId, data) => request({ url: `/communities/facilities/${facilityId}`, method: 'PUT', data }),
		deleteFacility: (facilityId) => request({ url: `/communities/facilities/${facilityId}`, method: 'DELETE' }),
		canManageFacilities: (communityId) => request({ url: `/communities/${communityId}/can-manage-facilities` }),
		feedbackFacility: (communityId, data) => {
			let url = `/communities/${communityId}/facilities/feedback?content=${encodeURIComponent(data.content)}`
			if (data.facilityId) {
				url += `&facilityId=${data.facilityId}`
			}
			return request({ url, method: 'POST' })
		}
	},
	
	// ========== 论坛 ========== ForumController /api/forum
	forum: {
		createPost: (data) => request({ url: '/forum/posts', method: 'POST', data }),
		getPosts: (params) => request({ url: '/forum/posts', data: params }),
		getPostDetail: (postId) => request({ url: `/forum/posts/${postId}` }),
		updatePost: (postId, data) => request({ url: `/forum/posts/${postId}`, method: 'PUT', data }),
		deletePost: (postId) => request({ url: `/forum/posts/${postId}`, method: 'DELETE' }),
		closePost: (postId) => request({ url: `/forum/posts/${postId}/close`, method: 'POST' }),
		createReply: (postId, data) => request({ url: `/forum/posts/${postId}/replies`, method: 'POST', data }),
		getReplies: (postId) => request({ url: `/forum/posts/${postId}/replies` }),
		deleteReply: (replyId) => request({ url: `/forum/replies/${replyId}`, method: 'DELETE' }),
		getUserPosts: (params) => request({ url: '/forum/posts/user', data: params })
	},
	
	// ========== 信用评价 ========== EvaluationController /api/evaluations
	evaluation: {
		submit: (data) => request({ url: '/evaluations', method: 'POST', data }),
		getUserEvaluations: (userId) => request({ url: `/evaluations/user/${userId}` }),
		checkCanEvaluate: (contractId) => request({ url: `/evaluations/check/${contractId}` })
	},
	
	// ========== 租赁交易 ========== RentalTransactionController /api/transactions
	transaction: {
		getMyList: (params) => request({ url: '/transactions/my', data: params }),
		getLiving: () => request({ url: '/transactions/living' }),
		getDetail: (transactionId) => request({ url: `/transactions/${transactionId}` }),
		getByContractId: (contractId) => request({ url: `/transactions/contract/${contractId}` }),
		confirmCheckin: (transactionId, data) => request({ url: `/transactions/${transactionId}/checkin`, method: 'POST', data }),
		confirmComplete: (transactionId, data) => request({ url: `/transactions/${transactionId}/complete`, method: 'POST', data }),
		cancel: (transactionId, reason) => request({ url: `/transactions/${transactionId}/cancel`, method: 'POST', data: { reason } })
	},
	
	// ========== 租金支付记录 ========== RentPaymentController /api/payments
	payment: {
		create: (data) => request({ url: '/payments/create', method: 'POST', data }),
		confirm: (recordId) => request({ url: `/payments/${recordId}/confirm`, method: 'POST' }),
		getLandlordRecords: () => request({ url: '/payments/landlord/records' }),
		getTenantRecords: () => request({ url: '/payments/tenant/records' }),
		getLandlordStats: () => request({ url: '/payments/landlord/statistics' }),
		getTransactionRecords: (transactionId) => request({ url: `/payments/transaction/${transactionId}` }),
		dispute: (recordId, reason) => request({ url: `/payments/${recordId}/dispute`, method: 'POST', data: { reason } }),
		generateBills: (transactionId) => request({ url: `/payments/generate-bills/${transactionId}`, method: 'POST' })
	},
	
		
	// ========== 文件上传 ========== FileUploadController /api/files
	file: {
		upload: (file, category, relatedId) => request({ url: '/files/upload', method: 'POST', data: { file, category, relatedId } }),
		uploadBatch: (files, category, relatedId) => request({ url: '/files/upload-batch', method: 'POST', data: { files, category, relatedId } }),
		delete: (fileId) => request({ url: `/files/${fileId}`, method: 'DELETE' }),
		getUrl: (fileName) => request({ url: `/files/${fileName}/url` }),
		getByCategory: (category, relatedId) => request({ url: `/files/category/${category}`, data: { relatedId } }),
		getUserFiles: () => request({ url: '/files/user' })
	},
	
	// ========== 用户反馈 ========== FeedbackController /api/feedback
	feedback: {
		submit: (data) => request({ url: '/feedback/submit', method: 'POST', data })
	},
	
	// ========== 小区身份认证 ========== CommunityVerificationController /api/community-verification
	communityVerification: {
		submit: (data) => request({ url: '/community-verification/submit', method: 'POST', data }),
		getStatus: (communityId) => request({ url: '/community-verification/status', data: { communityId } }),
		getHistory: (params) => request({ url: '/community-verification/history', data: params }),
		cancel: (verificationId) => request({ url: `/community-verification/${verificationId}`, method: 'DELETE' }),
		check: (communityId) => request({ url: '/community-verification/check', data: { communityId } })
	},
	
	// ========== 举报 ========== ReportController /api/reports
	report: {
		submit: (data) => request({ url: '/reports/submit', method: 'POST', data }),
		getMyReports: () => request({ url: '/reports/my-reports' }),
		withdraw: (reportId) => request({ url: `/reports/${reportId}/withdraw`, method: 'POST' })
	},
	
	// ========== 系统通知 ========== SystemNotificationController /api/notifications
	notification: {
		getList: (params) => request({ url: '/notifications/list', data: params }),
		getUnreadCount: () => request({ url: '/notifications/unread-count' }),
		markAsRead: (notificationId) => request({ url: `/notifications/${notificationId}/read`, method: 'POST' }),
		markAllAsRead: () => request({ url: '/notifications/mark-all-read', method: 'POST' }),
		delete: (notificationId) => request({ url: `/notifications/${notificationId}`, method: 'DELETE' })
	},
	
	// ========== 房东管理 ========== LandlordController /api/landlord
	landlord: {
		getTenants: (params) => request({ url: '/landlord/tenants', data: params }),
		getTenantDetail: (tenantId) => request({ url: `/landlord/tenants/${tenantId}` }),
		getTenantStats: () => request({ url: '/landlord/tenants/statistics' }),
		getRevenueStatistics: (period) => request({ url: '/landlord/revenue/statistics', data: { period } }),
		getRevenueDetails: (params) => request({ url: '/landlord/revenue/details', data: params }),
		getRevenueRanking: () => request({ url: '/landlord/revenue/ranking' }),
		getHouseStatistics: () => request({ url: '/landlord/houses/statistics' }),
		getContractStats: () => request({ url: '/landlord/contracts/statistics' }),
		getContracts: (params) => request({ url: '/landlord/contracts', data: params })
	},
	
	// ========== 管理员 ========== AdminController /api/admin
	admin: {
		getUsers: (params) => request({ url: '/admin/users', data: params }),
		auditUserAuth: (userId, data) => request({ url: `/admin/users/${userId}/auth-audit`, method: 'POST', data }),
		toggleUserStatus: (userId, data) => request({ url: `/admin/users/${userId}/toggle-status`, method: 'POST', data }),
		getCommunities: (params) => request({ url: '/admin/communities', data: params }),
		getCommunityDetail: (communityId) => request({ url: `/admin/communities/${communityId}` }),
		addCommunity: (data) => request({ url: '/admin/communities', method: 'POST', data }),
		updateCommunity: (communityId, data) => request({ url: `/admin/communities/${communityId}`, method: 'PUT', data }),
		deleteCommunity: (communityId) => request({ url: `/admin/communities/${communityId}`, method: 'DELETE' }),
		assignCommunity: (adminId, communityId) => request({ url: `/admin/admins/${adminId}/assign-community`, method: 'POST', data: { communityId } }),
		getUnassignedAdmins: () => request({ url: '/admin/admins/unassigned-community' }),
		getDashboard: () => request({ url: '/admin/dashboard' }),
		getPendingHouses: (params) => request({ url: '/admin/houses/pending', data: params }),
		auditHouse: (houseId, data) => request({ url: `/admin/houses/${houseId}/audit`, method: 'POST', data }),
		getPendingVerifications: (params) => request({ url: '/admin/verifications/platform/pending', data: params }),
		communityAuditVerification: (verificationId, data) => request({ url: `/admin/verifications/${verificationId}/community-audit`, method: 'POST', data }),
		platformAuditVerification: (verificationId, data) => request({ url: `/admin/verifications/${verificationId}/platform-audit`, method: 'POST', data }),
		getPendingContracts: (params) => request({ url: '/admin/contracts/pending', data: params }),
		auditContract: (contractId, data) => request({ url: `/admin/contracts/${contractId}/audit`, method: 'POST', data }),
		terminateContract: (contractId, data) => request({ url: `/admin/contracts/${contractId}/terminate`, method: 'POST', data }),
		deleteForumPost: (postId, data) => request({ url: `/admin/forum/posts/${postId}`, method: 'DELETE', data }),
		deleteForumReply: (replyId, data) => request({ url: `/admin/forum/replies/${replyId}`, method: 'DELETE', data }),
		batchSendNotifications: (data) => request({ url: '/admin/notifications/batch-send', method: 'POST', data }),
		getCommunityDashboard: (communityId) => request({ url: `/admin/dashboard/community/${communityId}` }),
		getCommunityUsers: (communityId, params) => request({ url: `/admin/users/community/${communityId}`, data: params }),
		getCommunityReports: (communityId, params) => request({ url: `/admin/reports/community/${communityId}`, data: params }),
		handleReport: (reportId, data) => request({ url: `/admin/reports/${reportId}/handle`, method: 'POST', data }),
		getCommunityFacilities: (communityId, params) => request({ url: `/admin/facilities/community/${communityId}`, data: params }),
		addFacility: (communityId, data) => request({ url: `/admin/facilities/community/${communityId}`, method: 'POST', data }),
		updateFacility: (facilityId, data) => request({ url: `/admin/facilities/${facilityId}`, method: 'PUT', data }),
		deleteFacility: (facilityId) => request({ url: `/admin/facilities/${facilityId}`, method: 'DELETE' }),
		getCommunityStats: (communityId) => request({ url: `/admin/stats/community/${communityId}` }),
		getSystemStatistics: () => request({ url: '/admin/statistics' })
	}
}

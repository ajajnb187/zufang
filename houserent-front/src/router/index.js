import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  // 平台管理员系统
  {
    path: '/platform-admin',
    name: 'PlatformAdmin',
    component: () => import('@/views/platform-admin/PlatformDashboard.vue'),
    meta: { requiresAuth: true, requiresRole: 'platform' },
    children: [
      {
        path: '',
        name: 'PlatformDashboard',
        component: () => import('@/views/platform-admin/GlobalDashboard.vue')
      },
      // 管理员管理
      {
        path: 'admins',
        name: 'AdminManagement',
        component: () => import('@/views/platform-admin/AdminManagement.vue')
      },
      // 小区管理
      {
        path: 'communities',
        name: 'CommunityManagement',
        component: () => import('@/views/platform-admin/CommunityList.vue')
      },
      // 违规处理
      {
        path: 'monitor',
        name: 'ViolationHandling',
        component: () => import('@/views/platform-admin/ViolationHandling.vue')
      },
      // 条例上传
      {
        path: 'settings',
        name: 'RulesUpload',
        component: () => import('@/views/platform-admin/RulesUpload.vue')
      },
      // 权限管控
      {
        path: 'permissions',
        name: 'PermissionControl',
        component: () => import('@/views/platform-admin/PermissionControl.vue')
      },
      // 认证终审
      {
        path: 'verification-review',
        name: 'VerificationReview',
        component: () => import('@/views/platform-admin/VerificationReview.vue')
      }
    ]
  },
  // 小区管理员系统
  {
    path: '/community-admin',
    name: 'CommunityAdmin',
    component: () => import('@/views/community-admin/CommunityDashboard.vue'),
    meta: { requiresAuth: true, requiresRole: 'community' },
    children: [
      {
        path: '',
        name: 'CommunityDashboard',
        component: () => import('@/views/community-admin/CommunityHome.vue')
      },
      // 认证审核
      {
        path: 'auth-review',
        name: 'AuthReview',
        component: () => import('@/views/community-admin/UserAuthReview.vue')
      },
      // 房源审核
      {
        path: 'house-review',
        name: 'HouseReview',
        component: () => import('@/views/community-admin/HouseReview.vue')
      },
      // 举报处理
      {
        path: 'report-handling',
        name: 'ReportHandling',
        component: () => import('@/views/community-admin/ReportHandle.vue')
      },
      // 合同审核
      {
        path: 'contract-review',
        name: 'ContractReview',
        component: () => import('@/views/community-admin/ContractReview.vue')
      },
      // 配套维护
      {
        path: 'facility-maintenance',
        name: 'FacilityMaintenance',
        component: () => import('@/views/community-admin/FacilitiesMaintenance.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 根据角色重定向到不同系统
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  const userInfo = JSON.parse(localStorage.getItem('admin_user') || '{}')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    // 根据角色重定向到对应系统
    if (userInfo.adminType === 'platform') {
      next('/platform-admin')
    } else if (userInfo.adminType === 'community') {
      next('/community-admin')
    } else {
      next('/login')
    }
  } else if (to.meta.requiresRole && userInfo.adminType !== to.meta.requiresRole) {
    // 角色权限检查
    next('/login')
  } else {
    next()
  }
})

export default router

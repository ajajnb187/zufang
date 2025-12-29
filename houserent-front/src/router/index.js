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
      { path: '', name: 'PlatformDashboard', component: () => import('@/views/platform-admin/GlobalDashboard.vue') },
      { path: 'admins', name: 'AdminManagement', component: () => import('@/views/platform-admin/AdminManagement.vue') },
      { path: 'communities', name: 'CommunityManagement', component: () => import('@/views/platform-admin/CommunityList.vue') },
      { path: 'monitor', name: 'ViolationHandling', component: () => import('@/views/platform-admin/ViolationHandling.vue') },
      { path: 'settings', name: 'RulesUpload', component: () => import('@/views/platform-admin/RulesUpload.vue') },
      { path: 'permissions', name: 'PermissionControl', component: () => import('@/views/platform-admin/PermissionControl.vue') },
      { path: 'verification-review', name: 'VerificationReview', component: () => import('@/views/platform-admin/VerificationReview.vue') }
    ]
  },
  // 小区管理员系统
  {
    path: '/community-admin',
    name: 'CommunityAdmin',
    component: () => import('@/views/community-admin/CommunityDashboard.vue'),
    meta: { requiresAuth: true, requiresRole: 'community' },
    children: [
      { path: '', name: 'CommunityDashboard', component: () => import('@/views/community-admin/CommunityHome.vue') },
      { path: 'auth-review', name: 'AuthReview', component: () => import('@/views/community-admin/UserAuthReview.vue') },
      { path: 'house-review', name: 'HouseReview', component: () => import('@/views/community-admin/HouseReview.vue') },
      { path: 'report-handling', name: 'ReportHandling', component: () => import('@/views/community-admin/ReportHandle.vue') },
      { path: 'contract-review', name: 'ContractReview', component: () => import('@/views/community-admin/ContractReview.vue') },
      { path: 'facility-maintenance', name: 'FacilityMaintenance', component: () => import('@/views/community-admin/FacilitiesMaintenance.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  const userInfo = JSON.parse(localStorage.getItem('admin_user') || '{}')

  // 1. 需要登录但没登录
  if (to.meta.requiresAuth && !token) {
    next('/login')
  }
  // 2. 已登录但访问 /login
  else if (to.path === '/login' && token) {
    if (userInfo.adminType === 'platform') {
      next('/platform-admin')
    } else if (userInfo.adminType === 'community') {
      next('/community-admin')
    } else {
      // 角色无效，直接回到登录页
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
      next('/login')
    }
  }
  // 3. 角色权限检查
  else if (to.meta.requiresRole && userInfo.adminType !== to.meta.requiresRole) {
    // 用户要求直接回到登录页
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    next('/login')
  }
  // 4. 其他情况直接放行
  else {
    next()
  }
})

export default router
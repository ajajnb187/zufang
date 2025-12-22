/**
 * 路由拦截器
 * 用于处理页面跳转前的权限验证和逻辑处理
 */

import { isLogin, getUserType } from './userType.js'

// 路由拦截配置
const routeConfig = {
  // 需要登录的页面
  requireAuth: [
    // 租客/通用页面
    '/pages/home/home',
    '/pages/favorite/favorite',
    '/pages/chat/chat',
    '/pages/chat/chat-detail/chat-detail',
    '/pages/profile/profile',
    '/pages/house/detail/detail',
    '/pages/house/search/search',
    '/pages/appointment/create/create',
    '/pages/community/auth/auth',
    '/pages/community/facilities/facilities',
    '/pages/evaluation/evaluation',
    '/pages/contract/list/list',
    '/pages/contract/sign/sign',
    '/pages/contract/detail/detail',
    '/pages/contract/create/create',
    '/pages/notification/notification',
    '/pages/landlord-verification/apply',
    '/pages/landlord-verification/status',
    // 房东专属页面
    '/pages/landlord/houses/houses',
    '/pages/landlord/publish/publish',
    '/pages/landlord/tenants/tenants',
    '/pages/landlord/revenue/revenue',
    '/pages/landlord/contracts/contracts',
    '/pages/landlord/profile/profile',
    '/pages/landlord/chat/chat',
    '/pages/landlord/house-detail/house-detail'
  ],
  
  // 房东专属页面
  landlordOnly: [
    '/pages/landlord/houses/houses',
    '/pages/landlord/publish/publish',
    '/pages/landlord/tenants/tenants',
    '/pages/landlord/revenue/revenue',
    '/pages/landlord/contracts/contracts',
    '/pages/landlord/profile/profile',
    '/pages/landlord/chat/chat',
    '/pages/landlord/house-detail/house-detail'
  ],
  
  // 租客专属页面（房东也可以访问，所以实际上不需要限制）
  tenantOnly: [
    // 移除所有限制，房东可以访问租客功能
  ]
}

/**
 * 初始化路由拦截器
 */
function initRouteInterceptor() {
  console.log('路由拦截器已初始化')
  
  // 重写 uni.navigateTo
  const originalNavigateTo = uni.navigateTo
  uni.navigateTo = function(options) {
    if (checkRoutePermission(options.url)) {
      return originalNavigateTo.call(this, options)
    }
    return Promise.reject('路由拦截')
  }
  
  // 重写 uni.redirectTo
  const originalRedirectTo = uni.redirectTo
  uni.redirectTo = function(options) {
    if (checkRoutePermission(options.url)) {
      return originalRedirectTo.call(this, options)
    }
    return Promise.reject('路由拦截')
  }
  
  // 重写 uni.reLaunch
  const originalReLaunch = uni.reLaunch
  uni.reLaunch = function(options) {
    console.log('【路由拦截器】uni.reLaunch被调用，目标URL:', options.url)
    if (checkRoutePermission(options.url)) {
      console.log('【路由拦截器】权限检查通过，执行原始reLaunch')
      const result = originalReLaunch.call(this, options)
      console.log('【路由拦截器】原始reLaunch执行完成')
      return result
    } else {
      console.log('【路由拦截器】权限检查失败，拒绝跳转')
      return Promise.reject('路由拦截')
    }
  }
  
  // 重写 uni.switchTab
  const originalSwitchTab = uni.switchTab
  uni.switchTab = function(options) {
    if (checkRoutePermission(options.url)) {
      return originalSwitchTab.call(this, options)
    }
    return Promise.reject('路由拦截')
  }
}

/**
 * 检查路由权限
 * @param {string} url - 路径
 * @returns {boolean} - 是否允许访问
 */
function checkRoutePermission(url) {
  // 解析路径
  const path = url.split('?')[0]
  console.log('【路由拦截器】checkRoutePermission被调用，检查路径:', path)
  
  // 检查是否需要登录
  if (routeConfig.requireAuth.includes(path)) {
    console.log('【路由拦截器】该路径需要登录验证')
    if (!isLogin()) {
      console.log('【路由拦截器】用户未登录，拒绝访问')
      uni.showToast({
        title: '请先登录',
        icon: 'none',
        duration: 1500
      })
      
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login/login'
        })
      }, 1500)
      
      return false
    } else {
      console.log('【路由拦截器】用户已登录，继续权限检查')
    }
  } else {
    console.log('【路由拦截器】该路径不需要登录验证')
  }
  
  // 检查房东权限
  if (routeConfig.landlordOnly.includes(path)) {
    const userType = getUserType()
    console.log('【路由拦截器】检查房东权限，path:', path)
    console.log('【路由拦截器】获取到的userType:', userType, '类型:', typeof userType)
    
    if (userType !== 4) { // 4 = 房东
      console.log('【路由拦截器】userType不等于4，拦截访问')
      uni.showToast({
        title: '仅房东可访问',
        icon: 'none',
        duration: 1500
      })
      
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/home/home'
        })
      }, 1500)
      
      return false
    } else {
      console.log('【路由拦截器】userType等于4，允许访问房东页面')
    }
  }
  
  // 检查租客权限（已移除限制，房东可以访问所有租客功能）
  // 不再限制租客页面访问，房东可以切换使用租客功能
  
  return true
}

/**
 * 获取当前页面路径
 */
function getCurrentPath() {
  const pages = getCurrentPages()
  if (pages.length === 0) return ''
  const currentPage = pages[pages.length - 1]
  return '/' + currentPage.route
}

/**
 * 检查是否在当前页面
 */
function isCurrentPage(url) {
  const currentPath = getCurrentPath()
  const targetPath = url.split('?')[0]
  return currentPath === targetPath
}

/**
 * 跳转到登录页
 */
function redirectToLogin() {
  uni.navigateTo({
    url: '/pages/login/login'
  })
}

/**
 * 返回上一页或默认页面
 */
function navigateBackOrDefault(defaultUrl = '/pages/home/home') {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.reLaunch({
      url: defaultUrl
    })
  }
}

// 导出工具函数
export {
  initRouteInterceptor,
  checkRoutePermission,
  getCurrentPath,
  isCurrentPage,
  redirectToLogin,
  navigateBackOrDefault,
  routeConfig
}

// 默认导出
export default initRouteInterceptor

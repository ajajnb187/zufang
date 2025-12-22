import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { adminLogin, adminRegister } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 初始化时从 localStorage 读取数据
  const storedUser = localStorage.getItem('admin_user')
  const initialUser = storedUser ? JSON.parse(storedUser) : {}
  
  const token = ref(localStorage.getItem('admin_token') || '')
  const user = ref(initialUser)

  const isAuthenticated = computed(() => !!token.value)
  const isPlatformAdmin = computed(() => {
    console.log('计算 isPlatformAdmin:', user.value.adminType, user.value.adminType === 'platform')
    return user.value.adminType === 'platform'
  })
  const isCommunityAdmin = computed(() => {
    console.log('计算 isCommunityAdmin:', user.value.adminType, user.value.adminType === 'community')
    return user.value.adminType === 'community'
  })

  const login = async (loginData) => {
    try {
      const response = await adminLogin(loginData)
      if (response.code === 200) {
        token.value = response.data.token
        user.value = response.data
        
        // 检查 adminType 是否存在
        if (!response.data.adminType || (response.data.adminType !== 'platform' && response.data.adminType !== 'community')) {
          console.error('登录数据异常:', response.data)
          return { 
            success: false, 
            message: '账号权限配置错误：缺少管理员身份信息。\n请联系管理员在数据库 admins 表中添加对应记录。\n参考文档: README_LOGIN_FIX.md' 
          }
        }
        
        // 更新响应式状态和localStorage
        token.value = response.data.token
        user.value = response.data
        localStorage.setItem('admin_token', response.data.token)
        localStorage.setItem('admin_user', JSON.stringify(response.data))
        
        return { success: true }
      } else {
        return { success: false, message: response.message || '登录失败' }
      }
    } catch (error) {
      console.error('登录失败:', error)
      return { 
        success: false, 
        message: error.response?.data?.message || error.message || '登录失败，请检查后端服务是否启动' 
      }
    }
  }

  const logout = () => {
    token.value = ''
    user.value = {}
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  const register = async (registerData) => {
    try {
      const response = await adminRegister(registerData)
      if (response.code === 200) {
        return { success: true, message: '注册成功' }
      } else {
        return { success: false, message: response.message || '注册失败' }
      }
    } catch (error) {
      console.error('注册失败:', error)
      return { 
        success: false, 
        message: error.response?.data?.message || error.message || '注册失败，请检查后端服务是否启动' 
      }
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    isPlatformAdmin,
    isCommunityAdmin,
    login,
    logout,
    register
  }
})

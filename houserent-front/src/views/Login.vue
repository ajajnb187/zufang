<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-left">
        <div class="left-content">
          <div class="brand">
            <div class="brand-icon-box">
              <el-icon class="brand-icon"><House /></el-icon>
            </div>
            <span class="brand-text">稳住租房</span>
          </div>
          <div class="intro">
            <h1>理想生活</h1>
            <h1>从这里启程</h1>
            <p>基于微信小区的房屋租赁管理平台，为您提供真实房源、便捷沟通、安全签约的一站式服务体验。</p>
          </div>
          <div class="decoration-circles">
            <div class="circle circle-1"></div>
            <div class="circle circle-2"></div>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="login-form-box">
          <div class="form-header">
            <h2>欢迎登录</h2>
            <p>请输入您的账号信息以继续</p>
          </div>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            size="large"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入手机号"
                prefix-icon="User"
                class="custom-input"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                class="custom-input"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <!-- <el-button link type="primary">忘记密码？</el-button> -->
            </div>
            
            <el-form-item>
              <el-button
                type="primary"
                class="submit-btn"
                :loading="loading"
                @click="handleLogin"
                auto-insert-space
              >
                登录
              </el-button>
            </el-form-item>
            
            <!-- 注册功能已禁用，小区管理员由平台管理员创建 -->
            <!-- <div class="register-link">
              还没有账号？ <el-link type="primary" @click="showRegisterDialog">立即注册</el-link>
            </div> -->
          </el-form>
        </div>
      </div>
    </div>
    
    <!-- 注册对话框（已禁用，小区管理员由平台管理员创建） -->
    <!-- <el-dialog
      v-model="registerDialogVisible"
      title="注册账号"
      width="440px"
      align-center
      class="register-dialog"
      destroy-on-close
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        size="large"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" prefix-icon="Iphone" />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="请输入昵称" prefix-icon="User" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock" />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password prefix-icon="Lock" />
        </el-form-item>
        
        <el-form-item label="注册身份" prop="adminType">
          <div class="role-selector">
            <div 
              class="role-card" 
              :class="{ active: registerForm.adminType === 'platform' }"
              @click="registerForm.adminType = 'platform'"
            >
              <div class="role-icon-box">
                <el-icon><Monitor /></el-icon>
              </div>
              <span>平台管理员</span>
              <div class="check-mark" v-if="registerForm.adminType === 'platform'"><el-icon><Check /></el-icon></div>
            </div>
            <div 
              class="role-card" 
              :class="{ active: registerForm.adminType === 'community' }"
              @click="registerForm.adminType = 'community'"
            >
              <div class="role-icon-box">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <span>小区管理员</span>
              <div class="check-mark" v-if="registerForm.adminType === 'community'"><el-icon><Check /></el-icon></div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="registerDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="registerLoading" @click="handleRegister">
            立即注册
          </el-button>
        </div>
      </template>
    </el-dialog> -->
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, House, Monitor, OfficeBuilding, Check, Iphone } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})
const rememberMe = ref(false)

const registerDialogVisible = ref(false)
const registerFormRef = ref()
const registerLoading = ref(false)
const registerForm = reactive({
  phone: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  adminType: 'platform'
})

const loginRules = {
  username: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度2-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    // 验证表单
    const valid = await loginFormRef.value.validate().catch(() => false)
    if (!valid) {
      return
    }
    
    loading.value = true
    
    const result = await authStore.login({
      username: loginForm.username,
      password: loginForm.password
    })
    
    if (result.success) {
      ElMessage.success('登录成功')
      
      // 根据用户类型跳转不同页面
      if (authStore.isPlatformAdmin) {
        router.push('/platform-admin')
      } else if (authStore.isCommunityAdmin) {
        router.push('/community-admin')
      }
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    // 如果错误包含验证信息，显示具体的验证错误
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('登录失败，请检查网络连接或联系管理员')
    }
  } finally {
    loading.value = false
  }
}

const showRegisterDialog = () => {
  registerDialogVisible.value = true
  if (registerFormRef.value) {
    registerFormRef.value.resetFields()
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    
    const result = await authStore.register({
      phone: registerForm.phone,
      nickname: registerForm.nickname,
      password: registerForm.password,
      adminType: registerForm.adminType,
      communityId: registerForm.adminType === 'community' ? registerForm.communityId : null
    })
    
    if (result.success) {
      ElMessage.success('注册成功，请登录')
      registerDialogVisible.value = false
      loginForm.username = registerForm.phone
      loginForm.password = registerForm.password
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请检查网络连接或联系管理员')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  padding: 20px;
  background-image: 
    radial-gradient(at 10% 10%, rgba(64, 158, 255, 0.1) 0px, transparent 50%),
    radial-gradient(at 90% 90%, rgba(103, 194, 58, 0.1) 0px, transparent 50%);
}

.login-content {
  display: flex;
  width: 1000px;
  height: 600px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #36d1dc 100%);
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
  color: white;
}

.left-content {
  position: relative;
  z-index: 2;
}

.brand {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 60px;
}

.brand-icon-box {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}

.brand-icon {
  font-size: 28px;
  color: white;
}

.brand-text {
  font-size: 28px;
  font-weight: bold;
  letter-spacing: 2px;
}

.intro h1 {
  font-size: 44px;
  line-height: 1.2;
  font-weight: 800;
  margin: 0;
  letter-spacing: 2px;
}

.intro p {
  margin-top: 30px;
  font-size: 16px;
  line-height: 1.8;
  opacity: 0.9;
  max-width: 85%;
  font-weight: 300;
}

.decoration-circles .circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(2px);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: -50px;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: #ffffff;
}

.login-form-box {
  width: 100%;
  max-width: 380px;
}

.form-header {
  text-align: left;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 12px;
  font-weight: 600;
}

.form-header p {
  color: #909399;
  font-size: 16px;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 12px;
  background-color: #f5f7fa;
  box-shadow: none !important;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background-color: white;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1) !important;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  border-radius: 25px;
  font-weight: 500;
  box-shadow: 0 10px 20px rgba(64, 158, 255, 0.2);
  transition: transform 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.3);
}

.submit-btn:active {
  transform: translateY(0);
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: #606266;
  margin-top: 10px;
}

.role-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.role-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 16px;
  border: 2px solid #f0f2f5;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.role-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  color: #909399;
  font-size: 20px;
  transition: all 0.3s;
}

.role-card span {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.role-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.role-card.active .role-icon-box {
  background: white;
  color: #409eff;
}

.role-card.active span {
  color: #409eff;
}

.check-mark {
  position: absolute;
  top: 0;
  right: 0;
  background: #409eff;
  color: white;
  width: 24px;
  height: 24px;
  border-bottom-left-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

@media (max-width: 992px) {
  .login-content {
    width: 100%;
    max-width: 500px;
    height: auto;
    flex-direction: column;
  }
  
  .login-left {
    display: none;
  }
  
  .login-right {
    padding: 40px;
  }
}
</style>

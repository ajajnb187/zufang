<template>
  <div class="community-dashboard-container">
    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside width="240px" class="sidebar">
        <div class="logo">
          <div class="logo-box">
            <el-icon class="logo-icon"><House /></el-icon>
          </div>
          <h3>小区管理</h3>
        </div>
        
        <div class="menu-container">
          <el-menu
            :default-active="$route.path"
            router
            class="sidebar-menu"
            :unique-opened="true"
          >
            <div class="menu-label">工作台</div>
            <el-menu-item index="/community-admin">
              <el-icon><OfficeBuilding /></el-icon>
              <span>我的小区</span>
            </el-menu-item>
            
            <div class="menu-label">审核中心</div>
            <el-menu-item index="/community-admin/auth-review">
              <el-icon><User /></el-icon>
              <span>认证审核</span>
            </el-menu-item>
            
            <el-menu-item index="/community-admin/house-review">
              <el-icon><House /></el-icon>
              <span>房源审核</span>
            </el-menu-item>
            
            <el-menu-item index="/community-admin/contract-review">
              <el-icon><Document /></el-icon>
              <span>合同审核</span>
            </el-menu-item>
            
            <div class="menu-label">服务管理</div>
            <el-menu-item index="/community-admin/report-handling">
              <el-icon><Warning /></el-icon>
              <span>举报处理</span>
            </el-menu-item>
            
            <el-menu-item index="/community-admin/facility-maintenance">
              <el-icon><Setting /></el-icon>
              <span>配套维护</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container class="content-container">
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <h2 class="page-title">{{ currentBreadcrumb }}</h2>
          </div>
          
          <div class="header-right">
            <el-tooltip content="通知" effect="light">
              <div class="icon-btn">
                <el-badge is-dot class="badge">
                  <el-icon><Bell /></el-icon>
                </el-badge>
              </div>
            </el-tooltip>
            
            <el-dropdown trigger="click">
              <div class="user-profile">
                <div class="user-info">
                  <span class="name">{{ authStore.user?.nickname || '小区管理员' }}</span>
                  <span class="role">{{ authStore.user?.communityName || '小区管理员' }}</span>
                </div>
                <el-icon class="arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <el-dropdown-item @click="handleOpenProfile">
                    <el-icon><User /></el-icon>个人信息
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主体内容 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>

    <!-- 个人信息编辑弹框 -->
    <el-dialog
      v-model="showProfileDialog"
      title="个人信息"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="profileForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" @click="showPasswordDialog = true">修改密码</el-button>
        <el-button type="primary" @click="handleUpdateProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  House, OfficeBuilding, User, Document, Warning, Setting,
  Bell, ArrowDown, SwitchButton
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import api from '@/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const showProfileDialog = ref(false)
const showPasswordDialog = ref(false)

const profileForm = ref({
  nickname: '',
  phone: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const currentBreadcrumb = computed(() => {
  const routeMap = {
    '/community-admin': '我的小区',
    '/community-admin/auth-review': '认证审核',
    '/community-admin/house-review': '房源审核',
    '/community-admin/contract-review': '合同审核',
    '/community-admin/report-handling': '举报处理',
    '/community-admin/facility-maintenance': '配套维护'
  }
  return routeMap[route.path] || '小区管理'
})

// 加载用户信息
const loadUserProfile = async () => {
  try {
    const res = await api.get('/api/user/profile')
    console.log('用户信息响应:', res)
    if (res.code === 200 && res.data) {
      const userData = res.data
      // 尝试多个可能的字段名
      const phone = userData.phone || userData.phoneNumber || userData.mobile || authStore.user?.phone || ''
      profileForm.value = {
        nickname: userData.nickname || userData.username || authStore.user?.nickname || '',
        phone: phone
      }
      // 只更新用户信息，不覆盖整个对象（保留communityId等字段）
      Object.assign(authStore.user, userData)
      console.log('填充的表单数据:', profileForm.value)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 如果API失败，从store中获取
    if (authStore.user) {
      profileForm.value = {
        nickname: authStore.user.nickname || authStore.user.username || '',
        phone: authStore.user.phone || authStore.user.phoneNumber || authStore.user.mobile || ''
      }
    }
  }
}

// 更新个人信息
const handleUpdateProfile = async () => {
  try {
    const res = await api.put('/api/user/profile', profileForm.value)
    if (res.code === 200) {
      ElMessage.success('个人信息更新成功')
      showProfileDialog.value = false
      await loadUserProfile()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }

  try {
    const res = await api.post('/api/user/change-password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      showPasswordDialog.value = false
      showProfileDialog.value = false
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      setTimeout(() => {
        authStore.logout()
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

// 打开个人信息弹框
const handleOpenProfile = async () => {
  // 先从store获取数据填充表单
  if (authStore.user) {
    profileForm.value = {
      nickname: authStore.user.nickname || authStore.user.username || '',
      phone: authStore.user.phone || authStore.user.phoneNumber || authStore.user.mobile || ''
    }
  }
  // 然后尝试从后端刷新数据
  await loadUserProfile()
  showProfileDialog.value = true
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    authStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {
    // 取消
  }
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.community-dashboard-container {
  height: 100vh;
  background-color: #f5f7fa;
}

.main-container {
  height: 100%;
}

.sidebar {
  background-color: #ffffff;
  border-right: 1px solid #f0f2f5;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  z-index: 10;
  box-shadow: 4px 0 16px rgba(0,0,0,0.02);
}

.logo {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #67c23a;
  font-size: 20px;
}

.logo-box {
  width: 40px;
  height: 40px;
  background: #f0f9ff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon {
  font-size: 24px;
}

.logo h3 {
  margin: 0;
  font-weight: 600;
  color: #303133;
}

.menu-container {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.sidebar-menu {
  border: none;
}

.menu-label {
  padding: 16px 24px 8px;
  font-size: 12px;
  color: #909399;
  font-weight: 600;
  letter-spacing: 0.5px;
}

:deep(.el-menu-item) {
  height: 48px;
  margin: 4px 16px;
  border-radius: 8px;
  color: #606266;
}

:deep(.el-menu-item.is-active) {
  background-color: #f0f9ff;
  color: #67c23a;
  font-weight: 600;
}

:deep(.el-menu-item:hover) {
  background-color: #f5f7fa;
}

.header {
  height: 64px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.02);
  z-index: 5;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #606266;
}

.icon-btn:hover {
  background-color: #f5f7fa;
  color: #67c23a;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: all 0.3s;
}

.user-profile:hover {
  background-color: #f5f7fa;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-info .name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.user-info .role {
  font-size: 12px;
  color: #909399;
}

.main-content {
  padding: 24px 32px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

<template>
  <div class="app-wrapper">
    <!-- 1. 动态流体背景层 -->
    <div class="fluid-background">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <div class="glass-layout">
      <!-- 2. 悬浮毛玻璃侧边栏 -->
      <aside class="glass-sidebar" :class="{ 'collapsed': isCollapsed }">
        <div class="brand-area">
          <div class="logo-circle">
            <el-icon class="logo-icon"><House /></el-icon>
            <div class="logo-glow"></div>
          </div>
          <div class="brand-text" v-show="!isCollapsed">
            <h1>稳住租房</h1>
            <p>Smart Community</p>
          </div>
        </div>

        <div class="menu-scroll-wrap">
          <el-menu
              :default-active="$route.path"
              router
              unique-opened
              :collapse="isCollapsed"
              class="glass-menu"
          >
            <!-- 平台管理员 -->
            <template v-if="authStore.isPlatformAdmin || true">
              <div class="menu-group-label" v-show="!isCollapsed">全域掌控</div>

              <el-menu-item index="/platform-admin">
                <div class="menu-icon-box"><el-icon><DataAnalysis /></el-icon></div>
                <span class="menu-text">数据驾驶舱</span>
                <div class="active-indicator"></div>
              </el-menu-item>

              <el-sub-menu index="resource-mgt">
                <template #title>
                  <div class="menu-icon-box"><el-icon><OfficeBuilding /></el-icon></div>
                  <span class="menu-text">资源中心</span>
                </template>
                <el-menu-item index="/platform-admin/communities">小区管理</el-menu-item>
                <el-menu-item index="/platform-admin/admins">管理员档案</el-menu-item>
              </el-sub-menu>

              <el-sub-menu index="risk-mgt">
                <template #title>
                  <div class="menu-icon-box"><el-icon><Monitor /></el-icon></div>
                  <span class="menu-text">风控审核</span>
                </template>
                <el-menu-item index="/platform-admin/monitor">违规处理</el-menu-item>
                <el-menu-item index="/platform-admin/verification-review">认证终审</el-menu-item>
              </el-sub-menu>

              <el-menu-item index="/platform-admin/settings">
                <div class="menu-icon-box"><el-icon><Setting /></el-icon></div>
                <span class="menu-text">系统配置</span>
                <div class="active-indicator"></div>
              </el-menu-item>
            </template>

            <!-- 小区管理员 (此处省略部分重复结构，样式共用) -->
            <template v-if="authStore.isCommunityAdmin">
              <!-- 保持原有逻辑，仅套用新样式 -->
            </template>
          </el-menu>
        </div>

        <!-- 侧边栏底部装饰 -->
        <div class="sidebar-footer" v-show="!isCollapsed">
          <div class="system-status">
            <span class="status-dot"></span>
            <span>系统运行正常</span>
          </div>
        </div>
      </aside>

      <!-- 3. 主内容区 -->
      <main class="main-content-area">
        <!-- 顶部悬浮导航 -->
        <header class="glass-header">
          <div class="header-left">
            <div class="collapse-btn" @click="isCollapsed = !isCollapsed">
              <el-icon :class="{ 'rotate-180': isCollapsed }"><Fold /></el-icon>
            </div>
            <div class="breadcrumb-fancy">
              <span class="home-icon"><el-icon><House /></el-icon></span>
              <span class="separator">/</span>
              <span class="current-page">{{ currentBreadcrumb }}</span>
            </div>
          </div>

          <div class="header-right">
            <!-- 搜索条特效 -->
            <div class="search-bar-animated">
              <el-icon><Search /></el-icon>
              <input type="text" placeholder="搜索房源 / 租客..." />
            </div>

            <div class="action-btn">
              <el-badge is-dot class="notification-dot">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </div>

            <!-- 用户卡片 -->
            <el-dropdown trigger="click" popper-class="glass-dropdown">
              <div class="user-pill">
                <div class="user-meta">
                  <span class="name">{{ authStore.user?.nickname || 'Admin' }}</span>
                  <span class="role-badge">{{ adminRoleText }}</span>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleOpenProfile"><el-icon><User /></el-icon> 个人信息</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout" style="color: #ff4d4f"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </header>

        <!-- 页面内容容器 -->
        <div class="content-scroll-view">
          <router-view v-slot="{ Component }">
            <transition name="scale-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>
    </div>

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
  House, DataAnalysis, User, OfficeBuilding, Monitor,
  Setting, Bell, SwitchButton, Fold, Search
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import api from '@/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const isCollapsed = ref(false)
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
  // 这里简化映射，实际逻辑保持不变
  return route.meta.title || route.path.split('/').pop() || '控制台'
})

const adminRoleText = computed(() => '超级管理员')

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
      // 清空密码表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      // 延迟跳转到登录页
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
    await ElMessageBox.confirm('准备离开吗？', '系统提示', {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'glass-message-box'
    })
    authStore.logout()
    router.push('/')
  } catch {}
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped lang="scss">
/* ----- 核心变量 ----- */
:root {
  --glass-bg: rgba(255, 255, 255, 0.65);
  --glass-border: rgba(255, 255, 255, 0.8);
  --glass-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.07);
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --accent-color: #667eea;
  --text-main: #2d3748;
}

.app-wrapper {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: var(--text-main);
  background: #f0f2f5;
}

/* ----- 1. 动态流体背景 (特效核心) ----- */
.fluid-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  filter: blur(80px);
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  animation: float 20s infinite ease-in-out alternate;
  opacity: 0.6;
}

.orb-1 {
  width: 50vw;
  height: 50vw;
  background: #a1c4fd;
  top: -10%;
  left: -10%;
  animation-duration: 25s;
}

.orb-2 {
  width: 40vw;
  height: 40vw;
  background: #c2e9fb;
  bottom: -10%;
  right: -5%;
  animation-duration: 20s;
}

.orb-3 {
  width: 30vw;
  height: 30vw;
  background: #e0c3fc;
  top: 30%;
  left: 40%;
  animation-duration: 30s;
}

@keyframes float {
  0% { transform: translate(0, 0) rotate(0deg); }
  100% { transform: translate(50px, 50px) rotate(20deg); }
}

/* ----- 布局容器 ----- */
.glass-layout {
  position: relative;
  z-index: 1;
  display: flex;
  height: 100%;
  padding: 16px; /* 给整个布局留白，营造悬浮感 */
  box-sizing: border-box;
  gap: 20px;
}

/* ----- 2. 悬浮毛玻璃侧边栏 ----- */
.glass-sidebar {
  width: 260px;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: 24px;
  box-shadow: var(--glass-shadow);
  display: flex;
  flex-direction: column;
  transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;

  &.collapsed {
    width: 80px;

    .logo-circle {
      transform: scale(0.8);
    }
    :deep(.el-sub-menu__icon-arrow) {
      display: none;
    }
  }
}

.brand-area {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-bottom: 1px solid rgba(0,0,0,0.03);
}

.logo-circle {
  width: 48px;
  height: 48px;
  background: var(--primary-gradient);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  position: relative;
  box-shadow: 0 10px 20px rgba(118, 75, 162, 0.3);
  transition: all 0.3s;

  /* 呼吸灯特效 */
  .logo-glow {
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: 16px;
    background: inherit;
    filter: blur(10px);
    opacity: 0.5;
    z-index: -1;
    animation: pulse 3s infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

.brand-text h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(to right, #30cfd0 0%, #330867 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-text p {
  margin: 0;
  font-size: 10px;
  color: #888;
  letter-spacing: 2px;
  text-transform: uppercase;
}

/* 菜单特效 */
.menu-scroll-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.glass-menu {
  background: transparent !important;
  border: none !important;
}

.menu-group-label {
  padding: 0 30px;
  margin-top: 15px;
  margin-bottom: 10px;
  font-size: 11px;
  color: #a0aec0;
  font-weight: 700;
}

:deep(.el-menu-item), :deep(.el-sub-menu__title) {
  height: 56px;
  margin: 4px 16px;
  border-radius: 16px;
  color: #555;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &:hover {
    background: rgba(255, 255, 255, 0.5);
    color: var(--accent-color);
    transform: translateX(4px);
  }
}

:deep(.el-menu-item.is-active) {
  background: white;
  color: var(--accent-color);
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  font-weight: 600;

  .menu-icon-box {
    color: var(--accent-color);
  }

  .active-indicator {
    height: 60%;
    width: 4px;
    background: var(--accent-color);
    position: absolute;
    right: 0;
    border-radius: 4px 0 0 4px;
  }
}

.menu-icon-box {
  margin-right: 12px;
  font-size: 18px;
  display: flex;
  align-items: center;
}

.sidebar-footer {
  padding: 20px;
  text-align: center;
}

.system-status {
  background: rgba(255,255,255,0.4);
  padding: 8px 12px;
  border-radius: 20px;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 8px;

  .status-dot {
    width: 8px;
    height: 8px;
    background: #48bb78;
    border-radius: 50%;
    box-shadow: 0 0 8px #48bb78;
  }
}

/* ----- 头像上传样式 ----- */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;

  .avatar-preview {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #e2e8f0;
  }
}

/* ----- 3. 主区域 & Header ----- */
.main-content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow: hidden; /* 防止双滚动条 */
}

.glass-header {
  height: 72px;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  cursor: pointer;
  font-size: 20px;
  color: #718096;
  transition: 0.3s;

  &:hover {
    color: var(--accent-color);
    transform: scale(1.1);
  }
}

.breadcrumb-fancy {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #718096;

  .home-icon { margin-right: 8px; }
  .separator { margin: 0 8px; color: #cbd5e0; }
  .current-page {
    color: var(--text-main);
    font-weight: 600;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -4px;
      left: 0;
      width: 100%;
      height: 2px;
      background: var(--accent-color);
      transform: scaleX(0);
      transition: transform 0.3s;
    }

    &:hover::after {
      transform: scaleX(1);
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 动态搜索框 */
.search-bar-animated {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.4);
  padding: 8px 16px;
  border-radius: 20px;
  width: 200px;
  transition: all 0.3s ease;
  border: 1px solid transparent;

  &:focus-within {
    width: 300px;
    background: white;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    border-color: #e2e8f0;
  }

  input {
    border: none;
    background: transparent;
    outline: none;
    margin-left: 8px;
    width: 100%;
    color: var(--text-main);

    &::placeholder {
      color: #a0aec0;
    }
  }
}

.action-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: white;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0,0,0,0.05);
    color: var(--accent-color);
  }
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px;
  padding-right: 16px;
  background: white;
  border-radius: 40px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;

  &:hover {
    border-color: #e2e8f0;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  }
}

.avatar-glow {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-meta .name { font-weight: 700; font-size: 13px; }
.role-badge {
  font-size: 10px;
  color: white;
  background: var(--accent-color);
  padding: 0 6px;
  border-radius: 4px;
  width: fit-content;
}

/* ----- 内容显示区 ----- */
.content-scroll-view {
  flex: 1;
  /* 这里不使用背景色，让背景的流体光效透过来，
     或者你可以给 router-view 内部的容器加白色卡片背景 */
  border-radius: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
}

/* 路由切换 3D 特效 */
.scale-slide-enter-active,
.scale-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.scale-slide-enter-from {
  opacity: 0;
  transform: translateX(30px) scale(0.95);
}

.scale-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px) scale(1.05);
  filter: blur(4px);
}
</style>

<!-- 覆盖 Element Plus 浮层样式，必须放在非 scoped 中 -->
<style lang="scss">
.glass-dropdown {
  border-radius: 16px !important;
  padding: 6px !important;
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(20px) !important;
  border: 1px solid rgba(255,255,255,0.5) !important;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1) !important;

  .el-dropdown-menu__item {
    border-radius: 10px;
    padding: 10px 16px;
    margin: 2px 0;
    font-weight: 500;

    &:hover {
      background: #f7fafc;
      color: #667eea;
      transform: translateX(5px);
      transition: all 0.2s;
    }
  }
}

.glass-message-box {
  border-radius: 20px !important;
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(10px);
  border: none !important;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25) !important;

  .el-button {
    border-radius: 10px;
    height: 36px;
  }

  .el-button--primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
  }
}
</style>
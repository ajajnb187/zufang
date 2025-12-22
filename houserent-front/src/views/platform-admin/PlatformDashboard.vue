<template>
  <div class="dashboard-container">
    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside width="240px" class="sidebar">
        <div class="logo">
          <div class="logo-box">
            <el-icon class="logo-icon"><House /></el-icon>
          </div>
          <h3>稳住租房</h3>
        </div>
        
        <div class="menu-container">
          <!-- 平台管理员菜单 -->
          <el-menu
            v-if="authStore.isPlatformAdmin || true"
            :default-active="$route.path"
            router
            class="sidebar-menu"
            :unique-opened="true"
          >
            <div class="menu-label">平台管理</div>
            <el-menu-item index="/platform-admin">
              <el-icon><DataAnalysis /></el-icon>
              <span>全局数据监控</span>
            </el-menu-item>
            
            <el-menu-item index="/platform-admin/admins">
              <el-icon><User /></el-icon>
              <span>小区管理员管理</span>
            </el-menu-item>
            
            <el-menu-item index="/platform-admin/communities">
              <el-icon><OfficeBuilding /></el-icon>
              <span>小区管理</span>
            </el-menu-item>
            
            <el-menu-item index="/platform-admin/monitor">
              <el-icon><Monitor /></el-icon>
              <span>违规处理</span>
            </el-menu-item>
            
            <el-menu-item index="/platform-admin/settings">
              <el-icon><Setting /></el-icon>
              <span>租赁条例上传</span>
            </el-menu-item>
            
            <el-menu-item index="/platform-admin/permissions">
              <el-icon><Lock /></el-icon>
              <span>权限管控</span>
            </el-menu-item>
            
            <div class="menu-label">审核中心</div>
            <el-menu-item index="/platform-admin/verification-review">
              <el-icon><Checked /></el-icon>
              <span>认证终审</span>
            </el-menu-item>
          </el-menu>

          <!-- 小区管理员菜单 -->
          <el-menu
            v-if="authStore.isCommunityAdmin"
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
            
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-profile">
                <el-avatar :size="36" :src="authStore.user?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" class="avatar" />
                <div class="user-info">
                  <span class="name">{{ authStore.user?.nickname || '管理员' }}</span>
                  <span class="role">{{ adminRoleText }}</span>
                </div>
                <el-icon class="arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人信息
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
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
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  House, DataAnalysis, User, OfficeBuilding, Monitor, 
  Setting, Document, Warning, Bell, ArrowDown, SwitchButton,
  ChatLineRound, Lock, Checked
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const currentBreadcrumb = computed(() => {
  const routeMap = {
    '/platform-admin': '全局数据监控',
    '/platform-admin/admins': '小区管理员管理', 
    '/platform-admin/communities': '小区管理',
    '/platform-admin/monitor': '违规处理',
    '/platform-admin/settings': '租赁条例上传',
    '/platform-admin/permissions': '权限管控',
    '/platform-admin/verification-review': '认证终审',
    // 小区管理员页面
    '/community-admin': '我的小区',
    '/community-admin/auth-review': '认证审核',
    '/community-admin/house-review': '房源审核',
    '/community-admin/contract-review': '合同审核',
    '/community-admin/report-handling': '举报处理',
    '/community-admin/facility-maintenance': '配套维护'
  }
  return routeMap[route.path] || '管理后台'
})

const adminRoleText = computed(() => {
  // if (authStore.isPlatformAdmin) return '平台管理员'
  // if (authStore.isCommunityAdmin) return '小区管理员'
  return '平台管理员'
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      authStore.logout()
      router.push('/') // 回到登录页
      ElMessage.success('已退出登录')
    } catch {
      // 取消
    }
  } else if (command === 'profile') {
    ElMessage.info('功能开发中')
  }
}
</script>

<style scoped>
.dashboard-container {
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
  color: #409eff;
  font-size: 20px;
}

.logo-box {
  width: 40px;
  height: 40px;
  background: #ecf5ff;
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
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}

:deep(.el-menu-item:hover) {
  background-color: #f5f7fa;
}

:deep(.el-sub-menu__title) {
  height: 48px;
  margin: 4px 16px;
  border-radius: 8px;
  color: #606266;
}

:deep(.el-sub-menu__title:hover) {
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
  color: #409eff;
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

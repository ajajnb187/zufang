<template>
  <div class="community-home">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ communityName }}管理中心</h2>
      <p>小区用户、房源、合同管理</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-icon user">
                <el-icon><User /></el-icon>
              </div>
              <div class="stats-content">
                <div class="stats-value">{{ communityStats.totalUsers }}</div>
                <div class="stats-label">小区用户数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-icon house">
                <el-icon><House /></el-icon>
              </div>
              <div class="stats-content">
                <div class="stats-value">{{ communityStats.totalHouses }}</div>
                <div class="stats-label">房源总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stats-content">
                <div class="stats-value">{{ communityStats.pendingAudit }}</div>
                <div class="stats-label">待审核项目</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-icon contract">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stats-content">
                <div class="stats-value">{{ communityStats.activeContracts }}</div>
                <div class="stats-label">有效合同</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 待办事项 -->
    <el-card class="pending-tasks">
      <template #header>
        <div class="card-header">
          <span>待办事项</span>
          <el-badge :value="communityStats.pendingAudit" class="badge" v-if="communityStats.pendingAudit > 0" />
        </div>
      </template>
      <div class="task-list">
        <div 
          v-for="task in pendingTasks" 
          :key="task.id"
          class="task-item"
        >
          <div class="task-content">
            <div class="task-title">{{ task.title }}</div>
            <div class="task-desc">{{ task.description }}</div>
            <div class="task-time">{{ task.time }}</div>
          </div>
          <el-button 
            v-if="task.path"
            type="primary" 
            size="small" 
            @click="goToTask(task.path)"
          >
            处理
          </el-button>
        </div>
        <el-empty v-if="pendingTasks.length === 0" description="暂无待办事项" />
      </div>
    </el-card>

    <!-- 快捷操作 -->
    <el-card class="quick-actions">
      <template #header>
        <span>快捷操作</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="action-item" @click="goToPage('/community-admin/auth-review')">
            <el-icon><UserFilled /></el-icon>
            <span>用户认证</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="action-item" @click="goToPage('/community-admin/house-review')">
            <el-icon><House /></el-icon>
            <span>房源预审</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="action-item" @click="goToPage('/community-admin/contract-review')">
            <el-icon><Document /></el-icon>
            <span>合同审核</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="action-item" @click="goToPage('/community-admin/facility-maintenance')">
            <el-icon><Tools /></el-icon>
            <span>配套维护</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

      </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  User, 
  House, 
  Clock, 
  Document, 
  UserFilled, 
  Tools 
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { getCommunityDashboard } from '@/api/community-admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const communityName = ref('')
const userInfo = computed(() => authStore.user)
const loading = ref(false)

const communityStats = ref({
  totalUsers: 0,
  totalHouses: 0,
  pendingAudit: 0,
  activeContracts: 0,
  pendingVerifications: 0,
  pendingHouses: 0
})

const pendingTasks = ref([])

const goToPage = (path) => {
  router.push(path)
}

const goToTask = (path) => {
  router.push(path)
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    const communityId = userInfo.value.communityId
    console.log('当前用户信息:', userInfo.value)
    console.log('communityId:', communityId)
    
    if (!communityId) {
      ElMessage.error('未获取到小区信息，请确保您的账号已关联小区')
      console.error('用户数据缺少communityId:', userInfo.value)
      return
    }
    
    const response = await getCommunityDashboard(communityId)
    if (response.code === 200) {
      const data = response.data
      communityStats.value = {
        totalUsers: data.totalUsers || 0,
        totalHouses: data.totalHouses || 0,
        pendingAudit: data.pendingAudit || 0,
        activeContracts: data.activeContracts || 0,
        pendingVerifications: data.pendingVerifications || 0,
        pendingHouses: data.pendingHouses || 0
      }
      // 设置小区名称（优先从 API 返回，其次从用户信息）
      communityName.value = data.communityName || userInfo.value.communityName || '小区管理'
      
      // 构建待办事项列表
      buildPendingTasks(data)
    } else {
      ElMessage.error(response.message || '获取仪表板数据失败')
    }
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

const buildPendingTasks = (data) => {
  const tasks = []
  
  if (data.pendingVerifications > 0) {
    tasks.push({
      id: 'verification',
      title: '用户认证审核',
      description: `有 ${data.pendingVerifications} 个用户认证申请等待审核`,
      time: '待处理',
      path: '/community-admin/auth-review'
    })
  }
  
  if (data.pendingHouses > 0) {
    tasks.push({
      id: 'house',
      title: '房源预审',
      description: `有 ${data.pendingHouses} 个房源等待预审`,
      time: '待处理',
      path: '/community-admin/house-review'
    })
  }
  
  if (tasks.length === 0) {
    tasks.push({
      id: 'none',
      title: '暂无待办事项',
      description: '当前没有需要处理的事项',
      time: '',
      path: ''
    })
  }
  
  pendingTasks.value = tasks
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style lang="scss" scoped>
.community-home {
  .page-header {
    margin-bottom: 24px;
    
    h2 {
      color: #1f2937;
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 600;
    }
    
    p {
      color: #6b7280;
      margin: 0;
      font-size: 14px;
    }
  }

  .stats-cards {
    margin-bottom: 24px;

    .stats-card {
      .stats-item {
        display: flex;
        align-items: center;
        padding: 8px 0;

        .stats-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          .el-icon {
            font-size: 24px;
            color: white;
          }

          &.user { background: #2f54eb; }
          &.house { background: #52c41a; }
          &.pending { background: #fa8c16; }
          &.contract { background: #eb2f96; }
        }

        .stats-content {
          flex: 1;

          .stats-value {
            font-size: 24px;
            font-weight: 600;
            color: #1f2937;
            line-height: 1;
            margin-bottom: 4px;
          }

          .stats-label {
            font-size: 14px;
            color: #6b7280;
          }
        }
      }
    }
  }

  .pending-tasks {
    margin-bottom: 24px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .task-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .task-content {
        flex: 1;

        .task-title {
          font-weight: 600;
          color: #1f2937;
          margin-bottom: 4px;
        }

        .task-desc {
          font-size: 14px;
          color: #6b7280;
          margin-bottom: 4px;
        }

        .task-time {
          font-size: 12px;
          color: #9ca3af;
        }
      }
    }
  }

  .quick-actions {
    margin-bottom: 24px;

    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #2f54eb;
        background: #f0f5ff;
        color: #2f54eb;
      }

      .el-icon {
        font-size: 32px;
        margin-bottom: 8px;
      }

      span {
        font-size: 14px;
        text-align: center;
      }
    }
  }

  .community-notices {
    :deep(.el-timeline-item__content) {
      font-size: 14px;
      color: #4b5563;
    }
  }
}
</style>

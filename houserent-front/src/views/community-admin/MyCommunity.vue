<template>
  <div class="my-community-container">
    <el-card class="header-card">
      <div class="community-header">
        <div class="community-info">
          <h2>{{ communityName }}</h2>
          <p class="community-desc">本小区数据概览</p>
        </div>
      </div>
    </el-card>

    <!-- 数据统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">小区用户数</div>
              <div class="stat-value">{{ stats.userCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon house-icon">
              <el-icon><House /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">房源数量</div>
              <div class="stat-value">{{ stats.houseCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon contract-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">租赁合同</div>
              <div class="stat-value">{{ stats.contractCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待审核事项</div>
              <div class="stat-value">{{ stats.pendingCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待审核认证</span>
              <el-button type="text" @click="$router.push('/dashboard/community-admin/user-auth')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-empty v-if="pendingAuth.length === 0" description="暂无待审核认证" />
          <div v-else class="todo-list">
            <div v-for="item in pendingAuth" :key="item.id" class="todo-item">
              <div class="todo-info">
                <span class="todo-title">{{ item.username }}</span>
                <span class="todo-time">{{ item.submitTime }}</span>
              </div>
              <el-tag type="warning" size="small">待审核</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待审核房源</span>
              <el-button type="text" @click="$router.push('/dashboard/community-admin/house-review')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-empty v-if="pendingHouses.length === 0" description="暂无待审核房源" />
          <div v-else class="todo-list">
            <div v-for="item in pendingHouses" :key="item.id" class="todo-item">
              <div class="todo-info">
                <span class="todo-title">{{ item.title }}</span>
                <span class="todo-time">{{ item.submitTime }}</span>
              </div>
              <el-tag type="warning" size="small">待审核</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动 -->
    <el-card class="activity-card">
      <template #header>
        <span>最近活动</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(activity, index) in recentActivities"
          :key="index"
          :timestamp="activity.time"
          placement="top"
        >
          {{ activity.content }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { 
  getCommunityDashboard,
  getCommunityPendingVerifications,
  getPendingAuditHouses 
} from '@/api/community-admin'

const authStore = useAuthStore()
const loading = ref(false)
const communityName = ref('')
const stats = ref({})
const pendingAuth = ref([])
const pendingHouses = ref([])
const recentActivities = ref([])

const userInfo = computed(() => authStore.user)
const communityId = computed(() => userInfo.value.communityId)

const loadCommunityData = async () => {
  if (!communityId.value) {
    ElMessage.error('无法获取小区信息')
    return
  }

  loading.value = true
  try {
    // 获取小区仪表板数据
    const dashboardRes = await getCommunityDashboard(communityId.value)
    if (dashboardRes.code === 200) {
      stats.value = dashboardRes.data
      communityName.value = dashboardRes.data.communityName || '我的小区'
    }

    // 获取待审核认证
    const authRes = await getCommunityPendingVerifications(communityId.value, { pageSize: 5 })
    if (authRes.code === 200) {
      pendingAuth.value = authRes.data.records || []
    }

    // 获取待审核房源
    const houseRes = await getPendingAuditHouses(communityId.value, { pageSize: 5 })
    if (houseRes.code === 200) {
      pendingHouses.value = houseRes.data.records || []
    }

  } catch (error) {
    console.error('加载小区数据失败:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCommunityData()
})
</script>

<style scoped>
.my-community-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.community-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.community-info h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.community-desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  margin-right: 16px;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.house-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.contract-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.pending-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.todo-card {
  margin-bottom: 20px;
  min-height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-list {
  max-height: 240px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-info {
  display: flex;
  flex-direction: column;
}

.todo-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.todo-time {
  font-size: 12px;
  color: #909399;
}

.activity-card {
  margin-top: 20px;
}
</style>

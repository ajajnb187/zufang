<template>
  <div class="global-dashboard-container">
    <div class="page-header">
      <h2>全局数据监控</h2>
      <p>平台整体运营数据概览</p>
    </div>

    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-icon communities">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-value">{{ platformStats.totalCommunities }}</div>
              <div class="stats-label">接入小区数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-value">{{ platformStats.totalUsers }}</div>
              <div class="stats-label">注册用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-icon houses">
              <el-icon><House /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-value">{{ platformStats.totalHouses }}</div>
              <div class="stats-label">房源总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-item">
            <div class="stats-icon contracts">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-value">{{ platformStats.totalContracts }}</div>
              <div class="stats-label">活跃合同数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 审核待处理数据 -->
    <el-row :gutter="20" class="pending-stats">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待处理审核</span>
              <el-button type="text" @click="refreshPendingStats">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="pending-list">
            <div class="pending-item">
              <span>用户认证审核</span>
              <el-tag type="warning">{{ pendingStats.userAuth }}</el-tag>
            </div>
            <div class="pending-item">
              <span>房源发布审核</span>
              <el-tag type="warning">{{ pendingStats.houseAudit }}</el-tag>
            </div>
            <div class="pending-item">
              <span>合同审核</span>
              <el-tag type="warning">{{ pendingStats.contractAudit }}</el-tag>
            </div>
            <div class="pending-item">
              <span>举报处理</span>
              <el-tag type="danger">{{ pendingStats.reports }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日数据</span>
            </div>
          </template>
          <div class="today-stats">
            <div class="today-item">
              <span>新增用户</span>
              <span class="value success">+{{ todayStats.newUsers }}</span>
            </div>
            <div class="today-item">
              <span>新发房源</span>
              <span class="value">+{{ todayStats.newHouses }}</span>
            </div>
            <div class="today-item">
              <span>签约合同</span>
              <span class="value success">+{{ todayStats.newContracts }}</span>
            </div>
            <div class="today-item">
              <span>违规处理</span>
              <span class="value">{{ todayStats.violationHandled }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 小区活跃度排行 -->
    <el-card class="community-ranking">
      <template #header>
        <div class="card-header">
          <span>小区活跃度排行</span>
          <el-button type="text" @click="loadCommunityRanking">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </template>
      <el-table :data="communityRanking" v-loading="loading" stripe>
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="communityName" label="小区名称" />
        <el-table-column prop="userCount" label="用户数" width="100" />
        <el-table-column prop="houseCount" label="房源数" width="100" />
        <el-table-column prop="contractCount" label="成交数" width="100" />
        <el-table-column prop="activityScore" label="活跃度" width="100">
          <template #default="{ row }">
            <el-progress
              :percentage="row.activityScore"
              :color="getActivityColor(row.activityScore)"
              :show-text="false"
              style="width: 80px"
            />
            <span class="score-text">{{ row.activityScore }}%</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 系统健康状态 -->
    <el-card class="system-health">
      <template #header>
        <div class="card-header">
          <span>系统健康状态</span>
          <el-button type="text" @click="checkSystemHealth">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="health-item">
            <el-icon class="health-icon" :class="systemHealth.database ? 'success' : 'error'">
              <Monitor />
            </el-icon>
            <div class="health-info">
              <div class="health-title">数据库</div>
              <div class="health-status" :class="systemHealth.database ? 'success' : 'error'">
                {{ systemHealth.database ? '正常' : '异常' }}
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="health-item">
            <el-icon class="health-icon" :class="systemHealth.redis ? 'success' : 'error'">
              <Monitor />
            </el-icon>
            <div class="health-info">
              <div class="health-title">缓存服务</div>
              <div class="health-status" :class="systemHealth.redis ? 'success' : 'error'">
                {{ systemHealth.redis ? '正常' : '异常' }}
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="health-item">
            <el-icon class="health-icon" :class="systemHealth.storage ? 'success' : 'error'">
              <Folder />
            </el-icon>
            <div class="health-info">
              <div class="health-title">文件存储</div>
              <div class="health-status" :class="systemHealth.storage ? 'success' : 'error'">
                {{ systemHealth.storage ? '正常' : '异常' }}
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  OfficeBuilding, User, House, Document, Refresh, Monitor, Folder
} from '@element-plus/icons-vue'
import { getPlatformStats, getPendingAuditStats, getTodayStats, getCommunityRanking, getSystemHealth } from '@/api/platform'

const loading = ref(false)

const platformStats = ref({
  totalCommunities: 0,
  totalUsers: 0,
  totalHouses: 0,
  totalContracts: 0
})

const pendingStats = ref({
  userAuth: 0,
  houseAudit: 0,
  contractAudit: 0,
  reports: 0
})

const todayStats = ref({
  newUsers: 0,
  newHouses: 0,
  newContracts: 0,
  violationHandled: 0
})

const communityRanking = ref([])

const systemHealth = ref({
  database: true,
  redis: true,
  storage: true
})

const loadPlatformStats = async () => {
  try {
    const response = await getPlatformStats()
    if (response.code === 200) {
      platformStats.value = response.data
    } else {
      ElMessage.error(response.message || '获取平台统计数据失败')
    }
  } catch (error) {
    console.error('加载平台统计数据失败:', error)
    ElMessage.error('加载数据失败，请重试')
  }
}

const loadPendingStats = async () => {
  try {
    const response = await getPendingAuditStats()
    if (response.code === 200) {
      pendingStats.value = response.data
    }
  } catch (error) {
    console.error('加载待处理统计失败:', error)
  }
}

const loadTodayStats = async () => {
  try {
    const response = await getTodayStats()
    if (response.code === 200) {
      todayStats.value = response.data
    }
  } catch (error) {
    console.error('加载今日统计失败:', error)
  }
}

const loadCommunityRanking = async () => {
  loading.value = true
  try {
    const response = await getCommunityRanking()
    if (response.code === 200) {
      communityRanking.value = response.data.map((item, index) => ({
        ...item,
        rank: index + 1
      }))
    }
  } catch (error) {
    console.error('加载小区排行失败:', error)
    ElMessage.error('加载小区排行数据失败')
  } finally {
    loading.value = false
  }
}

const checkSystemHealth = async () => {
  try {
    const response = await getSystemHealth()
    if (response.code === 200) {
      systemHealth.value = response.data
    }
  } catch (error) {
    console.error('检查系统健康状态失败:', error)
    ElMessage.error('系统健康检查失败')
  }
}

const refreshPendingStats = () => {
  loadPendingStats()
  ElMessage.success('数据已刷新')
}

const getActivityColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

onMounted(() => {
  loadPlatformStats()
  loadPendingStats()
  loadTodayStats()
  loadCommunityRanking()
  checkSystemHealth()
})
</script>

<style scoped>
.global-dashboard-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.stats-overview {
  margin-bottom: 20px;
}

.stats-card {
  transition: all 0.3s;
}

.stats-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stats-icon.communities {
  background: #e8f4fd;
  color: #409eff;
}

.stats-icon.users {
  background: #f0f9ff;
  color: #67c23a;
}

.stats-icon.houses {
  background: #fef0e6;
  color: #e6a23c;
}

.stats-icon.contracts {
  background: #fdf2f2;
  color: #f56c6c;
}

.stats-content {
  flex: 1;
}

.stats-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.pending-stats {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.pending-list, .today-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pending-item, .today-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f5;
}

.pending-item:last-child, .today-item:last-child {
  border-bottom: none;
}

.value {
  font-weight: 600;
  color: #303133;
}

.value.success {
  color: #67c23a;
}

.community-ranking {
  margin-bottom: 20px;
}

.score-text {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.system-health .health-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.health-icon {
  font-size: 24px;
}

.health-icon.success {
  color: #67c23a;
}

.health-icon.error {
  color: #f56c6c;
}

.health-info {
  flex: 1;
}

.health-title {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

.health-status {
  font-size: 12px;
  margin-top: 4px;
}

.health-status.success {
  color: #67c23a;
}

.health-status.error {
  color: #f56c6c;
}
</style>

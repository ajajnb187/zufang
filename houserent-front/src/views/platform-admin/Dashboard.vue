<template>
  <div class="platform-dashboard">
    <h2 class="page-title">首页仪表盘</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ dashboardData.totalUsers || 0 }}</div>
              <div class="stats-label">平台用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon house-icon">
              <el-icon><House /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ dashboardData.totalHouses || 0 }}</div>
              <div class="stats-label">房源总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon contract-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ dashboardData.totalContracts || 0 }}</div>
              <div class="stats-label">合同总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon pending-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ dashboardData.pendingVerifications || 0 }}</div>
              <div class="stats-label">待审核认证</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活跃度图表 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>用户活跃度趋势</span>
          </template>
          <div id="userActivityChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>房源发布趋势</span>
          </template>
          <div id="housePublishChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日数据 -->
    <el-card class="today-stats">
      <template #header>
        <span>今日数据</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="today-item">
            <div class="today-label">今日注册用户</div>
            <div class="today-value">{{ dashboardData.todayRegistrations || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="today-item">
            <div class="today-label">今日发布房源</div>
            <div class="today-value">{{ dashboardData.todayHousePublished || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="today-item">
            <div class="today-label">今日签订合同</div>
            <div class="today-value">{{ dashboardData.todayContractsSigned || 0 }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, House, Document, Clock } from '@element-plus/icons-vue'
import { getDashboardData } from '@/api/platform'
import * as echarts from 'echarts'

const dashboardData = ref({})

const loadDashboardData = async () => {
  try {
    const res = await getDashboardData()
    if (res.code === 200) {
      dashboardData.value = res.data
      initCharts()
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

const initCharts = () => {
  // 用户活跃度图表
  const userChart = echarts.init(document.getElementById('userActivityChart'))
  userChart.setOption({
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [120, 200, 150, 80, 70, 110, 130],
      type: 'line',
      smooth: true,
      itemStyle: { color: '#1890ff' }
    }]
  })

  // 房源发布图表
  const houseChart = echarts.init(document.getElementById('housePublishChart'))
  houseChart.setOption({
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [20, 32, 25, 18, 15, 22, 28],
      type: 'bar',
      itemStyle: { color: '#52c41a' }
    }]
  })
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style lang="scss" scoped>
.platform-dashboard {
  .page-title {
    margin-bottom: 24px;
    color: #1f2937;
    font-size: 24px;
    font-weight: 600;
  }

  .stats-row {
    margin-bottom: 24px;

    .stats-card {
      .stats-content {
        display: flex;
        align-items: center;

        .stats-icon {
          width: 56px;
          height: 56px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          .el-icon {
            font-size: 28px;
            color: white;
          }

          &.user-icon { background: #1890ff; }
          &.house-icon { background: #52c41a; }
          &.contract-icon { background: #fa8c16; }
          &.pending-icon { background: #eb2f96; }
        }

        .stats-info {
          .stats-value {
            font-size: 28px;
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

  .chart-card {
    margin-bottom: 24px;
  }

  .today-stats {
    .today-item {
      text-align: center;
      padding: 20px 0;

      .today-label {
        font-size: 14px;
        color: #6b7280;
        margin-bottom: 8px;
      }

      .today-value {
        font-size: 32px;
        font-weight: 600;
        color: #1890ff;
      }
    }
  }
}
</style>

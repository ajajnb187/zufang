<template>
  <div class="violation-handling-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.total || 0 }}</div>
            <div class="stat-label">总违规数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card warning">
          <div class="stat-content">
            <div class="stat-number">{{ stats.pending || 0 }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card success">
          <div class="stat-content">
            <div class="stat-number">{{ stats.today || 0 }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-button type="primary" @click="triggerAiScan" :loading="scanning">
            <el-icon><Refresh /></el-icon>
            AI扫描违规
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>违规处理</span>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="违规类型">
          <el-select v-model="searchForm.type" placeholder="请选择违规类型" clearable>
            <el-option label="垃圾信息" value="spam" />
            <el-option label="骚扰辱骂" value="harassment" />
            <el-option label="欺诈信息" value="fraud" />
            <el-option label="违法内容" value="illegal_content" />
            <el-option label="虚假信息" value="fake_info" />
            <el-option label="隐私泄露" value="privacy_leak" />
            <el-option label="恶意行为" value="malicious_action" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择处理状态" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="已处理" value="resolved" />
            <el-option label="已驳回" value="dismissed" />
            <el-option label="自动处理" value="auto_processed" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="searchForm.severity" placeholder="请选择严重程度" clearable>
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="严重" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="type" label="违规类型" width="110">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="违规内容" show-overflow-tooltip />
        <el-table-column prop="targetUser" label="违规用户" width="100" />
        <el-table-column prop="severity" label="严重程度" width="90">
          <template #default="{ row }">
            <el-tag :type="getSeverityColor(row.severity)" size="small">{{ getSeverityText(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actionTaken" label="处理措施" width="90">
          <template #default="{ row }">
            <span>{{ getActionText(row.actionTaken) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发现时间" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
            <template v-if="row.status === 'pending'">
              <el-button type="success" size="small" @click="openProcessDialog(row)">处理</el-button>
            </template>
            <template v-else-if="row.banUntil">
              <el-button type="warning" size="small" @click="handleUnban(row)">解封</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="违规详情" width="700px">
      <el-descriptions :column="2" border v-if="currentViolation">
        <el-descriptions-item label="违规ID">{{ currentViolation.id }}</el-descriptions-item>
        <el-descriptions-item label="违规用户">{{ currentViolation.userName || currentViolation.targetUser }}</el-descriptions-item>
        <el-descriptions-item label="用户手机">{{ currentViolation.userPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户状态">
          <el-tag :type="currentViolation.userStatus === 'active' ? 'success' : 'danger'">
            {{ currentViolation.userStatus === 'active' ? '正常' : currentViolation.userStatus === 'muted' ? '禁言' : '封禁' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="违规类型">
          <el-tag :type="getTypeColor(currentViolation.type)">{{ getTypeText(currentViolation.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="getSeverityColor(currentViolation.severity)">{{ getSeverityText(currentViolation.severity) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="违规内容" :span="2">{{ currentViolation.description || currentViolation.content }}</el-descriptions-item>
        <el-descriptions-item label="AI分析" :span="2">
          <div class="ai-analysis">{{ formatAiAnalysis(currentViolation.aiAnalysis) }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusColor(currentViolation.status)">{{ getStatusText(currentViolation.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理措施">{{ getActionText(currentViolation.actionTaken) }}</el-descriptions-item>
        <el-descriptions-item label="处理意见" :span="2">{{ currentViolation.handlerOpinion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发现时间">{{ currentViolation.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ currentViolation.handledAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="封禁截止" v-if="currentViolation.banUntil">{{ currentViolation.banUntil }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="showProcessDialog" title="处理违规" width="500px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理方式">
          <el-radio-group v-model="processForm.action">
            <el-radio label="warn">警告</el-radio>
            <el-radio label="mute">禁言</el-radio>
            <el-radio label="ban">封禁</el-radio>
            <el-radio label="dismiss">驳回(误判)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理时长" v-if="processForm.action === 'mute' || processForm.action === 'ban'">
          <el-select v-model="processForm.days" style="width: 200px">
            <el-option label="1天" :value="1" />
            <el-option label="3天" :value="3" />
            <el-option label="7天" :value="7" />
            <el-option label="15天" :value="15" />
            <el-option label="30天" :value="30" />
            <el-option label="永久" :value="9999" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理意见">
          <el-input v-model="processForm.opinion" type="textarea" :rows="3" placeholder="请输入处理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProcessDialog = false">取消</el-button>
        <el-button type="primary" @click="submitProcess" :loading="processing">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { 
  getViolationList, 
  getViolationDetail,
  getViolationStats,
  warnViolation,
  muteViolation,
  banViolation,
  dismissViolation,
  unbanViolation,
  triggerAiScan as triggerAiScanApi
} from '@/api/platform'

const loading = ref(false)
const scanning = ref(false)
const processing = ref(false)
const tableData = ref([])
const showDetailDialog = ref(false)
const showProcessDialog = ref(false)
const currentViolation = ref(null)

const stats = ref({
  total: 0,
  pending: 0,
  today: 0
})

const searchForm = ref({
  type: '',
  status: '',
  severity: ''
})

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

const processForm = reactive({
  action: 'warn',
  days: 1,
  opinion: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      type: searchForm.value.type,
      status: searchForm.value.status,
      severity: searchForm.value.severity,
      pageNum: pagination.value.current,
      pageSize: pagination.value.size
    }
    
    const response = await getViolationList(params)
    if (response.code === 200) {
      const data = response.data
      tableData.value = data.records || []
      pagination.value.total = data.total || 0
    } else {
      ElMessage.error(response.message || '获取违规数据失败')
    }
  } catch (error) {
    console.error('加载违规数据失败:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await getViolationStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const resetSearch = () => {
  searchForm.value = {
    type: '',
    status: '',
    severity: ''
  }
  loadData()
}

const triggerAiScan = async () => {
  try {
    await ElMessageBox.confirm('确定要触发AI扫描吗？将扫描最近24小时的聊天记录', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    scanning.value = true
    const response = await triggerAiScanApi({ hours: 24 })
    if (response.code === 200) {
      ElMessage.success(`扫描完成，发现${response.data.violationsFound}条违规内容`)
      loadData()
      loadStats()
    } else {
      ElMessage.error(response.message || 'AI扫描失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('AI扫描失败:', error)
      ElMessage.error('AI扫描失败，请重试')
    }
  } finally {
    scanning.value = false
  }
}

const handleView = async (row) => {
  try {
    const response = await getViolationDetail(row.id)
    if (response.code === 200) {
      currentViolation.value = response.data
      showDetailDialog.value = true
    } else {
      ElMessage.error(response.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const openProcessDialog = (row) => {
  currentViolation.value = row
  processForm.action = 'warn'
  processForm.days = 1
  processForm.opinion = ''
  showProcessDialog.value = true
}

const submitProcess = async () => {
  if (!currentViolation.value) return
  
  processing.value = true
  try {
    let response
    const violationId = currentViolation.value.id
    const payload = { opinion: processForm.opinion }
    
    switch (processForm.action) {
      case 'warn':
        response = await warnViolation(violationId, payload)
        break
      case 'mute':
        response = await muteViolation(violationId, { ...payload, days: processForm.days })
        break
      case 'ban':
        response = await banViolation(violationId, { ...payload, days: processForm.days })
        break
      case 'dismiss':
        response = await dismissViolation(violationId, payload)
        break
    }
    
    if (response && response.code === 200) {
      ElMessage.success(response.data || '处理成功')
      showProcessDialog.value = false
      loadData()
      loadStats()
    } else {
      ElMessage.error(response?.message || '处理失败')
    }
  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error('处理失败，请重试')
  } finally {
    processing.value = false
  }
}

const handleUnban = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解除用户"${row.targetUser}"的封禁/禁言吗？`, '确认解封', {
      confirmButtonText: '确定解封',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await unbanViolation(row.id, { opinion: '管理员手动解封' })
    if (response.code === 200) {
      ElMessage.success('解封成功')
      loadData()
    } else {
      ElMessage.error(response.message || '解封失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解封失败:', error)
      ElMessage.error('解封失败，请重试')
    }
  }
}

const formatAiAnalysis = (analysis) => {
  if (!analysis) return '-'
  try {
    const obj = JSON.parse(analysis)
    return `判断理由: ${obj.reason || '-'}\n处理建议: ${obj.suggestion || '-'}`
  } catch {
    return analysis
  }
}

const getTypeColor = (type) => {
  const colorMap = {
    spam: 'info',
    harassment: 'danger',
    fraud: 'danger',
    illegal_content: 'danger',
    fake_info: 'warning',
    privacy_leak: 'warning',
    malicious_action: 'danger',
    other: 'info'
  }
  return colorMap[type] || 'info'
}

const getTypeText = (type) => {
  const textMap = {
    spam: '垃圾信息',
    harassment: '骚扰辱骂',
    fraud: '欺诈信息',
    illegal_content: '违法内容',
    fake_info: '虚假信息',
    privacy_leak: '隐私泄露',
    malicious_action: '恶意行为',
    other: '其他'
  }
  return textMap[type] || '未知'
}

const getSeverityColor = (severity) => {
  const colorMap = {
    low: 'info',
    medium: 'warning',
    high: 'danger',
    critical: 'danger'
  }
  return colorMap[severity] || 'info'
}

const getSeverityText = (severity) => {
  const textMap = {
    low: '低',
    medium: '中',
    high: '高',
    critical: '严重'
  }
  return textMap[severity] || '未知'
}

const getStatusColor = (status) => {
  const colorMap = {
    pending: 'warning',
    resolved: 'success',
    dismissed: 'info',
    auto_processed: 'primary'
  }
  return colorMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待处理',
    resolved: '已处理',
    dismissed: '已驳回',
    auto_processed: '自动处理'
  }
  return textMap[status] || '未知'
}

const getActionText = (action) => {
  const textMap = {
    warning: '警告',
    mute: '禁言',
    ban: '封禁',
    none: '无',
    auto_ban: '自动封禁',
    auto_mute: '自动禁言'
  }
  return textMap[action] || '-'
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.violation-handling-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-card.warning {
  border-left: 4px solid #e6a23c;
}

.stat-card.success {
  border-left: 4px solid #67c23a;
}

.stat-content {
  padding: 10px 0;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.ai-analysis {
  white-space: pre-wrap;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}
</style>

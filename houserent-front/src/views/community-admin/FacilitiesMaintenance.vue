<template>
  <div class="facilities-maintenance-container">
    <!-- 顶部Tab切换：配套管理/反馈处理 -->
    <el-tabs v-model="mainTab" class="main-tabs">
      <el-tab-pane label="配套信息管理" name="facilities">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>小区配套信息更新</span>
              <el-button type="primary" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                添加配套设施
              </el-button>
            </div>
          </template>

          <!-- 配套分类筛选 -->
          <el-tabs v-model="activeCategory" @tab-click="handleCategoryChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="物业" name="物业" />
        <el-tab-pane label="快递" name="快递" />
        <el-tab-pane label="安保" name="安保" />
        <el-tab-pane label="停车" name="停车" />
        <el-tab-pane label="商超" name="商超" />
        <el-tab-pane label="公交" name="公交" />
        <el-tab-pane label="地铁" name="地铁" />
        <el-tab-pane label="学校" name="学校" />
        <el-tab-pane label="医院" name="医院" />
      </el-tabs>

      <!-- 配套列表 -->
      <el-row :gutter="20" v-loading="loading">
        <el-col
          v-for="facility in filteredFacilities"
          :key="facility.facilityId"
          :span="8"
          class="facility-col"
        >
          <el-card class="facility-card" shadow="hover">
            <div class="facility-icon">
              <el-icon :size="40" :color="getCategoryColor(facility.category)">
                <component :is="getFacilityIcon(facility.category)" />
              </el-icon>
            </div>
            <div class="facility-info">
              <h3>{{ facility.name }}</h3>
              <p class="facility-desc">{{ facility.description }}</p>
              <div class="facility-meta">
                <el-tag :type="getCategoryTagType(facility.category)" size="small">
                  {{ getCategoryText(facility.category) }}
                </el-tag>
                <span class="distance">{{ facility.distance || '未知距离' }}</span>
              </div>
            </div>
            <div class="facility-actions">
              <el-button type="primary" size="small" @click="handleEdit(facility)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(facility)">
                删除
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="filteredFacilities.length === 0" description="暂无配套设施" />
        </el-card>
      </el-tab-pane>

      <!-- 用户反馈处理 -->
      <el-tab-pane label="用户反馈处理" name="feedbacks">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>配套信息反馈</span>
              <el-radio-group v-model="feedbackStatus" size="small" @change="loadFeedbacks">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button label="pending">待处理</el-radio-button>
                <el-radio-button label="processed">已处理</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <el-table :data="feedbackList" v-loading="feedbackLoading" stripe>
            <el-table-column prop="feedbackId" label="ID" width="80" />
            <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="反馈时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handlerReply" label="处理回复" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button 
                  v-if="row.status === 'pending'" 
                  type="primary" 
                  size="small" 
                  @click="openHandleDialog(row)"
                >
                  处理
                </el-button>
                <el-button size="small" @click="viewFeedbackDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="feedbackTotal > 0"
            class="pagination"
            :current-page="feedbackPage"
            :page-size="feedbackPageSize"
            :total="feedbackTotal"
            layout="total, prev, pager, next"
            @current-change="handleFeedbackPageChange"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑/添加对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="facilityForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="设施名称" prop="name">
          <el-input v-model="facilityForm.name" placeholder="请输入设施名称" />
        </el-form-item>
        <el-form-item label="配套类型" prop="facilityType">
          <el-radio-group v-model="facilityForm.facilityType">
            <el-radio label="internal">小区内配套</el-radio>
            <el-radio label="surrounding">周边3公里配套</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设施分类" prop="category">
          <el-select v-model="facilityForm.category" placeholder="请选择分类">
            <el-option label="物业服务" value="物业" />
            <el-option label="快递服务" value="快递" />
            <el-option label="安保设施" value="安保" />
            <el-option label="停车设施" value="停车" />
            <el-option label="健身设施" value="健身" />
            <el-option label="商超购物" value="商超" />
            <el-option label="公交站点" value="公交" />
            <el-option label="地铁站点" value="地铁" />
            <el-option label="学校教育" value="学校" />
            <el-option label="医疗服务" value="医院" />
            <el-option label="银行金融" value="银行" />
            <el-option label="餐饮美食" value="餐饮" />
          </el-select>
        </el-form-item>
        <el-form-item label="距离(米)" prop="distance">
          <el-input-number
            v-model="facilityForm.distance"
            :min="0"
            :max="10000"
            :step="10"
            placeholder="请输入距离"
          />
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="facilityForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入详细描述"
          />
        </el-form-item>
        <el-form-item label="位置描述">
          <el-input v-model="facilityForm.location" placeholder="例如：小区东门入口处" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="facilityForm.openingHours" placeholder="例如：9:00-21:00" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="facilityForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 反馈处理对话框 -->
    <el-dialog v-model="handleDialogVisible" title="处理反馈" width="500px">
      <div class="feedback-content">
        <p><strong>反馈内容：</strong></p>
        <p class="content-text">{{ currentFeedback.content }}</p>
      </div>
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="处理方式">
          <el-radio-group v-model="handleForm.action">
            <el-radio label="processed">已处理</el-radio>
            <el-radio label="rejected">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="handleForm.reply"
            type="textarea"
            :rows="3"
            placeholder="请输入处理说明或回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedbackHandle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { 
  getCommunityFacilities,
  addFacility,
  updateFacility,
  deleteFacility,
  getFacilityFeedbacks,
  handleFacilityFeedback
} from '@/api/community-admin'
import { 
  OfficeBuilding, 
  ShoppingCart, 
  Bicycle,
  School, 
  Coffee 
} from '@element-plus/icons-vue'

const loading = ref(false)
const activeCategory = ref('all')
const facilities = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加配套设施')
const formRef = ref(null)
const authStore = useAuthStore()
const mainTab = ref('facilities')

// 反馈相关状态
const feedbackLoading = ref(false)
const feedbackList = ref([])
const feedbackStatus = ref('')
const feedbackPage = ref(1)
const feedbackPageSize = ref(10)
const feedbackTotal = ref(0)
const handleDialogVisible = ref(false)
const currentFeedback = ref({})
const handleForm = ref({
  action: 'processed',
  reply: ''
})

const facilityForm = ref({
  id: null,
  name: '',
  facilityType: 'internal',
  category: '',
  distance: 0,
  description: '',
  openingHours: '',
  phone: '',
  location: ''
})

const formRules = {
  name: [{ required: true, message: '请输入设施名称', trigger: 'blur' }],
  facilityType: [{ required: true, message: '请选择配套类型', trigger: 'change' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }]
}

const filteredFacilities = computed(() => {
  if (activeCategory.value === 'all') {
    return facilities.value
  }
  return facilities.value.filter(f => f.category === activeCategory.value)
})

const getCategoryText = (category) => {
  // 直接返回分类名称（中文）
  return category || '其他'
}

const getCategoryColor = (category) => {
  const map = {
    '物业': '#409eff',
    '快递': '#67c23a',
    '安保': '#f56c6c',
    '停车': '#e6a23c',
    '健身': '#9b59b6',
    '商超': '#67c23a',
    '公交': '#e6a23c',
    '地铁': '#3498db',
    '学校': '#f56c6c',
    '医院': '#e74c3c',
    '银行': '#1abc9c',
    '餐饮': '#ff9800'
  }
  return map[category] || '#909399'
}

const getCategoryTagType = (category) => {
  const map = {
    '物业': 'primary',
    '快递': 'success',
    '安保': 'danger',
    '停车': 'warning',
    '健身': '',
    '商超': 'success',
    '公交': 'warning',
    '地铁': 'primary',
    '学校': 'danger',
    '医院': 'danger',
    '银行': 'success',
    '餐饮': 'warning'
  }
  return map[category] || 'info'
}

const getFacilityIcon = (category) => {
  const map = {
    '物业': OfficeBuilding,
    '快递': ShoppingCart,
    '安保': OfficeBuilding,
    '停车': Bicycle,
    '健身': Coffee,
    '商超': ShoppingCart,
    '公交': Bicycle,
    '地铁': Bicycle,
    '学校': School,
    '医院': OfficeBuilding,
    '银行': OfficeBuilding,
    '餐饮': Coffee
  }
  return map[category] || OfficeBuilding
}

const loadData = async () => {
  loading.value = true
  try {
    const communityId = authStore.user.communityId
    if (!communityId) {
      ElMessage.error('未获取到小区信息')
      return
    }
    
    const response = await getCommunityFacilities(communityId, { pageSize: 100 })
    if (response.code === 200) {
      facilities.value = response.data.records || []
    } else {
      ElMessage.error(response.message || '获取配套设施失败')
    }
  } catch (error) {
    console.error('加载配套设施失败:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = () => {
  // 分类切换时的处理
}

const handleAdd = () => {
  dialogTitle.value = '添加配套设施'
  facilityForm.value = {
    id: null,
    name: '',
    facilityType: 'internal',
    category: '',
    distance: 0,
    description: '',
    openingHours: '',
    phone: '',
    location: ''
  }
  dialogVisible.value = true
}

const handleEdit = (facility) => {
  dialogTitle.value = '编辑配套设施'
  // 解析距离（去掉"米"单位）
  let distance = 0
  if (facility.distance) {
    const match = facility.distance.match(/\d+/)
    if (match) distance = parseInt(match[0])
  }
  // 解析联系信息
  let phone = ''
  let openingHours = ''
  if (facility.contactInfo) {
    const phoneMatch = facility.contactInfo.match(/电话:\s*([^|]+)/)
    const hoursMatch = facility.contactInfo.match(/营业时间:\s*(.+)/)
    if (phoneMatch) phone = phoneMatch[1].trim()
    if (hoursMatch) openingHours = hoursMatch[1].trim()
  }
  facilityForm.value = {
    id: facility.facilityId,
    name: facility.name,
    facilityType: facility.facilityType || 'internal',
    category: facility.category,
    distance: distance,
    description: facility.description,
    openingHours: openingHours,
    phone: phone,
    location: facility.location || ''
  }
  dialogVisible.value = true
}

const handleDelete = async (facility) => {
  try {
    await ElMessageBox.confirm('确定要删除该配套设施吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteFacility(facility.facilityId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除配套设施失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

const handleSubmit = async () => {
  const form = formRef.value
  if (!form) return

  await form.validate(async (valid) => {
    if (valid) {
      try {
        const communityId = authStore.user.communityId
        if (!communityId) {
          ElMessage.error('未获取到小区信息')
          return
        }
        
        let response
        const submitData = {
          name: facilityForm.value.name,
          facilityType: facilityForm.value.facilityType,
          category: facilityForm.value.category,
          distance: facilityForm.value.distance,
          description: facilityForm.value.description,
          openingHours: facilityForm.value.openingHours,
          phone: facilityForm.value.phone,
          location: facilityForm.value.location
        }
        
        if (facilityForm.value.id) {
          // 更新配套设施
          response = await updateFacility(facilityForm.value.id, submitData)
        } else {
          // 添加配套设施
          response = await addFacility(communityId, submitData)
        }
        
        if (response.code === 200) {
          ElMessage.success(facilityForm.value.id ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('提交配套设施失败:', error)
        ElMessage.error('操作失败，请重试')
      }
    }
  })
}

// 反馈相关方法
const loadFeedbacks = async () => {
  feedbackLoading.value = true
  try {
    const communityId = authStore.user.communityId
    if (!communityId) {
      ElMessage.error('未获取到小区信息')
      return
    }
    
    const response = await getFacilityFeedbacks(communityId, {
      status: feedbackStatus.value,
      pageNum: feedbackPage.value,
      pageSize: feedbackPageSize.value
    })
    
    if (response.code === 200) {
      feedbackList.value = response.data?.records || []
      feedbackTotal.value = response.data?.total || 0
    } else {
      ElMessage.error(response.message || '获取反馈列表失败')
    }
  } catch (error) {
    console.error('加载反馈列表失败:', error)
    ElMessage.error('加载反馈列表失败')
  } finally {
    feedbackLoading.value = false
  }
}

const handleFeedbackPageChange = (page) => {
  feedbackPage.value = page
  loadFeedbacks()
}

const openHandleDialog = (feedback) => {
  currentFeedback.value = feedback
  handleForm.value = { action: 'processed', reply: '' }
  handleDialogVisible.value = true
}

const viewFeedbackDetail = (feedback) => {
  ElMessageBox.alert(
    `<div>
      <p><strong>反馈内容：</strong>${feedback.content}</p>
      <p><strong>反馈时间：</strong>${formatDate(feedback.createdAt)}</p>
      <p><strong>处理状态：</strong>${getStatusText(feedback.status)}</p>
      ${feedback.handlerReply ? `<p><strong>处理回复：</strong>${feedback.handlerReply}</p>` : ''}
    </div>`,
    '反馈详情',
    { dangerouslyUseHTMLString: true }
  )
}

const submitFeedbackHandle = async () => {
  try {
    const response = await handleFacilityFeedback(
      currentFeedback.value.feedbackId,
      handleForm.value.action,
      handleForm.value.reply
    )
    
    if (response.code === 200) {
      ElMessage.success('处理成功')
      handleDialogVisible.value = false
      loadFeedbacks()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('处理反馈失败:', error)
    ElMessage.error('处理失败')
  }
}

const getStatusType = (status) => {
  const map = { pending: 'warning', processed: 'success', rejected: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { pending: '待处理', processed: '已处理', rejected: '已驳回' }
  return map[status] || '未知'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadData()
  loadFeedbacks()
})
</script>

<style scoped>
.facilities-maintenance-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.facility-col {
  margin-bottom: 20px;
}

.facility-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}

.facility-card:hover {
  transform: translateY(-4px);
}

.facility-icon {
  text-align: center;
  margin-bottom: 16px;
}

.facility-info {
  flex: 1;
}

.facility-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.facility-desc {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.facility-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.distance {
  font-size: 12px;
  color: #909399;
}

.facility-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.main-tabs {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.feedback-content {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.feedback-content .content-text {
  margin: 8px 0 0 0;
  color: #606266;
  line-height: 1.6;
}
</style>

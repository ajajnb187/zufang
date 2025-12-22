<template>
  <div class="verification-review-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>小区认证终审</span>
          <el-tag type="info">平台管理员</el-tag>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="小区">
          <el-select v-model="searchForm.communityId" placeholder="请选择小区" clearable filterable>
            <el-option 
              v-for="item in communityList" 
              :key="item.communityId" 
              :label="item.communityName" 
              :value="item.communityId" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="证明类型">
          <el-select v-model="searchForm.proofType" placeholder="请选择类型" clearable>
            <el-option label="租房合同" value="rental_contract" />
            <el-option label="物业缴费截图" value="property_fee" />
            <el-option label="房产证" value="ownership_cert" />
            <el-option label="其他证明" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 提示信息 -->
      <el-alert
        title="终审说明：只有小区管理员初审通过的申请才会出现在此列表，您需要进行最终审核"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="verification_id" label="申请ID" width="80" />
        <el-table-column label="申请用户" width="120">
          <template #default="{ row }">
            {{ row.user_nickname || row.user_id || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="用户电话" width="130">
          <template #default="{ row }">
            {{ row.user_phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="申请小区" width="150">
          <template #default="{ row }">
            {{ row.community_name || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="证明类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getProofTypeLabel(row.proof_type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请理由" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.apply_reason || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="小区管理员审核" width="150">
          <template #default="{ row }">
            <div>
              <el-tag type="success" size="small">已通过</el-tag>
              <div class="audit-time">{{ formatTime(row.community_admin_time) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="初审意见" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.community_admin_opinion || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button type="success" size="small" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="认证申请详情" width="700px">
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">{{ currentRow.verification_id }}</el-descriptions-item>
          <el-descriptions-item label="申请用户">{{ currentRow.user_nickname || currentRow.user_id }}</el-descriptions-item>
          <el-descriptions-item label="用户电话">{{ currentRow.user_phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请小区">{{ currentRow.community_name }}</el-descriptions-item>
          <el-descriptions-item label="证明类型">{{ getProofTypeLabel(currentRow.proof_type) }}</el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">{{ currentRow.apply_reason }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(currentRow.created_at) }}</el-descriptions-item>
          <el-descriptions-item label="初审时间">{{ formatTime(currentRow.community_admin_time) }}</el-descriptions-item>
          <el-descriptions-item label="初审意见" :span="2">{{ currentRow.community_admin_opinion || '无' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="proof-images">
          <h4>证明材料</h4>
          <el-row :gutter="20">
            <el-col :span="8" v-for="(img, index) in proofImageList" :key="index">
              <el-image 
                :src="img" 
                fit="cover" 
                :preview-src-list="proofImageList"
                :initial-index="index"
                class="proof-image"
              />
            </el-col>
          </el-row>
          <el-empty v-if="proofImageList.length === 0" description="暂无证明材料" />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="success" @click="handleApprove(currentRow)">通过</el-button>
        <el-button type="danger" @click="handleReject(currentRow)">驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPlatformPendingVerifications, platformAdminAudit } from '@/api/community'
import { getCommunityList } from '@/api/platform'

const loading = ref(false)
const searchForm = ref({
  communityId: '',
  proofType: ''
})

const tableData = ref([])
const communityList = ref([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const currentRow = ref(null)

const proofImageList = computed(() => {
  if (!currentRow.value?.proof_images) return []
  try {
    const images = typeof currentRow.value.proof_images === 'string' 
      ? JSON.parse(currentRow.value.proof_images) 
      : currentRow.value.proof_images
    return Array.isArray(images) ? images : []
  } catch {
    return []
  }
})

const getProofTypeLabel = (type) => {
  const map = {
    'rental_contract': '租房合同',
    'property_fee': '物业缴费截图',
    'ownership_cert': '房产证',
    'other': '其他证明'
  }
  return map[type] || type
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const loadCommunityList = async () => {
  try {
    const response = await getCommunityList({ pageNum: 1, pageSize: 200 })
    if (response.code === 200) {
      communityList.value = response.data.records || response.data || []
    }
  } catch (error) {
    console.error('加载小区列表失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      ...searchForm.value
    }
    
    const response = await getPlatformPendingVerifications(params)
    if (response.code === 200) {
      tableData.value = response.data.records || response.data || []
      pagination.value.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载待审核认证申请失败:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    communityId: '',
    proofType: ''
  }
  handleSearch()
}

const handleView = (row) => {
  currentRow.value = row
  detailDialogVisible.value = true
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认最终通过该用户的小区认证申请吗？通过后用户将获得该小区的房源查看和发布权限。',
      '终审确认',
      { type: 'warning' }
    )
    
    const response = await platformAdminAudit(row.verification_id, {
      status: 'approved',
      opinion: '平台终审通过'
    })
    
    if (response.code === 200) {
      ElMessage.success('终审通过，用户已获得小区权限')
      detailDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('终审通过失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const handleReject = async (row) => {
  try {
    const result = await ElMessageBox.prompt('请输入驳回原因', '终审驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入驳回原因'
    })
    
    const response = await platformAdminAudit(row.verification_id, {
      status: 'rejected',
      opinion: result.value
    })
    
    if (response.code === 200) {
      ElMessage.success('已驳回该认证申请')
      detailDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('终审驳回失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

onMounted(() => {
  loadCommunityList()
  loadData()
})
</script>

<style scoped>
.verification-review-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  padding: 10px 0;
}

.audit-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.proof-images {
  margin-top: 20px;
}

.proof-images h4 {
  margin-bottom: 16px;
  color: #303133;
}

.proof-image {
  width: 100%;
  height: 150px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
}
</style>

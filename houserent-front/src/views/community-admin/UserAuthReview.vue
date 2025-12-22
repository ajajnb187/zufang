<template>
  <div class="user-auth-review-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>本小区用户身份认证审核</span>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入用户昵称" clearable />
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

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="verification_id" label="申请ID" width="80" />
        <el-table-column label="用户昵称" width="120">
          <template #default="{ row }">
            {{ row.user_nickname || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="130">
          <template #default="{ row }">
            {{ row.user_phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="证明类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getProofTypeLabel(row.proof_type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apply_reason" label="申请理由" min-width="150" show-overflow-tooltip />
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.community_admin_status === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.community_admin_status === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="row.community_admin_status === 'rejected'" type="danger">已驳回</el-tag>
            <el-tag v-else type="warning">待审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleView(row)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="row.community_admin_status === 'pending'"
              type="success"
              size="small"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.community_admin_status === 'pending'"
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
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
    <el-dialog v-model="detailDialogVisible" title="认证详情" width="700px">
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">{{ currentRow.verification_id }}</el-descriptions-item>
          <el-descriptions-item label="用户昵称">{{ currentRow.user_nickname || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentRow.user_phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="证明类型">{{ getProofTypeLabel(currentRow.proof_type) }}</el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">{{ currentRow.apply_reason }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(currentRow.created_at) }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag v-if="currentRow.community_admin_status === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="currentRow.community_admin_status === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="currentRow.community_admin_status === 'rejected'" type="danger">已驳回</el-tag>
            <el-tag v-else type="warning">待审核</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRow.community_admin_opinion" label="审核意见">
            {{ currentRow.community_admin_opinion }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="proof-images">
          <h4>证明材料</h4>
          <el-row :gutter="20">
            <el-col :span="8" v-for="(img, index) in getProofImages(currentRow.proof_images)" :key="index">
              <el-image 
                :src="img" 
                fit="cover" 
                :preview-src-list="getProofImages(currentRow.proof_images)"
                :initial-index="index"
                class="proof-image"
              />
            </el-col>
          </el-row>
          <el-empty v-if="getProofImages(currentRow.proof_images).length === 0" description="暂无证明材料" />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="currentRow?.community_admin_status === 'pending'" type="success" @click="handleApprove(currentRow)">通过</el-button>
        <el-button v-if="currentRow?.community_admin_status === 'pending'" type="danger" @click="handleReject(currentRow)">驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { getCommunityPendingVerifications, communityAdminAudit } from '@/api/community'

const loading = ref(false)
const searchForm = ref({
  nickname: '',
  proofType: ''
})

const tableData = ref([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const currentRow = ref(null)

const authStore = useAuthStore()

const loadData = async () => {
  loading.value = true
  try {
    const communityId = authStore.user.communityId
    if (!communityId) {
      ElMessage.error('未获取到小区信息')
      return
    }
    
    const params = {
      pageNum: pagination.value.page,
      pageSize: pagination.value.pageSize,
      ...searchForm.value
    }
    
    const response = await getCommunityPendingVerifications(communityId, params)
    if (response.code === 200) {
      tableData.value = response.data.records || []
      pagination.value.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载认证申请失败:', error)
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
    nickname: '',
    proofType: ''
  }
  handleSearch()
}

const getProofTypeLabel = (type) => {
  const map = {
    'rental_contract': '租房合同',
    'property_fee': '物业缴费截图',
    'ownership_cert': '房产证',
    'other': '其他证明'
  }
  return map[type] || type || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getProofImages = (imagesStr) => {
  if (!imagesStr) return []
  try {
    const images = typeof imagesStr === 'string' ? JSON.parse(imagesStr) : imagesStr
    return Array.isArray(images) ? images : []
  } catch {
    return []
  }
}

const handleView = (row) => {
  currentRow.value = row
  detailDialogVisible.value = true
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该用户的认证申请吗？通过后将提交至平台管理员终审。', '确认操作', {
      type: 'warning'
    })
    
    const response = await communityAdminAudit(row.verification_id, {
      status: 'approved',
      opinion: '认证材料真实有效，初审通过'
    })
    
    if (response.code === 200) {
      ElMessage.success('初审通过，已提交平台终审')
      detailDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const handleReject = async (row) => {
  try {
    const result = await ElMessageBox.prompt('请输入驳回原因', '驳回认证', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入驳回原因'
    })
    
    const response = await communityAdminAudit(row.verification_id, {
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
      console.error('驳回失败:', error)
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
  loadData()
})
</script>

<style scoped>
.user-auth-review-container {
  padding: 20px;
}

.card-header {
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
  margin-bottom: 10px;
}
</style>

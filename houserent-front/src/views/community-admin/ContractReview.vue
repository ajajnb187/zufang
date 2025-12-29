<template>
  <div class="contract-review-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">合同审核</h2>
        <p class="page-subtitle">审核和管理本小区的租赁合同</p>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="合同编号">
          <el-input v-model="searchForm.contractNo" placeholder="请输入合同编号" clearable prefix-icon="Search" style="width: 180px" />
        </el-form-item>
        <el-form-item label="房东姓名">
          <el-input v-model="searchForm.landlord" placeholder="请输入房东姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="租客姓名">
          <el-input v-model="searchForm.tenant" placeholder="请输入租客姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="草稿" value="draft" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" icon="Search">查询</el-button>
          <el-button @click="handleReset" icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格卡片 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        stripe 
        class="modern-table"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: '600' }"
      >
        <el-table-column prop="contractNo" label="合同编号" width="150" align="center" />
        <el-table-column prop="houseTitle" label="房源" min-width="160" show-overflow-tooltip />
        <el-table-column prop="landlordName" label="房东" width="90" />
        <el-table-column prop="tenantName" label="租客" width="90" />
        <el-table-column label="月租金" width="100" align="center">
          <template #default="{ row }">
            <div class="rent-price">¥{{ row.rentPrice }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="起租日期" width="110" align="center" />
        <el-table-column prop="endDate" label="截止日期" width="110" align="center" />
        <el-table-column label="审核状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 'draft'" type="info" effect="light" round size="small">草稿</el-tag>
            <el-tag v-else-if="row.auditStatus === 'pending'" type="warning" effect="light" round size="small">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 'approved'" type="success" effect="light" round size="small">已通过</el-tag>
            <el-tag v-else-if="row.auditStatus === 'rejected'" type="danger" effect="light" round size="small">已驳回</el-tag>
            <el-tag v-else type="info" effect="light" round size="small">{{ row.auditStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="合同状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.contractStatus === 'draft'" type="info" effect="light" round size="small">草稿</el-tag>
            <el-tag v-else-if="row.contractStatus === 'signed'" type="primary" effect="light" round size="small">已签署</el-tag>
            <el-tag v-else-if="row.contractStatus === 'effective'" type="success" effect="light" round size="small">生效中</el-tag>
            <el-tag v-else-if="row.contractStatus === 'terminated'" type="danger" effect="light" round size="small">已终止</el-tag>
            <el-tag v-else-if="row.contractStatus === 'expired'" type="info" effect="light" round size="small">已过期</el-tag>
            <el-tag v-else type="info" effect="light" round size="small">{{ row.contractStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="解约" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.terminationStatus === 'pending'" type="warning" effect="light" round size="small">待审</el-tag>
            <el-tag v-else-if="row.terminationStatus === 'approved'" type="success" effect="light" round size="small">已通过</el-tag>
            <el-tag v-else-if="row.terminationStatus === 'rejected'" type="danger" effect="light" round size="small">已驳回</el-tag>
            <el-tag v-else type="info" effect="light" round size="small">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)" icon="View">查看</el-button>
            <el-button
              v-if="row.auditStatus === 'pending'"
              type="success"
              link
              @click="handleApprove(row)"
              icon="Check"
            >
              通过
            </el-button>
            <el-button
              v-if="row.auditStatus === 'pending'"
              type="danger"
              link
              @click="handleReject(row)"
              icon="Close"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="合同详情" width="800px">
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="合同编号" :span="2">{{ currentRow.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="房源标题" :span="2">{{ currentRow.houseTitle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房源类型">{{ currentRow.houseType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房源面积">{{ currentRow.houseArea ? currentRow.houseArea + '㎡' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="房东">{{ currentRow.landlordName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房东电话">{{ currentRow.landlordPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="租客">{{ currentRow.tenantName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="租客电话">{{ currentRow.tenantPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="月租金">
            <span class="rent-price">¥{{ currentRow.rentPrice || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="押金">
            <span class="rent-price">¥{{ currentRow.deposit || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="起租日期">{{ currentRow.startDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ currentRow.endDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="付款方式">{{ getPaymentMethodText(currentRow.paymentMethod) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentRow.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag v-if="currentRow.auditStatus === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="currentRow.auditStatus === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="currentRow.auditStatus === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="currentRow.auditStatus === 'rejected'" type="danger">已驳回</el-tag>
            <el-tag v-else type="info">{{ currentRow.auditStatus }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="合同状态">
            <el-tag v-if="currentRow.contractStatus === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="currentRow.contractStatus === 'signed'" type="primary">已签署</el-tag>
            <el-tag v-else-if="currentRow.contractStatus === 'effective'" type="success">生效中</el-tag>
            <el-tag v-else-if="currentRow.contractStatus === 'terminated'" type="danger">已终止</el-tag>
            <el-tag v-else-if="currentRow.contractStatus === 'expired'" type="info">已过期</el-tag>
            <el-tag v-else type="info">{{ currentRow.contractStatus }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="房东签名时间">{{ formatDateTime(currentRow.landlordSignTime) || '未签名' }}</el-descriptions-item>
          <el-descriptions-item label="租客签名时间">{{ formatDateTime(currentRow.tenantSignTime) || '未签名' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRow.auditOpinion" label="审核意见" :span="2">
            {{ currentRow.auditOpinion }}
          </el-descriptions-item>
          <el-descriptions-item label="补充条款" :span="2">
            {{ currentRow.customContent || '无' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 签名预览 -->
        <div v-if="currentRow.landlordSignature || currentRow.tenantSignature" class="signature-section">
          <h4>电子签名</h4>
          <div class="signature-container">
            <div v-if="currentRow.landlordSignature" class="signature-item">
              <span class="label">房东签名：</span>
              <img :src="currentRow.landlordSignature" alt="房东签名" class="signature-img" />
            </div>
            <div v-if="currentRow.tenantSignature" class="signature-item">
              <span class="label">租客签名：</span>
              <img :src="currentRow.tenantSignature" alt="租客签名" class="signature-img" />
            </div>
          </div>
        </div>
        
        <div v-if="currentRow.pdfUrl" class="contract-file">
          <h4>合同文件</h4>
          <el-button type="primary" @click="downloadContract(currentRow.pdfUrl)">
            <el-icon><Download /></el-icon>
            下载合同PDF
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentRow?.auditStatus === 'pending'"
          type="success"
          @click="handleApprove(currentRow)"
        >
          通过审核
        </el-button>
        <el-button
          v-if="currentRow?.auditStatus === 'pending'"
          type="danger"
          @click="handleReject(currentRow)"
        >
          驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { getCommunityContracts, auditContract, terminateContract, auditTermination } from '@/api/community-admin'
import { Download } from '@element-plus/icons-vue'

const loading = ref(false)
const searchForm = ref({
  contractNo: '',
  landlord: '',
  tenant: '',
  status: ''
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

// 付款方式文本转换
const getPaymentMethodText = (method) => {
  const map = {
    'monthly': '月付',
    'quarterly': '季付',
    'semi_annual': '半年付',
    'annual': '年付',
    '押一付一': '押一付一',
    '押一付三': '押一付三',
    '押二付一': '押二付一'
  }
  return map[method] || method || '-'
}

// 日期时间格式化
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateStr
  }
}

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
      status: searchForm.value.status,
      contractNo: searchForm.value.contractNo,
      landlord: searchForm.value.landlord,
      tenant: searchForm.value.tenant
    }
    
    const response = await getCommunityContracts(communityId, params)
    if (response.code === 200) {
      tableData.value = response.data.records || []
      pagination.value.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载合同列表失败:', error)
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
    contractNo: '',
    landlord: '',
    tenant: '',
    status: ''
  }
  handleSearch()
}

const handleView = (row) => {
  currentRow.value = row
  detailDialogVisible.value = true
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该合同的审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    const response = await auditContract(row.contractId, true, '合同条款合规，双方信息完整，审核通过')
    
    if (response.code === 200) {
      ElMessage.success('审核通过')
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
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回合同', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入驳回原因'
    })
    
    const response = await auditContract(row.contractId, false, value)
    
    if (response.code === 200) {
      ElMessage.success('已驳回')
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

const handleApproveTermination = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认同意该合同的解约申请吗？\n解约原因：${row.terminationReason || '未填写'}`, 
      '审核解约申请', 
      {
        confirmButtonText: '同意解约',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await auditTermination(row.contractId, true, '同意解约')
    
    if (response.code === 200) {
      ElMessage.success('已同意解约')
      loadData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核解约失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const handleRejectTermination = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝解约的原因', '拒绝解约申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入拒绝原因'
    })
    
    const response = await auditTermination(row.contractId, false, value)
    
    if (response.code === 200) {
      ElMessage.success('已拒绝解约申请')
      loadData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核解约失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const downloadContract = async (url) => {
  if (!url) {
    ElMessage.warning('合同文件不存在')
    return
  }
  try {
    // 使用fetch下载文件
    const response = await fetch(url)
    const blob = await response.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = 'contract.pdf'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  } catch (error) {
    console.error('下载失败:', error)
    // 降级方案：直接打开
    window.open(url, '_blank')
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
.contract-review-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

/* 搜索卡片 */
.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: none;
}

.search-card :deep(.el-card__body) {
  padding: 20px;
}

.search-form {
  margin: 0;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}

/* 表格卡片 */
.table-card {
  border-radius: 12px;
  border: none;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

/* 现代化表格 */
.modern-table {
  border-radius: 12px;
  overflow: hidden;
}

.modern-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.modern-table :deep(.el-table__row:hover) {
  background-color: #f8f9fa !important;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.modern-table :deep(.el-table td) {
  border-bottom: 1px solid #f0f2f5;
}

.rent-price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
}

/* 分页 */
.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  border-radius: 0 0 12px 12px;
}

.detail-content {
  padding: 10px 0;
}

.contract-file {
  margin-top: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.contract-file h4 {
  margin-bottom: 12px;
  color: #303133;
}

.signature-section {
  margin-top: 20px;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 4px;
}

.signature-section h4 {
  margin-bottom: 12px;
  color: #303133;
}

.signature-container {
  display: flex;
  gap: 40px;
}

.signature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.signature-item .label {
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.signature-img {
  max-width: 200px;
  max-height: 100px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
}
</style>

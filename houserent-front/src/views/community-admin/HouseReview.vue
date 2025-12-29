<template>
  <div class="house-review-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">房源审核</h2>
        <p class="page-subtitle">审核本小区房东发布的房源信息</p>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="房源标题">
          <el-input v-model="searchForm.title" placeholder="请输入房源标题" clearable prefix-icon="Search" style="width: 200px" />
        </el-form-item>
        <el-form-item label="房东姓名">
          <el-input v-model="searchForm.landlord" placeholder="请输入房东姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
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
        <el-table-column prop="title" label="房源标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="house-title">
              <el-icon class="house-icon"><House /></el-icon>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="landlord" label="房东" width="100" />
        <el-table-column prop="houseType" label="户型" width="100" align="center" />
        <el-table-column prop="area" label="面积" width="90" align="center">
          <template #default="{ row }">
            <span>{{ row.area }}㎡</span>
          </template>
        </el-table-column>
        <el-table-column prop="rentPrice" label="租金" width="110" align="center">
          <template #default="{ row }">
            <div class="rent-price">￥{{ row.rentPrice }}/月</div>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="提交时间" width="160">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon class="time-icon"><Clock /></el-icon>
              <span>{{ row.updatedAt }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 'pending'" type="warning" effect="light" round>待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 'approved'" type="success" effect="light" round>已通过</el-tag>
            <el-tag v-else-if="row.auditStatus === 'rejected'" type="danger" effect="light" round>已驳回</el-tag>
            <el-tag v-else type="info" effect="light" round>草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
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
    <el-dialog v-model="detailDialogVisible" title="房源详情" width="800px">
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="房源标题" :span="2">{{ currentRow.title }}</el-descriptions-item>
          <el-descriptions-item label="房东">{{ currentRow.landlord }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRow.phone }}</el-descriptions-item>
          <el-descriptions-item label="户型">{{ currentRow.houseType }}</el-descriptions-item>
          <el-descriptions-item label="面积">{{ currentRow.area }}㎡</el-descriptions-item>
          <el-descriptions-item label="租金">¥{{ currentRow.rentPrice }}/月</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ currentRow.floor }}</el-descriptions-item>
          <el-descriptions-item label="朝向">{{ currentRow.orientation }}</el-descriptions-item>
          <el-descriptions-item label="装修">{{ currentRow.decoration }}</el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ currentRow.updatedAt }}</el-descriptions-item>
          <el-descriptions-item label="审核状态" :span="2">
            <el-tag v-if="currentRow.auditStatus === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="currentRow.auditStatus === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="currentRow.auditStatus === 'rejected'" type="danger">已驳回</el-tag>
            <el-tag v-else type="info">草稿</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRow.auditOpinion && currentRow.auditStatus === 'rejected'" label="驳回原因" :span="2">
            {{ currentRow.auditOpinion }}
          </el-descriptions-item>
          <el-descriptions-item label="房源描述" :span="2">
            {{ currentRow.description }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="house-images">
          <h4>房源图片</h4>
          <el-row :gutter="10" v-if="currentRow.images && currentRow.images.length > 0">
            <el-col v-for="(img, index) in currentRow.images" :key="index" :span="6">
              <el-image :src="img" fit="cover" class="house-image" :preview-src-list="currentRow.images" />
            </el-col>
          </el-row>
          <el-empty v-else description="暂无房源图片" :image-size="80" />
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
import { House, Clock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { getPendingAuditHouses, auditHouse } from '@/api/community-admin'

const loading = ref(false)
const searchForm = ref({
  title: '',
  landlord: '',
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
      communityId: communityId,
      ...searchForm.value
    }
    
    const response = await getPendingAuditHouses(communityId, params)
    if (response.code === 200) {
      tableData.value = response.data.records || []
      pagination.value.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载房源列表失败:', error)
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
    title: '',
    landlord: '',
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
    await ElMessageBox.confirm('确认通过该房源的发布审核吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    const response = await auditHouse(row.houseId, true, '房源信息完整，符合发布要求，审核通过')
    
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
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回房源', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入驳回原因'
    })
    
    const response = await auditHouse(row.houseId, false, value)
    
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
.house-review-container {
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

/* 房源标题 */
.house-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.house-icon {
  color: #e6a23c;
  font-size: 18px;
}

/* 租金 */
.rent-price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
}

/* 时间单元格 */
.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.time-icon {
  color: #909399;
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

/* 详情对话框 */
.detail-content {
  padding: 10px 0;
}

.house-images {
  margin-top: 20px;
}

.house-images h4 {
  margin-bottom: 16px;
  color: #303133;
  font-weight: 600;
}

.house-image {
  width: 100%;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.house-image:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
</style>

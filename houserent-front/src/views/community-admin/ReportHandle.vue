<template>
  <div class="report-handle-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>虚假房源举报处理</span>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="房源标题">
          <el-input v-model="searchForm.title" placeholder="请输入房源标题" clearable />
        </el-form-item>
        <el-form-item label="举报人">
          <el-input v-model="searchForm.reporter" placeholder="请输入举报人" clearable />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="已处理" value="handled" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已撤回" value="withdrawn" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="reportId" label="举报编号" width="100" />
        <el-table-column prop="houseTitle" label="房源标题" width="200" show-overflow-tooltip />
        <el-table-column prop="landlord" label="房东" width="100" />
        <el-table-column prop="reporter" label="举报人" width="100" />
        <el-table-column prop="reportType" label="举报类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getReportTypeColor(row.reportType)">
              {{ getReportTypeText(row.reportType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportTime" label="举报时间" width="160" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'pending'" type="warning">待处理</el-tag>
            <el-tag v-else-if="row.status === 'handled'" type="success">已处理</el-tag>
            <el-tag v-else-if="row.status === 'rejected'" type="info">已驳回</el-tag>
            <el-tag v-else-if="row.status === 'withdrawn'" type="info">已撤回</el-tag>
            <el-tag v-else type="info">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="success"
              size="small"
              @click="handleProcess(row)"
            >
              处理
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
    <el-dialog v-model="detailDialogVisible" title="举报详情" width="700px">
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="举报编号">{{ currentRow.reportId }}</el-descriptions-item>
          <el-descriptions-item label="举报时间">{{ currentRow.reportTime }}</el-descriptions-item>
          <el-descriptions-item label="房源标题" :span="2">{{ currentRow.houseTitle }}</el-descriptions-item>
          <el-descriptions-item label="房东">{{ currentRow.landlord }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRow.landlordPhone }}</el-descriptions-item>
          <el-descriptions-item label="举报人">{{ currentRow.reporter }}</el-descriptions-item>
          <el-descriptions-item label="举报人电话">{{ currentRow.reporterPhone }}</el-descriptions-item>
          <el-descriptions-item label="举报类型">
            <el-tag :type="getReportTypeColor(currentRow.reportType)">
              {{ getReportTypeText(currentRow.reportType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag v-if="currentRow.status === 'pending'" type="warning">待处理</el-tag>
            <el-tag v-else-if="currentRow.status === 'handled'" type="success">已处理</el-tag>
            <el-tag v-else-if="currentRow.status === 'rejected'" type="info">已驳回</el-tag>
            <el-tag v-else-if="currentRow.status === 'withdrawn'" type="info">已撤回</el-tag>
            <el-tag v-else type="info">{{ currentRow.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="举报描述" :span="2">
            {{ currentRow.description }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRow.evidence" label="举报证据" :span="2">
            <el-image
              v-for="(img, index) in currentRow.evidence"
              :key="index"
              :src="img"
              fit="cover"
              class="evidence-image"
              :preview-src-list="currentRow.evidence"
            />
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRow.handleResult" label="处理结果" :span="2">
            {{ currentRow.handleResult }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentRow?.status === 'pending'"
          type="success"
          @click="handleProcess(currentRow)"
        >
          处理举报
        </el-button>
      </template>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理举报" width="600px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="processForm.result">
            <el-radio label="valid">举报属实，下架房源</el-radio>
            <el-radio label="invalid">举报不实，驳回举报</el-radio>
            <el-radio label="warning">举报属实，警告房东</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input
            v-model="processForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { getCommunityReports, handleReport } from '@/api/community-admin'

const loading = ref(false)
const authStore = useAuthStore()
const searchForm = ref({
  title: '',
  reporter: '',
  status: ''
})

const tableData = ref([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentRow = ref(null)
const processForm = ref({
  result: 'valid',
  description: ''
})

const getReportTypeText = (type) => {
  const map = {
    '虚假房源': '虚假房源',
    '重复发布': '重复发布',
    '已出租': '已出租',
    '信息有误': '信息有误',
    '其他': '其他',
    fake: '虚假房源',
    photo: '图片不符',
    price: '价格欺诈',
    info: '信息虚假',
    other: '其他'
  }
  return map[type] || type || '未知'
}

const getReportTypeColor = (type) => {
  const map = {
    '虚假房源': 'danger',
    '重复发布': 'warning',
    '已出租': 'info',
    '信息有误': 'warning',
    '其他': 'info',
    fake: 'danger',
    photo: 'warning',
    price: 'danger',
    info: 'warning',
    other: 'info'
  }
  return map[type] || 'info'
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
      ...searchForm.value
    }
    
    const response = await getCommunityReports(communityId, params)
    if (response.code === 200) {
      tableData.value = response.data.records || []
      pagination.value.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取举报数据失败')
    }
  } catch (error) {
    console.error('加载举报列表失败:', error)
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
    reporter: '',
    status: ''
  }
  handleSearch()
}

const handleView = (row) => {
  currentRow.value = row
  detailDialogVisible.value = true
}

const handleProcess = (row) => {
  currentRow.value = row
  processForm.value = {
    result: 'valid',
    description: ''
  }
  detailDialogVisible.value = false
  processDialogVisible.value = true
}

const submitProcess = async () => {
  if (!processForm.value.description) {
    ElMessage.warning('请输入处理说明')
    return
  }
  
  try {
    const response = await handleReport(currentRow.value.reportId, {
      action: processForm.value.result,
      reason: processForm.value.description
    })
    
    if (response.code === 200) {
      ElMessage.success('处理成功')
      processDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('处理举报失败:', error)
    ElMessage.error('处理失败，请重试')
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
.report-handle-container {
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

.evidence-image {
  width: 120px;
  height: 80px;
  border-radius: 4px;
  margin-right: 10px;
  cursor: pointer;
}
</style>

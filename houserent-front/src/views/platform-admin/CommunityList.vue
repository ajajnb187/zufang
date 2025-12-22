<template>
  <div class="community-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>小区管理</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            新增小区
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="小区名称">
          <el-input v-model="searchForm.name" placeholder="请输入小区名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="communityId" label="小区ID" width="80" />
        <el-table-column prop="communityName" label="小区名称" width="200" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="propertyCompany" label="物业公司" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCommunityList } from '@/api/platform'

const loading = ref(false)
const showAddDialog = ref(false)
const tableData = ref([])

const searchForm = ref({
  name: '',
  status: ''
})

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const response = await getCommunityList({
      name: searchForm.value.name,
      status: searchForm.value.status,
      pageNum: pagination.value.current,
      pageSize: pagination.value.size
    })
    
    if (response.code === 200) {
      const data = response.data
      tableData.value = data.records || []
      pagination.value.total = data.total || 0
    } else {
      ElMessage.error(response.message || '获取小区列表失败')
    }
  } catch (error) {
    console.error('加载小区列表失败:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = {
    name: '',
    status: ''
  }
  loadData()
}

const handleEdit = (row) => {
  ElMessage.info(`编辑小区：${row.communityName}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除小区"${row.communityName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 取消删除
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.community-list-container {
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

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>

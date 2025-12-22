<template>
  <div class="community-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>小区管理</span>
          <el-button type="primary" @click="handleAdd">
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

    <!-- 新增/编辑小区对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingCommunity ? '编辑小区' : '新增小区'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="小区名称" prop="communityName">
          <el-input v-model="formData.communityName" placeholder="请输入小区名称" />
        </el-form-item>
        <el-form-item label="所在城市" prop="city">
          <el-input v-model="formData.city" placeholder="请输入所在城市" />
        </el-form-item>
        <el-form-item label="所在区县" prop="district">
          <el-input v-model="formData.district" placeholder="请输入所在区县" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="物业公司" prop="propertyCompany">
          <el-input v-model="formData.propertyCompany" placeholder="请输入物业公司名称" />
        </el-form-item>
        <el-form-item label="物业电话" prop="propertyPhone">
          <el-input v-model="formData.propertyPhone" placeholder="请输入物业联系电话" />
        </el-form-item>
        <el-form-item label="小区简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入小区简介"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ editingCommunity ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCommunityList, addCommunity, updateCommunity, deleteCommunity } from '@/api/platform'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingCommunity = ref(null)
const formRef = ref(null)
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

const formData = reactive({
  communityName: '',
  city: '',
  district: '',
  address: '',
  propertyCompany: '',
  propertyPhone: '',
  description: '',
  status: 'active'
})

const formRules = {
  communityName: [
    { required: true, message: '请输入小区名称', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入所在城市', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

const resetForm = () => {
  Object.assign(formData, {
    communityName: '',
    city: '',
    district: '',
    address: '',
    propertyCompany: '',
    propertyPhone: '',
    description: '',
    status: 'active'
  })
  editingCommunity.value = null
}

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

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingCommunity.value = row
  Object.assign(formData, {
    communityName: row.communityName || '',
    city: row.city || '',
    district: row.district || '',
    address: row.address || '',
    propertyCompany: row.propertyCompany || '',
    propertyPhone: row.propertyPhone || '',
    description: row.description || '',
    status: row.status || 'active'
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    if (editingCommunity.value) {
      // 更新小区
      const response = await updateCommunity(editingCommunity.value.communityId, formData)
      if (response.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(response.message || '更新失败')
      }
    } else {
      // 创建小区
      const response = await addCommunity(formData)
      if (response.code === 200) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(response.message || '创建失败')
      }
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除小区"${row.communityName}"吗？删除后相关数据也将被清除。`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    const response = await deleteCommunity(row.communityId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  } finally {
    loading.value = false
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

<template>
  <div class="permission-control-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管控</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            新增权限配置
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="小区">
          <el-select v-model="searchForm.communityId" placeholder="请选择小区" clearable>
            <el-option
              v-for="community in communityOptions"
              :key="community.id"
              :label="community.name"
              :value="community.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="管理员">
          <el-input v-model="searchForm.adminName" placeholder="请输入管理员姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 权限配置表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="adminName" label="管理员" width="120" />
        <el-table-column prop="communityName" label="所属小区" width="150" />
        <el-table-column prop="maxHouseAudit" label="房源审核上限" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.maxHouseAudit }}/日</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxUserAudit" label="用户审核上限" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.maxUserAudit }}/日</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxContractAudit" label="合同审核上限" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.maxContractAudit }}/日</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="canManageFacilities" label="配套管理权限" width="120">
          <template #default="{ row }">
            <el-tag :type="row.canManageFacilities ? 'success' : 'danger'">
              {{ row.canManageFacilities ? '允许' : '禁止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="canHandleReports" label="举报处理权限" width="120">
          <template #default="{ row }">
            <el-tag :type="row.canHandleReports ? 'success' : 'danger'">
              {{ row.canHandleReports ? '允许' : '禁止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'warning'">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              :type="row.status === 'active' ? 'warning' : 'success'" 
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
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

    <!-- 添加/编辑权限配置对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingItem ? '编辑权限配置' : '新增权限配置'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="小区管理员" prop="adminId">
          <el-select 
            v-model="formData.adminId" 
            placeholder="请选择小区管理员" 
            style="width: 100%"
            @change="handleAdminChange"
          >
            <el-option
              v-for="admin in adminOptions"
              :key="admin.id"
              :label="`${admin.name} - ${admin.communityName}`"
              :value="admin.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="房源审核上限" prop="maxHouseAudit">
          <el-input-number
            v-model="formData.maxHouseAudit"
            :min="1"
            :max="999"
            placeholder="每日最大审核数量"
            style="width: 200px"
          />
          <span class="unit-text">个/日</span>
        </el-form-item>

        <el-form-item label="用户审核上限" prop="maxUserAudit">
          <el-input-number
            v-model="formData.maxUserAudit"
            :min="1"
            :max="999"
            placeholder="每日最大审核数量"
            style="width: 200px"
          />
          <span class="unit-text">个/日</span>
        </el-form-item>

        <el-form-item label="合同审核上限" prop="maxContractAudit">
          <el-input-number
            v-model="formData.maxContractAudit"
            :min="1"
            :max="999"
            placeholder="每日最大审核数量"
            style="width: 200px"
          />
          <span class="unit-text">个/日</span>
        </el-form-item>

        <el-form-item label="配套管理权限" prop="canManageFacilities">
          <el-switch
            v-model="formData.canManageFacilities"
            active-text="允许"
            inactive-text="禁止"
          />
        </el-form-item>

        <el-form-item label="举报处理权限" prop="canHandleReports">
          <el-switch
            v-model="formData.canHandleReports"
            active-text="允许"
            inactive-text="禁止"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="formData.remarks"
            type="textarea"
            :rows="3"
            placeholder="权限配置说明（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">
            {{ editingItem ? '更新' : '保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getPermissionConfigs, 
  createPermissionConfig, 
  updatePermissionConfig, 
  deletePermissionConfig,
  togglePermissionStatus,
  getCommunityAdmins,
  getCommunityList
} from '@/api/platform'

const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingItem = ref(null)
const formRef = ref(null)
const tableData = ref([])
const communityOptions = ref([])
const adminOptions = ref([])

const searchForm = ref({
  communityId: '',
  adminName: ''
})

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

const formData = reactive({
  adminId: '',
  maxHouseAudit: 50,
  maxUserAudit: 30,
  maxContractAudit: 20,
  canManageFacilities: true,
  canHandleReports: true,
  remarks: ''
})

const formRules = {
  adminId: [
    { required: true, message: '请选择小区管理员', trigger: 'change' }
  ],
  maxHouseAudit: [
    { required: true, message: '请输入房源审核上限', trigger: 'blur' }
  ],
  maxUserAudit: [
    { required: true, message: '请输入用户审核上限', trigger: 'blur' }
  ],
  maxContractAudit: [
    { required: true, message: '请输入合同审核上限', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      communityId: searchForm.value.communityId,
      adminName: searchForm.value.adminName,
      pageNum: pagination.value.current,
      pageSize: pagination.value.size
    }
    
    const response = await getPermissionConfigs(params)
    if (response.code === 200) {
      const data = response.data
      tableData.value = data.records || []
      pagination.value.total = data.total || 0
    } else {
      ElMessage.error(response.message || '获取权限配置失败')
    }
  } catch (error) {
    console.error('加载权限配置失败:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

const loadCommunityOptions = async () => {
  try {
    const response = await getCommunityList({ pageSize: 999 })
    if (response.code === 200) {
      communityOptions.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载小区列表失败:', error)
  }
}

const loadAdminOptions = async () => {
  try {
    const response = await getCommunityAdmins()
    if (response.code === 200) {
      adminOptions.value = response.data || []
    }
  } catch (error) {
    console.error('加载管理员列表失败:', error)
    adminOptions.value = []
  }
}

const resetSearch = () => {
  searchForm.value = {
    communityId: '',
    adminName: ''
  }
  loadData()
}

const resetForm = () => {
  Object.assign(formData, {
    adminId: '',
    maxHouseAudit: 50,
    maxUserAudit: 30,
    maxContractAudit: 20,
    canManageFacilities: true,
    canHandleReports: true,
    remarks: ''
  })
  editingItem.value = null
}

const handleAdd = () => {
  resetForm()
  showAddDialog.value = true
}

const handleEdit = (row) => {
  editingItem.value = row
  Object.assign(formData, {
    adminId: row.adminId,
    maxHouseAudit: row.maxHouseAudit,
    maxUserAudit: row.maxUserAudit,
    maxContractAudit: row.maxContractAudit,
    canManageFacilities: row.canManageFacilities,
    canHandleReports: row.canHandleReports,
    remarks: row.remarks || ''
  })
  showAddDialog.value = true
}

const handleAdminChange = (adminId) => {
  // 可以在这里根据选择的管理员自动填充一些默认值
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    saving.value = true
    
    const apiCall = editingItem.value 
      ? updatePermissionConfig(editingItem.value.id, formData)
      : createPermissionConfig(formData)
    
    const response = await apiCall
    if (response.code === 200) {
      ElMessage.success(editingItem.value ? '权限配置更新成功' : '权限配置创建成功')
      showAddDialog.value = false
      loadData()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存权限配置失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  try {
    const response = await togglePermissionStatus(row.id)
    if (response.code === 200) {
      ElMessage.success(`权限配置已${row.status === 'active' ? '停用' : '启用'}`)
      loadData()
    } else {
      ElMessage.error(response.message || '状态切换失败')
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除管理员"${row.adminName}"的权限配置吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deletePermissionConfig(row.id)
    if (response.code === 200) {
      ElMessage.success('权限配置删除成功')
      loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除权限配置失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

onMounted(() => {
  loadData()
  loadCommunityOptions()
  loadAdminOptions()
})
</script>

<style scoped>
.permission-control-container {
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

.unit-text {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.dialog-footer {
  text-align: right;
}
</style>

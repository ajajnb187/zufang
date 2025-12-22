<template>
  <div class="admin-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>小区管理员管理</span>
          <el-button type="primary" @click="handleAdd" :disabled="loading">
            <el-icon><Plus /></el-icon>
            新增管理员
          </el-button>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="adminId" label="管理员ID" width="100" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="communityName" label="所属小区" width="200">
          <template #default="{ row }">
            <el-tag v-if="row.communityId" type="success">{{ row.communityName }}</el-tag>
            <el-tag v-else type="warning">未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleAssign(row)"
              :disabled="loading"
            >
              {{ row.communityId ? '重新分配' : '分配小区' }}
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleEdit(row)"
              :disabled="loading"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :disabled="loading"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分配小区对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配小区" width="500px">
      <div v-if="currentAdmin" class="assign-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="管理员">{{ currentAdmin.nickname }} ({{ currentAdmin.phone }})</el-descriptions-item>
          <el-descriptions-item label="当前小区">
            <el-tag v-if="currentAdmin.communityId" type="info">{{ currentAdmin.communityName }}</el-tag>
            <span v-else class="text-gray">未分配</span>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="mt-4">
          <el-form :model="assignForm" label-width="100px">
            <el-form-item label="选择小区" required>
              <el-select 
                v-model="assignForm.communityId" 
                placeholder="请选择要分配的小区"
                style="width: 100%"
                clearable
              >
                <el-option
                  v-for="community in communityList"
                  :key="community.communityId"
                  :label="community.communityName"
                  :value="community.communityId"
                  :disabled="community.status !== 'active'"
                >
                  <span>{{ community.communityName }}</span>
                  <el-tag v-if="community.status !== 'active'" type="info" size="small" class="ml-2">
                    {{ community.status === 'inactive' ? '未激活' : '其他状态' }}
                  </el-tag>
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitAssign"
          :loading="assignLoading"
          :disabled="!assignForm.communityId"
        >
          确定分配
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑/新增管理员对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      :title="currentEditAdmin ? '编辑管理员' : '新增管理员'" 
      width="500px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="手机号" required>
          <el-input 
            v-model="editForm.phone" 
            placeholder="请输入手机号"
            :disabled="!!currentEditAdmin"
          />
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input 
            v-model="editForm.nickname" 
            placeholder="请输入昵称"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitEdit"
          :loading="loading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getUnassignedCommunityAdmins, 
  assignCommunityToAdmin,
  getCommunityList 
} from '@/api/platform'

const loading = ref(false)
const assignLoading = ref(false)
const tableData = ref([])
const communityList = ref([])

const assignDialogVisible = ref(false)
const currentAdmin = ref(null)
const assignForm = ref({
  communityId: null
})

const editDialogVisible = ref(false)
const currentEditAdmin = ref(null)
const editForm = ref({
  phone: '',
  nickname: ''
})

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const loadData = async () => {
  loading.value = true
  try {
    const [adminsResponse, communitiesResponse] = await Promise.all([
      getUnassignedCommunityAdmins(),
      getCommunityList({ pageNum: 1, pageSize: 1000 })
    ])
    
    if (adminsResponse.code === 200) {
      tableData.value = adminsResponse.data || []
    } else {
      ElMessage.error(adminsResponse.message || '获取管理员列表失败')
    }
    
    if (communitiesResponse.code === 200) {
      // 提取分页数据中的记录
      communityList.value = communitiesResponse.data.records || communitiesResponse.data || []
    } else {
      ElMessage.error(communitiesResponse.message || '获取小区列表失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleAssign = (row) => {
  currentAdmin.value = row
  assignForm.value.communityId = row.communityId || null
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  if (!assignForm.value.communityId) {
    ElMessage.warning('请选择要分配的小区')
    return
  }
  
  assignLoading.value = true
  try {
    const response = await assignCommunityToAdmin(
      currentAdmin.value.adminId, 
      assignForm.value.communityId
    )
    
    if (response.code === 200) {
      ElMessage.success('小区分配成功')
      assignDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(response.message || '分配失败')
    }
  } catch (error) {
    console.error('分配小区失败:', error)
    ElMessage.error('分配失败，请重试')
  } finally {
    assignLoading.value = false
  }
}

const handleAdd = () => {
  editForm.value = {
    phone: '',
    nickname: ''
  }
  currentEditAdmin.value = null
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  currentEditAdmin.value = row
  editForm.value = {
    phone: row.phone || '',
    nickname: row.nickname || ''
  }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!editForm.value.phone || !editForm.value.nickname) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    loading.value = true
    // TODO: 调用后端接口保存数据
    ElMessage.success(currentEditAdmin.value ? '编辑成功' : '新增成功')
    editDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认要删除管理员 ${row.nickname}(${row.phone}) 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    // TODO: 调用后端删除接口
    ElMessage.success('删除成功')
    loadData()
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
.admin-management-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.assign-content {
  padding: 10px 0;
}

.text-gray {
  color: #909399;
}

.mt-4 {
  margin-top: 16px;
}

.ml-2 {
  margin-left: 8px;
}
</style>

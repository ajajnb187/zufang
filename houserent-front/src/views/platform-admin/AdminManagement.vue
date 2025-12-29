<template>
  <div class="admin-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">管理员档案</h2>
        <p class="page-subtitle">管理小区管理员账号及权限分配</p>
      </div>
      <el-button type="primary" size="large" @click="handleAdd" :disabled="loading" class="add-btn">
        <el-icon><Plus /></el-icon>
        <span>新增管理员</span>
      </el-button>
    </div>

    <!-- 数据表格卡片 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        stripe 
        class="modern-table"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: '600' }"
      >
        <el-table-column prop="adminId" label="ID" width="80" align="center" />
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            <div class="phone-cell">
              <el-icon class="phone-icon"><Phone /></el-icon>
              <span>{{ row.phone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120">
          <template #default="{ row }">
            <div class="admin-name">
              <el-icon class="user-icon"><User /></el-icon>
              <span>{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="communityName" label="所属小区" min-width="180">
          <template #default="{ row }">
            <el-tag v-if="row.communityId" type="success" effect="light" round>
              <el-icon><OfficeBuilding /></el-icon>
              <span style="margin-left: 4px">{{ row.communityName }}</span>
            </el-tag>
            <el-tag v-else type="warning" effect="light" round>未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon class="time-icon"><Clock /></el-icon>
              <span>{{ formatDate(row.createdAt) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link
              @click="handleAssign(row)"
              :disabled="loading"
              icon="Location"
            >
              {{ row.communityId ? '重新分配' : '分配小区' }}
            </el-button>
            <el-button 
              type="warning" 
              link
              @click="handleEdit(row)"
              :disabled="loading"
              icon="Edit"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              link
              @click="handleDelete(row)"
              :disabled="loading"
              icon="Delete"
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
        <el-form-item v-if="!currentEditAdmin" label="密码" required>
          <el-input 
            v-model="editForm.password" 
            type="password"
            placeholder="请输入登录密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input 
            v-model="editForm.nickname" 
            placeholder="请输入昵称"
          />
        </el-form-item>
        <el-form-item v-if="!currentEditAdmin" label="分配小区">
          <el-select 
            v-model="editForm.communityId" 
            placeholder="可选，也可稍后分配"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="community in communityList"
              :key="community.communityId"
              :label="community.communityName"
              :value="community.communityId"
            />
          </el-select>
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
import { Plus, Phone, User, OfficeBuilding, Clock } from '@element-plus/icons-vue'
import { 
  getUnassignedCommunityAdmins, 
  assignCommunityToAdmin,
  getCommunityList,
  createCommunityAdmin,
  updateCommunityAdmin,
  deleteCommunityAdmin
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
  password: '',
  nickname: '',
  communityId: null
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
    password: '',
    nickname: '',
    communityId: null
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
  if (!editForm.value.nickname) {
    ElMessage.warning('请填写昵称')
    return
  }
  
  try {
    loading.value = true
    
    if (currentEditAdmin.value) {
      // 编辑现有管理员
      const response = await updateCommunityAdmin(currentEditAdmin.value.adminId, {
        nickname: editForm.value.nickname
      })
      if (response.code === 200) {
        ElMessage.success('编辑成功')
        editDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(response.message || '编辑失败')
      }
    } else {
      // 新增管理员
      if (!editForm.value.phone) {
        ElMessage.warning('请填写手机号')
        return
      }
      if (!editForm.value.password) {
        ElMessage.warning('请填写密码')
        return
      }
      
      const response = await createCommunityAdmin({
        phone: editForm.value.phone,
        password: editForm.value.password,
        nickname: editForm.value.nickname,
        communityId: editForm.value.communityId
      })
      if (response.code === 200) {
        ElMessage.success('创建成功')
        editDialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(response.message || '创建失败')
      }
    }
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
      `确认要删除管理员 ${row.nickname}(${row.phone}) 吗？删除后该用户将变为普通用户。`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    const response = await deleteCommunityAdmin(row.adminId)
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
.admin-management-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100%;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.add-btn {
  height: 40px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 500;
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

/* 单元格样式 */
.phone-cell,
.admin-name,
.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.phone-icon {
  color: #67c23a;
  font-size: 16px;
}

.user-icon {
  color: #409eff;
  font-size: 16px;
}

.time-icon {
  color: #909399;
  font-size: 14px;
}

/* 对话框 */
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

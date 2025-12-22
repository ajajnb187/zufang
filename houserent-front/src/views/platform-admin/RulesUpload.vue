<template>
  <div class="rules-upload-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>租赁条例上传</span>
        </div>
      </template>

      <!-- 上传区域 -->
      <el-row :gutter="24">
        <el-col :span="12">
          <div class="upload-section">
            <h3>上传新条例文档</h3>
            <el-upload
              ref="uploadRef"
              class="upload-demo"
              drag
              action="#"
              :on-change="handleFileChange"
              :before-upload="beforeUpload"
              :auto-upload="false"
              :show-file-list="false"
              accept=".pdf,.doc,.docx"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将文件拖拽到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  只能上传PDF/Word文档，且不超过10MB
                </div>
              </template>
            </el-upload>
            
            <div v-if="selectedFile" class="file-info">
              <p><strong>已选择文件：</strong>{{ selectedFile.name }}</p>
              <p><strong>文件大小：</strong>{{ formatFileSize(selectedFile.size) }}</p>
              
              <el-form :model="uploadForm" label-width="80px" style="margin-top: 15px;">
                <el-form-item label="文档标题">
                  <el-input v-model="uploadForm.title" placeholder="默认使用文件名" />
                </el-form-item>
                <el-form-item label="版本号">
                  <el-input v-model="uploadForm.version" placeholder="如：1.0" style="width: 120px" />
                </el-form-item>
                <el-form-item label="描述">
                  <el-input v-model="uploadForm.description" type="textarea" :rows="2" placeholder="条例说明（可选）" />
                </el-form-item>
              </el-form>
              
              <div class="upload-actions">
                <el-button @click="clearFile">取消</el-button>
                <el-button type="primary" @click="uploadFile" :loading="uploading">
                  <el-icon><Upload /></el-icon>
                  上传条例
                </el-button>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="12">
          <div class="rules-info">
            <h3>条例上传说明</h3>
            <el-alert
              title="上传要求"
              type="info"
              :closable="false"
              show-icon
            >
              <ul>
                <li>支持PDF、Word格式文档</li>
                <li>文件大小不超过10MB</li>
                <li>文档内容应包含完整的租赁条例</li>
                <li>上传后需手动设置为生效状态</li>
              </ul>
            </el-alert>
            
            <div class="stats-info" style="margin-top: 20px;">
              <el-statistic title="已上传条例" :value="rulesList.length" />
              <el-statistic title="生效中条例" :value="activeCount" style="margin-top: 10px;" />
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 已上传条例列表 -->
      <div class="rules-list">
        <div class="list-header">
          <h3>已上传条例</h3>
          <el-input v-model="searchKeyword" placeholder="搜索条例标题" style="width: 250px" clearable @input="loadRulesList">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <el-table :data="rulesList" v-loading="loading" stripe>
          <el-table-column prop="title" label="条例标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="fileName" label="文件名称" width="200" show-overflow-tooltip />
          <el-table-column prop="version" label="版本" width="80" />
          <el-table-column prop="fileSize" label="文件大小" width="100" />
          <el-table-column prop="downloadCount" label="下载次数" width="90" />
          <el-table-column prop="uploadTime" label="上传时间" width="160" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="handlePreview(row)">
                <el-icon><View /></el-icon> 预览
              </el-button>
              <el-button type="success" size="small" link @click="handleDownload(row)">
                <el-icon><Download /></el-icon> 下载
              </el-button>
              <el-button 
                :type="row.status === 'active' ? 'warning' : 'success'" 
                size="small" 
                link 
                @click="toggleStatus(row)"
              >
                {{ row.status === 'active' ? '撤销生效' : '设为生效' }}
              </el-button>
              <el-button type="info" size="small" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button type="danger" size="small" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑条例信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="editForm.version" style="width: 120px" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Upload, Search, View, Download, Edit, Delete } from '@element-plus/icons-vue'
import { 
  uploadRulesDocument, 
  getRulesDocuments, 
  deleteRulesDocument,
  updateRulesDocument,
  activateRulesDocument,
  deactivateRulesDocument,
  downloadRulesDocument
} from '@/api/platform'

const loading = ref(false)
const uploading = ref(false)
const saving = ref(false)
const selectedFile = ref(null)
const rulesList = ref([])
const searchKeyword = ref('')
const showEditDialog = ref(false)
const uploadRef = ref(null)
const currentDocument = ref(null)

const uploadForm = reactive({
  title: '',
  version: '1.0',
  description: ''
})

const editForm = reactive({
  id: null,
  title: '',
  version: '',
  description: ''
})

const activeCount = computed(() => {
  return rulesList.value.filter(r => r.status === 'active').length
})

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  uploadForm.title = file.name.split('.')[0]
}

const clearFile = () => {
  selectedFile.value = null
  uploadForm.title = ''
  uploadForm.version = '1.0'
  uploadForm.description = ''
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const beforeUpload = (file) => {
  const isValidType = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'].includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('只能上传PDF或Word文档!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文档大小不能超过10MB!')
    return false
  }
  return true
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const uploadFile = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('title', uploadForm.title || selectedFile.value.name.split('.')[0])
    formData.append('version', uploadForm.version || '1.0')
    formData.append('description', uploadForm.description || '')
    
    const response = await uploadRulesDocument(formData)
    if (response.code === 200) {
      ElMessage.success('条例上传成功！')
      clearFile()
      loadRulesList()
    } else {
      ElMessage.error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const loadRulesList = async () => {
  loading.value = true
  try {
    const params = searchKeyword.value ? { keyword: searchKeyword.value } : {}
    const response = await getRulesDocuments(params)
    if (response.code === 200) {
      rulesList.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取条例列表失败')
    }
  } catch (error) {
    console.error('加载条例列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handlePreview = (row) => {
  if (row.filePath) {
    window.open(row.filePath, '_blank')
  } else {
    ElMessage.warning('文件路径不存在')
  }
}

const handleDownload = async (row) => {
  try {
    const response = await downloadRulesDocument(row.id)
    if (response.code === 200 && response.data) {
      window.open(response.data, '_blank')
    } else {
      ElMessage.error('获取下载链接失败')
    }
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
  }
}

const handleEdit = (row) => {
  currentDocument.value = row
  editForm.id = row.id
  editForm.title = row.title
  editForm.version = row.version || ''
  editForm.description = row.description || ''
  showEditDialog.value = true
}

const submitEdit = async () => {
  if (!editForm.id) return
  
  saving.value = true
  try {
    const response = await updateRulesDocument(editForm.id, {
      title: editForm.title,
      version: editForm.version,
      description: editForm.description
    })
    if (response.code === 200) {
      ElMessage.success('更新成功')
      showEditDialog.value = false
      loadRulesList()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  try {
    const isActive = row.status === 'active'
    await ElMessageBox.confirm(
      isActive ? '确定要撤销该条例的生效状态吗？' : '确定要将该条例设为生效状态吗？',
      '确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    const response = isActive 
      ? await deactivateRulesDocument(row.id)
      : await activateRulesDocument(row.id)
    
    if (response.code === 200) {
      ElMessage.success(isActive ? '已撤销生效' : '已设为生效')
      loadRulesList()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态切换失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除条例"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteRulesDocument(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadRulesList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除条例失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

const getStatusType = (status) => {
  const map = {
    active: 'success',
    pending: 'warning',
    archived: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    active: '生效中',
    pending: '待生效',
    archived: '已归档'
  }
  return map[status] || '未知'
}

onMounted(() => {
  loadRulesList()
})
</script>

<style scoped>
.rules-upload-container {
  padding: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.upload-section {
  margin-bottom: 30px;
}

.upload-section h3,
.rules-info h3,
.rules-list h3 {
  margin-bottom: 16px;
  color: #303133;
}

.file-info {
  margin-top: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.file-info p {
  margin: 8px 0;
}

.upload-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.rules-info ul {
  margin: 0;
  padding-left: 20px;
}

.rules-info li {
  margin: 8px 0;
}

.rules-list {
  margin-top: 40px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.list-header h3 {
  margin: 0;
}

.upload-demo {
  width: 100%;
}

.stats-info {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}
</style>

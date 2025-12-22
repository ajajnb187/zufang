<template>
  <router-view />
</template>

<script setup>
// App.vue 作为路由容器
</script>

<style>
:root {
  --primary-color: #4facfe;
  --secondary-color: #00f2fe;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --border-color: #dcdfe6;
  --bg-color: #f5f7fa;
  --sidebar-width: 240px;
  --header-height: 60px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: var(--bg-color);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100vh;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

/* Element Plus 样式覆盖 */
.el-button--primary {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color)) !important;
  border: none !important;
}

.el-button--primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.el-card {
  border-radius: 12px !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
}
</style>

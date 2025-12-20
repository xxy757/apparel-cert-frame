<template>
  <div class="admin-container">
    <div class="admin-sidebar">
      <div class="admin-info">
        <div class="logo">
          <el-avatar size="large" :src="admin.avatar">{{ admin.username ? admin.username.charAt(0) : 'A' }}</el-avatar>
        </div>
        <div class="admin-details">
          <h3>{{ admin.username || '未登录' }}</h3>
          <p class="admin-role">管理员</p>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/user">
          <template #title>
            <span>用户管理</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/admin/content">
          <template #title>
            <span>内容管理</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/admin/certification">
          <template #title>
            <span>认证管理</span>
          </template>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button type="danger" size="small" @click="logout">退出登录</el-button>
      </div>
    </div>
    <div class="admin-content">
      <router-view />
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'AdminCenter',
  setup() {
    const router = useRouter()
    const activeMenu = ref('/admin/user')
    const admin = reactive({
      username: '管理员',
      avatar: ''
    })
    
    const logout = () => {
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userType')
      ElMessage.success('退出登录成功')
      router.push('/login')
    }
    
    return {
      activeMenu,
      admin,
      logout
    }
  }
}
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.admin-sidebar {
  width: 260px;
  background-color: #304156;
  color: #bfcbd9;
  display: flex;
  flex-direction: column;
}

.admin-info {
  padding: 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #475669;
}

.logo {
  margin-right: 15px;
}

.admin-details h3 {
  margin: 0;
  font-size: 16px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.admin-role {
  margin: 5px 0 0;
  font-size: 12px;
  opacity: 0.8;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #475669;
  text-align: center;
}

.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
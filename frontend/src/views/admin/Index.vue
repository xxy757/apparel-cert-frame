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
        <el-menu-item index="/admin/dashboard">
          <template #title>
            <span>仪表盘</span>
          </template>
        </el-menu-item>
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
    </div>
    <div class="admin-main">
      <!-- 顶部导航栏 -->
      <div class="top-header">
        <div class="header-left">
          <!-- 可以放置面包屑或其他内容 -->
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-dropdown">
              <el-avatar :size="36" :src="admin.avatar">{{ admin.username ? admin.username.charAt(0) : 'A' }}</el-avatar>
              <span class="username">{{ admin.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <!-- 内容区域 -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { clearSession, getActiveUserType, getSession } from '@/utils/auth'

export default {
  name: 'AdminCenter',
  components: {
    ArrowDown,
    User,
    SwitchButton
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const activeMenu = ref('/admin/user')
    const admin = reactive({
      username: '管理员',
      avatar: ''
    })
    
    onMounted(() => {
      // 设置当前激活的菜单
      activeMenu.value = route.path
      
      // 从localStorage获取管理员信息
      const username = getSession('3').username
      if (username) {
        admin.username = username
      }
    })
    
    const handleCommand = (command) => {
      if (command === 'profile') {
        router.push('/admin/user')
      } else if (command === 'logout') {
        logout()
      }
    }
    
    const logout = () => {
      // 仅退出管理员账号，不影响其他账号类型
      clearSession('3')
      localStorage.removeItem('savedPassword')
      localStorage.removeItem('savedUsername')
      
      sessionStorage.clear()
      
      ElMessage.success('退出登录成功')
      const nextType = getActiveUserType()
      if (nextType === '1') {
        router.push('/personal/resume')
      } else if (nextType === '2') {
        router.push('/enterprise/job')
      } else {
        router.push('/login')
      }
    }
    
    return {
      activeMenu,
      admin,
      handleCommand,
      logout
    }
  }
}
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
  height: 100vh;
  background-color: #f5f7fa;
  overflow: hidden;
}

.admin-sidebar {
  width: 260px;
  flex-shrink: 0;
  background-color: #304156;
  color: #bfcbd9;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
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

.admin-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.top-header {
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: sticky;
  top: 0;
  z-index: 10;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.user-dropdown .username {
  margin: 0 8px;
  font-size: 14px;
  color: #333;
}

.user-dropdown .el-icon {
  font-size: 12px;
  color: #999;
}

.admin-content {
  flex: 1;
  min-height: 0;
  padding: 20px;
  overflow-y: auto;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 8px;
}
</style>

<template>
  <div class="enterprise-container">
    <div class="enterprise-sidebar">
      <div class="enterprise-info">
        <div class="logo">
          <el-avatar size="large" :src="enterprise.logo">{{ enterprise.companyName ? enterprise.companyName.charAt(0) : 'E' }}</el-avatar>
        </div>
        <div class="enterprise-details">
          <h3>{{ enterprise.companyName || '未登录' }}</h3>
          <p class="enterprise-type">企业用户</p>
          <p class="auth-status">
            <el-tag :type="getAuthStatusType(enterprise.authStatus)">
              {{ getAuthStatusText(enterprise.authStatus) }}
            </el-tag>
          </p>
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
        <el-menu-item v-if="enterprise.authStatus !== 1" index="/enterprise/auth">
          <template #title>
            <span>企业认证</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/enterprise/job">
          <template #title>
            <span>职位管理</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/enterprise/resume">
          <template #title>
            <span>简历管理</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/enterprise/interview">
          <template #title>
            <span>面试管理</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/enterprise/talent">
          <template #title>
            <span>人才搜索</span>
          </template>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="enterprise-main">
      <!-- 顶部导航栏 -->
      <div class="top-header">
        <div class="header-left">
          <!-- 可以放置面包屑或其他内容 -->
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-dropdown">
              <el-avatar :size="36" :src="enterprise.logo">{{ enterprise.companyName ? enterprise.companyName.charAt(0) : 'E' }}</el-avatar>
              <span class="username">{{ enterprise.companyName || '企业用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><OfficeBuilding /></el-icon>
                  企业信息
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
      <div class="enterprise-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, OfficeBuilding, SwitchButton } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { clearSession, getActiveUserType, getSession } from '@/utils/auth'

export default {
  name: 'EnterpriseCenter',
  components: {
    ArrowDown,
    OfficeBuilding,
    SwitchButton
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const activeMenu = ref('/enterprise/job')
    const enterprise = reactive({
      companyName: '',
      authStatus: 0,
      logo: '',
      contactPerson: ''
    })
    
    onMounted(() => {
      // 设置当前激活的菜单
      activeMenu.value = route.path
      
      // 获取当前企业用户信息
      loadCurrentEnterprise()
    })
    
    const loadCurrentEnterprise = async () => {
      try {
        const response = await request.get('/auth/current-user')
        if (response.code === 200) {
          const enterpriseData = response.data
          enterprise.companyName = enterpriseData.companyName || getSession('2').username || ''
          enterprise.authStatus = enterpriseData.authStatus || 0
          enterprise.logo = enterpriseData.logo || ''
          enterprise.contactPerson = enterpriseData.contactPerson || ''
        } else {
          console.error('获取企业信息失败:', response.message)
          // 回退到localStorage中的用户名
          enterprise.companyName = getSession('2').username || ''
        }
      } catch (error) {
        console.error('获取企业信息异常:', error)
        // 回退到localStorage中的用户名
        enterprise.companyName = getSession('2').username || ''
      }
    }
    
    const getAuthStatusText = (status) => {
      const statusMap = {
        0: '待审核',
        1: '审核通过',
        2: '审核拒绝'
      }
      return statusMap[status] || '未知'
    }
    
    const getAuthStatusType = (status) => {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return typeMap[status] || 'info'
    }
    
    const handleCommand = (command) => {
      if (command === 'profile') {
        // 跳转到企业认证页面查看企业信息
        router.push('/enterprise/auth')
      } else if (command === 'logout') {
        logout()
      }
    }
    
    const logout = () => {
      // 仅退出企业账号，不影响其他账号类型
      clearSession('2')
      localStorage.removeItem('savedPassword')
      localStorage.removeItem('savedUsername')

      // 清除所有可能的缓存数据
      sessionStorage.clear()

      ElMessage.success('退出登录成功')
      const nextType = getActiveUserType()
      if (nextType === '1') {
        router.push('/personal/resume')
      } else if (nextType === '3') {
        router.push('/admin/dashboard')
      } else {
        router.push('/login')
      }
    }
    
    return {
      activeMenu,
      enterprise,
      getAuthStatusText,
      getAuthStatusType,
      handleCommand,
      logout
    }
  }
}
</script>

<style scoped>
.enterprise-container {
  display: flex;
  min-height: 100vh;
  height: 100vh;
  background-color: #f5f7fa;
  overflow: hidden;
}

.enterprise-sidebar {
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

.enterprise-info {
  padding: 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #475669;
}

.logo {
  margin-right: 15px;
}

.enterprise-details h3 {
  margin: 0;
  font-size: 16px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.enterprise-type {
  margin: 5px 0 0;
  font-size: 12px;
  opacity: 0.8;
}

.auth-status {
  margin: 5px 0 0;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.enterprise-main {
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
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dropdown .el-icon {
  font-size: 12px;
  color: #999;
}

.enterprise-content {
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

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
      <div class="sidebar-footer">
        <el-button type="danger" size="small" @click="logout">退出登录</el-button>
      </div>
    </div>
    <div class="enterprise-content">
      <router-view />
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'EnterpriseCenter',
  setup() {
    const router = useRouter()
    const activeMenu = ref('/enterprise/job')
    const enterprise = reactive({
      companyName: '某服装品牌有限公司',
      authStatus: 1,
      logo: ''
    })
    
    onMounted(() => {
      // TODO: 获取企业信息
      // 模拟获取企业信息
      console.log('获取企业信息')
    })
    
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
    
    const logout = () => {
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userType')
      ElMessage.success('退出登录成功')
      router.push('/login')
    }
    
    return {
      activeMenu,
      enterprise,
      getAuthStatusText,
      getAuthStatusType,
      logout
    }
  }
}
</script>

<style scoped>
.enterprise-container {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.enterprise-sidebar {
  width: 260px;
  background-color: #304156;
  color: #bfcbd9;
  display: flex;
  flex-direction: column;
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

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #475669;
  text-align: center;
}

.enterprise-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>

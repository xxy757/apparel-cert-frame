<template>
  <div class="personal-container">
    <div class="personal-sidebar">
      <div class="user-info">
        <div class="avatar">
          <el-avatar size="large">{{ user.name ? user.name.charAt(0) : 'U' }}</el-avatar>
        </div>
        <div class="user-details">
          <h3>{{ user.name || '未登录' }}</h3>
          <p class="user-type">个人用户</p>
          <p class="career-direction">{{ user.careerDirection || '未设置职业方向' }}</p>
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
        <el-menu-item index="/personal/resume">
          <template #title>
            <span>简历管理</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/personal/certification">
          <template #title>
            <span>技能认证</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/personal/job">
          <template #title>
            <span>求职服务</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/personal/training">
          <template #title>
            <span>培训推荐</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/personal/applications">
          <template #title>
            <span>我的申请</span>
          </template>
        </el-menu-item>
        <el-menu-item index="/personal/messages">
          <template #title>
            <span>消息中心</span>
          </template>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="personal-main">
      <!-- 顶部导航栏 -->
      <div class="top-header">
        <div class="header-left">
          <!-- 可以放置面包屑或其他内容 -->
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-dropdown">
              <el-avatar :size="36">{{ user.name ? user.name.charAt(0) : 'U' }}</el-avatar>
              <span class="username">{{ user.name || '用户' }}</span>
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
      <div class="personal-content">
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

export default {
  name: 'PersonalCenter',
  components: {
    ArrowDown,
    User,
    SwitchButton
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const activeMenu = ref('/personal/resume')
    const user = reactive({
      name: '张三',
      careerDirection: '设计师'
    })
    
    onMounted(() => {
      // 设置当前激活的菜单
      activeMenu.value = route.path
      
      // 从localStorage获取用户信息
      const username = localStorage.getItem('username')
      if (username) {
        user.name = username
      }
      console.log('获取用户信息')
    })
    
    const handleCommand = (command) => {
      if (command === 'profile') {
        // 跳转到个人信息页面
        router.push('/personal/resume')
      } else if (command === 'logout') {
        logout()
      }
    }
    
    const logout = () => {
      // 清除所有本地存储的用户信息
      localStorage.removeItem('token')
      localStorage.removeItem('userType')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('savedPassword')
      localStorage.removeItem('savedUsername')

      // 清除所有可能的缓存数据
      sessionStorage.clear()

      ElMessage.success('退出登录成功')
      router.push('/login')
    }
    
    return {
      activeMenu,
      user,
      handleCommand,
      logout
    }
  }
}
</script>

<style scoped>
.personal-container {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.personal-sidebar {
  width: 260px;
  background-color: #304156;
  color: #bfcbd9;
  display: flex;
  flex-direction: column;
}

.user-info {
  padding: 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #475669;
}

.avatar {
  margin-right: 15px;
}

.user-details h3 {
  margin: 0;
  font-size: 16px;
  color: #fff;
}

.user-type,
.career-direction {
  margin: 5px 0 0;
  font-size: 12px;
  opacity: 0.8;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.personal-main {
  flex: 1;
  display: flex;
  flex-direction: column;
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

.personal-content {
  flex: 1;
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

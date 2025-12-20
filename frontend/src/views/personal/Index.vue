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
      </el-menu>
      <div class="sidebar-footer">
        <el-button type="danger" size="small" @click="logout">退出登录</el-button>
      </div>
    </div>
    <div class="personal-content">
      <router-view />
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'PersonalCenter',
  setup() {
    const router = useRouter()
    const activeMenu = ref('/personal/resume')
    const user = reactive({
      name: '张三',
      careerDirection: '设计师'
    })
    
    onMounted(() => {
      // TODO: 获取用户信息
      // 模拟获取用户信息
      console.log('获取用户信息')
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
      user,
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

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #475669;
  text-align: center;
}

.personal-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>

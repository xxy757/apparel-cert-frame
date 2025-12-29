<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <header class="home-header">
      <div class="container">
        <div class="header-content">
          <div class="logo">
            <h1 class="logo-text">服装人才认证招聘平台</h1>
          </div>
          <nav class="nav-menu">
            <router-link to="/" class="nav-link active">首页</router-link>
            <a href="#features" class="nav-link">功能介绍</a>
            <a href="#industry-news" class="nav-link">行业动态</a>
            <a href="#about" class="nav-link">关于我们</a>
          </nav>
          <!-- 未登录状态 -->
          <div class="user-actions" v-if="!isLoggedIn">
            <router-link to="/login" class="btn btn-outline">登录</router-link>
            <router-link to="/register" class="btn btn-primary">注册</router-link>
          </div>
          <!-- 已登录状态 -->
          <div class="user-dropdown" v-else>
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-avatar-wrapper">
                <el-avatar :size="40" :src="userAvatar" class="user-avatar">
                  <el-icon :size="20"><User /></el-icon>
                </el-avatar>
                <span class="user-name">{{ displayName }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人信息</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </header>

    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <div class="hero-text">
            <h2 class="hero-title">专业认证，精准匹配</h2>
            <p class="hero-description">打造服装行业专业人才与优质企业的精准对接平台，提升行业整体水平</p>
            <div class="hero-buttons">
              <router-link to="/register" class="btn btn-primary btn-lg">立即注册</router-link>
              <router-link to="/login" class="btn btn-outline btn-lg">了解更多</router-link>
            </div>
          </div>
          <div class="hero-image">
            <img src="https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=600&h=400&fit=crop" alt="服装行业人才招聘" class="hero-img">
          </div>
        </div>
      </div>
    </section>

    <!-- 功能模块 -->
    <section id="features" class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">核心功能</h2>
          <p class="section-subtitle">为服装行业人才和企业提供全方位的服务支持</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" v-for="(feature, index) in features" :key="index">
            <div class="feature-icon" :style="{ backgroundColor: feature.color + '20' }">
              <el-icon :style="{ color: feature.color, fontSize: '48px' }">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>
            <router-link to="/login" class="feature-link">了解详情 <el-icon><ArrowRight /></el-icon></router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- 行业动态 -->
    <section id="industry-news" class="news-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">行业动态</h2>
          <p class="section-subtitle">关注服装行业最新资讯和发展趋势</p>
        </div>
        <div class="news-grid">
          <div class="news-card" v-for="news in newsList" :key="news.id">
            <div class="news-image">
              <img :src="news.image" :alt="news.title" class="news-img">
              <div class="news-date-badge">{{ news.date }}</div>
            </div>
            <div class="news-content">
              <h3 class="news-title">{{ news.title }}</h3>
              <p class="news-excerpt">{{ news.excerpt }}</p>
              <a href="#" class="read-more">阅读全文 <el-icon><ArrowRight /></el-icon></a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 关于我们 -->
    <section id="about" class="about-section">
      <div class="container">
        <div class="about-content">
          <div class="about-text">
            <h2 class="section-title">关于我们</h2>
            <p class="about-paragraph">本平台致力于打造服装行业专业人才与优质企业的精准对接平台，通过建立标准化的技能认证体系，提升行业整体水平。</p>
            <p class="about-paragraph">我们与行业协会、知名企业和教育机构深度合作，共同推动服装行业人才培养和职业发展，为行业的繁荣发展贡献力量。</p>
            <div class="about-cta">
              <router-link to="/register" class="btn btn-primary">加入我们</router-link>
            </div>
          </div>
          <div class="about-stats">
            <div class="stat-item">
              <div class="stat-number">10,000+</div>
              <div class="stat-label">认证人才</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">500+</div>
              <div class="stat-label">合作企业</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">20+</div>
              <div class="stat-label">认证类型</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">95%</div>
              <div class="stat-label">满意度</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="home-footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-about">
            <h3 class="footer-title">服装人才认证招聘平台</h3>
            <p class="footer-description">专业认证，精准匹配，推动服装行业人才发展</p>
          </div>
          <div class="footer-links">
            <h4 class="footer-subtitle">快速链接</h4>
            <ul class="footer-nav">
              <li><router-link to="/">首页</router-link></li>
              <li><a href="#features">功能介绍</a></li>
              <li><a href="#industry-news">行业动态</a></li>
              <li><a href="#about">关于我们</a></li>
            </ul>
          </div>
          <div class="footer-contact">
            <h4 class="footer-subtitle">联系我们</h4>
            <ul class="contact-list">
              <li class="contact-item"><el-icon><Phone /></el-icon> 400-123-4567</li>
              <li class="contact-item"><el-icon><Message /></el-icon> contact@apparelcert.com</li>
              <li class="contact-item"><el-icon><Location /></el-icon> 北京市朝阳区服装产业园区88号</li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <p class="copyright">&copy; 2024 服装人才认证招聘平台. 保留所有权利.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Briefcase, Medal, Document, Star, Message, Location, Phone, ArrowRight, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import request from '@/utils/request'

export default {
  name: 'Home',
  components: {
    User,
    Briefcase,
    Medal,
    Document,
    Star,
    Message,
    Location,
    Phone,
    ArrowRight,
    ArrowDown,
    SwitchButton
  },
  setup() {
    const router = useRouter()
    const userInfo = ref(null)
    const isLoggedIn = ref(false)

    // 计算显示名称
    const displayName = computed(() => {
      if (!userInfo.value) return ''
      const userType = localStorage.getItem('userType')
      if (userType === '1') {
        return userInfo.value.name || userInfo.value.username || '用户'
      } else if (userType === '2') {
        return userInfo.value.companyName || userInfo.value.contactPerson || '企业用户'
      }
      return '用户'
    })

    // 计算头像
    const userAvatar = computed(() => {
      if (!userInfo.value) return ''
      const userType = localStorage.getItem('userType')
      if (userType === '1') {
        return userInfo.value.avatar || ''
      } else if (userType === '2') {
        return userInfo.value.logo || ''
      }
      return ''
    })

    // 获取当前用户信息
    const fetchCurrentUser = async () => {
      const token = localStorage.getItem('token')
      if (!token) {
        isLoggedIn.value = false
        return
      }

      try {
        const res = await request.get('/auth/current-user')
        if (res.success) {
          userInfo.value = res
          isLoggedIn.value = true
        } else {
          isLoggedIn.value = false
          localStorage.removeItem('token')
          localStorage.removeItem('userType')
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        isLoggedIn.value = false
      }
    }

    // 处理下拉菜单命令
    const handleCommand = (command) => {
      if (command === 'profile') {
        showUserProfile()
      } else if (command === 'logout') {
        handleLogout()
      }
    }

    // 显示用户信息
    const showUserProfile = () => {
      const userType = localStorage.getItem('userType')
      let content = ''
      
      if (userType === '1' && userInfo.value) {
        content = `
          <div style="line-height: 2;">
            <p><strong>用户名：</strong>${userInfo.value.username || '-'}</p>
            <p><strong>姓名：</strong>${userInfo.value.name || '-'}</p>
            <p><strong>邮箱：</strong>${userInfo.value.email || '-'}</p>
            <p><strong>手机：</strong>${userInfo.value.phone || '-'}</p>
            <p><strong>职业方向：</strong>${userInfo.value.careerDirection || '-'}</p>
          </div>
        `
      } else if (userType === '2' && userInfo.value) {
        const authStatusMap = { 0: '待审核', 1: '已认证', 2: '审核未通过' }
        content = `
          <div style="line-height: 2;">
            <p><strong>用户名：</strong>${userInfo.value.username || '-'}</p>
            <p><strong>企业名称：</strong>${userInfo.value.companyName || '-'}</p>
            <p><strong>联系人：</strong>${userInfo.value.contactPerson || '-'}</p>
            <p><strong>联系电话：</strong>${userInfo.value.contactPhone || '-'}</p>
            <p><strong>邮箱：</strong>${userInfo.value.email || '-'}</p>
            <p><strong>认证状态：</strong>${authStatusMap[userInfo.value.authStatus] || '-'}</p>
          </div>
        `
      }

      ElMessageBox.alert(content, '个人信息', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定'
      })
    }

    // 退出登录
    const handleLogout = () => {
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('userType')
        userInfo.value = null
        isLoggedIn.value = false
        ElMessage.success('已退出登录')
        router.push('/')
      }).catch(() => {})
    }

    const newsList = ref([
      {
        id: 1,
        title: '2024年服装行业技能人才需求趋势分析',
        date: '2024-01-15',
        excerpt: '随着服装行业的快速发展，技能型人才的需求呈现出哪些新趋势？本文为您详细分析。',
        image: 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=300&h=200&fit=crop'
      },
      {
        id: 2,
        title: '服装设计师认证标准全新升级',
        date: '2024-01-10',
        excerpt: '为适应行业发展需求，本平台对服装设计师认证标准进行了全面升级，更加注重实践能力的考核。',
        image: 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=300&h=200&fit=crop'
      },
      {
        id: 3,
        title: '知名服装企业联合招聘专场即将举行',
        date: '2024-01-05',
        excerpt: '本平台将联合多家知名服装企业举办专场招聘会，提供众多优质岗位，欢迎广大人才参与。',
        image: 'https://images.unsplash.com/photo-1563013544-824ae1b704d3?w=300&h=200&fit=crop'
      }
    ])

    const features = ref([
      {
        title: '人才认证',
        description: '提供服装行业各类技能认证服务，建立标准化的人才评价体系',
        color: '#409eff',
        icon: 'Medal'
      },
      {
        title: '精准招聘',
        description: '基于技能认证结果，为企业匹配最合适的专业人才',
        color: '#67c23a',
        icon: 'Briefcase'
      },
      {
        title: '简历管理',
        description: '专业的简历创建和管理工具，帮助求职者展示个人能力',
        color: '#e6a23c',
        icon: 'User'
      },
      {
        title: '培训推荐',
        description: '根据职业发展需求，推荐相关技能培训课程',
        color: '#f56c6c',
        icon: 'Document'
      }
    ])

    onMounted(() => {
      // 页面加载时的初始化操作
      console.log('Home page loaded')
      fetchCurrentUser()
    })

    return {
      newsList,
      features,
      isLoggedIn,
      userInfo,
      displayName,
      userAvatar,
      handleCommand,
      showUserProfile,
      handleLogout
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.home-header {
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80px;
}

.logo h1 {
  font-size: 24px;
  color: #304156;
  margin: 0;
  font-weight: 600;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-link {
  color: #606266;
  text-decoration: none;
  font-size: 16px;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #409eff;
}

.user-actions {
  display: flex;
  gap: 15px;
}

.btn {
  padding: 8px 20px;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s;
  display: inline-block;
}

.btn-primary {
  background-color: #409eff;
  color: #fff;
  border: 1px solid #409eff;
}

.btn-primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.btn-outline {
  background-color: transparent;
  color: #409eff;
  border: 1px solid #409eff;
}

.btn-outline:hover {
  background-color: #ecf5ff;
}

.btn-lg {
  padding: 12px 30px;
  font-size: 18px;
}

/* 用户下拉菜单 */
.user-dropdown {
  display: flex;
  align-items: center;
}

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.user-avatar-wrapper:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  background-color: #409eff;
}

.user-name {
  margin-left: 10px;
  font-size: 14px;
  color: #606266;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  margin-left: 5px;
  color: #909399;
  font-size: 12px;
}

/* 英雄区域 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 160px 0 120px;
  margin-top: 80px;
}

.hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 50px;
}

.hero-text h2 {
  font-size: 48px;
  margin-bottom: 20px;
  line-height: 1.2;
}

.hero-text p {
  font-size: 20px;
  margin-bottom: 30px;
  opacity: 0.9;
  line-height: 1.6;
}

.hero-buttons {
  display: flex;
  gap: 20px;
}

.hero-image img {
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

/* 功能模块 */
.features-section {
  padding: 100px 0;
  background-color: #fff;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-header h2 {
  font-size: 36px;
  color: #304156;
  margin-bottom: 15px;
}

.section-header p {
  font-size: 18px;
  color: #606266;
  margin: 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
}

.feature-card {
  background-color: #f8f9fa;
  padding: 40px 30px;
  border-radius: 12px;
  text-align: center;
  transition: all 0.3s;
  border: 1px solid #e9ecef;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.feature-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 20px;
  color: #304156;
  margin-bottom: 15px;
}

.feature-card p {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 20px;
}

.feature-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.feature-link:hover {
  color: #66b1ff;
}

/* 行业动态 */
.news-section {
  padding: 100px 0;
  background-color: #f5f7fa;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.news-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.news-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.news-content {
  padding: 25px;
}

.news-content h3 {
  font-size: 20px;
  color: #304156;
  margin-bottom: 10px;
  line-height: 1.4;
}

.news-date {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
}

.news-excerpt {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 20px;
}

.read-more {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.read-more:hover {
  color: #66b1ff;
}

/* 关于我们 */
.about-section {
  padding: 100px 0;
  background-color: #fff;
}

.about-content {
  display: flex;
  gap: 80px;
  align-items: center;
}

.about-text {
  flex: 1;
}

.about-text h2 {
  font-size: 36px;
  color: #304156;
  margin-bottom: 20px;
}

.about-text p {
  font-size: 18px;
  color: #606266;
  line-height: 1.8;
  margin-bottom: 20px;
}

.about-stats {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
}

.stat-item {
  text-align: center;
  padding: 30px;
  background-color: #f8f9fa;
  border-radius: 12px;
}

.stat-number {
  font-size: 48px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 16px;
  color: #606266;
}

/* 页脚 */
.home-footer {
  background-color: #304156;
  color: #fff;
  padding: 60px 0 30px;
  margin-top: auto;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 50px;
  margin-bottom: 40px;
}

.footer-about h3 {
  font-size: 20px;
  margin-bottom: 20px;
}

.footer-about p {
  line-height: 1.6;
  opacity: 0.9;
}

.footer-links h4,
.footer-contact h4 {
  font-size: 18px;
  margin-bottom: 20px;
}

.footer-links ul,
.footer-contact ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li,
.footer-contact li {
  margin-bottom: 12px;
}

.footer-links a {
  color: #fff;
  text-decoration: none;
  opacity: 0.9;
  transition: opacity 0.3s;
}

.footer-links a:hover {
  opacity: 1;
}

.footer-bottom {
  text-align: center;
  padding-top: 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .container {
    width: 100%;
  }
  
  .hero-content {
    flex-direction: column;
    text-align: center;
  }
  
  .features-grid,
  .news-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .about-content {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  
  .hero-text h2 {
    font-size: 36px;
  }
  
  .hero-text p {
    font-size: 16px;
  }
  
  .features-grid,
  .news-grid,
  .about-stats,
  .footer-content {
    grid-template-columns: 1fr;
  }
  
  .section-header h2 {
    font-size: 28px;
  }
}
</style>
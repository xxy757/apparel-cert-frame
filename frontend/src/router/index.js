import { createRouter, createWebHistory } from 'vue-router'
import { getUserTypeByPath, hasSession, syncSessionForPath } from '../utils/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPassword.vue')
  },
  {
    path: '/personal',
    name: 'PersonalCenter',
    component: () => import('../views/personal/Index.vue'),
    redirect: '/personal/profile',
    children: [
      {
        path: 'resume',
        name: 'ResumeManage',
        component: () => import('../views/personal/Resume.vue')
      },
      {
        path: 'profile',
        name: 'PersonalProfile',
        component: () => import('../views/personal/Profile.vue')
      },
      {
        path: 'certification',
        name: 'CertificationManage',
        component: () => import('../views/personal/Certification.vue')
      },
      {
        path: 'job',
        name: 'JobSearch',
        component: () => import('../views/personal/JobSearch.vue')
      },
      {
        path: 'training',
        name: 'TrainingRecommend',
        component: () => import('../views/personal/Training.vue')
      },
      {
        path: 'exam',
        name: 'OnlineExam',
        component: () => import('../views/personal/Exam.vue')
      },
      {
        path: 'applications',
        name: 'MyApplications',
        component: () => import('../views/personal/Applications.vue')
      },
      {
        path: 'messages',
        name: 'MyMessages',
        component: () => import('../views/personal/Messages.vue')
      }
    ]
  },
  {
    path: '/enterprise',
    name: 'EnterpriseCenter',
    component: () => import('../views/enterprise/Index.vue'),
    redirect: '/enterprise/auth',
    children: [
      {
        path: 'auth',
        name: 'EnterpriseAuth',
        component: () => import('../views/enterprise/Auth.vue')
      },
      {
        path: 'job',
        name: 'EnterpriseJob',
        component: () => import('../views/enterprise/JobManage.vue')
      },
      {
        path: 'resume',
        name: 'EnterpriseResume',
        component: () => import('../views/enterprise/ResumeManage.vue')
      },
      {
        path: 'interview',
        name: 'EnterpriseInterview',
        component: () => import('../views/enterprise/Interview.vue')
      },
      {
        path: 'talent',
        name: 'TalentSearch',
        component: () => import('../views/enterprise/TalentSearch.vue')
      }
    ]
  },
  {
    path: '/admin',
    name: 'AdminCenter',
    component: () => import('../views/admin/Index.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('../views/admin/UserManage.vue')
      },
      {
        path: 'content',
        name: 'AdminContent',
        component: () => import('../views/admin/ContentManage.vue')
      },
      {
        path: 'certification',
        name: 'AdminCertification',
        component: () => import('../views/admin/CertificationManage.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由动态加载失败兜底，避免页面进入不可用状态
router.onError((error) => {
  const message = String(error?.message || '')
  if (message.includes('Failed to fetch dynamically imported module') ||
      message.includes('Importing a module script failed')) {
    window.location.reload()
  }
})

// 路由守卫 - 简单的身份验证
router.beforeEach((to, from, next) => {
  // 不需要登录的公开路由
  const publicRoutes = ['/login', '/register', '/forgot-password', '/']

  if (publicRoutes.includes(to.path)) {
    syncSessionForPath(to.path)
    // 公开路由直接通过
    return next()
  }

  // 仅对三类用户中心做登录态检查
  const requiredUserType = getUserTypeByPath(to.path)
  if (!requiredUserType) {
    syncSessionForPath(to.path)
    return next()
  }

  if (!hasSession(requiredUserType)) {
    return next('/login')
  }

  // 进入不同用户中心时，自动切换到对应账号会话
  syncSessionForPath(to.path)
  return next()
})

export default router

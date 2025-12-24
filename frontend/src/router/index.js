import { createRouter, createWebHistory } from 'vue-router'

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
    path: '/personal',
    name: 'PersonalCenter',
    component: () => import('../views/personal/Index.vue'),
    children: [
      {
        path: 'resume',
        name: 'ResumeManage',
        component: () => import('../views/personal/Resume.vue')
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
    children: [
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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 简单的身份验证
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userType = localStorage.getItem('userType')

  // 不需要登录的公开路由
  const publicRoutes = ['/login', '/register', '/']

  if (publicRoutes.includes(to.path)) {
    // 公开路由直接通过
    return next()
  }

  // 需要登录的路由
  if (!token) {
    // 没有token，跳转到登录页
    return next('/login')
  }

  // 有token，检查是否访问了正确的用户中心
  if (to.path.startsWith('/personal') && userType !== 'personal') {
    return next('/login')
  }
  if (to.path.startsWith('/enterprise') && userType !== 'enterprise') {
    return next('/login')
  }
  if (to.path.startsWith('/admin') && userType !== 'admin') {
    return next('/login')
  }

  // 允许访问
  return next()
})

export default router

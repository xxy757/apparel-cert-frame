<template>
  <div class="login-container">
    <div class="login-box">
      <h2>服装行业人才技能认证与招聘服务平台</h2>
      <div class="login-tabs">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="个人登录" name="personal">
            <el-form ref="personalFormRef" :model="personalForm" :rules="rules" label-width="80px">
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="personalForm.username"
                  placeholder="请输入用户名"
                  autocomplete="off"
                ></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="personalForm.password"
                  type="password"
                  placeholder="请输入密码"
                  autocomplete="new-password"
                ></el-input>
              </el-form-item>
              <!-- 登录失败次数限制提示 -->
              <div v-if="loginAttempts.personal > 0" class="login-warning">
                <el-alert
                  v-if="loginAttempts.personal < maxAttempts"
                  :title="`登录失败，还剩 ${maxAttempts - loginAttempts.personal} 次尝试机会`"
                  type="warning"
                  :closable="false"
                  show-icon
                />
                <el-alert
                  v-else
                  :title="`账号已锁定，请 ${lockCountdown} 秒后重试`"
                  type="error"
                  :closable="false"
                  show-icon
                />
              </div>
              <el-form-item>
                <div class="login-actions">
                  <el-button type="primary" @click="login('personal')" :loading="loading" :disabled="isLocked">登录</el-button>
                  <el-button @click="resetForm('personalForm')">重置</el-button>
                </div>
              </el-form-item>
              <div class="login-footer">
                <router-link to="/forgot-password">忘记密码？</router-link>
                <router-link to="/register" style="margin-left: 20px">立即注册</router-link>
              </div>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="企业登录" name="enterprise">
            <el-form ref="enterpriseFormRef" :model="enterpriseForm" :rules="rules" label-width="80px">
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="enterpriseForm.username"
                  placeholder="请输入用户名"
                  autocomplete="off"
                ></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="enterpriseForm.password"
                  type="password"
                  placeholder="请输入密码"
                  autocomplete="new-password"
                ></el-input>
              </el-form-item>
              <!-- 登录失败次数限制提示 -->
              <div v-if="loginAttempts.enterprise > 0" class="login-warning">
                <el-alert
                  v-if="loginAttempts.enterprise < maxAttempts"
                  :title="`登录失败，还剩 ${maxAttempts - loginAttempts.enterprise} 次尝试机会`"
                  type="warning"
                  :closable="false"
                  show-icon
                />
                <el-alert
                  v-else
                  :title="`账号已锁定，请 ${lockCountdown} 秒后重试`"
                  type="error"
                  :closable="false"
                  show-icon
                />
              </div>
              <el-form-item>
                <div class="login-actions">
                  <el-button type="primary" @click="login('enterprise')" :loading="loading" :disabled="isLocked">登录</el-button>
                  <el-button @click="resetForm('enterpriseForm')">重置</el-button>
                </div>
              </el-form-item>
              <div class="login-footer">
                <router-link to="/forgot-password">忘记密码？</router-link>
                <router-link to="/register" style="margin-left: 20px">立即注册</router-link>
              </div>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const activeTab = ref('personal')
    const personalForm = reactive({
      username: '',
      password: ''
    })
    const enterpriseForm = reactive({
      username: '',
      password: ''
    })
    const personalFormRef = ref(null)
    const enterpriseFormRef = ref(null)

    // 登录失败次数限制相关
    const maxAttempts = 5
    const lockDuration = 300 // 锁定时间（秒）
    const loginAttempts = reactive({
      personal: 0,
      enterprise: 0
    })
    const lockTime = reactive({
      personal: null,
      enterprise: null
    })
    const lockCountdown = ref(0)
    let countdownTimer = null

    // 计算是否被锁定
    const isLocked = computed(() => {
      const type = activeTab.value
      return loginAttempts[type] >= maxAttempts && lockCountdown.value > 0
    })

    // 开始倒计时
    const startCountdown = (type) => {
      lockTime[type] = Date.now()
      lockCountdown.value = lockDuration
      
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
      
      countdownTimer = setInterval(() => {
        const elapsed = Math.floor((Date.now() - lockTime[type]) / 1000)
        lockCountdown.value = Math.max(0, lockDuration - elapsed)
        
        if (lockCountdown.value <= 0) {
          clearInterval(countdownTimer)
          loginAttempts[type] = 0
          lockTime[type] = null
        }
      }, 1000)
    }

    // 检查锁定状态
    const checkLockStatus = (type) => {
      if (lockTime[type]) {
        const elapsed = Math.floor((Date.now() - lockTime[type]) / 1000)
        if (elapsed < lockDuration) {
          lockCountdown.value = lockDuration - elapsed
          startCountdown(type)
          return true
        } else {
          loginAttempts[type] = 0
          lockTime[type] = null
        }
      }
      return false
    }

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }
    
    const login = async (type) => {
      // 检查是否被锁定
      if (isLocked.value) {
        ElMessage.error(`账号已锁定，请 ${lockCountdown.value} 秒后重试`)
        return
      }

      const formRef = type === 'personal' ? personalFormRef : enterpriseFormRef
      const form = type === 'personal' ? personalForm : enterpriseForm

      if (!formRef.value) {
        ElMessage.error('表单未加载')
        return
      }

      // 验证表单
      formRef.value.validate(async (valid) => {
        if (!valid) {
          ElMessage.error('请填写完整的登录信息')
          return false
        }

        loading.value = true

        try {
          // 根据用户类型调用不同的登录接口
          const loginUrl = type === 'personal' ? '/auth/login/personal' : '/auth/login/enterprise'
          const response = await request.post(loginUrl, {
            username: form.username,
            password: form.password
          })

          // response 是 Result 对象 { code, message, data }
          // data 里面包含 token, userId 等
          if (response.data && response.data.token) {
            // 登录成功，重置失败次数
            loginAttempts[type] = 0
            lockTime[type] = null
            
            localStorage.setItem('token', response.data.token)
            localStorage.setItem('userType', response.data.userType || (type === 'personal' ? '1' : '2'))
            localStorage.setItem('userId', response.data.userId || '')
            localStorage.setItem('username', form.username)

            ElMessage.success('登录成功')

            // 根据用户类型跳转到对应的中心页面
            const redirectPath = type === 'personal' ? '/personal/resume' : '/enterprise/job'
            router.replace(redirectPath)
          } else {
            // 登录失败，增加失败次数
            loginAttempts[type]++
            
            if (loginAttempts[type] >= maxAttempts) {
              startCountdown(type)
              ElMessage.error(`登录失败次数过多，账号已锁定 ${lockDuration} 秒`)
            } else {
              ElMessage.error(response.message || `登录失败，还剩 ${maxAttempts - loginAttempts[type]} 次尝试机会`)
            }
          }
        } catch (error) {
          console.error('登录失败:', error)
          // 登录失败，增加失败次数
          loginAttempts[type]++
          
          if (loginAttempts[type] >= maxAttempts) {
            startCountdown(type)
            ElMessage.error(`登录失败次数过多，账号已锁定 ${lockDuration} 秒`)
          }
        } finally {
          loading.value = false
        }
      })
    }
    
    const resetForm = (formName) => {
      if (formName === 'personalForm') {
        personalFormRef.value.resetFields()
        // 强制清空密码字段
        personalForm.password = ''
      } else {
        enterpriseFormRef.value.resetFields()
        // 强制清空密码字段
        enterpriseForm.password = ''
      }
    }

    const forgetPassword = () => {
      // TODO: 实现忘记密码功能
      ElMessage.info('忘记密码功能开发中')
    }

    // 清除所有登录表单数据的函数
    const clearAllForms = () => {
      // 清空个人表单
      personalForm.username = ''
      personalForm.password = ''

      // 清空企业表单
      enterpriseForm.username = ''
      enterpriseForm.password = ''

      // 重置表单验证状态
      if (personalFormRef.value) {
        personalFormRef.value.clearValidate()
      }
      if (enterpriseFormRef.value) {
        enterpriseFormRef.value.clearValidate()
      }
    }

    // 监听标签页切换，清空另一个表单的数据
    watch(activeTab, (_newTab, oldTab) => {
      // 切换时检查新标签页的锁定状态
      checkLockStatus(_newTab)
      
      if (oldTab === 'personal') {
        // 从个人登录切换到企业登录，清空个人表单
        personalForm.username = ''
        personalForm.password = ''
        if (personalFormRef.value) {
          personalFormRef.value.clearValidate()
        }
      } else if (oldTab === 'enterprise') {
        // 从企业登录切换到个人登录，清空企业表单
        enterpriseForm.username = ''
        enterpriseForm.password = ''
        if (enterpriseFormRef.value) {
          enterpriseFormRef.value.clearValidate()
        }
      }
    })

    // 组件挂载时清除localStorage中可能存在的旧密码数据
    onMounted(() => {
      // 清除可能保存的敏感信息
      localStorage.removeItem('savedPassword')
      localStorage.removeItem('savedUsername')

      // 清空所有表单
      clearAllForms()
      
      // 检查锁定状态
      checkLockStatus('personal')
      checkLockStatus('enterprise')
    })

    // 组件卸载前清空表单数据
    onBeforeUnmount(() => {
      clearAllForms()
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
    })

    return {
      activeTab,
      loading,
      personalForm,
      enterpriseForm,
      personalFormRef,
      enterpriseFormRef,
      rules,
      login,
      resetForm,
      forgetPassword,
      // 登录失败次数限制相关
      maxAttempts,
      loginAttempts,
      lockCountdown,
      isLocked
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.login-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #1d1d1f;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.login-tabs {
  margin-top: 20px;
}

.login-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.login-actions .el-button {
  flex: 1;
  border-radius: 10px;
  padding: 12px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.login-actions .el-button--primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.login-actions .el-button--primary:disabled {
  background: #c0c4cc;
  box-shadow: none;
  cursor: not-allowed;
}

.login-footer {
  margin-top: 20px;
  text-align: right;
  font-size: 14px;
}

.login-footer a {
  color: #667eea;
  text-decoration: none;
  transition: color 0.3s ease;
}

.login-footer a:hover {
  color: #764ba2;
}

.login-warning {
  margin-bottom: 20px;
}

.login-warning .el-alert {
  border-radius: 10px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-warning .el-alert--warning {
  background: linear-gradient(135deg, #fff3cd 0%, #ffeeba 100%);
}

.login-warning .el-alert--error {
  background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
}

/* 美化表单输入框 */
:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

/* 美化标签页 */
:deep(.el-tabs__item) {
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  color: #667eea;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>

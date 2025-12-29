<template>
  <div class="forgot-container">
    <div class="forgot-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
    </div>

    <div class="forgot-card">
      <div class="forgot-header">
        <div class="logo">
          <el-icon :size="40"><Key /></el-icon>
        </div>
        <h1>找回密码</h1>
        <p>通过邮箱验证重置您的密码</p>
      </div>

      <!-- 步骤指示器 -->
      <el-steps :active="currentStep" finish-status="success" class="forgot-steps">
        <el-step title="验证邮箱" />
        <el-step title="输入验证码" />
        <el-step title="重置密码" />
      </el-steps>

      <!-- 步骤1：输入邮箱 -->
      <div v-show="currentStep === 0" class="step-content">
        <el-form ref="emailFormRef" :model="emailForm" :rules="emailRules" label-position="top">
          <el-form-item label="注册邮箱" prop="email">
            <el-input 
              v-model="emailForm.email" 
              placeholder="请输入您注册时使用的邮箱" 
              size="large"
            >
              <template #prefix><el-icon><Message /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" style="width: 100%" @click="sendVerifyCode" :loading="sending">
              {{ sending ? '发送中...' : '发送验证码' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤2：输入验证码 -->
      <div v-show="currentStep === 1" class="step-content">
        <div class="verify-info">
          <el-icon :size="48" color="#409eff"><Message /></el-icon>
          <p>验证码已发送至</p>
          <p class="email-display">{{ maskedEmail }}</p>
        </div>
        <el-form ref="codeFormRef" :model="codeForm" :rules="codeRules" label-position="top">
          <el-form-item label="验证码" prop="code">
            <div class="code-input-group">
              <el-input 
                v-model="codeForm.code" 
                placeholder="请输入6位验证码" 
                size="large"
                maxlength="6"
              >
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
              <el-button 
                :disabled="countdown > 0" 
                @click="resendCode"
                size="large"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '重新发送' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" style="width: 100%" @click="verifyCode" :loading="verifying">
              验证
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3：重置密码 -->
      <div v-show="currentStep === 2" class="step-content">
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
          <el-form-item label="新密码" prop="password">
            <el-input 
              v-model="passwordForm.password" 
              type="password" 
              placeholder="请输入新密码（至少6位）" 
              size="large"
              show-password
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
            <div class="password-strength">
              <span>密码强度：</span>
              <div class="strength-bar">
                <div class="strength-level" :class="passwordStrength" :style="{ width: passwordStrengthWidth }"></div>
              </div>
              <span class="strength-text">{{ passwordStrengthText }}</span>
            </div>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入新密码" 
              size="large"
              show-password
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" style="width: 100%" @click="resetPassword" :loading="resetting">
              重置密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤4：完成 -->
      <div v-show="currentStep === 3" class="step-content success-content">
        <div class="success-icon">
          <el-icon :size="80" color="#67c23a"><CircleCheckFilled /></el-icon>
        </div>
        <h2>密码重置成功！</h2>
        <p>您的密码已成功重置，请使用新密码登录</p>
        <el-button type="primary" size="large" @click="goToLogin">
          立即登录
        </el-button>
      </div>

      <div class="forgot-footer" v-if="currentStep < 3">
        <router-link to="/login">
          <el-icon><ArrowLeft /></el-icon> 返回登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Key, Message, Lock, CircleCheckFilled, ArrowLeft } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const currentStep = ref(0)
const sending = ref(false)
const verifying = ref(false)
const resetting = ref(false)
const countdown = ref(0)
let countdownTimer = null

const emailFormRef = ref(null)
const codeFormRef = ref(null)
const passwordFormRef = ref(null)

const emailForm = reactive({ email: '' })
const codeForm = reactive({ code: '' })
const passwordForm = reactive({ password: '', confirmPassword: '' })

const maskedEmail = computed(() => {
  const email = emailForm.email
  if (!email) return ''
  const [name, domain] = email.split('@')
  if (name.length <= 2) return email
  return `${name.slice(0, 2)}***@${domain}`
})

// 密码强度
const passwordStrength = computed(() => {
  const pwd = passwordForm.password
  if (!pwd) return ''
  let score = 0
  if (pwd.length >= 6) score++
  if (pwd.length >= 10) score++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++
  if (/\d/.test(pwd)) score++
  if (/[!@#$%^&*]/.test(pwd)) score++
  if (score <= 2) return 'weak'
  if (score <= 3) return 'medium'
  return 'strong'
})

const passwordStrengthWidth = computed(() => {
  const map = { weak: '33%', medium: '66%', strong: '100%' }
  return map[passwordStrength.value] || '0%'
})

const passwordStrengthText = computed(() => {
  const map = { weak: '弱', medium: '中', strong: '强' }
  return map[passwordStrength.value] || ''
})

// 验证规则
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const codeRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

const sendVerifyCode = async () => {
  try {
    await emailFormRef.value.validate()
    sending.value = true
    
    // 调用发送验证码接口
    await request.post('/auth/forgot-password', { email: emailForm.email })
    
    ElMessage.success('验证码已发送，请查收邮件')
    currentStep.value = 1
    startCountdown()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '发送失败，请稍后重试')
    }
  } finally {
    sending.value = false
  }
}

const resendCode = () => {
  if (countdown.value > 0) return
  sendVerifyCode()
}

const verifyCode = async () => {
  try {
    await codeFormRef.value.validate()
    verifying.value = true
    
    // 模拟验证码验证
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 验证成功
    ElMessage.success('验证成功')
    currentStep.value = 2
  } catch (error) {
    if (error !== false) {
      ElMessage.error('验证码错误，请重新输入')
    }
  } finally {
    verifying.value = false
  }
}

const resetPassword = async () => {
  try {
    await passwordFormRef.value.validate()
    resetting.value = true
    
    // 调用重置密码接口
    await request.post('/auth/reset-password', {
      email: emailForm.email,
      code: codeForm.code,
      newPassword: passwordForm.password
    })
    
    ElMessage.success('密码重置成功')
    currentStep.value = 3
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '重置失败，请稍后重试')
    }
  } finally {
    resetting.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 40px 20px;
}

.forgot-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 { width: 400px; height: 400px; top: -100px; right: -100px; }
.shape-2 { width: 300px; height: 300px; bottom: -50px; left: -50px; }

.forgot-card {
  width: 100%;
  max-width: 480px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
}

.forgot-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: #fff;
}

.forgot-header h1 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.forgot-header p {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.forgot-steps {
  margin-bottom: 32px;
}

.step-content {
  min-height: 200px;
}

.verify-info {
  text-align: center;
  margin-bottom: 24px;
}

.verify-info p {
  margin: 8px 0;
  color: #606266;
}

.email-display {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.code-input-group {
  display: flex;
  gap: 12px;
}

.code-input-group .el-input {
  flex: 1;
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: #ebeef5;
  border-radius: 2px;
  overflow: hidden;
}

.strength-level {
  height: 100%;
  transition: all 0.3s;
  border-radius: 2px;
}

.strength-level.weak { background: #f56c6c; }
.strength-level.medium { background: #e6a23c; }
.strength-level.strong { background: #67c23a; }

.success-content {
  text-align: center;
  padding: 40px 0;
}

.success-icon {
  margin-bottom: 24px;
}

.success-content h2 {
  color: #303133;
  margin: 0 0 12px 0;
}

.success-content p {
  color: #909399;
  margin: 0 0 32px 0;
}

.forgot-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.forgot-footer a {
  color: #409eff;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 576px) {
  .forgot-card {
    padding: 24px;
    border-radius: 16px;
  }
  
  .code-input-group {
    flex-direction: column;
  }
}
</style>

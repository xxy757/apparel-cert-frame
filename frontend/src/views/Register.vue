<template>
  <div class="register-container">
    <div class="register-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <div class="register-card">
      <div class="register-header">
        <div class="logo">
          <el-icon :size="40"><OfficeBuilding /></el-icon>
        </div>
        <h1>服装行业人才认证平台</h1>
        <p>创建账号，开启职业新旅程</p>
      </div>

      <el-tabs v-model="activeTab" class="register-tabs">
        <el-tab-pane label="个人注册" name="personal">
          <el-form ref="personalFormRef" :model="personalForm" :rules="personalRules" label-position="top">
            <!-- 步骤指示器 -->
            <el-steps :active="personalStep" simple class="register-steps">
              <el-step title="账号信息" />
              <el-step title="个人信息" />
              <el-step title="完成注册" />
            </el-steps>

            <!-- 步骤1：账号信息 -->
            <div v-show="personalStep === 0" class="step-content">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="personalForm.username" placeholder="6-20位字母、数字或下划线" size="large">
                  <template #prefix><el-icon><User /></el-icon></template>
                </el-input>
                <div class="field-hint" v-if="usernameStatus">
                  <el-icon :class="usernameStatus"><component :is="usernameStatus === 'available' ? 'CircleCheck' : 'CircleClose'" /></el-icon>
                  {{ usernameStatus === 'available' ? '用户名可用' : '用户名已被占用' }}
                </div>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="personalForm.password" type="password" placeholder="至少6位，包含字母和数字" size="large" show-password>
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
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="personalForm.confirmPassword" type="password" placeholder="请再次输入密码" size="large" show-password>
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="personalForm.email" placeholder="用于接收验证邮件和找回密码" size="large">
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
              </el-form-item>
            </div>

            <!-- 步骤2：个人信息 -->
            <div v-show="personalStep === 1" class="step-content">
              <el-form-item label="真实姓名" prop="name">
                <el-input v-model="personalForm.name" placeholder="请输入真实姓名" size="large">
                  <template #prefix><el-icon><UserFilled /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="personalForm.phone" placeholder="请输入手机号码" size="large">
                  <template #prefix><el-icon><Phone /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="职业方向" prop="careerDirection">
                <el-select v-model="personalForm.careerDirection" placeholder="请选择您的职业方向" size="large" style="width: 100%">
                  <el-option label="服装设计师" value="服装设计师" />
                  <el-option label="服装打版师" value="服装打版师" />
                  <el-option label="服装质检员" value="服装质检员" />
                  <el-option label="服装导购" value="服装导购" />
                  <el-option label="生产管理" value="生产管理" />
                  <el-option label="时装买手" value="时装买手" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-form-item label="工作经验">
                <el-select v-model="personalForm.experience" placeholder="请选择工作经验" size="large" style="width: 100%">
                  <el-option label="应届毕业生" value="应届毕业生" />
                  <el-option label="1-3年" value="1-3年" />
                  <el-option label="3-5年" value="3-5年" />
                  <el-option label="5-10年" value="5-10年" />
                  <el-option label="10年以上" value="10年以上" />
                </el-select>
              </el-form-item>
            </div>

            <!-- 步骤3：完成注册 -->
            <div v-show="personalStep === 2" class="step-content complete-step">
              <div class="complete-icon">
                <el-icon :size="80" color="#67c23a"><CircleCheckFilled /></el-icon>
              </div>
              <h3>信息确认</h3>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="用户名">{{ personalForm.username }}</el-descriptions-item>
                <el-descriptions-item label="邮箱">{{ personalForm.email }}</el-descriptions-item>
                <el-descriptions-item label="姓名">{{ personalForm.name }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ personalForm.phone }}</el-descriptions-item>
                <el-descriptions-item label="职业方向">{{ personalForm.careerDirection }}</el-descriptions-item>
              </el-descriptions>
              <el-checkbox v-model="agreeTerms" class="terms-checkbox">
                我已阅读并同意 <a href="#" @click.prevent="showTerms">《用户服务协议》</a> 和 <a href="#" @click.prevent="showPrivacy">《隐私政策》</a>
              </el-checkbox>
            </div>

            <!-- 步骤按钮 -->
            <div class="step-actions">
              <el-button v-if="personalStep > 0" @click="personalStep--" size="large">上一步</el-button>
              <el-button v-if="personalStep < 2" type="primary" @click="nextPersonalStep" size="large">下一步</el-button>
              <el-button v-if="personalStep === 2" type="primary" @click="register('personal')" size="large" :loading="loading" :disabled="!agreeTerms">
                完成注册
              </el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="企业注册" name="enterprise">
          <el-form ref="enterpriseFormRef" :model="enterpriseForm" :rules="enterpriseRules" label-position="top">
            <el-steps :active="enterpriseStep" simple class="register-steps">
              <el-step title="账号信息" />
              <el-step title="企业信息" />
              <el-step title="完成注册" />
            </el-steps>

            <!-- 步骤1：账号信息 -->
            <div v-show="enterpriseStep === 0" class="step-content">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="enterpriseForm.username" placeholder="6-20位字母、数字或下划线" size="large">
                  <template #prefix><el-icon><User /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="enterpriseForm.password" type="password" placeholder="至少6位，包含字母和数字" size="large" show-password>
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="enterpriseForm.confirmPassword" type="password" placeholder="请再次输入密码" size="large" show-password>
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="企业邮箱" prop="email">
                <el-input v-model="enterpriseForm.email" placeholder="请使用企业邮箱注册" size="large">
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
              </el-form-item>
            </div>

            <!-- 步骤2：企业信息 -->
            <div v-show="enterpriseStep === 1" class="step-content">
              <el-form-item label="企业名称" prop="companyName">
                <el-input v-model="enterpriseForm.companyName" placeholder="请输入企业全称" size="large">
                  <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="企业类型" prop="companyType">
                <el-select v-model="enterpriseForm.companyType" placeholder="请选择企业类型" size="large" style="width: 100%">
                  <el-option label="服装生产企业" value="服装生产企业" />
                  <el-option label="服装品牌公司" value="服装品牌公司" />
                  <el-option label="服装贸易公司" value="服装贸易公司" />
                  <el-option label="服装零售企业" value="服装零售企业" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-form-item label="联系人" prop="contactPerson">
                <el-input v-model="enterpriseForm.contactPerson" placeholder="请输入联系人姓名" size="large">
                  <template #prefix><el-icon><UserFilled /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="enterpriseForm.contactPhone" placeholder="请输入联系电话" size="large">
                  <template #prefix><el-icon><Phone /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="企业地址" prop="address">
                <el-input v-model="enterpriseForm.address" placeholder="请输入企业地址" size="large">
                  <template #prefix><el-icon><Location /></el-icon></template>
                </el-input>
              </el-form-item>
            </div>

            <!-- 步骤3：完成注册 -->
            <div v-show="enterpriseStep === 2" class="step-content complete-step">
              <div class="complete-icon">
                <el-icon :size="80" color="#67c23a"><CircleCheckFilled /></el-icon>
              </div>
              <h3>信息确认</h3>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="用户名">{{ enterpriseForm.username }}</el-descriptions-item>
                <el-descriptions-item label="企业邮箱">{{ enterpriseForm.email }}</el-descriptions-item>
                <el-descriptions-item label="企业名称">{{ enterpriseForm.companyName }}</el-descriptions-item>
                <el-descriptions-item label="企业类型">{{ enterpriseForm.companyType }}</el-descriptions-item>
                <el-descriptions-item label="联系人">{{ enterpriseForm.contactPerson }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ enterpriseForm.contactPhone }}</el-descriptions-item>
              </el-descriptions>
              <el-alert type="info" :closable="false" style="margin-top: 16px">
                企业注册后需要提交资质认证，审核通过后方可使用全部功能
              </el-alert>
              <el-checkbox v-model="agreeTerms" class="terms-checkbox">
                我已阅读并同意 <a href="#" @click.prevent="showTerms">《用户服务协议》</a> 和 <a href="#" @click.prevent="showPrivacy">《隐私政策》</a>
              </el-checkbox>
            </div>

            <div class="step-actions">
              <el-button v-if="enterpriseStep > 0" @click="enterpriseStep--" size="large">上一步</el-button>
              <el-button v-if="enterpriseStep < 2" type="primary" @click="nextEnterpriseStep" size="large">下一步</el-button>
              <el-button v-if="enterpriseStep === 2" type="primary" @click="register('enterprise')" size="large" :loading="loading" :disabled="!agreeTerms">
                完成注册
              </el-button>
            </div>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, Lock, Message, UserFilled, Phone, OfficeBuilding, Location,
  CircleCheckFilled, CircleCheck, CircleClose
} from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('personal')
const personalStep = ref(0)
const enterpriseStep = ref(0)
const agreeTerms = ref(false)
const usernameStatus = ref('')

const personalFormRef = ref(null)
const enterpriseFormRef = ref(null)

const personalForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
  email: '',
  careerDirection: '',
  experience: ''
})

const enterpriseForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  companyName: '',
  companyType: '',
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: ''
})

// 密码强度计算
const passwordStrength = computed(() => {
  const pwd = personalForm.password || enterpriseForm.password
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
const validateConfirmPassword = (rule, value, callback) => {
  const form = activeTab.value === 'personal' ? personalForm : enterpriseForm
  if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const personalRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 6, max: 20, message: '用户名长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  careerDirection: [{ required: true, message: '请选择职业方向', trigger: 'change' }]
}

const enterpriseRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 6, max: 20, message: '用户名长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入企业邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  companyType: [{ required: true, message: '请选择企业类型', trigger: 'change' }],
  contactPerson: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 检查用户名是否可用
watch(() => personalForm.username, async (val) => {
  if (val && val.length >= 6) {
    // 模拟检查用户名
    usernameStatus.value = Math.random() > 0.3 ? 'available' : 'taken'
  } else {
    usernameStatus.value = ''
  }
})

const nextPersonalStep = async () => {
  const fields = personalStep.value === 0 
    ? ['username', 'password', 'confirmPassword', 'email']
    : ['name', 'phone', 'careerDirection']
  
  try {
    await personalFormRef.value.validateField(fields)
    personalStep.value++
  } catch (e) {
    ElMessage.warning('请完善当前步骤的信息')
  }
}

const nextEnterpriseStep = async () => {
  const fields = enterpriseStep.value === 0
    ? ['username', 'password', 'confirmPassword', 'email']
    : ['companyName', 'companyType', 'contactPerson', 'contactPhone']
  
  try {
    await enterpriseFormRef.value.validateField(fields)
    enterpriseStep.value++
  } catch (e) {
    ElMessage.warning('请完善当前步骤的信息')
  }
}

const register = async (type) => {
  if (!agreeTerms.value) {
    ElMessage.warning('请先同意用户服务协议和隐私政策')
    return
  }

  loading.value = true
  const form = type === 'personal' ? personalForm : enterpriseForm

  try {
    const registerData = { username: form.username, password: form.password, email: form.email }
    let registerUrl = ''

    if (type === 'personal') {
      registerUrl = '/auth/register/personal'
      Object.assign(registerData, {
        name: form.name,
        phone: form.phone,
        careerDirection: form.careerDirection,
        experience: form.experience
      })
    } else {
      registerUrl = '/auth/register/enterprise'
      Object.assign(registerData, {
        companyName: form.companyName,
        companyType: form.companyType,
        contactPerson: form.contactPerson,
        contactPhone: form.contactPhone,
        address: form.address
      })
    }

    const response = await request.post(registerUrl, registerData)

    if (response.code === 200 || response.data) {
      ElMessage.success(type === 'personal' ? '注册成功！请登录' : '注册成功！待审核后可登录')
      setTimeout(() => router.push('/login'), 1500)
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const showTerms = () => ElMessage.info('用户服务协议')
const showPrivacy = () => ElMessage.info('隐私政策')
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 40px 20px;
}

.register-bg {
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
.shape-3 { width: 200px; height: 200px; top: 50%; left: 50%; transform: translate(-50%, -50%); }

.register-card {
  width: 100%;
  max-width: 520px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
}

.register-header {
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

.register-header h1 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.register-header p {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.register-tabs {
  margin-bottom: 24px;
}

.register-steps {
  margin-bottom: 32px;
}

.step-content {
  min-height: 300px;
}

.complete-step {
  text-align: center;
}

.complete-icon {
  margin-bottom: 16px;
}

.complete-step h3 {
  margin-bottom: 24px;
  color: #303133;
}

.field-hint {
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.field-hint .available { color: #67c23a; }
.field-hint .taken { color: #f56c6c; }

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

.strength-text {
  min-width: 20px;
}

.terms-checkbox {
  margin-top: 20px;
}

.terms-checkbox a {
  color: #409eff;
  text-decoration: none;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.step-actions .el-button {
  min-width: 120px;
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
  color: #909399;
}

.register-footer a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  margin-left: 8px;
}

@media (max-width: 576px) {
  .register-card {
    padding: 24px;
    border-radius: 16px;
  }
  
  .register-header h1 {
    font-size: 20px;
  }
}
</style>

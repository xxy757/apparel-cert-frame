<template>
  <div class="register-container">
    <div class="register-box">
      <h2>服装行业人才技能认证与招聘服务平台</h2>
      <div class="register-tabs">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="个人注册" name="personal">
            <el-form ref="personalForm" :model="personalForm" :rules="personalRules" label-width="100px">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="personalForm.username" placeholder="请输入用户名"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="personalForm.password" type="password" placeholder="请输入密码"></el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="personalForm.confirmPassword" type="password" placeholder="请确认密码"></el-input>
              </el-form-item>
              <el-form-item label="姓名" prop="name">
                <el-input v-model="personalForm.name" placeholder="请输入真实姓名"></el-input>
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="personalForm.phone" placeholder="请输入手机号"></el-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="personalForm.email" placeholder="请输入邮箱"></el-input>
              </el-form-item>
              <el-form-item label="职业方向" prop="careerDirection">
                <el-select v-model="personalForm.careerDirection" placeholder="请选择职业方向">
                  <el-option label="设计师" value="设计师"></el-option>
                  <el-option label="打版师" value="打版师"></el-option>
                  <el-option label="质检员" value="质检员"></el-option>
                  <el-option label="导购" value="导购"></el-option>
                  <el-option label="其他" value="其他"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <div class="register-actions">
                  <el-button type="primary" @click="register('personal')" :loading="loading">注册</el-button>
                  <el-button @click="resetForm('personalForm')">重置</el-button>
                </div>
              </el-form-item>
              <div class="register-footer">
                <a href="/login">已有账号？立即登录</a>
              </div>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="企业注册" name="enterprise">
            <el-form ref="enterpriseForm" :model="enterpriseForm" :rules="enterpriseRules" label-width="100px">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="enterpriseForm.username" placeholder="请输入用户名"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="enterpriseForm.password" type="password" placeholder="请输入密码"></el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="enterpriseForm.confirmPassword" type="password" placeholder="请确认密码"></el-input>
              </el-form-item>
              <el-form-item label="企业名称" prop="companyName">
                <el-input v-model="enterpriseForm.companyName" placeholder="请输入企业名称"></el-input>
              </el-form-item>
              <el-form-item label="企业类型" prop="companyType">
                <el-input v-model="enterpriseForm.companyType" placeholder="请输入企业类型"></el-input>
              </el-form-item>
              <el-form-item label="联系人" prop="contactPerson">
                <el-input v-model="enterpriseForm.contactPerson" placeholder="请输入联系人姓名"></el-input>
              </el-form-item>
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="enterpriseForm.contactPhone" placeholder="请输入联系电话"></el-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="enterpriseForm.email" placeholder="请输入邮箱"></el-input>
              </el-form-item>
              <el-form-item label="企业地址" prop="address">
                <el-input v-model="enterpriseForm.address" placeholder="请输入企业地址"></el-input>
              </el-form-item>
              <el-form-item>
                <div class="register-actions">
                  <el-button type="primary" @click="register('enterprise')" :loading="loading">注册</el-button>
                  <el-button @click="resetForm('enterpriseForm')">重置</el-button>
                </div>
              </el-form-item>
              <div class="register-footer">
                <a href="/login">已有账号？立即登录</a>
              </div>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const activeTab = ref('personal')
    
    const personalForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      name: '',
      phone: '',
      email: '',
      careerDirection: ''
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
    
    const personalFormRef = ref(null)
    const enterpriseFormRef = ref(null)
    
    const personalRules = {
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
      name: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      careerDirection: [
        { required: true, message: '请选择职业方向', trigger: 'change' }
      ]
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
      companyName: [
        { required: true, message: '请输入企业名称', trigger: 'blur' }
      ],
      contactPerson: [
        { required: true, message: '请输入联系人姓名', trigger: 'blur' }
      ],
      contactPhone: [
        { required: true, message: '请输入联系电话', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ]
    }
    
    const validateConfirmPassword = (rule, value, callback) => {
      const form = activeTab.value === 'personal' ? personalForm : enterpriseForm
      if (value !== form.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    
    const register = async (type) => {
      loading.value = true
      try {
        // TODO: 调用注册接口
        // 模拟注册成功
        ElMessage.success(type === 'personal' ? '个人注册成功' : '企业注册成功，待审核')
        router.push('/login')
      } catch (error) {
        ElMessage.error('注册失败：' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }
    
    const resetForm = (formName) => {
      if (formName === 'personalForm') {
        personalFormRef.value.resetFields()
      } else {
        enterpriseFormRef.value.resetFields()
      }
    }
    
    return {
      activeTab,
      loading,
      personalForm,
      enterpriseForm,
      personalFormRef,
      enterpriseFormRef,
      personalRules,
      enterpriseRules,
      register,
      resetForm
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.register-box {
  width: 500px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.register-tabs {
  margin-top: 20px;
}

.register-actions {
  display: flex;
  justify-content: space-between;
}

.register-footer {
  margin-top: 20px;
  text-align: right;
  font-size: 14px;
}

.register-footer a {
  color: #409eff;
  text-decoration: none;
}
</style>

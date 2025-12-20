<template>
  <div class="login-container">
    <div class="login-box">
      <h2>服装行业人才技能认证与招聘服务平台</h2>
      <div class="login-tabs">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="个人登录" name="personal">
            <el-form ref="personalForm" :model="personalForm" :rules="rules" label-width="80px">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="personalForm.username" placeholder="请输入用户名"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="personalForm.password" type="password" placeholder="请输入密码"></el-input>
              </el-form-item>
              <el-form-item>
                <div class="login-actions">
                  <el-button type="primary" @click="login('personal')" :loading="loading">登录</el-button>
                  <el-button @click="resetForm('personalForm')">重置</el-button>
                </div>
              </el-form-item>
              <div class="login-footer">
                <a href="#" @click.prevent="forgetPassword">忘记密码？</a>
                <a href="/register" style="margin-left: 20px">立即注册</a>
              </div>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="企业登录" name="enterprise">
            <el-form ref="enterpriseForm" :model="enterpriseForm" :rules="rules" label-width="80px">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="enterpriseForm.username" placeholder="请输入用户名"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="enterpriseForm.password" type="password" placeholder="请输入密码"></el-input>
              </el-form-item>
              <el-form-item>
                <div class="login-actions">
                  <el-button type="primary" @click="login('enterprise')" :loading="loading">登录</el-button>
                  <el-button @click="resetForm('enterpriseForm')">重置</el-button>
                </div>
              </el-form-item>
              <div class="login-footer">
                <a href="#" @click.prevent="forgetPassword">忘记密码？</a>
                <a href="/register" style="margin-left: 20px">立即注册</a>
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
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }
    
    const login = async (type) => {
      loading.value = true
      try {
        // TODO: 调用登录接口
        // 模拟登录成功
        localStorage.setItem('token', 'mock-token')
        localStorage.setItem('userType', type === 'personal' ? '1' : '2')
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        ElMessage.error('登录失败：' + (error.message || '未知错误'))
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
    
    const forgetPassword = () => {
      // TODO: 实现忘记密码功能
      ElMessage.info('忘记密码功能开发中')
    }
    
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
      forgetPassword
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
  background-color: #f5f7fa;
}

.login-box {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.login-tabs {
  margin-top: 20px;
}

.login-actions {
  display: flex;
  justify-content: space-between;
}

.login-footer {
  margin-top: 20px;
  text-align: right;
  font-size: 14px;
}

.login-footer a {
  color: #409eff;
  text-decoration: none;
}
</style>

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
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
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

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }
    
    const login = async (type) => {
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

          // 保存 token 和用户信息
          if (response.data && response.data.token) {
            localStorage.setItem('token', response.data.token)
            localStorage.setItem('userType', type)
            localStorage.setItem('userId', response.data.userId || response.data.id || '')
            localStorage.setItem('username', form.username)

            ElMessage.success('登录成功')

            // 根据用户类型跳转到对应的中心页面
            const redirectPath = type === 'personal' ? '/personal/resume' : '/enterprise/job'
            router.replace(redirectPath)
          } else {
            ElMessage.error('登录失败：返回数据格式错误')
          }
        } catch (error) {
          console.error('登录失败:', error)
          ElMessage.error(error.message || '登录失败，请检查用户名和密码')
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
    })

    // 组件卸载前清空表单数据
    onBeforeUnmount(() => {
      clearAllForms()
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

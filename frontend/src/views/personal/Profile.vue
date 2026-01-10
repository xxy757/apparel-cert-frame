<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人信息</h2>
    </div>
    
    <div class="profile-form">
      <el-card class="profile-card">
        <div class="avatar-section">
          <div class="avatar-preview">
            <el-avatar :size="100" :src="userInfo.avatar">
              {{ userInfo.name ? userInfo.name.charAt(0) : 'U' }}
            </el-avatar>
            <div class="avatar-actions">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :show-file-list="false"
                :headers="uploadHeaders"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                accept=".jpg,.jpeg,.png,.gif"
                :auto-upload="true"
              >
                <el-button type="primary" size="small">上传头像</el-button>
              </el-upload>
              <el-button size="small" @click="selectRandomDefaultAvatar" style="margin-top: 5px;">随机头像</el-button>
            </div>
          </div>
          
          <!-- 默认头像选择 -->
          <div class="default-avatars">
            <h4>选择默认头像:</h4>
            <div class="avatar-options">
              <el-avatar
                v-for="(avatar, index) in defaultAvatars"
                :key="index"
                :size="60"
                :src="avatar"
                :class="{ 'selected': userInfo.avatar === avatar }"
                @click="selectDefaultAvatar(index)"
                style="cursor: pointer; margin: 0 5px; border: 2px solid transparent;"
              />
            </div>
          </div>
          
          <p class="avatar-tip">点击上方头像预览当前头像，使用上面的按钮上传新头像，或从默认头像中选择一个。</p>
        </div>

        <el-form 
          :model="userInfo" 
          :rules="rules" 
          ref="formRef" 
          label-width="120px" 
          class="profile-form-content"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="userInfo.name" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="userInfo.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="userInfo.idCard" placeholder="请输入身份证号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职业方向" prop="careerDirection">
                <el-select 
                  v-model="userInfo.careerDirection" 
                  placeholder="请选择职业方向"
                  style="width: 100%"
                >
                  <el-option label="服装设计师" value="服装设计师" />
                  <el-option label="版型师" value="版型师" />
                  <el-option label="工艺师" value="工艺师" />
                  <el-option label="面料工程师" value="面料工程师" />
                  <el-option label="质量控制" value="质量控制" />
                  <el-option label="生产管理" value="生产管理" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="头像URL">
                <el-input 
                  v-model="userInfo.avatar" 
                  placeholder="可输入自定义头像URL地址"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button 
              type="primary" 
              @click="submitForm" 
              :loading="submitting"
              :disabled="!hasChanges"
            >
              {{ submitting ? '保存中...' : '保存信息' }}
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import request from '@/utils/request'

export default {
  name: 'PersonalProfile',
  components: {
    Edit
  },
  setup() {
    const formRef = ref(null)
    const submitting = ref(false)

    const userInfo = reactive({
      id: null,
      username: '',
      name: '',
      phone: '',
      email: '',
      idCard: '',
      careerDirection: '',
      avatar: ''
    })

    const originalUserInfo = reactive({})

    // 验证规则
    const rules = {
      name: [
        { required: true, message: '请输入姓名', trigger: 'blur' },
        { min: 2, max: 10, message: '姓名长度应在2-10个字符之间', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      idCard: [
        { 
          pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, 
          message: '请输入正确的身份证号码', 
          trigger: 'blur' 
        }
      ],
      avatar: [
        {
          validator: (rule, value, callback) => {
            if (!value) {
              callback();
              return;
            }
            // 验证是否为有效的图片URL
            const urlPattern = /^https?:\/\/.*\.(jpg|jpeg|png|gif|bmp|webp|svg|svg\?seed=.*)$/i;
            if (urlPattern.test(value)) {
              callback();
            } else {
              callback(new Error('请输入有效的图片URL地址'));
            }
          },
          trigger: 'blur'
        }
      ]
    }

    // 上传头像相关
    const uploadUrl = `${import.meta.env.VITE_APP_API_BASE_URL || '/api'}/file/avatar`
    const uploadHeaders = {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }

    // 检查是否有更改
    const hasChanges = computed(() => {
      return JSON.stringify(userInfo) !== JSON.stringify(originalUserInfo)
    })

    // 加载用户信息
    const loadUserInfo = async () => {
      try {
        const response = await request.get('/auth/current-user')
        if (response.code === 200) {
          const data = response.data
          Object.assign(userInfo, {
            id: data.userId,
            username: data.username || '',
            name: data.name || '',
            phone: data.phone || '',
            email: data.email || '',
            idCard: data.idCard || '',
            careerDirection: data.careerDirection || '',
            avatar: data.avatar || ''
          })
          // 保存原始信息用于比较
          Object.assign(originalUserInfo, JSON.parse(JSON.stringify(userInfo)))
        } else {
          ElMessage.error(response.message || '获取用户信息失败')
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败')
      }
    }

    // 处理头像上传成功
    const handleAvatarSuccess = (response, file, fileList) => {
      if (response.code === 200 && response.data && response.data.url) {
        userInfo.avatar = response.data.url
        ElMessage.success('头像上传成功')
        // 触发表单验证，因为头像发生了变化
        if (formRef.value) {
          formRef.value.validateField('avatar')
        }
      } else {
        ElMessage.error(response.message || '头像上传失败')
      }
    }

    // 上传头像前的验证
    const beforeAvatarUpload = (file) => {
      const isImage = file.type.indexOf('image/') === 0
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
      }
      if (!isLt5M) {
        ElMessage.error('图片大小不能超过 5MB!')
        return false
      }

      return true
    }

    // 提交表单
    const submitForm = async () => {
      if (!formRef.value) return

      // 验证表单
      await formRef.value.validate((valid) => {
        if (!valid) {
          ElMessage.error('请填写完整并正确的信息')
          return false
        }
      })

      submitting.value = true
      try {
        // 这里应该调用更新用户信息的API
        // 注意：实际项目中可能需要区分更新基本信息和更新头像的API
        const response = await request.post('/user-personal/update-basic-info', {
          id: userInfo.id,
          name: userInfo.name,
          phone: userInfo.phone,
          email: userInfo.email,
          idCard: userInfo.idCard,
          careerDirection: userInfo.careerDirection,
          avatar: userInfo.avatar
        })

        if (response.code === 200) {
          ElMessage.success('个人信息更新成功')
          // 更新原始信息
          Object.assign(originalUserInfo, JSON.parse(JSON.stringify(userInfo)))
        } else {
          ElMessage.error(response.message || '更新失败')
        }
      } catch (error) {
        console.error('更新个人信息失败:', error)
        ElMessage.error('更新失败，请稍后重试')
      } finally {
        submitting.value = false
      }
    }

    // 系统提供的默认头像列表
    const defaultAvatars = [
      'https://api.dicebear.com/7.x/adventurer/svg?seed=default1&backgroundColor=b6e3f4',
      'https://api.dicebear.com/7.x/adventurer/svg?seed=default2&backgroundColor=c0aede',
      'https://api.dicebear.com/7.x/adventurer/svg?seed=default3&backgroundColor=d1d4f9',
      'https://api.dicebear.com/7.x/adventurer/svg?seed=default4&backgroundColor=ffd5dc',
      'https://api.dicebear.com/7.x/adventurer/svg?seed=default5&backgroundColor=ffdfbf',
      'https://api.dicebear.com/7.x/adventurer/svg?seed=default6&backgroundColor=b6e3f4,c0aede',
      'https://api.dicebear.com/7.x/adventurer/svg?seed=default7&backgroundColor=d1d4f9,ffd5dc',
      'https://api.dicebear.com/7.x/adventurer/svg?seed=default8&backgroundColor=ffdfbf,b6e3f4'
    ];

    // 选择默认头像
    const selectDefaultAvatar = (index) => {
      userInfo.avatar = defaultAvatars[index];
    };

    // 随机选择一个默认头像
    const selectRandomDefaultAvatar = () => {
      const randomIndex = Math.floor(Math.random() * defaultAvatars.length);
      userInfo.avatar = defaultAvatars[randomIndex];
    };

    // 重置表单
    const resetForm = () => {
      // 恢复到原始信息
      Object.assign(userInfo, JSON.parse(JSON.stringify(originalUserInfo)))
    }

    onMounted(() => {
      loadUserInfo()
    })

    return {
      formRef,
      userInfo,
      rules,
      submitting,
      hasChanges,
      uploadUrl,
      uploadHeaders,
      handleAvatarSuccess,
      beforeAvatarUpload,
      submitForm,
      resetForm,
      defaultAvatars,
      selectDefaultAvatar,
      selectRandomDefaultAvatar
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.profile-header {
  margin-bottom: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 15px;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 5px;
}

.avatar-input {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-uploader:hover .avatar-edit-mask {
  opacity: 1;
}

.default-avatars {
  margin-top: 20px;
  text-align: center;
}

.avatar-options {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.default-avatars h4 {
  margin-bottom: 10px;
  font-weight: normal;
}

.selected {
  border-color: #409eff !important;
  box-shadow: 0 0 5px rgba(64, 158, 255, 0.5);
}

.avatar-edit-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-edit-mask .el-icon {
  color: white;
  font-size: 24px;
}

.avatar-tip {
  margin-top: 10px;
  color: #999;
  font-size: 14px;
}

.profile-form-content {
  margin-top: 20px;
}

.el-form {
  margin-top: 20px;
}
</style>
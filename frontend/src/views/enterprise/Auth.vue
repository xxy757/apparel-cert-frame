<template>
  <div class="auth-container">
    <div class="auth-header">
      <h2>企业认证</h2>
      <p class="auth-description">完成企业认证，提升企业可信度，享受更多招聘服务</p>
    </div>
    
    <!-- 认证状态显示 -->
    <div class="auth-status-section">
      <div class="status-card">
        <div class="status-header">
          <h3>当前认证状态</h3>
          <el-tag :type="getAuthStatusType(authInfo.status)" size="large">
            {{ getAuthStatusText(authInfo.status) }}
          </el-tag>
        </div>
        
        <div v-if="authInfo.status === 0" class="status-content">
          <el-alert
            title="审核中"
            type="warning"
            description="您的认证申请已提交，我们将在3-5个工作日内完成审核，请耐心等待。"
            show-icon
          />
        </div>
        
        <div v-else-if="authInfo.status === 1" class="status-content">
          <el-alert
            title="认证成功"
            type="success"
            description="恭喜您的企业已通过认证，现在可以享受更多招聘特权。"
            show-icon
          />
          <el-button type="primary" size="small" @click="viewAuthInfo">查看认证信息</el-button>
        </div>
        
        <div v-else-if="authInfo.status === 2" class="status-content">
          <el-alert
            title="认证失败"
            type="error"
            :description="`${authInfo.rejectReason || '您的认证申请未通过审核，请修改后重新提交。'}`"
            show-icon
            closable
          />
          <el-button type="primary" size="small" @click="editAuthInfo">重新提交</el-button>
        </div>
      </div>
    </div>
    
    <!-- 认证表单 -->
    <div v-if="authInfo.status !== 1" class="auth-form-section">
      <el-card shadow="hover" class="form-card">
        <template #header>
          <div class="card-header">
            <span>企业认证信息</span>
          </div>
        </template>
        
        <el-form
          ref="authFormRef"
          :model="authForm"
          :rules="authFormRules"
          label-position="top"
          label-width="100px"
          class="auth-form"
        >
          <!-- 基本信息 -->
          <div class="form-section">
            <h4>基本信息</h4>
            <div class="form-row">
              <el-form-item label="企业名称" prop="companyName">
                <el-input v-model="authForm.companyName" placeholder="请输入企业名称" />
              </el-form-item>
              <el-form-item label="统一社会信用代码" prop="creditCode">
                <el-input v-model="authForm.creditCode" placeholder="请输入统一社会信用代码" />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="企业类型" prop="companyType">
                <el-select v-model="authForm.companyType" placeholder="请选择企业类型">
                  <el-option label="有限责任公司" value="limited" />
                  <el-option label="股份有限公司" value="stock" />
                  <el-option label="个体工商户" value="individual" />
                  <el-option label="外资企业" value="foreign" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>
              <el-form-item label="企业规模" prop="companySize">
                <el-select v-model="authForm.companySize" placeholder="请选择企业规模">
                  <el-option label="50人以下" value="small" />
                  <el-option label="50-200人" value="medium" />
                  <el-option label="200-500人" value="large" />
                  <el-option label="500人以上" value="xlarge" />
                </el-select>
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="所在行业" prop="industry">
                <el-select v-model="authForm.industry" placeholder="请选择所在行业">
                  <el-option label="服装纺织" value="apparel" />
                  <el-option label="电子商务" value="ecommerce" />
                  <el-option label="批发零售" value="retail" />
                  <el-option label="制造业" value="manufacturing" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>
              <el-form-item label="成立时间" prop="establishDate">
                <el-date-picker
                  v-model="authForm.establishDate"
                  type="date"
                  placeholder="选择成立时间"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
          </div>
          
          <!-- 联系人信息 -->
          <div class="form-section">
            <h4>联系人信息</h4>
            <div class="form-row">
              <el-form-item label="联系人姓名" prop="contactName">
                <el-input v-model="authForm.contactName" placeholder="请输入联系人姓名" />
              </el-form-item>
              <el-form-item label="联系人职位" prop="contactPosition">
                <el-input v-model="authForm.contactPosition" placeholder="请输入联系人职位" />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="authForm.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
              <el-form-item label="电子邮箱" prop="contactEmail">
                <el-input v-model="authForm.contactEmail" placeholder="请输入电子邮箱" />
              </el-form-item>
            </div>
          </div>
          
          <!-- 企业地址 -->
          <div class="form-section">
            <h4>企业地址</h4>
            <div class="form-row">
              <el-form-item label="所在地区" prop="region">
                <el-cascader
                  v-model="authForm.region"
                  :options="regionOptions"
                  placeholder="选择所在地区"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="详细地址" prop="address">
                <el-input
                  v-model="authForm.address"
                  placeholder="请输入详细地址"
                  :rows="2"
                  type="textarea"
                />
              </el-form-item>
            </div>
          </div>
          
          <!-- 资质文件上传 -->
          <div class="form-section">
            <h4>资质文件上传</h4>
            <el-alert
              title="温馨提示"
              type="info"
              description="请上传清晰的营业执照扫描件或照片，文件大小不超过2MB，支持JPG、PNG、PDF格式。"
              show-icon
              closable
            />
            <div class="upload-section">
              <el-upload
                class="avatar-uploader"
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                :show-file-list="true"
                :file-list="fileList"
                accept=".jpg,.png,.pdf"
                :limit="1"
                :on-exceed="handleExceed"
              >
                <el-button size="small" type="primary">点击上传营业执照</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    只能上传jpg/png/pdf文件，且不超过2MB
                  </div>
                </template>
              </el-upload>
            </div>
          </div>
          
          <!-- 提交按钮 -->
          <div class="form-actions">
            <el-button type="primary" size="large" @click="submitAuth">提交认证申请</el-button>
            <el-button size="large" @click="resetForm">重置</el-button>
          </div>
        </el-form>
      </el-card>
    </div>
    
    <!-- 认证信息查看对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <el-descriptions border :column="2">
          <el-descriptions-item label="企业名称">{{ authForm.companyName }}</el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码">{{ authForm.creditCode }}</el-descriptions-item>
          <el-descriptions-item label="企业类型">{{ getCompanyTypeText(authForm.companyType) }}</el-descriptions-item>
          <el-descriptions-item label="企业规模">{{ getCompanySizeText(authForm.companySize) }}</el-descriptions-item>
          <el-descriptions-item label="所在行业">{{ getIndustryText(authForm.industry) }}</el-descriptions-item>
          <el-descriptions-item label="成立时间">{{ formatDate(authForm.establishDate) }}</el-descriptions-item>
          <el-descriptions-item label="联系人姓名">{{ authForm.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系人职位">{{ authForm.contactPosition }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ authForm.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="电子邮箱">{{ authForm.contactEmail }}</el-descriptions-item>
          <el-descriptions-item label="企业地址" :span="2">
            {{ authForm.region?.join('-') }} {{ authForm.address }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDate(authInfo.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核时间" v-if="authInfo.auditTime">{{ formatDate(authInfo.auditTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="fileList.length > 0" class="dialog-attachments">
          <h4>资质文件</h4>
          <el-upload
            class="file-list"
            :file-list="fileList"
            :auto-upload="false"
            :show-file-list="true"
            disabled
          >
          </el-upload>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button v-if="authInfo.status === 2" type="primary" @click="submitAuth">重新提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'EnterpriseAuth',
  setup() {
    // 认证信息
    const authInfo = reactive({
      status: 0, // 0: 待审核, 1: 通过, 2: 拒绝
      submitTime: '2023-11-15T10:30:00',
      auditTime: null,
      rejectReason: null
    })
    
    // 认证表单数据
    const authForm = reactive({
      companyName: '某服装品牌有限公司',
      creditCode: '91110000XXXXXXXXX',
      companyType: 'limited',
      companySize: 'medium',
      industry: 'apparel',
      establishDate: '2015-03-10',
      contactName: '张三',
      contactPosition: '人力资源经理',
      contactPhone: '13800138000',
      contactEmail: 'hr@example.com',
      region: ['北京市', '北京市', '朝阳区'],
      address: '建国路88号现代城A座18层'
    })
    
    // 文件列表
    const fileList = ref([
      {
        name: '营业执照.jpg',
        url: 'https://example.com/license.jpg'
      }
    ])
    
    // 表单引用
    const authFormRef = ref(null)
    
    // 对话框状态
    const dialogVisible = ref(false)
    const dialogTitle = computed(() => {
      return authInfo.status === 1 ? '认证信息详情' : '修改认证信息'
    })
    
    // 地区选项
    const regionOptions = ref([
      {
        value: '北京市',
        label: '北京市',
        children: [
          {
            value: '北京市',
            label: '北京市',
            children: [
              { value: '东城区', label: '东城区' },
              { value: '西城区', label: '西城区' },
              { value: '朝阳区', label: '朝阳区' },
              { value: '海淀区', label: '海淀区' },
              { value: '丰台区', label: '丰台区' }
            ]
          }
        ]
      },
      {
        value: '上海市',
        label: '上海市',
        children: [
          {
            value: '上海市',
            label: '上海市',
            children: [
              { value: '黄浦区', label: '黄浦区' },
              { value: '徐汇区', label: '徐汇区' },
              { value: '长宁区', label: '长宁区' },
              { value: '静安区', label: '静安区' },
              { value: '普陀区', label: '普陀区' }
            ]
          }
        ]
      }
    ])
    
    // 表单验证规则
    const authFormRules = {
      companyName: [
        { required: true, message: '请输入企业名称', trigger: 'blur' },
        { min: 2, max: 50, message: '企业名称长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      creditCode: [
        { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
        { pattern: /^[0-9A-Z]{18}$/, message: '请输入正确的统一社会信用代码', trigger: 'blur' }
      ],
      companyType: [
        { required: true, message: '请选择企业类型', trigger: 'change' }
      ],
      companySize: [
        { required: true, message: '请选择企业规模', trigger: 'change' }
      ],
      industry: [
        { required: true, message: '请选择所在行业', trigger: 'change' }
      ],
      establishDate: [
        { required: true, message: '请选择成立时间', trigger: 'change' }
      ],
      contactName: [
        { required: true, message: '请输入联系人姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '联系人姓名长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      contactPosition: [
        { required: true, message: '请输入联系人职位', trigger: 'blur' },
        { min: 2, max: 30, message: '联系人职位长度在 2 到 30 个字符', trigger: 'blur' }
      ],
      contactPhone: [
        { required: true, message: '请输入联系电话', trigger: 'blur' },
        { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      contactEmail: [
        { required: true, message: '请输入电子邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的电子邮箱地址', trigger: 'blur' }
      ],
      region: [
        { required: true, message: '请选择所在地区', trigger: 'change' }
      ],
      address: [
        { required: true, message: '请输入详细地址', trigger: 'blur' },
        { min: 5, max: 100, message: '详细地址长度在 5 到 100 个字符', trigger: 'blur' }
      ]
    }
    
    // 获取认证状态文本
    const getAuthStatusText = (status) => {
      const statusMap = {
        0: '待审核',
        1: '审核通过',
        2: '审核拒绝'
      }
      return statusMap[status] || '未知'
    }
    
    // 获取认证状态类型
    const getAuthStatusType = (status) => {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return typeMap[status] || 'info'
    }
    
    // 获取企业类型文本
    const getCompanyTypeText = (type) => {
      const typeMap = {
        'limited': '有限责任公司',
        'stock': '股份有限公司',
        'individual': '个体工商户',
        'foreign': '外资企业',
        'other': '其他'
      }
      return typeMap[type] || '未知'
    }
    
    // 获取企业规模文本
    const getCompanySizeText = (size) => {
      const sizeMap = {
        'small': '50人以下',
        'medium': '50-200人',
        'large': '200-500人',
        'xlarge': '500人以上'
      }
      return sizeMap[size] || '未知'
    }
    
    // 获取行业文本
    const getIndustryText = (industry) => {
      const industryMap = {
        'apparel': '服装纺织',
        'ecommerce': '电子商务',
        'retail': '批发零售',
        'manufacturing': '制造业',
        'other': '其他'
      }
      return industryMap[industry] || '未知'
    }
    
    // 格式化日期
    const formatDate = (date) => {
      if (!date) return ''
      return new Date(date).toLocaleDateString()
    }
    
    // 处理文件变化
    const handleFileChange = (file, fileList) => {
      // 这里可以添加文件验证逻辑
      console.log('文件变化:', file)
    }
    
    // 处理文件超出限制
    const handleExceed = (files, fileList) => {
      ElMessage.warning('最多只能上传一个文件')
    }
    
    // 查看认证信息
    const viewAuthInfo = () => {
      dialogTitle.value = '认证信息详情'
      dialogVisible.value = true
    }
    
    // 编辑认证信息
    const editAuthInfo = () => {
      dialogTitle.value = '修改认证信息'
      dialogVisible.value = true
    }
    
    // 提交认证
    const submitAuth = () => {
      if (authFormRef.value) {
        authFormRef.value.validate((valid) => {
          if (valid) {
            ElMessageBox.confirm('确定要提交认证申请吗？', '提交确认', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              // 模拟提交认证申请
              authInfo.status = 0
              authInfo.submitTime = new Date().toISOString()
              authInfo.auditTime = null
              authInfo.rejectReason = null
              
              ElMessage.success('认证申请已提交，我们将在3-5个工作日内完成审核')
              dialogVisible.value = false
            }).catch(() => {
              ElMessage.info('已取消提交')
            })
          } else {
            ElMessage.error('请完善认证信息')
            return false
          }
        })
      }
    }
    
    // 重置表单
    const resetForm = () => {
      if (authFormRef.value) {
        authFormRef.value.resetFields()
      }
    }
    
    return {
      authInfo,
      authForm,
      fileList,
      authFormRef,
      dialogVisible,
      dialogTitle,
      regionOptions,
      authFormRules,
      getAuthStatusText,
      getAuthStatusType,
      getCompanyTypeText,
      getCompanySizeText,
      getIndustryText,
      formatDate,
      handleFileChange,
      handleExceed,
      viewAuthInfo,
      editAuthInfo,
      submitAuth,
      resetForm
    }
  }
}
</script>

<style scoped>
.auth-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.auth-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.auth-description {
  color: #606266;
  font-size: 14px;
}

.auth-status-section {
  margin-bottom: 30px;
}

.status-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08),
              0 1px 3px rgba(0, 0, 0, 0.12);
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.status-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12),
              0 2px 8px rgba(0, 0, 0, 0.16);
  transform: translateY(-2px);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 自定义标签样式 */
.status-header .el-tag {
  border-radius: 8px;
  font-weight: 500;
  font-size: 16px;
  padding: 8px 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.auth-form-section {
  margin-top: 30px;
}

.form-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08),
              0 1px 3px rgba(0, 0, 0, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12),
              0 2px 8px rgba(0, 0, 0, 0.16);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.auth-form {
  max-width: 800px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(240, 240, 240, 0.8);
}

.form-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
  display: inline-block;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.form-row .el-form-item {
  flex: 1;
  margin-bottom: 0;
}

/* 表单组件美化 */
.auth-form .el-input__wrapper,
.auth-form .el-select__wrapper,
.auth-form .el-date-editor__wrapper,
.auth-form .el-textarea__wrapper {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.auth-form .el-input__wrapper:hover,
.auth-form .el-select__wrapper:hover,
.auth-form .el-date-editor__wrapper:hover,
.auth-form .el-textarea__wrapper:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.auth-form .el-input__wrapper.is-focus,
.auth-form .el-select__wrapper.is-focus,
.auth-form .el-date-editor__wrapper.is-focus,
.auth-form .el-textarea__wrapper.is-focus {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 文件上传区域设计 */
.upload-section {
  margin-top: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border: 2px dashed #409eff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.upload-section:hover {
  border-color: #67c23a;
  background: linear-gradient(135deg, #ecf5ff 0%, #f8f9fa 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.avatar-uploader {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  gap: 16px;
}

/* 自定义按钮样式 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
}

.form-actions .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border: none;
  border-radius: 8px;
  padding: 12px 32px;
  font-weight: 500;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #67c23a 0%, #409eff 100%);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
  transform: translateY(-2px);
}

.form-actions .el-button--primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.form-actions .el-button {
  border-radius: 8px;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* 对话框样式 */
.dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.dialog-attachments {
  margin-top: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.dialog-attachments h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
  display: inline-block;
}

.file-list {
  margin-top: 10px;
}

/* 描述列表样式 */
.dialog-content .el-descriptions {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.dialog-content .el-descriptions__header {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  color: white;
  padding: 16px;
}

.dialog-content .el-descriptions__body {
  background: white;
}

.dialog-content .el-descriptions-item {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(240, 240, 240, 0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dialog-content .el-descriptions-item:hover {
  background: rgba(64, 158, 255, 0.05);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .auth-container {
    padding: 10px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .status-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .el-descriptions {
    :deep(.el-descriptions__header),
    :deep(.el-descriptions__body) {
      display: flex;
      flex-direction: column;
    }
    
    :deep(.el-descriptions-item) {
      width: 100%;
    }
  }
  
  .form-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
}
</style>
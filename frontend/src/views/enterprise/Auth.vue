<template>
  <div class="auth-container">
    <div class="auth-header">
      <h2>企业认证与主页管理</h2>
      <p class="auth-description">完成企业认证，编辑企业主页，提升企业形象</p>
    </div>
    
    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="auth-tabs">
      <el-tab-pane label="企业认证" name="certification">
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
      </el-tab-pane>

      <!-- 企业主页编辑 -->
      <el-tab-pane label="企业主页" name="homepage">
        <div class="homepage-editor-section">
          <el-card shadow="hover" class="editor-card">
            <template #header>
              <div class="card-header">
                <span>企业主页编辑器</span>
                <el-button type="primary" @click="previewHomepage">
                  <el-icon><View /></el-icon> 预览主页
                </el-button>
              </div>
            </template>

            <el-form :model="homepageForm" label-position="top" class="homepage-form">
              <!-- 企业LOGO -->
              <div class="form-section">
                <h4>企业形象</h4>
                <div class="logo-upload-section">
                  <div class="logo-preview">
                    <el-avatar :size="120" :src="homepageForm.logo" shape="square">
                      <el-icon :size="40"><OfficeBuilding /></el-icon>
                    </el-avatar>
                    <el-upload
                      action="#"
                      :auto-upload="false"
                      :show-file-list="false"
                      accept="image/*"
                      @change="handleLogoChange"
                    >
                      <el-button size="small" type="primary" style="margin-top: 12px">
                        <el-icon><Upload /></el-icon> 上传LOGO
                      </el-button>
                    </el-upload>
                  </div>
                  <div class="banner-upload">
                    <label>企业封面图</label>
                    <div class="banner-preview" :style="{ backgroundImage: `url(${homepageForm.banner})` }">
                      <el-upload
                        action="#"
                        :auto-upload="false"
                        :show-file-list="false"
                        accept="image/*"
                        @change="handleBannerChange"
                      >
                        <el-button type="primary">
                          <el-icon><Upload /></el-icon> 上传封面图
                        </el-button>
                      </el-upload>
                    </div>
                    <p class="upload-tip">建议尺寸：1200 x 400 像素</p>
                  </div>
                </div>
              </div>

              <!-- 企业简介 -->
              <div class="form-section">
                <h4>企业简介</h4>
                <el-form-item label="企业口号/Slogan">
                  <el-input v-model="homepageForm.slogan" placeholder="请输入企业口号，如：创造时尚，引领潮流" maxlength="50" show-word-limit />
                </el-form-item>
                <el-form-item label="企业简介">
                  <div class="rich-editor">
                    <div class="editor-toolbar">
                      <el-button-group>
                        <el-button size="small" @click="formatText('bold')"><strong>B</strong></el-button>
                        <el-button size="small" @click="formatText('italic')"><em>I</em></el-button>
                        <el-button size="small" @click="formatText('underline')"><u>U</u></el-button>
                      </el-button-group>
                      <el-button-group style="margin-left: 12px">
                        <el-button size="small" @click="formatText('h2')">H2</el-button>
                        <el-button size="small" @click="formatText('h3')">H3</el-button>
                      </el-button-group>
                      <el-button-group style="margin-left: 12px">
                        <el-button size="small" @click="formatText('ul')">
                          <el-icon><List /></el-icon>
                        </el-button>
                        <el-button size="small" @click="formatText('ol')">
                          <el-icon><Rank /></el-icon>
                        </el-button>
                      </el-button-group>
                    </div>
                    <el-input
                      v-model="homepageForm.introduction"
                      type="textarea"
                      :rows="8"
                      placeholder="请输入企业简介，介绍企业的发展历程、主营业务、市场地位等..."
                    />
                  </div>
                </el-form-item>
              </div>

              <!-- 企业文化 -->
              <div class="form-section">
                <h4>企业文化</h4>
                <el-form-item label="企业愿景">
                  <el-input v-model="homepageForm.vision" type="textarea" :rows="3" placeholder="请输入企业愿景" />
                </el-form-item>
                <el-form-item label="企业使命">
                  <el-input v-model="homepageForm.mission" type="textarea" :rows="3" placeholder="请输入企业使命" />
                </el-form-item>
                <el-form-item label="核心价值观">
                  <div class="values-editor">
                    <el-tag
                      v-for="(value, index) in homepageForm.values"
                      :key="index"
                      closable
                      @close="removeValue(index)"
                      effect="plain"
                      size="large"
                    >
                      {{ value }}
                    </el-tag>
                    <el-input
                      v-if="valueInputVisible"
                      ref="valueInputRef"
                      v-model="valueInputValue"
                      size="small"
                      style="width: 120px"
                      @keyup.enter="addValue"
                      @blur="addValue"
                    />
                    <el-button v-else size="small" @click="showValueInput">
                      + 添加价值观
                    </el-button>
                  </div>
                </el-form-item>
              </div>

              <!-- 企业福利 -->
              <div class="form-section">
                <h4>企业福利</h4>
                <div class="benefits-grid">
                  <el-checkbox-group v-model="homepageForm.benefits">
                    <el-checkbox label="五险一金" />
                    <el-checkbox label="带薪年假" />
                    <el-checkbox label="年终奖金" />
                    <el-checkbox label="定期体检" />
                    <el-checkbox label="员工培训" />
                    <el-checkbox label="节日福利" />
                    <el-checkbox label="弹性工作" />
                    <el-checkbox label="免费班车" />
                    <el-checkbox label="员工食堂" />
                    <el-checkbox label="住房补贴" />
                    <el-checkbox label="交通补贴" />
                    <el-checkbox label="通讯补贴" />
                  </el-checkbox-group>
                </div>
                <el-form-item label="其他福利" style="margin-top: 16px">
                  <el-input v-model="homepageForm.otherBenefits" placeholder="请输入其他福利，多个福利用逗号分隔" />
                </el-form-item>
              </div>

              <!-- 企业相册 -->
              <div class="form-section">
                <h4>企业相册</h4>
                <el-upload
                  action="#"
                  list-type="picture-card"
                  :auto-upload="false"
                  :file-list="homepageForm.gallery"
                  :on-change="handleGalleryChange"
                  :on-remove="handleGalleryRemove"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
                <p class="upload-tip">上传企业环境、团队活动等照片，最多9张</p>
              </div>

              <!-- 联系方式 -->
              <div class="form-section">
                <h4>联系方式</h4>
                <div class="form-row">
                  <el-form-item label="官方网站">
                    <el-input v-model="homepageForm.website" placeholder="https://www.example.com">
                      <template #prepend>https://</template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="招聘邮箱">
                    <el-input v-model="homepageForm.hrEmail" placeholder="hr@example.com" />
                  </el-form-item>
                </div>
                <div class="form-row">
                  <el-form-item label="招聘热线">
                    <el-input v-model="homepageForm.hrPhone" placeholder="400-xxx-xxxx" />
                  </el-form-item>
                  <el-form-item label="微信公众号">
                    <el-input v-model="homepageForm.wechat" placeholder="请输入微信公众号名称" />
                  </el-form-item>
                </div>
              </div>

              <!-- 保存按钮 -->
              <div class="form-actions">
                <el-button type="primary" size="large" @click="saveHomepage">
                  <el-icon><Check /></el-icon> 保存主页信息
                </el-button>
                <el-button size="large" @click="resetHomepage">重置</el-button>
              </div>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
    
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
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 主页预览对话框 -->
    <el-dialog v-model="previewVisible" title="企业主页预览" width="900px" fullscreen>
      <div class="homepage-preview">
        <div class="preview-banner" :style="{ backgroundImage: `url(${homepageForm.banner || 'https://via.placeholder.com/1200x400'})` }">
          <div class="preview-company-info">
            <el-avatar :size="100" :src="homepageForm.logo" shape="square">
              <el-icon :size="40"><OfficeBuilding /></el-icon>
            </el-avatar>
            <div class="company-text">
              <h1>{{ authForm.companyName || '企业名称' }}</h1>
              <p class="slogan">{{ homepageForm.slogan || '企业口号' }}</p>
            </div>
          </div>
        </div>
        <div class="preview-content">
          <div class="preview-section">
            <h3>企业简介</h3>
            <p>{{ homepageForm.introduction || '暂无企业简介' }}</p>
          </div>
          <div class="preview-section" v-if="homepageForm.vision || homepageForm.mission">
            <h3>企业文化</h3>
            <div class="culture-grid">
              <div class="culture-item" v-if="homepageForm.vision">
                <h4>愿景</h4>
                <p>{{ homepageForm.vision }}</p>
              </div>
              <div class="culture-item" v-if="homepageForm.mission">
                <h4>使命</h4>
                <p>{{ homepageForm.mission }}</p>
              </div>
            </div>
            <div class="values-display" v-if="homepageForm.values.length">
              <h4>核心价值观</h4>
              <div class="values-tags">
                <el-tag v-for="v in homepageForm.values" :key="v" effect="dark" type="primary">{{ v }}</el-tag>
              </div>
            </div>
          </div>
          <div class="preview-section" v-if="homepageForm.benefits.length">
            <h3>企业福利</h3>
            <div class="benefits-display">
              <el-tag v-for="b in homepageForm.benefits" :key="b" effect="plain" size="large">{{ b }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, OfficeBuilding, Upload, Plus, Check, List, Rank } from '@element-plus/icons-vue'

const activeTab = ref('certification')

// 认证信息
const authInfo = reactive({
  status: 1, // 0: 待审核, 1: 通过, 2: 拒绝
  submitTime: '2023-11-15T10:30:00',
  auditTime: '2023-11-18T14:00:00',
  rejectReason: null
})

// 认证表单数据
const authForm = reactive({
  companyName: '时尚服饰有限公司',
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

// 企业主页表单
const homepageForm = reactive({
  logo: '',
  banner: '',
  slogan: '创造时尚，引领潮流',
  introduction: '时尚服饰有限公司成立于2015年，是一家专注于高端女装设计与生产的服装企业。公司拥有专业的设计团队和先进的生产设备，致力于为消费者提供高品质、时尚前沿的服装产品。\n\n经过多年发展，公司已成为国内知名的服装品牌，产品远销海内外，深受消费者喜爱。',
  vision: '成为中国最具影响力的时尚服装品牌',
  mission: '用设计创造美好生活，让每个人都能享受时尚',
  values: ['创新', '品质', '诚信', '共赢'],
  benefits: ['五险一金', '带薪年假', '年终奖金', '员工培训', '节日福利'],
  otherBenefits: '',
  gallery: [],
  website: 'www.fashion.com',
  hrEmail: 'hr@fashion.com',
  hrPhone: '400-888-8888',
  wechat: '时尚服饰官方'
})

// 文件列表
const fileList = ref([])

// 表单引用
const authFormRef = ref(null)

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('认证信息详情')
const previewVisible = ref(false)

// 价值观输入
const valueInputVisible = ref(false)
const valueInputValue = ref('')
const valueInputRef = ref(null)

// 地区选项
const regionOptions = ref([
  {
    value: '北京市', label: '北京市',
    children: [{ value: '北京市', label: '北京市', children: [
      { value: '东城区', label: '东城区' },
      { value: '西城区', label: '西城区' },
      { value: '朝阳区', label: '朝阳区' },
      { value: '海淀区', label: '海淀区' }
    ]}]
  },
  {
    value: '上海市', label: '上海市',
    children: [{ value: '上海市', label: '上海市', children: [
      { value: '黄浦区', label: '黄浦区' },
      { value: '徐汇区', label: '徐汇区' },
      { value: '静安区', label: '静安区' }
    ]}]
  }
])

// 表单验证规则
const authFormRules = {
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  creditCode: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }],
  companyType: [{ required: true, message: '请选择企业类型', trigger: 'change' }],
  companySize: [{ required: true, message: '请选择企业规模', trigger: 'change' }],
  industry: [{ required: true, message: '请选择所在行业', trigger: 'change' }],
  establishDate: [{ required: true, message: '请选择成立时间', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPosition: [{ required: true, message: '请输入联系人职位', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  contactEmail: [{ required: true, message: '请输入电子邮箱', trigger: 'blur' }],
  region: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

// 辅助函数
const getAuthStatusText = (status) => ({ 0: '待审核', 1: '审核通过', 2: '审核拒绝' }[status] || '未知')
const getAuthStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info')
const getCompanyTypeText = (type) => ({ limited: '有限责任公司', stock: '股份有限公司', individual: '个体工商户', foreign: '外资企业', other: '其他' }[type] || '未知')
const getCompanySizeText = (size) => ({ small: '50人以下', medium: '50-200人', large: '200-500人', xlarge: '500人以上' }[size] || '未知')
const getIndustryText = (industry) => ({ apparel: '服装纺织', ecommerce: '电子商务', retail: '批发零售', manufacturing: '制造业', other: '其他' }[industry] || '未知')
const formatDate = (date) => date ? new Date(date).toLocaleDateString() : ''

// 文件处理
const handleFileChange = (file) => { console.log('文件变化:', file) }
const handleExceed = () => { ElMessage.warning('最多只能上传一个文件') }
const handleLogoChange = (file) => {
  homepageForm.logo = URL.createObjectURL(file.raw)
  ElMessage.success('LOGO已更新')
}
const handleBannerChange = (file) => {
  homepageForm.banner = URL.createObjectURL(file.raw)
  ElMessage.success('封面图已更新')
}
const handleGalleryChange = (file, fileList) => {
  if (fileList.length > 9) {
    ElMessage.warning('最多上传9张图片')
    return
  }
  homepageForm.gallery = fileList
}
const handleGalleryRemove = (file, fileList) => {
  homepageForm.gallery = fileList
}

// 价值观编辑
const showValueInput = () => {
  valueInputVisible.value = true
  nextTick(() => { valueInputRef.value?.focus() })
}
const addValue = () => {
  if (valueInputValue.value && !homepageForm.values.includes(valueInputValue.value)) {
    homepageForm.values.push(valueInputValue.value)
  }
  valueInputVisible.value = false
  valueInputValue.value = ''
}
const removeValue = (index) => {
  homepageForm.values.splice(index, 1)
}

// 富文本格式化
const formatText = (format) => {
  ElMessage.info(`格式化功能：${format}`)
}

// 认证操作
const viewAuthInfo = () => { dialogVisible.value = true }
const editAuthInfo = () => { dialogVisible.value = true }
const submitAuth = () => {
  authFormRef.value?.validate((valid) => {
    if (valid) {
      ElMessageBox.confirm('确定要提交认证申请吗？', '提交确认', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        authInfo.status = 0
        ElMessage.success('认证申请已提交')
      }).catch(() => {})
    }
  })
}
const resetForm = () => { authFormRef.value?.resetFields() }

// 主页操作
const previewHomepage = () => { previewVisible.value = true }
const saveHomepage = () => { ElMessage.success('企业主页信息已保存') }
const resetHomepage = () => {
  ElMessageBox.confirm('确定要重置主页信息吗？', '确认重置', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    homepageForm.slogan = ''
    homepageForm.introduction = ''
    homepageForm.vision = ''
    homepageForm.mission = ''
    homepageForm.values = []
    homepageForm.benefits = []
    ElMessage.success('已重置')
  }).catch(() => {})
}
</script>

<style scoped>
.auth-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
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

.auth-tabs {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.auth-status-section {
  margin-bottom: 30px;
}

.status-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: all 0.3s;
}

.status-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
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

.form-card, .editor-card {
  border-radius: 12px;
  overflow: hidden;
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

.auth-form, .homepage-form {
  max-width: 900px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
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

.upload-section {
  margin-top: 20px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px dashed #409eff;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
}

/* 企业主页编辑器样式 */
.logo-upload-section {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.logo-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.banner-upload {
  flex: 1;
}

.banner-upload label {
  display: block;
  margin-bottom: 12px;
  font-weight: 500;
  color: #606266;
}

.banner-preview {
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
  background-position: center;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.rich-editor {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.values-editor {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

/* 预览对话框样式 */
.homepage-preview {
  background: #f5f7fa;
}

.preview-banner {
  height: 300px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: flex-end;
  padding: 40px;
}

.preview-company-info {
  display: flex;
  align-items: center;
  gap: 24px;
  color: #fff;
}

.preview-company-info h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
}

.preview-company-info .slogan {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.preview-content {
  padding: 40px;
  background: #fff;
}

.preview-section {
  margin-bottom: 32px;
}

.preview-section h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
  display: inline-block;
}

.culture-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.culture-item h4 {
  color: #409eff;
  margin-bottom: 8px;
}

.values-display h4 {
  margin-bottom: 12px;
}

.values-tags, .benefits-display {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .form-row { flex-direction: column; }
  .logo-upload-section { flex-direction: column; }
  .benefits-grid { grid-template-columns: repeat(2, 1fr); }
  .culture-grid { grid-template-columns: 1fr; }
}
</style>

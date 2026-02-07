<template>
  <div class="certification-container">
    <!-- 证书有效期提醒弹窗 -->
    <el-dialog v-model="expiryReminderVisible" title="证书有效期提醒" width="500px">
      <div class="expiry-reminder-content">
        <el-alert
          v-if="expiringCertificates.length > 0"
          title="您有证书即将过期"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>以下证书将在30天内过期，请及时续期：</p>
          </template>
        </el-alert>
        <div class="expiring-cert-list">
          <div v-for="cert in expiringCertificates" :key="cert.id" class="expiring-cert-item">
            <div class="cert-info-row">
              <el-icon class="cert-icon"><Medal /></el-icon>
              <div class="cert-details">
                <span class="cert-name">{{ cert.name }}</span>
                <span class="cert-number">{{ cert.certificateNo }}</span>
              </div>
            </div>
            <div class="cert-expiry">
              <el-tag :type="cert.remainingDays <= 7 ? 'danger' : 'warning'" size="small">
                剩余 {{ cert.remainingDays }} 天
              </el-tag>
              <span class="expiry-date">到期：{{ cert.expireDate }}</span>
            </div>
            <el-button type="primary" size="small" @click="renewCertificate(cert)">立即续期</el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="expiryReminderVisible = false">稍后处理</el-button>
        <el-button type="primary" @click="goToRenewalPage">查看全部证书</el-button>
      </template>
    </el-dialog>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>技能认证管理</h2>
          <div class="header-actions">
            <el-badge :value="expiringCertificates.length" :hidden="expiringCertificates.length === 0" class="expiry-badge">
              <el-button @click="checkExpiringCertificates" :loading="checkingExpiry">
                <el-icon><Bell /></el-icon> 有效期检查
              </el-button>
            </el-badge>
            <el-button type="primary" @click="applyCertification">申请认证</el-button>
          </div>
        </div>
      </template>
      
      <div class="certification-tabs">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="认证申请" name="application">
            <el-table :data="applications" style="width: 100%">
              <el-table-column prop="id" label="申请ID" width="100"></el-table-column>
              <el-table-column prop="standardName" label="认证项目" width="200"></el-table-column>
              <el-table-column prop="jobTypeLabel" label="岗位类型" width="120"></el-table-column>
              <el-table-column prop="levelLabel" label="等级" width="100"></el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="getStatusTagType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="认证材料" width="120">
                <template #default="scope">
                  <el-button
                    v-if="scope.row.practicalFileUrl"
                    type="primary"
                    link
                    @click="previewApplicationMaterial(scope.row)"
                  >
                    查看图片
                  </el-button>
                  <span v-else>未上传</span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="申请时间" width="180"></el-table-column>
              <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewApplication(scope.row)">查看详情</el-button>
                  <el-button
                    v-if="canUploadMaterial(scope.row)"
                    type="warning"
                    size="small"
                    @click="uploadPractical(scope.row)"
                  >
                    {{ scope.row.practicalFileUrl ? '更新图片' : '上传图片' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="我的证书" name="certificate">
            <div class="certificate-list">
              <el-card v-for="cert in certificates" :key="cert.id" shadow="hover" class="certificate-item">
                <div class="certificate-header">
                  <h3>{{ cert.name }}</h3>
                  <el-tag :type="cert.status === 1 ? 'success' : 'warning'">
                    {{ cert.status === 1 ? '有效' : '已过期' }}
                  </el-tag>
                </div>
                <div class="certificate-content">
                  <div class="cert-info">
                    <span class="label">证书编号：</span>
                    <span class="value">{{ cert.certificateNo }}</span>
                  </div>
                  <div class="cert-info">
                    <span class="label">认证项目：</span>
                    <span class="value">{{ cert.standardName }}</span>
                  </div>
                  <div class="cert-info">
                    <span class="label">岗位类型：</span>
                    <span class="value">{{ cert.jobType }}</span>
                  </div>
                  <div class="cert-info">
                    <span class="label">等级：</span>
                    <span class="value">{{ cert.level }}</span>
                  </div>
                  <div class="cert-info">
                    <span class="label">颁发日期：</span>
                    <span class="value">{{ cert.issueDate }}</span>
                  </div>
                  <div class="cert-info">
                    <span class="label">有效期至：</span>
                    <span class="value">{{ cert.expireDate }}</span>
                  </div>
                </div>
                <div class="certificate-actions">
                  <el-button type="primary" size="small" @click="downloadCertificate(cert)">下载证书</el-button>
                  <el-button type="success" size="small" @click="verifyCertificate(cert)">验证证书</el-button>
                  <el-button size="small" @click="shareCertificate(cert)">分享证书</el-button>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
          <el-tab-pane label="认证标准" name="standard">
            <el-table :data="standards" style="width: 100%">
              <el-table-column prop="id" label="标准ID" width="100"></el-table-column>
              <el-table-column prop="name" label="认证项目" width="200"></el-table-column>
              <el-table-column prop="jobTypeLabel" label="岗位类型" width="120"></el-table-column>
              <el-table-column prop="levelLabel" label="等级" width="100"></el-table-column>
              <el-table-column prop="description" label="描述"></el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewStandard(scope.row)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
    
    <!-- 申请认证对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请技能认证" width="600px" @close="handleApplyDialogClose">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="120px">
        <el-form-item label="岗位类型" prop="jobType">
          <el-select v-model="applyForm.jobType" placeholder="请选择岗位类型">
            <el-option label="设计师" value="designer"></el-option>
            <el-option label="打版师" value="patternmaker"></el-option>
            <el-option label="质检员" value="inspector"></el-option>
            <el-option label="导购" value="sales"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="认证等级" prop="level">
          <el-select v-model="applyForm.level" placeholder="请选择认证等级">
            <el-option label="初级" :value="1"></el-option>
            <el-option label="中级" :value="2"></el-option>
            <el-option label="高级" :value="3"></el-option>
            <el-option label="专家级" :value="4"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="认证项目" prop="standardId">
          <el-select v-model="applyForm.standardId" placeholder="请选择认证项目">
            <el-option
              v-for="standard in filteredStandards"
              :key="standard.id"
              :label="formatStandardOptionLabel(standard)"
              :value="standard.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input v-model="applyForm.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="认证图片" required>
          <el-upload
            ref="applyUploadRef"
            drag
            :auto-upload="false"
            :limit="1"
            :file-list="applyImageList"
            :on-change="handleApplyImageChange"
            :on-exceed="handleApplyImageExceed"
            accept=".jpg,.jpeg,.png,.webp"
            list-type="picture"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将图片拖到此处，或<em>点击上传图片</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                仅支持 JPG/JPEG/PNG/WEBP，图片不超过 10MB
              </div>
            </template>
          </el-upload>
          <div class="el-upload__tip" style="margin-top: 8px;">
            选择图片后，点击“提交申请并上传图片”完成提交。
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="applyDialogVisible = false" :disabled="applying">取消</el-button>
          <el-button type="primary" @click="submitApplication" :loading="applying">提交申请并上传图片</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 证书分享对话框 -->
    <el-dialog v-model="shareDialogVisible" title="分享证书" width="500px">
      <div class="share-dialog-content" v-if="currentShareCert">
        <div class="share-cert-preview">
          <div class="cert-preview-header">
            <el-icon class="cert-medal"><Medal /></el-icon>
            <div class="cert-preview-info">
              <h3>{{ currentShareCert.name }}</h3>
              <p>证书编号：{{ currentShareCert.certificateNo }}</p>
            </div>
          </div>
        </div>
        
        <div class="share-link-section">
          <h4>分享链接</h4>
          <div class="share-link-input">
            <el-input v-model="currentShareLink" readonly>
              <template #append>
                <el-button @click="copyShareLink">
                  <el-icon><CopyDocument /></el-icon> 复制
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
        
        <div class="share-social-section">
          <h4>分享到社交平台</h4>
          <div class="social-buttons">
            <el-button class="social-btn weibo" @click="shareToSocial('weibo')">
              <span class="social-icon">微博</span>
            </el-button>
            <el-button class="social-btn wechat" @click="shareToSocial('wechat')">
              <span class="social-icon">微信</span>
            </el-button>
            <el-button class="social-btn linkedin" @click="shareToSocial('linkedin')">
              <span class="social-icon">LinkedIn</span>
            </el-button>
            <el-button class="social-btn twitter" @click="shareToSocial('twitter')">
              <span class="social-icon">Twitter</span>
            </el-button>
          </div>
        </div>
        
        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 20px;"
        >
          分享链接可供他人验证您的证书真伪，有效期为30天。
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="shareDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 认证图片上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传认证图片" width="600px" @close="handleUploadDialogClose">
      <div v-if="currentUploadApp" class="upload-dialog-content">
        <div class="upload-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="认证项目">{{ currentUploadApp.standardName }}</el-descriptions-item>
            <el-descriptions-item label="岗位类型">{{ currentUploadApp.jobTypeLabel }}</el-descriptions-item>
            <el-descriptions-item label="认证等级">{{ currentUploadApp.levelLabel }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ currentUploadApp.createTime }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <el-alert
          title="上传要求"
          type="info"
          :closable="false"
          show-icon
          style="margin: 20px 0;"
        >
          <ul class="upload-requirements">
            <li>支持格式：JPG、JPEG、PNG、WEBP</li>
            <li>文件大小：不超过 10MB</li>
            <li>内容要求：上传能证明技能能力的图片材料</li>
          </ul>
        </el-alert>

        <el-form :model="uploadForm" label-width="100px">
          <el-form-item label="选择文件">
            <el-upload
              ref="uploadRef"
              class="upload-demo"
              drag
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-exceed="handleExceed"
              :file-list="fileList"
              accept=".jpg,.jpeg,.png,.webp"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将图片拖到此处，或<em>点击选择图片</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 JPG/JPEG/PNG/WEBP，图片不超过 10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item v-if="selectedFile" label="文件信息">
            <div class="file-info">
              <p><strong>文件名：</strong>{{ selectedFile.name }}</p>
              <p><strong>文件大小：</strong>{{ formatFileSize(selectedFile.size) }}</p>
              <p><strong>文件类型：</strong>{{ selectedFile.type || '未知' }}</p>
            </div>
          </el-form-item>

          <el-form-item v-if="uploadProgress > 0 && uploadProgress < 100" label="上传进度">
            <el-progress :percentage="uploadProgress" :status="uploadStatus"></el-progress>
          </el-form-item>
        </el-form>

        <div v-if="uploadedFileUrl" class="upload-success">
          <el-result icon="success" title="上传成功" sub-title="认证图片已成功上传，请等待管理员审核">
            <template #extra>
              <div class="uploaded-file-preview">
                <p>文件URL：{{ uploadedFileUrl }}</p>
                <el-button type="primary" @click="previewUploadedFile">预览图片</el-button>
              </div>
            </template>
          </el-result>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="uploadDialogVisible = false" :disabled="uploading">取消</el-button>
          <el-button type="primary" @click="confirmUpload" :loading="uploading" :disabled="!selectedFile">
            {{ uploading ? '上传中...' : '确认上传' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Medal, Bell, Share, CopyDocument, Link, UploadFilled } from '@element-plus/icons-vue'
import {
  getMyApplications,
  getMyCertificates,
  getCertificationStandards,
  submitNewApplication,
  getExpiringCertificates,
  renewCertificateRequest,
  getCertificateShareLink,
  uploadPracticalWork,
  getApplicationDetail,
  exportCertificateFile,
  verifyCertificateNumber
} from '../../api/certification'
import { getUserIdForPath } from '@/utils/auth'

export default {
  name: 'CertificationManage',
  components: { Medal, Bell, Share, CopyDocument, Link, UploadFilled },
  setup() {
    const activeTab = ref('application')
    const applyDialogVisible = ref(false)
    const applyFormRef = ref(null)
    const applyUploadRef = ref(null)
    const applyImageList = ref([])
    const applyImageFile = ref(null)
    const applying = ref(false)
    
    // 证书有效期提醒相关
    const expiryReminderVisible = ref(false)
    const expiringCertificates = ref([])
    const checkingExpiry = ref(false)
    
    // 证书分享相关
    const shareDialogVisible = ref(false)
    const currentShareCert = ref(null)
    const currentShareLink = ref('')

    // 实操作品上传相关
    const uploadDialogVisible = ref(false)
    const currentUploadApp = ref(null)
    const uploadRef = ref(null)
    const fileList = ref([])
    const selectedFile = ref(null)
    const uploadProgress = ref(0)
    const uploadStatus = ref('')
    const uploading = ref(false)
    const uploadedFileUrl = ref('')
    const uploadForm = reactive({})

    // 数据
    const applications = ref([])
    const certificates = ref([])
    const standards = ref([])
    
    const applyForm = reactive({
      jobType: '',
      level: '',
      standardId: '',
      description: ''
    })
    
    const applyRules = {
      jobType: [
        { required: true, message: '请选择岗位类型', trigger: 'change' }
      ],
      level: [
        { required: true, message: '请选择认证等级', trigger: 'change' }
      ],
      standardId: [
        { required: true, message: '请选择认证项目', trigger: 'change' }
      ]
    }

    const resetApplyImageState = () => {
      applyImageList.value = []
      applyImageFile.value = null
    }

    const resetApplyFormState = () => {
      applyForm.jobType = ''
      applyForm.level = ''
      applyForm.standardId = ''
      applyForm.description = ''
      applyFormRef.value?.clearValidate?.()
      resetApplyImageState()
    }

    const handleApplyDialogClose = () => {
      applying.value = false
      resetApplyFormState()
    }

    const handleApplyImageExceed = () => {
      ElMessage.warning('只能上传一张图片')
    }

    const handleApplyImageChange = (file) => {
      if (!file?.raw) return false
      const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
      const ext = (file.name || '').toLowerCase()
      const extAllowed = ext.endsWith('.jpg') || ext.endsWith('.jpeg') || ext.endsWith('.png') || ext.endsWith('.webp')
      if (!allowedTypes.includes(file.raw.type) && !extAllowed) {
        ElMessage.error('仅支持 JPG/JPEG/PNG/WEBP 图片')
        return false
      }
      const maxSize = 10 * 1024 * 1024
      if (file.raw.size > maxSize) {
        ElMessage.error('图片大小不能超过 10MB')
        return false
      }
      applyImageFile.value = file.raw
      applyImageList.value = [file]
      return true
    }

    const jobTypeLabelMap = {
      designer: '设计师',
      patternmaker: '打版师',
      inspector: '质检员',
      sales: '导购'
    }

    const levelLabelMap = {
      1: '初级',
      2: '中级',
      3: '高级',
      4: '专家级'
    }

    const normalizeJobType = (value) => {
      const raw = String(value || '').trim()
      const aliasMap = {
        designer: 'designer',
        patternmaker: 'patternmaker',
        inspector: 'inspector',
        sales: 'sales',
        '设计师': 'designer',
        '打版师': 'patternmaker',
        '质检员': 'inspector',
        '导购': 'sales'
      }
      return aliasMap[raw] || raw
    }

    const normalizeLevel = (value) => {
      const num = Number(value)
      return Number.isNaN(num) ? null : num
    }

    const mapStandardRecord = (standard) => ({
      ...standard,
      jobType: normalizeJobType(standard.type || standard.jobType),
      jobTypeLabel: jobTypeLabelMap[normalizeJobType(standard.type || standard.jobType)] || standard.type || standard.jobType,
      level: normalizeLevel(standard.level),
      levelLabel: levelLabelMap[normalizeLevel(standard.level)] || standard.level
    })

    const fetchStandards = async () => {
      const standardsRes = await getCertificationStandards({ page: 1, size: 100 })
      const standardRecords = standardsRes.data?.records || standardsRes.data || []
      standards.value = standardRecords.map(mapStandardRecord)
    }

    const filteredStandards = computed(() => {
      const selectedType = normalizeJobType(applyForm.jobType)
      const selectedLevel = applyForm.level !== '' ? normalizeLevel(applyForm.level) : null
      const byType = standards.value.filter((standard) => {
        const standardType = normalizeJobType(standard.jobType || standard.type)
        return selectedType ? standardType === selectedType : true
      })

      const exact = byType.filter((standard) => {
        if (selectedLevel === null) return true
        return normalizeLevel(standard.level) === selectedLevel
      })
      if (exact.length > 0) return exact

      if (byType.length > 0) return byType

      if (selectedLevel !== null) {
        const byLevel = standards.value.filter(
          standard => normalizeLevel(standard.level) === selectedLevel
        )
        if (byLevel.length > 0) return byLevel
      }

      return standards.value
    })

    const formatStandardOptionLabel = (standard) => {
      const typeText = standard.jobTypeLabel || jobTypeLabelMap[normalizeJobType(standard.jobType || standard.type)] || '-'
      const levelText = standard.levelLabel || levelLabelMap[normalizeLevel(standard.level)] || '-'
      return `${standard.name || '未命名项目'}（${typeText} / ${levelText}）`
    }

    const normalizeFileUrl = (fileUrl) => {
      if (!fileUrl) return ''
      if (/^https?:\/\//i.test(fileUrl)) return fileUrl
      return `${window.location.origin}${fileUrl.startsWith('/') ? '' : '/'}${fileUrl}`
    }

    const canUploadMaterial = (application) => Number(application?.status) !== 3

    const previewApplicationMaterial = (application) => {
      const url = normalizeFileUrl(application?.practicalFileUrl || '')
      if (!url) {
        ElMessage.warning('当前申请还未上传认证图片')
        return
      }
      window.open(url, '_blank')
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        0: '待提交图片',
        1: '待审核',
        2: '审核未通过',
        3: '审核通过',
        4: '审核未通过'
      }
      return statusMap[status] || '未知'
    }
    
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'info',
        1: 'warning',
        2: 'danger',
        3: 'success',
        4: 'danger'
      }
      return typeMap[status] || 'info'
    }

    // --- API 调用 ---
    const fetchAllData = async () => {
      try {
        const userId = Number(getUserIdForPath('/personal') || 0)
        const [applicationsRes, certificatesRes] = await Promise.all([
          getMyApplications({ userId }),
          getMyCertificates({ userId })
        ])
        await fetchStandards()

        const findStandardId = (certificationType, level) => {
          const targetType = normalizeJobType(certificationType)
          const targetLevel = normalizeLevel(level)
          const matched = standards.value.find(
            standard =>
              normalizeJobType(standard.type || standard.jobType) === targetType &&
              normalizeLevel(standard.level) === targetLevel
          )
          return matched?.id || null
        }

        applications.value = (applicationsRes.data || []).map((application) => ({
          id: application.id,
          standardId: findStandardId(application.certificationType, application.level),
          standardName: application.projectName || '未命名项目',
          jobTypeLabel: jobTypeLabelMap[normalizeJobType(application.certificationType)] || application.certificationType,
          levelLabel: levelLabelMap[application.level] || application.level,
          status: application.status,
          createTime: application.applyTime,
          updateTime: application.reviewTime,
          practicalFileUrl: application.practicalFileUrl || ''
        }))

        certificates.value = (certificatesRes.data || []).map((cert) => {
          const jobTypeLabel = jobTypeLabelMap[cert.certificationType] || cert.certificationType
          const levelLabel = levelLabelMap[cert.level] || cert.level
          return {
            ...cert,
            name: `${jobTypeLabel}${levelLabel ? `（${levelLabel}）` : ''}`,
            jobTypeLabel,
            levelLabel,
            certificateNo: cert.certificateNumber,
            standardName: cert.certificationType,
            status: cert.certificateStatus === '有效' ? 1 : 0
          }
        })
      } catch (error) {
        ElMessage.error('数据加载失败，请稍后重试。')
        console.error("Failed to fetch data:", error);
      }
    };
    
    const applyCertification = async () => {
      try {
        await fetchStandards()
      } catch (error) {
        ElMessage.error('加载认证标准失败，请稍后重试')
        return
      }
      if (standards.value.length === 0) {
        ElMessage.warning('暂无可申请的认证标准')
        return
      }
      resetApplyFormState()
      applyDialogVisible.value = true
    }
    
    const submitApplication = async () => {
      if (!applyFormRef.value) return;
      if (!applyImageFile.value) {
        ElMessage.warning('请先上传认证图片')
        return
      }
      await applyFormRef.value.validate(async (valid) => {
        if (valid) {
          applying.value = true
          try {
            const userId = Number(getUserIdForPath('/personal') || 0)
            const selectedStandard = standards.value.find(
              (standard) => String(standard.id) === String(applyForm.standardId)
            )
            const finalType = normalizeJobType(
              selectedStandard?.type || selectedStandard?.jobType || applyForm.jobType
            )
            const finalLevel = normalizeLevel(
              selectedStandard?.level ?? applyForm.level
            )
            if (!finalType || finalLevel === null) {
              ElMessage.error('认证项目信息不完整，请重新选择')
              return
            }
            const createResponse = await submitNewApplication({
              userId,
              certificationType: finalType,
              level: finalLevel,
              projectName: selectedStandard?.name || ''
            })
            let certificationId = createResponse?.data?.id
            if (!certificationId) {
              const latestRes = await getMyApplications({ userId })
              const latestList = latestRes?.data || []
              const matched = latestList
                .filter(item =>
                  normalizeJobType(item?.certificationType) === finalType &&
                  normalizeLevel(item?.level) === finalLevel
                )
                .sort((a, b) => Number(b?.id || 0) - Number(a?.id || 0))
              certificationId = matched[0]?.id
            }
            if (!certificationId) {
              throw new Error('未获取到申请ID')
            }
            await uploadPracticalWork(applyImageFile.value, certificationId)
            ElMessage.success('认证申请提交成功，图片已上传，待管理员审核')
            applyDialogVisible.value = false;
            resetApplyFormState()
            await fetchAllData(); // 重新加载数据
          } catch (error) {
            console.error('申请提交失败:', error)
            ElMessage.error(error?.message || '申请提交失败')
          } finally {
            applying.value = false
          }
        }
      });
    }
    
    const viewApplication = async (row) => {
      try {
        const response = await getApplicationDetail(row.id)
        const detail = response.data || {}
        const text = [
          `申请ID：${detail.id || row.id || '-'}`,
          `认证项目：${detail.projectName || row.standardName || '-'}`,
          `岗位类型：${jobTypeLabelMap[detail.certificationType] || row.jobTypeLabel || '-'}`,
          `认证等级：${levelLabelMap[detail.level] || row.levelLabel || '-'}`,
          `当前状态：${getStatusText(detail.status ?? row.status)}`,
          `认证图片：${(detail.practicalFileUrl || row.practicalFileUrl) ? '已上传' : '未上传'}`,
          `申请时间：${detail.applyTime || row.createTime || '-'}`,
          `审核时间：${detail.reviewTime || row.updateTime || '-'}`
        ].join('\n')
        ElMessageBox.alert(text, '申请详情', { confirmButtonText: '确定' })
      } catch (error) {
        console.error('获取申请详情失败:', error)
        ElMessage.error('获取申请详情失败，请稍后重试')
      }
    }
    
    const downloadCertificate = async (cert) => {
      if (!cert?.id) {
        ElMessage.warning('证书信息不完整，无法下载')
        return
      }
      try {
        const response = await exportCertificateFile(cert.id)
        const fileUrl = response.data
        if (!fileUrl) {
          ElMessage.error('未获取到证书下载地址')
          return
        }
        const downloadUrl = /^https?:\/\//i.test(fileUrl)
          ? fileUrl
          : `${window.location.origin}${fileUrl.startsWith('/') ? '' : '/'}${fileUrl}`
        window.open(downloadUrl, '_blank')
      } catch (error) {
        console.error('下载证书失败:', error)
        ElMessage.error('下载证书失败，请稍后重试')
      }
    }

    // --- 实操作品上传相关函数 ---
    const uploadPractical = (row) => {
      currentUploadApp.value = row
      uploadDialogVisible.value = true
      // 重置上传状态
      fileList.value = []
      selectedFile.value = null
      uploadProgress.value = 0
      uploadStatus.value = ''
      uploadedFileUrl.value = ''
    }

    const handleUploadDialogClose = () => {
      // 清理上传状态
      fileList.value = []
      selectedFile.value = null
      uploadProgress.value = 0
      uploadStatus.value = ''
      uploadedFileUrl.value = ''
      currentUploadApp.value = null
    }

    const handleFileChange = (file, uploadFiles) => {
      // 验证文件类型
      const allowedTypes = [
        'image/jpeg',
        'image/png',
        'image/webp'
      ]

      const allowedExtensions = ['.jpg', '.jpeg', '.png', '.webp']
      const fileExtension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase()

      if (!allowedTypes.includes(file.raw.type) && !allowedExtensions.includes(fileExtension)) {
        ElMessage.error('仅支持 JPG/JPEG/PNG/WEBP 图片')
        return false
      }

      // 验证文件大小（10MB）
      const maxSize = 10 * 1024 * 1024
      if (file.raw.size > maxSize) {
        ElMessage.error('图片大小不能超过 10MB')
        return false
      }

      selectedFile.value = file.raw
      fileList.value = [file]
      uploadedFileUrl.value = ''
      return true
    }

    const handleExceed = () => {
      ElMessage.warning('只能上传一张图片，请先删除已选择的图片')
    }

    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
    }

    const confirmUpload = async () => {
      if (!selectedFile.value || !currentUploadApp.value) {
        ElMessage.warning('请先选择要上传的图片')
        return
      }

      uploading.value = true
      uploadProgress.value = 0
      uploadStatus.value = ''

      try {
        const response = await uploadPracticalWork(
          selectedFile.value,
          currentUploadApp.value.id,
          (progress) => {
            uploadProgress.value = progress
          }
        )

        uploadedFileUrl.value = response?.data?.url || response?.data?.data?.url || ''
        uploadStatus.value = 'success'
        ElMessage.success('认证图片上传成功，等待管理员审核')
        await fetchAllData()
      } catch (error) {
        uploadStatus.value = 'exception'
        ElMessage.error(error.response?.data?.message || '上传图片失败，请稍后重试')
      } finally {
        uploading.value = false
      }
    }

    const previewUploadedFile = () => {
      if (uploadedFileUrl.value) {
        window.open(uploadedFileUrl.value, '_blank')
      }
    }

    const verifyCertificate = async (cert) => {
      if (!cert?.certificateNo) {
        ElMessage.warning('证书编号缺失，无法验证')
        return
      }
      try {
        const response = await verifyCertificateNumber(cert.certificateNo)
        const result = response.data || {}
        if (result.valid) {
          ElMessage.success(result.message || '证书验证通过')
        } else {
          ElMessage.warning(result.message || '证书验证未通过')
        }
      } catch (error) {
        console.error('验证证书失败:', error)
        ElMessage.error('验证证书失败，请稍后重试')
      }
    }
    
    const shareCertificate = async (cert) => {
      try {
        const response = await getCertificateShareLink(cert.id)
        const shareLink = response.data
        if (!shareLink) {
          ElMessage.error('未获取到分享链接')
          return
        }
        showShareDialog(cert, shareLink)
      } catch (error) {
        ElMessage.error('获取分享链接失败')
      }
    }
    
    const showShareDialog = (cert, shareLink) => {
      shareDialogVisible.value = true
      currentShareCert.value = cert
      currentShareLink.value = shareLink
    }
    
    const copyShareLink = async () => {
      try {
        await navigator.clipboard.writeText(currentShareLink.value)
        ElMessage.success('链接已复制到剪贴板')
      } catch (err) {
        ElMessage.error('复制失败，请手动复制')
      }
    }
    
    const shareToSocial = (platform) => {
      const cert = currentShareCert.value
      if (!cert) return;
      const shareText = `我获得了${cert.name}认证！证书编号：${cert.certificateNo}`
      const shareUrl = currentShareLink.value
      
      let url = ''
      switch (platform) {
        case 'weibo':
          url = `https://service.weibo.com/share/share.php?url=${encodeURIComponent(shareUrl)}&title=${encodeURIComponent(shareText)}`
          break
        case 'wechat':
          ElMessage.info('请使用微信扫描二维码分享')
          return
        case 'linkedin':
          url = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(shareUrl)}`
          break
        case 'twitter':
          url = `https://twitter.com/intent/tweet?text=${encodeURIComponent(shareText)}&url=${encodeURIComponent(shareUrl)}`
          break
      }
      
      if (url) {
        window.open(url, '_blank', 'width=600,height=400')
      }
    }
    
    const viewStandard = (standard) => {
      const text = [
        `标准ID：${standard.id || '-'}`,
        `认证项目：${standard.name || '-'}`,
        `岗位类型：${standard.jobTypeLabel || standard.jobType || '-'}`,
        `认证等级：${standard.levelLabel || standard.level || '-'}`,
        `说明：${standard.description || '-'}`
      ].join('\n')
      ElMessageBox.alert(text, '认证标准详情', { confirmButtonText: '确定' })
    }
    
    const checkExpiringCertificates = async () => {
      checkingExpiry.value = true
      try {
        const response = await getExpiringCertificates({
          userId: Number(getUserIdForPath('/personal') || 0),
          daysBeforeExpire: 30
        })
        if (response.data && response.data.length > 0) {
          expiringCertificates.value = response.data.map(cert => {
            const jobTypeLabel = jobTypeLabelMap[cert.certificationType] || cert.certificationType
            const levelLabel = levelLabelMap[cert.level] || cert.level
            return {
              ...cert,
              name: cert.name || `${jobTypeLabel}${levelLabel ? `（${levelLabel}）` : ''}`,
              certificateNo: cert.certificateNumber,
              jobTypeLabel,
              levelLabel,
              remainingDays: Math.ceil((new Date(cert.expireDate) - new Date()) / (1000 * 60 * 60 * 24))
            }
          })
          expiryReminderVisible.value = true
        } else {
          ElMessage.success('所有证书状态正常')
        }
      } catch (error) {
        ElMessage.error('检查证书有效期失败')
      } finally {
        checkingExpiry.value = false
      }
    }
    
    const renewCertificate = async (cert) => {
      try {
        await renewCertificateRequest({ certificateId: cert.id, years: 1 })
        ElMessage.success('证书续期申请已提交')
        expiryReminderVisible.value = false
        await checkExpiringCertificates(); // 重新检查
      } catch (error) {
        ElMessage.error('续期失败')
      }
    }
    
    const goToRenewalPage = () => {
      activeTab.value = 'certificate'
      expiryReminderVisible.value = false
    }
    
    onMounted(() => {
      fetchAllData();
      checkExpiringCertificates();
    })

    watch(
      () => [applyForm.jobType, applyForm.level],
      () => {
        applyForm.standardId = ''
      }
    )

    watch(
      () => activeTab.value,
      async (tab) => {
        if (tab === 'standard') {
          try {
            await fetchStandards()
          } catch (error) {
            ElMessage.error('刷新认证标准失败')
          }
        }
      }
    )
    
    return {
      activeTab,
      applyDialogVisible,
      applyFormRef,
      applyUploadRef,
      applyImageList,
      applying,
      applyForm,
      applyRules,
      filteredStandards,
      formatStandardOptionLabel,
      handleApplyDialogClose,
      handleApplyImageChange,
      handleApplyImageExceed,
      applications,
      certificates,
      standards,
      getStatusText,
      getStatusTagType,
      applyCertification,
      submitApplication,
      viewApplication,
      canUploadMaterial,
      previewApplicationMaterial,
      uploadPractical,
      downloadCertificate,
      verifyCertificate,
      shareCertificate,
      viewStandard,
      expiryReminderVisible,
      expiringCertificates,
      checkingExpiry,
      checkExpiringCertificates,
      renewCertificate,
      goToRenewalPage,
      shareDialogVisible,
      currentShareCert,
      currentShareLink,
      copyShareLink,
      shareToSocial,
      // 实操作品上传相关
      uploadDialogVisible,
      currentUploadApp,
      uploadRef,
      fileList,
      selectedFile,
      uploadProgress,
      uploadStatus,
      uploading,
      uploadedFileUrl,
      uploadForm,
      handleUploadDialogClose,
      handleFileChange,
      handleExceed,
      formatFileSize,
      confirmUpload,
      previewUploadedFile
    }
  }
}
</script>

<style scoped>
.certification-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.certification-tabs {
  margin-top: 20px;
}

.certificate-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.certificate-item {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.certificate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.certificate-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.certificate-content {
  flex: 1;
}

.cert-info {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.cert-info .label {
  font-weight: bold;
  margin-right: 10px;
  color: #606266;
}

.cert-info .value {
  color: #303133;
}

.certificate-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 证书有效期提醒样式 */
.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.expiry-badge :deep(.el-badge__content) {
  background-color: #f56c6c;
}

.expiry-reminder-content {
  padding: 10px 0;
}

.expiring-cert-list {
  margin-top: 20px;
}

.expiring-cert-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  border-radius: 12px;
  margin-bottom: 12px;
  border: 1px solid #ffeeba;
}

.cert-info-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cert-icon {
  font-size: 24px;
  color: #e6a23c;
}

.cert-details {
  display: flex;
  flex-direction: column;
}

.cert-name {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.cert-number {
  font-size: 12px;
  color: #909399;
}

.cert-expiry {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.expiry-date {
  font-size: 12px;
  color: #909399;
}

/* 证书分享对话框样式 */
.share-dialog-content {
  padding: 10px 0;
}

.share-cert-preview {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.cert-preview-header {
  display: flex;
  align-items: center;
  gap: 16px;
  color: white;
}

.cert-medal {
  font-size: 48px;
  color: #ffd700;
}

.cert-preview-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
}

.cert-preview-info p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.share-link-section, .share-social-section {
  margin-bottom: 20px;
}

.share-link-section h4, .share-social-section h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.share-link-input {
  display: flex;
  gap: 10px;
}

.social-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.social-btn {
  flex: 1;
  min-width: 100px;
  border-radius: 8px;
  font-weight: 500;
}

.social-btn.weibo {
  background: #e6162d;
  border-color: #e6162d;
  color: white;
}

.social-btn.weibo:hover {
  background: #cc1428;
}

.social-btn.wechat {
  background: #07c160;
  border-color: #07c160;
  color: white;
}

.social-btn.wechat:hover {
  background: #06ad56;
}

.social-btn.linkedin {
  background: #0077b5;
  border-color: #0077b5;
  color: white;
}

.social-btn.linkedin:hover {
  background: #006699;
}

.social-btn.twitter {
  background: #1da1f2;
  border-color: #1da1f2;
  color: white;
}

.social-btn.twitter:hover {
  background: #1a91da;
}

/* 实操作品上传对话框样式 */
.upload-dialog-content {
  padding: 10px 0;
}

.upload-info {
  margin-bottom: 20px;
}

.upload-requirements {
  margin: 0;
  padding-left: 20px;
}

.upload-requirements li {
  margin-bottom: 4px;
  color: #606266;
}

.upload-demo {
  width: 100%;
}

.file-info {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
}

.file-info p {
  margin: 4px 0;
  color: #606266;
}

.file-info strong {
  color: #303133;
}

.upload-success {
  margin-top: 20px;
}

.uploaded-file-preview {
  text-align: center;
}

.uploaded-file-preview p {
  margin-bottom: 12px;
  color: #909399;
  word-break: break-all;
}
</style>

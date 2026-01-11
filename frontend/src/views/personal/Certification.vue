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
              <el-table-column prop="jobType" label="岗位类型" width="120"></el-table-column>
              <el-table-column prop="level" label="等级" width="100"></el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="getStatusTagType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="申请时间" width="180"></el-table-column>
              <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewApplication(scope.row)">查看详情</el-button>
                  <el-button v-if="scope.row.status === 1" type="success" size="small" @click="startExam(scope.row)">开始考试</el-button>
                  <el-button v-if="scope.row.status === 2" type="warning" size="small" @click="uploadPractical(scope.row)">上传实操</el-button>
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
              <el-table-column prop="jobType" label="岗位类型" width="120"></el-table-column>
              <el-table-column prop="level" label="等级" width="100"></el-table-column>
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
    <el-dialog v-model="applyDialogVisible" title="申请技能认证" width="600px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="120px">
        <el-form-item label="岗位类型" prop="jobType">
          <el-select v-model="applyForm.jobType" placeholder="请选择岗位类型">
            <el-option label="设计师" value="设计师"></el-option>
            <el-option label="打版师" value="打版师"></el-option>
            <el-option label="质检员" value="质检员"></el-option>
            <el-option label="导购" value="导购"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="认证等级" prop="level">
          <el-select v-model="applyForm.level" placeholder="请选择认证等级">
            <el-option label="初级" value="初级"></el-option>
            <el-option label="中级" value="中级"></el-option>
            <el-option label="高级" value="高级"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="认证项目" prop="standardId">
          <el-select v-model="applyForm.standardId" placeholder="请选择认证项目">
            <el-option
              v-for="standard in standards"
              :key="standard.id"
              :label="standard.name"
              :value="standard.id"
              v-if="standard.jobType === applyForm.jobType && standard.level === applyForm.level"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input v-model="applyForm.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApplication">提交申请</el-button>
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
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Medal, Bell, Share, CopyDocument, Link } from '@element-plus/icons-vue'
import { 
  getMyApplications, 
  getMyCertificates, 
  getCertificationStandards,
  submitNewApplication,
  getExpiringCertificates,
  renewCertificateRequest,
  getCertificateShareLink
} from '../../api/certification'

export default {
  name: 'CertificationManage',
  components: { Medal, Bell, Share, CopyDocument, Link },
  setup() {
    const router = useRouter()
    const activeTab = ref('application')
    const applyDialogVisible = ref(false)
    const applyFormRef = ref(null)
    
    // 证书有效期提醒相关
    const expiryReminderVisible = ref(false)
    const expiringCertificates = ref([])
    const checkingExpiry = ref(false)
    
    // 证书分享相关
    const shareDialogVisible = ref(false)
    const currentShareCert = ref(null)
    const currentShareLink = ref('')
    
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
    
    const getStatusText = (status) => {
      const statusMap = {
        0: '待审核',
        1: '待考试',
        2: '待评审',
        3: '通过',
        4: '不通过'
      }
      return statusMap[status] || '未知'
    }
    
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'info',
        1: 'warning',
        2: 'warning',
        3: 'success',
        4: 'danger'
      }
      return typeMap[status] || 'info'
    }

    // --- API 调用 ---
    const fetchAllData = async () => {
      try {
        const [applicationsRes, certificatesRes, standardsRes] = await Promise.all([
          getMyApplications(),
          getMyCertificates(),
          getCertificationStandards()
        ]);
        applications.value = applicationsRes.data || [];
        certificates.value = certificatesRes.data || [];
        standards.value = standardsRes.data || [];
      } catch (error) {
        ElMessage.error('数据加载失败，请稍后重试。')
        console.error("Failed to fetch data:", error);
      }
    };
    
    const applyCertification = () => {
      applyDialogVisible.value = true
    }
    
    const submitApplication = async () => {
      if (!applyFormRef.value) return;
      await applyFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            await submitNewApplication(applyForm);
            ElMessage.success('认证申请提交成功，待审核');
            applyDialogVisible.value = false;
            await fetchAllData(); // 重新加载数据
          } catch (error) {
            ElMessage.error('申请提交失败');
          }
        }
      });
    }
    
    const viewApplication = (row) => {
      // TODO: 查看认证申请详情
      ElMessage.info('查看详情功能开发中')
    }
    
    const startExam = (row) => {
      router.push({
        path: '/personal/exam',
        query: {
          applicationId: row.id,
          examId: row.standardId || 1
        }
      })
    }
    
    const uploadPractical = (row) => {
      // TODO: 上传实操作品
      ElMessage.info('上传实操功能开发中')
    }
    
    const downloadCertificate = (cert) => {
      // TODO: 下载证书
      ElMessage.info('下载证书功能开发中')
    }
    
    const verifyCertificate = (cert) => {
      // TODO: 验证证书
      ElMessage.info('验证证书功能开发中')
    }
    
    const shareCertificate = async (cert) => {
      try {
        const response = await getCertificateShareLink(cert.id)
        const shareLink = response.data?.shareLink || `https://apparelcert.com/verify/${cert.certificateNo}`
        showShareDialog(cert, shareLink)
      } catch (error) {
        ElMessage.error('获取分享链接失败')
        // 作为备用，仍然可以显示一个模拟/占位链接
        const fallbackLink = `https://apparelcert.com/verify/${cert.certificateNo}`
        showShareDialog(cert, fallbackLink)
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
      ElMessage.info('查看认证标准详情功能开发中')
    }
    
    const checkExpiringCertificates = async () => {
      checkingExpiry.value = true
      try {
        const response = await getExpiringCertificates(30)
        if (response.data && response.data.length > 0) {
          expiringCertificates.value = response.data.map(cert => ({
            ...cert,
            remainingDays: Math.ceil((new Date(cert.expireDate) - new Date()) / (1000 * 60 * 60 * 24))
          }))
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
    
    return {
      activeTab,
      applyDialogVisible,
      applyFormRef,
      applyForm,
      applyRules,
      applications,
      certificates,
      standards,
      getStatusText,
      getStatusTagType,
      applyCertification,
      submitApplication,
      viewApplication,
      startExam,
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
      shareToSocial
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
</style>

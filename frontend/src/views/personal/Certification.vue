<template>
  <div class="certification-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>技能认证管理</h2>
          <el-button type="primary" @click="applyCertification">申请认证</el-button>
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
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'CertificationManage',
  setup() {
    const activeTab = ref('application')
    const applyDialogVisible = ref(false)
    const applyFormRef = ref(null)
    
    const applications = ref([
      {
        id: 1,
        standardName: '服装设计师初级认证',
        jobType: '设计师',
        level: '初级',
        status: 3,
        createTime: '2024-01-15 10:30:00',
        updateTime: '2024-01-20 14:20:00'
      },
      {
        id: 2,
        standardName: '服装设计师中级认证',
        jobType: '设计师',
        level: '中级',
        status: 1,
        createTime: '2024-02-10 09:15:00',
        updateTime: '2024-02-10 09:15:00'
      }
    ])
    
    const certificates = ref([
      {
        id: 1,
        name: '服装设计师初级证书',
        certificateNo: 'AC-2024-0001',
        standardName: '服装设计师初级认证',
        jobType: '设计师',
        level: '初级',
        issueDate: '2024-01-20',
        expireDate: '2027-01-20',
        status: 1
      }
    ])
    
    const standards = ref([
      {
        id: 1,
        name: '服装设计师初级认证',
        jobType: '设计师',
        level: '初级',
        description: '针对服装设计师的初级技能认证，考核设计基础理论和基本设计能力'
      },
      {
        id: 2,
        name: '服装设计师中级认证',
        jobType: '设计师',
        level: '中级',
        description: '针对服装设计师的中级技能认证，考核设计创新能力和项目实践经验'
      },
      {
        id: 3,
        name: '服装设计师高级认证',
        jobType: '设计师',
        level: '高级',
        description: '针对服装设计师的高级技能认证，考核设计理念和品牌策划能力'
      }
    ])
    
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
    
    const applyCertification = () => {
      applyDialogVisible.value = true
    }
    
    const submitApplication = () => {
      // TODO: 调用提交认证申请接口
      applyDialogVisible.value = false
      ElMessage.success('认证申请提交成功，待审核')
    }
    
    const viewApplication = (row) => {
      // TODO: 查看认证申请详情
      ElMessage.info('查看详情功能开发中')
    }
    
    const startExam = (row) => {
      // TODO: 开始理论考试
      ElMessage.info('开始考试功能开发中')
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
    
    const shareCertificate = (cert) => {
      // TODO: 分享证书
      ElMessage.info('分享证书功能开发中')
    }
    
    const viewStandard = (standard) => {
      // TODO: 查看认证标准详情
      ElMessage.info('查看认证标准详情功能开发中')
    }
    
    return {
      activeTab,
      applyDialogVisible,
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
      viewStandard
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
</style>

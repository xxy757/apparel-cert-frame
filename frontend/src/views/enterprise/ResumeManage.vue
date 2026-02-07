<template>
  <div class="resume-manage-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>简历管理</h2>
        </div>
      </template>
      
      <div class="resume-filters">
        <el-form :inline="true" :model="searchForm" label-width="100px">
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="请输入姓名"></el-input>
          </el-form-item>
          <el-form-item label="职位">
            <el-select v-model="searchForm.position" placeholder="请选择职位">
              <el-option label="全部" value=""></el-option>
              <el-option v-for="job in jobs" :key="job.id" :label="job.title" :value="job.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value=""></el-option>
              <el-option label="已投递" value="0"></el-option>
              <el-option label="已查看" value="1"></el-option>
              <el-option label="面试邀请" value="2"></el-option>
              <el-option label="已拒绝" value="3"></el-option>
              <el-option label="已通过" value="4"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="技能认证">
            <el-select v-model="searchForm.certification" placeholder="请选择技能认证">
              <el-option label="全部" value=""></el-option>
              <el-option label="设计师初级" value="1"></el-option>
              <el-option label="设计师中级" value="2"></el-option>
              <el-option label="打版师初级" value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchResumes">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="resumes" style="width: 100%">
        <el-table-column prop="id" label="投递ID" width="100"></el-table-column>
        <el-table-column prop="userName" label="姓名"></el-table-column>
        <el-table-column prop="positionName" label="投递职位" width="150"></el-table-column>
        <el-table-column prop="careerDirection" label="职业方向" width="120"></el-table-column>
        <el-table-column prop="education" label="学历" width="100"></el-table-column>
        <el-table-column prop="workExperience" label="工作经验" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="投递时间" width="180">
          <template #default="scope">
            <span class="time-text">{{ scope.row.createTimeDisplay }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button class="action-btn" type="primary" size="small" @click="viewResume(scope.row)">查看简历</el-button>
              <el-button
                v-if="scope.row.status === 0"
                class="action-btn"
                type="warning"
                size="small"
                @click="markAsViewed(scope.row)"
              >
                标记已查看
              </el-button>
              <el-button
                v-if="scope.row.status <= 1"
                class="action-btn"
                type="success"
                size="small"
                @click="openInterviewDialog(scope.row)"
              >
                发送面试邀请
              </el-button>
              <el-button
                v-if="scope.row.status <= 1"
                class="action-btn"
                type="danger"
                size="small"
                @click="rejectResume(scope.row)"
              >
                拒绝
              </el-button>
              <el-button
                v-if="scope.row.status === 3"
                class="action-btn"
                type="warning"
                size="small"
                @click="restoreResume(scope.row)"
              >
                恢复
              </el-button>
              <el-button class="action-btn" type="info" size="small" @click="viewInterview(scope.row)">面试详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalResumes"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 简历详情对话框 -->
    <el-dialog v-model="resumeDetailDialogVisible" title="简历详情" width="800px">
      <div v-if="selectedResume" class="resume-detail">
        <div class="resume-header">
          <div class="avatar-section" v-if="selectedResume.avatar">
            <el-avatar :size="80" :src="selectedResume.avatar"></el-avatar>
          </div>
          <h3>{{ selectedResume.userName }}</h3>
          <div class="resume-meta">
            <span class="meta-item" v-if="selectedResume.careerDirection">{{ selectedResume.careerDirection }}</span>
            <span class="meta-item" v-if="selectedResume.education">{{ selectedResume.education }}</span>
            <span class="meta-item" v-if="selectedResume.workExperience">{{ selectedResume.workExperience }}</span>
            <span class="meta-item" v-if="selectedResume.location">{{ selectedResume.location }}</span>
          </div>
          <div class="contact-info">
            <span v-if="selectedResume.phone">{{ selectedResume.phone }}</span>
            <span v-if="selectedResume.phone && selectedResume.email"> | </span>
            <span v-if="selectedResume.email">{{ selectedResume.email }}</span>
          </div>
          <div class="salary-info" v-if="selectedResume.expectedSalary">
            <span>期望薪资：{{ selectedResume.expectedSalary }}</span>
          </div>
        </div>

        <div class="resume-section" v-if="selectedResume.careerObjective">
          <h4>职业目标</h4>
          <p>{{ selectedResume.careerObjective }}</p>
        </div>

        <div class="resume-section" v-if="selectedResume.educationList && selectedResume.educationList.length > 0">
          <h4>教育背景</h4>
          <div v-for="(edu, index) in selectedResume.educationList" :key="index" class="edu-item">
            <div class="edu-header">
              <span class="school">{{ edu.school }}</span>
              <span class="period">{{ edu.startDate }} - {{ edu.endDate || '至今' }}</span>
            </div>
            <div class="edu-details">
              <span class="major" v-if="edu.major">{{ edu.major }}</span>
              <span class="degree" v-if="edu.degree">{{ edu.degree }}</span>
            </div>
            <p class="edu-desc" v-if="edu.description">{{ edu.description }}</p>
          </div>
        </div>

        <div class="resume-section" v-if="selectedResume.workExperienceList && selectedResume.workExperienceList.length > 0">
          <h4>工作经历</h4>
          <div v-for="(work, index) in selectedResume.workExperienceList" :key="index" class="work-item">
            <div class="work-header">
              <span class="company">{{ work.company }}</span>
              <span class="period">{{ work.startDate }} - {{ work.endDate || '至今' }}</span>
            </div>
            <div class="work-details">
              <span class="position" v-if="work.position">{{ work.position }}</span>
            </div>
            <p class="work-desc" v-if="work.description">{{ work.description }}</p>
            <ul class="work-responsibilities" v-if="work.responsibilities && work.responsibilities.length > 0">
              <li v-for="(resp, respIndex) in work.responsibilities" :key="respIndex">{{ resp }}</li>
            </ul>
          </div>
        </div>

        <div class="resume-section" v-if="selectedResume.projectExperienceList && selectedResume.projectExperienceList.length > 0">
          <h4>项目经验</h4>
          <div v-for="(project, index) in selectedResume.projectExperienceList" :key="index" class="project-item">
            <div class="project-header">
              <span class="project-name">{{ project.name }}</span>
              <span class="period" v-if="project.startDate">{{ project.startDate }} - {{ project.endDate || '至今' }}</span>
            </div>
            <div class="project-role" v-if="project.role">
              <span>角色：{{ project.role }}</span>
            </div>
            <p class="project-desc" v-if="project.description">{{ project.description }}</p>
          </div>
        </div>

        <div class="resume-section" v-if="selectedResume.skills && selectedResume.skills.length > 0">
          <h4>专业技能</h4>
          <div class="skills-list">
            <el-tag v-for="(skill, index) in selectedResume.skills" :key="index" size="small" class="skill-item">
              {{ typeof skill === 'string' ? skill : (skill.name || skill.skill) }}
              <span v-if="skill.level"> - {{ skill.level }}</span>
            </el-tag>
          </div>
        </div>

        <div class="resume-section" v-if="selectedResume.certificates && selectedResume.certificates.length > 0">
          <h4>证书资质</h4>
          <div class="certificates">
            <el-tag v-for="(cert, index) in selectedResume.certificates" :key="index" size="small" type="success" class="cert-item">
              {{ cert.name }}
              <span v-if="cert.date"> ({{ cert.date }})</span>
            </el-tag>
          </div>
        </div>

        <div class="resume-empty" v-if="!selectedResume.educationList?.length && !selectedResume.workExperienceList?.length && !selectedResume.skills?.length">
          <el-empty description="该用户暂未完善简历信息"></el-empty>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resumeDetailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 面试邀请对话框 -->
    <el-dialog v-model="interviewDialogVisible" title="发送面试邀请" width="600px">
      <el-form ref="interviewFormRef" :model="interviewForm" :rules="interviewRules" label-width="100px">
        <el-form-item label="职位">
          <el-input v-model="interviewForm.position" readonly></el-input>
        </el-form-item>
        <el-form-item label="候选人">
          <el-input v-model="interviewForm.candidateName" readonly></el-input>
        </el-form-item>
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="interviewForm.interviewTime"
            type="datetime"
            placeholder="请选择面试时间"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="面试地点" prop="interviewLocation">
          <el-input v-model="interviewForm.interviewLocation" placeholder="请输入面试地点"></el-input>
        </el-form-item>
        <el-form-item label="面试形式" prop="interviewType">
          <el-select v-model="interviewForm.interviewType" placeholder="请选择面试形式">
            <el-option label="现场面试" value="现场面试"></el-option>
            <el-option label="视频面试" value="视频面试"></el-option>
            <el-option label="电话面试" value="电话面试"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="面试官" prop="interviewer">
          <el-input v-model="interviewForm.interviewer" placeholder="请输入面试官姓名"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="interviewForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="interviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="sendInterview">发送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getJobApplications,
  updateApplicationStatus,
  sendInterviewInvitation as sendInterviewInvitationApi,
  getCurrentUser,
  getEnterpriseJobs,
  getEnterpriseInterviews,
  getResumeByUserId
} from '@/api/enterprise'

export default {
  name: 'ResumeManage',
  setup() {
    const resumeDetailDialogVisible = ref(false)
    const interviewDialogVisible = ref(false)
    const selectedResume = ref(null)
    const interviewFormRef = ref(null)
    
    const searchForm = reactive({
      name: '',
      position: '',
      status: '',
      certification: ''
    })

    const loading = ref(false)
    const enterpriseId = ref(null)
    
    const interviewForm = reactive({
      position: '',
      candidateName: '',
      interviewTime: '',
      interviewLocation: '',
      interviewType: '',
      interviewer: '',
      remark: ''
    })
    
    const interviewRules = {
      interviewTime: [
        { required: true, message: '请选择面试时间', trigger: 'change' }
      ],
      interviewLocation: [
        { required: true, message: '请输入面试地点', trigger: 'blur' }
      ],
      interviewType: [
        { required: true, message: '请选择面试形式', trigger: 'change' }
      ],
      interviewer: [
        { required: true, message: '请输入面试官姓名', trigger: 'blur' }
      ]
    }
    
    const jobs = ref([])
    
    const resumes = ref([])
    
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalResumes = ref(0)

    const pad2 = (num) => String(num).padStart(2, '0')

    const formatDateTime = (value) => {
      if (!value && value !== 0) return '-'

      let date
      if (value instanceof Date) {
        date = value
      } else if (typeof value === 'number') {
        date = new Date(value)
      } else if (typeof value === 'string') {
        const trimmed = value.trim()
        if (!trimmed) return '-'
        if (/^\d+$/.test(trimmed)) {
          const num = Number(trimmed)
          date = new Date(trimmed.length <= 10 ? num * 1000 : num)
        } else {
          date = new Date(trimmed.replace(' ', 'T'))
        }
      } else {
        date = new Date(value)
      }

      if (Number.isNaN(date?.getTime?.())) return '-'
      return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())} ${pad2(date.getHours())}:${pad2(date.getMinutes())}:${pad2(date.getSeconds())}`
    }

    const normalizeApplicationRecord = (item = {}) => {
      const userName = item.userName || item.username || item.name || (item.userId ? `用户#${item.userId}` : '')
      const positionName = item.positionName || item.jobTitle || item.title || (item.jobId ? `职位#${item.jobId}` : '')
      const createTime = item.createTime || item.applyTime || ''

      return {
        ...item,
        userName,
        positionName,
        careerDirection: item.careerDirection || item.intention || '',
        education: item.education || '',
        workExperience: item.workExperience || item.experience || '',
        createTime,
        createTimeDisplay: formatDateTime(createTime)
      }
    }

    // 加载企业职位列表（用于筛选）
    const loadJobs = async () => {
      if (!enterpriseId.value) return

      try {
        const response = await getEnterpriseJobs(1, 100, enterpriseId.value)
        if (response.data) {
          jobs.value = response.data.records || response.data.list || []
        }
      } catch (error) {
        console.error('加载职位列表失败:', error)
      }
    }

    // 加载简历投递记录
    const loadResumes = async () => {
      if (!enterpriseId.value) {
        return
      }

      loading.value = true
      try {
        const response = await getJobApplications(
          currentPage.value,
          pageSize.value,
          searchForm.position || undefined,
          searchForm.status || undefined,
          enterpriseId.value || undefined
        )

        if (response.data) {
          const rawRecords = response.data.records || response.data.list || []
          resumes.value = rawRecords.map(record => normalizeApplicationRecord(record))
          totalResumes.value = response.data.total != null ? response.data.total : resumes.value.length
        }
      } catch (error) {
        console.error('加载简历失败:', error)
        ElMessage.error('加载简历数据失败')
      } finally {
        loading.value = false
      }
    }

    // 初始化企业ID
    const initEnterpriseId = async () => {
      try {
        const response = await getCurrentUser()
        if (response.data && response.data.enterpriseId) {
          enterpriseId.value = response.data.enterpriseId
          // 加载职位列表和简历数据
          await loadJobs()
          await loadResumes()
        } else {
          ElMessage.error('无法获取企业信息，请重新登录')
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败')
      }
    }

    // 组件挂载时初始化
    onMounted(() => {
      initEnterpriseId()
    })

    const getStatusText = (status) => {
      const statusMap = {
        0: '已投递',
        1: '已查看',
        2: '面试邀请',
        3: '已拒绝',
        4: '已通过'
      }
      return statusMap[status] || '未知'
    }
    
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'info',
        1: 'warning',
        2: 'primary',
        3: 'danger',
        4: 'success'
      }
      return typeMap[status] || 'info'
    }
    
    const searchResumes = () => {
      currentPage.value = 1
      loadResumes()
    }
    
    const resetSearch = () => {
      searchForm.name = ''
      searchForm.position = ''
      searchForm.status = ''
      searchForm.certification = ''
      currentPage.value = 1
      loadResumes()
    }
    
    const viewResume = async (resume) => {
      if (!resume.userId) {
        ElMessage.warning('无法获取用户信息')
        return
      }

      try {
        const response = await getResumeByUserId(resume.userId)
        if (response.data) {
          const resumeData = response.data
          // 构建完整的简历数据
          selectedResume.value = {
            ...resume,
            userName: resumeData.basicInfo?.name || resume.userName,
            phone: resumeData.basicInfo?.phone || resume.phone || '',
            email: resumeData.basicInfo?.email || resume.email || '',
            careerDirection: resumeData.basicInfo?.careerDirection || resumeData.basicInfo?.careerObjective || resume.careerDirection || '',
            careerObjective: resumeData.basicInfo?.careerObjective || '',
            education: resumeData.basicInfo?.education || resume.education || '',
            workExperience: resumeData.basicInfo?.workYears ? `${resumeData.basicInfo.workYears}年` : resume.workExperience || '',
            location: resumeData.basicInfo?.location || '',
            expectedSalary: resumeData.basicInfo?.expectedSalary || '',
            avatar: resumeData.basicInfo?.avatar || '',
            educationList: resumeData.education || [],
            workExperienceList: resumeData.workExperience || [],
            projectExperienceList: resumeData.projectExperience || [],
            skills: resumeData.skills || [],
            certificates: resumeData.certificates || []
          }
          resumeDetailDialogVisible.value = true
        } else {
          ElMessage.warning('该用户暂无简历信息')
        }
      } catch (error) {
        console.error('获取简历详情失败:', error)
        ElMessage.error('获取简历详情失败')
      }
    }
    
    const markAsViewed = async (resume) => {
      try {
        await updateApplicationStatus(resume.id, 1)
        ElMessage.success('已标记为已查看')
        await loadResumes()
      } catch (error) {
        console.error('标记已查看失败:', error)
        ElMessage.error('标记已查看失败')
      }
    }
    
    const openInterviewDialog = (resume) => {
      // 填充面试邀请表单
      selectedResume.value = resume
      interviewForm.position = resume.positionName
      interviewForm.candidateName = resume.userName
      // 重置其他字段
      interviewForm.interviewTime = ''
      interviewForm.interviewLocation = ''
      interviewForm.interviewType = ''
      interviewForm.interviewer = ''
      interviewForm.remark = ''
      interviewDialogVisible.value = true
    }

    const sendInterview = async () => {
      // 验证表单
      const valid = await interviewFormRef.value?.validate()
      if (!valid) return

      try {
        // 构建面试邀请数据
        const interviewData = {
          deliveryId: selectedResume.value?.id,
          userId: selectedResume.value?.userId,
          enterpriseId: enterpriseId.value,
          jobId: selectedResume.value?.jobId || selectedResume.value?.positionId,
          interviewTime: interviewForm.interviewTime,
          interviewLocation: interviewForm.interviewLocation,
          interviewType: interviewForm.interviewType,
          interviewer: interviewForm.interviewer,
          remark: interviewForm.remark
        }

        await sendInterviewInvitationApi(interviewData)
        interviewDialogVisible.value = false
        ElMessage.success('面试邀请发送成功')
        await loadResumes()
      } catch (error) {
        console.error('发送面试邀请失败:', error)
        ElMessage.error('发送面试邀请失败')
      }
    }
    
    const rejectResume = async (resume) => {
      try {
        await updateApplicationStatus(resume.id, 3)
        ElMessage.success('已拒绝该简历')
        await loadResumes()
      } catch (error) {
        console.error('拒绝简历失败:', error)
        ElMessage.error('拒绝简历失败')
      }
    }

    const restoreResume = async (resume) => {
      try {
        await ElMessageBox.confirm(
          '确定要恢复该简历吗？恢复后状态将变为"已查看"',
          '确认恢复',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        await updateApplicationStatus(resume.id, 1)
        ElMessage.success('简历已恢复')
        await loadResumes()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('恢复简历失败:', error)
          ElMessage.error('恢复简历失败')
        }
      }
    }

    const getInterviewStatusText = (status) => {
      const statusMap = {
        0: '待确认',
        1: '已确认',
        2: '已完成',
        3: '已取消'
      }
      return statusMap[status] || '未知'
    }

    const viewInterview = async (resume) => {
      if (!enterpriseId.value) {
        ElMessage.warning('企业信息未初始化，请稍后重试')
        return
      }

      try {
        const response = await getEnterpriseInterviews(1, 200, enterpriseId.value)
        const records = response.data?.records || response.data?.list || []

        const matched = records
          .filter((item) =>
            String(item.deliveryId || '') === String(resume.id || '') ||
            (
              String(item.userId || '') === String(resume.userId || '') &&
              String(item.jobId || '') === String(resume.jobId || resume.positionId || '')
            )
          )
          .sort((a, b) => new Date(b.interviewTime || 0) - new Date(a.interviewTime || 0))

        if (matched.length === 0) {
          ElMessage.info('该候选人暂无面试记录')
          return
        }

        const latest = matched[0]
        const interviewTime = formatDateTime(latest.interviewTime)
        const detailText = [
          `面试ID：${latest.id || '-'}`,
          `候选人：${resume.userName || '-'}`,
          `职位：${resume.positionName || '-'}`,
          `时间：${interviewTime}`,
          `地点：${latest.interviewLocation || '-'}`,
          `形式：${latest.interviewType || '-'}`,
          `面试官：${latest.interviewer || '-'}`,
          `状态：${getInterviewStatusText(latest.status)}`,
          `备注：${latest.remark || '-'}`
        ].join('\n')

        ElMessageBox.alert(detailText, '面试详情', { confirmButtonText: '确定' })
      } catch (error) {
        console.error('获取面试详情失败:', error)
        ElMessage.error('获取面试详情失败')
      }
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadResumes()
    }

    const handleCurrentChange = (current) => {
      currentPage.value = current
      loadResumes()
    }
    
    return {
      resumeDetailDialogVisible,
      interviewDialogVisible,
      selectedResume,
      searchForm,
      interviewForm,
      interviewRules,
      interviewFormRef,
      jobs,
      resumes,
      currentPage,
      pageSize,
      totalResumes,
      getStatusText,
      getStatusTagType,
      searchResumes,
      resetSearch,
      viewResume,
      markAsViewed,
      openInterviewDialog,
      sendInterview,
      rejectResume,
      restoreResume,
      viewInterview,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.resume-manage-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resume-filters {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.time-text {
  font-variant-numeric: tabular-nums;
  color: #606266;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.action-btn {
  min-width: 92px;
  margin: 0 !important;
}

.resume-detail {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.resume-header {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.avatar-section {
  margin-bottom: 15px;
}

.resume-header h3 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.resume-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 10px;
  color: #606266;
}

.meta-item {
  padding: 4px 12px;
  background-color: #f0f9eb;
  color: #67c23a;
  border-radius: 16px;
  font-size: 14px;
}

.contact-info {
  color: #606266;
}

.salary-info {
  margin-top: 10px;
  color: #e6a23c;
  font-weight: 500;
}

.resume-section {
  margin-bottom: 20px;
}

.resume-section h4 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #303133;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 5px;
}

.edu-item, .work-item {
  margin-bottom: 20px;
}

.edu-header, .work-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.school, .company {
  font-weight: bold;
  color: #303133;
}

.period {
  color: #606266;
  font-size: 14px;
}

.edu-details, .work-details {
  display: flex;
  gap: 20px;
  margin-bottom: 5px;
  color: #606266;
}

.edu-desc {
  color: #606266;
  line-height: 1.5;
}

.work-responsibilities {
  margin: 10px 0 0 20px;
  color: #606266;
}

.work-responsibilities li {
  margin-bottom: 5px;
  line-height: 1.5;
}

.work-desc {
  color: #606266;
  line-height: 1.6;
  margin: 10px 0;
}

.project-item {
  margin-bottom: 20px;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.project-name {
  font-weight: bold;
  color: #303133;
}

.project-role {
  color: #409EFF;
  margin-bottom: 5px;
}

.project-desc {
  color: #606266;
  line-height: 1.6;
}

.skills-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.skill-item {
  margin-bottom: 5px;
}

.certificates {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.cert-item {
  margin-bottom: 10px;
}

.resume-empty {
  padding: 40px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

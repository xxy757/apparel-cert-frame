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
        <el-table-column prop="createTime" label="投递时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewResume(scope.row)">查看简历</el-button>
            <el-button v-if="scope.row.status === 0" type="warning" size="small" @click="markAsViewed(scope.row)">标记已查看</el-button>
            <el-button v-if="scope.row.status <= 1" type="success" size="small" @click="sendInterviewInvitation(scope.row)">发送面试邀请</el-button>
            <el-button v-if="scope.row.status <= 1" type="danger" size="small" @click="rejectResume(scope.row)">拒绝</el-button>
            <el-button type="info" size="small" @click="viewInterview(scope.row)">面试详情</el-button>
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
          <h3>{{ selectedResume.userName }}</h3>
          <div class="resume-meta">
            <span class="meta-item">{{ selectedResume.careerDirection }}</span>
            <span class="meta-item">{{ selectedResume.education }}</span>
            <span class="meta-item">{{ selectedResume.workExperience }}</span>
          </div>
          <div class="contact-info">
            <span>{{ selectedResume.phone }}</span> | <span>{{ selectedResume.email }}</span>
          </div>
        </div>
        
        <div class="resume-section">
          <h4>职业目标</h4>
          <p>{{ selectedResume.careerObjective }}</p>
        </div>
        
        <div class="resume-section">
          <h4>教育背景</h4>
          <div v-for="(edu, index) in selectedResume.educationList" :key="index" class="edu-item">
            <div class="edu-header">
              <span class="school">{{ edu.school }}</span>
              <span class="period">{{ edu.startDate }} - {{ edu.endDate }}</span>
            </div>
            <div class="edu-details">
              <span class="major">{{ edu.major }}</span>
              <span class="degree">{{ edu.degree }}</span>
            </div>
            <p class="edu-desc">{{ edu.description }}</p>
          </div>
        </div>
        
        <div class="resume-section">
          <h4>工作经历</h4>
          <div v-for="(work, index) in selectedResume.workExperienceList" :key="index" class="work-item">
            <div class="work-header">
              <span class="company">{{ work.company }}</span>
              <span class="period">{{ work.startDate }} - {{ work.endDate }}</span>
            </div>
            <div class="work-details">
              <span class="position">{{ work.position }}</span>
            </div>
            <ul class="work-responsibilities">
              <li v-for="(resp, respIndex) in work.responsibilities" :key="respIndex">{{ resp }}</li>
            </ul>
          </div>
        </div>
        
        <div class="resume-section">
          <h4>技能证书</h4>
          <div class="certificates">
            <el-tag v-for="(cert, index) in selectedResume.certificates" :key="index" size="small" type="info" class="cert-item">
              {{ cert.name }} ({{ cert.date }})
            </el-tag>
          </div>
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

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
    
    const jobs = ref([
      { id: 1, title: '服装设计师' },
      { id: 2, title: '服装打版师' }
    ])
    
    const resumes = ref([
      {
        id: 1,
        userName: '张三',
        positionName: '服装设计师',
        careerDirection: '设计师',
        education: '本科',
        workExperience: '2年',
        phone: '13800138000',
        email: 'zhangsan@example.com',
        careerObjective: '寻求服装设计师职位，展示创意设计能力',
        educationList: [
          {
            school: '北京服装学院',
            major: '服装设计',
            degree: '本科',
            startDate: '2018-09',
            endDate: '2022-06',
            description: '主修服装设计、服装史、色彩搭配等课程'
          }
        ],
        workExperienceList: [
          {
            company: '某服装品牌',
            position: '助理设计师',
            startDate: '2022-07',
            endDate: '2023-12',
            responsibilities: [
              '参与季节性服装设计',
              '协助设计师绘制设计稿',
              '跟进样衣制作过程'
            ]
          }
        ],
        certificates: [
          { name: '服装设计师初级证书', date: '2022-06' }
        ],
        status: 0,
        createTime: '2024-01-20 09:30:00'
      },
      {
        id: 2,
        userName: '李四',
        positionName: '服装设计师',
        careerDirection: '设计师',
        education: '硕士',
        workExperience: '3年',
        phone: '13900139000',
        email: 'lisi@example.com',
        careerObjective: '寻求高级服装设计师职位，主导品牌设计方向',
        educationList: [
          {
            school: '东华大学',
            major: '服装设计',
            degree: '硕士',
            startDate: '2019-09',
            endDate: '2022-06',
            description: '主修服装创意设计、品牌策划等课程'
          }
        ],
        workExperienceList: [
          {
            company: '知名服装品牌',
            position: '设计师',
            startDate: '2022-07',
            endDate: '2024-01',
            responsibilities: [
              '独立完成季节性服装设计',
              '参与品牌设计方向制定',
              '指导助理设计师工作'
            ]
          }
        ],
        certificates: [
          { name: '服装设计师中级证书', date: '2023-03' }
        ],
        status: 1,
        createTime: '2024-01-18 14:20:00'
      }
    ])
    
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalResumes = ref(resumes.value.length)
    
    const getStatusText = (status) => {
      const statusMap = {
        0: '已投递',
        1: '已查看',
        2: '面试邀请',
        3: '已拒绝'
      }
      return statusMap[status] || '未知'
    }
    
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'info',
        1: 'warning',
        2: 'success',
        3: 'danger'
      }
      return typeMap[status] || 'info'
    }
    
    const searchResumes = () => {
      // TODO: 调用简历搜索接口
      console.log('搜索简历:', searchForm)
    }
    
    const resetSearch = () => {
      searchForm.name = ''
      searchForm.position = ''
      searchForm.status = ''
      searchForm.certification = ''
    }
    
    const viewResume = (resume) => {
      selectedResume.value = resume
      resumeDetailDialogVisible.value = true
    }
    
    const markAsViewed = (resume) => {
      // TODO: 标记简历为已查看
      resume.status = 1
      ElMessage.success('已标记为已查看')
    }
    
    const sendInterviewInvitation = (resume) => {
      // 填充面试邀请表单
      interviewForm.position = resume.positionName
      interviewForm.candidateName = resume.userName
      interviewDialogVisible.value = true
    }
    
    const sendInterview = () => {
      // TODO: 发送面试邀请
      interviewDialogVisible.value = false
      ElMessage.success('面试邀请发送成功')
    }
    
    const rejectResume = (resume) => {
      // TODO: 拒绝简历
      resume.status = 3
      ElMessage.success('已拒绝该简历')
    }
    
    const viewInterview = (resume) => {
      // TODO: 查看面试详情
      ElMessage.info('查看面试详情功能开发中')
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }
    
    const handleCurrentChange = (current) => {
      currentPage.value = current
    }
    
    return {
      resumeDetailDialogVisible,
      interviewDialogVisible,
      selectedResume,
      searchForm,
      interviewForm,
      interviewRules,
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
      sendInterviewInvitation,
      sendInterview,
      rejectResume,
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

.resume-detail {
  padding: 20px;
}

.resume-header {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
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

.certificates {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.cert-item {
  margin-bottom: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

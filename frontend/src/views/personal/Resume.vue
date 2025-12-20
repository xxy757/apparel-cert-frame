<template>
  <div class="resume-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>简历管理</h2>
          <el-button type="primary" @click="editResume">编辑简历</el-button>
        </div>
      </template>
      
      <div v-if="!isEditing" class="resume-preview">
        <div class="resume-header">
          <h1>{{ resume.basicInfo.name }}</h1>
          <p class="contact-info">
            {{ resume.basicInfo.phone }} | {{ resume.basicInfo.email }} | {{ resume.basicInfo.location }}
          </p>
          <p class="career-objective">{{ resume.basicInfo.careerObjective }}</p>
        </div>
        
        <div class="resume-section">
          <h3>教育背景</h3>
          <div v-for="(edu, index) in resume.education" :key="index" class="edu-item">
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
          <h3>工作经历</h3>
          <div v-for="(work, index) in resume.workExperience" :key="index" class="work-item">
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
          <h3>项目经验</h3>
          <div v-for="(project, index) in resume.projectExperience" :key="index" class="project-item">
            <div class="project-header">
              <span class="project-name">{{ project.name }}</span>
              <span class="period">{{ project.startDate }} - {{ project.endDate }}</span>
            </div>
            <div class="project-details">
              <span class="role">{{ project.role }}</span>
            </div>
            <p class="project-desc">{{ project.description }}</p>
            <ul class="project-achievements">
              <li v-for="(ach, achIndex) in project.achievements" :key="achIndex">{{ ach }}</li>
            </ul>
          </div>
        </div>
        
        <div class="resume-section">
          <h3>技能证书</h3>
          <div class="skills-certificates">
            <div class="skills">
              <h4>专业技能</h4>
              <div class="skill-tags">
                <el-tag v-for="(skill, index) in resume.skills" :key="index" size="small" type="info">
                  {{ skill }}
                </el-tag>
              </div>
            </div>
            <div class="certificates">
              <h4>认证证书</h4>
              <div v-for="(cert, index) in resume.certificates" :key="index" class="cert-item">
                <span class="cert-name">{{ cert.name }}</span>
                <span class="cert-date">{{ cert.date }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="resume-actions">
          <el-button type="success" @click="exportResume">导出PDF</el-button>
          <el-switch
            v-model="resume.isPublic"
            active-text="公开"
            inactive-text="私有"
            @change="togglePublic"
          ></el-switch>
        </div>
      </div>
      
      <div v-else class="resume-edit">
        <el-form ref="resumeForm" :model="resume" label-width="100px" :rules="resumeRules">
          <el-card shadow="hover" class="edit-section">
            <template #header><h3>基本信息</h3></template>
            <el-form-item label="姓名" prop="basicInfo.name">
              <el-input v-model="resume.basicInfo.name"></el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="basicInfo.phone">
              <el-input v-model="resume.basicInfo.phone"></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="basicInfo.email">
              <el-input v-model="resume.basicInfo.email"></el-input>
            </el-form-item>
            <el-form-item label="所在地">
              <el-input v-model="resume.basicInfo.location"></el-input>
            </el-form-item>
            <el-form-item label="职业目标">
              <el-input v-model="resume.basicInfo.careerObjective" type="textarea" :rows="3"></el-input>
            </el-form-item>
          </el-card>
          
          <el-card shadow="hover" class="edit-section">
            <template #header>
              <div class="section-header">
                <h3>教育背景</h3>
                <el-button type="primary" size="small" @click="addEducation">添加教育经历</el-button>
              </div>
            </template>
            <div v-for="(edu, index) in resume.education" :key="index" class="form-item-group">
              <div class="group-header">
                <span>教育经历 {{ index + 1 }}</span>
                <el-button type="danger" size="small" @click="removeEducation(index)">删除</el-button>
              </div>
              <el-form-item :label="'学校 ' + (index + 1)">
                <el-input v-model="edu.school"></el-input>
              </el-form-item>
              <el-form-item :label="'专业 ' + (index + 1)">
                <el-input v-model="edu.major"></el-input>
              </el-form-item>
              <el-form-item :label="'学历 ' + (index + 1)">
                <el-select v-model="edu.degree" placeholder="请选择学历">
                  <el-option label="专科" value="专科"></el-option>
                  <el-option label="本科" value="本科"></el-option>
                  <el-option label="硕士" value="硕士"></el-option>
                  <el-option label="博士" value="博士"></el-option>
                </el-select>
              </el-form-item>
              <div class="date-range">
                <el-form-item :label="'开始时间 ' + (index + 1)">
                  <el-date-picker v-model="edu.startDate" type="date" format="YYYY-MM" value-format="YYYY-MM"></el-date-picker>
                </el-form-item>
                <el-form-item :label="'结束时间 ' + (index + 1)">
                  <el-date-picker v-model="edu.endDate" type="date" format="YYYY-MM" value-format="YYYY-MM"></el-date-picker>
                </el-form-item>
              </div>
              <el-form-item :label="'描述 ' + (index + 1)">
                <el-input v-model="edu.description" type="textarea" :rows="2"></el-input>
              </el-form-item>
            </div>
          </el-card>
          
          <!-- 工作经历、项目经验、技能证书等编辑部分类似，此处省略 -->
          
          <div class="form-actions">
            <el-button type="primary" @click="saveResume">保存简历</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'ResumeManage',
  setup() {
    const isEditing = ref(false)
    const resumeForm = ref(null)
    
    const resume = reactive({
      basicInfo: {
        name: '张三',
        phone: '13800138000',
        email: 'zhangsan@example.com',
        location: '北京',
        careerObjective: '寻求服装设计师职位，展示创意设计能力，为品牌创造价值'
      },
      education: [
        {
          school: '北京服装学院',
          major: '服装设计',
          degree: '本科',
          startDate: '2018-09',
          endDate: '2022-06',
          description: '主修服装设计、服装史、色彩搭配等课程'
        }
      ],
      workExperience: [
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
      projectExperience: [
        {
          name: '2023春季系列设计',
          role: '助理设计师',
          startDate: '2022-10',
          endDate: '2023-01',
          description: '参与春季系列服装的设计与开发',
          achievements: ['设计的3款服装被选中量产', '协助完成了20款设计稿']
        }
      ],
      skills: ['服装设计', 'Adobe Illustrator', 'Adobe Photoshop', '色彩搭配'],
      certificates: [
        {
          name: '服装设计师初级认证',
          date: '2022-06'
        }
      ],
      isPublic: true
    })
    
    const resumeRules = {
      'basicInfo.name': [
        { required: true, message: '请输入姓名', trigger: 'blur' }
      ],
      'basicInfo.phone': [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      'basicInfo.email': [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ]
    }
    
    const editResume = () => {
      isEditing.value = true
    }
    
    const saveResume = () => {
      // TODO: 调用保存简历接口
      isEditing.value = false
      ElMessage.success('简历保存成功')
    }
    
    const cancelEdit = () => {
      isEditing.value = false
      if (resumeForm.value) {
        resumeForm.value.resetFields()
      }
    }
    
    const exportResume = () => {
      // TODO: 实现导出PDF功能
      ElMessage.info('导出PDF功能开发中')
    }
    
    const togglePublic = () => {
      // TODO: 调用设置简历公开状态接口
      ElMessage.success(resume.isPublic ? '简历已设为公开' : '简历已设为私有')
    }
    
    const addEducation = () => {
      resume.education.push({
        school: '',
        major: '',
        degree: '',
        startDate: '',
        endDate: '',
        description: ''
      })
    }
    
    const removeEducation = (index) => {
      if (resume.education.length > 1) {
        resume.education.splice(index, 1)
      } else {
        ElMessage.warning('至少保留一条教育经历')
      }
    }
    
    return {
      isEditing,
      resume,
      resumeForm,
      resumeRules,
      editResume,
      saveResume,
      cancelEdit,
      exportResume,
      togglePublic,
      addEducation,
      removeEducation
    }
  }
}
</script>

<style scoped>
.resume-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resume-preview {
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
}

.resume-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.resume-header h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  color: #303133;
}

.contact-info {
  margin: 0 0 10px 0;
  color: #606266;
}

.career-objective {
  margin: 0;
  font-style: italic;
  color: #606266;
}

.resume-section {
  margin-bottom: 30px;
}

.resume-section h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #303133;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 5px;
}

.edu-item, .work-item, .project-item {
  margin-bottom: 20px;
}

.edu-header, .work-header, .project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.school, .company, .project-name {
  font-weight: bold;
  color: #303133;
}

.period {
  color: #606266;
  font-size: 14px;
}

.edu-details, .work-details, .project-details {
  display: flex;
  gap: 20px;
  margin-bottom: 5px;
  color: #606266;
}

.edu-desc, .career-objective {
  color: #606266;
  line-height: 1.5;
}

.work-responsibilities, .project-achievements {
  margin: 10px 0 0 20px;
  color: #606266;
}

.work-responsibilities li, .project-achievements li {
  margin-bottom: 5px;
  line-height: 1.5;
}

.skills-certificates {
  display: flex;
  gap: 40px;
}

.skills, .certificates {
  flex: 1;
}

.skills h4, .certificates h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.cert-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #606266;
}

.resume-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.resume-edit {
  padding: 20px 0;
}

.edit-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-group {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 8px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-weight: bold;
}

.date-range {
  display: flex;
  gap: 20px;
}

.date-range .el-form-item {
  flex: 1;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}
</style>

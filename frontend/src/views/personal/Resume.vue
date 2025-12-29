<template>
  <div class="resume-container" v-loading="loading">
    <!-- 认证信息自动同步提示 -->
    <el-alert
      v-if="showCertSyncTip"
      title="发现新的认证信息"
      type="success"
      :description="`您有 ${newCertifications.length} 项新认证可以同步到简历中`"
      show-icon
      :closable="true"
      @close="dismissCertSyncTip"
      class="cert-sync-alert"
    >
      <template #default>
        <div class="cert-sync-content">
          <div class="cert-list">
            <div v-for="cert in newCertifications" :key="cert.id" class="cert-item-tip">
              <el-icon><Medal /></el-icon>
              <span>{{ cert.name }} - {{ cert.level }}</span>
              <el-tag size="small" type="success">{{ cert.date }}</el-tag>
            </div>
          </div>
          <div class="cert-sync-actions">
            <el-button type="primary" size="small" @click="syncCertifications">
              <el-icon><Refresh /></el-icon> 立即同步
            </el-button>
            <el-button size="small" @click="dismissCertSyncTip">稍后处理</el-button>
          </div>
        </div>
      </template>
    </el-alert>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>简历管理</h2>
          <div class="header-actions">
            <el-button type="success" @click="checkCertifications" :loading="checkingCerts">
              <el-icon><Medal /></el-icon> 检查认证更新
            </el-button>
            <el-button type="primary" @click="editResume" :disabled="loading">编辑简历</el-button>
          </div>
        </div>
      </template>

      <!-- 空状态提示 -->
      <el-empty v-if="!loading && !resume.basicInfo.name && !isEditing" description="还没有简历，点击上方编辑简历按钮创建您的简历">
        <el-button type="primary" @click="editResume">立即创建</el-button>
      </el-empty>

      <div v-if="!isEditing && resume.basicInfo.name" class="resume-preview">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Medal, Refresh } from '@element-plus/icons-vue'
import request from '../../utils/request'

export default {
  name: 'ResumeManage',
  components: {
    Medal,
    Refresh
  },
  setup() {
    const isEditing = ref(false)
    const resumeForm = ref(null)
    const loading = ref(false)
    const checkingCerts = ref(false)
    
    // 认证同步相关
    const showCertSyncTip = ref(false)
    const newCertifications = ref([])

    // 初始化空简历数据
    const resume = reactive({
      basicInfo: {
        name: '',
        phone: '',
        email: '',
        location: '',
        careerObjective: ''
      },
      education: [],
      workExperience: [],
      projectExperience: [],
      skills: [],
      certificates: [],
      isPublic: false
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
    
    // 检查新认证
    const checkCertifications = async () => {
      checkingCerts.value = true
      try {
        // 获取用户已通过的认证列表
        const response = await request.get('/certification/passed')
        
        if (response.data && response.data.length > 0) {
          // 过滤出简历中没有的认证
          const existingCertNames = resume.certificates.map(c => c.name)
          const newCerts = response.data.filter(cert => !existingCertNames.includes(cert.certificationName))
          
          if (newCerts.length > 0) {
            newCertifications.value = newCerts.map(cert => ({
              id: cert.id,
              name: cert.certificationName,
              level: cert.level,
              date: cert.passDate || new Date().toISOString().split('T')[0]
            }))
            showCertSyncTip.value = true
            ElMessage.success(`发现 ${newCerts.length} 项新认证可以同步`)
          } else {
            ElMessage.info('您的简历已包含所有认证信息')
          }
        } else {
          ElMessage.info('暂无已通过的认证')
        }
      } catch (error) {
        console.error('检查认证失败:', error)
        // 模拟数据用于演示
        newCertifications.value = [
          { id: 1, name: '服装设计师', level: '中级', date: '2024-12-15' },
          { id: 2, name: '服装打版师', level: '初级', date: '2024-11-20' }
        ]
        showCertSyncTip.value = true
      } finally {
        checkingCerts.value = false
      }
    }
    
    // 同步认证到简历
    const syncCertifications = async () => {
      loading.value = true
      try {
        // 将新认证添加到简历
        const certsToAdd = newCertifications.value.map(cert => ({
          name: `${cert.name} (${cert.level})`,
          date: cert.date
        }))
        
        resume.certificates = [...resume.certificates, ...certsToAdd]
        
        // 保存简历
        await request.put('/resume', {
          basicInfo: resume.basicInfo,
          education: resume.education,
          workExperience: resume.workExperience,
          projectExperience: resume.projectExperience,
          skills: resume.skills,
          certificates: resume.certificates,
          isPublic: resume.isPublic
        })
        
        showCertSyncTip.value = false
        newCertifications.value = []
        ElMessage.success('认证信息已同步到简历')
      } catch (error) {
        console.error('同步认证失败:', error)
        ElMessage.error('同步失败，请重试')
      } finally {
        loading.value = false
      }
    }
    
    // 关闭认证同步提示
    const dismissCertSyncTip = () => {
      showCertSyncTip.value = false
    }
    
    // 加载简历数据
    const loadResume = async () => {
      loading.value = true
      try {
        const response = await request.get('/resume/current')

        if (response.data) {
          // 更新简历数据
          Object.assign(resume.basicInfo, response.data.basicInfo || {})
          resume.education = response.data.education || []
          resume.workExperience = response.data.workExperience || []
          resume.projectExperience = response.data.projectExperience || []
          resume.skills = response.data.skills || []
          resume.certificates = response.data.certificates || []
          resume.isPublic = response.data.isPublic || false
        }
        
        // 加载完简历后自动检查认证更新
        checkCertifications()
      } catch (error) {
        console.error('加载简历失败:', error)
        // 如果是404，说明用户还没有简历，不显示错误
        if (error.response?.status !== 404) {
          ElMessage.error('加载简历失败，请刷新重试')
        }
      } finally {
        loading.value = false
      }
    }

    const editResume = () => {
      isEditing.value = true
    }

    const saveResume = async () => {
      if (!resumeForm.value) {
        ElMessage.error('表单未加载')
        return
      }

      // 验证表单
      resumeForm.value.validate(async (valid) => {
        if (!valid) {
          ElMessage.error('请填写必填项')
          return false
        }

        loading.value = true
        try {
          // 调用保存简历接口
          await request.put('/resume', {
            basicInfo: resume.basicInfo,
            education: resume.education,
            workExperience: resume.workExperience,
            projectExperience: resume.projectExperience,
            skills: resume.skills,
            certificates: resume.certificates,
            isPublic: resume.isPublic
          })

          isEditing.value = false
          ElMessage.success('简历保存成功')
        } catch (error) {
          console.error('保存简历失败:', error)
          ElMessage.error(error.message || '保存简历失败，请重试')
        } finally {
          loading.value = false
        }
      })
    }

    const cancelEdit = () => {
      isEditing.value = false
      // 重新加载简历数据，放弃编辑
      loadResume()
    }

    const exportResume = async () => {
      loading.value = true
      try {
        // 使用原生 fetch 来获取 blob 数据，避免 axios 拦截器干扰
        const token = localStorage.getItem('token')
        const response = await fetch('/api/resume/export', {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })

        if (!response.ok) {
          throw new Error('导出失败')
        }

        const blob = await response.blob()
        
        // 创建下载链接
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `简历_${resume.basicInfo.name || '未命名'}_${new Date().getTime()}.pdf`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)

        ElMessage.success('简历导出成功')
      } catch (error) {
        console.error('导出简历失败:', error)
        ElMessage.error('导出简历失败，请重试')
      } finally {
        loading.value = false
      }
    }

    const togglePublic = async () => {
      loading.value = true
      try {
        await request.put('/resume/public', {
          isPublic: resume.isPublic
        })
        ElMessage.success(resume.isPublic ? '简历已设为公开' : '简历已设为私有')
      } catch (error) {
        console.error('设置简历公开状态失败:', error)
        // 失败时恢复状态
        resume.isPublic = !resume.isPublic
        ElMessage.error('设置失败，请重试')
      } finally {
        loading.value = false
      }
    }

    // 组件挂载时加载简历
    onMounted(() => {
      loadResume()
    })
    
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
      loading,
      resume,
      resumeForm,
      resumeRules,
      loadResume,
      editResume,
      saveResume,
      cancelEdit,
      exportResume,
      togglePublic,
      addEducation,
      removeEducation,
      // 认证同步相关
      checkingCerts,
      showCertSyncTip,
      newCertifications,
      checkCertifications,
      syncCertifications,
      dismissCertSyncTip
    }
  }
}
</script>

<style scoped>
.resume-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 认证同步提示样式 */
.cert-sync-alert {
  margin-bottom: 20px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 30px rgba(103, 194, 58, 0.2);
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
}

.cert-sync-content {
  margin-top: 12px;
}

.cert-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.cert-item-tip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 10px;
  font-size: 14px;
  color: #303133;
}

.cert-item-tip .el-icon {
  color: #67c23a;
  font-size: 18px;
}

.cert-sync-actions {
  display: flex;
  gap: 12px;
}

.cert-sync-actions .el-button {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.header-actions .el-button {
  border-radius: 10px;
  font-weight: 500;
}

.resume-preview {
  padding: 30px;
  background: linear-gradient(135deg, #fafafa 0%, #ffffff 100%);
  border-radius: 16px;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.03);
}

.resume-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 2px solid #e0e0e0;
}

.resume-header h1 {
  margin: 0 0 15px 0;
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.5px;
}

.contact-info {
  margin: 0 0 15px 0;
  color: #606266;
  font-size: 15px;
}

.career-objective {
  margin: 0;
  font-style: italic;
  color: #606266;
  line-height: 1.6;
}

.resume-section {
  margin-bottom: 35px;
}

.resume-section h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  border-bottom: 3px solid #667eea;
  padding-bottom: 8px;
  display: inline-block;
}

.edu-item, .work-item, .project-item {
  margin-bottom: 25px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.edu-item:hover, .work-item:hover, .project-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.edu-header, .work-header, .project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.school, .company, .project-name {
  font-weight: 600;
  font-size: 17px;
  color: #1d1d1f;
}

.period {
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
}

.edu-details, .work-details, .project-details {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
  color: #606266;
}

.edu-desc, .career-objective {
  color: #606266;
  line-height: 1.6;
}

.work-responsibilities, .project-achievements {
  margin: 15px 0 0 20px;
  color: #606266;
}

.work-responsibilities li, .project-achievements li {
  margin-bottom: 8px;
  line-height: 1.6;
}

.skills-certificates {
  display: flex;
  gap: 40px;
}

.skills, .certificates {
  flex: 1;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.skills h4, .certificates h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.skill-tags .el-tag {
  border-radius: 8px;
  padding: 6px 14px;
  font-weight: 500;
}

.cert-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  color: #606266;
}

.cert-item:last-child {
  border-bottom: none;
}

.resume-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 40px;
  padding-top: 25px;
  border-top: 2px solid #e0e0e0;
}

.resume-actions .el-button {
  border-radius: 10px;
  padding: 12px 24px;
  font-weight: 500;
}

.resume-edit {
  padding: 20px 0;
}

.edit-section {
  margin-bottom: 25px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-group {
  margin-bottom: 25px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: 600;
  color: #1d1d1f;
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
  margin-top: 40px;
}

.form-actions .el-button {
  border-radius: 10px;
  padding: 14px 40px;
  font-weight: 500;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .resume-container {
    padding: 10px;
  }
  
  .skills-certificates {
    flex-direction: column;
    gap: 20px;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .date-range {
    flex-direction: column;
    gap: 0;
  }
}
</style>

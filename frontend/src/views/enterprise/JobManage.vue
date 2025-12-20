<template>
  <div class="job-manage-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>职位管理</h2>
          <el-button type="primary" @click="openCreateJobDialog">发布职位</el-button>
        </div>
      </template>
      
      <div class="job-filters">
        <el-form :inline="true" :model="searchForm" label-width="80px">
          <el-form-item label="职位名称">
            <el-input v-model="searchForm.title" placeholder="请输入职位名称"></el-input>
          </el-form-item>
          <el-form-item label="职位状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value=""></el-option>
              <el-option label="招聘中" value="1"></el-option>
              <el-option label="已结束" value="0"></el-option>
              <el-option label="暂停招聘" value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchJobs">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="jobs" style="width: 100%">
        <el-table-column prop="id" label="职位ID" width="100"></el-table-column>
        <el-table-column prop="title" label="职位名称"></el-table-column>
        <el-table-column prop="jobType" label="岗位类型" width="120"></el-table-column>
        <el-table-column prop="salaryMin" label="薪资范围" width="150">
          <template #default="scope">
            {{ scope.row.salaryMin }}K - {{ scope.row.salaryMax }}K
          </template>
        </el-table-column>
        <el-table-column prop="workLocation" label="工作地点" width="150"></el-table-column>
        <el-table-column prop="workType" label="工作性质" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180"></el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100"></el-table-column>
        <el-table-column prop="applyCount" label="投递数" width="100"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewJobDetails(scope.row)">查看详情</el-button>
            <el-button type="warning" size="small" @click="editJob(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 1" type="danger" size="small" @click="stopRecruitment(scope.row)">结束招聘</el-button>
            <el-button v-else type="success" size="small" @click="resumeRecruitment(scope.row)">重新招聘</el-button>
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
          :total="totalJobs"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 发布职位对话框 -->
    <el-dialog v-model="createJobDialogVisible" title="发布职位" width="800px">
      <el-form ref="jobFormRef" :model="jobForm" :rules="jobRules" label-width="120px">
        <el-form-item label="职位名称" prop="title">
          <el-input v-model="jobForm.title" placeholder="请输入职位名称"></el-input>
        </el-form-item>
        <el-form-item label="岗位类型" prop="jobType">
          <el-select v-model="jobForm.jobType" placeholder="请选择岗位类型">
            <el-option label="设计师" value="设计师"></el-option>
            <el-option label="打版师" value="打版师"></el-option>
            <el-option label="质检员" value="质检员"></el-option>
            <el-option label="导购" value="导购"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="工作性质" prop="workType">
          <el-select v-model="jobForm.workType" placeholder="请选择工作性质">
            <el-option label="全职" value="全职"></el-option>
            <el-option label="兼职" value="兼职"></el-option>
            <el-option label="实习" value="实习"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="薪资范围" prop="salaryMin">
          <div class="salary-range">
            <el-input-number v-model="jobForm.salaryMin" :min="0" :step="1" placeholder="最低薪资（K）"></el-input-number>
            <span class="salary-separator">-</span>
            <el-input-number v-model="jobForm.salaryMax" :min="0" :step="1" placeholder="最高薪资（K）"></el-input-number>
          </div>
        </el-form-item>
        <el-form-item label="工作地点" prop="workLocation">
          <el-input v-model="jobForm.workLocation" placeholder="请输入工作地点"></el-input>
        </el-form-item>
        <el-form-item label="招聘人数" prop="recruitNum">
          <el-input-number v-model="jobForm.recruitNum" :min="1" :step="1" placeholder="请输入招聘人数"></el-input-number>
        </el-form-item>
        <el-form-item label="技能要求" prop="skills">
          <el-select v-model="jobForm.skills" multiple placeholder="请选择技能要求">
            <el-option label="服装设计" value="服装设计"></el-option>
            <el-option label="Adobe Illustrator" value="Adobe Illustrator"></el-option>
            <el-option label="Adobe Photoshop" value="Adobe Photoshop"></el-option>
            <el-option label="服装打版" value="服装打版"></el-option>
            <el-option label="质量检测" value="质量检测"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="职位描述" prop="description">
          <el-input v-model="jobForm.description" type="textarea" :rows="5" placeholder="请输入职位描述"></el-input>
        </el-form-item>
        <el-form-item label="任职要求" prop="requirements">
          <el-input v-model="jobForm.requirements" type="textarea" :rows="5" placeholder="请输入任职要求"></el-input>
        </el-form-item>
        <el-form-item label="福利待遇" prop="benefits">
          <el-input v-model="jobForm.benefits" type="textarea" :rows="3" placeholder="请输入福利待遇"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createJobDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitJobForm">发布</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 职位详情对话框 -->
    <el-dialog v-model="jobDetailDialogVisible" title="职位详情" width="600px">
      <div v-if="selectedJob" class="job-detail">
        <h3>{{ selectedJob.title }}</h3>
        <div class="job-meta">
          <span class="meta-item">{{ selectedJob.salaryMin }}K - {{ selectedJob.salaryMax }}K</span>
          <span class="meta-item">{{ selectedJob.workType }}</span>
          <span class="meta-item">{{ selectedJob.workLocation }}</span>
        </div>
        <div class="job-section">
          <h4>职位描述</h4>
          <p>{{ selectedJob.description }}</p>
        </div>
        <div class="job-section">
          <h4>任职要求</h4>
          <p>{{ selectedJob.requirements }}</p>
        </div>
        <div class="job-section">
          <h4>福利待遇</h4>
          <p>{{ selectedJob.benefits }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="jobDetailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'JobManage',
  setup() {
    const createJobDialogVisible = ref(false)
    const jobDetailDialogVisible = ref(false)
    const selectedJob = ref(null)
    const jobFormRef = ref(null)
    
    const searchForm = reactive({
      title: '',
      status: ''
    })
    
    const jobForm = reactive({
      title: '',
      jobType: '',
      workType: '全职',
      salaryMin: 0,
      salaryMax: 0,
      workLocation: '',
      recruitNum: 1,
      skills: [],
      description: '',
      requirements: '',
      benefits: ''
    })
    
    const jobRules = {
      title: [
        { required: true, message: '请输入职位名称', trigger: 'blur' }
      ],
      jobType: [
        { required: true, message: '请选择岗位类型', trigger: 'change' }
      ],
      workType: [
        { required: true, message: '请选择工作性质', trigger: 'change' }
      ],
      salaryMin: [
        { required: true, message: '请输入最低薪资', trigger: 'blur' },
        { min: 0, message: '最低薪资不能小于0', trigger: 'blur' }
      ],
      salaryMax: [
        { required: true, message: '请输入最高薪资', trigger: 'blur' },
        { min: 0, message: '最高薪资不能小于0', trigger: 'blur' }
      ],
      workLocation: [
        { required: true, message: '请输入工作地点', trigger: 'blur' }
      ],
      recruitNum: [
        { required: true, message: '请输入招聘人数', trigger: 'blur' },
        { min: 1, message: '招聘人数不能小于1', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入职位描述', trigger: 'blur' }
      ],
      requirements: [
        { required: true, message: '请输入任职要求', trigger: 'blur' }
      ]
    }
    
    const jobs = ref([
      {
        id: 1,
        title: '服装设计师',
        jobType: '设计师',
        salaryMin: 8,
        salaryMax: 15,
        workLocation: '北京',
        workType: '全职',
        recruitNum: 2,
        skills: ['服装设计', 'Adobe Illustrator', 'Adobe Photoshop'],
        description: '负责品牌服装的设计开发，参与产品规划，跟进样衣制作过程',
        requirements: '1. 服装设计相关专业，2年以上工作经验\n2. 熟练使用AI、PS等设计软件\n3. 有独立设计能力和创新思维',
        benefits: '五险一金、带薪年假、节日福利、绩效奖金',
        status: 1,
        viewCount: 120,
        applyCount: 25,
        createTime: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        title: '服装打版师',
        jobType: '打版师',
        salaryMin: 6,
        salaryMax: 12,
        workLocation: '上海',
        workType: '全职',
        recruitNum: 1,
        skills: ['服装打版'],
        description: '负责服装版型设计，根据设计稿制作纸样，跟进生产过程',
        requirements: '1. 服装打版相关专业，3年以上工作经验\n2. 熟练使用打版软件\n3. 熟悉服装生产工艺',
        benefits: '五险一金、年终奖、员工旅游',
        status: 1,
        viewCount: 85,
        applyCount: 18,
        createTime: '2024-01-10 14:20:00'
      }
    ])
    
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalJobs = ref(jobs.value.length)
    
    const getStatusText = (status) => {
      const statusMap = {
        0: '已结束',
        1: '招聘中',
        2: '暂停招聘'
      }
      return statusMap[status] || '未知'
    }
    
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'danger',
        1: 'success',
        2: 'warning'
      }
      return typeMap[status] || 'info'
    }
    
    const openCreateJobDialog = () => {
      createJobDialogVisible.value = true
    }
    
    const submitJobForm = () => {
      // TODO: 调用发布职位接口
      createJobDialogVisible.value = false
      ElMessage.success('职位发布成功')
    }
    
    const searchJobs = () => {
      // TODO: 调用职位搜索接口
      console.log('搜索职位:', searchForm)
    }
    
    const resetSearch = () => {
      searchForm.title = ''
      searchForm.status = ''
    }
    
    const viewJobDetails = (job) => {
      selectedJob.value = job
      jobDetailDialogVisible.value = true
    }
    
    const editJob = (job) => {
      // TODO: 编辑职位
      ElMessage.info('编辑职位功能开发中')
    }
    
    const stopRecruitment = (job) => {
      // TODO: 结束招聘
      job.status = 0
      ElMessage.success('已结束招聘')
    }
    
    const resumeRecruitment = (job) => {
      // TODO: 重新招聘
      job.status = 1
      ElMessage.success('已重新开始招聘')
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }
    
    const handleCurrentChange = (current) => {
      currentPage.value = current
    }
    
    return {
      createJobDialogVisible,
      jobDetailDialogVisible,
      selectedJob,
      searchForm,
      jobForm,
      jobRules,
      jobs,
      currentPage,
      pageSize,
      totalJobs,
      getStatusText,
      getStatusTagType,
      openCreateJobDialog,
      submitJobForm,
      searchJobs,
      resetSearch,
      viewJobDetails,
      editJob,
      stopRecruitment,
      resumeRecruitment,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.job-manage-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.job-filters {
  margin-bottom: 20px;
}

.salary-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.salary-separator {
  font-size: 18px;
  color: #606266;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.job-detail {
  padding: 20px;
}

.job-meta {
  display: flex;
  gap: 20px;
  margin: 10px 0 20px 0;
  color: #606266;
}

.meta-item {
  padding: 4px 12px;
  background-color: #f0f9eb;
  color: #67c23a;
  border-radius: 16px;
  font-size: 14px;
}

.job-section {
  margin: 20px 0;
}

.job-section h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

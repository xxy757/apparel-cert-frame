<template>
  <div class="job-manage-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>职位管理</h2>
      <p>管理您发布的招聘职位</p>
    </div>

    <el-card shadow="hover" class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="openCreateJobDialog">
              <el-icon><Plus /></el-icon> 发布职位
            </el-button>
          </div>
          <div class="header-right" v-if="selectedJobs.length > 0">
            <span class="selected-count">已选择 {{ selectedJobs.length }} 个职位</span>
            <el-button type="warning" @click="batchPause">批量暂停</el-button>
            <el-button type="danger" @click="batchDelete">批量删除</el-button>
            <el-button @click="clearSelection">取消选择</el-button>
          </div>
        </div>
      </template>
      
      <div class="job-filters">
        <el-form :inline="true" :model="searchForm" label-width="80px">
          <el-form-item label="职位名称">
            <el-input v-model="searchForm.title" placeholder="请输入职位名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="职位状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="全部" value=""></el-option>
              <el-option label="招聘中" value="1"></el-option>
              <el-option label="已结束" value="0"></el-option>
              <el-option label="暂停招聘" value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchJobs">
              <el-icon><Search /></el-icon> 查询
            </el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table 
        :data="jobs" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
        class="job-table"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="职位ID" width="80"></el-table-column>
        <el-table-column prop="title" label="职位名称" min-width="180">
          <template #default="scope">
            <div class="job-title-cell">
              <el-tag v-if="scope.row.isTop" size="small" type="warning" class="top-tag">
                <el-icon><Top /></el-icon> 置顶
              </el-tag>
              <span class="job-title">{{ scope.row.title }}</span>
              <el-tag v-if="scope.row.isUrgent" size="small" type="danger">急聘</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="jobType" label="岗位类型" width="100"></el-table-column>
        <el-table-column prop="salaryMin" label="薪资范围" width="120">
          <template #default="scope">
            <span class="salary-text">{{ getSalaryDisplay(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="workLocation" label="工作地点" width="100">
          <template #default="scope">
            <span>{{ scope.row.workLocation || scope.row.location || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" effect="light">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70">
          <template #default="scope">
            <span class="stat-num">{{ scope.row.viewCount ?? scope.row.views ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="applyCount" label="投递" width="70">
          <template #default="scope">
            <span class="stat-num highlight">{{ scope.row.applyCount ?? scope.row.applications ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="150"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="viewJobDetails(scope.row)">详情</el-button>
            <el-button type="warning" link @click="editJob(scope.row)">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, scope.row)">
              <el-button type="info" link>
                更多 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="top">
                    <el-icon><Top /></el-icon> {{ scope.row.isTop ? '取消置顶' : '置顶职位' }}
                  </el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 1" command="pause">暂停招聘</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 2" command="resume">恢复招聘</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status !== 0" command="stop">结束招聘</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 0" command="restart">重新招聘</el-dropdown-item>
                  <el-dropdown-item command="refresh">刷新职位</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除职位</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
    <el-dialog v-model="createJobDialogVisible" :title="isEditing ? '编辑职位' : '发布职位'" width="800px" class="job-dialog">
      <el-form ref="jobFormRef" :model="jobForm" :rules="jobRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位名称" prop="title">
              <el-input v-model="jobForm.title" placeholder="请输入职位名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位类型" prop="jobType">
              <el-select v-model="jobForm.jobType" placeholder="请选择岗位类型" style="width: 100%">
                <el-option label="设计师" value="设计师"></el-option>
                <el-option label="打版师" value="打版师"></el-option>
                <el-option label="质检员" value="质检员"></el-option>
                <el-option label="导购" value="导购"></el-option>
                <el-option label="其他" value="其他"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作性质" prop="workType">
              <el-select v-model="jobForm.workType" placeholder="请选择工作性质" style="width: 100%">
                <el-option label="全职" value="全职"></el-option>
                <el-option label="兼职" value="兼职"></el-option>
                <el-option label="实习" value="实习"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作地点" prop="workLocation">
              <el-input v-model="jobForm.workLocation" placeholder="请输入工作地点"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="薪资范围" prop="salaryMin">
              <div class="salary-range">
                <el-input-number v-model="jobForm.salaryMin" :min="0" :step="1" placeholder="最低"></el-input-number>
                <span class="salary-separator">-</span>
                <el-input-number v-model="jobForm.salaryMax" :min="0" :step="1" placeholder="最高"></el-input-number>
                <span class="salary-unit">K</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="recruitNum">
              <el-input-number v-model="jobForm.recruitNum" :min="1" :step="1" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="技能要求" prop="skills">
          <el-select v-model="jobForm.skills" multiple placeholder="请选择技能要求" style="width: 100%">
            <el-option label="服装设计" value="服装设计"></el-option>
            <el-option label="Adobe Illustrator" value="Adobe Illustrator"></el-option>
            <el-option label="Adobe Photoshop" value="Adobe Photoshop"></el-option>
            <el-option label="服装打版" value="服装打版"></el-option>
            <el-option label="质量检测" value="质量检测"></el-option>
            <el-option label="CAD制图" value="CAD制图"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="职位描述" prop="description">
          <el-input v-model="jobForm.description" type="textarea" :rows="4" placeholder="请输入职位描述"></el-input>
        </el-form-item>
        <el-form-item label="任职要求" prop="requirements">
          <el-input v-model="jobForm.requirements" type="textarea" :rows="4" placeholder="请输入任职要求"></el-input>
        </el-form-item>
        <el-form-item label="福利待遇" prop="benefits">
          <el-input v-model="jobForm.benefits" type="textarea" :rows="2" placeholder="请输入福利待遇"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="急聘标记">
              <el-switch v-model="jobForm.isUrgent" active-text="是" inactive-text="否"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createJobDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitJobForm">{{ isEditing ? '保存修改' : '发布职位' }}</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 职位详情对话框 -->
    <el-dialog v-model="jobDetailDialogVisible" title="职位详情" width="650px" class="detail-dialog">
      <div v-if="selectedJob" class="job-detail">
        <div class="detail-header">
          <h3>{{ selectedJob.title }}</h3>
          <div class="detail-tags">
            <el-tag :type="getStatusTagType(selectedJob.status)">{{ getStatusText(selectedJob.status) }}</el-tag>
            <el-tag v-if="selectedJob.isUrgent" type="danger">急聘</el-tag>
          </div>
        </div>
        <div class="detail-meta">
          <span class="meta-item salary">{{ getSalaryDisplay(selectedJob) }}</span>
          <span class="meta-item">{{ selectedJob.workType || '-' }}</span>
          <span class="meta-item">{{ selectedJob.workLocation || selectedJob.location || '-' }}</span>
          <span class="meta-item">招{{ selectedJob.recruitNum || '-' }}人</span>
        </div>
        <div class="detail-stats">
          <div class="stat-item">
            <span class="stat-value">{{ selectedJob.viewCount ?? selectedJob.views ?? 0 }}</span>
            <span class="stat-label">浏览量</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ selectedJob.applyCount ?? selectedJob.applications ?? 0 }}</span>
            <span class="stat-label">投递数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ selectedJob.createTime?.split(' ')[0] }}</span>
            <span class="stat-label">发布日期</span>
          </div>
        </div>
        <div class="detail-section">
          <h4>职位描述</h4>
          <p>{{ selectedJob.description }}</p>
        </div>
        <div class="detail-section">
          <h4>任职要求</h4>
          <p style="white-space: pre-line;">{{ selectedJob.requirements }}</p>
        </div>
        <div class="detail-section">
          <h4>福利待遇</h4>
          <p>{{ selectedJob.benefits }}</p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="jobDetailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="editJob(selectedJob)">编辑职位</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, ArrowDown, Top } from '@element-plus/icons-vue'
import {
  getEnterpriseJobs,
  publishJob,
  updateJob,
  offlineJob,
  batchUpdateJobStatus,
  batchDeleteJobs,
  topJob,
  getJobStatistics
} from '@/api/enterprise'
import { getCurrentUser } from '@/api/enterprise'

export default {
  name: 'JobManage',
  components: {
    Plus, Search, ArrowDown, Top
  },
  setup() {
    const createJobDialogVisible = ref(false)
    const jobDetailDialogVisible = ref(false)
    const selectedJob = ref(null)
    const jobFormRef = ref(null)
    const isEditing = ref(false)
    const selectedJobs = ref([])
    
    const searchForm = reactive({
      title: '',
      status: ''
    })
    
    const jobForm = reactive({
      id: null,
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
      benefits: '',
      isUrgent: false
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
      workLocation: [
        { required: true, message: '请输入工作地点', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入职位描述', trigger: 'blur' }
      ],
      requirements: [
        { required: true, message: '请输入任职要求', trigger: 'blur' }
      ]
    }
    
    const loading = ref(false)
    const enterpriseId = ref(null)

    const jobs = ref([])

    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalJobs = ref(0)

    const parseSalaryRange = (salary) => {
      if (!salary) return { min: null, max: null }
      const match = salary.replace(/[kK]/g, '').match(/(\d+)(?:\s*-\s*(\d+))?/)
      if (!match) return { min: null, max: null }
      const rawMin = parseInt(match[1], 10)
      const rawMax = parseInt(match[2] || match[1], 10)
      const toK = (value) => (value >= 1000 ? Math.round(value / 1000) : value)
      return { min: toK(rawMin), max: toK(rawMax) }
    }

    const getSalaryDisplay = (job) => {
      if (!job) return '-'
      const min = job.salaryMin ?? null
      const max = job.salaryMax ?? null
      if (min !== null || max !== null) {
        const minVal = min ?? 0
        const maxVal = max ?? minVal
        return `${minVal}K - ${maxVal}K`
      }
      return job.salary || '-'
    }

    const normalizeJobItem = (job) => {
      if (!job) return job
      const range = parseSalaryRange(job.salary)
      return {
        ...job,
        jobType: job.jobType || job.type,
        workLocation: job.workLocation || job.location,
        salaryMin: job.salaryMin ?? range.min,
        salaryMax: job.salaryMax ?? range.max,
        viewCount: job.viewCount ?? job.views,
        applyCount: job.applyCount ?? job.applications,
        skills: Array.isArray(job.skills) ? job.skills : (job.skills ? job.skills.split(',') : [])
      }
    }

    const buildJobPayload = () => {
      const salaryMin = Number(jobForm.salaryMin) || 0
      const salaryMax = Number(jobForm.salaryMax) || 0
      const salary =
        salaryMin || salaryMax
          ? `${salaryMin * 1000}-${salaryMax * 1000}`
          : ''

      return {
        id: jobForm.id,
        title: jobForm.title,
        type: jobForm.jobType,
        workType: jobForm.workType,
        salaryMin,
        salaryMax,
        salary,
        location: jobForm.workLocation,
        recruitNum: jobForm.recruitNum,
        skills: jobForm.skills?.length ? jobForm.skills.join(',') : '',
        description: jobForm.description,
        requirements: jobForm.requirements,
        benefits: jobForm.benefits,
        isUrgent: jobForm.isUrgent ? 1 : 0
      }
    }

    // 加载职位数据
    const loadJobs = async () => {
      if (!enterpriseId.value) {
        ElMessage.warning('请先登录企业账号')
        return
      }

      loading.value = true
      try {
        const response = await getEnterpriseJobs(
          currentPage.value,
          pageSize.value,
          enterpriseId.value,
          searchForm.status || undefined
        )

        if (response.data) {
          const rawJobs = response.data.records || response.data.list || []
          jobs.value = rawJobs.map(j => normalizeJobItem(j))
          totalJobs.value = response.data.total || jobs.value.length
        }
      } catch (error) {
        console.error('加载职位失败:', error)
        ElMessage.error('加载职位数据失败')
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
          // 加载职位数据
          await loadJobs()
        } else {
          ElMessage.error('无法获取企业信息，请重新登录')
          // 这里可以根据实际路由跳转到登录页
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
        0: '已结束',
        1: '招聘中',
        2: '暂停中'
      }
      return statusMap[status] || '未知'
    }
    
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'info',
        1: 'success',
        2: 'warning'
      }
      return typeMap[status] || 'info'
    }
    
    const resetForm = () => {
      jobForm.id = null
      jobForm.title = ''
      jobForm.jobType = ''
      jobForm.workType = '全职'
      jobForm.salaryMin = 0
      jobForm.salaryMax = 0
      jobForm.workLocation = ''
      jobForm.recruitNum = 1
      jobForm.skills = []
      jobForm.description = ''
      jobForm.requirements = ''
      jobForm.benefits = ''
      jobForm.isUrgent = false
    }
    
    const openCreateJobDialog = () => {
      isEditing.value = false
      resetForm()
      createJobDialogVisible.value = true
    }
    
    const submitJobForm = async () => {
      const valid = await jobFormRef.value?.validate()
      if (!valid) return

      try {
        const payload = buildJobPayload()
        let result
        if (isEditing.value) {
          // 更新职位
          result = await updateJob({
            ...payload,
            enterpriseId: enterpriseId.value
          })
          if (result.code === 200) {
            ElMessage.success('职位修改成功')
          }
        } else {
          // 发布新职位
          result = await publishJob({
            ...payload,
            enterpriseId: enterpriseId.value,
            status: 1, // 招聘中
            views: 0,
            applications: 0
          })
          if (result.code === 200) {
            ElMessage.success('职位发布成功')
          }
        }
        createJobDialogVisible.value = false
        // 重新加载职位列表
        await loadJobs()
      } catch (error) {
        console.error('提交职位失败:', error)
        ElMessage.error(isEditing.value ? '职位修改失败' : '职位发布失败')
      }
    }
    
    const searchJobs = () => {
      currentPage.value = 1
      loadJobs()
    }
    
    const resetSearch = () => {
      searchForm.title = ''
      searchForm.status = ''
      currentPage.value = 1
      loadJobs()
    }
    
    const viewJobDetails = (job) => {
      selectedJob.value = normalizeJobItem(job)
      jobDetailDialogVisible.value = true
    }
    
    const editJob = (job) => {
      const normalizedJob = normalizeJobItem(job)
      isEditing.value = true
      Object.assign(jobForm, {
        id: normalizedJob.id,
        title: normalizedJob.title,
        jobType: normalizedJob.jobType,
        workType: normalizedJob.workType,
        salaryMin: normalizedJob.salaryMin || 0,
        salaryMax: normalizedJob.salaryMax || 0,
        workLocation: normalizedJob.workLocation,
        recruitNum: normalizedJob.recruitNum || 1,
        skills: normalizedJob.skills || [],
        description: normalizedJob.description,
        requirements: normalizedJob.requirements,
        benefits: normalizedJob.benefits,
        isUrgent: !!normalizedJob.isUrgent
      })
      jobDetailDialogVisible.value = false
      createJobDialogVisible.value = true
    }
    
    const handleCommand = async (command, job) => {
      switch (command) {
        case 'pause':
          try {
            await batchUpdateJobStatus([job.id], 2)
            ElMessage.success('职位已暂停招聘')
            await loadJobs()
          } catch (error) {
            console.error('暂停职位失败:', error)
            ElMessage.error('暂停职位失败')
          }
          break
        case 'resume':
          try {
            await batchUpdateJobStatus([job.id], 1)
            ElMessage.success('职位已恢复招聘')
            await loadJobs()
          } catch (error) {
            console.error('恢复职位失败:', error)
            ElMessage.error('恢复职位失败')
          }
          break
        case 'stop':
          try {
            await offlineJob(job.id)
            ElMessage.success('职位已结束招聘')
            await loadJobs()
          } catch (error) {
            console.error('结束职位失败:', error)
            ElMessage.error('结束职位失败')
          }
          break
        case 'restart':
          try {
            await batchUpdateJobStatus([job.id], 1)
            ElMessage.success('职位已重新开始招聘')
            await loadJobs()
          } catch (error) {
            console.error('重新开始招聘失败:', error)
            ElMessage.error('重新开始招聘失败')
          }
          break
        case 'refresh':
          await loadJobs()
          ElMessage.success('职位已刷新')
          break
        case 'top':
          await toggleJobTop(job)
          break
        case 'delete':
          ElMessageBox.confirm('确定要删除该职位吗？', '删除确认', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(async () => {
            try {
              await batchDeleteJobs([job.id])
              ElMessage.success('职位已删除')
              await loadJobs()
            } catch (error) {
              console.error('删除职位失败:', error)
              ElMessage.error('删除职位失败')
            }
          }).catch(() => {})
          break
      }
    }
    
    // 置顶/取消置顶职位
    const toggleJobTop = async (job) => {
      const newTopStatus = !job.isTop
      try {
        await topJob(job.id, newTopStatus ? 1 : 0)
        // 重新加载列表以获取最新排序
        await loadJobs()
        ElMessage.success(newTopStatus ? '职位已置顶' : '已取消置顶')
      } catch (error) {
        console.error('置顶操作失败:', error)
        ElMessage.error('置顶操作失败')
      }
    }
    
    const handleSelectionChange = (selection) => {
      selectedJobs.value = selection
    }
    
    const clearSelection = () => {
      selectedJobs.value = []
    }
    
    const batchPause = async () => {
      if (selectedJobs.value.length === 0) {
        ElMessage.warning('请先选择要暂停的职位')
        return
      }

      ElMessageBox.confirm(`确定要暂停选中的 ${selectedJobs.value.length} 个职位吗？`, '批量暂停', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const jobIds = selectedJobs.value.map(job => job.id)
          await batchUpdateJobStatus(jobIds, 2)
          ElMessage.success(`已暂停 ${selectedJobs.value.length} 个职位`)
          await loadJobs()
          clearSelection()
        } catch (error) {
          console.error('批量暂停失败:', error)
          ElMessage.error('批量暂停失败')
        }
      }).catch(() => {})
    }
    
    const batchDelete = async () => {
      if (selectedJobs.value.length === 0) {
        ElMessage.warning('请先选择要删除的职位')
        return
      }

      ElMessageBox.confirm(`确定要删除选中的 ${selectedJobs.value.length} 个职位吗？此操作不可恢复。`, '批量删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }).then(async () => {
        try {
          const jobIds = selectedJobs.value.map(job => job.id)
          await batchDeleteJobs(jobIds)
          ElMessage.success(`已删除 ${selectedJobs.value.length} 个职位`)
          await loadJobs()
          clearSelection()
        } catch (error) {
          console.error('批量删除失败:', error)
          ElMessage.error('批量删除失败')
        }
      }).catch(() => {})
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadJobs()
    }
    
    const handleCurrentChange = (current) => {
      currentPage.value = current
      loadJobs()
    }
    
    return {
      createJobDialogVisible,
      jobDetailDialogVisible,
      selectedJob,
      jobFormRef,
      isEditing,
      selectedJobs,
      searchForm,
      jobForm,
      jobRules,
      jobs,
      currentPage,
      pageSize,
      totalJobs,
      getStatusText,
      getStatusTagType,
      getSalaryDisplay,
      openCreateJobDialog,
      submitJobForm,
      searchJobs,
      resetSearch,
      viewJobDetails,
      editJob,
      handleCommand,
      handleSelectionChange,
      clearSelection,
      batchPause,
      batchDelete,
      handleSizeChange,
      handleCurrentChange,
      toggleJobTop
    }
  }
}
</script>

<style scoped>
.job-manage-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 25px;
  padding: 30px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.page-header h2 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 10px 0;
}

.page-header p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

/* 主卡片 */
.main-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left .el-button--primary {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  color: #667eea;
  font-weight: 500;
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 6px;
}

/* 筛选区域 */
.job-filters {
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

/* 表格样式 */
.job-table {
  border-radius: 12px;
  overflow: hidden;
}

.job-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-tag {
  display: flex;
  align-items: center;
  gap: 2px;
}

.top-tag .el-icon {
  font-size: 12px;
}

.job-title {
  font-weight: 500;
  color: #303133;
}

.salary-text {
  color: #f56c6c;
  font-weight: 600;
}

.stat-num {
  color: #909399;
}

.stat-num.highlight {
  color: #667eea;
  font-weight: 600;
}

/* 分页 */
.pagination-container {
  margin-top: 25px;
  display: flex;
  justify-content: flex-end;
}

/* 对话框样式 */
.job-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #e4e7ed;
  padding: 20px 24px;
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

.salary-unit {
  color: #909399;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 详情对话框 */
.job-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1d1d1f;
}

.detail-tags {
  display: flex;
  gap: 8px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-meta .meta-item {
  color: #606266;
  font-size: 15px;
}

.detail-meta .meta-item.salary {
  color: #f56c6c;
  font-weight: 700;
  font-size: 20px;
}

.detail-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 25px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-item .stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
}

.stat-item .stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
  display: inline-block;
}

.detail-section p {
  color: #606266;
  line-height: 1.8;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .job-manage-container {
    padding: 15px;
  }

  .card-header {
    flex-direction: column;
    gap: 15px;
  }

  .header-right {
    flex-wrap: wrap;
    justify-content: center;
  }

  .detail-stats {
    flex-direction: column;
    gap: 15px;
  }
}
</style>

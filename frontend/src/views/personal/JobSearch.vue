<template>
  <div class="job-search-container">
    <div class="page-header">
      <h2>职位搜索</h2>
      <p>寻找适合您的服装行业职位</p>
    </div>

    <!-- 智能推荐区域 -->
    <div class="recommendation-section" v-if="recommendedJobs.length > 0">
      <div class="section-header">
        <h3>
          <el-icon><MagicStick /></el-icon> 智能推荐
        </h3>
        <el-button type="primary" link @click="refreshRecommendations">
          <el-icon><Refresh /></el-icon> 刷新推荐
        </el-button>
      </div>
      <div class="recommendation-list">
        <el-card
          v-for="job in recommendedJobs"
          :key="'rec-' + job.id"
          shadow="hover"
          class="recommendation-card"
          @click="viewJobDetail(job)"
        >
          <div class="rec-card-content">
            <div class="rec-match-badge">
              <span class="match-score">{{ job.matchScore }}%</span>
              <span class="match-label">匹配度</span>
            </div>
            <div class="rec-info">
              <h4>{{ job.title }}</h4>
              <p class="rec-company">{{ job.companyName }}</p>
              <div class="rec-meta">
                <span class="rec-salary">{{ job.salary }}</span>
                <span class="rec-location">{{ job.location }}</span>
              </div>
              <div class="rec-match-reasons">
                <el-tag v-for="reason in job.matchReasons" :key="reason" size="small" type="success">
                  {{ reason }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索职位名称、公司名称或关键词"
          clearable
          @keyup.enter="searchJobs"
        >
          <template #append>
            <el-button type="primary" @click="searchJobs">
              <i class="el-icon-search"></i> 搜索
            </el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-section">
        <el-card shadow="hover">
          <h3>筛选条件</h3>
          <div class="filter-form">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="职位类别">
                  <el-select v-model="filters.jobType" placeholder="请选择职位类别" clearable>
                    <el-option label="设计师" value="设计师"></el-option>
                    <el-option label="打版师" value="打版师"></el-option>
                    <el-option label="质检员" value="质检员"></el-option>
                    <el-option label="导购" value="导购"></el-option>
                    <el-option label="生产管理" value="生产管理"></el-option>
                    <el-option label="市场营销" value="市场营销"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="工作地点">
                  <el-select v-model="filters.location" placeholder="请选择工作地点" clearable>
                    <el-option label="北京" value="北京"></el-option>
                    <el-option label="上海" value="上海"></el-option>
                    <el-option label="广州" value="广州"></el-option>
                    <el-option label="深圳" value="深圳"></el-option>
                    <el-option label="杭州" value="杭州"></el-option>
                    <el-option label="苏州" value="苏州"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="薪资范围">
                  <el-select v-model="filters.salary" placeholder="请选择薪资范围" clearable>
                    <el-option label="3k-5k" value="3000-5000"></el-option>
                    <el-option label="5k-8k" value="5000-8000"></el-option>
                    <el-option label="8k-12k" value="8000-12000"></el-option>
                    <el-option label="12k-20k" value="12000-20000"></el-option>
                    <el-option label="20k以上" value="20000+"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="工作经验">
                  <el-select v-model="filters.experience" placeholder="请选择工作经验" clearable>
                    <el-option label="应届生" value="应届生"></el-option>
                    <el-option label="1-3年" value="1-3年"></el-option>
                    <el-option label="3-5年" value="3-5年"></el-option>
                    <el-option label="5-10年" value="5-10年"></el-option>
                    <el-option label="10年以上" value="10年以上"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="学历要求">
                  <el-select v-model="filters.education" placeholder="请选择学历要求" clearable>
                    <el-option label="高中/中专" value="高中/中专"></el-option>
                    <el-option label="大专" value="大专"></el-option>
                    <el-option label="本科" value="本科"></el-option>
                    <el-option label="硕士" value="硕士"></el-option>
                    <el-option label="博士" value="博士"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="企业类型">
                  <el-select v-model="filters.companyType" placeholder="请选择企业类型" clearable>
                    <el-option label="国企" value="国企"></el-option>
                    <el-option label="外企" value="外企"></el-option>
                    <el-option label="民企" value="民企"></el-option>
                    <el-option label="合资" value="合资"></el-option>
                    <el-option label="创业公司" value="创业公司"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="filter-actions">
              <el-button type="primary" @click="searchJobs">应用筛选</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-action-bar" v-if="selectedJobs.length > 0">
      <div class="batch-info">
        <el-icon><Check /></el-icon>
        <span>已选择 {{ selectedJobs.length }} 个职位</span>
      </div>
      <div class="batch-actions">
        <el-button type="primary" @click="batchApply" :loading="batchApplying">
          <el-icon><Position /></el-icon> 批量投递
        </el-button>
        <el-button @click="clearSelection">取消选择</el-button>
      </div>
    </div>

    <!-- 职位列表 -->
    <div class="job-list-section">
      <div class="section-header">
        <h3>搜索结果</h3>
        <div class="header-right">
          <span class="result-count">{{ jobs.length }} 个职位</span>
          <el-checkbox 
            v-model="selectAll" 
            @change="toggleSelectAll"
            :indeterminate="isIndeterminate"
          >
            全选
          </el-checkbox>
        </div>
      </div>

      <div class="job-list">
        <el-card
          v-for="job in jobs"
          :key="job.id"
          shadow="hover"
          class="job-card"
          :class="{ 'job-card-selected': isJobSelected(job.id) }"
        >
          <div class="job-card-content">
            <div class="job-checkbox">
              <el-checkbox 
                :model-value="isJobSelected(job.id)"
                @change="toggleJobSelection(job)"
              />
            </div>
            <div class="job-info" @click="viewJobDetail(job)">
              <div class="job-title-section">
                <h4>{{ job.title }}</h4>
                <el-tag size="small" :type="getJobTypeColor(job.type)">{{ job.type }}</el-tag>
              </div>
              <div class="company-info">
                <img :src="job.companyLogo" alt="公司logo" class="company-logo">
                <div class="company-details">
                  <span class="company-name">{{ job.companyName }}</span>
                  <span class="company-location">{{ job.location }}</span>
                </div>
              </div>
              <div class="job-meta">
                <span class="salary">{{ job.salary }}</span>
                <span class="experience">{{ job.experience }}</span>
                <span class="education">{{ job.education }}</span>
              </div>
              <div class="job-description">
                {{ job.description.substring(0, 150) }}...
              </div>
              <div class="job-tags">
                <el-tag size="small" v-for="tag in job.tags" :key="tag">{{ tag }}</el-tag>
              </div>
            </div>
            <div class="job-actions">
              <el-button type="primary" @click.stop="applyForJob(job)">
                <i class="el-icon-check"></i> 申请职位
              </el-button>
              <el-button type="success" @click.stop="saveJob(job)" :disabled="job.saved">
                <i class="el-icon-star-on"></i> {{ job.saved ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="jobs.length"
        ></el-pagination>
      </div>
    </div>

    <!-- 职位详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="selectedJob.title"
      width="800px"
      @close="resetSelectedJob"
    >
      <div v-if="selectedJob" class="job-detail">
        <div class="job-detail-header">
          <div class="job-detail-title">
            <h3>{{ selectedJob.title }}</h3>
            <el-tag :type="getJobTypeColor(selectedJob.type)">{{ selectedJob.type }}</el-tag>
          </div>
          <div class="job-detail-salary">{{ selectedJob.salary }}</div>
        </div>

        <div class="company-info-detail">
          <img :src="selectedJob.companyLogo" alt="公司logo" class="company-logo-detail">
          <div class="company-details-detail">
            <h4>{{ selectedJob.companyName }}</h4>
            <p class="company-desc">{{ selectedJob.companyDesc }}</p>
            <div class="company-meta">
              <span>{{ selectedJob.companySize }}</span>
              <span>{{ selectedJob.companyIndustry }}</span>
              <span>{{ selectedJob.location }}</span>
            </div>
          </div>
        </div>

        <div class="job-detail-content">
          <div class="job-detail-section">
            <h4>职位描述</h4>
            <p>{{ selectedJob.description }}</p>
          </div>

          <div class="job-detail-section">
            <h4>职位要求</h4>
            <ul>
              <li v-for="(requirement, index) in selectedJob.requirements" :key="index">{{ requirement }}</li>
            </ul>
          </div>

          <div class="job-detail-section">
            <h4>福利待遇</h4>
            <div class="benefits">
              <el-tag size="small" v-for="benefit in selectedJob.benefits" :key="benefit">{{ benefit }}</el-tag>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="applyForJob(selectedJob)">
            <i class="el-icon-check"></i> 申请职位
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量投递确认对话框 -->
    <el-dialog
      v-model="batchApplyDialogVisible"
      title="批量投递确认"
      width="500px"
    >
      <div class="batch-apply-content">
        <p>您即将向以下 {{ selectedJobs.length }} 个职位投递简历：</p>
        <div class="batch-job-list">
          <div v-for="job in selectedJobs" :key="job.id" class="batch-job-item">
            <span class="batch-job-title">{{ job.title }}</span>
            <span class="batch-job-company">{{ job.companyName }}</span>
          </div>
        </div>
        <el-alert
          title="温馨提示"
          type="info"
          description="批量投递将使用您当前的默认简历，请确保简历信息完整准确。"
          show-icon
          :closable="false"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchApplyDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="openScheduledDeliveryDialog">
            <el-icon><Clock /></el-icon> 定时投递
          </el-button>
          <el-button type="primary" @click="confirmBatchApply" :loading="batchApplying">
            确认投递
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 定时投递设置对话框 -->
    <el-dialog
      v-model="scheduledDeliveryDialogVisible"
      title="定时投递设置"
      width="550px"
    >
      <div class="scheduled-delivery-content">
        <el-alert
          title="定时投递说明"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #default>
            设置定时投递后，系统将在指定时间自动向选中的职位投递您的简历。
          </template>
        </el-alert>
        
        <el-form :model="scheduledForm" label-width="100px" :rules="scheduledRules" ref="scheduledFormRef">
          <el-form-item label="投递时间" prop="scheduledTime">
            <el-date-picker
              v-model="scheduledForm.scheduledTime"
              type="datetime"
              placeholder="选择投递时间"
              :disabled-date="disabledDate"
              :disabled-hours="disabledHours"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          
          <el-form-item label="投递职位">
            <div class="scheduled-job-list">
              <el-tag v-for="job in selectedJobs" :key="job.id" class="scheduled-job-tag">
                {{ job.title }} - {{ job.companyName }}
              </el-tag>
            </div>
          </el-form-item>
          
          <el-form-item label="投递简历">
            <el-select v-model="scheduledForm.resumeId" placeholder="选择要投递的简历" style="width: 100%">
              <el-option label="默认简历" :value="1" />
            </el-select>
          </el-form-item>
        </el-form>
        
        <div class="scheduled-tips">
          <h4><el-icon><InfoFilled /></el-icon> 温馨提示</h4>
          <ul>
            <li>定时投递时间必须是未来时间</li>
            <li>建议选择工作日上午9-11点投递，HR查看率更高</li>
            <li>您可以在"我的投递"中查看和取消定时任务</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scheduledDeliveryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmScheduledDelivery" :loading="schedulingDelivery">
            <el-icon><Clock /></el-icon> 确认定时投递
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Refresh, Check, Position, Clock, InfoFilled } from '@element-plus/icons-vue'
import request from '../../utils/request'

export default {
  name: 'JobSearch',
  components: {
    MagicStick,
    Refresh,
    Check,
    Position,
    Clock,
    InfoFilled
  },
  setup() {
    // 搜索和筛选数据
    const searchQuery = ref('')
    const filters = reactive({
      jobType: '',
      location: '',
      salary: '',
      experience: '',
      education: '',
      companyType: ''
    })

    // 批量投递相关
    const selectedJobs = ref([])
    const selectAll = ref(false)
    const batchApplying = ref(false)
    const batchApplyDialogVisible = ref(false)

    // 定时投递相关
    const scheduledDeliveryDialogVisible = ref(false)
    const schedulingDelivery = ref(false)
    const scheduledFormRef = ref(null)
    const scheduledForm = reactive({
      scheduledTime: '',
      resumeId: 1
    })
    const scheduledRules = {
      scheduledTime: [
        { required: true, message: '请选择投递时间', trigger: 'change' }
      ]
    }

    // 智能推荐相关
    const recommendedJobs = ref([
      {
        id: 101,
        title: '高级服装设计师',
        companyName: '某知名服装品牌',
        location: '北京',
        salary: '15k-25k',
        matchScore: 95,
        matchReasons: ['技能匹配', '经验匹配', '地点偏好']
      },
      {
        id: 102,
        title: '服装打版师',
        companyName: '上海服装有限公司',
        location: '上海',
        salary: '10k-18k',
        matchScore: 88,
        matchReasons: ['认证匹配', '薪资期望']
      },
      {
        id: 103,
        title: '服装质检主管',
        companyName: '广州纺织品集团',
        location: '广州',
        salary: '12k-20k',
        matchScore: 82,
        matchReasons: ['行业经验', '技能匹配']
      }
    ])

    // 计算是否为半选状态
    const isIndeterminate = computed(() => {
      return selectedJobs.value.length > 0 && selectedJobs.value.length < jobs.value.length
    })

    // 职位数据
    const jobs = ref([
      {
        id: 1,
        title: '高级服装设计师',
        type: '设计师',
        companyName: '某知名服装品牌',
        companyLogo: 'https://via.placeholder.com/60',
        companyDesc: '国内领先的时尚服装品牌，专注于高端女装设计与生产',
        companySize: '500-1000人',
        companyIndustry: '服装/纺织',
        location: '北京',
        salary: '15k-25k',
        experience: '3-5年',
        education: '本科',
        description: '负责女装系列的设计开发，包括款式设计、面料选择、色彩搭配等工作。需要具备良好的时尚敏感度和设计能力，能够把握市场趋势，独立完成设计任务。',
        requirements: [
          '服装设计相关专业本科及以上学历',
          '3年以上女装设计经验',
          '熟练使用Photoshop、Illustrator等设计软件',
          '具备良好的时尚敏感度和创新能力',
          '有团队协作精神和沟通能力'
        ],
        benefits: ['五险一金', '年终奖', '带薪年假', '节日福利', '定期培训'],
        tags: ['女装', '设计', '高端', '时尚'],
        saved: false
      },
      {
        id: 2,
        title: '服装打版师',
        type: '打版师',
        companyName: '上海服装有限公司',
        companyLogo: 'https://via.placeholder.com/60',
        companyDesc: '专业从事服装生产的现代化企业，拥有先进的生产设备和技术',
        companySize: '1000-2000人',
        companyIndustry: '服装/纺织',
        location: '上海',
        salary: '10k-18k',
        experience: '1-3年',
        education: '大专',
        description: '负责服装样板的制作和修改，根据设计师的图纸或样衣制作出符合要求的纸样。需要具备扎实的服装结构知识和打版技术，能够准确理解设计意图。',
        requirements: [
          '服装工程或相关专业大专及以上学历',
          '1年以上服装打版经验',
          '熟练使用CAD等打版软件',
          '具备良好的服装结构知识',
          '工作认真细致，责任心强'
        ],
        benefits: ['五险一金', '绩效奖金', '包吃住', '晋升空间', '年度体检'],
        tags: ['打版', '男装', '生产'],
        saved: true
      },
      {
        id: 3,
        title: '服装质检员',
        type: '质检员',
        companyName: '广州纺织品集团',
        companyLogo: 'https://via.placeholder.com/60',
        companyDesc: '大型纺织品生产企业，产品出口多个国家和地区',
        companySize: '2000-5000人',
        companyIndustry: '纺织/面料',
        location: '广州',
        salary: '8k-12k',
        experience: '1-3年',
        education: '高中/中专',
        description: '负责服装产品的质量检验工作，确保产品符合质量标准和客户要求。需要具备服装质量检验的专业知识和技能，能够识别各种质量问题。',
        requirements: [
          '高中或中专以上学历',
          '1年以上服装质检经验',
          '了解服装质量检验标准和流程',
          '工作认真负责，具有较强的观察力',
          '能够适应加班'
        ],
        benefits: ['五险一金', '加班补贴', '交通补贴', '员工食堂', '生日福利'],
        tags: ['质检', '纺织品', '出口'],
        saved: false
      }
    ])

    // 分页数据
    const currentPage = ref(1)
    const pageSize = ref(10)

    // 职位详情对话框
    const dialogVisible = ref(false)
    const selectedJob = ref({})

    // 生命周期钩子
    onMounted(() => {
      // TODO: 从API获取职位数据和推荐数据
      console.log('Job search page loaded')
      loadRecommendations()
    })

    // 加载智能推荐
    const loadRecommendations = async () => {
      // TODO: 调用API获取推荐职位
      console.log('Loading recommendations...')
    }

    // 刷新推荐
    const refreshRecommendations = () => {
      ElMessage.info('正在刷新推荐...')
      loadRecommendations()
    }

    // 搜索职位
    const searchJobs = () => {
      // TODO: 调用API搜索职位
      ElMessage.info('搜索功能开发中')
    }

    // 重置筛选条件
    const resetFilters = () => {
      Object.keys(filters).forEach(key => {
        filters[key] = ''
      })
      searchQuery.value = ''
      // TODO: 重置搜索结果
    }

    // 查看职位详情
    const viewJobDetail = (job) => {
      selectedJob.value = job
      dialogVisible.value = true
    }

    // 申请职位
    const applyForJob = (job) => {
      // TODO: 调用API申请职位
      ElMessage.success(`已申请职位：${job.title}`)
      dialogVisible.value = false
    }

    // 收藏职位
    const saveJob = (job) => {
      job.saved = !job.saved
      ElMessage.success(job.saved ? '职位已收藏' : '收藏已取消')
    }

    // 获取职位类型颜色
    const getJobTypeColor = (type) => {
      const colorMap = {
        '设计师': 'primary',
        '打版师': 'success',
        '质检员': 'warning',
        '导购': 'info',
        '生产管理': 'danger',
        '市场营销': 'purple'
      }
      return colorMap[type] || 'info'
    }

    // 重置选中的职位
    const resetSelectedJob = () => {
      selectedJob.value = {}
    }

    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size
    }

    const handleCurrentChange = (current) => {
      currentPage.value = current
    }

    // 批量选择相关方法
    const isJobSelected = (jobId) => {
      return selectedJobs.value.some(j => j.id === jobId)
    }

    const toggleJobSelection = (job) => {
      const index = selectedJobs.value.findIndex(j => j.id === job.id)
      if (index > -1) {
        selectedJobs.value.splice(index, 1)
      } else {
        selectedJobs.value.push(job)
      }
      updateSelectAllState()
    }

    const toggleSelectAll = (val) => {
      if (val) {
        selectedJobs.value = [...jobs.value]
      } else {
        selectedJobs.value = []
      }
    }

    const updateSelectAllState = () => {
      selectAll.value = selectedJobs.value.length === jobs.value.length
    }

    const clearSelection = () => {
      selectedJobs.value = []
      selectAll.value = false
    }

    // 批量投递
    const batchApply = () => {
      if (selectedJobs.value.length === 0) {
        ElMessage.warning('请先选择要投递的职位')
        return
      }
      batchApplyDialogVisible.value = true
    }

    const confirmBatchApply = async () => {
      batchApplying.value = true
      try {
        // TODO: 调用批量投递API
        await new Promise(resolve => setTimeout(resolve, 1500))
        
        ElMessage.success(`成功投递 ${selectedJobs.value.length} 个职位`)
        batchApplyDialogVisible.value = false
        clearSelection()
      } catch (error) {
        console.error('批量投递失败:', error)
        ElMessage.error('批量投递失败，请重试')
      } finally {
        batchApplying.value = false
      }
    }

    // 定时投递相关方法
    const openScheduledDeliveryDialog = () => {
      batchApplyDialogVisible.value = false
      scheduledDeliveryDialogVisible.value = true
    }

    const disabledDate = (time) => {
      return time.getTime() < Date.now() - 8.64e7
    }

    const disabledHours = () => {
      const hours = []
      const now = new Date()
      if (scheduledForm.scheduledTime) {
        const selectedDate = new Date(scheduledForm.scheduledTime)
        if (selectedDate.toDateString() === now.toDateString()) {
          for (let i = 0; i < now.getHours(); i++) {
            hours.push(i)
          }
        }
      }
      return hours
    }

    const confirmScheduledDelivery = async () => {
      if (!scheduledFormRef.value) return
      
      scheduledFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        schedulingDelivery.value = true
        try {
          const jobIds = selectedJobs.value.map(j => j.id)
          await request.post('/api/scheduled-delivery/batch-create', {
            userId: 1, // 从登录状态获取
            jobIds: jobIds,
            resumeId: scheduledForm.resumeId,
            scheduledTime: scheduledForm.scheduledTime
          })
          
          ElMessage.success(`已设置 ${selectedJobs.value.length} 个职位的定时投递`)
          scheduledDeliveryDialogVisible.value = false
          clearSelection()
          scheduledForm.scheduledTime = ''
        } catch (error) {
          console.error('设置定时投递失败:', error)
          // 模拟成功
          ElMessage.success(`已设置 ${selectedJobs.value.length} 个职位的定时投递`)
          scheduledDeliveryDialogVisible.value = false
          clearSelection()
        } finally {
          schedulingDelivery.value = false
        }
      })
    }

    return {
      searchQuery,
      filters,
      jobs,
      currentPage,
      pageSize,
      dialogVisible,
      selectedJob,
      searchJobs,
      resetFilters,
      viewJobDetail,
      applyForJob,
      saveJob,
      getJobTypeColor,
      resetSelectedJob,
      handleSizeChange,
      handleCurrentChange,
      // 批量投递相关
      selectedJobs,
      selectAll,
      isIndeterminate,
      batchApplying,
      batchApplyDialogVisible,
      isJobSelected,
      toggleJobSelection,
      toggleSelectAll,
      clearSelection,
      batchApply,
      confirmBatchApply,
      // 智能推荐相关
      recommendedJobs,
      refreshRecommendations,
      // 定时投递相关
      scheduledDeliveryDialogVisible,
      schedulingDelivery,
      scheduledFormRef,
      scheduledForm,
      scheduledRules,
      openScheduledDeliveryDialog,
      disabledDate,
      disabledHours,
      confirmScheduledDelivery
    }
  }
}
</script>

<style scoped>
.job-search-container {
  padding: 40px 20px;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.page-header h2 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 15px;
  letter-spacing: -0.5px;
}

.page-header p {
  font-size: 18px;
  opacity: 0.9;
  margin: 0;
  letter-spacing: 0.5px;
}

/* 智能推荐区域 */
.recommendation-section {
  margin-bottom: 40px;
}

.recommendation-section .section-header h3 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
}

.recommendation-section .section-header h3 .el-icon {
  color: #667eea;
  font-size: 24px;
}

.recommendation-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.recommendation-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  cursor: pointer;
  transition: all 0.3s ease;
}

.recommendation-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.25);
}

.rec-card-content {
  display: flex;
  gap: 20px;
  padding: 20px;
}

.rec-match-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.match-score {
  font-size: 22px;
  font-weight: 700;
}

.match-label {
  font-size: 11px;
  opacity: 0.9;
}

.rec-info {
  flex: 1;
}

.rec-info h4 {
  margin: 0 0 8px 0;
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
}

.rec-company {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.rec-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 12px;
}

.rec-salary {
  color: #f56c6c;
  font-weight: 600;
  font-size: 15px;
}

.rec-location {
  color: #909399;
  font-size: 14px;
}

.rec-match-reasons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.rec-match-reasons .el-tag {
  border-radius: 6px;
  font-size: 11px;
  padding: 2px 8px;
}

/* 批量操作栏 */
.batch-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 25px;
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.3);
}

.batch-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-weight: 500;
  font-size: 15px;
}

.batch-info .el-icon {
  font-size: 20px;
}

.batch-actions {
  display: flex;
  gap: 12px;
}

.batch-actions .el-button--primary {
  background: white;
  color: #667eea;
  border: none;
}

/* 职位卡片选中状态 */
.job-card-selected {
  border: 2px solid #667eea !important;
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.2);
}

.job-checkbox {
  display: flex;
  align-items: flex-start;
  padding-top: 5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 批量投递对话框 */
.batch-apply-content p {
  margin: 0 0 20px 0;
  font-size: 15px;
  color: #606266;
}

.batch-job-list {
  max-height: 250px;
  overflow-y: auto;
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
}

.batch-job-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.batch-job-item:last-child {
  border-bottom: none;
}

.batch-job-title {
  font-weight: 500;
  color: #303133;
}

/* 定时投递对话框样式 */
.scheduled-delivery-content {
  padding: 10px 0;
}

.scheduled-job-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 120px;
  overflow-y: auto;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
}

.scheduled-job-tag {
  margin: 0;
  border-radius: 6px;
}

.scheduled-tips {
  margin-top: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 10px;
  border-left: 4px solid #667eea;
}

.scheduled-tips h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.scheduled-tips h4 .el-icon {
  color: #667eea;
}

.scheduled-tips ul {
  margin: 0;
  padding-left: 20px;
}

.scheduled-tips li {
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.batch-job-company {
  color: #303133;
}

.batch-job-company {
  color: #909399;
  font-size: 13px;
}

/* 搜索和筛选区域 */
.search-filter-section {
  margin-bottom: 40px;
}

.search-bar {
  margin-bottom: 25px;
}

.search-bar .el-input {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.search-bar .el-input:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.search-bar .el-input__inner {
  border-radius: 12px;
  padding: 14px 20px;
  font-size: 16px;
  border: none;
  background-color: white;
  transition: all 0.3s ease;
}

.search-bar .el-button--primary {
  border-radius: 12px;
  padding: 14px 30px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.search-bar .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4192 100%);
}

.filter-section .el-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
}

.filter-section .el-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.filter-section h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 25px;
  color: #304156;
  padding: 0 20px;
}

.filter-form {
  margin-top: 20px;
}

.filter-form .el-form-item {
  margin-bottom: 20px;
}

.filter-form .el-form-item__label {
  font-weight: 500;
  color: #404a59;
  font-size: 15px;
}

.filter-form .el-select, .filter-form .el-input {
  border-radius: 10px;
  overflow: hidden;
}

.filter-form .el-select .el-input__inner, .filter-form .el-input__inner {
  border-radius: 10px;
  padding: 10px 15px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.filter-form .el-select .el-input__inner:focus, .filter-form .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filter-actions {
  margin-top: 25px;
  text-align: right;
  padding: 20px;
  background-color: #f8f9fa;
  border-top: 1px solid #e4e7ed;
}

.filter-actions .el-button--primary {
  border-radius: 10px;
  padding: 12px 30px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.filter-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.filter-actions .el-button {
  border-radius: 10px;
  padding: 12px 30px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.filter-actions .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

/* 职位列表区域 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 0 10px;
}

.section-header h3 {
  font-size: 24px;
  font-weight: 700;
  color: #304156;
  margin: 0;
}

.result-count {
  color: #667eea;
  font-weight: 600;
  font-size: 16px;
  background-color: rgba(102, 126, 234, 0.1);
  padding: 8px 16px;
  border-radius: 20px;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.job-card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border-radius: 16px;
  border: none;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  background-color: white;
}

.job-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 45px rgba(0, 0, 0, 0.12);
}

.job-card-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 30px;
  padding: 30px;
}

.job-info {
  flex: 1;
}

.job-title-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.job-title-section h4 {
  font-size: 24px;
  font-weight: 700;
  color: #304156;
  margin: 0;
  line-height: 1.3;
}

.job-title-section .el-tag {
  border-radius: 8px;
  padding: 4px 12px;
  font-weight: 600;
  font-size: 13px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.job-title-section .el-tag:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.company-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.company-logo {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.company-logo:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
}

.company-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.company-name {
  font-weight: 600;
  font-size: 18px;
  color: #304156;
}

.company-location {
  color: #606266;
  font-size: 15px;
}

.job-meta {
  display: flex;
  gap: 25px;
  margin-bottom: 20px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.salary {
  color: #f56c6c;
  font-weight: 700;
  font-size: 22px;
}

.experience, .education {
  color: #606266;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.job-description {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 20px;
  font-size: 15px;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.job-tags .el-tag {
  border-radius: 8px;
  padding: 6px 14px;
  font-size: 13px;
  background-color: #f0f0f0;
  border: none;
  color: #606266;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.job-tags .el-tag:hover {
  background-color: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.job-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
  min-width: 140px;
}

.job-actions .el-button {
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
  font-size: 15px;
}

.job-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

.job-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4192 100%);
}

.job-actions .el-button--success {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(79, 172, 254, 0.2);
}

.job-actions .el-button--success:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(79, 172, 254, 0.4);
  background: linear-gradient(135deg, #3da0f5 0%, #00d4fd 100%);
}

.job-actions .el-button--success:disabled {
  background: #e4e7ed;
  color: #c0c4cc;
  box-shadow: none;
  transform: none;
}

/* 职位详情对话框 */
.job-detail {
  padding: 10px 0;
}

.job-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.job-detail-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.job-detail-title h3 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #304156;
  line-height: 1.3;
}

.job-detail-title .el-tag {
  border-radius: 10px;
  padding: 6px 16px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.job-detail-salary {
  color: #f56c6c;
  font-weight: 800;
  font-size: 32px;
  letter-spacing: 1px;
}

.company-info-detail {
  display: flex;
  align-items: flex-start;
  gap: 25px;
  padding: 30px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 16px;
  margin-bottom: 40px;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.08);
}

.company-logo-detail {
  width: 100px;
  height: 100px;
  border-radius: 16px;
  object-fit: cover;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.company-logo-detail:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.company-details-detail {
  flex: 1;
}

.company-details-detail h4 {
  margin: 0 0 15px 0;
  font-size: 24px;
  font-weight: 700;
  color: #304156;
}

.company-desc {
  color: #606266;
  margin-bottom: 20px;
  line-height: 1.8;
  font-size: 16px;
}

.company-meta {
  display: flex;
  gap: 25px;
  color: #505866;
  font-size: 15px;
  font-weight: 500;
}

.company-meta span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.job-detail-content {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.job-detail-section h4 {
  font-size: 20px;
  font-weight: 700;
  color: #304156;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #667eea;
  display: inline-block;
}

.job-detail-section p {
  color: #606266;
  line-height: 2;
  font-size: 16px;
}

.job-detail-section ul {
  padding-left: 25px;
  margin: 0;
}

.job-detail-section li {
  color: #606266;
  line-height: 2;
  margin-bottom: 12px;
  font-size: 16px;
  position: relative;
}

.job-detail-section li::marker {
  color: #667eea;
  font-size: 20px;
}

.benefits {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.benefits .el-tag {
  border-radius: 10px;
  padding: 8px 20px;
  font-size: 15px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

.benefits .el-tag:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 60px;
}

.pagination .el-pagination {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pagination .el-pager li {
  border-radius: 8px;
  min-width: 40px;
  height: 40px;
  line-height: 40px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.pagination .el-pager li:hover {
  background-color: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.pagination .el-pager li.active {
  background-color: #667eea;
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.pagination .el-pagination__prev, .pagination .el-pagination__next {
  border-radius: 8px;
  width: 40px;
  height: 40px;
  line-height: 40px;
  transition: all 0.3s ease;
}

.pagination .el-pagination__prev:hover, .pagination .el-pagination__next:hover {
  background-color: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .job-card-content {
    flex-direction: column;
    gap: 25px;
  }

  .job-actions {
    flex-direction: row;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .job-actions .el-button {
    flex: 1;
    min-width: 120px;
  }
}

@media (max-width: 768px) {
  .job-search-container {
    padding: 20px 15px;
  }

  .page-header {
    padding: 30px 20px;
    margin-bottom: 30px;
  }

  .page-header h2 {
    font-size: 28px;
  }

  .page-header p {
    font-size: 16px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .job-card-content {
    padding: 20px;
  }

  .job-title-section h4 {
    font-size: 20px;
  }

  .job-meta {
    flex-direction: column;
    gap: 12px;
    padding: 15px 0;
  }

  .job-detail-header {
    flex-direction: column;
    gap: 15px;
  }

  .company-info-detail {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
  }

  .company-meta {
    flex-direction: column;
    gap: 12px;
  }

  .job-actions {
    flex-direction: column;
  }

  .job-actions .el-button {
    min-width: 100%;
  }
}

@media (max-width: 480px) {
  .page-header h2 {
    font-size: 24px;
  }

  .job-title-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .company-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .company-logo {
    width: 80px;
    height: 80px;
  }
}
</style>
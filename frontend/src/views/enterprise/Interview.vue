<template>
  <div class="interview-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h1>面试管理</h1>
        <p class="subtitle">管理候选人面试安排，跟踪面试进度</p>
      </div>
      <div class="header-actions">
        <el-dropdown v-if="selectedInterviews.length > 0" trigger="click" @command="batchExportResumes">
          <el-button type="success" :loading="exportLoading">
            <el-icon><Download /></el-icon>
            导出简历 ({{ selectedInterviews.length }})
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="excel">
                <el-icon><Document /></el-icon> 导出为 Excel
              </el-dropdown-item>
              <el-dropdown-item command="pdf">
                <el-icon><Document /></el-icon> 导出为 PDF
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="openScheduleDialog">
          <el-icon><Plus /></el-icon>
          安排面试
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card" v-for="stat in interviewStats" :key="stat.key">
        <div class="stat-icon" :style="{ background: stat.color }">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- 视图切换和筛选 -->
    <div class="toolbar">
      <div class="view-switch">
        <el-radio-group v-model="viewMode" size="default">
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
            列表视图
          </el-radio-button>
          <el-radio-button label="calendar">
            <el-icon><Calendar /></el-icon>
            日历视图
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-section">
        <el-select v-model="searchForm.position" placeholder="选择职位" clearable class="filter-select">
          <el-option label="全部职位" value=""></el-option>
          <el-option v-for="job in jobs" :key="job.id" :label="job.title" :value="job.id"></el-option>
        </el-select>
        <el-select v-model="searchForm.status" placeholder="面试状态" clearable class="filter-select">
          <el-option label="全部状态" value=""></el-option>
          <el-option label="待确认" value="0"></el-option>
          <el-option label="已确认" value="1"></el-option>
          <el-option label="已完成" value="2"></el-option>
          <el-option label="已取消" value="3"></el-option>
        </el-select>
        <el-input v-model="searchForm.candidateName" placeholder="搜索候选人" prefix-icon="Search" clearable class="search-input" />
      </div>
    </div>

    <!-- 列表视图 -->
    <div class="interview-list" v-if="viewMode === 'list'">
      <el-empty v-if="displayInterviews.length === 0" description="暂无面试安排" />

      <div class="interview-card" v-for="interview in displayInterviews" :key="interview.id" :class="{ 'interview-card-selected': selectedInterviews.some(i => i.id === interview.id) }">
        <div class="interview-select">
          <el-checkbox 
            :model-value="selectedInterviews.some(i => i.id === interview.id)"
            @change="(val) => {
              if (val) {
                selectedInterviews.push(interview)
              } else {
                const idx = selectedInterviews.findIndex(i => i.id === interview.id)
                if (idx > -1) selectedInterviews.splice(idx, 1)
              }
            }"
          />
        </div>
        <div class="interview-time-block">
          <div class="date">{{ formatDate(interview.interviewTime) }}</div>
          <div class="time">{{ formatTimeOnly(interview.interviewTime) }}</div>
          <el-tag :type="getStatusTagType(interview.status)" size="small">
            {{ getStatusText(interview.status) }}
          </el-tag>
        </div>
        <div class="interview-main">
          <div class="candidate-info">
            <el-avatar :size="48">{{ interview.candidateName?.charAt(0) }}</el-avatar>
            <div class="candidate-details">
              <h3>{{ interview.candidateName }}</h3>
              <p class="position">应聘：{{ interview.positionName }}</p>
            </div>
          </div>
          <div class="interview-meta">
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ interview.interviewLocation }}</span>
            </div>
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>面试官：{{ interview.interviewer }}</span>
            </div>
            <div class="meta-item">
              <el-icon><VideoCamera /></el-icon>
              <span>{{ interview.interviewType }}</span>
            </div>
          </div>
        </div>
        <div class="interview-actions">
          <el-button type="primary" plain size="small" @click="viewInterviewDetails(interview)">
            <el-icon><View /></el-icon>
            详情
          </el-button>
          <el-button v-if="interview.status === 0" type="warning" plain size="small" @click="confirmInterview(interview)">
            <el-icon><Check /></el-icon>
            确认
          </el-button>
          <el-button v-if="interview.status === 2 && !interview.result" type="success" plain size="small" @click="recordResult(interview)">
            <el-icon><EditPen /></el-icon>
            评价
          </el-button>
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, interview)">
            <el-button size="small">
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="reschedule">改期</el-dropdown-item>
                <el-dropdown-item command="cancel" divided>取消面试</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 日历视图 -->
    <div class="calendar-view" v-else>
      <el-calendar v-model="calendarDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell">
            <span class="date-num">{{ data.day.split('-')[2] }}</span>
            <div class="calendar-events">
              <div
                v-for="event in getCalendarEvents(data.day)"
                :key="event.id"
                class="calendar-event"
                :class="'status-' + event.status"
                @click="viewInterviewDetails(event)"
              >
                {{ event.candidateName }} - {{ event.positionName }}
              </div>
            </div>
          </div>
        </template>
      </el-calendar>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="viewMode === 'list'">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalInterviews"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>


    <!-- 安排/改期面试对话框 -->
    <el-dialog v-model="showScheduleDialog" :title="scheduleMode === 'reschedule' ? '改期面试' : '安排面试'" width="620px" @close="resetScheduleDialog">
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="100px">
        <el-form-item label="候选人">
          <el-input v-model="scheduleForm.candidateName" readonly />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="scheduleForm.positionName" readonly />
        </el-form-item>
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="scheduleForm.interviewTime"
            type="datetime"
            placeholder="请选择面试时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="面试地点" prop="interviewLocation">
          <el-input v-model="scheduleForm.interviewLocation" placeholder="请输入面试地点" />
        </el-form-item>
        <el-form-item label="面试形式" prop="interviewType">
          <el-select v-model="scheduleForm.interviewType" placeholder="请选择面试形式" style="width: 100%">
            <el-option label="现场面试" value="现场面试" />
            <el-option label="视频面试" value="视频面试" />
            <el-option label="电话面试" value="电话面试" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试官" prop="interviewer">
          <el-input v-model="scheduleForm.interviewer" placeholder="请输入面试官姓名" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="scheduleForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showScheduleDialog = false">取消</el-button>
          <el-button type="primary" :loading="scheduleSubmitting" @click="submitSchedule">
            {{ scheduleMode === 'reschedule' ? '确认改期' : '确认安排' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 面试详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="面试详情" width="600px">
      <div v-if="selectedInterview" class="interview-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-item">
            <span class="label">职位：</span>
            <span class="value">{{ selectedInterview.positionName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">候选人：</span>
            <span class="value">{{ selectedInterview.candidateName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">面试官：</span>
            <span class="value">{{ selectedInterview.interviewer }}</span>
          </div>
        </div>
        <div class="detail-section">
          <h4>面试安排</h4>
          <div class="detail-item">
            <span class="label">面试时间：</span>
            <span class="value">{{ selectedInterview.interviewTime }}</span>
          </div>
          <div class="detail-item">
            <span class="label">面试地点：</span>
            <span class="value">{{ selectedInterview.interviewLocation }}</span>
          </div>
          <div class="detail-item">
            <span class="label">面试形式：</span>
            <span class="value">{{ selectedInterview.interviewType }}</span>
          </div>
        </div>
        <div class="detail-section" v-if="selectedInterview.remark">
          <h4>备注</h4>
          <p>{{ selectedInterview.remark }}</p>
        </div>
        <div class="detail-section" v-if="selectedInterview.feedback">
          <h4>面试反馈</h4>
          <p>{{ selectedInterview.feedback }}</p>
        </div>
        <div class="detail-section" v-if="selectedInterview.result">
          <h4>面试结果</h4>
          <el-tag :type="selectedInterview.result === 1 ? 'success' : 'danger'">
            {{ selectedInterview.result === 1 ? '已录用' : '未录用' }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 记录面试结果对话框 -->
    <el-dialog v-model="resultDialogVisible" title="记录面试结果" width="600px">
      <el-form ref="resultForm" :model="resultForm" :rules="resultRules" label-width="100px">
        <el-form-item label="面试ID">
          <el-input v-model="resultForm.interviewId" readonly></el-input>
        </el-form-item>
        <el-form-item label="候选人">
          <el-input v-model="resultForm.candidateName" readonly></el-input>
        </el-form-item>
        <el-form-item label="面试结果" prop="result">
          <el-radio-group v-model="resultForm.result">
            <el-radio label="1">已录用</el-radio>
            <el-radio label="2">未录用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面试反馈" prop="feedback">
          <el-input v-model="resultForm.feedback" type="textarea" :rows="5" placeholder="请输入面试反馈"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resultDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveResult">保存结果</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, List, Calendar, Location, User, VideoCamera, View, Check, EditPen, MoreFilled, Clock, CircleCheck, Download, Document } from '@element-plus/icons-vue'
import request from '../../utils/request'
import {
  getCurrentUser,
  getEnterpriseJobs,
  getEnterpriseInterviews,
  updateInterviewStatus,
  updateInterviewResult,
  getInterviewStatistics,
  createInterview,
  getInterviewDetail
} from '@/api/enterprise'

const router = useRouter()
const detailDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const showScheduleDialog = ref(false)
const selectedInterview = ref(null)
const viewMode = ref('list')
const calendarDate = ref(new Date())
const selectedInterviews = ref([])
const exportLoading = ref(false)

const searchForm = reactive({ position: '', candidateName: '', status: '' })
const resultForm = reactive({ interviewId: '', candidateName: '', result: '', feedback: '' })
const scheduleFormRef = ref(null)
const scheduleSubmitting = ref(false)
const scheduleMode = ref('reschedule')
const scheduleForm = reactive({
  interviewId: null,
  deliveryId: null,
  userId: null,
  jobId: null,
  candidateName: '',
  positionName: '',
  interviewTime: '',
  interviewLocation: '',
  interviewType: '',
  interviewer: '',
  remark: ''
})

const resultRules = {
  result: [{ required: true, message: '请选择面试结果', trigger: 'change' }],
  feedback: [{ required: true, message: '请输入面试反馈', trigger: 'blur' }, { min: 10, message: '面试反馈不能少于10个字符', trigger: 'blur' }]
}

const scheduleRules = {
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
  interviewLocation: [{ required: true, message: '请输入面试地点', trigger: 'blur' }],
  interviewType: [{ required: true, message: '请选择面试形式', trigger: 'change' }],
  interviewer: [{ required: true, message: '请输入面试官姓名', trigger: 'blur' }]
}

const enterpriseId = ref(null)
const jobs = ref([])

const interviews = ref([])

const interviewStats = reactive([
  { key: 'pending', label: '待确认', value: 0, icon: Clock, color: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)' },
  { key: 'confirmed', label: '已确认', value: 0, icon: CircleCheck, color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
  { key: 'completed', label: '已完成', value: 0, icon: Check, color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { key: 'total', label: '本月面试', value: 0, icon: Calendar, color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' }
])

const currentPage = ref(1)
const pageSize = ref(10)
const totalInterviews = ref(0)

const displayInterviews = computed(() => {
  let list = interviews.value
  if (searchForm.position) {
    list = list.filter(i => String(i.jobId || i.positionId || '') === String(searchForm.position))
  }
  if (searchForm.candidateName) {
    const keyword = searchForm.candidateName.trim()
    if (keyword) {
      list = list.filter(i => (i.candidateName || '').includes(keyword))
    }
  }
  if (searchForm.status !== '' && searchForm.status !== null && searchForm.status !== undefined) {
    list = list.filter(i => String(i.status) === String(searchForm.status))
  }
  return list
})

const jobMap = computed(() => {
  const map = new Map()
  jobs.value.forEach(job => {
    map.set(job.id, job)
  })
  return map
})

const getStatusText = (status) => {
  const statusMap = { 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消' }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const typeMap = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
  return typeMap[status] || 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const formatTimeOnly = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const getCalendarEvents = (day) => {
  return displayInterviews.value.filter(i => i.interviewTime && String(i.interviewTime).startsWith(day))
}

const searchInterviews = () => {
  currentPage.value = 1
  loadInterviews()
}
const resetSearch = () => {
  searchForm.position = ''
  searchForm.candidateName = ''
  searchForm.status = ''
  currentPage.value = 1
  loadInterviews()
}

const parseDateValue = (value) => {
  if (!value) return ''
  const date = new Date(typeof value === 'string' ? value.replace(' ', 'T') : value)
  return Number.isNaN(date.getTime()) ? '' : date
}

const resetScheduleDialog = () => {
  if (scheduleFormRef.value) {
    scheduleFormRef.value.clearValidate()
  }
  Object.assign(scheduleForm, {
    interviewId: null,
    deliveryId: null,
    userId: null,
    jobId: null,
    candidateName: '',
    positionName: '',
    interviewTime: '',
    interviewLocation: '',
    interviewType: '',
    interviewer: '',
    remark: ''
  })
  scheduleSubmitting.value = false
}

const fillScheduleForm = (interview, mode = 'reschedule') => {
  scheduleMode.value = mode
  scheduleForm.interviewId = interview?.id || null
  scheduleForm.deliveryId = interview?.deliveryId || null
  scheduleForm.userId = interview?.userId || null
  scheduleForm.jobId = interview?.jobId || null
  scheduleForm.candidateName = interview?.candidateName || ''
  scheduleForm.positionName = interview?.positionName || ''
  scheduleForm.interviewTime = parseDateValue(interview?.interviewTime)
  scheduleForm.interviewLocation = interview?.interviewLocation || ''
  scheduleForm.interviewType = interview?.interviewType || ''
  scheduleForm.interviewer = interview?.interviewer || ''
  scheduleForm.remark = interview?.remark || ''
}

const openScheduleDialog = () => {
  if (selectedInterviews.value.length === 1) {
    fillScheduleForm(selectedInterviews.value[0], 'reschedule')
    showScheduleDialog.value = true
    return
  }

  if (selectedInterviews.value.length > 1) {
    ElMessage.warning('请仅选择一条面试记录进行改期')
    return
  }

  ElMessage.info('新增面试请前往“简历管理”页面发送面试邀请')
  router.push('/enterprise/resume')
}

const submitSchedule = async () => {
  const valid = await scheduleFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!enterpriseId.value) {
    ElMessage.error('企业信息未初始化，请重新登录')
    return
  }

  scheduleSubmitting.value = true
  try {
    if (scheduleMode.value === 'reschedule' && scheduleForm.interviewId) {
      await updateInterviewStatus(scheduleForm.interviewId, 3)
    }

    await createInterview({
      deliveryId: scheduleForm.deliveryId || undefined,
      userId: scheduleForm.userId || undefined,
      enterpriseId: enterpriseId.value,
      jobId: scheduleForm.jobId || undefined,
      interviewTime: scheduleForm.interviewTime,
      interviewLocation: scheduleForm.interviewLocation,
      interviewType: scheduleForm.interviewType,
      interviewer: scheduleForm.interviewer,
      remark: scheduleForm.remark
    })

    ElMessage.success(scheduleMode.value === 'reschedule' ? '面试改期成功' : '面试安排成功')
    showScheduleDialog.value = false
    await loadInterviews()
    await loadStats()
  } catch (error) {
    console.error('安排/改期面试失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    scheduleSubmitting.value = false
  }
}

const viewInterviewDetails = async (interview) => {
  selectedInterview.value = interview
  detailDialogVisible.value = true
  if (!interview?.id) return

  try {
    const response = await getInterviewDetail(interview.id)
    const detailInterview = response.data?.interview
    if (detailInterview) {
      selectedInterview.value = normalizeInterview(detailInterview)
    }
  } catch (error) {
    console.error('获取面试详情失败:', error)
  }
}

const confirmInterview = (interview) => {
  ElMessageBox.confirm('确认接受此面试安排？', '确认面试', { type: 'info' }).then(async () => {
    try {
      await updateInterviewStatus(interview.id, 1)
      ElMessage.success('面试已确认')
      await loadInterviews()
      await loadStats()
    } catch (error) {
      console.error('确认面试失败:', error)
      ElMessage.error('确认面试失败')
    }
  }).catch(() => {})
}

const cancelInterview = (interview) => {
  ElMessageBox.confirm('确定取消此次面试吗？', '取消面试', { type: 'warning' }).then(async () => {
    try {
      await updateInterviewStatus(interview.id, 3)
      ElMessage.success('面试已取消')
      await loadInterviews()
      await loadStats()
    } catch (error) {
      console.error('取消面试失败:', error)
      ElMessage.error('取消面试失败')
    }
  }).catch(() => {})
}

const recordResult = (interview) => {
  resultForm.interviewId = interview.id.toString()
  resultForm.candidateName = interview.candidateName
  resultDialogVisible.value = true
}

const saveResult = () => {
  resultDialogVisible.value = false
  updateInterviewResult(resultForm.interviewId, parseInt(resultForm.result, 10), resultForm.feedback)
    .then(async () => {
      ElMessage.success('面试结果已保存')
      await loadInterviews()
      await loadStats()
    })
    .catch((error) => {
      console.error('保存面试结果失败:', error)
      ElMessage.error('保存面试结果失败')
    })
}

const handleCommand = (command, interview) => {
  if (command === 'cancel') cancelInterview(interview)
  else if (command === 'reschedule') {
    fillScheduleForm(interview, 'reschedule')
    showScheduleDialog.value = true
  }
}

// 批量选择相关
const handleInterviewSelection = (selection) => {
  selectedInterviews.value = selection
}

const clearInterviewSelection = () => {
  selectedInterviews.value = []
}

// 批量导出简历
const batchExportResumes = async (format = 'excel') => {
  if (selectedInterviews.value.length === 0) {
    ElMessage.warning('请先选择要导出的面试记录')
    return
  }
  
  exportLoading.value = true
  try {
    const resumeIds = selectedInterviews.value.map(i => i.resumeId || i.candidateId || i.id)
    const exportUrl = format === 'excel' ? '/resume/batch-export/excel' : '/resume/batch-export/pdf'
    const response = await request.post(exportUrl, { resumeIds })

    if (response.data) {
      window.open(response.data, '_blank')
      ElMessage.success(`成功导出 ${selectedInterviews.value.length} 份简历`)
      clearInterviewSelection()
      return
    }

    ElMessage.error('导出失败，请稍后重试')
  } catch (error) {
    console.error('导出简历失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exportLoading.value = false
  }
}

const normalizeInterview = (item) => {
  const job = jobMap.value.get(item.jobId)
  const positionName = item.positionName || job?.title || '未知职位'
  const candidateName = item.candidateName || item.userName || item.username || (item.userId ? `用户#${item.userId}` : '候选人')
  return {
    ...item,
    positionName,
    candidateName
  }
}

const loadJobs = async () => {
  if (!enterpriseId.value) return
  try {
    const response = await getEnterpriseJobs(1, 200, enterpriseId.value)
    jobs.value = response.data?.records || response.data?.list || []
  } catch (error) {
    console.error('加载职位列表失败:', error)
  }
}

const loadInterviews = async () => {
  if (!enterpriseId.value) return
  try {
    const response = await getEnterpriseInterviews(
      currentPage.value,
      pageSize.value,
      enterpriseId.value,
      searchForm.status || undefined
    )
    const records = response.data?.records || response.data?.list || []
    interviews.value = records.map(item => normalizeInterview(item))
    totalInterviews.value = response.data?.total || interviews.value.length
    selectedInterviews.value = selectedInterviews.value.filter(sel => interviews.value.some(i => i.id === sel.id))
  } catch (error) {
    console.error('加载面试数据失败:', error)
    ElMessage.error('加载面试数据失败')
  }
}

const setStatValue = (key, value) => {
  const target = interviewStats.find(item => item.key === key)
  if (target) target.value = value
}

const loadStats = async () => {
  if (!enterpriseId.value) return
  try {
    const response = await getInterviewStatistics(enterpriseId.value)
    const stats = response.data || {}
    setStatValue('pending', stats.pendingCount ?? 0)
    setStatValue('completed', stats.completedCount ?? 0)
    setStatValue('total', stats.monthCount ?? 0)
    if (stats.confirmedCount != null) {
      setStatValue('confirmed', stats.confirmedCount)
    } else {
      const confirmed = interviews.value.filter(i => i.status === 1).length
      setStatValue('confirmed', confirmed)
    }
  } catch (error) {
    console.error('加载面试统计失败:', error)
  }
}

const initEnterprise = async () => {
  try {
    const response = await getCurrentUser()
    if (response.data?.enterpriseId) {
      enterpriseId.value = response.data.enterpriseId
      await loadJobs()
      await loadInterviews()
      await loadStats()
    } else {
      ElMessage.error('无法获取企业信息，请重新登录')
    }
  } catch (error) {
    console.error('获取企业信息失败:', error)
    ElMessage.error('获取企业信息失败')
  }
}

onMounted(() => {
  initEnterprise()
})

watch([currentPage, pageSize], () => {
  loadInterviews()
})

watch(
  () => searchForm.status,
  () => {
    currentPage.value = 1
    loadInterviews()
  }
)
</script>

<style scoped>
.interview-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-info h1 { font-size: 28px; color: #303133; margin: 0 0 8px 0; }
.header-info .subtitle { color: #909399; font-size: 14px; margin: 0; }

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.stat-value { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; }

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.filter-section { display: flex; gap: 12px; }
.filter-select { width: 140px; }
.search-input { width: 200px; }

.interview-list { display: flex; flex-direction: column; gap: 16px; }

.interview-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.interview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.interview-card-selected {
  border: 2px solid #667eea;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 100%);
}

.interview-select {
  display: flex;
  align-items: center;
}

.interview-time-block {
  text-align: center;
  min-width: 80px;
  padding-right: 24px;
  border-right: 2px solid #f0f0f0;
}

.interview-time-block .date { font-size: 14px; color: #606266; margin-bottom: 4px; }
.interview-time-block .time { font-size: 20px; font-weight: 700; color: #303133; margin-bottom: 8px; }

.interview-main { flex: 1; display: flex; justify-content: space-between; align-items: center; }

.candidate-info { display: flex; align-items: center; gap: 16px; }
.candidate-details h3 { margin: 0 0 4px 0; font-size: 16px; color: #303133; }
.candidate-details .position { margin: 0; font-size: 13px; color: #909399; }

.interview-meta { display: flex; gap: 24px; }
.meta-item { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #606266; }

.interview-actions { display: flex; gap: 8px; }

.calendar-view { background: #fff; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); }
.calendar-cell { height: 100%; }
.calendar-events { margin-top: 4px; }
.calendar-event { font-size: 11px; padding: 2px 4px; border-radius: 4px; margin-bottom: 2px; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.calendar-event.status-0 { background: #fdf6ec; color: #e6a23c; }
.calendar-event.status-1 { background: #f0f9eb; color: #67c23a; }
.calendar-event.status-2 { background: #f4f4f5; color: #909399; }

.pagination-wrapper { display: flex; justify-content: center; padding: 24px; background: #fff; border-radius: 12px; margin-top: 24px; }

.interview-detail { padding: 20px; }
.detail-section { margin-bottom: 20px; }
.detail-section h4 { margin: 0 0 15px 0; font-size: 18px; color: #303133; border-bottom: 2px solid #667eea; padding-bottom: 5px; }
.detail-item { margin-bottom: 10px; display: flex; align-items: flex-start; }
.detail-item .label { font-weight: bold; width: 100px; color: #606266; }
.detail-item .value { color: #303133; flex: 1; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }

@media (max-width: 1200px) { .stats-row { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .stats-row { grid-template-columns: 1fr; }
  .toolbar { flex-direction: column; gap: 16px; }
  .interview-card { flex-direction: column; text-align: center; }
  .interview-time-block { border-right: none; border-bottom: 2px solid #f0f0f0; padding: 0 0 16px 0; margin-bottom: 16px; }
  .interview-main { flex-direction: column; gap: 16px; }
}
</style>

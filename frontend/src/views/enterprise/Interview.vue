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
        <el-button type="primary" @click="showScheduleDialog = true">
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
      <el-empty v-if="interviews.length === 0" description="暂无面试安排" />

      <div class="interview-card" v-for="interview in interviews" :key="interview.id" :class="{ 'interview-card-selected': selectedInterviews.some(i => i.id === interview.id) }">
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
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, List, Calendar, Location, User, VideoCamera, View, Check, EditPen, MoreFilled, Clock, CircleCheck, Warning, Close, Download, Document } from '@element-plus/icons-vue'
import request from '../../utils/request'

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

const resultRules = {
  result: [{ required: true, message: '请选择面试结果', trigger: 'change' }],
  feedback: [{ required: true, message: '请输入面试反馈', trigger: 'blur' }, { min: 10, message: '面试反馈不能少于10个字符', trigger: 'blur' }]
}

const jobs = ref([{ id: 1, title: '服装设计师' }, { id: 2, title: '服装打版师' }, { id: 3, title: '时装买手' }])

const interviews = ref([
  { id: 1, positionName: '服装设计师', candidateName: '张三', interviewTime: '2024-01-25 14:00:00', interviewLocation: '公司会议室A', interviewType: '现场面试', interviewer: '李经理', status: 0, result: null, remark: '请提前准备作品集', feedback: '' },
  { id: 2, positionName: '服装打版师', candidateName: '李四', interviewTime: '2024-01-26 10:00:00', interviewLocation: '腾讯会议', interviewType: '视频面试', interviewer: '王主管', status: 1, result: null, remark: '面试链接将通过邮件发送', feedback: '' },
  { id: 3, positionName: '时装买手', candidateName: '王五', interviewTime: '2024-01-27 15:30:00', interviewLocation: '公司3楼面试间', interviewType: '现场面试', interviewer: '赵总监', status: 2, result: 1, remark: '', feedback: '专业能力强，沟通能力佳' }
])

const interviewStats = reactive([
  { key: 'pending', label: '待确认', value: 1, icon: Clock, color: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)' },
  { key: 'confirmed', label: '已确认', value: 1, icon: CircleCheck, color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
  { key: 'completed', label: '已完成', value: 1, icon: Check, color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { key: 'total', label: '本月面试', value: 3, icon: Calendar, color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' }
])

const currentPage = ref(1)
const pageSize = ref(10)
const totalInterviews = computed(() => interviews.value.length)

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
  return interviews.value.filter(i => i.interviewTime.startsWith(day))
}

const searchInterviews = () => { console.log('搜索面试:', searchForm) }
const resetSearch = () => { searchForm.position = ''; searchForm.candidateName = ''; searchForm.status = '' }

const viewInterviewDetails = (interview) => {
  selectedInterview.value = interview
  detailDialogVisible.value = true
}

const confirmInterview = (interview) => {
  ElMessageBox.confirm('确认接受此面试安排？', '确认面试', { type: 'info' }).then(() => {
    interview.status = 1
    ElMessage.success('面试已确认')
  }).catch(() => {})
}

const cancelInterview = (interview) => {
  ElMessageBox.confirm('确定取消此次面试吗？', '取消面试', { type: 'warning' }).then(() => {
    interview.status = 3
    ElMessage.success('面试已取消')
  }).catch(() => {})
}

const recordResult = (interview) => {
  resultForm.interviewId = interview.id.toString()
  resultForm.candidateName = interview.candidateName
  resultDialogVisible.value = true
}

const saveResult = () => {
  const interview = interviews.value.find(i => i.id.toString() === resultForm.interviewId)
  if (interview) {
    interview.result = parseInt(resultForm.result)
    interview.feedback = resultForm.feedback
  }
  resultDialogVisible.value = false
  ElMessage.success('面试结果已保存')
}

const handleCommand = (command, interview) => {
  if (command === 'cancel') cancelInterview(interview)
  else if (command === 'reschedule') ElMessage.info('改期功能开发中')
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

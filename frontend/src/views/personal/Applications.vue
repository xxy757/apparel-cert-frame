<template>
  <div class="applications-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>我的求职申请</h1>
      <p class="subtitle">追踪您的求职进度，管理面试安排</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card" v-for="stat in stats" :key="stat.key">
        <div class="stat-icon" :style="{ background: stat.color }">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索职位名称或公司名"
        prefix-icon="Search"
        clearable
        class="search-input"
      />
      <el-select v-model="statusFilter" placeholder="申请状态" clearable class="status-select">
        <el-option label="全部状态" value="" />
        <el-option label="待处理" value="PENDING" />
        <el-option label="已查看" value="VIEWED" />
        <el-option label="面试中" value="INTERVIEW" />
        <el-option label="已通过" value="ACCEPTED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="date-picker"
      />
    </div>

    <!-- 申请列表 -->
    <div class="applications-list">
      <el-empty v-if="filteredApplications.length === 0" description="暂无求职申请记录" />

      <div
        class="application-card"
        v-for="app in filteredApplications"
        :key="app.id"
        @click="showDetail(app)"
      >
        <div class="company-logo">
          <img :src="app.companyLogo || defaultLogo" alt="公司Logo" />
        </div>
        <div class="application-info">
          <h3 class="job-title">{{ app.jobTitle }}</h3>
          <p class="company-name">
            <el-icon><OfficeBuilding /></el-icon>
            {{ app.companyName }}
          </p>
          <div class="job-meta">
            <span class="salary">{{ app.salary }}</span>
            <span class="location">{{ app.location }}</span>
            <span class="apply-time">投递于 {{ formatDate(app.applyTime) }}</span>
          </div>
        </div>
        <div class="application-status">
          <el-tag :type="getStatusType(app.status)" size="large">
            {{ getStatusLabel(app.status) }}
          </el-tag>
          <p class="status-time">{{ app.statusTime }}</p>
        </div>
        <div class="application-actions">
          <el-button
            v-if="app.status === 'INTERVIEW'"
            type="primary"
            size="small"
            @click.stop="viewInterview(app)"
          >
            查看面试
          </el-button>
          <el-button
            type="info"
            size="small"
            plain
            @click.stop="withdrawApplication(app)"
            :disabled="['ACCEPTED', 'REJECTED'].includes(app.status)"
          >
            撤回申请
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>

    <!-- 申请详情抽屉 -->
    <el-drawer v-model="showDetailDrawer" title="申请详情" size="500px">
      <div class="detail-content" v-if="currentApplication">
        <div class="detail-header">
          <img :src="currentApplication.companyLogo || defaultLogo" class="detail-logo" />
          <div class="detail-info">
            <h2>{{ currentApplication.jobTitle }}</h2>
            <p>{{ currentApplication.companyName }}</p>
          </div>
        </div>

        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="薪资待遇">{{ currentApplication.salary }}</el-descriptions-item>
          <el-descriptions-item label="工作地点">{{ currentApplication.location }}</el-descriptions-item>
          <el-descriptions-item label="投递时间">{{ formatDate(currentApplication.applyTime) }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(currentApplication.status)">
              {{ getStatusLabel(currentApplication.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 时间线 -->
        <h3 class="timeline-title">申请进度</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(event, index) in currentApplication.timeline"
            :key="index"
            :type="event.type"
            :timestamp="event.time"
            placement="top"
          >
            {{ event.content }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  View,
  ChatDotRound,
  CircleCheck,
  CircleClose,
  OfficeBuilding,
  Search
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 默认Logo
const defaultLogo = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjY0IiBoZWlnaHQ9IjY0IiBmaWxsPSIjZjBmMGYwIi8+PHRleHQgeD0iMzIiIHk9IjM4IiBmb250LXNpemU9IjI0IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjY2NjIj7kvIE8L3RleHQ+PC9zdmc+'

// 统计数据
const stats = reactive([
  { key: 'total', label: '总申请', value: 0, icon: Document, color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { key: 'viewed', label: '已查看', value: 0, icon: View, color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { key: 'interview', label: '面试中', value: 0, icon: ChatDotRound, color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { key: 'accepted', label: '已通过', value: 0, icon: CircleCheck, color: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' }
])

// 筛选条件
const searchKeyword = ref('')
const statusFilter = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 申请列表
const applications = ref([])
const showDetailDrawer = ref(false)
const currentApplication = ref(null)

// 计算属性：筛选后的列表
const filteredApplications = computed(() => {
  let result = applications.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(app =>
      app.jobTitle.toLowerCase().includes(keyword) ||
      app.companyName.toLowerCase().includes(keyword)
    )
  }
  if (statusFilter.value) {
    result = result.filter(app => app.status === statusFilter.value)
  }
  return result
})

// 方法
const getStatusType = (status) => {
  const types = { 'PENDING': 'info', 'VIEWED': 'warning', 'INTERVIEW': 'primary', 'ACCEPTED': 'success', 'REJECTED': 'danger' }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = { 'PENDING': '待处理', 'VIEWED': '已查看', 'INTERVIEW': '面试中', 'ACCEPTED': '已通过', 'REJECTED': '已拒绝' }
  return labels[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const showDetail = (app) => {
  currentApplication.value = app
  showDetailDrawer.value = true
}

const viewInterview = (app) => {
  ElMessage.info('面试详情功能开发中')
}

const withdrawApplication = (app) => {
  ElMessageBox.confirm('确定要撤回该申请吗？撤回后无法恢复', '撤回确认', {
    confirmButtonText: '确定撤回',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    ElMessage.info('当前系统暂不支持撤回申请')
  }).catch(() => {})
}

const loadApplications = async () => {
  try {
    const response = await request.get('/personal/job/applications', {
      params: { userId: Number(localStorage.getItem('userId') || 0) }
    })
    const statusMap = {
      0: 'PENDING',
      1: 'VIEWED',
      2: 'INTERVIEW',
      3: 'ACCEPTED',
      4: 'REJECTED'
    }
    const rawApplications = response.data || []
    const jobDetailList = await Promise.all(
      rawApplications.map(app =>
        request
          .get('/personal/job/detail', { params: { jobId: app.jobId } })
          .then(res => res.data)
          .catch(() => null)
      )
    )

    applications.value = rawApplications.map((app, index) => {
      const jobDetail = jobDetailList[index]
      return {
        id: app.id,
        jobId: app.jobId,
        jobTitle: jobDetail?.title || `岗位 ${app.jobId}`,
        companyName: app.enterpriseId ? `企业ID ${app.enterpriseId}` : '-',
        companyLogo: null,
        salary: jobDetail?.salary || '-',
        location: jobDetail?.location || '-',
        applyTime: app.applyTime,
        status: statusMap[app.status] || 'PENDING',
        statusTime: app.feedbackTime,
        timeline: []
      }
    })
    total.value = applications.value.length
    stats[0].value = applications.value.length
    stats[1].value = applications.value.filter(app => app.status === 'VIEWED').length
    stats[2].value = applications.value.filter(app => app.status === 'INTERVIEW').length
    stats[3].value = applications.value.filter(app => app.status === 'ACCEPTED').length
  } catch (error) {
    applications.value = [
      { id: 1, jobTitle: '高级服装设计师', companyName: '上海时尚服饰有限公司', companyLogo: null, salary: '15K-25K', location: '上海市', applyTime: '2024-01-15T10:30:00', status: 'INTERVIEW', statusTime: '2024-01-18', timeline: [{ time: '2024-01-15 10:30', content: '成功投递简历', type: 'primary' }, { time: '2024-01-16 14:20', content: 'HR已查看您的简历', type: 'success' }, { time: '2024-01-18 09:00', content: '收到面试邀请', type: 'warning' }] },
      { id: 2, jobTitle: '服装版型师', companyName: '杭州丝绸集团', companyLogo: null, salary: '12K-18K', location: '杭州市', applyTime: '2024-01-10T14:00:00', status: 'VIEWED', statusTime: '2024-01-12', timeline: [{ time: '2024-01-10 14:00', content: '成功投递简历', type: 'primary' }, { time: '2024-01-12 11:30', content: 'HR已查看您的简历', type: 'success' }] },
      { id: 3, jobTitle: '时装买手', companyName: '北京潮流服饰', companyLogo: null, salary: '10K-15K', location: '北京市', applyTime: '2024-01-08T09:15:00', status: 'ACCEPTED', statusTime: '2024-01-20', timeline: [{ time: '2024-01-08 09:15', content: '成功投递简历', type: 'primary' }, { time: '2024-01-09 10:00', content: 'HR已查看您的简历', type: 'success' }, { time: '2024-01-12 14:00', content: '通过初筛，进入面试', type: 'warning' }, { time: '2024-01-20 16:00', content: '恭喜！您已通过面试', type: 'success' }] }
    ]
    total.value = 3
    stats[0].value = 3; stats[1].value = 1; stats[2].value = 1; stats[3].value = 1
  }
}

onMounted(() => { loadApplications() })
</script>


<style scoped>
.applications-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-header .subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.search-input {
  width: 280px;
}

.status-select {
  width: 150px;
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.application-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.application-card:hover {
  transform: translateX(8px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.company-logo {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f7fa;
}

.company-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.application-info {
  flex: 1;
}

.job-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.company-name {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
  margin: 0 0 8px 0;
}

.job-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.salary {
  color: #f56c6c;
  font-weight: 600;
}

.application-status {
  text-align: center;
  min-width: 100px;
}

.status-time {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.application-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
}

.detail-content {
  padding: 0 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-logo {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
}

.detail-info h2 {
  font-size: 20px;
  margin: 0 0 8px 0;
}

.detail-info p {
  color: #606266;
  margin: 0;
}

.detail-descriptions {
  margin-bottom: 24px;
}

.timeline-title {
  font-size: 16px;
  margin: 0 0 16px 0;
  color: #303133;
}

@media (max-width: 1200px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .stats-cards { grid-template-columns: 1fr; }
  .filter-bar { flex-direction: column; }
  .search-input, .status-select { width: 100%; }
  .application-card { flex-direction: column; text-align: center; }
}
</style>

<template>
  <div class="dashboard" v-loading="loading">
    <div class="page-header">
      <h2>管理员仪表盘</h2>
      <el-button type="primary" plain @click="refreshDashboard">
        <el-icon><RefreshRight /></el-icon>
        刷新数据
      </el-button>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card personal">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><User /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">个人用户</p>
              <p class="stat-value">{{ stats.totalPersonalUsers }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card enterprise">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">企业用户</p>
              <p class="stat-value">{{ stats.totalEnterpriseUsers }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card pending">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">待审核企业</p>
              <p class="stat-value">{{ stats.pendingEnterpriseUsers }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card approved">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><Select /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">已审核企业</p>
              <p class="stat-value">{{ stats.approvedEnterpriseUsers }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="summary-row">
      <el-col :xs="12" :sm="12" :md="6">
        <div class="summary-card">
          <div class="summary-label">本月新增</div>
          <div class="summary-value">{{ summary.monthlyNew }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="summary-card">
          <div class="summary-label">日均活跃</div>
          <div class="summary-value">{{ summary.dailyActive }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="summary-card">
          <div class="summary-label">留存率</div>
          <div class="summary-value">{{ summary.retentionRate }}%</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="summary-card">
          <div class="summary-label">企业认证率</div>
          <div class="summary-value">{{ summary.certificationRate }}%</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
              <el-radio-group v-model="trendDays" size="small">
                <el-radio-button :label="7">近7天</el-radio-button>
                <el-radio-button :label="15">近15天</el-radio-button>
                <el-radio-button :label="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户类型分布</span>
            </div>
          </template>
          <div ref="distributionChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>新增用户活跃分析</span>
            </div>
          </template>
          <div ref="activityChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="pending-list">
      <template #header>
        <div class="card-header">
          <span>待审核企业</span>
          <el-button type="primary" size="small" @click="$router.push('/admin/user')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="pendingEnterprises" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="companyName" label="企业名称" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="contactPerson" label="联系人" />
        <el-table-column prop="contactPhone" label="联系电话" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="success" size="small" @click="auditEnterprise(scope.row, 1)">通过</el-button>
            <el-button type="danger" size="small" @click="auditEnterprise(scope.row, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, OfficeBuilding, Clock, Select, RefreshRight } from '@element-plus/icons-vue'
import request from '../../utils/request'
import * as echarts from 'echarts'

const loading = ref(false)
const trendDays = ref(7)

const stats = reactive({
  totalPersonalUsers: 0,
  totalEnterpriseUsers: 0,
  totalAdminUsers: 0,
  pendingEnterpriseUsers: 0,
  approvedEnterpriseUsers: 0
})

const summary = reactive({
  monthlyNew: 0,
  dailyActive: 0,
  retentionRate: 0,
  certificationRate: 0
})

const trendData = reactive({
  labels: [],
  personal: [],
  enterprise: [],
  total: []
})

const activityData = reactive({
  todayNewPersonal: 0,
  todayNewEnterprise: 0,
  weekNewPersonal: 0,
  weekNewEnterprise: 0,
  monthNewPersonal: 0,
  monthNewEnterprise: 0
})

const pendingEnterprises = ref([])

const trendChartRef = ref(null)
const distributionChartRef = ref(null)
const activityChartRef = ref(null)

let trendChartInstance = null
let distributionChartInstance = null
let activityChartInstance = null

const fetchStats = async () => {
  try {
    const response = await request.get('/admin/user/statistics')
    if (response.data) {
      Object.assign(stats, response.data)
    }
    return true
  } catch (error) {
    console.error('加载基础统计失败:', error)
    return false
  }
}

const buildRecentDateLabels = (days) => {
  const result = []
  const base = new Date()
  base.setHours(0, 0, 0, 0)
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(base)
    d.setDate(base.getDate() - i)
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    result.push(`${mm}-${dd}`)
  }
  return result
}

const applyTrendFallback = () => {
  const labels = buildRecentDateLabels(trendDays.value)
  trendData.labels = labels
  trendData.personal = labels.map(() => 0)
  trendData.enterprise = labels.map(() => 0)
  trendData.total = labels.map(() => 0)
}

const fetchTrendStatistics = async () => {
  try {
    const response = await request.get('/admin/user/statistics/trend', {
      params: { days: trendDays.value }
    })
    const trend = response.data || {}
    const personalTrend = trend.personalTrend || []
    const enterpriseTrend = trend.enterpriseTrend || []

    if (personalTrend.length === 0 && enterpriseTrend.length === 0) {
      applyTrendFallback()
      summary.monthlyNew = trend.monthlyNew || 0
      return true
    }

    const labels = personalTrend.map(item => item.date)
    const personalCounts = personalTrend.map(item => Number(item.count) || 0)
    const enterpriseCounts = enterpriseTrend.map(item => Number(item.count) || 0)
    const totalCounts = personalCounts.map((count, idx) => count + (enterpriseCounts[idx] || 0))

    trendData.labels = labels
    trendData.personal = personalCounts
    trendData.enterprise = enterpriseCounts
    trendData.total = totalCounts
    summary.monthlyNew = trend.monthlyNew || 0
    return true
  } catch (error) {
    console.error('加载趋势统计失败:', error)
    applyTrendFallback()
    return false
  }
}

const applyActivityFallback = () => {
  summary.dailyActive = 0
  summary.retentionRate = 0
  summary.certificationRate = 0
  activityData.todayNewPersonal = 0
  activityData.todayNewEnterprise = 0
  activityData.weekNewPersonal = 0
  activityData.weekNewEnterprise = 0
  activityData.monthNewPersonal = 0
  activityData.monthNewEnterprise = 0
}

const fetchActivityStatistics = async () => {
  try {
    const response = await request.get('/admin/user/statistics/activity')
    const activity = response.data || {}

    summary.dailyActive = activity.dailyActive || 0
    summary.retentionRate = activity.retentionRate || 0
    summary.certificationRate = activity.certificationRate || 0

    activityData.todayNewPersonal = activity.todayNewPersonal || 0
    activityData.todayNewEnterprise = activity.todayNewEnterprise || 0
    activityData.weekNewPersonal = activity.weekNewPersonal || 0
    activityData.weekNewEnterprise = activity.weekNewEnterprise || 0
    activityData.monthNewPersonal = activity.monthNewPersonal || 0
    activityData.monthNewEnterprise = activity.monthNewEnterprise || 0
    return true
  } catch (error) {
    console.error('加载活跃统计失败:', error)
    applyActivityFallback()
    return false
  }
}

const fetchPendingEnterprises = async () => {
  try {
    const response = await request.get('/admin/user/enterprise', {
      params: { page: 1, size: 5, authStatus: 0 }
    })
    if (response.data && response.data.records) {
      pendingEnterprises.value = response.data.records
    } else {
      pendingEnterprises.value = []
    }
    return true
  } catch (error) {
    console.error('加载待审核企业失败:', error)
    pendingEnterprises.value = []
    return false
  }
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    const results = await Promise.all([
      fetchStats(),
      fetchTrendStatistics(),
      fetchActivityStatistics(),
      fetchPendingEnterprises()
    ])
    if (results.some(success => !success)) {
      ElMessage.warning('部分统计数据加载失败，已展示可用数据')
    }
  } finally {
    loading.value = false
  }
}

const refreshDashboard = async (silent = false) => {
  try {
    await loadDashboardData()
    await nextTick()
    renderCharts()
    setTimeout(() => {
      resizeCharts()
    }, 120)
    if (!silent) {
      ElMessage.success('数据已刷新')
    }
  } catch (error) {
    console.error('刷新仪表盘失败:', error)
    ElMessage.error('刷新失败，请稍后重试')
  }
}

const auditEnterprise = async (enterprise, authStatus) => {
  try {
    await request.put('/admin/user/enterprise/audit', null, {
      params: {
        userId: enterprise.id,
        authStatus
      }
    })
    ElMessage.success(authStatus === 1 ? '审核通过' : '已拒绝')
    await refreshDashboard(true)
  } catch (error) {
    console.error('审核企业失败:', error)
    ElMessage.error('操作失败')
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChartRef.value)
  }

  const trendSeriesValues = [
    ...(trendData.total || []),
    ...(trendData.personal || []),
    ...(trendData.enterprise || [])
  ]
  const hasTrendData = trendSeriesValues.some(value => Number(value) > 0)

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['当日新增合计', '个人新增', '企业新增'] },
    grid: { left: 24, right: 24, top: 40, bottom: 24, containLabel: true },
    xAxis: {
      type: 'category',
      data: trendData.labels,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '当日新增合计',
        type: 'line',
        smooth: true,
        data: trendData.total,
        lineStyle: { width: 3, color: '#6366f1' },
        itemStyle: { color: '#6366f1' },
        areaStyle: { color: 'rgba(99, 102, 241, 0.15)' }
      },
      {
        name: '个人新增',
        type: 'line',
        smooth: true,
        data: trendData.personal,
        lineStyle: { width: 2, color: '#10b981' },
        itemStyle: { color: '#10b981' }
      },
      {
        name: '企业新增',
        type: 'line',
        smooth: true,
        data: trendData.enterprise,
        lineStyle: { width: 2, color: '#f97316' },
        itemStyle: { color: '#f97316' }
      }
    ],
    graphic: hasTrendData ? [] : [
      {
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: `近${trendDays.value}天暂无新增数据`,
          fill: '#9ca3af',
          font: '14px sans-serif'
        }
      }
    ]
  }

  trendChartInstance.setOption(option)
}

const renderDistributionChart = () => {
  if (!distributionChartRef.value) return
  if (!distributionChartInstance) {
    distributionChartInstance = echarts.init(distributionChartRef.value)
  }

  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['36%', '68%'],
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 600 } },
        data: [
          { value: stats.totalPersonalUsers, name: '个人用户' },
          { value: stats.totalEnterpriseUsers, name: '企业用户' },
          { value: stats.totalAdminUsers, name: '管理员' }
        ]
      }
    ]
  }

  distributionChartInstance.setOption(option)
}

const renderActivityChart = () => {
  if (!activityChartRef.value) return
  if (!activityChartInstance) {
    activityChartInstance = echarts.init(activityChartRef.value)
  }

  const personalSeries = [activityData.todayNewPersonal, activityData.weekNewPersonal, activityData.monthNewPersonal]
  const enterpriseSeries = [activityData.todayNewEnterprise, activityData.weekNewEnterprise, activityData.monthNewEnterprise]
  const hasActivityData = [...personalSeries, ...enterpriseSeries].some(value => Number(value) > 0)

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['个人新增', '企业新增'] },
    grid: { left: 24, right: 24, top: 40, bottom: 24, containLabel: true },
    xAxis: {
      type: 'category',
      data: ['今日新增', '本周新增', '本月新增']
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '个人新增',
        type: 'bar',
        stack: 'total',
        itemStyle: { color: '#34d399' },
        data: personalSeries
      },
      {
        name: '企业新增',
        type: 'bar',
        stack: 'total',
        itemStyle: { color: '#f97316' },
        data: enterpriseSeries
      }
    ],
    graphic: hasActivityData ? [] : [
      {
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: '暂无活跃新增数据',
          fill: '#9ca3af',
          font: '14px sans-serif'
        }
      }
    ]
  }

  activityChartInstance.setOption(option)
}

const resizeCharts = () => {
  if (trendChartInstance) trendChartInstance.resize()
  if (distributionChartInstance) distributionChartInstance.resize()
  if (activityChartInstance) activityChartInstance.resize()
}

const renderCharts = () => {
  renderTrendChart()
  renderDistributionChart()
  renderActivityChart()
  resizeCharts()
}

watch(trendDays, async () => {
  const ok = await fetchTrendStatistics()
  await nextTick()
  renderTrendChart()
  resizeCharts()
  if (!ok) {
    ElMessage.warning('趋势数据加载失败，已使用空数据展示')
  }
})

onMounted(async () => {
  await loadDashboardData()
  await nextTick()
  renderCharts()
  setTimeout(() => {
    resizeCharts()
  }, 120)
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  if (trendChartInstance) trendChartInstance.dispose()
  if (distributionChartInstance) distributionChartInstance.dispose()
  if (activityChartInstance) activityChartInstance.dispose()
  trendChartInstance = null
  distributionChartInstance = null
  activityChartInstance = null
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
}

.stat-card.personal {
  border-left: 4px solid #409eff;
}

.stat-card.enterprise {
  border-left: 4px solid #67c23a;
}

.stat-card.pending {
  border-left: 4px solid #e6a23c;
}

.stat-card.approved {
  border-left: 4px solid #f56c6c;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  margin-right: 20px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background-color: #f5f7fa;
}

.stat-card.personal .stat-icon {
  color: #409eff;
  background-color: #ecf5ff;
}

.stat-card.enterprise .stat-icon {
  color: #67c23a;
  background-color: #f0f9ff;
}

.stat-card.pending .stat-icon {
  color: #e6a23c;
  background-color: #fdf6ec;
}

.stat-card.approved .stat-icon {
  color: #f56c6c;
  background-color: #fef0f0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.stat-value {
  margin: 10px 0 0 0;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  height: 100%;
  border-radius: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
}

.summary-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 10px;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
}

.chart {
  height: 320px;
}

.pending-list {
  margin-top: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 12px;
  }

  .chart {
    height: 280px;
  }
}
</style>

<template>
  <div class="user-manage-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>用户管理</h1>
        <p class="subtitle">管理平台所有用户，维护系统安全</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card" @click="showStatisticsDialog">
        <div class="stat-icon total">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.total }}</span>
          <span class="stat-label">总用户数</span>
        </div>
        <el-icon class="stat-chart-icon"><TrendCharts /></el-icon>
      </div>
      <div class="stat-card">
        <div class="stat-icon personal">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.personal }}</span>
          <span class="stat-label">个人用户</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon enterprise">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.enterprise }}</span>
          <span class="stat-label">企业用户</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.active }}</span>
          <span class="stat-label">活跃用户</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon disabled">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.disabled }}</span>
          <span class="stat-label">已禁用</span>
        </div>
      </div>
    </div>

    <!-- 用户统计图表对话框 -->
    <el-dialog v-model="statisticsDialogVisible" title="用户统计分析" width="900px" class="statistics-dialog">
      <div class="statistics-content">
        <el-tabs v-model="statisticsTab">
          <el-tab-pane label="用户增长趋势" name="trend">
            <div class="chart-container">
              <div ref="trendChartRef" class="chart" style="height: 350px;"></div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="用户类型分布" name="distribution">
            <div class="chart-container">
              <div ref="distributionChartRef" class="chart" style="height: 350px;"></div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="活跃度分析" name="activity">
            <div class="chart-container">
              <div ref="activityChartRef" class="chart" style="height: 350px;"></div>
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <div class="statistics-summary">
          <div class="summary-item">
            <span class="summary-label">本月新增用户</span>
            <span class="summary-value">{{ statisticsData.monthlyNew }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">日均活跃用户</span>
            <span class="summary-value">{{ statisticsData.dailyActive }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">用户留存率</span>
            <span class="summary-value">{{ statisticsData.retentionRate }}%</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">企业认证率</span>
            <span class="summary-value">{{ statisticsData.certificationRate }}%</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 搜索和操作区域 -->
    <el-card shadow="hover" class="filter-card">
      <div class="filter-section">
        <div class="filter-row">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名、邮箱、手机号..."
            clearable
            class="search-input"
            @keyup.enter="searchUsers"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="searchForm.userType" placeholder="用户类型" clearable>
            <el-option label="全部类型" value=""></el-option>
            <el-option label="个人用户" value="personal"></el-option>
            <el-option label="企业用户" value="enterprise"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
          <el-select v-model="searchForm.status" placeholder="用户状态" clearable>
            <el-option label="全部状态" value=""></el-option>
            <el-option label="正常" value="1"></el-option>
            <el-option label="已禁用" value="0"></el-option>
          </el-select>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="注册开始日期"
            end-placeholder="注册结束日期"
            value-format="YYYY-MM-DD"
          />
          <el-button type="primary" @click="searchUsers">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </div>
        <div class="action-row">
          <el-button type="primary" @click="openCreateUserDialog">
            <el-icon><Plus /></el-icon> 添加用户
          </el-button>
          <el-button type="success" :disabled="selectedUsers.length === 0" @click="batchEnable">
            <el-icon><CircleCheck /></el-icon> 批量启用
          </el-button>
          <el-button type="danger" :disabled="selectedUsers.length === 0" @click="batchDisable">
            <el-icon><CircleClose /></el-icon> 批量禁用
          </el-button>
          <el-button :disabled="selectedUsers.length === 0" @click="exportUsers">
            <el-icon><Download /></el-icon> 导出选中
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 用户列表 -->
    <el-card shadow="hover" class="table-card">
      <el-table
        :data="users"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户信息" min-width="200">
          <template #default="scope">
            <div class="user-info-cell">
              <el-avatar :size="40" :src="scope.row.avatar">
                {{ scope.row.username?.charAt(0) }}
              </el-avatar>
              <div class="user-details">
                <span class="username">{{ scope.row.username }}</span>
                <span class="email">{{ scope.row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="userType" label="用户类型" width="120">
          <template #default="scope">
            <el-tag :type="getUserTypeTagType(scope.row.userType)" effect="light">
              {{ getUserTypeText(scope.row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="170" />
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="viewUserDetails(scope.row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button type="warning" link @click="editUser(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-dropdown trigger="click">
              <el-button type="info" link>
                更多 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="resetPassword(scope.row)">
                    <el-icon><Key /></el-icon> 重置密码
                  </el-dropdown-item>
                  <el-dropdown-item @click="viewLoginHistory(scope.row)">
                    <el-icon><Clock /></el-icon> 登录记录
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="deleteUser(scope.row)" style="color: #f56c6c">
                    <el-icon><Delete /></el-icon> 删除用户
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalUsers"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="userDialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="userForm.userType" placeholder="请选择用户类型" style="width: 100%">
            <el-option label="个人用户" value="personal" />
            <el-option label="企业用户" value="enterprise" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="userForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUserForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="700px">
      <div class="user-detail-content" v-if="currentUser">
        <div class="detail-header">
          <el-avatar :size="80" :src="currentUser.avatar">
            {{ currentUser.username?.charAt(0) }}
          </el-avatar>
          <div class="detail-basic">
            <h3>{{ currentUser.username }}</h3>
            <el-tag :type="getUserTypeTagType(currentUser.userType)">
              {{ getUserTypeText(currentUser.userType) }}
            </el-tag>
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'" style="margin-left: 8px">
              {{ currentUser.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </div>
        </div>
        <el-divider />
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="用户类型">{{ getUserTypeText(currentUser.userType) }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentUser.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后登录">{{ currentUser.lastLoginTime || '从未登录' }}</el-descriptions-item>
          <el-descriptions-item label="登录次数">{{ currentUser.loginCount || 0 }} 次</el-descriptions-item>
          <el-descriptions-item label="登录IP">{{ currentUser.lastLoginIp || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentUser.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button type="primary" @click="editUser(currentUser)">编辑信息</el-button>
          <el-button @click="resetPassword(currentUser)">重置密码</el-button>
          <el-button :type="currentUser.status === 1 ? 'danger' : 'success'" 
            @click="toggleUserStatus(currentUser)">
            {{ currentUser.status === 1 ? '禁用账号' : '启用账号' }}
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 登录记录对话框 -->
    <el-dialog v-model="loginHistoryVisible" title="登录记录" width="700px">
      <el-table :data="loginHistory" style="width: 100%">
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column prop="loginIp" label="登录IP" width="150" />
        <el-table-column prop="location" label="登录地点" />
        <el-table-column prop="device" label="设备信息" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, UserFilled, OfficeBuilding, CircleCheck, CircleClose,
  Search, Refresh, Plus, Download, View, Edit, Delete,
  ArrowDown, Key, Clock, TrendCharts, DataAnalysis
} from '@element-plus/icons-vue'
import request from '../../utils/request'

// 统计数据
const stats = reactive({
  total: 1256,
  personal: 892,
  enterprise: 328,
  active: 1180,
  disabled: 76
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  userType: '',
  status: '',
  dateRange: []
})

// 用户表单
const userFormRef = ref(null)
const userDialogVisible = ref(false)
const isEdit = ref(false)
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  email: '',
  phone: '',
  userType: 'personal',
  status: 1,
  remark: ''
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

// 用户列表
const users = ref([])
const selectedUsers = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalUsers = ref(0)

// 详情对话框
const detailDialogVisible = ref(false)
const currentUser = ref(null)

// 登录记录
const loginHistoryVisible = ref(false)
const loginHistory = ref([])

// 用户统计图表相关
const statisticsDialogVisible = ref(false)
const statisticsTab = ref('trend')
const trendChartRef = ref(null)
const distributionChartRef = ref(null)
const activityChartRef = ref(null)
const statisticsData = reactive({
  monthlyNew: 156,
  dailyActive: 423,
  retentionRate: 78.5,
  certificationRate: 45.2
})

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  try {
    const endpointMap = {
      personal: '/admin/user/personal',
      enterprise: '/admin/user/enterprise',
      admin: '/admin/user/admin'
    }
    const endpoint = endpointMap[searchForm.userType] || '/admin/user/personal'
    const response = await request.get(endpoint, {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchForm.keyword
      }
    })
        
    users.value = response.data.records || []
    totalUsers.value = response.data.total || 0
  } catch (error) {
    console.error('加载用户失败:', error)
    ElMessage.error('加载用户失败')
    users.value = []
    totalUsers.value = 0
  }
}

const searchUsers = () => {
  loadUsers()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.userType = ''
  searchForm.status = ''
  searchForm.dateRange = []
  loadUsers()
}

const getUserTypeText = (type) => {
  const map = { personal: '个人用户', enterprise: '企业用户', admin: '管理员' }
  return map[type] || '未知'
}

const getUserTypeTagType = (type) => {
  const map = { personal: 'info', enterprise: 'success', admin: 'warning' }
  return map[type] || 'info'
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

const handleStatusChange = (user) => {
  const action = user.status === 1 ? '启用' : '禁用'
  ElMessage.success(`已${action}用户：${user.username}`)
}

const openCreateUserDialog = () => {
  isEdit.value = false
  Object.assign(userForm, { id: null, username: '', password: '', email: '', phone: '', userType: 'personal', status: 1, remark: '' })
  userDialogVisible.value = true
}

const editUser = (user) => {
  isEdit.value = true
  Object.assign(userForm, { ...user })
  userDialogVisible.value = true
  detailDialogVisible.value = false
}

const submitUserForm = () => {
  userFormRef.value?.validate((valid) => {
    if (valid) {
      ElMessage.success(isEdit.value ? '用户信息已更新' : '用户添加成功')
      userDialogVisible.value = false
      loadUsers()
    }
  })
}

const viewUserDetails = (user) => {
  currentUser.value = { ...user }
  detailDialogVisible.value = true
}

const toggleUserStatus = (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${action}用户 "${user.username}" 吗？`, '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    user.status = newStatus
    currentUser.value.status = newStatus
    ElMessage.success(`已${action}用户`)
  }).catch(() => {})
}

const resetPassword = (user) => {
  ElMessageBox.confirm(`确定要重置用户 "${user.username}" 的密码吗？`, '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('密码已重置为默认密码：123456')
  }).catch(() => {})
}

const viewLoginHistory = async (user) => {
  try {
    loginHistory.value = []
    loginHistoryVisible.value = true
    ElMessage.info('当前暂未提供登录记录接口')
  } catch (error) {
    console.error('加载登录记录失败:', error)
    ElMessage.error('加载登录记录失败')
    loginHistory.value = []
    loginHistoryVisible.value = true
  }
}

const deleteUser = (user) => {
  ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？此操作不可恢复！`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error'
  }).then(() => {
    users.value = users.value.filter(u => u.id !== user.id)
    ElMessage.success('用户已删除')
  }).catch(() => {})
}

const batchEnable = () => {
  ElMessageBox.confirm(`确定要启用选中的 ${selectedUsers.value.length} 个用户吗？`, '批量启用', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    selectedUsers.value.forEach(u => { u.status = 1 })
    ElMessage.success(`已启用 ${selectedUsers.value.length} 个用户`)
  }).catch(() => {})
}

const batchDisable = () => {
  ElMessageBox.confirm(`确定要禁用选中的 ${selectedUsers.value.length} 个用户吗？`, '批量禁用', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    selectedUsers.value.forEach(u => { u.status = 0 })
    ElMessage.success(`已禁用 ${selectedUsers.value.length} 个用户`)
  }).catch(() => {})
}

const exportUsers = () => {
  ElMessage.success(`正在导出 ${selectedUsers.value.length} 个用户数据...`)
}

const handleSizeChange = (size) => { pageSize.value = size }
const handleCurrentChange = (page) => { currentPage.value = page }

// 显示统计图表对话框
const showStatisticsDialog = async () => {
  statisticsDialogVisible.value = true
  
  // 等待DOM更新后渲染图表
  await nextTick()
  setTimeout(() => {
    renderCharts()
  }, 100)
}

// 渲染图表（使用简单的CSS图表，避免依赖echarts）
const renderCharts = () => {
  // 由于没有echarts，使用简单的数据展示
  // 实际项目中可以引入echarts进行图表渲染
  console.log('Charts would be rendered here with echarts')
}

// 加载统计数据
const loadStatisticsData = async () => {
  try {
    // 获取用户增长趋势
    const trendResponse = await request.get('/admin/user/statistics/trend')
    // 获取用户活跃度
    const activityResponse = await request.get('/admin/user/statistics/activity')
    
    // 更新统计数据
    if (trendResponse.data) {
      statisticsData.monthlyNew = trendResponse.data.monthlyNew || 156
    }
    if (activityResponse.data) {
      statisticsData.dailyActive = activityResponse.data.dailyActive || 423
      statisticsData.retentionRate = activityResponse.data.retentionRate || 78.5
    }
  } catch (error) {
    console.log('使用模拟统计数据')
  }
}
</script>

<style scoped>
.user-manage-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  min-height: 100vh;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px 40px;
  border-radius: 20px;
  margin-bottom: 24px;
  color: #fff;
}

.page-header h1 {
  font-size: 28px;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.page-header .subtitle {
  opacity: 0.9;
  margin: 0;
  font-size: 14px;
}

/* 统计卡片 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-chart-icon {
  position: absolute;
  right: 16px;
  top: 16px;
  font-size: 20px;
  color: #909399;
  opacity: 0.6;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.stat-icon.total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.personal { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-icon.enterprise { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.active { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.disabled { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 筛选卡片 */
.filter-card {
  border-radius: 16px;
  margin-bottom: 24px;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  width: 280px;
}

.action-row {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 表格卡片 */
.table-card {
  border-radius: 16px;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-details .username {
  font-weight: 600;
  color: #303133;
}

.user-details .email {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 详情对话框 */
.user-detail-content {
  padding: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.detail-basic h3 {
  margin: 0 0 12px 0;
  font-size: 24px;
  color: #303133;
}

.detail-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 1400px) {
  .stats-section {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .stats-section {
    grid-template-columns: 1fr;
  }
}

/* 统计图表对话框样式 */
.statistics-dialog .statistics-content {
  padding: 10px 0;
}

.statistics-dialog .chart-container {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.statistics-dialog .chart {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
  font-size: 16px;
}

.statistics-dialog .chart::after {
  content: '📊 图表区域 (需要引入 ECharts 库)';
}

.statistics-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8ecf1 100%);
  border-radius: 12px;
}

.summary-item {
  text-align: center;
  padding: 16px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.summary-label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
}

@media (max-width: 768px) {
  .statistics-summary {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

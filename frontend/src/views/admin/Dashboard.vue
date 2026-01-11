<template>
  <div class="dashboard">
    <h2>管理员仪表盘</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card personal">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">个人用户</p>
              <p class="stat-value">{{ stats.totalPersonalUsers }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card enterprise">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">企业用户</p>
              <p class="stat-value">{{ stats.totalEnterpriseUsers }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card pending">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">待审核企业</p>
              <p class="stat-value">{{ stats.pendingEnterpriseUsers }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card approved">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><Select /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">已审核企业</p>
              <p class="stat-value">{{ stats.approvedEnterpriseUsers }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待审核企业列表 -->
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

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, OfficeBuilding, Clock, Select } from '@element-plus/icons-vue'
import request from '../../utils/request'

export default {
  name: 'AdminDashboard',
  components: {
    User,
    OfficeBuilding,
    Clock,
    Select
  },
  setup() {
    const stats = reactive({
      totalPersonalUsers: 0,
      totalEnterpriseUsers: 0,
      pendingEnterpriseUsers: 0,
      approvedEnterpriseUsers: 0
    })

    const pendingEnterprises = ref([])

    // 获取统计数据
    const fetchStats = async () => {
      try {
        const response = await request.get('/admin/user/statistics')
        if (response.data) {
          Object.assign(stats, response.data)
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    }

    // 获取待审核企业列表
    const fetchPendingEnterprises = async () => {
      try {
        const response = await request.get('/admin/user/enterprise', {
          params: { page: 1, size: 5, authStatus: 0 }
        })
        if (response.data && response.data.records) {
          pendingEnterprises.value = response.data.records
        }
      } catch (error) {
        console.error('获取待审核企业失败:', error)
      }
    }

    // 审核企业
    const auditEnterprise = async (enterprise, authStatus) => {
      try {
        await request.put('/admin/user/enterprise/audit', null, {
          params: {
            userId: enterprise.id,
            authStatus: authStatus
          }
        })
        ElMessage.success(authStatus === 1 ? '审核通过' : '已拒绝')
        // 刷新数据
        fetchPendingEnterprises()
        fetchStats()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    onMounted(() => {
      fetchStats()
      fetchPendingEnterprises()
    })

    return {
      stats,
      pendingEnterprises,
      auditEnterprise
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard h2 {
  margin-bottom: 20px;
  color: #303133;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-card.personal {
  border-left: 4px solid #409EFF;
}

.stat-card.enterprise {
  border-left: 4px solid #67C23A;
}

.stat-card.pending {
  border-left: 4px solid #E6A23C;
}

.stat-card.approved {
  border-left: 4px solid #F56C6C;
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
  color: #409EFF;
  background-color: #ecf5ff;
}

.stat-card.enterprise .stat-icon {
  color: #67C23A;
  background-color: #f0f9ff;
}

.stat-card.pending .stat-icon {
  color: #E6A23C;
  background-color: #fdf6ec;
}

.stat-card.approved .stat-icon {
  color: #F56C6C;
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

.pending-list {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
</style>

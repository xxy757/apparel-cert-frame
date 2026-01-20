<template>
  <div class="certification-manage-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>认证管理</h2>
          <el-button type="primary" @click="openCreateCertStandardDialog">创建认证标准</el-button>
        </div>
      </template>
      
      <div class="certification-filters">
        <el-form :inline="true" :model="searchForm" label-width="80px">
          <el-form-item label="申请人">
            <el-input v-model="searchForm.username" placeholder="请输入申请人姓名"></el-input>
          </el-form-item>
          <el-form-item label="认证类型">
            <el-select v-model="searchForm.certType" placeholder="请选择认证类型">
              <el-option label="全部" value=""></el-option>
              <el-option label="设计师" value="designer"></el-option>
              <el-option label="打版师" value="patternmaker"></el-option>
              <el-option label="质检员" value="inspector"></el-option>
              <el-option label="导购" value="sales"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="认证状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value=""></el-option>
              <el-option label="待审核" value="0"></el-option>
              <el-option label="审核通过" value="1"></el-option>
              <el-option label="审核拒绝" value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchCertifications">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="certifications" style="width: 100%">
        <el-table-column prop="id" label="认证ID" width="100"></el-table-column>
        <el-table-column prop="username" label="申请人"></el-table-column>
        <el-table-column prop="certType" label="认证类型" width="120">
          <template #default="scope">
            <el-tag :type="getCertTypeTagType(scope.row.certType)">
              {{ getCertTypeText(scope.row.certType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180"></el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewer" label="审核人" width="120"></el-table-column>
        <el-table-column prop="reviewTime" label="审核时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewCertDetails(scope.row)">查看详情</el-button>
            <el-button v-if="scope.row.status === 0" type="success" size="small" @click="approveCert(scope.row)">通过</el-button>
            <el-button v-if="scope.row.status === 0" type="danger" size="small" @click="rejectCert(scope.row)">拒绝</el-button>
            <el-button v-else type="warning" size="small" @click="viewCertificate(scope.row)">查看证书</el-button>
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
          :total="totalCertifications"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 创建认证标准对话框 -->
    <el-dialog v-model="createCertStandardDialogVisible" title="创建认证标准" width="600px">
      <el-form ref="certStandardFormRef" :model="certStandardForm" :rules="certStandardRules" label-width="120px">
        <el-form-item label="认证类型" prop="certType">
          <el-select v-model="certStandardForm.certType" placeholder="请选择认证类型">
            <el-option label="设计师" value="designer"></el-option>
            <el-option label="打版师" value="patternmaker"></el-option>
            <el-option label="质检员" value="inspector"></el-option>
            <el-option label="导购" value="sales"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="认证名称" prop="certName">
          <el-input v-model="certStandardForm.certName" placeholder="请输入认证名称"></el-input>
        </el-form-item>
        <el-form-item label="认证等级" prop="certLevel">
          <el-select v-model="certStandardForm.certLevel" placeholder="请选择认证等级">
            <el-option label="初级" value="1"></el-option>
            <el-option label="中级" value="2"></el-option>
            <el-option label="高级" value="3"></el-option>
            <el-option label="专家级" value="4"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="考试费用" prop="examFee">
          <el-input v-model="certStandardForm.examFee" type="number" placeholder="请输入考试费用"></el-input>
        </el-form-item>
        <el-form-item label="有效期(年)" prop="validityPeriod">
          <el-input v-model="certStandardForm.validityPeriod" type="number" placeholder="请输入有效期"></el-input>
        </el-form-item>
        <el-form-item label="认证标准" prop="certStandard">
          <el-input v-model="certStandardForm.certStandard" type="textarea" :rows="5" placeholder="请输入认证标准详情"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createCertStandardDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createCertStandard">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

export default {
  name: 'CertificationManage',
  setup() {
    const certStandardFormRef = ref(null)
    const createCertStandardDialogVisible = ref(false)
    const searchForm = reactive({
      username: '',
      certType: '',
      status: ''
    })
    const certStandardForm = reactive({
      certType: 'designer',
      certName: '',
      certLevel: '1',
      examFee: 0,
      validityPeriod: 3,
      certStandard: ''
    })
    const certStandardRules = {
      certType: [
        { required: true, message: '请选择认证类型', trigger: 'change' }
      ],
      certName: [
        { required: true, message: '请输入认证名称', trigger: 'blur' }
      ],
      certLevel: [
        { required: true, message: '请选择认证等级', trigger: 'change' }
      ],
      examFee: [
        { required: true, message: '请输入考试费用', trigger: 'blur' },
        { type: 'number', min: 0, message: '考试费用必须大于等于0', trigger: 'blur' }
      ],
      validityPeriod: [
        { required: true, message: '请输入有效期', trigger: 'blur' },
        { type: 'number', min: 1, message: '有效期必须大于等于1', trigger: 'blur' }
      ],
      certStandard: [
        { required: true, message: '请输入认证标准详情', trigger: 'blur' }
      ]
    }
    const certifications = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalCertifications = ref(0)
    
    onMounted(() => {
      loadCertifications()
    })
    
    const loadCertifications = async () => {
      try {
        const response = await request.get('/personal/certification/page', {
          params: {
            page: currentPage.value,
            size: pageSize.value
          }
        })
        
        certifications.value = response.data.records || []
        totalCertifications.value = response.data.total || 0
      } catch (error) {
        console.error('加载认证申请失败:', error)
        ElMessage.error('加载认证申请失败')
        certifications.value = []
        totalCertifications.value = 0
      }
    }
    
    const searchCertifications = () => {
      loadCertifications()
    }
    
    const resetSearch = () => {
      searchForm.username = ''
      searchForm.certType = ''
      searchForm.status = ''
      loadCertifications()
    }
    
    const getCertTypeText = (type) => {
      const typeMap = {
        designer: '设计师',
        patternmaker: '打版师',
        inspector: '质检员',
        sales: '导购'
      }
      return typeMap[type] || '未知'
    }
    
    const getCertTypeTagType = (type) => {
      const typeMap = {
        designer: 'info',
        patternmaker: 'success',
        inspector: 'warning',
        sales: 'primary'
      }
      return typeMap[type] || 'info'
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        0: '待审核',
        1: '审核通过',
        2: '审核拒绝'
      }
      return statusMap[status] || '未知'
    }
    
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return typeMap[status] || 'info'
    }
    
    const openCreateCertStandardDialog = () => {
      // 重置表单
      certStandardForm.certType = 'designer'
      certStandardForm.certName = ''
      certStandardForm.certLevel = '1'
      certStandardForm.examFee = 0
      certStandardForm.validityPeriod = 3
      certStandardForm.certStandard = ''
      createCertStandardDialogVisible.value = true
    }
    
    const createCertStandard = () => {
      certStandardFormRef.value.validate((valid) => {
        if (valid) {
          // 模拟创建认证标准
          ElMessage.success('认证标准创建成功')
          createCertStandardDialogVisible.value = false
        } else {
          return false
        }
      })
    }
    
    const viewCertDetails = (cert) => {
      // 查看认证详情逻辑
      ElMessage.info('查看认证详情功能开发中')
    }
    
    const approveCert = (cert) => {
      // 审核通过认证逻辑
      cert.status = 1
      cert.reviewer = '管理员'
      cert.reviewTime = new Date().toLocaleString()
      ElMessage.success('认证审核通过')
      loadCertifications()
    }
    
    const rejectCert = (cert) => {
      // 拒绝认证逻辑
      cert.status = 2
      cert.reviewer = '管理员'
      cert.reviewTime = new Date().toLocaleString()
      ElMessage.success('认证审核拒绝')
      loadCertifications()
    }
    
    const viewCertificate = (cert) => {
      // 查看证书逻辑
      ElMessage.info('查看证书功能开发中')
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      loadCertifications()
    }
    
    const handleCurrentChange = (current) => {
      currentPage.value = current
      loadCertifications()
    }
    
    return {
      certStandardFormRef,
      createCertStandardDialogVisible,
      searchForm,
      certStandardForm,
      certStandardRules,
      certifications,
      currentPage,
      pageSize,
      totalCertifications,
      getCertTypeText,
      getCertTypeTagType,
      getStatusText,
      getStatusTagType,
      openCreateCertStandardDialog,
      createCertStandard,
      searchCertifications,
      resetSearch,
      viewCertDetails,
      approveCert,
      rejectCert,
      viewCertificate,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.certification-manage-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.certification-filters {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

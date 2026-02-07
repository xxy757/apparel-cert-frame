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
              <el-option label="待提交图片" value="0"></el-option>
              <el-option label="待审核" value="1"></el-option>
          <el-option label="审核通过" value="3"></el-option>
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
        <el-table-column prop="certTypeText" label="认证类型" width="120">
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
        <el-table-column label="认证材料" width="120">
          <template #default="scope">
            <el-button
              v-if="scope.row.practicalFileUrl"
              type="primary"
              link
              @click="previewMaterial(scope.row)"
            >
              查看图片
            </el-button>
            <span v-else>未上传</span>
          </template>
        </el-table-column>
        <el-table-column prop="reviewer" label="审核人" width="120"></el-table-column>
        <el-table-column prop="reviewTime" label="审核时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewCertDetails(scope.row)">查看详情</el-button>
            <el-button v-if="canReview(scope.row)" type="success" size="small" @click="approveCert(scope.row)">通过</el-button>
            <el-button v-if="canReview(scope.row)" type="danger" size="small" @click="rejectCert(scope.row)">拒绝</el-button>
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

    <el-card shadow="hover" class="standard-list-card">
      <template #header>
        <div class="card-header">
          <h2>认证标准列表</h2>
        </div>
      </template>
      <el-table :data="certificationStandards" style="width: 100%" v-loading="standardsLoading">
        <el-table-column prop="id" label="标准ID" width="100"></el-table-column>
        <el-table-column prop="name" label="认证名称" width="220"></el-table-column>
        <el-table-column prop="certTypeText" label="认证类型" width="120">
          <template #default="scope">
            <el-tag :type="getCertTypeTagType(scope.row.certType)">
              {{ scope.row.certTypeText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="levelText" label="认证等级" width="120"></el-table-column>
        <el-table-column prop="description" label="认证标准"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="materialPreviewVisible" title="认证图片材料" width="700px">
      <div v-if="currentMaterialUrl" class="material-preview">
        <el-image
          :src="currentMaterialUrl"
          fit="contain"
          style="width: 100%; max-height: 520px"
        />
      </div>
      <el-empty v-else description="暂无图片材料" />
      <template #footer>
        <el-button @click="materialPreviewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
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
          <el-input v-model.number="certStandardForm.examFee" type="number" placeholder="请输入考试费用"></el-input>
        </el-form-item>
        <el-form-item label="有效期(年)" prop="validityPeriod">
          <el-input v-model.number="certStandardForm.validityPeriod" type="number" placeholder="请输入有效期"></el-input>
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
    const certificationStandards = ref([])
    const standardsLoading = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalCertifications = ref(0)
    const materialPreviewVisible = ref(false)
    const currentMaterialUrl = ref('')
    
    onMounted(() => {
      loadCertifications()
      loadCertificationStandards()
    })
    
    const loadCertifications = async () => {
      try {
        const response = await request.get('/personal/certification/page', {
          params: {
            page: currentPage.value,
            size: pageSize.value
          }
        })
        
        const records = response.data.records || []
        certifications.value = records
          .filter(item => {
            if (searchForm.certType && item.certificationType !== searchForm.certType) return false
            if (searchForm.status !== '' && String(item.status) !== String(searchForm.status)) return false
            if (searchForm.username && item.userName && !item.userName.includes(searchForm.username)) return false
            return true
          })
          .map(item => mapCertificationItem(item))

        totalCertifications.value = response.data.total || certifications.value.length
      } catch (error) {
        console.error('加载认证申请失败:', error)
        ElMessage.error('加载认证申请失败')
        certifications.value = []
        totalCertifications.value = 0
      }
    }

    const getLevelText = (level) => {
      const levelMap = {
        1: '初级',
        2: '中级',
        3: '高级',
        4: '专家级'
      }
      return levelMap[level] || String(level || '')
    }

    const mapCertificationStandard = (item) => {
      const certType = item.type || item.jobType
      return {
        ...item,
        certType,
        certTypeText: getCertTypeText(certType),
        levelText: getLevelText(item.level)
      }
    }

    const loadCertificationStandards = async () => {
      standardsLoading.value = true
      try {
        const response = await request.get('/admin/certification/standard', {
          params: {
            page: 1,
            size: 200
          }
        })
        const records = response.data?.records || []
        certificationStandards.value = records.map(item => mapCertificationStandard(item))
      } catch (error) {
        console.error('加载认证标准失败:', error)
        ElMessage.error('加载认证标准失败')
        certificationStandards.value = []
      } finally {
        standardsLoading.value = false
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
        0: '待提交图片',
        1: '待审核',
        2: '审核拒绝',
        3: '审核通过'
      }
      return statusMap[status] || '未知'
    }
    
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'info',
        1: 'warning',
        2: 'danger',
        3: 'success'
      }
      return typeMap[status] || 'info'
    }

    const normalizeFileUrl = (fileUrl) => {
      if (!fileUrl) return ''
      if (/^https?:\/\//i.test(fileUrl)) return fileUrl
      return `${window.location.origin}${fileUrl.startsWith('/') ? '' : '/'}${fileUrl}`
    }

    const canReview = (cert) => [0, 1].includes(Number(cert?.status))

    const previewMaterial = (cert) => {
      const url = normalizeFileUrl(cert?.practicalFileUrl || '')
      if (!url) {
        ElMessage.warning('申请人尚未上传认证图片')
        return
      }
      currentMaterialUrl.value = url
      materialPreviewVisible.value = true
    }

    const mapCertificationItem = (item) => {
      const certTypeText = getCertTypeText(item.certificationType)
      return {
        ...item,
        certType: item.certificationType,
        certTypeText,
        levelText: item.level ? `${item.level}级` : '',
        username: item.userName || item.userId || '用户',
        applyTime: item.applyTime || item.createTime,
        reviewTime: item.reviewTime,
        practicalFileUrl: item.practicalFileUrl || ''
      }
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
          createStandard()
        } else {
          ElMessage.warning('请完善认证标准信息后再提交')
          return false
        }
      })
    }

    const createStandard = async () => {
      try {
        await request.post('/admin/certification/standard', {
          type: certStandardForm.certType,
          level: parseInt(certStandardForm.certLevel, 10),
          name: certStandardForm.certName,
          description: certStandardForm.certStandard,
          practicalRequirements: certStandardForm.certStandard
        })
        ElMessage.success('认证标准创建成功')
        createCertStandardDialogVisible.value = false
        loadCertificationStandards()
      } catch (error) {
        console.error('创建认证标准失败:', error)
        ElMessage.error('创建认证标准失败')
      }
    }
    
    const viewCertDetails = (cert) => {
      const material = cert.practicalFileUrl ? '已上传' : '未上传'
      ElMessage.info(`认证详情：${cert.certTypeText}（${cert.levelText}），图片材料：${material}`)
    }
    
    const approveCert = async (cert) => {
      if (!cert?.practicalFileUrl) {
        ElMessage.warning('申请人未上传认证图片，无法审核通过')
        return
      }
      try {
        await request.put('/personal/certification/admin/update-status', {
          certificationId: cert.id,
          status: 3,
          reviewComment: '审核通过'
        })
        ElMessage.success('认证审核通过')
        loadCertifications()
      } catch (error) {
        console.error('审核通过失败:', error)
        ElMessage.error('审核通过失败')
      }
    }
    
    const rejectCert = async (cert) => {
      if (!cert?.practicalFileUrl) {
        ElMessage.warning('申请人未上传认证图片，可直接驳回并要求补充')
      }
      try {
        await request.put('/personal/certification/admin/update-status', {
          certificationId: cert.id,
          status: 2,
          reviewComment: '审核拒绝'
        })
        ElMessage.success('认证审核拒绝')
        loadCertifications()
      } catch (error) {
        console.error('审核拒绝失败:', error)
        ElMessage.error('审核拒绝失败')
      }
    }
    
    const viewCertificate = (cert) => {
      if (cert.status !== 3) {
        ElMessage.warning('该认证尚未通过，无法查看证书')
        return
      }
      ElMessage.info('证书查看请到证书管理页面')
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
      certificationStandards,
      standardsLoading,
      currentPage,
      pageSize,
      totalCertifications,
      materialPreviewVisible,
      currentMaterialUrl,
      getCertTypeText,
      getCertTypeTagType,
      getStatusText,
      getStatusTagType,
      canReview,
      previewMaterial,
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

.standard-list-card {
  margin-top: 20px;
}

.material-preview {
  display: flex;
  justify-content: center;
}
</style>

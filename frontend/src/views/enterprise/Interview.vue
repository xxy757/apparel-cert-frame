<template>
  <div class="interview-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>面试管理</h2>
        </div>
      </template>
      
      <div class="interview-filters">
        <el-form :inline="true" :model="searchForm" label-width="100px">
          <el-form-item label="职位">
            <el-select v-model="searchForm.position" placeholder="请选择职位">
              <el-option label="全部" value=""></el-option>
              <el-option v-for="job in jobs" :key="job.id" :label="job.title" :value="job.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="候选人">
            <el-input v-model="searchForm.candidateName" placeholder="请输入候选人姓名"></el-input>
          </el-form-item>
          <el-form-item label="面试状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value=""></el-option>
              <el-option label="待确认" value="0"></el-option>
              <el-option label="已确认" value="1"></el-option>
              <el-option label="已完成" value="2"></el-option>
              <el-option label="已取消" value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchInterviews">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="interviews" style="width: 100%">
        <el-table-column prop="id" label="面试ID" width="100"></el-table-column>
        <el-table-column prop="positionName" label="职位" width="150"></el-table-column>
        <el-table-column prop="candidateName" label="候选人" width="120"></el-table-column>
        <el-table-column prop="interviewTime" label="面试时间" width="180"></el-table-column>
        <el-table-column prop="interviewLocation" label="面试地点" width="150"></el-table-column>
        <el-table-column prop="interviewType" label="面试形式" width="120"></el-table-column>
        <el-table-column prop="interviewer" label="面试官" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.result" :type="scope.row.result === 1 ? 'success' : 'danger'">
              {{ scope.row.result === 1 ? '已录用' : '未录用' }}
            </el-tag>
            <span v-else>待评定</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewInterviewDetails(scope.row)">查看详情</el-button>
            <el-button v-if="scope.row.status === 0" type="warning" size="small" @click="confirmInterview(scope.row)">确认面试</el-button>
            <el-button v-if="scope.row.status <= 1" type="danger" size="small" @click="cancelInterview(scope.row)">取消面试</el-button>
            <el-button v-if="scope.row.status === 2 && !scope.row.result" type="success" size="small" @click="recordResult(scope.row)">记录结果</el-button>
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
          :total="totalInterviews"
        ></el-pagination>
      </div>
    </el-card>
    
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

<script>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'InterviewManage',
  setup() {
    const detailDialogVisible = ref(false)
    const resultDialogVisible = ref(false)
    const selectedInterview = ref(null)
    const resultFormRef = ref(null)
    
    const searchForm = reactive({
      position: '',
      candidateName: '',
      status: ''
    })
    
    const resultForm = reactive({
      interviewId: '',
      candidateName: '',
      result: '',
      feedback: ''
    })
    
    const resultRules = {
      result: [
        { required: true, message: '请选择面试结果', trigger: 'change' }
      ],
      feedback: [
        { required: true, message: '请输入面试反馈', trigger: 'blur' },
        { min: 10, message: '面试反馈不能少于10个字符', trigger: 'blur' }
      ]
    }
    
    const jobs = ref([
      { id: 1, title: '服装设计师' },
      { id: 2, title: '服装打版师' }
    ])
    
    const interviews = ref([
      {
        id: 1,
        positionName: '服装设计师',
        candidateName: '张三',
        interviewTime: '2024-01-25 14:00:00',
        interviewLocation: '公司会议室A',
        interviewType: '现场面试',
        interviewer: '李经理',
        status: 2,
        result: null,
        remark: '请提前准备作品集',
        feedback: ''
      },
      {
        id: 2,
        positionName: '服装打版师',
        candidateName: '李四',
        interviewTime: '2024-01-26 10:00:00',
        interviewLocation: '视频面试',
        interviewType: '视频面试',
        interviewer: '王主管',
        status: 1,
        result: null,
        remark: '面试链接将通过邮件发送',
        feedback: ''
      }
    ])
    
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalInterviews = ref(interviews.value.length)
    
    const getStatusText = (status) => {
      const statusMap = {
        0: '待确认',
        1: '已确认',
        2: '已完成',
        3: '已取消'
      }
      return statusMap[status] || '未知'
    }
    
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'info',
        3: 'danger'
      }
      return typeMap[status] || 'info'
    }
    
    const searchInterviews = () => {
      // TODO: 调用面试搜索接口
      console.log('搜索面试:', searchForm)
    }
    
    const resetSearch = () => {
      searchForm.position = ''
      searchForm.candidateName = ''
      searchForm.status = ''
    }
    
    const viewInterviewDetails = (interview) => {
      selectedInterview.value = interview
      detailDialogVisible.value = true
    }
    
    const confirmInterview = (interview) => {
      // TODO: 确认面试
      interview.status = 1
      ElMessage.success('面试已确认')
    }
    
    const cancelInterview = (interview) => {
      // TODO: 取消面试
      interview.status = 3
      ElMessage.success('面试已取消')
    }
    
    const recordResult = (interview) => {
      // 填充面试结果表单
      resultForm.interviewId = interview.id.toString()
      resultForm.candidateName = interview.candidateName
      resultDialogVisible.value = true
    }
    
    const saveResult = () => {
      // TODO: 保存面试结果
      resultDialogVisible.value = false
      ElMessage.success('面试结果已保存')
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
    }
    
    const handleCurrentChange = (current) => {
      currentPage.value = current
    }
    
    return {
      detailDialogVisible,
      resultDialogVisible,
      selectedInterview,
      searchForm,
      resultForm,
      resultRules,
      jobs,
      interviews,
      currentPage,
      pageSize,
      totalInterviews,
      getStatusText,
      getStatusTagType,
      searchInterviews,
      resetSearch,
      viewInterviewDetails,
      confirmInterview,
      cancelInterview,
      recordResult,
      saveResult,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.interview-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.interview-filters {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.interview-detail {
  padding: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #303133;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 5px;
}

.detail-item {
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
}

.detail-item .label {
  font-weight: bold;
  width: 100px;
  color: #606266;
}

.detail-item .value {
  color: #303133;
  flex: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

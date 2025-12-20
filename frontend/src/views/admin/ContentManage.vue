<template>
  <div class="content-manage-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>内容管理</h2>
          <el-button type="primary" @click="openCreateContentDialog">发布内容</el-button>
        </div>
      </template>
      
      <div class="content-filters">
        <el-form :inline="true" :model="searchForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入内容标题"></el-input>
          </el-form-item>
          <el-form-item label="内容类型">
            <el-select v-model="searchForm.contentType" placeholder="请选择内容类型">
              <el-option label="全部" value=""></el-option>
              <el-option label="文章" value="article"></el-option>
              <el-option label="公告" value="notice"></el-option>
              <el-option label="新闻" value="news"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" value=""></el-option>
              <el-option label="已发布" value="1"></el-option>
              <el-option label="草稿" value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchContents">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="contents" style="width: 100%">
        <el-table-column prop="id" label="内容ID" width="100"></el-table-column>
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column prop="contentType" label="内容类型" width="120">
          <template #default="scope">
            <el-tag :type="getContentTypeTagType(scope.row.contentType)">
              {{ getContentTypeText(scope.row.contentType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100"></el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewContentDetails(scope.row)">查看详情</el-button>
            <el-button type="warning" size="small" @click="editContent(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 1" type="danger" size="small" @click="unpublishContent(scope.row)">下架</el-button>
            <el-button v-else type="success" size="small" @click="publishContent(scope.row)">发布</el-button>
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
          :total="totalContents"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 发布内容对话框 -->
    <el-dialog v-model="createContentDialogVisible" title="发布内容" width="800px">
      <el-form ref="contentFormRef" :model="contentForm" :rules="contentRules" label-width="120px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="contentForm.title" placeholder="请输入内容标题"></el-input>
        </el-form-item>
        <el-form-item label="内容类型" prop="contentType">
          <el-select v-model="contentForm.contentType" placeholder="请选择内容类型">
            <el-option label="文章" value="article"></el-option>
            <el-option label="公告" value="notice"></el-option>
            <el-option label="新闻" value="news"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="contentForm.author" placeholder="请输入作者"></el-input>
        </el-form-item>
        <el-form-item label="内容摘要" prop="summary">
          <el-input v-model="contentForm.summary" type="textarea" placeholder="请输入内容摘要"></el-input>
        </el-form-item>
        <el-form-item label="内容详情" prop="content">
          <el-input v-model="contentForm.content" type="textarea" :rows="10" placeholder="请输入内容详情"></el-input>
        </el-form-item>
        <el-form-item label="内容状态" prop="status">
          <el-select v-model="contentForm.status" placeholder="请选择内容状态">
            <el-option label="已发布" value="1"></el-option>
            <el-option label="草稿" value="0"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createContentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createContent">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'ContentManage',
  setup() {
    const contentFormRef = ref(null)
    const createContentDialogVisible = ref(false)
    const searchForm = reactive({
      title: '',
      contentType: '',
      status: ''
    })
    const contentForm = reactive({
      title: '',
      contentType: 'article',
      author: '',
      summary: '',
      content: '',
      status: '1'
    })
    const contentRules = {
      title: [
        { required: true, message: '请输入内容标题', trigger: 'blur' },
        { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
      ],
      contentType: [
        { required: true, message: '请选择内容类型', trigger: 'change' }
      ],
      author: [
        { required: true, message: '请输入作者', trigger: 'blur' }
      ],
      summary: [
        { required: true, message: '请输入内容摘要', trigger: 'blur' },
        { min: 10, max: 200, message: '长度在 10 到 200 个字符', trigger: 'blur' }
      ],
      content: [
        { required: true, message: '请输入内容详情', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择内容状态', trigger: 'change' }
      ]
    }
    const contents = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalContents = ref(0)
    
    // 模拟内容数据
    const mockContents = [
      { id: 1, title: '2024年服装行业技能认证标准更新公告', contentType: 'notice', author: '管理员', summary: '关于2024年服装行业技能认证标准的最新更新公告', content: '详细内容...', status: 1, viewCount: 1234, createTime: '2024-01-15 10:30:00' },
      { id: 2, title: '服装设计师职业发展前景分析', contentType: 'article', author: '张三', summary: '深入分析服装设计师的职业发展前景和市场需求', content: '详细内容...', status: 1, viewCount: 856, createTime: '2024-01-16 14:20:00' },
      { id: 3, title: '打版师技能认证考试大纲', contentType: 'news', author: '李四', summary: '2024年打版师技能认证考试的详细大纲', content: '详细内容...', status: 1, viewCount: 623, createTime: '2024-01-17 09:15:00' },
      { id: 4, title: '2023年度技能认证通过率统计报告', contentType: 'article', author: '王五', summary: '2023年度服装行业技能认证的通过率统计和分析', content: '详细内容...', status: 1, viewCount: 456, createTime: '2024-01-18 16:45:00' },
      { id: 5, title: '质检员认证流程优化通知', contentType: 'notice', author: '管理员', summary: '关于质检员认证流程优化的通知', content: '详细内容...', status: 1, viewCount: 321, createTime: '2024-01-19 11:20:00' },
      { id: 6, title: '服装行业数字化转型趋势', contentType: 'news', author: '赵六', summary: '服装行业数字化转型的最新趋势和案例分析', content: '详细内容...', status: 0, viewCount: 0, createTime: '2024-01-20 15:30:00' },
      { id: 7, title: '2024年春季招聘需求预测', contentType: 'article', author: '孙七', summary: '2024年春季服装行业的招聘需求预测', content: '详细内容...', status: 1, viewCount: 789, createTime: '2024-01-21 08:45:00' },
      { id: 8, title: '技能认证教材更新说明', contentType: 'notice', author: '管理员', summary: '关于技能认证教材更新的说明', content: '详细内容...', status: 0, viewCount: 0, createTime: '2024-01-22 13:10:00' }
    ]
    
    onMounted(() => {
      loadContents()
    })
    
    const loadContents = () => {
      // 模拟加载内容数据
      contents.value = mockContents
      totalContents.value = mockContents.length
    }
    
    const searchContents = () => {
      // 模拟搜索功能
      let filteredContents = [...mockContents]
      if (searchForm.title) {
        filteredContents = filteredContents.filter(content => content.title.includes(searchForm.title))
      }
      if (searchForm.contentType) {
        filteredContents = filteredContents.filter(content => content.contentType === searchForm.contentType)
      }
      if (searchForm.status) {
        filteredContents = filteredContents.filter(content => content.status === parseInt(searchForm.status))
      }
      contents.value = filteredContents
      totalContents.value = filteredContents.length
      currentPage.value = 1
    }
    
    const resetSearch = () => {
      searchForm.title = ''
      searchForm.contentType = ''
      searchForm.status = ''
      loadContents()
    }
    
    const getContentTypeText = (type) => {
      const typeMap = {
        article: '文章',
        notice: '公告',
        news: '新闻'
      }
      return typeMap[type] || '未知'
    }
    
    const getContentTypeTagType = (type) => {
      const typeMap = {
        article: 'info',
        notice: 'warning',
        news: 'success'
      }
      return typeMap[type] || 'info'
    }
    
    const getStatusText = (status) => {
      return status === 1 ? '已发布' : '草稿'
    }
    
    const getStatusTagType = (status) => {
      return status === 1 ? 'success' : 'danger'
    }
    
    const openCreateContentDialog = () => {
      // 重置表单
      contentForm.title = ''
      contentForm.contentType = 'article'
      contentForm.author = ''
      contentForm.summary = ''
      contentForm.content = ''
      contentForm.status = '1'
      createContentDialogVisible.value = true
    }
    
    const createContent = () => {
      contentFormRef.value.validate((valid) => {
        if (valid) {
          // 模拟发布内容
          ElMessage.success('内容发布成功')
          createContentDialogVisible.value = false
          loadContents()
        } else {
          return false
        }
      })
    }
    
    const viewContentDetails = (content) => {
      // 查看内容详情逻辑
      ElMessage.info('查看内容详情功能开发中')
    }
    
    const editContent = (content) => {
      // 编辑内容逻辑
      ElMessage.info('编辑内容功能开发中')
    }
    
    const publishContent = (content) => {
      // 发布内容逻辑
      ElMessage.success('内容已发布')
      content.status = 1
    }
    
    const unpublishContent = (content) => {
      // 下架内容逻辑
      ElMessage.success('内容已下架')
      content.status = 0
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      loadContents()
    }
    
    const handleCurrentChange = (current) => {
      currentPage.value = current
      loadContents()
    }
    
    return {
      contentFormRef,
      createContentDialogVisible,
      searchForm,
      contentForm,
      contentRules,
      contents,
      currentPage,
      pageSize,
      totalContents,
      getContentTypeText,
      getContentTypeTagType,
      getStatusText,
      getStatusTagType,
      openCreateContentDialog,
      createContent,
      searchContents,
      resetSearch,
      viewContentDetails,
      editContent,
      publishContent,
      unpublishContent,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.content-manage-container {
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

.content-filters {
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
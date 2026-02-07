<template>
  <div class="content-manage-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>内容管理</h2>
      <p>管理系统公告、行业动态和平台资讯</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon notice-icon">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ noticeCount }}</span>
          <span class="stat-label">系统公告</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon article-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ articleCount }}</span>
          <span class="stat-label">文章资讯</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon news-icon">
          <el-icon><Promotion /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ newsCount }}</span>
          <span class="stat-label">行业动态</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon view-icon">
          <el-icon><View /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ totalViews }}</span>
          <span class="stat-label">总浏览量</span>
        </div>
      </div>
    </div>

    <el-card shadow="hover" class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-radio-group v-model="activeTab" @change="handleTabChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="notice">公告</el-radio-button>
              <el-radio-button label="article">文章</el-radio-button>
              <el-radio-button label="news">动态</el-radio-button>
            </el-radio-group>
          </div>
          <el-button type="primary" @click="openCreateContentDialog">
            <el-icon><Plus /></el-icon> 发布内容
          </el-button>
        </div>
      </template>
      
      <div class="content-filters">
        <el-form :inline="true" :model="searchForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入内容标题" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="全部" value=""></el-option>
              <el-option label="已发布" value="1"></el-option>
              <el-option label="草稿" value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchContents">
              <el-icon><Search /></el-icon> 查询
            </el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="contents" style="width: 100%" class="content-table">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="250">
          <template #default="scope">
            <div class="title-cell">
              <span class="title-text">{{ scope.row.title }}</span>
              <el-tag v-if="scope.row.isTop" size="small" type="danger">置顶</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contentType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getContentTypeTagType(scope.row.contentType)" effect="light">
              {{ getContentTypeText(scope.row.contentType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" effect="plain">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80">
          <template #default="scope">
            <span class="view-count">{{ scope.row.viewCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160"></el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="viewContentDetails(scope.row)">查看</el-button>
            <el-button type="warning" link @click="editContent(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 1" type="danger" link @click="unpublishContent(scope.row)">下架</el-button>
            <el-button v-else type="success" link @click="publishContent(scope.row)">发布</el-button>
            <el-button type="danger" link @click="deleteContent(scope.row)">删除</el-button>
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
    
    <!-- 发布/编辑内容对话框 -->
    <el-dialog 
      v-model="createContentDialogVisible" 
      :title="isEditing ? '编辑内容' : '发布内容'" 
      width="800px"
      class="content-dialog"
    >
      <el-form ref="contentFormRef" :model="contentForm" :rules="contentRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="标题" prop="title">
              <el-input v-model="contentForm.title" placeholder="请输入内容标题" maxlength="100" show-word-limit></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="内容类型" prop="contentType">
              <el-select v-model="contentForm.contentType" placeholder="请选择" style="width: 100%">
                <el-option label="系统公告" value="notice">
                  <el-icon><Bell /></el-icon> 系统公告
                </el-option>
                <el-option label="文章资讯" value="article">
                  <el-icon><Document /></el-icon> 文章资讯
                </el-option>
                <el-option label="行业动态" value="news">
                  <el-icon><Promotion /></el-icon> 行业动态
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="作者" prop="author">
              <el-input v-model="contentForm.author" placeholder="请输入作者"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="contentForm.status" placeholder="请选择" style="width: 100%">
                <el-option label="立即发布" value="1"></el-option>
                <el-option label="保存草稿" value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="置顶">
              <el-switch v-model="contentForm.isTop" active-text="是" inactive-text="否"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="内容摘要" prop="summary">
          <el-input 
            v-model="contentForm.summary" 
            type="textarea" 
            :rows="3"
            placeholder="请输入内容摘要（将显示在列表中）"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
        
        <!-- 行业动态特有字段 -->
        <template v-if="contentForm.contentType === 'news'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="来源名称">
                <el-input v-model="contentForm.sourceName" placeholder="如：中国服装协会">
                  <template #prefix>
                    <el-icon><Document /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="来源链接">
                <el-input v-model="contentForm.sourceUrl" placeholder="原文链接地址">
                  <template #prefix>
                    <el-icon><Link /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="封面图片">
            <el-input v-model="contentForm.coverImage" placeholder="封面图片URL">
              <template #prefix>
                <el-icon><Picture /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="标签">
            <el-select 
              v-model="contentForm.tags" 
              multiple 
              filterable 
              allow-create 
              default-first-option
              placeholder="选择或输入标签"
              style="width: 100%"
            >
              <el-option label="行业趋势" value="行业趋势"></el-option>
              <el-option label="政策法规" value="政策法规"></el-option>
              <el-option label="技术创新" value="技术创新"></el-option>
              <el-option label="市场分析" value="市场分析"></el-option>
              <el-option label="企业动态" value="企业动态"></el-option>
              <el-option label="展会活动" value="展会活动"></el-option>
            </el-select>
          </el-form-item>
        </template>
        
        <el-form-item label="内容详情" prop="content">
          <el-input 
            v-model="contentForm.content" 
            type="textarea" 
            :rows="12" 
            placeholder="请输入内容详情（支持富文本格式）"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createContentDialogVisible = false">取消</el-button>
          <el-button type="info" @click="saveDraft" v-if="!isEditing">保存草稿</el-button>
          <el-button type="primary" @click="createContent">
            {{ isEditing ? '保存修改' : '立即发布' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 内容详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      :title="selectedContent.title" 
      width="700px"
      class="detail-dialog"
    >
      <div class="content-detail" v-if="selectedContent">
        <div class="detail-meta">
          <el-tag :type="getContentTypeTagType(selectedContent.contentType)">
            {{ getContentTypeText(selectedContent.contentType) }}
          </el-tag>
          <span class="meta-item">
            <el-icon><User /></el-icon> {{ selectedContent.author }}
          </span>
          <span class="meta-item">
            <el-icon><Clock /></el-icon> {{ selectedContent.createTime }}
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon> {{ selectedContent.viewCount }} 次浏览
          </span>
        </div>
        <div class="detail-summary">
          <h4>摘要</h4>
          <p>{{ selectedContent.summary }}</p>
        </div>
        <div class="detail-content">
          <h4>正文</h4>
          <div class="content-body" v-html="selectedContent.content"></div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="editContent(selectedContent)">编辑</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Document, Promotion, View, Plus, Search, User, Clock, Link, Picture } from '@element-plus/icons-vue'
import request from '../../utils/request'

export default {
  name: 'ContentManage',
  components: {
    Bell, Document, Promotion, View, Plus, Search, User, Clock, Link, Picture
  },
  setup() {
    const contentFormRef = ref(null)
    const createContentDialogVisible = ref(false)
    const detailDialogVisible = ref(false)
    const isEditing = ref(false)
    const selectedContent = ref({})
    const activeTab = ref('all')
    
    const searchForm = reactive({
      title: '',
      contentType: '',
      status: ''
    })
    
    const contentForm = reactive({
      id: null,
      title: '',
      contentType: 'notice',
      author: '',
      summary: '',
      content: '',
      status: '1',
      isTop: false,
      // 行业动态特有字段
      sourceUrl: '',
      sourceName: '',
      coverImage: '',
      tags: []
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

    const contentTypeToType = {
      notice: 1,
      article: 2,
      news: 4
    }

    const typeToContentType = {
      1: 'notice',
      2: 'article',
      3: 'article',
      4: 'news'
    }

    const toAnnouncementPayload = () => ({
      id: contentForm.id || undefined,
      title: contentForm.title,
      content: contentForm.content,
      type: contentTypeToType[contentForm.contentType] || 1,
      status: Number(contentForm.status),
      isTop: contentForm.isTop ? 1 : 0,
      publisherName: contentForm.author,
      coverImage: contentForm.contentType === 'news' ? contentForm.coverImage : undefined
    })
    
    // 统计数据
    const noticeCount = computed(() => contents.value.filter(c => c.contentType === 'notice').length)
    const articleCount = computed(() => contents.value.filter(c => c.contentType === 'article').length)
    const newsCount = computed(() => contents.value.filter(c => c.contentType === 'news').length)
    const totalViews = computed(() => contents.value.reduce((sum, c) => sum + (c.viewCount || 0), 0))
    
    onMounted(() => {
      loadContents()
    })
    
    const loadContents = async () => {
      try {
        const type = activeTab.value === 'all' ? undefined : contentTypeToType[activeTab.value]
        const response = await request.get('/announcement/admin/list', {
          params: {
            page: currentPage.value,
            size: pageSize.value,
            type,
            keyword: searchForm.title || undefined,
            status: searchForm.status || undefined
          }
        })

        contents.value = (response.data.records || []).map((item) => ({
          ...item,
          contentType: typeToContentType[item.type] || 'notice',
          author: item.publisherName || item.author || '',
          viewCount: item.views || 0,
          createTime: item.publishTime || item.createTime,
          summary: item.summary || (item.content ? item.content.slice(0, 120) : '')
        }))
        totalContents.value = response.data.total || 0
      } catch (error) {
        console.error('加载内容失败:', error)
        ElMessage.error('加载内容失败')
        contents.value = []
        totalContents.value = 0
      }
    }
    
    const handleTabChange = () => {
      loadContents()
    }
    
    const searchContents = () => {
      loadContents()
    }
    
    const resetSearch = () => {
      searchForm.title = ''
      searchForm.status = ''
      loadContents()
    }
    
    const getContentTypeText = (type) => {
      const typeMap = {
        article: '文章',
        notice: '公告',
        news: '动态'
      }
      return typeMap[type] || '未知'
    }
    
    const getContentTypeTagType = (type) => {
      const typeMap = {
        article: '',
        notice: 'warning',
        news: 'success'
      }
      return typeMap[type] || 'info'
    }
    
    const getStatusText = (status) => {
      return status === 1 ? '已发布' : '草稿'
    }
    
    const getStatusTagType = (status) => {
      return status === 1 ? 'success' : 'info'
    }
    
    const resetForm = () => {
      contentForm.id = null
      contentForm.title = ''
      contentForm.contentType = 'notice'
      contentForm.author = ''
      contentForm.summary = ''
      contentForm.content = ''
      contentForm.status = '1'
      contentForm.isTop = false
      contentForm.sourceUrl = ''
      contentForm.sourceName = ''
      contentForm.coverImage = ''
      contentForm.tags = []
    }
    
    const openCreateContentDialog = () => {
      isEditing.value = false
      resetForm()
      createContentDialogVisible.value = true
    }
    
    const saveDraft = () => {
      contentForm.status = '0'
      createContent()
    }
    
    const createContent = async () => {
      contentFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const payload = toAnnouncementPayload()
            if (isEditing.value) {
              await request.put('/announcement', payload)
            } else if (Number(contentForm.status) === 1) {
              await request.post('/announcement/publish', payload)
            } else {
              await request.post('/announcement/draft', payload)
            }

            if (isEditing.value) {
              ElMessage.success('内容修改成功')
            } else {
              ElMessage.success(contentForm.status === '1' ? '内容发布成功' : '草稿保存成功')
            }
            createContentDialogVisible.value = false
            loadContents()
          } catch (error) {
            console.error('保存内容失败:', error)
            ElMessage.error('保存失败，请稍后重试')
          }
        } else {
          return false
        }
      })
    }
    
    const viewContentDetails = (content) => {
      selectedContent.value = content
      detailDialogVisible.value = true
    }
    
    const editContent = (content) => {
      isEditing.value = true
      Object.assign(contentForm, {
        id: content.id,
        title: content.title,
        contentType: content.contentType,
        author: content.author,
        summary: content.summary,
        content: content.content,
        status: String(content.status),
        isTop: content.isTop
      })
      detailDialogVisible.value = false
      createContentDialogVisible.value = true
    }
    
    const publishContent = (content) => {
      ElMessageBox.confirm('确定要发布该内容吗？', '发布确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        await request.put('/announcement', { id: content.id, status: 1 })
        ElMessage.success('内容已发布')
        loadContents()
      }).catch(() => {})
    }
    
    const unpublishContent = (content) => {
      ElMessageBox.confirm('确定要下架该内容吗？', '下架确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await request.put('/announcement/offline', null, {
          params: { announcementId: content.id }
        })
        ElMessage.success('内容已下架')
        loadContents()
      }).catch(() => {})
    }
    
    const deleteContent = (content) => {
      ElMessageBox.confirm('确定要删除该内容吗？此操作不可恢复。', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }).then(async () => {
        await request.delete('/announcement', {
          params: { announcementId: content.id }
        })
        ElMessage.success('内容已删除')
        loadContents()
      }).catch(() => {})
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
      detailDialogVisible,
      isEditing,
      selectedContent,
      activeTab,
      searchForm,
      contentForm,
      contentRules,
      contents,
      currentPage,
      pageSize,
      totalContents,
      noticeCount,
      articleCount,
      newsCount,
      totalViews,
      handleTabChange,
      getContentTypeText,
      getContentTypeTagType,
      getStatusText,
      getStatusTagType,
      openCreateContentDialog,
      saveDraft,
      createContent,
      searchContents,
      resetSearch,
      viewContentDetails,
      editContent,
      publishContent,
      unpublishContent,
      deleteContent,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.content-manage-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 30px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.page-header h2 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 10px 0;
  letter-spacing: -0.5px;
}

.page-header p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 25px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.notice-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.article-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.news-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.view-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 主卡片 */
.main-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.card-header .el-button--primary {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

/* 筛选区域 */
.content-filters {
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

/* 表格样式 */
.content-table {
  border-radius: 12px;
  overflow: hidden;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-text {
  font-weight: 500;
  color: #303133;
}

.view-count {
  color: #667eea;
  font-weight: 500;
}

/* 分页 */
.pagination-container {
  margin-top: 25px;
  display: flex;
  justify-content: flex-end;
}

/* 对话框样式 */
.content-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #e4e7ed;
  padding: 20px 24px;
}

.content-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 详情对话框 */
.content-detail {
  padding: 10px 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
}

.detail-summary, .detail-content {
  margin-bottom: 25px;
}

.detail-summary h4, .detail-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
  display: inline-block;
}

.detail-summary p {
  color: #606266;
  line-height: 1.8;
  margin: 0;
}

.content-body {
  color: #606266;
  line-height: 2;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .content-manage-container {
    padding: 15px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    gap: 15px;
  }

  .header-left {
    width: 100%;
    justify-content: center;
  }
}
</style>

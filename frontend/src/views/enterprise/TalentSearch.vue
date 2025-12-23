<template>
  <div class="talent-search-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>人才搜索</h1>
        <p class="subtitle">发现服装行业优秀人才，精准匹配岗位需求</p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-num">{{ totalTalents }}</span>
          <span class="stat-label">认证人才</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ activeToday }}</span>
          <span class="stat-label">今日活跃</span>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索姓名、技能、岗位..."
          size="large"
          clearable
          @keyup.enter="searchTalents"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="large" @click="searchTalents">
          搜索人才
        </el-button>
      </div>

      <!-- 快捷筛选标签 -->
      <div class="quick-tags">
        <span class="tag-label">热门岗位：</span>
        <el-tag
          v-for="tag in hotTags"
          :key="tag"
          effect="plain"
          :class="{ active: selectedTag === tag }"
          @click="selectTag(tag)"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>

    <!-- 高级筛选 -->
    <el-collapse v-model="filterExpanded" class="filter-collapse">
      <el-collapse-item name="filter">
        <template #title>
          <div class="filter-title">
            <el-icon><Filter /></el-icon>
            高级筛选
            <el-tag v-if="activeFilterCount > 0" type="primary" size="small">
              {{ activeFilterCount }}项
            </el-tag>
          </div>
        </template>
        <div class="filter-grid">
          <div class="filter-item">
            <label>岗位类型</label>
            <el-select v-model="filters.jobType" placeholder="全部岗位" clearable>
              <el-option label="服装设计师" value="designer" />
              <el-option label="服装版型师" value="patternmaker" />
              <el-option label="时装买手" value="buyer" />
              <el-option label="服装质检员" value="inspector" />
              <el-option label="服装导购" value="sales" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>认证等级</label>
            <el-select v-model="filters.certLevel" placeholder="全部等级" clearable>
              <el-option label="初级" value="junior" />
              <el-option label="中级" value="middle" />
              <el-option label="高级" value="senior" />
              <el-option label="专家级" value="expert" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>工作经验</label>
            <el-select v-model="filters.experience" placeholder="不限经验" clearable>
              <el-option label="应届生" value="0" />
              <el-option label="1-3年" value="1-3" />
              <el-option label="3-5年" value="3-5" />
              <el-option label="5-10年" value="5-10" />
              <el-option label="10年以上" value="10+" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>学历要求</label>
            <el-select v-model="filters.education" placeholder="不限学历" clearable>
              <el-option label="高中/中专" value="high" />
              <el-option label="大专" value="college" />
              <el-option label="本科" value="bachelor" />
              <el-option label="硕士及以上" value="master" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>期望薪资</label>
            <el-select v-model="filters.salary" placeholder="不限薪资" clearable>
              <el-option label="5K以下" value="0-5" />
              <el-option label="5K-10K" value="5-10" />
              <el-option label="10K-20K" value="10-20" />
              <el-option label="20K-30K" value="20-30" />
              <el-option label="30K以上" value="30+" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>所在城市</label>
            <el-select v-model="filters.city" placeholder="不限城市" clearable>
              <el-option label="北京" value="beijing" />
              <el-option label="上海" value="shanghai" />
              <el-option label="广州" value="guangzhou" />
              <el-option label="深圳" value="shenzhen" />
              <el-option label="杭州" value="hangzhou" />
            </el-select>
          </div>
        </div>
        <div class="filter-actions">
          <el-button @click="resetFilters">重置筛选</el-button>
          <el-button type="primary" @click="applyFilters">应用筛选</el-button>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 搜索结果统计 -->
    <div class="result-header">
      <span class="result-count">找到 <strong>{{ talents.length }}</strong> 位人才</span>
      <div class="sort-options">
        <span>排序：</span>
        <el-radio-group v-model="sortBy" size="small">
          <el-radio-button label="match">匹配度</el-radio-button>
          <el-radio-button label="active">活跃度</el-radio-button>
          <el-radio-button label="experience">经验</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 人才列表 -->
    <div class="talent-list">
      <div class="talent-card" v-for="talent in talents" :key="talent.id">
        <div class="talent-avatar">
          <el-avatar :size="72" :src="talent.avatar">{{ talent.name?.charAt(0) }}</el-avatar>
          <div class="online-status" :class="{ online: talent.isOnline }"></div>
        </div>
        <div class="talent-info">
          <div class="talent-header">
            <h3 class="talent-name">{{ talent.name }}</h3>
            <el-tag v-if="talent.certLevel" :type="getCertLevelType(talent.certLevel)" size="small">
              {{ talent.certLevel }}
            </el-tag>
          </div>
          <p class="talent-title">{{ talent.jobTitle }}</p>
          <div class="talent-meta">
            <span><el-icon><Location /></el-icon>{{ talent.city }}</span>
            <span><el-icon><Briefcase /></el-icon>{{ talent.experience }}</span>
            <span><el-icon><School /></el-icon>{{ talent.education }}</span>
          </div>
          <div class="talent-skills">
            <el-tag v-for="skill in talent.skills?.slice(0, 4)" :key="skill" size="small" effect="plain">
              {{ skill }}
            </el-tag>
          </div>
          <p class="talent-intro">{{ talent.intro }}</p>
        </div>
        <div class="talent-actions">
          <div class="salary-expect">
            <span class="salary-label">期望薪资</span>
            <span class="salary-value">{{ talent.expectedSalary }}</span>
          </div>
          <div class="action-buttons">
            <el-button type="primary" @click="viewResume(talent)">
              <el-icon><View /></el-icon>
              查看简历
            </el-button>
            <el-button @click="inviteInterview(talent)">
              <el-icon><ChatDotRound /></el-icon>
              邀请面试
            </el-button>
            <el-button :icon="talent.collected ? 'StarFilled' : 'Star'"
              :type="talent.collected ? 'warning' : 'default'"
              @click="toggleCollect(talent)"
            >
              {{ talent.collected ? '已收藏' : '收藏' }}
            </el-button>
          </div>
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
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Filter, Location, Briefcase, School, View, ChatDotRound, Star, StarFilled } from '@element-plus/icons-vue'

const searchKeyword = ref('')
const selectedTag = ref('')
const filterExpanded = ref([])
const sortBy = ref('match')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(50)
const totalTalents = ref(12580)
const activeToday = ref(1256)

const hotTags = ['服装设计师', '版型师', '买手', '质检员', '生产管理']

const filters = reactive({
  jobType: '',
  certLevel: '',
  experience: '',
  education: '',
  salary: '',
  city: ''
})

const activeFilterCount = computed(() => {
  return Object.values(filters).filter(v => v).length
})

const talents = ref([
  { id: 1, name: '张小雅', avatar: '', jobTitle: '高级服装设计师', certLevel: '高级认证', city: '上海', experience: '5年', education: '本科', expectedSalary: '20K-30K', skills: ['女装设计', 'AI设计', '面料选择', '色彩搭配'], intro: '5年高端女装设计经验，擅长原创设计和品牌策划，曾主导多个系列产品开发。', isOnline: true, collected: false },
  { id: 2, name: '李明华', avatar: '', jobTitle: '资深版型师', certLevel: '中级认证', city: '杭州', experience: '8年', education: '大专', expectedSalary: '15K-20K', skills: ['CAD制版', '立体裁剪', '样衣制作'], intro: '8年版型工作经验，精通各类服装版型开发，熟练使用ET、格柏等制版软件。', isOnline: false, collected: true },
  { id: 3, name: '王雨晴', avatar: '', jobTitle: '时装买手', certLevel: '初级认证', city: '深圳', experience: '3年', education: '本科', expectedSalary: '12K-18K', skills: ['市场分析', '品牌采购', '趋势预测'], intro: '3年时装买手经验，对国际时尚趋势敏感，擅长品牌组合和商品策划。', isOnline: true, collected: false }
])

const selectTag = (tag) => {
  selectedTag.value = selectedTag.value === tag ? '' : tag
  searchTalents()
}

const searchTalents = () => {
  ElMessage.info('搜索功能已触发')
}

const resetFilters = () => {
  Object.keys(filters).forEach(k => filters[k] = '')
}

const applyFilters = () => {
  ElMessage.success('筛选条件已应用')
}

const getCertLevelType = (level) => {
  const types = { '初级认证': 'info', '中级认证': 'warning', '高级认证': 'success', '专家认证': 'danger' }
  return types[level] || 'info'
}

const viewResume = (talent) => { ElMessage.info(`查看 ${talent.name} 的简历`) }
const inviteInterview = (talent) => { ElMessage.success(`已向 ${talent.name} 发送面试邀请`) }
const toggleCollect = (talent) => {
  talent.collected = !talent.collected
  ElMessage.success(talent.collected ? '已收藏' : '已取消收藏')
}
</script>

<style scoped>
.talent-search-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px 40px;
  border-radius: 20px;
  margin-bottom: 24px;
  color: #fff;
}

.header-content h1 { font-size: 32px; margin: 0 0 8px 0; }
.header-content .subtitle { opacity: 0.9; margin: 0; }
.header-stats { display: flex; gap: 40px; }
.stat-item { text-align: center; }
.stat-num { font-size: 36px; font-weight: 700; display: block; }
.stat-label { font-size: 14px; opacity: 0.9; }

.search-section {
  background: #fff;
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.search-bar { display: flex; gap: 16px; margin-bottom: 20px; }
.search-bar .el-input { flex: 1; }

.quick-tags { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.tag-label { color: #909399; font-size: 14px; }
.quick-tags .el-tag { cursor: pointer; transition: all 0.3s; }
.quick-tags .el-tag:hover, .quick-tags .el-tag.active { background: #667eea; color: #fff; border-color: #667eea; }

.filter-collapse { background: #fff; border-radius: 16px; margin-bottom: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); }
.filter-title { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.filter-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; padding: 20px; }
.filter-item label { display: block; margin-bottom: 8px; color: #606266; font-size: 14px; }
.filter-actions { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 20px; border-top: 1px solid #f0f0f0; }

.result-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.result-count { font-size: 15px; color: #606266; }
.result-count strong { color: #667eea; font-size: 18px; }

.talent-list { display: flex; flex-direction: column; gap: 16px; }

.talent-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  gap: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.talent-card:hover { transform: translateY(-4px); box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12); }

.talent-avatar { position: relative; flex-shrink: 0; }
.online-status { position: absolute; bottom: 4px; right: 4px; width: 14px; height: 14px; border-radius: 50%; background: #dcdfe6; border: 2px solid #fff; }
.online-status.online { background: #67c23a; }

.talent-info { flex: 1; }
.talent-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.talent-name { font-size: 18px; font-weight: 600; margin: 0; color: #303133; }
.talent-title { color: #606266; margin: 0 0 12px 0; }
.talent-meta { display: flex; gap: 20px; margin-bottom: 12px; font-size: 13px; color: #909399; }
.talent-meta span { display: flex; align-items: center; gap: 4px; }
.talent-skills { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.talent-intro { color: #606266; font-size: 14px; line-height: 1.6; margin: 0; }

.talent-actions { display: flex; flex-direction: column; justify-content: space-between; align-items: flex-end; min-width: 160px; }
.salary-expect { text-align: right; margin-bottom: 16px; }
.salary-label { font-size: 12px; color: #909399; display: block; }
.salary-value { font-size: 20px; font-weight: 700; color: #f56c6c; }
.action-buttons { display: flex; flex-direction: column; gap: 8px; }

.pagination-wrapper { display: flex; justify-content: center; padding: 24px; background: #fff; border-radius: 12px; margin-top: 24px; }

@media (max-width: 1200px) { .filter-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .page-header { flex-direction: column; text-align: center; gap: 20px; }
  .filter-grid { grid-template-columns: 1fr; }
  .talent-card { flex-direction: column; }
  .talent-actions { align-items: stretch; width: 100%; }
}
</style>

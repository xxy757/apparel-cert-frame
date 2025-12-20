<template>
  <div class="training-container">
    <div class="page-header">
      <h2>培训推荐</h2>
      <p>提升您的服装行业技能，开启职业新篇章</p>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索培训课程名称或关键词"
          clearable
          @keyup.enter="searchTrainings"
        >
          <template #append>
            <el-button type="primary" @click="searchTrainings">
              <i class="el-icon-search"></i> 搜索
            </el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-section">
        <el-card shadow="hover">
          <h3>筛选条件</h3>
          <div class="filter-form">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="培训类别">
                  <el-select v-model="filters.category" placeholder="请选择培训类别" clearable>
                    <el-option label="服装设计" value="服装设计"></el-option>
                    <el-option label="服装打版" value="服装打版"></el-option>
                    <el-option label="服装质检" value="服装质检"></el-option>
                    <el-option label="服装营销" value="服装营销"></el-option>
                    <el-option label="生产管理" value="生产管理"></el-option>
                    <el-option label="时尚趋势" value="时尚趋势"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="培训形式">
                  <el-select v-model="filters.format" placeholder="请选择培训形式" clearable>
                    <el-option label="线上培训" value="线上培训"></el-option>
                    <el-option label="线下培训" value="线下培训"></el-option>
                    <el-option label="混合培训" value="混合培训"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="培训级别">
                  <el-select v-model="filters.level" placeholder="请选择培训级别" clearable>
                    <el-option label="初级" value="初级"></el-option>
                    <el-option label="中级" value="中级"></el-option>
                    <el-option label="高级" value="高级"></el-option>
                    <el-option label="专家级" value="专家级"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="培训时长">
                  <el-select v-model="filters.duration" placeholder="请选择培训时长" clearable>
                    <el-option label="1-3天" value="1-3天"></el-option>
                    <el-option label="1-2周" value="1-2周"></el-option>
                    <el-option label="1个月" value="1个月"></el-option>
                    <el-option label="2-3个月" value="2-3个月"></el-option>
                    <el-option label="3个月以上" value="3个月以上"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="认证类型">
                  <el-select v-model="filters.certification" placeholder="请选择认证类型" clearable>
                    <el-option label="行业认证" value="行业认证"></el-option>
                    <el-option label="企业认证" value="企业认证"></el-option>
                    <el-option label="技能证书" value="技能证书"></el-option>
                    <el-option label="无认证" value="无认证"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="价格范围">
                  <el-select v-model="filters.priceRange" placeholder="请选择价格范围" clearable>
                    <el-option label="免费" value="免费"></el-option>
                    <el-option label="1000元以下" value="1000元以下"></el-option>
                    <el-option label="1000-5000元" value="1000-5000元"></el-option>
                    <el-option label="5000-10000元" value="5000-10000元"></el-option>
                    <el-option label="10000元以上" value="10000元以上"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="filter-actions">
              <el-button type="primary" @click="searchTrainings">应用筛选</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 培训课程列表 -->
    <div class="training-list-section">
      <div class="section-header">
        <h3>推荐课程</h3>
        <span class="result-count">{{ trainings.length }} 个课程</span>
      </div>

      <div class="training-list">
        <el-card
          v-for="training in trainings"
          :key="training.id"
          shadow="hover"
          class="training-card"
          @click="viewTrainingDetail(training)"
        >
          <div class="training-card-header">
            <img :src="training.image" alt="培训封面" class="training-image">
            <div class="training-tags">
              <el-tag size="small" :type="getCategoryColor(training.category)">{{ training.category }}</el-tag>
              <el-tag size="small" v-if="training.certification" type="success">认证课程</el-tag>
              <el-tag size="small" v-if="training.isHot" type="danger">热门</el-tag>
              <el-tag size="small" v-if="training.discount" type="warning">折扣</el-tag>
            </div>
          </div>
          
          <div class="training-card-content">
            <div class="training-info">
              <h4 class="training-title">{{ training.title }}</h4>
              <div class="training-meta">
                <div class="trainer-info">
                  <img :src="training.trainer.avatar" alt="讲师头像" class="trainer-avatar">
                  <span class="trainer-name">{{ training.trainer.name }}</span>
                  <span class="trainer-title">{{ training.trainer.title }}</span>
                </div>
                <div class="training-stats">
                  <el-rate
                    v-model="training.rating"
                    disabled
                    size="small"
                    show-score
                    text-color="#ff9900"
                    score-template="{value}"
                  ></el-rate>
                  <span class="enrollment-count">{{ training.enrollmentCount }} 人已报名</span>
                </div>
              </div>
              <div class="training-description">
                {{ training.description.substring(0, 120) }}...
              </div>
              <div class="training-features">
                <el-icon class="feature-icon"><Clock /></el-icon>
                <span>{{ training.duration }}</span>
                <el-icon class="feature-icon"><Calendar /></el-icon>
                <span>{{ formatDate(training.startDate) }}</span>
                <el-icon class="feature-icon"><View /></el-icon>
                <span>{{ training.format }}</span>
              </div>
            </div>
            
            <div class="training-price-section">
              <div class="price-container">
                <div class="price">
                  <span v-if="training.discount" class="original-price">¥{{ training.originalPrice }}</span>
                  <span class="current-price">¥{{ training.price }}</span>
                </div>
                <div class="discount" v-if="training.discount">
                  {{ training.discount }}折
                </div>
              </div>
              <el-button type="primary" @click.stop="enrollTraining(training)">
                <i class="el-icon-plus"></i> 立即报名
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[12, 24, 48]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="trainings.length"
        ></el-pagination>
      </div>
    </div>

    <!-- 培训详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="selectedTraining.title"
      width="800px"
      @close="resetSelectedTraining"
    >
      <div v-if="selectedTraining" class="training-detail">
        <div class="training-detail-header">
          <img :src="selectedTraining.image" alt="培训封面" class="training-detail-image">
          <div class="training-detail-tags">
            <el-tag size="small" :type="getCategoryColor(selectedTraining.category)">{{ selectedTraining.category }}</el-tag>
            <el-tag size="small" v-if="selectedTraining.certification" type="success">认证课程</el-tag>
            <el-tag size="small" v-if="selectedTraining.isHot" type="danger">热门</el-tag>
            <el-tag size="small" v-if="selectedTraining.discount" type="warning">折扣</el-tag>
          </div>
        </div>

        <div class="training-detail-content">
          <div class="training-info-section">
            <div class="info-header">
              <h3>{{ selectedTraining.title }}</h3>
              <div class="training-status">
                <el-rate
                  v-model="selectedTraining.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value} 分"
                ></el-rate>
                <span class="enrollment-count">{{ selectedTraining.enrollmentCount }} 人已报名</span>
              </div>
            </div>

            <div class="trainer-section">
              <h4>讲师信息</h4>
              <div class="trainer-detail">
                <img :src="selectedTraining.trainer.avatar" alt="讲师头像" class="trainer-detail-avatar">
                <div class="trainer-detail-info">
                  <h5>{{ selectedTraining.trainer.name }}</h5>
                  <p class="trainer-title">{{ selectedTraining.trainer.title }}</p>
                  <p class="trainer-bio">{{ selectedTraining.trainer.bio }}</p>
                </div>
              </div>
            </div>

            <div class="training-overview">
              <h4>课程概况</h4>
              <div class="overview-grid">
                <div class="overview-item">
                  <el-icon class="overview-icon"><Clock /></el-icon>
                  <div>
                    <div class="overview-label">培训时长</div>
                    <div class="overview-value">{{ selectedTraining.duration }}</div>
                  </div>
                </div>
                <div class="overview-item">
                  <el-icon class="overview-icon"><Calendar /></el-icon>
                  <div>
                    <div class="overview-label">开始时间</div>
                    <div class="overview-value">{{ formatDate(selectedTraining.startDate) }}</div>
                  </div>
                </div>
                <div class="overview-item">
                  <el-icon class="overview-icon"><View /></el-icon>
                  <div>
                    <div class="overview-label">培训形式</div>
                    <div class="overview-value">{{ selectedTraining.format }}</div>
                  </div>
                </div>
                <div class="overview-item">
                  <el-icon class="overview-icon"><User /></el-icon>
                  <div>
                    <div class="overview-label">适合人群</div>
                    <div class="overview-value">{{ selectedTraining.targetAudience }}</div>
                  </div>
                </div>
                <div class="overview-item">
                  <el-icon class="overview-icon"><Coin /></el-icon>
                  <div>
                    <div class="overview-label">认证类型</div>
                    <div class="overview-value">{{ selectedTraining.certification || '无认证' }}</div>
                  </div>
                </div>
                <div class="overview-item">
                  <el-icon class="overview-icon"><Location /></el-icon>
                  <div>
                    <div class="overview-label">培训地点</div>
                    <div class="overview-value">{{ selectedTraining.location }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="course-content">
              <h4>课程内容</h4>
              <el-timeline>
                <el-timeline-item
                  v-for="(module, index) in selectedTraining.modules"
                  :key="index"
                  :timestamp="module.title"
                  placement="top"
                >
                  <div class="module-content">
                    <p>{{ module.description }}</p>
                    <div class="module-duration">
                      <el-icon><Clock /></el-icon> {{ module.duration }}
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>

            <div class="training-benefits">
              <h4>培训收益</h4>
              <ul class="benefits-list">
                <li v-for="(benefit, index) in selectedTraining.benefits" :key="index">
                  <el-icon class="benefit-icon"><Check /></el-icon>
                  {{ benefit }}
                </li>
              </ul>
            </div>
          </div>

          <div class="training-price-sidebar">
            <el-card shadow="always" class="price-card">
              <div class="price-display">
                <div class="current-price">
                  ¥{{ selectedTraining.price }}
                </div>
                <div class="original-price" v-if="selectedTraining.discount">
                  原价: ¥{{ selectedTraining.originalPrice }}
                </div>
                <div class="discount-badge" v-if="selectedTraining.discount">
                  {{ selectedTraining.discount }}折
                </div>
              </div>
              
              <div class="price-features">
                <div class="feature-item">
                  <el-icon><Check /></el-icon>
                  <span>课程视频</span>
                </div>
                <div class="feature-item">
                  <el-icon><Check /></el-icon>
                  <span>课后练习</span>
                </div>
                <div class="feature-item">
                  <el-icon><Check /></el-icon>
                  <span>讲师答疑</span>
                </div>
                <div class="feature-item">
                  <el-icon><Check /></el-icon>
                  <span>学习资料</span>
                </div>
                <div class="feature-item" v-if="selectedTraining.certification">
                  <el-icon><Check /></el-icon>
                  <span>认证证书</span>
                </div>
              </div>
              
              <el-button type="primary" size="large" block @click="enrollTraining(selectedTraining)">
                <i class="el-icon-plus"></i> 立即报名
              </el-button>
              <el-button size="large" block @click="addFavorite(selectedTraining)">
                <i class="el-icon-star-on"></i> 收藏课程
              </el-button>
            </el-card>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Calendar, View, User, Coin, Location, Check } from '@element-plus/icons-vue'

export default {
  name: 'Training',
  components: {
    Clock,
    Calendar,
    View,
    User,
    Coin,
    Location,
    Check
  },
  setup() {
    // 搜索和筛选数据
    const searchQuery = ref('')
    const filters = reactive({
      category: '',
      format: '',
      level: '',
      duration: '',
      certification: '',
      priceRange: ''
    })

    // 培训课程数据
    const trainings = ref([
      {
        id: 1,
        title: '高级服装设计与创意开发',
        description: '本课程将深入讲解高级服装设计的理念和方法，包括创意构思、面料选择、色彩搭配、款式设计等方面的内容。通过实际案例分析和实践操作，提升学员的设计能力和创新思维。',
        category: '服装设计',
        format: '线上培训',
        level: '高级',
        duration: '2个月',
        startDate: '2024-06-15',
        price: 3999,
        originalPrice: 4999,
        discount: 8,
        certification: '行业认证',
        isHot: true,
        enrollmentCount: 156,
        rating: 4.8,
        image: 'https://via.placeholder.com/400x200/FFD700/FFFFFF?text=服装设计课程',
        trainer: {
          name: '张设计师',
          title: '首席设计师',
          avatar: 'https://via.placeholder.com/60',
          bio: '拥有15年服装设计经验，曾任职于国际知名品牌，多次获得国内外设计奖项。'
        },
        modules: [
          {
            title: '模块一：设计理论与创意构思',
            description: '学习服装设计的基本理论和创意构思方法，掌握设计灵感的获取和转化技巧。',
            duration: '2周'
          },
          {
            title: '模块二：面料与色彩搭配',
            description: '了解各种服装面料的特性和应用，掌握色彩搭配的原则和技巧。',
            duration: '2周'
          },
          {
            title: '模块三：款式设计与结构',
            description: '学习服装款式设计的方法和技巧，掌握服装结构的基本原理。',
            duration: '3周'
          },
          {
            title: '模块四：设计实践与作品集',
            description: '通过实际设计项目，提升设计实践能力，完成个人作品集。',
            duration: '3周'
          }
        ],
        benefits: [
          '掌握高级服装设计的核心技能',
          '提升创新思维和设计能力',
          '获得行业认证证书',
          '建立个人设计作品集',
          '拓展行业人脉资源'
        ],
        targetAudience: '有一定设计基础的服装从业者',
        location: '线上'
      },
      {
        id: 2,
        title: '服装打版与工艺技术',
        description: '本课程将系统讲解服装打版的基本原理和工艺技术，包括平面打版、立体裁剪、工艺制作等方面的内容。通过实际操作，提升学员的打版技能和工艺水平。',
        category: '服装打版',
        format: '线下培训',
        level: '中级',
        duration: '1个月',
        startDate: '2024-07-01',
        price: 2999,
        certification: '技能证书',
        isHot: false,
        enrollmentCount: 89,
        rating: 4.6,
        image: 'https://via.placeholder.com/400x200/87CEEB/FFFFFF?text=服装打版课程',
        trainer: {
          name: '李打版师',
          title: '高级打版师',
          avatar: 'https://via.placeholder.com/60',
          bio: '拥有20年服装打版经验，曾为多个知名品牌提供打版服务，技术精湛。'
        },
        modules: [
          {
            title: '模块一：打版基础理论',
            description: '学习服装打版的基本原理和术语，掌握人体测量和尺寸设计。',
            duration: '1周'
          },
          {
            title: '模块二：平面打版技术',
            description: '掌握服装平面打版的方法和技巧，包括上衣、裤子、裙子等基本款式的打版。',
            duration: '2周'
          },
          {
            title: '模块三：立体裁剪技术',
            description: '学习立体裁剪的基本方法和技巧，提升造型能力。',
            duration: '2周'
          },
          {
            title: '模块四：工艺制作与调整',
            description: '掌握服装工艺制作的基本方法，学习版型调整和优化。',
            duration: '1周'
          }
        ],
        benefits: [
          '掌握服装打版的核心技术',
          '提升工艺制作能力',
          '获得技能证书',
          '适应不同品牌的打版要求',
          '提高工作效率和质量'
        ],
        targetAudience: '服装打版师和相关从业者',
        location: '北京、上海、广州'
      },
      {
        id: 3,
        title: '服装质检与质量控制',
        description: '本课程将全面讲解服装质检的标准和方法，包括面料检验、成衣检验、质量控制等方面的内容。通过实际案例分析，提升学员的质检能力和质量意识。',
        category: '服装质检',
        format: '混合培训',
        level: '初级',
        duration: '2周',
        startDate: '2024-06-20',
        price: 1999,
        certification: '企业认证',
        isHot: true,
        enrollmentCount: 123,
        rating: 4.7,
        image: 'https://via.placeholder.com/400x200/98FB98/000000?text=服装质检课程',
        trainer: {
          name: '王质检员',
          title: '质量总监',
          avatar: 'https://via.placeholder.com/60',
          bio: '拥有18年服装质量控制经验，熟悉国内外质量标准和检测方法。'
        },
        modules: [
          {
            title: '模块一：质检基础知识',
            description: '学习服装质量检验的基本概念和标准，掌握质检的基本流程。',
            duration: '3天'
          },
          {
            title: '模块二：面料检验技术',
            description: '掌握各种服装面料的检验方法和标准，识别常见的面料缺陷。',
            duration: '3天'
          },
          {
            title: '模块三：成衣检验技术',
            description: '学习成衣检验的方法和标准，掌握不同服装品类的检验要点。',
            duration: '4天'
          },
          {
            title: '模块四：质量控制与管理',
            description: '了解服装质量控制的方法和工具，掌握质量管理的基本原理。',
            duration: '4天'
          }
        ],
        benefits: [
          '掌握服装质检的核心技能',
          '提升质量控制能力',
          '获得企业认证证书',
          '适应不同客户的质量要求',
          '提高产品质量和客户满意度'
        ],
        targetAudience: '服装质检人员和质量管理人员',
        location: '线上+线下'
      },
      {
        id: 4,
        title: '服装营销与品牌管理',
        description: '本课程将系统讲解服装营销的策略和方法，包括市场分析、品牌建设、渠道管理、促销策划等方面的内容。通过实际案例分析，提升学员的营销能力和品牌意识。',
        category: '服装营销',
        format: '线上培训',
        level: '中级',
        duration: '1.5个月',
        startDate: '2024-07-10',
        price: 2599,
        certification: '行业认证',
        isHot: false,
        enrollmentCount: 98,
        rating: 4.5,
        image: 'https://via.placeholder.com/400x200/FF69B4/FFFFFF?text=服装营销课程',
        trainer: {
          name: '刘营销师',
          title: '营销总监',
          avatar: 'https://via.placeholder.com/60',
          bio: '拥有12年服装营销经验，曾成功打造多个服装品牌，营销业绩突出。'
        },
        modules: [
          {
            title: '模块一：服装市场分析',
            description: '学习服装市场的分析方法，掌握市场调研和消费者行为分析。',
            duration: '2周'
          },
          {
            title: '模块二：品牌建设与管理',
            description: '了解服装品牌建设的方法和策略，掌握品牌管理的基本原理。',
            duration: '2周'
          },
          {
            title: '模块三：渠道管理与销售',
            description: '学习服装渠道管理的方法和技巧，掌握销售管理的基本原理。',
            duration: '2周'
          },
          {
            title: '模块四：促销策划与执行',
            description: '掌握服装促销策划的方法和技巧，提升促销活动的执行能力。',
            duration: '1周'
          }
        ],
        benefits: [
          '掌握服装营销的核心技能',
          '提升品牌建设能力',
          '获得行业认证证书',
          '提高销售业绩和市场份额',
          '拓展职业发展空间'
        ],
        targetAudience: '服装营销人员和品牌管理人员',
        location: '线上'
      }
    ])

    // 分页数据
    const currentPage = ref(1)
    const pageSize = ref(12)

    // 培训详情对话框
    const dialogVisible = ref(false)
    const selectedTraining = ref({})

    // 生命周期钩子
    onMounted(() => {
      console.log('Training page loaded')
    })

    // 搜索培训
    const searchTrainings = () => {
      // TODO: 调用API搜索培训
      ElMessage.info('搜索功能开发中')
    }

    // 重置筛选条件
    const resetFilters = () => {
      Object.keys(filters).forEach(key => {
        filters[key] = ''
      })
      searchQuery.value = ''
      // TODO: 重置搜索结果
    }

    // 查看培训详情
    const viewTrainingDetail = (training) => {
      selectedTraining.value = training
      dialogVisible.value = true
    }

    // 报名培训
    const enrollTraining = (training) => {
      // TODO: 调用API报名培训
      ElMessage.success(`已报名培训：${training.title}`)
      dialogVisible.value = false
    }

    // 添加收藏
    const addFavorite = (training) => {
      // TODO: 调用API添加收藏
      ElMessage.success(`已收藏培训：${training.title}`)
    }

    // 获取类别颜色
    const getCategoryColor = (category) => {
      const colorMap = {
        '服装设计': 'primary',
        '服装打版': 'success',
        '服装质检': 'warning',
        '服装营销': 'info',
        '生产管理': 'danger',
        '时尚趋势': 'purple'
      }
      return colorMap[category] || 'info'
    }

    // 格式化日期
    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
    }

    // 重置选中的培训
    const resetSelectedTraining = () => {
      selectedTraining.value = {}
    }

    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size
    }

    const handleCurrentChange = (current) => {
      currentPage.value = current
    }

    return {
      searchQuery,
      filters,
      trainings,
      currentPage,
      pageSize,
      dialogVisible,
      selectedTraining,
      searchTrainings,
      resetFilters,
      viewTrainingDetail,
      enrollTraining,
      addFavorite,
      getCategoryColor,
      formatDate,
      resetSelectedTraining,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.training-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 28px;
  color: #304156;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #606266;
}

/* 搜索和筛选区域 */
.search-filter-section {
  margin-bottom: 30px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-bar .el-input {
  width: 100%;
}

.filter-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #304156;
}

.filter-form {
  margin-top: 20px;
}

.filter-actions {
  margin-top: 20px;
  text-align: right;
}

/* 培训列表区域 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 20px;
  color: #304156;
}

.result-count {
  color: #409eff;
  font-weight: 500;
}

.training-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.training-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08),
              0 1px 3px rgba(0, 0, 0, 0.12);
}

.training-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12),
              0 4px 12px rgba(0, 0, 0, 0.16);
}

/* 自定义按钮样式 */
.el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #67c23a 0%, #409eff 100%);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
  transform: translateY(-2px);
}

.el-button--primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.training-price-section .el-button {
  border-radius: 8px;
  font-weight: 500;
}

/* 精致化标签样式 */
.el-tag {
  border-radius: 6px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.training-tags .el-tag {
  margin-bottom: 4px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9);
}

.training-card-header {
  position: relative;
  height: 200px;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
}

.training-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), filter 0.4s ease;
  filter: saturate(1) brightness(1);
}

.training-card:hover .training-image {
  transform: scale(1.1);
  filter: saturate(1.1) brightness(1.05);
}

.training-tags {
  position: absolute;
  bottom: 10px;
  left: 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.training-card-content {
  padding: 16px;
}

.training-title {
  font-size: 18px;
  color: #304156;
  margin-bottom: 12px;
  line-height: 1.4;
}

.training-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.trainer-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trainer-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.trainer-name {
  font-weight: 500;
  color: #304156;
}

.trainer-title {
  font-size: 12px;
  color: #909399;
}

.training-stats {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.enrollment-count {
  font-size: 12px;
  color: #909399;
}

.training-description {
  color: #606266;
  line-height: 1.5;
  margin-bottom: 12px;
  font-size: 14px;
}

.training-features {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.feature-icon {
  margin-right: 4px;
}

.training-price-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-container {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.current-price {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
}

.discount {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 500;
}

/* 培训详情对话框 */
.training-detail {
  padding: 20px 0;
}

.training-detail-header {
  position: relative;
  margin-bottom: 30px;
}

.training-detail-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 8px;
}

.training-detail-tags {
  position: absolute;
  bottom: 16px;
  left: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.training-detail-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 30px;
}

.info-header {
  margin-bottom: 24px;
}

.info-header h3 {
  font-size: 24px;
  color: #304156;
  margin-bottom: 12px;
}

.training-status {
  display: flex;
  align-items: center;
  gap: 16px;
}

.trainer-section {
  margin-bottom: 30px;
}

.trainer-section h4 {
  font-size: 18px;
  color: #304156;
  margin-bottom: 16px;
}

.trainer-detail {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.trainer-detail-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}

.trainer-detail-info h5 {
  font-size: 16px;
  color: #304156;
  margin-bottom: 4px;
}

.trainer-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.trainer-bio {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin: 0;
}

.training-overview {
  margin-bottom: 30px;
}

.training-overview h4 {
  font-size: 18px;
  color: #304156;
  margin-bottom: 16px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.overview-icon {
  color: #409eff;
  font-size: 20px;
}

.overview-label {
  font-size: 12px;
  color: #909399;
}

.overview-value {
  font-size: 14px;
  color: #304156;
  font-weight: 500;
}

.course-content {
  margin-bottom: 30px;
}

.course-content h4 {
  font-size: 18px;
  color: #304156;
  margin-bottom: 16px;
}

.module-content {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.module-content p {
  margin: 0 0 8px 0;
  color: #606266;
}

.module-duration {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.training-benefits {
  margin-bottom: 30px;
}

.training-benefits h4 {
  font-size: 18px;
  color: #304156;
  margin-bottom: 16px;
}

.benefits-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.benefits-list li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  color: #606266;
  line-height: 1.5;
}

.benefit-icon {
  color: #67c23a;
  margin-top: 2px;
}

/* 价格侧边栏 */
.training-price-sidebar {
  position: sticky;
  top: 20px;
  align-self: start;
}

.price-card {
  border-radius: 8px;
}

.price-display {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.price-card .current-price {
  font-size: 36px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 8px;
}

.price-card .original-price {
  font-size: 16px;
  color: #909399;
  text-decoration: line-through;
}

.discount-badge {
  display: inline-block;
  background-color: #f56c6c;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  margin-top: 8px;
}

.price-features {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  color: #606266;
}

.feature-item .el-icon {
  color: #67c23a;
}

.price-card .el-button {
  margin-top: 16px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .training-detail-content {
    grid-template-columns: 1fr;
  }
  
  .training-price-sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .training-list {
    grid-template-columns: 1fr;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .training-meta {
    flex-direction: column;
    gap: 12px;
  }
  
  .training-stats {
    align-items: flex-start;
  }
  
  .training-price-section {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
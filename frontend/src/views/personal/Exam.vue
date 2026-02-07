<template>
  <div class="exam-container">
    <!-- 考试头部信息 -->
    <div class="exam-header">
      <div class="exam-info">
        <h2>{{ examInfo.title }}</h2>
        <div class="info-tags">
          <el-tag type="info">{{ examInfo.category }}</el-tag>
          <el-tag type="warning">{{ examInfo.level }}</el-tag>
          <el-tag>总分：{{ examInfo.totalScore }}分</el-tag>
        </div>
      </div>
      <div class="exam-timer">
        <div class="timer-box">
          <el-icon><Clock /></el-icon>
          <span class="time-text">剩余时间</span>
          <span class="time-value" :class="{ 'time-warning': timeRemaining < 600 }">
            {{ formattedTime }}
          </span>
        </div>
      </div>
    </div>

    <!-- 进度条 -->
    <div class="progress-bar">
      <el-progress
        :percentage="progressPercentage"
        :color="progressColor"
        :stroke-width="8"
      />
      <span class="progress-text">已完成 {{ answeredCount }}/{{ totalQuestions }} 题</span>
    </div>

    <!-- 题目卡片 -->
    <div class="question-area" v-if="currentQuestion">
      <div class="question-card">
        <div class="question-header">
          <span class="question-number">第 {{ currentQuestionIndex + 1 }} 题</span>
          <el-tag :type="getQuestionTypeTag(currentQuestion.type)">
            {{ getQuestionTypeLabel(currentQuestion.type) }}
          </el-tag>
          <span class="question-score">{{ currentQuestion.score }} 分</span>
        </div>

        <div class="question-content">
          <p class="question-text">{{ currentQuestion.content }}</p>
        </div>

        <!-- 单选题 -->
        <div class="answer-area" v-if="currentQuestion.type === 'SINGLE_CHOICE'">
          <el-radio-group v-model="userAnswers[currentQuestion.id]" size="large">
            <el-radio
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              :label="option.key"
              class="answer-option"
            >
              <span class="option-key">{{ option.key }}</span>
              <span class="option-text">{{ option.value }}</span>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 多选题 -->
        <div class="answer-area" v-if="currentQuestion.type === 'MULTIPLE_CHOICE'">
          <el-checkbox-group v-model="userAnswers[currentQuestion.id]">
            <el-checkbox
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              :label="option.key"
              class="answer-option"
            >
              <span class="option-key">{{ option.key }}</span>
              <span class="option-text">{{ option.value }}</span>
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <!-- 判断题 -->
        <div class="answer-area" v-if="currentQuestion.type === 'TRUE_FALSE'">
          <el-radio-group v-model="userAnswers[currentQuestion.id]" size="large">
            <el-radio label="true" class="answer-option">
              <span class="option-key">✓</span>
              <span class="option-text">正确</span>
            </el-radio>
            <el-radio label="false" class="answer-option">
              <span class="option-key">✗</span>
              <span class="option-text">错误</span>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 简答题 -->
        <div class="answer-area" v-if="currentQuestion.type === 'SHORT_ANSWER'">
          <el-input
            v-model="userAnswers[currentQuestion.id]"
            type="textarea"
            :rows="8"
            placeholder="请输入您的答案..."
            maxlength="1000"
            show-word-limit
          />
        </div>
      </div>

      <!-- 导航按钮 -->
      <div class="navigation-buttons">
        <el-button
          @click="previousQuestion"
          :disabled="currentQuestionIndex === 0"
          size="large"
        >
          <el-icon><ArrowLeft /></el-icon>
          上一题
        </el-button>

        <el-button
          type="primary"
          @click="nextQuestion"
          v-if="currentQuestionIndex < totalQuestions - 1"
          size="large"
        >
          下一题
          <el-icon><ArrowRight /></el-icon>
        </el-button>

        <el-button
          type="success"
          @click="submitExam"
          v-else
          size="large"
        >
          <el-icon><Select /></el-icon>
          提交试卷
        </el-button>
      </div>
    </div>

    <!-- 题目导航面板 -->
    <div class="question-nav-panel">
      <h3>答题卡</h3>
      <div class="question-nav-grid">
        <div
          v-for="(question, index) in questions"
          :key="question.id"
          class="question-nav-item"
          :class="{
            'current': index === currentQuestionIndex,
            'answered': isAnswered(question.id)
          }"
          @click="goToQuestion(index)"
        >
          {{ index + 1 }}
        </div>
      </div>
      <div class="nav-legend">
        <span class="legend-item"><i class="dot current"></i>当前题</span>
        <span class="legend-item"><i class="dot answered"></i>已作答</span>
        <span class="legend-item"><i class="dot unanswered"></i>未作答</span>
      </div>
    </div>

    <!-- 提交确认对话框 -->
    <el-dialog v-model="showSubmitDialog" title="提交确认" width="500px" center>
      <div class="submit-dialog-content">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <p>您确定要提交试卷吗？</p>
        <p class="submit-stats">
          已答题目：<strong>{{ answeredCount }}</strong> / {{ totalQuestions }} 题
        </p>
        <p class="submit-warning" v-if="answeredCount < totalQuestions">
          还有 {{ totalQuestions - answeredCount }} 道题目未作答！
        </p>
      </div>
      <template #footer>
        <el-button @click="showSubmitDialog = false">继续答题</el-button>
        <el-button type="primary" @click="confirmSubmit" :loading="submitting">
          确认提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 考试结果对话框 -->
    <el-dialog v-model="showResultDialog" title="考试结果" width="600px" center :close-on-click-modal="false">
      <div class="result-dialog-content">
        <div class="result-score" :class="{ 'pass': examResult.passed, 'fail': !examResult.passed }">
          <div class="score-circle">
            <span class="score-value">{{ examResult.score }}</span>
            <span class="score-label">分</span>
          </div>
        </div>
        <div class="result-status">
          <el-tag :type="examResult.passed ? 'success' : 'danger'" size="large">
            {{ examResult.passed ? '恭喜通过！' : '未通过' }}
          </el-tag>
        </div>
        <div class="result-details">
          <p>总题数：{{ totalQuestions }} 题</p>
          <p>正确题数：{{ examResult.correctCount }} 题</p>
          <p>及格分数：{{ examInfo.passScore }} 分</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="backToCertification">返回认证中心</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, ArrowLeft, ArrowRight, Select, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getUserIdForPath } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

// 考试信息
const examInfo = reactive({
  title: '服装设计师（中级）理论考试',
  category: '服装设计',
  level: '中级',
  totalScore: 100,
  passScore: 60,
  duration: 7200 // 秒
})

// 题目数据
const questions = ref([])
const currentQuestionIndex = ref(0)
const userAnswers = reactive({})
const examRecordId = ref(null)

// 计时器
const timeRemaining = ref(7200)
let timer = null

// 对话框
const showSubmitDialog = ref(false)
const showResultDialog = ref(false)
const submitting = ref(false)

// 考试结果
const examResult = reactive({
  score: 0,
  correctCount: 0,
  passed: false
})

// 计算属性
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value])
const totalQuestions = computed(() => questions.value.length)
const answeredCount = computed(() => {
  return Object.keys(userAnswers).filter(key => {
    const answer = userAnswers[key]
    if (Array.isArray(answer)) return answer.length > 0
    return answer !== undefined && answer !== ''
  }).length
})


const progressPercentage = computed(() => {
  if (totalQuestions.value === 0) return 0
  return Math.round((answeredCount.value / totalQuestions.value) * 100)
})

const progressColor = computed(() => {
  if (progressPercentage.value < 30) return '#f56c6c'
  if (progressPercentage.value < 70) return '#e6a23c'
  return '#67c23a'
})

const formattedTime = computed(() => {
  const hours = Math.floor(timeRemaining.value / 3600)
  const minutes = Math.floor((timeRemaining.value % 3600) / 60)
  const seconds = timeRemaining.value % 60
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

// 方法
const getQuestionTypeLabel = (type) => {
  const labels = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'TRUE_FALSE': '判断题',
    'SHORT_ANSWER': '简答题'
  }
  return labels[type] || type
}

const getQuestionTypeTag = (type) => {
  const tags = {
    'SINGLE_CHOICE': '',
    'MULTIPLE_CHOICE': 'warning',
    'TRUE_FALSE': 'info',
    'SHORT_ANSWER': 'success'
  }
  return tags[type] || ''
}

const isAnswered = (questionId) => {
  const answer = userAnswers[questionId]
  if (Array.isArray(answer)) return answer.length > 0
  return answer !== undefined && answer !== ''
}

const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < totalQuestions.value - 1) {
    currentQuestionIndex.value++
  }
}

const goToQuestion = (index) => {
  currentQuestionIndex.value = index
}

const submitExam = () => {
  showSubmitDialog.value = true
}

const confirmSubmit = async () => {
  submitting.value = true
  try {
    const userId = Number(getUserIdForPath('/personal') || 0)
    const response = await request.post('/exam/submit', {
      examRecordId: examRecordId.value,
      userId,
      answers: JSON.stringify(userAnswers)
    })

    // 设置考试结果
    examResult.score = response.data?.score || 85
    examResult.correctCount = response.data?.correctCount || 17
    examResult.passed = examResult.score >= examInfo.passScore

    showSubmitDialog.value = false
    showResultDialog.value = true

    // 停止计时器
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '提交试卷失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const backToCertification = () => {
  router.push('/personal/certification')
}

const loadExamQuestions = async () => {
  try {
    const userId = Number(getUserIdForPath('/personal') || 0)
    const response = await request.post('/exam/generate', {
      userId,
      applicationId: Number(route.query.applicationId),
      standardId: Number(route.query.examId),
      questionCount: 20
    })
    examRecordId.value = response.data?.examRecordId || null
    questions.value = response.data?.questions || []
    Object.keys(userAnswers).forEach((key) => {
      delete userAnswers[key]
    })
    questions.value.forEach((q) => {
      if (q.type === 'MULTIPLE_CHOICE') {
        userAnswers[q.id] = []
      }
    })
    examInfo.totalScore = response.data?.totalScore || examInfo.totalScore
    examInfo.passScore = response.data?.passScore || Math.round(examInfo.totalScore * 0.6)
    examInfo.duration = (response.data?.duration || 60) * 60
    timeRemaining.value = examInfo.duration
    return questions.value.length > 0
  } catch (error) {
    console.error('加载试题失败:', error)
    ElMessage.error(error?.response?.data?.message || '试题加载失败，请稍后重试')
    questions.value = []
    timeRemaining.value = 0
    return false
  }
}

const startTimer = () => {
  timer = setInterval(() => {
    if (timeRemaining.value > 0) {
      timeRemaining.value--
    } else {
      clearInterval(timer)
      ElMessageBox.alert('考试时间已到，系统将自动提交试卷', '时间到', {
        confirmButtonText: '确定',
        callback: () => {
          confirmSubmit()
        }
      })
    }
  }, 1000)
}

// 防作弊：检测页面切换
const handleVisibilityChange = () => {
  if (document.hidden) {
    ElMessage.warning('请勿切换页面，否则可能影响考试成绩')
  }
}

onMounted(async () => {
  const loaded = await loadExamQuestions()
  if (loaded) {
    startTimer()
  }
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>


<style scoped>
.exam-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: 100vh;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px 32px;
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.exam-info h2 {
  color: #fff;
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: 600;
}

.info-tags {
  display: flex;
  gap: 10px;
}

.info-tags .el-tag {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
}

.exam-timer {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 16px 24px;
  backdrop-filter: blur(10px);
}

.timer-box {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}

.timer-box .el-icon {
  font-size: 24px;
}

.time-text {
  font-size: 14px;
  opacity: 0.9;
}

.time-value {
  font-size: 28px;
  font-weight: 700;
  font-family: 'Monaco', monospace;
}

.time-warning {
  color: #ffd700;
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.progress-bar {
  background: #fff;
  padding: 20px 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 20px;
}

.progress-bar .el-progress {
  flex: 1;
}

.progress-text {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.question-area {
  flex: 1;
  margin-right: 280px;
}

.question-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.question-number {
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
}

.question-score {
  margin-left: auto;
  color: #909399;
  font-size: 14px;
  background: #f4f4f5;
  padding: 4px 12px;
  border-radius: 20px;
}

.question-content {
  margin-bottom: 28px;
}

.question-text {
  font-size: 17px;
  line-height: 1.8;
  color: #303133;
}

.answer-area {
  padding: 20px;
  background: #fafbfc;
  border-radius: 12px;
}

.answer-option {
  display: flex !important;
  align-items: center;
  padding: 16px 20px;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 10px;
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
  cursor: pointer;
}

.answer-option:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.answer-option:last-child {
  margin-bottom: 0;
}

.option-key {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-right: 16px;
  flex-shrink: 0;
}

.option-text {
  font-size: 15px;
  color: #303133;
}

.el-radio-group,
.el-checkbox-group {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.navigation-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.navigation-buttons .el-button {
  min-width: 140px;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
}

/* 答题卡面板 */
.question-nav-panel {
  position: fixed;
  right: 20px;
  top: 100px;
  width: 240px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.question-nav-panel h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #303133;
  text-align: center;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.question-nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  margin-bottom: 20px;
}

.question-nav-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f5f7fa;
  color: #909399;
  border: 2px solid transparent;
}

.question-nav-item:hover {
  background: #e8eaed;
}

.question-nav-item.current {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.question-nav-item.answered {
  background: #67c23a;
  color: #fff;
}

.question-nav-item.answered.current {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.nav-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.legend-item .dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.dot.current {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.dot.answered {
  background: #67c23a;
}

.dot.unanswered {
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
}

/* 提交确认对话框 */
.submit-dialog-content {
  text-align: center;
  padding: 20px 0;
}

.submit-dialog-content .warning-icon {
  font-size: 64px;
  color: #e6a23c;
  margin-bottom: 16px;
}

.submit-dialog-content p {
  font-size: 16px;
  color: #303133;
  margin: 8px 0;
}

.submit-stats strong {
  color: #667eea;
  font-size: 18px;
}

.submit-warning {
  color: #f56c6c;
  font-size: 14px;
}

/* 考试结果对话框 */
.result-dialog-content {
  text-align: center;
  padding: 20px 0;
}

.result-score {
  margin-bottom: 24px;
}

.score-circle {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.result-score.pass .score-circle {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
}

.result-score.fail .score-circle {
  background: linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%);
}

.score-value {
  font-size: 48px;
  font-weight: 700;
  color: #fff;
}

.score-label {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
}

.result-status {
  margin-bottom: 24px;
}

.result-details {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 12px;
}

.result-details p {
  margin: 8px 0;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .question-area {
    margin-right: 0;
  }

  .question-nav-panel {
    position: static;
    width: 100%;
    margin-bottom: 20px;
  }

  .question-nav-grid {
    grid-template-columns: repeat(10, 1fr);
  }
}

@media (max-width: 768px) {
  .exam-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .question-nav-grid {
    grid-template-columns: repeat(6, 1fr);
  }

  .navigation-buttons {
    flex-direction: column;
  }

  .navigation-buttons .el-button {
    width: 100%;
  }
}
</style>

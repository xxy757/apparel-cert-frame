<template>
  <div class="messages-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>消息中心</h1>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="unread-badge">
          <span class="subtitle">您有 {{ unreadCount }} 条未读消息</span>
        </el-badge>
      </div>
      <div class="header-actions">
        <el-button @click="markAllAsRead" :disabled="unreadCount === 0">
          <el-icon><Check /></el-icon>
          全部已读
        </el-button>
        <el-button type="danger" plain @click="clearAllMessages" :disabled="messages.length === 0">
          <el-icon><Delete /></el-icon>
          清空消息
        </el-button>
      </div>
    </div>

    <!-- 消息分类标签 -->
    <div class="message-tabs">
      <el-radio-group v-model="activeTab" size="large">
        <el-radio-button label="all">
          全部消息
          <el-badge :value="messages.length" :hidden="messages.length === 0" />
        </el-radio-button>
        <el-radio-button label="certification">
          <el-icon><Medal /></el-icon>
          认证通知
        </el-radio-button>
        <el-radio-button label="interview">
          <el-icon><ChatDotRound /></el-icon>
          面试邀请
        </el-radio-button>
        <el-radio-button label="job">
          <el-icon><Briefcase /></el-icon>
          职位推荐
        </el-radio-button>
        <el-radio-button label="system">
          <el-icon><Setting /></el-icon>
          系统通知
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 消息列表 -->
    <div class="messages-list">
      <el-empty v-if="filteredMessages.length === 0" description="暂无消息" />

      <transition-group name="message" tag="div">
        <div
          v-for="message in filteredMessages"
          :key="message.id"
          class="message-card"
          :class="{ 'unread': !message.isRead }"
          @click="viewMessage(message)"
        >
          <div class="message-icon" :style="{ background: getTypeColor(message.type) }">
            <el-icon><component :is="getTypeIcon(message.type)" /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-header">
              <h3 class="message-title">{{ message.title }}</h3>
              <span class="message-time">{{ formatTime(message.createTime) }}</span>
            </div>
            <p class="message-summary">{{ message.content }}</p>
            <div class="message-footer" v-if="message.actionUrl">
              <el-button type="primary" link size="small" @click.stop="handleAction(message)">
                {{ message.actionText || '查看详情' }}
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="message-actions">
            <el-button
              v-if="!message.isRead"
              type="primary"
              link
              @click.stop="markAsRead(message)"
            >
              标记已读
            </el-button>
            <el-button type="danger" link @click.stop="deleteMessage(message)">
              删除
            </el-button>
          </div>
        </div>
      </transition-group>
    </div>

    <!-- 加载更多 -->
    <div class="load-more" v-if="hasMore">
      <el-button @click="loadMore" :loading="loading">加载更多</el-button>
    </div>

    <!-- 消息详情对话框 -->
    <el-dialog v-model="showDetailDialog" :title="currentMessage?.title" width="600px">
      <div class="message-detail" v-if="currentMessage">
        <div class="detail-meta">
          <el-tag :color="getTypeColor(currentMessage.type)" effect="dark" size="small">
            {{ getTypeLabel(currentMessage.type) }}
          </el-tag>
          <span class="detail-time">{{ formatTime(currentMessage.createTime) }}</span>
        </div>
        <div class="detail-content">
          {{ currentMessage.content }}
        </div>
        <div class="detail-action" v-if="currentMessage.actionUrl">
          <el-button type="primary" @click="handleAction(currentMessage)">
            {{ currentMessage.actionText || '查看详情' }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check, Delete, Medal, ChatDotRound, Briefcase, Setting,
  ArrowRight, Bell, InfoFilled
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const activeTab = ref('all')
const messages = ref([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const showDetailDialog = ref(false)
const currentMessage = ref(null)

// 计算未读数量
const unreadCount = computed(() => messages.value.filter(m => !m.isRead).length)

// 筛选消息
const filteredMessages = computed(() => {
  if (activeTab.value === 'all') return messages.value
  return messages.value.filter(m => m.type === activeTab.value)
})

// 获取类型图标
const getTypeIcon = (type) => {
  const icons = { certification: Medal, interview: ChatDotRound, job: Briefcase, system: Setting }
  return icons[type] || Bell
}

// 获取类型颜色
const getTypeColor = (type) => {
  const colors = { certification: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', interview: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', job: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', system: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' }
  return colors[type] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
}

// 获取类型标签
const getTypeLabel = (type) => {
  const labels = { certification: '认证通知', interview: '面试邀请', job: '职位推荐', system: '系统通知' }
  return labels[type] || '通知'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return date.toLocaleDateString('zh-CN')
}

// 查看消息
const viewMessage = (message) => {
  currentMessage.value = message
  showDetailDialog.value = true
  if (!message.isRead) markAsRead(message)
}

// 标记已读
const markAsRead = async (message) => {
  try {
    await request.put(`/notification/read/${message.id}`)
  } catch (e) {}
  message.isRead = true
}

// 全部已读
const markAllAsRead = async () => {
  try {
    await request.put('/notification/read-all')
  } catch (e) {}
  messages.value.forEach(m => m.isRead = true)
  ElMessage.success('已全部标记为已读')
}

// 删除消息
const deleteMessage = (message) => {
  ElMessageBox.confirm('确定删除这条消息吗？', '删除确认', { type: 'warning' }).then(async () => {
    try {
      await request.delete(`/notification/${message.id}`)
    } catch (e) {}
    const index = messages.value.findIndex(m => m.id === message.id)
    if (index > -1) messages.value.splice(index, 1)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 清空消息
const clearAllMessages = () => {
  ElMessageBox.confirm('确定清空所有消息吗？此操作不可撤销', '清空确认', { type: 'warning' }).then(async () => {
    messages.value = []
    ElMessage.success('已清空所有消息')
  }).catch(() => {})
}

// 处理操作
const handleAction = (message) => {
  if (message.actionUrl) {
    router.push(message.actionUrl)
    showDetailDialog.value = false
  }
}

// 加载更多
const loadMore = async () => {
  loading.value = true
  currentPage.value++
  setTimeout(() => { loading.value = false; hasMore.value = false }, 1000)
}

// 加载消息
const loadMessages = async () => {
  try {
    const response = await request.get('/notification/list')
    messages.value = response.data || []
  } catch (error) {
    messages.value = [
      { id: 1, type: 'certification', title: '认证申请已通过', content: '恭喜！您的"服装设计师（中级）"认证申请已通过审核，证书将在3个工作日内发放。', createTime: new Date().toISOString(), isRead: false, actionUrl: '/personal/certification', actionText: '查看证书' },
      { id: 2, type: 'interview', title: '收到面试邀请', content: '上海时尚服饰有限公司邀请您参加"高级服装设计师"职位的面试，请在24小时内确认。', createTime: new Date(Date.now() - 3600000).toISOString(), isRead: false, actionUrl: '/personal/applications', actionText: '查看详情' },
      { id: 3, type: 'job', title: '新职位推荐', content: '根据您的简历和技能认证，为您推荐5个高匹配度职位，快来看看吧！', createTime: new Date(Date.now() - 86400000).toISOString(), isRead: true, actionUrl: '/personal/job', actionText: '查看职位' },
      { id: 4, type: 'system', title: '系统维护通知', content: '平台将于本周六凌晨2:00-6:00进行系统升级维护，届时部分功能可能无法使用。', createTime: new Date(Date.now() - 172800000).toISOString(), isRead: true }
    ]
  }
}

onMounted(() => { loadMessages() })
</script>

<style scoped>
.messages-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.message-tabs {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.message-card:hover {
  transform: translateX(8px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.message-card.unread {
  border-left-color: #667eea;
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.05) 0%, #fff 100%);
}

.message-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.message-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.message-summary {
  font-size: 14px;
  color: #606266;
  margin: 0 0 8px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 24px;
}

.message-detail {
  padding: 16px 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-time {
  font-size: 14px;
  color: #909399;
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 24px;
}

.detail-action {
  text-align: center;
}

/* 动画 */
.message-enter-active, .message-leave-active {
  transition: all 0.3s ease;
}

.message-enter-from, .message-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media (max-width: 768px) {
  .page-header { flex-direction: column; gap: 16px; align-items: flex-start; }
  .message-card { flex-direction: column; }
}
</style>

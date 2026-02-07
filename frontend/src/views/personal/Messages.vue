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
import { getSession } from '@/utils/auth'

const router = useRouter()
const activeTab = ref('all')
const messages = ref([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
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

const getAuthInfo = () => {
  const session = getSession('1')
  const userId = Number(session.userId || 0)
  const userType = 1
  return { userId, userType }
}

// 标记已读
const markAsRead = async (message) => {
  try {
    const { userId } = getAuthInfo()
    if (!userId) return
    await request({
      url: `/notification/read/${message.id}`,
      method: 'put',
      params: { userId }
    })
  } catch (e) {}
  message.isRead = true
}

// 全部已读
const markAllAsRead = async () => {
  try {
    const { userId, userType } = getAuthInfo()
    if (!userId || !userType) return
    await request({
      url: '/notification/read-all',
      method: 'put',
      params: { userId, userType }
    })
  } catch (e) {}
  messages.value.forEach(m => m.isRead = true)
  ElMessage.success('已全部标记为已读')
}

// 删除消息
const deleteMessage = (message) => {
  ElMessageBox.confirm('确定删除这条消息吗？', '删除确认', { type: 'warning' }).then(async () => {
    try {
      const { userId } = getAuthInfo()
      if (!userId) return
      await request.delete(`/notification/${message.id}`, { params: { userId } })
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
const resolveFallbackPath = (type) => {
  const pathMap = {
    certification: '/personal/certification',
    interview: '/personal/applications',
    job: '/personal/job',
    system: '/personal/messages'
  }
  return pathMap[type] || '/personal/messages'
}

const handleAction = (message) => {
  const actionUrl = (message?.actionUrl || '').trim()
  if (!actionUrl) {
    ElMessage.info('该消息暂未配置跳转链接')
    return
  }

  if (/^https?:\/\//i.test(actionUrl)) {
    window.open(actionUrl, '_blank')
    showDetailDialog.value = false
    return
  }

  const target = router.resolve(actionUrl)
  if (!target.matched || target.matched.length === 0) {
    const fallback = resolveFallbackPath(message?.type)
    ElMessage.warning('跳转地址不存在，已为您跳转到相关页面')
    router.push(fallback)
    showDetailDialog.value = false
    return
  }

  router.push(actionUrl)
  showDetailDialog.value = false
}

// 加载更多
const loadMore = async () => {
  if (loading.value || !hasMore.value) return
  currentPage.value += 1
  await loadMessages(true)
}

// 加载消息
const loadMessages = async (append = false) => {
  const { userId, userType } = getAuthInfo()
  if (!userId || !userType) {
    messages.value = []
    hasMore.value = false
    return
  }

  loading.value = true
  try {
    const response = await request.get('/notification/list', {
      params: {
        userId,
        userType,
        current: currentPage.value,
        size: pageSize.value
      }
    })

    const pageData = response.data || {}
    const rawRecords = Array.isArray(pageData)
      ? pageData
      : (pageData.records || pageData.list || [])

    const normalized = rawRecords.map(item => ({
      ...item,
      isRead: item.isRead === 1 || item.isRead === true,
      type: item.type || 'system'
    }))

    messages.value = append ? [...messages.value, ...normalized] : normalized

    if (pageData.pages !== undefined) {
      hasMore.value = currentPage.value < pageData.pages
    } else if (pageData.total !== undefined) {
      hasMore.value = messages.value.length < pageData.total
    } else {
      hasMore.value = normalized.length === pageSize.value
    }
  } catch (error) {
    if (!append) messages.value = []
    hasMore.value = false
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
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

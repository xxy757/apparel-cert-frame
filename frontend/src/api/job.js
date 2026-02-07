import request from '@/utils/request'

/**
 * 搜索职位（分页、筛选）
 * @param {object} params - 包含分页信息、搜索关键词和筛选条件的对象
 * @returns {Promise}
 */
export function searchJobs(params) {
  return request({
    url: '/personal/job',
    method: 'get',
    params
  })
}

/**
 * 获取智能推荐职位
 * @returns {Promise}
 */
export function getRecommendedJobs(userId) {
  return request({
    url: '/personal/job/recommended',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取单个职位的详细信息
 * @param {string} id - 职位ID
 * @returns {Promise}
 */
export function getJobDetail(jobId) {
  return request({
    url: '/personal/job/detail',
    method: 'get',
    params: { jobId }
  })
}

/**
 * 获取当前用户投递记录
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserApplications(userId) {
  return request({
    url: '/personal/job/applications',
    method: 'get',
    params: { userId }
  })
}

/**
 * 申请单个职位
 * @param {object} data - { jobId, resumeId }
 * @returns {Promise}
 */
export function applyToJob(params) {
  return request({
    url: '/personal/job/apply',
    method: 'post',
    params
  })
}

/**
 * 批量申请职位
 * @param {object} data - { jobIds, resumeId }
 * @returns {Promise}
 */
export function batchApplyToJobs(data) {
  return request({
    url: '/personal/job/batch-apply',
    method: 'post',
    data
  })
}

/**
 * 创建定时投递任务
 * @param {object} data - 定时投递任务信息
 * @returns {Promise}
 */
export function createScheduledDelivery(data) {
  return request({
    url: '/scheduled-delivery/batch-create',
    method: 'post',
    data
  })
}

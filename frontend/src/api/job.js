import request from '@/utils/request'

/**
 * 搜索职位（分页、筛选）
 * @param {object} params - 包含分页信息、搜索关键词和筛选条件的对象
 * @returns {Promise}
 */
export function searchJobs(params) {
  return request({
    url: '/api/job/search',
    method: 'get',
    params
  })
}

/**
 * 获取智能推荐职位
 * @returns {Promise}
 */
export function getRecommendedJobs() {
  return request({
    url: '/api/job/recommend',
    method: 'get'
  })
}

/**
 * 获取单个职位的详细信息
 * @param {string} id - 职位ID
 * @returns {Promise}
 */
export function getJobDetail(id) {
    return request({
        url: `/api/job/${id}`,
        method: 'get'
    })
}

/**
 * 申请单个职位
 * @param {object} data - { jobId, resumeId }
 * @returns {Promise}
 */
export function applyToJob(data) {
  return request({
    url: '/api/resume-delivery/apply',
    method: 'post',
    data
  })
}

/**
 * 批量申请职位
 * @param {object} data - { jobIds, resumeId }
 * @returns {Promise}
 */
export function batchApplyToJobs(data) {
  return request({
    url: '/api/resume-delivery/batch-apply',
    method: 'post',
    data
  })
}

/**
 * 收藏或取消收藏职位
 * @param {string} jobId - 职位ID
 * @returns {Promise}
 */
export function toggleSaveJob(jobId) {
  return request({
    url: `/api/job/save/${jobId}`,
    method: 'post'
  })
}

/**
 * 创建定时投递任务
 * @param {object} data - 定时投递任务信息
 * @returns {Promise}
 */
export function createScheduledDelivery(data) {
  return request({
    url: '/api/scheduled-delivery/batch-create',
    method: 'post',
    data
  })
}

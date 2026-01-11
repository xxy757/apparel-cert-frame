import request from '@/utils/request'

/**
 * 企业认证相关API
 */

/**
 * 获取企业信息
 * @param {number} enterpriseId - 企业ID
 * @returns {Promise}
 */
export function getEnterpriseInfo(enterpriseId) {
  return request({
    url: '/api/enterprise',
    method: 'get',
    params: { enterpriseId }
  })
}

/**
 * 更新企业信息
 * @param {object} enterprise - 企业信息对象
 * @returns {Promise}
 */
export function updateEnterpriseInfo(enterprise) {
  return request({
    url: '/api/enterprise',
    method: 'post',
    data: enterprise
  })
}

/**
 * 提交企业认证
 * @param {number} enterpriseId - 企业ID
 * @param {string} businessLicense - 营业执照
 * @param {string} otherCertificates - 其他资质证书
 * @returns {Promise}
 */
export function submitEnterpriseAuth(enterpriseId, businessLicense, otherCertificates) {
  return request({
    url: '/api/enterprise/auth',
    method: 'post',
    params: { enterpriseId, businessLicense, otherCertificates }
  })
}

/**
 * 获取企业认证状态
 * @param {number} enterpriseId - 企业ID
 * @returns {Promise}
 */
export function getEnterpriseAuthStatus(enterpriseId) {
  return request({
    url: '/api/enterprise/auth/status',
    method: 'get',
    params: { enterpriseId }
  })
}

/**
 * 职位管理相关API
 */

/**
 * 发布职位
 * @param {object} job - 职位信息
 * @returns {Promise}
 */
export function publishJob(job) {
  return request({
    url: '/api/enterprise/job',
    method: 'post',
    data: job
  })
}

/**
 * 获取企业发布的职位列表
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @param {number} enterpriseId - 企业ID
 * @param {number} status - 职位状态（可选）
 * @returns {Promise}
 */
export function getEnterpriseJobs(page, size, enterpriseId, status) {
  return request({
    url: '/api/enterprise/job',
    method: 'get',
    params: { page, size, enterpriseId, status }
  })
}

/**
 * 获取职位详情
 * @param {number} jobId - 职位ID
 * @returns {Promise}
 */
export function getJobDetail(jobId) {
  return request({
    url: '/api/enterprise/job/detail',
    method: 'get',
    params: { jobId }
  })
}

/**
 * 更新职位信息
 * @param {object} job - 职位信息
 * @returns {Promise}
 */
export function updateJob(job) {
  return request({
    url: '/api/enterprise/job',
    method: 'put',
    data: job
  })
}

/**
 * 下架职位
 * @param {number} jobId - 职位ID
 * @returns {Promise}
 */
export function offlineJob(jobId) {
  return request({
    url: '/api/enterprise/job/offline',
    method: 'put',
    params: { jobId }
  })
}

/**
 * 批量更新职位状态
 * @param {Array<number>} jobIds - 职位ID数组
 * @param {number} status - 状态
 * @returns {Promise}
 */
export function batchUpdateJobStatus(jobIds, status) {
  return request({
    url: '/api/enterprise/job/batch-status',
    method: 'put',
    data: { jobIds, status }
  })
}

/**
 * 批量删除职位
 * @param {Array<number>} jobIds - 职位ID数组
 * @returns {Promise}
 */
export function batchDeleteJobs(jobIds) {
  return request({
    url: '/api/enterprise/job/batch',
    method: 'delete',
    data: jobIds
  })
}

/**
 * 获取职位统计数据
 * @param {number} jobId - 职位ID
 * @returns {Promise}
 */
export function getJobStatistics(jobId) {
  return request({
    url: '/api/enterprise/job/statistics',
    method: 'get',
    params: { jobId }
  })
}

/**
 * 获取企业职位统计
 * @param {number} enterpriseId - 企业ID
 * @returns {Promise}
 */
export function getEnterpriseJobStatistics(enterpriseId) {
  return request({
    url: '/api/enterprise/job/enterprise-statistics',
    method: 'get',
    params: { enterpriseId }
  })
}

/**
 * 置顶职位
 * @param {number} jobId - 职位ID
 * @param {number} isTop - 是否置顶（1：置顶，0：取消）
 * @returns {Promise}
 */
export function topJob(jobId, isTop = 1) {
  return request({
    url: '/api/enterprise/job/top',
    method: 'put',
    params: { jobId, isTop }
  })
}

/**
 * 人才管理相关API
 */

/**
 * 获取岗位投递记录
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @param {number} jobId - 职位ID
 * @param {number} status - 投递状态（可选）
 * @returns {Promise}
 */
export function getJobApplications(page, size, jobId, status) {
  return request({
    url: '/api/enterprise/talent/applications',
    method: 'get',
    params: { page, size, jobId, status }
  })
}

/**
 * 更新投递状态
 * @param {number} applicationId - 投递记录ID
 * @param {number} status - 状态
 * @returns {Promise}
 */
export function updateApplicationStatus(applicationId, status) {
  return request({
    url: '/api/enterprise/talent/application/status',
    method: 'put',
    params: { applicationId, status }
  })
}

/**
 * 获取简历详情
 * @param {number} resumeId - 简历ID
 * @returns {Promise}
 */
export function getResumeDetail(resumeId) {
  return request({
    url: '/api/enterprise/talent/resume',
    method: 'get',
    params: { resumeId }
  })
}

/**
 * 发送面试邀请
 * @param {object} interview - 面试信息
 * @returns {Promise}
 */
export function sendInterviewInvitation(interview) {
  return request({
    url: '/api/enterprise/talent/interview',
    method: 'post',
    data: interview
  })
}

/**
 * 获取企业面试列表
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @param {number} enterpriseId - 企业ID
 * @param {number} status - 面试状态（可选）
 * @returns {Promise}
 */
export function getEnterpriseInterviews(page, size, enterpriseId, status) {
  return request({
    url: '/api/enterprise/talent/interviews',
    method: 'get',
    params: { page, size, enterpriseId, status }
  })
}

/**
 * 更新面试状态
 * @param {number} interviewId - 面试ID
 * @param {number} status - 状态
 * @returns {Promise}
 */
export function updateInterviewStatus(interviewId, status) {
  return request({
    url: '/api/enterprise/talent/interview/status',
    method: 'put',
    params: { interviewId, status }
  })
}

/**
 * 更新面试结果
 * @param {number} interviewId - 面试ID
 * @param {number} result - 结果
 * @param {string} feedback - 反馈（可选）
 * @returns {Promise}
 */
export function updateInterviewResult(interviewId, result, feedback) {
  return request({
    url: '/api/enterprise/talent/interview/result',
    method: 'put',
    params: { interviewId, result, feedback }
  })
}

/**
 * 搜索人才
 * @param {string} keyword - 关键词（可选）
 * @param {string} careerDirection - 职业方向（可选）
 * @param {string} education - 学历（可选）
 * @param {string} certification - 认证（可选）
 * @returns {Promise}
 */
export function searchTalents(keyword, careerDirection, education, certification) {
  return request({
    url: '/api/enterprise/talent/search',
    method: 'get',
    params: { keyword, careerDirection, education, certification }
  })
}

/**
 * 面试管理相关API
 */

/**
 * 创建面试邀请
 * @param {object} interview - 面试信息
 * @returns {Promise}
 */
export function createInterview(interview) {
  return request({
    url: '/api/interview',
    method: 'post',
    data: interview
  })
}

/**
 * 获取企业面试列表（通用）
 * @param {number} enterpriseId - 企业ID
 * @param {number} page - 页码（默认1）
 * @param {number} size - 每页大小（默认10）
 * @param {number} status - 面试状态（可选）
 * @returns {Promise}
 */
export function getEnterpriseInterviewList(enterpriseId, page = 1, size = 10, status) {
  return request({
    url: '/api/interview/enterprise',
    method: 'get',
    params: { enterpriseId, page, size, status }
  })
}

/**
 * 获取面试详情
 * @param {number} interviewId - 面试ID
 * @returns {Promise}
 */
export function getInterviewDetail(interviewId) {
  return request({
    url: '/api/interview/detail',
    method: 'get',
    params: { interviewId }
  })
}

/**
 * 更新面试状态（通用）
 * @param {number} interviewId - 面试ID
 * @param {number} status - 状态
 * @returns {Promise}
 */
export function updateInterviewStatusGeneral(interviewId, status) {
  return request({
    url: '/api/interview/status',
    method: 'put',
    params: { interviewId, status }
  })
}

/**
 * 更新面试结果（通用）
 * @param {number} interviewId - 面试ID
 * @param {number} result - 结果
 * @param {string} feedback - 反馈（可选）
 * @returns {Promise}
 */
export function updateInterviewResultGeneral(interviewId, result, feedback) {
  return request({
    url: '/api/interview/result',
    method: 'put',
    data: { interviewId, result, feedback }
  })
}

/**
 * 记录面试反馈
 * @param {number} interviewId - 面试ID
 * @param {string} feedback - 反馈
 * @param {number} evaluation - 评分（可选）
 * @param {string} strengths - 优点（可选）
 * @param {string} weaknesses - 不足（可选）
 * @returns {Promise}
 */
export function recordInterviewFeedback(interviewId, feedback, evaluation, strengths, weaknesses) {
  return request({
    url: '/api/interview/feedback',
    method: 'post',
    data: { interviewId, feedback, evaluation, strengths, weaknesses }
  })
}

/**
 * 更新录用状态
 * @param {number} interviewId - 面试ID
 * @param {number} hireStatus - 录用状态
 * @param {string} salary - 薪资（可选）
 * @param {string} entryDate - 入职日期（可选）
 * @returns {Promise}
 */
export function updateHireStatus(interviewId, hireStatus, salary, entryDate) {
  return request({
    url: '/api/interview/hire-status',
    method: 'put',
    data: { interviewId, hireStatus, salary, entryDate }
  })
}

/**
 * 获取面试统计
 * @param {number} enterpriseId - 企业ID
 * @returns {Promise}
 */
export function getInterviewStatistics(enterpriseId) {
  return request({
    url: '/api/interview/statistics',
    method: 'get',
    params: { enterpriseId }
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getCurrentUser() {
  return request({
    url: '/auth/current-user',
    method: 'get'
  })
}
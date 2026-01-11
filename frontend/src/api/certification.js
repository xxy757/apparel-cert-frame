import request from '@/utils/request'

/**
 * 获取当前用户的认证申请列表
 * @returns {Promise}
 */
export function getMyApplications() {
  // 假设后端提供了 /api/application/my 接口获取当前用户的申请列表
  return request({
    url: '/api/application/my',
    method: 'get'
  })
}

/**
 * 获取当前用户的证书列表
 * @returns {Promise}
 */
export function getMyCertificates() {
  // 假设后端提供了 /api/certificate/my 接口获取当前用户的证书
  return request({
    url: '/api/certificate/my',
    method: 'get'
  })
}

/**
 * 获取所有认证标准
 * @returns {Promise}
 */
export function getCertificationStandards() {
  return request({
    url: '/api/certification-standard',
    method: 'get'
  })
}

/**
 * 提交新的认证申请
 * @param {object} data - 申请表单数据
 * @returns {Promise}
 */
export function submitNewApplication(data) {
  return request({
    url: '/api/application',
    method: 'post',
    data
  })
}

/**
 * 获取即将过期的证书列表
 * @param {number} daysBeforeExpire - 过期前天数
 * @returns {Promise}
 */
export function getExpiringCertificates(daysBeforeExpire) {
  return request({
    url: '/api/certificate/expiring',
    method: 'get',
    params: { daysBeforeExpire }
  })
}

/**
 * 为证书续期
 * @param {object} data - { certificateId, years }
 * @returns {Promise}
 */
export function renewCertificateRequest(data) {
  return request({
    url: '/api/certificate/renew',
    method: 'post',
    params: data
  })
}

/**
 * 获取证书分享链接
 * @param {string} certificateId - 证书ID
 * @returns {Promise}
 */
export function getCertificateShareLink(certificateId) {
    return request({
        url: '/api/certificate/share-link',
        method: 'get',
        params: { certificateId }
    })
}

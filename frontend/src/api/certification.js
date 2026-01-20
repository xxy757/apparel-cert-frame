import request from '@/utils/request'

/**
 * 获取当前用户的认证申请列表
 * @returns {Promise}
 */
export function getMyApplications(params) {
  return request({
    url: '/personal/certification/list',
    method: 'get',
    params
  })
}

/**
 * 获取当前用户的证书列表
 * @returns {Promise}
 */
export function getMyCertificates(params) {
  return request({
    url: '/admin/certification/certificate/by-user',
    method: 'get',
    params
  })
}

/**
 * 获取所有认证标准
 * @returns {Promise}
 */
export function getCertificationStandards(params) {
  return request({
    url: '/admin/certification/standard',
    method: 'get',
    params
  })
}

/**
 * 提交新的认证申请
 * @param {object} data - 申请表单数据
 * @returns {Promise}
 */
export function submitNewApplication(data) {
  return request({
    url: '/personal/certification',
    method: 'post',
    data
  })
}

/**
 * 获取即将过期的证书列表
 * @param {number} daysBeforeExpire - 过期前天数
 * @returns {Promise}
 */
export function getExpiringCertificates(params) {
  return request({
    url: '/admin/certification/certificate/expiring',
    method: 'get',
    params
  })
}

/**
 * 为证书续期
 * @param {object} data - { certificateId, years }
 * @returns {Promise}
 */
export function renewCertificateRequest(data) {
  return request({
    url: '/admin/certification/certificate/renew',
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
        url: '/admin/certification/certificate/share-link',
        method: 'get',
        params: { certificateId }
    })
}

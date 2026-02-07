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

/**
 * 上传实操作品文件
 * @param {File} file - 文件对象
 * @param {number} certificationId - 认证申请ID
 * @param {function} onProgress - 上传进度回调
 * @returns {Promise}
 */
export function uploadPracticalWork(file, certificationId, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('certificationId', certificationId)

    return request({
        url: '/upload/practical',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
            if (onProgress && progressEvent.total) {
                const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
                onProgress(percentCompleted)
            }
        }
    })
}

/**
 * 获取认证申请详情
 * @param {number} id - 认证申请ID
 * @returns {Promise}
 */
export function getApplicationDetail(id) {
    return request({
        url: '/personal/certification/detail',
        method: 'get',
        params: { id }
    })
}

/**
 * 删除已上传的文件
 * @param {string} fileUrl - 文件URL
 * @returns {Promise}
 */
export function deleteFile(fileUrl) {
    return request({
        url: '/upload',
        method: 'delete',
        params: { fileUrl }
    })
}

/**
 * 导出证书
 * @param {number} certificateId - 证书ID
 * @returns {Promise}
 */
export function exportCertificateFile(certificateId) {
    return request({
        url: '/admin/certification/certificate/export',
        method: 'get',
        params: { certificateId }
    })
}

/**
 * 验证证书
 * @param {string} certificateNumber - 证书编号
 * @returns {Promise}
 */
export function verifyCertificateNumber(certificateNumber) {
    return request({
        url: '/admin/certification/certificate/verify',
        method: 'get',
        params: { certificateNumber }
    })
}

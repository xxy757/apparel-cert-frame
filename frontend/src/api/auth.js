import request from '@/utils/request'

/**
 * 发送注册验证码
 * @param {string} email - 邮箱地址
 * @returns {Promise}
 */
export function sendRegisterCode(email) {
  return request({
    url: '/auth/send-register-code',
    method: 'post',
    data: { email }
  })
}

/**
 * 验证注册验证码
 * @param {string} email - 邮箱地址
 * @param {string} code - 验证码
 * @returns {Promise}
 */
export function verifyRegisterCode(email, code) {
  return request({
    url: '/auth/verify-register-code',
    method: 'post',
    data: { email, code }
  })
}

/**
 * 忘记密码 - 发送重置验证码
 * @param {string} email - 邮箱地址
 * @param {number} userType - 用户类型
 * @returns {Promise}
 */
export function sendResetCode(email, userType) {
  return request({
    url: '/auth/forget-password',
    method: 'post',
    data: { email, userType }
  })
}

/**
 * 验证重置密码验证码
 * @param {string} email - 邮箱地址
 * @param {string} code - 验证码
 * @returns {Promise}
 */
export function verifyResetCode(email, code) {
  return request({
    url: '/auth/verify-reset-code',
    method: 'post',
    data: { email, code }
  })
}

/**
 * 重置密码（邮件验证码方式）
 * @param {string} email - 邮箱地址
 * @param {string} code - 验证码
 * @param {string} newPassword - 新密码
 * @param {number} userType - 用户类型
 * @returns {Promise}
 */
export function resetPassword(email, code, newPassword, userType) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data: { email, code, newPassword, userType }
  })
}

/**
 * 开发模式重置密码（一步重置，无需验证码）
 * 仅在 app.resetPwd.devMode=true 且来源IP在允许范围内时可用
 * @param {string} account - 用户名/邮箱/手机号
 * @param {string} newPassword - 新密码
 * @returns {Promise}
 */
export function devResetPassword(account, newPassword) {
  return request({
    url: '/auth/dev-reset-password',
    method: 'post',
    data: { account, newPassword }
  })
}

/**
 * 检查账号锁定状态
 * @param {string} username - 用户名
 * @param {number} userType - 用户类型
 * @returns {Promise}
 */
export function checkLockStatus(username, userType) {
  return request({
    url: '/auth/check-lock',
    method: 'get',
    params: { username, userType }
  })
}

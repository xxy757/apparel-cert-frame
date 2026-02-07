const USER_TYPES = Object.freeze({
  PERSONAL: '1',
  ENTERPRISE: '2',
  ADMIN: '3'
})

const TYPE_TO_SCOPE = Object.freeze({
  [USER_TYPES.PERSONAL]: 'personal',
  [USER_TYPES.ENTERPRISE]: 'enterprise',
  [USER_TYPES.ADMIN]: 'admin'
})

const STORAGE_KEYS = Object.freeze({
  activeUserType: 'activeUserType',
  legacyToken: 'token',
  legacyUserType: 'userType',
  legacyUserId: 'userId',
  legacyUsername: 'username'
})

const SESSION_KEYS = Object.freeze({
  [USER_TYPES.PERSONAL]: {
    token: 'token_personal',
    userId: 'userId_personal',
    username: 'username_personal'
  },
  [USER_TYPES.ENTERPRISE]: {
    token: 'token_enterprise',
    userId: 'userId_enterprise',
    username: 'username_enterprise'
  },
  [USER_TYPES.ADMIN]: {
    token: 'token_admin',
    userId: 'userId_admin',
    username: 'username_admin'
  }
})

let legacyMigrated = false

const hasLocalStorage = () => typeof window !== 'undefined' && !!window.localStorage

const normalizeUserType = (value) => {
  const text = String(value || '')
  return TYPE_TO_SCOPE[text] ? text : ''
}

const getSessionKeys = (userType) => SESSION_KEYS[normalizeUserType(userType)] || null

const getPathname = (path) => {
  if (typeof path === 'string' && path) return path
  if (typeof window !== 'undefined' && window.location) return window.location.pathname || '/'
  return '/'
}

const clearLegacySession = () => {
  if (!hasLocalStorage()) return
  localStorage.removeItem(STORAGE_KEYS.legacyToken)
  localStorage.removeItem(STORAGE_KEYS.legacyUserType)
  localStorage.removeItem(STORAGE_KEYS.legacyUserId)
  localStorage.removeItem(STORAGE_KEYS.legacyUsername)
}

const setLegacyFromSession = (userType) => {
  const normalizedType = normalizeUserType(userType)
  if (!normalizedType || !hasLocalStorage()) return false

  const session = getSession(normalizedType)
  if (!session.token) return false

  localStorage.setItem(STORAGE_KEYS.activeUserType, normalizedType)
  localStorage.setItem(STORAGE_KEYS.legacyToken, session.token)
  localStorage.setItem(STORAGE_KEYS.legacyUserType, normalizedType)

  if (session.userId) {
    localStorage.setItem(STORAGE_KEYS.legacyUserId, session.userId)
  } else {
    localStorage.removeItem(STORAGE_KEYS.legacyUserId)
  }

  if (session.username) {
    localStorage.setItem(STORAGE_KEYS.legacyUsername, session.username)
  } else {
    localStorage.removeItem(STORAGE_KEYS.legacyUsername)
  }

  return true
}

const migrateLegacySession = () => {
  if (legacyMigrated || !hasLocalStorage()) return
  legacyMigrated = true

  const legacyType = normalizeUserType(localStorage.getItem(STORAGE_KEYS.legacyUserType))
  const legacyToken = localStorage.getItem(STORAGE_KEYS.legacyToken) || ''
  if (!legacyType || !legacyToken) return

  const keys = getSessionKeys(legacyType)
  if (!keys) return

  if (!localStorage.getItem(keys.token)) {
    localStorage.setItem(keys.token, legacyToken)
  }

  const legacyUserId = localStorage.getItem(STORAGE_KEYS.legacyUserId)
  if (legacyUserId && !localStorage.getItem(keys.userId)) {
    localStorage.setItem(keys.userId, legacyUserId)
  }

  const legacyUsername = localStorage.getItem(STORAGE_KEYS.legacyUsername)
  if (legacyUsername && !localStorage.getItem(keys.username)) {
    localStorage.setItem(keys.username, legacyUsername)
  }

  if (!normalizeUserType(localStorage.getItem(STORAGE_KEYS.activeUserType))) {
    localStorage.setItem(STORAGE_KEYS.activeUserType, legacyType)
  }
}

export const resolveUserType = (value) => normalizeUserType(value)

export const userTypeFromLoginType = (loginType) => {
  if (loginType === 'personal') return USER_TYPES.PERSONAL
  if (loginType === 'enterprise') return USER_TYPES.ENTERPRISE
  if (loginType === 'admin') return USER_TYPES.ADMIN
  return ''
}

export const getUserTypeByPath = (path) => {
  const currentPath = getPathname(path)
  if (currentPath.startsWith('/personal')) return USER_TYPES.PERSONAL
  if (currentPath.startsWith('/enterprise')) return USER_TYPES.ENTERPRISE
  if (currentPath.startsWith('/admin')) return USER_TYPES.ADMIN
  return ''
}

export const getSession = (userType) => {
  migrateLegacySession()

  const normalizedType = normalizeUserType(userType)
  const keys = getSessionKeys(normalizedType)
  if (!keys || !hasLocalStorage()) {
    return { userType: normalizedType, token: '', userId: '', username: '' }
  }

  return {
    userType: normalizedType,
    token: localStorage.getItem(keys.token) || '',
    userId: localStorage.getItem(keys.userId) || '',
    username: localStorage.getItem(keys.username) || ''
  }
}

export const hasSession = (userType) => {
  return Boolean(getSession(userType).token)
}

export const getAvailableUserTypes = () => {
  return Object.values(USER_TYPES).filter(type => hasSession(type))
}

export const setActiveSession = (userType) => {
  migrateLegacySession()
  return setLegacyFromSession(userType)
}

export const getActiveUserType = () => {
  migrateLegacySession()
  if (!hasLocalStorage()) return ''

  const activeType = normalizeUserType(localStorage.getItem(STORAGE_KEYS.activeUserType))
  if (activeType && hasSession(activeType)) {
    setLegacyFromSession(activeType)
    return activeType
  }

  const legacyType = normalizeUserType(localStorage.getItem(STORAGE_KEYS.legacyUserType))
  if (legacyType && hasSession(legacyType)) {
    setLegacyFromSession(legacyType)
    return legacyType
  }

  const fallbackType = getAvailableUserTypes()[0] || ''
  if (fallbackType) {
    setLegacyFromSession(fallbackType)
    return fallbackType
  }

  localStorage.removeItem(STORAGE_KEYS.activeUserType)
  clearLegacySession()
  return ''
}

export const saveSession = (userType, payload = {}) => {
  migrateLegacySession()
  if (!hasLocalStorage()) return false

  const normalizedType = normalizeUserType(userType)
  const keys = getSessionKeys(normalizedType)
  if (!normalizedType || !keys) return false

  const token = String(payload.token || '')
  if (!token) return false

  localStorage.setItem(keys.token, token)

  if (payload.userId !== undefined && payload.userId !== null && String(payload.userId) !== '') {
    localStorage.setItem(keys.userId, String(payload.userId))
  } else {
    localStorage.removeItem(keys.userId)
  }

  if (payload.username) {
    localStorage.setItem(keys.username, String(payload.username))
  } else {
    localStorage.removeItem(keys.username)
  }

  return setLegacyFromSession(normalizedType)
}

export const clearSession = (userType) => {
  migrateLegacySession()
  if (!hasLocalStorage()) return

  const normalizedType = normalizeUserType(userType)
  const keys = getSessionKeys(normalizedType)
  if (!normalizedType || !keys) return

  localStorage.removeItem(keys.token)
  localStorage.removeItem(keys.userId)
  localStorage.removeItem(keys.username)

  const currentActiveType = normalizeUserType(localStorage.getItem(STORAGE_KEYS.activeUserType))
  const currentLegacyType = normalizeUserType(localStorage.getItem(STORAGE_KEYS.legacyUserType))
  if (currentActiveType !== normalizedType && currentLegacyType !== normalizedType) return

  const fallbackType = getAvailableUserTypes()[0] || ''
  if (fallbackType) {
    setLegacyFromSession(fallbackType)
  } else {
    localStorage.removeItem(STORAGE_KEYS.activeUserType)
    clearLegacySession()
  }
}

export const clearSessionByPath = (path) => {
  const routeType = getUserTypeByPath(path)
  if (routeType) {
    clearSession(routeType)
    return
  }

  const activeType = getActiveUserType()
  if (activeType) {
    clearSession(activeType)
  }
}

export const clearAllSessions = () => {
  if (!hasLocalStorage()) return

  Object.values(SESSION_KEYS).forEach(keys => {
    localStorage.removeItem(keys.token)
    localStorage.removeItem(keys.userId)
    localStorage.removeItem(keys.username)
  })

  localStorage.removeItem(STORAGE_KEYS.activeUserType)
  clearLegacySession()
}

export const syncSessionForPath = (path) => {
  const routeType = getUserTypeByPath(path)
  if (routeType) {
    return setLegacyFromSession(routeType)
  }

  const activeType = getActiveUserType()
  if (!activeType) return false
  return setLegacyFromSession(activeType)
}

export const getTokenForPath = (path) => {
  const routeType = getUserTypeByPath(path)
  if (routeType) {
    return getSession(routeType).token
  }

  const activeType = getActiveUserType()
  if (!activeType) return ''
  return getSession(activeType).token
}

export const getUserIdForPath = (path) => {
  const routeType = getUserTypeByPath(path)
  if (routeType) {
    return getSession(routeType).userId
  }

  const activeType = getActiveUserType()
  if (!activeType) return ''
  return getSession(activeType).userId
}

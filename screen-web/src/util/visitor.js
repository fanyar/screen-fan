const VISITOR_KEY = 'sx-platform-visitor-key'

const createVisitorKey = () => {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID().replaceAll('-', '')
  }
  return `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 12)}`
}

export const getVisitorKey = () => {
  try {
    let value = window.localStorage.getItem(VISITOR_KEY)
    if (!value) {
      value = createVisitorKey()
      window.localStorage.setItem(VISITOR_KEY, value)
    }
    return value
  } catch (error) {
    return createVisitorKey()
  }
}

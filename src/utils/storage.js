const TOKEN_KEY = 'chronic_care_token'
const USER_KEY = 'chronic_care_user'

export const storage = {
  get(key) {
    const value = localStorage.getItem(key)
    if (value) {
      try {
        return JSON.parse(value)
      } catch {
        return value
      }
    }
    return null
  },

  set(key, value) {
    if (typeof value === 'object') {
      localStorage.setItem(key, JSON.stringify(value))
    } else {
      localStorage.setItem(key, value)
    }
  },

  remove(key) {
    localStorage.removeItem(key)
  },

  clear() {
    localStorage.clear()
  },

  getToken() {
    return this.get(TOKEN_KEY)
  },

  setToken(token) {
    this.set(TOKEN_KEY, token)
  },

  removeToken() {
    this.remove(TOKEN_KEY)
  },

  getUser() {
    return this.get(USER_KEY)
  },

  setUser(user) {
    this.set(USER_KEY, user)
  },

  removeUser() {
    this.remove(USER_KEY)
  }
}

export default storage

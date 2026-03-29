export const isValidPhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

export const isValidEmail = (email) => {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
}

export const isValidPassword = (password) => {
  return password && password.length >= 6 && password.length <= 20
}

export const isValidIdCard = (idCard) => {
  return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard)
}

export const isValidUsername = (username) => {
  return /^[a-zA-Z0-9_]{4,16}$/.test(username)
}

export const isValidRealName = (name) => {
  return /^[\u4e00-\u9fa5]{2,10}$/.test(name)
}

export const isValidSmsCode = (code) => {
  return /^\d{6}$/.test(code)
}

export const isEmpty = (value) => {
  return value === null || value === undefined || value === ''
}

export const isNotEmpty = (value) => {
  return !isEmpty(value)
}

export const isValidUrl = (url) => {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

export const isValidFileSize = (file, maxSizeMB = 10) => {
  return file && file.size <= maxSizeMB * 1024 * 1024
}

export const isValidImageType = (file) => {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png']
  return file && validTypes.includes(file.type)
}

export const isValidPdfType = (file) => {
  return file && file.type === 'application/pdf'
}

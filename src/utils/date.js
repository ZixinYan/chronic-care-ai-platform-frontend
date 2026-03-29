import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

export const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return ''
  return dayjs(date).format(format)
}

export const formatDateTime = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return ''
  return dayjs(date).format(format)
}

export const formatTime = (date, format = 'HH:mm:ss') => {
  if (!date) return ''
  return dayjs(date).format(format)
}

export const fromNow = (date) => {
  if (!date) return ''
  return dayjs(date).fromNow()
}

export const getAge = (birthday) => {
  if (!birthday) return 0
  return dayjs().diff(dayjs(birthday), 'year')
}

export const isToday = (date) => {
  return dayjs(date).isSame(dayjs(), 'day')
}

export const isBeforeToday = (date) => {
  return dayjs(date).isBefore(dayjs(), 'day')
}

export const isAfterToday = (date) => {
  return dayjs(date).isAfter(dayjs(), 'day')
}

export const getDaysDiff = (date1, date2) => {
  return dayjs(date1).diff(dayjs(date2), 'day')
}

export const addDays = (date, days) => {
  return dayjs(date).add(days, 'day').format('YYYY-MM-DD')
}

export const subtractDays = (date, days) => {
  return dayjs(date).subtract(days, 'day').format('YYYY-MM-DD')
}

export const getWeekStart = (date) => {
  return dayjs(date).startOf('week').format('YYYY-MM-DD')
}

export const getWeekEnd = (date) => {
  return dayjs(date).endOf('week').format('YYYY-MM-DD')
}

export const getMonthStart = (date) => {
  return dayjs(date).startOf('month').format('YYYY-MM-DD')
}

export const getMonthEnd = (date) => {
  return dayjs(date).endOf('month').format('YYYY-MM-DD')
}

export default dayjs

const ROLE_NAME_MAP = {
  ADMIN: '管理员',
  TEACHER: '教师',
  HEAD_TEACHER: '班主任',
  STUDENT: '学生'
}

const ROLE_TYPE_MAP = {
  ADMIN: 'danger',
  TEACHER: 'warning',
  HEAD_TEACHER: 'primary',
  STUDENT: 'success'
}

const ROLE_COLOR_MAP = {
  ADMIN: '#EF4444',
  TEACHER: '#F59E0B',
  HEAD_TEACHER: '#4F46E5',
  STUDENT: '#10B981'
}

export const getRoleName = (role) => {
  return ROLE_NAME_MAP[role] || role || '-'
}

export const getRoleType = (role) => {
  return ROLE_TYPE_MAP[role] || 'info'
}

export const getRoleColor = (role) => {
  return ROLE_COLOR_MAP[role] || '#9CA3AF'
}

export const ROLE_OPTIONS = [
  { label: '管理员', value: 'ADMIN' },
  { label: '教师', value: 'TEACHER' },
  { label: '班主任', value: 'HEAD_TEACHER' },
  { label: '学生', value: 'STUDENT' }
]

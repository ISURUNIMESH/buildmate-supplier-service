import { useAuth } from '../auth/AuthContext'

/** True when JWT/user.roles includes ADMIN / ROLE_ADMIN / admin. */
export function isAdminUser(user) {
  const roles = user?.roles
  if (!Array.isArray(roles)) return false
  return roles.some((role) => {
    const normalized = String(role || '').trim().toUpperCase().replace(/^ROLE_/, '')
    return normalized === 'ADMIN'
  })
}

export function useIsAdmin() {
  const { user } = useAuth()
  return isAdminUser(user)
}

export function currentUserId(user) {
  return user?.id || user?.sub || ''
}

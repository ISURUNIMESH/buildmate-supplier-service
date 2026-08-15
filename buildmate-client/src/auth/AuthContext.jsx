import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'

const TOKEN_KEY = 'buildmate_access_token'
const EXPIRES_KEY = 'buildmate_token_expires_at'
const USER_KEY = 'buildmate_user'

const AuthContext = createContext(null)

function decodeJwtPayload(token) {
  try {
    const part = token.split('.')[1]
    const normalized = part.replace(/-/g, '+').replace(/_/g, '/')
    const json = atob(normalized)
    return JSON.parse(json)
  } catch {
    return null
  }
}

function readStoredAuth() {
  const token = localStorage.getItem(TOKEN_KEY)
  const expiresAt = Number(localStorage.getItem(EXPIRES_KEY) || 0)
  const userRaw = localStorage.getItem(USER_KEY)
  if (!token || !expiresAt || Date.now() >= expiresAt) {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EXPIRES_KEY)
    localStorage.removeItem(USER_KEY)
    return { token: null, user: null, expiresAt: 0 }
  }
  let user = null
  try {
    user = userRaw ? JSON.parse(userRaw) : null
  } catch {
    user = null
  }
  if (!user) {
    const payload = decodeJwtPayload(token)
    if (payload) {
      user = {
        id: payload.id || payload.sub,
        email: payload.email,
        name: payload.name,
        profileImageUrl: payload.picture,
        roles: payload.roles || [],
      }
    }
  }
  return { token, user, expiresAt }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => readStoredAuth())
  const [booting, setBooting] = useState(true)

  useEffect(() => {
    setAuth(readStoredAuth())
    setBooting(false)
  }, [])

  const loginWithToken = useCallback((token, expiresInSeconds) => {
    const payload = decodeJwtPayload(token)
    if (!payload) {
      throw new Error('Invalid token received')
    }
    const expiresAt = Date.now() + (Number(expiresInSeconds) || 3600) * 1000
    const user = {
      id: payload.id || payload.sub,
      email: payload.email,
      name: payload.name,
      profileImageUrl: payload.picture,
      roles: payload.roles || [],
    }
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(EXPIRES_KEY, String(expiresAt))
    localStorage.setItem(USER_KEY, JSON.stringify(user))
    setAuth({ token, user, expiresAt })
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EXPIRES_KEY)
    localStorage.removeItem(USER_KEY)
    setAuth({ token: null, user: null, expiresAt: 0 })
  }, [])

  const value = useMemo(() => ({
    token: auth.token,
    user: auth.user,
    isAuthenticated: Boolean(auth.token),
    booting,
    loginWithToken,
    logout,
    getAccessToken: () => {
      const current = readStoredAuth()
      if (!current.token) {
        setAuth(current)
        return null
      }
      return current.token
    },
  }), [auth, booting, loginWithToken, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

export { TOKEN_KEY }

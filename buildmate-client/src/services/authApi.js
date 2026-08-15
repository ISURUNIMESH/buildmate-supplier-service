import api from './api'

export const authApi = {
  me: () => api.get('/auth/me').then((r) => r.data),
  listUsers: () => api.get('/auth/users').then((r) => r.data),
}

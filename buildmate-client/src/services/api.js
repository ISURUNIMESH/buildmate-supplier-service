import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE || '/api'

export const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

let authHandlers = {
  getToken: () => null,
  onUnauthorized: () => {},
}

export function configureApiAuth({ getToken, onUnauthorized }) {
  authHandlers = {
    getToken: getToken || (() => null),
    onUnauthorized: onUnauthorized || (() => {}),
  }
}

api.interceptors.request.use((config) => {
  const token = authHandlers.getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      authHandlers.onUnauthorized()
    }
    return Promise.reject(error)
  },
)

export function getErrorMessage(error) {
  if (!error) return 'Unexpected error'
  if (!error.response) {
    return error.message?.includes('Network')
      ? 'Network error. Check that the Gateway and Auth Server are running.'
      : (error.message || 'Network error')
  }

  const { status, data } = error.response
  const backendMessage =
    data?.message ||
    data?.error ||
    (data?.errors && typeof data.errors === 'object'
      ? Object.values(data.errors).join(', ')
      : null) ||
    (typeof data === 'string' ? data : null)

  switch (status) {
    case 400:
      return backendMessage || 'Bad request. Please check your input.'
    case 401:
      return backendMessage || 'Unauthorized. Please sign in again.'
    case 403:
      return backendMessage || 'Forbidden.'
    case 404:
      return backendMessage || 'Resource not found.'
    case 409:
      return backendMessage || 'Conflict with existing data.'
    case 429:
      return backendMessage || 'Too many requests. Please wait and try again.'
    case 502:
      return backendMessage || 'Upstream service unavailable.'
    case 500:
      return backendMessage || 'Internal server error.'
    default:
      return backendMessage || `Request failed (${status}).`
  }
}

export default api

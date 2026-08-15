import api from './api'

export const paymentApi = {
  list: () => api.get('/payments').then((r) => r.data),
  getById: (id) => api.get(`/payments/${id}`).then((r) => r.data),
  create: (body) => api.post('/payments', body).then((r) => r.data),
  history: (userId) => api.get(`/payments/history/${encodeURIComponent(userId)}`).then((r) => r.data),
  byUser: (userId) => api.get(`/payments/user/${encodeURIComponent(userId)}`).then((r) => r.data),
  pending: () => api.get('/payments/pending').then((r) => r.data),
  byStatus: (status) => api.get(`/payments/status/${encodeURIComponent(status)}`).then((r) => r.data),
  updateStatus: (id, status) => api.patch(`/payments/${id}/status`, null, { params: { status } }).then((r) => r.data),
  refund: (id) => api.post(`/payments/${id}/refund`).then((r) => r.data),
  retry: (id) => api.post(`/payments/${id}/retry`).then((r) => r.data),

  createInvoice: (body) => api.post('/invoices', body).then((r) => r.data),
  getInvoice: (id) => api.get(`/invoices/${id}`).then((r) => r.data),

  revenue: () => api.get('/reports/revenue').then((r) => r.data),
  monthly: () => api.get('/reports/monthly').then((r) => r.data),
  topCustomers: () => api.get('/reports/top-customers').then((r) => r.data),
}

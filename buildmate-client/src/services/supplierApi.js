import api from './api'

export const supplierApi = {
  list: () => api.get('/suppliers').then((r) => r.data),
  getById: (id) => api.get(`/suppliers/${id}`).then((r) => r.data),
  create: (body) => api.post('/suppliers', body).then((r) => r.data),
  update: (id, body) => api.put(`/suppliers/${id}`, body).then((r) => r.data),
  remove: (id) => api.delete(`/suppliers/${id}`),
  updateStatus: (id, status) => api.patch(`/suppliers/${id}/status`, { status }).then((r) => r.data),
  updateRating: (id, rating) => api.patch(`/suppliers/${id}/rating`, { rating }).then((r) => r.data),
  topRated: () => api.get('/suppliers/top-rated').then((r) => r.data),
  listMaterials: (id) => api.get(`/suppliers/${encodeURIComponent(id)}/materials`).then((r) => r.data),
  listDocuments: (id) => api.get(`/suppliers/${id}/documents`).then((r) => r.data),
  uploadDocument: (id, body) => api.post(`/suppliers/${id}/documents`, body).then((r) => r.data),
}

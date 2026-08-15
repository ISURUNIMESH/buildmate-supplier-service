import api from './api'

export const orderApi = {
  list: () => api.get('/orders').then((r) => r.data),
  getById: (id) => api.get(`/orders/${id}`).then((r) => r.data),
  create: (body) => api.post('/orders', body).then((r) => r.data),
  updateStatus: (id, status) => api.patch(`/orders/${id}/status`, null, { params: { status } }).then((r) => r.data),
  remove: (id) => api.delete(`/orders/${id}`),
  byUser: (userId) => api.get(`/orders/user/${encodeURIComponent(userId)}`).then((r) => r.data),
  byStatus: (status) => api.get(`/orders/status/${encodeURIComponent(status)}`).then((r) => r.data),

  getCart: (userId) => api.get(`/cart/${encodeURIComponent(userId)}`).then((r) => r.data),
  addToCart: (body) => api.post('/cart', body).then((r) => r.data),
  clearCart: (userId) => api.delete(`/cart/${encodeURIComponent(userId)}`),

  listInventory: () => api.get('/inventory').then((r) => r.data),
  createInventory: (body) => api.post('/inventory', body).then((r) => r.data),
  updateInventory: (materialId, body) =>
    api.put(`/inventory/${encodeURIComponent(materialId)}`, body).then((r) => r.data),
  reserve: (materialId, quantity) =>
    api.patch(`/inventory/${encodeURIComponent(materialId)}/reserve`, { quantity }).then((r) => r.data),
  release: (materialId, quantity) =>
    api.patch(`/inventory/${encodeURIComponent(materialId)}/release`, { quantity }).then((r) => r.data),
  inventoryHistory: () => api.get('/inventory/history').then((r) => r.data),
}

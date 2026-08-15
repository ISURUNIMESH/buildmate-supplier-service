import api from './api'

export const materialApi = {
  list: () => api.get('/materials').then((r) => r.data),
  getById: (id) => api.get(`/materials/${id}`).then((r) => r.data),
  create: (body) => api.post('/materials', body).then((r) => r.data),
  update: (id, body) => api.put(`/materials/${id}`, body).then((r) => r.data),
  remove: (id) => api.delete(`/materials/${id}`),
  byCategory: (category) => api.get(`/materials/category/${encodeURIComponent(category)}`).then((r) => r.data),
  search: (keyword) => api.get('/materials/search', { params: { keyword } }).then((r) => r.data),
  lowStock: () => api.get('/materials/low-stock').then((r) => r.data),
  updateStock: (id, stock) => api.patch(`/materials/${id}/stock`, { stock }).then((r) => r.data),
  updatePrice: (id, price) => api.patch(`/materials/${id}/price`, { price }).then((r) => r.data),
  listCategories: () => api.get('/categories').then((r) => r.data),
  createCategory: (body) => api.post('/categories', body).then((r) => r.data),
  updateCategory: (id, body) => api.put(`/categories/${id}`, body).then((r) => r.data),
  deleteCategory: (id) => api.delete(`/categories/${id}`),
  listBrands: () => api.get('/brands').then((r) => r.data),
  createBrand: (body) => api.post('/brands', body).then((r) => r.data),
  updateBrand: (id, body) => api.put(`/brands/${id}`, body).then((r) => r.data),
  deleteBrand: (id) => api.delete(`/brands/${id}`),
}

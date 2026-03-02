// src/services/userService.js
import api from './api';

export const userService = {
    getAll: () => api.get('/users'),
    getById: (id) => api.get(`/users/${id}`),
    create: (data) => api.post('/users', data),
    update: (id, data) => api.put(`/users/${id}`, data),
    delete: (id) => api.delete(`/users/${id}`),
    changeRole: (id, role) => api.put(`/users/${id}/role`, null, { params: { role } }),
    toggleStatus: (id) => api.put(`/users/${id}/toggle-status`),
};
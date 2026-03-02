// src/services/statsService.js
import api from './api';

export const statsService = {
    getDashboard: () => api.get('/admin/dashboard'),
    // ou /api/stats/dashboard si tu préfères séparer
};
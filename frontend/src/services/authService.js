import api from './api';

export const authService = {
    login: (credentials) => api.post('/auth/login', credentials),
    register: (data) => api.post('/auth/register', data),
    logout: () => api.post('/auth/logout'),
    refresh: (token) => api.post('/auth/refresh', { token }),
};
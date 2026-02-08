import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Intercepteur pour ajouter le token JWT
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Intercepteur pour gérer les erreurs 401
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// API Auth
export const authAPI = {
    login: (credentials) => api.post('/auth/login', credentials),
    logout: () => api.post('/auth/logout'),
};

// API Candidatures
export const candidatureAPI = {
    getAll: () => api.get('/candidatures'),
    getById: (id) => api.get(`/candidatures/${id}`),
    create: (data) => api.post('/candidatures/create', data),
    validate: (id, decision, commentaire, auteur) =>
        api.put(`/candidatures/${id}/validate`, null, {
            params: { decision, commentaire, auteur }
        }),
};

// API Stats
export const statsAPI = {
    getDashboard: () => api.get('/stats/dashboard'),
};

// API Users
export const userAPI = {
    getAll: () => api.get('/users'),
    getById: (id) => api.get(`/users/${id}`),
    create: (data) => api.post('/users', data),
    update: (id, data) => api.put(`/users/${id}`, data),
    delete: (id) => api.delete(`/users/${id}`),
    changeRole: (id, role) => api.put(`/users/${id}/role`, null, { params: { role } }),
    toggleStatus: (id) => api.put(`/users/${id}/toggle-status`),
};

export default api;
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

// Create axios instance
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add token to requests if it exists
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

// Handle response errors
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

// Auth API
export const authAPI = {
    login: (credentials) => api.post('/auth/login', credentials),
    logout: () => api.post('/auth/logout'),
};

// Candidatures API
export const candidatureAPI = {
    getAll: () => api.get('/candidatures'),
    getById: (id) => api.get(`/candidatures/${id}`),
    create: (data) => api.post('/candidatures/create', data),
    validate: (id, decision, commentaire, auteur) =>
        api.put(`/candidatures/${id}/validate`, null, {
            params: { decision, commentaire, auteur }
        }),
};

// Stats API
export const statsAPI = {
    getDashboard: () => api.get('/stats/dashboard'),
};

export default api;
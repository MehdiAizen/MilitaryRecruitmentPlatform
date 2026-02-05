import axios from 'axios';

// On utilise l'URL fournie par l'environnement ou '/api' par défaut (pour Nginx proxy)
// Cela corrige le bug où l'application tentait d'appeler localhost:8081 depuis Docker
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

// Create axios instance
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000, // 10 secondes timeout
});

// Request interceptor - Add token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        // Log en développement
        if (process.env.NODE_ENV === 'development') {
            console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, config.params || '');
        }

        return config;
    },
    (error) => {
        console.error('[API Request Error]', error);
        return Promise.reject(error);
    }
);

// Response interceptor - Handle errors and retry
api.interceptors.response.use(
    (response) => {
        // Log en développement
        if (process.env.NODE_ENV === 'development') {
            console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, response.status);
        }
        return response;
    },
    async (error) => {
        const originalRequest = error.config;

        // Retry logic for network errors (max 3 attempts)
        if (!error.response && originalRequest && !originalRequest._retry) {
            originalRequest._retryCount = originalRequest._retryCount || 0;

            if (originalRequest._retryCount < 3) {
                originalRequest._retryCount++;
                originalRequest._retry = true;

                console.warn(`[API Retry] Attempt ${originalRequest._retryCount}/3 for ${originalRequest.url}`);

                // Delay exponentiel: 1s, 2s, 4s
                const delay = Math.pow(2, originalRequest._retryCount - 1) * 1000;
                await new Promise(resolve => setTimeout(resolve, delay));

                return api(originalRequest);
            }
        }

        // Handle 401 Unauthorized
        if (error.response?.status === 401) {
            console.warn('[API] 401 Unauthorized - Redirecting to login');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
            return Promise.reject(new Error('Session expirée. Veuillez vous reconnecter.'));
        }

        // Handle 403 Forbidden
        if (error.response?.status === 403) {
            return Promise.reject(new Error('Accès refusé. Vous n\'avez pas les permissions nécessaires.'));
        }

        // Handle 404 Not Found
        if (error.response?.status === 404) {
            return Promise.reject(new Error('Ressource non trouvée.'));
        }

        // Handle validation errors (400)
        if (error.response?.status === 400 && error.response?.data) {
            const validationErrors = error.response.data;
            const errorMessages = Object.entries(validationErrors)
                .map(([field, message]) => `${field}: ${message}`)
                .join(', ');
            return Promise.reject(new Error(`Erreur de validation: ${errorMessages}`));
        }

        // Handle 500+ errors
        if (error.response?.status >= 500) {
            return Promise.reject(new Error('Erreur serveur. Veuillez réessayer plus tard.'));
        }

        // Network error (no response)
        if (!error.response) {
            return Promise.reject(new Error('Erreur réseau. Vérifiez votre connexion.'));
        }

        // Default error
        const message = error.response?.data?.message || error.message || 'Une erreur est survenue';
        return Promise.reject(new Error(message));
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

// Users API
export const userAPI = {
    getAll: () => api.get('/users'),
    getById: (id) => api.get(`/users/${id}`),
    create: (data) => api.post('/users', data),
    update: (id, data) => api.put(`/users/${id}`, data),
    delete: (id) => api.delete(`/users/${id}`),
    changeRole: (id, role) => api.put(`/users/${id}/role`, null, { params: { role } }),
    toggleStatus: (id) => api.put(`/users/${id}/toggle-status`),
};

// Stats API
export const statsAPI = {
    getDashboard: () => api.get('/stats/dashboard'),
};

// Unites API (ajouté si besoin)
export const uniteAPI = {
    getAll: () => api.get('/unites'),
    getById: (id) => api.get(`/unites/${id}`),
    create: (data) => api.post('/unites', data),
    update: (id, data) => api.put(`/unites/${id}`, data),
    delete: (id) => api.delete(`/unites/${id}`),
};

export default api;
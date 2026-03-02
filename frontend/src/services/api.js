import axios from 'axios';

const getBaseURL = () => {
    const apiUrl = import.meta.env.VITE_API_URL;

    if (!apiUrl) {
        throw new Error(
            'VITE_API_URL environment variable is required. ' +
            'Please set it in your .env file or environment variables.'
        );
    }

    try {
        // FIX: accepte les URLs relatives comme '/api' (cas Docker)
        // new URL('/api') plante car ce n'est pas une URL absolue
        const url = apiUrl.startsWith('http')
            ? new URL(apiUrl)
            : new URL(apiUrl, window.location.origin);

        if (!['http:', 'https:'].includes(url.protocol)) {
            throw new Error(`Invalid protocol: ${url.protocol}`);
        }

        if (import.meta.env.PROD && url.protocol !== 'https:' && !apiUrl.startsWith('/')) {
            console.warn('Warning: Using HTTP in production is not recommended');
        }

        return apiUrl;
    } catch (e) {
        throw new Error(`Invalid VITE_API_URL: ${apiUrl}. Error: ${e.message}`);
    }
};

const api = axios.create({
    baseURL: getBaseURL(),
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 30000,
    withCredentials: true,
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        config.headers['X-Request-ID'] = generateRequestId();
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response?.status === 401) {
            if (!error.config.url.includes('/auth/login')) {
                console.error('Session expirée ou invalide');
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                setTimeout(() => {
                    window.location.href = '/login?expired=true';
                }, 100);
            }
        }

        if (error.response?.status >= 500) {
            console.error('Erreur serveur:', error.response.data);
        }

        return Promise.reject(error);
    }
);

function generateRequestId() {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

export default api;
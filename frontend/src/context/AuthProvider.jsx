import React, { useState, useEffect, useCallback, useRef } from 'react';
import { AuthContext } from './AuthContext';
import { authService } from '../services/authService';

const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    const [loading, setLoading] = useState(true);
    const refreshTimeoutRef = useRef(null);

    const parseJwt = (tkn) => {
        try {
            const base64Url = tkn.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(
                atob(base64).split('').map(c =>
                    '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
                ).join('')
            );
            return JSON.parse(jsonPayload);
        } catch (e) {
            return null;
        }
    };

    const clearAuth = useCallback(() => {
        if (refreshTimeoutRef.current) {
            clearTimeout(refreshTimeoutRef.current);
            refreshTimeoutRef.current = null;
        }
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
    }, []);

    const scheduleTokenRefresh = useCallback((currentToken) => {
        if (refreshTimeoutRef.current) {
            clearTimeout(refreshTimeoutRef.current);
            refreshTimeoutRef.current = null;
        }
        const payload = parseJwt(currentToken);
        if (!payload) return;
        const timeUntilRefresh = payload.exp * 1000 - Date.now() - 60000;
        if (timeUntilRefresh > 0) {
            refreshTimeoutRef.current = setTimeout(async () => {
                try {
                    const response = await authService.refresh(currentToken);
                    const { token: newToken } = response.data;
                    setToken(newToken);
                    localStorage.setItem('token', newToken);
                    scheduleTokenRefresh(newToken);
                } catch (error) {
                    clearAuth();
                    window.location.href = '/login?expired=true';
                }
            }, timeUntilRefresh);
        }
    }, [clearAuth]);

    useEffect(() => {
        const initAuth = () => {
            try {
                const savedToken = localStorage.getItem('token');
                const savedUser = localStorage.getItem('user');
                if (savedToken && savedUser) {
                    const payload = parseJwt(savedToken);
                    if (payload && payload.exp * 1000 > Date.now()) {
                        setToken(savedToken);
                        setUser(JSON.parse(savedUser));
                        scheduleTokenRefresh(savedToken);
                    } else {
                        clearAuth();
                    }
                }
            } catch (error) {
                clearAuth();
            } finally {
                setLoading(false);
            }
        };
        initAuth();
        return () => {
            if (refreshTimeoutRef.current) clearTimeout(refreshTimeoutRef.current);
        };
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    const login = async (email, password) => {
        try {
            const response = await authService.login({ email, password });
            const { token: newToken, ...userData } = response.data;
            setToken(newToken);
            setUser(userData);
            localStorage.setItem('user', JSON.stringify(userData));
            localStorage.setItem('token', newToken);
            scheduleTokenRefresh(newToken);
            return { success: true };
        } catch (error) {
            return {
                success: false,
                message: error.response?.data?.message || 'Erreur de connexion',
            };
        }
    };

    const logout = async () => {
        try {
            await authService.logout();
        } catch (error) {
            console.error('Erreur logout:', error);
        } finally {
            clearAuth();
        }
    };

    return (
        <AuthContext.Provider value={{
            user, token, login, logout, loading,
            isAuthenticated: !!token && !!user,
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export default AuthProvider;
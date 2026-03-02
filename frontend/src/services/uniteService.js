// src/services/uniteService.js
import api from './api';

export const uniteService = {
    getAll: () => api.get('/unites'),
    // FIX : était '/unites/unites/noms' (double /unites), corrigé en '/unites/noms'
    getNoms: () => api.get('/unites/noms'),
};
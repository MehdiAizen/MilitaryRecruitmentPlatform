// src/services/candidatureService.js
import api from './api';

export const candidatureService = {
    create: (data) => api.post('/candidatures/create', data),
    getAll: () => api.get('/candidatures'),
    getById: (id) => api.get(`/candidatures/${id}`),
    getMyCandidatures: () => api.get('/candidatures/mes-candidatures'),
    getMyCandidatureDetail: (id) => api.get(`/candidatures/${id}/mon-dossier`),
    updateMyCandidature: (id, data) => api.put(`/candidatures/${id}/modifier`, data),
    validate: (id, decision, commentaire) =>
        api.put(`/candidatures/${id}/validate`, null, {
            params: { decision, commentaire },
        }),
    mettreEnExamen: (id, commentaire) =>
        api.put(`/candidatures/${id}/examen`, null, { params: { commentaire } }),
    signer: (id) => api.post(`/candidatures/${id}/signer`),
};
// src/components/StatusBadge.jsx
import React from 'react';
import Badge from './Badge';

const StatusBadge = ({ statut }) => {
    const variants = {
        EN_ATTENTE: 'warning',
        EN_EXAMEN: 'info',
        VALIDEE: 'success',
        REJETEE: 'danger',
    };

    const texts = {
        EN_ATTENTE: 'En attente',
        EN_EXAMEN: 'En examen',
        VALIDEE: 'Validée',
        REJETEE: 'Rejetée',
    };

    return (
        <Badge variant={variants[statut] || 'default'}>
            {texts[statut] || statut}
        </Badge>
    );
};

export default StatusBadge;
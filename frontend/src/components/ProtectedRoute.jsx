import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children, allowedRoles }) => {
    const { user, loading } = useAuth();

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
        );
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && !allowedRoles.includes(user.role)) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="card max-w-md text-center">
                    <h2 className="text-2xl font-bold text-red-600 mb-4">Accès Refusé</h2>
                    <p className="text-gray-600">
                        Vous n'avez pas les permissions nécessaires pour accéder à cette page.
                    </p>
                </div>
            </div>
        );
    }

    return children;
};

export default ProtectedRoute;
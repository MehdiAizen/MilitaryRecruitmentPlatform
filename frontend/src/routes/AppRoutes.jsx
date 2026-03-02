// src/routes/AppRoutes.jsx
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from '../components/ProtectedRoute';

// Pages
import Login from '../pages/Login';
import Register from '../pages/Register';
import CandidatureForm from '../pages/CandidatureForm';
import MyCandidatures from '../pages/MyCandidatures';
import CandidaturesList from '../pages/CandidaturesList';
import Dashboard from '../pages/Dashboard';
import Stats from '../pages/Stats';
import UserManagement from '../pages/UserManagement';
import UniteManagement  from "../pages/UniteManagement.jsx";
const AppRoutes = () => {
    return (
        <Routes>
            {/* Routes publiques */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/candidature" element={<CandidatureForm />} />

            {/* Routes protégées */}
            <Route
                path="/dashboard"
                element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'RH', 'COMMANDANT', 'CANDIDAT']}>
                        <Dashboard />
                    </ProtectedRoute>
                }
            />

            {/* Candidat : ses propres candidatures */}
            <Route
                path="/my-candidatures"
                element={
                    <ProtectedRoute allowedRoles={['CANDIDAT']}>
                        <MyCandidatures />
                    </ProtectedRoute>
                }
            />

            {/* RH & Commandant : liste complète */}
            <Route
                path="/candidatures"
                element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'RH', 'COMMANDANT']}>
                        <CandidaturesList />
                    </ProtectedRoute>
                }
            />

            {/* Admin only */}
            <Route
                path="/stats"
                element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                        <Stats />
                    </ProtectedRoute>
                }
            />
            <Route
                path="/unites"
                element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                        <UniteManagement />
                    </ProtectedRoute>
                }
            />
            <Route
                path="/users"
                element={
                    <ProtectedRoute allowedRoles={['ADMIN']}>
                        <UserManagement />
                    </ProtectedRoute>
                }
            />

            {/* Redirections par défaut */}
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
    );
};

export default AppRoutes;
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

// Pages
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import CandidatureForm from './pages/CandidatureForm';
import CandidaturesList from './pages/CandidaturesList';
import Stats from './pages/Stats';
import UserManagement from './pages/UserManagement';

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    {/* Public Routes */}
                    <Route path="/login" element={<Login />} />
                    <Route path="/candidature" element={<CandidatureForm />} />

                    {/* Protected Routes */}
                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute allowedRoles={['ADMIN', 'RH', 'COMMANDANT']}>
                                <Dashboard />
                            </ProtectedRoute>
                        }
                    />

                    <Route
                        path="/candidatures"
                        element={
                            <ProtectedRoute allowedRoles={['ADMIN', 'RH', 'COMMANDANT']}>
                                <CandidaturesList />
                            </ProtectedRoute>
                        }
                    />

                    {/* Stats route - only for ADMIN */}
                    <Route
                        path="/stats"
                        element={
                            <ProtectedRoute allowedRoles={['ADMIN']}>
                                <Stats />
                            </ProtectedRoute>
                        }
                    />

                    {/* User Management route - only for ADMIN */}
                    <Route
                        path="/users"
                        element={
                            <ProtectedRoute allowedRoles={['ADMIN']}>
                                <UserManagement />
                            </ProtectedRoute>
                        }
                    />

                    {/* Default Redirect */}
                    <Route path="/" element={<Navigate to="/login" replace />} />
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
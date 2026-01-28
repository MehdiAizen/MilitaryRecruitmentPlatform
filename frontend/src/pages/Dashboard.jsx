import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { statsAPI, candidatureAPI } from '../services/api';
import {
    Users, Clock, CheckCircle, XCircle,
    FileText, LogOut, TrendingUp
} from 'lucide-react';

const Dashboard = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [recentCandidatures, setRecentCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            // Fetch stats only for ADMIN
            if (user.role === 'ADMIN') {
                const statsResponse = await statsAPI.getDashboard();
                setStats(statsResponse.data);
            }

            // Fetch recent candidatures
            const candidaturesResponse = await candidatureAPI.getAll();
            setRecentCandidatures(candidaturesResponse.data.slice(0, 5));
        } catch (error) {
            console.error('Error fetching data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const getStatusBadge = (statut) => {
        const styles = {
            EN_ATTENTE: 'badge badge-warning',
            EN_EXAMEN: 'badge badge-info',
            VALIDEE: 'badge badge-success',
            REJETEE: 'badge badge-danger',
        };
        return styles[statut] || 'badge';
    };

    const getStatusText = (statut) => {
        const texts = {
            EN_ATTENTE: 'En attente',
            EN_EXAMEN: 'En examen',
            VALIDEE: 'Validée',
            REJETEE: 'Rejetée',
        };
        return texts[statut] || statut;
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <header className="bg-white shadow-sm">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                    <div className="flex justify-between items-center">
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">
                                Tableau de bord
                            </h1>
                            <p className="text-sm text-gray-600 mt-1">
                                Bienvenue, {user.prenom} {user.nom} ({user.role})
                            </p>
                        </div>
                        <button
                            onClick={handleLogout}
                            className="btn-secondary flex items-center gap-2"
                        >
                            <LogOut className="w-4 h-4" />
                            Déconnexion
                        </button>
                    </div>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Stats Cards - Only for ADMIN */}
                {user.role === 'ADMIN' && stats && (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                        <div className="card bg-gradient-to-br from-blue-500 to-blue-600 text-white">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-blue-100 text-sm font-medium">Total</p>
                                    <p className="text-3xl font-bold mt-2">{stats.total}</p>
                                </div>
                                <Users className="w-12 h-12 text-blue-200" />
                            </div>
                        </div>

                        <div className="card bg-gradient-to-br from-yellow-500 to-yellow-600 text-white">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-yellow-100 text-sm font-medium">En attente</p>
                                    <p className="text-3xl font-bold mt-2">{stats.en_attente}</p>
                                </div>
                                <Clock className="w-12 h-12 text-yellow-200" />
                            </div>
                        </div>

                        <div className="card bg-gradient-to-br from-green-500 to-green-600 text-white">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-green-100 text-sm font-medium">Validées</p>
                                    <p className="text-3xl font-bold mt-2">{stats.validees}</p>
                                </div>
                                <CheckCircle className="w-12 h-12 text-green-200" />
                            </div>
                        </div>

                        <div className="card bg-gradient-to-br from-red-500 to-red-600 text-white">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-red-100 text-sm font-medium">Rejetées</p>
                                    <p className="text-3xl font-bold mt-2">{stats.rejetees}</p>
                                </div>
                                <XCircle className="w-12 h-12 text-red-200" />
                            </div>
                        </div>
                    </div>
                )}

                {/* Quick Actions */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <button
                        onClick={() => navigate('/candidatures')}
                        className="card hover:shadow-lg transition-shadow cursor-pointer group"
                    >
                        <div className="flex items-center gap-4">
                            <div className="bg-primary-100 p-3 rounded-lg group-hover:bg-primary-200 transition-colors">
                                <FileText className="w-6 h-6 text-primary-600" />
                            </div>
                            <div className="text-left">
                                <h3 className="font-semibold text-gray-900">Candidatures</h3>
                                <p className="text-sm text-gray-600">Gérer les candidatures</p>
                            </div>
                        </div>
                    </button>

                    {user.role === 'ADMIN' && (
                        <button
                            onClick={() => navigate('/stats')}
                            className="card hover:shadow-lg transition-shadow cursor-pointer group"
                        >
                            <div className="flex items-center gap-4">
                                <div className="bg-green-100 p-3 rounded-lg group-hover:bg-green-200 transition-colors">
                                    <TrendingUp className="w-6 h-6 text-green-600" />
                                </div>
                                <div className="text-left">
                                    <h3 className="font-semibold text-gray-900">Statistiques</h3>
                                    <p className="text-sm text-gray-600">Voir les rapports</p>
                                </div>
                            </div>
                        </button>
                    )}
                </div>

                {/* Recent Candidatures */}
                <div className="card">
                    <h2 className="text-xl font-bold text-gray-900 mb-4">
                        Candidatures Récentes
                    </h2>
                    {recentCandidatures.length === 0 ? (
                        <p className="text-gray-600 text-center py-8">
                            Aucune candidature pour le moment
                        </p>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                        Candidat
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                        CIN
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                        Email
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                        Unité
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                        Statut
                                    </th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {recentCandidatures.map((candidature) => (
                                    <tr key={candidature.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="font-medium text-gray-900">
                                                {candidature.prenom} {candidature.nom}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                            {candidature.cin}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                            {candidature.email}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                            {candidature.uniteChoisie}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                        <span className={getStatusBadge(candidature.statut)}>
                          {getStatusText(candidature.statut)}
                        </span>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                    <div className="mt-4 text-center">
                        <button
                            onClick={() => navigate('/candidatures')}
                            className="text-primary-600 hover:text-primary-700 font-medium text-sm"
                        >
                            Voir toutes les candidatures →
                        </button>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default Dashboard;
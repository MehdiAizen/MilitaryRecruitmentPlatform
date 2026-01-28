// NEW FILE: src/pages/Stats.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { statsAPI } from '../services/api';
import { ArrowLeft, TrendingUp, TrendingDown, Users, CheckCircle, XCircle, Clock } from 'lucide-react';

const Stats = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchStats();
    }, []);

    const fetchStats = async () => {
        try {
            const response = await statsAPI.getDashboard();
            setStats(response.data);
        } catch (error) {
            console.error('Error fetching stats:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
        );
    }

    const calculatePercentage = (value, total) => {
        return total > 0 ? ((value / total) * 100).toFixed(1) : 0;
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <header className="bg-white shadow-sm">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                    <div className="flex items-center gap-4">
                        <button
                            onClick={() => navigate('/dashboard')}
                            className="p-2 hover:bg-gray-100 rounded-lg"
                        >
                            <ArrowLeft className="w-5 h-5" />
                        </button>
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">
                                Statistiques Détaillées
                            </h1>
                            <p className="text-sm text-gray-600 mt-1">
                                Analyse des candidatures
                            </p>
                        </div>
                    </div>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Overview Cards */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    <div className="card bg-gradient-to-br from-blue-500 to-blue-600 text-white">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-blue-100 text-sm font-medium">Total</p>
                                <p className="text-3xl font-bold mt-2">{stats?.total || 0}</p>
                            </div>
                            <Users className="w-12 h-12 text-blue-200" />
                        </div>
                    </div>

                    <div className="card bg-gradient-to-br from-yellow-500 to-yellow-600 text-white">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-yellow-100 text-sm font-medium">En attente</p>
                                <p className="text-3xl font-bold mt-2">{stats?.en_attente || 0}</p>
                                <p className="text-xs text-yellow-100 mt-1">
                                    {calculatePercentage(stats?.en_attente, stats?.total)}% du total
                                </p>
                            </div>
                            <Clock className="w-12 h-12 text-yellow-200" />
                        </div>
                    </div>

                    <div className="card bg-gradient-to-br from-green-500 to-green-600 text-white">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-green-100 text-sm font-medium">Validées</p>
                                <p className="text-3xl font-bold mt-2">{stats?.validees || 0}</p>
                                <p className="text-xs text-green-100 mt-1">
                                    {calculatePercentage(stats?.validees, stats?.total)}% du total
                                </p>
                            </div>
                            <CheckCircle className="w-12 h-12 text-green-200" />
                        </div>
                    </div>

                    <div className="card bg-gradient-to-br from-red-500 to-red-600 text-white">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-red-100 text-sm font-medium">Rejetées</p>
                                <p className="text-3xl font-bold mt-2">{stats?.rejetees || 0}</p>
                                <p className="text-xs text-red-100 mt-1">
                                    {calculatePercentage(stats?.rejetees, stats?.total)}% du total
                                </p>
                            </div>
                            <XCircle className="w-12 h-12 text-red-200" />
                        </div>
                    </div>
                </div>

                {/* Detailed Statistics */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {/* Taux de validation */}
                    <div className="card">
                        <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                            <TrendingUp className="w-5 h-5 text-green-600" />
                            Taux de Validation
                        </h3>
                        <div className="space-y-4">
                            <div>
                                <div className="flex justify-between mb-2">
                                    <span className="text-sm font-medium text-gray-700">Validées</span>
                                    <span className="text-sm font-medium text-green-600">
                                        {calculatePercentage(stats?.validees, stats?.total)}%
                                    </span>
                                </div>
                                <div className="w-full bg-gray-200 rounded-full h-2">
                                    <div
                                        className="bg-green-600 h-2 rounded-full transition-all"
                                        style={{ width: `${calculatePercentage(stats?.validees, stats?.total)}%` }}
                                    ></div>
                                </div>
                            </div>

                            <div>
                                <div className="flex justify-between mb-2">
                                    <span className="text-sm font-medium text-gray-700">Rejetées</span>
                                    <span className="text-sm font-medium text-red-600">
                                        {calculatePercentage(stats?.rejetees, stats?.total)}%
                                    </span>
                                </div>
                                <div className="w-full bg-gray-200 rounded-full h-2">
                                    <div
                                        className="bg-red-600 h-2 rounded-full transition-all"
                                        style={{ width: `${calculatePercentage(stats?.rejetees, stats?.total)}%` }}
                                    ></div>
                                </div>
                            </div>

                            <div>
                                <div className="flex justify-between mb-2">
                                    <span className="text-sm font-medium text-gray-700">En attente</span>
                                    <span className="text-sm font-medium text-yellow-600">
                                        {calculatePercentage(stats?.en_attente, stats?.total)}%
                                    </span>
                                </div>
                                <div className="w-full bg-gray-200 rounded-full h-2">
                                    <div
                                        className="bg-yellow-600 h-2 rounded-full transition-all"
                                        style={{ width: `${calculatePercentage(stats?.en_attente, stats?.total)}%` }}
                                    ></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Summary */}
                    <div className="card">
                        <h3 className="text-lg font-semibold text-gray-900 mb-4">
                            Résumé
                        </h3>
                        <div className="space-y-3">
                            <div className="flex justify-between py-2 border-b border-gray-200">
                                <span className="text-gray-600">Total des candidatures</span>
                                <span className="font-semibold text-gray-900">{stats?.total || 0}</span>
                            </div>
                            <div className="flex justify-between py-2 border-b border-gray-200">
                                <span className="text-gray-600">Candidatures traitées</span>
                                <span className="font-semibold text-gray-900">
                                    {(stats?.validees || 0) + (stats?.rejetees || 0)}
                                </span>
                            </div>
                            <div className="flex justify-between py-2 border-b border-gray-200">
                                <span className="text-gray-600">Taux de traitement</span>
                                <span className="font-semibold text-gray-900">
                                    {calculatePercentage(
                                        (stats?.validees || 0) + (stats?.rejetees || 0),
                                        stats?.total
                                    )}%
                                </span>
                            </div>
                            <div className="flex justify-between py-2">
                                <span className="text-gray-600">En attente de traitement</span>
                                <span className="font-semibold text-yellow-600">{stats?.en_attente || 0}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default Stats;
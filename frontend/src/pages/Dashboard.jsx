import React, { useState, useEffect, useCallback } from 'react';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import Card from '../components/Card';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../hooks/useAuth';
import { candidatureService } from '../services/candidatureService';
import { statsService } from '../services/statsService';
import { Users, Clock, CheckCircle, XCircle } from 'lucide-react';

const Dashboard = () => {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [recentCandidatures, setRecentCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);

    // FIX: useCallback avec user comme dépendance pour éviter le stale closure
    // Avant : useEffect(() => { void fetchData(); }, []) avec eslint-disable
    // user.role était lu depuis une closure figée — si user changeait, fetchData ne se re-exécutait pas
    const fetchData = useCallback(async () => {
        try {
            if (user.role === 'ADMIN') {
                const statsResponse = await statsService.getDashboard();
                const statsData = statsResponse.data?.stats ?? statsResponse.data;
                setStats(statsData);
            }
            const candidaturesResponse = await candidatureService.getAll();
            setRecentCandidatures(candidaturesResponse.data.slice(0, 5));
        } catch (error) {
            console.error('Erreur chargement données:', error);
        } finally {
            setLoading(false);
        }
    }, [user]);

    useEffect(() => {
        void fetchData();
    }, [fetchData]);

    const formatDate = (date) => date ? new Date(date).toLocaleDateString('fr-FR') : '-';

    const getUniteNom = (c) => {
        if (!c?.uniteChoisie) return '-';
        if (typeof c.uniteChoisie === 'string') return c.uniteChoisie;
        return c.uniteChoisie.nom || '-';
    };

    if (loading) return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-military-navy"></div>
        </div>
    );

    const total     = stats?.total      ?? 0;
    const enAttente = stats?.en_attente ?? 0;
    const validees  = stats?.validees   ?? 0;
    const rejetees  = stats?.rejetees   ?? 0;

    return (
        <div className="flex min-h-screen bg-gray-50">
            <Sidebar />
            <div className="flex-1 ml-64 pt-20">
                <Navbar />
                <main className="p-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-8">Tableau de bord</h1>

                    {user.role === 'ADMIN' && stats && (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                            <Card>
                                <div className="flex items-center justify-between">
                                    <div><p className="text-sm text-gray-600">Total</p><p className="text-3xl font-bold">{total}</p></div>
                                    <Users className="w-10 h-10 text-military-navy" />
                                </div>
                            </Card>
                            <Card>
                                <div className="flex items-center justify-between">
                                    <div><p className="text-sm text-gray-600">En attente</p><p className="text-3xl font-bold">{enAttente}</p></div>
                                    <Clock className="w-10 h-10 text-yellow-600" />
                                </div>
                            </Card>
                            <Card>
                                <div className="flex items-center justify-between">
                                    <div><p className="text-sm text-gray-600">Validées</p><p className="text-3xl font-bold">{validees}</p></div>
                                    <CheckCircle className="w-10 h-10 text-green-600" />
                                </div>
                            </Card>
                            <Card>
                                <div className="flex items-center justify-between">
                                    <div><p className="text-sm text-gray-600">Rejetées</p><p className="text-3xl font-bold">{rejetees}</p></div>
                                    <XCircle className="w-10 h-10 text-red-600" />
                                </div>
                            </Card>
                        </div>
                    )}

                    <Card title="Candidatures récentes">
                        {recentCandidatures.length === 0 ? (
                            <p className="text-center py-8 text-gray-600">Aucune candidature récente</p>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Candidat</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Unité</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Statut</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
                                    </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                    {recentCandidatures.map((c) => (
                                        <tr key={c.id} className="hover:bg-gray-50">
                                            <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{c.prenom} {c.nom}</td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{getUniteNom(c)}</td>
                                            <td className="px-6 py-4 whitespace-nowrap"><StatusBadge statut={c.statut} /></td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{formatDate(c.createdAt)}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </Card>
                </main>
            </div>
        </div>
    );
};

export default Dashboard;
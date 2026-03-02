import React, { useState, useEffect } from 'react';
import { statsService } from '../services/statsService';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import Card from '../components/Card';

const Stats = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => { void fetchStats(); }, []); // eslint-disable-line

    const fetchStats = async () => {
        try {
            const response = await statsService.getDashboard();
            const statsData = response.data?.stats ?? response.data;
            setStats(statsData);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
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
                    <h1 className="text-3xl font-bold text-gray-900 mb-8">Statistiques</h1>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                        <Card title="Total"><p className="text-4xl font-bold">{total}</p></Card>
                        <Card title="En attente"><p className="text-4xl font-bold text-yellow-600">{enAttente}</p></Card>
                        <Card title="Validées"><p className="text-4xl font-bold text-green-600">{validees}</p></Card>
                        <Card title="Rejetées"><p className="text-4xl font-bold text-red-600">{rejetees}</p></Card>
                    </div>
                    {total > 0 && (
                        <Card title="Taux de validation">
                            <div className="flex items-center gap-4">
                                <div className="flex-1 bg-gray-200 rounded-full h-4">
                                    <div
                                        className="bg-green-500 h-4 rounded-full transition-all"
                                        style={{ width: `${Math.round((validees / total) * 100)}%` }}
                                    />
                                </div>
                                <span className="text-lg font-bold text-green-600">
                                    {Math.round((validees / total) * 100)}%
                                </span>
                            </div>
                        </Card>
                    )}
                </main>
            </div>
        </div>
    );
};

export default Stats;
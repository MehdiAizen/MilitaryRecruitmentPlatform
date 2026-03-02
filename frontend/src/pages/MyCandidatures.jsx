import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import Card from '../components/Card';
import StatusBadge from '../components/StatusBadge';
import { candidatureService } from '../services/candidatureService';

const MyCandidatures = () => {
    const [candidatures, setCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetch = async () => {
            try {
                const response = await candidatureService.getMyCandidatures();
                setCandidatures(response.data);
            } catch (err) {
                console.error(err);
                setError("Impossible de charger vos candidatures.");
            } finally {
                setLoading(false);
            }
        };
        fetch();
    }, []);

    if (loading) return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-military-navy"></div>
        </div>
    );

    return (
        <div className="flex min-h-screen bg-gray-50">
            <Sidebar />
            <div className="flex-1 ml-64 pt-20">
                <Navbar />
                <main className="p-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-8">Mes Candidatures</h1>

                    {error && (
                        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>
                    )}

                    {candidatures.length === 0 && !error ? (
                        <Card>
                            <p className="text-center py-8 text-gray-600">Aucune candidature soumise.</p>
                        </Card>
                    ) : (
                        <div className="grid gap-6">
                            {candidatures.map((c) => (
                                <Card key={c.id}>
                                    <div className="flex justify-between items-start">
                                        <div>
                                            <h3 className="text-xl font-semibold">{c.uniteChoisie || 'Unité non spécifiée'}</h3>
                                            <p className="text-sm text-gray-600">
                                                Soumise le {c.createdAt ? new Date(c.createdAt).toLocaleDateString('fr-FR') : '-'}
                                            </p>
                                        </div>
                                        <StatusBadge statut={c.statut} />
                                    </div>
                                    <div className="mt-4 text-sm text-gray-700">
                                        <p><strong>Référence :</strong> #{c.id}</p>
                                    </div>
                                </Card>
                            ))}
                        </div>
                    )}
                </main>
            </div>
        </div>
    );
};

export default MyCandidatures;
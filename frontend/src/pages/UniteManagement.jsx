import React, { useState, useEffect } from 'react';
import { uniteService } from '../services/uniteService';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import Card from '../components/Card';
import Button from '../components/Button';
import InputField from '../components/InputField';

const UniteManagement = () => {
    const [unites, setUnites] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [modalMode, setModalMode] = useState('create');
    const [selectedUnite, setSelectedUnite] = useState(null);
    const [formData, setFormData] = useState({ nom: '' });
    const [error, setError] = useState('');

    useEffect(() => {
        fetchUnites();
    }, []);

    const fetchUnites = async () => {
        try {
            const response = await uniteService.getAll();
            setUnites(response.data);
        } catch (error) {
            setError(error.response?.data?.message || 'Erreur chargement unités');
        } finally {
            setLoading(false);
        }
    };

    const openModal = (mode, unite = null) => {
        setModalMode(mode);
        setSelectedUnite(unite);
        setFormData({ nom: unite ? unite.nom : '' });
        setError('');
        setShowModal(true);
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            if (modalMode === 'create') {
                await uniteService.create(formData);
            } else {
                await uniteService.update(selectedUnite.id, formData);
            }
            setShowModal(false);
            fetchUnites();
        } catch (error) {
            setError(error.response?.data?.message || 'Erreur opération');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Supprimer cette unité ?')) return;
        try {
            await uniteService.delete(id);
            fetchUnites();
        } catch (error) {
            setError(error.response?.data?.message || 'Erreur suppression');
        }
    };

    if (loading) return <div className="flex min-h-screen items-center justify-center">Chargement...</div>;

    return (
        <div className="flex min-h-screen bg-gray-50">
            <Sidebar />

            <div className="flex-1 ml-64 pt-20">
                <Navbar />

                <main className="p-8">
                    <div className="flex justify-between items-center mb-8">
                        <h1 className="text-3xl font-bold text-gray-900">Gestion des Unités Militaires</h1>
                        <Button onClick={() => openModal('create')}>+ Nouvelle Unité</Button>
                    </div>

                    {error && <div className="mb-4 p-4 bg-red-100 text-red-800 rounded">{error}</div>}

                    <Card>
                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs uppercase">Nom de l'unité</th>
                                    <th className="px-6 py-3 text-right text-xs uppercase">Actions</th>
                                </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-200">
                                {unites.map(u => (
                                    <tr key={u.id}>
                                        <td className="px-6 py-4">{u.nom}</td>
                                        <td className="px-6 py-4 text-right">
                                            <button onClick={() => openModal('edit', u)} className="text-blue-600 mr-3 hover:underline">Modifier</button>
                                            <button onClick={() => handleDelete(u.id)} className="text-red-600 hover:underline">Supprimer</button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </Card>

                    {/* MODAL */}
                    {showModal && (
                        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
                            <Card className="w-full max-w-lg p-8">
                                <h2 className="text-2xl font-bold mb-6 text-gray-900">
                                    {modalMode === 'create' ? 'Créer une unité' : 'Modifier une unité'}
                                </h2>

                                {error && <p className="text-red-600 mb-4 font-medium">{error}</p>}

                                <form onSubmit={handleSubmit} className="space-y-5">
                                    <InputField
                                        label="Nom de l'unité *"
                                        name="nom"
                                        value={formData.nom}
                                        onChange={handleChange}
                                        required
                                    />

                                    <div className="flex gap-4 mt-8">
                                        <button
                                            type="button"
                                            onClick={() => setShowModal(false)}
                                            className="flex-1 px-6 py-3 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition font-medium"
                                        >
                                            Annuler
                                        </button>
                                        <button
                                            type="submit"
                                            className="flex-1 px-6 py-3 bg-military-navy text-white rounded-lg hover:bg-military-800 transition font-medium shadow-md"
                                        >
                                            {modalMode === 'create' ? 'Créer' : 'Modifier'}
                                        </button>
                                    </div>
                                </form>
                            </Card>
                        </div>
                    )}
                </main>
            </div>
        </div>
    );
};

export default UniteManagement;
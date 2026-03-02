import React, { useState, useEffect } from 'react';
import { userService } from '../services/userService';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import Card from '../components/Card';
import Button from '../components/Button';
import InputField from '../components/InputField';
import Badge from '../components/Badge';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [modalMode, setModalMode] = useState('create');
    const [selectedUser, setSelectedUser] = useState(null);
    const [formData, setFormData] = useState({
        nom: '',
        prenom: '',
        email: '',
        password: '',
        role: 'CANDIDAT',
    });
    const [error, setError] = useState('');

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            const response = await userService.getAll();
            setUsers(response.data);
        } catch (error) {
            setError(error.response?.data?.message || 'Erreur chargement utilisateurs');
        } finally {
            setLoading(false);
        }
    };

    const openModal = (mode, user = null) => {
        setModalMode(mode);
        setSelectedUser(user);
        if (user) {
            setFormData({
                nom: user.nom || '',
                prenom: user.prenom || '',
                email: user.email || '',
                password: '',
                role: user.role || 'CANDIDAT',
            });
        } else {
            setFormData({ nom: '', prenom: '', email: '', password: '', role: 'CANDIDAT' });
        }
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
                await userService.create(formData);
            } else {
                await userService.update(selectedUser.id, formData);
            }
            setShowModal(false);
            fetchUsers();
        } catch (error) {
            // FIX: apostrophe dans string → guillemets doubles
            setError(error.response?.data?.message || "Erreur lors de l'opération");
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Supprimer cet utilisateur ?')) return;
        try {
            await userService.delete(id);
            fetchUsers();
        } catch (error) {
            setError(error.response?.data?.message || 'Erreur suppression');
        }
    };

    const handleToggleStatus = async (id) => {
        try {
            await userService.toggleStatus(id);
            fetchUsers();
        } catch (error) {
            setError(error.response?.data?.message || 'Erreur changement statut');
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
                        <h1 className="text-3xl font-bold text-gray-900">Gestion des Utilisateurs</h1>
                        <Button onClick={() => openModal('create')}>+ Nouvel utilisateur</Button>
                    </div>

                    {error && <div className="mb-4 p-4 bg-red-100 text-red-800 rounded">{error}</div>}

                    <Card>
                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs uppercase">Nom</th>
                                    <th className="px-6 py-3 text-left text-xs uppercase">Email</th>
                                    <th className="px-6 py-3 text-left text-xs uppercase">Rôle</th>
                                    <th className="px-6 py-3 text-left text-xs uppercase">Statut</th>
                                    <th className="px-6 py-3 text-right text-xs uppercase">Actions</th>
                                </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-200">
                                {users.map(u => (
                                    <tr key={u.id}>
                                        <td className="px-6 py-4">{u.prenom} {u.nom}</td>
                                        <td className="px-6 py-4">{u.email}</td>
                                        <td className="px-6 py-4">
                                            <Badge variant="primary">{u.role}</Badge>
                                        </td>
                                        <td className="px-6 py-4">
                                            <Badge variant={u.isActive ? 'success' : 'danger'}>
                                                {u.isActive ? 'Actif' : 'Inactif'}
                                            </Badge>
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <button onClick={() => openModal('edit', u)} className="text-blue-600 mr-3 hover:underline">Modifier</button>
                                            <button onClick={() => handleToggleStatus(u.id)} className="mr-3 hover:underline">
                                                {u.isActive ? 'Désactiver' : 'Activer'}
                                            </button>
                                            <button onClick={() => handleDelete(u.id)} className="text-red-600 hover:underline">Supprimer</button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </Card>

                    {showModal && (
                        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
                            <Card className="w-full max-w-lg p-8">
                                <h2 className="text-2xl font-bold mb-6 text-gray-900">
                                    {modalMode === 'create' ? 'Créer un utilisateur' : 'Modifier un utilisateur'}
                                </h2>

                                {error && <p className="text-red-600 mb-4 font-medium">{error}</p>}

                                <form onSubmit={handleSubmit} className="space-y-5">
                                    <InputField label="Prénom *" name="prenom" value={formData.prenom} onChange={handleChange} required />
                                    <InputField label="Nom *" name="nom" value={formData.nom} onChange={handleChange} required />
                                    <InputField label="Email *" name="email" type="email" value={formData.email} onChange={handleChange} required />
                                    <InputField
                                        label={modalMode === 'create' ? 'Mot de passe *' : 'Nouveau mot de passe (optionnel)'}
                                        name="password"
                                        type="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        required={modalMode === 'create'}
                                    />
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Rôle *</label>
                                        <select
                                            name="role"
                                            value={formData.role}
                                            onChange={handleChange}
                                            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-military-navy focus:border-transparent outline-none"
                                            required
                                        >
                                            <option value="ADMIN">Admin</option>
                                            <option value="RH">RH</option>
                                            <option value="COMMANDANT">Commandant</option>
                                            <option value="CANDIDAT">Candidat</option>
                                        </select>
                                    </div>

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

export default UserManagement;
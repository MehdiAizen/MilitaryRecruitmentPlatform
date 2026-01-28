import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { candidatureAPI } from '../services/api';
import {
    ArrowLeft, Search, Filter, CheckCircle,
    XCircle, Eye, Calendar
} from 'lucide-react';

const CandidaturesList = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [candidatures, setCandidatures] = useState([]);
    const [filteredCandidatures, setFilteredCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [selectedCandidature, setSelectedCandidature] = useState(null);
    const [showValidationModal, setShowValidationModal] = useState(false);
    const [validationForm, setValidationForm] = useState({
        decision: '',
        commentaire: '',
    });

    useEffect(() => {
        fetchCandidatures();
    }, []);

    useEffect(() => {
        filterCandidatures();
    }, [searchTerm, statusFilter, candidatures]);

    const fetchCandidatures = async () => {
        try {
            const response = await candidatureAPI.getAll();
            setCandidatures(response.data);
        } catch (error) {
            console.error('Error fetching candidatures:', error);
        } finally {
            setLoading(false);
        }
    };

    const filterCandidatures = () => {
        let filtered = [...candidatures];

        // Filter by status
        if (statusFilter !== 'ALL') {
            filtered = filtered.filter((c) => c.statut === statusFilter);
        }

        // Filter by search term
        if (searchTerm) {
            filtered = filtered.filter(
                (c) =>
                    c.nom.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    c.prenom.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    c.cin.includes(searchTerm) ||
                    c.email.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        setFilteredCandidatures(filtered);
    };

    const handleValidate = async () => {
        if (!validationForm.decision || !validationForm.commentaire) {
            alert('Veuillez remplir tous les champs');
            return;
        }

        try {
            await candidatureAPI.validate(
                selectedCandidature.id,
                validationForm.decision,
                validationForm.commentaire,
                `${user.prenom} ${user.nom}`
            );

            setShowValidationModal(false);
            setValidationForm({ decision: '', commentaire: '' });
            fetchCandidatures();
        } catch (error) {
            alert('Erreur lors de la validation: ' + error.message);
        }
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

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('fr-FR');
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
                    <div className="flex items-center gap-4">
                        <button
                            onClick={() => navigate('/dashboard')}
                            className="p-2 hover:bg-gray-100 rounded-lg"
                        >
                            <ArrowLeft className="w-5 h-5" />
                        </button>
                        <div>
                            <h1 className="text-2xl font-bold text-gray-900">
                                Gestion des Candidatures
                            </h1>
                            <p className="text-sm text-gray-600 mt-1">
                                {filteredCandidatures.length} candidature(s)
                            </p>
                        </div>
                    </div>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Filters */}
                <div className="card mb-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {/* Search */}
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                            <input
                                type="text"
                                placeholder="Rechercher par nom, CIN, email..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="input-field pl-10"
                            />
                        </div>

                        {/* Status Filter */}
                        <div className="relative">
                            <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                            <select
                                value={statusFilter}
                                onChange={(e) => setStatusFilter(e.target.value)}
                                className="input-field pl-10"
                            >
                                <option value="ALL">Tous les statuts</option>
                                <option value="EN_ATTENTE">En attente</option>
                                <option value="EN_EXAMEN">En examen</option>
                                <option value="VALIDEE">Validées</option>
                                <option value="REJETEE">Rejetées</option>
                            </select>
                        </div>
                    </div>
                </div>

                {/* Candidatures Table */}
                <div className="card">
                    {filteredCandidatures.length === 0 ? (
                        <div className="text-center py-12">
                            <p className="text-gray-600">Aucune candidature trouvée</p>
                        </div>
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
                                        Contact
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                        Diplôme
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                        Unité
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                        Statut
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                                        Date
                                    </th>
                                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                                        Actions
                                    </th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {filteredCandidatures.map((candidature) => (
                                    <tr key={candidature.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="font-medium text-gray-900">
                                                {candidature.prenom} {candidature.nom}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                {formatDate(candidature.dateNaissance)}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                            {candidature.cin}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900">
                                                {candidature.email}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                {candidature.telephone}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="text-sm text-gray-900">
                                                {candidature.diplome}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                {candidature.etablissement}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                            {candidature.uniteChoisie}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                        <span className={getStatusBadge(candidature.statut)}>
                          {getStatusText(candidature.statut)}
                        </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                            <div className="flex items-center gap-1">
                                                <Calendar className="w-4 h-4" />
                                                {formatDate(candidature.createdAt)}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
                                            {user.role === 'COMMANDANT' &&
                                                candidature.statut === 'EN_ATTENTE' && (
                                                    <div className="flex justify-end gap-2">
                                                        <button
                                                            onClick={() => {
                                                                setSelectedCandidature(candidature);
                                                                setValidationForm({ decision: 'VALIDEE', commentaire: '' });
                                                                setShowValidationModal(true);
                                                            }}
                                                            className="p-2 text-green-600 hover:bg-green-50 rounded-lg"
                                                            title="Valider"
                                                        >
                                                            <CheckCircle className="w-5 h-5" />
                                                        </button>
                                                        <button
                                                            onClick={() => {
                                                                setSelectedCandidature(candidature);
                                                                setValidationForm({ decision: 'REJETEE', commentaire: '' });
                                                                setShowValidationModal(true);
                                                            }}
                                                            className="p-2 text-red-600 hover:bg-red-50 rounded-lg"
                                                            title="Rejeter"
                                                        >
                                                            <XCircle className="w-5 h-5" />
                                                        </button>
                                                    </div>
                                                )}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </main>

            {/* Validation Modal */}
            {showValidationModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-lg max-w-md w-full p-6">
                        <h3 className="text-xl font-bold text-gray-900 mb-4">
                            {validationForm.decision === 'VALIDEE' ? 'Valider' : 'Rejeter'} la candidature
                        </h3>

                        <div className="mb-4">
                            <p className="text-sm text-gray-600 mb-2">
                                Candidat: <span className="font-medium">
                  {selectedCandidature?.prenom} {selectedCandidature?.nom}
                </span>
                            </p>
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Commentaire *
                            </label>
                            <textarea
                                value={validationForm.commentaire}
                                onChange={(e) =>
                                    setValidationForm({ ...validationForm, commentaire: e.target.value })
                                }
                                className="input-field"
                                rows="4"
                                placeholder="Entrez votre commentaire..."
                                required
                            />
                        </div>

                        <div className="flex gap-3">
                            <button
                                onClick={() => {
                                    setShowValidationModal(false);
                                    setValidationForm({ decision: '', commentaire: '' });
                                }}
                                className="flex-1 btn-secondary"
                            >
                                Annuler
                            </button>
                            <button
                                onClick={handleValidate}
                                className={
                                    validationForm.decision === 'VALIDEE'
                                        ? 'flex-1 btn-success'
                                        : 'flex-1 btn-danger'
                                }
                            >
                                Confirmer
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CandidaturesList;
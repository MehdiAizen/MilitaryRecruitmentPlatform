// src/pages/CandidaturesList.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../hooks/useAuth.js';
import { candidatureService } from '../services/candidatureService';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import StatusBadge from '../components/StatusBadge';
import Card from '../components/Card';
import { Search, Filter, CheckCircle, XCircle, Calendar, Eye } from 'lucide-react';

const CandidaturesList = () => {
    const { user } = useAuth();
    const [candidatures, setCandidatures] = useState([]);
    const [filteredCandidatures, setFilteredCandidatures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [selectedCandidature, setSelectedCandidature] = useState(null);
    const [showValidationModal, setShowValidationModal] = useState(false);
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [validationForm, setValidationForm] = useState({ decision: '', commentaire: '' });

    // FIX : fetchCandidatures déclaré avant useEffect pour éviter le warning de dépendance
    const fetchCandidatures = useCallback(async () => {
        try {
            const response = await candidatureService.getAll();
            setCandidatures(response.data);
        } catch (error) {
            console.error('Erreur chargement candidatures:', error);
        } finally {
            setLoading(false);
        }
    }, []);

    // FIX : filterCandidatures dans useCallback pour pouvoir l'inclure dans les deps
    const filterCandidatures = useCallback(() => {
        let filtered = [...candidatures];

        if (statusFilter !== 'ALL') {
            filtered = filtered.filter((c) => c.statut === statusFilter);
        }

        if (searchTerm) {
            filtered = filtered.filter(
                (c) =>
                    c.nom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    c.prenom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    c.cin?.includes(searchTerm) ||
                    c.email?.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        setFilteredCandidatures(filtered);
    }, [candidatures, searchTerm, statusFilter]);

    useEffect(() => {
        // FIX : void pour éviter "Promise returned from fetchCandidatures is ignored"
        void fetchCandidatures();
    }, [fetchCandidatures]);

    // FIX : filterCandidatures inclus dans les deps (résout le warning missing dependency)
    useEffect(() => {
        filterCandidatures();
    }, [filterCandidatures]);

    const handleValidate = async () => {
        if (!validationForm.decision || !validationForm.commentaire) {
            alert('Veuillez remplir tous les champs');
            return;
        }

        try {
            // FIX : await ajouté (résout "Missing await for an async function call" :76)
            await candidatureService.validate(
                selectedCandidature.id,
                validationForm.decision,
                validationForm.commentaire
            );
            setShowValidationModal(false);
            setValidationForm({ decision: '', commentaire: '' });
            void fetchCandidatures();
        } catch (error) {
            alert('Erreur validation : ' + (error.response?.data?.message || error.message));
        }
    };

    // FIX : helper pour extraire le nom de l'unité (objet, pas string)
    const getUniteNom = (candidature) => {
        if (!candidature?.uniteChoisie) return 'Non spécifiée';
        if (typeof candidature.uniteChoisie === 'string') return candidature.uniteChoisie;
        return candidature.uniteChoisie.nom || 'Non spécifiée';
    };

    const formatDate = (dateString) => {
        if (!dateString) return '-';
        return new Date(dateString).toLocaleDateString('fr-FR');
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-military-navy"></div>
            </div>
        );
    }

    return (
        <div className="flex min-h-screen bg-gray-50">
            <Sidebar />

            <div className="flex-1 ml-64 pt-20">
                <Navbar />

                <main className="p-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-8">Gestion des Candidatures</h1>

                    <Card title="Filtres" className="mb-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
                    </Card>

                    <Card>
                        {filteredCandidatures.length === 0 ? (
                            <p className="text-center py-12 text-gray-600">Aucune candidature trouvée</p>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Candidat</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">CIN</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Contact</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Diplôme</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Unité</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Statut</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
                                        <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
                                    </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                    {filteredCandidatures.map((c) => (
                                        <tr key={c.id} className="hover:bg-gray-50">
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className="font-medium text-gray-900">{c.prenom} {c.nom}</div>
                                                <div className="text-sm text-gray-500">{formatDate(c.dateNaissance)}</div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{c.cin}</td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className="text-sm text-gray-900">{c.email}</div>
                                                <div className="text-sm text-gray-500">{c.telephone}</div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className="text-sm text-gray-900">{c.diplome}</div>
                                                <div className="text-sm text-gray-500">{c.etablissement}</div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                                {getUniteNom(c)}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <StatusBadge statut={c.statut} />
                                            </td>
                                            {/* FIX :187 — c.createdAt et non "createdAt" standalone */}
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                                <div className="flex items-center gap-1">
                                                    <Calendar className="w-4 h-4" />
                                                    {formatDate(c.createdAt)}
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
                                                <div className="flex justify-end gap-2">
                                                    <button
                                                        onClick={() => {
                                                            setSelectedCandidature(c);
                                                            setShowDetailModal(true);
                                                        }}
                                                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg"
                                                        title="Voir détail"
                                                    >
                                                        <Eye className="w-5 h-5" />
                                                    </button>
                                                    {user.role === 'COMMANDANT' && (c.statut === 'EN_ATTENTE' || c.statut === 'EN_EXAMEN') && (
                                                        <>
                                                            <button
                                                                onClick={() => {
                                                                    setSelectedCandidature(c);
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
                                                                    setSelectedCandidature(c);
                                                                    setValidationForm({ decision: 'REJETEE', commentaire: '' });
                                                                    setShowValidationModal(true);
                                                                }}
                                                                className="p-2 text-red-600 hover:bg-red-50 rounded-lg"
                                                                title="Rejeter"
                                                            >
                                                                <XCircle className="w-5 h-5" />
                                                            </button>
                                                        </>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </Card>
                </main>

                {/* Modal validation */}
                {showValidationModal && selectedCandidature && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                        <div className="bg-white rounded-lg max-w-md w-full p-6">
                            <h3 className="text-xl font-bold text-gray-900 mb-4">
                                {validationForm.decision === 'VALIDEE' ? 'Valider' : 'Rejeter'} la candidature
                            </h3>
                            <p className="text-sm text-gray-600 mb-2">
                                Candidat : <span className="font-medium">{selectedCandidature.prenom} {selectedCandidature.nom}</span>
                            </p>
                            <p className="text-sm text-gray-600 mb-4">
                                Unité : <span className="font-medium">{getUniteNom(selectedCandidature)}</span>
                            </p>
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-gray-700 mb-2">Commentaire *</label>
                                <textarea
                                    value={validationForm.commentaire}
                                    onChange={(e) => setValidationForm({ ...validationForm, commentaire: e.target.value })}
                                    className="input-field"
                                    rows="4"
                                    placeholder="Entrez votre commentaire..."
                                />
                            </div>
                            <div className="flex gap-3">
                                <button onClick={() => setShowValidationModal(false)} className="flex-1 btn-secondary">
                                    Annuler
                                </button>
                                <button
                                    onClick={handleValidate}
                                    className={`flex-1 px-4 py-2 rounded-lg font-semibold text-white ${
                                        validationForm.decision === 'VALIDEE'
                                            ? 'bg-green-600 hover:bg-green-700'
                                            : 'bg-red-600 hover:bg-red-700'
                                    }`}
                                >
                                    Confirmer
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* Modal détail */}
                {showDetailModal && selectedCandidature && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                        <div className="bg-white rounded-lg max-w-lg w-full p-6 max-h-[90vh] overflow-y-auto">
                            <h3 className="text-xl font-bold text-gray-900 mb-4">
                                Détail — {selectedCandidature.prenom} {selectedCandidature.nom}
                            </h3>
                            <div className="grid grid-cols-2 gap-3 text-sm">
                                <span className="text-gray-500">CIN</span>
                                <span className="font-medium">{selectedCandidature.cin}</span>

                                <span className="text-gray-500">Date naissance</span>
                                <span className="font-medium">{formatDate(selectedCandidature.dateNaissance)}</span>

                                <span className="text-gray-500">Email</span>
                                <span className="font-medium">{selectedCandidature.email}</span>

                                <span className="text-gray-500">Téléphone</span>
                                <span className="font-medium">{selectedCandidature.telephone}</span>

                                <span className="text-gray-500">Diplôme</span>
                                <span className="font-medium">{selectedCandidature.diplome}</span>

                                <span className="text-gray-500">Établissement</span>
                                <span className="font-medium">{selectedCandidature.etablissement}</span>

                                <span className="text-gray-500">Unité souhaitée</span>
                                <span className="font-medium">{getUniteNom(selectedCandidature)}</span>

                                <span className="text-gray-500">Statut</span>
                                <StatusBadge statut={selectedCandidature.statut} />

                                {/* FIX :321/:324 — selectedCandidature.validePar et non "validePar" standalone */}
                                {selectedCandidature.validePar && (
                                    <>
                                        <span className="text-gray-500">Validé par</span>
                                        <span className="font-medium">{selectedCandidature.validePar}</span>
                                    </>
                                )}

                                {/* FIX :327 — selectedCandidature.commentaireValidation et non "commentaireValidation" standalone */}
                                {selectedCandidature.commentaireValidation && (
                                    <>
                                        <span className="text-gray-500">Commentaire</span>
                                        <span className="font-medium">{selectedCandidature.commentaireValidation}</span>
                                    </>
                                )}
                            </div>
                            <button
                                onClick={() => setShowDetailModal(false)}
                                className="mt-6 w-full btn-secondary"
                            >
                                Fermer
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default CandidaturesList;
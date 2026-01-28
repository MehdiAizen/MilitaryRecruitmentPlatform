import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { candidatureAPI } from '../services/api';
import { Shield, CheckCircle, AlertCircle } from 'lucide-react';

const CandidatureForm = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState('');

    const [formData, setFormData] = useState({
        nom: '',
        prenom: '',
        cin: '',
        dateNaissance: '',
        email: '',
        telephone: '',
        diplome: '',
        etablissement: '',
        uniteChoisie: '',
    });

    const unites = [
        'Forces Spéciales',
        'Armée de Terre',
        'Armée de l\'Air',
        'Marine Nationale',
        'Garde Nationale',
    ];

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await candidatureAPI.create(formData);
            setSuccess(true);
            setTimeout(() => {
                navigate('/login');
            }, 3000);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Une erreur est survenue. Veuillez réessayer.'
            );
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
                <div className="card max-w-md text-center">
                    <div className="flex justify-center mb-4">
                        <div className="bg-green-100 p-4 rounded-full">
                            <CheckCircle className="w-12 h-12 text-green-600" />
                        </div>
                    </div>
                    <h2 className="text-2xl font-bold text-gray-900 mb-4">
                        Candidature Envoyée !
                    </h2>
                    <p className="text-gray-600 mb-6">
                        Votre candidature a été enregistrée avec succès. Vous recevrez une
                        notification par email concernant le traitement de votre dossier.
                    </p>
                    <p className="text-sm text-gray-500">
                        Redirection vers la page de connexion...
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-3xl mx-auto">
                {/* Header */}
                <div className="text-center mb-8">
                    <div className="flex justify-center mb-4">
                        <div className="bg-primary-600 p-4 rounded-full">
                            <Shield className="w-10 h-10 text-white" />
                        </div>
                    </div>
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">
                        Formulaire de Candidature
                    </h1>
                    <p className="text-gray-600">
                        Ministère de la Défense Nationale - Recrutement
                    </p>
                </div>

                <div className="card">
                    {error && (
                        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                            <p className="text-red-800 text-sm">{error}</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Informations Personnelles */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b">
                                Informations Personnelles
                            </h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Nom *
                                    </label>
                                    <input
                                        type="text"
                                        name="nom"
                                        value={formData.nom}
                                        onChange={handleChange}
                                        className="input-field"
                                        required
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Prénom *
                                    </label>
                                    <input
                                        type="text"
                                        name="prenom"
                                        value={formData.prenom}
                                        onChange={handleChange}
                                        className="input-field"
                                        required
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        CIN *
                                    </label>
                                    <input
                                        type="text"
                                        name="cin"
                                        value={formData.cin}
                                        onChange={handleChange}
                                        className="input-field"
                                        placeholder="12345678"
                                        maxLength="8"
                                        required
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Date de Naissance *
                                    </label>
                                    <input
                                        type="date"
                                        name="dateNaissance"
                                        value={formData.dateNaissance}
                                        onChange={handleChange}
                                        className="input-field"
                                        required
                                    />
                                </div>
                            </div>
                        </div>

                        {/* Contact */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b">
                                Contact
                            </h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Email *
                                    </label>
                                    <input
                                        type="email"
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        className="input-field"
                                        placeholder="votre.email@example.tn"
                                        required
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Téléphone *
                                    </label>
                                    <input
                                        type="tel"
                                        name="telephone"
                                        value={formData.telephone}
                                        onChange={handleChange}
                                        className="input-field"
                                        placeholder="+216 XX XXX XXX"
                                        required
                                    />
                                </div>
                            </div>
                        </div>

                        {/* Formation */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b">
                                Formation Académique
                            </h3>
                            <div className="grid grid-cols-1 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Diplôme *
                                    </label>
                                    <input
                                        type="text"
                                        name="diplome"
                                        value={formData.diplome}
                                        onChange={handleChange}
                                        className="input-field"
                                        placeholder="Ex: Licence en Informatique"
                                        required
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Établissement *
                                    </label>
                                    <input
                                        type="text"
                                        name="etablissement"
                                        value={formData.etablissement}
                                        onChange={handleChange}
                                        className="input-field"
                                        placeholder="Ex: Université de Tunis"
                                        required
                                    />
                                </div>
                            </div>
                        </div>

                        {/* Unité Choisie */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b">
                                Affectation Souhaitée
                            </h3>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Unité Choisie *
                                </label>
                                <select
                                    name="uniteChoisie"
                                    value={formData.uniteChoisie}
                                    onChange={handleChange}
                                    className="input-field"
                                    required
                                >
                                    <option value="">-- Sélectionnez une unité --</option>
                                    {unites.map((unite) => (
                                        <option key={unite} value={unite}>
                                            {unite}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {/* Buttons */}
                        <div className="flex gap-4 pt-6">
                            <Link to="/login" className="flex-1 btn-secondary text-center">
                                Annuler
                            </Link>
                            <button
                                type="submit"
                                disabled={loading}
                                className="flex-1 btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {loading ? 'Envoi en cours...' : 'Soumettre ma candidature'}
                            </button>
                        </div>
                    </form>

                    <div className="mt-6 pt-6 border-t border-gray-200 text-center">
                        <p className="text-sm text-gray-600">
                            Déjà inscrit ?{' '}
                            <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
                                Se connecter
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CandidatureForm;
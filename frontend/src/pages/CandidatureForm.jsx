import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import { candidatureService } from '../services/candidatureService';
import { uniteService } from '../services/uniteService';
import { Shield, CheckCircle, AlertCircle, Upload, Loader2 } from 'lucide-react';
import InputField from '../components/InputField';
import Card from '../components/Card';

const CandidatureForm = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState('');
    const [unites, setUnites] = useState([]);
    const [loadingUnites, setLoadingUnites] = useState(true);

    const [uploadingFile, setUploadingFile] = useState(false);
    const [fileName, setFileName] = useState('');

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
        documentUrls: [],
    });

    useEffect(() => {
        const fetchUnites = async () => {
            setLoadingUnites(true);
            try {
                const response = await uniteService.getNoms();
                if (Array.isArray(response.data)) {
                    setUnites(response.data);
                } else {
                    console.error('Format de données incorrect');
                    setError('Impossible de charger les unités (format incorrect)');
                }
            } catch (err) {
                console.error('Erreur chargement unités:', err);
                // Message explicite pour l'utilisateur
                setError("Erreur critique : Impossible de charger la liste des unités. Vérifiez que le backend est lancé et que SecurityConfig.java autorise '/unites/noms'.");
            } finally {
                setLoadingUnites(false);
            }
        };
        fetchUnites();
    }, []);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleFileUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        if (file.size > 10 * 1024 * 1024) {
            alert("Le fichier est trop volumineux (Max 10MB)");
            return;
        }

        const uploadFormData = new FormData();
        uploadFormData.append("file", file);

        setUploadingFile(true);
        setFileName(file.name);

        try {
            const response = await api.post('/files/upload', uploadFormData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });

            setFormData(prev => ({
                ...prev,
                documentUrls: [response.data.url]
            }));

        } catch (error) {
            console.error("Upload failed", error);
            alert("Erreur lors de l'upload. Vérifiez que vous êtes connecté ou que l'endpoint est public.");
            setFileName('');
        } finally {
            setUploadingFile(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await candidatureService.create(formData);
            setSuccess(true);
            setTimeout(() => navigate('/login'), 3000);
        } catch (err) {
            setError(err.response?.data?.message || "Erreur lors de l'envoi");
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
                <Card className="max-w-md text-center">
                    <div className="flex justify-center mb-4">
                        <div className="bg-green-100 p-4 rounded-full">
                            <CheckCircle className="w-12 h-12 text-green-600" />
                        </div>
                    </div>
                    <h2 className="text-2xl font-bold text-gray-900 mb-4">Candidature Envoyée !</h2>
                    <p className="text-gray-600 mb-6">
                        Votre candidature a été enregistrée avec succès.
                    </p>
                    <p className="text-sm text-gray-500">Redirection...</p>
                </Card>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-3xl mx-auto">
                <div className="text-center mb-8">
                    <Shield className="w-12 h-12 text-military-navy mx-auto mb-2" />
                    <h1 className="text-3xl font-bold text-gray-900">Formulaire de Candidature</h1>
                </div>

                <Card>
                    {error && (
                        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                            <p className="text-red-800 text-sm">{error}</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Informations Personnelles */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b">Informations Personnelles</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <InputField label="Nom *" name="nom" value={formData.nom} onChange={handleChange} required />
                                <InputField label="Prénom *" name="prenom" value={formData.prenom} onChange={handleChange} required />
                                <InputField label="CIN *" name="cin" value={formData.cin} onChange={handleChange} required maxLength="8" />
                                <InputField label="Date de Naissance *" type="date" name="dateNaissance" value={formData.dateNaissance} onChange={handleChange} required />
                            </div>
                        </div>

                        {/* Contact */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b">Contact</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <InputField label="Email *" type="email" name="email" value={formData.email} onChange={handleChange} required />
                                <InputField label="Téléphone *" type="tel" name="telephone" value={formData.telephone} onChange={handleChange} required />
                            </div>
                        </div>

                        {/* Formation */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b">Formation</h3>
                            <div className="grid grid-cols-1 gap-4">
                                <InputField label="Diplôme *" name="diplome" value={formData.diplome} onChange={handleChange} required />
                                <InputField label="Établissement *" name="etablissement" value={formData.etablissement} onChange={handleChange} required />
                            </div>
                        </div>

                        {/* Upload - AMÉLIORÉ */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b">Pièces Jointes</h3>
                            <label className="flex flex-col items-center justify-center w-full h-40 border-2 border-gray-300 border-dashed rounded-lg cursor-pointer bg-gray-50 hover:bg-gray-100 transition">
                                <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                    {uploadingFile ? (
                                        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
                                    ) : (
                                        <Upload className="w-8 h-8 text-gray-400 mb-2" />
                                    )}
                                    <p className="mb-2 text-sm text-gray-500">
                                        <span className="font-semibold">{fileName ? fileName : "Cliquez pour uploader"}</span>
                                    </p>
                                    <p className="text-xs text-gray-500">PDF, JPG, PNG (MAX. 10MB)</p>
                                </div>
                                <input type="file" className="hidden" accept=".pdf,.jpg,.jpeg,.png" onChange={handleFileUpload} />
                            </label>
                            {formData.documentUrls.length > 0 && (
                                <p className="text-sm text-green-600 mt-2 text-center">✓ Fichier prêt</p>
                            )}
                        </div>

                        {/* Unité - AMÉLIORÉ */}
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b">Affectation</h3>
                            <label className="block text-sm font-medium text-gray-700 mb-2">Unité Choisie *</label>
                            {loadingUnites ? (
                                <div className="flex items-center gap-2 text-gray-500">
                                    <Loader2 className="w-4 h-4 animate-spin" />
                                    Chargement...
                                </div>
                            ) : (
                                <select
                                    name="uniteChoisie"
                                    value={formData.uniteChoisie}
                                    onChange={handleChange}
                                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-military-navy"
                                    required
                                    disabled={unites.length === 0}
                                >
                                    <option value="">
                                        {unites.length === 0 ? "-- Erreur de chargement --" : "-- Sélectionnez une unité --"}
                                    </option>
                                    {unites.map((nom, index) => (
                                        <option key={index} value={nom}>{nom}</option>
                                    ))}
                                </select>
                            )}
                        </div>

                        <div className="flex gap-4 pt-6">
                            <Link to="/login" className="flex-1 text-center bg-gray-200 py-3 rounded-lg font-medium">
                                Annuler
                            </Link>
                            <button
                                type="submit"
                                disabled={loading || unites.length === 0}
                                className="flex-1 bg-military-navy text-white py-3 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {loading ? 'Envoi...' : 'Soumettre'}
                            </button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
};

export default CandidatureForm;
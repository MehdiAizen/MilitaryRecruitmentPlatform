// src/pages/Register.jsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/authService';
import { Shield, AlertCircle } from 'lucide-react';
import InputField from '../components/InputField';
import Card from '../components/Card';

const Register = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        nom: '',
        prenom: '',
        email: '',
        password: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await authService.register(formData);
            navigate('/login');
        } catch (err) {
            // FIX: apostrophe dans string → guillemets doubles
            setError(err.response?.data?.message || "Erreur lors de l'inscription");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-military-900 via-military-800 to-military-700 px-4">
            <div className="max-w-md w-full">
                <div className="text-center mb-8">
                    <div className="flex justify-center mb-4">
                        <Shield className="w-12 h-12 text-white" />
                    </div>
                    <h1 className="text-3xl font-bold text-white mb-2">Inscription Candidat</h1>
                    <p className="text-gray-300">Ministère de la Défense Nationale</p>
                </div>

                <Card>
                    {error && (
                        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                            <p className="text-red-800 text-sm">{error}</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <InputField
                                label="Prénom"
                                name="prenom"
                                value={formData.prenom}
                                onChange={handleChange}
                                required
                            />
                            <InputField
                                label="Nom"
                                name="nom"
                                value={formData.nom}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <InputField
                            label="Email"
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                        />

                        <InputField
                            label="Mot de passe"
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />

                        <button type="submit" disabled={loading} className="w-full btn-primary py-3">
                            {loading ? 'Inscription...' : "S'inscrire"}
                        </button>
                    </form>

                    <div className="mt-6 text-center text-sm text-gray-600">
                        Déjà inscrit ?{' '}
                        <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
                            Se connecter
                        </Link>
                    </div>
                </Card>
            </div>
        </div>
    );
};

export default Register;
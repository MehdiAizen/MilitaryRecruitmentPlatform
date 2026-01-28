import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Mail, Lock, AlertCircle } from 'lucide-react';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        const result = await login(email, password);

        if (result.success) {
            navigate('/dashboard');
        } else {
            setError(result.message);
        }
        setLoading(false);
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-military-900 via-military-800 to-military-700 px-4">
            <div className="max-w-md w-full">
                {/* Logo & Title */}
                <div className="text-center mb-8">
                    <div className="flex justify-center mb-4">
                        <div className="bg-primary-600 p-4 rounded-full">
                            <Shield className="w-12 h-12 text-white" />
                        </div>
                    </div>
                    <h1 className="text-3xl font-bold text-white mb-2">
                        Plateforme de Recrutement
                    </h1>
                    <p className="text-gray-300">Ministère de la Défense Nationale</p>
                </div>

                {/* Login Form */}
                <div className="card">
                    <h2 className="text-2xl font-bold text-gray-900 mb-6">Connexion</h2>

                    {error && (
                        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                            <p className="text-red-800 text-sm">{error}</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Email
                            </label>
                            <div className="relative">
                                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                                <input
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    className="input-field pl-10"
                                    placeholder="votre.email@defense.tn"
                                    required
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Mot de passe
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                                <input
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="input-field pl-10"
                                    placeholder="••••••••"
                                    required
                                />
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full btn-primary py-3 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {loading ? 'Connexion...' : 'Se connecter'}
                        </button>
                    </form>

                    <div className="mt-6 pt-6 border-t border-gray-200">
                        <p className="text-center text-sm text-gray-600">
                            Candidat externe ?{' '}
                            <Link to="/candidature" className="text-primary-600 hover:text-primary-700 font-medium">
                                Postuler ici
                            </Link>
                        </p>
                    </div>

                    {/* Demo Credentials */}
                    <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                        <p className="text-xs text-gray-600 font-semibold mb-2">Comptes de test:</p>
                        <div className="space-y-1 text-xs text-gray-600">
                            <p><span className="font-medium">Admin:</span> admin@defense-test.tn / admin123</p>
                            <p><span className="font-medium">RH:</span> rh@defense-test.tn / rh123</p>
                            <p><span className="font-medium">CMD:</span> cmd@defense-test.tn / cmd123</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
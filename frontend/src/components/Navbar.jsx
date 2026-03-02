import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { LogOut, Shield } from 'lucide-react';

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    if (!user) return null;

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <header className="fixed top-0 left-64 right-0 z-40 bg-military-navy text-white shadow-xl border-b border-military-olive/40">
            <div className="max-w-full mx-auto px-8">
                <div className="flex justify-between items-center h-20">
                    {/* Logo + Titre avec symbole bleu vif */}
                    <div className="flex items-center gap-4">
                        <div className="relative">
                            <Shield className="w-10 h-10 text-blue-400 drop-shadow-lg" />
                            <div className="absolute inset-0 bg-blue-500/20 rounded-full blur-xl animate-pulse"></div>
                        </div>
                        <h1 className="text-2xl font-bold tracking-tight text-white">Recrutement Militaire</h1>
                    </div>

                    {/* Profil + Déconnexion */}
                    <div className="flex items-center gap-6">
                        <div className="flex items-center gap-3">


                        </div>

                        <button
                            onClick={handleLogout}
                            className="flex items-center gap-2 px-6 py-2.5 bg-red-700 hover:bg-red-800 rounded-lg transition-all duration-300 shadow-md hover:shadow-lg font-medium text-white"
                        >
                            <LogOut className="w-5 h-5" />
                            Déconnexion
                        </button>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Navbar;
import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
    LayoutDashboard,
    FileText,
    ListChecks,
    BarChart3,
    Users,
    Building,
} from 'lucide-react';

const Sidebar = () => {
    const { user } = useAuth();

    if (!user) return null;

    const menuItems = [
        { path: '/dashboard', label: 'Tableau de bord', icon: LayoutDashboard, roles: ['ADMIN', 'RH', 'COMMANDANT', 'CANDIDAT'] },
        { path: '/my-candidatures', label: 'Mes Candidatures', icon: FileText, roles: ['CANDIDAT'] },
        { path: '/candidatures', label: 'Toutes les Candidatures', icon: ListChecks, roles: ['ADMIN', 'RH', 'COMMANDANT'] },
        { path: '/stats', label: 'Statistiques', icon: BarChart3, roles: ['ADMIN'] },
        { path: '/users', label: 'Gestion Utilisateurs', icon: Users, roles: ['ADMIN'] },
        { path: '/unites', label: 'Gestion Unités', icon: Building, roles: ['ADMIN'] },
    ];

    const filteredItems = menuItems.filter(item => item.roles.includes(user.role));

    return (
        <aside className="w-64 bg-military-navy text-white h-screen fixed left-0 top-0 pt-20 overflow-y-auto border-r border-military-olive/30 shadow-xl">
            <div className="px-4 py-6">
                <div className="flex items-center gap-3 mb-8 px-2">
                    <div className="w-10 h-10 rounded-full bg-military-olive flex items-center justify-center font-bold text-white">
                        {user.prenom?.[0]}{user.nom?.[0]}
                    </div>
                    <div>
                        <p className="font-semibold">{user.prenom} {user.nom}</p>
                        <p className="text-xs text-gray-400">{user.role}</p>
                    </div>
                </div>

                <nav className="space-y-1">
                    {filteredItems.map(item => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) =>
                                `flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 group ${
                                    isActive
                                        ? 'bg-military-olive/20 text-white font-medium border-l-4 border-military-olive'
                                        : 'text-gray-300 hover:bg-military-olive/10 hover:text-white'
                                }`
                            }
                        >
                            <item.icon className="w-5 h-5 transition-transform group-hover:scale-110" />
                            <span className="text-sm">{item.label}</span>
                        </NavLink>
                    ))}
                </nav>
            </div>

            <div className="absolute bottom-6 left-0 right-0 px-6 text-xs text-gray-500 text-center">
                Plateforme © 2025
            </div>
        </aside>
    );
};

export default Sidebar;
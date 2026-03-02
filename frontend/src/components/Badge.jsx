// src/components/Badge.jsx
import React from 'react';

const Badge = ({ children, variant = 'default', className = '' }) => {
    const variants = {
        default: 'bg-gray-100 text-gray-800 border border-gray-300',
        success: 'bg-green-100 text-green-800 border border-green-300',
        warning: 'bg-yellow-100 text-yellow-800 border border-yellow-300',
        danger: 'bg-red-100 text-red-800 border border-red-300',
        info: 'bg-blue-100 text-blue-800 border border-blue-300',
        primary: 'bg-military-navy text-white border border-military-olive',
    };

    return (
        <span
            className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${variants[variant]} ${className}`}
        >
      {children}
    </span>
    );
};

export default Badge;
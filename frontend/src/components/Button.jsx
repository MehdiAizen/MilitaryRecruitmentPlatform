// src/components/Button.jsx
import React from 'react';

const Button = ({
                    children,
                    variant = 'primary',
                    size = 'md',
                    disabled = false,
                    className = '',
                    ...props
                }) => {
    const base = 'font-semibold rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 transition';

    const variants = {
        primary: 'bg-military-navy text-white hover:bg-military-800 focus:ring-military-olive',
        secondary: 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 focus:ring-gray-500',
        success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
        danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
        outline: 'bg-transparent border border-military-navy text-military-navy hover:bg-military-navy hover:text-white',
    };

    const sizes = {
        sm: 'px-3 py-1.5 text-sm',
        md: 'px-4 py-2.5 text-base',
        lg: 'px-6 py-3 text-lg',
    };

    return (
        <button
            disabled={disabled}
            className={`${base} ${variants[variant]} ${sizes[size]} ${disabled ? 'opacity-50 cursor-not-allowed' : ''} ${className}`}
            {...props}
        >
            {children}
        </button>
    );
};

export default Button;
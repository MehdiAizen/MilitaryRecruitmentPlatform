// src/components/Card.jsx
import React from 'react';

const Card = ({ children, title, className = '' }) => {
    return (
        <div className={`bg-white rounded-xl shadow-md border border-gray-200 p-6 ${className}`}>
            {title && (
                <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b border-gray-200">
                    {title}
                </h3>
            )}
            {children}
        </div>
    );
};

export default Card;
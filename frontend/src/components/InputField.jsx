// src/components/InputField.jsx
import React from 'react';

const InputField = ({
                        label,
                        type = 'text',
                        name,
                        value,
                        onChange,
                        placeholder,
                        required = false,
                        error,
                        className = '',
                        ...props
                    }) => {
    return (
        <div className={className}>
            {label && (
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    {label} {required && <span className="text-red-500">*</span>}
                </label>
            )}
            <input
                type={type}
                name={name}
                value={value}
                onChange={onChange}
                placeholder={placeholder}
                required={required}
                className={`w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-military-navy focus:border-transparent outline-none bg-white ${
                    error ? 'border-red-500' : ''
                }`}
                {...props}
            />
            {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
        </div>
    );
};

export default InputField;
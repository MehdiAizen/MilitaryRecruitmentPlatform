/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                'military-navy': '#1e3a5f',
                'military-olive': '#556b2f',
                'military-khaki': '#c3b091',
                'military-sand': '#e1c16e',
                'military-camo-green': '#4d774e',
                'military-steel': '#4682b4',
                'primary-600': '#1e3a5f',
                'primary-700': '#0d2538',
                'primary-100': '#e8f4fd',
            },
        },
    },
    plugins: [],
}
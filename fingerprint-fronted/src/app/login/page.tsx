'use client';

import React, { useState } from 'react';

export default function LoginPage() {
    const [error, setError] = useState('');

    const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const formData = new FormData(e.currentTarget);
        const username = formData.get('usuario');
        const password = formData.get('password');

        try {
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const userData = await response.json();
                // Si llegamos aquí, la base de datos ya dijo que el usuario es válido
                localStorage.setItem('isLogged', 'true');
                localStorage.setItem('userRole', userData.rol);
                window.location.href = '/';
            } else {
                setError('Usuario o contraseña incorrectos en la base de datos');
            }
        } catch (err) {
            setError('Error: No hay conexión con el servidor (Spring Boot)');
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-slate-900 fixed inset-0">
            <form onSubmit={handleLogin} className="bg-white p-8 rounded-xl shadow-2xl w-96 space-y-4">
                <h2 className="text-2xl font-bold text-center text-gray-800">Acceso al Sistema</h2>
                {error && <p className="bg-red-100 text-red-700 p-2 rounded text-sm text-center">{error}</p>}

                <input name="usuario" type="text" placeholder="Usuario" className="w-full border p-3 rounded" required />
                <input name="password" type="password" placeholder="Contraseña" className="w-full border p-3 rounded" required />

                <button type="submit" className="w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700 transition-colors">
                    Entrar
                </button>
            </form>
        </div>
    );
}
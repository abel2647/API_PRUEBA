'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { EntrySelectorModal } from '@/components/EntrySelectorModal';

export default function LoginPage() {
    const router = useRouter();

    // ESTADOS
    const [error, setError] = useState('');
    const [showEntryModal, setShowEntryModal] = useState(false);
    const [mounted, setMounted] = useState(false);

    // useEffect para asegurar que el componente esté montado en el cliente
    useEffect(() => {
        setMounted(true);
    }, []);

    // --- FUNCIÓN PARA EMPLEADOS (COLUMNAS) ---
    const handleEntrySelection = (numero: number) => {
        setShowEntryModal(false);
        // IMPORTANTE: Asegúrate que tu carpeta se llame 'acceso-puerta'
        router.push(`/acceso-puerta?puerta=${numero}`);
    };

    // --- FUNCIÓN PARA ADMIN (LOGIN) ---
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
                localStorage.setItem('isLogged', 'true');
                localStorage.setItem('userRole', userData.rol);
                localStorage.setItem('username', userData.username);

                // El admin va a su panel de gestión
                router.push('/estudiantes');
            } else {
                setError('Usuario o contraseña incorrectos');
            }
        } catch (err) {
            setError('Error de conexión con el servidor');
        }
    };

    if (!mounted) return null;

    return (
        <div className="min-h-screen w-full bg-gradient-to-b from-orange-400 to-gray-300 flex items-start justify-center">

            <div className="bg-orange-50 my-[5vh] px-8 pt-16 pb-10 rounded-2xl shadow-xl w-[350px] flex flex-col items-center border border-orange-100">
                <div className="w-full flex justify-center mb-10">
                    <img
                        src="/Logo_ITO.webp"
                        alt="Logo ITO"
                        className="h-30 object-contain drop-shadow-sm"
                    />
                </div>

                <h4 className="text-lg font-normal text-center text-black mb-8 leading-snug">
                    Instituto Tecnológico de Oaxaca
                </h4>

                {/* FORMULARIO DE LOGIN */}
                <form onSubmit={handleLogin} className="w-full flex flex-col items-center">
                    {error && <p className="bg-red-100 text-red-700 p-2 rounded text-xs text-center w-full mb-4">{error}</p>}

                    <div className="w-full space-y-5 mb-6">
                        <input
                            name="usuario"
                            type="text"
                            placeholder="Usuario"
                            className="w-full bg-white border border-gray-400 py-2.5 px-6 rounded-full text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-sky-300 transition-all shadow-sm"
                            required
                        />
                        <input
                            name="password"
                            type="password"
                            placeholder="Contraseña"
                            className="w-full bg-white border border-gray-400 py-2.5 px-6 rounded-full text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-sky-300 transition-all shadow-sm"
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        className="px-8 py-2 bg-sky-400 text-black text-sm rounded-full font-medium hover:bg-sky-500 transition-colors shadow-md mb-6"
                    >
                        Iniciar sesión
                    </button>
                </form>

                {/* BOTÓN REGISTRO DE ENTRADA (FUERA DEL FORM) */}
                <button
                    type="button"
                    onClick={() => setShowEntryModal(true)}
                    className="mt-2 flex items-center gap-2 text-gray-600 cursor-pointer hover:text-black transition-colors group"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
                    </svg>
                    <span className="text-sm font-light">Registro de entrada</span>
                </button>
            </div>

            {/* EL MODAL */}
            <EntrySelectorModal
                isOpen={showEntryModal}
                onClose={() => setShowEntryModal(false)}
                onSelect={handleEntrySelection}
            />
        </div>
    );
}
'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation'; 
import { EntrySelectorModal } from '@/components/EntrySelectorModal'; 

export default function LoginPage() {
    const [error, setError] = useState('');
    const [showEntryModal, setShowEntryModal] = useState(false);
    
    const router = useRouter();

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
                
                // --- ESTA ES LA CLAVE ---
                localStorage.setItem('isLogged', 'true');
                localStorage.setItem('userRole', userData.rol); // <--- ESTO ACTIVA LA LÓGICA DE ROLES
                localStorage.setItem('username', userData.username); 

                window.location.href = '/';
            } else {
                setError('Usuario o contraseña incorrectos');
            }
        } catch (err) {
            setError('Error: No hay conexión con el servidor (Spring Boot)');
        }
    };

    const handleEntrySelection = (numero: number) => {
        console.log("Número de entrada seleccionado:", numero);
        localStorage.setItem('numeroEntrada', numero.toString());
        setShowEntryModal(false);
    };

    return (
        <div className="min-h-screen w-full bg-gradient-to-b from-orange-400 to-gray-300 flex items-start justify-center">
            
            <form 
                onSubmit={handleLogin} 
                className="bg-orange-50 my-[5vh] px-8 pt-16 pb-10 rounded-2xl shadow-xl w-[350px] flex flex-col items-center border border-orange-100"
            >
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
                
                {error && <p className="bg-red-100 text-red-700 p-2 rounded text-xs text-center w-full mb-4">{error}</p>}

                <div className="w-full space-y-5 mb-6">
                    <input 
                        name="usuario" 
                        type="text" 
                        placeholder="Usuario"
                        className="w-full bg-white border border-gray-400 py-2.5 px-6 rounded-full text-sm text-gray-700 placeholder:text-gray-400 placeholder:italic placeholder:font-light focus:outline-none focus:ring-2 focus:ring-sky-300 transition-all shadow-sm" 
                        required 
                    />
                    <input 
                        name="password" 
                        type="password" 
                        placeholder="Contraseña" 
                        className="w-full bg-white border border-gray-400 py-2.5 px-6 rounded-full text-sm text-gray-700 placeholder:text-gray-400 placeholder:italic placeholder:font-light focus:outline-none focus:ring-2 focus:ring-sky-300 transition-all shadow-sm" 
                        required 
                    />
                </div>

                <button 
                    type="submit" 
                    className="px-8 py-2 bg-sky-400 text-black text-sm rounded-full font-medium hover:bg-sky-500 transition-colors shadow-md shadow-gray-300 mb-6"
                >
                    Iniciar sesión
                </button>

                <div 
                    onClick={() => setShowEntryModal(true)} 
                    className="mt-2 flex items-center gap-2 text-gray-600 cursor-pointer hover:text-black transition-colors group"
                >
                    <div className="relative">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
                        </svg>
                    </div>
                    <span className="text-sm font-light text-gray-600">Registro de entrada</span>
                </div>

            </form>

            <EntrySelectorModal 
                isOpen={showEntryModal}
                onClose={() => setShowEntryModal(false)} // Pequeño ajuste aquí para evitar warnings de tipos
                onSelect={handleEntrySelection}
            />
        </div>
    );
}
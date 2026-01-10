'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { LogOut, ChevronDown } from 'lucide-react';

export const Navbar = () => {
    const router = useRouter();
    const [username, setUsername] = useState<string>('Usuario');
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    // Recuperar el nombre del usuario
    useEffect(() => {
        const storedName = localStorage.getItem('username');
        if (storedName) {
            setUsername(storedName);
        }
    }, []);

    // Función de cerrar sesión
    const handleLogout = () => {
        localStorage.removeItem('isLogged');
        localStorage.removeItem('username');
        localStorage.removeItem('userRole');
        router.replace('/login');
    };

    return (
        <header 
            className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 sticky top-0 z-30 shadow-sm"
            onClick={() => isMenuOpen && setIsMenuOpen(false)} // Cierra el menú si clicas fuera
        >
            {/* --- LADO IZQUIERDO: LOGO Y TEXTO --- */}
            <div className="flex items-center gap-4">
                <img 
                    src="/Logo_ITO.webp" 
                    alt="Logo ITO" 
                    className="h-10 w-auto object-contain"
                />
                <span className="font-bold text-gray-800 text-lg tracking-tight hidden md:block">
                    Instituto Tecnológico de Oaxaca
                </span>
                {/* Versión corta para móviles */}
                <span className="font-bold text-gray-800 text-lg tracking-tight md:hidden">
                    ITO
                </span>
            </div>

            {/* --- LADO DERECHO: USUARIO Y MENÚ --- */}
            <div className="relative">
                <button 
                    onClick={(e) => {
                        e.stopPropagation();
                        setIsMenuOpen(!isMenuOpen);
                    }}
                    className="flex items-center gap-2 hover:bg-gray-100 py-1.5 px-3 rounded-full transition-colors duration-200"
                >
                    {/* 1. Avatar (Ahora va primero) */}
                    <img 
                        src={`https://ui-avatars.com/api/?name=${username}&background=0ea5e9&color=fff&size=128&bold=true`} 
                        alt="Avatar"
                        className="h-8 w-8 rounded-full border border-gray-200 shadow-sm"
                    />

                    {/* 2. Nombre de usuario (En una sola línea, sin "En línea") */}
                    <span className="text-sm font-semibold text-gray-700 hidden sm:block">
                        {username}
                    </span>
                    
                    {/* 3. Flecha */}
                    <ChevronDown className={`h-4 w-4 text-gray-400 transition-transform duration-200 ${isMenuOpen ? 'rotate-180' : ''}`} />
                </button>

                {/* Dropdown Menu */}
                {isMenuOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-xl border border-gray-100 py-1 animate-in fade-in zoom-in-95 duration-200 origin-top-right">
                        {/* Mostrar nombre en móvil dentro del menú ya que arriba se oculta */}
                        <div className="px-4 py-2 border-b border-gray-100 sm:hidden">
                            <p className="text-sm font-semibold text-gray-800">{username}</p>
                        </div>
                        
                        <button
                            onClick={handleLogout}
                            className="w-full text-left px-4 py-3 text-sm text-red-600 hover:bg-red-50 flex items-center gap-2 transition-colors"
                        >
                            <LogOut className="h-4 w-4" />
                            Cerrar sesión
                        </button>
                    </div>
                )}
            </div>
        </header>
    );
};
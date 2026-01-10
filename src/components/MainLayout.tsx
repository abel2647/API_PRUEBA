'use client';

import React, { useState, useEffect } from 'react';
import { Sidebar } from './Sidebar';
import { Navbar } from './Navbar'; // <--- IMPORTAR NAVBAR
import { cn } from '@/lib/utils';
import { usePathname, useRouter } from 'next/navigation';

export default function MainLayout({ children }: { children: React.ReactNode }) {
    const [isCollapsed, setIsCollapsed] = useState(false);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);
    const pathname = usePathname();
    const router = useRouter();

    useEffect(() => {
        const checkAuth = () => {
            const logged = localStorage.getItem('isLogged');

            if (!logged && pathname !== '/login') {
                router.replace('/login');
                return;
            }
            if (logged) {
                setIsAuthenticated(true);
            }
            setLoading(false);
        };
        checkAuth();
    }, [pathname, router]);

    if (loading) {
        return <div className="h-screen w-screen flex items-center justify-center bg-gray-900 text-white">Cargando sistema...</div>;
    }

    if (pathname === '/login') {
        return <div className="min-h-screen bg-gray-900">{children}</div>;
    }

    if (!isAuthenticated) return null;

    return (
        <div className="flex min-h-screen bg-gray-50 text-slate-900">
            {/* Sidebar fija a la izquierda */}
            <Sidebar isCollapsed={isCollapsed} setIsCollapsed={setIsCollapsed} />

            {/* Contenedor principal */}
            <main className={cn(
                "flex-1 transition-all duration-300 min-h-screen flex flex-col",
                isCollapsed ? "ml-16" : "ml-64"
            )}>
                {/* --- AQUÍ PONEMOS LA NAVBAR --- */}
                <Navbar /> 
                
                {/* El contenido de las páginas va debajo */}
                <div className="flex-1 w-full">
                    {children}
                </div>
            </main>
        </div>
    );
}
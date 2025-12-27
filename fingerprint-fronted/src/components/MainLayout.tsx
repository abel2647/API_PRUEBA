'use client';

import React, { useState, useEffect } from 'react';
import { Sidebar } from './Sidebar'; // Asegúrate que Sidebar.tsx esté en la misma carpeta
import { cn } from '@/lib/utils';
import { usePathname, useRouter } from 'next/navigation';

export default function MainLayout({ children }: { children: React.ReactNode }) {
    const [isCollapsed, setIsCollapsed] = useState(false);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);
    const pathname = usePathname();
    const router = useRouter();

    useEffect(() => {
        // Verificamos si existe la sesión en el almacenamiento local
        const logged = localStorage.getItem('isLogged');

        if (!logged && pathname !== '/login') {
            // Si no está logueado y no está en la página de login, lo mandamos para allá
            setIsAuthenticated(false);
            router.push('/login');
        } else if (logged) {
            // Si está logueado, permitimos la entrada
            setIsAuthenticated(true);
        }
        setLoading(false);
    }, [pathname, router]);

    // Mientras verificamos la sesión, mostramos una pantalla de carga simple
    if (loading) {
        return (
            <div className="h-screen w-screen flex items-center justify-center bg-gray-900 text-white">
                Cargando sistema...
            </div>
        );
    }

    // Si estamos en el login, renderizamos la página "desnuda" (sin sidebar ni márgenes)
    if (pathname === '/login') {
        return <div className="min-h-screen bg-gray-900">{children}</div>;
    }

    return (
        <div className="flex min-h-screen bg-gray-50 text-slate-900">
            {/* 1. Sidebar: recibe el estado de colapso */}
            <Sidebar isCollapsed={isCollapsed} setIsCollapsed={setIsCollapsed} />

            {/* 2. Área de Contenido Principal: el margen izquierdo (ml)
                   cambia dinámicamente según el estado del Sidebar */}
            <main className={cn(
                "flex-1 transition-all duration-300 p-8 min-h-screen",
                isCollapsed ? "ml-16" : "ml-64"
            )}>
                {/* Solo mostramos el contenido si está autenticado */}
                {isAuthenticated ? children : null}
            </main>
        </div>
    );
}
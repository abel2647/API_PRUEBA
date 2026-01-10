'use client';

import React, { useState, useEffect } from 'react';
import { Sidebar } from './Sidebar';
import { Navbar } from './Navbar';
import { cn } from '@/lib/utils';
import { usePathname, useRouter } from 'next/navigation';
import { ShieldAlert } from 'lucide-react';

export default function MainLayout({ children }: { children: React.ReactNode }) {
    const [isCollapsed, setIsCollapsed] = useState(false);
    
    // Estados
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userRole, setUserRole] = useState<string>('');
    const [loading, setLoading] = useState(true);
    
    const pathname = usePathname();
    const router = useRouter();

    useEffect(() => {
        // Función para validar la sesión sin causar loops
        const validateSession = () => {
            const logged = localStorage.getItem('isLogged');
            const role = localStorage.getItem('userRole');

            if (logged === 'true') {
                // TRUCO PARA EVITAR EL BUCLE INFINITO:
                // Solo actualizamos el estado si es diferente al actual.
                // Si ya es true, no hacemos nada.
                setIsAuthenticated(prev => {
                    if (!prev) return true; 
                    return prev;
                });

                setUserRole(prev => {
                    if (prev !== role) return role || '';
                    return prev;
                });

            } else {
                // Si no está logueado y no estamos en login, mandar a login
                if (pathname !== '/login') {
                    router.replace('/login');
                }
            }
            
            // Finalizar carga
            setLoading(false);
        };

        validateSession();
        
    // Ejecutamos esto cuando cambia el pathname (navegación) o al montar
    }, [pathname, router]); 

    // --- RENDERIZADO ---

    // 1. Cargando
    if (loading) {
        return <div className="h-screen w-screen flex items-center justify-center bg-gray-900 text-white">Cargando sistema...</div>;
    }

    // 2. Login (Vista limpia)
    if (pathname === '/login') {
        return <div className="min-h-screen bg-gray-900">{children}</div>;
    }

    // 3. Protección final (Si cargó, no es login y no está autenticado, no mostrar nada)
    if (!isAuthenticated) return null;

    // --- LÓGICA DE BLOQUEO DE SECCIONES ---
    const restrictedRoutes = ['/estudiantes', '/visitantes', '/usuarios', '/consulta'];
    const isRestrictedAccess = restrictedRoutes.some(route => pathname.startsWith(route));
    
    // Si quiere entrar a zona restringida y NO es ADMIN
    const isBlocked = isRestrictedAccess && userRole !== 'ADMIN';

    return (
        <div className="flex min-h-screen bg-gray-50 text-slate-900">
            <Sidebar isCollapsed={isCollapsed} setIsCollapsed={setIsCollapsed} />

            <main className={cn("flex-1 transition-all duration-300 min-h-screen flex flex-col", isCollapsed ? "ml-16" : "ml-64")}>
                <Navbar /> 
                
                <div className="flex-1 w-full p-6">
                    {isBlocked ? (
                        <div className="h-full flex flex-col items-center justify-center text-center animate-in fade-in zoom-in duration-300">
                            <div className="bg-red-50 p-6 rounded-full mb-6">
                                <ShieldAlert className="h-20 w-20 text-red-500" />
                            </div>
                            <h2 className="text-3xl font-bold text-gray-800 mb-2">Acceso Restringido</h2>
                            <p className="text-gray-600 text-lg max-w-md">
                                Necesita un acceso de <strong>Administrador</strong> para ver el contenido de esta sección.
                            </p>
                        </div>
                    ) : (
                        children
                    )}
                </div>
            </main>
        </div>
    );
}
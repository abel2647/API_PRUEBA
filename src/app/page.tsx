'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { UserPlus, GraduationCap, ArrowRight, Send, Users, BarChart3 } from 'lucide-react';

export default function HomePage() {
    const router = useRouter();
    
    // --- ESTADOS ---
    const [username, setUsername] = useState<string>('');
    const [userRole, setUserRole] = useState<string>('');
    const [loading, setLoading] = useState(true);

    // --- EFECTO SEGURO (IGUAL QUE EN MAINLAYOUT) ---
    useEffect(() => {
        const storedName = localStorage.getItem('username');
        const storedRole = localStorage.getItem('userRole');

        // 1. Actualización segura de USERNAME
        if (storedName) {
            setUsername(prev => {
                // Si el valor ya es el mismo, devolvemos 'prev' para no renderizar
                if (prev !== storedName) return storedName;
                return prev;
            });
        }

        // 2. Actualización segura de ROL
        if (storedRole) {
            setUserRole(prev => {
                // Si el valor ya es el mismo, devolvemos 'prev' para no renderizar
                if (prev !== storedRole) return storedRole;
                return prev;
            });
        }

        setLoading(false);
    }, []); // Array vacío: Solo corre al montar, pero la lógica interna evita loops

    // Si está cargando, retornamos null
    if (loading) return null;

    // =========================================================
    // VISTA 1: EMPLEADO (Si NO es ADMIN)
    // =========================================================
    if (userRole !== 'ADMIN') {
        return (
            <div className="flex flex-col items-center justify-center min-h-[70vh] gap-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
                <div className="text-center space-y-4">
                    <h1 className="text-5xl font-extrabold text-gray-900 tracking-tight">
                        Bienvenido al sistema
                    </h1>
                    <p className="text-xl text-gray-500">
                        Hola, <span className="font-semibold text-blue-600">{username}</span>. ¿Listo para comenzar?
                    </p>
                </div>

                {/* BOTÓN PARA EMPLEADOS */}
                <button 
                    onClick={() => alert('Información enviada (Simulación)')}
                    className="group relative inline-flex items-center justify-center px-8 py-4 font-bold text-white transition-all duration-200 bg-blue-600 rounded-full hover:bg-blue-700 hover:shadow-lg hover:-translate-y-1 focus:outline-none ring-offset-2 focus:ring-2"
                >
                    <Send className="mr-3 h-5 w-5 group-hover:translate-x-1 transition-transform" />
                    Enviar información
                </button>
            </div>
        );
    }

    // =========================================================
    // VISTA 2: ADMINISTRADOR (Tarjetas de Módulos)
    // =========================================================
    
    const modules = [
        {
            title: 'Registro de Visitantes',
            description: 'Registrar nuevas visitas, generar pases con QR y validar accesos.',
            icon: <UserPlus className="h-12 w-12 text-blue-500" />,
            path: '/visitantes',
            color: 'hover:border-blue-500'
        },
        {
            title: 'Módulo de Estudiantes',
            description: 'Gestión de datos de alumnos, credenciales y registros escolares.',
            icon: <GraduationCap className="h-12 w-12 text-green-500" />,
            path: '/estudiantes',
            color: 'hover:border-green-500'
        },
        {
            title: 'Gestión de Usuarios',
            description: 'Administrar accesos y roles del sistema.',
            icon: <Users className="h-12 w-12 text-purple-500" />,
            path: '/usuarios/lista',
            color: 'hover:border-purple-500'
        },
        
    ];

    return (
        <div className="relative min-h-[80vh] flex flex-col items-center justify-start pt-16 space-y-8">
            
            <div className="text-center space-y-2">
                <h1 className="text-4xl font-bold text-gray-900">Panel de Administración</h1>
                <p className="text-gray-500 text-lg">
                    Bienvenido, <b>{username}</b>. Seleccione un módulo para gestionar.
                </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 w-full max-w-6xl px-4">
                {modules.map((module) => (
                    <Card
                        key={module.path}
                        className={`cursor-pointer transition-all duration-300 transform hover:-translate-y-2 border-2 ${module.color} shadow-md bg-white`}
                        onClick={() => router.push(module.path)}
                    >
                        <CardHeader className="flex flex-col items-center text-center space-y-4">
                            <div className="p-4 bg-gray-50 rounded-full">
                                {module.icon}
                            </div>
                            <div className="space-y-1">
                                <CardTitle className="text-xl font-bold">{module.title}</CardTitle>
                                <CardDescription className="text-sm px-4">
                                    {module.description}
                                </CardDescription>
                            </div>
                        </CardHeader>
                        <CardContent className="flex justify-center pb-6">
                            <div className="flex items-center text-sm font-semibold text-gray-400 group-hover:text-gray-900 transition-colors">
                                Entrar al módulo <ArrowRight className="ml-2 h-4 w-4" />
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
}
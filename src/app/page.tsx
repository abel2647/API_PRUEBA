'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { UserPlus, GraduationCap, ArrowRight, LogOut, ChevronDown } from 'lucide-react';

export default function HomePage() {
    const router = useRouter();
    const [username, setUsername] = useState<string>('Usuario');
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    useEffect(() => {
        const storedName = localStorage.getItem('username');
        if (storedName) {
            setUsername(storedName);
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('isLogged');
        localStorage.removeItem('username');
        localStorage.removeItem('userRole');
        router.replace('/login');
    };

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
        }
    ];

    return (
        <div className="relative min-h-[80vh] flex flex-col items-center justify-center space-y-8" onClick={() => isMenuOpen && setIsMenuOpen(false)}>
            
           
            {/* ---------------------------------------- */}

            <div className="text-center space-y-2 mt-16">
                <h1 className="text-4xl font-bold text-gray-900">Bienvenido al Sistema</h1>
                <p className="text-gray-500 text-lg">Seleccione el módulo que desea gestionar el día de hoy.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-4xl px-4">
                {modules.map((module) => (
                    <Card
                        key={module.path}
                        className={`cursor-pointer transition-all duration-300 transform hover:-translate-y-2 border-2 ${module.color} shadow-md`}
                        onClick={() => router.push(module.path)}
                    >
                        <CardHeader className="flex flex-col items-center text-center space-y-4">
                            <div className="p-4 bg-gray-50 rounded-full">
                                {module.icon}
                            </div>
                            <div className="space-y-1">
                                <CardTitle className="text-2xl">{module.title}</CardTitle>
                                <CardDescription className="text-sm px-4">
                                    {module.description}
                                </CardDescription>
                            </div>
                        </CardHeader>
                        <CardContent className="flex justify-center pb-6">
                            <div className="flex items-center text-sm font-semibold text-gray-400 group-hover:text-gray-900">
                                Entrar al módulo <ArrowRight className="ml-2 h-4 w-4" />
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
}
'use client';
import React from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { UserPlus, ShieldCheck, ArrowRight } from 'lucide-react';

export default function EstudiantesMenuPage() {
    const router = useRouter();

    const subModulos = [
        {
            title: 'Registro de Estudiantes',
            description: 'Alta de nuevos alumnos y captura de huella digital por primera vez.',
            icon: <UserPlus className="h-10 w-10 text-green-500" />,
            path: '/estudiantes/registro',
            color: 'hover:border-green-500'
        },
        {
            title: 'Verificación / Acceso',
            description: 'Validar identidad de alumnos ya registrados mediante biometría.',
            icon: <ShieldCheck className="h-10 w-10 text-blue-500" />,
            path: '/estudiantes/verificacion', // Lo dejaremos listo para después
            color: 'hover:border-blue-500'
        }
    ];

    return (
        <div className="flex flex-col items-center justify-center min-h-[70vh] space-y-10">
            <div className="text-center space-y-2">
                <h1 className="text-3xl font-bold text-slate-800">Módulo de Estudiantes</h1>
                <p className="text-slate-500 text-lg">¿Qué acción deseas realizar?</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-3xl px-6">
                {subModulos.map((mod) => (
                    <Card
                        key={mod.path}
                        className={`cursor-pointer transition-all duration-300 transform hover:-translate-y-2 border-2 border-transparent shadow-lg ${mod.color}`}
                        onClick={() => router.push(mod.path)}
                    >
                        <CardHeader className="text-center space-y-4">
                            <div className="mx-auto p-4 bg-slate-50 rounded-2xl w-fit">
                                {mod.icon}
                            </div>
                            <div>
                                <CardTitle className="text-xl">{mod.title}</CardTitle>
                                <CardDescription className="mt-2 text-sm text-slate-500">
                                    {mod.description}
                                </CardDescription>
                            </div>
                        </CardHeader>
                        <CardContent className="flex justify-center pb-6">
                            <span className="text-blue-600 font-semibold flex items-center text-sm">
                                Seleccionar <ArrowRight className="ml-2 h-4 w-4" />
                            </span>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </div>
    );
}
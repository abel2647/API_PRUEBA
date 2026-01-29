'use client';

import React from 'react';
// IMPORTANTE: Cambiamos el componente importado
import TablaGeneralAlumnos from '@/components/TablaGeneralAlumnos';
import { Users } from 'lucide-react';

export default function ConsultaAlumnosPage() {
    return (
        <div className="p-6 space-y-6 animate-in fade-in duration-500">
            {/* Encabezado de la Sección */}
            <div className="flex flex-col gap-1">
                <div className="flex items-center gap-3">
                    <div className="bg-indigo-600 p-2 rounded-lg text-white">
                        {/* Cambié el ícono para diferenciarlo del historial puro */}
                        <Users className="h-6 w-6" />
                    </div>
                    <h1 className="text-3xl font-black text-slate-900 uppercase tracking-tighter">
                        Historial de Alumnos
                    </h1>
                </div>
                <p className="text-slate-500 text-sm mt-2">
                </p>
            </div>

            {/* Nueva Tabla General */}
            <TablaGeneralAlumnos />
        </div>
    );
}
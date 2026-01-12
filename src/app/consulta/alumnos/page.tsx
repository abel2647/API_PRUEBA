'use client';

import React from 'react';
import HistorialEstudiantes from '@/components/HistorialEstudiantes';
import { GraduationCap } from 'lucide-react';

export default function ConsultaAlumnosPage() {
    return (
        <div className="p-6 space-y-6 animate-in fade-in duration-500">
            {/* Encabezado de la Sección */}
            <div className="flex flex-col gap-1">
                <div className="flex items-center gap-3">
                    <div className="bg-blue-600 p-2 rounded-lg text-white">
                        <GraduationCap className="h-6 w-6" />
                    </div>
                    <h1 className="text-3xl font-black text-slate-900 uppercase tracking-tighter">
                        Consulta de Estudiantes
                    </h1>
                </div>
                <p className="text-slate-500 text-sm mt-2">
                    Visualiza y filtra el historial de entradas y salidas de los alumnos registrados.
                </p>
            </div>

            {/* Tabla de Historial */}
            <HistorialEstudiantes />
        </div>
    );
}

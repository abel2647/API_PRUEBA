'use client';

import React from 'react';
import HistorialEstudiantes from '@/components/HistorialEstudiantes';

export default function HistorialEstudiantesPage() {
    return (
        <div className="space-y-6 animate-in fade-in duration-500">
            <div className="flex flex-col gap-1">
                <h1 className="text-3xl font-black text-slate-900 uppercase tracking-tighter">
                    Gestión Académica
                </h1>
                <p className="text-slate-500 text-sm">
                    Consulta el registro histórico de entradas y salidas de la comunidad estudiantil.
                </p>
            </div>

            <HistorialEstudiantes />
        </div>
    );
}
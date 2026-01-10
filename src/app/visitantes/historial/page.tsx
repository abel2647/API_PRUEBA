// src/app/visitantes/historial/page.tsx
import React from 'react';
import { HistorialVisitantes } from '@/components/HistorialVisitantes'; // Asegúrate que la ruta sea correcta según donde guardaste el componente

export default function HistorialPage() {
    return (
        <div className="p-4">
            <h1 className="text-2xl font-bold mb-4">Gestión de Visitantes</h1>
            <HistorialVisitantes />
        </div>
    );
}
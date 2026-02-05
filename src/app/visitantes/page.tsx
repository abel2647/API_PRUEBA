'use client';

import React from 'react';
import { RegistroVisitanteForm } from '@/components/RegistroVisitantesForm';
import { ValidationModal } from '@/components/ValidationModal';

export default function Home() {
    return (
        <div className="container mx-auto py-8 px-4 max-w-7xl">
            {/* 1. FORMULARIO DE REGISTRO */}
            <RegistroVisitanteForm />

            {/* 2. EL MODAL (Botón Flotante) */}
            {/* Ya no le pasamos 'isOpen' porque el modal se manda solo */}
            <ValidationModal />
        </div>
    );
}
'use client';

import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { DoorOpen, CheckCircle2 } from 'lucide-react';

interface EntrySelectorModalProps {
    isOpen: boolean;
    onClose: (open: boolean) => void;
    onSelect: (numeroEntrada: number) => void;
}

export const EntrySelectorModal = ({ isOpen, onClose, onSelect }: EntrySelectorModalProps) => {

    // Array para generar los botones del 1 al 6
    const puertas = [1, 2, 3, 4, 5, 6];

    // Estado para saber cuál es la puerta actual guardada (para pintarla diferente)
    const [puertaActiva, setPuertaActiva] = useState<number | null>(null);

    // Al abrir el modal, leemos qué puerta está guardada actualmente
    useEffect(() => {
        if (isOpen) {
            const guardada = localStorage.getItem('numeroEntrada');
            if (guardada) {
                setPuertaActiva(parseInt(guardada));
            }
        }
    }, [isOpen]);

    const handleSelection = (numero: number) => {
        // 1. GUARDADO CRÍTICO EN LOCALSTORAGE
        // Esto es lo que permite que el ValidationModal sepa qué puerta enviar al backend
        localStorage.setItem('numeroEntrada', numero.toString());

        // 2. Disparamos evento (opcional, ayuda a sincronizar pestañas)
        window.dispatchEvent(new Event('storage'));

        // 3. Actualizamos estado visual local
        setPuertaActiva(numero);

        console.log(`Puerta ${numero} seleccionada y guardada.`);

        // 4. Comunicamos al padre y cerramos
        onSelect(numero);
        onClose(false);
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-[400px]">
                <DialogHeader>
                    <div className="flex items-center justify-center mb-2">
                        <div className="p-3 bg-blue-100 rounded-full">
                            <DoorOpen className="h-8 w-8 text-blue-600" />
                        </div>
                    </div>
                    <DialogTitle className="text-center text-xl">Seleccionar Acceso</DialogTitle>
                    <DialogDescription className="text-center">
                        Por favor, indica en qué puerta o torniquete se está registrando la entrada.
                    </DialogDescription>
                </DialogHeader>

                <div className="grid grid-cols-3 gap-3 mt-4">
                    {puertas.map((numero) => {
                        const isSelected = puertaActiva === numero;

                        return (
                            <Button
                                key={numero}
                                variant={isSelected ? "default" : "outline"} // Cambia estilo si está seleccionado
                                className={`
                                    h-16 text-2xl font-bold border-2
                                    transition-all duration-200 relative
                                    ${isSelected
                                    ? 'bg-blue-600 text-white border-blue-600 ring-2 ring-blue-300 ring-offset-2'
                                    : 'hover:bg-blue-50 hover:text-blue-600 hover:border-blue-200 text-gray-600'}
                                `}
                                onClick={() => handleSelection(numero)}
                            >
                                {numero}
                                {isSelected && (
                                    <div className="absolute top-1 right-1">
                                        <CheckCircle2 size={12} className="text-white" />
                                    </div>
                                )}
                            </Button>
                        );
                    })}
                </div>

                <div className="mt-4 text-center">
                    <p className="text-xs text-gray-400 uppercase tracking-widest">
                        Sistema de Control • Puerta Actual: {puertaActiva || '-'}
                    </p>
                </div>
            </DialogContent>
        </Dialog>
    );
};
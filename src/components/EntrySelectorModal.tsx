'use client';

import React from 'react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog'; // Ajusta la ruta si es './ui/dialog'
import { DoorOpen } from 'lucide-react';

interface EntrySelectorModalProps {
    isOpen: boolean;
    onClose: (open: boolean) => void;
    onSelect: (numeroEntrada: number) => void;
}

export const EntrySelectorModal = ({ isOpen, onClose, onSelect }: EntrySelectorModalProps) => {
    
    // Array para generar los botones del 1 al 7
    const puertas = [1, 2, 3, 4, 5, 6, 7];

    const handleSelection = (numero: number) => {
        onSelect(numero); // Aquí pasamos el valor (ej: 5) a la variable del padre
        onClose(false);   // Cerramos el modal
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
                    {puertas.map((numero) => (
                        <Button
                            key={numero}
                            variant="outline"
                            className={`
                                h-16 text-2xl font-bold border-2
                                hover:bg-blue-600 hover:text-white hover:border-blue-600
                                transition-all duration-200
                                ${numero === 7 ? 'col-start-2' : ''} /* Centra el 7 en la última fila */
                            `}
                            onClick={() => handleSelection(numero)}
                        >
                            {numero}
                        </Button>
                    ))}
                </div>
                
                <div className="mt-4 text-center">
                    <p className="text-xs text-gray-400 uppercase tracking-widest">
                        Sistema de Control
                    </p>
                </div>
            </DialogContent>
        </Dialog>
    );
};
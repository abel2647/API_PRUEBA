'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { CheckCircle, XCircle, Search } from 'lucide-react'; // Iconos

//Componentes de Shadcn UI para el modal
import {
    Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger,
} from './ui/dialog';
import { Separator } from './ui/separator';

// Definición de la interfaz de datos de la API
interface ValidationResult {
    primerNombre: string;
    apellidoPaterno: string;
    asunto: string;
    numeroAcompañantes: number;
    fechaExpiracion: string; // La recibimos como string y la convertiremos
    uuid: string;
    deleted: number;
}

export const ValidationModal = () => {
    const [uuidInput, setUuidInput] = useState('');
    const [result, setResult] = useState<ValidationResult | null>(null);
    const [isValid, setIsValid] = useState<boolean | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState('');

    const handleValidation = async () => {
        if (!uuidInput) {
            setMessage("Por favor, introduce un UUID o escanea el QR.");
            return;
        }

        setIsLoading(true);
        setResult(null);
        setIsValid(null);
        setMessage('');

        try {
            // Llama al endpoint de validación
            const response = await fetch(`http://localhost:8080/api/visitante/validar/${uuidInput}`);

            if (response.status === 404) {
                // Caso: UUID no encontrado
                setIsValid(false);
                setMessage("PASE NO VÁLIDO O NO REGISTRADO.");
                setUuidInput('');
                return;
            }

            if (!response.ok) {
                throw new Error("Error en la conexión con la API.");
            }

            const data: ValidationResult = await response.json();

            // 1. Verificar si ha expirado (Lógica clave)
            const expirationTime = new Date(data.fechaExpiracion);
            const now = new Date();
            const expired = expirationTime < now;

            // 2. Verificar si está marcado como eliminado (deleted = 1)
            const deleted = data.deleted === 1;

            if (expired || deleted) {
                setIsValid(false);
                setMessage(expired ? "PASE EXPIRADO" : "PASE CANCELADO");
            } else {
                setIsValid(true);
                setMessage("ACCESO PERMITIDO");
            }

            setResult(data);

        } catch (error) {
            console.error(error);
            setIsValid(false);
            setMessage("ERROR DE SISTEMA O CONEXIÓN.");
        } finally {
            setIsLoading(false);
        }
    };

    // Función para formatear la fecha
    const formatDateTime = (isoString: string) => {
        if (!isoString) return 'N/A';
        const date = new Date(isoString);
        return date.toLocaleString('es-MX', {
            year: 'numeric',
            month: 'numeric',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    return (
        <Dialog>
            <DialogTrigger asChild>
                {/* Botón para abrir el Modal (Simula la estación de escaneo) */}
                <Button variant="outline" className="fixed bottom-4 right-4 z-50 shadow-lg bg-red-700 text-white hover:bg-red-800 hover:text-white">
                    <Search className="mr-2 h-4 w-4" /> Validacion de QR
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-[500px]">
                <DialogHeader>
                    <DialogTitle className="text-2xl font-bold">Verificación de Pase QR</DialogTitle>
                    <DialogDescription>
                        Introduce o escanea el UUID para verificar el acceso.
                    </DialogDescription>
                </DialogHeader>

                <div className="flex space-x-2 my-4">
                    <Input
                        placeholder="Pegar UUID o escanear..."
                        value={uuidInput}
                        onChange={(e) => setUuidInput(e.target.value)}
                        disabled={isLoading}
                    />
                    <Button onClick={handleValidation} disabled={isLoading} className="bg-green-600 hover:bg-green-700">
                        {isLoading ? 'Buscando...' : 'Validar'}
                    </Button>
                </div>

                <Separator />

                {/* SECCIÓN DE RESULTADO DE VALIDACIÓN */}
                {(isValid !== null || isLoading) && (
                    <Card className={`border-4 ${isValid ? 'border-green-500' : 'border-red-500'} mt-4`}>
                        <CardHeader>
                            <div className="flex items-center justify-between">
                                <CardTitle className={`text-2xl ${isValid ? 'text-green-600' : 'text-red-600'}`}>
                                    {message}
                                </CardTitle>
                                {isValid ?
                                    <CheckCircle className="h-10 w-10 text-green-500" /> :
                                    <XCircle className="h-10 w-10 text-red-500" />
                                }
                            </div>
                        </CardHeader>
                        <CardContent>
                            {result && (
                                <div className="space-y-2 text-sm">
                                    <p><strong>Visitante:</strong> {result.primerNombre} {result.apellidoPaterno}</p>
                                    <p><strong>Acompañantes:</strong> {result.numeroAcompañantes} personas</p>
                                    <p><strong>Motivo:</strong> {result.asunto}</p>

                                    <Separator className="my-2" />

                                    {/* Nuevo elemento crucial: Fecha de Expiración */}
                                    <p className={`font-bold ${isValid ? 'text-gray-700' : 'text-red-700'}`}>
                                        FECHA DE EXPIRACIÓN: {formatDateTime(result.fechaExpiracion)}
                                    </p>

                                    <p className="text-xs text-gray-500">UUID: {result.uuid}</p>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                )}
            </DialogContent>
        </Dialog>
    );
};
'use client';

import React, { useState, useRef, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { CheckCircle, XCircle, Search, Loader2, ScanLine } from 'lucide-react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog';

interface Props {
    puertaInicial?: string;
}

interface ValidationResult {
    primerNombre: string;
    apellidoPaterno: string;
    asunto: string;
    numeroAcompañantes: number;
    fechaExpiracion: string;
    uuid: string;
    deleted: number;
}

export const ValidationModal = ({ puertaInicial }: Props) => {

    // --- CORRECCIÓN DE ESTADO INICIAL ---
    // Si hay prop, úsala. Si no, busca en storage. Si no, usa '1'.
    const [currentPuerta, setCurrentPuerta] = useState<string>('1');

    // Efecto de inicialización robusto
    useEffect(() => {
        // 1. Preferencia: Propiedad directa (URL)
        if (puertaInicial && puertaInicial !== '') {
            setCurrentPuerta(puertaInicial);
            localStorage.setItem('numeroEntrada', puertaInicial);
        }
        // 2. Respaldo: Memoria del navegador
        else {
            const guardada = localStorage.getItem('numeroEntrada');
            if (guardada) {
                setCurrentPuerta(guardada);
            }
        }
    }, [puertaInicial]);

    const [uuidInput, setUuidInput] = useState('');
    const [result, setResult] = useState<ValidationResult | null>(null);
    const [isValid, setIsValid] = useState<boolean | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState('');

    const inputRef = useRef<HTMLInputElement>(null);
    const showingResult = !!(result || message);

    // CONTROL DE FOCO
    useEffect(() => {
        if (!showingResult && !isLoading) {
            const timer = setTimeout(() => {
                inputRef.current?.focus();
            }, 100);
            return () => clearTimeout(timer);
        }
    }, [showingResult, isLoading]);

    // ESCANEO AUTOMÁTICO
    useEffect(() => {
        if (uuidInput.length > 0 && !isLoading) {
            const timer = setTimeout(() => {
                handleValidation(uuidInput);
            }, 1000);
            return () => clearTimeout(timer);
        }
    }, [uuidInput]);

    // LIMPIEZA AUTOMÁTICA
    useEffect(() => {
        let timer: NodeJS.Timeout;
        if (showingResult) {
            timer = setTimeout(() => {
                handleReset();
            }, 3000);
        }
        return () => clearTimeout(timer);
    }, [showingResult]);

    const handleReset = () => {
        setResult(null);
        setIsValid(null);
        setMessage('');
        setUuidInput('');
    };

    const handleValidation = async (codeToValidate: string) => {
        setIsLoading(true);
        setMessage('');

        try {
            // --- CORRECCIÓN FINAL DE VALOR ---
            // Aseguramos que usamos el valor más fresco posible
            const puertaFinal = currentPuerta || localStorage.getItem('numeroEntrada') || '1';

            console.log(`Enviando validación... UUID: ${codeToValidate}, Puerta: ${puertaFinal}`);

            const response = await fetch(
                `http://localhost:8080/api/visitante/validar/${codeToValidate}?numeroEntrada=${puertaFinal}`
            );

            setUuidInput('');

            if (response.status === 404) {
                setIsValid(false);
                setMessage("PASE NO VÁLIDO");
                setResult(null);
                return;
            }

            if (!response.ok) throw new Error("Error API");

            const data: ValidationResult = await response.json();
            const expirationTime = new Date(data.fechaExpiracion);
            const now = new Date();
            const expired = expirationTime < now;
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
            setMessage("ERROR DE SISTEMA");
            setUuidInput('');
        } finally {
            setIsLoading(false);
        }
    };

    const formatDateTime = (isoString: string) => {
        if (!isoString) return 'N/A';
        const date = new Date(isoString);
        return date.toLocaleString('es-MX', {
            year: 'numeric', month: 'numeric', day: 'numeric',
            hour: '2-digit', minute: '2-digit',
        });
    };

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button variant="outline" className="fixed bottom-4 right-4 z-50 shadow-lg bg-red-700 text-white hover:bg-red-800 hover:text-white">
                    <Search className="mr-2 h-4 w-4" /> Escáner
                </Button>
            </DialogTrigger>

            <DialogContent
                className="sm:max-w-[500px] min-h-[300px] flex flex-col justify-center"
                onOpenAutoFocus={(e) => {
                    e.preventDefault();
                    setTimeout(() => inputRef.current?.focus(), 100);
                }}
            >
                {!showingResult ? (
                    <div className="flex flex-col items-center justify-center space-y-6 animate-in fade-in duration-300">
                        <DialogHeader>
                            <DialogTitle className="text-2xl font-bold text-center">Escanear Pase</DialogTitle>
                            <DialogDescription className="text-center">
                                El sistema está listo. Escanea el código QR ahora.
                                <br/>
                                <span className="text-xs text-blue-600 font-mono mt-1 block font-bold">
                                    Puerta Activa: {currentPuerta}
                                </span>
                            </DialogDescription>
                        </DialogHeader>

                        <div className="relative w-full max-w-sm">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <ScanLine className={`h-5 w-5 ${isLoading ? 'text-blue-500 animate-pulse' : 'text-gray-400'}`} />
                            </div>
                            <Input
                                ref={inputRef}
                                placeholder={isLoading ? "Validando..." : "Escanea aquí..."}
                                value={uuidInput}
                                onChange={(e) => setUuidInput(e.target.value)}
                                disabled={isLoading}
                                autoComplete="off"
                                className="pl-10 text-lg font-mono h-12 border-2 focus-visible:ring-blue-500"
                            />
                            {isLoading && (
                                <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                                    <Loader2 className="h-5 w-5 animate-spin text-blue-600" />
                                </div>
                            )}
                        </div>
                        <div className="text-xs text-gray-400 italic">
                            {isLoading ? "Consultando base de datos..." : "Esperando entrada..."}
                        </div>
                    </div>
                ) : (
                    <div className="animate-in zoom-in-95 duration-300 w-full">
                        <DialogHeader className="mb-2">
                            <DialogTitle className="text-center text-xl text-gray-400">Resultado de Validación</DialogTitle>
                        </DialogHeader>

                        <Card className={`border-4 ${isValid ? 'border-green-500' : isValid === false ? 'border-red-500' : 'border-gray-200'} shadow-xl`}>
                            <CardHeader className="pb-2 bg-gray-50/50">
                                <div className="flex flex-col items-center justify-center gap-2">
                                    {isValid === true && <CheckCircle className="h-16 w-16 text-green-500" />}
                                    {isValid === false && <XCircle className="h-16 w-16 text-red-500" />}
                                    <CardTitle className={`text-2xl font-black uppercase text-center ${isValid ? 'text-green-700' : 'text-red-700'}`}>
                                        {message}
                                    </CardTitle>
                                </div>
                            </CardHeader>
                            <CardContent className="pt-4">
                                {result ? (
                                    <div className="space-y-4 text-base">
                                        <div className="grid grid-cols-2 gap-4 text-center">
                                            <div>
                                                <p className="text-xs text-gray-500 uppercase">Visitante</p>
                                                <p className="font-bold text-lg leading-tight">{result.primerNombre} {result.apellidoPaterno}</p>
                                            </div>
                                            <div>
                                                <p className="text-xs text-gray-500 uppercase">Acompañantes</p>
                                                <p className="font-bold text-lg">{result.numeroAcompañantes}</p>
                                            </div>
                                        </div>
                                        <div className="bg-gray-100 p-3 rounded-lg text-center">
                                            <p className="text-xs text-gray-500 uppercase font-bold mb-1">Asunto</p>
                                            <p className="text-sm font-medium">{result.asunto}</p>
                                        </div>
                                        <p className={`text-xs font-bold text-center mt-2 ${isValid ? 'text-green-800' : 'text-red-800'}`}>
                                            VENCE: {formatDateTime(result.fechaExpiracion)}
                                        </p>
                                    </div>
                                ) : (
                                    <div className="text-center py-4 text-gray-500">
                                        No se encontraron datos para este código.
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                        <div className="mt-4">
                            <div className="w-full bg-gray-200 h-1 rounded-full overflow-hidden">
                                <div className="bg-gray-800 h-full animate-[progress_3s_linear_forwards] w-full origin-left" />
                            </div>
                            <p className="text-[10px] text-center text-gray-400 mt-1 uppercase tracking-wider">
                                Volviendo al escáner en 3s...
                            </p>
                        </div>
                    </div>
                )}
            </DialogContent>
            <style jsx global>{`@keyframes progress { from { width: 100%; } to { width: 0%; } }`}</style>
        </Dialog>
    );
};
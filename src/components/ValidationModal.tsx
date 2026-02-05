'use client';

import React, { useState, useRef, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { CheckCircle, XCircle, Search, Loader2, ScanLine, Hash, DoorOpen, X } from 'lucide-react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog';

interface Props {
    puertaInicial?: string;
}

interface ValidationResult {
    acceso: boolean;
    mensaje: string;
    visitante: string | null;
    asunto: string | null;
    totalAccesos?: number;
    puerta?: string;
}

export const ValidationModal = ({ puertaInicial }: Props) => {

    const [currentPuerta, setCurrentPuerta] = useState<string>('1');

    useEffect(() => {
        if (puertaInicial && puertaInicial !== '') {
            setCurrentPuerta(puertaInicial);
            localStorage.setItem('numeroEntrada', puertaInicial);
        } else {
            const guardada = localStorage.getItem('numeroEntrada');
            if (guardada) setCurrentPuerta(guardada);
        }
    }, [puertaInicial]);

    const [uuidInput, setUuidInput] = useState('');
    const [result, setResult] = useState<ValidationResult | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    const inputRef = useRef<HTMLInputElement>(null);

    const showingResult = !!result;
    const isSuccess = result?.acceso === true;

    // CONTROL DE FOCO
    useEffect(() => {
        if (!showingResult && !isLoading) {
            const timer = setTimeout(() => inputRef.current?.focus(), 100);
            return () => clearTimeout(timer);
        }
    }, [showingResult, isLoading]);

    // ESCANEO AUTOMÁTICO AL ESCRIBIR
    useEffect(() => {
        if (uuidInput.length > 5 && !isLoading) {
            const timer = setTimeout(() => handleValidation(uuidInput), 800);
            return () => clearTimeout(timer);
        }
    }, [uuidInput]);

    // *** ELIMINADO EL useEffect DE CIERRE AUTOMÁTICO ***

    const handleReset = () => {
        setResult(null);
        setUuidInput('');
    };

    const handleValidation = async (codeToValidate: string) => {
        setIsLoading(true);

        try {
            const puertaFinal = currentPuerta || localStorage.getItem('numeroEntrada') || '1';
            console.log(`Validando ${codeToValidate} en Puerta ${puertaFinal}`);

            const response = await fetch('http://localhost:8080/api/visitante/validar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    uuid: codeToValidate,
                    puerta: parseInt(puertaFinal)
                })
            });

            if (!response.ok) throw new Error("Error de conexión");

            const data: ValidationResult = await response.json();
            setResult(data);

        } catch (error) {
            console.error(error);
            setResult({
                acceso: false,
                mensaje: "ERROR DE CONEXIÓN",
                visitante: null,
                asunto: null
            });
            setUuidInput('');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button variant="outline" className="fixed bottom-4 right-4 z-50 shadow-lg bg-red-700 text-white hover:bg-red-800 hover:text-white">
                    <Search className="mr-2 h-4 w-4" /> Escáner
                </Button>
            </DialogTrigger>

            <DialogContent
                className="sm:max-w-[500px] min-h-[350px] flex flex-col justify-center"
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
                    </div>
                ) : (
                    <div className="animate-in zoom-in-95 duration-300 w-full">
                        <DialogHeader className="mb-2">
                            <DialogTitle className="text-center text-xl text-gray-400">Resultado</DialogTitle>
                        </DialogHeader>

                        <Card className={`border-4 ${isSuccess ? 'border-green-500' : 'border-red-500'} shadow-xl`}>
                            <CardHeader className="pb-2 bg-gray-50/50">
                                <div className="flex flex-col items-center justify-center gap-2">
                                    {isSuccess ?
                                        <CheckCircle className="h-16 w-16 text-green-500" /> :
                                        <XCircle className="h-16 w-16 text-red-500" />
                                    }
                                    <CardTitle className={`text-2xl font-black uppercase text-center ${isSuccess ? 'text-green-700' : 'text-red-700'}`}>
                                        {result?.mensaje}
                                    </CardTitle>
                                </div>
                            </CardHeader>

                            <CardContent className="pt-4">
                                {result?.visitante ? (
                                    <div className="space-y-4 text-base">
                                        <div className="text-center">
                                            <p className="text-xs text-gray-500 uppercase">Visitante</p>
                                            <p className="font-bold text-xl uppercase leading-tight">{result.visitante}</p>
                                        </div>

                                        <div className="grid grid-cols-2 gap-4 text-center border-t border-b border-gray-100 py-3 mt-2">
                                            <div className="flex flex-col items-center">
                                                <div className="flex items-center gap-1 text-xs text-gray-500 uppercase">
                                                    <Hash className="w-3 h-3"/> Accesos
                                                </div>
                                                <p className="font-bold text-2xl font-mono text-blue-700">
                                                    {result.totalAccesos}
                                                </p>
                                            </div>
                                            <div className="flex flex-col items-center">
                                                <div className="flex items-center gap-1 text-xs text-gray-500 uppercase">
                                                    <DoorOpen className="w-3 h-3"/> Ubicación
                                                </div>
                                                <p className="font-bold text-lg uppercase">
                                                    {result.puerta}
                                                </p>
                                            </div>
                                        </div>

                                        <div className="bg-gray-100 p-2 rounded text-center">
                                            <p className="text-xs text-gray-500 uppercase font-bold">Asunto</p>
                                            <p className="text-sm font-medium uppercase">{result.asunto}</p>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="text-center py-4 text-gray-500 italic">
                                        Intente nuevamente o contacte al administrador.
                                    </div>
                                )}
                            </CardContent>

                            {/* NUEVO BOTÓN PARA CERRAR */}
                            <CardFooter className="pt-4 pb-4 flex justify-center bg-gray-50">
                                <Button
                                    onClick={handleReset}
                                    size="lg"
                                    className={`w-full text-lg font-bold ${isSuccess ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'}`}
                                >
                                    <X className="mr-2 h-5 w-5" />
                                    CERRAR
                                </Button>
                            </CardFooter>
                        </Card>
                    </div>
                )}
            </DialogContent>
        </Dialog>
    );
};
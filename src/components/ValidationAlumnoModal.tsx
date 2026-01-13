'use client';

import React, { useState, useRef, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { CheckCircle, XCircle, UserCheck, Loader2, ScanLine } from 'lucide-react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog';

export const ValidationAlumnoModal = () => {
    const [matriculaInput, setMatriculaInput] = useState('');
    const [alumno, setAlumno] = useState<any>(null);
    const [isValid, setIsValid] = useState<boolean | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState('');

    const inputRef = useRef<HTMLInputElement>(null);
    const showingResult = !!(alumno || message);

    // Foco automático al abrir o resetear
    useEffect(() => {
        if (!showingResult && !isLoading) {
            const timer = setTimeout(() => inputRef.current?.focus(), 100);
            return () => clearTimeout(timer);
        }
    }, [showingResult, isLoading]);

    // Reset automático después de 3 segundos al mostrar resultado
    useEffect(() => {
        if (showingResult) {
            const timer = setTimeout(() => handleReset(), 3000);
            return () => clearTimeout(timer);
        }
    }, [showingResult]);

    const handleReset = () => {
        setAlumno(null);
        setIsValid(null);
        setMessage('');
        setMatriculaInput('');
    };

    const handleValidation = async (e?: React.FormEvent) => {
        if (e) e.preventDefault();
        if (!matriculaInput || isLoading) return;

        setIsLoading(true);
        setMessage('');

        try {
            const numeroEntrada = localStorage.getItem('numeroEntrada') || "1";

            const response = await fetch('http://localhost:8080/api/alumnos/registrar-asistencia', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    numeroControl: matriculaInput,
                    idEntrada: parseInt(numeroEntrada)
                }),
            });

            if (response.status === 404) {
                setIsValid(false);
                setMessage("ALUMNO NO ENCONTRADO");
                return;
            }

            if (!response.ok) throw new Error("Error en servidor");

            const data = await response.json();
            // Data es el RegistroAsistenciaModel que devuelve tu backend
            setAlumno(data.alumno);
            setIsValid(true);
            setMessage("ACCESO PERMITIDO");

        } catch (error) {
            console.error(error);
            setIsValid(false);
            setMessage("ERROR DE CONEXIÓN");
        } finally {
            setIsLoading(false);
            setMatriculaInput('');
        }
    };

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button variant="outline" className="fixed bottom-4 left-4 z-50 shadow-lg bg-blue-700 text-white hover:bg-blue-800 hover:text-white">
                    <UserCheck className="mr-2 h-4 w-4" /> Lector Alumnos
                </Button>
            </DialogTrigger>

            <DialogContent className="sm:max-w-[450px] min-h-[300px]">
                {!showingResult ? (
                    <div className="flex flex-col items-center space-y-6 py-4">
                        <DialogHeader>
                            <DialogTitle className="text-2xl font-bold text-center">Escaneo de Alumnos</DialogTitle>
                            <DialogDescription className="text-center">Ingrese matrícula o use el lector de barras</DialogDescription>
                        </DialogHeader>

                        <form onSubmit={handleValidation} className="w-full relative">
                            <ScanLine className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                            <Input
                                ref={inputRef}
                                placeholder="Esperando lectura..."
                                value={matriculaInput}
                                onChange={(e) => setMatriculaInput(e.target.value)}
                                className="pl-10 text-lg h-12 font-mono"
                                disabled={isLoading}
                            />
                        </form>
                        {isLoading && <Loader2 className="animate-spin text-blue-600" />}
                    </div>
                ) : (
                    <Card className={`border-4 ${isValid ? 'border-green-500' : 'border-red-500'} shadow-2xl`}>
                        <CardHeader className="text-center">
                            <div className="flex justify-center mb-2">
                                {isValid ? <CheckCircle className="h-16 w-16 text-green-500" /> : <XCircle className="h-16 w-16 text-red-500" />}
                            </div>
                            <CardTitle className={`text-2xl font-black ${isValid ? 'text-green-700' : 'text-red-700'}`}>
                                {message}
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="text-center space-y-2">
                            {alumno && (
                                <>
                                    <p className="text-xl font-bold">{alumno.primerNombre} {alumno.apellidoPaterno}</p>
                                    <p className="text-gray-500 font-mono">{alumno.numeroControl}</p>
                                    <div className="bg-blue-50 p-2 rounded text-blue-800 text-sm font-bold">
                                        {alumno.carreraClave}
                                    </div>
                                </>
                            )}
                        </CardContent>
                    </Card>
                )}
            </DialogContent>
        </Dialog>
    );
};
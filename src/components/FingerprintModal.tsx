'use client';

import React, { useState, useEffect } from 'react';
import { Fingerprint, Loader2, CheckCircle2, X, RefreshCw } from 'lucide-react'; // Importamos RefreshCw
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

interface FingerprintModalProps {
    isOpen: boolean;
    onClose: () => void;
    onComplete: () => void;
    studentId: number;
    readerId: string;
}

export default function FingerprintModal({ isOpen, onClose, onComplete, studentId, readerId }: FingerprintModalProps) {
    const [step, setStep] = useState(0);
    const [status, setStatus] = useState<'IDLE' | 'SCANNING' | 'SUCCESS' | 'ERROR'>('IDLE');
    const [message, setMessage] = useState('Coloca tu dedo en el lector');

    const progress = (step / 3) * 100;

    // --- FUNCIÓN DE REINTENTO ---
    const reiniciarRegistro = () => {
        setStep(0);
        setStatus('IDLE');
        setMessage('Coloca tu dedo en el lector para iniciar de nuevo');
    };

    const captureStep = async (currentStep: number) => {
        if (currentStep > 3) return;

        setStatus('SCANNING');
        setMessage(`Escaneando... paso ${currentStep} de 3`);

        try {
            const response = await fetch(`http://localhost:8080/api/v1/multi-fingerprint/enroll-step/${studentId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ readerId, step: currentStep })
            });

            const result = await response.text();

            if (response.ok) {
                setStep(currentStep);

                if (currentStep < 3) {
                    setStatus('IDLE');
                    setMessage('¡Bien! Levanta el dedo y colócalo otra vez...');
                    setTimeout(() => {
                        captureStep(currentStep + 1);
                    }, 1500);
                } else {
                    setStatus('SUCCESS');
                    setMessage(`¡Registro completado con éxito!`);
                    setTimeout(() => {
                        onComplete();
                        onClose();
                    }, 2000);
                }
            } else {
                setStatus('ERROR');
                // Si el backend manda un error específico (ej: "Dedo movido"), lo mostramos
                setMessage(result || "Error en la captura. Intenta de nuevo.");
            }
        } catch (error) {
            setStatus('ERROR');
            setMessage("Error de comunicación con el servidor");
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-slate-900/70 backdrop-blur-sm flex items-center justify-center z-[100]">
            <div className="bg-white p-10 rounded-[3rem] max-w-sm w-full shadow-2xl text-center relative border border-slate-200">
                <button onClick={onClose} className="absolute top-6 right-6 text-slate-400 hover:text-slate-600">
                    <X className="w-6 h-6" />
                </button>

                <h2 className="text-2xl font-bold text-slate-800 mb-6">Registro Biométrico</h2>

                <div className="relative w-56 h-56 mx-auto mb-8">
                    <svg className="w-full h-full transform -rotate-90">
                        <circle cx="112" cy="112" r="100" stroke="#f1f5f9" strokeWidth="12" fill="transparent" />
                        <circle
                            cx="112" cy="112" r="100"
                            stroke={status === 'ERROR' ? '#ef4444' : '#2563eb'}
                            strokeWidth="12" fill="transparent"
                            strokeDasharray="628"
                            strokeDashoffset={628 - (628 * progress) / 100}
                            strokeLinecap="round"
                            className="transition-all duration-700 ease-in-out"
                        />
                    </svg>

                    <div className="absolute inset-0 flex flex-col items-center justify-center">
                        {status === 'SUCCESS' ? (
                            <CheckCircle2 className="w-20 h-20 text-green-500 animate-bounce" />
                        ) : status === 'SCANNING' ? (
                            <div className="relative">
                                <Fingerprint className="w-20 h-20 text-blue-200" />
                                <div className="absolute inset-0 bg-blue-500/20 animate-pulse rounded-full" />
                            </div>
                        ) : (
                            <Fingerprint className={cn(
                                "w-20 h-20 transition-colors duration-500",
                                step > 0 ? "text-blue-600" : "text-slate-200",
                                status === 'ERROR' && "text-red-300"
                            )} />
                        )}
                        <span className="text-xl font-bold text-slate-400 mt-2">{step}/3</span>
                    </div>
                </div>

                <div className={cn(
                    "p-4 rounded-2xl mb-6 text-sm font-medium transition-colors min-h-[60px] flex items-center justify-center",
                    status === 'ERROR' ? "bg-red-50 text-red-600" : "bg-blue-50 text-blue-700"
                )}>
                    {message}
                </div>

                <div className="flex flex-col gap-3">
                    {/* BOTÓN PRINCIPAL */}
                    <Button
                        onClick={() => captureStep(1)}
                        disabled={status !== 'IDLE' || step === 3}
                        className="w-full h-14 rounded-2xl bg-blue-600 hover:bg-blue-700 text-lg font-semibold shadow-lg shadow-blue-200 transition-all active:scale-95"
                    >
                        {status === 'SCANNING' && <Loader2 className="animate-spin mr-2" />}
                        {status === 'SCANNING' ? 'Procesando...' : step > 0 ? 'Continuar' : 'Iniciar registro'}
                    </Button>

                    {/* BOTÓN DE REINTENTAR: Solo aparece si hay error o si se quedó a medias */}
                    {(status === 'ERROR' || (step > 0 && step < 3 && status === 'IDLE')) && (
                        <Button
                            onClick={reiniciarRegistro}
                            variant="ghost"
                            className="w-full h-12 rounded-2xl text-slate-500 hover:text-orange-600 hover:bg-orange-50 transition-all font-medium"
                        >
                            <RefreshCw className="mr-2 h-4 w-4" />
                            Reiniciar captura
                        </Button>
                    )}
                </div>
            </div>
        </div>
    );
}
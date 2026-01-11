'use client';

import React, { useState, useEffect } from 'react';
import { Fingerprint, Loader2, CheckCircle2, X, AlertCircle } from 'lucide-react';
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

    // Animación de llenado (como en el celular)
    const progress = (step / 3) * 100;

    const captureStep = async (currentStep: number) => {
        if (currentStep > 3) return;

        setStatus('SCANNING');
        setMessage(`Escaneando... paso ${currentStep} en el lector`);

        try {
            // IMPORTANTE: studentId debe ser un número y readerId el que recibiste
            const response = await fetch(`http://localhost:8080/api/v1/multi-fingerprint/enroll-step/${studentId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ readerId, step: currentStep })
            });

            const result = await response.text();
            console.log("Respuesta del Backend:", result);

            if (response.ok) {
                setStep(currentStep);//actualiza el circulo azul currentStep / 3 *100

                if (currentStep < 3) {
                    setStatus('IDLE');
                    setMessage('Levanta el dedo y colocalo de nuevo...');
                    //Espera 1.5 segundos y pasa a la siguiente captura
                    setTimeout(() => {
                        captureStep(currentStep + 1);
                    }, 1500);
                } else {
                    setStatus('SUCCESS');
                    setMessage(`Registro completo`);
                    setTimeout(() => {
                        onComplete();
                        onClose();
                    }, 2000);
                }
            } else {
                setStatus('ERROR');
                setMessage(result);
            }
        } catch (error) {
            setStatus('ERROR');
            setMessage("No hay conexión con el servidor");
        }
    };

    if (!isOpen) return null;
//front
    return (
        <div className="fixed inset-0 bg-slate-900/70 backdrop-blur-sm flex items-center justify-center z-[100]">
            <div className="bg-white p-10 rounded-[3rem] max-w-sm w-full shadow-2xl text-center relative border border-slate-200">
                <button onClick={onClose} className="absolute top-6 right-6 text-slate-400 hover:text-slate-600">
                    <X className="w-6 h-6" />
                </button>

                <h2 className="text-2xl font-bold text-slate-800 mb-6">Registro Biométrico</h2>

                {/* Círculo de Progreso Estilo Celular */}
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

                    {/* Icono Central Animado */}
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
                                step > 0 ? "text-blue-600" : "text-slate-200"
                            )} />
                        )}
                        <span className="text-xl font-bold text-slate-400 mt-2">{step}/3</span>
                    </div>
                </div>

                <div className={cn(
                    "p-4 rounded-2xl mb-6 text-sm font-medium transition-colors",
                    status === 'ERROR' ? "bg-red-50 text-red-600" : "bg-blue-50 text-blue-700"
                )}>
                    {message}
                </div>

                <Button
                    onClick={() => captureStep(1)} //Inicia en el paso 1
                    disabled={status !== 'IDLE' || step === 3}
                    className="w-full h-14 rounded-2xl bg-blue-600 hover:bg-blue-700 text-lg font-semibold shadow-lg shadow-blue-200 transition-all active:scale-95"
                >
                    {status === 'SCANNING' ? <Loader2 className="animate-spin mr-2" /> : null}
                    {status === 'SCANNING' ? 'Procesando...' : 'Iniciar registro'}
                </Button>
            </div>
        </div>
    );
}
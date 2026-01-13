'use client';

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Fingerprint, User, GraduationCap, Barcode, CheckCircle, XCircle, Loader2, X, RefreshCw } from 'lucide-react';
import { Separator } from '@/components/ui/separator';

export default function VerificacionHuellaPage() {
    const [alumno, setAlumno] = useState<any>(null);
    const [status, setStatus] = useState<'idle' | 'success' | 'error'>('idle');
    const [shouldVibrate, setShouldVibrate] = useState(false);
    const [isManualModalOpen, setIsManualModalOpen] = useState(false);
    const [barcodeData, setBarcodeData] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const inputRef = useRef<HTMLInputElement>(null);

    const resetPage = useCallback(() => {
        setAlumno(null);
        setStatus('idle');
        setShouldVibrate(false);
        setIsManualModalOpen(false);
        setBarcodeData('');
        setIsSubmitting(false);
    }, []);

    // FUNCIÓN DE VALIDACIÓN (LA QUE VA AL BACKEND)
    const ejecutarRegistroEscaneo = useCallback(async (valorMatricula: string) => {
        if (!valorMatricula || valorMatricula.length < 3 || isSubmitting) return;

        setIsSubmitting(true);
        try {
            const numeroEntrada = localStorage.getItem('numeroEntrada') || "1";
            const response = await fetch('http://localhost:8080/api/alumnos/registrar-asistencia', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    numeroControl: valorMatricula.trim(),
                    idEntrada: parseInt(numeroEntrada)
                }),
            });

            if (response.ok) {
                const data = await response.json();
                setAlumno(data.alumno);
                setStatus('success');
                setIsManualModalOpen(false);
                setTimeout(resetPage, 4000);
            } else {
                setStatus('error'); // Si no existe el alumno, mostramos pantalla roja
                setIsManualModalOpen(false);
                setTimeout(resetPage, 3000);
            }
        } catch (error) {
            console.error("Error de red:", error);
            alert("❌ ERROR DE CONEXIÓN AL BACKEND");
        } finally {
            setIsSubmitting(false);
        }
    }, [isSubmitting, resetPage]);

    // --- AUTOMATIZACIÓN POR TIEMPO (DEBOUNCE) ---
    // Detecta cuando el lector deja de escribir por 300ms y dispara la búsqueda
    useEffect(() => {
        if (barcodeData.length > 0 && !isSubmitting) {
            const delayDebounceFn = setTimeout(() => {
                ejecutarRegistroEscaneo(barcodeData);
            }, 500); // 500ms de espera tras el último caracter

            return () => clearTimeout(delayDebounceFn);
        }
    }, [barcodeData, ejecutarRegistroEscaneo, isSubmitting]);

    // Foco automático
    useEffect(() => {
        if (isManualModalOpen && inputRef.current) {
            inputRef.current.focus();
        }
    }, [isManualModalOpen]);

    // Escucha de huella digital
    const iniciarEscucha = useCallback(async () => {
        if (status !== 'idle' || isManualModalOpen) return;
        try {
            const res = await fetch('http://localhost:8080/api/v1/multi-fingerprint/identify-auto');
            if (res.status === 404) {
                setStatus('error');
                setShouldVibrate(true);
                return;
            }
            if (res.ok) {
                const data = await res.json();
                if (data && data.primerNombre) {
                    setAlumno(data);
                    setStatus('success');
                    setTimeout(resetPage, 4000);
                } else {
                    iniciarEscucha();
                }
            }
        } catch (error) {
            setTimeout(iniciarEscucha, 2000);
        }
    }, [status, isManualModalOpen, resetPage]);

    useEffect(() => {
        iniciarEscucha();
    }, [iniciarEscucha]);

    return (
        <div className="min-h-screen bg-slate-100 p-4 flex items-center justify-center relative overflow-hidden font-sans uppercase">
            <div className="w-full max-w-md z-10 font-bold tracking-tight">

                {status === 'idle' && (
                    <Card className="border-2 border-blue-200 shadow-xl animate-in fade-in zoom-in">
                        <CardHeader className="bg-blue-600 text-white text-center py-8">
                            <Fingerprint size={48} className="mx-auto mb-4 animate-pulse"/>
                            <CardTitle className="text-2xl font-black text-white">ACCESO ALUMNOS</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col items-center py-12 bg-white text-slate-500">
                            <Loader2 className="animate-spin text-blue-600 mb-4" size={32}/>
                            <p className="font-black tracking-widest text-center">ESPERANDO HUELLA O ESCÁNER...</p>
                        </CardContent>
                    </Card>
                )}

                {status === 'success' && alumno && (
                    <Card className="border-t-8 border-t-green-500 shadow-2xl bg-white animate-in slide-in-from-bottom-4 overflow-hidden">
                        <CardHeader className="text-center pt-8">
                            <CheckCircle className="text-green-500 mx-auto mb-2" size={64}/>
                            <CardTitle className="text-2xl font-black text-gray-800 tracking-tighter">ACCESO PERMITIDO</CardTitle>
                        </CardHeader>
                        <CardContent className="px-8 pb-6 text-slate-800">
                            <Separator className="my-4"/>
                            <div className="space-y-4">
                                <div className="flex items-start gap-4">
                                    <div className="bg-slate-100 p-2 rounded-lg"><User className="text-slate-500" size={24}/></div>
                                    <div>
                                        <p className="text-[10px] text-slate-400 font-black mb-0.5">NOMBRE COMPLETO</p>
                                        <p className="text-lg leading-tight font-black uppercase">{alumno.primerNombre} {alumno.apellidoPaterno} {alumno.apellidoMaterno}</p>
                                    </div>
                                </div>
                                <div className="flex items-start gap-4">
                                    <div className="bg-slate-100 p-2 rounded-lg"><GraduationCap className="text-slate-500" size={24}/></div>
                                    <div>
                                        <p className="text-[10px] text-slate-400 font-black mb-0.5">CARRERA / CLAVE</p>
                                        <p className="text-sm font-black uppercase">{alumno.carreraClave || 'INGENIERÍA'}</p>
                                    </div>
                                </div>
                                <div className="flex items-start gap-4">
                                    <div className="bg-slate-100 p-2 rounded-lg"><Barcode className="text-slate-500" size={24}/></div>
                                    <div>
                                        <p className="text-[10px] text-slate-400 font-black mb-0.5">NÚMERO DE CONTROL</p>
                                        <p className="text-lg font-mono font-black">{alumno.numeroControl}</p>
                                    </div>
                                </div>
                            </div>
                            <div className="mt-8 bg-gray-100 h-2 rounded-full overflow-hidden">
                                <div className="bg-green-500 h-full animate-[progress_4s_linear_forwards] w-full origin-left"/>
                            </div>
                        </CardContent>
                    </Card>
                )}

                {status === 'error' && (
                    <Card className="border-t-8 border-t-red-500 shadow-2xl bg-white animate-in zoom-in ring-4 ring-red-100">
                        <CardHeader className="text-center pt-8">
                            <XCircle className="text-red-500 mx-auto mb-2 animate-bounce" size={64}/>
                            <CardTitle className="text-2xl font-black text-gray-800 tracking-tighter uppercase text-center">SIN REGISTRO</CardTitle>
                        </CardHeader>
                        <CardContent className="text-center px-8 pb-8">
                            <Separator className="my-4"/>
                            <p className="text-slate-600 mb-8 font-black leading-relaxed">
                                LA CREDENCIAL/HUELLA NO COINCIDE <br/> CON NINGÚN REGISTRO.
                            </p>
                            <button onClick={resetPage} className="w-full flex items-center justify-center gap-3 bg-red-600 hover:bg-red-700 text-white py-5 rounded-2xl font-black shadow-xl transition-all">
                                <RefreshCw size={24}/> REINTENTAR
                            </button>
                        </CardContent>
                    </Card>
                )}
            </div>

            <button
                onClick={() => setIsManualModalOpen(true)}
                className={`fixed bottom-10 right-10 p-6 rounded-full shadow-2xl transition-all duration-500 z-50
                    ${shouldVibrate ? 'bg-red-600 animate-shake scale-125 ring-[12px] ring-red-100' : 'bg-blue-600 hover:scale-110 shadow-blue-500/40'}`}
            >
                <Barcode size={36} color="white"/>
            </button>

            {isManualModalOpen && (
                <div className="fixed inset-0 bg-slate-900/90 backdrop-blur-md flex items-center justify-center z-[100] p-4">
                    <Card className="w-full max-w-sm border-none shadow-2xl overflow-hidden animate-in zoom-in duration-200">
                        <CardHeader className="bg-slate-800 text-white text-center pb-10 pt-12 relative">
                            <button onClick={() => setIsManualModalOpen(false)} className="absolute right-6 top-6 text-slate-500 hover:text-white">
                                <X size={28}/>
                            </button>
                            <div className="bg-blue-600/20 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-4 border border-blue-500/30">
                                <Barcode size={40} className="text-blue-400"/>
                            </div>
                            <CardTitle className="text-xl font-black tracking-[0.15em]">MODO CREDENCIAL</CardTitle>
                        </CardHeader>
                        <CardContent className="bg-white p-10">
                            <div className="space-y-2 text-center">
                                <label className="text-[10px] font-black text-slate-400 tracking-widest uppercase">ESCANEANDO AUTOMÁTICAMENTE...</label>
                                <input
                                    ref={inputRef}
                                    type="text"
                                    className="w-full px-4 py-5 border-b-4 border-slate-100 bg-slate-50 rounded-t-xl focus:border-blue-600 focus:bg-white outline-none text-center text-3xl font-mono font-black tracking-widest transition-all"
                                    value={barcodeData}
                                    onChange={(e) => setBarcodeData(e.target.value)}
                                    placeholder="--------"
                                    disabled={isSubmitting}
                                />
                                {isSubmitting && (
                                    <div className="flex justify-center items-center gap-2 text-blue-600 font-bold mt-4 animate-pulse uppercase">
                                        <Loader2 className="animate-spin" size={20} />
                                        PROCESANDO...
                                    </div>
                                )}
                            </div>
                        </CardContent>
                    </Card>
                </div>
            )}

            <style jsx global>{`
                @keyframes progress { from { width: 100%; } to { width: 0%; } }
                @keyframes shake {
                    0%, 100% { transform: translateX(0); }
                    25% { transform: translateX(-10px) rotate(-4deg); }
                    75% { transform: translateX(10px) rotate(4deg); }
                }
                .animate-shake { animation: shake 0.25s infinite ease-in-out; }
            `}</style>
        </div>
    );
}
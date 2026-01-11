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
    const inputRef = useRef<HTMLInputElement>(null);

    const resetPage = useCallback(() => {
        setAlumno(null);
        setStatus('idle');
        setShouldVibrate(false);
        setIsManualModalOpen(false);
        setBarcodeData('');
    }, []);

    useEffect(() => {
        if (isManualModalOpen && inputRef.current) {
            inputRef.current.focus();
        }
    }, [isManualModalOpen]);

    const iniciarEscucha = useCallback(async () => {
        // Si ya estamos en éxito o error, no pedimos más
        if (status !== 'idle' || isManualModalOpen) return;

        try {
            const res = await fetch('http://localhost:8080/api/v1/multi-fingerprint/identify-auto');

            if (res.ok) {
                const data = await res.json();

                if (data && data.primerNombre) {
                    // CASO 1: Huella reconocida
                    setAlumno(data);
                    setStatus('success');
                    setTimeout(resetPage, 4000);
                } else {
                    // CASO 2: El sensor terminó el ciclo pero NO encontró a nadie
                    // Aquí volvemos a llamar a la función silenciosamente para que el sensor se reactive
                    iniciarEscucha();
                }
            } else {
                // CASO 3: El servidor devolvió un error (como el TIMED_OUT)
                // No llamamos a manejarFallo, simplemente reintentamos la escucha
                console.log("Ciclo de sensor completado sin lectura, reiniciando...");
                iniciarEscucha();
            }
        } catch (error) {
            // CASO 4: Error de red o servidor apagado
            // Esperamos un poco más para no saturar
            setTimeout(iniciarEscucha, 3000);
        }
    }, [status, isManualModalOpen, resetPage]);

    const manejarFallo = () => {
        setStatus('error');
        setShouldVibrate(true);
        // Sin timeout automático para que el botón de reintento sea necesario
    };

    const procesarEscaneo = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!barcodeData) return;
        try {
            const res = await fetch(`http://localhost:8080/api/v1/alumnos/control/${barcodeData}`);
            if (res.ok) {
                const data = await res.json();
                setAlumno(data);
                setStatus('success');
                setIsManualModalOpen(false);
                setTimeout(resetPage, 4000);
            } else {
                alert("CREDENCIAL NO ENCONTRADA");
                setBarcodeData('');
            }
        } catch (error) { console.error(error); }
    };

    useEffect(() => { iniciarEscucha(); }, [iniciarEscucha]);

    return (
        <div className="min-h-screen bg-slate-100 p-4 flex items-center justify-center relative overflow-hidden font-sans uppercase">
            <div className="w-full max-w-md z-10 font-bold tracking-tight">

                {/* 1. ESPERA (IDLE) */}
                {status === 'idle' && (
                    <Card className="border-2 border-blue-200 shadow-xl animate-in fade-in zoom-in">
                        <CardHeader className="bg-blue-600 text-white text-center py-8">
                            <Fingerprint size={48} className="mx-auto mb-4 animate-pulse" />
                            <CardTitle className="text-2xl font-black tracking-tighter text-white">ACCESO ALUMNOS</CardTitle>
                        </CardHeader>
                        <CardContent className="flex flex-col items-center py-12 bg-white">
                            <Loader2 className="animate-spin text-blue-600 mb-4" size={32} />
                            <p className="text-slate-500 font-black tracking-widest">ESPERANDO HUELLA...</p>
                        </CardContent>
                    </Card>
                )}

                {/* 2. ÉXITO (DISEÑO FULL RESTAURADO) */}
                {status === 'success' && alumno && (
                    <Card className="border-t-8 border-t-green-500 shadow-2xl bg-white animate-in slide-in-from-bottom-4 overflow-hidden">
                        <CardHeader className="text-center pt-8">
                            <CheckCircle className="text-green-500 mx-auto mb-2" size={64} />
                            <CardTitle className="text-2xl font-black text-gray-800">ACCESO PERMITIDO</CardTitle>
                        </CardHeader>
                        <CardContent className="px-8 pb-6 text-slate-800">
                            <Separator className="my-4" />
                            <div className="space-y-4">
                                <div className="flex items-start gap-4">
                                    <div className="bg-slate-100 p-2 rounded-lg"><User className="text-slate-500" size={24} /></div>
                                    <div>
                                        <p className="text-[10px] text-slate-400 font-black mb-0.5">NOMBRE COMPLETO</p>
                                        <p className="text-lg leading-tight font-black">{alumno.primerNombre} {alumno.apellidoPaterno} {alumno.apellidoMaterno}</p>
                                    </div>
                                </div>
                                <div className="flex items-start gap-4">
                                    <div className="bg-slate-100 p-2 rounded-lg"><GraduationCap className="text-slate-500" size={24} /></div>
                                    <div>
                                        <p className="text-[10px] text-slate-400 font-black mb-0.5">CARRERA / CLAVE</p>
                                        <p className="text-sm font-black">{alumno.carreraClave || 'INGENIERÍA'}</p>
                                    </div>
                                </div>
                                <div className="flex items-start gap-4">
                                    <div className="bg-slate-100 p-2 rounded-lg"><Barcode className="text-slate-500" size={24} /></div>
                                    <div>
                                        <p className="text-[10px] text-slate-400 font-black mb-0.5">NÚMERO DE CONTROL</p>
                                        <p className="text-lg font-mono font-black">{alumno.numeroControl}</p>
                                    </div>
                                </div>
                            </div>
                            <div className="mt-8 bg-gray-100 h-2 rounded-full overflow-hidden">
                                <div className="bg-green-500 h-full animate-[progress_4s_linear_forwards] w-full origin-left" />
                            </div>
                        </CardContent>
                    </Card>
                )}

                {/* 3. ERROR (DISEÑO FULL RESTAURADO + BOTÓN REINTENTAR) */}
                {status === 'error' && (
                    <Card className="border-t-8 border-t-red-500 shadow-2xl bg-white animate-in zoom-in ring-4 ring-red-100">
                        <CardHeader className="text-center pt-8">
                            <XCircle className="text-red-500 mx-auto mb-2 animate-bounce" size={64} />
                            <CardTitle className="text-2xl font-black text-gray-800 tracking-tighter uppercase">SIN REGISTRO</CardTitle>
                        </CardHeader>
                        <CardContent className="text-center px-8 pb-8">
                            <Separator className="my-4" />
                            <p className="text-slate-600 mb-8 font-black leading-relaxed">
                                LA HUELLA NO COINCIDE CON <br/> NINGÚN ALUMNO REGISTRADO.
                            </p>

                            <button
                                onClick={resetPage}
                                className="group w-full flex items-center justify-center gap-3 bg-red-600 hover:bg-red-700 text-white py-5 rounded-2xl font-black shadow-xl shadow-red-200 transition-all active:scale-95"
                            >
                                <RefreshCw size={24} className="group-hover:rotate-180 transition-transform duration-500" />
                                REINTENTAR LECTURA
                            </button>

                            <p className="mt-8 text-blue-600 text-[11px] font-black tracking-[0.2em] animate-pulse">
                                O ESCANEA CÓDIGO DE BARRAS
                            </p>
                        </CardContent>
                    </Card>
                )}
            </div>

            {/* BOTÓN FLOTANTE LECTOR BARRAS */}
            <button
                onClick={() => setIsManualModalOpen(true)}
                className={`fixed bottom-10 right-10 p-6 rounded-full shadow-2xl transition-all duration-500 z-50
                    ${shouldVibrate ? 'bg-red-600 animate-shake scale-125 ring-[12px] ring-red-100 shadow-red-500/50' : 'bg-blue-600 hover:scale-110 shadow-blue-500/40 shadow-2xl'}`}
            >
                <Barcode size={36} color="white" />
            </button>

            {/* MODAL LECTOR CREDENCIAL (DISEÑO FULL) */}
            {isManualModalOpen && (
                <div className="fixed inset-0 bg-slate-900/90 backdrop-blur-md flex items-center justify-center z-[100] p-4">
                    <Card className="w-full max-w-sm animate-in zoom-in duration-200 border-none shadow-2xl overflow-hidden">
                        <CardHeader className="bg-slate-800 text-white text-center pb-10 pt-12 relative">
                            <button onClick={() => setIsManualModalOpen(false)} className="absolute right-6 top-6 text-slate-500 hover:text-white transition-colors">
                                <X size={28} />
                            </button>
                            <div className="bg-blue-600/20 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-4 border border-blue-500/30">
                                <Barcode size={40} className="text-blue-400" />
                            </div>
                            <CardTitle className="text-xl font-black tracking-[0.15em]">MODO CREDENCIAL</CardTitle>
                        </CardHeader>
                        <CardContent className="bg-white p-10">
                            <form onSubmit={procesarEscaneo} className="space-y-6">
                                <div className="space-y-2">
                                    <label className="text-[10px] font-black text-slate-400 tracking-widest ml-1">ESPERANDO LECTURA...</label>
                                    <input
                                        ref={inputRef}
                                        type="text"
                                        className="w-full px-4 py-5 border-b-4 border-slate-100 bg-slate-50 rounded-t-xl focus:border-blue-600 focus:bg-white outline-none text-center text-2xl font-mono font-black tracking-widest transition-all"
                                        value={barcodeData}
                                        onChange={(e) => setBarcodeData(e.target.value)}
                                        placeholder="00000000"
                                    />
                                </div>
                                <button type="submit" className="w-full bg-blue-600 text-white py-4 rounded-xl font-black tracking-widest hover:bg-blue-700 shadow-lg shadow-blue-100">
                                    VERIFICAR AHORA
                                </button>
                            </form>
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
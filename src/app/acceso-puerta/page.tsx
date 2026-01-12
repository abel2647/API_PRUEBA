'use client';

import React, { Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { LogOut, UserCheck, Users, Fingerprint } from 'lucide-react';

// IMPORTACIONES
import VerificacionHuellaPage from '../estudiantes/verificacion/page';
import VisitantesPage from '../visitantes/page';

function PanelContenido() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const puerta = searchParams.get('puerta') || "1";

    return (
        <div className="h-screen w-screen flex flex-col bg-slate-100 overflow-hidden font-sans">

            {/* HEADER */}
            <header className="h-16 bg-white border-b px-8 flex justify-between items-center shadow-sm z-50">
                <div className="flex items-center gap-4">
                    <div className="bg-slate-900 p-2 rounded-xl text-white shadow-lg">
                        <Fingerprint className="w-6 h-6" />
                    </div>
                    <div>
                        <h1 className="text-lg font-black text-slate-800 uppercase tracking-tighter leading-none">
                            Módulo de Acceso
                        </h1>
                        <p className="text-[10px] font-bold text-blue-600 uppercase tracking-widest mt-1">
                            Puerta {puerta} • Activa
                        </p>
                    </div>
                </div>

                <button
                    onClick={() => router.push('/login')}
                    className="flex items-center gap-2 bg-slate-50 text-slate-500 px-5 py-2 rounded-2xl font-bold hover:bg-red-50 hover:text-red-600 transition-all border border-slate-200 text-xs uppercase"
                >
                    <LogOut className="w-4 h-4" />
                    Salir
                </button>
            </header>

            <main className="flex-1 flex gap-3 p-3 overflow-hidden bg-slate-200">

                {/* COLUMNA IZQUIERDA: ESTUDIANTES */}
                <div className="flex-1 flex flex-col bg-white rounded-[2.5rem] shadow-xl overflow-hidden relative isolation-isolate border border-white">
                    <div className="bg-blue-600 px-6 py-3 text-white flex items-center gap-3 z-20">
                        <UserCheck className="w-5 h-5" />
                        <h2 className="font-black uppercase text-[10px] tracking-widest">Validación Estudiantes</h2>
                    </div>

                    <div className="flex-1 overflow-y-auto no-scrollbar">
                        <VerificacionHuellaPage />
                    </div>
                </div>

                {/* COLUMNA DERECHA: VISITANTES */}
                <div className="flex-1 flex flex-col bg-white rounded-[2.5rem] shadow-xl overflow-hidden relative isolation-isolate border border-white">
                    <div className="bg-emerald-600 px-6 py-3 text-white flex items-center gap-3 z-20 shadow-md">
                        <Users className="w-5 h-5" />
                        <h2 className="font-black uppercase text-[10px] tracking-widest">Registro Visitantes</h2>
                    </div>

                    <div className="flex-1 overflow-y-auto no-scrollbar relative">
                        <VisitantesPage />
                    </div>
                </div>

            </main>

            <style jsx global>{`
                .no-scrollbar::-webkit-scrollbar { display: none; }
                .no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }

                .isolation-isolate {
                    isolation: isolate;
                    transform: translateZ(0);
                }

                // BOTÓN ESTUDIANTES 
                .isolation-isolate:first-child [class*="fixed"],
                .isolation-isolate:first-child [class*="absolute"] {
                    position: absolute !important;
                    bottom: 10rem !important;
                    right: 1.5rem !important;
                    z-index: 40 !important;
                }

                //TRANSFORMACIÓN COMPLETA DEL BOTÓN ORIGINAL DE VISITANTES
                .isolation-isolate:last-child [class*="fixed"],
                .isolation-isolate:last-child [class*="absolute"],
                .isolation-isolate:last-child button[title*="QR"] {
                    position: absolute !important;
                    bottom: 2rem !important;
                    left: 2rem !important;
                    right: auto !important;
                    
                    // TAMAÑO Y FORMA
                    min-width: 180px !important;
                    height: 64px !important;
                    background-color: #059669 !important; /* Esmeralda 600 */
                    color: white !important;
                    border-radius: 1.25rem !important; /* 20px */
                    padding: 0 1.5rem !important;
                    
                    //TIPOGRAFÍA 
                    font-weight: 900 !important;
                    text-transform: uppercase !important;
                    font-size: 0.75rem !important;
                    letter-spacing: 0.05em !important;
                    
                    // DISEÑO
                    display: flex !important;
                    flex-direction: row-reverse !important; /* El icono a la izquierda */
                    align-items: center !important;
                    justify-content: center !important;
                    gap: 0.75rem !important;
                    box-shadow: 0 20px 25px -5px rgb(16 185 129 / 0.3) !important;
                    border: 2px solid rgba(255, 255, 255, 0.2) !important;
                    transition: all 0.2s ease !important;
                    z-index: 50 !important;
                }

                //INYECCIÓN DEL ICONO DE QR MEDIANTE UN PSEUDO-ELEMENTO 
                .isolation-isolate:last-child button[title*="QR"]::before {
                    content: '' !important;
                    display: block !important;
                    width: 24px !important;
                    height: 24px !important;
                    background-color: white !important;
                    /* Usamos una máscara para el icono de QR */
                    mask: url('https://api.iconify.design/lucide:qr-code.svg') no-repeat center / contain !important;
                    -webkit-mask: url('https://api.iconify.design/lucide:qr-code.svg') no-repeat center / contain !important;
                    background-image: none !important;
                }

                // Efecto hover 
                .isolation-isolate:last-child button[title*="QR"]:hover {
                    background-color: #047857 !important;
                    transform: scale(1.05) !important;
                }
            `}</style>
        </div>
    );
}

export default function AccesoPuertaPage() {
    return (
        <Suspense fallback={<div className="h-screen w-screen flex items-center justify-center bg-slate-900 text-white font-black uppercase tracking-widest">Cargando Módulos...</div>}>
            <PanelContenido />
        </Suspense>
    );
}
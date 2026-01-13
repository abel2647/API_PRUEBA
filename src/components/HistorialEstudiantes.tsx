"use client";

import React, { useState, useEffect, useMemo, useCallback } from "react";
import { Search, RotateCcw, Edit3, ArrowUpDown, CheckCircle2, X, Save, Loader2, RefreshCw, Fingerprint, Calendar as CalendarIcon, Clock } from "lucide-react";
import FingerprintModal from "./FingerprintModal";

const CARRERAS = [
    { nombre: "Ingeniería en Sistemas Computacionales", clave: "ISIC" },
    { nombre: "Ingeniería Electrónica", clave: "IELC" },
    { nombre: "Ingeniería Química", clave: "IQUI" },
    { nombre: "Ingeniería Industrial", clave: "IIND" },
    { nombre: "Ingeniería Mecánica", clave: "IMEC" },
    { nombre: "Ingeniería Eléctrica", clave: "IELE" },
    { nombre: "Ingeniería Civil", clave: "ICIV" },
    { nombre: "Administración", clave: "IADM" },
    { nombre: "Contaduría Pública", clave: "CPUB" },
    { nombre: "Gestión Empresarial", clave: "IGEM" }
];

export default function HistorialEstudiantes() {
    // --- FILTROS ---
    const [nombre, setNombre] = useState("");
    const [paterno, setPaterno] = useState("");
    const [matricula, setMatricula] = useState("");
    const [fechaSeleccionada, setFechaSeleccionada] = useState(""); // UNA SOLA FECHA
    const [horaBusqueda, setHoraBusqueda] = useState("");
    const [orden, setOrden] = useState("reciente");

    // --- DATOS ---
    const [asistencias, setAsistencias] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const [updating, setUpdating] = useState(false);
    const [showToast, setShowToast] = useState(false);

    // --- MODALES ---
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [fpModalOpen, setFpModalOpen] = useState(false);
    const [alumnoAEditar, setAlumnoAEditar] = useState<any>(null);

    // --- CAMPOS TEMPORALES ---
    const [editNombre, setEditNombre] = useState("");
    const [editPaterno, setEditPaterno] = useState("");
    const [editMaterno, setEditMaterno] = useState("");
    const [editMatricula, setEditMatricula] = useState("");
    const [editCarrera, setEditCarrera] = useState("");
    const [currentReaderId, setCurrentReaderId] = useState("");

    // FUNCIÓN DE BÚSQUEDA (ACTUALIZAR VISTA)
    const fetchAsistencias = useCallback(async () => {
        setLoading(true);
        try {
            const queryParams = new URLSearchParams();
            if (nombre) queryParams.append("nombre", nombre);
            if (paterno) queryParams.append("paterno", paterno);
            if (matricula) queryParams.append("matricula", matricula);

            // Enviamos la fecha al backend (Asegúrate que el endpoint acepte 'fecha')
            if (fechaSeleccionada) queryParams.append("fecha", fechaSeleccionada);

            const response = await fetch(`http://localhost:8080/api/alumnos/asistencia/historial?${queryParams.toString()}`);
            if (response.ok) {
                let data = await response.json();

                // Filtro local por hora
                if (horaBusqueda) {
                    data = data.filter((item: any) => item.fechaHora.includes(horaBusqueda));
                }

                setAsistencias(data);
            }
        } catch (error) {
            console.error("Error al buscar:", error);
        } finally {
            setLoading(false);
        }
    }, [nombre, paterno, matricula, fechaSeleccionada, horaBusqueda]);

    useEffect(() => { fetchAsistencias(); }, [fetchAsistencias]);

    const handleLimpiar = () => {
        setNombre(""); setPaterno(""); setMatricula("");
        setFechaSeleccionada(""); setHoraBusqueda("");
        setTimeout(() => fetchAsistencias(), 50);
    };

    const handleEditClick = (item: any) => {
        setAlumnoAEditar(item);
        const partes = item.nombreCompleto.split(" ");
        setEditNombre(partes[0] || "");
        setEditPaterno(partes[1] || "");
        setEditMaterno(partes[2] || "");
        setEditMatricula(item.matricula);
        setEditCarrera(CARRERAS.find(c => c.nombre === item.carrera || c.clave === item.carrera)?.clave || "ISIC");
        setEditModalOpen(true);
    };

    const handleUpdateDatabase = async () => {
        if (!alumnoAEditar) return;
        setUpdating(true);
        try {
            const response = await fetch(`http://localhost:8080/api/alumnos/${alumnoAEditar.uuid}/${alumnoAEditar.alumno_id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    primerNombre: editNombre,
                    apellidoPaterno: editPaterno,
                    apellidoMaterno: editMaterno,
                    numeroControl: editMatricula,
                    carreraClave: editCarrera
                }),
            });
            if (response.ok) {
                setEditModalOpen(false);
                setShowToast(true);
                setTimeout(() => setShowToast(false), 3000);
                fetchAsistencias();
            }
        } catch (error) { alert("Error de conexión"); } finally { setUpdating(false); }
    };

    const handleFingerprintEdit = async (item: any) => {
        // 1. Verificamos que el registro tenga el ID del alumno
        if (!item.alumno_id) {
            alert("Error: El registro no tiene un ID de alumno vinculado.");
            return;
        }

        // Guardamos al alumno seleccionado para el modal
        setAlumnoAEditar(item);

        try {
            // 2. Intentamos detectar el lector en el puerto 8080
            const res = await fetch('http://localhost:8080/api/v1/multi-fingerprint/auto-select');

            if (res.ok) {
                const readers = await res.json();
                if (readers && readers.length > 0) {
                    // 3. Si hay lector, guardamos su ID y abrimos el modal
                    setCurrentReaderId(readers[0]);
                    setFpModalOpen(true);
                } else {
                    alert("Lector no detectado. Por favor, conéctalo por USB.");
                }
            } else {
                alert("El servicio de biometría no responde.");
            }
        } catch (error) {
            console.error("Error de conexión con el lector:", error);
            alert("Error crítico: Asegúrate de que el Backend esté corriendo.");
        }
    };

    const asistenciasOrdenadas = useMemo(() => {
        const temp = [...asistencias];
        if (orden === "reciente") return temp.sort((a, b) => new Date(b.fechaHora).getTime() - new Date(a.fechaHora).getTime());
        if (orden === "antiguo") return temp.sort((a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime());
        return temp;
    }, [asistencias, orden]);

    return (
        <div className="p-4 bg-slate-50 min-h-screen font-sans uppercase">
            {showToast && (
                <div className="fixed bottom-10 left-1/2 -translate-x-1/2 z-[200] animate-in fade-in slide-in-from-bottom-10">
                    <div className="bg-slate-900 px-8 py-4 rounded-full shadow-2xl flex items-center gap-4 border border-slate-700">
                        <CheckCircle2 className="text-green-500" size={20} />
                        <span className="text-white font-black text-[10px] tracking-widest text-center">CAMBIOS SINCRONIZADOS EXITOSAMENTE</span>
                    </div>
                </div>
            )}

            <div className="max-w-5xl mx-auto bg-white rounded-[2rem] shadow-xl p-8 border border-slate-100">
                <div className="flex justify-between items-center mb-8">
                    <h2 className="text-xl font-black text-slate-800 italic tracking-tighter uppercase">Historial Estudiantes</h2>
                    <select className="bg-slate-100 p-2.5 rounded-xl text-[10px] font-black text-slate-600 outline-none border border-slate-200 cursor-pointer" value={orden} onChange={(e)=>setOrden(e.target.value)}>
                        <option value="reciente">RECIENTES</option>
                        <option value="antiguo">ANTIGUOS</option>
                    </select>
                </div>

                {/* FILTROS SIMPLIFICADOS */}
                <div className="bg-slate-50 p-6 rounded-3xl border border-slate-100 mb-8 shadow-inner">
                    <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
                        <div>
                            <label className="text-[8px] font-black text-slate-400 mb-1 block ml-1 uppercase">Nombre</label>
                            <input type="text" className="w-full bg-white border border-slate-200 p-2 rounded-xl font-bold text-[11px] outline-none focus:border-blue-500" value={nombre} onChange={(e)=>setNombre(e.target.value)} />
                        </div>
                        <div>
                            <label className="text-[8px] font-black text-slate-400 mb-1 block ml-1 uppercase">Paterno</label>
                            <input type="text" className="w-full bg-white border border-slate-200 p-2 rounded-xl font-bold text-[11px] outline-none focus:border-blue-500" value={paterno} onChange={(e)=>setPaterno(e.target.value)} />
                        </div>
                        <div>
                            <label className="text-[8px] font-black text-slate-400 mb-1 block ml-1 uppercase">Matrícula</label>
                            <input type="text" className="w-full bg-white border border-slate-200 p-2 rounded-xl font-bold text-[11px] outline-none focus:border-blue-500" value={matricula} onChange={(e)=>setMatricula(e.target.value)} />
                        </div>
                        <div>
                            <label className="text-[8px] font-black text-slate-400 mb-1 block ml-1 uppercase text-blue-600 font-black">📅 FECHA</label>
                            <input type="date" className="w-full bg-white border border-slate-200 p-2 rounded-xl font-bold text-[11px] outline-none focus:border-blue-500" value={fechaSeleccionada} onChange={(e)=>setFechaSeleccionada(e.target.value)} />
                        </div>
                        <div>
                            <label className="text-[8px] font-black text-slate-400 mb-1 block ml-1 uppercase">🕒 HORA</label>
                            <input type="time" className="w-full bg-white border border-slate-200 p-2 rounded-xl font-bold text-[11px] outline-none focus:border-blue-500" value={horaBusqueda} onChange={(e)=>setHoraBusqueda(e.target.value)} />
                        </div>
                    </div>
                </div>

                {/* BOTONES */}
                <div className="flex gap-2 mb-8">
                    <button onClick={fetchAsistencias} className="bg-blue-600 text-white px-10 py-3 rounded-2xl font-black text-[10px] tracking-widest hover:bg-blue-700 shadow-lg shadow-blue-100 flex items-center gap-2">
                        <Search size={16} /> FILTRAR REGISTROS
                    </button>
                    <button onClick={handleLimpiar} className="bg-slate-100 text-slate-400 px-4 py-3 rounded-2xl hover:bg-slate-200 transition-all border border-slate-200">
                        <RotateCcw size={18} />
                    </button>
                </div>

                {/* TABLA */}
                <div className="overflow-hidden rounded-3xl border border-slate-100 shadow-sm">
                    <table className="w-full text-left">
                        <thead className="bg-slate-900 text-white font-black text-[9px] tracking-[0.2em] italic uppercase">
                        <tr>
                            <th className="p-4">REF</th>
                            <th className="p-4">ESTUDIANTE</th>
                            <th className="p-4 text-center">N° CONTROL</th>
                            <th className="p-4 text-center">HORA REGISTRO</th>
                            <th className="p-4 text-center">ACCIONES</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-50 font-bold">
                        {asistenciasOrdenadas.map((item) => {
                            const [fecha, hora] = item.fechaHora.split(' ');
                            return (
                                <tr key={item.id} className="hover:bg-slate-50 transition-colors group">
                                    <td className="p-4 text-blue-600 font-black text-[10px]">#{item.id.split('-')[0]}</td>
                                    <td className="p-4">
                                        <div className="text-slate-800 font-black text-sm uppercase leading-tight">{item.nombreCompleto}</div>
                                        <div className="text-[9px] text-slate-400 font-bold tracking-widest">{item.carrera}</div>
                                    </td>
                                    <td className="p-4 text-center font-black text-slate-600 text-xs">{item.matricula}</td>
                                    <td className="p-4 text-center">
                                        <div className="inline-flex flex-col items-center bg-slate-100 px-3 py-1 rounded-lg group-hover:bg-white transition-colors">
                                            <span className="text-slate-800 font-black text-[10px] italic">{fecha}</span>
                                            <span className="text-blue-600 font-bold text-[9px] tracking-widest">{hora}</span>
                                        </div>
                                    </td>
                                    <td className="p-4 text-center">
                                        <div className="flex justify-center gap-2">
                                            <button onClick={() => handleEditClick(item)} className="p-2.5 text-slate-400 hover:text-blue-600 bg-slate-50 rounded-xl transition-all shadow-sm"><Edit3 size={16}/></button>
                                            <button onClick={() => handleFingerprintEdit(item)} className="p-2.5 text-slate-400 hover:text-orange-600 bg-slate-50 rounded-xl transition-all shadow-sm"><Fingerprint size={16}/></button>
                                        </div>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* MODAL EDICIÓN DATOS */}
            {editModalOpen && (
                <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md flex items-center justify-center z-[150] p-4">
                    <div className="bg-white rounded-[2.5rem] p-8 max-w-sm w-full shadow-2xl border border-slate-100 relative">
                        <h3 className="text-xl font-black italic text-slate-800 mb-6 border-b pb-4 uppercase tracking-tighter">Ficha Edición</h3>
                        <div className="space-y-4">
                            <input type="text" className="w-full bg-slate-50 border border-slate-200 p-3 rounded-xl font-bold text-xs uppercase" value={editNombre} onChange={(e)=>setEditNombre(e.target.value)} />
                            <div className="grid grid-cols-2 gap-2">
                                <input type="text" className="bg-slate-50 border border-slate-200 p-3 rounded-xl font-bold text-xs uppercase" value={editPaterno} onChange={(e)=>setEditPaterno(e.target.value)} />
                                <input type="text" className="bg-slate-50 border border-slate-200 p-3 rounded-xl font-bold text-xs uppercase" value={editMaterno} onChange={(e)=>setEditMaterno(e.target.value)} />
                            </div>
                            <input type="text" className="w-full bg-slate-50 border border-slate-200 p-3 rounded-xl font-bold text-xs" value={editMatricula} onChange={(e)=>setEditMatricula(e.target.value)} />
                            <select className="w-full bg-slate-50 border border-slate-200 p-3 rounded-xl font-black text-[10px] cursor-pointer" value={editCarrera} onChange={(e)=>setEditCarrera(e.target.value)}>
                                {CARRERAS.map(c => <option key={c.clave} value={c.clave}>{c.nombre}</option>)}
                            </select>
                            <button onClick={handleUpdateDatabase} className="w-full bg-slate-900 text-white py-4 rounded-2xl font-black text-[10px] tracking-[0.2em] shadow-xl hover:bg-black transition-all flex justify-center items-center gap-2">
                                <Save size={16} /> GUARDAR EN SQL
                            </button>
                            <button onClick={()=>setEditModalOpen(false)} className="w-full text-slate-400 font-black text-[9px] tracking-widest py-2">CANCELAR</button>
                        </div>
                    </div>
                </div>
            )}

            {/* MODAL HUELLA */}
            {fpModalOpen && alumnoAEditar && (
                <FingerprintModal
                    isOpen={fpModalOpen}
                    onClose={() => setFpModalOpen(false)}
                    onComplete={() => { setFpModalOpen(false); setShowToast(true); setTimeout(() => setShowToast(false), 3000); fetchAsistencias(); }}
                    studentId={alumnoAEditar.alumno_id}
                    readerId={currentReaderId}
                />
            )}
        </div>
    );
}
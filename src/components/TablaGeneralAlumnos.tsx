'use client';

import React, { useState, useEffect } from 'react';
import { Search, Edit, ChevronLeft, ChevronRight, CheckCircle, XCircle, Fingerprint, ScanFace } from 'lucide-react';
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import ModalEditarAlumno from './ModalEditarAlumno';
import FingerprintModal from './FingerprintModal';

// === CONFIGURACIÓN ===
const DEFAULT_READER_ID = "0";

interface Alumno {
    id_alumno: number;
    uuid: string;
    primerNombre: string;
    apellidoPaterno: string;
    apellidoMaterno: string;
    numeroControl: string;
    carreraClave: string;
    activo: number;
    huellaFmd?: string;
}

export default function TablaGeneralAlumnos() {
    const [alumnos, setAlumnos] = useState<Alumno[]>([]);
    const [filtro, setFiltro] = useState('');
    const [loading, setLoading] = useState(true);
    const [paginaActual, setPaginaActual] = useState(1);
    const [itemsPorPagina] = useState(7);

    // Estado para Modal de Edición
    const [alumnoAEditar, setAlumnoAEditar] = useState<Alumno | null>(null);
    const [modalEditarAbierto, setModalEditarAbierto] = useState(false);

    // Estado para Modal de Huella
    const [alumnoParaHuella, setAlumnoParaHuella] = useState<Alumno | null>(null);
    const [modalHuellaAbierto, setModalHuellaAbierto] = useState(false);

    const cargarAlumnos = async () => {
        setLoading(true);
        try {
            const res = await fetch('http://localhost:8080/api/alumnos');
            if (res.ok) {
                const data = await res.json();
                setAlumnos(data);
            }
        } catch (error) {
            console.error("Error cargando alumnos:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        cargarAlumnos();
    }, []);

    // Lógica de filtrado
    const alumnosFiltrados = alumnos.filter(a =>
        (a.primerNombre + ' ' + a.apellidoPaterno + ' ' + a.numeroControl).toLowerCase().includes(filtro.toLowerCase())
    );

    // Lógica de paginación
    const indiceUltimoItem = paginaActual * itemsPorPagina;
    const indicePrimerItem = indiceUltimoItem - itemsPorPagina;
    const alumnosPaginados = alumnosFiltrados.slice(indicePrimerItem, indiceUltimoItem);
    const totalPaginas = Math.ceil(alumnosFiltrados.length / itemsPorPagina);

    // Handlers
    const handleEditar = (alumno: Alumno) => {
        setAlumnoAEditar(alumno);
        setModalEditarAbierto(true);
    };

    const handleHuella = (alumno: Alumno) => {
        setAlumnoParaHuella(alumno);
        setModalHuellaAbierto(true);
    };

    const cambiarPagina = (nuevaPagina: number) => {
        if (nuevaPagina >= 1 && nuevaPagina <= totalPaginas) {
            setPaginaActual(nuevaPagina);
        }
    };

    return (
        <div className="space-y-4">
            {/* Barra de búsqueda */}
            <div className="flex gap-2 bg-white p-4 rounded-xl shadow-sm border border-slate-200">
                <Search className="text-slate-400" />
                <Input
                    placeholder="Buscar por nombre, apellido o número de control..."
                    value={filtro}
                    onChange={(e) => { setFiltro(e.target.value); setPaginaActual(1); }}
                    className="border-none shadow-none focus-visible:ring-0 text-base"
                />
            </div>

            {/* Tabla */}
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <Table>
                    <TableHeader>
                        <TableRow className="bg-slate-50 hover:bg-slate-50">
                            <TableHead className="font-bold text-slate-700">No. Control</TableHead>
                            <TableHead className="font-bold text-slate-700">Estudiante</TableHead>
                            <TableHead className="font-bold text-slate-700">Carrera</TableHead>
                            <TableHead className="font-bold text-slate-700">Estatus</TableHead>
                            <TableHead className="font-bold text-slate-700">Biometría</TableHead>
                            <TableHead className="text-right font-bold text-slate-700 pr-6">Acciones</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {loading ? (
                            <TableRow>
                                <TableCell colSpan={6} className="text-center py-10 text-slate-500">
                                    Cargando directorio...
                                </TableCell>
                            </TableRow>
                        ) : alumnosPaginados.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={6} className="text-center py-10 text-slate-500">
                                    No se encontraron resultados.
                                </TableCell>
                            </TableRow>
                        ) : (
                            alumnosPaginados.map((alumno) => (
                                <TableRow key={alumno.id_alumno} className="hover:bg-slate-50 transition-colors">
                                    <TableCell className="font-mono font-medium text-slate-600">
                                        {alumno.numeroControl}
                                    </TableCell>
                                    <TableCell>
                                        <div className="flex flex-col">
                                            <span className="font-semibold text-slate-900">
                                                {alumno.primerNombre} {alumno.apellidoPaterno}
                                            </span>
                                            <span className="text-xs text-slate-500">{alumno.apellidoMaterno}</span>
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant="outline" className="bg-indigo-50 text-indigo-700 border-indigo-200">
                                            {alumno.carreraClave}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        <Badge className={`${alumno.activo === 1 ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100' : 'bg-red-100 text-red-700 hover:bg-red-100'} border-none shadow-none`}>
                                            {alumno.activo === 1 ? (
                                                <><CheckCircle size={12} className="mr-1" /> Activo</>
                                            ) : (
                                                <><XCircle size={12} className="mr-1" /> Baja</>
                                            )}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        {alumno.huellaFmd ? (
                                            <div className="flex items-center text-emerald-600 text-xs font-bold gap-1 bg-emerald-50 w-fit px-2 py-1 rounded-full">
                                                <Fingerprint size={14} /> Registrada
                                            </div>
                                        ) : (
                                            <div className="flex items-center text-amber-600 text-xs font-bold gap-1 bg-amber-50 w-fit px-2 py-1 rounded-full">
                                                <ScanFace size={14} /> Pendiente
                                            </div>
                                        )}
                                    </TableCell>

                                    {/* COLUMNA DE ACCIONES */}
                                    <TableCell className="text-right">
                                        <div className="flex justify-end items-center gap-1">
                                            {/* BOTÓN DE HUELLA */}
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => handleHuella(alumno)}
                                                className="text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 h-8 px-2"
                                                title="Registrar huella"
                                            >
                                                <Fingerprint size={18} />
                                            </Button>

                                            {/* BOTÓN DE EDITAR */}
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => handleEditar(alumno)}
                                                className="text-blue-600 hover:text-blue-700 hover:bg-blue-50 h-8 px-2"
                                                title="Editar información"
                                            >
                                                <Edit size={18} />
                                            </Button>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>

                {/* Paginación */}
                <div className="flex items-center justify-between px-4 py-4 border-t border-slate-100 bg-slate-50">
                    <span className="text-sm text-slate-500">
                        Mostrando {indicePrimerItem + 1} - {Math.min(indiceUltimoItem, alumnosFiltrados.length)} de {alumnosFiltrados.length}
                    </span>
                    <div className="flex gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => cambiarPagina(paginaActual - 1)}
                            disabled={paginaActual === 1}
                        >
                            <ChevronLeft size={16} /> Anterior
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => cambiarPagina(paginaActual + 1)}
                            disabled={paginaActual === totalPaginas}
                        >
                            Siguiente <ChevronRight size={16} />
                        </Button>
                    </div>
                </div>
            </div>

            {/* Modal de Edición */}
            <ModalEditarAlumno
                alumno={alumnoAEditar}
                isOpen={modalEditarAbierto}
                onClose={() => setModalEditarAbierto(false)}
                onSave={cargarAlumnos}
            />

            {/* Modal de Huella Dactilar */}
            {alumnoParaHuella && (
                <FingerprintModal
                    isOpen={modalHuellaAbierto}
                    onClose={() => setModalHuellaAbierto(false)}
                    studentId={alumnoParaHuella.id_alumno}
                    readerId={DEFAULT_READER_ID}
                    onComplete={() => {
                        setModalHuellaAbierto(false);
                        cargarAlumnos(); // Recarga para actualizar el icono de biometría
                    }}
                />
            )}
        </div>
    );
}
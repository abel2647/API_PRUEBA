'use client';

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import {
    Search, RefreshCw, Eraser, History, DoorOpen, Calendar, User,
    ChevronLeft, ChevronRight, LogOut, ScanLine, Loader2, CheckCircle, XCircle, X
} from 'lucide-react';

// --- INTERFACES ---
interface VisitanteResumen {
    id: number;
    primerNombre: string;
    apellidoPaterno: string;
    apellidoMaterno: string | null;
    asunto: string;
    fechaCreacion: string;
    ultimaFechaEscaneo: string | null;
    totalEntradas: number;
    totalSalidas: number; //
    ultimaPuerta: string;
    fechaExpiracion: string | null;
}

interface ResultadoSalida {
    acceso: boolean; // true = Salida Exitosa, false = Error
    mensaje: string;
    visitante: string | null;
    asunto: string | null;
    totalAccesos?: number; // Nuevo
    totalSalidas?: number; // Nuevo
    puerta?: string;       // Nuevo
}

export const HistorialVisitantes = () => {
    // --- ESTADOS DE DATOS ---
    const [visitantes, setVisitantes] = useState<VisitanteResumen[]>([]);
    const [loading, setLoading] = useState(false);

    // --- ESTADOS DE FILTROS ---
    const [fNombre, setFNombre] = useState('');
    const [fPaterno, setFPaterno] = useState('');
    const [fMaterno, setFMaterno] = useState('');
    const [fFecha, setFFecha] = useState('');
    const [fPuerta, setFPuerta] = useState('todas');

    // --- PAGINACIÓN ---
    const [paginaActual, setPaginaActual] = useState(1);
    const itemsPorPagina = 10;

    // --- ESTADOS DEL MODAL DE SALIDA ---
    const [modalOpen, setModalOpen] = useState(false);
    const [visitanteSeleccionado, setVisitanteSeleccionado] = useState<string | null>(null); // Nombre del que esperamos
    const [scanInput, setScanInput] = useState('');
    const [procesandoSalida, setProcesandoSalida] = useState(false);
    const [resultadoSalida, setResultadoSalida] = useState<ResultadoSalida | null>(null);
    const inputRef = useRef<HTMLInputElement>(null);

    // --- CARGAR DATOS ---
    const cargarDatos = useCallback(async () => {
        setLoading(true);
        try {
            const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
            const res = await fetch(`${apiUrl}/api/visitante/historial`);
            if (res.ok) {
                const data = await res.json();
                setVisitantes(data);
            }
        } catch (error) {
            console.error("Error cargando historial:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { cargarDatos(); }, [cargarDatos]);

    // --- LÓGICA MODAL SALIDA ---
    const abrirModalSalida = (nombre: string) => {
        setVisitanteSeleccionado(nombre);
        setResultadoSalida(null);
        setScanInput('');
        setModalOpen(true);
        // Foco automático
        setTimeout(() => inputRef.current?.focus(), 100);
    };

    const cerrarModal = () => {
        setModalOpen(false);
        setResultadoSalida(null);
        setScanInput('');
        cargarDatos(); // Recargar tabla al cerrar para ver cambios
    };

    // Auto-escaneo al escribir en el modal
    useEffect(() => {
        if (modalOpen && scanInput.length > 5 && !procesandoSalida && !resultadoSalida) {
            const timer = setTimeout(() => procesarSalida(scanInput), 800);
            return () => clearTimeout(timer);
        }
    }, [scanInput, modalOpen, procesandoSalida, resultadoSalida]);

    const procesarSalida = async (uuid: string) => {
        setProcesandoSalida(true);
        try {
            const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
            const res = await fetch(`${apiUrl}/api/visitante/salida`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ uuid })
            });

            if (res.ok) {
                const data: ResultadoSalida = await res.json();
                setResultadoSalida(data);
            } else {
                setResultadoSalida({
                    acceso: false,
                    mensaje: "ERROR DE SERVIDOR",
                    visitante: null,
                    asunto: null
                });
            }
        } catch (error) {
            console.error(error);
            setResultadoSalida({
                acceso: false,
                mensaje: "ERROR DE CONEXIÓN",
                visitante: null,
                asunto: null
            });
        } finally {
            setProcesandoSalida(false);
            setScanInput('');
        }
    };

    // --- HELPERS VISUALES ---
    const esVigente = (fechaExpiracionStr: string | null) => {
        if (!fechaExpiracionStr) return false;
        return new Date(fechaExpiracionStr) > new Date();
    };

    const getBadgeEstatus = (fechaExpiracionStr: string | null) => {
        if (!fechaExpiracionStr) return <Badge variant="outline" className="text-gray-400">Sin Pase</Badge>;
        return esVigente(fechaExpiracionStr)
            ? <Badge className="bg-green-600 hover:bg-green-700">VIGENTE</Badge>
            : <Badge variant="destructive">VENCIDO</Badge>;
    };

    const formatearFecha = (fecha: string | null) => {
        if (!fecha) return '--';
        return new Date(fecha).toLocaleString('es-MX', {
            day: '2-digit', month: '2-digit', year: '2-digit', hour: '2-digit', minute: '2-digit'
        });
    };

    const limpiarFiltros = () => {
        setFNombre(''); setFPaterno(''); setFMaterno(''); setFFecha(''); setFPuerta('todas'); setPaginaActual(1);
    };

    // --- FILTRADO Y PAGINACIÓN ---
    const datosFiltrados = visitantes.filter(v => {
        const matchNombre = v.primerNombre.toLowerCase().includes(fNombre.toLowerCase());
        const matchPaterno = v.apellidoPaterno.toLowerCase().includes(fPaterno.toLowerCase());
        const matchMaterno = (v.apellidoMaterno || '').toLowerCase().includes(fMaterno.toLowerCase());
        let matchFecha = true;
        if (fFecha) { matchFecha = v.fechaCreacion.split('T')[0] === fFecha; }
        let matchPuerta = true;
        if (fPuerta !== 'todas') { matchPuerta = v.ultimaPuerta.toLowerCase().includes(fPuerta.toLowerCase()); }
        return matchNombre && matchPaterno && matchMaterno && matchFecha && matchPuerta;
    });

    const totalPaginas = Math.ceil(datosFiltrados.length / itemsPorPagina);
    const indiceInicio = (paginaActual - 1) * itemsPorPagina;
    const datosPaginados = datosFiltrados.slice(indiceInicio, indiceInicio + itemsPorPagina);

    return (
        <>
            <Card className="w-full shadow-lg border-0 bg-white">
                <CardHeader className="bg-gray-50/80 border-b pb-4">
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                        <CardTitle className="text-2xl font-black text-gray-800 flex items-center gap-2">
                            <History className="w-6 h-6 text-blue-700"/>
                            CONSULTA DE VISITANTES
                        </CardTitle>
                        <Button variant="outline" size="sm" onClick={cargarDatos} disabled={loading} className="hover:bg-blue-50 border-blue-200 text-blue-700">
                            <RefreshCw className={`w-4 h-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
                            Actualizar
                        </Button>
                    </div>

                    {/* FILTROS */}
                    <div className="grid grid-cols-1 md:grid-cols-6 gap-3 mt-4 pt-2">
                        <div className="relative">
                            <User className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
                            <Input placeholder="Nombre" className="pl-8 h-9" value={fNombre} onChange={e => setFNombre(e.target.value)} />
                        </div>
                        <Input placeholder="A. Paterno" className="h-9" value={fPaterno} onChange={e => setFPaterno(e.target.value)} />
                        <Input placeholder="A. Materno" className="h-9" value={fMaterno} onChange={e => setFMaterno(e.target.value)} />
                        <div className="relative">
                            <Calendar className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-400" />
                            <Input type="date" className="pl-8 h-9" value={fFecha} onChange={e => setFFecha(e.target.value)} />
                        </div>
                        <Select value={fPuerta} onValueChange={setFPuerta}>
                            <SelectTrigger className="h-9">
                                <DoorOpen className="w-4 h-4 mr-2 text-gray-400"/>
                                <SelectValue placeholder="Puerta" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="todas">Todas</SelectItem>
                                <SelectItem value="1">Puerta 1</SelectItem>
                                <SelectItem value="2">Puerta 2</SelectItem>
                                <SelectItem value="Sin registro">Sin Registro</SelectItem>
                            </SelectContent>
                        </Select>
                        <Button variant="ghost" onClick={limpiarFiltros} className="h-9 text-red-600 hover:bg-red-50 hover:text-red-700 border border-transparent hover:border-red-200">
                            <Eraser className="w-4 h-4 mr-2" />
                            Limpiar
                        </Button>
                    </div>
                </CardHeader>

                <CardContent className="p-0">
                    <div className="border-t min-h-[400px]">
                        <Table>
                            <TableHeader className="bg-gray-100/50">
                                <TableRow>
                                    <TableHead className="font-bold text-gray-700">NOMBRE COMPLETO</TableHead>
                                    <TableHead className="font-bold text-gray-700">ASUNTO</TableHead>
                                    <TableHead className="font-bold text-gray-700">CREACIÓN</TableHead>
                                    <TableHead className="font-bold text-gray-700 text-center">ÚLTIMO ESCANEO</TableHead>
                                    <TableHead className="font-bold text-gray-700 text-center">ACCESOS</TableHead>
                                    <TableHead className="font-bold text-gray-700 text-center">SALIDAS</TableHead>
                                    <TableHead className="font-bold text-gray-700 text-center">PUERTA</TableHead>
                                    <TableHead className="font-bold text-gray-700 text-center">ESTATUS</TableHead>
                                    <TableHead className="font-bold text-gray-700 text-center">ACCIONES</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {datosFiltrados.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={8} className="text-center h-32 text-gray-500">
                                            <div className="flex flex-col items-center justify-center gap-2">
                                                <Search className="w-8 h-8 opacity-20"/>
                                                <p>No se encontraron registros con esos filtros</p>
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    datosPaginados.map((v) => (
                                        <TableRow key={v.id} className="hover:bg-blue-50/30 transition-colors">
                                            <TableCell className="font-medium uppercase text-gray-700">
                                                {v.primerNombre} {v.apellidoPaterno} {v.apellidoMaterno}
                                            </TableCell>
                                            <TableCell className="uppercase text-xs font-semibold text-gray-500">
                                                {v.asunto}
                                            </TableCell>
                                            <TableCell className="text-xs text-gray-600 font-mono">
                                                {formatearFecha(v.fechaCreacion)}
                                            </TableCell>
                                            <TableCell className="text-center text-xs text-gray-600 font-mono">
                                                {formatearFecha(v.ultimaFechaEscaneo)}
                                            </TableCell>
                                            <TableCell className="text-center">
                                                <Badge variant="secondary" className="font-mono font-bold bg-gray-200 text-gray-700">
                                                    {v.totalEntradas}
                                                </Badge>
                                            </TableCell>
                                            {/* CELDA NUEVA */}
                                            <TableCell className="text-center">
                                                <Badge variant="outline" className="font-mono font-bold border-blue-200 text-blue-700 bg-blue-50">
                                                    {v.totalSalidas}
                                                </Badge>
                                            </TableCell>
                                            <TableCell className="text-center text-xs font-bold uppercase text-blue-700">
                                                {v.ultimaPuerta}
                                            </TableCell>
                                            <TableCell className="text-center">
                                                {getBadgeEstatus(v.fechaExpiracion)}
                                            </TableCell>
                                            <TableCell className="text-center">
                                                {esVigente(v.fechaExpiracion) && (
                                                    <Button
                                                        variant="destructive"
                                                        size="sm"
                                                        className="h-7 text-xs bg-red-600 hover:bg-red-700 text-white shadow-sm"
                                                        onClick={() => abrirModalSalida(v.primerNombre)}
                                                    >
                                                        <LogOut className="w-3 h-3 mr-1" />
                                                        Terminar
                                                    </Button>
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>
                    </div>

                    {/* PAGINACIÓN FOOTER */}
                    <div className="p-4 border-t bg-gray-50 flex items-center justify-between">
                        <div className="text-xs text-gray-500 font-medium">
                            Mostrando {indiceInicio + 1} - {Math.min(indiceInicio + itemsPorPagina, datosFiltrados.length)} de {datosFiltrados.length} resultados
                        </div>
                        <div className="flex items-center gap-2">
                            <Button variant="outline" size="sm" onClick={() => setPaginaActual(p => Math.max(1, p - 1))} disabled={paginaActual === 1} className="h-8 w-8 p-0">
                                <ChevronLeft className="h-4 w-4" />
                            </Button>
                            <span className="text-xs font-bold text-gray-700 w-16 text-center">Página {paginaActual} de {Math.max(1, totalPaginas)}</span>
                            <Button variant="outline" size="sm" onClick={() => setPaginaActual(p => Math.min(totalPaginas, p + 1))} disabled={paginaActual >= totalPaginas} className="h-8 w-8 p-0">
                                <ChevronRight className="h-4 w-4" />
                            </Button>
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* --- MODAL DE SALIDA --- */}
            <Dialog open={modalOpen} onOpenChange={(open) => !open && cerrarModal()}>
                <DialogContent
                    className="sm:max-w-[450px] min-h-[300px] flex flex-col justify-center"
                    onOpenAutoFocus={(e) => { e.preventDefault(); setTimeout(() => inputRef.current?.focus(), 100); }}
                >
                    {!resultadoSalida ? (
                        // VISTA DE ESCANEO
                        <div className="flex flex-col items-center justify-center space-y-6 animate-in fade-in zoom-in-95 duration-300">
                            <DialogHeader>
                                <DialogTitle className="text-2xl font-bold text-center text-gray-800">Registrar Salida</DialogTitle>
                                <DialogDescription className="text-center">
                                    Escanea el código QR de <span className="font-bold text-black">{visitanteSeleccionado}</span> para finalizar su visita.
                                </DialogDescription>
                            </DialogHeader>

                            <div className="relative w-full max-w-sm">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <ScanLine className={`h-6 w-6 ${procesandoSalida ? 'text-blue-500 animate-pulse' : 'text-gray-400'}`} />
                                </div>
                                <Input
                                    ref={inputRef}
                                    placeholder={procesandoSalida ? "Procesando..." : "Escanea QR aquí..."}
                                    value={scanInput}
                                    onChange={(e) => setScanInput(e.target.value)}
                                    disabled={procesandoSalida}
                                    autoComplete="off"
                                    className="pl-12 text-xl h-14 border-2 border-blue-100 focus-visible:ring-blue-600 font-mono text-center shadow-inner"
                                />
                                {procesandoSalida && (
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                                        <Loader2 className="h-5 w-5 animate-spin text-blue-600" />
                                    </div>
                                )}
                            </div>
                            <p className="text-xs text-gray-400">El sistema procesará la salida automáticamente.</p>
                        </div>
                    ) : (
                        // VISTA DE RESULTADO (ACTUALIZADA CON CONTADORES)
                        <div className="animate-in zoom-in-95 duration-300 w-full">
                            <Card className={`border-4 ${resultadoSalida.acceso ? 'border-green-500' : 'border-red-500'} shadow-xl`}>
                                <CardHeader className="pb-2 bg-gray-50/50">
                                    <div className="flex flex-col items-center justify-center gap-2">
                                        {resultadoSalida.acceso ?
                                            <CheckCircle className="h-16 w-16 text-green-600" /> :
                                            <XCircle className="h-16 w-16 text-red-600" />
                                        }
                                        <CardTitle className={`text-2xl font-black uppercase text-center ${resultadoSalida.acceso ? 'text-green-700' : 'text-red-700'}`}>
                                            {resultadoSalida.mensaje}
                                        </CardTitle>
                                    </div>
                                </CardHeader>

                                <CardContent className="pt-4 text-center space-y-4">
                                    {resultadoSalida.visitante ? (
                                        <>
                                            <div>
                                                <p className="text-xs text-gray-500 uppercase font-bold tracking-wider">Visitante</p>
                                                <p className="font-bold text-xl uppercase text-gray-800">{resultadoSalida.visitante}</p>
                                            </div>

                                            {/* GRID DE ESTADÍSTICAS */}
                                            <div className="grid grid-cols-3 gap-2 bg-gray-100 p-3 rounded-lg">
                                                <div className="flex flex-col">
                                                    <span className="text-[10px] uppercase text-gray-500 font-bold">Entradas</span>
                                                    <span className="text-lg font-mono font-bold text-blue-700">{resultadoSalida.totalAccesos || 0}</span>
                                                </div>
                                                <div className="flex flex-col border-l border-r border-gray-300">
                                                    <span className="text-[10px] uppercase text-gray-500 font-bold">Salidas</span>
                                                    <span className="text-lg font-mono font-bold text-orange-600">{resultadoSalida.totalSalidas || 0}</span>
                                                </div>
                                                <div className="flex flex-col">
                                                    <span className="text-[10px] uppercase text-gray-500 font-bold">Puerta</span>
                                                    <span className="text-lg font-mono font-bold text-gray-700">{resultadoSalida.puerta || '--'}</span>
                                                </div>
                                            </div>

                                            {resultadoSalida.acceso && (
                                                <p className="text-xs font-medium text-gray-400">Pase desactivado correctamente</p>
                                            )}
                                        </>
                                    ) : (
                                        <p className="text-gray-500">No se encontraron datos del visitante.</p>
                                    )}
                                </CardContent>

                                <CardFooter className="pt-2 pb-4 flex justify-center bg-gray-50">
                                    <Button
                                        onClick={cerrarModal}
                                        size="lg"
                                        className={`w-full font-bold ${resultadoSalida.acceso ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'}`}
                                    >
                                        <X className="mr-2 h-5 w-5" />
                                        CERRAR VENTANA
                                    </Button>
                                </CardFooter>
                            </Card>
                        </div>
                    )}
                </DialogContent>
            </Dialog>
        </>
    );
};
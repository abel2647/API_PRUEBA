'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Search, RefreshCw, Eraser, History, DoorOpen, Calendar, User, ChevronLeft, ChevronRight } from 'lucide-react';

interface VisitanteResumen {
    id: number;
    primerNombre: string;
    apellidoPaterno: string;
    apellidoMaterno: string | null;
    asunto: string;
    fechaCreacion: string;
    ultimaFechaEscaneo: string | null;
    totalEntradas: number;
    ultimaPuerta: string;
    fechaExpiracion: string | null;
}

export const HistorialVisitantes = () => {
    const [visitantes, setVisitantes] = useState<VisitanteResumen[]>([]);
    const [loading, setLoading] = useState(false);

    // --- ESTADOS DE FILTROS ---
    const [fNombre, setFNombre] = useState('');
    const [fPaterno, setFPaterno] = useState('');
    const [fMaterno, setFMaterno] = useState('');
    const [fFecha, setFFecha] = useState('');
    const [fPuerta, setFPuerta] = useState('todas');

    // --- ESTADOS DE PAGINACIÓN (NUEVO) ---
    const [paginaActual, setPaginaActual] = useState(1);
    const itemsPorPagina = 10; // Muestra 10 filas por página

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

    useEffect(() => {
        cargarDatos();
    }, [cargarDatos]);

    // Resetea a la página 1 cuando cambian los filtros
    useEffect(() => {
        setPaginaActual(1);
    }, [fNombre, fPaterno, fMaterno, fFecha, fPuerta]);

    const limpiarFiltros = () => {
        setFNombre('');
        setFPaterno('');
        setFMaterno('');
        setFFecha('');
        setFPuerta('todas');
        setPaginaActual(1);
    };

    // ... (Lógica de Estatus y Fecha igual que antes) ...
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

    // --- FILTRADO ---
    const datosFiltrados = visitantes.filter(v => {
        const matchNombre = v.primerNombre.toLowerCase().includes(fNombre.toLowerCase());
        const matchPaterno = v.apellidoPaterno.toLowerCase().includes(fPaterno.toLowerCase());
        const matchMaterno = (v.apellidoMaterno || '').toLowerCase().includes(fMaterno.toLowerCase());

        let matchFecha = true;
        if (fFecha) {
            const fechaItem = v.fechaCreacion.split('T')[0];
            matchFecha = fechaItem === fFecha;
        }

        let matchPuerta = true;
        if (fPuerta !== 'todas') {
            matchPuerta = v.ultimaPuerta.toLowerCase().includes(fPuerta.toLowerCase());
        }

        return matchNombre && matchPaterno && matchMaterno && matchFecha && matchPuerta;
    });

    // --- LÓGICA DE PAGINACIÓN (NUEVO) ---
    const totalPaginas = Math.ceil(datosFiltrados.length / itemsPorPagina);
    const indiceInicio = (paginaActual - 1) * itemsPorPagina;
    const datosPaginados = datosFiltrados.slice(indiceInicio, indiceInicio + itemsPorPagina);

    return (
        <Card className="w-full shadow-lg border-0 bg-white">
            <CardHeader className="bg-gray-50/80 border-b pb-4">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <CardTitle className="text-2xl font-black text-gray-800 flex items-center gap-2">
                        <History className="w-6 h-6 text-blue-700"/>
                        CONSULTA DE VISITANTES
                    </CardTitle>

                </div>

                {/* BARRA DE FILTROS */}
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
                                <TableHead className="font-bold text-gray-700 text-center">PUERTA</TableHead>
                                <TableHead className="font-bold text-gray-700 text-center">ESTATUS</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {datosFiltrados.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={7} className="text-center h-32 text-gray-500">
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
                                        <TableCell className="text-center text-xs font-bold uppercase text-blue-700">
                                            {v.ultimaPuerta}
                                        </TableCell>
                                        <TableCell className="text-center">
                                            {getBadgeEstatus(v.fechaExpiracion)}
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </div>

                {/* --- FOOTER CON PAGINACIÓN --- */}
                <div className="p-4 border-t bg-gray-50 flex items-center justify-between">
                    <div className="text-xs text-gray-500 font-medium">
                        Mostrando {indiceInicio + 1} - {Math.min(indiceInicio + itemsPorPagina, datosFiltrados.length)} de {datosFiltrados.length} resultados
                    </div>

                    <div className="flex items-center gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setPaginaActual(p => Math.max(1, p - 1))}
                            disabled={paginaActual === 1}
                            className="h-8 w-8 p-0"
                        >
                            <ChevronLeft className="h-4 w-4" />
                        </Button>
                        <span className="text-xs font-bold text-gray-700 w-16 text-center">
                            Página {paginaActual} de {Math.max(1, totalPaginas)}
                        </span>
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setPaginaActual(p => Math.min(totalPaginas, p + 1))}
                            disabled={paginaActual >= totalPaginas}
                            className="h-8 w-8 p-0"
                        >
                            <ChevronRight className="h-4 w-4" />
                        </Button>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
};
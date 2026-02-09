'use client';

import React, { useState, useEffect } from 'react';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, Legend, ResponsiveContainer,
    AreaChart, Area
} from 'recharts';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Search, Users, UserCheck, User, RefreshCcw, Eraser, FileSpreadsheet, DoorOpen, FileText } from 'lucide-react';

// --- INTERFACES ---
interface DatosPuerta {
    puerta: string;
    total: number;
}

interface DatoHorario {
    hora: string;
    alumnos: number;
    visitantes: number;
}

interface DashboardData {
    totalAlumnosHoy: number;
    totalVisitantesHoy: number;
    entradasPorPuerta: DatosPuerta[];
    asistenciaSemanal: DatoHorario[];
}

export const Estadisticas = () => {
    const fechaHoy = new Date().toLocaleDateString('en-CA');

    // --- ESTADOS ---
    const [data, setData] = useState<DashboardData>({
        totalAlumnosHoy: 0,
        totalVisitantesHoy: 0,
        entradasPorPuerta: [],
        asistenciaSemanal: []
    });

    const [loading, setLoading] = useState(false);

    // Estado de los filtros
    const [filtros, setFiltros] = useState({
        fecha: fechaHoy,
        horaInicio: '',
        horaFin: '',
        tipo: 'TODOS',
        puerta: 'TODAS' // Nuevo estado para la puerta
    });

    // --- FUNCIONES ---

    const obtenerEstadisticas = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams();
            if (filtros.fecha) params.append('fecha', filtros.fecha);
            if (filtros.horaInicio) params.append('horaInicio', filtros.horaInicio);
            if (filtros.horaFin) params.append('horaFin', filtros.horaFin);
            if (filtros.tipo) params.append('tipo', filtros.tipo);
            if (filtros.puerta && filtros.puerta !== 'TODAS') params.append('puerta', filtros.puerta);

            const response = await fetch(`http://localhost:8080/api/dashboard/filtrado?${params.toString()}`);

            if (response.ok) {
                const result = await response.json();
                setData(result);
            } else {
                console.error("Error al obtener estadísticas");
            }
        } catch (error) {
            console.error("Error de conexión:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleDescargarExcel = async () => {
        try {
            const params = new URLSearchParams();
            if (filtros.fecha) params.append('fecha', filtros.fecha);
            if (filtros.horaInicio) params.append('horaInicio', filtros.horaInicio);
            if (filtros.horaFin) params.append('horaFin', filtros.horaFin);
            if (filtros.tipo) params.append('tipo', filtros.tipo);
            if (filtros.puerta && filtros.puerta !== 'TODAS') params.append('puerta', filtros.puerta);

            const response = await fetch(`http://localhost:8080/api/dashboard/exportar-excel?${params.toString()}`, {
                method: 'GET',
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `Reporte de accesos del ${filtros.fecha || 'General'}.xlsx`;
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(url);
            } else {
                alert("No se pudo descargar el reporte.");
            }
        } catch (error) {
            console.error("Error de red al descargar:", error);
        }
    };

    useEffect(() => {
        obtenerEstadisticas();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleFiltroChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFiltros({
            ...filtros,
            [e.target.name]: e.target.value
        });
    };

    const handleSelectChange = (value: string, campo: string) => {
        setFiltros({
            ...filtros,
            [campo]: value
        });
    }

    const aplicarFiltros = () => {
        obtenerEstadisticas();
    };

    const limpiarFiltros = () => {
        setFiltros({
            fecha: fechaHoy,
            horaInicio: '',
            horaFin: '',
            tipo: 'TODOS',
            puerta: 'TODAS'
        });
    };

    const handleDescargarPdf = async () => {
        try {
            const params = new URLSearchParams();
            if (filtros.fecha) params.append('fecha', filtros.fecha);
            if (filtros.horaInicio) params.append('horaInicio', filtros.horaInicio);
            if (filtros.horaFin) params.append('horaFin', filtros.horaFin);
            if (filtros.tipo) params.append('tipo', filtros.tipo);
            if (filtros.puerta && filtros.puerta !== 'TODAS') params.append('puerta', filtros.puerta);

            const response = await fetch(`http://localhost:8080/api/dashboard/exportar-pdf?${params.toString()}`, {
                method: 'GET',
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `Reporte de accesos del ${filtros.fecha || 'General'}.pdf`;
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(url);
            } else {
                alert("No se pudo descargar el reporte PDF.");
            }
        } catch (error) {
            console.error("Error de red al descargar PDF:", error);
        }
    };

    return (
        <div className="space-y-6 p-4 md:p-8">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-3xl font-bold tracking-tight">Dashboard de Asistencia</h2>
                    <p className="text-muted-foreground">Estadísticas de entradas de Alumnos y Visitantes.</p>
                </div>
            </div>

            {/* --- SECCIÓN DE FILTROS --- */}
            <Card>
                <CardHeader>
                    <CardTitle className="text-lg flex items-center gap-2">
                        <Search className="w-5 h-5" />
                        Filtros de Búsqueda
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-4 items-end">

                        {/* Fecha */}
                        <div className="space-y-2">
                            <Label htmlFor="fecha">Fecha</Label>
                            <Input
                                id="fecha"
                                name="fecha"
                                type="date"
                                value={filtros.fecha}
                                onChange={handleFiltroChange}
                            />
                        </div>

                        {/* Hora Inicio */}
                        <div className="space-y-2">
                            <Label htmlFor="horaInicio">Hora Inicio</Label>
                            <Input
                                id="horaInicio"
                                name="horaInicio"
                                type="time"
                                value={filtros.horaInicio}
                                onChange={handleFiltroChange}
                            />
                        </div>

                        {/* Hora Fin */}
                        <div className="space-y-2">
                            <Label htmlFor="horaFin">Hora Fin</Label>
                            <Input
                                id="horaFin"
                                name="horaFin"
                                type="time"
                                value={filtros.horaFin}
                                onChange={handleFiltroChange}
                            />
                        </div>

                        {/* Tipo de Persona */}
                        <div className="space-y-2">
                            <Label>Tipo</Label>
                            <Select value={filtros.tipo} onValueChange={(val) => handleSelectChange(val, 'tipo')}>
                                <SelectTrigger>
                                    <SelectValue placeholder="Seleccionar" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="TODOS">Todos</SelectItem>
                                    <SelectItem value="ALUMNO">Alumnos</SelectItem>
                                    <SelectItem value="VISITANTE">Visitantes</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        {/* Filtro Puerta (NUEVO) */}
                        <div className="space-y-2">
                            <Label>Puerta</Label>
                            <Select value={filtros.puerta} onValueChange={(val) => handleSelectChange(val, 'puerta')}>
                                <SelectTrigger>
                                    <SelectValue placeholder="Todas" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="TODAS">Todas</SelectItem>
                                    <SelectItem value="1">Puerta 1</SelectItem>
                                    <SelectItem value="2">Puerta 2</SelectItem>
                                    <SelectItem value="3">Puerta 3</SelectItem>
                                    <SelectItem value="4">Puerta 4</SelectItem>
                                    <SelectItem value="5">Puerta 5</SelectItem>
                                    <SelectItem value="6">Puerta 6</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        {/* BOTONES DE ACCIÓN */}
                        <div className="flex flex-col gap-2">
                            <div className="flex gap-2">
                                <Button
                                    className="flex-1 bg-blue-600 hover:bg-blue-700 text-white"
                                    onClick={aplicarFiltros}
                                >
                                    <Search className="w-4 h-4 mr-2" />
                                    Filtrar
                                </Button>
                                <Button
                                    variant="outline"
                                    size="icon"
                                    onClick={limpiarFiltros}
                                    title="Limpiar filtros"
                                >
                                    <Eraser className="w-4 h-4" />
                                </Button>
                            </div>

                            {/* Fila de Exportación: Excel y PDF */}
                            <div className="grid grid-cols-2 gap-2">
                                <Button
                                    className="bg-green-600 hover:bg-green-700 text-white shadow-sm"
                                    onClick={handleDescargarExcel}
                                >
                                    <FileSpreadsheet className="w-4 h-4 mr-2" />
                                    Excel
                                </Button>
                                <Button
                                    className="bg-red-600 hover:bg-red-700 text-white shadow-sm"
                                    onClick={handleDescargarPdf}
                                >
                                    <FileText className="w-4 h-4 mr-2" />
                                    PDF
                                </Button>
                            </div>
                        </div>

                    </div>
                </CardContent>
            </Card>

            {/* --- TARJETAS DE TOTALES (KPIs) --- */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Alumnos Filtrados</CardTitle>
                        <UserCheck className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{data.totalAlumnosHoy}</div>
                        <p className="text-xs text-muted-foreground">Coinciden con filtros</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Visitantes Filtrados</CardTitle>
                        <Users className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{data.totalVisitantesHoy}</div>
                        <p className="text-xs text-muted-foreground">Coinciden con filtros</p>
                    </CardContent>
                </Card>
            </div>

            {/* --- GRÁFICAS --- */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">

                {/* GRÁFICA DE LÍNEAS */}
                <Card className="col-span-4">
                    <CardHeader>
                        <CardTitle>Flujo de Entradas</CardTitle>
                        <CardDescription>
                            {filtros.puerta !== 'TODAS' ? `Actividad específica en Puerta ${filtros.puerta}` : 'Actividad global en todas las puertas'}
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="pl-2">
                        <div className="h-[300px] w-full">
                            {data.asistenciaSemanal && data.asistenciaSemanal.length > 0 ? (
                                <ResponsiveContainer width="100%" height="100%">
                                    <AreaChart data={data.asistenciaSemanal}>
                                        <defs>
                                            <linearGradient id="colorAlumnos" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8} />
                                                <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                                            </linearGradient>
                                            <linearGradient id="colorVisitantes" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#f97316" stopOpacity={0.8} />
                                                <stop offset="95%" stopColor="#f97316" stopOpacity={0} />
                                            </linearGradient>
                                        </defs>
                                        <XAxis dataKey="hora" fontSize={12} minTickGap={15} />
                                        <YAxis fontSize={12} />
                                        <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                        <RechartsTooltip />
                                        <Legend />
                                        <Area type="monotone" dataKey="alumnos" name="Alumnos" stroke="#3b82f6" fillOpacity={1} fill="url(#colorAlumnos)" />
                                        <Area type="monotone" dataKey="visitantes" name="Visitantes" stroke="#f97316" fillOpacity={1} fill="url(#colorVisitantes)" />
                                    </AreaChart>
                                </ResponsiveContainer>
                            ) : (
                                <div className="h-full flex items-center justify-center text-slate-400">
                                    No hay datos para mostrar.
                                </div>
                            )}
                        </div>
                    </CardContent>
                </Card>

                {/* GRÁFICA DE BARRAS */}
                <Card className="col-span-3">
                    <CardHeader>
                        <CardTitle>Entradas por Puerta</CardTitle>
                        <CardDescription>
                            Comparativa total (Se muestran todas las puertas).
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="h-[300px] w-full">
                            {data.entradasPorPuerta && data.entradasPorPuerta.length > 0 ? (
                                <ResponsiveContainer width="100%" height="100%">
                                    <BarChart data={data.entradasPorPuerta}>
                                        <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                        <XAxis dataKey="puerta" fontSize={12} />
                                        <YAxis fontSize={12} />
                                        <RechartsTooltip cursor={{ fill: 'transparent' }} />
                                        <Legend />
                                        <Bar dataKey="total" name="Total Entradas" fill="#10b981" radius={[4, 4, 0, 0]} />
                                    </BarChart>
                                </ResponsiveContainer>
                            ) : (
                                <div className="h-full flex items-center justify-center text-slate-400">
                                    Sin actividad reciente.
                                </div>
                            )}
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};
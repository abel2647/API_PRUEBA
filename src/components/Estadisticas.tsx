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
// Importamos el icono FileSpreadsheet para el botón de Excel
import { Search, Users, UserCheck, User, RefreshCcw, Eraser, FileSpreadsheet } from 'lucide-react';

// --- INTERFACES ---
interface DatosPuerta {
    puerta: number; // o string, dependiendo de tu backend (idEntrada)
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
    // Fecha actual para inicializar
    //const fechaHoy = new Date().toISOString().split('T')[0];
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
        tipo: 'TODOS' // Valores: TODOS, ALUMNO, VISITANTE
    });

    // --- FUNCIONES ---

    // 1. Obtener datos para las gráficas (JSON)
    const obtenerEstadisticas = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams();
            if (filtros.fecha) params.append('fecha', filtros.fecha);
            if (filtros.horaInicio) params.append('horaInicio', filtros.horaInicio);
            if (filtros.horaFin) params.append('horaFin', filtros.horaFin);
            if (filtros.tipo) params.append('tipo', filtros.tipo);

            // Ajusta la URL a tu puerto local (ej. 8080)
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

    // 2. Descargar Excel (Blob) - NUEVA FUNCIÓN
    const handleDescargarExcel = async () => {
        try {
            const params = new URLSearchParams();
            if (filtros.fecha) params.append('fecha', filtros.fecha);
            if (filtros.horaInicio) params.append('horaInicio', filtros.horaInicio);
            if (filtros.horaFin) params.append('horaFin', filtros.horaFin);
            if (filtros.tipo) params.append('tipo', filtros.tipo);

            // Llamada al endpoint que devuelve el archivo binario
            const response = await fetch(`http://localhost:8080/api/dashboard/exportar-excel?${params.toString()}`, {
                method: 'GET',
            });

            if (response.ok) {
                // Convertir la respuesta en un Blob (archivo binario)
                const blob = await response.blob();
                // Crear una URL temporal para el navegador
                const url = window.URL.createObjectURL(blob);
                // Crear un elemento <a> invisible para forzar la descarga
                const a = document.createElement('a');
                a.href = url;
                // Nombre del archivo sugerido
                a.download = `Reporte_Asistencia_${filtros.fecha || 'General'}.xlsx`;
                document.body.appendChild(a);
                a.click();
                // Limpieza
                a.remove();
                window.URL.revokeObjectURL(url);
            } else {
                console.error("Error al descargar el archivo Excel");
                alert("No se pudo descargar el reporte. Verifica que haya datos.");
            }
        } catch (error) {
            console.error("Error de red al descargar:", error);
        }
    };

    // Efecto inicial
    useEffect(() => {
        obtenerEstadisticas();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []); // Se ejecuta solo al montar

    // Manejadores de eventos
    const handleFiltroChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFiltros({
            ...filtros,
            [e.target.name]: e.target.value
        });
    };

    const handleSelectChange = (value: string) => {
        setFiltros({
            ...filtros,
            tipo: value
        });
    }

    const aplicarFiltros = () => {
        obtenerEstadisticas();
    };

    const limpiarFiltros = () => {
        const reset = {
            fecha: fechaHoy,
            horaInicio: '',
            horaFin: '',
            tipo: 'TODOS'
        };
        setFiltros(reset);
        // Opcional: llamar a obtenerEstadisticas() inmediatamente con los valores reset
    };

    return (
        <div className="space-y-6 p-4 md:p-8">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h2 className="text-3xl font-bold tracking-tight">Dashboard de Asistencia</h2>
                    <p className="text-muted-foreground">Estadísticas de entradas de Alumnos y Visitantes.</p>
                </div>
                {/* <Button variant="outline" onClick={obtenerEstadisticas} disabled={loading}>
                    <RefreshCcw className={`mr-2 h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                    Actualizar
                </Button>*/}
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
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 items-end">

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
                            <Select value={filtros.tipo} onValueChange={handleSelectChange}>
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

                        {/* BOTONES DE ACCIÓN */}
                        <div className="flex flex-col gap-2">
                            {/* Fila superior: Filtrar y Limpiar */}
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

                            {/* Fila inferior: DESCARGAR EXCEL (Botón Verde) */}
                            <Button
                                className="w-full bg-green-600 hover:bg-green-700 text-white shadow-sm"
                                onClick={handleDescargarExcel}
                            >
                                <FileSpreadsheet className="w-4 h-4 mr-2" />
                                Descargar Excel
                            </Button>
                        </div>

                    </div>
                </CardContent>
            </Card>

            {/* --- TARJETAS DE TOTALES (KPIs) --- */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Alumnos Hoy</CardTitle>
                        <UserCheck className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{data.totalAlumnosHoy}</div>
                        <p className="text-xs text-muted-foreground">Entradas registradas</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Visitantes Hoy</CardTitle>
                        <Users className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{data.totalVisitantesHoy}</div>
                        <p className="text-xs text-muted-foreground">Entradas registradas</p>
                    </CardContent>
                </Card>
                {/* Puedes agregar más cards aquí si lo deseas */}
            </div>

            {/* --- GRÁFICAS --- */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">

                {/* GRÁFICA DE LÍNEAS / ÁREA (Timeline) */}
                <Card className="col-span-4">
                    <CardHeader>
                        <CardTitle>Flujo de Entradas</CardTitle>
                        <CardDescription>
                            Distribución por hora del día seleccionado.
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="pl-2">
                        <div className="h-[300px] w-full">
                            {data.asistenciaSemanal && data.asistenciaSemanal.length > 0 ? (
                                <ResponsiveContainer width="100%" height="100%">
                                    <AreaChart data={data.asistenciaSemanal}>
                                        <defs>
                                            <linearGradient id="colorAlumnos" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8}/>
                                                <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                                            </linearGradient>
                                            <linearGradient id="colorVisitantes" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#f97316" stopOpacity={0.8}/>
                                                <stop offset="95%" stopColor="#f97316" stopOpacity={0}/>
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
                                    No hay datos para mostrar en este rango.
                                </div>
                            )}
                        </div>
                    </CardContent>
                </Card>

                {/* GRÁFICA DE BARRAS (Por Puerta) */}
                <Card className="col-span-3">
                    <CardHeader>
                        <CardTitle>Entradas por Puerta</CardTitle>
                        <CardDescription>
                            Total de registros por acceso.
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
                                        <RechartsTooltip cursor={{fill: 'transparent'}} />
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
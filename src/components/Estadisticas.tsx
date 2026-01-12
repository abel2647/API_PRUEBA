'use client';

import React, { useState, useEffect } from 'react';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, Legend, ResponsiveContainer,
    AreaChart, Area // <--- IMPORTAR ESTO
} from 'recharts';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Search, Users, UserCheck, User, RefreshCcw, Eraser } from 'lucide-react';

// Interfaces
interface DatosPuerta {
    puerta: number;
    alumnos: number;
    visitantes: number;
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
    asistenciaSemanal: DatoHorario[]; // Aquí vendrá la data por horas
}

export const Estadisticas = () => {
    const fechaHoy = new Date().toISOString().split('T')[0];

    const filtrosIniciales = {
        fecha: fechaHoy,
        horaInicio: '',
        horaFin: '',
        tipo: 'TODOS'
    };

    const [filtros, setFiltros] = useState(filtrosIniciales);
    const [data, setData] = useState<DashboardData | null>(null);
    const [loading, setLoading] = useState(false);

    const fetchEstadisticas = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams();
            if (filtros.fecha) params.append('fecha', filtros.fecha);
            if (filtros.horaInicio) params.append('horaInicio', filtros.horaInicio);
            if (filtros.horaFin) params.append('horaFin', filtros.horaFin);
            if (filtros.tipo) params.append('tipo', filtros.tipo);

            const res = await fetch(`http://localhost:8080/api/dashboard/filtrado?${params.toString()}`);
            if (res.ok) {
                const jsonData = await res.json();
                setData(jsonData);
            }
        } catch (error) {
            console.error("Error cargando estadísticas:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchEstadisticas();
    }, []);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setFiltros({ ...filtros, [e.target.name]: e.target.value });
    };

    const limpiarFiltros = () => {
        setFiltros(filtrosIniciales);
    };

    return (
        <div className="space-y-6 animate-in fade-in duration-500">

            {/* ... (SECCION DE HEADER Y FILTROS - IDÉNTICA A TU CÓDIGO ANTERIOR) ... */}
            {/* Copia aquí el bloque <div className="flex flex-col gap-4"> ... </div> que ya tienes */}
            <div className="flex flex-col gap-4">
                <div>
                    <h2 className="text-3xl font-bold tracking-tight text-slate-800">Panel de Estadísticas</h2>
                    <p className="text-slate-500">Métricas de acceso por Puerta y Tipo de Usuario</p>
                </div>

                <Card className="bg-white shadow-sm border-slate-200">
                    <CardContent className="p-4">
                        <div className="grid grid-cols-1 md:grid-cols-5 gap-4 items-end">
                            {/* Inputs de filtros (Igual a tu código anterior) */}
                            <div className="space-y-1">
                                <Label className="text-xs font-semibold text-slate-500">Fecha</Label>
                                <Input type="date" name="fecha" value={filtros.fecha} onChange={handleChange} className="h-9"/>
                            </div>
                            <div className="space-y-1">
                                <Label className="text-xs font-semibold text-slate-500">Hora Inicio</Label>
                                <Input type="time" name="horaInicio" value={filtros.horaInicio} onChange={handleChange} className="h-9"/>
                            </div>
                            <div className="space-y-1">
                                <Label className="text-xs font-semibold text-slate-500">Hora Fin</Label>
                                <Input type="time" name="horaFin" value={filtros.horaFin} onChange={handleChange} className="h-9"/>
                            </div>
                            <div className="space-y-1">
                                <Label className="text-xs font-semibold text-slate-500">Tipo</Label>
                                <select name="tipo" value={filtros.tipo} onChange={handleChange} className="h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm">
                                    <option value="TODOS">Todos</option>
                                    <option value="ALUMNO">Alumnos</option>
                                    <option value="VISITANTE">Visitantes</option>
                                </select>
                            </div>
                            <div className="flex gap-2">
                                <Button onClick={fetchEstadisticas} disabled={loading} className="flex-1 bg-blue-600 hover:bg-blue-700 h-9">
                                    {loading ? <RefreshCcw className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4 mr-2" />}
                                    Filtrar
                                </Button>
                                <Button onClick={limpiarFiltros} variant="outline" className="h-9 px-3"><Eraser className="w-4 h-4 text-slate-500" /></Button>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* SECCION DE KPIS - IDÉNTICA A TU CÓDIGO ANTERIOR */}
            <div className="grid gap-4 md:grid-cols-3">
                <Card className="bg-white border-l-4 border-l-blue-500 shadow-sm">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium text-slate-600">Total en Rango</CardTitle>
                        <Users className="h-4 w-4 text-blue-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold text-slate-800">{data ? (data.totalAlumnosHoy + data.totalVisitantesHoy) : 0}</div>
                    </CardContent>
                </Card>
                <Card className="bg-white shadow-sm">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium text-slate-600">Alumnos</CardTitle>
                        <UserCheck className="h-4 w-4 text-slate-400" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold text-slate-800">{data?.totalAlumnosHoy || 0}</div>
                    </CardContent>
                </Card>
                <Card className="bg-white shadow-sm">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium text-slate-600">Visitantes</CardTitle>
                        <User className="h-4 w-4 text-orange-400" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold text-slate-800">{data?.totalVisitantesHoy || 0}</div>
                    </CardContent>
                </Card>
            </div>

            {/* --- SECCIÓN DE GRÁFICAS (GRID DE 2) --- */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

                {/* 1. GRÁFICA DE PUERTAS (Bar Chart) - Ya la tenías */}
                <Card className="shadow-sm">
                    <CardHeader>
                        <CardTitle>Afluencia por Puerta</CardTitle>
                        <CardDescription>¿Por dónde entran?</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="h-[300px] w-full">
                            {data?.entradasPorPuerta && data.entradasPorPuerta.length > 0 ? (
                                <ResponsiveContainer width="100%" height="100%">
                                    <BarChart data={data.entradasPorPuerta}>
                                        <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                        <XAxis dataKey="puerta" tickFormatter={(val) => `P${val}`} fontSize={12} />
                                        <YAxis fontSize={12} />
                                        <RechartsTooltip />
                                        <Legend />
                                        <Bar dataKey="alumnos" name="Alumnos" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                                        <Bar dataKey="visitantes" name="Visitantes" fill="#f97316" radius={[4, 4, 0, 0]} />
                                    </BarChart>
                                </ResponsiveContainer>
                            ) : (
                                <div className="h-full flex items-center justify-center text-slate-400">Sin datos</div>
                            )}
                        </div>
                    </CardContent>
                </Card>

                {/* 2. NUEVA GRÁFICA: AFLUENCIA POR HORA (Area Chart) */}
                <Card className="shadow-sm">
                    <CardHeader>
                        <CardTitle>Tendencia Horaria</CardTitle>
                        <CardDescription>¿A qué hora entran?</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="h-[300px] w-full">
                            {data?.asistenciaSemanal && data.asistenciaSemanal.length > 0 ? (
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
                                <div className="h-full flex items-center justify-center text-slate-400">Sin datos</div>
                            )}
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};
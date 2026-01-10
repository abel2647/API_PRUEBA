'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Search, X, Calendar, Clock, User, FileText, Filter, CheckCircle2, AlertCircle } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
//import { Badge } from "@/components/ui/badge"; // Si tienes shadcn badge, si no usa un span con clases (abajo te muestro cómo)

// Actualizamos la interfaz
interface Visita {
    id: number;
    nombreCompleto: string;
    asunto: string;
    fechaEntrada: string;
    estatus: 'Vigente' | 'Expirado'; // Nuevo campo
}

export const HistorialVisitantes = () => {
    const [data, setData] = useState<Visita[]>([]);
    const [loading, setLoading] = useState(false);
    
    const [filters, setFilters] = useState({
        nombre: '',
        paterno: '',
        materno: '',
        fecha: '', 
        hora: ''
    });

    const fetchVisitantes = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams();
            if (filters.nombre) params.append('nombre', filters.nombre);
            if (filters.paterno) params.append('paterno', filters.paterno);
            if (filters.materno) params.append('materno', filters.materno);
            if (filters.fecha) params.append('fecha', filters.fecha);
            if (filters.hora) params.append('hora', filters.hora);

            const response = await fetch(`http://localhost:8080/api/visitante/historial?${params.toString()}`);
            if (!response.ok) throw new Error('Error al obtener datos');
            
            const result: Visita[] = await response.json();
            setData(result);
        } catch (error) {
            console.error("Error:", error);
        } finally {
            setLoading(false);
        }
    }, [filters]); 

    useEffect(() => {
        fetchVisitantes();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []); 

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    const handleClear = () => {
        setFilters({ nombre: '', paterno: '', materno: '', fecha: '', hora: '' });
    };

    // Formateadores
    const formatDate = (iso: string) => new Date(iso).toLocaleDateString('es-MX', { day: '2-digit', month: 'short', year: 'numeric' });
    const formatTime = (iso: string) => new Date(iso).toLocaleTimeString('es-MX', { hour: '2-digit', minute: '2-digit', hour12: true });

    return (
        <Card className="w-full max-w-7xl mx-auto shadow-sm mt-6 border-slate-200">
            <CardHeader className="bg-slate-50 border-b">
                <div className="flex items-center gap-3">
                    <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
                        <FileText className="w-6 h-6" />
                    </div>
                    <div>
                        <CardTitle className="text-xl font-bold text-slate-800">Historial de Accesos</CardTitle>
                        <CardDescription>Monitor de visitas y estatus de pases</CardDescription>
                    </div>
                </div>
            </CardHeader>
            
            <CardContent className="p-6">
                {/* FILTROS (Igual que antes) */}
                <form onSubmit={(e) => { e.preventDefault(); fetchVisitantes(); }} className="bg-white p-4 rounded-lg border border-slate-100 shadow-sm mb-6">
                    <div className="flex items-center gap-2 mb-4 text-slate-500 font-medium text-sm">
                        <Filter className="w-4 h-4" /> Filtros
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-6 gap-4 items-end">
                        <div className="space-y-1"><Label className="text-xs">Nombre</Label><Input name="nombre" value={filters.nombre} onChange={handleInputChange} className="h-9"/></div>
                        <div className="space-y-1"><Label className="text-xs">Paterno</Label><Input name="paterno" value={filters.paterno} onChange={handleInputChange} className="h-9"/></div>
                        <div className="space-y-1"><Label className="text-xs">Materno</Label><Input name="materno" value={filters.materno} onChange={handleInputChange} className="h-9"/></div>
                        <div className="space-y-1"><Label className="text-xs">Fecha</Label><Input type="date" name="fecha" value={filters.fecha} onChange={handleInputChange} className="h-9"/></div>
                        <div className="space-y-1"><Label className="text-xs">Hora</Label><Input type="time" name="hora" value={filters.hora} onChange={handleInputChange} className="h-9"/></div>
                        <div className="flex gap-2">
                            <Button type="button" variant="outline" onClick={handleClear} className="px-3"><X className="w-4 h-4" /></Button>
                            <Button type="submit" className="bg-blue-600 hover:bg-blue-700 flex-1"><Search className="w-4 h-4" /></Button>
                        </div>
                    </div>
                </form>

                {/* TABLA ACTUALIZADA */}
                <div className="rounded-md border border-slate-200 overflow-hidden">
                    <Table>
                        <TableHeader>
                            <TableRow className="bg-slate-50">
                                <TableHead className="font-bold">Nombre Completo</TableHead>
                                <TableHead className="font-bold">Asunto</TableHead>
                                <TableHead className="font-bold w-[180px]">Fecha Entrada</TableHead>
                                <TableHead className="font-bold w-[120px]">Estatus</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow><TableCell colSpan={4} className="h-24 text-center">Cargando...</TableCell></TableRow>
                            ) : data.length === 0 ? (
                                <TableRow><TableCell colSpan={4} className="h-24 text-center text-slate-400">Sin resultados.</TableCell></TableRow>
                            ) : (
                                data.map((item) => (
                                    <TableRow key={item.id} className="hover:bg-slate-50/50">
                                        <TableCell className="font-medium uppercase">
                                            <div className="flex items-center gap-2">
                                                <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-400">
                                                    <User className="w-4 h-4" />
                                                </div>
                                                {item.nombreCompleto}
                                            </div>
                                        </TableCell>
                                        <TableCell className="text-slate-600">{item.asunto}</TableCell>
                                        <TableCell>
                                            <div className="flex flex-col">
                                                <span className="text-sm font-medium text-slate-700">{formatDate(item.fechaEntrada)}</span>
                                                <span className="text-xs text-slate-500">{formatTime(item.fechaEntrada)}</span>
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            {/* BADGE DE ESTATUS */}
                                            <div className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${
                                                item.estatus === 'Vigente' 
                                                    ? 'bg-green-50 text-green-700 border-green-200' 
                                                    : 'bg-slate-100 text-slate-600 border-slate-200'
                                            }`}>
                                                {item.estatus === 'Vigente' ? (
                                                    <CheckCircle2 className="w-3 h-3 mr-1" />
                                                ) : (
                                                    <AlertCircle className="w-3 h-3 mr-1" />
                                                )}
                                                {item.estatus}
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </div>
            </CardContent>
        </Card>
    );
};
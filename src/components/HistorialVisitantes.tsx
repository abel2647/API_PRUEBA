'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Search, X, Calendar, Clock, User, FileText, CheckCircle2, MapPin, Hash } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import {
    Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";

interface Visita {
    id: number;
    nombreCompleto: string;
    asunto: string;
    fechaCreacion: string | null;
    ultimaFechaEscaneo: string | null;
    totalEntradas: number;
    ultimaPuerta: string | null;
    estatus: string;
}

export const HistorialVisitantes = () => {
    const [data, setData] = useState<Visita[]>([]);
    const [filteredData, setFilteredData] = useState<Visita[]>([]);
    const [loading, setLoading] = useState(false);

    const [filters, setFilters] = useState({
        nombre: '', paterno: '', materno: '', fecha: '', hora: '', puerta: ''
    });

    const fetchVisitantes = useCallback(async () => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8080/api/visitante/historial`);
            if (!response.ok) throw new Error('Error al obtener datos');
            const result = await response.json();

            const mappedData: Visita[] = result.map((item: any) => ({
                id: item.id,
                nombreCompleto: item.nombreCompleto || 'Desconocido',
                asunto: item.asunto || 'General',
                fechaCreacion: item.fechaCreacion,
                ultimaFechaEscaneo: item.ultimaFechaEscaneo,
                totalEntradas: item.totalEntradas || 0,
                ultimaPuerta: item.ultimaPuerta || 'Sin registro',
                estatus: 'Vigente'
            }));

            setData(mappedData);
            setFilteredData(mappedData);
        } catch (error) {
            console.error("Error:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { fetchVisitantes(); }, [fetchVisitantes]);

    // Filtros
    useEffect(() => {
        const filtered = data.filter(item => {
            const nombreSafe = (item.nombreCompleto || '').toLowerCase();
            const puertaSafe = (item.ultimaPuerta || '').toLowerCase();

            const matchNombre = nombreSafe.includes(filters.nombre.toLowerCase());
            const matchPaterno = nombreSafe.includes(filters.paterno.toLowerCase());
            const matchMaterno = nombreSafe.includes(filters.materno.toLowerCase());
            const matchPuerta = filters.puerta === '' || puertaSafe.includes(filters.puerta.toLowerCase());
            const fechaStr = filters.fecha;
            const matchFecha = fechaStr === '' || (item.fechaCreacion && item.fechaCreacion.includes(fechaStr));

            return matchNombre && matchPaterno && matchMaterno && matchPuerta && matchFecha;
        });
        setFilteredData(filtered);
    }, [filters, data]);

    const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
    };

    const formatDateTime = (iso: string | null) => {
        if (!iso) return <span className="text-slate-300 italic">-</span>;
        return new Date(iso).toLocaleDateString('es-MX', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    };

    return (
        <Card className="w-full shadow-sm border-slate-200">
            <CardContent className="p-6">
                {/* FILTROS */}
                <div className="grid grid-cols-1 md:grid-cols-6 gap-4 mb-6">
                    <div className="space-y-1"><Label>Nombre</Label><Input name="nombre" placeholder="..." value={filters.nombre} onChange={handleFilterChange} /></div>
                    <div className="space-y-1"><Label>Paterno</Label><Input name="paterno" placeholder="..." value={filters.paterno} onChange={handleFilterChange} /></div>
                    <div className="space-y-1"><Label>Materno</Label><Input name="materno" placeholder="..." value={filters.materno} onChange={handleFilterChange} /></div>
                    <div className="space-y-1"><Label>Fecha</Label><Input name="fecha" type="date" value={filters.fecha} onChange={handleFilterChange} /></div>
                    <div className="space-y-1"><Label>Puerta</Label><Input name="puerta" placeholder="..." value={filters.puerta} onChange={handleFilterChange} /></div>
                    <div className="flex items-end"><Button variant="outline" onClick={() => setFilters({ nombre: '', paterno: '', materno: '', fecha: '', hora: '', puerta: '' })}><X className="w-4 h-4 mr-2"/> Limpiar</Button></div>
                </div>

                {/* TABLA */}
                <div className="rounded-md border border-slate-200 overflow-hidden">
                    <Table>
                        <TableHeader>
                            <TableRow className="bg-slate-50">
                                <TableHead>Nombre</TableHead>
                                <TableHead>Asunto</TableHead>
                                <TableHead>Creación</TableHead>
                                <TableHead>Último Escaneo</TableHead>
                                <TableHead className="text-center">Accesos</TableHead>
                                <TableHead>Puerta</TableHead>
                                <TableHead>Estatus</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? <TableRow><TableCell colSpan={7} className="text-center h-24">Cargando...</TableCell></TableRow> :
                                filteredData.map((item) => (
                                    <TableRow key={item.id} className="hover:bg-slate-50">
                                        <TableCell><div className="font-semibold uppercase text-sm">{item.nombreCompleto}</div></TableCell>
                                        <TableCell><span className="text-sm uppercase">{item.asunto}</span></TableCell>
                                        <TableCell>{formatDateTime(item.fechaCreacion)}</TableCell>
                                        <TableCell className="text-blue-700 font-medium">{formatDateTime(item.ultimaFechaEscaneo)}</TableCell>
                                        <TableCell className="text-center"><span className="bg-slate-100 px-2 py-1 rounded-full text-xs font-bold">{item.totalEntradas}</span></TableCell>
                                        <TableCell>{item.ultimaPuerta}</TableCell>
                                        <TableCell><div className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border bg-green-50 text-green-700 border-green-200"><CheckCircle2 className="w-3 h-3 mr-1" />{item.estatus}</div></TableCell>
                                    </TableRow>
                                ))}
                        </TableBody>
                    </Table>
                </div>
            </CardContent>
        </Card>
    );
};
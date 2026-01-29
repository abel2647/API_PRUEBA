'use client';

import React, { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { AlertCircle, CheckCircle2 } from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

interface Alumno {
    id_alumno: number;
    uuid: string;
    primerNombre: string;
    apellidoPaterno: string;
    apellidoMaterno: string;
    numeroControl: string;
    carreraClave: string;
    activo: number;
}

interface Props {
    alumno: Alumno | null;
    isOpen: boolean;
    onClose: () => void;
    onSave: () => void;
}

export default function ModalEditarAlumno({ alumno, isOpen, onClose, onSave }: Props) {
    const [formData, setFormData] = useState<Partial<Alumno>>({});
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (alumno) {
            setFormData(alumno);
            setError(null);
        }
    }, [alumno, isOpen]);

    // === CAMBIO IMPORTANTE AQUÍ ===
    // Convertimos a mayúsculas automáticamente al escribir
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value.toUpperCase() });
    };

    const handleSelectChange = (field: string, value: string) => {
        setFormData({ ...formData, [field]: value });
    };

    const handleGuardar = async () => {
        if (!alumno || !formData) return;
        setLoading(true);
        setError(null);

        try {
            // Aseguramos que activo sea número
            const payload = {
                ...formData,
                activo: Number(formData.activo)
            };

            const response = await fetch(`http://localhost:8080/api/alumnos/${alumno.uuid}/${alumno.id_alumno}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                onSave();
                onClose();
            } else {
                const errorText = await response.text();
                setError(`Error del servidor: ${errorText}`);
            }
        } catch (err) {
            console.error(err);
            setError("No se pudo conectar con el servidor.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-[600px]">
                <DialogHeader>
                    <DialogTitle className="text-xl font-bold text-slate-800 uppercase">
                        Editar Estudiante
                    </DialogTitle>
                    <DialogDescription>
                        Actualiza los datos personales. Los textos se guardarán en mayúsculas automáticamente.
                    </DialogDescription>
                </DialogHeader>

                {error && (
                    <Alert variant="destructive">
                        <AlertCircle className="h-4 w-4" />
                        <AlertTitle>Error</AlertTitle>
                        <AlertDescription>{error}</AlertDescription>
                    </Alert>
                )}

                <div className="grid grid-cols-2 gap-4 py-4">

                    {/* Fila 1: Nombre */}
                    <div className="col-span-2 space-y-2">
                        <Label htmlFor="primerNombre" className="font-semibold text-slate-700">Nombre(s)</Label>
                        <Input
                            id="primerNombre"
                            name="primerNombre"
                            value={formData.primerNombre || ''}
                            onChange={handleChange}
                            className="bg-slate-50 uppercase" // Clase uppercase agregada
                        />
                    </div>

                    {/* Fila 2: Apellidos */}
                    <div className="space-y-2">
                        <Label htmlFor="apellidoPaterno" className="font-semibold text-slate-700">Apellido Paterno</Label>
                        <Input
                            id="apellidoPaterno"
                            name="apellidoPaterno"
                            value={formData.apellidoPaterno || ''}
                            onChange={handleChange}
                            className="uppercase" // Clase uppercase agregada
                        />
                    </div>
                    <div className="space-y-2">
                        <Label htmlFor="apellidoMaterno" className="font-semibold text-slate-700">Apellido Materno</Label>
                        <Input
                            id="apellidoMaterno"
                            name="apellidoMaterno"
                            value={formData.apellidoMaterno || ''}
                            onChange={handleChange}
                            className="uppercase" // Clase uppercase agregada
                        />
                    </div>

                    {/* Fila 3: Control y Carrera */}
                    <div className="space-y-2">
                        <Label htmlFor="numeroControl" className="font-semibold text-slate-700">No. Control</Label>
                        <Input
                            id="numeroControl"
                            name="numeroControl"
                            value={formData.numeroControl || ''}
                            onChange={handleChange}
                            className="font-mono bg-slate-50 uppercase" // Clase uppercase agregada
                        />
                    </div>
                    <div className="space-y-2">
                        <Label htmlFor="carreraClave" className="font-semibold text-slate-700">Carrera</Label>
                        <Select
                            value={formData.carreraClave}
                            onValueChange={(val) => handleSelectChange("carreraClave", val)}
                        >
                            <SelectTrigger>
                                <SelectValue placeholder="Selecciona carrera" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="L20">Ing. Sistemas Computacionales (L20)</SelectItem>
                                <SelectItem value="L19">Ing. Gestión Empresarial (L19)</SelectItem>
                                <SelectItem value="L13">Ing. Industrial (L13)</SelectItem>
                                <SelectItem value="L10">Ing. Electrónica (L10)</SelectItem>
                                <SelectItem value="L15">Ing. Eléctrica (L15)</SelectItem>
                                <SelectItem value="L16">Ing. Química (L16)</SelectItem>
                                <SelectItem value="L17">Ing. Mecánica (L17)</SelectItem>
                                <SelectItem value="L18">Ing. Civil (L18)</SelectItem>
                                <SelectItem value="L11">Lic. Administración (L11)</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    {/* Fila 4: Estatus */}
                    <div className="col-span-2 space-y-2 pt-2 border-t mt-2">
                        <Label htmlFor="activo" className="font-semibold text-slate-700">Estatus del Estudiante</Label>
                        <Select
                            value={String(formData.activo)}
                            onValueChange={(val) => handleSelectChange("activo", val)}
                        >
                            <SelectTrigger className={formData.activo == 1 ? "border-emerald-500 text-emerald-700 bg-emerald-50" : "border-red-500 text-red-700 bg-red-50"}>
                                <SelectValue placeholder="Seleccionar estatus" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="1">
                                    <div className="flex items-center text-emerald-600 font-medium">
                                        <CheckCircle2 size={16} className="mr-2"/> ACTIVO (Acceso permitido)
                                    </div>
                                </SelectItem>
                                <SelectItem value="0">
                                    <div className="flex items-center text-red-600 font-medium">
                                        <AlertCircle size={16} className="mr-2"/> BAJA / INACTIVO (Acceso denegado)
                                    </div>
                                </SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={onClose} type="button">Cancelar</Button>
                    <Button onClick={handleGuardar} disabled={loading} className="bg-blue-600 hover:bg-blue-700">
                        {loading ? "Guardando..." : "Guardar Cambios"}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
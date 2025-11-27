'use client';

import React, { useState } from 'react';
import { VisitanteForm } from '@/types/visitante';

// Importación de componentes de Shadcn UI
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue
} from '@/components/ui/select';

// Definición del estado inicial del formulario
const initialFormState: VisitanteForm = {
    primerNombre: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    sexo: 'Masculino', // Valor inicial
    edad: '', // Como string para el input
    numTelefono: '', // Como string para el input
    asunto: '',
};

export const RegistroVisitanteForm = () => {
    const [formData, setFormData] = useState<VisitanteForm>(initialFormState);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);


    // Función genérica para manejar cambios en inputs (texto y textarea)
    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Función específica para manejar cambios en el Select (Sexo)
    const handleSelectChange = (value: string) => {
        setFormData(prev => ({ ...prev, sexo: value }));
    };

    // Función para manejar el envío del formulario a Spring Boot
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setMessage(null);

        // 1. Preparar los datos para enviar
        const dataToSend = {
            // Campos del formulario
            primerNombre: formData.primerNombre,
            apellidoPaterno: formData.apellidoPaterno,
            apellidoMaterno: formData.apellidoMaterno,
            sexo: formData.sexo,
            asunto: formData.asunto,

            // Conversión de string a number para la API de Spring Boot
            edad: Number(formData.edad),
            numTelefono: Number(formData.numTelefono),

            // Campo requerido por el backend (que no está en el formulario):
            usuario: 1, // ID fijo del usuario que realiza el registro (se puede cambiar)
        };

        // 2. Realizar la llamada a la API
        try {
            const response = await fetch('http://localhost:8080/api/visitante', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(dataToSend),
            });

            if (!response.ok) {
                // Manejo de errores de HTTP (ej. 400, 500)
                throw new Error(`Error ${response.status}: El servidor rechazó la solicitud.`);
            }

            // Éxito
            const result = await response.json();
            setMessage({ type: 'success', text: `Visitante ${result.primerNombre} ${result.apellidoPaterno} registrado exitosamente. ID: ${result.id_visitante}` });
            setFormData(initialFormState); // Limpiar el formulario

        } catch (error) {
            console.error('Error al registrar visitante:', error);
            // @ts-ignore
            setMessage({ type: 'error', text: `Error al registrar: ${error.message}` });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Card className="max-w-4xl mx-auto shadow-2xl mt-10">
            <CardHeader>
                <CardTitle className="text-3xl font-bold">Registro de visitantes</CardTitle>
            </CardHeader>

            <CardContent>
                {/* Mostrar mensajes de éxito o error */}
                {message && (
                    <div className={`p-3 mb-4 rounded text-white ${message.type === 'success' ? 'bg-green-500' : 'bg-red-500'}`}>
                        {message.text}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    {/* SECCIÓN PRINCIPAL: DIVIDIDA EN DOS COLUMNAS (PERSONAL Y CONTACTO) */}
                    <div className="grid grid-cols-2 gap-x-10 gap-y-6">

                        {/* COLUMNA IZQUIERDA: DATOS PERSONALES */}
                        <div className="space-y-6">
                            <div className="space-y-2">
                                <Label htmlFor="primerNombre">Nombre</Label>
                                <Input
                                    id="primerNombre"
                                    name="primerNombre"
                                    value={formData.primerNombre}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="apellidoPaterno">Apellido paterno</Label>
                                <Input
                                    id="apellidoPaterno"
                                    name="apellidoPaterno"
                                    value={formData.apellidoPaterno}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="apellidoMaterno">Apellido materno</Label>
                                <Input
                                    id="apellidoMaterno"
                                    name="apellidoMaterno"
                                    value={formData.apellidoMaterno}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        {/* COLUMNA DERECHA: SEXO, EDAD, TELÉFONO */}
                        <div className="space-y-6">

                            <div className="space-y-2">
                                <Label htmlFor="sexo">Sexo</Label>
                                <Select name="sexo" value={formData.sexo} onValueChange={handleSelectChange} required>
                                    <SelectTrigger id="sexo">
                                        <SelectValue placeholder="Seleccionar" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="Masculino">Masculino</SelectItem>
                                        <SelectItem value="Femenino">Femenino</SelectItem>
                                        <SelectItem value="Otro">Otro</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="edad">Edad</Label>
                                <Input
                                    id="edad"
                                    name="edad"
                                    type="number"
                                    value={formData.edad}
                                    onChange={handleChange}
                                    required
                                    min="1"
                                    placeholder="23"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="numTelefono">Teléfono</Label>
                                <Input
                                    id="numTelefono"
                                    name="numTelefono"
                                    type="tel"
                                    value={formData.numTelefono}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>
                    </div>

                    {/* SECCIÓN DE VISITA Y BOTONES */}
                    <div className="mt-10">
                        <h3 className="text-xl font-semibold mb-4">Datos de visita</h3>

                        <div className="grid grid-cols-3 gap-8">

                            {/* Columna 1: ASUNTO (2/3 del ancho) */}
                            <div className="col-span-2 space-y-2">
                                <Label htmlFor="asunto">Asunto</Label>
                                <Textarea
                                    id="asunto"
                                    name="asunto"
                                    value={formData.asunto}
                                    onChange={handleChange}
                                    rows={4}
                                    required
                                    placeholder="Trámite en servicios escolares"
                                />
                            </div>

                            {/* Columna 2: QR (1/3 del ancho) */}
                            <div className="col-span-1 flex flex-col items-center justify-center border border-gray-200 rounded-lg p-4 bg-white">
                                <div className="text-sm text-gray-500 mb-2">QR Code Placeholder</div>
                                <div className="w-full h-auto max-h-[150px] aspect-square bg-gray-300 flex items-center justify-center">
                                    {/* Aquí iría el componente real de QR si lo implementas */}


                                    [Image of QR Code Placeholder]

                                </div>
                            </div>
                        </div>
                    </div>

                    {/* BOTONES */}
                    <div className="mt-10 flex justify-end gap-4">
                        <Button type="button" variant="destructive" disabled={isLoading}>
                            Cancelar
                        </Button>
                        <Button type="submit" className="bg-blue-500 hover:bg-blue-600" disabled={isLoading}>
                            {isLoading ? 'Registrando...' : 'Aceptar'}
                        </Button>
                        <Button type="button" className="bg-green-600 hover:bg-green-700" disabled={isLoading}>
                            Imprimir
                        </Button>
                    </div>
                </form>
            </CardContent>
        </Card>
    );
};
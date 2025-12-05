'use client';

import React, { useState, useEffect } from 'react';
// Importamos la interfaz que define qué datos vamos a mandar
import { VisitanteForm } from '@/types/visitante';
// Importamos el componente que dibuja el código QR
import { QRCodeDisplay } from '@/components/QRCodeDisplay';

// Importaciones de Shadcn UI
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

// Datos iniciales del formulario
const initialFormState: VisitanteForm = {
    primerNombre: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    sexo: 'Masculino',
    edad: '',
    numTelefono: '',
    asunto: '',
    numeroAcompañantes: '0',
};

export const RegistroVisitanteForm = () => {
    // Estados para manejar el formulario y la respuesta
    const [formData, setFormData] = useState<VisitanteForm>(initialFormState);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);
    const [qrValue, setQrValue] = useState<string | null>(null);
    const [registroId, setRegistroId] = useState<number | null>(null);
    const [hasMounted, setHasMounted] = useState(false);

    // Función de montaje para corregir el error de hidratación
    useEffect(() => {
        setHasMounted(true);
    }, []);

    // ----------------------------------------------------
    // MANEJO DE ESTADO Y UTILIDADES
    // ----------------------------------------------------

    // Función de limpieza para los botones Cancelar / Imprimir
    const handleReset = () => {
        setFormData(initialFormState);
        setQrValue(null);
        setMessage(null);
        setRegistroId(null);
    };

    // Función para lo que se escribe en los inputs
    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Función para lo que se selecciona en el campo Sexo
    const handleSelectChange = (value: string) => {
        setFormData(prev => ({ ...prev, sexo: value }));
    };

    // ----------------------------------------------------
    // LÓGICA DE IMPRESIÓN
    // ----------------------------------------------------

    const handlePrint = () => {
        if (typeof window !== 'undefined') {
            const printContent = document.getElementById('visitor-pass');

            if (printContent) {
                const printWindow = window.open('', '', 'height=500,width=800');
                if (printWindow) {
                    printWindow.document.write('<html><head><title>Pase de Visitante</title>');
                    printWindow.document.write('</head><body>');
                    printWindow.document.write('<div style="padding: 20px; border: 1px solid #ccc; max-width: 400px; margin: 20px auto;">');
                    printWindow.document.write(printContent.innerHTML);
                    printWindow.document.write('</div></body></html>');
                    printWindow.document.close();
                    printWindow.print();

                    // Limpiar el formulario después de la impresión
                    handleReset();
                }
            }
        }
    };


    // ----------------------------------------------------
    // LÓGICA DE ENVÍO A LA API
    // ----------------------------------------------------

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setMessage(null);
        setQrValue(null);

        // Prepara el objeto JSON que va a Spring Boot
        const dataToSend = {
            primerNombre: formData.primerNombre,
            apellidoPaterno: formData.apellidoPaterno,
            apellidoMaterno: formData.apellidoMaterno,
            sexo: formData.sexo,
            asunto: formData.asunto,

            // Conversión de string a number
            edad: Number(formData.edad),
            numTelefono: Number(formData.numTelefono),
            numeroAcompañantes: Number(formData.numeroAcompañantes),

            usuario: 1,
        };

        try {
            const response = await fetch('http://localhost:8080/api/visitante', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dataToSend),
            });

            if (!response.ok) {
                throw new Error(`Error ${response.status}: El servidor rechazó la solicitud.`);
            }

            // Éxito
            const result = await response.json();
            setMessage({
                type: 'success',
                text: `Visitante ${result.primerNombre} ${result.apellidoPaterno} registrado exitosamente. ID: ${result.id_visitante}`
            });
            setQrValue(result.uuid); // Guarda el UUID para el QR
            setRegistroId(result.id_visitante); // Guarda el ID para el pase
            // El formulario se queda con los datos para Imprimir


        } catch (error) {
            console.error('Error al registrar visitante:', error);
            // @ts-ignore
            setMessage({ type: 'error', text: `Error al registrar: ${error.message}` });
        } finally {
            setIsLoading(false);
        }
    };

    // Evita el error de hidratación
    if (!hasMounted) {
        return <div className="p-10 text-center">Cargando formulario...</div>;
    }

    return (
        // La ID 'visitor-pass' es crucial para la función de impresión
        <Card id="visitor-pass" className="w-full max-w-4xl mx-auto shadow-2xl mt-10">
            <CardHeader>
                <CardTitle className="text-3xl font-bold">Registro de visitantes</CardTitle>
            </CardHeader>

            <CardContent>
                {/* Mensajes de éxito/error */}
                {message && (
                    <div className={`p-3 mb-4 rounded text-white ${message.type === 'success' ? 'bg-green-500' : 'bg-red-500'}`}>
                        {message.text}
                    </div>
                )}

                <form onSubmit={handleSubmit}>

                    {/* SECCIÓN PRINCIPAL: DATOS PERSONALES (Nombre, Sexo, Edad, etc.) */}
                    <div className="grid grid-cols-2 gap-x-6 gap-y-6">

                        {/* COLUMNA IZQUIERDA */}
                        <div className="space-y-6">
                            <div className="space-y-2">
                                <Label htmlFor="primerNombre">Nombre</Label>
                                <Input id="primerNombre" name="primerNombre" value={formData.primerNombre} onChange={handleChange} required disabled={!!qrValue} placeholder=""/>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="apellidoPaterno">Apellido paterno</Label>
                                <Input id="apellidoPaterno" name="apellidoPaterno" value={formData.apellidoPaterno} onChange={handleChange} required disabled={!!qrValue} placeholder=""/>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="apellidoMaterno">Apellido materno</Label>
                                <Input id="apellidoMaterno" name="apellidoMaterno" value={formData.apellidoMaterno} onChange={handleChange} required disabled={!!qrValue} placeholder=""/>
                            </div>
                        </div>

                        {/* COLUMNA DERECHA */}
                        <div className="space-y-6">

                            <div className="space-y-2">
                                <Label htmlFor="sexo">Sexo</Label>
                                <Select name="sexo" value={formData.sexo} onValueChange={handleSelectChange} required disabled={!!qrValue}>
                                    <SelectTrigger id="sexo">
                                        <SelectValue placeholder="Seleccionar" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="Masculino">Masculino</SelectItem>
                                        <SelectItem value="Femenino">Femenino</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="edad">Edad</Label>
                                <Input id="edad" name="edad" type="number" value={formData.edad} onChange={handleChange} required min="1" disabled={!!qrValue} placeholder=""/>
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="numTelefono">Teléfono</Label>
                                <Input id="numTelefono" name="numTelefono" type="tel" value={formData.numTelefono} onChange={handleChange} required disabled={!!qrValue} placeholder=""/>
                            </div>
                        </div>
                    </div>

                    {/* SECCIÓN INFERIOR: ACOMPAÑANTES, ASUNTO Y QR (Alineados) */}
                    <div className="mt-10 grid grid-cols-3 gap-x-6 gap-y-6">

                        {/* COLUMNA 1: ACOMPAÑANTES */}
                        <div className="space-y-2">
                            <Label htmlFor="numeroAcompanantes">Acompañantes</Label>
                            <Input
                                id="numeroAcompanantes"
                                name="numeroAcompañantes"
                                type="number"
                                value={formData.numeroAcompañantes}
                                onChange={handleChange}
                                required
                                min="0"
                                disabled={!!qrValue}
                                placeholder="0"
                            />
                        </div>

                        {/* COLUMNA 2: ESPACIO OCULTO (Alineación) */}
                        <div className="hidden sm:block">
                            {/* Este espacio ayuda a que el QR se alinee a la derecha de la página */}
                        </div>

                        {/* COLUMNA 3: CONTENEDOR DEL QR (SE MUESTRA/OCULTA AQUÍ) */}
                        <div className={`row-span-3 col-span-1 
                             flex flex-col items-center justify-center p-4 h-full
                             ${qrValue ? 'block' : 'hidden'} 
                             border border-gray-200 rounded-lg bg-white`}
                        >
                            {/* Contenido del Pase (Solo visible si qrValue existe) */}
                            <div className="text-sm text-gray-500 mb-2">
                                {`Pase ID: ${registroId}`}
                            </div>
                            {qrValue && <QRCodeDisplay value={qrValue} />}
                        </div>

                        {/* ASUNTO (OCUPA EL ESPACIO COMPLETO CUANDO EL QR ESTÁ OCULTO) */}
                        {/* Si no hay QR (o sea, si la columna 3 está hidden), esta columna ocupa el espacio restante. */}
                        <div className={`space-y-2 mt-4 
                             ${qrValue ? 'col-span-2' : 'col-span-3'}`}>
                            <h3 className="text-xl font-semibold mb-4">Datos de visita</h3>
                            <Label htmlFor="asunto">Asunto</Label>
                            <Textarea id="asunto" name="asunto" value={formData.asunto} onChange={handleChange} rows={2} required disabled={!!qrValue} placeholder="Ejemplo: Trámite en servicios escolares"/>
                        </div>
                    </div>

                    {/* BOTONES */}
                    <div className="mt-10 flex justify-end gap-4">
                        <Button type="button" onClick={handleReset} variant="destructive" disabled={isLoading && !qrValue}>
                            Cancelar
                        </Button>
                        <Button type="submit" className="bg-blue-500 hover:bg-blue-600" disabled={isLoading || !!qrValue}>
                            {isLoading ? 'Registrando...' : (qrValue ? 'Aceptar (Listo)' : 'Aceptar')}
                        </Button>
                        <Button type="button" onClick={handlePrint} disabled={!qrValue || isLoading} className="bg-green-600 hover:bg-green-700">
                            Imprimir
                        </Button>
                    </div>
                </form>
            </CardContent>
        </Card>
    );
};
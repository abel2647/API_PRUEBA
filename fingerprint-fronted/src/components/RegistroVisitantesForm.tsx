'use client';

import React, { useState, useEffect } from 'react';
// Importamos la interfaz que define qué datos vamos a mandar
import { VisitanteForm } from '@/types/visitante';
// Importamos el componente que dibuja el código QR
import { QRCodeDisplay } from '@/components/QRCodeDisplay';

// Importaciones de todos los componentes visuales de Shadcn UI
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

// Datos iniciales del formulario (para limpiar después de guardar)
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
    // Para guardar lo que el usuario escribe en el form
    const [formData, setFormData] = useState<VisitanteForm>(initialFormState);

    // Para deshabilitar el botón mientras se envía la petición
    const [isLoading, setIsLoading] = useState(false);

    // Para mostrar mensajes de éxito o error al usuario
    const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);

    // Para guardar el UUID que nos da el backend y poder generar el QR
    const [qrValue, setQrValue] = useState<string | null>(null);

    // Para guardar el ID y ponerlo en el pase
    const [registroId, setRegistroId] = useState<number | null>(null);

    // Para arreglar el error de hidratación que salía al principio
    const [hasMounted, setHasMounted] = useState(false);

    // Esto solo se ejecuta una vez en el navegador para que React sepa que ya cargó.
    useEffect(() => {
        setHasMounted(true);
    }, []);

    // ----------------------------------------------------
    // FUNCIONES BÁSICAS DE MANEJO DE ESTADO
    // ----------------------------------------------------

    // Función general para lo que se escribe en los inputs
    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Función para lo que se selecciona en el campo Sexo
    const handleSelectChange = (value: string) => {
        setFormData(prev => ({ ...prev, sexo: value }));
    };

    // Función principal para limpiar todo (se usa en Cancelar y después de Imprimir)
    const handleReset = () => {
        setFormData(initialFormState);
        setQrValue(null);
        setMessage(null);
        setRegistroId(null);
    };

    // ----------------------------------------------------
    // LÓGICA DE IMPRESIÓN (handlePrint)
    // ----------------------------------------------------

    const handlePrint = () => {
        if (typeof window !== 'undefined') {

            const printContent = document.getElementById('visitor-pass');

            if (printContent) {

                // Abrimos una ventana temporal para la impresión
                const printWindow = window.open('', '', 'height=500,width=800');
                if (printWindow) {

                    // Escribimos un HTML básico con el contenido de la Card
                    printWindow.document.write('<html><head><title>Pase de Visitante</title>');
                    printWindow.document.write('</head><body>');
                    printWindow.document.write('<div style="padding: 20px; border: 1px solid #ccc; max-width: 400px; margin: 20px auto;">');
                    printWindow.document.write(printContent.innerHTML);
                    printWindow.document.write('</div></body></html>');

                    printWindow.document.close();

                    // Disparar el diálogo de impresión
                    printWindow.print();

                    // Limpiar el formulario después de la impresión
                    handleReset();
                }
            }
        }
    };


    // ----------------------------------------------------
    // LÓGICA DE ENVÍO A LA API (handleSubmit)
    // ----------------------------------------------------

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setMessage(null);
        setQrValue(null);

        // Preparamos el objeto JSON tal como lo espera Spring Boot
        const dataToSend = {
            primerNombre: formData.primerNombre,
            apellidoPaterno: formData.apellidoPaterno,
            apellidoMaterno: formData.apellidoMaterno,
            sexo: formData.sexo,
            asunto: formData.asunto,

            // Convertimos los strings del input a números para el backend
            edad: Number(formData.edad),
            numTelefono: Number(formData.numTelefono),
            numeroAcompañantes: Number(formData.numeroAcompañantes), // <-- NUEVO

            // Dato fijo que el backend espera
            usuario: 1,
        };

        try {
            // Mandamos la petición POST a nuestro backend de Spring Boot
            const response = await fetch('http://localhost:8080/api/visitante', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(dataToSend),
            });

            if (!response.ok) {
                // Si sale 400 o 500, lanzamos error
                throw new Error(`Error ${response.status}: El servidor rechazó la solicitud.`);
            }

            // Si todo sale bien (200 OK)
            const result = await response.json();
            setMessage({
                type: 'success',
                text: `Visitante ${result.primerNombre} ${result.apellidoPaterno} registrado exitosamente. ID: ${result.id_visitante}`
            });
            // Guardamos el UUID que usaremos para el QR
            setQrValue(result.uuid);
            setRegistroId(result.id_visitante);
            // El formulario se queda con los datos para Imprimir


        } catch (error) {
            console.error('Error al registrar visitante:', error);
            // @ts-ignore
            setMessage({ type: 'error', text: `Error al registrar: ${error.message}` });
        } finally {
            setIsLoading(false);
        }
    };

    // Si aún no está montado en el cliente, mostramos un mensaje de carga para evitar el error de hidratación
    if (!hasMounted) {
        return <div className="p-10 text-center">Cargando formulario...</div>;
    }

    return (
        // La ID 'visitor-pass' es importante para que funcione la impresión
        <Card id="visitor-pass" className="max-w-4xl mx-auto shadow-2xl mt-10">
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

                    {/* Aquí va el layout de dos columnas */}
                    <div className="grid grid-cols-2 gap-x-10 gap-y-6">

                        {/* COLUMNA IZQUIERDA */}
                        <div className="space-y-6">
                            {/* Campo Nombre */}
                            <div className="space-y-2">
                                <Label htmlFor="primerNombre">Nombre</Label>
                                <Input
                                    id="primerNombre"
                                    name="primerNombre"
                                    value={formData.primerNombre}
                                    onChange={handleChange}
                                    required
                                    disabled={!!qrValue} // Deshabilitamos después de registrar
                                    placeholder=" "
                                />
                            </div>

                            {/* ... (Apellidos paterno y materno similares) ... */}
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

                            {/* Campo Sexo (Select) */}
                            <div className="space-y-2">
                                <Label htmlFor="sexo">Sexo</Label>
                                <Select name="sexo" value={formData.sexo} onValueChange={handleSelectChange} required disabled={!!qrValue}>
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

                            {/* Campo Edad (Number) */}
                            <div className="space-y-2">
                                <Label htmlFor="edad">Edad</Label>
                                <Input id="edad" name="edad" type="number" value={formData.edad} onChange={handleChange} required min="1" disabled={!!qrValue} placeholder=""/>
                            </div>

                            {/* Campo Teléfono (Tel) */}
                            <div className="space-y-2">
                                <Label htmlFor="numTelefono">Teléfono</Label>
                                <Input id="numTelefono" name="numTelefono" type="tel" value={formData.numTelefono} onChange={handleChange} required disabled={!!qrValue} placeholder=""/>
                            </div>
                        </div>
                        {/* -------------------------------------- */}
                        {/* NUEVO CAMPO: Número de Acompañantes */}
                        {/* -------------------------------------- */}
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
                    </div>

                    {/* -------------------------------------- */}
                    {/* SECCIÓN DE VISITA Y QR */}
                    {/* -------------------------------------- */}
                    <div className="mt-10">
                        <h3 className="text-xl font-semibold mb-4">Datos de visita</h3>

                        <div className="grid grid-cols-3 gap-8">

                            {/* Columna 1: ASUNTO */}
                            <div className="col-span-2 space-y-2">
                                <Label htmlFor="asunto">Asunto</Label>
                                <Textarea id="asunto" name="asunto" value={formData.asunto} onChange={handleChange} rows={4} required disabled={!!qrValue} placeholder=" " />
                            </div>

                            {/* Columna 2: QR */}
                            <div className="col-span-1 flex flex-col items-center justify-center border border-gray-200 rounded-lg p-4 bg-white">
                                <div className="text-sm text-gray-500 mb-2">
                                    {qrValue ? `Pase ID: ${registroId}` : 'QR Código Pendiente'}
                                </div>
                                {qrValue ? (
                                    <QRCodeDisplay value={qrValue} /> // <-- Aquí se dibuja el QR con el UUID
                                ) : (
                                    <div className="w-full h-auto max-h-[150px] aspect-square bg-gray-300 flex items-center justify-center">


                                        [Codigo QR]

                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* BOTONES */}
                    <div className="mt-10 flex justify-end gap-4">
                        <Button
                            type="button"
                            onClick={handleReset} // <-- Llama a la función de limpieza
                            variant="destructive"
                            disabled={isLoading && !qrValue}
                        >
                            Cancelar
                        </Button>
                        <Button
                            type="submit"
                            className="bg-blue-500 hover:bg-blue-600"
                            disabled={isLoading || !!qrValue} // Deshabilitado si ya se registró
                        >
                            {isLoading ? 'Registrando...' : (qrValue ? 'Aceptar (Listo)' : 'Aceptar')}
                        </Button>
                        <Button
                            type="button"
                            onClick={handlePrint} // <-- Llama a la función de impresión
                            disabled={!qrValue || isLoading}
                            className="bg-green-600 hover:bg-green-700"
                        >
                            Imprimir
                        </Button>
                    </div>
                </form>
            </CardContent>
        </Card>
    );
};
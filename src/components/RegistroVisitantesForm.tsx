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

        // LÓGICA DE IMPRESIÓN

        const handlePrint = () => {
            if (typeof window !== 'undefined') {
                // Datos necesarios del estado
                const passId = registroId || 'N/A';
                const asuntoText = formData.asunto;
                const numAcompanantes = Number(formData.numeroAcompañantes);

                //Código SVG del QR (usando el ID 'qr-pass-container')
                const qrContainer = document.getElementById('qr-pass-container');
                let qrCodeSvgHtml = '';
                if (qrContainer) {
                    qrCodeSvgHtml = qrContainer.innerHTML;
                }

                //Plantilla HTML del Ticket
                const printContent = `
                <div style="font-family: Arial, sans-serif; width: 350px; border: 3px solid #000; padding: 20px; margin: 20px auto; display: flex; flex-direction: column; text-align: center;">
                    
                    <h2 style="font-size: 18px; margin-bottom: 5px; font-weight: bold;">PASE DE ACCESO DE VISITANTE</h2>
                    
                    <div style="border-top: 1px dashed #333; margin: 15px 0;"></div>

                    <div style="margin-bottom: 10px; padding: 10px; display: flex; justify-content: center; align-items: center;">
                        ${qrCodeSvgHtml}
                    </div>
                    
                    <div style="border-top: 1px dashed #333; margin: 15px 0;"></div>

                    <p style="font-size: 14px; margin-bottom: 5px; font-weight: bold; text-transform: uppercase;">ASUNTO: ${asuntoText}</p>
                    
                    <div style="border-top: 1px dashed #333; margin: 10px 0;"></div>

                    <p style="font-size: 12px; margin-bottom: 5px;">ACOMPAÑANTES: ${numAcompanantes} PERSONAS</p>
                    <p style="font-size: 12px; margin-bottom: 5px; font-weight: bold;">ID DE REGISTRO: ${passId}</p>
                </div>
            `;

                //Ventana de impresión
                const printWindow = window.open('', '', 'height=500,width=400');
                if (printWindow) {
                    printWindow.document.write('<html><head><title>Pase de Visitante</title>');
                    // Estilos para forzar la impresión en blanco y negro
                    printWindow.document.write('<style>@media print { body { -webkit-print-color-adjust: exact; print-color-adjust: exact; color-adjust: exact; } }</style>');
                    printWindow.document.write('</head><body>');
                    printWindow.document.write(printContent);
                    printWindow.document.write('</body></html>');

                    printWindow.document.close();
                    printWindow.print();

                    handleReset();
                }
            }
        };

        // LÓGICA DE ENVÍO A LA API

        const handleSubmit = async (e: React.FormEvent) => {
            e.preventDefault();
            setIsLoading(true);
            setMessage(null);
            setQrValue(null);

            // Objeto JSON que va a Spring Boot
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
                //@ts-ignore
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
            // La ID 'visitor-pass' para la función de impresión
            <Card id="visitor-pass" className="w-full max-w-4xl mx-auto shadow-none border-0 mt-3 bg-transparent">
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

                        {/* SECCIÓN PRINCIPAL: DATOS PERSONALES */}
                        <div className="grid grid-cols-2 gap-x-6 gap-y-6">

                            {/* COLUMNA IZQUIERDA (Nombres) */}
                            <div className="space-y-6">
                                <div className="space-y-2">
                                    <Label htmlFor="primerNombre">Nombre</Label>
                                    <Input id="primerNombre" name="primerNombre" value={formData.primerNombre} onChange={handleChange} required disabled={!!qrValue} placeholder="" />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="apellidoPaterno">Apellido paterno</Label>
                                    <Input id="apellidoPaterno" name="apellidoPaterno" value={formData.apellidoPaterno} onChange={handleChange} required disabled={!!qrValue} placeholder="" />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="apellidoMaterno">Apellido materno</Label>
                                    <Input id="apellidoMaterno" name="apellidoMaterno" value={formData.apellidoMaterno} onChange={handleChange} required disabled={!!qrValue} placeholder="" />
                                </div>
                            </div>

                            {/* COLUMNA DERECHA */}
                            <div className="space-y-6">

                                {/* FILA 1: SEXO Y EDAD (Juntos) */}
                                <div className="grid grid-cols-2 gap-4">
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
                                        <Input id="edad" name="edad" type="number" value={formData.edad} onChange={handleChange} required min="1" disabled={!!qrValue} placeholder="" />
                                    </div>
                                </div>

                                {/* FILA 2: TELÉFONO */}
                                <div className="space-y-2">
                                    <Label htmlFor="numTelefono">Teléfono</Label>
                                    <Input id="numTelefono" name="numTelefono" type="tel" value={formData.numTelefono} onChange={handleChange} required disabled={!!qrValue} placeholder="" />
                                </div>

                                {/* FILA 3: ACOMPAÑANTES (Movido aquí) */}
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
                        </div>

                        {/* SECCIÓN INFERIOR: ASUNTO Y QR */}
                        <div className="mt-10 grid grid-cols-3 gap-x-6 gap-y-6">

                            {/* ASUNTO (Ocupa 2 columnas si hay QR, o 3 si no hay) */}
                            <div className={`space-y-2 mt-4 
                                ${qrValue ? 'col-span-2' : 'col-span-3'}`}>
                                <h3 className="text-xl font-semibold mb-4">Datos de visita</h3>
                                <Label htmlFor="asunto">Asunto</Label>
                                <Textarea id="asunto" name="asunto" value={formData.asunto} onChange={handleChange} rows={2} required disabled={!!qrValue} placeholder="Ejemplo: Trámite en servicios escolares" />
                            </div>

                            {/* COLUMNA 3: CONTENEDOR DEL QR */}
                            <div className={`row-span-1 col-span-1 
                                flex flex-col items-center justify-center p-4 h-full
                                ${qrValue ? 'block' : 'hidden'} 
                                border border-gray-200 rounded-lg bg-white`}
                            >
                                {/* Contenido del Pase (Solo visible si qrValue existe) */}
                                <div className="text-sm text-gray-500 mb-2">
                                    {`Pase ID: ${registroId}`}
                                </div>

                                {/* Contenedor con el ID para la extracción del SVG */}
                                {qrValue && (
                                    <div id="qr-pass-container">
                                        <QRCodeDisplay value={qrValue} />
                                    </div>
                                )}
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
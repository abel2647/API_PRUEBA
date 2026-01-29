'use client';

import React, { useState } from 'react';
import FingerprintModal from '@/components/FingerprintModal';
import { Button } from '@/components/ui/button';
import { Fingerprint, UserPlus, GraduationCap, Phone, User } from 'lucide-react';
import { cn } from '@/lib/utils';
import Swal from 'sweetalert2';

export default function RegistroEstudiantesPage() {
    const [form, setForm] = useState({
        primerNombre: '',
        segundoNombre: '',
        apellidoPaterno: '',
        apellidoMaterno: '',
        numeroControl: '',
        carreraClave: '', // Lo inicializamos vacío para obligar a seleccionar
        numTelefono: '',
        idAlumno: 0
    });

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [readerId, setReaderId] = useState("");
    const [huellaRegistrada, setHuellaRegistrada] = useState(false);

    // Catálogo con Claves para la Base de Datos
    const CARRERAS = [
        { nombre: "Ingeniería en Sistemas Computacionales", clave: "L20" },
        { nombre: "Ingeniería Electrónica", clave: "L10" },
        { nombre: "Ingeniería Química", clave: "L16" },
        { nombre: "Ingeniería Industrial", clave: "L13" },
        { nombre: "Ingeniería Mecánica", clave: "L17" },
        { nombre: "Ingeniería Eléctrica", clave: "L15" },
        { nombre: "Ingeniería Civil", clave: "L18" },
        { nombre: "Licenciatura en Administracion", clave: "L11" },
        { nombre: "Ingenieria en Gestión Empresarial", clave: "L19" }
    ];

    const iniciarCaptura = async () => {
        // 1. Validación básica
        if (!form.primerNombre || !form.carreraClave) {
            return Swal.fire('Atención', 'Nombre y Carrera son obligatorios', 'warning');
        }

        try {
            let idParaHuella = form.idAlumno; // Usamos el ID que tengamos en el estado

            // 2. Solo creamos el registro en BD si NO tenemos un ID todavía (es decir, es la primera vez)
            if (idParaHuella === 0) {
                const resRegistro = await fetch('http://localhost:8080/api/alumnos', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(form)
                });

                if (!resRegistro.ok) {
                    throw new Error("Error en el servidor al registrar datos");
                }

                const alumnoCreado = await resRegistro.json();
                idParaHuella = alumnoCreado.id_alumno; // Obtenemos el nuevo ID

                if (!idParaHuella) {
                    return Swal.fire('Error', 'El servidor no generó un ID válido', 'error');
                }

                // Guardamos el ID en el estado para que, si damos clic de nuevo, no se duplique
                setForm(prev => ({ ...prev, idAlumno: idParaHuella }));
            } else {
                console.log("Reutilizando registro existente ID:", idParaHuella);
            }

            // 3. Buscar lectores (Esto sigue igual, pero ya seguros de tener un ID único)
            const resLector = await fetch('http://localhost:8080/api/v1/multi-fingerprint/auto-select');
            const readers = await resLector.json();

            if (readers && readers.length > 0) {
                setReaderId(readers[0]);
                setIsModalOpen(true);
            } else {
                Swal.fire('Lector no hallado', 'Conecta el dispositivo USB', 'error');
            }
        } catch (error) {
            console.error("Error en iniciarCaptura:", error);
            Swal.fire('Error', 'No se pudo iniciar el proceso de captura', 'error');
        }
    };

    const handleGuardarFinal = async () => {
        if (!huellaRegistrada) {
            return Swal.fire({
                title: 'Validación requerida',
                text: 'Por favor, captura la huella biométrica antes de finalizar.',
                icon: 'warning',
                confirmButtonColor: '#2563eb',
                customClass: { popup: 'rounded-[2rem]' }
            });
        }

        Swal.fire({
            title: 'Guardando registro...',
            allowOutsideClick: false,
            didOpen: () => { Swal.showLoading(); }
        });

        try {
            const res = await fetch(`http://localhost:8080/api/v1/multi-fingerprint/completar-registro/${form.idAlumno}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(form)
            });

            if (res.ok) {
                await Swal.fire({
                    title: '¡Registro Exitoso!',
                    text: `Alumno registrado con éxito con la clave ${form.carreraClave}`,
                    icon: 'success',
                    timer: 2000,
                    showConfirmButton: false,
                    customClass: { popup: 'rounded-[2rem]' }
                });
                window.location.reload();
            } else {
                throw new Error("Error en el servidor");
            }
        } catch (error: any) {
            Swal.fire('Error', 'No se pudo completar el registro final', 'error');
        }
    };

    return (
        <div className="min-h-screen bg-slate-50 p-8">
            <div className="max-w-4xl mx-auto bg-white rounded-[3rem] shadow-xl p-12 border border-slate-100">
                <header className="flex items-center gap-4 mb-10">
                    <div className="bg-blue-600 p-3 rounded-2xl text-white">
                        <UserPlus size={32} />
                    </div>
                    <div>
                        <h1 className="text-3xl font-bold text-slate-800">Nuevo Registro</h1>
                        <p className="text-slate-500">Información Biométrica y Académica</p>
                    </div>
                </header>

                <div className="grid grid-cols-2 gap-8 mb-10">
                    <div className="space-y-6">
                        <InputField label="Primer Nombre" icon={<User size={18}/>} value={form.primerNombre} onChange={(v) => setForm({...form, primerNombre: v})} />
                        <InputField label="Apellido Paterno" icon={<User size={18}/>} value={form.apellidoPaterno} onChange={(v) => setForm({...form, apellidoPaterno: v})} />
                        <InputField label="Número de Control" icon={<User size={18}/>} value={form.numeroControl} onChange={(v) => setForm({...form, numeroControl: v})} />

                        <div className="flex flex-col gap-2">
                            <label className="text-sm font-semibold text-slate-600 ml-1">Carrera</label>
                            <div className="relative">
                                <GraduationCap className="absolute left-4 top-4 text-slate-400" size={18} />
                                <select
                                    className="w-full bg-slate-50 border-none rounded-2xl p-4 pl-12 outline-none focus:ring-2 focus:ring-blue-500 text-slate-700 font-medium appearance-none"
                                    value={form.carreraClave}
                                    onChange={(e) => setForm({ ...form, carreraClave: e.target.value })}
                                >
                                    <option value="">Selecciona una carrera...</option>
                                    {CARRERAS.map((c) => (
                                        <option key={c.clave} value={c.clave}>{c.nombre}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                    </div>

                    <div className="space-y-6">
                        <InputField label="Segundo Nombre" icon={<User size={18}/>} value={form.segundoNombre} onChange={(v) => setForm({...form, segundoNombre: v})} />
                        <InputField label="Apellido Materno" icon={<User size={18}/>} value={form.apellidoMaterno} onChange={(v) => setForm({...form, apellidoMaterno: v})} />
                        <InputField label="Teléfono" icon={<Phone size={18}/>} value={form.numTelefono} onChange={(v) => setForm({...form, numTelefono: v})} />
                    </div>
                </div>

                <div className={cn(
                    "p-8 rounded-[2rem] border-2 border-dashed flex items-center justify-between mb-10 transition-all",
                    huellaRegistrada ? "bg-green-50 border-green-200" : "bg-blue-50 border-blue-100"
                )}>
                    <div className="flex items-center gap-5">
                        <div className={cn("p-4 rounded-2xl shadow-sm", huellaRegistrada ? "bg-green-500 text-white" : "bg-white text-blue-500")}>
                            <Fingerprint size={32} />
                        </div>
                        <div>
                            <p className="font-bold text-slate-800 text-lg">Biometría</p>
                            <p className="text-slate-500">{huellaRegistrada ? "Captura completada" : "Captura triple requerida"}</p>
                        </div>
                    </div>
                    <Button onClick={iniciarCaptura} className="bg-blue-600 hover:bg-blue-700 px-8 h-12 rounded-xl shadow-lg">
                        {huellaRegistrada ? "Recapturar" : "Capturar Huella"}
                    </Button>
                </div>

                <Button onClick={handleGuardarFinal} className="w-full bg-slate-900 hover:bg-black text-white h-16 rounded-2xl text-xl font-bold transition-all shadow-xl">
                    Finalizar Registro
                </Button>
            </div>

            <FingerprintModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onComplete={() => setHuellaRegistrada(true)}
                studentId={form.idAlumno}
                readerId={readerId}
            />
        </div>
    );
}

function InputField({ label, icon, value, onChange }: { label: string, icon: any, value: string, onChange: (v: string) => void }) {
    return (
        <div className="flex flex-col gap-2">
            <label className="text-sm font-semibold text-slate-600 ml-1">{label}</label>
            <div className="relative">
                <div className="absolute left-4 top-4 text-slate-400">{icon}</div>
                <input
                    type="text"
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    className="w-full bg-slate-50 border-none rounded-2xl p-4 pl-12 outline-none focus:ring-2 focus:ring-blue-500 text-slate-700 font-medium placeholder:text-slate-300"
                    placeholder={`Escribe aquí...`}
                />
            </div>
        </div>
    );
}
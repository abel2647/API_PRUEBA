"use client";

import React, { useState, useEffect } from "react";
import { Search, RotateCcw } from "lucide-react";

interface Historial {
    id: string;
    nombreCompleto: string;
    matricula: string;
    carrera: string;
    fechaHora: string;
    tipo: string;
}

const HistorialEstudiantes = () => {
    // Estados para los filtros
    const [nombre, setNombre] = useState("");
    const [paterno, setPaterno] = useState("");
    const [materno, setMaterno] = useState("");
    const [matricula, setMatricula] = useState("");

    // Estados para los datos y UI
    const [asistencias, setAsistencias] = useState<Historial[]>([]);
    const [loading, setLoading] = useState(false);

    // Función para obtener datos desde el backend
    const fetchAsistencias = async () => {
        setLoading(true);
        try {
            // Construimos los parámetros para la URL
            const queryParams = new URLSearchParams();
            if (nombre) queryParams.append("nombre", nombre);
            if (paterno) queryParams.append("paterno", paterno);
            if (materno) queryParams.append("materno", materno);
            if (matricula) queryParams.append("matricula", matricula);

            const response = await fetch(
                `http://localhost:8080/api/alumnos/asistencia/historial?${queryParams.toString()}`
            );

            if (response.ok) {
                const data = await response.json();
                setAsistencias(data);
            } else {
                console.error("Error al obtener el historial");
            }
        } catch (error) {
            console.error("Error de conexión con el servidor:", error);
        } finally {
            setLoading(false);
        }
    };

    // Carga inicial al montar el componente
    useEffect(() => {
        fetchAsistencias();
    }, []);

    // Función para limpiar filtros y recargar tabla
    const handleLimpiar = () => {
        setNombre("");
        setPaterno("");
        setMaterno("");
        setMatricula("");
        // Pequeño delay para asegurar que los estados se limpien antes de la petición
        setTimeout(() => {
            fetchAsistencias();
        }, 50);
    };

    return (
        <div className="p-6 bg-white rounded-lg shadow-md min-h-screen">
            <h2 className="text-2xl font-bold mb-6 text-gray-800">Consulta de Estudiantes</h2>

            {/* Grid de Filtros */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Nombre(s)</label>
                    <input
                        type="text"
                        className="border p-2 rounded w-full focus:ring-2 focus:ring-blue-500 outline-none"
                        value={nombre}
                        onChange={(e) => setNombre(e.target.value)}
                        placeholder="Ej: Juan"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Apellido Paterno</label>
                    <input
                        type="text"
                        className="border p-2 rounded w-full focus:ring-2 focus:ring-blue-500 outline-none"
                        value={paterno}
                        onChange={(e) => setPaterno(e.target.value)}
                        placeholder="Ej: Pérez"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Apellido Materno</label>
                    <input
                        type="text"
                        className="border p-2 rounded w-full focus:ring-2 focus:ring-blue-500 outline-none"
                        value={materno}
                        onChange={(e) => setMaterno(e.target.value)}
                        placeholder="Ej: García"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Matrícula</label>
                    <input
                        type="text"
                        className="border p-2 rounded w-full focus:ring-2 focus:ring-blue-500 outline-none"
                        value={matricula}
                        onChange={(e) => setMatricula(e.target.value)}
                        placeholder="Ej: 2021005"
                    />
                </div>
            </div>

            {/* Botones */}
            <div className="flex gap-3 mb-8">
                <button
                    onClick={fetchAsistencias}
                    className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2 rounded-md flex items-center gap-2 transition-all shadow-sm"
                >
                    <Search size={18} />
                    Buscar
                </button>
                <button
                    onClick={handleLimpiar}
                    className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-5 py-2 rounded-md flex items-center gap-2 transition-all border border-gray-300"
                >
                    <RotateCcw size={18} />
                    Limpiar
                </button>
            </div>

            {/* Tabla de Resultados */}
            <div className="overflow-x-auto border border-gray-200 rounded-lg shadow-sm">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-gray-50 text-gray-600 uppercase text-xs font-semibold">
                    <tr>
                        <th className="p-4 border-b">ID Registro</th>
                        <th className="p-4 border-b">Nombre del Alumno</th>
                        <th className="p-4 border-b">Matrícula</th>
                        <th className="p-4 border-b">Carrera / Clave</th>
                        <th className="p-4 border-b">Fecha y Hora</th>
                        <th className="p-4 border-b">Estatus</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white">
                    {loading ? (
                        <tr>
                            <td colSpan={6} className="p-10 text-center text-gray-400 animate-pulse">
                                Consultando base de datos...
                            </td>
                        </tr>
                    ) : asistencias.length > 0 ? (
                        asistencias.map((item) => (
                            <tr key={item.id} className="hover:bg-blue-50 transition-colors">
                                <td className="p-4 text-sm font-mono text-blue-600">{item.id}</td>
                                <td className="p-4 text-sm font-medium text-gray-900">{item.nombreCompleto}</td>
                                <td className="p-4 text-sm text-gray-600">{item.matricula}</td>
                                <td className="p-4 text-sm text-gray-600">{item.carrera}</td>
                                <td className="p-4 text-sm text-gray-600">{item.fechaHora}</td>
                                <td className="p-4">
                    <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-xs font-bold border border-green-200">
                      {item.tipo}
                    </span>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={6} className="p-12 text-center text-gray-500 italic">
                                No se encontraron registros que coincidan con los filtros.
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default HistorialEstudiantes;
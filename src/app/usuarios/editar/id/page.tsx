'use client';
import React, { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation'; // Ojo: useParams
import { Button } from '@/components/ui/button';

export default function EditarUsuarioPage() {
    const router = useRouter();
    const params = useParams(); // Obtiene el ID de la URL
    const { id } = params;

    const [formData, setFormData] = useState({ username: '', password: '', rol: '' });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Cargar datos actuales del usuario
        fetch(`http://localhost:8080/api/auth/users`) // Idealmente harías un endpoint getById, pero podemos filtrar de la lista si no creaste getById
            .then(res => res.json())
            .then((data: any[]) => {
                const found = data.find(u => u.id == id);
                if (found) {
                    setFormData({ username: found.username, password: found.password, rol: found.rol });
                }
                setLoading(false);
            });
    }, [id]);

    const handleUpdate = async (e: React.FormEvent) => {
        e.preventDefault();
        await fetch(`http://localhost:8080/api/auth/users/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData),
        });
        router.push('/usuarios/lista');
    };

    if(loading) return <div className="p-8">Cargando...</div>;

    return (
        <div className="p-8 max-w-2xl mx-auto">
            <h1 className="text-2xl font-bold mb-6">Editar Usuario #{id}</h1>
            <form onSubmit={handleUpdate} className="bg-white p-6 shadow rounded-lg space-y-4">
                 {/* Reutiliza los mismos inputs que en 'Nuevo' */}
                 <div>
                    <label className="block text-sm mb-1">Username</label>
                    <input className="w-full border p-2 rounded" value={formData.username} onChange={e => setFormData({...formData, username: e.target.value})} />
                 </div>
                 <div>
                    <label className="block text-sm mb-1">Password</label>
                    <input className="w-full border p-2 rounded" type="text" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} />
                 </div>
                 <div>
                    <label className="block text-sm mb-1">Rol</label>
                    <select className="w-full border p-2 rounded" value={formData.rol} onChange={e => setFormData({...formData, rol: e.target.value})}>
                        <option value="ADMIN">ADMIN</option>
                        <option value="EMPLEADO">EMPLEADO</option>
                    </select>
                 </div>
                 <Button type="submit" className="w-full bg-blue-600 text-white">Actualizar</Button>
            </form>
        </div>
    );
}
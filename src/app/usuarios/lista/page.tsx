'use client';

import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Search, Trash2, Edit, X, Save, AlertTriangle } from 'lucide-react';
import { useRouter } from 'next/navigation';

// Definimos la interfaz del Usuario
interface Usuario {
    id: number;
    username: string;
    rol: string;
    password?: string; // Opcional para cuando editamos
}

export default function ListaUsuariosPage() {
    const router = useRouter();
    
    // --- ESTADOS ---
    const [usuarios, setUsuarios] = useState<Usuario[]>([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);

    // Estados para MODALES
    const [userToDelete, setUserToDelete] = useState<Usuario | null>(null); // Si no es null, muestra modal borrar
    const [userToEdit, setUserToEdit] = useState<Usuario | null>(null);     // Si no es null, muestra modal editar

    // --- CARGAR DATOS ---
    useEffect(() => {
        fetchUsuarios();
    }, []);

    const fetchUsuarios = async () => {
        try {
            const res = await fetch('http://localhost:8080/api/auth/users');
            if (res.ok) {
                const data = await res.json();
                setUsuarios(data);
            }
        } catch (error) {
            console.error('Error al cargar usuarios:', error);
        } finally {
            setLoading(false);
        }
    };

    // --- LÓGICA DE BORRADO ---
    const confirmDelete = async () => {
        if (!userToDelete) return;

        try {
            const res = await fetch(`http://localhost:8080/api/auth/users/${userToDelete.id}`, {
                method: 'DELETE',
            });

            if (res.ok) {
                // Actualizamos la lista visualmente sin recargar
                setUsuarios(usuarios.filter(u => u.id !== userToDelete.id));
                setUserToDelete(null); // Cerrar modal
            } else {
                alert('No se pudo eliminar el usuario');
            }
        } catch (error) {
            console.error(error);
            alert('Error de conexión');
        }
    };

    // --- LÓGICA DE EDICIÓN ---
    const handleEditChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        if (userToEdit) {
            setUserToEdit({
                ...userToEdit,
                [e.target.name]: e.target.value
            });
        }
    };

    const saveEdit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!userToEdit) return;

        try {
            const res = await fetch(`http://localhost:8080/api/auth/users/${userToEdit.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(userToEdit),
            });

            if (res.ok) {
                const updatedUser = await res.json();
                // Actualizamos el usuario en la lista local
                setUsuarios(usuarios.map(u => (u.id === updatedUser.id ? updatedUser : u)));
                setUserToEdit(null); // Cerrar modal
            } else {
                alert('Error al actualizar');
            }
        } catch (error) {
            console.error(error);
        }
    };

    // --- FILTRADO ---
    const filteredUsers = usuarios.filter(user => 
        user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.rol.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="p-8 max-w-6xl mx-auto relative">
            {/* ENCABEZADO */}
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold text-gray-800">Lista de Usuarios</h1>
                <Button onClick={() => router.push('/usuarios/nuevo')} className="bg-blue-600 hover:bg-blue-700">
                    + Nuevo Usuario
                </Button>
            </div>

            {/* BUSCADOR */}
            <div className="relative mb-6">
                <Search className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
                <input
                    type="text"
                    placeholder="Buscar por usuario o rol..."
                    className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </div>

            {/* TABLA */}
            <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-gray-50 text-gray-700 uppercase text-xs font-semibold">
                        <tr>
                            <th className="px-6 py-3 border-b">ID</th>
                            <th className="px-6 py-3 border-b">Username</th>
                            <th className="px-6 py-3 border-b">Rol</th>
                            <th className="px-6 py-3 border-b text-center">Acciones</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                        {loading ? (
                            <tr><td colSpan={4} className="p-4 text-center">Cargando...</td></tr>
                        ) : filteredUsers.length === 0 ? (
                            <tr><td colSpan={4} className="p-4 text-center text-gray-500">No se encontraron usuarios.</td></tr>
                        ) : (
                            filteredUsers.map((user) => (
                                <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                                    <td className="px-6 py-4 text-gray-600">{user.id}</td>
                                    <td className="px-6 py-4 font-medium text-gray-900">{user.username}</td>
                                    <td className="px-6 py-4">
                                        <span className={`px-2 py-1 text-xs rounded-full font-semibold
                                            ${user.rol === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'}
                                        `}>
                                            {user.rol}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 flex justify-center gap-2">
                                        <Button 
                                            variant="ghost" 
                                            size="sm" 
                                            className="text-blue-600 hover:bg-blue-50"
                                            onClick={() => setUserToEdit(user)} // ABRIR MODAL EDITAR
                                        >
                                            <Edit className="h-4 w-4" />
                                        </Button>
                                        <Button 
                                            variant="ghost" 
                                            size="sm" 
                                            className="text-red-600 hover:bg-red-50"
                                            onClick={() => setUserToDelete(user)} // ABRIR MODAL BORRAR
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* ================= MODAL DE BORRAR ================= */}
            {userToDelete && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-in fade-in duration-200">
                    <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6 transform transition-all scale-100">
                        <div className="flex items-center gap-3 text-red-600 mb-4">
                            <AlertTriangle className="h-6 w-6" />
                            <h3 className="text-lg font-bold">Confirmar Eliminación</h3>
                        </div>
                        
                        <p className="text-gray-600 mb-6">
                            ¿Estás seguro que deseas borrar al usuario <span className="font-bold text-gray-900">&quot;{userToDelete.username}"</span>? 
                            <br/><span className="text-sm text-red-500">Esta acción no se puede deshacer.</span>
                        </p>
                        
                        <div className="flex justify-end gap-3">
                            <Button variant="outline" onClick={() => setUserToDelete(null)}>
                                Cancelar
                            </Button>
                            <Button className="bg-red-600 hover:bg-red-700 text-white" onClick={confirmDelete}>
                                Borrar Usuario
                            </Button>
                        </div>
                    </div>
                </div>
            )}

            {/* ================= MODAL DE EDITAR ================= */}
            {userToEdit && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-in fade-in duration-200">
                    <div className="bg-white rounded-lg shadow-xl max-w-lg w-full p-6">
                        <div className="flex justify-between items-center mb-6 border-b pb-2">
                            <h3 className="text-xl font-bold text-gray-800">Editar Usuario</h3>
                            <button onClick={() => setUserToEdit(null)} className="text-gray-400 hover:text-gray-600">
                                <X className="h-5 w-5" />
                            </button>
                        </div>

                        <form onSubmit={saveEdit} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre de Usuario</label>
                                <input
                                    type="text"
                                    name="username"
                                    value={userToEdit.username}
                                    onChange={handleEditChange}
                                    className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 outline-none"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
                                <input
                                    type="text" // Visible para editar facil, o password si prefieres ocultar
                                    name="password"
                                    value={userToEdit.password}
                                    onChange={handleEditChange}
                                    className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 outline-none"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Rol</label>
                                <select
                                    name="rol"
                                    value={userToEdit.rol}
                                    onChange={handleEditChange}
                                    className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 outline-none"
                                >
                                    <option value="ADMIN">ADMIN</option>
                                    <option value="EMPLEADO">EMPLEADO</option>
                                    <option value="INVITADO">INVITADO</option>
                                </select>
                            </div>

                            <div className="flex justify-end gap-3 pt-4">
                                <Button type="button" variant="outline" onClick={() => setUserToEdit(null)}>
                                    Cancelar
                                </Button>
                                <Button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white gap-2">
                                    <Save className="h-4 w-4" /> Guardar Cambios
                                </Button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
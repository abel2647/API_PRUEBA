'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';

export default function LoginPage() {

    const handleLogin = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        // 1. Extraemos los datos directamente de los inputs por su atributo 'name'
        const formData = new FormData(e.currentTarget);
        const formUser = formData.get('usuario')?.toString().trim();
        const formPass = formData.get('password')?.toString().trim();

        // 2. Aquí es donde asignas los valores correctos
        // IMPORTANTE: Escríbelos tal cual (minúsculas/mayúsculas importan)
        if (formUser === 'admin' && formPass === '1234') {
            console.log("Login exitoso");
            localStorage.setItem('isLogged', 'true');
            localStorage.setItem('userRole', 'ADMIN');

            // Forzamos el redireccionamiento para que el Sidebar se entere
            window.location.href = '/';
        } else {
            alert(`Acceso denegado.\nEscribiste: "${formUser}" / "${formPass}"\nSe esperaba: "admin" / "1234"`);
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-900 fixed inset-0 z-[100]">
            <Card className="w-full max-w-md shadow-2xl">
                <CardHeader>
                    <CardTitle className="text-2xl text-center font-bold">Iniciar Sesión</CardTitle>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleLogin} className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="usuario">Usuario</Label>
                            <Input
                                id="usuario"
                                name="usuario" // El 'name' debe coincidir con el formData.get
                                type="text"
                                placeholder="admin"
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="password">Contraseña</Label>
                            <Input
                                id="password"
                                name="password" // El 'name' debe coincidir con el formData.get
                                type="password"
                                placeholder="••••"
                                required
                            />
                        </div>
                        <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700">
                            Entrar al Sistema
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
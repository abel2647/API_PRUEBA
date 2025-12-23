'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

export default function LoginPage() {
    const [user, setUser] = useState('');
    const [pass, setPass] = useState('');
    const router = useRouter();

    const handleLogin = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        // Capturamos los datos directamente del formulario para evitar errores de sincronización
        const formData = new FormData(e.currentTarget);
        const formUser = formData.get('usuario')?.toString().trim();
        const formPass = formData.get('password')?.toString().trim();

        console.log("Intentando login con:", formUser, formPass); // Para que revises en la consola (F12)

        // Validación exacta
        if (formUser === 'admin' && formPass === '123456') {
            localStorage.setItem('isLogged', 'true');
            localStorage.setItem('userRole', 'ADMIN');
            // Usamos window.location.href para forzar la recarga completa del MainLayout
            window.location.href = '/';
        } else {
            alert('Credenciales inválidas.');
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-900">
            <Card className="w-[350px]">
                <CardHeader>
                    <CardTitle className="text-center">Iniciar Sesión</CardTitle>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleLogin} className="space-y-4">
                        <Input placeholder="Usuario" onChange={e => setUser(e.target.value)} />
                        <Input type="password" placeholder="Contraseña" onChange={e => setPass(e.target.value)} />
                        <Button type="submit" className="w-full">Entrar</Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
// Importamos el MainLayout que creamos en la carpeta components
// Usamos @/ para que Next.js encuentre la ruta sin importar en qué carpeta estés
import MainLayout from "@/components/MainLayout";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
    title: "Sistema de Registro",
    description: "Gestión de visitantes y estudiantes con autenticación",
};

export default function RootLayout({
                                       children,
                                   }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="es">
        <body className={inter.className}>
        {/* MainLayout envuelve a toda la aplicación.
            Él se encarga de:
            1. Verificar si el usuario está logueado.
            2. Mostrar el Sidebar solo si hay sesión.
            3. Redirigir al /login si no hay sesión.
        */}
        <MainLayout>
            {children}
        </MainLayout>
        </body>
        </html>
    );
}
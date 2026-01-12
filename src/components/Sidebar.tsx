'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { usePathname, useRouter } from 'next/navigation';
import { Home, GraduationCap, User, BarChart3, ChevronLeft, ChevronRight, ChevronDown, Users } from 'lucide-react';
// Interfaces
interface SubItem {
    label: string;
    path: string;
}

interface MenuItem {
    id: string;
    label: string;
    icon: React.ReactNode;
    path?: string; 
    subItems?: SubItem[];
}

interface SidebarProps {
    isCollapsed: boolean;
    setIsCollapsed: (value: boolean) => void;
}

export const Sidebar = ({ isCollapsed, setIsCollapsed }: SidebarProps) => {
    const router = useRouter();
    const pathname = usePathname();
    
    // Estado para controlar qué menú está abierto (guarda el ID del menú: 'estudiante' o 'visitante')
    const [openMenu, setOpenMenu] = useState<string | null>(null);

    const menuItems: MenuItem[] = [
        { id: 'inicio', label: 'Inicio', icon: <Home size={20} />, path: '/' },
        
        { 
            id: 'estudiante', 
            label: 'Estudiante', 
            icon: <GraduationCap size={20} />, 
            // path eliminado, ahora se maneja por subItems
            subItems: [
                { label: 'Registro', path: '/estudiantes' },        // Ruta principal
                { label: 'Consulta', path: '/consulta/alumnos' }    // Ruta de consulta
            ]
        },
        
        { 
            id: 'visitante', 
            label: 'Visitante', 
            icon: <User size={20} />, 
            subItems: [
                { label: 'Registro', path: '/visitantes' },         // Ruta principal
                { label: 'Consulta', path: '/visitantes/historial' } // Ruta de consulta
            ]
        },
    { 
        id: 'usuarios', 
        label: 'Usuarios', 
        icon: <Users size={20} />, 
        subItems: [
            { label: 'Nuevo Usuario', path: '/usuarios/nuevo' },
            { label: 'Lista Usuarios', path: '/usuarios/lista' }
        ]
    },

    {   id: 'estadistica', 
        label: 'Estadística', 
        icon: <BarChart3 size={20} />, 
        path: '/estadisticas' },
];

    const isActive = (path: string) => pathname === path;

    // Verifica si algún hijo está activo para mantener el padre resaltado
    const isParentActive = (item: MenuItem) => {
        return item.subItems?.some((sub) => pathname === sub.path) ?? false;
    };

    const handleItemClick = (item: MenuItem) => {
        if (item.subItems) {
            // Lógica para menús desplegables
            if (isCollapsed) {
                setIsCollapsed(false); // Abrir sidebar si está colapsada
                setOpenMenu(item.id);  // Abrir este menú
            } else {
                // Si ya está abierto este menú, lo cerramos. Si no, lo abrimos.
                setOpenMenu(openMenu === item.id ? null : item.id);
            }
        } else if (item.path) {
            // Lógica para enlaces normales
            router.push(item.path);
        }
    };

    return (
        <aside className={cn(
            "bg-gray-900 text-white transition-all duration-300 flex flex-col h-screen fixed left-0 top-0 z-40",
            isCollapsed ? "w-16" : "w-64"
        )}>
            {/* Header */}
            <div className="p-4 border-b border-gray-800 flex items-center justify-between h-16">
                {!isCollapsed && <span className="font-bold text-lg tracking-tight">Sistema Registro</span>}
                <div className={cn("text-xl", isCollapsed && "mx-auto")}></div>
            </div>

            {/* Menú */}
            <nav className="flex-1 p-3 overflow-y-auto">
                <ul className="space-y-2">
                    {menuItems.map((item) => (
                        <li key={item.id}>
                            <Button
                                variant="ghost"
                                className={cn(
                                    "w-full text-white hover:bg-gray-800 flex items-center transition-all justify-between",
                                    (item.path && isActive(item.path)) || isParentActive(item) ? "bg-gray-800 border-r-4 border-blue-500" : "",
                                    isCollapsed ? "justify-center px-0" : "justify-between px-4"
                                )}
                                onClick={() => handleItemClick(item)}
                            >
                                <div className="flex items-center">
                                    <span className={cn(isCollapsed ? "mr-0" : "mr-3")}>{item.icon}</span>
                                    {!isCollapsed && <span>{item.label}</span>}
                                </div>
                                
                                {/* Flecha para desplegables */}
                                {item.subItems && !isCollapsed && (
                                    <ChevronDown 
                                        size={16} 
                                        className={cn("transition-transform duration-200", openMenu === item.id ? "rotate-180" : "")} 
                                    />
                                )}
                            </Button>

                            {/* Renderizado de Sub-items */}
                            {item.subItems && openMenu === item.id && !isCollapsed && (
                                <ul className="mt-1 bg-gray-950/50 rounded-md overflow-hidden animate-in slide-in-from-top-2 duration-200">
                                    {item.subItems.map((subItem) => (
                                        <li key={subItem.path}>
                                            <Button
                                                variant="ghost"
                                                className={cn(
                                                    "w-full text-gray-400 hover:text-white hover:bg-gray-800 flex items-center justify-start pl-12 py-2 h-9 text-sm",
                                                    isActive(subItem.path) ? "text-blue-400 bg-gray-800/50" : ""
                                                )}
                                                onClick={() => router.push(subItem.path)}
                                            >
                                                {subItem.label}
                                            </Button>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </li>
                    ))}
                </ul>
            </nav>

            {/* Botón de Colapso */}
            <div className="p-4 border-t border-gray-800">
                <Button
                    variant="ghost"
                    className={cn(
                        "w-full text-white hover:bg-gray-800",
                        isCollapsed ? "justify-center" : "justify-start"
                    )}
                    onClick={() => setIsCollapsed(!isCollapsed)}
                >
                    {isCollapsed ? <ChevronRight size={20} /> : (
                        <div className="flex items-center">
                            <ChevronLeft size={20} className="mr-3" />
                            <span>Ocultar Menú</span>
                        </div>
                    )}
                </Button>
            </div>
        </aside>
    );
};
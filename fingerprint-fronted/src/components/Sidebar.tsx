'use client';

import React from 'react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { usePathname, useRouter } from 'next/navigation';
import { Home, GraduationCap, User, Search, BarChart3, ChevronLeft, ChevronRight } from 'lucide-react';

interface SidebarProps {
    isCollapsed: boolean;
    setIsCollapsed: (value: boolean) => void;
}

export const Sidebar = ({ isCollapsed, setIsCollapsed }: SidebarProps) => {
    const router = useRouter();
    const pathname = usePathname();

    const menuItems = [
        { id: 'inicio', label: 'Inicio', icon: <Home size={20} />, path: '/' },
        { id: 'estudiante', label: 'Estudiante', icon: <GraduationCap size={20} />, path: '/estudiantes' },
        { id: 'visitante', label: 'Visitante', icon: <User size={20} />, path: '/visitantes' },
        { id: 'consulta', label: 'Consulta', icon: <Search size={20} />, path: '/consulta' },
        { id: 'estadistica', label: 'Estadística', icon: <BarChart3 size={20} />, path: '/estadistica' },
    ];

    const isActive = (path: string) => pathname === path;

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
                                    "w-full text-white hover:bg-gray-800 flex items-center transition-all",
                                    isActive(item.path) ? "bg-gray-800 border-r-4 border-blue-500" : "",
                                    isCollapsed ? "justify-center px-0" : "justify-start px-4"
                                )}
                                onClick={() => router.push(item.path)}
                            >
                                <span className={cn(isCollapsed ? "mr-0" : "mr-3")}>{item.icon}</span>
                                {!isCollapsed && <span>{item.label}</span>}
                            </Button>
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
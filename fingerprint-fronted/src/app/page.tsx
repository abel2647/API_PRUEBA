// src/app/page.tsx
import { RegistroVisitanteForm } from '@/components/RegistroVisitantesForm';

export default function Home() {
    return (
        <main className="flex min-h-screen items-center justify-center p-24 bg-gray-50">
            <RegistroVisitanteForm />
        </main>
    );
}
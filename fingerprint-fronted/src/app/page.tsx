// src/app/page.tsx
import { RegistroVisitanteForm } from '@/components/RegistroVisitantesForm';

export default function Home() {
    return (
        <main className="flex min-h-screen justify-center p-4 bg-gray-50 w-full">
            <RegistroVisitanteForm />
        </main>
    );
}
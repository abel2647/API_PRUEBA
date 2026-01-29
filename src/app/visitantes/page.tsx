'use client';

import { RegistroVisitanteForm } from '@/components/RegistroVisitantesForm';
import { ValidationModal } from '@/components/ValidationModal';
import { useSearchParams } from 'next/navigation';

export default function Home() {

    const searchParams = useSearchParams();
    const puerta = searchParams.get('puerta') || "1";
    return (
        <>
            {
            }
            <RegistroVisitanteForm />
            {/*<ValidationModal />*/}
            <ValidationModal puertaInicial={puerta}/>
        </>
    );
}
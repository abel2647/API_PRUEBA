// src/components/QRCodeDisplay.tsx

'use client';

import React from 'react';
// CORRECCIÓN: Usar importación nombrada (QRCodeSVG)
import { QRCodeSVG } from 'qrcode.react';

interface QRCodeDisplayProps {
    value: string; // El UUID del visitante
}

export const QRCodeDisplay: React.FC<QRCodeDisplayProps> = ({ value }) => {
    if (!value) return null;

    return (
        <div className="p-2 border border-gray-300">
            {/* Usar el componente QRCodeSVG */}
            <QRCodeSVG
                value={value}
                size={180}
                level="H"
            />
        </div>
    );
};
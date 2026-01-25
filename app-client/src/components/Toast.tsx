import React, { useEffect, useState } from 'react';
import './toast.scss';

interface ToastProps {
    message: string;
    type?: 'success' | 'error';
    duration?: number;
}

const Toast: React.FC<ToastProps> = ({ message, type = 'success', duration = 5000 }) => {
    const [visible, setVisible] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => {
            setVisible(false);
        }, duration);

        return () => clearTimeout(timer);
    }, [duration]);

    const fadeOutDelay = `${(duration - 500) / 1000}s`;

    return visible ? (
        <div className={`toast toast-${type}`} style={{ '--fade-out-delay': fadeOutDelay } as React.CSSProperties}>
            {message}
        </div>
    ) : null;
};

export default Toast;
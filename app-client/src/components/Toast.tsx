import React, { useEffect, useState } from 'react';
import './toast.scss';

interface ToastProps {
    message: string;
    duration?: number;
}

const Toast: React.FC<ToastProps> = ({ message, duration = 5000 }) => {
    const [visible, setVisible] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => {
            setVisible(false);
        }, duration);

        return () => clearTimeout(timer);
    }, [duration]);

    const fadeOutDelay = `${(duration - 500) / 1000}s`;

    return visible ? (
        <div className="toast" style={{ '--fade-out-delay': fadeOutDelay } as React.CSSProperties}>
            {message}
        </div>
    ) : null;
};

export default Toast;
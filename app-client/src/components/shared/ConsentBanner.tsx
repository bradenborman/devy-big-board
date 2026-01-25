import React, { useEffect, useState } from 'react';
import './consentBanner.scss';

const ConsentBanner: React.FC = () => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        const consent = localStorage.getItem('cookie_consent');

        if (consent === 'granted') {
            window.gtag?.('consent', 'update', {
                ad_storage: 'granted',
                analytics_storage: 'granted',
            });
            window.gtag?.('config', 'G-03DV8L1WEX');
        } else if (consent === 'denied') {
            window.gtag?.('consent', 'update', {
                ad_storage: 'denied',
                analytics_storage: 'denied',
            });
        } else {
            setVisible(true);
        }
    }, []);

    const acceptCookies = () => {
        localStorage.setItem('cookie_consent', 'granted');

        window.gtag?.('consent', 'update', {
            ad_storage: 'granted',
            analytics_storage: 'granted',
        });

        window.gtag?.('config', 'G-03DV8L1WEX');

        setVisible(false);
    };

    const declineCookies = () => {
        localStorage.setItem('cookie_consent', 'denied');

        window.gtag?.('consent', 'update', {
            ad_storage: 'denied',
            analytics_storage: 'denied',
        });

        setVisible(false);
    };

    if (!visible) return null;

    return (
        <div className="consent-banner">
            <p>This site uses cookies to improve your experience. Do you accept analytics tracking?</p>
            <div className="buttons">
                <button onClick={acceptCookies}>Accept</button>
                <button onClick={declineCookies}>Decline</button>
            </div>
        </div>
    );
};

export default ConsentBanner;
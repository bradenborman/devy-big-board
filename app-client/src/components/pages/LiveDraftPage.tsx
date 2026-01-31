import React from 'react';
import { useNavigate } from 'react-router-dom';
import './stub-page.scss';

const LiveDraftPage: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div className="stub-page">
            <nav className="navbar">
                <div className="nav-content">
                    <div className="logo" onClick={() => navigate('/')}>
                        <span className="logo-icon">🏈</span>
                        <span className="logo-text">Devy BigBoard</span>
                    </div>
                    <button onClick={() => navigate('/')} className="back-btn">
                        ← Back to Home
                    </button>
                </div>
            </nav>

            <div className="stub-content">
                <div className="stub-icon">🚀</div>
                <h1>Live Draft Mode</h1>
                <p className="subtitle">Real-time drafting with your league</p>

                <div className="cta-section">
                    <button className="primary-btn" onClick={() => navigate('/live-draft/setup')}>
                        Start a Draft
                    </button>
                </div>
            </div>
        </div>
    );
};

export default LiveDraftPage;

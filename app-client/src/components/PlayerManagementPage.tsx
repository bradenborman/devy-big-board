import React from 'react';
import { useNavigate } from 'react-router-dom';
import './stub-page.scss';

const PlayerManagementPage: React.FC = () => {
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
                <div className="stub-icon">👥</div>
                <h1>Player Management</h1>
                <p className="subtitle">Manage your player database</p>
                
                <div className="coming-soon-badge">Coming Soon</div>
                
                <div className="feature-preview">
                    <h2>What's Coming:</h2>
                    <ul>
                        <li>
                            <span className="check">✓</span>
                            <span>Add and edit player information</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>Verify player submissions from the community</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>View comprehensive player statistics</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>Track ADP trends over time</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>Import players from CSV or spreadsheet</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>Custom player notes and rankings</span>
                        </li>
                    </ul>
                </div>

                <div className="info-box">
                    <h3>Current Player Database</h3>
                    <p>
                        Our database currently includes rookie prospects from the 2025-2029 draft classes.
                        Players are automatically added to your draft board when you start an offline draft.
                    </p>
                </div>

                <div className="alternative">
                    <p>Ready to start drafting?</p>
                    <button onClick={() => navigate('/offline-draft')} className="alt-btn">
                        Start Offline Draft
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PlayerManagementPage;

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
                
                <div className="coming-soon-badge">Coming Soon</div>
                
                <div className="feature-preview">
                    <h2>What's Coming:</h2>
                    <ul>
                        <li>
                            <span className="check">✓</span>
                            <span>Host live drafts with up to 16 teams</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>Real-time synchronization across all devices</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>Draft timer with customizable pick clock</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>Live chat with league members</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>Commissioner controls and draft management</span>
                        </li>
                        <li>
                            <span className="check">✓</span>
                            <span>Automatic ADP tracking across all live drafts</span>
                        </li>
                    </ul>
                </div>

                <div className="cta-section">
                    <p>Want to be notified when live drafts launch?</p>
                    <button className="notify-btn" onClick={() => alert('Email notifications coming soon!')}>
                        Notify Me
                    </button>
                </div>

                <div className="alternative">
                    <p>In the meantime, try our offline draft mode:</p>
                    <button onClick={() => navigate('/offline-draft')} className="alt-btn">
                        Start Offline Draft
                    </button>
                </div>
            </div>
        </div>
    );
};

export default LiveDraftPage;

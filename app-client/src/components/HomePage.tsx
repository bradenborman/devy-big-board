import React from 'react';
import { useNavigate } from 'react-router-dom';
import './homepage.scss';

const HomePage: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div className="homepage">
            <nav className="navbar">
                <div className="nav-content">
                    <div className="logo">
                        <span className="logo-icon">🏈</span>
                        <span className="logo-text">Devy BigBoard</span>
                    </div>
                    <div className="nav-links">
                        <button onClick={() => navigate('/offline-draft')} className="nav-link">
                            Offline Draft
                        </button>
                        <button onClick={() => navigate('/live-draft')} className="nav-link">
                            Live Draft
                        </button>
                        <button onClick={() => navigate('/player-management')} className="nav-link">
                            Players
                        </button>
                    </div>
                </div>
            </nav>

            <section className="hero">
                <div className="hero-content">
                    <h1 className="hero-title">
                        Master Your<br /><span className="gradient-text">Devy Draft</span>
                    </h1>
                    <p className="hero-subtitle">
                        The ultimate tool for dynasty fantasy football rookie drafts. 
                        Track players, manage your board, and dominate your league.
                    </p>
                    <div className="hero-buttons">
                        <button 
                            onClick={() => navigate('/offline-draft')} 
                            className="btn btn-primary"
                        >
                            Start Offline Draft
                        </button>
                        <button 
                            onClick={() => navigate('/live-draft')} 
                            className="btn btn-secondary"
                        >
                            Join Live Draft
                        </button>
                    </div>
                </div>
                <div className="hero-image">
                    <div className="floating-card card-1">
                        <div className="card-header">QB</div>
                        <div className="card-body">Caleb Williams</div>
                        <div className="card-footer">ADP: 1.02</div>
                    </div>
                    <div className="floating-card card-2">
                        <div className="card-header">RB</div>
                        <div className="card-body">Bijan Robinson</div>
                        <div className="card-footer">ADP: 1.01</div>
                    </div>
                    <div className="floating-card card-3">
                        <div className="card-header">WR</div>
                        <div className="card-body">Marvin Harrison Jr</div>
                        <div className="card-footer">ADP: 1.03</div>
                    </div>
                </div>
            </section>

            <section className="features">
                <h2 className="section-title">Everything You Need to Draft Smart</h2>
                <div className="features-grid">
                    <div className="feature-card">
                        <div className="feature-icon">📊</div>
                        <h3>Real-Time ADP</h3>
                        <p>Track average draft position across all completed drafts to make informed decisions.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">👥</div>
                        <h3>Crowd-Sourced Players</h3>
                        <p>The community can add missing players to keep the database up-to-date and comprehensive.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">🔄</div>
                        <h3>Drag & Drop</h3>
                        <p>Intuitive interface lets you quickly add players to your board with a single click.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">📤</div>
                        <h3>Export & Share</h3>
                        <p>Export your completed draft to PDF, CSV, or share a link with your league.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">👥</div>
                        <h3>Live Drafts</h3>
                        <p>Host real-time drafts with your league members (Coming Soon).</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">⚡</div>
                        <h3>Lightning Fast</h3>
                        <p>No lag, no delays. Built for speed and performance during your draft.</p>
                    </div>
                </div>
            </section>

            <section className="cta">
                <div className="cta-content">
                    <h2>Ready to Dominate Your Draft?</h2>
                    <p>Join thousands of dynasty managers using Devy BigBoard</p>
                    <button 
                        onClick={() => navigate('/offline-draft')} 
                        className="btn btn-large"
                    >
                        Get Started Free
                    </button>
                </div>
            </section>

            <footer className="footer">
                <div className="footer-content">
                    <p>© 2025 Devy BigBoard. Built for dynasty fantasy football managers.</p>
                    <div className="footer-links">
                        <a href="https://github.com" target="_blank" rel="noopener noreferrer">GitHub</a>
                        <span>•</span>
                        <a href="mailto:support@devybigboard.com">Contact</a>
                    </div>
                </div>
            </footer>
        </div>
    );
};

export default HomePage;

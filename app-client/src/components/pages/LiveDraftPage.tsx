import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './stub-page.scss';

interface LobbyDraft {
    uuid: string;
    draftName: string;
    createdBy: string;
    participantCount: number;
    totalRounds: number;
    lobbyUrl: string;
    createdAt: string;
}

const LiveDraftPage: React.FC = () => {
    const navigate = useNavigate();
    const [lobbyDrafts, setLobbyDrafts] = useState<LobbyDraft[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchLobbyDrafts();
    }, []);

    const fetchLobbyDrafts = async () => {
        try {
            const response = await fetch('/api/live-drafts/lobbies');
            if (response.ok) {
                const data = await response.json();
                setLobbyDrafts(data);
            }
        } catch (error) {
            console.error('Failed to fetch lobby drafts:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleJoinLobby = (uuid: string) => {
        navigate(`/draft/${uuid}/lobby`);
    };

    return (
        <div className="stub-page live-draft-page">
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
                <div className="page-header">
                    <div className="stub-icon">🚀</div>
                    <h1>Live Draft Mode</h1>
                    <p className="subtitle">Real-time drafting with your league</p>
                </div>

                <div className="content-wrapper">
                    <div className="create-draft-section">
                        <div className="section-content">
                            <h2>Start a New Draft</h2>
                            <p>Create a draft lobby and invite your league members</p>
                            <button className="create-btn" onClick={() => navigate('/live-draft/setup')}>
                                <span className="btn-icon">➕</span>
                                Create Draft
                            </button>
                        </div>
                    </div>

                    {loading ? (
                        <div className="lobbies-section">
                            <h2>Available Lobbies</h2>
                            <div className="lobbies-loading">
                                <div className="spinner"></div>
                                <p>Loading lobbies...</p>
                            </div>
                        </div>
                    ) : lobbyDrafts.length > 0 ? (
                        <div className="lobbies-section">
                            <h2>Available Lobbies</h2>
                            <p className="section-subtitle">Join an existing draft</p>
                            <div className="lobbies-grid">
                                {lobbyDrafts.map((draft) => (
                                    <div key={draft.uuid} className="lobby-card">
                                        <div className="lobby-card-header">
                                            <h3>{draft.draftName}</h3>
                                            <span className="status-badge">Open</span>
                                        </div>
                                        <div className="lobby-card-body">
                                            <div className="lobby-info">
                                                <div className="info-row">
                                                    <span className="info-icon">👤</span>
                                                    <span className="info-text">{draft.createdBy}</span>
                                                </div>
                                                <div className="info-row">
                                                    <span className="info-icon">👥</span>
                                                    <span className="info-text">{draft.participantCount} Teams</span>
                                                </div>
                                                <div className="info-row">
                                                    <span className="info-icon">🔄</span>
                                                    <span className="info-text">{draft.totalRounds} Rounds</span>
                                                </div>
                                            </div>
                                        </div>
                                        <button 
                                            className="join-lobby-btn"
                                            onClick={() => handleJoinLobby(draft.uuid)}
                                        >
                                            Join Lobby →
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ) : (
                        <div className="lobbies-section">
                            <h2>Available Lobbies</h2>
                            <div className="empty-lobbies">
                                <div className="empty-icon">📭</div>
                                <h3>No Open Lobbies</h3>
                                <p>Be the first to create a draft!</p>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default LiveDraftPage;

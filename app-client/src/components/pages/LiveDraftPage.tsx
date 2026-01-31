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

                {loading ? (
                    <div className="lobbies-loading">
                        <div className="spinner"></div>
                        <p>Loading available lobbies...</p>
                    </div>
                ) : lobbyDrafts.length > 0 ? (
                    <div className="lobbies-section">
                        <h2>Available Lobbies</h2>
                        <p className="lobbies-subtitle">Join an existing draft lobby</p>
                        <div className="lobbies-list">
                            {lobbyDrafts.map((draft) => (
                                <div key={draft.uuid} className="lobby-card">
                                    <div className="lobby-header">
                                        <h3>{draft.draftName}</h3>
                                        <span className="lobby-badge">Lobby</span>
                                    </div>
                                    <div className="lobby-details">
                                        <div className="detail-item">
                                            <span className="detail-label">Created by:</span>
                                            <span className="detail-value">{draft.createdBy}</span>
                                        </div>
                                        <div className="detail-item">
                                            <span className="detail-label">Teams:</span>
                                            <span className="detail-value">{draft.participantCount}</span>
                                        </div>
                                        <div className="detail-item">
                                            <span className="detail-label">Rounds:</span>
                                            <span className="detail-value">{draft.totalRounds}</span>
                                        </div>
                                    </div>
                                    <button 
                                        className="join-btn"
                                        onClick={() => handleJoinLobby(draft.uuid)}
                                    >
                                        Join Lobby
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>
                ) : null}
            </div>
        </div>
    );
};

export default LiveDraftPage;

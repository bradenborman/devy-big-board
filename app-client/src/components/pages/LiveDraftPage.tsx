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

            <div className="live-draft-grid-container">
                <h1 className="page-title">Live Drafts</h1>
                
                <div className="drafts-grid">
                    {/* Create New Draft Card */}
                    <div className="draft-card create-card" onClick={() => navigate('/live-draft/setup')}>
                        <div className="card-icon">+</div>
                        <h3>Create New Draft</h3>
                        <p>Start a new draft lobby</p>
                    </div>

                    {/* Loading State */}
                    {loading && (
                        <div className="draft-card loading-card">
                            <div className="spinner"></div>
                            <p>Loading...</p>
                        </div>
                    )}

                    {/* Lobby Cards */}
                    {!loading && lobbyDrafts.map((draft) => (
                        <div 
                            key={draft.uuid} 
                            className="draft-card lobby-card"
                            onClick={() => handleJoinLobby(draft.uuid)}
                        >
                            <div className="card-header">
                                <h3>{draft.draftName}</h3>
                                <span className="badge">Open</span>
                            </div>
                            <div className="card-details">
                                <div className="detail-item">
                                    <span className="label">Host:</span>
                                    <span className="value">{draft.createdBy}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="label">Teams:</span>
                                    <span className="value">{draft.participantCount}</span>
                                </div>
                                <div className="detail-item">
                                    <span className="label">Rounds:</span>
                                    <span className="value">{draft.totalRounds}</span>
                                </div>
                            </div>
                            <div className="card-action">
                                Join →
                            </div>
                        </div>
                    ))}

                    {/* Empty State */}
                    {!loading && lobbyDrafts.length === 0 && (
                        <div className="draft-card empty-card">
                            <div className="empty-icon">📭</div>
                            <p>No open lobbies</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default LiveDraftPage;

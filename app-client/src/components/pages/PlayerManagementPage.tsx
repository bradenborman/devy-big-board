import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AddPlayerModal from '../modals/AddPlayerModal';
import EditPlayerModal from '../modals/EditPlayerModal';
import { Player } from '../draft/BigBoard';
import Toast from '../shared/Toast';
import './stub-page.scss';
import './playerManagement.scss';

interface PlayerWithId extends Player {
    id?: number;
    verified?: boolean;
    createdAt?: string;
    imageUrl?: string;
}

type ViewMode = 'table' | 'cards';

const INITIAL_CARDS_SHOWN = 8;

const PlayerManagementPage: React.FC = () => {
    const navigate = useNavigate();
    const [players, setPlayers] = useState<PlayerWithId[]>([]);
    const [loading, setLoading] = useState(true);
    const [showAddModal, setShowAddModal] = useState(false);
    const [editingPlayer, setEditingPlayer] = useState<PlayerWithId | null>(null);
    const [deleteConfirm, setDeleteConfirm] = useState<number | null>(null);
    const [verificationCode, setVerificationCode] = useState('');
    const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
    const [viewMode, setViewMode] = useState<ViewMode>('cards');
    const [activeMenu, setActiveMenu] = useState<number | null>(null);
    const [expandedPositions, setExpandedPositions] = useState<Record<string, boolean>>({});
    const [selectedYears, setSelectedYears] = useState<number[]>([new Date().getFullYear()]);
    const [playersWithHeadshots, setPlayersWithHeadshots] = useState<Set<number>>(new Set());

    useEffect(() => {
        fetchPlayers();
        fetchPlayersWithHeadshots();
    }, []);

    const fetchPlayers = async () => {
        try {
            console.log('[PlayerManagement] Fetching players...');
            const response = await fetch('/api/players/manage');
            if (response.ok) {
                const data = await response.json();
                console.log('[PlayerManagement] Fetched players:', data);
                setPlayers(data);
            }
        } catch (error) {
            console.error('[PlayerManagement] Failed to load players:', error);
            showToast('Failed to load players', 'error');
        } finally {
            setLoading(false);
        }
    };

    const fetchPlayersWithHeadshots = async () => {
        try {
            console.log('[PlayerManagement] Fetching players with headshots...');
            const response = await fetch('/api/players/manage/headshots/available');
            if (response.ok) {
                const playerIds: number[] = await response.json();
                console.log('[PlayerManagement] Players with headshots:', playerIds);
                setPlayersWithHeadshots(new Set(playerIds));
            }
        } catch (error) {
            console.error('[PlayerManagement] Failed to load headshot info:', error);
        }
    };

    const showToast = (message: string, type: 'success' | 'error') => {
        setToast({ message, type });
        setTimeout(() => setToast(null), 3000);
    };

    const handleAddPlayer = async (player: Player, code?: string) => {
        try {
            const response = await fetch('/api/players/manage', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: player.name,
                    position: player.position,
                    team: player.team,
                    college: player.college,
                    draftyear: player.draftyear,
                    verificationCode: code
                })
            });

            if (response.ok) {
                const newPlayer = await response.json();
                setPlayers([...players, newPlayer]);
                showToast(
                    newPlayer.verified 
                        ? 'Player added and verified!' 
                        : 'Player added as pending. Awaiting verification.',
                    'success'
                );
            } else {
                showToast('Failed to add player', 'error');
            }
        } catch (error) {
            showToast('Failed to add player', 'error');
        }
    };

    const handleUpdatePlayer = async (player: PlayerWithId, code: string) => {
        try {
            const response = await fetch(`/api/players/manage/${player.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: player.name,
                    position: player.position,
                    team: player.team,
                    college: player.college,
                    draftyear: player.draftyear,
                    verificationCode: code
                })
            });

            if (response.ok) {
                const updated = await response.json();
                setPlayers(players.map(p => p.id === updated.id ? updated : p));
                setEditingPlayer(null);
                showToast('Player updated successfully', 'success');
                // Refresh to get latest image
                setTimeout(() => {
                    fetchPlayers();
                    fetchPlayersWithHeadshots();
                }, 500);
            } else if (response.status === 403) {
                showToast('Invalid verification code', 'error');
            } else {
                showToast('Failed to update player', 'error');
            }
        } catch (error) {
            showToast('Failed to update player', 'error');
        }
    };

    const handleDeletePlayer = async (playerId: number) => {
        if (!verificationCode) {
            showToast('Verification code is required', 'error');
            return;
        }

        try {
            const response = await fetch(`/api/players/manage/${playerId}?code=${encodeURIComponent(verificationCode)}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                setPlayers(players.filter(p => p.id !== playerId));
                setDeleteConfirm(null);
                setVerificationCode('');
                showToast('Player deleted successfully', 'success');
            } else if (response.status === 403) {
                showToast('Invalid verification code', 'error');
            } else {
                showToast('Failed to delete player', 'error');
            }
        } catch (error) {
            showToast('Failed to delete player', 'error');
        }
    };

    const filteredPlayers = players.filter(p => {
        // In card view, only show verified players
        if (viewMode === 'cards' && !p.verified) return false;
        
        // Filter by draft year
        if (selectedYears.length > 0 && p.draftyear && !selectedYears.includes(p.draftyear)) {
            return false;
        }
        
        return true;
    });

    const getPlayersByPosition = (position: string) => {
        return filteredPlayers.filter(p => p.position === position);
    };

    const getVisiblePlayers = (position: string) => {
        const positionPlayers = getPlayersByPosition(position);
        if (expandedPositions[position]) {
            return positionPlayers;
        }
        return positionPlayers.slice(0, INITIAL_CARDS_SHOWN);
    };

    const togglePositionExpanded = (position: string) => {
        setExpandedPositions(prev => ({
            ...prev,
            [position]: !prev[position]
        }));
    };

    const toggleYear = (year: number) => {
        setSelectedYears(prev => {
            if (prev.includes(year)) {
                return prev.filter(y => y !== year);
            } else {
                return [...prev, year].sort();
            }
        });
    };

    const getAvailableYears = () => {
        const years = new Set<number>();
        players.forEach(p => {
            if (p.draftyear) years.add(p.draftyear);
        });
        return Array.from(years).sort();
    };

    const verifiedPlayerCount = players.filter(p => p.verified).length;
    const pendingPlayerCount = players.filter(p => !p.verified).length;

    const getPlayerInitials = (name: string) => {
        const parts = name.split(' ');
        if (parts.length >= 2) {
            return parts[0][0] + parts[parts.length - 1][0];
        }
        return name.substring(0, 2);
    };

    const toggleMenu = (playerId: number) => {
        setActiveMenu(activeMenu === playerId ? null : playerId);
    };

    return (
        <div className="stub-page player-management-page">
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

            <div className="management-content">
                <div className="header-section">
                    <div>
                        <h1>Player Pool</h1>
                        <p className="pool-stats">
                            {filteredPlayers.length} player{filteredPlayers.length !== 1 ? 's' : ''} 
                            {viewMode === 'table' && ` (${verifiedPlayerCount} verified, ${pendingPlayerCount} pending)`}
                        </p>
                    </div>
                    <div className="header-actions">
                        <div className="view-toggle">
                            <button 
                                className={viewMode === 'cards' ? 'active' : ''} 
                                onClick={() => setViewMode('cards')}
                                title="Card View"
                            >
                                ⊞
                            </button>
                            <button 
                                className={viewMode === 'table' ? 'active' : ''} 
                                onClick={() => setViewMode('table')}
                                title="Table View"
                            >
                                ☰
                            </button>
                        </div>
                        <button className="add-btn" onClick={() => setShowAddModal(true)}>
                            + Add Player
                        </button>
                    </div>
                </div>

                <div className="filter-section">
                    <span className="filter-label">Draft Class:</span>
                    {getAvailableYears().length > 0 ? (
                        getAvailableYears().map(year => (
                            <button
                                key={year}
                                className={selectedYears.includes(year) ? 'active' : ''}
                                onClick={() => toggleYear(year)}
                            >
                                {year}
                            </button>
                        ))
                    ) : (
                        <span style={{ color: 'rgba(255, 255, 255, 0.6)', fontSize: '0.9rem' }}>
                            No draft years available
                        </span>
                    )}
                </div>

                {loading ? (
                    <div className="loading">Loading players...</div>
                ) : viewMode === 'table' ? (
                    <div className="players-table">
                        <table>
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Position</th>
                                    <th>Team</th>
                                    <th>College</th>
                                    <th>Draft Year</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredPlayers.map(player => (
                                    <tr key={player.id}>
                                        <td>{player.name}</td>
                                        <td>{player.position}</td>
                                        <td>{player.team || '-'}</td>
                                        <td>{player.college || '-'}</td>
                                        <td>{player.draftyear || '-'}</td>
                                        <td>
                                            <span className={`status-badge ${player.verified ? 'verified' : 'pending'}`}>
                                                {player.verified ? '✓ Verified' : '⏳ Pending'}
                                            </span>
                                        </td>
                                        <td className="actions">
                                            <button 
                                                className="edit-btn"
                                                onClick={() => setEditingPlayer(player)}
                                            >
                                                Edit
                                            </button>
                                            <button 
                                                className="delete-btn"
                                                onClick={() => setDeleteConfirm(player.id!)}
                                            >
                                                Delete
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        {filteredPlayers.length === 0 && (
                            <div className="empty-state">
                                No players found for selected draft class{selectedYears.length > 1 ? 'es' : ''}
                            </div>
                        )}
                    </div>
                ) : (
                    <div className="cards-view">
                        {['QB', 'RB', 'WR', 'TE'].map(position => {
                            const positionPlayers = getPlayersByPosition(position);
                            const visiblePlayers = getVisiblePlayers(position);
                            const hasMore = positionPlayers.length > INITIAL_CARDS_SHOWN;
                            const isExpanded = expandedPositions[position];
                            
                            if (positionPlayers.length === 0) return null;
                            
                            return (
                                <div key={position} className="position-section">
                                    <h2 className="position-title">
                                        {position}s <span className="count">({positionPlayers.length})</span>
                                    </h2>
                                    <div className="player-cards-grid">
                                        {visiblePlayers.map(player => (
                                            <div key={player.id} className="player-card">
                                                <div className="card-menu">
                                                    <button 
                                                        className="menu-trigger"
                                                        onClick={() => toggleMenu(player.id!)}
                                                    >
                                                        ⋮
                                                    </button>
                                                    {activeMenu === player.id && (
                                                        <div className="menu-dropdown">
                                                            <button onClick={() => {
                                                                setEditingPlayer(player);
                                                                setActiveMenu(null);
                                                            }}>
                                                                ✏️ Edit
                                                            </button>
                                                            <button onClick={() => {
                                                                setDeleteConfirm(player.id!);
                                                                setActiveMenu(null);
                                                            }}>
                                                                🗑️ Delete
                                                            </button>
                                                        </div>
                                                    )}
                                                </div>
                                                <div className="player-avatar">
                                                    {(() => {
                                                        const hasHeadshot = playersWithHeadshots.has(player.id!);
                                                        console.log(`[PlayerCard] ${player.name}:`, {
                                                            id: player.id,
                                                            hasHeadshot
                                                        });
                                                        
                                                        if (hasHeadshot) {
                                                            return (
                                                                <img 
                                                                    src={`/api/players/manage/${player.id}/headshot`}
                                                                    alt={player.name}
                                                                    style={{ 
                                                                        width: '100%', 
                                                                        height: '100%', 
                                                                        objectFit: 'cover',
                                                                        borderRadius: '50%'
                                                                    }}
                                                                    onError={(e) => {
                                                                        console.error(`[PlayerCard] Image failed to load for ${player.name}`);
                                                                        e.currentTarget.style.display = 'none';
                                                                    }}
                                                                    onLoad={() => {
                                                                        console.log(`[PlayerCard] Image loaded successfully for ${player.name}`);
                                                                    }}
                                                                />
                                                            );
                                                        } else {
                                                            return getPlayerInitials(player.name);
                                                        }
                                                    })()}
                                                </div>
                                                <div className="player-info">
                                                    <h3 className="player-name">{player.name}</h3>
                                                    <div className="player-meta">
                                                        <span className="position-badge">{player.position}</span>
                                                        {player.team && <span className="team">{player.team}</span>}
                                                    </div>
                                                    {player.college && (
                                                        <div className="player-college">{player.college}</div>
                                                    )}
                                                    {player.draftyear && (
                                                        <div className="player-year">Class of {player.draftyear}</div>
                                                    )}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                    {hasMore && (
                                        <div className="show-more-container">
                                            <button 
                                                className="show-more-btn"
                                                onClick={() => togglePositionExpanded(position)}
                                            >
                                                {isExpanded 
                                                    ? '↑ Show Less' 
                                                    : `↓ Show ${positionPlayers.length - INITIAL_CARDS_SHOWN} More`
                                                }
                                            </button>
                                        </div>
                                    )}
                                </div>
                            );
                        })}
                        {filteredPlayers.length === 0 && (
                            <div className="empty-state">
                                No players found for selected draft class{selectedYears.length > 1 ? 'es' : ''}
                            </div>
                        )}
                    </div>
                )}
            </div>

            <AddPlayerModal
                visible={showAddModal}
                onClose={() => setShowAddModal(false)}
                onSubmit={handleAddPlayer}
            />

            <EditPlayerModal
                visible={!!editingPlayer}
                player={editingPlayer}
                onClose={() => setEditingPlayer(null)}
                onSubmit={handleUpdatePlayer}
            />

            {deleteConfirm && (
                <div className="modal-overlay" onClick={() => setDeleteConfirm(null)}>
                    <div className="modal-content delete-modal" onClick={(e) => e.stopPropagation()}>
                        <h2>Delete Player</h2>
                        <p>Are you sure you want to delete this player?</p>
                        <div className="verification-section">
                            <input 
                                type="password"
                                placeholder="Verification Code *" 
                                value={verificationCode} 
                                onChange={(e) => setVerificationCode(e.target.value)} 
                            />
                            <small className="help-text">Required to delete players</small>
                        </div>
                        <button className="delete-btn" onClick={() => handleDeletePlayer(deleteConfirm)}>
                            Delete
                        </button>
                        <button className="cancel" onClick={() => {
                            setDeleteConfirm(null);
                            setVerificationCode('');
                        }}>Cancel</button>
                    </div>
                </div>
            )}

            {toast && <Toast message={toast.message} type={toast.type} />}
        </div>
    );
};

export default PlayerManagementPage;

import React, { useState, useEffect } from 'react';
import { Player } from '../draft/BigBoard';
import './mobileDraft.scss';

interface MobileDraftProps {
    teams: number;
    rounds: number;
    players: (Player | null)[][];
    playerPool: Player[];
    onDraftPlayer: (player: Player, row: number, col: number) => void;
    onRemovePlayer: (row: number, col: number) => void;
    onExport: () => void;
    onExit: () => void;
}

const MobileDraft: React.FC<MobileDraftProps> = ({
    teams,
    rounds,
    players,
    playerPool,
    onDraftPlayer,
    onRemovePlayer,
    onExport,
    onExit
}) => {
    const [currentRound, setCurrentRound] = useState(1);
    const [currentPick, setCurrentPick] = useState(1);
    const [showPlayerSheet, setShowPlayerSheet] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [positionFilter, setPositionFilter] = useState<string>('ALL');
    const [playersWithHeadshots, setPlayersWithHeadshots] = useState<Set<number>>(new Set());

    useEffect(() => {
        fetch('/api/players/manage/headshots/available')
            .then(res => res.json())
            .then((playerIds: number[]) => {
                setPlayersWithHeadshots(new Set(playerIds));
            })
            .catch(err => console.error('Failed to fetch headshot info:', err));
    }, []);

    const getCurrentPickPlayer = () => {
        return players[currentRound - 1]?.[currentPick - 1];
    };

    const getPickNumber = (round: number, pick: number) => {
        return (round - 1) * teams + pick;
    };

    const handleDraftPlayer = (player: Player) => {
        onDraftPlayer(player, currentRound, currentPick);
        setShowPlayerSheet(false);
        advanceToNextPick();
    };

    const advanceToNextPick = () => {
        if (currentPick < teams) {
            setCurrentPick(currentPick + 1);
        } else if (currentRound < rounds) {
            setCurrentRound(currentRound + 1);
            setCurrentPick(1);
        }
    };

    const goToPreviousPick = () => {
        if (currentPick > 1) {
            setCurrentPick(currentPick - 1);
        } else if (currentRound > 1) {
            setCurrentRound(currentRound - 1);
            setCurrentPick(teams);
        }
    };

    const goToNextPick = () => {
        if (currentPick < teams) {
            setCurrentPick(currentPick + 1);
        } else if (currentRound < rounds) {
            setCurrentRound(currentRound + 1);
            setCurrentPick(1);
        }
    };

    const filteredPlayers = playerPool.filter(player => {
        const matchesSearch = player.name.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesPosition = positionFilter === 'ALL' || player.position === positionFilter;
        return matchesSearch && matchesPosition;
    });

    const currentPlayer = getCurrentPickPlayer();
    const totalPicks = teams * rounds;
    const currentPickNumber = getPickNumber(currentRound, currentPick);
    const progress = (currentPickNumber / totalPicks) * 100;

    return (
        <div className="mobile-draft">
            {/* Header */}
            <div className="mobile-draft-header">
                <button className="header-btn" onClick={onExit}>
                    ← Exit
                </button>
                <div className="header-title">
                    <div className="round-info">Round {currentRound}</div>
                    <div className="pick-info">Pick {currentPick} of {teams}</div>
                </div>
                <button className="header-btn" onClick={onExport}>
                    Export
                </button>
            </div>

            {/* Progress Bar */}
            <div className="draft-progress">
                <div className="progress-bar">
                    <div className="progress-fill" style={{ width: `${progress}%` }}></div>
                </div>
                <div className="progress-text">
                    Pick {currentPickNumber} of {totalPicks}
                </div>
            </div>

            {/* Current Pick Card */}
            <div className="current-pick-section">
                <div className="pick-label">Current Pick: {currentRound}.{currentPick}</div>
                
                {currentPlayer ? (
                    <div className="drafted-player-card">
                        <div className="player-avatar-large">
                            {playersWithHeadshots.has(currentPlayer.id!) ? (
                                <img 
                                    src={`/api/players/manage/${currentPlayer.id}/headshot`}
                                    alt={currentPlayer.name}
                                    className="avatar-image"
                                />
                            ) : (
                                <span className="avatar-icon">🏈</span>
                            )}
                        </div>
                        <div className="player-details">
                            <div className="player-name">{currentPlayer.name}</div>
                            <div className="player-meta">
                                <span className={`position-badge ${currentPlayer.position}`}>
                                    {currentPlayer.position}
                                </span>
                                <span className="team-name">{currentPlayer.team}</span>
                            </div>
                            {currentPlayer.college && (
                                <div className="player-college">{currentPlayer.college}</div>
                            )}
                        </div>
                        <button 
                            className="undo-btn"
                            onClick={() => onRemovePlayer(currentRound, currentPick)}
                        >
                            ✕
                        </button>
                    </div>
                ) : (
                    <div className="empty-pick-card">
                        <div className="empty-icon">👤</div>
                        <div className="empty-text">No player selected</div>
                    </div>
                )}

                {/* Navigation */}
                <div className="pick-navigation">
                    <button 
                        className="nav-btn"
                        onClick={goToPreviousPick}
                        disabled={currentRound === 1 && currentPick === 1}
                    >
                        ← Previous
                    </button>
                    <button 
                        className="nav-btn"
                        onClick={goToNextPick}
                        disabled={currentRound === rounds && currentPick === teams}
                    >
                        Next →
                    </button>
                </div>
            </div>

            {/* Draft Button */}
            {!currentPlayer && (
                <button 
                    className="draft-player-btn"
                    onClick={() => setShowPlayerSheet(true)}
                >
                    Select Player
                </button>
            )}

            {/* Player Selection Sheet */}
            {showPlayerSheet && (
                <div className="player-sheet-overlay" onClick={() => setShowPlayerSheet(false)}>
                    <div className="player-sheet" onClick={(e) => e.stopPropagation()}>
                        <div className="sheet-header">
                            <h3>Select Player</h3>
                            <button className="close-btn" onClick={() => setShowPlayerSheet(false)}>
                                ✕
                            </button>
                        </div>

                        {/* Search */}
                        <div className="sheet-search">
                            <input 
                                type="text"
                                placeholder="Search players..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                autoFocus
                            />
                        </div>

                        {/* Position Filter */}
                        <div className="sheet-filters">
                            {['ALL', 'QB', 'RB', 'WR', 'TE'].map(pos => (
                                <button
                                    key={pos}
                                    className={`filter-btn ${positionFilter === pos ? 'active' : ''}`}
                                    onClick={() => setPositionFilter(pos)}
                                >
                                    {pos}
                                </button>
                            ))}
                        </div>

                        {/* Player List */}
                        <div className="sheet-player-list">
                            {filteredPlayers.length > 0 ? (
                                filteredPlayers.map(player => (
                                    <div 
                                        key={player.id}
                                        className="sheet-player-item"
                                        onClick={() => handleDraftPlayer(player)}
                                    >
                                        <div className="player-avatar-small">
                                            {playersWithHeadshots.has(player.id!) ? (
                                                <img 
                                                    src={`/api/players/manage/${player.id}/headshot`}
                                                    alt={player.name}
                                                    className="avatar-image"
                                                />
                                            ) : (
                                                <span className="avatar-icon-small">🏈</span>
                                            )}
                                        </div>
                                        <div className="player-info">
                                            <div className="player-name">{player.name}</div>
                                            <div className="player-meta">
                                                <span className={`position-badge ${player.position}`}>
                                                    {player.position}
                                                </span>
                                                {player.team && (
                                                    <span className="team-name">{player.team}</span>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <div className="empty-list">No players found</div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MobileDraft;

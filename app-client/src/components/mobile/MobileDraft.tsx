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
    rookiesOnly?: boolean;
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
    // rookiesOnly is handled by parent component filtering
}) => {
    const [currentRound, setCurrentRound] = useState(1);
    const [currentPick, setCurrentPick] = useState(1);
    const [showPlayerSheet, setShowPlayerSheet] = useState(false);
    const [positionFilter, setPositionFilter] = useState<string>('ALL');
    const [playersWithHeadshots, setPlayersWithHeadshots] = useState<Set<number>>(new Set());
    const [touchStart, setTouchStart] = useState<number | null>(null);
    const [touchEnd, setTouchEnd] = useState<number | null>(null);

    // Minimum swipe distance (in px)
    const minSwipeDistance = 50;

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
        // Auto-advance to next pick
        setTimeout(() => {
            goToNextPick();
        }, 100); // Small delay to show the selection
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

    const onTouchStart = (e: React.TouchEvent) => {
        setTouchEnd(null);
        setTouchStart(e.targetTouches[0].clientX);
    };

    const onTouchMove = (e: React.TouchEvent) => {
        setTouchEnd(e.targetTouches[0].clientX);
    };

    const onTouchEnd = () => {
        if (!touchStart || !touchEnd) return;
        
        const distance = touchStart - touchEnd;
        const isLeftSwipe = distance > minSwipeDistance;
        const isRightSwipe = distance < -minSwipeDistance;
        
        if (isLeftSwipe) {
            // Swipe left = go to next pick
            goToNextPick();
        }
        if (isRightSwipe) {
            // Swipe right = go to previous pick
            goToPreviousPick();
        }
    };

    const filteredPlayers = playerPool.filter(player => {
        const matchesPosition = positionFilter === 'ALL' || player.position === positionFilter;
        return matchesPosition;
    });

    const currentPlayer = getCurrentPickPlayer();
    const totalPicks = teams * rounds;
    const currentPickNumber = getPickNumber(currentRound, currentPick);
    const progress = (currentPickNumber / totalPicks) * 100;
    
    // Check if draft is complete
    const isDraftComplete = players.every(round => round.every(pick => pick !== null));

    return (
        <div className="mobile-draft">
            {/* Header */}
            <div className="mobile-draft-header">
                <button className="leave-btn" onClick={onExit} title="Leave draft">
                    ✕
                </button>
                <div className="header-title">
                    <div className="round-info">Round {currentRound}</div>
                    <div className="pick-info">Pick {currentPick} of {teams}</div>
                </div>
                {isDraftComplete && (
                    <button className="export-btn" onClick={onExport}>
                        Export
                    </button>
                )}
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

            {/* Draft Carousel - All Picks */}
            <div className="draft-carousel-container">
                <div className="carousel-label">
                    <span className="swipe-hint">← Swipe to navigate →</span>
                </div>
                <div 
                    className="draft-carousel"
                    onTouchStart={onTouchStart}
                    onTouchMove={onTouchMove}
                    onTouchEnd={onTouchEnd}
                >
                    <div className="carousel-track">
                        {Array.from({ length: totalPicks }).map((_, index) => {
                            const pickNum = index + 1;
                            const round = Math.floor(index / teams) + 1;
                            const pick = (index % teams) + 1;
                            const player = players[round - 1]?.[pick - 1];
                            const isCurrentPick = pickNum === currentPickNumber;
                            
                            return (
                                <div 
                                    key={pickNum} 
                                    className={`carousel-card ${isCurrentPick ? 'current' : ''} ${player ? 'filled' : 'empty'}`}
                                    onClick={() => {
                                        setCurrentRound(round);
                                        setCurrentPick(pick);
                                    }}
                                >
                                    <div className="carousel-pick-label">{round}.{pick}</div>
                                    {player ? (
                                        <>
                                            <div className="carousel-avatar">
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
                                            <div className="carousel-player-name">{player.name}</div>
                                            <span className={`carousel-position-badge ${player.position}`}>
                                                {player.position}
                                            </span>
                                        </>
                                    ) : (
                                        <div className="carousel-empty">
                                            <div className="empty-avatar">?</div>
                                        </div>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>

            {/* Current Pick Info */}
            <div className="current-pick-info">
                <div className="pick-label">Pick: {currentRound}.{currentPick}</div>
            </div>

            {/* Select Player Section - Always Visible */}
            <div className="select-player-section">
                
                {currentPlayer ? (
                    <div className="selected-player-display">
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
                        <button 
                            className="select-player-btn"
                            onClick={() => setShowPlayerSheet(true)}
                        >
                            Select Player
                        </button>
                    </div>
                )}
            </div>

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

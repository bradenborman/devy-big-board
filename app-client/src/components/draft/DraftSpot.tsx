import React, { useState, useEffect } from 'react';
import { Player } from './BigBoard';

interface DraftSpotProps {
    player: Player | null;
    row: number;
    col: number;
    removeDraftedPlayer: (row: number, col: number) => void;
    isTierBreak?: boolean;
    onRightClick?: (row: number, col: number) => void;
}

const DraftSpot: React.FC<DraftSpotProps> = ({
    player,
    row,
    col,
    removeDraftedPlayer,
    isTierBreak = false,
    onRightClick
}) => {
    const [playersWithHeadshots, setPlayersWithHeadshots] = useState<Set<number>>(new Set());
    const [imageLoaded, setImageLoaded] = useState(false);

    useEffect(() => {
        // Fetch list of players with headshots once
        fetch('/api/players/manage/headshots/available')
            .then(res => res.json())
            .then((playerIds: number[]) => {
                setPlayersWithHeadshots(new Set(playerIds));
            })
            .catch(err => console.error('Failed to fetch headshot info:', err));
    }, []);

    const getPositionIcon = (position: string) => {
        switch (position) {
            case 'QB': return '🏈';
            case 'RB': return '🏈';
            case 'WR': return '🏈';
            case 'TE': return '🏈';
            default: return '👤';
        }
    };

    const hasHeadshot = player?.id && playersWithHeadshots.has(player.id);

    const splitName = (fullName: string) => {
        const firstSpaceIndex = fullName.indexOf(' ');
        if (firstSpaceIndex === -1) {
            // No space found, return as single name
            return { firstName: fullName, lastName: '' };
        }
        const firstName = fullName.substring(0, firstSpaceIndex);
        const lastName = fullName.substring(firstSpaceIndex + 1);
        return { firstName, lastName };
    };

    return (
        <div
            className={`draft-spot${isTierBreak ? ' tier-break' : ''}`}
            onDoubleClick={() => player && removeDraftedPlayer(row, col)}
            onContextMenu={(e) => {
                e.preventDefault();
                onRightClick?.(row, col);
            }}
        >
            {player ? (
                <>
                    <div className="slot">{`${row}.${col}`}</div>
                    <div className="player-avatar">
                        {hasHeadshot ? (
                            <>
                                <img 
                                    src={`/api/players/manage/${player.id}/headshot`}
                                    alt={player.name}
                                    className="avatar-image"
                                    style={{ display: imageLoaded ? 'block' : 'none' }}
                                    onLoad={() => setImageLoaded(true)}
                                    onError={() => setImageLoaded(false)}
                                />
                                {!imageLoaded && (
                                    <span className="avatar-icon">{getPositionIcon(player.position)}</span>
                                )}
                            </>
                        ) : (
                            <span className="avatar-icon">{getPositionIcon(player.position)}</span>
                        )}
                    </div>
                    <div className="player-info">
                        <div className="player-name">
                            {(() => {
                                const { firstName, lastName } = splitName(player.name);
                                return (
                                    <>
                                        <span className="first-name">{firstName}</span>
                                        {lastName && (
                                            <>
                                                <br />
                                                <span className="last-name">{lastName}</span>
                                            </>
                                        )}
                                    </>
                                );
                            })()}
                        </div>
                        <div className="player-meta">
                            <span className={`position-badge ${player.position}`}>{player.position}</span>
                            <span className="team-name">{player.team}</span>
                        </div>
                    </div>
                </>
            ) : (
                <div className="empty-slot">
                    <span className="slot-number">{`${row}.${col}`}</span>
                </div>
            )}
        </div>
    );
};

export default DraftSpot;
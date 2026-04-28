import React, { useState, useEffect } from 'react';
import { Player } from './BigBoard';

interface DraftSpotProps {
    player: Player | null;
    row: number;
    col: number;
    removeDraftedPlayer: (row: number, col: number) => void;
    isTierBreak?: boolean;
    onRightClick?: (row: number, col: number) => void;
    showStats: boolean;
    onPlayerDrafted?: (player: Player) => void;
}

const DraftSpot: React.FC<DraftSpotProps> = ({
    player, row, col, removeDraftedPlayer,
    isTierBreak = false, onRightClick, showStats, onPlayerDrafted
}) => {
    const [playersWithHeadshots, setPlayersWithHeadshots] = useState<Set<number>>(new Set());
    const [imageLoaded, setImageLoaded] = useState(false);
    const [prevPlayerId, setPrevPlayerId] = useState<number | undefined>(undefined);

    useEffect(() => {
        fetch('/api/players/manage/headshots/available')
            .then(res => res.json())
            .then((ids: number[]) => setPlayersWithHeadshots(new Set(ids)))
            .catch(() => {});
    }, []);

    // Fire toast when a NEW player lands in this slot
    useEffect(() => {
        if (player?.id && player.id !== prevPlayerId && showStats && onPlayerDrafted) {
            onPlayerDrafted(player);
        }
        setPrevPlayerId(player?.id);
    }, [player?.id]);

    const getPositionIcon = (position: string) => {
        switch (position) {
            case 'QB': return '🏈';
            case 'RB': return '🏃';
            case 'WR': return '🙌';
            case 'TE': return '💪';
            default:   return '👤';
        }
    };

    const hasHeadshot = player?.id && playersWithHeadshots.has(player.id);

    const splitName = (fullName: string) => {
        const i = fullName.indexOf(' ');
        if (i === -1) return { firstName: fullName, lastName: '' };
        return { firstName: fullName.substring(0, i), lastName: fullName.substring(i + 1) };
    };

    return (
        <div
            className={`draft-spot${isTierBreak ? ' tier-break' : ''}`}
            onDoubleClick={() => player && removeDraftedPlayer(row, col)}
            onContextMenu={(e) => { e.preventDefault(); onRightClick?.(row, col); }}
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
                                        {lastName && <><br /><span className="last-name">{lastName}</span></>}
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

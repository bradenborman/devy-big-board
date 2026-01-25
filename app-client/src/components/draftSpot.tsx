import React from 'react';
import { Player } from './bigBoard';

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
    const getPositionIcon = (position: string) => {
        switch (position) {
            case 'QB': return '🏈';
            case 'RB': return '🏈';
            case 'WR': return '🏈';
            case 'TE': return '🏈';
            default: return '👤';
        }
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
                        <span className="avatar-icon">{getPositionIcon(player.position)}</span>
                    </div>
                    <div className="player-info">
                        <div className="player-name">{player.name}</div>
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
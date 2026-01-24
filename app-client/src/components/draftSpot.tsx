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
                    <p className="slot">{`${row}.${col}`}</p>
                    <p className="name">{player.name}</p>
                    <p className={`position ${player.position}`}>{player.position}</p>
                    <p className="team">{player.team}</p>
                </>
            ) : (
                <div className="slot empty">
                    <p>{`${row}.${col}`}</p>
                </div>
            )}
        </div>
    );
};

export default DraftSpot;
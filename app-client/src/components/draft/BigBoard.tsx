import React from 'react';
import DraftSpot from './DraftSpot';


export interface Player {
    id?: number;
    name: string;
    position: string;
    team: string;
    college?: string;
    draftyear: number;
    adp: number;
    imageUrl?: string;
}

interface BigBoardProps {
    teams: number;
    rounds: number;
    players: (Player | null)[][];
    removeDraftedPlayer: (row: number, col: number) => void;
    tierBreaks: { row: number; col: number }[];
}

const BigBoard: React.FC<BigBoardProps> = ({
    teams,
    rounds,
    players,
    removeDraftedPlayer,
    tierBreaks
}) => {

    const isTierBreak = (row: number, col: number) =>
        tierBreaks.some((tb) => tb.row === row + 1 && tb.col === col + 1);

    return (
        <div className="big-board-wrapper">
            <div
                className="big-board"
                style={{ '--teams': teams, '--rounds': rounds } as React.CSSProperties}
            >
                {Array.from({ length: rounds }).map((_, rowIndex) => (
                    <div key={rowIndex} className="grid-row">
                        {Array.from({ length: teams }).map((_, colIndex) => (
                            <DraftSpot
                                key={colIndex}
                                player={players[rowIndex][colIndex]}
                                row={rowIndex + 1}
                                col={colIndex + 1}
                                removeDraftedPlayer={removeDraftedPlayer}
                                isTierBreak={isTierBreak(rowIndex, colIndex)}
                            />
                        ))}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default BigBoard;

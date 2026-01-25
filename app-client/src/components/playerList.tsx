import React, { useState } from 'react';
import { Player } from './bigBoard';

interface PlayerListProps {
    playerPool: Player[];
    addPlayerToNextOpenSpot: (player: Player) => void;
    playerListOpen: boolean;
    activePositionFilters: string[];
    activeYearFilters: number[];
}


const PlayerList: React.FC<PlayerListProps> = ({ 
    playerPool, 
    addPlayerToNextOpenSpot, 
    playerListOpen,
    activePositionFilters,
    activeYearFilters
}) => {
    const filteredPlayers = playerPool.filter((player) => {
        const matchesPosition =
            activePositionFilters.length === 0 || activePositionFilters.includes(player.position);
        const matchesYear =
            activeYearFilters.length === 0 || activeYearFilters.includes(player.draftyear);
        return matchesPosition && matchesYear;
    });


    if (!playerListOpen) {
        return null;
    }

    return (
        <div className="player-list">
            <ul>
                {filteredPlayers.length === 0 ? (
                    <li className="empty-list">No players match filters</li>
                ) : (
                    filteredPlayers.map((player, index) => (
                        <li key={index} className="player-entry" onClick={() => addPlayerToNextOpenSpot(player)}>
                            <span className="player-position">{player.position}</span>
                            <span className="player-name"> {player.name}<span className="player-adp">{player.adp.toFixed(1)}</span>
                            </span>
                        </li>
                    ))
                )}
            </ul>
        </div>
    );
};

export default PlayerList;
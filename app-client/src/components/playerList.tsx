import React, { useState } from 'react';
import { Player } from './bigBoard';

interface PlayerListProps {
    playerPool: Player[];
    addPlayerToNextOpenSpot: (player: Player) => void;
    playerListOpen: boolean;
}


const PlayerList: React.FC<PlayerListProps> = ({ playerPool, addPlayerToNextOpenSpot, playerListOpen }) => {
    const [activePositionFilters, setActivePositionFilters] = useState<string[]>([]);
    const [activeYearFilters, setActiveYearFilters] = useState<number[]>([]);
    const currentYear = new Date().getFullYear();
    const yearRange = Array.from({ length: 4 }, (_, i) => currentYear + i);


    const togglePositionFilter = (position: string) => {
        if (position === 'ALL') {
            setActivePositionFilters([]);
        } else {
            setActivePositionFilters((prev) =>
                prev.includes(position) ? prev.filter((p) => p !== position) : [...prev, position]
            );
        }
    };

    const toggleYearFilter = (year: number) => {
        setActiveYearFilters((prev) =>
            prev.includes(year) ? prev.filter((y) => y !== year) : [...prev, year]
        );
    };

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
            <div className="filters">
                <span className="filter-label">Position</span>
                <div className="filter-buttons">
                    <button
                        className={`filter-btn ${activePositionFilters.length === 0 ? 'active' : ''}`}
                        onClick={() => togglePositionFilter('ALL')}
                    >
                        All
                    </button>
                    {['QB', 'RB', 'WR', 'TE'].map((position) => (
                        <button
                            key={position}
                            className={`filter-btn ${activePositionFilters.includes(position) ? 'active' : ''}`}
                            onClick={() => togglePositionFilter(position)}
                        >
                            {position}
                        </button>
                    ))}
                </div>
            </div>
            <div className="filters">
                <span className="filter-label">Draft Class</span>
                <div className="filter-buttons">
                    {yearRange.map((year) => (
                        <button
                            key={year}
                            className={`filter-btn ${activeYearFilters.includes(year) ? 'active' : ''}`}
                            onClick={() => toggleYearFilter(year)}
                        >
                            {year}
                        </button>
                    ))}
                </div>
            </div>
            <div className='scroll-list'>
                <ul>
                    {filteredPlayers.length === 0 ? (
                        <li className="empty-list">No players left</li>
                    ) : (
                        filteredPlayers.map((player, index) => (
                            <li key={index} className="player-entry">
                                <span className="player-position">{player.position}</span>
                                <span className="player-name"> {player.name}<span className="player-adp">{player.adp.toFixed(1)}</span>
                                </span>
                                <button className="add-btn" onClick={() => addPlayerToNextOpenSpot(player)}>
                                    +
                                </button>
                            </li>
                        ))
                    )}
                </ul>
            </div>
        </div>
    );
};

export default PlayerList;
import React, { useState } from 'react';
import { PlayerResponse } from '../../models/WebSocketMessages';
import './live-player-pool.scss';

interface LivePlayerPoolProps {
  availablePlayers: PlayerResponse[];
  isMyTurn: boolean;
  onMakePick: (playerId: number) => void;
  onForcePick: (playerId: number) => void;
}

const LivePlayerPool: React.FC<LivePlayerPoolProps> = ({
  availablePlayers,
  isMyTurn,
  onMakePick,
  onForcePick,
}) => {
  const [positionFilter, setPositionFilter] = useState<string>('');
  const [yearFilter, setYearFilter] = useState<string>('');

  // Get unique positions for filter
  const positions = Array.from(new Set(availablePlayers.map((p) => p.position))).sort();

  // Get unique years for filter
  const years = Array.from(
    new Set(availablePlayers.map((p) => p.draftyear).filter((year): year is number => year !== undefined && year !== null))
  ).sort();

  // Filter players based on position and year
  const filteredPlayers = availablePlayers.filter((player) => {
    const matchesPosition = !positionFilter || player.position === positionFilter;
    
    const matchesYear = !yearFilter || (player.draftyear && player.draftyear.toString() === yearFilter);

    return matchesPosition && matchesYear;
  });

  const handleDragStart = (e: React.DragEvent, player: PlayerResponse) => {
    if (!isMyTurn) {
      e.preventDefault();
      return;
    }
    e.dataTransfer.setData('playerId', player.id.toString());
    e.dataTransfer.effectAllowed = 'move';
  };

  return (
    <div className="live-player-pool">
      <div className="pool-filters">
        <div className="filter-row">
          <select
            value={positionFilter}
            onChange={(e) => setPositionFilter(e.target.value)}
            className="position-filter"
          >
            <option value="">All Positions</option>
            {positions.map((pos) => (
              <option key={pos} value={pos}>
                {pos}
              </option>
            ))}
          </select>
          <select
            value={yearFilter}
            onChange={(e) => setYearFilter(e.target.value)}
            className="year-filter"
          >
            <option value="">All Years</option>
            {years.map((year) => (
              <option key={year} value={year}>
                {year}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="player-list">
        {filteredPlayers.length === 0 ? (
          <div className="empty-state">
            <p>No players found</p>
          </div>
        ) : (
          filteredPlayers.map((player) => (
            <div
              key={player.id}
              className={`player-card ${isMyTurn ? 'draggable' : 'not-draggable'}`}
              draggable={isMyTurn}
              onDragStart={(e) => handleDragStart(e, player)}
            >
              <div className="player-info">
                <div className="player-header">
                  <span className="player-position">{player.position}</span>
                  <span className="player-name">{player.name}</span>
                </div>
                <div className="player-details">
                  <span className="player-team">{player.team}</span>
                  <span className="separator">•</span>
                  <span className="player-college">{player.college}</span>
                  {player.adp && (
                    <>
                      <span className="separator">•</span>
                      <span className="player-adp">ADP: {player.adp.toFixed(1)}</span>
                    </>
                  )}
                </div>
              </div>
              <div className="player-actions">
                {isMyTurn ? (
                  <button
                    className="btn-pick"
                    onClick={() => onMakePick(player.id)}
                  >
                    Pick
                  </button>
                ) : (
                  <button
                    className="btn-force-pick"
                    onClick={() => onForcePick(player.id)}
                  >
                    Force Pick
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default LivePlayerPool;

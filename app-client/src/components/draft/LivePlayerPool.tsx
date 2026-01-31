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
  const [searchTerm, setSearchTerm] = useState('');
  const [positionFilter, setPositionFilter] = useState<string>('');

  // Get unique positions for filter
  const positions = Array.from(new Set(availablePlayers.map((p) => p.position))).sort();

  // Filter players based on search and position
  const filteredPlayers = availablePlayers.filter((player) => {
    const matchesSearch =
      !searchTerm ||
      player.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      player.team.toLowerCase().includes(searchTerm.toLowerCase()) ||
      player.college.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesPosition = !positionFilter || player.position === positionFilter;

    return matchesSearch && matchesPosition;
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
      <div className="pool-header">
        <h3>Available Players ({filteredPlayers.length})</h3>
        {!isMyTurn && (
          <div className="not-your-turn-badge">Not your turn</div>
        )}
      </div>

      <div className="pool-filters">
        <input
          type="text"
          placeholder="Search players..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
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

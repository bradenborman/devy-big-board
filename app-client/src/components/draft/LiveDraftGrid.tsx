import React from 'react';
import { PickMessage, ParticipantInfo } from '../../models/WebSocketMessages';
import './live-draft-grid.scss';

interface LiveDraftGridProps {
  participants: ParticipantInfo[];
  participantCount: number;
  picks: PickMessage[];
  totalRounds: number;
  isSnakeDraft: boolean;
  currentRound: number;
  currentTurnPosition: string;
  userPosition: string;
  onDropPlayer: (position: string, round: number) => void;
  onUndoLastPick: () => void;
}

const LiveDraftGrid: React.FC<LiveDraftGridProps> = ({
  participants,
  participantCount,
  picks,
  totalRounds,
  isSnakeDraft,
  currentRound,
  currentTurnPosition,
  userPosition,
  onDropPlayer,
  onUndoLastPick,
}) => {
  // Generate all positions based on participantCount
  const allPositions = Array.from({ length: participantCount }, (_, i) => {
    const position = String.fromCharCode(65 + i); // A, B, C, D, etc.
    const participant = participants.find(p => p.position === position);
    return {
      position,
      nickname: participant?.nickname || position, // Show position letter if no one joined
      isJoined: !!participant,
    };
  });

  // Calculate which position picks in each round/pick slot
  const getPositionForPick = (round: number, pickInRound: number): string => {
    if (isSnakeDraft) {
      // Snake draft: odd rounds forward, even rounds reverse
      // Round 1: A, B, C, D (forward)
      // Round 2: D, C, B, A (reverse)
      // Round 3: A, B, C, D (forward)
      const isReverse = round % 2 === 0;
      const index = isReverse ? participantCount - pickInRound : pickInRound - 1;
      return allPositions[index]?.position || '';
    } else {
      // Linear draft: all rounds go A, B, C, D (forward)
      const index = pickInRound - 1;
      return allPositions[index]?.position || '';
    }
  };

  // Get pick for a specific round and position
  const getPickForSlot = (round: number, position: string): PickMessage | null => {
    return (
      picks.find((pick) => pick.roundNumber === round && pick.pickedByPosition === position) ||
      null
    );
  };

  // Check if this is the current pick slot
  const isCurrentPickSlot = (round: number, pickInRound: number): boolean => {
    if (round !== currentRound) return false;
    const position = getPositionForPick(round, pickInRound);
    return position === currentTurnPosition;
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  };

  const handleDrop = (e: React.DragEvent, position: string, round: number) => {
    e.preventDefault();
    const playerId = e.dataTransfer.getData('playerId');
    if (playerId) {
      onDropPlayer(position, round);
    }
  };

  return (
    <div className="live-draft-grid">
      <div className="grid-header">
        <div className="round-header-spacer"></div>
        {allPositions.map((positionInfo) => (
          <div
            key={positionInfo.position}
            className={`header-cell ${positionInfo.position === userPosition ? 'my-team' : ''} ${!positionInfo.isJoined ? 'empty-slot' : ''}`}
          >
            <div className="position-label">Position {positionInfo.position}</div>
            <div className="nickname-label">
              {positionInfo.position === userPosition ? (
                <span className="my-team-label">Me</span>
              ) : (
                positionInfo.nickname
              )}
            </div>
          </div>
        ))}
      </div>

      {picks.length > 0 && (
        <div className="undo-button-container">
          <button 
            className="undo-last-pick-btn"
            onClick={onUndoLastPick}
            title="Undo last pick"
          >
            ↶ Undo Last Pick
          </button>
        </div>
      )}

      <div className="grid-body">
        {Array.from({ length: totalRounds }, (_, roundIndex) => {
          const round = roundIndex + 1;
          return (
            <div key={round} className="grid-row">
              <div className="round-label">{round}</div>
              {Array.from({ length: participantCount }, (_, pickIndex) => {
                const pickInRound = pickIndex + 1;
                const position = getPositionForPick(round, pickInRound);
                const pick = getPickForSlot(round, position);
                const isCurrent = isCurrentPickSlot(round, pickInRound);
                const isMyColumn = position === userPosition;

                return (
                  <div
                    key={pickInRound}
                    className={`grid-cell ${isCurrent ? 'current-pick' : ''} ${
                      isMyColumn ? 'my-column' : ''
                    } ${pick ? 'filled' : 'empty'}`}
                    onDragOver={handleDragOver}
                    onDrop={(e) => handleDrop(e, position, round)}
                  >
                    {pick ? (
                      <div className="pick-content">
                        <img 
                          src={`/api/players/manage/${pick.playerId}/headshot`}
                          alt={pick.playerName}
                          className="player-image"
                          onError={(e) => {
                            e.currentTarget.style.display = 'none';
                          }}
                        />
                        <div className="pick-header">
                          <span className="pick-position">{pick.position}</span>
                          <span className="pick-name">{pick.playerName}</span>
                        </div>
                        <div className="pick-details">
                          <span>{pick.team}</span>
                          <span className="separator">•</span>
                          <span>{pick.college}</span>
                        </div>
                        {pick.forcedByPosition && (
                          <div className="forced-by">
                            Forced by {pick.forcedByPosition}
                          </div>
                        )}
                      </div>
                    ) : isCurrent ? (
                      <div className="empty-content current">
                        <span className="pick-indicator">📍</span>
                        <span>Current Pick</span>
                      </div>
                    ) : (
                      <div className="empty-content">
                        <span className="pick-number">
                          {(round - 1) * participantCount + pickInRound}
                        </span>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default LiveDraftGrid;

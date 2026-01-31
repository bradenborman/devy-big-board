import React from 'react';
import { PickMessage, ParticipantInfo } from '../../models/WebSocketMessages';
import './live-draft-grid.scss';

interface LiveDraftGridProps {
  participants: ParticipantInfo[];
  picks: PickMessage[];
  totalRounds: number;
  currentRound: number;
  currentTurnPosition: string;
  userPosition: string;
  onDropPlayer: (position: string, round: number) => void;
}

const LiveDraftGrid: React.FC<LiveDraftGridProps> = ({
  participants,
  picks,
  totalRounds,
  currentRound,
  currentTurnPosition,
  userPosition,
  onDropPlayer,
}) => {
  const participantCount = participants.length;

  // Sort participants by position (A, B, C, ...)
  const sortedParticipants = [...participants].sort((a, b) =>
    a.position.localeCompare(b.position)
  );

  // Calculate which position picks in each round/pick slot (snake draft)
  const getPositionForPick = (round: number, pickInRound: number): string => {
    // Round 1: A, B, C, D (forward)
    // Round 2: D, C, B, A (reverse)
    // Round 3: A, B, C, D (forward)
    const isReverse = round % 2 === 0;
    const index = isReverse ? participantCount - pickInRound : pickInRound - 1;
    return sortedParticipants[index]?.position || '';
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
        {sortedParticipants.map((participant) => (
          <div
            key={participant.position}
            className={`header-cell ${participant.position === userPosition ? 'my-team' : ''}`}
          >
            <div className="position-label">Position {participant.position}</div>
            <div className="nickname-label">
              {participant.position === userPosition ? (
                <span className="my-team-label">My Team ({participant.nickname})</span>
              ) : (
                participant.nickname
              )}
            </div>
          </div>
        ))}
      </div>

      <div className="grid-body">
        {Array.from({ length: totalRounds }, (_, roundIndex) => {
          const round = roundIndex + 1;
          return (
            <div key={round} className="grid-row">
              <div className="round-label">Round {round}</div>
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

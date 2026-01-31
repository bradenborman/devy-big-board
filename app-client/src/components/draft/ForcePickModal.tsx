import React, { useState } from 'react';
import { ParticipantInfo } from '../../models/WebSocketMessages';
import './force-pick-modal.scss';

interface ForcePickModalProps {
  isOpen: boolean;
  playerName: string;
  participants: ParticipantInfo[];
  participantCount: number;
  currentUserPosition: string;
  currentTurnPosition: string;
  onConfirm: (targetPosition: string) => void;
  onCancel: () => void;
}

const ForcePickModal: React.FC<ForcePickModalProps> = ({
  isOpen,
  playerName,
  participants,
  participantCount,
  currentUserPosition,
  currentTurnPosition,
  onConfirm,
  onCancel,
}) => {
  const [selectedPosition, setSelectedPosition] = useState('');

  // Auto-select the current turn position when modal opens
  React.useEffect(() => {
    if (isOpen && currentTurnPosition) {
      setSelectedPosition(currentTurnPosition);
    }
  }, [isOpen, currentTurnPosition]);

  if (!isOpen) return null;

  const handleConfirm = () => {
    if (selectedPosition) {
      onConfirm(selectedPosition);
      setSelectedPosition('');
    }
  };

  const handleCancel = () => {
    setSelectedPosition('');
    onCancel();
  };

  // Generate all positions based on participantCount
  const allPositions = Array.from({ length: participantCount }, (_, i) => {
    const position = String.fromCharCode(65 + i); // A, B, C, D, etc.
    const participant = participants.find(p => p.position === position);
    return {
      position,
      nickname: participant?.nickname || '(Empty)',
      isJoined: !!participant,
    };
  });

  return (
    <div className="force-pick-modal-overlay" onClick={handleCancel}>
      <div className="force-pick-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Force Pick</h2>
          <button className="close-btn" onClick={handleCancel}>
            ×
          </button>
        </div>

        <div className="modal-body">
          <p className="modal-description">
            You are forcing a pick of <strong>{playerName}</strong>
          </p>
          <p className="modal-warning">
            Select which team should receive this player:
          </p>

          <div className="position-list">
            {allPositions.map((positionInfo) => (
              <label
                key={positionInfo.position}
                className={`position-option ${
                  selectedPosition === positionInfo.position ? 'selected' : ''
                } ${positionInfo.position === currentTurnPosition ? 'on-clock' : ''}`}
              >
                <input
                  type="radio"
                  name="position"
                  value={positionInfo.position}
                  checked={selectedPosition === positionInfo.position}
                  onChange={(e) => setSelectedPosition(e.target.value)}
                />
                <div className="position-info">
                  <span className="position-label">Position {positionInfo.position}</span>
                  <span className="nickname">{positionInfo.nickname}</span>
                  {positionInfo.position === currentUserPosition && (
                    <span className="you-badge">(You)</span>
                  )}
                  {positionInfo.position === currentTurnPosition && (
                    <span className="clock-badge">⏰ On the Clock</span>
                  )}
                </div>
              </label>
            ))}
          </div>
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={handleCancel}>
            Cancel
          </button>
          <button
            className="btn btn-primary"
            onClick={handleConfirm}
            disabled={!selectedPosition}
          >
            Confirm Force Pick
          </button>
        </div>
      </div>
    </div>
  );
};

export default ForcePickModal;

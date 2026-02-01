import React, { useState, useEffect } from 'react';
import { ParticipantInfo } from '../../models/WebSocketMessages';
import './participant-list.scss';

interface ParticipantListProps {
  participants: ParticipantInfo[];
  participantCount: number;
  currentUserPosition?: string;
}

const ParticipantList: React.FC<ParticipantListProps> = ({
  participants,
  participantCount,
  currentUserPosition,
}) => {
  const [isExpanded, setIsExpanded] = useState(true);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);

  useEffect(() => {
    const handleResize = () => {
      const mobile = window.innerWidth <= 768;
      setIsMobile(mobile);
      // Collapse by default on mobile
      if (mobile && isExpanded) {
        setIsExpanded(false);
      }
    };

    window.addEventListener('resize', handleResize);
    // Set initial state
    handleResize();

    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Create array of all positions (A-Z based on participant count)
  const allPositions = Array.from({ length: participantCount }, (_, i) =>
    String.fromCharCode(65 + i)
  );

  // Create a map of position to participant for quick lookup
  const participantMap = new Map(
    participants.map((p) => [p.position, p])
  );

  const handleToggle = () => {
    if (isMobile) {
      setIsExpanded(!isExpanded);
    }
  };

  return (
    <div className="participant-list">
      <h3 onClick={handleToggle}>
        Participants ({participants.length}/{participantCount})
        {isMobile && (
          <span className={`toggle-icon ${isExpanded ? '' : 'collapsed'}`}>▼</span>
        )}
      </h3>
      <div className={`participants ${isExpanded ? 'expanded' : ''}`}>
        {allPositions.map((position) => {
          const participant = participantMap.get(position);
          const isCurrentUser = position === currentUserPosition;

          if (participant) {
            return (
              <div
                key={position}
                className={`participant ${isCurrentUser ? 'current-user' : ''} ${
                  participant.isReady ? 'ready' : 'not-ready'
                }`}
              >
                <div className="participant-info">
                  <span className="position">Position {position}:</span>
                  <span className="nickname">
                    {participant.nickname}
                    {isCurrentUser && <span className="you-badge">(You)</span>}
                  </span>
                </div>
                <div className="status">
                  {participant.isReady ? (
                    <span className="ready-indicator">
                      <span className="checkmark">✓</span>
                      Ready
                    </span>
                  ) : (
                    <span className="waiting-indicator">Waiting...</span>
                  )}
                </div>
              </div>
            );
          } else {
            return (
              <div key={position} className="participant empty">
                <div className="participant-info">
                  <span className="position">Position {position}:</span>
                  <span className="nickname empty-text">(Empty)</span>
                </div>
                <div className="status">
                  <span className="empty-indicator">—</span>
                </div>
              </div>
            );
          }
        })}
      </div>
    </div>
  );
};

export default ParticipantList;

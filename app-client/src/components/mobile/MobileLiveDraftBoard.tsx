import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { useWebSocket } from '../../contexts/WebSocketContext';
import { DraftStateMessage, PickMessage } from '../../models/WebSocketMessages';
import Toast from '../shared/Toast';
import './mobileLiveDraft.scss';

interface ToastNotification {
  id: number;
  message: string;
  type: 'success' | 'error';
}

const MobileLiveDraftBoard: React.FC = () => {
  const { uuid } = useParams<{ uuid: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { connect, subscribeToDraft, sendMessage, isConnected } = useWebSocket();

  const [draftState, setDraftState] = useState<DraftStateMessage | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reconnecting, setReconnecting] = useState(false);
  const [toasts, setToasts] = useState<ToastNotification[]>([]);
  const [showPlayerSheet, setShowPlayerSheet] = useState(false);
  const [positionFilter, setPositionFilter] = useState<string>('');
  const [yearFilter, setYearFilter] = useState<string>('');
  const [showFilters, setShowFilters] = useState(false);

  const previousPicksRef = useRef<PickMessage[]>([]);
  const toastIdCounter = useRef(0);
  const carouselRef = useRef<HTMLDivElement>(null);

  const userPosition = searchParams.get('x');

  // Validate position
  useEffect(() => {
    if (!userPosition || !/^[A-Z]$/.test(userPosition)) {
      setError('Invalid or missing position parameter. Please join the lobby first.');
      setLoading(false);
    }
  }, [userPosition]);

  // Validate position is within participant count
  useEffect(() => {
    if (!draftState || !userPosition) return;

    const positionIndex = userPosition.charCodeAt(0) - 65;
    const totalSlots = draftState.participantCount;

    if (positionIndex >= totalSlots) {
      setError(`Position ${userPosition} is invalid for a ${totalSlots}-team draft.`);
      return;
    }

    const isParticipant = draftState.participants.some((p) => p.position === userPosition);
    if (!isParticipant) {
      if (draftState.status === 'IN_PROGRESS' || draftState.status === 'COMPLETED') {
        setError(`This draft has already started. Position ${userPosition} was not assigned when the draft began.`);
      } else {
        setError(`You are not a participant in this draft. Please join the lobby first.`);
      }
    }
  }, [draftState, userPosition]);

  // Connect to WebSocket
  useEffect(() => {
    if (!uuid || !userPosition) return;

    const connectToDraft = async () => {
      try {
        setReconnecting(true);
        await connect(uuid);
        setReconnecting(false);
        setLoading(false);
      } catch (err) {
        console.error('Failed to connect to draft:', err);
        setError('Failed to connect to draft. Please try again.');
        setReconnecting(false);
        setLoading(false);
      }
    };

    if (!isConnected) {
      connectToDraft();
    } else {
      setLoading(false);
    }
  }, [uuid, userPosition, connect, isConnected]);

  // Handle reconnection
  useEffect(() => {
    if (!uuid || !userPosition) return;

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible' && !isConnected) {
        setReconnecting(true);
        connect(uuid)
          .then(() => {
            setReconnecting(false);
            sendMessage(`/app/draft/${uuid}/state`, { draftUuid: uuid });
            showToast('Reconnected successfully', 'success');
          })
          .catch((err) => {
            console.error('Reconnection failed:', err);
            setReconnecting(false);
            showToast('Reconnection failed. Please refresh the page.', 'error');
          });
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [uuid, userPosition, isConnected, connect, sendMessage]);

  const showToast = useCallback((message: string, type: 'success' | 'error' = 'success') => {
    const id = toastIdCounter.current++;
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 5000);
  }, []);

  // Subscribe to draft updates
  useEffect(() => {
    if (!uuid || !isConnected) return;

    subscribeToDraft(uuid, (message: DraftStateMessage) => {
      previousPicksRef.current = message.picks;
      setDraftState(message);
      setLoading(false);
      setError(null);
    });

    try {
      sendMessage(`/app/draft/${uuid}/state`, { draftUuid: uuid });
    } catch (err) {
      console.error('Error requesting draft state:', err);
      setError('Failed to load draft state. Please refresh the page.');
      setLoading(false);
    }
  }, [uuid, isConnected, subscribeToDraft, sendMessage]);

  const isMyTurn = useCallback((): boolean => {
    if (!draftState || !userPosition) return false;
    return draftState.currentTurnPosition === userPosition;
  }, [draftState, userPosition]);

  const handleMakePick = useCallback(
    async (playerId: number) => {
      if (!uuid || !userPosition || !isMyTurn()) return;

      try {
        sendMessage(`/app/draft/${uuid}/pick`, {
          draftUuid: uuid,
          playerId,
          position: userPosition,
        });
        setShowPlayerSheet(false);
      } catch (err) {
        console.error('Failed to make pick:', err);
        setError('Failed to make pick. Please try again.');
      }
    },
    [uuid, userPosition, isMyTurn, sendMessage]
  );

  const handleForcePick = useCallback(
    (playerId: number) => {
      if (!draftState || !uuid || !userPosition) return;

      const targetPosition = draftState.currentTurnPosition;

      try {
        sendMessage(`/app/draft/${uuid}/force-pick`, {
          draftUuid: uuid,
          playerId,
          targetPosition,
          forcingPosition: userPosition,
        });
        setShowPlayerSheet(false);
      } catch (err) {
        console.error('Failed to force pick:', err);
        setError('Failed to force pick. Please try again.');
      }
    },
    [draftState, uuid, userPosition, sendMessage]
  );

  // Get unique positions and years for filters
  const positions = draftState
    ? Array.from(new Set(draftState.availablePlayers.map((p) => p.position))).sort()
    : [];
  const years = draftState
    ? Array.from(
        new Set(
          draftState.availablePlayers
            .map((p) => p.draftyear)
            .filter((year): year is number => year !== undefined && year !== null)
        )
      ).sort()
    : [];

  // Filter players
  const filteredPlayers = draftState
    ? draftState.availablePlayers.filter((player) => {
        const matchesPosition = !positionFilter || player.position === positionFilter;
        const matchesYear = !yearFilter || (player.draftyear && player.draftyear.toString() === yearFilter);
        return matchesPosition && matchesYear;
      })
    : [];

  // Calculate current pick info
  const getCurrentPickInfo = () => {
    if (!draftState) return null;
    const pickNumber = (draftState.currentRound - 1) * draftState.participantCount + draftState.currentPick;
    const totalPicks = draftState.totalRounds * draftState.participantCount;
    return { pickNumber, totalPicks };
  };

  const pickInfo = getCurrentPickInfo();

  // Scroll carousel to current pick
  useEffect(() => {
    if (!carouselRef.current || !pickInfo) return;
    const cardWidth = 110 + 12;
    const scrollPosition = (pickInfo.pickNumber - 1) * cardWidth - window.innerWidth / 2 + cardWidth / 2;
    carouselRef.current.scrollTo({
      left: Math.max(0, scrollPosition),
      behavior: 'smooth',
    });
  }, [pickInfo]);

  if (loading) {
    return (
      <div className="mobile-live-draft">
        <div className="loading-container">
          <div className="spinner large"></div>
          <p>Loading draft...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mobile-live-draft">
        <div className="error-container">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={() => navigate('/')} className="btn btn-primary">
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  if (!draftState || !pickInfo) return null;

  const progress = (pickInfo.pickNumber / pickInfo.totalPicks) * 100;
  const currentTurnParticipant = draftState.participants.find(
    (p) => p.position === draftState.currentTurnPosition
  );

  return (
    <div className="mobile-live-draft">
      {/* Header */}
      <div className="mobile-draft-header">
        <button className="leave-btn" onClick={() => navigate(`/draft/${uuid}/lobby`)} title="Leave draft">
          ✕
        </button>
        <div className="header-title">
          <div className="round-info">Round {draftState.currentRound}</div>
          <div className="pick-info">
            Pick {draftState.currentPick} of {draftState.participantCount}
          </div>
        </div>
        <div className="header-spacer"></div>
      </div>

      {/* Progress Bar */}
      <div className="draft-progress">
        <div className="progress-bar">
          <div className="progress-fill" style={{ width: `${progress}%` }}></div>
        </div>
        <div className="progress-text">
          Pick {pickInfo.pickNumber} of {pickInfo.totalPicks}
        </div>
      </div>

      {/* Draft Carousel */}
      <div className="draft-carousel-container">
        <div className="carousel-label">
          <span className="swipe-hint">← Swipe to navigate →</span>
        </div>
        <div className="draft-carousel" ref={carouselRef}>
          <div className="carousel-track">
            {draftState.picks.map((pick) => {
              const pickNum = pick.pickNumber;
              const isCurrent = pickNum === pickInfo.pickNumber;
              
              // Split player name into first and last
              const nameParts = pick.playerName.split(' ');
              const firstName = nameParts[0];
              const lastName = nameParts.slice(1).join(' ');
              
              // Get the participant who made the pick
              const picker = draftState.participants.find(p => p.position === pick.pickedByPosition);

              return (
                <div key={pickNum} className={`carousel-card ${isCurrent ? 'current' : ''} filled`}>
                  <div className="carousel-pick-label">
                    {pick.roundNumber}.{pick.pickNumber - (pick.roundNumber - 1) * draftState.participantCount}
                  </div>
                  <div className="carousel-avatar">
                    <img
                      src={`/api/players/manage/${pick.playerId}/headshot`}
                      alt={pick.playerName}
                      className="avatar-image"
                      onError={(e) => {
                        e.currentTarget.style.display = 'none';
                        e.currentTarget.nextElementSibling?.classList.remove('hidden');
                      }}
                    />
                    <span className="avatar-icon-small hidden">🏈</span>
                  </div>
                  <div className="carousel-player-name">
                    <div className="first-name">{firstName}</div>
                    <div className="last-name">{lastName}</div>
                  </div>
                  <span className={`carousel-position-badge ${pick.position}`}>{pick.position}</span>
                  {picker && (
                    <div className="picker-badge">{picker.nickname}</div>
                  )}
                </div>
              );
            })}
            {/* Empty slots for remaining picks */}
            {Array.from({ length: pickInfo.totalPicks - draftState.picks.length }).map((_, index) => {
              const pickNum = draftState.picks.length + index + 1;
              const isCurrent = pickNum === pickInfo.pickNumber;

              return (
                <div key={pickNum} className={`carousel-card ${isCurrent ? 'current' : ''} empty`}>
                  <div className="carousel-pick-label">{pickNum}</div>
                  <div className="carousel-empty">
                    <div className="empty-avatar">?</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Current Turn Info */}
      <div className="current-turn-info">
        <div className="turn-label">
          {isMyTurn() ? (
            <span className="my-turn">🎯 Your Turn!</span>
          ) : (
            <span className="waiting">
              Waiting for {currentTurnParticipant?.nickname || draftState.currentTurnPosition}...
            </span>
          )}
        </div>
      </div>

      {/* Select Player Button */}
      <div className="select-player-section">
        <button className="select-player-btn" onClick={() => setShowPlayerSheet(true)}>
          {isMyTurn() ? 'Select Player' : 'View Players'}
        </button>
      </div>

      {/* Player Selection Sheet */}
      {showPlayerSheet && (
        <div className="player-sheet-overlay" onClick={() => setShowPlayerSheet(false)}>
          <div className="player-sheet" onClick={(e) => e.stopPropagation()}>
            <div className="sheet-header">
              <h3>Select Player</h3>
              <button className="close-btn" onClick={() => setShowPlayerSheet(false)}>
                ✕
              </button>
            </div>

            {/* Filter Toggle */}
            <div className="filter-toggle-container">
              <button className="filter-toggle-btn" onClick={() => setShowFilters(!showFilters)}>
                <span>Filters</span>
                <span className={`chevron ${showFilters ? 'open' : ''}`}>▼</span>
              </button>
            </div>

            {/* Filters */}
            <div className={`filters-container ${showFilters ? 'open' : ''}`}>
              <div className="sheet-filters">
                <div className="filter-label">Position:</div>
                <button
                  className={`filter-btn ${!positionFilter ? 'active' : ''}`}
                  onClick={() => setPositionFilter('')}
                >
                  ALL
                </button>
                {positions.map((pos) => (
                  <button
                    key={pos}
                    className={`filter-btn ${positionFilter === pos ? 'active' : ''}`}
                    onClick={() => setPositionFilter(pos)}
                  >
                    {pos}
                  </button>
                ))}
              </div>

              <div className="sheet-filters">
                <div className="filter-label">Year:</div>
                <button
                  className={`filter-btn ${!yearFilter ? 'active' : ''}`}
                  onClick={() => setYearFilter('')}
                >
                  ALL
                </button>
                {years.map((year) => (
                  <button
                    key={year}
                    className={`filter-btn ${yearFilter === year.toString() ? 'active' : ''}`}
                    onClick={() => setYearFilter(year.toString())}
                  >
                    {year}
                  </button>
                ))}
              </div>
            </div>

            {/* Player List */}
            <div className="sheet-player-list">
              {filteredPlayers.length > 0 ? (
                filteredPlayers.map((player) => (
                  <div
                    key={player.id}
                    className="sheet-player-item"
                    onClick={() => (isMyTurn() ? handleMakePick(player.id) : handleForcePick(player.id))}
                  >
                    <div className="player-avatar-small">
                      <img
                        src={`/api/players/manage/${player.id}/headshot`}
                        alt={player.name}
                        className="avatar-image"
                        onError={(e) => {
                          e.currentTarget.style.display = 'none';
                          e.currentTarget.nextElementSibling?.classList.remove('hidden');
                        }}
                      />
                      <span className="avatar-icon-small hidden">🏈</span>
                    </div>
                    <div className="player-info">
                      <div className="player-name">{player.name}</div>
                      <div className="player-meta">
                        <span className={`position-badge ${player.position}`}>{player.position}</span>
                        {player.team && <span className="team-name">{player.team}</span>}
                        {player.adp && <span className="adp-badge">ADP: {player.adp.toFixed(1)}</span>}
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <div className="empty-list">No players found</div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Toast Notifications */}
      <div className="toast-container">
        {toasts.map((toast) => (
          <Toast key={toast.id} message={toast.message} type={toast.type} />
        ))}
      </div>

      {/* Reconnecting Banner */}
      {reconnecting && (
        <div className="reconnecting-banner">
          <span className="spinner-small"></span>
          <span>Reconnecting...</span>
        </div>
      )}
    </div>
  );
};

export default MobileLiveDraftBoard;

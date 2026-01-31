import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { useWebSocket } from '../../contexts/WebSocketContext';
import { DraftStateMessage, PickMessage } from '../../models/WebSocketMessages';
import LivePlayerPool from './LivePlayerPool';
import LiveDraftGrid from './LiveDraftGrid';
import ForcePickModal from './ForcePickModal';
import Toast from '../shared/Toast';
import './live-draft-board.scss';

interface ToastNotification {
  id: number;
  message: string;
  type: 'success' | 'error';
}

interface LiveDraftBoardProps {}

const LiveDraftBoard: React.FC<LiveDraftBoardProps> = () => {
  const { uuid } = useParams<{ uuid: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { connect, subscribeToDraft, sendMessage, isConnected } = useWebSocket();

  const [draftState, setDraftState] = useState<DraftStateMessage | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reconnecting, setReconnecting] = useState(false);
  const [toasts, setToasts] = useState<ToastNotification[]>([]);
  const [forcePickModal, setForcePickModal] = useState<{
    isOpen: boolean;
    playerId: number | null;
    playerName: string;
  }>({
    isOpen: false,
    playerId: null,
    playerName: '',
  });

  const previousPicksRef = useRef<PickMessage[]>([]);
  const toastIdCounter = useRef(0);

  // Get user's position from URL query param
  const userPosition = searchParams.get('x');

  // Validate position
  useEffect(() => {
    if (!userPosition || !/^[A-Z]$/.test(userPosition)) {
      setError('Invalid or missing position parameter. Please join the lobby first.');
      setLoading(false);
    }
  }, [userPosition]);

  // Validate position is within participant count once draft state is loaded
  useEffect(() => {
    if (!draftState || !userPosition) return;

    const positionIndex = userPosition.charCodeAt(0) - 65; // A=0, B=1, etc.
    const totalSlots = draftState.participantCount; // Total configured slots, not just joined participants

    if (positionIndex >= totalSlots) {
      setError(`Position ${userPosition} is invalid for a ${totalSlots}-team draft.`);
      return;
    }

    // Check if user is actually a participant
    const isParticipant = draftState.participants.some((p) => p.position === userPosition);
    if (!isParticipant) {
      // Check if draft has started
      if (draftState.status === 'IN_PROGRESS' || draftState.status === 'COMPLETED') {
        setError(`This draft has already started. Position ${userPosition} was not assigned when the draft began.`);
      } else {
        setError(`You are not a participant in this draft. Please join the lobby first.`);
      }
    }
  }, [draftState, userPosition]);

  // Connect to WebSocket
  useEffect(() => {
    if (!uuid || !userPosition) {
      console.log('Missing uuid or userPosition:', { uuid, userPosition });
      return; // Don't set error yet, wait for params to load
    }

    const connectToDraft = async () => {
      try {
        console.log('Connecting to draft:', uuid, 'as position:', userPosition);
        setReconnecting(true);
        await connect(uuid);
        setReconnecting(false);
        setLoading(false);
        console.log('Successfully connected to draft');
      } catch (err) {
        console.error('Failed to connect to draft:', err);
        setError('Failed to connect to draft. Please try again.');
        setReconnecting(false);
        setLoading(false);
      }
    };

    // Only connect if not already connected
    if (!isConnected) {
      connectToDraft();
    } else {
      console.log('Already connected, skipping connection');
      setLoading(false);
    }

    // Don't disconnect on unmount - keep connection alive
  }, [uuid, userPosition, connect, isConnected]);

  // Handle connection state changes and reconnection
  useEffect(() => {
    if (!uuid || !userPosition) return;

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible' && !isConnected) {
        console.log('Page became visible, attempting to reconnect...');
        setReconnecting(true);
        connect(uuid)
          .then(() => {
            setReconnecting(false);
            // Request fresh draft state after reconnection
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

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [uuid, userPosition, isConnected, connect, sendMessage]);

  const showToast = useCallback((message: string, type: 'success' | 'error' = 'success') => {
    const id = toastIdCounter.current++;
    setToasts((prev) => [...prev, { id, message, type }]);
    
    // Auto-remove toast after 5 seconds
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 5000);
  }, []);

  // Subscribe to draft updates
  useEffect(() => {
    if (!uuid || !isConnected) {
      console.log('Cannot subscribe to draft - uuid:', uuid, 'isConnected:', isConnected);
      return;
    }

    console.log('Subscribing to draft updates for:', uuid);

    subscribeToDraft(uuid, (message: DraftStateMessage) => {
      console.log('Received draft state:', message);
      
      // Update previous picks reference without showing toasts
      previousPicksRef.current = message.picks;
      setDraftState(message);
      setLoading(false); // Ensure loading is false when we get state
      setError(null); // Clear any errors when we successfully get state
    });

    // Request initial draft state
    console.log('Requesting initial draft state');
    try {
      sendMessage(`/app/draft/${uuid}/state`, { draftUuid: uuid });
    } catch (err) {
      console.error('Error requesting draft state:', err);
      setError('Failed to load draft state. Please refresh the page.');
      setLoading(false);
    }
  }, [uuid, isConnected, subscribeToDraft, sendMessage, showToast]);

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
      } catch (err) {
        console.error('Failed to make pick:', err);
        setError('Failed to make pick. Please try again.');
      }
    },
    [uuid, userPosition, isMyTurn, sendMessage]
  );

  const handleOpenForcePick = useCallback((playerId: number) => {
    if (!draftState) return;
    const player = draftState.availablePlayers.find((p) => p.id === playerId);
    if (player) {
      setForcePickModal({
        isOpen: true,
        playerId,
        playerName: player.name,
      });
    }
  }, [draftState]);

  const handleConfirmForcePick = useCallback(
    async (targetPosition: string) => {
      if (!uuid || !userPosition || !forcePickModal.playerId) return;

      try {
        sendMessage(`/app/draft/${uuid}/force-pick`, {
          draftUuid: uuid,
          playerId: forcePickModal.playerId,
          targetPosition,
          forcingPosition: userPosition,
        });
        setForcePickModal({ isOpen: false, playerId: null, playerName: '' });
      } catch (err) {
        console.error('Failed to force pick:', err);
        setError('Failed to force pick. Please try again.');
      }
    },
    [uuid, userPosition, forcePickModal.playerId, sendMessage]
  );

  const handleCancelForcePick = useCallback(() => {
    setForcePickModal({ isOpen: false, playerId: null, playerName: '' });
  }, []);

  const handleDropPlayer = useCallback(
    (position: string, round: number) => {
      // This is called when a player is dropped on the grid
      // For now, we'll just log it - the actual pick is made via drag from player pool
      console.log('Player dropped on position', position, 'round', round);
    },
    []
  );

  if (loading) {
    return (
      <div className="live-draft-board">
        <div className="loading-container">
          <div className="spinner large"></div>
          <p>Loading draft...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="live-draft-board">
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

  return (
    <div className="live-draft-board">
      <nav className="navbar">
        <div className="nav-content">
          <div className="logo" onClick={() => navigate('/')}>
            <span className="logo-icon">🏈</span>
            <span className="logo-text">Devy BigBoard</span>
          </div>
          <div className="nav-actions">
            <div className="breadcrumb">
              <span className="breadcrumb-item" onClick={() => navigate('/')}>Home</span>
              <span className="breadcrumb-separator">›</span>
              <span className="breadcrumb-item" onClick={() => navigate('/live-draft')}>Live Draft</span>
              <span className="breadcrumb-separator">›</span>
              <span className="breadcrumb-item active">Draft Board</span>
            </div>
            {draftState && draftState.status === 'LOBBY' && (
              <button 
                onClick={() => navigate(`/draft/${uuid}/lobby`)} 
                className="back-to-lobby-btn"
              >
                ← Back to Lobby
              </button>
            )}
            <div className="draft-status">
              {reconnecting && <span className="reconnecting">Reconnecting...</span>}
              {draftState && (
                <span className="round-info">
                  Round {draftState.currentRound} • Pick {draftState.currentPick}
                </span>
              )}
            </div>
          </div>
        </div>
      </nav>

      <div className="draft-content">
        {reconnecting && (
          <div className="reconnecting-banner">
            <span className="spinner-small"></span>
            <span>Reconnecting to draft...</span>
          </div>
        )}
        
        {draftState && (
          <>
            <div className="draft-layout">
              <div className="player-pool-section">
                <LivePlayerPool
                  availablePlayers={draftState.availablePlayers}
                  isMyTurn={isMyTurn()}
                  onMakePick={handleMakePick}
                  onForcePick={handleOpenForcePick}
                />
              </div>

              <div className="draft-board-section">
                <LiveDraftGrid
                  participants={draftState.participants}
                  participantCount={draftState.participantCount}
                  picks={draftState.picks}
                  totalRounds={draftState.totalRounds}
                  isSnakeDraft={draftState.isSnakeDraft}
                  currentRound={draftState.currentRound}
                  currentTurnPosition={draftState.currentTurnPosition}
                  userPosition={userPosition || ''}
                  onDropPlayer={handleDropPlayer}
                />
              </div>
            </div>

            <ForcePickModal
              isOpen={forcePickModal.isOpen}
              playerName={forcePickModal.playerName}
              participants={draftState.participants}
              participantCount={draftState.participantCount}
              currentUserPosition={userPosition || ''}
              currentTurnPosition={draftState.currentTurnPosition}
              onConfirm={handleConfirmForcePick}
              onCancel={handleCancelForcePick}
            />
          </>
        )}
      </div>

      <div className="toast-container">
        {toasts.map((toast) => (
          <Toast key={toast.id} message={toast.message} type={toast.type} />
        ))}
      </div>
    </div>
  );
};

export default LiveDraftBoard;

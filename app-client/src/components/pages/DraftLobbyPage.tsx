import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import { useWebSocket } from '../../contexts/WebSocketContext';
import { webSocketService } from '../../services/WebSocketService';
import { LobbyStateMessage } from '../../models/WebSocketMessages';
import ParticipantList from '../draft/ParticipantList';
import StartDraftButton from '../draft/StartDraftButton';
import './draft-lobby.scss';

const DraftLobbyPage: React.FC = () => {
  const { uuid } = useParams<{ uuid: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const { connect, subscribeToLobby, sendMessage, isConnected } = useWebSocket();

  const [lobbyState, setLobbyState] = useState<LobbyStateMessage | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [startingDraft, setStartingDraft] = useState(false);
  const [showPositionSelector, setShowPositionSelector] = useState(false);
  const [nicknameInput, setNicknameInput] = useState('');
  const [nicknameError, setNicknameError] = useState('');
  const [lobbyStateTimeout, setLobbyStateTimeout] = useState(false);
  const [showCopiedToast, setShowCopiedToast] = useState(false);
  const [showPinModal, setShowPinModal] = useState(false);
  const [pinInput, setPinInput] = useState('');
  const [pinError, setPinError] = useState('');

  // Get position, nickname, and PIN from URL params
  const currentUserPosition = searchParams.get('position');
  const currentUserNickname = searchParams.get('nickname');
  const urlPin = searchParams.get('pin'); // PIN from share link

  // Get creator nickname from navigation state
  const creatorNickname = (location.state as any)?.creatorNickname;
  
  // Get PIN from lobbyState (backend) or fallback to URL/state
  const draftPin = lobbyState?.pin || (location.state as any)?.pin || urlPin;

  // Set a timeout for lobby state loading
  useEffect(() => {
    const timer = setTimeout(() => {
      if (!lobbyState && isConnected && showPositionSelector) {
        console.error('Lobby state not received after 10 seconds');
        setLobbyStateTimeout(true);
      }
    }, 10000); // Increased to 10 seconds

    return () => clearTimeout(timer);
  }, [lobbyState, isConnected, showPositionSelector]);

  // Check if user needs to select position
  useEffect(() => {
    if (!currentUserPosition || !currentUserNickname) {
      if (creatorNickname) {
        // Creator is joining for the first time
        setNicknameInput(creatorNickname);
      }
      setShowPositionSelector(true);
    }
  }, [currentUserPosition, currentUserNickname, creatorNickname]);

  // Connect to WebSocket
  useEffect(() => {
    if (!uuid) {
      setError('Invalid draft UUID');
      setLoading(false);
      return;
    }

    const connectToLobby = async () => {
      try {
        await connect(uuid);
        setLoading(false);
      } catch (err) {
        console.error('Failed to connect to lobby:', err);
        setError('Failed to connect to lobby. Please try again.');
        setLoading(false);
      }
    };

    connectToLobby();

    // Don't disconnect on unmount - let the draft board reuse the connection
    // return () => {
    //   disconnect();
    // };
  }, [uuid, connect]);

  // Subscribe to lobby updates
  useEffect(() => {
    if (!uuid || !isConnected) {
      console.log('Cannot subscribe - uuid:', uuid, 'isConnected:', isConnected);
      return;
    }

    console.log('Subscribing to lobby updates for draft:', uuid);
    console.log('WebSocket connected:', webSocketService.isConnected());

    // Subscribe to broadcast lobby updates (for join/leave/ready events and draft started)
    subscribeToLobby(uuid, (message: any) => {
      console.log('Received lobby state from broadcast:', message);
      console.log('Message type check - firstTurnPosition:', message.firstTurnPosition);
      console.log('Message type check - message field:', message.message);
      console.log('Message type check - participants:', message.participants);
      
      // Check if this is a draft started message by checking for firstTurnPosition field
      if (message.firstTurnPosition !== undefined || (message.message && typeof message.message === 'string' && message.message.toLowerCase().includes('started'))) {
        console.log('✅ Draft started detected! Redirecting to draft board...');
        // Get the current position from URL params at the time of navigation
        const urlParams = new URLSearchParams(window.location.search);
        const currentPosition = urlParams.get('position');
        console.log('Current user position from URL:', currentPosition);
        setStartingDraft(false); // Reset loading state
        // Redirect to draft board
        if (currentPosition) {
          console.log('Navigating to:', `/draft/${uuid}?x=${currentPosition}`);
          navigate(`/draft/${uuid}?x=${currentPosition}`);
        } else {
          console.error('❌ Cannot navigate: position not found in URL params');
          console.error('Current URL:', window.location.href);
        }
        return;
      }
      
      // Otherwise, treat it as a lobby state message
      if (message.participants) {
        console.log('Treating as lobby state message');
        setLobbyState(message as LobbyStateMessage);
      }
    });

    // Also subscribe to user-specific lobby state responses
    try {
      if (webSocketService.isConnected()) {
        console.log('Subscribing to /user/queue/lobby-state');
        webSocketService.subscribe('/user/queue/lobby-state', (message: LobbyStateMessage) => {
          console.log('Received lobby state from user queue:', message);
          setLobbyState(message);
        });
        
        // Subscribe to error messages
        console.log('Subscribing to /user/queue/errors');
        webSocketService.subscribe('/user/queue/errors', (errorMessage: any) => {
          console.error('Received error message:', errorMessage);
          
          // Handle PIN verification errors
          if (errorMessage.code === 'READY_ERROR' && errorMessage.message.includes('PIN')) {
            setPinError('Invalid PIN');
            
            // Remove participant and navigate away after showing error
            setTimeout(() => {
              if (uuid && currentUserPosition) {
                sendMessage(`/app/draft/${uuid}/leave`, {
                  draftUuid: uuid,
                  position: currentUserPosition,
                });
              }
              
              // Navigate back to live draft page
              navigate('/live-draft');
            }, 1500);
          } else {
            setStartingDraft(false); // Reset loading state on other errors
            setError(errorMessage.message || 'An error occurred');
          }
        });
      } else {
        console.error('WebSocket not connected, cannot subscribe to user queue');
      }
    } catch (err) {
      console.error('Error subscribing to user queue:', err);
    }

    // Request initial lobby state
    console.log('Requesting initial lobby state for draft:', uuid);
    try {
      // Add a small delay to ensure WebSocket is fully connected
      setTimeout(() => {
        sendMessage(`/app/draft/${uuid}/lobby/state`, { draftUuid: uuid });
        console.log('Lobby state request sent successfully');
      }, 100);
    } catch (err) {
      console.error('Error sending lobby state request:', err);
    }
  }, [uuid, isConnected, subscribeToLobby, sendMessage]);

  const handleJoin = useCallback(
    async (nickname: string, position: string) => {
      if (!uuid) return;

      try {
        sendMessage(`/app/draft/${uuid}/join`, {
          draftUuid: uuid,
          nickname,
          position,
        });

        // Update URL with position and nickname params
        setSearchParams({ position, nickname });
        setShowPositionSelector(false);
        
        // If PIN is in URL (from share link), auto-verify
        if (urlPin) {
          console.log('Auto-verifying with PIN from URL');
          setTimeout(() => {
            sendMessage(`/app/draft/${uuid}/ready`, {
              draftUuid: uuid,
              position,
              isReady: true,
              pin: urlPin,
            });
          }, 500); // Small delay to ensure join completes first
        } else if (nickname !== creatorNickname) {
          // No PIN in URL and not creator - show PIN modal
          setShowPinModal(true);
        }
      } catch (err) {
        console.error('Failed to join lobby:', err);
        throw new Error('Failed to join lobby. Please try again.');
      }
    },
    [uuid, sendMessage, setSearchParams, creatorNickname, urlPin]
  );

  const handlePositionSelect = useCallback(
    (position: string) => {
      const nickname = nicknameInput.trim();
      
      if (!nickname) {
        setNicknameError('Nickname is required');
        return;
      }
      
      if (nickname.length < 2) {
        setNicknameError('Nickname must be at least 2 characters');
        return;
      }
      
      if (nickname.length > 20) {
        setNicknameError('Nickname must be 20 characters or less');
        return;
      }

      handleJoin(nickname, position);
    },
    [nicknameInput, handleJoin]
  );

  const handleShowPositionSelector = useCallback(() => {
    setShowPositionSelector(true);
  }, []);

  const handleStartDraft = useCallback(() => {
    if (!uuid || !currentUserPosition) {
      console.error('Cannot start draft - missing uuid or position:', { uuid, currentUserPosition });
      return;
    }

    console.log('🚀 Starting draft...', { uuid, currentUserPosition });
    setStartingDraft(true);
    try {
      sendMessage(`/app/draft/${uuid}/start`, {
        draftUuid: uuid,
        position: currentUserPosition,
      });
      console.log('✅ Start draft message sent successfully');
      
      // Set a timeout to reset loading state if no response after 10 seconds
      setTimeout(() => {
        console.warn('⏱️ Timeout: No response after 10 seconds, resetting loading state');
        setStartingDraft(false);
      }, 10000);
    } catch (err) {
      console.error('❌ Error starting draft:', err);
      setStartingDraft(false);
      setError('Failed to start draft. Please try again.');
    }
  }, [uuid, currentUserPosition, sendMessage]);

  const handleDeleteDraft = useCallback(async () => {
    if (!uuid) return;
    
    const confirmed = window.confirm('Are you sure you want to delete this draft? This cannot be undone.');
    if (!confirmed) return;
    
    try {
      const response = await fetch(`/api/live-drafts/${uuid}`, {
        method: 'DELETE',
      });
      
      if (!response.ok) {
        throw new Error('Failed to delete draft');
      }
      
      // Redirect to live draft page
      navigate('/live-draft');
    } catch (err) {
      console.error('Failed to delete draft:', err);
      setError('Failed to delete draft. Please try again.');
    }
  }, [uuid, navigate]);

  const isCreator = (): boolean => {
    if (!lobbyState || !currentUserNickname) return false;
    // The creator is identified by matching nickname with createdBy field
    return currentUserNickname === lobbyState.createdBy;
  };

  const handleCopyLink = useCallback(async () => {
    if (!uuid) return;
    
    try {
      // Fetch share link from backend (includes PIN)
      const response = await fetch(`/api/drafts/${uuid}/share-link`);
      if (!response.ok) {
        throw new Error('Failed to get share link');
      }
      
      const data = await response.json();
      const shareUrl = data.shareUrl;
      
      navigator.clipboard.writeText(shareUrl).then(() => {
        setShowCopiedToast(true);
        setTimeout(() => setShowCopiedToast(false), 3000);
      }).catch(err => {
        console.error('Failed to copy link:', err);
      });
    } catch (err) {
      console.error('Failed to get share link:', err);
    }
  }, [uuid]);

  const handlePinSubmit = useCallback(() => {
    if (!uuid || !currentUserPosition) return;
    
    if (!pinInput || pinInput.length !== 4) {
      setPinError('PIN must be 4 digits');
      return;
    }
    
    try {
      sendMessage(`/app/draft/${uuid}/ready`, {
        draftUuid: uuid,
        position: currentUserPosition,
        isReady: true,
        pin: pinInput,
      });
      
      // If successful, close modal after a short delay
      setTimeout(() => {
        setShowPinModal(false);
        setPinInput('');
        setPinError('');
      }, 500);
    } catch (err) {
      console.error('Failed to verify PIN:', err);
      setPinError('Failed to verify PIN. Please try again.');
    }
  }, [uuid, currentUserPosition, pinInput, sendMessage]);

  const handlePinCancel = useCallback(() => {
    if (!uuid || !currentUserPosition) return;
    
    // Leave the lobby when canceling PIN verification
    try {
      sendMessage(`/app/draft/${uuid}/leave`, {
        draftUuid: uuid,
        position: currentUserPosition,
      });
      
      // Navigate back to live draft page
      navigate('/live-draft');
    } catch (err) {
      console.error('Failed to leave lobby:', err);
    }
  }, [uuid, currentUserPosition, sendMessage, navigate]);

  if (loading) {
    return (
      <div className="draft-lobby-page">
        <div className="loading-container">
          <div className="spinner large"></div>
          <p>Connecting to lobby...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="draft-lobby-page">
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
    <div className="draft-lobby-page">
      <nav className="navbar">
        <div className="nav-content">
          <div className="logo" onClick={() => navigate('/')}>
            <span className="logo-icon">🏈</span>
            <span className="logo-text">Devy BigBoard</span>
          </div>
          <div className="nav-right">
            <div className="breadcrumb">
              <span className="breadcrumb-item" onClick={() => navigate('/')}>Home</span>
              <span className="breadcrumb-separator">›</span>
              <span className="breadcrumb-item" onClick={() => navigate('/live-draft')}>Live Draft</span>
              <span className="breadcrumb-separator">›</span>
              <span className="breadcrumb-item active">Lobby</span>
            </div>
            <button onClick={() => navigate('/')} className="back-btn">
              ← Back to Home
            </button>
          </div>
        </div>
      </nav>

      <div className="lobby-content">
        <div className="lobby-header">
          <h1>{lobbyState?.draftName || 'Draft Lobby'}</h1>
          <div className="draft-info">
            <span className="info-item">
              <strong>{lobbyState?.participantCount}</strong> Teams
            </span>
            <span className="separator">•</span>
            <span className="info-item">
              <strong>{lobbyState?.totalRounds}</strong> Rounds
            </span>
          </div>
        </div>

        <div className="lobby-main">
          <div className="lobby-left">
            {!currentUserPosition ? (
              showPositionSelector ? (
                <div className="position-selector">
                  <h2>Select Your Position</h2>
                  <p className="selector-description">Choose your draft position by clicking a spot below</p>
                  
                  {!creatorNickname && (
                    <div className="nickname-input-group">
                      <label htmlFor="nickname">Your Nickname</label>
                      <input
                        id="nickname"
                        type="text"
                        value={nicknameInput}
                        onChange={(e) => {
                          setNicknameInput(e.target.value);
                          setNicknameError('');
                        }}
                        placeholder="Enter your nickname"
                        maxLength={20}
                        className={nicknameError ? 'error' : ''}
                        autoFocus
                      />
                      {nicknameError && <span className="error-message">{nicknameError}</span>}
                    </div>
                  )}

                  {creatorNickname && (
                    <div className="nickname-display">
                      <label>Your Nickname</label>
                      <div className="nickname-value">{creatorNickname}</div>
                    </div>
                  )}

                  {!lobbyState ? (
                    <div className="loading-positions">
                      <div className="spinner"></div>
                      <p>Loading positions...</p>
                      {lobbyStateTimeout && (
                        <div className="timeout-message">
                          <p>Taking longer than expected. Check your connection.</p>
                          <button 
                            onClick={() => {
                              console.log('Retrying lobby state request');
                              sendMessage(`/app/draft/${uuid}/lobby/state`, { draftUuid: uuid });
                              setLobbyStateTimeout(false);
                            }}
                            className="btn-retry"
                          >
                            Retry
                          </button>
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className="position-grid">
                      {Array.from({ length: lobbyState.participantCount }, (_, i) => {
                        const position = String.fromCharCode(65 + i);
                        const isTaken = lobbyState.participants.some(p => p.position === position);
                        const participant = lobbyState.participants.find(p => p.position === position);
                        const hasNickname = nicknameInput.trim().length >= 2 || !!creatorNickname;
                        
                        return (
                          <button
                            key={position}
                            className={`position-spot ${isTaken ? 'taken' : 'available'} ${!hasNickname ? 'disabled' : ''}`}
                            onClick={() => !isTaken && hasNickname && handlePositionSelect(position)}
                            disabled={isTaken || !hasNickname}
                          >
                            <span className="position-number">1.{i + 1}</span>
                            <span className="position-letter">{position}</span>
                            {isTaken && participant && (
                              <span className="position-owner">{participant.nickname}</span>
                            )}
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>
              ) : (
                <div className="join-prompt">
                  <h2>Join the Draft</h2>
                  <p>Click below to select your position</p>
                  <button onClick={handleShowPositionSelector} className="btn btn-primary">
                    Choose Position
                  </button>
                </div>
              )
            ) : (
              <div className="user-controls">
                {draftPin && (lobbyState?.participants.find(p => p.position === currentUserPosition)?.isVerified || isCreator()) && (
                  <div className="pin-display-large">
                    <span className="pin-label">PIN</span>
                    <span className="pin-value">{draftPin}</span>
                  </div>
                )}
                <div className="user-info">
                  <h3>You are {currentUserNickname}</h3>
                  <p>Position {currentUserPosition}</p>
                </div>
                <div className="action-buttons">
                  <StartDraftButton
                    isCreator={isCreator()}
                    allReady={lobbyState?.allReady || false}
                    onStart={handleStartDraft}
                    loading={startingDraft}
                  />
                  {(lobbyState?.participants.find(p => p.position === currentUserPosition)?.isVerified || isCreator()) && (
                    <button 
                      className="share-link-btn"
                      onClick={handleCopyLink}
                    >
                      📋 Copy Lobby Link
                    </button>
                  )}
                  {isCreator() && (
                    <button 
                      className="delete-draft-btn"
                      onClick={handleDeleteDraft}
                    >
                      🗑️ Delete Draft
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>

          <div className="lobby-right">
            {lobbyState && (
              <ParticipantList
                participants={lobbyState.participants}
                participantCount={lobbyState.participantCount}
                currentUserPosition={currentUserPosition || undefined}
              />
            )}
          </div>
        </div>
        
        {showCopiedToast && (
          <div className="copied-toast">Link copied to clipboard!</div>
        )}
      </div>

      {showPinModal && (
        <div className="pin-modal-overlay" onClick={() => setShowPinModal(false)}>
          <div className="pin-modal" onClick={(e) => e.stopPropagation()}>
            <h2>Enter Draft PIN</h2>
            <p>Please enter the 4-digit PIN to verify your participation</p>
            {draftPin && (
              <div className="pin-hint">
                <strong>Your PIN:</strong> {draftPin}
              </div>
            )}
            <input
              type="text"
              value={pinInput}
              onChange={(e) => {
                setPinInput(e.target.value.replace(/\D/g, '').slice(0, 4));
                setPinError('');
              }}
              placeholder="Enter 4-digit PIN"
              maxLength={4}
              className={pinError ? 'error' : ''}
              autoFocus
            />
            {pinError && <span className="error-message">{pinError}</span>}
            <div className="pin-modal-actions">
              <button onClick={handlePinCancel} className="btn btn-secondary">
                Cancel
              </button>
              <button onClick={handlePinSubmit} className="btn btn-primary">
                Verify
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DraftLobbyPage;

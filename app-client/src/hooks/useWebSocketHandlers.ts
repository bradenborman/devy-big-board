import { useEffect, useCallback } from 'react';
import { useWebSocket } from '../contexts/WebSocketContext';
import {
  DraftStateMessage,
  LobbyStateMessage,
  ErrorMessage,
  ParticipantJoinedMessage,
  ParticipantLeftMessage,
  DraftStartedMessage,
  PickMessage,
} from '../models/WebSocketMessages';

/**
 * Message handler callbacks
 */
export interface WebSocketHandlers {
  onDraftStateUpdate?: (message: DraftStateMessage) => void;
  onLobbyStateUpdate?: (message: LobbyStateMessage) => void;
  onPickMade?: (pick: PickMessage) => void;
  onError?: (error: ErrorMessage) => void;
  onParticipantJoined?: (message: ParticipantJoinedMessage) => void;
  onParticipantLeft?: (message: ParticipantLeftMessage) => void;
  onDraftStarted?: (message: DraftStartedMessage) => void;
}

/**
 * Custom hook for handling WebSocket messages
 * Provides a clean API for components to handle different message types
 * 
 * @param draftUuid The UUID of the draft
 * @param handlers Object containing callback functions for different message types
 */
export const useWebSocketHandlers = (draftUuid: string | null, handlers: WebSocketHandlers) => {
  const {
    subscribeToDraft,
    subscribeToLobby,
    onError,
    onParticipantJoined,
    onParticipantLeft,
    onDraftStarted,
  } = useWebSocket();

  /**
   * Handle draft state updates
   */
  const handleDraftState = useCallback((message: DraftStateMessage) => {
    console.log('Draft state update received:', message);
    
    if (handlers.onDraftStateUpdate) {
      handlers.onDraftStateUpdate(message);
    }

    // If there are new picks, call the onPickMade handler for the latest pick
    if (handlers.onPickMade && message.picks && message.picks.length > 0) {
      const latestPick = message.picks[message.picks.length - 1];
      handlers.onPickMade(latestPick);
    }
  }, [handlers]);

  /**
   * Handle lobby state updates
   */
  const handleLobbyState = useCallback((message: LobbyStateMessage) => {
    console.log('Lobby state update received:', message);
    
    if (handlers.onLobbyStateUpdate) {
      handlers.onLobbyStateUpdate(message);
    }
  }, [handlers]);

  /**
   * Handle error messages
   */
  const handleError = useCallback((error: ErrorMessage) => {
    console.error('WebSocket error received:', error);
    
    if (handlers.onError) {
      handlers.onError(error);
    }
  }, [handlers]);

  /**
   * Handle participant joined messages
   */
  const handleParticipantJoined = useCallback((message: ParticipantJoinedMessage) => {
    console.log('Participant joined:', message);
    
    if (handlers.onParticipantJoined) {
      handlers.onParticipantJoined(message);
    }
  }, [handlers]);

  /**
   * Handle participant left messages
   */
  const handleParticipantLeft = useCallback((message: ParticipantLeftMessage) => {
    console.log('Participant left:', message);
    
    if (handlers.onParticipantLeft) {
      handlers.onParticipantLeft(message);
    }
  }, [handlers]);

  /**
   * Handle draft started messages
   */
  const handleDraftStarted = useCallback((message: DraftStartedMessage) => {
    console.log('Draft started:', message);
    
    if (handlers.onDraftStarted) {
      handlers.onDraftStarted(message);
    }
  }, [handlers]);

  /**
   * Subscribe to topics when draftUuid is available
   */
  useEffect(() => {
    if (!draftUuid) {
      return;
    }

    // Subscribe to draft state updates if handler is provided
    if (handlers.onDraftStateUpdate || handlers.onPickMade) {
      subscribeToDraft(draftUuid, handleDraftState);
    }

    // Subscribe to lobby state updates if handler is provided
    if (handlers.onLobbyStateUpdate) {
      subscribeToLobby(draftUuid, handleLobbyState);
    }

    // Register error handler if provided
    if (handlers.onError) {
      onError(handleError);
    }

    // Register participant joined handler if provided
    if (handlers.onParticipantJoined) {
      onParticipantJoined(handleParticipantJoined);
    }

    // Register participant left handler if provided
    if (handlers.onParticipantLeft) {
      onParticipantLeft(handleParticipantLeft);
    }

    // Register draft started handler if provided
    if (handlers.onDraftStarted) {
      onDraftStarted(handleDraftStarted);
    }
  }, [
    draftUuid,
    handlers.onDraftStateUpdate,
    handlers.onLobbyStateUpdate,
    handlers.onPickMade,
    handlers.onError,
    handlers.onParticipantJoined,
    handlers.onParticipantLeft,
    handlers.onDraftStarted,
    subscribeToDraft,
    subscribeToLobby,
    onError,
    onParticipantJoined,
    onParticipantLeft,
    onDraftStarted,
    handleDraftState,
    handleLobbyState,
    handleError,
    handleParticipantJoined,
    handleParticipantLeft,
    handleDraftStarted,
  ]);
};

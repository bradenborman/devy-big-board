import React, { createContext, useContext, useEffect, useState, useCallback, ReactNode } from 'react';
import { webSocketService } from '../services/WebSocketService';
import {
  DraftStateMessage,
  LobbyStateMessage,
  ErrorMessage,
  ParticipantJoinedMessage,
  ParticipantLeftMessage,
  DraftStartedMessage,
} from '../models/WebSocketMessages';

/**
 * WebSocket connection state
 */
export enum ConnectionState {
  DISCONNECTED = 'DISCONNECTED',
  CONNECTING = 'CONNECTING',
  CONNECTED = 'CONNECTED',
  RECONNECTING = 'RECONNECTING',
  ERROR = 'ERROR',
}

/**
 * WebSocket context value
 */
interface WebSocketContextValue {
  connectionState: ConnectionState;
  error: string | null;
  connect: (draftUuid: string) => Promise<void>;
  disconnect: () => void;
  subscribeToDraft: (draftUuid: string, callback: (message: DraftStateMessage) => void) => void;
  subscribeToLobby: (draftUuid: string, callback: (message: LobbyStateMessage) => void) => void;
  onError: (callback: (error: ErrorMessage) => void) => void;
  onParticipantJoined: (callback: (message: ParticipantJoinedMessage) => void) => void;
  onParticipantLeft: (callback: (message: ParticipantLeftMessage) => void) => void;
  onDraftStarted: (callback: (message: DraftStartedMessage) => void) => void;
  sendMessage: (destination: string, message: any) => void;
  isConnected: boolean;
}

const WebSocketContext = createContext<WebSocketContextValue | undefined>(undefined);

/**
 * Props for WebSocketProvider
 */
interface WebSocketProviderProps {
  children: ReactNode;
}

/**
 * WebSocket provider component
 * Manages WebSocket connection state and provides context to child components
 */
export const WebSocketProvider: React.FC<WebSocketProviderProps> = ({ children }) => {
  const [connectionState, setConnectionState] = useState<ConnectionState>(ConnectionState.DISCONNECTED);
  const [error, setError] = useState<string | null>(null);

  /**
   * Connect to WebSocket server
   */
  const connect = useCallback(async (draftUuid: string) => {
    try {
      setConnectionState(ConnectionState.CONNECTING);
      setError(null);

      await webSocketService.connect(draftUuid);
      setConnectionState(ConnectionState.CONNECTED);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to connect to WebSocket';
      setError(errorMessage);
      setConnectionState(ConnectionState.ERROR);
      throw err;
    }
  }, []);

  /**
   * Disconnect from WebSocket server
   */
  const disconnect = useCallback(() => {
    webSocketService.disconnect();
    setConnectionState(ConnectionState.DISCONNECTED);
    setError(null);
  }, []);

  /**
   * Subscribe to draft state updates
   */
  const subscribeToDraft = useCallback((draftUuid: string, callback: (message: DraftStateMessage) => void) => {
    const topic = `/topic/draft/${draftUuid}`;

    // Subscribe to topic
    if (webSocketService.isConnected()) {
      webSocketService.subscribe(topic, callback);
    }
  }, []);

  /**
   * Subscribe to lobby state updates
   */
  const subscribeToLobby = useCallback((draftUuid: string, callback: (message: LobbyStateMessage) => void) => {
    const topic = `/topic/draft/${draftUuid}/lobby`;

    // Subscribe to topic
    if (webSocketService.isConnected()) {
      webSocketService.subscribe(topic, callback);
    }
  }, []);

  /**
   * Register error callback
   */
  const onError = useCallback((callback: (error: ErrorMessage) => void) => {
    // Error callbacks are handled by the message handlers
    // This is a placeholder for future implementation
    console.log('Error callback registered:', callback);
  }, []);

  /**
   * Register participant joined callback
   */
  const onParticipantJoined = useCallback((callback: (message: ParticipantJoinedMessage) => void) => {
    // Participant joined callbacks are handled by the message handlers
    // This is a placeholder for future implementation
    console.log('Participant joined callback registered:', callback);
  }, []);

  /**
   * Register participant left callback
   */
  const onParticipantLeft = useCallback((callback: (message: ParticipantLeftMessage) => void) => {
    // Participant left callbacks are handled by the message handlers
    // This is a placeholder for future implementation
    console.log('Participant left callback registered:', callback);
  }, []);

  /**
   * Register draft started callback
   */
  const onDraftStarted = useCallback((callback: (message: DraftStartedMessage) => void) => {
    // Draft started callbacks are handled by the message handlers
    // This is a placeholder for future implementation
    console.log('Draft started callback registered:', callback);
  }, []);

  /**
   * Send a message to the server
   */
  const sendMessage = useCallback((destination: string, message: any) => {
    if (!webSocketService.isConnected()) {
      throw new Error('WebSocket not connected. Cannot send message.');
    }
    webSocketService.send(destination, message);
  }, []);

  /**
   * Cleanup on unmount
   */
  useEffect(() => {
    return () => {
      disconnect();
    };
  }, [disconnect]);

  const contextValue: WebSocketContextValue = {
    connectionState,
    error,
    connect,
    disconnect,
    subscribeToDraft,
    subscribeToLobby,
    onError,
    onParticipantJoined,
    onParticipantLeft,
    onDraftStarted,
    sendMessage,
    isConnected: connectionState === ConnectionState.CONNECTED,
  };

  return (
    <WebSocketContext.Provider value={contextValue}>
      {children}
    </WebSocketContext.Provider>
  );
};

/**
 * Hook to use WebSocket context
 */
export const useWebSocket = (): WebSocketContextValue => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};

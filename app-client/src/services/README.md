# WebSocket Client Documentation

This directory contains the WebSocket client implementation for real-time communication with the backend.

## Overview

The WebSocket client uses:
- **SockJS** for fallback support (works even if WebSocket is blocked)
- **STOMP protocol** for messaging
- **React Context** for state management
- **TypeScript** for type safety

## Architecture

```
WebSocketService (Low-level)
    ↓
WebSocketContext (React Context)
    ↓
useWebSocketHandlers (Custom Hook)
    ↓
React Components
```

## Files

### Core Services

- **`WebSocketService.ts`** - Low-level WebSocket connection management
  - Handles connection, disconnection, reconnection
  - Manages subscriptions to topics
  - Sends messages to the server
  - Implements exponential backoff for reconnection

- **`WebSocketMessageSender.ts`** - Type-safe message sending utilities
  - Provides methods for all WebSocket operations
  - Handles message formatting and destination routing

### React Integration

- **`../contexts/WebSocketContext.tsx`** - React Context for WebSocket state
  - Manages connection state
  - Provides hooks for subscribing to topics
  - Handles error states

- **`../hooks/useWebSocketHandlers.ts`** - Custom hook for message handling
  - Simplifies subscribing to different message types
  - Provides clean callback API for components

### Type Definitions

- **`../models/WebSocketMessages.ts`** - TypeScript interfaces for all messages
  - Matches backend Java message models
  - Provides type safety for all WebSocket communication

## Usage

### 1. Wrap your app with WebSocketProvider

```tsx
import { WebSocketProvider } from './contexts/WebSocketContext';

function App() {
  return (
    <WebSocketProvider>
      <YourComponents />
    </WebSocketProvider>
  );
}
```

### 2. Connect to WebSocket in your component

```tsx
import { useWebSocket } from '../contexts/WebSocketContext';

function MyComponent() {
  const { connect, disconnect, isConnected } = useWebSocket();
  const draftUuid = 'your-draft-uuid';

  useEffect(() => {
    connect(draftUuid);
    return () => disconnect();
  }, [draftUuid]);

  return <div>Connected: {isConnected ? 'Yes' : 'No'}</div>;
}
```

### 3. Handle messages with useWebSocketHandlers

```tsx
import { useWebSocketHandlers } from '../hooks/useWebSocketHandlers';

function DraftBoard() {
  const draftUuid = 'your-draft-uuid';

  useWebSocketHandlers(draftUuid, {
    onDraftStateUpdate: (message) => {
      console.log('Draft state updated:', message);
      // Update your component state
    },
    onPickMade: (pick) => {
      console.log('New pick:', pick);
      // Show notification
    },
    onError: (error) => {
      console.error('Error:', error);
      // Show error to user
    },
  });

  return <div>Draft Board</div>;
}
```

### 4. Send messages to the server

```tsx
import { WebSocketMessageSender } from '../services/WebSocketMessageSender';

function JoinButton() {
  const handleJoin = () => {
    WebSocketMessageSender.joinLobby({
      draftUuid: 'your-draft-uuid',
      nickname: 'Alice',
      position: 'A',
    });
  };

  return <button onClick={handleJoin}>Join Lobby</button>;
}
```

## Message Types

### Incoming Messages (from server)

- **`DraftStateMessage`** - Complete draft state (picks, participants, current turn)
- **`LobbyStateMessage`** - Lobby state (participants, ready status)
- **`PickMessage`** - Individual pick notification
- **`ErrorMessage`** - Error notification
- **`ParticipantJoinedMessage`** - Someone joined the lobby
- **`ParticipantLeftMessage`** - Someone left the lobby
- **`DraftStartedMessage`** - Draft has started

### Outgoing Messages (to server)

- **`JoinRequest`** - Join a draft lobby
- **`ReadyRequest`** - Toggle ready status
- **`MakePickRequest`** - Make a pick
- **`ForcePickRequest`** - Force a pick for another position

## Topics

### Subscribe to these topics:

- **`/topic/draft/{draftUuid}/lobby`** - Lobby updates (join, ready, leave)
- **`/topic/draft/{draftUuid}`** - Draft updates (picks, state changes)

### Send messages to these destinations:

- **`/app/draft/{draftUuid}/join`** - Join lobby
- **`/app/draft/{draftUuid}/ready`** - Toggle ready
- **`/app/draft/{draftUuid}/leave`** - Leave lobby
- **`/app/draft/{draftUuid}/start`** - Start draft
- **`/app/draft/{draftUuid}/pick`** - Make pick
- **`/app/draft/{draftUuid}/force-pick`** - Force pick
- **`/app/draft/{draftUuid}/state`** - Request current state
- **`/app/draft/{draftUuid}/lobby/state`** - Request lobby state

## Connection Management

### Automatic Reconnection

The WebSocket client automatically attempts to reconnect if the connection is lost:
- Uses exponential backoff (1s, 2s, 4s, 8s, 16s, 30s max)
- Maximum 10 reconnection attempts
- Automatically re-subscribes to topics after reconnection

### Manual Reconnection

```tsx
const { connect, disconnect } = useWebSocket();

// Disconnect
disconnect();

// Reconnect
await connect(draftUuid);
```

## Environment Configuration

The WebSocket client automatically detects the environment:

- **Development**: Connects to `http://localhost:8080/ws`
- **Production**: Connects to the same host as the frontend

## Error Handling

```tsx
useWebSocketHandlers(draftUuid, {
  onError: (error) => {
    // Display error to user
    alert(`Error: ${error.message}`);
  },
});
```

## Best Practices

1. **Always disconnect on unmount** - Use cleanup in useEffect
2. **Handle reconnection** - Show "Reconnecting..." indicator to users
3. **Request state on reconnect** - Use `requestDraftState()` after reconnection
4. **Show connection status** - Display connection state to users
5. **Handle errors gracefully** - Show user-friendly error messages

## Example: Complete Draft Component

```tsx
import React, { useEffect, useState } from 'react';
import { useWebSocket } from '../contexts/WebSocketContext';
import { useWebSocketHandlers } from '../hooks/useWebSocketHandlers';
import { WebSocketMessageSender } from '../services/WebSocketMessageSender';
import { DraftStateMessage } from '../models/WebSocketMessages';

function LiveDraftBoard({ draftUuid, position }: { draftUuid: string; position: string }) {
  const { connect, disconnect, isConnected, connectionState } = useWebSocket();
  const [draftState, setDraftState] = useState<DraftStateMessage | null>(null);

  // Connect on mount
  useEffect(() => {
    connect(draftUuid);
    return () => disconnect();
  }, [draftUuid]);

  // Handle messages
  useWebSocketHandlers(draftUuid, {
    onDraftStateUpdate: (message) => {
      setDraftState(message);
    },
    onPickMade: (pick) => {
      console.log('New pick:', pick.playerName);
    },
    onError: (error) => {
      alert(`Error: ${error.message}`);
    },
  });

  // Make a pick
  const handlePick = (playerId: number) => {
    WebSocketMessageSender.makePick({
      draftUuid,
      playerId,
      position,
    });
  };

  if (!isConnected) {
    return <div>Connecting... ({connectionState})</div>;
  }

  return (
    <div>
      <h1>Live Draft</h1>
      <p>Current Turn: {draftState?.currentTurnPosition}</p>
      <p>Round: {draftState?.currentRound}</p>
      {/* Render draft board */}
    </div>
  );
}
```

## Troubleshooting

### Connection fails
- Check that backend is running on port 8080
- Check browser console for errors
- Verify WebSocket endpoint is accessible

### Messages not received
- Verify you're subscribed to the correct topic
- Check that draftUuid is correct
- Look for errors in browser console

### Reconnection not working
- Check that you didn't manually disconnect
- Verify backend is running
- Check reconnection attempts in console logs

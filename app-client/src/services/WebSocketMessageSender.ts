import { webSocketService } from './WebSocketService';
import {
  JoinRequest,
  ReadyRequest,
  MakePickRequest,
  ForcePickRequest,
} from '../models/WebSocketMessages';

/**
 * Utility class for sending WebSocket messages to the backend
 * Provides type-safe methods for all WebSocket operations
 */
export class WebSocketMessageSender {
  /**
   * Join a draft lobby
   */
  static joinLobby(request: JoinRequest): void {
    const destination = `/app/draft/${request.draftUuid}/join`;
    webSocketService.send(destination, request);
  }

  /**
   * Toggle ready status in lobby
   */
  static toggleReady(request: ReadyRequest): void {
    const destination = `/app/draft/${request.draftUuid}/ready`;
    webSocketService.send(destination, request);
  }

  /**
   * Leave a draft lobby
   */
  static leaveLobby(draftUuid: string, position: string): void {
    const destination = `/app/draft/${draftUuid}/leave`;
    webSocketService.send(destination, { draftUuid, position });
  }

  /**
   * Start a draft (creator only)
   */
  static startDraft(draftUuid: string, position: string): void {
    const destination = `/app/draft/${draftUuid}/start`;
    webSocketService.send(destination, { draftUuid, position });
  }

  /**
   * Make a pick during the draft
   */
  static makePick(request: MakePickRequest): void {
    const destination = `/app/draft/${request.draftUuid}/pick`;
    webSocketService.send(destination, request);
  }

  /**
   * Force a pick for another position
   */
  static forcePick(request: ForcePickRequest): void {
    const destination = `/app/draft/${request.draftUuid}/force-pick`;
    webSocketService.send(destination, request);
  }

  /**
   * Undo the last pick in the draft
   */
  static undoLastPick(draftUuid: string): void {
    const destination = `/app/draft/${draftUuid}/undo`;
    webSocketService.send(destination, { draftUuid });
  }

  /**
   * Request current draft state (for reconnection)
   */
  static requestDraftState(draftUuid: string): void {
    const destination = `/app/draft/${draftUuid}/state`;
    webSocketService.send(destination, { draftUuid });
  }

  /**
   * Request current lobby state (for reconnection)
   */
  static requestLobbyState(draftUuid: string): void {
    const destination = `/app/draft/${draftUuid}/lobby/state`;
    webSocketService.send(destination, { draftUuid });
  }
}

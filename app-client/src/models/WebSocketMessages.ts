/**
 * TypeScript interfaces for WebSocket messages
 * These match the backend Java message models
 */

export interface ParticipantInfo {
  position: string;
  nickname: string;
  isReady: boolean;
  joinedAt: string;
}

export interface PickMessage {
  playerId: number;
  playerName: string;
  position: string;
  team: string;
  college: string;
  roundNumber: number;
  pickNumber: number;
  pickedByPosition: string;
  forcedByPosition?: string;
  pickedAt: string;
}

export interface PlayerResponse {
  id: number;
  name: string;
  position: string;
  team: string;
  college: string;
  adp?: number;
}

export interface DraftStateMessage {
  draftUuid: string;
  status: string;
  currentRound: number;
  currentPick: number;
  currentTurnPosition: string;
  participantCount: number;
  participants: ParticipantInfo[];
  picks: PickMessage[];
  availablePlayers: PlayerResponse[];
}

export interface LobbyStateMessage {
  draftUuid: string;
  draftName: string;
  status: string;
  participantCount: number;
  totalRounds: number;
  participants: ParticipantInfo[];
  allReady: boolean;
  canStart: boolean;
  createdBy?: string;
}

export interface ErrorMessage {
  message: string;
  code: string;
  timestamp: string;
}

export interface ParticipantJoinedMessage {
  draftUuid: string;
  participant: ParticipantInfo;
  message: string;
}

export interface ParticipantLeftMessage {
  draftUuid: string;
  position: string;
  nickname: string;
  message: string;
}

export interface DraftStartedMessage {
  draftUuid: string;
  startedAt: string;
  firstTurnPosition: string;
  message: string;
}

// Request message types
export interface JoinRequest {
  draftUuid: string;
  nickname: string;
  position: string;
}

export interface ReadyRequest {
  draftUuid: string;
  position: string;
  isReady: boolean;
}

export interface MakePickRequest {
  draftUuid: string;
  playerId: number;
  position: string;
}

export interface ForcePickRequest {
  draftUuid: string;
  playerId: number;
  targetPosition: string;
  forcingPosition: string;
}

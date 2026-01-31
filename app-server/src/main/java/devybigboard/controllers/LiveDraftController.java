package devybigboard.controllers;

import devybigboard.exceptions.DraftNotFoundException;
import devybigboard.exceptions.ValidationException;
import devybigboard.models.*;
import devybigboard.services.DraftService;
import devybigboard.services.ParticipantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket controller for handling live draft real-time communication.
 * Manages lobby operations (join, ready, leave, start) and draft operations (pick, force-pick, state).
 * 
 * Uses STOMP protocol over WebSocket with two main topics:
 * - /topic/draft/{draftUuid}/lobby - for lobby state updates
 * - /topic/draft/{draftUuid} - for draft state updates during active draft
 */
@Controller
public class LiveDraftController {
    
    private static final Logger logger = LoggerFactory.getLogger(LiveDraftController.class);
    
    private final DraftService draftService;
    private final ParticipantService participantService;
    private final SimpMessagingTemplate messagingTemplate;
    
    public LiveDraftController(DraftService draftService, 
                              ParticipantService participantService,
                              SimpMessagingTemplate messagingTemplate) {
        this.draftService = draftService;
        this.participantService = participantService;
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Handle participant joining the draft lobby.
     * Validates nickname and position availability, adds participant to lobby,
     * and broadcasts join notification and updated lobby state to all participants.
     * 
     * @param request the join request containing draftUuid, nickname, and position
     * @return error message if validation fails (sent only to requesting user)
     */
    @MessageMapping("/draft/{draftUuid}/join")
    public void joinLobby(@Valid @Payload JoinRequest request) {
        try {
            logger.info("Join request received for draft {} - nickname: {}, position: {}", 
                request.getDraftUuid(), request.getNickname(), request.getPosition());
            
            // Get draft to validate it exists and get its ID
            Draft draft = draftService.getDraftByUuid(request.getDraftUuid());
            
            // Add participant to lobby
            DraftParticipant participant = participantService.joinDraft(
                draft.getId(), 
                request.getNickname(), 
                request.getPosition()
            );
            
            // Create participant info DTO
            ParticipantInfo participantInfo = ParticipantInfo.fromEntity(participant);
            
            // Broadcast participant joined message
            ParticipantJoinedMessage joinedMessage = new ParticipantJoinedMessage(
                request.getDraftUuid(), 
                participantInfo
            );
            messagingTemplate.convertAndSend(
                "/topic/draft/" + request.getDraftUuid() + "/lobby", 
                joinedMessage
            );
            
            // Broadcast updated lobby state
            broadcastLobbyState(request.getDraftUuid());
            
            logger.info("Participant {} joined draft {} at position {}", 
                request.getNickname(), request.getDraftUuid(), request.getPosition());
            
        } catch (DraftNotFoundException | ValidationException | IllegalStateException e) {
            logger.error("Error joining lobby: {}", e.getMessage());
            sendErrorToUser(e.getMessage(), "JOIN_ERROR");
        } catch (Exception e) {
            logger.error("Unexpected error joining lobby", e);
            sendErrorToUser("An unexpected error occurred while joining the lobby", "INTERNAL_ERROR");
        }
    }
    
    /**
     * Handle participant toggling ready status.
     * Updates participant's ready status and broadcasts updated lobby state.
     * 
     * @param request the ready request containing draftUuid, position, and isReady status
     */
    @MessageMapping("/draft/{draftUuid}/ready")
    public void toggleReady(@Valid @Payload ReadyRequest request) {
        try {
            logger.info("Ready toggle request for draft {} - position: {}, ready: {}", 
                request.getDraftUuid(), request.getPosition(), request.getIsReady());
            
            // Get draft to validate it exists and get its ID
            Draft draft = draftService.getDraftByUuid(request.getDraftUuid());
            
            // Update ready status
            participantService.setReady(
                draft.getId(), 
                request.getPosition(), 
                request.getIsReady(),
                request.getPin()
            );
            
            // Broadcast updated lobby state
            broadcastLobbyState(request.getDraftUuid());
            
            logger.info("Participant at position {} set ready to {} in draft {}", 
                request.getPosition(), request.getIsReady(), request.getDraftUuid());
            
        } catch (DraftNotFoundException | ValidationException e) {
            logger.error("Error toggling ready status: {}", e.getMessage());
            sendErrorToUser(e.getMessage(), "READY_ERROR");
        } catch (Exception e) {
            logger.error("Unexpected error toggling ready status", e);
            sendErrorToUser("An unexpected error occurred while updating ready status", "INTERNAL_ERROR");
        }
    }
    
    /**
     * Handle participant leaving the draft lobby.
     * Removes participant from lobby and broadcasts leave notification and updated lobby state.
     * 
     * @param request the join request containing draftUuid and position (reusing JoinRequest for simplicity)
     */
    @MessageMapping("/draft/{draftUuid}/leave")
    public void leaveLobby(@Payload JoinRequest request) {
        try {
            logger.info("Leave request for draft {} - position: {}", 
                request.getDraftUuid(), request.getPosition());
            
            // Get draft to validate it exists and get its ID
            Draft draft = draftService.getDraftByUuid(request.getDraftUuid());
            
            // Get participant info before removing (for the leave message)
            List<DraftParticipant> participants = participantService.getParticipants(draft.getId());
            DraftParticipant leavingParticipant = participants.stream()
                .filter(p -> p.getPosition().equals(request.getPosition()))
                .findFirst()
                .orElse(null);
            
            if (leavingParticipant != null) {
                String nickname = leavingParticipant.getNickname();
                
                // Remove participant from lobby
                participantService.leaveDraft(draft.getId(), request.getPosition());
                
                // Broadcast participant left message
                ParticipantLeftMessage leftMessage = new ParticipantLeftMessage(
                    request.getDraftUuid(), 
                    request.getPosition(), 
                    nickname
                );
                messagingTemplate.convertAndSend(
                    "/topic/draft/" + request.getDraftUuid() + "/lobby", 
                    leftMessage
                );
                
                // Broadcast updated lobby state
                broadcastLobbyState(request.getDraftUuid());
                
                logger.info("Participant {} left draft {} from position {}", 
                    nickname, request.getDraftUuid(), request.getPosition());
            }
            
        } catch (DraftNotFoundException | ValidationException e) {
            logger.error("Error leaving lobby: {}", e.getMessage());
            sendErrorToUser(e.getMessage(), "LEAVE_ERROR");
        } catch (Exception e) {
            logger.error("Unexpected error leaving lobby", e);
            sendErrorToUser("An unexpected error occurred while leaving the lobby", "INTERNAL_ERROR");
        }
    }
    
    /**
     * Handle starting the draft.
     * Validates all participants are ready and requester is the creator,
     * starts the draft, and broadcasts start notification and initial draft state.
     * 
     * @param request the join request containing draftUuid and position (position should be creator's)
     */
    @MessageMapping("/draft/{draftUuid}/start")
    public void startDraft(@Payload JoinRequest request) {
        try {
            logger.info("Start draft request for draft {} from position {}", 
                request.getDraftUuid(), request.getPosition());
            
            // Get draft to validate it exists
            Draft draft = draftService.getDraftByUuid(request.getDraftUuid());
            
            // Validate requester is the creator
            // Get participant at the requesting position
            List<DraftParticipant> participants = participantService.getParticipants(draft.getId());
            DraftParticipant requester = participants.stream()
                .filter(p -> p.getPosition().equals(request.getPosition()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("You must be in the lobby to start the draft"));
            
            // Check if requester's nickname matches the creator
            if (!requester.getNickname().equals(draft.getCreatedBy())) {
                throw new ValidationException("Only the draft creator can start the draft");
            }
            
            // Validate all participants are ready
            if (!draftService.canStartDraft(request.getDraftUuid())) {
                throw new IllegalStateException("Cannot start draft: not all participants are ready");
            }
            
            // Start the draft
            Draft startedDraft = draftService.startDraft(request.getDraftUuid());
            
            // Get first turn position
            String firstTurnPosition = draftService.getCurrentTurn(request.getDraftUuid());
            
            // Broadcast draft started message to lobby
            DraftStartedMessage startedMessage = new DraftStartedMessage(
                request.getDraftUuid(), 
                startedDraft.getStartedAt(), 
                firstTurnPosition
            );
            messagingTemplate.convertAndSend(
                "/topic/draft/" + request.getDraftUuid() + "/lobby", 
                startedMessage
            );
            
            // Broadcast initial draft state to draft topic
            broadcastDraftState(request.getDraftUuid());
            
            logger.info("Draft {} started successfully", request.getDraftUuid());
            
        } catch (DraftNotFoundException | ValidationException | IllegalStateException e) {
            logger.error("Error starting draft: {}", e.getMessage());
            sendErrorToUser(e.getMessage(), "START_ERROR");
        } catch (Exception e) {
            logger.error("Unexpected error starting draft", e);
            sendErrorToUser("An unexpected error occurred while starting the draft", "INTERNAL_ERROR");
        }
    }
    
    /**
     * Handle making a pick during the draft.
     * Validates it's the participant's turn, makes the pick,
     * and broadcasts updated draft state to all participants.
     * 
     * @param request the pick request containing draftUuid, playerId, and position
     */
    @MessageMapping("/draft/{draftUuid}/pick")
    public void makePick(@Valid @Payload MakePickRequest request) {
        try {
            logger.info("Pick request for draft {} - player: {}, position: {}", 
                request.getDraftUuid(), request.getPlayerId(), request.getPosition());
            
            // Validate it's this position's turn
            if (!draftService.isValidPick(request.getDraftUuid(), request.getPosition())) {
                throw new ValidationException("It's not your turn to pick");
            }
            
            // Make the pick
            draftService.makePick(
                request.getDraftUuid(), 
                request.getPlayerId(), 
                request.getPosition()
            );
            
            // Broadcast updated draft state
            broadcastDraftState(request.getDraftUuid());
            
            logger.info("Pick made successfully in draft {} by position {}", 
                request.getDraftUuid(), request.getPosition());
            
        } catch (DraftNotFoundException | ValidationException | IllegalStateException | IllegalArgumentException e) {
            logger.error("Error making pick: {}", e.getMessage());
            sendErrorToUser(e.getMessage(), "PICK_ERROR");
        } catch (Exception e) {
            logger.error("Unexpected error making pick", e);
            sendErrorToUser("An unexpected error occurred while making the pick", "INTERNAL_ERROR");
        }
    }
    
    /**
     * Handle forcing a pick for another position during the draft.
     * Bypasses turn validation, makes the pick with attribution,
     * and broadcasts updated draft state to all participants.
     * 
     * @param request the force pick request containing draftUuid, playerId, targetPosition, and forcingPosition
     */
    @MessageMapping("/draft/{draftUuid}/force-pick")
    public void forcePick(@Valid @Payload ForcePickRequest request) {
        try {
            logger.info("Force pick request for draft {} - player: {}, target: {}, forcing: {}", 
                request.getDraftUuid(), request.getPlayerId(), 
                request.getTargetPosition(), request.getForcingPosition());
            
            // Force the pick (no turn validation)
            draftService.forcePick(
                request.getDraftUuid(), 
                request.getPlayerId(), 
                request.getTargetPosition(), 
                request.getForcingPosition()
            );
            
            // Broadcast updated draft state
            broadcastDraftState(request.getDraftUuid());
            
            logger.info("Force pick made successfully in draft {} by position {} for position {}", 
                request.getDraftUuid(), request.getForcingPosition(), request.getTargetPosition());
            
        } catch (DraftNotFoundException | IllegalStateException | IllegalArgumentException e) {
            logger.error("Error forcing pick: {}", e.getMessage());
            sendErrorToUser(e.getMessage(), "FORCE_PICK_ERROR");
        } catch (Exception e) {
            logger.error("Unexpected error forcing pick", e);
            sendErrorToUser("An unexpected error occurred while forcing the pick", "INTERNAL_ERROR");
        }
    }
    
    /**
     * Handle undoing the last pick in the draft.
     * Removes the most recent pick, reverts draft state,
     * and broadcasts updated draft state to all participants.
     * 
     * @param request simple request containing draftUuid and position
     */
    @MessageMapping("/draft/{draftUuid}/undo")
    public void undoLastPick(@Valid @Payload StateRequest request) {
        try {
            logger.info("Undo pick request for draft {}", request.getDraftUuid());
            
            // Undo the last pick
            draftService.undoLastPick(request.getDraftUuid(), "SYSTEM");
            
            // Broadcast updated draft state
            broadcastDraftState(request.getDraftUuid());
            
            logger.info("Last pick undone successfully in draft {}", request.getDraftUuid());
            
        } catch (DraftNotFoundException | IllegalStateException e) {
            logger.error("Error undoing pick: {}", e.getMessage());
            sendErrorToUser(e.getMessage(), "UNDO_ERROR");
        } catch (Exception e) {
            logger.error("Unexpected error undoing pick", e);
            sendErrorToUser("An unexpected error occurred while undoing the pick", "INTERNAL_ERROR");
        }
    }
    
    /**
     * Handle request for current draft state.
     * Returns complete draft state to the requesting user.
     * Used for reconnection and state synchronization.
     * 
     * @param request simple request containing draftUuid
     * @return the current draft state message
     */
    @MessageMapping("/draft/{draftUuid}/state")
    @SendToUser("/queue/draft-state")
    public DraftStateMessage getDraftState(@Valid @Payload StateRequest request) {
        try {
            logger.info("Draft state request for draft {}", request.getDraftUuid());
            
            // Get complete draft state
            DraftState draftState = draftService.getDraftState(request.getDraftUuid());
            
            // Convert to message format
            return buildDraftStateMessage(draftState);
            
        } catch (DraftNotFoundException e) {
            logger.error("Error getting draft state: {}", e.getMessage());
            sendErrorToUser(e.getMessage(), "STATE_ERROR");
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error getting draft state", e);
            sendErrorToUser("An unexpected error occurred while retrieving draft state", "INTERNAL_ERROR");
            return null;
        }
    }
    
    /**
     * Handle request for current lobby state.
     * Returns complete lobby state to the requesting user.
     * Used for reconnection and state synchronization.
     * 
     * @param request simple request containing draftUuid
     * @return the current lobby state message
     */
    @MessageMapping("/draft/{draftUuid}/lobby/state")
    @SendToUser("/queue/lobby-state")
    public LobbyStateMessage getLobbyState(@Valid @Payload StateRequest request) {
        try {
            logger.info("Lobby state request for draft {}", request.getDraftUuid());
            
            // Get lobby state
            Draft draft = draftService.getLobbyState(request.getDraftUuid());
            
            // Convert to message format
            return buildLobbyStateMessage(draft);
            
        } catch (DraftNotFoundException e) {
            logger.error("Error getting lobby state: {}", e.getMessage());
            sendErrorToUser(e.getMessage(), "STATE_ERROR");
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error getting lobby state", e);
            sendErrorToUser("An unexpected error occurred while retrieving lobby state", "INTERNAL_ERROR");
            return null;
        }
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Broadcast updated lobby state to all participants in the lobby.
     * 
     * @param draftUuid the unique identifier of the draft
     */
    private void broadcastLobbyState(String draftUuid) {
        try {
            Draft draft = draftService.getLobbyState(draftUuid);
            LobbyStateMessage message = buildLobbyStateMessage(draft);
            messagingTemplate.convertAndSend("/topic/draft/" + draftUuid + "/lobby", message);
        } catch (Exception e) {
            logger.error("Error broadcasting lobby state for draft {}", draftUuid, e);
        }
    }
    
    /**
     * Broadcast updated draft state to all participants in the draft.
     * 
     * @param draftUuid the unique identifier of the draft
     */
    private void broadcastDraftState(String draftUuid) {
        try {
            DraftState draftState = draftService.getDraftState(draftUuid);
            DraftStateMessage message = buildDraftStateMessage(draftState);
            messagingTemplate.convertAndSend("/topic/draft/" + draftUuid, message);
        } catch (Exception e) {
            logger.error("Error broadcasting draft state for draft {}", draftUuid, e);
        }
    }
    
    /**
     * Build a LobbyStateMessage from a Draft entity.
     * 
     * @param draft the draft entity
     * @return the lobby state message
     */
    private LobbyStateMessage buildLobbyStateMessage(Draft draft) {
        // Convert participants to DTOs
        List<ParticipantInfo> participantInfos = draft.getParticipants().stream()
            .map(ParticipantInfo::fromEntity)
            .collect(Collectors.toList());
        
        // Check if all participants are ready
        boolean allReady = draft.getParticipants().size() == draft.getParticipantCount() &&
            draft.getParticipants().stream().allMatch(DraftParticipant::getIsReady);
        
        // Check if draft can start
        boolean canStart = draftService.canStartDraft(draft.getUuid());
        
        return new LobbyStateMessage(
            draft.getUuid(),
            draft.getDraftName(),
            draft.getStatus(),
            draft.getParticipantCount(),
            draft.getTotalRounds(),
            participantInfos,
            allReady,
            canStart,
            draft.getCreatedBy()
        );
    }
    
    /**
     * Build a DraftStateMessage from a DraftState object.
     * 
     * @param draftState the draft state object
     * @return the draft state message
     */
    private DraftStateMessage buildDraftStateMessage(DraftState draftState) {
        // Convert participants to DTOs
        List<ParticipantInfo> participantInfos = draftState.getParticipants().stream()
            .map(ParticipantInfo::fromEntity)
            .collect(Collectors.toList());
        
        // Convert picks to DTOs
        List<PickMessage> pickMessages = draftState.getPicks().stream()
            .map(PickMessage::fromEntity)
            .collect(Collectors.toList());
        
        // Convert available players to DTOs
        List<PlayerResponse> availablePlayers = draftState.getAvailablePlayers().stream()
            .map(PlayerResponse::new)
            .collect(Collectors.toList());
        
        return new DraftStateMessage(
            draftState.getUuid(),
            draftState.getStatus(),
            draftState.getCurrentRound(),
            draftState.getCurrentPick(),
            draftState.getCurrentTurnPosition(),
            draftState.getParticipantCount(),
            draftState.getTotalRounds(),
            draftState.getIsSnakeDraft(),
            participantInfos,
            pickMessages,
            availablePlayers
        );
    }
    
    /**
     * Send an error message to the requesting user only.
     * 
     * @param message the error message
     * @param code the error code
     */
    private void sendErrorToUser(String message, String code) {
        ErrorMessage errorMessage = new ErrorMessage(message, code);
        messagingTemplate.convertAndSendToUser(
            "user", 
            "/queue/errors", 
            errorMessage
        );
    }
}

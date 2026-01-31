package devybigboard.controllers;

import devybigboard.exceptions.DraftNotFoundException;
import devybigboard.exceptions.ValidationException;
import devybigboard.models.*;
import devybigboard.services.DraftService;
import devybigboard.services.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LiveDraftController.
 * Tests WebSocket message handling for lobby and draft operations.
 */
@ExtendWith(MockitoExtension.class)
class LiveDraftControllerTest {

    @Mock
    private DraftService draftService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private LiveDraftController controller;

    private Draft testDraft;
    private DraftParticipant testParticipant;

    @BeforeEach
    void setUp() {
        // Create test draft
        testDraft = new Draft();
        testDraft.setId(1L);
        testDraft.setUuid("test-uuid-123");
        testDraft.setDraftName("Test Draft");
        testDraft.setStatus("LOBBY");
        testDraft.setParticipantCount(4);
        testDraft.setTotalRounds(10);
        testDraft.setCreatedBy("Alice");
        testDraft.setCurrentRound(1);
        testDraft.setCurrentPick(1);
        testDraft.setParticipants(new ArrayList<>());

        // Create test participant
        testParticipant = new DraftParticipant(testDraft, "A", "Alice");
        testParticipant.setId(1L);
        testParticipant.setIsReady(false);
        testParticipant.setJoinedAt(LocalDateTime.now());
    }

    @Test
    void testJoinLobby_Success() {
        // Arrange
        JoinRequest request = new JoinRequest("test-uuid-123", "Alice", "A");
        testDraft.getParticipants().add(testParticipant);
        
        when(draftService.getDraftByUuid("test-uuid-123")).thenReturn(testDraft);
        when(participantService.joinDraft(1L, "Alice", "A")).thenReturn(testParticipant);
        when(draftService.getLobbyState("test-uuid-123")).thenReturn(testDraft);
        when(draftService.canStartDraft("test-uuid-123")).thenReturn(false);

        // Act
        controller.joinLobby(request);

        // Assert
        verify(draftService).getDraftByUuid("test-uuid-123");
        verify(participantService).joinDraft(1L, "Alice", "A");
        verify(messagingTemplate).convertAndSend(eq("/topic/draft/test-uuid-123/lobby"), any(ParticipantJoinedMessage.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/draft/test-uuid-123/lobby"), any(LobbyStateMessage.class));
    }

    @Test
    void testJoinLobby_DraftNotFound() {
        // Arrange
        JoinRequest request = new JoinRequest("invalid-uuid", "Alice", "A");
        when(draftService.getDraftByUuid("invalid-uuid"))
            .thenThrow(new DraftNotFoundException("invalid-uuid"));

        // Act
        controller.joinLobby(request);

        // Assert
        verify(draftService).getDraftByUuid("invalid-uuid");
        verify(participantService, never()).joinDraft(anyLong(), anyString(), anyString());
        verify(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(ErrorMessage.class));
    }

    @Test
    void testJoinLobby_PositionTaken() {
        // Arrange
        JoinRequest request = new JoinRequest("test-uuid-123", "Bob", "A");
        when(draftService.getDraftByUuid("test-uuid-123")).thenReturn(testDraft);
        when(participantService.joinDraft(1L, "Bob", "A"))
            .thenThrow(new ValidationException("Position A is already taken"));

        // Act
        controller.joinLobby(request);

        // Assert
        verify(participantService).joinDraft(1L, "Bob", "A");
        verify(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(ErrorMessage.class));
    }

    @Test
    void testToggleReady_Success() {
        // Arrange
        ReadyRequest request = new ReadyRequest("test-uuid-123", "A", true);
        testDraft.getParticipants().add(testParticipant);
        
        when(draftService.getDraftByUuid("test-uuid-123")).thenReturn(testDraft);
        when(participantService.setReady(1L, "A", true)).thenReturn(testParticipant);
        when(draftService.getLobbyState("test-uuid-123")).thenReturn(testDraft);
        when(draftService.canStartDraft("test-uuid-123")).thenReturn(false);

        // Act
        controller.toggleReady(request);

        // Assert
        verify(draftService).getDraftByUuid("test-uuid-123");
        verify(participantService).setReady(1L, "A", true);
        verify(messagingTemplate).convertAndSend(anyString(), any(LobbyStateMessage.class));
    }

    @Test
    void testLeaveLobby_Success() {
        // Arrange
        JoinRequest request = new JoinRequest("test-uuid-123", "Alice", "A");
        List<DraftParticipant> participants = List.of(testParticipant);
        testDraft.getParticipants().add(testParticipant);
        
        when(draftService.getDraftByUuid("test-uuid-123")).thenReturn(testDraft);
        when(participantService.getParticipants(1L)).thenReturn(participants);
        when(draftService.getLobbyState("test-uuid-123")).thenReturn(testDraft);
        when(draftService.canStartDraft("test-uuid-123")).thenReturn(false);

        // Act
        controller.leaveLobby(request);

        // Assert
        verify(draftService).getDraftByUuid("test-uuid-123");
        verify(participantService).getParticipants(1L);
        verify(participantService).leaveDraft(1L, "A");
        verify(messagingTemplate).convertAndSend(eq("/topic/draft/test-uuid-123/lobby"), any(ParticipantLeftMessage.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/draft/test-uuid-123/lobby"), any(LobbyStateMessage.class));
    }

    @Test
    void testStartDraft_Success() {
        // Arrange
        JoinRequest request = new JoinRequest("test-uuid-123", "Alice", "A");
        testParticipant.setIsReady(true);
        testDraft.getParticipants().add(testParticipant);
        
        Draft startedDraft = new Draft();
        startedDraft.setUuid("test-uuid-123");
        startedDraft.setStatus("IN_PROGRESS");
        startedDraft.setStartedAt(LocalDateTime.now());
        startedDraft.setParticipants(new ArrayList<>());
        startedDraft.getParticipants().add(testParticipant);
        startedDraft.setPicks(new ArrayList<>());
        
        DraftState draftState = new DraftState(
            "test-uuid-123",
            "Test Draft",
            "IN_PROGRESS",
            1,
            1,
            10,
            4,
            "A",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            LocalDateTime.now(),
            null
        );

        when(draftService.getDraftByUuid("test-uuid-123")).thenReturn(testDraft);
        when(participantService.getParticipants(1L)).thenReturn(List.of(testParticipant));
        when(draftService.canStartDraft("test-uuid-123")).thenReturn(true);
        when(draftService.startDraft("test-uuid-123")).thenReturn(startedDraft);
        when(draftService.getCurrentTurn("test-uuid-123")).thenReturn("A");
        when(draftService.getDraftState("test-uuid-123")).thenReturn(draftState);

        // Act
        controller.startDraft(request);

        // Assert
        verify(draftService).canStartDraft("test-uuid-123");
        verify(draftService).startDraft("test-uuid-123");
        verify(messagingTemplate).convertAndSend(eq("/topic/draft/test-uuid-123/lobby"), any(DraftStartedMessage.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/draft/test-uuid-123"), any(DraftStateMessage.class));
    }

    @Test
    void testStartDraft_NotCreator() {
        // Arrange
        JoinRequest request = new JoinRequest("test-uuid-123", "Bob", "B");
        DraftParticipant bobParticipant = new DraftParticipant(testDraft, "B", "Bob");
        
        when(draftService.getDraftByUuid("test-uuid-123")).thenReturn(testDraft);
        when(participantService.getParticipants(1L)).thenReturn(List.of(bobParticipant));

        // Act
        controller.startDraft(request);

        // Assert
        verify(draftService, never()).startDraft(anyString());
        verify(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(ErrorMessage.class));
    }

    @Test
    void testMakePick_Success() {
        // Arrange
        MakePickRequest request = new MakePickRequest("test-uuid-123", 100L, "A");
        DraftState draftState = new DraftState(
            "test-uuid-123",
            "Test Draft",
            "IN_PROGRESS",
            1,
            2,
            10,
            4,
            "B",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            LocalDateTime.now(),
            null
        );
        
        when(draftService.isValidPick("test-uuid-123", "A")).thenReturn(true);
        when(draftService.makePick("test-uuid-123", 100L, "A")).thenReturn(testDraft);
        when(draftService.getDraftState("test-uuid-123")).thenReturn(draftState);

        // Act
        controller.makePick(request);

        // Assert
        verify(draftService).isValidPick("test-uuid-123", "A");
        verify(draftService).makePick("test-uuid-123", 100L, "A");
        verify(messagingTemplate).convertAndSend(anyString(), any(DraftStateMessage.class));
    }

    @Test
    void testMakePick_NotYourTurn() {
        // Arrange
        MakePickRequest request = new MakePickRequest("test-uuid-123", 100L, "B");
        when(draftService.isValidPick("test-uuid-123", "B")).thenReturn(false);

        // Act
        controller.makePick(request);

        // Assert
        verify(draftService).isValidPick("test-uuid-123", "B");
        verify(draftService, never()).makePick(anyString(), anyLong(), anyString());
        verify(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(ErrorMessage.class));
    }

    @Test
    void testForcePick_Success() {
        // Arrange
        ForcePickRequest request = new ForcePickRequest("test-uuid-123", 100L, "B", "A");
        DraftState draftState = new DraftState(
            "test-uuid-123",
            "Test Draft",
            "IN_PROGRESS",
            1,
            2,
            10,
            4,
            "C",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            LocalDateTime.now(),
            null
        );
        
        when(draftService.forcePick("test-uuid-123", 100L, "B", "A")).thenReturn(testDraft);
        when(draftService.getDraftState("test-uuid-123")).thenReturn(draftState);

        // Act
        controller.forcePick(request);

        // Assert
        verify(draftService).forcePick("test-uuid-123", 100L, "B", "A");
        verify(messagingTemplate).convertAndSend(anyString(), any(DraftStateMessage.class));
    }

    @Test
    void testGetDraftState_Success() {
        // Arrange
        StateRequest request = new StateRequest("test-uuid-123");
        DraftState draftState = new DraftState(
            "test-uuid-123",
            "Test Draft",
            "IN_PROGRESS",
            1,
            1,
            10,
            4,
            "A",
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            LocalDateTime.now(),
            null
        );
        when(draftService.getDraftState("test-uuid-123")).thenReturn(draftState);

        // Act
        DraftStateMessage result = controller.getDraftState(request);

        // Assert
        assertNotNull(result);
        assertEquals("test-uuid-123", result.getDraftUuid());
        assertEquals("IN_PROGRESS", result.getStatus());
        verify(draftService).getDraftState("test-uuid-123");
    }

    @Test
    void testGetLobbyState_Success() {
        // Arrange
        StateRequest request = new StateRequest("test-uuid-123");
        when(draftService.getLobbyState("test-uuid-123")).thenReturn(testDraft);

        // Act
        LobbyStateMessage result = controller.getLobbyState(request);

        // Assert
        assertNotNull(result);
        assertEquals("test-uuid-123", result.getDraftUuid());
        assertEquals("LOBBY", result.getStatus());
        verify(draftService).getLobbyState("test-uuid-123");
    }
}

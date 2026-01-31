package devybigboard.controllers;

import devybigboard.exceptions.DraftNotFoundException;
import devybigboard.models.*;
import devybigboard.services.DraftService;
import devybigboard.services.ExportService;
import devybigboard.services.DevyBoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApiController live draft REST endpoints.
 * Tests POST /api/live-drafts and GET /api/drafts/{uuid}/lobby.
 */
@ExtendWith(MockitoExtension.class)
class ApiControllerLiveDraftTest {

    @Mock
    private DevyBoardService devyBoardService;

    @Mock
    private DraftService draftService;

    @Mock
    private ExportService exportService;

    @InjectMocks
    private ApiController controller;

    private Draft testDraft;
    private DraftParticipant testParticipant;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Create test draft
        testDraft = new Draft();
        testDraft.setId(1L);
        testDraft.setUuid("test-uuid-123");
        testDraft.setDraftName("Test Live Draft");
        testDraft.setStatus("LOBBY");
        testDraft.setParticipantCount(4);
        testDraft.setTotalRounds(10);
        testDraft.setCreatedBy("Alice");
        testDraft.setCurrentRound(1);
        testDraft.setCurrentPick(1);
        testDraft.setParticipants(new ArrayList<>());
        testDraft.setCreatedAt(LocalDateTime.now());

        // Create test participant
        testParticipant = new DraftParticipant(testDraft, "A", "Alice");
        testParticipant.setId(1L);
        testParticipant.setIsReady(false);
        testParticipant.setJoinedAt(LocalDateTime.now());

        // Create mock HTTP request
        mockRequest = new MockHttpServletRequest();
        mockRequest.setScheme("http");
        mockRequest.setServerName("localhost");
        mockRequest.setServerPort(8080);
    }

    @Test
    void testCreateLiveDraft_Success() {
        // Arrange
        CreateLiveDraftRequest request = new CreateLiveDraftRequest(
            "Test Live Draft",
            "Alice",
            4,
            10
        );

        when(draftService.createLiveDraft("Test Live Draft", "Alice", 4, 10, false))
            .thenReturn(testDraft);

        // Act
        LiveDraftResponse response = controller.createLiveDraft(request, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test-uuid-123", response.getUuid());
        assertEquals("Test Live Draft", response.getDraftName());
        assertEquals("LOBBY", response.getStatus());
        assertEquals(4, response.getParticipantCount());
        assertEquals(10, response.getTotalRounds());
        assertEquals("Alice", response.getCreatedBy());
        assertEquals("http://localhost:8080/draft/test-uuid-123/lobby", response.getLobbyUrl());

        verify(draftService).createLiveDraft("Test Live Draft", "Alice", 4, 10, false);
    }

    @Test
    void testCreateLiveDraft_WithHttpsAndCustomPort() {
        // Arrange
        CreateLiveDraftRequest request = new CreateLiveDraftRequest(
            "Test Live Draft",
            "Alice",
            4,
            10
        );

        mockRequest.setScheme("https");
        mockRequest.setServerName("example.com");
        mockRequest.setServerPort(443);

        when(draftService.createLiveDraft("Test Live Draft", "Alice", 4, 10, false))
            .thenReturn(testDraft);

        // Act
        LiveDraftResponse response = controller.createLiveDraft(request, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals("https://example.com/draft/test-uuid-123/lobby", response.getLobbyUrl());
    }

    @Test
    void testGetLobbyState_Success_NoParticipants() {
        // Arrange
        when(draftService.getLobbyState("test-uuid-123")).thenReturn(testDraft);
        when(draftService.canStartDraft("test-uuid-123")).thenReturn(false);

        // Act
        LobbyStateMessage response = controller.getLobbyState("test-uuid-123");

        // Assert
        assertNotNull(response);
        assertEquals("test-uuid-123", response.getDraftUuid());
        assertEquals("Test Live Draft", response.getDraftName());
        assertEquals("LOBBY", response.getStatus());
        assertEquals(4, response.getParticipantCount());
        assertEquals(10, response.getTotalRounds());
        assertTrue(response.getParticipants().isEmpty());
        assertFalse(response.getAllReady());
        assertFalse(response.getCanStart());

        verify(draftService).getLobbyState("test-uuid-123");
        verify(draftService).canStartDraft("test-uuid-123");
    }

    @Test
    void testGetLobbyState_Success_WithParticipants() {
        // Arrange
        testDraft.getParticipants().add(testParticipant);
        testParticipant.setIsReady(true);

        when(draftService.getLobbyState("test-uuid-123")).thenReturn(testDraft);
        when(draftService.canStartDraft("test-uuid-123")).thenReturn(false);

        // Act
        LobbyStateMessage response = controller.getLobbyState("test-uuid-123");

        // Assert
        assertNotNull(response);
        assertEquals("test-uuid-123", response.getDraftUuid());
        assertEquals(1, response.getParticipants().size());
        
        ParticipantInfo participant = response.getParticipants().get(0);
        assertEquals("A", participant.getPosition());
        assertEquals("Alice", participant.getNickname());
        assertTrue(participant.getIsReady());
        
        assertTrue(response.getAllReady());
        assertFalse(response.getCanStart());

        verify(draftService).getLobbyState("test-uuid-123");
        verify(draftService).canStartDraft("test-uuid-123");
    }

    @Test
    void testGetLobbyState_Success_AllReadyCanStart() {
        // Arrange
        // Add 4 participants (all ready)
        for (int i = 0; i < 4; i++) {
            char position = (char) ('A' + i);
            DraftParticipant participant = new DraftParticipant(testDraft, String.valueOf(position), "Player" + position);
            participant.setIsReady(true);
            participant.setJoinedAt(LocalDateTime.now());
            testDraft.getParticipants().add(participant);
        }

        when(draftService.getLobbyState("test-uuid-123")).thenReturn(testDraft);
        when(draftService.canStartDraft("test-uuid-123")).thenReturn(true);

        // Act
        LobbyStateMessage response = controller.getLobbyState("test-uuid-123");

        // Assert
        assertNotNull(response);
        assertEquals(4, response.getParticipants().size());
        assertTrue(response.getAllReady());
        assertTrue(response.getCanStart());

        verify(draftService).getLobbyState("test-uuid-123");
        verify(draftService).canStartDraft("test-uuid-123");
    }

    @Test
    void testGetLobbyState_DraftNotFound() {
        // Arrange
        when(draftService.getLobbyState("invalid-uuid"))
            .thenThrow(new DraftNotFoundException("invalid-uuid"));

        // Act & Assert
        assertThrows(DraftNotFoundException.class, () -> {
            controller.getLobbyState("invalid-uuid");
        });

        verify(draftService).getLobbyState("invalid-uuid");
        verify(draftService, never()).canStartDraft(anyString());
    }

    @Test
    void testGetLobbyState_MixedReadyStatus() {
        // Arrange
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        participant1.setIsReady(true);
        participant1.setJoinedAt(LocalDateTime.now());
        
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");
        participant2.setIsReady(false);
        participant2.setJoinedAt(LocalDateTime.now());
        
        testDraft.getParticipants().add(participant1);
        testDraft.getParticipants().add(participant2);

        when(draftService.getLobbyState("test-uuid-123")).thenReturn(testDraft);
        when(draftService.canStartDraft("test-uuid-123")).thenReturn(false);

        // Act
        LobbyStateMessage response = controller.getLobbyState("test-uuid-123");

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getParticipants().size());
        assertFalse(response.getAllReady()); // Not all ready
        assertFalse(response.getCanStart());

        verify(draftService).getLobbyState("test-uuid-123");
        verify(draftService).canStartDraft("test-uuid-123");
    }
}


package devybigboard.services;

import devybigboard.dao.DraftRepository;
import devybigboard.models.Draft;
import devybigboard.models.DraftParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyCleanupServiceTest {
    
    @Mock
    private DraftRepository draftRepository;
    
    @InjectMocks
    private LobbyCleanupService lobbyCleanupService;
    
    private Draft staleLobby;
    private Draft recentLobby;
    
    @BeforeEach
    void setUp() {
        staleLobby = new Draft();
        staleLobby.setUuid("stale-uuid");
        staleLobby.setDraftName("Stale Lobby");
        staleLobby.setStatus("LOBBY");
        staleLobby.setCreatedAt(LocalDateTime.now().minusHours(2));
        staleLobby.setStartedAt(null);
        
        DraftParticipant participant = new DraftParticipant(staleLobby, "A", "Player1");
        staleLobby.addParticipant(participant);
        
        recentLobby = new Draft();
        recentLobby.setUuid("recent-uuid");
        recentLobby.setDraftName("Recent Lobby");
        recentLobby.setStatus("LOBBY");
        recentLobby.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        recentLobby.setStartedAt(null);
    }
    
    @Test
    void cleanupStaleLobbies_shouldDeleteStaleLobbies() {
        when(draftRepository.findStaleLobbyDrafts(eq("LOBBY"), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(staleLobby));
        
        lobbyCleanupService.cleanupStaleLobbies();
        
        verify(draftRepository).findStaleLobbyDrafts(eq("LOBBY"), any(LocalDateTime.class));
        verify(draftRepository).delete(staleLobby);
    }
    
    @Test
    void cleanupStaleLobbies_shouldHandleNoStaleLobbies() {
        when(draftRepository.findStaleLobbyDrafts(eq("LOBBY"), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        
        lobbyCleanupService.cleanupStaleLobbies();
        
        verify(draftRepository).findStaleLobbyDrafts(eq("LOBBY"), any(LocalDateTime.class));
        verify(draftRepository, never()).delete(any());
    }
    
    @Test
    void cleanupStaleLobbies_shouldHandleMultipleStaleLobbies() {
        Draft anotherStaleLobby = new Draft();
        anotherStaleLobby.setUuid("another-stale-uuid");
        anotherStaleLobby.setDraftName("Another Stale Lobby");
        anotherStaleLobby.setStatus("LOBBY");
        anotherStaleLobby.setCreatedAt(LocalDateTime.now().minusHours(3));
        
        when(draftRepository.findStaleLobbyDrafts(eq("LOBBY"), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(staleLobby, anotherStaleLobby));
        
        lobbyCleanupService.cleanupStaleLobbies();
        
        verify(draftRepository).delete(staleLobby);
        verify(draftRepository).delete(anotherStaleLobby);
    }
    
    @Test
    void cleanupStaleLobbies_shouldContinueOnError() {
        Draft errorLobby = new Draft();
        errorLobby.setUuid("error-uuid");
        errorLobby.setDraftName("Error Lobby");
        errorLobby.setStatus("LOBBY");
        errorLobby.setCreatedAt(LocalDateTime.now().minusHours(2));
        
        when(draftRepository.findStaleLobbyDrafts(eq("LOBBY"), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(staleLobby, errorLobby));
        
        doThrow(new RuntimeException("Database error")).when(draftRepository).delete(staleLobby);
        
        lobbyCleanupService.cleanupStaleLobbies();
        
        verify(draftRepository).delete(staleLobby);
        verify(draftRepository).delete(errorLobby);
    }
}

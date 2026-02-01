package devybigboard.controllers;

import devybigboard.models.CompletedDraftResponse;
import devybigboard.models.LeagueFilter;
import devybigboard.models.Player;
import devybigboard.models.PlayerWithAdp;
import devybigboard.services.DevyBoardService;
import devybigboard.services.DraftService;
import devybigboard.services.ExportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final DevyBoardService devyBoardService;
    private final DraftService draftService;
    private final ExportService exportService;

    public ApiController(DevyBoardService devyBoardService, DraftService draftService, ExportService exportService) {
        this.devyBoardService = devyBoardService;
        this.draftService = draftService;
        this.exportService = exportService;
    }

    @GetMapping("/draft/count")
    public Integer draftsCompletedCount() {
        return draftService.draftsCompletedCount();
    }

    @GetMapping("/players")
    public List<PlayerWithAdp> allPlayers() {
        return devyBoardService.getAllPlayers();
    }

    @PostMapping("/draft/complete")
    public String draftComplete(@RequestParam(defaultValue = "offline") String draftType,
                                @RequestBody List<Player> draftedPlayers) {
       return devyBoardService.saveDraftAdpResults(draftType, draftedPlayers);
    }

    @GetMapping("draft/{uuid}")
    public CompletedDraftResponse getDraftByUuid(@PathVariable String uuid) {
        return devyBoardService.getDraftByUuid(uuid);
    }

    @GetMapping("/filters")
    public List<LeagueFilter> getAllFilters() {
        return draftService.getAllLeagueFilters();
    }

    @PostMapping("/filters")
    public long createFilter(@RequestBody String leagueName) {
        return draftService.createLeagueFilter(leagueName);
    }

    @PostMapping("/filters/{filterId}/add")
    public void addPlayerToFilter(
            @PathVariable long filterId,
            @RequestBody Player player
    ) {
        draftService.addPlayerToFilter(filterId, player);
    }

    @PostMapping("/filters/{filterId}/remove")
    public void removePlayerFromFilter(
            @PathVariable long filterId,
            @RequestBody Player player
    ) {
        draftService.removePlayerFromFilter(filterId, player);
    }

    @GetMapping("/players/filter/{filterId}")
    public List<PlayerWithAdp> getPlayersExcludingFilter(@PathVariable long filterId) {
        return devyBoardService.getPlayersExcludingFilter(filterId);
    }

    @DeleteMapping("/filters/{filterId}")
    public void deleteFilter(@PathVariable long filterId) {
        draftService.deleteLeagueFilter(filterId);
    }

    /**
     * Save a completed draft.
     * POST /api/drafts
     * 
     * @param draftDTO the draft data
     * @param request the HTTP servlet request to extract base URL
     * @return 201 Created with the saved draft data including UUID and shareable URL
     */
    @PostMapping("/drafts")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public devybigboard.models.DraftResponse saveDraft(
            @jakarta.validation.Valid @RequestBody devybigboard.models.DraftDTO draftDTO,
            jakarta.servlet.http.HttpServletRequest request) {
        devybigboard.models.Draft draft = draftService.saveDraft(draftDTO);
        
        // Construct shareable URL
        String baseUrl = getBaseUrl(request);
        String shareableUrl = baseUrl + "/drafts/" + draft.getUuid();
        
        devybigboard.models.DraftResponse response = new devybigboard.models.DraftResponse(draft);
        response.setShareableUrl(shareableUrl);
        
        return response;
    }
    
    /**
     * Helper method to construct the base URL from the request.
     * 
     * @param request the HTTP servlet request
     * @return the base URL (e.g., "http://localhost:8080" or "https://example.com")
     */
    private String getBaseUrl(jakarta.servlet.http.HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        
        // Only include port if it's not the default for the scheme
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }
        
        return baseUrl.toString();
    }
    
    /**
     * Get a draft by UUID.
     * GET /api/drafts/{uuid}
     * 
     * @param uuid the draft UUID
     * @return 200 OK with the draft data
     * @throws devybigboard.exceptions.DraftNotFoundException if draft does not exist (returns 404)
     */
    @GetMapping("/drafts/{uuid}")
    public devybigboard.models.DraftResponse getDraftByUuidNew(@PathVariable String uuid) {
        devybigboard.models.Draft draft = draftService.getDraftByUuid(uuid);
        return new devybigboard.models.DraftResponse(draft);
    }

    /**
     * Export draft to CSV format.
     * GET /api/drafts/{uuid}/export/csv
     * 
     * @param uuid the draft UUID
     * @return CSV file with draft data
     * @throws devybigboard.exceptions.DraftNotFoundException if draft does not exist (returns 404)
     */
    @GetMapping("/drafts/{uuid}/export/csv")
    public org.springframework.http.ResponseEntity<byte[]> exportDraftToCSV(@PathVariable String uuid) {
        devybigboard.models.Draft draft = draftService.getDraftByUuid(uuid);
        byte[] csv = exportService.exportToCSV(draft);
        
        return org.springframework.http.ResponseEntity.ok()
            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=draft-" + uuid + ".csv")
            .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
            .body(csv);
    }
    
    /**
     * Export draft to JSON format.
     * GET /api/drafts/{uuid}/export/json
     * 
     * @param uuid the draft UUID
     * @return JSON document with complete draft data
     * @throws devybigboard.exceptions.DraftNotFoundException if draft does not exist (returns 404)
     */
    @GetMapping("/drafts/{uuid}/export/json")
    public org.springframework.http.ResponseEntity<String> exportDraftToJSON(@PathVariable String uuid) {
        devybigboard.models.Draft draft = draftService.getDraftByUuid(uuid);
        String json = exportService.exportToJSON(draft);
        
        return org.springframework.http.ResponseEntity.ok()
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .body(json);
    }
    
    /**
     * Export draft to PDF format.
     * GET /api/drafts/{uuid}/export/pdf
     * 
     * @param uuid the draft UUID
     * @return PDF file with printable draft results
     * @throws devybigboard.exceptions.DraftNotFoundException if draft does not exist (returns 404)
     */
    @GetMapping("/drafts/{uuid}/export/pdf")
    public org.springframework.http.ResponseEntity<byte[]> exportDraftToPDF(@PathVariable String uuid) {
        devybigboard.models.Draft draft = draftService.getDraftByUuid(uuid);
        byte[] pdf = exportService.exportToPDF(draft);
        
        return org.springframework.http.ResponseEntity.ok()
            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=draft-" + uuid + ".pdf")
            .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
            .body(pdf);
    }
    
    // ========== Live Draft REST Endpoints ==========
    
    /**
     * Create a new live draft.
     * POST /api/live-drafts
     * 
     * @param request the live draft creation request
     * @param servletRequest the HTTP servlet request to extract base URL
     * @return 201 Created with the draft details and lobby URL
     */
    @PostMapping("/live-drafts")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public devybigboard.models.LiveDraftResponse createLiveDraft(
            @jakarta.validation.Valid @RequestBody devybigboard.models.CreateLiveDraftRequest request,
            jakarta.servlet.http.HttpServletRequest servletRequest) {
        
        // Create the live draft
        devybigboard.models.Draft draft = draftService.createLiveDraft(
            request.getDraftName(),
            request.getCreatorNickname(),
            request.getParticipantCount(),
            request.getTotalRounds(),
            request.getPin(),
            request.getIsSnakeDraft()
        );
        
        // Construct lobby URL
        String baseUrl = getBaseUrl(servletRequest);
        String lobbyUrl = baseUrl + "/draft/" + draft.getUuid() + "/lobby";
        
        // Return response with lobby URL
        return new devybigboard.models.LiveDraftResponse(draft, lobbyUrl);
    }
    
    /**
     * Get lobby state for a draft.
     * GET /api/drafts/{uuid}/lobby
     * 
     * This is a REST fallback for retrieving lobby state.
     * Real-time updates should use WebSocket subscriptions.
     * 
     * @param uuid the draft UUID
     * @return 200 OK with lobby state including participants and ready status
     * @throws devybigboard.exceptions.DraftNotFoundException if draft does not exist (returns 404)
     */
    @GetMapping("/drafts/{uuid}/lobby")
    public devybigboard.models.LobbyStateMessage getLobbyState(@PathVariable String uuid) {
        // Get draft with participants
        devybigboard.models.Draft draft = draftService.getLobbyState(uuid);
        
        // Convert participants to ParticipantInfo list
        java.util.List<devybigboard.models.ParticipantInfo> participantInfos = 
            draft.getParticipants().stream()
                .map(devybigboard.models.ParticipantInfo::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        
        // Check if all participants are ready
        boolean allReady = !draft.getParticipants().isEmpty() && 
            draft.getParticipants().stream().allMatch(devybigboard.models.DraftParticipant::getIsReady);
        
        // Check if draft can start
        boolean canStart = draftService.canStartDraft(uuid);
        
        // Build and return lobby state message
        return new devybigboard.models.LobbyStateMessage(
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
     * Get share link for a draft with PIN included.
     * GET /api/drafts/{uuid}/share-link
     * 
     * @param uuid the unique identifier of the draft
     * @param servletRequest the HTTP servlet request to extract base URL
     * @return 200 OK with share URL including PIN
     */
    @GetMapping("/drafts/{uuid}/share-link")
    public java.util.Map<String, String> getShareLink(
            @PathVariable String uuid,
            jakarta.servlet.http.HttpServletRequest servletRequest) {
        
        devybigboard.models.Draft draft = draftService.getDraftByUuid(uuid);
        String baseUrl = getBaseUrl(servletRequest);
        
        // Build share URL with PIN
        String shareUrl = baseUrl + "/draft/" + draft.getUuid() + "/lobby";
        if (draft.getPin() != null && !draft.getPin().isEmpty()) {
            shareUrl += "?pin=" + draft.getPin();
        }
        
        return java.util.Map.of("shareUrl", shareUrl);
    }
    
    /**
     * Get all drafts currently in LOBBY status.
     * GET /api/live-drafts/lobbies
     * 
     * @param servletRequest the HTTP servlet request to extract base URL
     * @return 200 OK with list of lobby drafts including join URLs (PIN excluded for security)
     */
    @GetMapping("/live-drafts/lobbies")
    public java.util.List<devybigboard.models.LobbyListResponse> getAllLobbyDrafts(
            jakarta.servlet.http.HttpServletRequest servletRequest) {
        
        java.util.List<devybigboard.models.Draft> lobbyDrafts = draftService.getAllLobbyDrafts();
        String baseUrl = getBaseUrl(servletRequest);
        
        return lobbyDrafts.stream()
            .map(draft -> {
                String lobbyUrl = baseUrl + "/draft/" + draft.getUuid() + "/lobby";
                return new devybigboard.models.LobbyListResponse(draft, lobbyUrl);
            })
            .collect(java.util.stream.Collectors.toList());
    }

}
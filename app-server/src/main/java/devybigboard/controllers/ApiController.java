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

}
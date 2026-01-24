package devybigboard.controllers;

import devybigboard.models.CompletedDraftResponse;
import devybigboard.models.LeagueFilter;
import devybigboard.models.Player;
import devybigboard.models.PlayerWithAdp;
import devybigboard.services.DevyBoardService;
import devybigboard.services.DraftService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final DevyBoardService devyBoardService;
    private final DraftService draftService;

    public ApiController(DevyBoardService devyBoardService, DraftService draftService) {
        this.devyBoardService = devyBoardService;
        this.draftService = draftService;
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
     * @return 201 Created with the saved draft data including UUID
     */
    @PostMapping("/drafts")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public devybigboard.models.DraftResponse saveDraft(@jakarta.validation.Valid @RequestBody devybigboard.models.DraftDTO draftDTO) {
        devybigboard.models.Draft draft = draftService.saveDraft(draftDTO);
        return new devybigboard.models.DraftResponse(draft);
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

}
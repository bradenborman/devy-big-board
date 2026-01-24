package devybigboard.services;

import devybigboard.dao.DraftDao;
import devybigboard.dao.PlayerDao;
import devybigboard.models.CompletedDraftResponse;
import devybigboard.models.Player;
import devybigboard.models.PlayerWithAdp;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DevyBoardService {

    private final DraftDao draftDao;
    private final PlayerDao playerDao;

    public DevyBoardService(DraftDao draftDao, PlayerDao playerDao) {
        this.draftDao = draftDao;
        this.playerDao = playerDao;
    }

    public String saveDraftAdpResults(String draftType, List<Player> draftedPlayers) {
        List<PlayerWithAdp> playersWithAdp = new ArrayList<>();
        for (int i = 0; i < draftedPlayers.size(); i++) {
            Player p = draftedPlayers.get(i);
            playersWithAdp.add(new PlayerWithAdp(
                    p.name(),
                    p.position(),
                    p.team(),
                    p.draftyear(),
                    i + 1
            ));
        }

        long draftId = draftDao.createDraft(draftType);
        playersWithAdp.forEach(playerWithAdp -> draftDao.insertDraftPickResult(draftId, playerWithAdp));
        return draftDao.queryForUUID(draftId);
    }

    public List<PlayerWithAdp> getAllPlayers() {
        return playerDao.getAllPlayers();
    }

    public CompletedDraftResponse getDraftByUuid(String uuid) {
        return draftDao.draftByUUID(uuid);
    }

    public List<PlayerWithAdp> getPlayersExcludingFilter(long filterId) {
        return playerDao.getPlayersExcludingFilter(filterId);
    }

}
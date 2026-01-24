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

    // TODO: Refactor these methods to work with new JPA entities
    // These methods use the old DAO pattern and Player record structure
    // Will be reimplemented with Spring Data JPA repositories in later tasks
    
    public String saveDraftAdpResults(String draftType, List<Player> draftedPlayers) {
        throw new UnsupportedOperationException("Will be reimplemented with JPA repositories");
    }

    public List<PlayerWithAdp> getAllPlayers() {
        return playerDao.getAllPlayers();
    }

    public CompletedDraftResponse getDraftByUuid(String uuid) {
        throw new UnsupportedOperationException("Will be reimplemented with JPA repositories");
    }

    public List<PlayerWithAdp> getPlayersExcludingFilter(long filterId) {
        return playerDao.getPlayersExcludingFilter(filterId);
    }

}
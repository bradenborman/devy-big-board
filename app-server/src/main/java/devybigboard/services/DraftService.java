package devybigboard.services;

import devybigboard.dao.DraftDao;
import devybigboard.models.LeagueFilter;
import devybigboard.models.Player;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftService {

    private final DraftDao draftDao;

    public DraftService(DraftDao draftDao) {
        this.draftDao = draftDao;
    }

    public Integer draftsCompletedCount() {
        return draftDao.draftsCompletedCount();
    }

    public List<LeagueFilter> getAllLeagueFilters() {
        return draftDao.getAllLeagueFilters();
    }

    public long createLeagueFilter(String leagueName) {
        return draftDao.createFilter(leagueName);
    }

    public void addPlayerToFilter(long filterId, Player player) {
        draftDao.addPlayerToFilter(filterId, player.name(), player.position(), player.team());
    }

    public void removePlayerFromFilter(long filterId, Player player) {
        draftDao.removePlayerFromFilter(filterId, player.name(), player.position(), player.team());
    }

    public void deleteLeagueFilter(long filterId) {
        draftDao.deleteFilter(filterId);
    }

}
package ptit.e23cnpm01.pikachu.server.dao;

import java.util.ArrayList;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;

/**
 * Truy vấn bảng xếp hạng và tổng điểm.
 *
 * <p>Phụ trách: Dev 2.
 */
public class PlayerRankingDAO extends DAO {
    public PlayerRankingDAO() {
        // TODO
    }

    public ArrayList<PlayerRanking> getRanking() {
        throw new UnsupportedOperationException("TODO");
    }

    public ArrayList<PlayerRanking> getTotalPoints(ArrayList<Player> listPlayer) {
        throw new UnsupportedOperationException("TODO");
    }
}

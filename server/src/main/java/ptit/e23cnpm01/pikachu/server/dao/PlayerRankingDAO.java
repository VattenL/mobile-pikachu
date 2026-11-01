package ptit.e23cnpm01.pikachu.server.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;

/**
 * Truy vấn bảng xếp hạng và tổng điểm.
 *
 * <p>Phụ trách: Dev 2.
 */
public class PlayerRankingDAO extends DAO {
    /**
     * Truy vấn ở pha 3, mục 8d: điểm mỗi ván tính từ result (bảng tạm r), tổng điểm (t), trung bình tổng điểm hiện
     * tại của đối thủ theo từng ván, trung bình thời gian các ván thắng có finishTime. Chỉ gồm người đã có ván.
     */
    private static final String RANKING_SQL =
            "WITH r AS (SELECT matchId, playerId, result, finishTime,"
            + "                  CASE result WHEN 'WIN' THEN 1 WHEN 'DRAW' THEN 0.5"
            + "                              ELSE 0 END AS point"
            + "           FROM tblMatchResult),"
            + "     t AS (SELECT playerId, SUM(point) AS totalPoint FROM r GROUP BY playerId)"
            + " SELECT p.id, p.username,"
            + "       t.totalPoint,"
            + "       o.avgOpponentPoint,"
            + "       w.avgWinTime"
            + " FROM tblPlayer p"
            + " JOIN t ON t.playerId = p.id"
            + " JOIN (SELECT me.playerId, AVG(ot.totalPoint) AS avgOpponentPoint"
            + "      FROM r me"
            + "      JOIN r opp ON opp.matchId = me.matchId AND opp.playerId <> me.playerId"
            + "      JOIN t ot ON ot.playerId = opp.playerId"
            + "      GROUP BY me.playerId) o ON o.playerId = p.id"
            + " LEFT JOIN (SELECT playerId, AVG(finishTime) AS avgWinTime"
            + "           FROM r"
            + "           WHERE result = 'WIN' AND finishTime IS NOT NULL"
            + "           GROUP BY playerId) w ON w.playerId = p.id"
            + " ORDER BY t.totalPoint DESC, o.avgOpponentPoint DESC,"
            + "         w.avgWinTime IS NULL, w.avgWinTime ASC, p.username ASC";

    public PlayerRankingDAO() {
        super();
    }

    /**
     * Cả bảng xếp hạng đã sắp xếp theo tổng điểm giảm dần, trung bình điểm đối thủ giảm dần, trung bình thời gian
     * thắng tăng dần (rỗng xếp cuối), rồi theo tên. Mỗi dòng đóng gói một PlayerRanking (id, username và ba chỉ số;
     * rank do RankingController đánh). Lỗi CSDL thì trả danh sách rỗng.
     */
    public ArrayList<PlayerRanking> getRanking() {
        ArrayList<PlayerRanking> listRanking = new ArrayList<>();
        synchronized (DAO.class) {
            try (PreparedStatement ps = con.prepareStatement(RANKING_SQL);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlayerRanking pr = new PlayerRanking();
                    pr.setId(rs.getInt("id"));
                    pr.setUsername(rs.getString("username"));
                    pr.setTotalPoint(rs.getFloat("totalPoint"));
                    pr.setAvgOpponentPoint(rs.getFloat("avgOpponentPoint"));
                    float avgWinTime = rs.getFloat("avgWinTime");
                    pr.setAvgWinTime(rs.wasNull() ? null : avgWinTime);
                    listRanking.add(pr);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return listRanking;
    }

    /**
     * Tổng điểm của các người chơi trong listPlayer (dùng cho danh sách online), người chưa có ván nào có 0 điểm.
     * Truy vấn ở pha 4, mục 2e. Làm ở bước sảnh (M4).
     */
    public ArrayList<PlayerRanking> getTotalPoints(ArrayList<Player> listPlayer) {
        throw new UnsupportedOperationException("TODO");
    }
}

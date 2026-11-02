package ptit.e23cnpm01.pikachu.server.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.StringJoiner;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Match;
import ptit.e23cnpm01.pikachu.core.model.MatchResult;

/**
 * Lưu ván và hai kết quả trong một giao dịch.
 *
 * <p>Phụ trách: Dev 2.
 */
public class MatchDAO extends DAO {
    public MatchDAO() {
        super();
    }

    /**
     * Trong một giao dịch: thêm dòng tblMatch (kèm đề bài), lấy khóa sinh ra gán vào match.id, thêm một dòng
     * tblMatchResult cho mỗi kết quả (kết quả, thời gian hoàn thành, bàn lúc kết thúc; điểm không lưu). Lỗi thì
     * rollback và trả false. Chỉ tự commit khi được gọi lúc autoCommit đang bật, nên test tắt autoCommit trước khi gọi
     * thì rollback được toàn bộ.
     */
    public boolean saveMatch(Match match) {
        synchronized (DAO.class) {
            boolean autoCommit = true;
            try {
                autoCommit = con.getAutoCommit();
                con.setAutoCommit(false);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO tblMatch (startTime, endTime, timeLimit, tiles) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, new Timestamp(match.getStartTime().getTime()));
                    ps.setTimestamp(2, new Timestamp(match.getEndTime().getTime()));
                    ps.setInt(3, match.getTimeLimit());
                    ps.setString(4, tilesString(match.getBoard()));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        match.setId(keys.getInt(1));
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO tblMatchResult (result, finishTime, tiles, matchId, playerId)"
                        + " VALUES (?, ?, ?, ?, ?)")) {
                    for (MatchResult r : match.getResults()) {
                        ps.setString(1, r.getResult());
                        if (r.getFinishTime() == null) {
                            ps.setNull(2, Types.INTEGER);
                        } else {
                            ps.setInt(2, r.getFinishTime());
                        }
                        ps.setString(3, tilesString(r.getBoard()));
                        ps.setInt(4, match.getId());
                        ps.setInt(5, r.getPlayer().getId());
                        ps.executeUpdate();
                    }
                }
                if (autoCommit) {
                    con.commit();
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                return false;
            } finally {
                try {
                    con.setAutoCommit(autoCommit);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Mã hình của các ô vùng chơi (bỏ viền), đọc theo từng hàng, cách nhau bởi dấu phẩy (pha 3, mục 2b). */
    private static String tilesString(Board board) {
        int[][] tiles = board.getTiles();
        StringJoiner joiner = new StringJoiner(",");
        for (int r = 1; r < board.getRowCount() - 1; r++) {
            for (int c = 1; c < board.getColumnCount() - 1; c++) {
                joiner.add(String.valueOf(tiles[r][c]));
            }
        }
        return joiner.toString();
    }
}

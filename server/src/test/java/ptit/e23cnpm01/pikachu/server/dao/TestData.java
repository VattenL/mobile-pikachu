package ptit.e23cnpm01.pikachu.server.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

/**
 * Dữ liệu riêng cho test, chèn trong một giao dịch trên kết nối dùng chung của DAO rồi rollback ở cuối test.
 * Các test giả định CSDL đã nạp schema.sql và seed.sql.
 */
public final class TestData {
    private static final String TILES = String.join(",", Collections.nCopies(100, "0"));

    private TestData() {
    }

    /** Mở kết nối nếu chưa có và tắt autoCommit; mọi thay đổi sau đó bị hủy bằng rollback(). */
    public static void begin() throws SQLException {
        new PlayerRankingDAO();
        DAO.con.setAutoCommit(false);
    }

    public static void rollback() throws SQLException {
        DAO.con.rollback();
        DAO.con.setAutoCommit(true);
    }

    /** Xóa mọi ván (kết quả bị xóa theo), giữ người chơi: bảng xếp hạng rỗng. */
    public static void clearMatches() throws SQLException {
        try (Statement st = DAO.con.createStatement()) {
            st.executeUpdate("DELETE FROM tblMatch");
        }
    }

    public static int addPlayer(String username) throws SQLException {
        try (PreparedStatement ps = DAO.con.prepareStatement(
                "INSERT INTO tblPlayer (username, password) VALUES (?, SHA2('123456', 256))",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.executeUpdate();
            return generatedKey(ps);
        }
    }

    /** winner thắng loser; finishMs rỗng nếu thắng do đối thủ bỏ cuộc. */
    public static void addWin(int winner, int loser, Integer finishMs) throws SQLException {
        int matchId = addMatch();
        addResult(matchId, winner, "WIN", finishMs);
        addResult(matchId, loser, "LOSE", null);
    }

    public static void addDraw(int a, int b) throws SQLException {
        int matchId = addMatch();
        addResult(matchId, a, "DRAW", null);
        addResult(matchId, b, "DRAW", null);
    }

    private static int addMatch() throws SQLException {
        try (PreparedStatement ps = DAO.con.prepareStatement(
                "INSERT INTO tblMatch (startTime, endTime, timeLimit, tiles) VALUES (NOW(), NOW(), 600, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, TILES);
            ps.executeUpdate();
            return generatedKey(ps);
        }
    }

    private static void addResult(int matchId, int playerId, String result, Integer finishMs) throws SQLException {
        try (PreparedStatement ps = DAO.con.prepareStatement(
                "INSERT INTO tblMatchResult (result, finishTime, tiles, matchId, playerId) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, result);
            ps.setObject(2, finishMs);
            ps.setString(3, TILES);
            ps.setInt(4, matchId);
            ps.setInt(5, playerId);
            ps.executeUpdate();
        }
    }

    private static int generatedKey(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}

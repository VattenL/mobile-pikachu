package ptit.e23cnpm01.pikachu.server.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Match;
import ptit.e23cnpm01.pikachu.core.model.MatchResult;
import ptit.e23cnpm01.pikachu.core.model.Player;

/** Pha 4, mục 3b, test 8: MatchDAO.saveMatch(). Ghi trong giao dịch rồi rollback; cần CSDL đã nạp seed.sql. */
public class MatchDAOTest {
    private final MatchDAO dao = new MatchDAO();

    @Before
    public void setUp() throws SQLException {
        TestData.begin();
    }

    @After
    public void tearDown() throws SQLException {
        TestData.rollback();
    }

    /** Lưới 12x12 có viền trống, vùng chơi ô (r, c) mang mã (r * 10 + c) % 25 + 1, trừ các ô trong cleared. */
    private static Board board(boolean cleared) {
        int[][] tiles = new int[12][12];
        for (int r = 1; r <= 10; r++) {
            for (int c = 1; c <= 10; c++) {
                tiles[r][c] = cleared ? 0 : (r * 10 + c) % 25 + 1;
            }
        }
        return new Board(12, 12, tiles);
    }

    private static MatchResult result(int playerId, String result, Integer finishTime, Board board) {
        Player p = new Player();
        p.setId(playerId);
        MatchResult r = new MatchResult();
        r.setPlayer(p);
        r.setResult(result);
        r.setFinishTime(finishTime);
        r.setBoard(board);
        return r;
    }

    /** hoangnam (id 1) thắng minhanh (id 2) sau 402 giây; minhanh còn nguyên bàn. */
    private static Match match(int loserId) {
        Match m = new Match();
        m.setStartTime(new Date(System.currentTimeMillis() - 402000));
        m.setEndTime(new Date());
        m.setTimeLimit(600);
        m.setBoard(board(false));
        m.setResults(new MatchResult[] {
            result(1, "WIN", 402000, board(true)),
            result(loserId, "LOSE", null, board(false))});
        return m;
    }

    @Test
    public void luuVanHopLe_motDongTblMatchHaiDongKetQua() throws SQLException {
        Match m = match(2);
        int before = TestData.countMatches();
        assertTrue(dao.saveMatch(m));
        assertTrue(m.getId() > 0);
        assertEquals(before + 1, TestData.countMatches());
        assertEquals(2, TestData.countResultsOfMatch(m.getId()));
        String fullTiles = tilesOfMatch(m.getId());
        assertEquals(100, fullTiles.split(",").length);
        assertEquals("12,13,14,15,16,17,18,19,20,21", String.join(",",
                Arrays.copyOfRange(fullTiles.split(","), 0, 10)));
        try (PreparedStatement ps = DAO.con.prepareStatement(
                "SELECT playerId, result, finishTime, tiles FROM tblMatchResult WHERE matchId = ? ORDER BY playerId")) {
            ps.setInt(1, m.getId());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                assertEquals(1, rs.getInt("playerId"));
                assertEquals("WIN", rs.getString("result"));
                assertEquals(402000, rs.getInt("finishTime"));
                assertTrue(rs.getString("tiles").matches("0(,0){99}"));
                rs.next();
                assertEquals(2, rs.getInt("playerId"));
                assertEquals("LOSE", rs.getString("result"));
                assertNull(rs.getObject("finishTime"));
                assertEquals(fullTiles, rs.getString("tiles"));
            }
        }
    }

    @Test
    public void xoaVanVuaLuu_haiDongKetQuaBiXoaTheo() throws SQLException {
        Match m = match(2);
        assertTrue(dao.saveMatch(m));
        try (PreparedStatement ps = DAO.con.prepareStatement("DELETE FROM tblMatch WHERE id = ?")) {
            ps.setInt(1, m.getId());
            ps.executeUpdate();
        }
        assertEquals(0, TestData.countResultsOfMatch(m.getId()));
    }

    @Test
    public void nguoiChoiKhongTonTai_traSai_khongLuuDongNao() throws SQLException {
        int before = TestData.countMatches();
        assertFalse(dao.saveMatch(match(999999)));
        assertEquals(before, TestData.countMatches());
    }

    private static String tilesOfMatch(int matchId) throws SQLException {
        try (PreparedStatement ps = DAO.con.prepareStatement("SELECT tiles FROM tblMatch WHERE id = ?")) {
            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }
}

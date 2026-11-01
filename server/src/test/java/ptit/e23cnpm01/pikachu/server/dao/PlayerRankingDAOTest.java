package ptit.e23cnpm01.pikachu.server.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;

/** Pha 4, mục 3b, test 6: PlayerRankingDAO.getRanking(). Cần CSDL đã nạp schema.sql và seed.sql. */
public class PlayerRankingDAOTest {
    private final PlayerRankingDAO dao = new PlayerRankingDAO();

    @Before
    public void setUp() throws SQLException {
        TestData.begin();
    }

    @After
    public void tearDown() throws SQLException {
        TestData.rollback();
    }

    private static int indexOf(ArrayList<PlayerRanking> list, String username) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUsername().equals(username)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    public void thuTuTheoTongDiemGiamDan() {
        ArrayList<PlayerRanking> list = dao.getRanking();
        assertEquals(12, list.size());
        assertEquals("minhanh", list.get(0).getUsername());
        assertEquals(12.5f, list.get(0).getTotalPoint(), 0);
        assertEquals("hoangnam", list.get(11).getUsername());
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getTotalPoint() >= list.get(i).getTotalPoint());
        }
    }

    @Test
    public void bangTongDiem_theoTbDiemDoiThuGiamDan() {
        ArrayList<PlayerRanking> list = dao.getRanking();
        PlayerRanking h = list.get(indexOf(list, "nguoichoih"));
        PlayerRanking i = list.get(indexOf(list, "nguoichoii"));
        assertEquals(h.getTotalPoint(), i.getTotalPoint(), 0);
        assertTrue(h.getAvgOpponentPoint() > i.getAvgOpponentPoint());
        assertEquals(indexOf(list, "nguoichoih") + 1, indexOf(list, "nguoichoii"));
    }

    @Test
    public void bangCaHai_theoTbThoiGianThangTangDan_trungCaBa_theoTen() throws SQLException {
        TestData.clearMatches();
        int x = TestData.addPlayer("t_x");
        int y = TestData.addPlayer("t_y");
        int z = TestData.addPlayer("t_d");
        int w = TestData.addPlayer("t_c");
        TestData.addWin(y, w, 420000);
        TestData.addWin(x, z, 300000);
        TestData.addDraw(z, w);
        ArrayList<PlayerRanking> list = dao.getRanking();
        assertEquals(4, list.size());
        // x, y: cùng 1 điểm, cùng TB điểm đối thủ 0.5; x thắng nhanh hơn
        assertEquals("t_x", list.get(0).getUsername());
        assertEquals("t_y", list.get(1).getUsername());
        assertEquals(0.5f, list.get(0).getAvgOpponentPoint(), 1e-4);
        assertEquals(0.5f, list.get(1).getAvgOpponentPoint(), 1e-4);
        assertEquals(300000f, list.get(0).getAvgWinTime(), 0);
        // t_c, t_d trùng cả ba tiêu chí: đứng liền nhau theo tên
        assertEquals("t_c", list.get(2).getUsername());
        assertEquals("t_d", list.get(3).getUsername());
        assertEquals(list.get(2).getAvgOpponentPoint(), list.get(3).getAvgOpponentPoint(), 0);
        assertNull(list.get(2).getAvgWinTime());
    }

    @Test
    public void chuaThangVanNao_dungSauOTieuChi3() throws SQLException {
        TestData.clearMatches();
        int q = TestData.addPlayer("t_a");   // tên đứng trước nhưng chưa thắng ván nào
        int p = TestData.addPlayer("t_b");
        int r = TestData.addPlayer("t_r");
        int s = TestData.addPlayer("t_s");
        int t = TestData.addPlayer("t_t");
        int u = TestData.addPlayer("t_u");
        TestData.addWin(p, r, 300000);
        TestData.addDraw(r, u);
        TestData.addDraw(q, s);
        TestData.addDraw(q, t);
        ArrayList<PlayerRanking> list = dao.getRanking();
        PlayerRanking first = list.get(0);
        PlayerRanking second = list.get(1);
        assertEquals("t_b", first.getUsername());
        assertEquals("t_a", second.getUsername());
        assertEquals(first.getTotalPoint(), second.getTotalPoint(), 0);
        assertEquals(first.getAvgOpponentPoint(), second.getAvgOpponentPoint(), 1e-4);
        assertNull(second.getAvgWinTime());
    }

    @Test
    public void thangDoDoiThuBoCuoc_khongTinhVaoTbThoiGianThang() throws SQLException {
        TestData.clearMatches();
        int x = TestData.addPlayer("t_x");
        int y = TestData.addPlayer("t_y");
        int z = TestData.addPlayer("t_z");
        TestData.addWin(x, y, 300000);
        TestData.addWin(x, z, null);
        ArrayList<PlayerRanking> list = dao.getRanking();
        assertEquals("t_x", list.get(0).getUsername());
        assertEquals(300000f, list.get(0).getAvgWinTime(), 0);
    }

    @Test
    public void gapCungDoiThuHaiVan_diemDoiThuTinhHaiLan() throws SQLException {
        TestData.clearMatches();
        int x = TestData.addPlayer("t_x");
        int y = TestData.addPlayer("t_y");
        int z = TestData.addPlayer("t_z");
        TestData.addWin(x, y, 300000);
        TestData.addWin(x, y, 320000);
        TestData.addWin(z, x, 310000);
        ArrayList<PlayerRanking> list = dao.getRanking();
        // x: 2 điểm; đối thủ theo ván y (0), y (0), z (1): (0 + 0 + 1) / 3
        PlayerRanking px = list.get(indexOf(list, "t_x"));
        assertEquals(2f, px.getTotalPoint(), 0);
        assertEquals(1f / 3, px.getAvgOpponentPoint(), 1e-4);
    }

    @Test
    public void nguoiChuaCoVan_khongCoTrongBang() throws SQLException {
        TestData.addPlayer("thanhtung");
        ArrayList<PlayerRanking> list = dao.getRanking();
        assertEquals(12, list.size());
        assertEquals(-1, indexOf(list, "thanhtung"));
    }

    @Test
    public void chuaCoVanNao_danhSachRong() throws SQLException {
        TestData.clearMatches();
        assertTrue(dao.getRanking().isEmpty());
    }
}

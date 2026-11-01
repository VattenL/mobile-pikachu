package ptit.e23cnpm01.pikachu.server.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.reflect.TypeToken;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.server.dao.TestData;

/** Pha 4, mục 3b, test 6: RankingController.handleGetRanking(). Cần CSDL đã nạp schema.sql và seed.sql. */
public class RankingControllerTest {
    /** ClientHandler giữ lại message được gửi thay vì ghi ra socket. */
    private static class CapturingHandler extends ClientHandler {
        Message sent;

        CapturingHandler(String username) {
            super(null);
            if (username != null) {
                Player p = new Player();
                p.setUsername(username);
                setPlayer(p);
            }
        }

        @Override
        public void send(Message msg) {
            sent = msg;
        }
    }

    @Before
    public void setUp() throws SQLException {
        TestData.begin();
    }

    @After
    public void tearDown() throws SQLException {
        TestData.rollback();
    }

    private static Message request(Integer page, boolean mine) {
        Message msg = new Message(MessageType.GET_RANKING);
        if (page != null) {
            msg.put("page", page);
        }
        if (mine) {
            msg.put("mine", true);
        }
        return msg;
    }

    private static Message ask(String username, Message request) {
        CapturingHandler handler = new CapturingHandler(username);
        new RankingController().handleGetRanking(handler, request);
        // đi qua chuỗi JSON như trên mạng
        return Message.fromJson(handler.sent.toJson());
    }

    private static ArrayList<PlayerRanking> rows(Message res) {
        return res.get("listRanking", new TypeToken<ArrayList<PlayerRanking>>() { }.getType());
    }

    @Test
    public void muoiHaiNguoi_phanTrang() {
        Message p1 = ask(null, request(1, false));
        assertEquals(MessageType.RANKING, p1.getType());
        assertEquals(1, p1.getInt("page"));
        assertEquals(2, p1.getInt("pageCount"));
        assertEquals(10, rows(p1).size());
        assertEquals(1, rows(p1).get(0).getRank());
        assertEquals("minhanh", rows(p1).get(0).getUsername());
        assertFalse(p1.has("me"));

        Message p2 = ask(null, request(2, false));
        assertEquals(2, p2.getInt("page"));
        assertEquals(2, rows(p2).size());
        assertEquals(12, rows(p2).get(1).getRank());
    }

    @Test
    public void dongChiGomNamTruong() {
        Message res = ask(null, request(1, false));
        String json = res.toJson();
        assertFalse(json.contains("\"id\""));
        assertFalse(json.contains("\"status\""));
        PlayerRanking first = rows(res).get(0);
        assertEquals(12.5f, first.getTotalPoint(), 0);
        assertEquals(9.2f, first.getAvgOpponentPoint(), 0.05f);
        assertEquals(348000f, first.getAvgWinTime(), 0);
    }

    @Test
    public void trungCaBaTieuChi_cungHang() throws SQLException {
        TestData.clearMatches();
        int a = TestData.addPlayer("t_a");
        int b = TestData.addPlayer("t_b");
        int c = TestData.addPlayer("t_c");
        int d = TestData.addPlayer("t_d");
        int e = TestData.addPlayer("t_e");
        int f = TestData.addPlayer("t_f");
        int g = TestData.addPlayer("t_g");
        TestData.addWin(a, f, 300000);
        TestData.addWin(a, g, 300000);
        TestData.addWin(b, d, 400000);
        TestData.addWin(c, e, 400000);
        // a 2 điểm; b, c trùng cả ba tiêu chí; f, g (đối thủ của a) rồi d, e (đối thủ của b, c)
        ArrayList<PlayerRanking> list = rows(ask(null, request(1, false)));
        int[] ranks = list.stream().mapToInt(PlayerRanking::getRank).toArray();
        assertEquals(Arrays.toString(new int[] {1, 2, 2, 4, 4, 6, 6}), Arrays.toString(ranks));
    }

    @Test
    public void mine_hang12_traTrang2() {
        Message res = ask("hoangnam", request(null, true));
        assertEquals(2, res.getInt("page"));
        assertEquals(2, res.getInt("pageCount"));
        assertEquals(Integer.valueOf(12), res.get("me", Integer.class));
        List<PlayerRanking> list = rows(res);
        assertEquals("hoangnam", list.get(list.size() - 1).getUsername());
    }

    @Test
    public void mine_chuaCoVanHoacKhach_meRong() throws SQLException {
        TestData.addPlayer("thanhtung");
        for (String username : new String[] {"thanhtung", null}) {
            Message res = ask(username, request(null, true));
            assertNull(res.get("me", Integer.class));
            assertFalse(res.has("page"));
            assertEquals(2, res.getInt("pageCount"));
            assertTrue(rows(res).isEmpty());
        }
    }

    @Test
    public void pageNgoaiKhoang_kepVe() {
        assertEquals(1, ask(null, request(0, false)).getInt("page"));
        assertEquals(1, ask(null, request(null, false)).getInt("page"));
        Message last = ask(null, request(99, false));
        assertEquals(2, last.getInt("page"));
        assertEquals(2, rows(last).size());
    }

    @Test
    public void bangRong_pageCount1_khongCoDong() throws SQLException {
        TestData.clearMatches();
        Message res = ask(null, request(1, false));
        assertEquals(1, res.getInt("page"));
        assertEquals(1, res.getInt("pageCount"));
        assertTrue(rows(res).isEmpty());
    }
}

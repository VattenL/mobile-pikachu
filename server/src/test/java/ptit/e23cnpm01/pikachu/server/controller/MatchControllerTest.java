package ptit.e23cnpm01.pikachu.server.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ptit.e23cnpm01.pikachu.core.logic.BoardGenerator;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.server.dao.TestData;

/**
 * Pha 4, mục 3b, test 8: MatchController (kết thúc ván, thoát, mất kết nối, hết giờ, tái đấu). Ván được lưu trong giao
 * dịch rồi rollback; cần CSDL đã nạp seed.sql. Cần phần của Dev 1 (sinh đề, sao bàn, đếm ô, MatchRoom.getBoard(),
 * getOpponent()), chưa có thì tự bỏ qua.
 */
public class MatchControllerTest {
    private final MatchController controller = new MatchController();
    private FakeClient a;
    private FakeClient b;
    private MatchRoom room;

    @Before
    public void setUp() throws SQLException {
        assumeTrue("chờ phần core và MatchRoom của Dev 1", FakeClient.matchReady());
        TestData.begin();
        a = FakeClient.online(1, "hoangnam");
        b = FakeClient.online(2, "minhanh");
        room = MatchManager.getInstance().createMatch(a, b, BoardGenerator.generateBoard(10, 10, 25, 5L));
        OnlinePlayerManager.getInstance().updateStatus(
                new ArrayList<>(Arrays.asList(a.getPlayer(), b.getPlayer())), "BUSY");
    }

    @After
    public void tearDown() throws SQLException {
        if (a == null) {
            return;
        }
        for (FakeClient f : Arrays.asList(a, b)) {
            if (f.getRoomId() != null) {
                MatchManager.getInstance().removeRoom(f.getRoomId());
            }
            f.leave();
        }
        MatchManager.getInstance().removeRoom(room.getRoomId());
        TestData.rollback();
    }

    /** endMatch() là hàm riêng (handleMove của Dev 1 gọi khi bàn hết ô), test gọi qua reflection. */
    private void endMatch(String reason, ClientHandler player) throws Exception {
        Method m = MatchController.class.getDeclaredMethod("endMatch", MatchRoom.class, String.class,
                ClientHandler.class);
        m.setAccessible(true);
        synchronized (room) {
            m.invoke(controller, room, reason, player);
        }
    }

    private Message rematch(boolean accept) {
        return new Message(MessageType.REMATCH_REPLY).put("roomId", room.getRoomId()).put("accept", accept);
    }

    private Message leave() {
        return new Message(MessageType.LEAVE_MATCH).put("roomId", room.getRoomId());
    }

    private static String status(FakeClient f) {
        return f.getPlayer().getStatus();
    }

    @Test
    public void xoaHetO_nguoiDoThangCoFinishTime_haiNguoiDuocHoiTaiDau() throws Exception {
        endMatch("CLEARED", a);
        Message endA = a.last(MessageType.MATCH_END);
        Message endB = b.last(MessageType.MATCH_END);
        assertEquals("WIN", endA.getString("result"));
        assertEquals(1f, endA.get("point", Float.class), 0f);
        assertTrue(endA.has("finishTime"));
        assertFalse(endA.has("opponentFinishTime"));
        assertEquals("LOSE", endB.getString("result"));
        assertEquals(0f, endB.get("point", Float.class), 0f);
        assertFalse(endB.has("finishTime"));
        assertEquals(endA.getInt("finishTime"), endB.getInt("opponentFinishTime"));
        assertTrue(endA.getBoolean("rematch"));
        assertTrue(endB.getBoolean("rematch"));
        assertEquals(30, endA.getInt("replySec"));
        assertEquals(room.getRoomId(), endA.getInt("roomId"));
        assertTrue(room.getMatch().getId() > 0);
        assertEquals(2, TestData.countResultsOfMatch(room.getMatch().getId()));
        assertEquals("BUSY", status(a));
        assertEquals("BUSY", status(b));
    }

    @Test
    public void hetGio_haiNguoiHoa() {
        controller.onTimeout(room);
        Message endA = a.last(MessageType.MATCH_END);
        Message endB = b.last(MessageType.MATCH_END);
        assertEquals("DRAW", endA.getString("result"));
        assertEquals("DRAW", endB.getString("result"));
        assertEquals(0.5f, endA.get("point", Float.class), 0f);
        assertEquals("TIMEOUT", endA.getString("reason"));
        assertEquals(100, endA.getInt("remaining"));
        assertEquals(100, endA.getInt("opponentRemaining"));
        assertTrue(endB.getBoolean("rematch"));
    }

    @Test
    public void thoat_nguoiThoatThuaVeRoiNgay_nguoiKiaThangKhongFinishTime() {
        controller.handleLeaveMatch(a, leave());
        assertNull(a.last(MessageType.MATCH_END));
        Message endB = b.last(MessageType.MATCH_END);
        assertEquals("WIN", endB.getString("result"));
        assertFalse(endB.has("finishTime"));
        assertEquals("QUIT", endB.getString("reason"));
        assertFalse(endB.getBoolean("rematch"));
        assertEquals("IDLE", status(a));
        assertNull(a.getRoomId());
        assertEquals("BUSY", status(b));
        assertEquals(Integer.valueOf(room.getRoomId()), b.getRoomId());
        assertNull(b.last(MessageType.OPPONENT_LEFT));
        assertNotNull(a.last(MessageType.ONLINE_LIST));
    }

    @Test
    public void matKetNoiGiuaVan_nhuThoat() {
        controller.handleDisconnect(a);
        Message endB = b.last(MessageType.MATCH_END);
        assertEquals("WIN", endB.getString("result"));
        assertEquals("DISCONNECT", endB.getString("reason"));
        assertFalse(endB.getBoolean("rematch"));
        assertEquals("IDLE", status(a));
        assertEquals("BUSY", status(b));
    }

    @Test
    public void haiNguoiHoanThanhGanNhuCungLuc_chiNguoiDauThang() throws Exception {
        endMatch("CLEARED", a);
        endMatch("CLEARED", b);
        assertEquals(1, a.of(MessageType.MATCH_END).size());
        assertEquals(1, b.of(MessageType.MATCH_END).size());
        assertEquals("WIN", a.last(MessageType.MATCH_END).getString("result"));
        assertEquals(2, TestData.countResultsOfMatch(room.getMatch().getId()));
    }

    @Test
    public void henGio10PhutDenSauKhiVanKetThuc_khongLamGi() throws Exception {
        endMatch("CLEARED", a);
        controller.onTimeout(room);
        assertEquals(1, a.of(MessageType.MATCH_END).size());
        assertEquals("WIN", a.last(MessageType.MATCH_END).getString("result"));
    }

    @Test
    public void caHaiDongY_phongMoiRoomIdKhac_haiNguoiVanBan() {
        controller.onTimeout(room);
        controller.handleRematchReply(a, rematch(true));
        assertNull(a.last(MessageType.MATCH_START));
        controller.handleRematchReply(b, rematch(true));
        assertNull(MatchManager.getInstance().getRoom(room.getRoomId()));
        Message startA = a.last(MessageType.MATCH_START);
        Message startB = b.last(MessageType.MATCH_START);
        assertNotNull(startA);
        assertNotEquals(room.getRoomId(), startA.getInt("roomId"));
        assertEquals(startA.getInt("roomId"), startB.getInt("roomId"));
        assertEquals(Integer.valueOf(startA.getInt("roomId")), a.getRoomId());
        assertEquals("minhanh", startA.get("opponent", Player.class).getUsername());
        assertEquals("BUSY", status(a));
        assertEquals("BUSY", status(b));
    }

    @Test
    public void motNguoiDung_haiNguoiRoi_nguoiKiaNhanOpponentLeft() {
        controller.onTimeout(room);
        controller.handleRematchReply(b, rematch(true));
        controller.handleRematchReply(a, rematch(false));
        assertEquals("IDLE", status(a));
        assertEquals("IDLE", status(b));
        assertNotNull(b.last(MessageType.OPPONENT_LEFT));
        assertNull(a.last(MessageType.OPPONENT_LEFT));
        assertNull(MatchManager.getInstance().getRoom(room.getRoomId()));
        assertNull(a.getRoomId());
        assertNull(b.getRoomId());
    }

    @Test
    public void dongYRoiDoiSangDung_xuLyNhuDung() {
        controller.onTimeout(room);
        controller.handleRematchReply(a, rematch(true));
        controller.handleRematchReply(a, rematch(false));
        assertEquals("IDLE", status(a));
        assertEquals("IDLE", status(b));
        assertNotNull(b.last(MessageType.OPPONENT_LEFT));
        assertNull(MatchManager.getInstance().getRoom(room.getRoomId()));
    }

    @Test
    public void het30GiayKhiMoiMotNguoiDongY_haiNguoiRoi() {
        controller.onTimeout(room);
        controller.handleRematchReply(a, rematch(true));
        controller.onRematchTimeout(room);
        assertEquals("IDLE", status(a));
        assertEquals("IDLE", status(b));
        assertNotNull(a.last(MessageType.OPPONENT_LEFT));
        assertNull(MatchManager.getInstance().getRoom(room.getRoomId()));
    }

    @Test
    public void doiThuDaThoat_nguoiConLaiVeSanh_phongBiBo() {
        controller.handleLeaveMatch(a, leave());
        controller.handleRematchReply(b, rematch(true));
        assertEquals("BUSY", status(b));
        controller.handleRematchReply(b, rematch(false));
        assertEquals("IDLE", status(b));
        assertNull(b.getRoomId());
        assertNull(b.last(MessageType.OPPONENT_LEFT));
        assertNull(MatchManager.getInstance().getRoom(room.getRoomId()));
    }

    @Test
    public void matKetNoiODialogKetQua_nhuChonDung() {
        controller.onTimeout(room);
        controller.handleDisconnect(a);
        assertEquals("IDLE", status(b));
        assertNotNull(b.last(MessageType.OPPONENT_LEFT));
        assertNull(MatchManager.getInstance().getRoom(room.getRoomId()));
    }

    @Test
    public void nguoiNgoaiPhong_biBoQua() {
        FakeClient c = FakeClient.online(3, "nguoichoic");
        try {
            controller.handleLeaveMatch(c, leave());
            assertFalse(room.isEnded());
            controller.onTimeout(room);
            controller.handleRematchReply(c, rematch(false));
            assertNull(b.last(MessageType.OPPONENT_LEFT));
            assertEquals("BUSY", status(a));
        } finally {
            c.leave();
        }
    }
}

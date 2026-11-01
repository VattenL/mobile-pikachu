package ptit.e23cnpm01.pikachu.server.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;

/**
 * Pha 4, mục 3b, test 7: ChallengeController (lời mời, trả lời, hết hạn, danh sách online). Cần CSDL đã nạp seed.sql
 * (tổng điểm của danh sách online). Người chơi dùng id và tên của dữ liệu mẫu.
 */
public class ChallengeControllerTest {
    private final ChallengeController controller = new ChallengeController();
    private FakeClient a;
    private FakeClient b;
    private FakeClient c;

    @Before
    public void setUp() {
        a = FakeClient.online(1, "hoangnam");
        b = FakeClient.online(2, "minhanh");
        c = FakeClient.online(3, "nguoichoic");
    }

    @After
    public void tearDown() {
        for (FakeClient f : Arrays.asList(a, b, c)) {
            if (f.getRoomId() != null) {
                MatchManager.getInstance().removeRoom(f.getRoomId());
            }
            f.leave();
        }
    }

    private static Message invite(String to) {
        return new Message(MessageType.INVITE).put("to", to);
    }

    private static Message reply(String from, boolean accept) {
        return new Message(MessageType.INVITE_REPLY).put("from", from).put("accept", accept);
    }

    @Test
    public void moiHopLe_nguoiDuocMoiNhanInviteReceived() {
        controller.handleInvite(a, invite("minhanh"));
        Message received = b.last(MessageType.INVITE_RECEIVED);
        assertNotNull(received);
        assertEquals("hoangnam", received.getString("from"));
        assertEquals(30, received.getInt("timeoutSec"));
        assertTrue(OnlinePlayerManager.getInstance().hasInvitation("hoangnam"));
    }

    @Test
    public void moiKhiDaCoLoiMoiDi_hoacTuMoiMinh_khongGuiGi() {
        controller.handleInvite(a, invite("minhanh"));
        controller.handleInvite(a, invite("nguoichoic"));
        controller.handleInvite(c, invite("nguoichoic"));
        assertTrue(c.sent.isEmpty());
        assertTrue(a.sent.isEmpty());
    }

    @Test
    public void moiNguoiBan_hoacKhongOnline_baoLyDo() {
        b.getPlayer().setStatus("BUSY");
        controller.handleInvite(a, invite("minhanh"));
        Message busy = a.last(MessageType.INVITE_RESULT);
        assertEquals("minhanh", busy.getString("to"));
        assertFalse(busy.getBoolean("accepted"));
        assertEquals("BUSY", busy.getString("reason"));
        assertTrue(b.of(MessageType.INVITE_RECEIVED).isEmpty());

        controller.handleInvite(a, invite("khongcoai"));
        assertEquals("OFFLINE", a.last(MessageType.INVITE_RESULT).getString("reason"));
        assertFalse(OnlinePlayerManager.getInstance().hasInvitation("hoangnam"));
    }

    @Test
    public void moiNguoiLuyenTap_vanGuiDuoc() {
        b.getPlayer().setStatus("TRAINING");
        controller.handleInvite(a, invite("minhanh"));
        assertNotNull(b.last(MessageType.INVITE_RECEIVED));
    }

    @Test
    public void huyLoiMoi_nguoiDuocMoiNhanCancelled() {
        controller.handleInvite(a, invite("minhanh"));
        controller.handleInviteCancel(a, new Message(MessageType.INVITE_CANCEL).put("to", "minhanh"));
        Message closed = b.last(MessageType.INVITE_CLOSED);
        assertEquals("hoangnam", closed.getString("from"));
        assertEquals("CANCELLED", closed.getString("reason"));
        assertFalse(OnlinePlayerManager.getInstance().hasInvitation("hoangnam"));
    }

    @Test
    public void hetHan_haiBenNhanExpired() {
        controller.handleInvite(a, invite("minhanh"));
        controller.onInviteTimeout("hoangnam", "minhanh");
        assertEquals("EXPIRED", a.last(MessageType.INVITE_RESULT).getString("reason"));
        assertEquals("EXPIRED", b.last(MessageType.INVITE_CLOSED).getString("reason"));
        // hết hạn lần nữa (lời mời đã xử lý): không gửi thêm
        controller.onInviteTimeout("hoangnam", "minhanh");
        assertEquals(1, a.of(MessageType.INVITE_RESULT).size());
    }

    @Test
    public void traLoiLoiMoiDaHetHan_nhanExpired_khongTaoPhong() {
        controller.handleInvite(a, invite("minhanh"));
        controller.onInviteTimeout("hoangnam", "minhanh");
        controller.handleInviteReply(b, reply("hoangnam", true));
        assertEquals(2, b.of(MessageType.INVITE_CLOSED).size());
        assertEquals("EXPIRED", b.last(MessageType.INVITE_CLOSED).getString("reason"));
        assertNull(a.getRoomId());
        assertNull(b.getRoomId());
    }

    @Test
    public void tuChoi_nguoiMoiNhanRejected_haiNguoiVanRoi() {
        controller.handleInvite(a, invite("minhanh"));
        controller.handleInviteReply(b, reply("hoangnam", false));
        Message result = a.last(MessageType.INVITE_RESULT);
        assertEquals("REJECTED", result.getString("reason"));
        assertEquals("IDLE", a.getPlayer().getStatus());
        assertEquals("IDLE", b.getPlayer().getStatus());
    }

    @Test
    public void chapNhanKhiNguoiMoiDaBan_khongTaoPhong() {
        controller.handleInvite(a, invite("minhanh"));
        a.getPlayer().setStatus("BUSY");
        controller.handleInviteReply(b, reply("hoangnam", true));
        assertNull(a.getRoomId());
        assertNull(b.getRoomId());
        assertEquals("BUSY", b.last(MessageType.INVITE_CLOSED).getString("reason"));
    }

    @Test
    public void chapNhanHopLe_taoMotPhong_haiNguoiBan_loiMoiKhacBiDong() {
        assumeTrue("chờ BoardGenerator.generateBoard(), Board.copy() của Dev 1", FakeClient.coreReady());
        FakeClient d = FakeClient.online(4, "nguoichoid");
        try {
            controller.handleInvite(c, invite("minhanh"));   // lời mời khác tới B
            controller.handleInvite(a, invite("minhanh"));
            controller.handleInvite(d, invite("hoangnam"));  // lời mời khác tới A
            controller.handleInviteReply(b, reply("hoangnam", true));

            assertNotNull(a.getRoomId());
            assertEquals(a.getRoomId(), b.getRoomId());
            assertEquals("BUSY", a.getPlayer().getStatus());
            assertEquals("BUSY", b.getPlayer().getStatus());
            Message startA = a.last(MessageType.MATCH_START);
            Message startB = b.last(MessageType.MATCH_START);
            assertEquals((int) a.getRoomId(), startA.getInt("roomId"));
            assertEquals(600, startA.getInt("timeLimitSec"));
            assertEquals("minhanh", startA.get("opponent", JsonObject.class).get("username").getAsString());
            assertEquals("hoangnam", startB.get("opponent", JsonObject.class).get("username").getAsString());
            assertEquals(12, startA.get("tiles", JsonArray.class).size());
            assertEquals("BUSY", c.last(MessageType.INVITE_RESULT).getString("reason"));
            assertEquals("BUSY", d.last(MessageType.INVITE_RESULT).getString("reason"));
            assertFalse(OnlinePlayerManager.getInstance().hasInvitation("nguoichoic"));
        } finally {
            d.leave();
        }
    }

    @Test
    public void dongLoiMoiKhiRoiMang_caLoiMoiDiVaDen() {
        controller.handleInvite(a, invite("minhanh"));
        controller.handleInvite(c, invite("hoangnam"));
        controller.closeInvitations("hoangnam", "OFFLINE");
        assertEquals("OFFLINE", b.last(MessageType.INVITE_CLOSED).getString("reason"));
        assertEquals("OFFLINE", c.last(MessageType.INVITE_RESULT).getString("reason"));
        assertFalse(OnlinePlayerManager.getInstance().hasInvitation("hoangnam"));
        assertFalse(OnlinePlayerManager.getInstance().hasInvitation("nguoichoic"));
    }

    @Test
    public void capNhatTrangThai() {
        controller.handleUpdateStatus(a, new Message(MessageType.UPDATE_STATUS).put("status", "TRAINING"));
        assertEquals("TRAINING", a.getPlayer().getStatus());
        controller.handleUpdateStatus(a, new Message(MessageType.UPDATE_STATUS).put("status", "BUSY"));
        assertEquals("TRAINING", a.getPlayer().getStatus());
        b.getPlayer().setStatus("BUSY");
        controller.handleUpdateStatus(b, new Message(MessageType.UPDATE_STATUS).put("status", "IDLE"));
        assertEquals("BUSY", b.getPlayer().getStatus());
    }

    @Test
    public void danhSachOnline_khongGomNguoiHoi_sapTheoDiem_baTruong() {
        controller.handleGetOnlineList(a, new Message(MessageType.GET_ONLINE_LIST));
        Message list = a.last(MessageType.ONLINE_LIST);
        JsonArray rows = list.get("onlinePlayers", JsonArray.class);
        assertEquals(2, rows.size());
        JsonObject first = rows.get(0).getAsJsonObject();
        assertEquals("minhanh", first.get("username").getAsString());
        assertEquals(12.5f, first.get("totalPoint").getAsFloat(), 0);
        assertEquals("IDLE", first.get("status").getAsString());
        assertEquals(3, first.size());
        assertEquals("nguoichoic", rows.get(1).getAsJsonObject().get("username").getAsString());
    }

    @Test
    public void guiLaiDanhSach_chiChoNguoiRoi() {
        b.getPlayer().setStatus("TRAINING");
        c.getPlayer().setStatus("BUSY");
        controller.broadcastOnlineList();
        assertNotNull(a.last(MessageType.ONLINE_LIST));
        assertNull(b.last(MessageType.ONLINE_LIST));
        assertNull(c.last(MessageType.ONLINE_LIST));
        JsonArray rows = a.last(MessageType.ONLINE_LIST).get("onlinePlayers", JsonArray.class);
        ArrayList<String> statuses = new ArrayList<>();
        rows.forEach(r -> statuses.add(r.getAsJsonObject().get("status").getAsString()));
        assertEquals(Arrays.asList("TRAINING", "BUSY"), statuses);
    }
}

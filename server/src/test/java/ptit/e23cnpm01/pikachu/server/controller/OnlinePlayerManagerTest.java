package ptit.e23cnpm01.pikachu.server.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.After;
import org.junit.Test;
import ptit.e23cnpm01.pikachu.core.model.Player;

/** Pha 4, mục 3b, test 5 (addOnlinePlayer(), removeOnlinePlayer()) và test 7 (lời mời, trạng thái). */
public class OnlinePlayerManagerTest {
    private final OnlinePlayerManager manager = OnlinePlayerManager.getInstance();
    private final ArrayList<ClientHandler> created = new ArrayList<>();

    private ClientHandler handlerOf(String username) {
        Player p = new Player();
        p.setUsername(username);
        ClientHandler h = new ClientHandler(null);
        h.setPlayer(p);
        created.add(h);
        return h;
    }

    @After
    public void tearDown() {
        for (ClientHandler h : created) {
            manager.removeOnlinePlayer(h);
        }
    }

    @Test
    public void addOnlinePlayer_chuaOnline_coTrongDanhSach() {
        ClientHandler h = handlerOf("hoangnam");
        assertTrue(manager.addOnlinePlayer(h));
        assertSame(h, manager.getHandler("hoangnam"));
        assertEquals("IDLE", h.getPlayer().getStatus());
        assertEquals(1, manager.getOnlinePlayers().size());
    }

    @Test
    public void addOnlinePlayer_dangOnlineMayKhac_traSai() {
        ClientHandler first = handlerOf("hoangnam");
        ClientHandler second = handlerOf("hoangnam");
        assertTrue(manager.addOnlinePlayer(first));
        assertFalse(manager.addOnlinePlayer(second));
        assertSame(first, manager.getHandler("hoangnam"));
        assertEquals(1, manager.getOnlinePlayers().size());
    }

    @Test
    public void removeOnlinePlayer_dangOnline_biXoa() {
        ClientHandler h = handlerOf("hoangnam");
        manager.addOnlinePlayer(h);
        manager.addOnlinePlayer(handlerOf("minhanh"));
        assertTrue(manager.removeOnlinePlayer(h));
        assertNull(manager.getHandler("hoangnam"));
        assertEquals(1, manager.getOnlinePlayers().size());
    }

    @Test
    public void removeOnlinePlayer_khongCoTrongDanhSach_khongDoi() {
        manager.addOnlinePlayer(handlerOf("minhanh"));
        assertFalse(manager.removeOnlinePlayer(handlerOf("hoangnam")));
        assertFalse(manager.removeOnlinePlayer(new ClientHandler(null)));
        assertEquals(1, manager.getOnlinePlayers().size());
    }

    @Test
    public void sendInvitation_nguoiRoiHoacLuyenTap_ghiLoiMoi() {
        manager.addOnlinePlayer(handlerOf("hoangnam"));
        ClientHandler b = handlerOf("minhanh");
        manager.addOnlinePlayer(b);
        ClientHandler c = handlerOf("nguoichoic");
        manager.addOnlinePlayer(c);
        c.getPlayer().setStatus("TRAINING");
        try {
            assertNull(manager.sendInvitation("hoangnam", "minhanh"));
            assertTrue(manager.hasInvitation("hoangnam"));
            assertNull(manager.sendInvitation("minhanh", "nguoichoic"));
            assertTrue(manager.hasInvitation("minhanh"));
        } finally {
            manager.removeInvitationsOf("hoangnam");
            manager.removeInvitationsOf("minhanh");
        }
    }

    @Test
    public void sendInvitation_nguoiBan_hoacKhongOnline_traLyDo() {
        manager.addOnlinePlayer(handlerOf("hoangnam"));
        ClientHandler b = handlerOf("minhanh");
        manager.addOnlinePlayer(b);
        b.getPlayer().setStatus("BUSY");
        assertEquals("BUSY", manager.sendInvitation("hoangnam", "minhanh"));
        assertEquals("OFFLINE", manager.sendInvitation("hoangnam", "khongcoai"));
        assertFalse(manager.hasInvitation("hoangnam"));
    }

    @Test
    public void removeInvitation_lanThuHai_traSai() {
        manager.addOnlinePlayer(handlerOf("hoangnam"));
        manager.addOnlinePlayer(handlerOf("minhanh"));
        manager.sendInvitation("hoangnam", "minhanh");
        assertTrue(manager.removeInvitation("hoangnam", "minhanh"));
        assertFalse(manager.removeInvitation("hoangnam", "minhanh"));
    }

    @Test
    public void removeInvitationsOf_traLoiMoiDiVaDen_giuLoiMoiKhac() {
        for (String u : new String[] {"hoangnam", "minhanh", "nguoichoic", "nguoichoid", "nguoichoie"}) {
            manager.addOnlinePlayer(handlerOf(u));
        }
        manager.sendInvitation("hoangnam", "minhanh");     // đi từ hoangnam
        manager.sendInvitation("nguoichoic", "hoangnam");  // đến hoangnam
        manager.sendInvitation("nguoichoid", "nguoichoie"); // không liên quan
        try {
            HashMap<String, String> removed = manager.removeInvitationsOf("hoangnam");
            assertEquals(2, removed.size());
            assertEquals("minhanh", removed.get("hoangnam"));
            assertEquals("hoangnam", removed.get("nguoichoic"));
            assertTrue(manager.hasInvitation("nguoichoid"));
        } finally {
            manager.removeInvitationsOf("nguoichoid");
        }
    }

    @Test
    public void updateStatus_banRoiRoi() {
        ClientHandler a = handlerOf("hoangnam");
        ClientHandler b = handlerOf("minhanh");
        manager.addOnlinePlayer(a);
        manager.addOnlinePlayer(b);
        ArrayList<Player> both = new ArrayList<>(Arrays.asList(a.getPlayer(), b.getPlayer()));
        assertTrue(manager.updateStatus(both, "BUSY"));
        assertEquals("BUSY", manager.getHandler("hoangnam").getPlayer().getStatus());
        assertEquals("BUSY", manager.getHandler("minhanh").getPlayer().getStatus());
        assertTrue(manager.updateStatus(both, "IDLE"));
        assertEquals("IDLE", a.getPlayer().getStatus());
        assertEquals("IDLE", b.getPlayer().getStatus());
    }

    @Test
    public void removeOnlinePlayer_ketNoiBiTuChoi_khongGoNguoiDangOnline() {
        // lần đăng nhập thứ hai bị từ chối; kết nối đó đóng thì người đang online ở máy đầu vẫn còn
        ClientHandler first = handlerOf("hoangnam");
        ClientHandler second = handlerOf("hoangnam");
        manager.addOnlinePlayer(first);
        manager.addOnlinePlayer(second);
        assertFalse(manager.removeOnlinePlayer(second));
        assertSame(first, manager.getHandler("hoangnam"));
    }
}

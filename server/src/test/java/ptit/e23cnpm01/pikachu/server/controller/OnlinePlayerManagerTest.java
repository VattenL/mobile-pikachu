package ptit.e23cnpm01.pikachu.server.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Test;
import ptit.e23cnpm01.pikachu.core.model.Player;

/** Pha 4, mục 3b, test 5: addOnlinePlayer(), removeOnlinePlayer(). */
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

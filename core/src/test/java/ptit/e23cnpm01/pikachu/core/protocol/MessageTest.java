package ptit.e23cnpm01.pikachu.core.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import ptit.e23cnpm01.pikachu.core.model.Cell;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.model.PlayerRanking;

public class MessageTest {
    @Test
    public void toJson_motDong_typeDungDau() {
        Message msg = new Message(MessageType.MOVE).put("roomId", 12)
                .put("a", new Cell(3, 5)).put("b", new Cell(7, 2));
        assertEquals("{\"type\":\"MOVE\",\"roomId\":12,\"a\":{\"row\":3,\"col\":5},\"b\":{\"row\":7,\"col\":2}}",
                msg.toJson());
    }

    @Test
    public void fromJson_docLaiCacTruong() {
        Message msg = Message.fromJson(
                "{\"type\":\"MOVE\",\"roomId\":12,\"a\":{\"row\":3,\"col\":5},\"ok\":true,\"error\":\"x\"}");
        assertEquals(MessageType.MOVE, msg.getType());
        assertEquals(12, msg.getInt("roomId"));
        Cell a = msg.getCell("a");
        assertEquals(3, a.row);
        assertEquals(5, a.col);
        assertTrue(msg.getBoolean("ok"));
        assertEquals("x", msg.getString("error"));
        assertFalse(msg.has("type"));
    }

    @Test
    public void truongKhongCo_traGiaTriRong() {
        Message msg = new Message(MessageType.RANKING).put("me", null);
        assertFalse(msg.has("me"));
        assertNull(msg.get("me", Integer.class));
        assertNull(msg.getString("error"));
        assertEquals(0, msg.getInt("page"));
        assertFalse(msg.getBoolean("mine"));
    }

    @Test
    public void chuoiCoXuongDong_vanLaMotDong() {
        String json = new Message(MessageType.LOGIN_RESULT).put("error", "dòng 1\ndòng 2").toJson();
        assertFalse(json.contains("\n"));
        assertEquals("dòng 1\ndòng 2", Message.fromJson(json).getString("error"));
    }

    @Test
    public void player_khongMangMatKhau() {
        Player p = new Player();
        p.setId(1);
        p.setUsername("hoangnam");
        p.setPassword("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92");
        p.setStatus("IDLE");
        String json = new Message(MessageType.LOGIN_RESULT).put("player", p).toJson();
        assertFalse(json.contains("password"));
        Player back = Message.fromJson(json).get("player", Player.class);
        assertEquals("hoangnam", back.getUsername());
        assertNull(back.getPassword());
    }

    @Test
    public void danhSach_docLaiBangTypeToken() {
        PlayerRanking r = new PlayerRanking();
        r.setUsername("minhanh");
        r.setStatus("BUSY");
        r.setTotalPoint(12.5f);
        String json = new Message(MessageType.ONLINE_LIST).put("players", Arrays.asList(r)).toJson();
        ArrayList<PlayerRanking> list = Message.fromJson(json)
                .get("players", new TypeToken<ArrayList<PlayerRanking>>() { }.getType());
        assertEquals(1, list.size());
        assertEquals("minhanh", list.get(0).getUsername());
        assertEquals("BUSY", list.get(0).getStatus());
        assertEquals(12.5f, list.get(0).getTotalPoint(), 0);
        assertNull(list.get(0).getAvgWinTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromJson_khongPhaiJson() {
        Message.fromJson("không phải json");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromJson_thieuType() {
        Message.fromJson("{\"roomId\":1}");
    }
}

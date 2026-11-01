package ptit.e23cnpm01.pikachu.server.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Test;
import ptit.e23cnpm01.pikachu.core.logic.BoardGenerator;
import ptit.e23cnpm01.pikachu.core.model.Board;

/** Pha 4, mục 3b, test 8: MatchManager.createMatch(), removeRoom(). Cần sinh đề và sao bàn của Dev 1. */
public class MatchManagerTest {
    private final MatchManager manager = MatchManager.getInstance();

    @Before
    public void setUp() {
        assumeTrue("chờ BoardGenerator.generateBoard(), Board.copy() của Dev 1", FakeClient.coreReady());
    }

    @Test
    public void haiPhongLienTiep_roomIdKhacNhau_handlerGiuDungRoomId() {
        FakeClient a = new FakeClient(1, "hoangnam");
        FakeClient b = new FakeClient(2, "minhanh");
        FakeClient c = new FakeClient(3, "nguoichoic");
        FakeClient d = new FakeClient(4, "nguoichoid");
        Board board = BoardGenerator.generateBoard(10, 10, 25, 7L);
        MatchRoom r1 = manager.createMatch(a, b, board);
        MatchRoom r2 = manager.createMatch(c, d, board);
        try {
            assertNotEquals(r1.getRoomId(), r2.getRoomId());
            assertEquals(Integer.valueOf(r1.getRoomId()), a.getRoomId());
            assertEquals(Integer.valueOf(r1.getRoomId()), b.getRoomId());
            assertEquals(Integer.valueOf(r2.getRoomId()), c.getRoomId());
            assertSame(r1, manager.getRoom(r1.getRoomId()));
            assertEquals(600, r1.getMatch().getTimeLimit());
            assertEquals(2, r1.getBoards().length);
        } finally {
            manager.removeRoom(r1.getRoomId());
            manager.removeRoom(r2.getRoomId());
        }
    }

    @Test
    public void boPhong_getRoomRong_roomIdVeRong() {
        FakeClient a = new FakeClient(1, "hoangnam");
        FakeClient b = new FakeClient(2, "minhanh");
        MatchRoom room = manager.createMatch(a, b, BoardGenerator.generateBoard(10, 10, 25, 8L));
        manager.removeRoom(room.getRoomId());
        assertNull(manager.getRoom(room.getRoomId()));
        assertNull(a.getRoomId());
        assertNull(b.getRoomId());
    }
}

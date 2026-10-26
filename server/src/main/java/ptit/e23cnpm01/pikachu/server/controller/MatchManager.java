package ptit.e23cnpm01.pikachu.server.controller;

import java.util.HashMap;
import ptit.e23cnpm01.pikachu.core.model.Board;

/**
 * Giữ các ván đang diễn ra: tạo, tra cứu, bỏ phòng chơi.
 *
 * <p>Phụ trách: Dev 2.
 */
public class MatchManager {
    private HashMap<Integer, MatchRoom> rooms;

    public static MatchManager getInstance() {
        throw new UnsupportedOperationException("TODO");
    }

    public MatchRoom createMatch(ClientHandler a, ClientHandler b, Board board) {
        throw new UnsupportedOperationException("TODO");
    }

    public MatchRoom getRoom(int matchId) {
        throw new UnsupportedOperationException("TODO");
    }
}

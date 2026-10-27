package ptit.e23cnpm01.pikachu.server.controller;

import java.util.HashMap;
import ptit.e23cnpm01.pikachu.core.model.Board;

/**
 * Giữ các ván đang diễn ra theo mã phòng roomId (số tăng dần trong bộ nhớ; Match.id chỉ có khi lưu lúc kết thúc).
 *
 * <p>Phụ trách: Dev 2.
 */
public class MatchManager {
    private HashMap<Integer, MatchRoom> rooms;
    private int nextRoomId;

    public static MatchManager getInstance() {
        throw new UnsupportedOperationException("TODO");
    }

    public MatchRoom createMatch(ClientHandler a, ClientHandler b, Board board) {
        throw new UnsupportedOperationException("TODO");
    }

    public MatchRoom getRoom(int roomId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Bỏ phòng, hủy hẹn giờ của phòng, xóa roomId khỏi các ClientHandler còn giữ nó. */
    public void removeRoom(int roomId) {
        throw new UnsupportedOperationException("TODO");
    }
}

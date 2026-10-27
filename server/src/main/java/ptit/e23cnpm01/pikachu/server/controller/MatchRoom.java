package ptit.e23cnpm01.pikachu.server.controller;

import java.util.Timer;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Match;

/**
 * Một ván đang diễn ra: hai bàn riêng của hai người (cùng đề ban đầu); thao tác được đồng bộ.
 *
 * <p>Phụ trách: Dev 1.
 */
public class MatchRoom {
    private int roomId;
    private Match match;
    private ClientHandler[] handlers;
    private Board[] boards;
    private long startMillis;
    /** 10 phút của ván; sau khi ván kết thúc dùng lại để hẹn 30 giây chờ trả lời tái đấu (phần của Dev 2). */
    private Timer timer;
    private Boolean[] rematchReplies;
    /** Ván đã kết thúc: endMatch() chỉ chạy một lần (phần của Dev 2). */
    private boolean ended;

    public Board getBoard(ClientHandler handler) {
        throw new UnsupportedOperationException("TODO");
    }

    public ClientHandler getOpponent(ClientHandler handler) {
        throw new UnsupportedOperationException("TODO");
    }
}

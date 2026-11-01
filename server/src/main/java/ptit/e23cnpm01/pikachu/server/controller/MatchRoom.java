package ptit.e23cnpm01.pikachu.server.controller;

import java.util.Timer;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Match;

/**
 * Một ván đang diễn ra: hai bàn riêng của hai người (cùng đề ban đầu); thao tác được đồng bộ.
 *
 * <p>Phụ trách: Dev 1. Hàm khởi tạo và get/set do Dev 2 viết vì MatchManager.createMatch() tạo phòng và phần kết thúc
 * ván dùng roomId, timer, rematchReplies, ended.
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

    /** Phòng mới: handlers[i] chơi trên boards[i]; bắt đầu tính giờ từ lúc tạo. */
    public MatchRoom(int roomId, Match match, ClientHandler[] handlers, Board[] boards) {
        this.roomId = roomId;
        this.match = match;
        this.handlers = handlers;
        this.boards = boards;
        this.startMillis = System.currentTimeMillis();
        this.rematchReplies = new Boolean[handlers.length];
        this.ended = false;
    }

    public int getRoomId() {
        return roomId;
    }

    public Match getMatch() {
        return match;
    }

    public ClientHandler[] getHandlers() {
        return handlers;
    }

    public Board[] getBoards() {
        return boards;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Boolean[] getRematchReplies() {
        return rematchReplies;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public Board getBoard(ClientHandler handler) {
        throw new UnsupportedOperationException("TODO");
    }

    public ClientHandler getOpponent(ClientHandler handler) {
        throw new UnsupportedOperationException("TODO");
    }
}

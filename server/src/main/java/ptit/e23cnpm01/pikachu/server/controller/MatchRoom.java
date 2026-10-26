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
    private Match match;
    private ClientHandler[] handlers;
    private Board[] boards;
    private long startMillis;
    private Timer timer;
    private Boolean[] rematchReplies;

    public Board getBoard(ClientHandler handler) {
        throw new UnsupportedOperationException("TODO");
    }

    public ClientHandler getOpponent(ClientHandler handler) {
        throw new UnsupportedOperationException("TODO");
    }
}

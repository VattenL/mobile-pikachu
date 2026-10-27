package ptit.e23cnpm01.pikachu.server.controller;

import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Trọng tài ván đối kháng. Dev 1: handleMove(); Dev 2: phần còn lại (kết thúc ván, hết giờ, thoát, mất kết nối, tái đấu).
 *
 * <p>Phụ trách: Dev 1 và Dev 2.
 */
public class MatchController {
    public void handleMove(ClientHandler handler, Message msg) {
        // TODO
    }

    public void handleLeaveMatch(ClientHandler handler, Message msg) {
        // TODO
    }

    public void handleRematchReply(ClientHandler handler, Message msg) {
        // TODO
    }

    public void onTimeout(MatchRoom room) {
        // TODO
    }

    public void onRematchTimeout(MatchRoom room) {
        // TODO
    }

    public void handleDisconnect(ClientHandler handler) {
        // TODO
    }

    private void endMatch(MatchRoom room, String reason) {
        // TODO
    }

    /** Người stopper dừng (chọn Dừng, Về sảnh, thoát, hết giờ trả lời hoặc mất kết nối ở dialog kết quả). */
    private void stopRematch(MatchRoom room, ClientHandler stopper) {
        // TODO
    }
}

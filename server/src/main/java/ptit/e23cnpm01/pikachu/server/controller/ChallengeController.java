package ptit.e23cnpm01.pikachu.server.controller;

import java.util.Timer;
import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Xử lý GET_ONLINE_LIST, INVITE, INVITE_CANCEL, INVITE_REPLY, UPDATE_STATUS; hết hạn lời mời; gửi lại danh sách online.
 *
 * <p>Phụ trách: Dev 2.
 */
public class ChallengeController {
    /** Hẹn giờ 30 giây cho các lời mời đang chờ. */
    private Timer inviteTimer;

    public void broadcastOnlineList() {
        // TODO
    }

    public void handleGetOnlineList(ClientHandler handler, Message msg) {
        // TODO
    }

    public void handleInvite(ClientHandler handler, Message msg) {
        // TODO
    }

    public void handleInviteCancel(ClientHandler handler, Message msg) {
        // TODO
    }

    public void onInviteTimeout(String from, String to) {
        // TODO
    }

    /** Đóng mọi lời mời đi và đến username với lý do reason (BUSY khi vào ván, OFFLINE khi rời mạng). */
    public void closeInvitations(String username, String reason) {
        // TODO
    }

    public void handleInviteReply(ClientHandler handler, Message msg) {
        // TODO
    }

    public void handleUpdateStatus(ClientHandler handler, Message msg) {
        // TODO
    }
}

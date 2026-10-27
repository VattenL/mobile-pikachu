package ptit.e23cnpm01.pikachu.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import ptit.e23cnpm01.pikachu.core.model.Player;

/**
 * Danh sách người chơi online, trạng thái và lời mời đang chờ (chỉ trong bộ nhớ). Chỉ giữ dữ liệu, không gửi message.
 *
 * <p>Phụ trách: Dev 2.
 */
public class OnlinePlayerManager {
    private HashMap<String, ClientHandler> onlineHandlers;
    /** Lời mời đang chờ: người mời -> người được mời (mỗi người mời chỉ có một lời mời đi). */
    private HashMap<String, String> pendingInvites;

    public static OnlinePlayerManager getInstance() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean addOnlinePlayer(ClientHandler handler) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean removeOnlinePlayer(ClientHandler handler) {
        throw new UnsupportedOperationException("TODO");
    }

    public ArrayList<Player> getOnlinePlayers() {
        throw new UnsupportedOperationException("TODO");
    }

    public ClientHandler getHandler(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Ghi lời mời from -> to; trả null nếu ghi được, "OFFLINE" hoặc "BUSY" nếu không. */
    public String sendInvitation(String from, String to) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Gỡ lời mời from -> to nếu còn; trả false nếu lời mời không còn (đã trả lời, hủy hoặc hết hạn). */
    public boolean removeInvitation(String from, String to) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Gỡ và trả mọi lời mời (người mời -> người được mời) đi hoặc đến username. */
    public HashMap<String, String> removeInvitationsOf(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean updateStatus(ArrayList<Player> listPlayer, String status) {
        throw new UnsupportedOperationException("TODO");
    }
}

package ptit.e23cnpm01.pikachu.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import ptit.e23cnpm01.pikachu.core.model.Player;

/**
 * Danh sách người chơi online, trạng thái và lời mời (chỉ trong bộ nhớ).
 *
 * <p>Phụ trách: Dev 2.
 */
public class OnlinePlayerManager {
    private HashMap<String, ClientHandler> onlineHandlers;

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

    public boolean sendInvitation(Player from, Player to) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean updateStatus(ArrayList<Player> listPlayer, String status) {
        throw new UnsupportedOperationException("TODO");
    }
}

package ptit.e23cnpm01.pikachu.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import ptit.e23cnpm01.pikachu.core.model.Player;

/**
 * Danh sách người chơi online, trạng thái và lời mời đang chờ (chỉ trong bộ nhớ). Chỉ giữ dữ liệu, không gửi message.
 *
 * <p>Trạng thái của mỗi người là thuộc tính status của Player trong ClientHandler: IDLE (rỗi), TRAINING (luyện tập),
 * BUSY (bận). Mọi phương thức được đồng bộ vì nhiều luồng ClientHandler và các bộ hẹn giờ cùng truy cập.
 *
 * <p>Phụ trách: Dev 2.
 */
public class OnlinePlayerManager {
    private static OnlinePlayerManager instance;

    /** Tên đăng nhập -> ClientHandler của người đang online. */
    private HashMap<String, ClientHandler> onlineHandlers;
    /** Lời mời đang chờ: người mời -> người được mời (mỗi người mời chỉ có một lời mời đi). */
    private HashMap<String, String> pendingInvites;

    private OnlinePlayerManager() {
        onlineHandlers = new HashMap<>();
        pendingInvites = new HashMap<>();
    }

    public static synchronized OnlinePlayerManager getInstance() {
        if (instance == null) {
            instance = new OnlinePlayerManager();
        }
        return instance;
    }

    /**
     * Thêm người chơi của handler (đã gán bằng setPlayer) vào danh sách online với trạng thái rỗi. Trả false nếu
     * tài khoản đang online ở máy khác.
     */
    public synchronized boolean addOnlinePlayer(ClientHandler handler) {
        Player player = handler.getPlayer();
        if (onlineHandlers.containsKey(player.getUsername())) {
            return false;
        }
        player.setStatus("IDLE");
        onlineHandlers.put(player.getUsername(), handler);
        return true;
    }

    /**
     * Xóa ánh xạ tên đăng nhập -> handler. Trả false nếu handler không có trong danh sách (chưa đăng nhập, đã gỡ,
     * hoặc tên đó đang online qua một kết nối khác).
     */
    public synchronized boolean removeOnlinePlayer(ClientHandler handler) {
        Player player = handler.getPlayer();
        if (player == null) {
            return false;
        }
        return onlineHandlers.remove(player.getUsername(), handler);
    }

    public synchronized ArrayList<Player> getOnlinePlayers() {
        ArrayList<Player> list = new ArrayList<>();
        for (ClientHandler handler : onlineHandlers.values()) {
            list.add(handler.getPlayer());
        }
        return list;
    }

    /** ClientHandler của người đang online, null nếu người đó không online. */
    public synchronized ClientHandler getHandler(String username) {
        return onlineHandlers.get(username);
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

package ptit.e23cnpm01.pikachu.server.controller;

import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;
import ptit.e23cnpm01.pikachu.server.dao.PlayerDAO;

/**
 * Xử lý REGISTER, LOGIN, LOGOUT.
 *
 * <p>Phụ trách: Dev 1.
 */
public class LoginController {

    public void handleRegister(ClientHandler handler, Message msg) {
        String username = msg.get("username", String.class);
        String password = msg.get("password", String.class);

        // 1. Kiểm tra không ô nào rỗng trước khi xử lý (theo đúng thiết kế pha 3 & tài liệu Dev 1)
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            Message res = new Message(MessageType.REGISTER_RESULT);
            res.put("ok", false);
            res.put("error", "Tên đăng nhập và mật khẩu không được để trống");
            handler.send(res);
            return;
        }

        Player p = new Player();
        p.setUsername(username.trim());
        p.setPassword(password.trim());

        // 2. Gọi DAO thêm người chơi (nhận kiểu boolean theo đúng thiết kế)
        boolean ok = new PlayerDAO().addPlayer(p);

        Message res = new Message(MessageType.REGISTER_RESULT);
        res.put("ok", ok);
        if (!ok) {
            res.put("error", "Tên đăng nhập đã tồn tại");
        }
        handler.send(res);
    }

    public void handleLogin(ClientHandler handler, Message msg) {
        String username = msg.get("username", String.class);
        String password = msg.get("password", String.class);

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            Message res = new Message(MessageType.LOGIN_RESULT);
            res.put("ok", false);
            res.put("error", "Tên đăng nhập và mật khẩu không được để trống");
            handler.send(res);
            return;
        }

        Player player = new PlayerDAO().checkLogin(username.trim(), password.trim());
        Message res = new Message(MessageType.LOGIN_RESULT);

        if (player == null) {
            res.put("ok", false);
            res.put("error", "Sai tên đăng nhập hoặc mật khẩu");
            handler.send(res);
            return;
        }

        // Set player vào handler TRƯỚC KHI thêm vào OnlinePlayerManager
        handler.setPlayer(player);
        boolean added = OnlinePlayerManager.getInstance().addOnlinePlayer(handler);
        if (!added) {
            handler.setPlayer(null);
            res.put("ok", false);
            res.put("error", "Tài khoản đang đăng nhập ở nơi khác");
            handler.send(res);
            return;
        }

        res.put("ok", true);
        res.put("player", player);
        handler.send(res);
        new ChallengeController().broadcastOnlineList();
    }

    public void handleLogout(ClientHandler handler, Message msg) {
        Player player = handler.getPlayer();
        if (player != null) {
            OnlinePlayerManager.getInstance().removeOnlinePlayer(handler);
            new ChallengeController().closeInvitations(player.getUsername(), "OFFLINE");
            handler.setPlayer(null);
            new ChallengeController().broadcastOnlineList();
        }
    }
}
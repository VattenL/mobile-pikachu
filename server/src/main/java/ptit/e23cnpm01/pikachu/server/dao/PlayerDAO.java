package ptit.e23cnpm01.pikachu.server.dao;

import ptit.e23cnpm01.pikachu.core.model.Player;

/**
 * Thêm người chơi, kiểm tra đăng nhập (mật khẩu băm SHA-256).
 *
 * <p>Phụ trách: Dev 1.
 */
public class PlayerDAO extends DAO {
    public PlayerDAO() {
        // TODO
    }

    public boolean addPlayer(Player player) {
        throw new UnsupportedOperationException("TODO");
    }

    public Player checkLogin(String username, String password) {
        throw new UnsupportedOperationException("TODO");
    }
}

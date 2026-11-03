package ptit.e23cnpm01.pikachu.server.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import ptit.e23cnpm01.pikachu.core.model.Player;

/**
 * Thêm người chơi, kiểm tra đăng nhập (mật khẩu băm SHA-256).
 *
 * <p>Phụ trách: Dev 1.
 */
public class PlayerDAO extends DAO {
    public PlayerDAO() {
        super();
    }
    public boolean addPlayer(Player player) {
        synchronized (DAO.class) {
            String sql = "INSERT INTO tblPlayer (username, password) VALUES (?, SHA2(?, 256))";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, player.getUsername());
                ps.setString(2, player.getPassword());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                return false; // Vi phạm UNIQUE username
            }
        }
    }
    public Player checkLogin(String username, String password) {
        synchronized (DAO.class) {
            String sql = "SELECT id, username FROM tblPlayer WHERE username = ? AND password = SHA2(?, 256)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Player p = new Player();
                        p.setId(rs.getInt("id"));
                        p.setUsername(rs.getString("username"));
                        return p;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

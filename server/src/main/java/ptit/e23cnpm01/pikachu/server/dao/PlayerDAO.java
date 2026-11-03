package ptit.e23cnpm01.pikachu.server.dao;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import ptit.e23cnpm01.pikachu.core.model.Player;
import ptit.e23cnpm01.pikachu.server.controller.ServerConfig;

/**
 * Thêm người chơi, kiểm tra đăng nhập (mật khẩu băm SHA-256).
 *
 * <p>Phụ trách: Dev 1.
 */
public class PlayerDAO extends DAO {
    public PlayerDAO() {
        super();
    }

    private boolean ensureConnection() {
        synchronized (DAO.class) {
            try {
                if (con == null || con.isClosed()) {
                    Properties p = ServerConfig.load();
                    con = DriverManager.getConnection(
                            p.getProperty("db.url"),
                            p.getProperty("db.user"),
                            p.getProperty("db.password")
                    );
                }
                return true;
            } catch (SQLException e) {
                System.err.println("Không thể kết nối CSDL: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Thêm người chơi mới vào tblPlayer.
     * @return true nếu thêm thành công; false nếu trùng tên đăng nhập hoặc lỗi.
     */
    public boolean addPlayer(Player player) {
        synchronized (DAO.class) {
            if (!ensureConnection()) {
                return false;
            }
            String sql = "INSERT INTO tblPlayer (username, password) VALUES (?, SHA2(?, 256))";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, player.getUsername());
                ps.setString(2, player.getPassword());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                // Vi phạm ràng buộc duy nhất (UNIQUE) của username trong MySQL
                return false;
            }
        }
    }

    public Player checkLogin(String username, String password) {
        synchronized (DAO.class) {
            if (!ensureConnection()) {
                return null;
            }
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
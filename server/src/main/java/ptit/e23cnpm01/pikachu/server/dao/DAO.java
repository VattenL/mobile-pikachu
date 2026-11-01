package ptit.e23cnpm01.pikachu.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import ptit.e23cnpm01.pikachu.server.controller.ServerConfig;

/**
 * Giữ một Connection dùng chung, mở từ server.properties.
 *
 * <p>Nhiều luồng ClientHandler dùng chung kết nối này, nên mọi phương thức của DAO con, cả đọc lẫn ghi, khóa trên
 * DAO.class; phương thức ghi dùng thêm giao dịch (setAutoCommit(false), commit, lỗi thì rollback).
 *
 * <p>Phụ trách: Dev 2.
 */
public abstract class DAO {
    protected static Connection con;

    public DAO() {
        synchronized (DAO.class) {
            if (con == null) {
                Properties p = ServerConfig.load();
                try {
                    con = DriverManager.getConnection(p.getProperty("db.url"),
                            p.getProperty("db.user"), p.getProperty("db.password"));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

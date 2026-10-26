package ptit.e23cnpm01.pikachu.server.dao;

import java.sql.Connection;

/**
 * Giữ một Connection dùng chung, mở từ server.properties.
 *
 * <p>Phụ trách: Dev 2.
 */
public abstract class DAO {
    protected static Connection con;

    public DAO() {
        // TODO
    }
}

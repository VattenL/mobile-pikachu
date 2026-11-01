package ptit.e23cnpm01.pikachu.server.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Test;

/** Kết nối dùng chung của DAO. Cần MySQL đã chạy create-db.sql, schema.sql, seed.sql và server/server.properties. */
public class DAOTest {
    private static class AnyDAO extends DAO {
    }

    @Test
    public void ketNoiMoMotLan_dungChung() throws SQLException {
        new AnyDAO();
        assertNotNull(DAO.con);
        assertTrue(DAO.con.isValid(2));
        Connection first = DAO.con;
        new AnyDAO();
        assertSame(first, DAO.con);
    }

    @Test
    public void ketNoiUtf8mb4_docDuocDuLieuMau() throws SQLException {
        new AnyDAO();
        synchronized (DAO.class) {
            try (Statement st = DAO.con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT @@character_set_connection, "
                         + "(SELECT COUNT(*) FROM tblPlayer), (SELECT COUNT(*) FROM tblMatch)")) {
                assertTrue(rs.next());
                assertEquals("utf8mb4", rs.getString(1));
                assertEquals(12, rs.getInt(2));
                assertEquals(88, rs.getInt(3));
            }
        }
    }

    @Test
    public void tenTiengViet_ghiDocLaiNguyenVen() throws SQLException {
        new AnyDAO();
        String name = "Hoàng Nam Đặng Ữ 😀";
        synchronized (DAO.class) {
            DAO.con.setAutoCommit(false);
            try (PreparedStatement ins = DAO.con.prepareStatement(
                         "INSERT INTO tblPlayer (username, password) VALUES (?, ?)");
                 PreparedStatement sel = DAO.con.prepareStatement(
                         "SELECT username, CHAR_LENGTH(username) FROM tblPlayer WHERE username = ?")) {
                ins.setString(1, name);
                ins.setString(2, "x");
                ins.executeUpdate();
                sel.setString(1, name);
                try (ResultSet rs = sel.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(name, rs.getString(1));
                    assertEquals(name.codePointCount(0, name.length()), rs.getInt(2));
                }
            } finally {
                DAO.con.rollback();
                DAO.con.setAutoCommit(true);
            }
        }
    }
}

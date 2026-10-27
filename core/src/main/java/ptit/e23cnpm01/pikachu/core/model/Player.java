package ptit.e23cnpm01.pikachu.core.model;

/**
 * Người chơi. username cũng là tên hiển thị. status: IDLE, TRAINING, BUSY (chỉ giữ trong bộ nhớ server).
 *
 * <p>Phụ trách: dùng chung.
 */
public class Player {
    private int id;
    private String username;
    /** Chuỗi băm SHA-256; transient để Gson không gửi qua mạng khi đối tượng nằm trong một message. */
    private transient String password;
    private String status;

    public Player() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

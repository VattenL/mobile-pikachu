package ptit.e23cnpm01.pikachu.core.model;

/**
 * Người chơi. status: IDLE, TRAINING, BUSY (chỉ giữ trong bộ nhớ server).
 *
 * <p>Phụ trách: dùng chung.
 */
public class Player {
    private int id;
    private String username;
    private String password;
    private String name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

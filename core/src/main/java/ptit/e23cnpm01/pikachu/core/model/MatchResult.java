package ptit.e23cnpm01.pikachu.core.model;

/**
 * Kết quả của một người trong một ván; finishTime (ms) rỗng nếu chưa xóa hết ô.
 *
 * <p>Phụ trách: dùng chung.
 */
public class MatchResult {
    private int id;
    private Player player;
    private Board board;
    private String result;
    private float point;
    private Integer finishTime;

    public MatchResult() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public float getPoint() {
        return point;
    }

    public void setPoint(float point) {
        this.point = point;
    }

    public Integer getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Integer finishTime) {
        this.finishTime = finishTime;
    }
}

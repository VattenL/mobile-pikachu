package ptit.e23cnpm01.pikachu.core.model;

/**
 * Một dòng bảng xếp hạng: người chơi kèm ba chỉ số, tính bằng truy vấn.
 *
 * <p>Phụ trách: dùng chung.
 */
public class PlayerRanking extends Player {
    private int rank;
    private float totalPoint;
    private float avgOpponentPoint;
    private Float avgWinTime;

    public PlayerRanking() {
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public float getTotalPoint() {
        return totalPoint;
    }

    public void setTotalPoint(float totalPoint) {
        this.totalPoint = totalPoint;
    }

    public float getAvgOpponentPoint() {
        return avgOpponentPoint;
    }

    public void setAvgOpponentPoint(float avgOpponentPoint) {
        this.avgOpponentPoint = avgOpponentPoint;
    }

    public Float getAvgWinTime() {
        return avgWinTime;
    }

    public void setAvgWinTime(Float avgWinTime) {
        this.avgWinTime = avgWinTime;
    }
}

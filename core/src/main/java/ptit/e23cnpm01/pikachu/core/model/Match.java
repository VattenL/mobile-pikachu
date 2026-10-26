package ptit.e23cnpm01.pikachu.core.model;

import java.util.Date;

/**
 * Ván đấu đối kháng; timeLimit tính bằng giây.
 *
 * <p>Phụ trách: dùng chung.
 */
public class Match {
    private int id;
    private Date startTime;
    private Date endTime;
    private int timeLimit;
    private Board board;
    private MatchResult[] results;

    public Match() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public MatchResult[] getResults() {
        return results;
    }

    public void setResults(MatchResult[] results) {
        this.results = results;
    }
}

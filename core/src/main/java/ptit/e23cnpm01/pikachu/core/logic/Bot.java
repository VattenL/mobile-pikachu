package ptit.e23cnpm01.pikachu.core.logic;

import java.util.List;
import java.util.Random;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Cell;

/**
 * Máy chơi: tìm cặp hợp lệ, chờ T_delay theo độ khó.
 *
 * <p>Phụ trách: Dev 1.
 */
public class Bot {
    private final Difficulty difficulty;
    private final Random random = new Random();

    public Bot(Difficulty difficulty) {
        this.difficulty = (difficulty != null) ? difficulty : Difficulty.MEDIUM;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Thời gian chờ giữa 2 nước đi của Bot (ms).
     * Dễ: 6s (6000ms), Trung bình: 4s (4000ms), Khó: 2s (2000ms).
     */
    public long getDelayMs() {
        switch (difficulty) {
            case EASY:
                return 6000L;
            case MEDIUM:
                return 4000L;
            case HARD:
                return 2000L;
            default:
                return 4000L;
        }
    }

    /**
     * Quyết định nước đi tiếp theo của Bot trên bàn chơi hiện tại.
     *
     * @param board bàn cờ độc lập của Bot
     * @return mảng 2 phần tử Cell[] {a, b} của cặp ô được chọn; null nếu bàn cờ đang bế tắc
     */
    public Cell[] nextMove(Board board) {
        if (board == null) return null;

        List<Cell[]> pairs = PathFinder.findAllAvailablePairs(board);
        if (pairs.isEmpty()) {
            return null;
        }

        // Với mức Dễ và Trung bình: Chọn ngẫu nhiên một cặp trong danh sách các cặp nối được
        if (difficulty == Difficulty.EASY || difficulty == Difficulty.MEDIUM) {
            return pairs.get(random.nextInt(pairs.size()));
        }

        // Mức Khó (HARD): Heuristic - chọn cặp nước đi để lại nhiều lựa chọn tiếp theo nhất, hạn chế bế tắc
        Cell[] bestPair = pairs.get(0);
        int maxNextMoves = -1;

        for (Cell[] pair : pairs) {
            Board testBoard = board.copy();
            testBoard.removePair(pair[0], pair[1]);

            if (testBoard.isCleared()) {
                // Nước đi dọn sạch bàn thắng luôn -> tối ưu tuyệt đối
                return pair;
            }

            int remainingMovesCount = PathFinder.findAllAvailablePairs(testBoard).size();
            if (remainingMovesCount > maxNextMoves) {
                maxNextMoves = remainingMovesCount;
                bestPair = pair;
            }
        }

        return bestPair;
    }
}
package ptit.e23cnpm01.pikachu.core.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Cell;

/**
 * Sinh đề và xáo bàn theo seed.
 *
 * <p>Phụ trách: Dev 1.
 */
public final class BoardGenerator {
    private BoardGenerator() {
    }

    /**
     * Sinh bàn chơi ngẫu nhiên đảm bảo có cặp chẵn và giải được ít nhất 1 cặp.
     *
     * @param rows số hàng vùng chơi (mặc định 10)
     * @param cols số cột vùng chơi (mặc định 10)
     * @param iconTypes số loại icon khác nhau (mặc định 25)
     * @param seed hạt giống ngẫu nhiên
     * @return Board có kích thước (rows + 2) x (cols + 2) với viền ngoài cùng bằng 0
     */
    public static Board generateBoard(int rows, int cols, int iconTypes, long seed) {
        int totalPlayCells = rows * cols;
        if (totalPlayCells % 2 != 0) {
            throw new IllegalArgumentException("Total play cells must be even");
        }

        List<Integer> values = new ArrayList<>(totalPlayCells);
        int pairsCount = totalPlayCells / 2;
        for (int i = 0; i < pairsCount; i++) {
            int icon = (i % iconTypes) + 1;
            values.add(icon);
            values.add(icon);
        }

        Random rng = new Random(seed);
        int fullRows = rows + 2;
        int fullCols = cols + 2;

        Board board;
        do {
            // Fisher-Yates shuffle
            for (int i = values.size() - 1; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int temp = values.get(i);
                values.set(i, values.get(j));
                values.set(j, temp);
            }

            int[][] tiles = new int[fullRows][fullCols];
            int idx = 0;
            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {
                    tiles[r][c] = values.get(idx++);
                }
            }
            board = new Board(fullRows, fullCols, tiles);
        } while (PathFinder.findAvailablePair(board) == null);

        return board;
    }

    /**
     * Xáo các ô còn lại trên bàn cờ khi bị bế tắc, giữ nguyên các ô trống (0).
     *
     * @param board bàn cờ hiện tại
     * @param seed hạt giống ngẫu nhiên
     * @return bàn cờ sau khi xáo đảm bảo giải tiếp được
     */
    public static Board shuffle(Board board, long seed) {
        if (board == null) return null;

        int rowCount = board.getRowCount();
        int colCount = board.getColumnCount();
        int[][] tiles = board.getTiles();

        List<Cell> nonZeroCells = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                if (tiles[r][c] > 0) {
                    nonZeroCells.add(new Cell(r, c));
                    values.add(tiles[r][c]);
                }
            }
        }

        if (values.size() <= 1) {
            return board;
        }

        Random rng = new Random(seed);
        int maxAttempts = 500;
        int attempt = 0;

        do {
            // Fisher-Yates shuffle
            for (int i = values.size() - 1; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int temp = values.get(i);
                values.set(i, values.get(j));
                values.set(j, temp);
            }

            for (int i = 0; i < nonZeroCells.size(); i++) {
                Cell cell = nonZeroCells.get(i);
                tiles[cell.row][cell.col] = values.get(i);
            }

            attempt++;
            if (PathFinder.findAvailablePair(board) != null) {
                break;
            }
        } while (attempt < maxAttempts);

        return board;
    }
}
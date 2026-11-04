package ptit.e23cnpm01.pikachu.core.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import ptit.e23cnpm01.pikachu.core.model.Board;
import ptit.e23cnpm01.pikachu.core.model.Cell;

/**
 * Kiểm tra đường nối hai ô: tối đa 3 đoạn thẳng, chỉ đi qua ô trống.
 *
 * <p>Phụ trách: Dev 1.
 */
public final class PathFinder {
    private static final int[][] DIRS = {
            {-1, 0}, // 0: LÊN (UP)
            {1, 0},  // 1: XUỐNG (DOWN)
            {0, -1}, // 2: TRÁI (LEFT)
            {0, 1}   // 3: PHẢI (RIGHT)
    };

    private PathFinder() {
    }

    /**
     * Kiểm tra đường nối giữa hai ô theo luật Pikachu cổ điển.
     *
     * @param board bàn cờ 12x12 (vùng chơi 10x10 và viền trống 0 ngoài cùng)
     * @param a ô bắt đầu
     * @param b ô kết thúc
     * @return danh sách các điểm gấp khúc [a, ...các điểm rẽ..., b] nếu nối được; null nếu không nối được
     */
    public static ArrayList<Cell> checkPath(Board board, Cell a, Cell b) {
        if (board == null || a == null || b == null) {
            return null;
        }
        if (a.row == b.row && a.col == b.col) {
            return null;
        }

        int[][] tiles = board.getTiles();
        int rowCount = board.getRowCount();
        int colCount = board.getColumnCount();

        if (a.row < 0 || a.row >= rowCount || a.col < 0 || a.col >= colCount ||
                b.row < 0 || b.row >= rowCount || b.col < 0 || b.col >= colCount) {
            return null;
        }

        int iconA = tiles[a.row][a.col];
        int iconB = tiles[b.row][b.col];

        if (iconA == 0 || iconA != iconB) {
            return null;
        }

        // best[r][c][d]: số đoạn thẳng nhỏ nhất khi đi tới ô (r, c) theo hướng d
        int[][][] best = new int[rowCount][colCount][4];
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                for (int d = 0; d < 4; d++) {
                    best[r][c][d] = Integer.MAX_VALUE;
                }
            }
        }

        Queue<SearchNode> queue = new ArrayDeque<>();
        ArrayList<Cell> startCorners = new ArrayList<>();
        startCorners.add(a);
        queue.add(new SearchNode(a.row, a.col, -1, 0, startCorners));

        while (!queue.isEmpty()) {
            SearchNode curr = queue.poll();

            for (int dIdx = 0; dIdx < 4; dIdx++) {
                // Không đi giật lùi 180 độ
                if (curr.direction != -1) {
                    if ((curr.direction == 0 && dIdx == 1) || (curr.direction == 1 && dIdx == 0) ||
                            (curr.direction == 2 && dIdx == 3) || (curr.direction == 3 && dIdx == 2)) {
                        continue;
                    }
                }

                int nextK = (curr.direction == dIdx) ? curr.segments : (curr.segments + 1);
                if (nextK > 3) {
                    continue;
                }

                ArrayList<Cell> nextCorners = new ArrayList<>(curr.corners);
                if (curr.direction != -1 && curr.direction != dIdx) {
                    nextCorners.add(new Cell(curr.row, curr.col));
                }

                int dr = DIRS[dIdx][0];
                int dc = DIRS[dIdx][1];
                int nr = curr.row + dr;
                int nc = curr.col + dc;

                // Đi thẳng liên tục theo hướng dIdx
                while (nr >= 0 && nr < rowCount && nc >= 0 && nc < colCount) {
                    if (nr == b.row && nc == b.col) {
                        nextCorners.add(b);
                        return nextCorners;
                    }
                    if (tiles[nr][nc] != 0) {
                        break;
                    }
                    if (best[nr][nc][dIdx] <= nextK) {
                        break;
                    }

                    best[nr][nc][dIdx] = nextK;
                    queue.add(new SearchNode(nr, nc, dIdx, nextK, nextCorners));

                    nr += dr;
                    nc += dc;
                }
            }
        }

        return null;
    }

    /**
     * Tìm một cặp ô còn nối được trên bàn cờ để xác định bàn có bế tắc không.
     *
     * @param board bàn cờ
     * @return mảng 2 phần tử Cell[] {a, b} nếu tìm thấy; null nếu bàn cờ bế tắc (deadlock)
     */
    public static Cell[] findAvailablePair(Board board) {
        if (board == null) return null;
        int[][] tiles = board.getTiles();
        int rowCount = board.getRowCount();
        int colCount = board.getColumnCount();

        Map<Integer, List<Cell>> map = new HashMap<>();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                int tile = tiles[r][c];
                if (tile > 0) {
                    map.computeIfAbsent(tile, k -> new ArrayList<>()).add(new Cell(r, c));
                }
            }
        }

        for (List<Cell> cells : map.values()) {
            int size = cells.size();
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    Cell p = cells.get(i);
                    Cell q = cells.get(j);
                    if (checkPath(board, p, q) != null) {
                        return new Cell[]{p, q};
                    }
                }
            }
        }

        return null;
    }

    /**
     * Lấy toàn bộ danh sách các cặp ô còn nối được trên bàn cờ (dùng cho Bot heuristic).
     *
     * @param board bàn cờ
     * @return danh sách các cặp Cell[] nối được
     */
    public static List<Cell[]> findAllAvailablePairs(Board board) {
        if (board == null) return Collections.emptyList();
        int[][] tiles = board.getTiles();
        int rowCount = board.getRowCount();
        int colCount = board.getColumnCount();

        Map<Integer, List<Cell>> map = new HashMap<>();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                int tile = tiles[r][c];
                if (tile > 0) {
                    map.computeIfAbsent(tile, k -> new ArrayList<>()).add(new Cell(r, c));
                }
            }
        }

        List<Cell[]> result = new ArrayList<>();
        for (List<Cell> cells : map.values()) {
            int size = cells.size();
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    Cell p = cells.get(i);
                    Cell q = cells.get(j);
                    if (checkPath(board, p, q) != null) {
                        result.add(new Cell[]{p, q});
                    }
                }
            }
        }
        return result;
    }

    private static class SearchNode {
        final int row;
        final int col;
        final int direction;
        final int segments;
        final ArrayList<Cell> corners;

        SearchNode(int row, int col, int direction, int segments, ArrayList<Cell> corners) {
            this.row = row;
            this.col = col;
            this.direction = direction;
            this.segments = segments;
            this.corners = corners;
        }
    }
}
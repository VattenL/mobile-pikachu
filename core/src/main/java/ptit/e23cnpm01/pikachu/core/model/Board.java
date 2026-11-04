package ptit.e23cnpm01.pikachu.core.model;

/**
 * Bàn chơi: lưới 12x12 gồm vùng chơi 10x10 và viền trống, ô trống = 0.
 *
 * <p>Phụ trách: Dev 1.
 */
public class Board {
    private int id;
    private int rowCount;
    private int columnCount;
    private int[][] tiles;

    public Board(int rowCount, int columnCount, int[][] tiles) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.tiles = tiles;
    }

    public int getId() {
        return id;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int[][] getTiles() {
        return tiles;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTile(Cell c) {
        return tiles[c.row][c.col];
    }

    public int getTile(int row, int col) {
        return tiles[row][col];
    }

    public void removePair(Cell a, Cell b) {
        if (a != null && a.row >= 0 && a.row < rowCount && a.col >= 0 && a.col < columnCount) {
            tiles[a.row][a.col] = 0;
        }
        if (b != null && b.row >= 0 && b.row < rowCount && b.col >= 0 && b.col < columnCount) {
            tiles[b.row][b.col] = 0;
        }
    }

    public boolean isCleared() {
        return countRemaining() == 0;
    }

    public int countRemaining() {
        int count = 0;
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                if (tiles[r][c] > 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public Board copy() {
        int[][] newTiles = new int[rowCount][columnCount];
        for (int r = 0; r < rowCount; r++) {
            System.arraycopy(tiles[r], 0, newTiles[r], 0, columnCount);
        }
        Board copy = new Board(rowCount, columnCount, newTiles);
        copy.setId(this.id);
        return copy;
    }
}
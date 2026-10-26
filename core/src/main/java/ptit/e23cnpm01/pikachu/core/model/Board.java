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

    public void removePair(Cell a, Cell b) {
        // TODO
    }

    public boolean isCleared() {
        throw new UnsupportedOperationException("TODO");
    }

    public int countRemaining() {
        throw new UnsupportedOperationException("TODO");
    }

    public Board copy() {
        throw new UnsupportedOperationException("TODO");
    }
}

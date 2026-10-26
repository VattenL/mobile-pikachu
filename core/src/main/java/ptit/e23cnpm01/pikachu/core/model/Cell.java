package ptit.e23cnpm01.pikachu.core.model;

/**
 * Toạ độ một ô trên lưới 12x12 (không dùng java.awt.Point vì Android không có java.awt).
 *
 * <p>Phụ trách: Dev 1.
 */
public final class Cell {
    public final int row;
    public final int col;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }
}

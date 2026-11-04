package ptit.e23cnpm01.pikachu.core.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return row == cell.row && col == cell.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
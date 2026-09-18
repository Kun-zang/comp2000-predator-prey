/**
 * An immutable row and column pair identifying one cell of the world.
 */
public class Position {
    private final int row;
    private final int column;

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    /** Returns a new Position offset from this one. */
    public Position offsetBy(int rowChange, int columnChange) {
        return new Position(row + rowChange, column + columnChange);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + column + ")";
    }
}

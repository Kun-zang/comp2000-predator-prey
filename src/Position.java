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

    /**
     * How many steps apart two cells are when an animal can move diagonally
     * (the larger of the row gap and the column gap).
     */
    public int distanceTo(Position other) {
        return Math.max(Math.abs(row - other.row), Math.abs(column - other.column));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Position)) {
            return false;
        }
        Position that = (Position) other;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return 31 * row + column;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + column + ")";
    }
}

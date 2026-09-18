import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * A rectangular grid of cells, each either empty or holding one occupant.
 *
 * <p>The type parameter is bounded: {@code T extends Entity} means a Grid can be
 * created for any kind of Entity (or for Entity itself), but never for something
 * unrelated such as a String. The bound is what lets this class call Entity
 * methods like getPosition() on its occupants.</p>
 */
public class Grid<T extends Entity> {
    private final int rows;
    private final int columns;

    /**
     * Cells are stored in a single list rather than an array, because Java's type
     * erasure makes "new T[rows][columns]" illegal: at runtime the type T is gone,
     * so the JVM would not know what kind of array to create.
     */
    private final List<T> cells;

    public Grid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.cells = new ArrayList<>(Collections.nCopies(rows * columns, null));
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public boolean contains(Position position) {
        return position.getRow() >= 0 && position.getRow() < rows
            && position.getColumn() >= 0 && position.getColumn() < columns;
    }

    private int indexOf(Position position) {
        return position.getRow() * columns + position.getColumn();
    }

    /**
     * The occupant of a cell, if there is one. Returning an Optional rather than
     * null makes "this cell may be empty" part of the method's type, so callers
     * cannot forget to handle the empty case.
     */
    public Optional<T> at(Position position) {
        if (!contains(position)) {
            return Optional.empty();
        }
        return Optional.ofNullable(cells.get(indexOf(position)));
    }

    public boolean isEmpty(Position position) {
        return contains(position) && at(position).isEmpty();
    }

    /** Puts an occupant in a cell if that cell is inside the grid and empty. */
    public void put(T occupant, Position position) {
        if (isEmpty(position)) {
            cells.set(indexOf(position), occupant);
        }
    }

    public void clear(Position position) {
        if (contains(position)) {
            cells.set(indexOf(position), null);
        }
    }

    /** Every occupant currently on the grid, in row order. */
    public List<T> occupants() {
        List<T> found = new ArrayList<>();
        for (T occupant : cells) {
            if (occupant != null) {
                found.add(occupant);
            }
        }
        return found;
    }

    /** The cells surrounding a position, in random order. */
    public List<Position> neighboursOf(Position position, Random random) {
        List<Position> neighbours = new ArrayList<>();
        for (int rowChange = -1; rowChange <= 1; rowChange++) {
            for (int columnChange = -1; columnChange <= 1; columnChange++) {
                if (rowChange == 0 && columnChange == 0) {
                    continue;
                }
                Position candidate = position.offsetBy(rowChange, columnChange);
                if (contains(candidate)) {
                    neighbours.add(candidate);
                }
            }
        }
        Collections.shuffle(neighbours, random);
        return neighbours;
    }

    /** Every position on the grid, empty or not. */
    public List<Position> allPositions() {
        List<Position> positions = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                positions.add(new Position(row, column));
            }
        }
        return positions;
    }
}

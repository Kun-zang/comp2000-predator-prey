import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    /**
     * Puts an occupant in a cell if that cell is empty.
     *
     * @return true if the occupant was placed, false if the cell was already taken
     * @throws InvalidPositionException if the position is outside the grid
     */
    public boolean put(T occupant, Position position) {
        if (!contains(position)) {
            throw new InvalidPositionException(position, rows, columns);
        }
        if (at(position).isPresent()) {
            return false;
        }
        cells.set(indexOf(position), occupant);
        return true;
    }

    public void clear(Position position) {
        if (contains(position)) {
            cells.set(indexOf(position), null);
        }
    }

    /** A stream of every occupant currently on the grid, in row order. */
    public Stream<T> stream() {
        return cells.stream().filter(Objects::nonNull);
    }

    /** Every occupant currently on the grid, in row order. */
    public List<T> occupants() {
        return stream().collect(Collectors.toList());
    }

    /**
     * Every occupant within a square of the given radius around a cell, not
     * counting the cell itself. This is what an animal can "see".
     */
    public Stream<T> occupantsWithin(Position centre, int radius) {
        return squareAround(centre, radius)
            .map(this::at)
            .flatMap(Optional::stream);
    }

    /** The cells surrounding a position, in random order. */
    public List<Position> neighboursOf(Position position, Random random) {
        List<Position> neighbours = squareAround(position, 1)
            .filter(this::contains)
            .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(neighbours, random);
        return neighbours;
    }

    /**
     * Every position in a square around a centre, not counting the centre itself
     * and not checked against the edges of the grid.
     */
    private Stream<Position> squareAround(Position centre, int radius) {
        return IntStream.rangeClosed(-radius, radius)
            .boxed()
            .flatMap(rowChange -> IntStream.rangeClosed(-radius, radius)
                .mapToObj(columnChange -> centre.offsetBy(rowChange, columnChange)))
            .filter(position -> !position.equals(centre));
    }

    /** Every position on the grid, empty or not, in row order. */
    public List<Position> allPositions() {
        return IntStream.range(0, rows * columns)
            .mapToObj(index -> new Position(index / columns, index % columns))
            .collect(Collectors.toList());
    }
}

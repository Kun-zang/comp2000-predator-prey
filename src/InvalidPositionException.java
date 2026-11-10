/**
 * Thrown when something tries to use a cell that is not on the grid.
 *
 * <p>This is unchecked (a RuntimeException) on purpose. Every caller works from
 * Grid.neighboursOf or Grid.allPositions, which only ever produce positions on the
 * grid, so this can only happen through a bug in the calling code. There is nothing
 * sensible a caller could do to recover, so no caller is forced to catch it: the
 * program stops loudly and the message says exactly which position was wrong.</p>
 *
 * <p>Compare ConfigurationException, which is checked because a bad setting is a
 * mistake by the person running the program, not a bug, and can always be recovered from.</p>
 */
public class InvalidPositionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final transient Position position;

    public InvalidPositionException(Position position, int rows, int columns) {
        super("Position " + position + " is outside the " + rows + "x" + columns + " grid");
        this.position = position;
    }

    /** The position that was not on the grid. */
    public Position getPosition() {
        return position;
    }
}

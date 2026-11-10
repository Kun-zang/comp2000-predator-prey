/**
 * Observer pattern: anything that wants to know when the world has moved on.
 *
 * <p>The World (the subject) keeps a list of these and tells each one after every
 * step, passing the new Census. The World does not know or care what its listeners
 * do with it: redraw the screen, plot a graph, write a CSV file, watch for
 * extinctions. New observers can be added without changing World at all.</p>
 *
 * <p>It is a functional interface, so a simple observer can be a one-line lambda.</p>
 */
@FunctionalInterface
public interface SimulationListener {
    void stepCompleted(int step, Census census);
}

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * An observer that remembers the most recent censuses, so the panel can draw
 * how the populations have changed over time.
 */
public class PopulationHistory implements SimulationListener {
    private final int capacity;
    private final Deque<Census> recent;

    public PopulationHistory(int capacity) {
        this.capacity = capacity;
        this.recent = new ArrayDeque<>(capacity);
    }

    @Override
    public void stepCompleted(int step, Census census) {
        if (recent.size() == capacity) {
            recent.removeFirst();
        }
        recent.addLast(census);
    }

    /** The remembered censuses, oldest first. A copy, so callers cannot change the history. */
    public List<Census> snapshot() {
        return new ArrayList<>(recent);
    }

    public int capacity() {
        return capacity;
    }

    /** The largest value of some measurement across the remembered history. */
    public int peak(ToIntFunction<Census> measure) {
        return recent.stream().mapToInt(measure).max().orElse(0);
    }
}

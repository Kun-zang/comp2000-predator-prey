import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * An observer that notices the step on which a species dies out.
 */
public class ExtinctionWatcher implements SimulationListener {
    private final Map<String, ToIntFunction<Census>> species = new LinkedHashMap<>();
    private final Map<String, Integer> extinctAt = new LinkedHashMap<>();

    public ExtinctionWatcher() {
        species.put("rabbits", Census::rabbits);
        species.put("foxes", Census::foxes);
    }

    @Override
    public void stepCompleted(int step, Census census) {
        species.forEach((name, count) -> {
            if (count.applyAsInt(census) == 0) {
                extinctAt.putIfAbsent(name, step);
            }
        });
    }

    /** Each species that has died out, with the step it happened on, in the order they died. */
    public Map<String, Integer> extinctions() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(extinctAt));
    }

    public boolean anyExtinct() {
        return !extinctAt.isEmpty();
    }

    /** A short sentence for the status line, or an empty string if everyone is still alive. */
    public String summary() {
        StringBuilder text = new StringBuilder();
        extinctAt.forEach((name, step) -> text.append(name).append(" died out at step ").append(step).append("   "));
        return text.toString().strip();
    }
}

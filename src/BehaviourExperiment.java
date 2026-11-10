import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Runs the simulation many times without a window, once for each seed 1..N, for
 * one pairing of rabbit and fox behaviour, and prints a summary. Every pairing is
 * run on the same seeds, so pairings can be compared fairly.
 *
 * <pre>java -cp out BehaviourExperiment cautious stalk 30 2000</pre>
 * (rabbit behaviour, fox behaviour, number of runs, steps per run). Add "sequential"
 * as a fifth argument to run the seeds one after another instead of in parallel,
 * to compare the two timings.
 */
public class BehaviourExperiment {
    private static final int SETTLING_STEPS = 200;
    private static final int SAMPLE_EVERY = 50;

    /** What one run produced. */
    record RunResult(long seed, Map<String, Integer> extinctions,
                     double meanRabbits, double meanFoxes, double meanGapToFox) {
        boolean bothSurvived() {
            return extinctions.isEmpty();
        }

        String outcome() {
            if (bothSurvived()) {
                return "both survived";
            }
            return extinctions.containsKey("rabbits") ? "rabbits wiped out" : "only foxes died out";
        }
    }

    public static void main(String[] args) {
        String rabbitMove = args.length > 0 ? args[0] : "cautious";
        String foxMove = args.length > 1 ? args[1] : "stalk";
        int runs = args.length > 2 ? Integer.parseInt(args[2]) : 30;
        int steps = args.length > 3 ? Integer.parseInt(args[3]) : 2000;
        boolean parallel = args.length <= 4 || !args[4].equalsIgnoreCase("sequential");

        SimulationConfig config;
        try {
            // Validate the settings once, before any run starts. Lambdas inside a stream
            // cannot throw a checked exception, so this must not happen inside one.
            config = SimulationConfig.fromArgs(new String[] {"rabbitmove=" + rabbitMove, "foxmove=" + foxMove});
        } catch (ConfigurationException e) {
            System.err.println("Cannot run experiment: " + e.getMessage());
            return;
        }

        long started = System.nanoTime();
        LongStream seeds = LongStream.rangeClosed(1, runs);
        if (parallel) {
            seeds = seeds.parallel();
        }
        List<RunResult> results = seeds
            .mapToObj(seed -> runOnce(config, seed, steps))
            .collect(Collectors.toList());
        long millis = (System.nanoTime() - started) / 1_000_000;

        report(rabbitMove, foxMove, runs, steps, results, millis, parallel);
    }

    /**
     * One complete, independent run. This is what makes the parallel stream above
     * safe: every run builds its own World with its own Random and its own observers,
     * and the strategies are fresh and keep no state, so no two threads ever touch
     * the same object. Each seed gives the same result whichever thread runs it.
     */
    static RunResult runOnce(SimulationConfig config, long seed, int steps) {
        World world = new World(config.getRows(), config.getColumns(), seed);
        world.populate(config.getGrassChance(), config.getRabbitChance(), config.getFoxChance(),
            config.rabbitMovement(), config.foxMovement());

        ExtinctionWatcher extinctions = new ExtinctionWatcher();
        DoubleSummaryStatistics rabbits = new DoubleSummaryStatistics();
        DoubleSummaryStatistics foxes = new DoubleSummaryStatistics();
        List<Double> gaps = new ArrayList<>();

        world.addListener(extinctions);
        world.addListener((step, census) -> {
            if (step > SETTLING_STEPS) {
                rabbits.accept(census.rabbits());
                foxes.accept(census.foxes());
                if (step % SAMPLE_EVERY == 0) {
                    meanGapToNearestFox(world).ifPresent(gaps::add);
                }
            }
        });

        for (int i = 0; i < steps; i++) {
            world.step();
        }
        double meanGap = gaps.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return new RunResult(seed, extinctions.extinctions(),
            rabbits.getAverage(), foxes.getAverage(), meanGap);
    }

    /** How far, on average, each rabbit is from the fox closest to it. */
    static OptionalDouble meanGapToNearestFox(World world) {
        List<Position> foxes = world.entities()
            .filter(Fox.class::isInstance)
            .map(Entity::getPosition)
            .collect(Collectors.toList());
        if (foxes.isEmpty()) {
            return OptionalDouble.empty();
        }
        return world.entities()
            .filter(Rabbit.class::isInstance)
            .mapToInt(rabbit -> foxes.stream()
                .mapToInt(fox -> fox.distanceTo(rabbit.getPosition()))
                .min()
                .getAsInt())
            .average();
    }

    private static void report(String rabbitMove, String foxMove, int runs, int steps,
                               List<RunResult> results, long millis, boolean parallel) {
        Map<String, Long> outcomes = results.stream()
            .collect(Collectors.groupingBy(RunResult::outcome, TreeMap::new, Collectors.counting()));

        List<RunResult> survivors = results.stream()
            .filter(RunResult::bothSurvived)
            .collect(Collectors.toList());

        System.out.printf("rabbits %s, foxes %s: %d runs of %d steps, %s (%d ms)%n",
            rabbitMove, foxMove, runs, steps, parallel ? "in parallel" : "sequentially", millis);
        System.out.println("  outcomes: " + outcomes);
        if (!survivors.isEmpty()) {
            System.out.printf("  where both survived, after step %d: mean rabbits %.0f, mean foxes %.0f, "
                    + "mean distance from a rabbit to its nearest fox %.2f%n",
                SETTLING_STEPS,
                survivors.stream().collect(Collectors.averagingDouble(RunResult::meanRabbits)),
                survivors.stream().collect(Collectors.averagingDouble(RunResult::meanFoxes)),
                survivors.stream().collect(Collectors.averagingDouble(RunResult::meanGapToFox)));
        }
    }
}

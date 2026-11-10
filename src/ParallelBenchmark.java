import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Times the census sequentially and in parallel on grids of different sizes, to find
 * where a parallel stream starts to pay off. This is what World.PARALLEL_THRESHOLD is
 * based on.
 *
 * <pre>java -cp out ParallelBenchmark</pre>
 *
 * Each size is warmed up first (so the JIT compiler has optimised the code before
 * anything is timed), then timed five times; the median is reported, which is less
 * affected by a one-off pause than the average.
 */
public class ParallelBenchmark {
    private static final int[][] SIZES = {{30, 40}, {60, 80}, {100, 150}, {150, 200}, {200, 300}, {300, 400}};
    private static final int ROUNDS = 5;

    public static void main(String[] args) {
        System.out.println("Census timing, " + Runtime.getRuntime().availableProcessors()
            + " processors available (microseconds per census, median of " + ROUNDS + " rounds)");
        System.out.printf("%-10s %-10s %-12s %-12s %-10s %s%n",
            "grid", "entities", "sequential", "parallel", "faster", "same result");

        for (int[] size : SIZES) {
            World world = new World(size[0], size[1], 1);
            world.populate(0.35, 0.052, 0.012, Rabbit.cautious(), Fox.stalking());
            world.step();
            List<Entity> everyone = world.entities().collect(Collectors.toList());

            int repetitions = Math.max(20, 2_000_000 / everyone.size());
            Census sequential = everyone.stream().collect(Census.collector());
            Census parallel = everyone.parallelStream().collect(Census.collector());

            for (int warmUp = 0; warmUp < 3; warmUp++) {
                time(everyone, false, repetitions);
                time(everyone, true, repetitions);
            }
            double[] sequentialTimes = new double[ROUNDS];
            double[] parallelTimes = new double[ROUNDS];
            for (int round = 0; round < ROUNDS; round++) {
                sequentialTimes[round] = time(everyone, false, repetitions);
                parallelTimes[round] = time(everyone, true, repetitions);
            }
            double sequentialMedian = median(sequentialTimes);
            double parallelMedian = median(parallelTimes);

            System.out.printf("%-10s %-10d %-12.1f %-12.1f %-10s %s%n",
                size[0] + "x" + size[1], everyone.size(), sequentialMedian, parallelMedian,
                parallelMedian < sequentialMedian ? "parallel" : "sequential",
                sequential.equals(parallel) ? "yes" : "NO");
        }
    }

    /** Average microseconds per census over the given number of repetitions. */
    private static double time(List<Entity> everyone, boolean parallel, int repetitions) {
        long start = System.nanoTime();
        int sink = 0;
        for (int i = 0; i < repetitions; i++) {
            Census census = (parallel ? everyone.parallelStream() : everyone.stream())
                .collect(Census.collector());
            sink += census.grass();
        }
        long elapsed = System.nanoTime() - start;
        if (sink == -1) {
            System.out.println(); // stops the JIT from optimising the loop away
        }
        return elapsed / 1_000.0 / repetitions;
    }

    private static double median(double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }
}

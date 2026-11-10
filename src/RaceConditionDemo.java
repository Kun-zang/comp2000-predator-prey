import java.util.concurrent.atomic.AtomicInteger;

/**
 * Week 12: the race condition the Census collector is designed to avoid.
 *
 * <p>It builds a large world and counts every entity on it several times, on a parallel
 * stream, in four different ways. Only the first is wrong, and it is wrong in a
 * different way each time it runs:</p>
 * <ol>
 *   <li><b>Shared counter, no protection.</b> {@code unsafeCount++} is really three steps
 *       (read, add one, write back). Two threads can both read the same value, both add
 *       one, and both write back the same result, so one of the increments is lost.</li>
 *   <li><b>AtomicInteger.</b> incrementAndGet() does all three steps as one indivisible
 *       operation, so nothing is lost, but every thread queues up on the same variable.</li>
 *   <li><b>synchronized block.</b> Only one thread at a time may run the increment.
 *       Correct, and the slowest, because the threads spend their time waiting for each other.</li>
 *   <li><b>The Census collector.</b> Each thread counts into its own Tally and the
 *       tallies are added together at the end. Nothing is shared, so there is nothing
 *       to protect.</li>
 * </ol>
 *
 * <pre>java -cp out RaceConditionDemo</pre>
 */
public class RaceConditionDemo {
    private static final int TRIALS = 10;
    private static final Object LOCK = new Object();

    // Static fields because a lambda cannot change a local variable.
    private static int unsafeCount;
    private static int lockedCount;

    public static void main(String[] args) {
        World world = new World(300, 400, 1);
        world.populate(0.35, 0.052, 0.012, Rabbit.cautious(), Fox.stalking());
        world.step();

        long expected = world.entities().count();
        System.out.println("Counting " + expected + " entities on a parallel stream, "
            + TRIALS + " times each way (" + Runtime.getRuntime().availableProcessors() + " cores)");
        System.out.printf("%-6s %-22s %-14s %-14s %-12s%n",
            "trial", "shared int (unsafe)", "AtomicInteger", "synchronized", "Census");

        for (int trial = 1; trial <= TRIALS; trial++) {
            unsafeCount = 0;
            world.entities().parallel().forEach(entity -> unsafeCount++);

            AtomicInteger atomicCount = new AtomicInteger();
            world.entities().parallel().forEach(entity -> atomicCount.incrementAndGet());

            lockedCount = 0;
            world.entities().parallel().forEach(entity -> {
                synchronized (LOCK) {
                    lockedCount++;
                }
            });

            Census census = world.entities().parallel().collect(Census.collector());
            int censusCount = census.grass() + census.animals();

            System.out.printf("%-6d %-22s %-14d %-14d %-12d%n", trial,
                unsafeCount + (unsafeCount == expected ? "" : "  (lost " + (expected - unsafeCount) + ")"),
                atomicCount.get(), lockedCount, censusCount);
        }
    }
}

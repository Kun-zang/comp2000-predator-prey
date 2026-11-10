import java.util.stream.Collector;

/**
 * A head count of the world at one moment: how much grass, how many rabbits and
 * foxes, and how well fed the animals are on average.
 *
 * <p>A Census is built by streaming every entity on the grid into a custom
 * Collector (see {@link #collector()}). The collector is written so that it is
 * safe to run on a parallel stream.</p>
 */
public record Census(int grass, int ripeGrass, int rabbits, int foxes,
                     double meanRabbitEnergy, double meanFoxEnergy) {

    /** The census of an empty world. */
    public static final Census EMPTY = new Census(0, 0, 0, 0, 0.0, 0.0);

    public int animals() {
        return rabbits + foxes;
    }

    /**
     * A custom collector that counts entities into a Census.
     *
     * <p>The four parts of a collector, and why each one keeps a parallel stream safe:</p>
     * <ul>
     *   <li>supplier ({@code Tally::new}): each thread working on part of the stream
     *       gets its own brand-new Tally, so no two threads ever write to the same object;</li>
     *   <li>accumulator ({@code Tally::add}): adds one entity to that thread's own Tally;</li>
     *   <li>combiner ({@code Tally::merge}): adds two partial Tallies together once
     *       their threads are done; the stream framework calls it, never two threads at once
     *       on the same pair;</li>
     *   <li>finisher ({@code Tally::toCensus}): turns the mutable Tally into an
     *       immutable Census record, so the result can be handed to anyone safely.</li>
     * </ul>
     * <p>It is UNORDERED because counting does not depend on the order entities arrive in,
     * which lets a parallel stream split and merge the work however it likes.</p>
     */
    public static Collector<Entity, ?, Census> collector() {
        return Collector.of(Tally::new, Tally::add, Tally::merge, Tally::toCensus,
            Collector.Characteristics.UNORDERED);
    }

    /** The mutable running totals used while a census is being taken. Never shared. */
    private static final class Tally {
        private int grass;
        private int ripeGrass;
        private int rabbits;
        private int foxes;
        private long rabbitEnergy;
        private long foxEnergy;

        void add(Entity entity) {
            if (entity instanceof Grass) {
                grass++;
                if (((Grass) entity).isMature()) {
                    ripeGrass++;
                }
            } else if (entity instanceof Rabbit) {
                rabbits++;
                rabbitEnergy += ((Rabbit) entity).getEnergy();
            } else if (entity instanceof Fox) {
                foxes++;
                foxEnergy += ((Fox) entity).getEnergy();
            }
        }

        Tally merge(Tally other) {
            grass += other.grass;
            ripeGrass += other.ripeGrass;
            rabbits += other.rabbits;
            foxes += other.foxes;
            rabbitEnergy += other.rabbitEnergy;
            foxEnergy += other.foxEnergy;
            return this;
        }

        Census toCensus() {
            return new Census(grass, ripeGrass, rabbits, foxes,
                rabbits == 0 ? 0.0 : (double) rabbitEnergy / rabbits,
                foxes == 0 ? 0.0 : (double) foxEnergy / foxes);
        }
    }
}

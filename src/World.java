import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The world the simulation runs in. Owns the grid and advances time.
 *
 * <p>World is also the subject in the Observer pattern: after every step it takes a
 * census and passes it to each registered SimulationListener.</p>
 */
public class World {
    private static final double GRASS_REGROWTH_CHANCE = 0.05;

    /**
     * Grids with at least this many cells take their census in parallel. Timed with
     * ParallelBenchmark on my machine, over two runs: at the default 30x40 grid (1,200
     * cells, about 500 entities) a parallel census took 16 to 18 microseconds against 0.8
     * sequentially, because splitting the work costs far more than the counting. At
     * 100x150 (15,000 cells) the two were roughly even, with parallel slightly ahead in
     * one run and slightly behind in the other. From 150x200 (30,000 cells) upwards
     * parallel was clearly faster, and at 300x400 it took about 42 microseconds against
     * 250 to 260. So 15,000 cells is where parallel stops costing anything.
     */
    private static final int PARALLEL_THRESHOLD = 15_000;

    private final Grid<Entity> grid;
    private final Random random;
    private final List<SimulationListener> listeners = new ArrayList<>();
    private int stepCount = 0;

    /**
     * @param seed the seed for every random choice in the run, so that a run can be
     *             repeated exactly by passing the same seed again
     */
    public World(int rows, int columns, long seed) {
        this.grid = new Grid<>(rows, columns);
        this.random = new Random(seed);
    }

    public int getRows() {
        return grid.getRows();
    }

    public int getColumns() {
        return grid.getColumns();
    }

    public int getStepCount() {
        return stepCount;
    }

    public Random getRandom() {
        return random;
    }

    /** The entity in a cell, if any. Empty means the cell is free or off the grid. */
    public Optional<Entity> entityAt(Position position) {
        return grid.at(position);
    }

    public boolean isEmpty(Position position) {
        return grid.isEmpty(position);
    }

    /** Every entity on the grid, in row order. */
    public Stream<Entity> entities() {
        return grid.stream();
    }

    /**
     * Energy statistics for each kind of animal: how many there are, and their lowest,
     * average and highest energy. groupingBy sorts the animals into one group per
     * species (a TreeMap keeps the species in alphabetical order), and the downstream
     * collector summarizingDouble works out the statistics within each group.
     */
    public Map<String, DoubleSummaryStatistics> energyBySpecies() {
        return entities()
            .filter(Animal.class::isInstance)
            .map(Animal.class::cast)
            .collect(Collectors.groupingBy(
                animal -> animal.getClass().getSimpleName(),
                TreeMap::new,
                Collectors.summarizingDouble(Animal::getEnergy)));
    }

    /** Every entity an animal at this position could see, up to the given distance away. */
    public Stream<Entity> entitiesNear(Position position, int vision) {
        return grid.occupantsWithin(position, vision);
    }

    public List<Position> neighboursOf(Position position) {
        return grid.neighboursOf(position, random);
    }

    /** Puts an entity on the grid at its own position, if that cell is free. */
    public void place(Entity entity) {
        grid.put(entity, entity.getPosition());
    }

    /** Kills an entity and takes it off the grid, e.g. when it is eaten. */
    public void consume(Entity prey) {
        prey.die();
        if (grid.at(prey.getPosition()).orElse(null) == prey) {
            grid.clear(prey.getPosition());
        }
    }

    /** Moves an entity into an empty cell and updates its record of where it is. */
    public void moveEntity(Entity entity, Position target) {
        if (!grid.isEmpty(target)) {
            return;
        }
        if (grid.at(entity.getPosition()).orElse(null) == entity) {
            grid.clear(entity.getPosition());
        }
        entity.setPosition(target);
        grid.put(entity, target);
    }

    /** Registers an observer to be told about every step from now on. */
    public void addListener(SimulationListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Advances the simulation by one step: everyone acts, the dead are cleared,
     * grass regrows, and then every listener is told the new census.
     *
     * <p>The acting part must stay sequential. Entities change the shared grid as
     * they act (moving, eating, giving birth), so two acting at once could both
     * move into the same empty cell, or both eat the same rabbit.</p>
     */
    public void step() {
        for (Entity entity : grid.occupants()) {
            if (entity.isAlive()) {
                entity.act(this);
            }
        }
        removeTheDead();
        regrowGrass();
        stepCount++;

        Census census = census();
        listeners.forEach(listener -> listener.stepCompleted(stepCount, census));
    }

    /**
     * Counts everything on the grid with the custom Census collector, in parallel
     * when the grid is big enough for that to pay off.
     *
     * <p>Why the parallel version is thread-safe: it only runs between steps, when
     * nothing is changing the grid, so the worker threads only ever read; each worker
     * accumulates into its own Tally (see Census.collector) and the partial results
     * are merged by the stream framework; and handing work to the fork/join pool
     * guarantees the workers see every change made before the census started.</p>
     */
    public Census census() {
        Stream<Entity> everyone = grid.stream();
        if (grid.getRows() * grid.getColumns() >= PARALLEL_THRESHOLD) {
            everyone = everyone.parallel();
        }
        return everyone.collect(Census.collector());
    }

    private void removeTheDead() {
        grid.occupants().stream()
            .filter(entity -> !entity.isAlive())
            .map(Entity::getPosition)
            .forEach(grid::clear);
    }

    /**
     * Seed blows in: empty cells sometimes sprout new grass.
     * Sequential on purpose: java.util.Random is shared, and the run must be
     * repeatable from its seed, which needs the rolls to happen in a fixed order.
     */
    private void regrowGrass() {
        grid.allPositions().stream()
            .filter(grid::isEmpty)
            .filter(position -> random.nextDouble() < GRASS_REGROWTH_CHANCE)
            .forEach(position -> grid.put(new Grass(position), position));
    }


    /**
     * Scatters a starting population across the grid. The movement strategies are
     * handed in from outside, so the same World can be run with different behaviours.
     *
     * <p>This stays a plain loop on purpose: each cell needs exactly one random roll,
     * taken in order, and the if/else chain reads more clearly than a stream would.</p>
     */
    public void populate(double grassChance, double rabbitChance, double foxChance,
                         MovementStrategy rabbitMovement, MovementStrategy foxMovement) {
        for (Position position : grid.allPositions()) {
            double roll = random.nextDouble();
            if (roll < foxChance) {
                place(new Fox(position, foxMovement));
            } else if (roll < foxChance + rabbitChance) {
                place(new Rabbit(position, rabbitMovement));
            } else if (roll < foxChance + rabbitChance + grassChance) {
                place(new Grass(position, 6));
            }
        }
    }
}

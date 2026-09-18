import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * The world the simulation runs in. Owns the grid and advances time.
 */
public class World {
    private static final double GRASS_REGROWTH_CHANCE = 0.05;

    private final Grid<Entity> grid;
    private final Random random = new Random();
    private int stepCount = 0;

    public World(int rows, int columns) {
        this.grid = new Grid<>(rows, columns);
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

    /** Advances the simulation by one step: everyone acts, the dead are cleared, grass regrows. */
    public void step() {
        for (Entity entity : grid.occupants()) {
            if (entity.isAlive()) {
                entity.act(this);
            }
        }
        removeTheDead();
        regrowGrass();
        stepCount++;
    }

    private void removeTheDead() {
        for (Entity entity : grid.occupants()) {
            if (!entity.isAlive()) {
                grid.clear(entity.getPosition());
            }
        }
    }

    /** Seed blows in: empty cells sometimes sprout new grass. */
    private void regrowGrass() {
        for (Position position : grid.allPositions()) {
            if (grid.isEmpty(position) && random.nextDouble() < GRASS_REGROWTH_CHANCE) {
                grid.put(new Grass(position), position);
            }
        }
    }

    /** How many entities of a given kind are alive right now. */
    public int count(Class<? extends Entity> type) {
        int total = 0;
        for (Entity entity : grid.occupants()) {
            if (type.isInstance(entity)) {
                total++;
            }
        }
        return total;
    }

    /** Scatters a starting population across the grid. */
    public void populate(double grassChance, double rabbitChance, double foxChance) {
        for (Position position : grid.allPositions()) {
            double roll = random.nextDouble();
            if (roll < foxChance) {
                place(new Fox(position));
            } else if (roll < foxChance + rabbitChance) {
                place(new Rabbit(position));
            } else if (roll < foxChance + rabbitChance + grassChance) {
                place(new Grass(position, 6));
            }
        }
    }
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The grid the simulation runs on. Holds every entity and advances time.
 */
public class World {
    private static final double GRASS_REGROWTH_CHANCE = 0.05;

    private final int rows;
    private final int columns;
    private final Entity[][] cells;
    private final Random random = new Random();
    private int stepCount = 0;

    public World(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.cells = new Entity[rows][columns];
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getStepCount() {
        return stepCount;
    }

    public Random getRandom() {
        return random;
    }

    public boolean isInside(Position position) {
        return position.getRow() >= 0 && position.getRow() < rows
            && position.getColumn() >= 0 && position.getColumn() < columns;
    }

    public Entity getEntityAt(Position position) {
        if (!isInside(position)) {
            return null;
        }
        return cells[position.getRow()][position.getColumn()];
    }

    /** Puts an entity into its own cell, if that cell is inside the grid and empty. */
    public void place(Entity entity) {
        Position position = entity.getPosition();
        if (isInside(position) && getEntityAt(position) == null) {
            cells[position.getRow()][position.getColumn()] = entity;
        }
    }

    /** Kills an entity and takes it off the grid, e.g. when it is eaten. */
    public void consume(Entity prey) {
        prey.die();
        if (getEntityAt(prey.getPosition()) == prey) {
            removeAt(prey.getPosition());
        }
    }

    public void removeAt(Position position) {
        if (isInside(position)) {
            cells[position.getRow()][position.getColumn()] = null;
        }
    }

    /** Moves an entity to an empty target cell and updates its own record of where it is. */
    public void moveEntity(Entity entity, Position target) {
        if (!isInside(target) || getEntityAt(target) != null) {
            return;
        }
        if (getEntityAt(entity.getPosition()) == entity) {
            removeAt(entity.getPosition());
        }
        entity.setPosition(target);
        cells[target.getRow()][target.getColumn()] = entity;
    }

    /** The cells around a position, inside the grid, in random order. */
    public List<Position> neighboursOf(Position position) {
        List<Position> neighbours = new ArrayList<>();
        for (int rowChange = -1; rowChange <= 1; rowChange++) {
            for (int columnChange = -1; columnChange <= 1; columnChange++) {
                if (rowChange == 0 && columnChange == 0) {
                    continue;
                }
                Position candidate = position.offsetBy(rowChange, columnChange);
                if (isInside(candidate)) {
                    neighbours.add(candidate);
                }
            }
        }
        Collections.shuffle(neighbours, random);
        return neighbours;
    }

    /** Advances the simulation by one step: everyone acts, then the dead are cleared away. */
    public void step() {
        List<Entity> actingThisStep = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Entity entity = cells[row][column];
                if (entity != null) {
                    actingThisStep.add(entity);
                }
            }
        }

        for (Entity entity : actingThisStep) {
            if (entity.isAlive()) {
                entity.act(this);
            }
        }

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Entity entity = cells[row][column];
                if (entity != null && !entity.isAlive()) {
                    cells[row][column] = null;
                }
            }
        }
        regrowGrass();
        stepCount++;
    }

    /** Seed blows in: empty cells sometimes sprout new grass. */
    private void regrowGrass() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (cells[row][column] == null && random.nextDouble() < GRASS_REGROWTH_CHANCE) {
                    cells[row][column] = new Grass(new Position(row, column));
                }
            }
        }
    }

    public int count(Class<? extends Entity> type) {
        int total = 0;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (type.isInstance(cells[row][column])) {
                    total++;
                }
            }
        }
        return total;
    }

    /** Scatters a starting population across the grid. */
    public void populate(double grassChance, double rabbitChance, double foxChance) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Position position = new Position(row, column);
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
}

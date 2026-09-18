import java.awt.Color;

/**
 * Eats mature grass, breeds readily, and starves when the meadow is bare.
 */
public class Rabbit extends Animal {
    private static final int STARTING_ENERGY = 20;
    private static final int ENERGY_PER_STEP = 1;
    private static final int ENERGY_PER_GRASS = 11;
    private static final int BREEDING_ENERGY = 35;
    private static final int MAXIMUM_AGE = 60;

    public Rabbit(Position position) {
        this(position, STARTING_ENERGY);
    }

    public Rabbit(Position position, int startingEnergy) {
        super(position, startingEnergy);
    }

    @Override
    public void act(World world) {
        growOlder(MAXIMUM_AGE);
        changeEnergy(-ENERGY_PER_STEP);
        if (!isAlive()) {
            return;
        }
        if (!tryToEat(world)) {
            wander(world);
        }
        tryToBreed(world, BREEDING_ENERGY);
    }

    /** Looks for mature grass nearby, eats it, and takes over its cell. */
    private boolean tryToEat(World world) {
        for (Position candidate : world.neighboursOf(getPosition())) {
            Entity occupant = world.getEntityAt(candidate);
            if (occupant instanceof Grass && ((Grass) occupant).isMature()) {
                world.consume(occupant);
                world.moveEntity(this, candidate);
                changeEnergy(ENERGY_PER_GRASS);
                return true;
            }
        }
        return false;
    }

    @Override
    protected Animal offspringAt(Position position, int energy) {
        return new Rabbit(position, energy);
    }

    @Override
    public Color getColor() {
        return new Color(228, 228, 238);
    }
}

import java.awt.Color;

/**
 * Hunts rabbits. Costs more energy to run than a rabbit does, and often misses,
 * so fox numbers lag behind the rabbit population rather than tracking it exactly.
 */
public class Fox extends Animal {
    private static final int STARTING_ENERGY = 45;
    private static final int ENERGY_PER_STEP = 2;
    private static final int ENERGY_PER_RABBIT = 14;
    private static final int BREEDING_ENERGY = 130;
    private static final int MAXIMUM_AGE = 120;
    private static final double CATCH_CHANCE = 0.6;

    public Fox(Position position) {
        this(position, STARTING_ENERGY);
    }

    public Fox(Position position, int startingEnergy) {
        super(position, startingEnergy);
    }

    @Override
    public void act(World world) {
        growOlder(MAXIMUM_AGE);
        changeEnergy(-ENERGY_PER_STEP);
        if (!isAlive()) {
            return;
        }
        if (!tryToHunt(world)) {
            wander(world);
        }
        tryToBreed(world, BREEDING_ENERGY);
    }

    /** Chases a neighbouring rabbit. The rabbit sometimes escapes. */
    private boolean tryToHunt(World world) {
        for (Position candidate : world.neighboursOf(getPosition())) {
            Entity occupant = world.getEntityAt(candidate);
            if (occupant instanceof Rabbit) {
                if (world.getRandom().nextDouble() > CATCH_CHANCE) {
                    continue;
                }
                world.consume(occupant);
                world.moveEntity(this, candidate);
                changeEnergy(ENERGY_PER_RABBIT);
                return true;
            }
        }
        return false;
    }

    @Override
    protected Animal offspringAt(Position position, int energy) {
        return new Fox(position, energy);
    }

    @Override
    public Color getColor() {
        return new Color(216, 106, 46);
    }
}

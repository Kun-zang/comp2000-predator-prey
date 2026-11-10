import java.awt.Color;
import java.util.Optional;

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
    private static final int VISION = 4;

    public Fox(Position position, MovementStrategy movement) {
        this(position, STARTING_ENERGY, movement);
    }

    public Fox(Position position, int startingEnergy, MovementStrategy movement) {
        super(position, startingEnergy, movement);
    }

    /** Stalks the nearest rabbit it can see; otherwise wanders. */
    public static MovementStrategy stalking() {
        return Movements.towards(entity -> entity instanceof Rabbit, VISION)
            .orElse(Movements.randomStep());
    }

    @Override
    public void act(World world) {
        growOlder(MAXIMUM_AGE);
        changeEnergy(-ENERGY_PER_STEP);
        if (!isAlive()) {
            return;
        }
        if (!tryToHunt(world)) {
            move(world);
        }
        tryToBreed(world, BREEDING_ENERGY);
    }

    /**
     * Pounces on a neighbouring rabbit. Each rabbit it tries for may escape.
     * The random roll sits inside the stream's filter, and streams are lazy, so a
     * roll is only made for each rabbit actually tried, stopping at the first catch:
     * the same behaviour as the loop this replaced.
     */
    private boolean tryToHunt(World world) {
        Optional<Entity> caught = world.neighboursOf(getPosition()).stream()
            .map(world::entityAt)
            .flatMap(Optional::stream)
            .filter(Rabbit.class::isInstance)
            .filter(rabbit -> world.getRandom().nextDouble() <= CATCH_CHANCE)
            .findFirst();
        caught.ifPresent(rabbit -> {
            Position where = rabbit.getPosition();
            world.consume(rabbit);
            world.moveEntity(this, where);
            changeEnergy(ENERGY_PER_RABBIT);
        });
        return caught.isPresent();
    }

    @Override
    protected Animal offspringAt(Position position, int energy) {
        return new Fox(position, energy, getMovement());
    }

    @Override
    public Color getColor() {
        return new Color(216, 106, 46);
    }
}

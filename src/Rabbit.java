import java.awt.Color;
import java.util.Optional;

/**
 * Eats mature grass, breeds readily, and starves when the meadow is bare.
 */
public class Rabbit extends Animal {
    private static final int STARTING_ENERGY = 20;
    private static final int ENERGY_PER_STEP = 1;
    private static final int ENERGY_PER_GRASS = 11;
    private static final int BREEDING_ENERGY = 35;
    private static final int MAXIMUM_AGE = 60;
    private static final int VISION = 3;

    public Rabbit(Position position, MovementStrategy movement) {
        this(position, STARTING_ENERGY, movement);
    }

    public Rabbit(Position position, int startingEnergy, MovementStrategy movement) {
        super(position, startingEnergy, movement);
    }

    /** Heads for ripe grass it can see; otherwise wanders. */
    public static MovementStrategy grazing() {
        return Movements.towards(Rabbit::isRipeGrass, VISION)
            .orElse(Movements.randomStep());
    }

    /** Runs from any fox it can see; otherwise grazes. */
    public static MovementStrategy cautious() {
        return Movements.awayFrom(entity -> entity instanceof Fox, VISION)
            .orElse(grazing());
    }

    private static boolean isRipeGrass(Entity entity) {
        return entity instanceof Grass && ((Grass) entity).isMature();
    }

    @Override
    public void act(World world) {
        growOlder(MAXIMUM_AGE);
        changeEnergy(-ENERGY_PER_STEP);
        if (!isAlive()) {
            return;
        }
        if (!tryToEat(world)) {
            move(world);
        }
        tryToBreed(world, BREEDING_ENERGY);
    }

    /** Looks for mature grass next to it, eats it, and takes over its cell. */
    private boolean tryToEat(World world) {
        Optional<Grass> meal = world.neighboursOf(getPosition()).stream()
            .map(world::entityAt)
            .flatMap(Optional::stream)
            .filter(Grass.class::isInstance)
            .map(Grass.class::cast)
            .filter(Grass::isMature)
            .findFirst();
        meal.ifPresent(grass -> {
            world.consume(grass);
            world.moveEntity(this, grass.getPosition());
            changeEnergy(ENERGY_PER_GRASS);
        });
        return meal.isPresent();
    }

    @Override
    protected Animal offspringAt(Position position, int energy) {
        return new Rabbit(position, energy, getMovement());
    }

    @Override
    public Color getColor() {
        return new Color(228, 228, 238);
    }
}

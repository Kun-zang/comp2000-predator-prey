/**
 * An entity that moves, ages, and must eat to stay alive.
 *
 * <p>How an animal moves is not decided here or in a subclass. It is delegated to a
 * MovementStrategy the animal is given when it is created (composition rather than
 * inheritance). Young inherit their parent's strategy.</p>
 */
public abstract class Animal extends Entity {
    private final MovementStrategy movement;
    private int energy;
    private int age;

    protected Animal(Position position, int startingEnergy, MovementStrategy movement) {
        super(position);
        this.energy = startingEnergy;
        this.movement = movement;
    }

    public int getEnergy() {
        return energy;
    }

    public int getAge() {
        return age;
    }

    public MovementStrategy getMovement() {
        return movement;
    }

    /** Adds or removes energy, and dies if it runs out. */
    protected void changeEnergy(int amount) {
        energy += amount;
        if (energy <= 0) {
            die();
        }
    }

    /** Ages by one step, and dies of old age past the given limit. */
    protected void growOlder(int maximumAge) {
        age++;
        if (age > maximumAge) {
            die();
        }
    }

    /** Asks this animal's strategy where to go, and goes there. */
    protected void move(World world) {
        movement.chooseStep(this, world)
            .ifPresent(target -> world.moveEntity(this, target));
    }

    /**
     * Splits this animal's energy with a newborn in a neighbouring cell,
     * once it has enough energy to spare.
     */
    protected void tryToBreed(World world, int breedingEnergy) {
        if (energy < breedingEnergy) {
            return;
        }
        world.neighboursOf(getPosition()).stream()
            .filter(world::isEmpty)
            .findFirst()
            .ifPresent(nursery -> {
                int passedOn = energy / 2;
                changeEnergy(-passedOn);
                world.place(offspringAt(nursery, passedOn));
            });
    }

    /** Each species creates its own kind of young. */
    protected abstract Animal offspringAt(Position position, int energy);
}

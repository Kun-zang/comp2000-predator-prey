import java.util.List;

/**
 * An entity that moves, ages, and must eat to stay alive.
 */
public abstract class Animal extends Entity {
    private int energy;
    private int age;

    protected Animal(Position position, int startingEnergy) {
        super(position);
        this.energy = startingEnergy;
    }

    public int getEnergy() {
        return energy;
    }

    public int getAge() {
        return age;
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

    /** Steps into a neighbouring empty cell if there is one. */
    protected void wander(World world) {
        List<Position> neighbours = world.neighboursOf(getPosition());
        for (Position candidate : neighbours) {
            if (world.getEntityAt(candidate) == null) {
                world.moveEntity(this, candidate);
                return;
            }
        }
    }

    /**
     * Splits this animal's energy with a newborn in a neighbouring cell,
     * once it has enough energy to spare.
     */
    protected void tryToBreed(World world, int breedingEnergy) {
        if (energy < breedingEnergy) {
            return;
        }
        for (Position candidate : world.neighboursOf(getPosition())) {
            if (world.getEntityAt(candidate) == null) {
                int passedOn = energy / 2;
                changeEnergy(-passedOn);
                world.place(offspringAt(candidate, passedOn));
                return;
            }
        }
    }

    /** Each species creates its own kind of young. */
    protected abstract Animal offspringAt(Position position, int energy);
}

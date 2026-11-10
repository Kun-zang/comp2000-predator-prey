import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * The concrete movement strategies. Each one is a lambda returned from a factory
 * method, so there is no WanderStrategy / SeekStrategy / FleeStrategy class
 * hierarchy: a strategy is just a function from (animal, world) to a step.
 *
 * <p>The factories take parameters (what to look for, how far the animal can see)
 * and the lambdas capture them, so one factory makes a whole family of strategies.</p>
 */
public final class Movements {
    private Movements() {
    }

    /** Steps into a random free neighbouring cell. Never has "no opinion" unless boxed in. */
    public static MovementStrategy randomStep() {
        return (mover, world) -> world.neighboursOf(mover.getPosition()).stream()
            .filter(world::isEmpty)
            .findFirst();
    }

    /**
     * Heads for the nearest visible entity that matches the target test.
     * Has no opinion when nothing matching is within sight.
     */
    public static MovementStrategy towards(Predicate<Entity> target, int vision) {
        return (mover, world) -> world.entitiesNear(mover.getPosition(), vision)
            .filter(target)
            .min(Comparator.comparingInt(found -> found.getPosition().distanceTo(mover.getPosition())))
            .flatMap(goal -> bestFreeStep(mover, world,
                Comparator.comparingInt(cell -> cell.distanceTo(goal.getPosition()))));
    }

    /**
     * Steps to whichever free neighbouring cell is furthest from every visible threat.
     * Has no opinion when no threat is within sight.
     */
    public static MovementStrategy awayFrom(Predicate<Entity> threat, int vision) {
        return (mover, world) -> {
            List<Position> threats = world.entitiesNear(mover.getPosition(), vision)
                .filter(threat)
                .map(Entity::getPosition)
                .collect(Collectors.toList());
            if (threats.isEmpty()) {
                return Optional.empty();
            }
            ToIntFunction<Position> distanceToClosestThreat = cell -> threats.stream()
                .mapToInt(cell::distanceTo)
                .min()
                .orElse(Integer.MAX_VALUE);
            return bestFreeStep(mover, world,
                Comparator.comparingInt(distanceToClosestThreat).reversed());
        };
    }

    /**
     * The free neighbouring cell that comes first in the given order. Neighbours
     * arrive shuffled, so ties are broken at random rather than always favouring
     * the same direction.
     */
    private static Optional<Position> bestFreeStep(Animal mover, World world,
                                                   Comparator<Position> preference) {
        return world.neighboursOf(mover.getPosition()).stream()
            .filter(world::isEmpty)
            .min(preference);
    }
}

import java.util.Optional;

/**
 * Strategy pattern: how an animal decides where to step on its turn.
 *
 * <p>An Animal holds one of these and asks it for a step, instead of each species
 * hard-coding its own movement in an overridden method. That means movement can be
 * swapped without touching Rabbit or Fox, mixed and matched, and chosen at start-up
 * from the settings.</p>
 *
 * <p>Because it has exactly one abstract method, it is a functional interface, so
 * each concrete strategy is a lambda (see Movements) rather than a separate class.</p>
 */
@FunctionalInterface
public interface MovementStrategy {

    /**
     * Picks the empty neighbouring cell the animal should step into this turn.
     *
     * @return the chosen cell, or empty if this strategy has no opinion this turn
     *         (for example, a fleeing strategy when no predator is in sight)
     */
    Optional<Position> chooseStep(Animal mover, World world);

    /**
     * Combines two strategies: use this one, and only if it has no opinion, ask
     * the fallback. This is how simple strategies are built up into behaviours,
     * e.g. "flee foxes, otherwise head for grass, otherwise wander".
     */
    default MovementStrategy orElse(MovementStrategy fallback) {
        return (mover, world) -> chooseStep(mover, world)
            .or(() -> fallback.chooseStep(mover, world));
    }
}
